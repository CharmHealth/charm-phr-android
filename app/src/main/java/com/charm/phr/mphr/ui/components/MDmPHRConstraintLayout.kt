package com.charm.phr.mphr.ui.components

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.charm.phr.mphr.R

class MDmPHRConstraintLayout constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr) {


    constructor(context: Context,attrs: AttributeSet?):this(context,attrs , -1)

    constructor(context: Context):this(context , null , -1)


    private var mdStrokeColor = ContextCompat.getColor(context, R.color.mdDefaultStrokeColor)
    private var mdCornerRadius = context.resources.getDimension(R.dimen.md_default_stroke_radius)

    private var mdBgColor = ContextCompat.getColor(context , android.R.color.white)

    init {

        val typedArray = context.obtainStyledAttributes(attrs , R.styleable.MDmPHRConstraintLayout)

        this.mdStrokeColor = typedArray.getColor(R.styleable.MDmPHRConstraintLayout_mdStrokeColor ,mdStrokeColor)
        this.mdCornerRadius = typedArray.getDimension(R.styleable.MDmPHRConstraintLayout_mdCornerRadius ,mdCornerRadius)
        this.mdBgColor = typedArray.getColor(R.styleable.MDmPHRConstraintLayout_mdBackgroundColor ,mdBgColor)


        typedArray.recycle()

        renderView()

    }

    private fun renderView(){

//        val outerRadius = floatArrayOf(mdCornerRadius, mdCornerRadius, mdCornerRadius, mdCornerRadius, mdCornerRadius, mdCornerRadius, mdCornerRadius, mdCornerRadius)
//
//
//
//        val shapeDrawable = ShapeDrawable()
//        shapeDrawable.paint.style = Paint.Style.STROKE
//        shapeDrawable.paint.strokeWidth = 5f
//        shapeDrawable.paint.color = mdStrokeColor
//
//        val shapePadding = 10
//        shapeDrawable.setPadding(shapePadding,shapePadding,shapePadding,shapePadding)
//
//        val whiteShapeDrawable = ShapeDrawable()
//        whiteShapeDrawable.paint.color =  ContextCompat.getColor(context , android.R.color.white)
//
//        setLayerType(View.LAYER_TYPE_SOFTWARE, shapeDrawable.paint)
//
//        shapeDrawable.shape = RoundRectShape(outerRadius, null, null)
//        whiteShapeDrawable.shape = RoundRectShape(outerRadius +5f, null, null )
//
//        background = LayerDrawable(arrayOf<Drawable>( shapeDrawable , whiteShapeDrawable ))

        val outerRadius = floatArrayOf(mdCornerRadius, mdCornerRadius, mdCornerRadius, mdCornerRadius, mdCornerRadius, mdCornerRadius, mdCornerRadius, mdCornerRadius)

        setBackgroundResource(R.drawable.md_mphr_constraint_bg)

        val gradientDrawable = background as GradientDrawable

        gradientDrawable.setStroke( 2,mdStrokeColor)
        gradientDrawable.setColor(mdBgColor)
        gradientDrawable.cornerRadii = outerRadius
    }


    fun setStrokeColor(@ColorRes strokeColor: Int){
        this.mdStrokeColor = ContextCompat.getColor(context, strokeColor)
        renderView()
    }

    fun setBgColor(@ColorRes bgColor: Int){
        this.mdBgColor = ContextCompat.getColor(context , bgColor)
        renderView()
    }
}