package com.charm.phr.mphr.util

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.charm.phr.mphr.R
import com.charm.phr.mphr.util.MDBaseActivity.Companion.isPassCodeAllowed
import kotlin.reflect.KFunction1


object PermissionUtil {

    private const val isFirstTimeForCameraPermission = "isFirstTimeForCameraPermission"
    private const val isFirstTimeForWriteStoragePermission = "isFirstTimeForWriteStoragePermission"
    private const val isFirstTimeForRecordAudioPermission = "isFirstTimeForRecordAudioPermission"
    private const val isCameraPermissionDenied = "isCameraPermissionDenied"
    private const val isWriteStoragePermissionDenied = "isWriteStoragePermissionDenied"
    private const val isRecordAudioPermissionDenied = "isRecordAudioPermissionDenied"
    const val isNotificationPermissionDenied = "isNotificationPermissionDenied"

    /**
     * Methods Checks Whether the permissions are allowed or not in application
     * Even if single permission not allowed return false
     */
    fun checkPermission(context: Context, permissions: Array<String>): Boolean {
        var allPermitted = false
        for (permission in permissions) {
            allPermitted = (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED)
            if (!allPermitted) break
        }
        return allPermitted
    }

    /**
     * Methods Checks for WRITE_EXTERNAL_STORAGE
     * WRITE_EXTERNAL_STORAGE is not required from android 13
     */
    private fun hasWriteExternalStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkPermission(context, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
        } else checkPermission(context, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    /**
     * Methods Checks for READ_EXTERNAL_STORAGE
     * READ_EXTERNAL_STORAGE is not required from android 13
     * READ_MEDIA_VIDEO and READ_MEDIA_IMAGES are used instead of READ_EXTERNAL_STORAGE from 13
     */
    private fun hasVideoImageReadStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(
                context,
                arrayOf(
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkPermission(context, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        } else checkPermission(context, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
    }

    private fun shouldShowRequestPermissionRationale(
        context: Activity,
        permissions: Array<String>
    ): Boolean {
        return permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(context, it)
        }
    }

    private fun setPermissionDenied(context: Activity, deniedPermissions: Array<String>) {
        val pref = PreferenceManager.getDefaultSharedPreferences(context).edit()
        deniedPermissions.forEach { permission ->
            val deniedModelUtilPerm = getDeniedParam(permission)
            pref.putBoolean(deniedModelUtilPerm, true)
        }
        pref.commit()
    }

    private fun getDeniedParam(permission: String): String {
        return when (permission) {
            Manifest.permission.CAMERA -> {
                isCameraPermissionDenied
            }

            Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                isWriteStoragePermissionDenied
            }

            Manifest.permission.POST_NOTIFICATIONS -> {
                isNotificationPermissionDenied
            }

            Manifest.permission.RECORD_AUDIO -> {
                isRecordAudioPermissionDenied
            }

            else -> {
                ""
            }
        }
    }

    private fun getPermissionDenied(context: Activity, deniedPermissions: Array<String>): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        deniedPermissions.forEach { permission ->
            val deniedModelUtilPerm = getDeniedParam(permission)
            if (!pref.getBoolean(deniedModelUtilPerm, false)) {
                return false
            }
        }
        return true
    }

