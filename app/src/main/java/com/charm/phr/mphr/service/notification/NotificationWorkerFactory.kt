package com.charm.phr.mphr.service.notification

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.charm.phr.mphr.model.repository.NotificationRepository
import com.charm.phr.mphr.util.AuthManager
import javax.inject.Provider

class NotificationWorkerFactory(
    val notificationRepository: Provider<NotificationRepository>,
    val authManager: Provider<AuthManager>,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        Log.d("NotificationWorkerFactory", "createWorker called with $workerClassName")

        return when (Class.forName(workerClassName)) {
            RegisterDeviceWorker::class.java ->
                RegisterDeviceWorker(appContext, workerParameters, notificationRepository.get())
            UnRegisterDeviceWorker::class.java ->
                UnRegisterDeviceWorker(appContext, workerParameters, notificationRepository.get())
            else -> null
        }
    }
}