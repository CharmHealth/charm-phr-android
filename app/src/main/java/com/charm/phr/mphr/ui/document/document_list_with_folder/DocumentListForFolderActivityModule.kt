package com.charm.phr.mphr.ui.document.document_list_with_folder

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.charm.phr.mphr.ui.document.DocumentListAdapter
import dagger.Module
import dagger.Provides

@Module
class DocumentListForFolderActivityModule {

    @Provides
    fun provideDocumentListForFolderViewModel (miscRepository: MiscRepository):
            DocumentListForFolderViewModel = DocumentListForFolderViewModel(miscRepository)

    @Provides
    fun provideDocumentListForFolderViewModelProvider (documentListForFolderViewModel: DocumentListForFolderViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(documentListForFolderViewModel)

    @Provides
    fun provideDocumentListAdapter():
            DocumentListAdapter = DocumentListAdapter()
}