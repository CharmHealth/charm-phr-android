package com.charm.phr.mphr.ui.support

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class SupportActivityModule {

    @Provides
    fun provideSupportActivityModule(miscRepository: MiscRepository): SupportViewModel = SupportViewModel(miscRepository)

    @Provides
    fun provideSupportViewModelFactory(supportViewModel: SupportViewModel): ViewModelProvider.Factory = ViewModelProviderFactory(supportViewModel)

    @Provides
    fun provideSupportAttachmentAdapter():
            SupportAttachmentAdapter = SupportAttachmentAdapter()
}