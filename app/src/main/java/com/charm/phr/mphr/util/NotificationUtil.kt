package com.charm.phr.mphr.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.service.reminders.NotificationBroadcastReceiver
import com.google.gson.JsonObject
import com.google.gson.JsonParser

object NotificationUtil {

    private const val TAG = "NotificationUtil"

    //region Reminder const

    const val REMINDER_CHANNEL_ID = "reminder_channel_id"
    const val MESSAGE_CHANNEL_ID = "message_channel_id"

    private const val REMINDER_CHANNEL_NAME = "Reminder"
    private const val REMINDER_CHANNEL_DESCRIPTION = "Reminder"

    private const val MESSAGE_CHANNEL_NAME = "Message"
    private const val MESSAGE_CHANNEL_DESCRIPTION = "Message"

    //endregion

    fun createNotificationChannels(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (notificationManager.getNotificationChannel(REMINDER_CHANNEL_ID) == null) {
                val channel = NotificationChannel(REMINDER_CHANNEL_ID, REMINDER_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                channel.description = REMINDER_CHANNEL_DESCRIPTION
                notificationManager.createNotificationChannel(channel)
            }

            if (notificationManager.getNotificationChannel(MESSAGE_CHANNEL_ID) == null) {
                val channel = NotificationChannel(MESSAGE_CHANNEL_ID, MESSAGE_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                channel.description = MESSAGE_CHANNEL_DESCRIPTION
                notificationManager.createNotificationChannel(channel)
            }

            //Put the code here if new chanel needs to be created

        }

    }

    /**
     * use like: channelId.showNotification()
     */
    fun String.showNotification(
        applicationContext: Context,
        notificationId: Int,
        title: String,
        message: String,
        pendingIntent: PendingIntent
    ) {
        //If prams is getting added in future then move to a custom data class

        val channelId = this
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(applicationContext)) {
            if (PermissionUtil.checkPermission(applicationContext, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS))) {
                notify(notificationId, builder.build())
            } else {
                Log.d(TAG, "Notification permission not granted, cannot show notification")
            }
        }

    }

    fun scheduleNotification(
        applicationContext: Context,
        title: String,
        message: String,
        time: Long,
        notificationID: Int,
        reminderIdArr: String,
        timeString: String?
    ) {
        val intent = Intent(applicationContext, NotificationBroadcastReceiver::class.java)
        intent.putExtra(ModelUtil.reminderTitle, title)
        intent.putExtra(ModelUtil.reminderMessage, message)
        intent.putExtra(ModelUtil.notificationId, notificationID)
        intent.putExtra(ModelUtil.ReminderId, reminderIdArr)
        intent.putExtra(ModelUtil.ReminderTime, timeString)

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (PermissionUtil.canScheduleExactAlarms(applicationContext)) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        }

    }

    fun cancelNotification(applicationContext: Context, title: String, message: String, notificationID: Int) {
        val intent = Intent(applicationContext, NotificationBroadcastReceiver::class.java)
        intent.putExtra(ModelUtil.reminderTitle, title)
        intent.putExtra(ModelUtil.reminderMessage, message)
        intent.putExtra(ModelUtil.notificationId, notificationID)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    fun getNotificationTitle(isMedication: Boolean, isSupplement: Boolean, applicationContext: Context, patient: Patient?): String {
        if (isMedication && isSupplement) {
            return applicationContext.getString(R.string.notification_title_medication_and_supplements, patient?.firstName)
        } else if (isMedication) {
            return applicationContext.getString(R.string.notification_title_medication, patient?.firstName)
        } else if (isSupplement) {
            return applicationContext.getString(R.string.notification_title_supplements, patient?.firstName)
        }
        return applicationContext.getString(R.string.notification_title, patient?.firstName)
    }

    fun clearAllNotification(applicationContext: Context) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    //region Push Notification

    fun isDeviceRegisteredForNotifications(applicationContext: Context): Boolean {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val isRegistered = sharedPreferences.getBoolean(ModelUtil.isDeviceRegisteredForNotification, false)
        return isRegistered
    }

    // Saving the payload for unregistered device to avoid conflict
    fun savePayloadForUnregisteredDevice(
        applicationContext: Context,
        payload: String
    ) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPreferences.edit().putString(ModelUtil.unregisteredDevicePayload, payload).commit()
    }

    fun getPayloadForUnregisteredDevice(
        applicationContext: Context
    ): JsonObject? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val unregisterPayloadString = sharedPreferences.getString(ModelUtil.unregisteredDevicePayload, null)

        return JsonParser.parseString(unregisterPayloadString).asJsonObject
    }

    fun clearDeviceRegistrationPerf(appContext: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        sharedPreferences.edit(commit = true) {
            remove(ModelUtil.isDeviceRegisteredForNotification)
            remove(ModelUtil.installationIdForNotification)
        }
    }

    //endregion

    /**
     * Saves a collection of notification IDs to SharedPreferences.
     *
     * @param prefs SharedPreferences instance to store the IDs.
     * @param ids Collection of notification ID strings to save.
     * @param preferenceKey The key under which the IDs are stored in SharedPreferences.
     */
    fun saveNotificationIds(context: Context, ids: Collection<String>, preferenceKey: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val set = try {
            prefs.getStringSet(preferenceKey, mutableSetOf())?.toMutableSet()
        } catch (e: Exception) {
            Log.e(TAG, "Exception while retrieving notification IDs from SharedPreferences", e)
            null
        }
        if (set == null) {
            Log.d(TAG, "Failed to retrieve notification IDs from SharedPreferences")
            return
        }
        set.addAll(ids)
        prefs.edit().putStringSet(preferenceKey, set).commit()
    }

//    fun removeNotificationIds(prefs: SharedPreferences, ids: Collection<String>, preferenceKey: String) {
//        try {
//            val set = prefs.getStringSet(preferenceKey, mutableSetOf())?.toMutableSet()
//            set?.removeAll(ids)
//            prefs.edit().putStringSet(preferenceKey, set).commit()
//        } catch (e: Exception) {
//            Log.e(TAG, "Exception while removing notification IDs from SharedPreferences", e)
//        }
//    }

    fun getNotificationIds(prefs: SharedPreferences, preferenceKey: String): Set<String> {
        return prefs.getStringSet(preferenceKey, mutableSetOf()) ?: emptySet()
    }

    /**
     * Cancels notifications for the given IDs and removes them from SharedPreferences.
     *
     * @param context The context used to access system services and preferences.
     * @param notificationIds Set of notification ID strings to cancel.
     * @param preferenceKey The key under which the IDs are stored in SharedPreferences.
     */
    fun clearNotificationForIds(context: Context, notificationIds: Set<String>, preferenceKey: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            for (id in notificationIds) {
                id.toIntOrNull()?.let {
                    notificationManager.cancel(it)
                }
            }
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().remove(preferenceKey).commit()

        } catch (e: Exception) {
            Log.e(TAG, "Error clearing notifications for IDs: $notificationIds", e)
        }
    }

    fun generateUniqueTimeBasedId(): Int {
        return (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    }

}