package com.charm.phr.mphr.ui.reminder.reminder_history

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.ReminderRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class ReminderHistoryModule {

    @Provides
    fun provideReminderHistoryViewModel(reminderRepository: ReminderRepository):
            ReminderHistoryViewModel = ReminderHistoryViewModel(reminderRepository)

    @Provides
    fun provideReminderHistoryViewModelFactory(reminderHistoryViewModel: ReminderHistoryViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(reminderHistoryViewModel)

}