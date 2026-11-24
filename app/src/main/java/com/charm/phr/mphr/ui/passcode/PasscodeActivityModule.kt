package com.charm.phr.mphr.ui.passcode

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class PasscodeActivityModule {

    @Provides
    fun providePasscodeViewModel(miscRepository: MiscRepository):
            PasscodeViewModel = PasscodeViewModel( miscRepository)

    @Provides
    fun providePasscodeViewModelProvider(passcodeViewModel: PasscodeViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(passcodeViewModel)

}