    private fun getPermissionDenied(
        context: Activity,
        deniedModelUtilPerm: String,
        isFirstTimeParam: String
    ): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return pref.getBoolean(deniedModelUtilPerm, false) || (!(pref.getBoolean(
            isFirstTimeParam,
            true
        )))
    }

    private fun checkPermissionBasedOnVersion(
        context: Activity,
        permissions: Array<String>
    ): Boolean {
        return when (permissions[0]) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                hasWriteExternalStoragePermission(context)
            }

            Manifest.permission.READ_EXTERNAL_STORAGE -> {
                hasVideoImageReadStoragePermission(context)
            }

            else -> {
                checkPermission(context, permissions)
            }
        }
    }

    private fun checkAndRequestPermission(
        context: Activity,
        permissions: Array<String>,
        deniedModelUtilPerm: String,
        permissionCallback: KFunction1<Array<String>, Unit>,
        permissionLauncher: ActivityResultLauncher<Array<String>>?,
        isFirstTimeParam: String,
        skipOnDenied: Boolean = false
    ) {
        if (!checkPermissionBasedOnVersion(context, permissions)) {
            if (getPermissionDenied(context, deniedModelUtilPerm, isFirstTimeParam)) {
                //if Permission denied Once by User
                if (!skipOnDenied){
                    UIUtil.showSettingsDialog(
                        context,
                        getPermissionForUI(permissions, context as ComponentActivity)
                    )
                }
            } else {
                requestPermission(permissions, permissionLauncher)
            }
        } else {
            //Granted
            permissionCallback(permissions)
        }
    }

    fun requestPermission(
        permissions: Array<String>, permissionLauncher: ActivityResultLauncher<Array<String>>?
    ) {
        permissionLauncher?.launch(permissions)
    }

    fun getPermissionResultLauncher(
        context: ComponentActivity,
        permissionCallback: KFunction1<Array<String>, Unit>,
        @StringRes permissionMsg: Int,
        fragment: Fragment?,
        isFragment: Boolean,
        permissionDeniedCallBack: KFunction1<Array<String>, Unit>
    ): ActivityResultLauncher<Array<String>>? {
        return if (isFragment) {
            fragment?.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionMap ->
                requestCallBackHandler(
                    permissionMap,
                    permissionCallback,
                    context,
                    permissionDeniedCallBack,
                    permissionMsg
                )
            }
        } else {
            context.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionMap ->
                requestCallBackHandler(
                    permissionMap,
                    permissionCallback,
                    context,
                    permissionDeniedCallBack,
                    permissionMsg
                )
            }
        }
    }

    private fun requestCallBackHandler(
        permissionMap: Map<String, @JvmSuppressWildcards Boolean>,
        permissionCallback: KFunction1<Array<String>, Unit>,
        context: ComponentActivity,
        permissionDeniedCallBack: (Array<String>) -> Unit,
        @StringRes permissionMsg: Int
    ) {
        val deniedPermissionArr = ArrayList<String>()
        val acceptPermissionArr = ArrayList<String>()
        permissionMap.entries.forEach {
            if (it.value) {
                acceptPermissionArr.add(it.key)
            } else {
                deniedPermissionArr.add(it.key)
            }
        }
        if (acceptPermissionArr.isNotEmpty()) {
            permissionCallback(acceptPermissionArr.toTypedArray())
        }
        if (deniedPermissionArr.isNotEmpty()) {
            requestDeniedHandler(
                context,
                deniedPermissionArr.toTypedArray(),
                permissionMsg,
                permissionDeniedCallBack
            )
        }
    }


    private fun requestDeniedHandler(
        context: ComponentActivity,
        permissions: Array<String>,
        @StringRes permissionMsg: Int,
        permissionDeniedCallBack: (Array<String>) -> Unit
    ) {
        if (shouldShowRequestPermissionRationale(context, permissions) && !getPermissionDenied(
                context,
                permissions
            )
        ) {
            setPermissionDenied(context, permissions)
            UIUtil.showEducationalDialog(
                context,
                permissions,
                permissionMsg,
                getPermissionForUI(permissions, context),
                permissionDeniedCallBack
            )
        }
    }

    private fun getPermissionForUI(permissions: Array<String>, context: ComponentActivity): String {
        var result = ""
        permissions.forEach { permission ->
            when (permission) {
                Manifest.permission.READ_MEDIA_VIDEO -> {
                    if (result.isNotBlank()) {
                        result = result + "," + context.getString(R.string.photo_videos_str)
                    } else {
                        result += context.getString(R.string.photo_videos_str)
                    }
                }

                Manifest.permission.READ_MEDIA_IMAGES -> {
//                    if (result.isNotBlank()) {
//                        result = result + "," + context.getString(R.string.photos_str)
//                    } else {
//                        result += context.getString(R.string.photos_str)
//                    }
                }

                Manifest.permission.CAMERA -> {
                    if (result.isNotBlank()) {
                        result = result + "," + context.getString(R.string.camera_str)
                    } else {
                        result += context.getString(R.string.camera_str)
                    }
                }

                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE -> {
                    if (result.isNotBlank()) {
                        result = "$result," + getStoragePermissionString(context)
                    } else {
                        result += getStoragePermissionString(context)
                    }
                }

                Manifest.permission.RECORD_AUDIO -> {
                    if (result.isNotBlank()) {
                        result = "$result," + context.getString(R.string.microphone_str)
                    } else {
                        result += context.getString(R.string.microphone_str)
                    }
                }

                Manifest.permission.POST_NOTIFICATIONS -> {
                    if (result.isNotBlank()) {
                        result = "$result," + context.getString(R.string.notification_str)
                    } else {
                        result += context.getString(R.string.notification_str)
                    }
                }
            }
        }
        return result
    }

    private fun getStoragePermissionString(context: ComponentActivity): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getString(R.string.photo_videos_str)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getString(R.string.file_media_str)
        } else {
            context.getString(R.string.storage_str)
        }
    }


    fun intentToSettings(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

    fun requestCameraPermission(
        context: Activity, permissionAcceptCallBack: KFunction1<Array<String>, Unit>,
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>?
    ) {
        checkAndRequestPermission(
            context,
            arrayOf(Manifest.permission.CAMERA),
            isCameraPermissionDenied,
            permissionAcceptCallBack,
            requestPermissionLauncher,
            isFirstTimeForCameraPermission
        )
    }

    fun requestWriteStoragePermission(
        context: Activity, permissionAcceptCallBack: KFunction1<Array<String>, Unit>,
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>?
    ) {
        checkAndRequestPermission(
            context,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            isWriteStoragePermissionDenied,
            permissionAcceptCallBack,
            requestPermissionLauncher,
            isFirstTimeForWriteStoragePermission
        )
    }

    fun requestRecordAudioPermission(
        context: Activity, permissionAcceptCallBack: KFunction1<Array<String>, Unit>,
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>?
    ) {
        checkAndRequestPermission(
            context,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            isRecordAudioPermissionDenied,
            permissionAcceptCallBack,
            requestPermissionLauncher,
            isFirstTimeForRecordAudioPermission
        )
    }

    fun requestNotificationPermission(
        context: Activity, permissionAcceptCallBack: KFunction1<Array<String>, Unit>,
        requestPermissionLauncher: ActivityResultLauncher<Array<String>>?,
        skipOnDenied: Boolean = false
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkAndRequestPermission(
                context,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                isNotificationPermissionDenied,
                permissionAcceptCallBack,
                requestPermissionLauncher, "", skipOnDenied
            )
        }
    }

    fun canScheduleExactAlarms(applicationContext: Context): Boolean {
        val alarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    }

    fun requestScheduleExactAlarms(applicationContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            applicationContext.startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        }
    }

    fun isNotificationPermissionAllowed(context: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkPermission(context, arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        } else {
            true
        }
    }

    /**
     * Launches the gallery/photo picker depending on Android version.
     * @param pickMediaLauncher The ActivityResultLauncher for modern photo picker (Android 13+)
     * @param legacyPicker The ActivityResultLauncher for legacy gallery picker
     */
    fun launchGalleryPicker(
        pickMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>,
        legacyPicker: ActivityResultLauncher<String>
    ) {
        isPassCodeAllowed = false // Prevent passcode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ → Use modern photo picker
            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            legacyPicker.launch("image/*")
        }
    }

    /**
     * Launches the system camera app to capture an image and save to a file.
     * @param activity The calling Activity
     * @param cameraLauncher The ActivityResultLauncher<Uri> for TakePicture
     * @param fileProviderAuthority The authority string for FileProvider (usually "<package>.fileprovider")
     * @return The Uri where the image will be saved
     */
    fun launchSystemCamera(
        activity: Activity,
        cameraLauncher: ActivityResultLauncher<Uri>,
        fileProviderAuthority: String
    ): Pair<String?, Uri?> {
        isPassCodeAllowed = false // Prevent passcode
        val imageFile = FileUtil.createNewFile(
            FileUtil.FileType.TEMP,
            FileUtil.FileExtension.IMG,
            activity
        )
        val imageUri = androidx.core.content.FileProvider.getUriForFile(
            activity,
            fileProviderAuthority,
            imageFile
        )
        cameraLauncher.launch(imageUri)
        return Pair(imageFile.absolutePath, imageUri)
    }
}