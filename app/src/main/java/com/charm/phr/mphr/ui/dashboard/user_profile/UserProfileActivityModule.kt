package com.charm.phr.mphr.ui.dashboard.user_profile

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class UserProfileActivityModule {

    @Provides
    fun provideUserProfileViewModel():
            UserProfileViewModel = UserProfileViewModel()

    @Provides
    fun provideUserProfileViewModelFactory(userProfileViewModel: UserProfileViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(userProfileViewModel)
}