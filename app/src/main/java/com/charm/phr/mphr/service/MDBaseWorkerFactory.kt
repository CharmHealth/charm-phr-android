package com.charm.phr.mphr.service

import androidx.work.DelegatingWorkerFactory
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.model.repository.NotificationRepository
import com.charm.phr.mphr.model.repository.PrescriptionRepository
import com.charm.phr.mphr.model.repository.ReminderRepository
import com.charm.phr.mphr.service.notification.NotificationWorkerFactory
import com.charm.phr.mphr.service.reminders.ReminderWorkerFactory
import com.charm.phr.mphr.util.AuthManager
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class MDBaseWorkerFactory @Inject constructor(
    reminderRepository: Provider<ReminderRepository>,
    authManager: Provider<AuthManager>,
    prescriptionRepository: Provider<PrescriptionRepository>,
    miscRepository: Provider<MiscRepository>,
    notificationRepository: Provider<NotificationRepository>,
) : DelegatingWorkerFactory() {
    init {
        addFactory(ReminderWorkerFactory(reminderRepository, authManager, prescriptionRepository, miscRepository))
        addFactory(NotificationWorkerFactory(notificationRepository, authManager))
    }
}