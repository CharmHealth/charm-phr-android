package com.charm.phr.mphr.ui.document.add_document

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddDocumentActivityModule {
    @Provides
    fun provideAddDocumentViewModel(miscRepository: MiscRepository):
            AddDocumentViewModel = AddDocumentViewModel(miscRepository)

    @Provides
    fun provideAddDocumentViewModelFactory(addDocumentViewModel: AddDocumentViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(addDocumentViewModel)

}