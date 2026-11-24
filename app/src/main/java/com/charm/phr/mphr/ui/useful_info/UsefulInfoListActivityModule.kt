package com.charm.phr.mphr.ui.useful_info

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.charm.phr.mphr.util.AuthManager
import dagger.Module
import dagger.Provides

@Module
class UsefulInfoListActivityModule {

    @Provides
    fun provideUsefulInfoListViewModel(miscRepository: MiscRepository):
            UsefulInfoListViewModel = UsefulInfoListViewModel(miscRepository)

    @Provides
    fun provideUsefulInfoListViewModelFactory(usefulInfoListViewModel: UsefulInfoListViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(usefulInfoListViewModel)


    @Provides
    fun provideUsefulInfoListAdapter(authManager: AuthManager):
            UsefulInfoListAdapter = UsefulInfoListAdapter(authManager)

}