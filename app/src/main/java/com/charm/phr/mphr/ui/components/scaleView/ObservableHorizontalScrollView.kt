package com.charm.phr.mphr.ui.components.scaleView

import android.content.Context
import android.widget.HorizontalScrollView
import androidx.annotation.Nullable


internal class ObservableHorizontalScrollView(context: Context,
                                              listener: ScrollChangedListener) : HorizontalScrollView(context) {






    private var mLastScrollUpdateMills: Long = -1

    @Nullable
    private var mScrollChangedListener: ScrollChangedListener? = null


    init {
        mScrollChangedListener = listener
    }

    private val mScrollerTask: Runnable = object : Runnable {
        override fun run() {
            if (System.currentTimeMillis() - mLastScrollUpdateMills > NEW_CHECK_DURATION) {
                mLastScrollUpdateMills = -1
                mScrollChangedListener?.onScrollStopped()
            } else {
                //Post next delay
                postDelayed(this, NEW_CHECK_DURATION)
            }
        }
    }

    override fun onScrollChanged(horizontalOrigin: Int,
                                 verticalOrigin: Int,
                                 oldHorizontalOrigin: Int,
                                 oldVerticalOrigin: Int) {
        super.onScrollChanged(horizontalOrigin, verticalOrigin, oldHorizontalOrigin, oldVerticalOrigin)
        mScrollChangedListener?.onScrollChanged()
        if (mLastScrollUpdateMills == -1L) postDelayed(mScrollerTask, NEW_CHECK_DURATION)
        mLastScrollUpdateMills = System.currentTimeMillis()
    }

    /**
     * Listener to get callbacks on scrollview scroll events.
     */
    internal interface ScrollChangedListener {
        /**
         * Called upon change in scroll position.
         */
        fun onScrollChanged()

        /**
         * Called when the scrollview stops scrolling.
         */
        fun onScrollStopped()
    }

    companion object {
        private const val NEW_CHECK_DURATION = 100L
    }

    /**
     * Constructor.
     *
     * @param context  [Context] of caller.
     * @param listener [ScrollChangedListener] to get callbacks when scroll starts or stops.
     * @see ScrollChangedListener
     */
}