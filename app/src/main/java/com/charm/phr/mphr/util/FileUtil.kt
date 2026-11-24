package com.charm.phr.mphr.util

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.model.MessagesAttachmentsMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.joda.time.DateTime
import java.io.*
import java.nio.channels.FileChannel
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class FileUtil {

    enum class FileType { TEMP}
    enum class FileExtension { PDF, IMG }

    companion object {

        private const val TAG = "FileUtil"
        private val PATTERN: Pattern = Pattern.compile("(.*?)(?:\\((\\d+)\\))?(\\.[^.]*)?")

        /**
         * File Name String for image files
         */
        fun getImageFileNameStamp(extension: String = "jpg"): String = " IMG ${DateTime.now().toString(ModelUtil.timeStapStringForFile())}.$extension"


        /**
         * File Name String for pdf files
         */
        private fun getPdfFileNameStamp(): String = " PDF ${DateTime.now().toString(ModelUtil.timeStapStringForFile())}.pdf"


        fun getFolderPathAsSting(context: Context, folderName: String) =
            context.filesDir.path + "/" + folderName


        fun saveMessageAttachment(mdFile: MDFile, attachmentsMap: MessagesAttachmentsMap, context: Context): File? {

            val attachmentNameArr = (attachmentsMap.attachmentName ?: "").split(".")

            val extension = attachmentNameArr[attachmentNameArr.size - 1]

            val fileName = "${ModelUtil.moduleNameMessage} ${attachmentsMap.messageId}_${attachmentsMap.attachmentId} ${extension.uppercase(Locale.ENGLISH)} ${
                DateTime.now().toString(ModelUtil.timeStapStringForFile())
            }.${extension}"

            val filePath = getFolderPathAsSting(context, ModelUtil.folderTemp)
            val pdfFile = File(filePath, fileName)

            FileOutputStream(pdfFile).buffered().use { fos ->

                fos.write(mdFile.responseBody!!.bytes())
                fos.flush()
                fos.close()

            }

            return File(pdfFile.path)
        }


        fun getMessageAttachment(context: Context, attachmentsMap: MessagesAttachmentsMap): File? {


            val dirPath = getFolderPathAsSting(context, ModelUtil.folderTemp)

            val folderTemp = File(dirPath)

            if (folderTemp.isDirectory) {
                val fileList = folderTemp.listFiles { file, fileName ->
                    fileName?.contains("${attachmentsMap.messageId}_${attachmentsMap.attachmentId}") == true
                }

                if (fileList != null) {
                    if (fileList.isNotEmpty()) {
                        return fileList[0]
                    }
                }
            }
            return null
        }

        fun savePdfFromResponseBody(mdFile: MDFile, context: Context, moduleName: String): File? {

            val fileName = getPdfFileNameStamp()
            return saveFileFromResponseBody(mdFile, context, fileName)
        }

        fun saveFileFromResponseBody(mdFile: MDFile, context: Context, fileName: String): File? {
            val filePath = getFolderPathAsSting(context, ModelUtil.folderTemp)
            val file = File(filePath, fileName)

            FileOutputStream(file).buffered().use { fos ->

                fos.write(mdFile.responseBody!!.bytes())
                fos.flush()
                fos.close()

            }

            return File(file.path)
        }


        fun saveImageFromBitmap(bitmap: Bitmap, context: Context, moduleName: String): File? {
            val fileName = moduleName + getImageFileNameStamp("png")
            val filePath = getFolderPathAsSting(context, ModelUtil.folderTemp)
            val pdfFile = File(filePath, fileName)

            FileOutputStream(pdfFile).buffered().use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()
            }

            return File(pdfFile.path)
        }


        fun saveDocumentFromResponseBody(mdFile: MDFile, context: Context, moduleName: String): File? {

            val fileName = moduleName + if (mdFile.extension == "pdf") {
                getPdfFileNameStamp()
            } else {
                getImageFileNameStamp(mdFile.extension ?: "")
            }

            val filePath = getFolderPathAsSting(context, ModelUtil.folderTemp)
            val pdfFile = File(filePath, fileName)

            FileOutputStream(pdfFile).buffered().use { fos ->

                fos.write(mdFile.responseBody!!.bytes())
                fos.flush()
                fos.close()

            }

            return File(pdfFile.path)
        }

        fun createNewFile(fileType: FileType, fileExtn: FileExtension, context: Context): File {

            val fileNameStamp = getRespectiveNameStamp(fileExtn)
            val folderPath = getFolderPathAsSting(context, getFolderName(fileType))
            val file = File(folderPath, fileNameStamp)
            file.createNewFile()
            return file

        }

        fun getRespectiveNameStamp(fileExtn: FileExtension): String {
            return when (fileExtn) {
                FileExtension.PDF -> getPdfFileNameStamp()
                else -> getImageFileNameStamp()
            }
        }

        fun getFolderName(fileType: FileType): String {
            return /*when (fileType) {
                FileType.PAPERCHART -> ModelUtil.folderPaperChartPDF
                FileType.IMG -> ModelUtil.folderPatientImg
                FileType.LAB_RESULTS -> ModelUtil.folderLabResults
                else ->*/ ModelUtil.folderTemp

        }

        fun deleteSwaasaFiles(context: Context) {
            val tempDir = File(context.filesDir, ModelUtil.folderSwaasa)
            if (tempDir.exists()) {
                deleteChildInDirectory(tempDir)
            } else {
                tempDir.mkdir()
            }
        }

        fun getSwaasaFile(context: Context): File? {

            val tempDir = File(context.filesDir, ModelUtil.folderSwaasa)
            if (tempDir.exists()) {

                val fileList = tempDir.list()

                if (fileList?.isNotEmpty() == true) {
                    val file = File(tempDir, fileList[0])
                    if (file.exists()) {
                        return file
                    }
                }
            }

            return null
        }


        fun getFileChooserIntent(): Intent {
            val mimeTypes = arrayOf("image/*", "application/pdf")
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = if (mimeTypes.size == 1) mimeTypes[0] else "*/*"
            if (mimeTypes.isNotEmpty()) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
            return intent
        }

        fun getFileName(uri: Uri, context: Context): String? {
            var result: String? = null
            if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                } finally {
                    cursor?.close()
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result!!.lastIndexOf('/')
                if (cut != -1) {
                    result = result.substring(cut + 1)
                }
            }
            return result
        }

        fun getMimeType(context: Context, uri: Uri): String? {
            //Check uri format to avoid null
            val extension: String? = if (uri.scheme == ContentResolver.SCHEME_CONTENT) { //If scheme is a content
                val mime = MimeTypeMap.getSingleton()
                mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
            } else { //If scheme is a File
                //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
                MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path)).toString())
            }
            return extension
        }

        fun saveAttachment(uri: Uri, extension: String, context: Context): String? {

            val filePath = if (extension.equals("pdf", true)) {
                savePdfFromUri(uri, context, ModelUtil.folderTemp)
            } else {
                createImageFileFromUri(uri, context, ModelUtil.folderTemp, extension)
            }

            return filePath
        }

        fun createUriFromFilePath(filePath: String?): Uri? {

            filePath ?: return null

            val file = File(filePath)
            if (!file.exists()) return null

            return Uri.fromFile(file)

        }

        fun createImageFileFromBitmap(context: Context, bitmap: Bitmap?, fileType: FileType): String? {

            bitmap ?: return null

            val outPutFile = createNewFile(fileType, FileExtension.IMG, context)

            FileOutputStream(outPutFile.path).let { fos ->

                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()

            }

            return outPutFile.path
        }

        /**
         * @param  customFileName when file Name and Extension are given fileExtension (not needed in customFileName flow)
         * @param  fileExtension To get auto generated file name with extension & timestamp (not needed in non-customFileName flow)
         */
        fun createFileFromUri(
            uri: Uri, context: Context, folderName: String, fileExtension: FileExtension?, allowCompression: Boolean = false,
            customFileName: String = "",
        ): String? {

            try {
                Log.d(TAG, "createImageFileFromUri")

                val folderPath = getFolderPathAsSting(context, folderName)
                val outputFile = if (customFileName.isNotBlank()) {
                    File(folderPath, customFileName)
                } else {
                    val fileNameStamp = getRespectiveNameStamp(fileExtension ?: FileExtension.IMG)
                    File(folderPath, fileNameStamp)
                }

                val inputStream = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(outputFile.path)
                inputStream.use { input ->
                    outputStream.buffered().use { fileOut ->
                        input ?: return null
                        input.copyTo(fileOut)
                    }
                }

                outputStream.flush()
                outputStream.close()

//                if (allowCompression) { //Only used for image files
//                    val tempFile = createNewFile(FileType.TEMP, FileExtension.IMG, context)
//                    try {
//                        val mdCompressor = MdCompressor(
//                            context = context,
//                            maxWidth = 1080, maxHeight = 1280,
//                            quality = 95,
//                            compressFormat = Bitmap.CompressFormat.JPEG,
//                            destinationFilePath = tempFile.absolutePath
//                        )
//                        Log.d(TAG, "createImageFileFromUri outPutFile after compression :${outputFile.length() / 1000000F}Mb")
//                        val cmPath = mdCompressor.compressToFile(outputFile)
//                        tempFile.copyTo(outputFile, overwrite = true)
//                        tempFile.delete()
//                        if (cmPath == null) return null
//                        return outputFile.absolutePath
//                    } catch (e: IOException) {
//                        return null
//                    }
//                }

                Log.d(TAG, "createFileFromUri ${outputFile.path}")

                return outputFile.path
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                return null
            }

        }


        fun createImageFileFromUri(uri: Uri, context: Context, folderName: String, extension: String /*, allowCompression: Boolean = false*/): String? {


            val path = getFolderPathAsSting(context, folderName)

            val outPutFile = File(path, getImageFileNameStamp(extension))

            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(outPutFile.path)

            if (inputStream != null) {

                inputStream?.use { input ->

                    outputStream.buffered().use { fileOut ->
                        input ?: return null
                        input.copyTo(fileOut)
                    }

                }

                outputStream.flush()
                outputStream.close()

                /*if (allowCompression) {

                        val tempFile = createNewFile(FileType.TEMP, FileExtension.IMG, context)

                        try {

                            val mdCompressor = MdCompressor(
                                    context = context,
                                    maxWidth = 1080, maxHeight = 1280,
                                    quality = 95,
                                    compressFormat = Bitmap.CompressFormat.JPEG,
                                    destinationFilePath = tempFile.absolutePath)

                            Log.d(TAG, "createImageFileFromUri outPutFile after compression :${outPutFile.length() / 1000000F}Mb")

                            val cmPath = mdCompressor.compressToFile(outPutFile)

                            tempFile.copyTo(outPutFile, overwrite = true)

                            tempFile.delete()

                            if(cmPath == null) return null

                            return outPutFile.absolutePath

                        } catch (e: IOException) {
                            return null
                        }

                    }*/

            } else return null



            return outPutFile.path

        }

        fun savePdfFromUri(uri: Uri, context: Context, moduleName: String): String? {

            val fileName = moduleName + getPdfFileNameStamp()
            val filePath = getFolderPathAsSting(context, ModelUtil.folderTemp)

            val pdfFile = File(filePath, fileName)

            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(pdfFile)

            if (inputStream != null) {

                inputStream.use { input ->
                    outputStream.buffered().use { fileOut ->
                        input ?: return null
                        input.copyTo(fileOut)
                    }
                }

            } else return null

            outputStream.flush()
            outputStream.close()

            return pdfFile.path
        }

        fun renameFile(filePath: String?, newName: String): File {

            val file = File(filePath ?: "")

            val parent = file.parentFile
            val newFile = File(parent, newName + "." + file.extension)
            file.renameTo(newFile)

            return newFile
        }

        fun readFileToString(filePath: String): String? {

            val file = File(filePath)
            var fis: FileInputStream? = null
            // Creating a byte array using the length of the file
            // file.length returns long which is cast to int
            val bArray = ByteArray(file.length().toInt())
            try {
                fis = FileInputStream(file)
                fis.read(bArray)
                fis.close()
            } catch (ioExp: IOException) {
                ioExp.printStackTrace()
            }
            val encodedString: String =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Base64.getEncoder().encodeToString(bArray);
                } else {
                    android.util.Base64.encodeToString(bArray, android.util.Base64.DEFAULT)
                }

            return encodedString
        }

        fun createRequestBodyForFile(filePath: String): RequestBody? {
            val file = File(filePath)
            return createRequestBodyForFile(file)
        }

        fun createRequestBodyForFile(file: File): RequestBody? {

            if (!file.exists()) return null


            val fileName = file.name ?: ""
            val typeStr = if (fileName.endsWith("pdf")) "pdf/*"
            else if (fileName.endsWith("wav")) "audio/wav"
            else if (fileName.endsWith("png")) "image/png"
            else "image/*"

            return if (typeStr.isNotBlank())
                RequestBody.create(typeStr.toMediaTypeOrNull(), file)/*, file.name)*/
            else null

        }

        fun StringToBitMap(image: String?): Bitmap? {
            return try {
                val encodeByte: ByteArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Base64.getDecoder().decode(image);
                } else {
                    android.util.Base64.decode(image, android.util.Base64.DEFAULT)
                }
                val inputStream: InputStream = ByteArrayInputStream(encodeByte)
                BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                e.message
                null
            }
        }

        fun deleteAllFilesInDirectory(context: Context, folderPath: String) {

            val path = getFolderPathAsSting(context, folderPath)
            val dir = File(path)
            deleteChildInDirectory(dir)

        }

        fun deleteChildInDirectory(dir: File) {

            //Dir wont be deleted and should not be

            if (dir.isDirectory) {

                val childList = dir.list()
                for (i in childList.indices) {

                    val file = File(dir, childList[i])
                    if (file.exists()) deleteFile(file)

                }
            }
        }

        private fun deleteFile(file: File) {

            if (!file.exists()) return

            file.delete()
        }

        //inSampleSize should be power of 2
        fun getBitmapFromFile(filePath: String?, inSampleSize: Int? = null): Bitmap? {

            filePath ?: return null

            val file = File(filePath)

            if (!file.exists()) return null

            val factOptions = BitmapFactory.Options()

            inSampleSize?.let { factOptions.inSampleSize = inSampleSize }


            val bm = BitmapFactory.decodeFile(filePath, factOptions) ?: return null

            val exif = androidx.exifinterface.media.ExifInterface(filePath)
            val rotation =
                exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL)
            val rotationInDegrees = exifToDegrees(rotation).toFloat()
            val matrix = Matrix()
            if (rotation != 0) {
                matrix.preRotate(rotationInDegrees)
            }

            return Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)

        }

        private fun exifToDegrees(exifOrientation: Int): Int {
            return when (exifOrientation) {
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }

        fun convertStreamToString(isInputStream: InputStream?): String {
            val reader = BufferedReader(InputStreamReader(isInputStream))
            val sb = StringBuilder()
            var line: String? = null
            while (reader.readLine().also({ line = it }) != null) {
                sb.append(line).append("\n")
            }
            reader.close()
            return sb.toString()
        }

        fun getStringFromFile(filePath: String?): String? {
            val fl = File(filePath)
            val fin = FileInputStream(fl)
            val ret = convertStreamToString(fin)
            //Make sure you close all streams.
            fin.close()
            return ret
        }


        fun copyFile(appContext: Context, srcPath: String?, destFolderName: String, tempFileName: String? = null): File? {

            val src = File(srcPath ?: "")

            Log.d("mdemo", getLocalPath(appContext) ?: "")
            Log.d("mdemo", appContext.getExternalFilesDir(null)?.getAbsolutePath() ?: "")
            //  Log.d("mdemo" ,     Environment.getExternalStorageDirectory().path)

            val dstPath: String = getLocalPath(appContext) + File.separator.toString() + appContext.getString(R.string.app_name) + File.separator
            val dst = File(dstPath)

            //if folder does not exist
            if (!dst.exists()) {
                if (!dst.mkdir()) {
                    return null
                }
            }

            val destFolder = File(dst, destFolderName)

            //if folder does not exist
            if (!destFolder.exists()) {
                if (!destFolder.mkdir()) {
                    return null
                }
            }

            var fileName = if (tempFileName != null) {
                tempFileName
            } else {
                val timeStamp: String = DateTime.now().toString("yyyyMMdd_HHmmss")
                "${src.extension.toString().uppercase()}_" + timeStamp + ".${src.extension}"
            }

            fileName = getNewName(fileName, destFolder.path ?: "") ?: ""

            val expFile = File(destFolder.path + File.separator.toString() + fileName)
            var inChannel: FileChannel? = null
            var outChannel: FileChannel? = null
            try {
                inChannel = FileInputStream(src).channel
                outChannel = FileOutputStream(expFile).channel
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            try {
                inChannel?.transferTo(0, inChannel.size(), outChannel)
            } finally {
                if (inChannel != null) inChannel.close()
                if (outChannel != null) outChannel.close()
            }
            return expFile
        }

        private fun getLocalPath(appContext: Context): String? {
            return File(
                File(
                    File(
                        File(appContext.getExternalFilesDir(null)?.parent ?: "").parent
                            ?: ""
                    ).parent ?: ""
                ).parent ?: ""
            ).path
        }

        fun getNewName(filename: String, folderPath: String): String? {
            var filename = filename
            if (fileExists(filename, folderPath)) {
                val m: Matcher = PATTERN.matcher(filename ?: "")
                if (m.matches()) {
                    val prefix: String = m.group(1)
                    val last: String = m.group(2)
                    var suffix: String = m.group(3)
                    if (suffix == null) suffix = ""
                    var count = last?.toInt() ?: 0
                    do {
                        count++
                        filename = "$prefix($count)$suffix"
                    } while (fileExists(filename, folderPath))
                }
            }
            return filename
        }

        private fun fileExists(filename: String, folderPath: String): Boolean {

            val file = File(folderPath, filename)
            return file.exists()
        }

        fun getIconForFileType(fileType: String): Int {
            when (fileType) {
                ModelUtil.imageFileTypePNG, ModelUtil.imageFileTypeJPEG, ModelUtil.imageFileTypeJPG -> {
                    return R.drawable.ic_image
                }
                ModelUtil.fileTypePDF -> {
                    return R.drawable.ic_pdf
                }
                else -> {
                    return R.drawable.ic_image
                }
            }
        }

        fun getFileSizeFromURI(uri: Uri, context: Context): Long {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                return (cursor.getLong(sizeIndex) / 1024) / 1024
            }
            return 0
        }

        fun isPasswordProtectedFile(filePath: String?): Boolean {
            filePath ?: return false

            return try {
                val parcelFileDescriptor = ParcelFileDescriptor.open(File(filePath), ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(parcelFileDescriptor)
                false
            } catch (e: SecurityException) {
                true
            } catch (e: java.lang.Exception) {
                Log.d(TAG, e.toString())
                true
            }
        }

        fun getRotatedBitmapAndSave(
            context: Context,
            uri: Uri,
            fileType:FileType = FileType.TEMP
        ): Pair<Bitmap?, String?> {
            try {
                // Decode bitmap from URI
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap == null) return Pair(null, null)

                // Read EXIF orientation
                val exifInputStream = context.contentResolver.openInputStream(uri)
                val exif = exifInputStream?.let { androidx.exifinterface.media.ExifInterface(it) }
                exifInputStream?.close()
                val orientation = exif?.getAttributeInt(
                    androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
                ) ?: androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL

                val matrix = Matrix()
                when (orientation) {
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                }

                val rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )

                // Save rotated bitmap to file
                val rotatedFilePath =
                   createImageFileFromBitmap(
                        context,
                        rotatedBitmap,
                        fileType
                    )
                return Pair(rotatedBitmap, rotatedFilePath)
            } catch (e: Exception) {
                e.printStackTrace()
                return Pair(null, null)
            }
        }

    }

}


