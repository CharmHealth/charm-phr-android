package com.charm.phr.mphr.ui.procedure.add_procedure

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddProcedureActivityModule {

    @Provides
    fun provideAddProcedureViewModel(wellnessRepository: WellnessRepository):
            AddProcedureViewModel = AddProcedureViewModel(wellnessRepository)

    @Provides
    fun provideAddProcedureViewModelFactory(addProcedureViewModel: AddProcedureViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory<AddProcedureViewModel>(addProcedureViewModel)
}