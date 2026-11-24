package com.charm.phr.mphr.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Interpolator
import android.widget.Scroller

class MDViewPagerCustomDuration : androidx.viewpager.widget.ViewPager {

    private var mScroller: ScrollerCustomDuration? = null

    constructor(context: Context) : super(context) {
        postInitViewPager()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        postInitViewPager()
    }

    /**
     * Override the Scroller instance with our own class so we can change the
     * duration
     */
    private fun postInitViewPager() {
        try {
            val scroller = androidx.viewpager.widget.ViewPager::class.java.getDeclaredField("mScroller")
            scroller.isAccessible = true
            val interpolator = androidx.viewpager.widget.ViewPager::class.java.getDeclaredField("sInterpolator")
            interpolator.isAccessible = true

            mScroller = ScrollerCustomDuration(context,
                    interpolator.get(null) as Interpolator)
            scroller.set(this, mScroller)
        } catch (e: Exception) {
        }

    }

    /**
     * Set the factor by which the duration will change
     */
    fun setScrollDurationFactor(scrollFactor: Double) {
        mScroller!!.setScrollDurationFactor(scrollFactor)
    }

}

class ScrollerCustomDuration(context: Context, interpolator: Interpolator) : Scroller(context, interpolator) {

    var scrollFactor = 1.0

    /**
     * Set the factor by which the duration will change
     */
    fun setScrollDurationFactor(scrollFactor: Double) {
        this.scrollFactor = scrollFactor
    }

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, (duration * scrollFactor).toInt())
    }

}