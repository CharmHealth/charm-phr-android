package com.charm.phr.mphr.ui.demographics

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class PatientDemographicsActivityModule{

    @Provides
    fun providePatientDemographicsViewModel(patientRepository: PatientRepository):
            PatientDemographicsViewModel = PatientDemographicsViewModel(patientRepository)

    @Provides
    fun providePatientDemographicsViewModelFactory(patientDemographicsViewModel: PatientDemographicsViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(patientDemographicsViewModel)

}