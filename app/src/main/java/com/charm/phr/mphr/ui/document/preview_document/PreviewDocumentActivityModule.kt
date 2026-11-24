package com.charm.phr.mphr.ui.document.preview_document

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class PreviewDocumentActivityModule {

    @Provides
    fun providePreviewDocumentViewModel(miscRepository: MiscRepository):
            PreviewDocumentViewModel = PreviewDocumentViewModel(miscRepository)

    @Provides
    fun provideDocumentViewModelFactory(previewDocumentViewModel: PreviewDocumentViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(previewDocumentViewModel)
}