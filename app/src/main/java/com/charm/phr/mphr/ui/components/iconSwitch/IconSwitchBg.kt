package com.charm.phr.mphr.ui.components.iconSwitch

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.annotation.NonNull

internal class IconSwitchBg : Drawable() {
    private var bounds: RectF = RectF()
    private var paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var radius = 12f


    init {
          bounds = RectF()
          paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f

    }

    fun init(imageSize: Int, width: Int, height: Int) {
        val centerX = width * 0.5f
        val centerY = height * 0.5f
        val halfWidth = imageSize * 1.75f
        val halfHeight = imageSize
        bounds[centerX - halfWidth, (centerY - halfHeight)+2.5f, centerX + halfWidth] = centerY + halfHeight

    }

    override fun draw(@NonNull canvas: Canvas) {
        canvas.drawRoundRect(bounds, radius, radius, paint)
    }

    fun setColor(color: Int) {
        paint.color = color
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}

}
