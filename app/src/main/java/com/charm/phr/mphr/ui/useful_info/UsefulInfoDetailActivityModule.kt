package com.charm.phr.mphr.ui.useful_info

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class UsefulInfoDetailActivityModule {
    @Provides
    fun provideUsefulInfoDetailViewModel(miscRepository: MiscRepository):
            UsefulInfoDetailViewModel = UsefulInfoDetailViewModel(miscRepository)

    @Provides
    fun provideUsefulInfoDetailViewModelFactory(usefulInfoDetailViewModel: UsefulInfoDetailViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(usefulInfoDetailViewModel)


}