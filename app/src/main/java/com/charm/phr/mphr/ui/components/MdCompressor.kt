package com.charm.phr.mphr.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MdCompressor(context: Context, var maxWidth: Int = 612,
                   var maxHeight: Int = 816,
                   var compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
                   var quality: Int = 80,
                   var destinationFilePath: String) {


    @Throws(IOException::class)
    fun compressToFile(imageFile: File): String? {
        return compressToFile(imageFile, imageFile.name)
    }

    @Throws(IOException::class)
    private fun compressToFile(imageFile: File, compressedFileName: String): String? {
        return compressImage(imageFile, maxWidth, maxHeight, compressFormat, quality, destinationFilePath)
    }

    @Throws(IOException::class)
    fun compressToBitmap(imageFile: File): Bitmap? {
        return decodeSampledBitmapFromFile(imageFile, maxWidth, maxHeight)
    }

    //

    @Throws(IOException::class)
    private fun compressImage(imageFile: File, reqWidth: Int, reqHeight: Int, compressFormat: Bitmap.CompressFormat, quality: Int, destinationFileName: String): String? {

        var fileOutputStream: FileOutputStream? = null
        val file = File(destinationFileName)
        if (!file.exists()) {
            file.mkdirs()
        }

        var bitmap: Bitmap? = null

        try {

            fileOutputStream = FileOutputStream(file)
            // write the compressed bitmap at the destination specified by destinationPath.
            bitmap = decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight)
            bitmap?.compress(compressFormat, quality, fileOutputStream)

        } finally {

            if (fileOutputStream != null) {

                if (bitmap == null){
                    fileOutputStream.flush()
                    fileOutputStream.close()
                    return null
                } else {
                    fileOutputStream.flush()
                    fileOutputStream.close()
                }

            }

        }

        return file.path

    }

    @Throws(IOException::class)
    private fun decodeSampledBitmapFromFile(imageFile: File, reqWidth: Int, reqHeight: Int): Bitmap? {

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imageFile.absolutePath, options)

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false

        var scaledBitmap:Bitmap? = BitmapFactory.decodeFile(imageFile.absolutePath, options)

        //check the rotation of the image and display it properly
        val exif = androidx.exifinterface.media.ExifInterface(imageFile.absolutePath)
        val orientation = exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, 0)
        val matrix = Matrix()
        when (orientation) {
            6 -> matrix.postRotate(90f)
            3 -> matrix.postRotate(180f)
            8 -> matrix.postRotate(270f)
        }

        if (scaledBitmap != null)
        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
        else return null

        return scaledBitmap

    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {

        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }

        }

        return inSampleSize

    }

}
