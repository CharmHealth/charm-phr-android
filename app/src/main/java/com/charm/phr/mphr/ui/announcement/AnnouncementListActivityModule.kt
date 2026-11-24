package com.charm.phr.mphr.ui.announcement

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.charm.phr.mphr.util.AuthManager
import dagger.Module
import dagger.Provides

@Module
class AnnouncementListActivityModule {

    @Provides
    fun provideAnnouncementListViewModel(miscRepository: MiscRepository):
            AnnouncementListViewModel = AnnouncementListViewModel(miscRepository)

    @Provides
    fun provideAnnouncementListViewModelFactory(announcementListViewModel: AnnouncementListViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(announcementListViewModel)


    @Provides
    fun provideAnnouncementListAdapter(authManager: AuthManager):
            AnnouncementListAdapter = AnnouncementListAdapter(authManager)

}