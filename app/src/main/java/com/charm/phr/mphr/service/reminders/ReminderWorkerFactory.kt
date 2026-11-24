package com.charm.phr.mphr.service.reminders

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.model.repository.PrescriptionRepository
import com.charm.phr.mphr.model.repository.ReminderRepository
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.MDAccountsRelatedUtil
import javax.inject.Provider

class ReminderWorkerFactory(
    val reminderRepository: Provider<ReminderRepository>,
    val authManager: Provider<AuthManager>,
    val prescriptionRepository: Provider<PrescriptionRepository>,
    val miscRepository: Provider<MiscRepository>
) :
    WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val pref = PreferenceManager.getDefaultSharedPreferences(appContext)
        val userLoggedIn =
            pref.getBoolean(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN, false)
        if (!userLoggedIn) {
            authManager.get().cancelAllWorker()
            return null
        }
        return when (workerClassName) {
            ScheduleReminderPerDayWorker::class.java.name ->
                ScheduleReminderPerDayWorker(
                    appContext,
                    workerParameters,
                    reminderRepository.get(),
                    authManager.get()
                )

            FetchReminderDataWorker::class.java.name ->
                FetchReminderDataWorker(
                    appContext,
                    workerParameters,
                    authManager.get(),
                    reminderRepository.get(),
                    prescriptionRepository.get(),
                    miscRepository.get()
                )

            else ->
                // Return null, so that the base class can delegate to the default WorkerFactory.
                null
        }
    }
}