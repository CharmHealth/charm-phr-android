package com.charm.phr.mphr.util

import android.graphics.Bitmap
import com.squareup.picasso.Transformation

class ResizeTransformation(private val maxSize: Int) : Transformation {
    override fun transform(source: Bitmap?): Bitmap? {
        if (source == null) return null

        var width = source.width
        var height = source.height

        val bitmapRatio = width.toFloat() / height.toFloat()

        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }

        val result = Bitmap.createScaledBitmap(source, width, height, true)

        if (result != source) {
            source.recycle()

        }

        return result
    }


    override fun key() = "resize()"
}
