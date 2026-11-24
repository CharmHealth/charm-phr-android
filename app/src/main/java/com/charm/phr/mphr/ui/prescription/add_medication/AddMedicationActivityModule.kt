package com.charm.phr.mphr.ui.prescription.add_medication

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.PrescriptionRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddMedicationActivityModule {

    @Provides
    fun provideAddMedicationViewModel(prescriptionRepository: PrescriptionRepository):
            AddMedicationViewModel = AddMedicationViewModel(prescriptionRepository)

    @Provides
    fun provideAddMedicationViewModelFactory(addMedicationViewModel:   AddMedicationViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(addMedicationViewModel)
}