package com.charm.phr.mphr.ui.vaccine.add_vaccine

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddVaccineActivityModule {

    @Provides
    fun provideAddVaccineViewModel(wellnessRepository: WellnessRepository):
            AddVaccineViewModel = AddVaccineViewModel(wellnessRepository)

    @Provides
    fun provideAddVaccineViewModelFactory(addVaccineViewModel: AddVaccineViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory<AddVaccineViewModel>(addVaccineViewModel)
}