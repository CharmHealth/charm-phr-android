package com.charm.phr.mphr.ui.document

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.charm.phr.mphr.util.ModelUtil
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class DocumentActivityModule {

    @Provides
    fun provideDocumentViewModel(miscRepository: MiscRepository):
            DocumentViewModel = DocumentViewModel(miscRepository)

    @Provides
    fun provideDocumentViewModelFactory(documentViewModel: DocumentViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(documentViewModel)


    @Provides
    fun provideDocumentViewPagerAdapter(documentActivity: DocumentActivity, appContext: Context):
            DocumentViewPagerAdapter = DocumentViewPagerAdapter(documentActivity , appContext , documentActivity.intent.getBooleanExtra(ModelUtil.isHealthPassportIntent , false))

}


//region FragmentProvider

@Module
internal abstract class DocumentListFragmentProvider {

    @ContributesAndroidInjector(modules = [DocumentListFragmentModule::class])
    internal abstract fun provideActiveDocumentFragment(): DocumentListFragment
}

@Module
internal abstract class DocumentFolderFragmentProvider {

    @ContributesAndroidInjector(modules = [DocumentFolderFragmentModule::class])
    internal abstract fun provideHistoryDocumentFragment(): DocumentFolderFragment
}

//endregion

//region FragmentModule

@Module
class DocumentListFragmentModule {
    //

    @Provides
    fun provideDocumentListAdapter():
            DocumentListAdapter = DocumentListAdapter()

}

@Module
class DocumentFolderFragmentModule {
    //
    @Provides
    fun provideDocumentFolderAdapter():
            DocumentFolderAdapter = DocumentFolderAdapter()
}
