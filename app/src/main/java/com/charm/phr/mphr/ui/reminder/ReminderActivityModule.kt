package com.charm.phr.mphr.ui.reminder

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.PrescriptionRepository
import com.charm.phr.mphr.model.repository.ReminderRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.charm.phr.mphr.util.AuthManager
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector


@Module
class ReminderActivityModule {

    @Provides
    fun provideReminderViewModel(reminderRepository: ReminderRepository,prescriptionRepository: PrescriptionRepository):
            ReminderViewModel = ReminderViewModel(reminderRepository,prescriptionRepository)

    @Provides
    fun provideReminderViewModelFactory(reminderViewModel: ReminderViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(reminderViewModel)

    @Provides
    fun provideReminderViewPagerAdapter(reminderActivity: ReminderActivity, appContext: Context,authManager: AuthManager):
            ReminderViewPagerAdapter = ReminderViewPagerAdapter(reminderActivity , appContext,authManager )

}

@Module
internal abstract class CurrentReminderFragmentProvider {

    @ContributesAndroidInjector(modules = [CurrentReminderModule::class])
    internal abstract fun CurrentReminderFragment(): CurrentReminderFragment
}

@Module
class  CurrentReminderModule {
    @Provides
    fun provideCurrentReminderListAdapter(activity: ReminderActivity):
            CurrentReminderListAdapter = CurrentReminderListAdapter(activity)
}

@Module
internal abstract class AllReminderFragmentProvider {

    @ContributesAndroidInjector(modules = [AllReminderModule::class])
    internal abstract fun AllReminderFragment(): AllReminderFragment
}

@Module
class  AllReminderModule {
    @Provides
    fun provideAllReminderListAdapter(activity: ReminderActivity):
            AllReminderListAdapter = AllReminderListAdapter(activity)

}
