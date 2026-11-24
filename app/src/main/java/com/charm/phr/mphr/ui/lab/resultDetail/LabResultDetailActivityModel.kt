package com.charm.phr.mphr.ui.lab.resultDetail

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.LabRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class LabResultDetailActivityModel {

    @Provides
    fun provideLabResultDetailViewModel(labRepository: LabRepository):
            LabResultDetailViewModel = LabResultDetailViewModel(labRepository)

    @Provides
    fun provideLabResultDetailViewModelFactory(labResultDetailViewModel: LabResultDetailViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory<LabResultDetailViewModel>(labResultDetailViewModel)

    @Provides
    fun provideLabResultDetailListAdapter():
            LabResultDetailListAdapter = LabResultDetailListAdapter()
}