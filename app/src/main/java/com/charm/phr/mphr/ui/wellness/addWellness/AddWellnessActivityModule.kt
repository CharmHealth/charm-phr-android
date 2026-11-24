package com.charm.phr.mphr.ui.wellness.addWellness

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class AddWellnessActivityModule {

    @Provides
    fun provideAddWellnessViewModel(wellnessRepository: WellnessRepository):
            AddWellnessViewModel = AddWellnessViewModel(wellnessRepository)

    @Provides
    fun provideAddWellnessViewModelFactory(addWellnessViewModel: AddWellnessViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(addWellnessViewModel)

}