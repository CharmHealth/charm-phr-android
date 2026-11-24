package com.charm.phr.mphr.ui.physician

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class PhysicianActivityModule {

    @Provides
    fun providePhysicianViewModel(miscRepository: MiscRepository):
            PhysicianViewModel = PhysicianViewModel(miscRepository)

    @Provides
    fun providePhysicianViewModelFactory(physicianViewModel: PhysicianViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(physicianViewModel)

    @Provides
    fun provideProcedureListAdapter():
            PhysicianListAdapter = PhysicianListAdapter()

}