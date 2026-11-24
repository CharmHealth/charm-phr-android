package com.charm.phr.mphr.ui.lab

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.LabRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.charm.phr.mphr.util.AuthManager
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class LabActivityModule {

    @Provides
    fun provideLabViewModel(labRepository: LabRepository): LabViewModel = LabViewModel(labRepository)

    @Provides
    fun provideLabViewModelProviderFactory(labViewModel: LabViewModel): ViewModelProvider.Factory = ViewModelProviderFactory(labViewModel)

    @Provides
    fun provideLabViewPagerAdapter(labActivity: LabActivity, appContext: Context): LabViewPagerAdapter = LabViewPagerAdapter(labActivity, appContext)

}

//region FragmentProvider

@Module
internal abstract class LabOrderListFragmentProvider {

    @ContributesAndroidInjector(modules = [LabOrderListFragmentModule::class])
    internal abstract fun provideLabOrderListFragment(): LabOrderListFragment
}

@Module
internal abstract class LabResultListFragmentProvider {

    @ContributesAndroidInjector(modules = [LabResultListFragmentModule::class])
    internal abstract fun provideReceiptFragment(): LabResultListFragment
}

//endregion

//region FragmentModule

@Module
class LabOrderListFragmentModule {
    //

    @Provides
    fun provideLabOrderListAdapter(authManager: AuthManager): LabOrderListAdapter = LabOrderListAdapter(authManager)

}

@Module
class LabResultListFragmentModule {
    //

    @Provides
    fun provideLabResultListAdapter(authManager: AuthManager): LabResultListAdapter = LabResultListAdapter(authManager)
}

//endregion