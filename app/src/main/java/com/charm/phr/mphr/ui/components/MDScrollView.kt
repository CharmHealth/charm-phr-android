package com.charm.phr.mphr.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class MDScrollView constructor(context: Context, attributeSet: AttributeSet) : ScrollView(context, attributeSet){

    private var mScrollable = true

    fun setScrollingEnable(enabled: Boolean){
        mScrollable = enabled
    }

    fun isScrollable() = mScrollable



    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return mScrollable && super.onTouchEvent(ev)

    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean = mScrollable && super.onInterceptTouchEvent(ev)
}
