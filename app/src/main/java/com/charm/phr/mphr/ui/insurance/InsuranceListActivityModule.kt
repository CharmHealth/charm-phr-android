package com.charm.phr.mphr.ui.insurance

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.charm.phr.mphr.util.AuthManager
import dagger.Module
import dagger.Provides

@Module
class InsuranceListActivityModule {

    @Provides
    fun provideInsuranceListViewModel(patientRepository: PatientRepository):
            InsuranceListViewModel = InsuranceListViewModel(patientRepository)

    @Provides
    fun provideInsuranceListViewModelFactory(insuranceListViewModel: InsuranceListViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(insuranceListViewModel)

    @Provides
    fun provideInsuranceListAdapter(authManager: AuthManager):
            InsuranceListAdapter = InsuranceListAdapter(authManager)

}