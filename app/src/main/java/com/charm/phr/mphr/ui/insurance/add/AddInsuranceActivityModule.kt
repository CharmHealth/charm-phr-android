package com.charm.phr.mphr.ui.insurance.add

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddInsuranceActivityModule {

    @Provides
    fun provideAddInsuranceViewModel(patientRepository: PatientRepository):
            AddInsuranceViewModel = AddInsuranceViewModel(patientRepository)

    @Provides
    fun provideAddInsuranceViewModelFactory(addInsuranceViewModel: AddInsuranceViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(addInsuranceViewModel)
}