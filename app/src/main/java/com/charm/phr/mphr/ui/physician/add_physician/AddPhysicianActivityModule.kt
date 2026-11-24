package com.charm.phr.mphr.ui.physician.add_physician

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddPhysicianActivityModule {

    @Provides
    fun provideAddPhysicianViewModel(miscRepository: MiscRepository):
            AddPhysicianViewModel = AddPhysicianViewModel(miscRepository)

    @Provides
    fun provideAddProcedureViewModelFactory(addPhysicianViewModel: AddPhysicianViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory<AddPhysicianViewModel>(addPhysicianViewModel)

}