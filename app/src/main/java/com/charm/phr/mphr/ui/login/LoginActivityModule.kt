package com.charm.phr.mphr.ui.login

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class LoginActivityModule {

    @Provides
    fun provideLoginViewModel(patientRepository: PatientRepository , miscRepository: MiscRepository): LoginViewModel = LoginViewModel(patientRepository , miscRepository)

    @Provides
    fun provideLoginViewModelProviderFactory(loginViewModel: LoginViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(loginViewModel)
}