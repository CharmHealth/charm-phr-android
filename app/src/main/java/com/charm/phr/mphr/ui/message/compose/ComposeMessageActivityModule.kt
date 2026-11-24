package com.charm.phr.mphr.ui.message.compose

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MessageRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class ComposeMessageActivityModule {

    @Provides
    fun provideComposeMessageViewModel(messageRepository: MessageRepository):
            ComposeMessageViewModel = ComposeMessageViewModel(messageRepository)

    @Provides
    fun provideComposeMessageViewModelFactory(composeMessageViewModel: ComposeMessageViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(composeMessageViewModel)

}

@Module
internal abstract class ComposeMessageFragmentProvider {

    @ContributesAndroidInjector(modules = [ComposeMessageFragmentModule::class])
    internal abstract fun provideComposeMessageFragment(): ComposeMessageFragment
}

@Module
class ComposeMessageFragmentModule {
    @Provides
    fun provideComposeMessageAttachmentAdapter():
            ComposeMessageAttachmentAdapter = ComposeMessageAttachmentAdapter()

}
