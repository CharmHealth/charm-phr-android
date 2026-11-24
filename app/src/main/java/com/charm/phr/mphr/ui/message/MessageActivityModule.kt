package com.charm.phr.mphr.ui.message

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MessageRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class MessageActivityModule {

    @Provides
    fun provideMessageViewModel(messageRepository: MessageRepository): MessageViewModel = MessageViewModel(messageRepository)

    @Provides
    fun provideMessageViewModelProviderFactory(messageViewModel: MessageViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(messageViewModel)

    @Provides
    fun provideMessageViewPagerAdapter(messageActivity: MessageActivity , appContext: Context):
            MessageViewPagerAdapter = MessageViewPagerAdapter(messageActivity , appContext)

}

//region FragmentProvider

@Module
internal abstract class MessageInboxFragmentProvider {

    @ContributesAndroidInjector(modules = [MessageInboxFragmentModule::class])
    internal abstract fun provideMessageInboxFragment(): MessageInboxFragment
}

@Module
internal abstract class MessageSentFragmentProvider {

    @ContributesAndroidInjector(modules = [MessageSentFragmentModule::class])
    internal abstract fun provideMessageSentFragment(): MessageSentFragment
}

//endregion

//region FragmentModule

@Module
class MessageInboxFragmentModule {
    //

    @Provides
    fun provideMessageInboxListAdapter():
            MessageInboxListAdapter = MessageInboxListAdapter()

}

@Module
class MessageSentFragmentModule {
    //
    @Provides
    fun provideMessageSentListAdapter():
            MessageSentListAdapter = MessageSentListAdapter()
}

//endregion