package com.charm.phr.mphr.ui.contact.add_contact

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddContactActivityModule {

    @Provides
    fun provideAddContactViewModel(miscRepository: MiscRepository):
            AddContactViewModel = AddContactViewModel(miscRepository)

    @Provides
    fun provideAddContactViewModelFactory(addContactViewModel: AddContactViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(addContactViewModel)
}