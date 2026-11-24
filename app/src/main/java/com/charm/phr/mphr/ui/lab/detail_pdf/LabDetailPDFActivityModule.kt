package com.charm.phr.mphr.ui.lab.detail_pdf

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.LabRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class LabDetailPDFActivityModule {

    @Provides
    fun provideLabDetailPDFViewModel(labRepository: LabRepository):
            LabDetailPDFViewModel = LabDetailPDFViewModel(labRepository)

    @Provides
    fun provideLabDetailPDFViewModelFactory(labDetailPDFViewModel: LabDetailPDFViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(labDetailPDFViewModel)
}