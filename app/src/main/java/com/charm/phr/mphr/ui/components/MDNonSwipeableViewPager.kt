package com.charm.phr.mphr.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class MDNonSwipeableViewPager constructor(context: Context, attributeSet: AttributeSet) : ViewPager(context, attributeSet) {

    override fun onTouchEvent(ev: MotionEvent?): Boolean = false

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean = false

}