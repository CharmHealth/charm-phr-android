package com.charm.phr.mphr.ui.document.share_document

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class ShareDocumentActivityModule {

    @Provides
    fun provideShareDocumentViewModel(miscRepository: MiscRepository):
            ShareDocumentViewModel = ShareDocumentViewModel(miscRepository)

    @Provides
    fun provideShareDocumentViewModelFactory(shareDocumentViewModel: ShareDocumentViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(shareDocumentViewModel)

    @Provides
    fun provideShareDocumentListAdapter():
            ShareDocumentListAdapter = ShareDocumentListAdapter()
}