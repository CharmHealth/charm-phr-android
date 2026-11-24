package com.charm.phr.mphr.ui.reminder.update_reminder

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.ReminderRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class UpdateReminderHistoryModule {

    @Provides
    fun provideUpdateReminderModel(reminderRepository: ReminderRepository):
            UpdateReminderHistoryModel = UpdateReminderHistoryModel(reminderRepository)

    @Provides
    fun provideUpdateReminderModelFactory(updateReminderModel: UpdateReminderHistoryModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(updateReminderModel)

}