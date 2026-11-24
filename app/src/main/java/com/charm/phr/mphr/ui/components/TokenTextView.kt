package com.charm.phr.mphr.ui.components

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class TokenTextView  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : AppCompatTextView(context, attrs, defStyleAttr) {


    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, -1)

    constructor(context: Context) : this(context, null, -1)



    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)

      /*  if (selected){
            setBackgroundResource(R.drawable.item_book_appointment_slot_selected_button)
            setTextColor(ContextCompat.getColor(context , android.R.color.white))
            setCompoundDrawablesWithIntrinsicBounds(0 ,0,R.drawable.ic_close_white,0)

            layoutParams?.width =   measuredWidth + resources.getDimensionPixelSize(R.dimen.member_chip_height)
            requestLayout()
            compoundDrawablePadding  = resources.getDimensionPixelSize(R.dimen.chip_drawable_padding)
        }
        else{
            setBackgroundResource(android.R.color.transparent)
            setTextColor(ContextCompat.getColor(context , android.R.color.black))
            setCompoundDrawablesWithIntrinsicBounds(0 ,0,0,0)
            compoundDrawablePadding  = 0

            layoutParams?.width =   measuredWidth - resources.getDimensionPixelSize(R.dimen.member_chip_height)
            requestLayout()
        }*/

    }
}