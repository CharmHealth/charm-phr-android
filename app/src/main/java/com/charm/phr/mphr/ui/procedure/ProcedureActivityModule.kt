package com.charm.phr.mphr.ui.procedure

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.charm.phr.mphr.util.AuthManager
import dagger.Module
import dagger.Provides

@Module
class ProcedureActivityModule {

    @Provides
    fun provideProcedureViewModel(wellnessRepository: WellnessRepository):
            ProcedureViewModel = ProcedureViewModel(wellnessRepository)

    @Provides
    fun provideProcedureViewModelFactory(procedureViewModel: ProcedureViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(procedureViewModel)

    @Provides
    fun provideProcedureListAdapter(authManager: AuthManager):
            ProcedureListAdapter = ProcedureListAdapter(authManager)

}