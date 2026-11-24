package com.charm.phr.mphr.ui.demographics.demographics_pdf

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class PatDemoPDFActivityModule {
    @Provides
    fun providePatDemoPDFViewModel(patientRepository: PatientRepository):
            PatDemoPDFViewModel = PatDemoPDFViewModel(patientRepository)

    @Provides
    fun providePatDemoPDFViewModelFactory(patDemoPDFViewModel: PatDemoPDFViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(patDemoPDFViewModel)

}