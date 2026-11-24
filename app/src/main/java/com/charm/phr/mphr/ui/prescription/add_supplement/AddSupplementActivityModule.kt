package com.charm.phr.mphr.ui.prescription.add_supplement

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.PrescriptionRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddSupplementActivityModule {

    @Provides
    fun provideAddSupplementViewModel(prescriptionRepository: PrescriptionRepository): AddSupplementViewModel = AddSupplementViewModel(prescriptionRepository)

    @Provides
    fun providePrescriptionViewModelProviderFactory(addSupplementViewModel: AddSupplementViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(addSupplementViewModel)

}


