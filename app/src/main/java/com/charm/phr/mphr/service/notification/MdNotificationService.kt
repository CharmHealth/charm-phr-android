package com.charm.phr.mphr.service.notification

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.SectionConfigList
import com.charm.phr.mphr.ui.message.messageDetail.MessageDetailActivity
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.NotificationUtil
import com.charm.phr.mphr.util.NotificationUtil.showNotification
import com.charm.phr.mphr.util.RbacUtil
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.JsonObject

class MdNotificationService : FirebaseMessagingService() {



//    @Inject
//    lateinit var authManager: AuthManager

    enum class NotificationModule(val moduleName: String) {
        MESSAGE("Message"),/* REMINDER, APPOINTMENT, LAB_RESULT, PRESCRIPTION,*/
    }

    companion object {

        private val TAG = "MdNotificationService"

        fun fetchFcmToken(onTokenReceived: (String?) -> Unit) {
            if (!BuildUtil.isPushNotificationEnabled) {
                onTokenReceived(null)
                return
            }
            try {
                FirebaseMessaging.getInstance().token
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onTokenReceived(task.result)
                        } else {
                            onTokenReceived(null)
                        }
                    }
            } catch (e: Exception) {
                onTokenReceived(null)
                Log.d(TAG, "Error fetching FCM token: ${e.message}")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken token: $token")

        if (!BuildUtil.isPushNotificationEnabled) {
            Log.d(TAG, "Push notifications disabled for this flavor — skipping FCM device registration")
            return
        }

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val oldToken = pref.getString(ModelUtil.fcmTokenForNotification, "")

        if (oldToken.isNullOrEmpty()) {
            pref.edit().putString(ModelUtil.fcmTokenForNotification, token).commit()
            Log.d(TAG, "New FCM token saved: $token")
        } else if (oldToken != token) {
            UnRegisterDeviceWorker.enqueueUnregisterDeviceWork(applicationContext)
            pref.edit().putString(ModelUtil.fcmTokenForNotification, token).commit()
            RegisterDeviceWorker.enqueueRegisterDeviceWork(applicationContext)
            Log.d(TAG, "FCM token updated: $token")
        } else {
            // Token is unchanged, no action needed
            Log.d(TAG, "FCM token is unchanged, no action needed.")
        }

    }

    override fun onCreate() {
        super.onCreate()
        if (!BuildUtil.isPushNotificationEnabled) {
            Log.d(TAG, "Push notifications disabled for this flavor — skipping FCM initialization")
            return
        }
        fetchFcmToken {

        }
        Log.d(TAG, "-----------Notification Service onCreate----------")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "-----------Notification Service onDestroy----------")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Message data payload: ${remoteMessage.data}")

        if (!BuildUtil.isPushNotificationEnabled) {
            Log.d(TAG, "Push notifications disabled for this flavor — skipping message handling")
            return
        }

        parseDataAndSendNotification(remoteMessage.data)

    }

    private fun parseDataAndSendNotification(data: Map<String, String>) {
        try {

            val addInfo = data["addInfo"]

            if (addInfo != null) {
                val jsonObject = org.json.JSONObject(addInfo)
                val module = jsonObject.getString("module")
                val patientId = jsonObject.getString("patient_id")
                val messageId = jsonObject.getString("message_id")
                val practiceId = jsonObject.getString("practice_id")


                when {
                    module.equals(NotificationModule.MESSAGE.moduleName, ignoreCase = true) -> {
                        handleMessageNotification(patientId, messageId, practiceId)
                    }

                    else -> {
                        Log.d(TAG, "Unknown module received: $module")
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error in parseDataAndSendNotification: ${e.message}", e)
        }

    }

    private fun handleMessageNotification(patientId: String, messageId: String, practiceId: String) {
        val isMessageModuleEnabled = ModelUtil.isModuleEnabled(getSelectedPatientSection(patientId), ModelUtil.messageModuleId)
        val hasAddMessagesPrivilege = RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_MESSAGES, applicationContext)

        if (!(isMessageModuleEnabled && hasAddMessagesPrivilege)) {
            // If the message module is not enabled or privilege is not granted, do not show notification
            Log.d(TAG, "Message module privilege is not granted")
            return
        }

        val uniqueRequestId = NotificationUtil.generateUniqueTimeBasedId()

        NotificationUtil.saveNotificationIds(
            applicationContext,
            listOf(uniqueRequestId.toString()),
            ModelUtil.localMessageNotificationIds
        )

        val messageIntent = Intent(this, MessageDetailActivity::class.java)
        messageIntent.putExtra(ModelUtil.patientIdIntent, patientId)
        messageIntent.putExtra(ModelUtil.messageIdIntent, messageId)
        messageIntent.putExtra(ModelUtil.practiceIdIntent, practiceId)
        messageIntent.putExtra(ModelUtil.isNotificationFlow, true)

        messageIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP


        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            uniqueRequestId,
            messageIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        NotificationUtil.MESSAGE_CHANNEL_ID.showNotification(
            applicationContext = this,
            notificationId = uniqueRequestId,
            title = getString(R.string.new_message),
            message = getString(R.string.you_have_a_new_message_from_your_practice),
            pendingIntent = pendingIntent
        )
    }

    fun getSelectedPatientSection(patientId: String): SectionConfigList? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val resp = sharedPreferences.getString(ModelUtil.patientSectionPrefix + patientId, "")
        return if (resp.isNullOrBlank()) {
            null
        } else {
            MDParsingHelper.formPatientSection(resp.parseStringAsJsonObject() ?: JsonObject())
        }
    }

}

