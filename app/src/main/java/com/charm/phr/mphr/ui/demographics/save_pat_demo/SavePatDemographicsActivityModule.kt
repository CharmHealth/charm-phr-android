package com.charm.phr.mphr.ui.demographics.save_pat_demo

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class SavePatDemographicsActivityModule {
    @Provides
    fun provideSavePatDemographicsViewModel(patientRepository: PatientRepository):
            SavePatDemographicsViewModel = SavePatDemographicsViewModel(patientRepository)

    @Provides
    fun provideSavePatDemographicsViewModelFactory(savePatDemographicsViewModel: SavePatDemographicsViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(savePatDemographicsViewModel)

}