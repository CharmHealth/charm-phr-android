package com.charm.phr.mphr.ui.wellness.wellnessDetail

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class WellnessDetailActivityModule {

    @Provides
    fun provideWellnessViewModel(wellnessRepository: WellnessRepository):
            WellnessDetailViewModel = WellnessDetailViewModel(wellnessRepository)

    @Provides
    fun provideWellnessViewModelFactory(wellnessDetailViewModel: WellnessDetailViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(wellnessDetailViewModel)



}

@Module
abstract class WellnessList1FragmentProvider {

    @ContributesAndroidInjector(modules = [WellnessList1FragmentModule::class])
    internal abstract fun provideWellnessList1Fragment(): WellnessList1Fragment
}



//endregion

//region FragmentModule

@Module
class WellnessList1FragmentModule {
    //

    @Provides
    fun provideWellnessListAdapter():
            WellnessDetailListAdapter = WellnessDetailListAdapter()

    @Provides
    fun provideWellnessDetailGridAdapter():
            WellnessDetailGridAdapter = WellnessDetailGridAdapter()

    @Provides
    fun provideWellnessDetailMetricListAdapter():
            WellnessDetailMetricListAdapter = WellnessDetailMetricListAdapter()


}

