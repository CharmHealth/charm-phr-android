package com.charm.phr.mphr.ui.vaccine

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.charm.phr.mphr.util.AuthManager
import dagger.Module
import dagger.Provides

@Module
class VaccineActivityModule {

    @Provides
    fun provideVaccineViewModel(wellnessRepository: WellnessRepository):
            VaccineViewModel = VaccineViewModel(wellnessRepository)

    @Provides
    fun provideVaccineViewModelFactory(vaccineViewModel: VaccineViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(vaccineViewModel)

    @Provides
    fun provideVaccineListAdapter(authManager: AuthManager):
            VaccineListAdapter = VaccineListAdapter(authManager)

}