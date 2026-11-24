package com.charm.phr.mphr.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.util.ModelUtil

class SimpleDividerItemDecoration(val context: Context) : RecyclerView.ItemDecoration() {

    private val mDivider : Drawable? = ContextCompat.getDrawable(context , R.drawable.recyclerview_divider)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val paddingValue = 10 /*if (context.resources.getBoolean(R.bool.isTenInchTablet)){
            20
        }
        else{
            10
        }*/

        val padding = ModelUtil.dpToPxConversion(paddingValue , context)
        val left = padding
        val right =parent.width - padding

        val  childCount = parent.childCount
        for ( i in 0 until childCount) {
            val child = parent.getChildAt(i)

            val params = child?.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + (mDivider?.intrinsicHeight ?: 0)
            mDivider?.setBounds(left, top, right, bottom)
            mDivider?.draw(c)
        }
    }

}