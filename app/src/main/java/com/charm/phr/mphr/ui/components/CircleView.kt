package com.charm.phr.mphr.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import com.charm.phr.mphr.R

class CircleView : View {

    private var mPaint: Paint? = null
    private var defaultColor = ContextCompat.getColor(context.applicationContext, R.color.passcodeBulletin)
    private var givenColor = defaultColor
    private var isColorVisible = true
    var text: String? = null
    var textSize: Float? = null
    var textColor: Int? = null

    var textFamily: String? = null
    var color = defaultColor
        set(color) {

            field = color
            invalidate()
        }

    constructor(context: Context) : super(context) {
        init()
        initColor(context, null)

    }


    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        init()
        initColor(context , attrs)
    }

    constructor(context: Context, @Nullable attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
        initColor(context , attrs)

    }

    private fun init() {
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    private fun initColor(context: Context , attrs: AttributeSet?){
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.CircleView)

            givenColor = typedArray.getColor(R.styleable.CircleView_circleViewBgColor, color)
            isColorVisible = typedArray.getBoolean(R.styleable.CircleView_isColorVisible, true)
            text = typedArray.getString(R.styleable.CircleView_text)
            textSize = typedArray.getDimension(R.styleable.CircleView_textViewSize, 0F)
            textColor = typedArray.getColor(R.styleable.CircleView_textViewColor, 0)

            textFamily = typedArray.getString(R.styleable.CircleView_textFamily)

            color = if (isColorVisible) {
                givenColor
            } else {
                defaultColor
            }

            typedArray.recycle()
        }

    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        val width = (width - paddingLeft - paddingRight) * 0.5
        val height = (height - paddingTop - paddingBottom) * 0.5
        val cx = (paddingLeft + width).toFloat()
        val cy = (paddingTop + height).toFloat()
        val radius = Math.min(width, height).toFloat()
        mPaint!!.color = this.color
        canvas.drawCircle(cx, cy, radius, mPaint!!)

        if (text.isNullOrBlank()) return

        if (text != null) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = textColor ?: Color.BLACK
            paint.textSize = textSize ?: 0F
            paint.textAlign = Paint.Align.CENTER
            paint.isAntiAlias = true

            val currentTypeFace: Typeface = Typeface.DEFAULT
            val bold = Typeface.create(currentTypeFace, Typeface.BOLD)

            if (textFamily.isNullOrBlank()){
                paint.typeface = bold
            }
            else{
                try {
                    val selectedTypeface =   Typeface.create(textFamily, Typeface.NORMAL)
                    if (selectedTypeface != null){
                        paint.typeface = selectedTypeface
                    }
                    else{
                        paint.typeface = bold
                    }
                }
                catch (e: Exception){
                    paint.typeface = bold
                }
            }


            val xPos = canvas.width / 2
            val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2).toInt()
            //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.
            //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.
            canvas.drawText(text!!, xPos.toFloat(), yPos.toFloat(), paint)
        }

    }

    fun showColor(){
        if(!isColorVisible){
            isColorVisible = true
            color = givenColor
            invalidate()

        }
    }
    fun hideColor(){
        if(isColorVisible){
            isColorVisible = false
            color = defaultColor
            invalidate()
        }
    }

    fun toggleColor(){
        if (isColorVisible){
            hideColor()
        }
        else {
            showColor()
        }
    }

    fun setCircleColor(color: Int) {
        givenColor = color
        this.color = color
        showColor()
    }

    fun setTextValue(string: String?) {
        text ?: return
        text = string
        invalidate()
    }

    fun setTextColorValue(color: Int?) {
        color ?: return
        textColor = color
        invalidate()
    }

    fun getGivenColor() = givenColor
}