package com.charm.phr.mphr.ui.cardonfile.addOrEditCof

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.model.repository.PaymentRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddOrEditCardOnFileActivityModule {
    @Provides
    fun provideAddOrEditCofModel(paymentRepository: PaymentRepository,patientRepository: PatientRepository): AddOrEditCofModel = AddOrEditCofModel(paymentRepository,patientRepository)

    @Provides
    fun provideAddOrEditCofModelProviderFactory(addOrEditCofModel: AddOrEditCofModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(addOrEditCofModel)
}