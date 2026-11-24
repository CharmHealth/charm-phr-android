package com.charm.phr.mphr.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.ImageView
import com.charm.phr.mphr.R

@SuppressLint("AppCompatCustomView")
class MDImageView : ImageView {
    private var mRadius = 0f
    private var mColor: Int = Color.WHITE
    private var mPaint: Paint? = null
    private var mPath: Path? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
        val a: TypedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MDImageView,
                0, 0)
        try {
            setRadius(a.getDimension(R.styleable.MDImageView_mdImageViewRadius, 0f))
        } finally {
            a.recycle()
        }
    }

    private fun init() {
        setRadius(mRadius)
    }


    private fun setRadius(radius: Float) {
        mRadius = radius
        val r = RectF(0f, 0f ,2 * mRadius, 2 * mRadius)
        mPath = Path()
        mPath?.moveTo(0f, 0f)
        mPath?.lineTo(0f, mRadius)
        mPath?.arcTo(r, 180f, 90f)
        mPath?.lineTo(0f, 0f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {

        /*This just draws 4 little inverted corners */

        if (mPath != null && mPaint != null) {
            val path  = mPath!!
            val paint = mPaint!!
            val w = getWidth().toFloat()
            val h = getHeight().toFloat()
            canvas.drawPath(path, paint)
            canvas.save()
            canvas.translate(w, 0f)
            canvas.rotate(90f)
            canvas.drawPath(path, paint)
            canvas.restore()
            canvas.save()
            canvas.translate(w, h)
            canvas.rotate(180f)
            canvas.drawPath(path, paint)
            canvas.restore()
            canvas.translate(0f, h)
            canvas.rotate(270f)
            canvas.drawPath(path, paint)
        }
    }
}