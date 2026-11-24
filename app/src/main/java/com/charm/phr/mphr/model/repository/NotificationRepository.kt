package com.charm.phr.mphr.model.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.charm.phr.mphr.model.NotificationRegister
import com.charm.phr.mphr.util.APIUtil
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.NotificationUtil
import com.charm.phr.mphr.util.WebService
import com.charm.phr.mphr.util.getString
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val webService: WebService,
    private val authManager: AuthManager,
    private val appContext: Context,
    private val apiUtil: APIUtil,
    private val sharedPreferences: SharedPreferences
) {

    val TAG = "NotificationRepository"

    fun getInstallationIdForNotification(): String {
        val installationId = sharedPreferences.getString(ModelUtil.installationIdForNotification, "") ?: ""
        return installationId
    }

    suspend fun getInstallationId(patientId: String): String? {
        return withContext(Dispatchers.IO) {
            val call = webService.getInstallationId(patientId)
            val resp = apiUtil.initiateSynchronizeRequest(call)
            if (resp is JsonObject) {
                Log.d(TAG, "getInstallationId success" + resp.toString())
                val outputStringJson = resp.getAsJsonObject("output_string")
                val installationId = outputStringJson.getString("installation_id") ?: ""
                sharedPreferences.edit().putString(ModelUtil.installationIdForNotification, installationId).commit()
                installationId
            } else {
                null
            }
        }
    }

    suspend fun getFCMToken(): String? {
        return withContext(Dispatchers.IO) {
            if (!BuildUtil.isPushNotificationEnabled) return@withContext null
            try {
                val fcmTokenForNotification = sharedPreferences.getString(ModelUtil.fcmTokenForNotification, "")
                if (fcmTokenForNotification.isNullOrBlank()) {
                    FirebaseMessaging.getInstance().token.await()
                } else {
                    fcmTokenForNotification
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error fetching FCM token: ${e.message}")
                null
            }
        }
    }

    suspend fun checkAndRegisterDevice(): Boolean {
        if (!BuildUtil.isPushNotificationEnabled) {
            Log.d(TAG, "Push notifications disabled for this flavor — skipping device registration")
            return false
        }
        return try {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            withContext(Dispatchers.IO) {
                val isDeviceRegistered = NotificationUtil.isDeviceRegisteredForNotifications(appContext)
                Log.d(TAG, "checkAndRegisterDevice isDeviceRegistered: $isDeviceRegistered")
                if (isDeviceRegistered == false) {
                    getInstallationId(patientId)
                    registerDevice(patientId)
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "checkAndRegisterDevice() Error: ${e.message}")
            false
        }
    }

    private suspend fun registerDevice(patientId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val notificationRegister = formNotificationRegister(appContext)
                if (notificationRegister != null) {
                    val registerPostObject = MDJsonBuilder.getRegisterDeviceObj(notificationRegister)
                    val call = webService.registerDevice(patientId, registerPostObject)

                    val resp = apiUtil.initiateSynchronizeRequest(call)
                    if (resp is JsonObject) {
                        Log.d(TAG, "registerDevice success" + resp.toString())
                        val outputStringJson = resp.getAsJsonObject("output_string")
                        val installationId = outputStringJson.getString("req_id") ?: ""

                        NotificationUtil.savePayloadForUnregisteredDevice(
                            appContext,
                            registerPostObject.toString()
                        )
                        sharedPreferences.edit(commit = true) {
                            putBoolean(ModelUtil.isDeviceRegisteredForNotification, true)
                            putString(ModelUtil.registeredPatientIdForNotification, patientId)
                        }
                        true
                    } else {
                        Log.d(TAG, "Failed to form NotificationRegister  ${resp?.toString()}")
                        false
                    }
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "registerDevice() Error registering device: ${e.message}")
            false
        }
    }

    private suspend fun formNotificationRegister(context: Context): NotificationRegister? {
        try {
            return withContext(Dispatchers.IO) {
                val osInfo = android.os.Build.VERSION.RELEASE ?: ""
                val deviceInfo = android.os.Build.MODEL ?: ""
                val deviceUid = android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                )
                val appVersion = context.packageManager.getPackageInfo(
                    context.packageName, 0
                ).versionName

                val nfid = getFCMToken()
                val installationId = getInstallationIdForNotification()

                NotificationRegister().apply {
                    this.nfid = nfid
                    this.installationId = installationId
                    this.osInfo = osInfo
                    this.deviceInfo = deviceInfo
                    this.deviceUniqueId = deviceUid
                    this.appVersion = appVersion
                }
            }
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun unRegisterDevice(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val jsonObject = NotificationUtil.getPayloadForUnregisteredDevice(appContext)
                if (jsonObject == null) {
                    NotificationUtil.clearDeviceRegistrationPerf(appContext)
                    false
                } else {
                    val patientId = sharedPreferences.getString(ModelUtil.registeredPatientIdForNotification, "") ?: ""
                    val call = webService.unRegisterDevice(patientId, jsonObject)
                    val resp = apiUtil.initiateSynchronizeRequest(call)
                    if (resp is JsonObject) {
                        Log.d(TAG, "unRegisterDevice success" + resp.toString())
                        NotificationUtil.clearDeviceRegistrationPerf(appContext)
                        true
                    } else {
                        Log.d(TAG, "onMDFailure unregistering device: ${resp?.toString()}")
                        false
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error unregistering device: ${e.message}")
            false
        }
    }

}