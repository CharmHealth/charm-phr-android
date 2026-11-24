package com.charm.phr.mphr.ui.contact_us

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class ContactUsModule {
    @Provides
    fun provideContactUsViewModel(): ContactUsViewModel = ContactUsViewModel()

    @Provides
    fun provideContactUsViewModelFactory(contactUsViewModel: ContactUsViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(contactUsViewModel)
}