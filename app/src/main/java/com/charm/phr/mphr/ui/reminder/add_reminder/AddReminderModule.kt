package com.charm.phr.mphr.ui.reminder.add_reminder

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.ReminderRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddReminderModule {
    @Provides
    fun provideAddReminderViewModel(reminderRepository: ReminderRepository):
            AddReminderViewModel = AddReminderViewModel(reminderRepository)

    @Provides
    fun provideAddReminderViewModelFactory(addReminderViewModel: AddReminderViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(addReminderViewModel)
}