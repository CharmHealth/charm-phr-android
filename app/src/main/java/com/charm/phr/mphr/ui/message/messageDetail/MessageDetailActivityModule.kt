package com.charm.phr.mphr.ui.message.messageDetail

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MessageRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class MessageDetailActivityModule {

    @Provides
    fun provideMessageDetailViewModel(messageRepository: MessageRepository): MessageDetailViewModel = MessageDetailViewModel(messageRepository)

    @Provides
    fun provideMessageDetailViewModelProviderFactory(messageDetailViewModel: MessageDetailViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(messageDetailViewModel)

    @Provides
    fun provideMessageDetailAttachmentAdapter():
            MessageDetailAttachmentAdapter = MessageDetailAttachmentAdapter()

}