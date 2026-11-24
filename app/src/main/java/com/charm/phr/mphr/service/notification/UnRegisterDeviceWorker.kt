package com.charm.phr.mphr.service.notification

import android.app.Notification
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.repository.NotificationRepository
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDAccountsRelatedUtil
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.NotificationUtil

class UnRegisterDeviceWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val notificationRepository: NotificationRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        private val TAG = "UnRegisterDeviceWorker"

        fun enqueueUnregisterDeviceWork(appContext: Context) {
            if (!BuildUtil.isPushNotificationEnabled) {
                Log.d(TAG, "Push notifications disabled for this flavor — skipping device unregistration")
                return
            }

            val isRegistered = NotificationUtil.isDeviceRegisteredForNotifications(appContext)
            if (!isRegistered) {
                Log.d(TAG, "isDeviceRegisteredForNotifications false, skipping unregistration")
                return
            }

            val pref = PreferenceManager.getDefaultSharedPreferences(appContext)
            val userLoggedIn = pref.getBoolean(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN, false)
            if (!userLoggedIn) {
                Log.d(TAG, "IS_USER_SUCCESSFULLY_LOGGED_IN false, skipping device unregistration")
                NotificationUtil.clearDeviceRegistrationPerf(appContext)
                return
            }

            val workRequest = OneTimeWorkRequestBuilder<UnRegisterDeviceWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(appContext).enqueueUniqueWork(
                ModelUtil.unregisterDeviceWorkerTAG,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val isSuccess = notificationRepository.unRegisterDevice()
            if (isSuccess) {
                Log.d(TAG, "unRegisterDevice successfully")
                Result.success()
            } else {
                Log.d(TAG, "unRegisterDevice failed")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during unRegisterDevice", e)
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificationId = NotificationUtil.generateUniqueTimeBasedId()
        val notification = createNotificationForForeground()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    fun createNotificationForForeground(): Notification {
        return NotificationCompat.Builder(applicationContext, NotificationUtil.MESSAGE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(applicationContext.getString(R.string.unregistering_your_device))
            .setContentText(applicationContext.getString(R.string.this_process_may_take_few_moments)).build()
    }
}