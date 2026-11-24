package com.charm.phr.mphr.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import com.charm.phr.mphr.R
import com.google.android.material.textfield.TextInputLayout

class MDTextInputLayout constructor(context: Context, attributeSet: AttributeSet?) : TextInputLayout(context, attributeSet) {


    constructor(context: Context):this(context , null)

    private var canvasWidth = 0f
    private var canvasHeight = 0f


    private val paint = Paint()

    private val path = Path()

    private val fillPaint =  Paint(Paint.ANTI_ALIAS_FLAG)


    private var verticalSpacing = 30f
    private var endSpacing = 100f

    private var showDropDown = true

    private var showLine = true

    init {
        paint.color = Color.parseColor("#33000000")
        paint.strokeWidth = 2f

        fillPaint.strokeWidth = 2f
        fillPaint.color = Color.BLACK
        fillPaint.style = Paint.Style.FILL_AND_STROKE
        fillPaint.isAntiAlias = true

        path.fillType = Path.FillType.EVEN_ODD

        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.MDTextInputLayout)
        showDropDown = typedArray.getBoolean(R.styleable.MDTextInputLayout_mdShowDropdown , true)
        showLine = typedArray.getBoolean(R.styleable.MDTextInputLayout_mdShowLine , true)
        verticalSpacing = typedArray.getFloat(R.styleable.MDTextInputLayout_mdVerticalSpacing , verticalSpacing)
        endSpacing = typedArray.getFloat(R.styleable.MDTextInputLayout_mdEndSpacing , endSpacing)

        typedArray.recycle()

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasWidth = w.toFloat()
        canvasHeight = h.toFloat()
    }

    fun setShowDropDown(showDropDown: Boolean){
        this.showDropDown = showDropDown
        invalidate()
    }

    fun setShowLine(showLine: Boolean){
        this.showLine = showLine
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        isFocusable = false
        isClickable = true


        if (isEnabled) {

            if (showDropDown) {

                path.moveTo(canvasWidth - ( (endSpacing/2) + 10), (canvasHeight / 2) - 5)
                //  path.moveTo(canvasWidth - 57.5f, (canvasHeight/2) + 5)
                path.lineTo(canvasWidth - ( (endSpacing/2) - 10), (canvasHeight / 2) - 5)
                //  path.moveTo(canvasWidth - 43.5f, (canvasHeight/2) + 5)
                path.lineTo(canvasWidth - (endSpacing/2), (canvasHeight / 2) + 5)
                path.lineTo(canvasWidth -( (endSpacing/2) + 10), (canvasHeight / 2) - 5)

                path.close()

                canvas?.drawPath(path, fillPaint)
            }

            if (showLine) //Temporarily added 30F later convert 4 dp tp px
                canvas?.drawLine(canvasWidth - endSpacing - 30F, verticalSpacing, canvasWidth - endSpacing - 30F, canvasHeight - verticalSpacing, paint)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        invalidate()
    }


}