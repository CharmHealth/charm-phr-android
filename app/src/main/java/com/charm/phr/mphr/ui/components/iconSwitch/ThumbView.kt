package com.charm.phr.mphr.ui.components.iconSwitch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi

internal class ThumbView : View {
    private var center: PointF? = null
    private var radius = 0f
    var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val cornerRadius = 12f

    private var bounds: RectF = RectF()
    init {
        paint.color = Color.GRAY
        center = PointF()
    }

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center!![w * 0.5f] = h * 0.5f
        radius = Math.min(w, h) * 0.5f
    }

    override fun onDraw(canvas: Canvas) {

        bounds.left = center!!.x - radius
        bounds.right = center!!.x + radius
        bounds.top = (center!!.y - radius)+ 2.5f
        bounds.bottom =(center!!.y + radius) + 3f
        canvas.drawRoundRect(bounds, cornerRadius,cornerRadius, paint)
    }

    var color: Int
        get() = paint.color
        set(color) {
            paint.color = color
            invalidate()
        }

}