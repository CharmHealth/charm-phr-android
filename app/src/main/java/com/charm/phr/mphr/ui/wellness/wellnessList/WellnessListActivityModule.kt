package com.charm.phr.mphr.ui.wellness.wellnessList

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class WellnessListActivityModule {

    @Provides
    fun provideWellnessListViewModel(wellnessRepository: WellnessRepository):
            WellnessListViewModel = WellnessListViewModel(wellnessRepository)

    @Provides
    fun provideWellnessListViewModelFactory(wellnessListViewModel: WellnessListViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(wellnessListViewModel)


    @Provides
    fun provideWellnessDataListAdapter():
            WellnessDataListAdapter = WellnessDataListAdapter()
}