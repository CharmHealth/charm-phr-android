package com.charm.phr.mphr.ui.lab.plotDetailGraph

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.LabRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class LabDetailsGraphActivityModule {

    @Provides
    fun provideLabDetailPDFViewModel(labRepository: LabRepository):
            LabDetailsGraphViewModel = LabDetailsGraphViewModel(labRepository)

    @Provides
    fun provideLabDetailPDFViewModelFactory(labDetailsGraphViewModel: LabDetailsGraphViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(labDetailsGraphViewModel)
}