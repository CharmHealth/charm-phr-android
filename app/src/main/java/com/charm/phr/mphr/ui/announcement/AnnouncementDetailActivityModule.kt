package com.charm.phr.mphr.ui.announcement

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AnnouncementDetailActivityModule {
    @Provides
    fun provideAnnouncementDetailViewModel(miscRepository: MiscRepository):
            AnnouncementDetailViewModel = AnnouncementDetailViewModel(miscRepository)

    @Provides
    fun provideAnnouncementDetailViewModelFactory(announcementDetailViewModel: AnnouncementDetailViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(announcementDetailViewModel)


}