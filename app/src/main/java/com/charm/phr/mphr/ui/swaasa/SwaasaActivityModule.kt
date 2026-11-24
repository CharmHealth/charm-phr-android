package com.charm.phr.mphr.ui.swaasa

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class SwaasaActivityModule {

    @Provides
    fun provideSwaasaViewModel(miscRepository: MiscRepository):
            SwaasaViewModel = SwaasaViewModel(miscRepository)

    @Provides
    fun provideSwaasaViewModelFactory(swaasaViewModel: SwaasaViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(swaasaViewModel)
}