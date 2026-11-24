package com.charm.phr.mphr.ui.allergy.addAllergy

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddAllergyActivityModule {

    @Provides
    fun provideAddAllergyViewModel(wellnessRepository: WellnessRepository):
            AddAllergyViewModel = AddAllergyViewModel(wellnessRepository)

    @Provides
    fun provideAddAllergyViewModelFactory(addAllergyViewModel: AddAllergyViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory<AddAllergyViewModel>(addAllergyViewModel)

}