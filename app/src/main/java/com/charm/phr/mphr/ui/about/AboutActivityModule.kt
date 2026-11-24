package com.charm.phr.mphr.ui.about

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class AboutActivityModule {

    @Provides
    fun provideAboutViewModel(miscRepository: MiscRepository):
            AboutViewModel = AboutViewModel(miscRepository)

    @Provides
    fun provideAboutViewModelFactory(aboutViewModel: AboutViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(aboutViewModel)

    @Provides
    fun provideAboutViewPagerAdapter(aboutActivity: AboutActivity, appContext: Context):
            AboutViewPagerAdapter = AboutViewPagerAdapter(aboutActivity , appContext)

}

//region FragmentProvider

@Module
internal abstract class FacilityListFragmentProvider {

    @ContributesAndroidInjector(modules = [FacilityListFragmentModule::class])
    internal abstract fun provideFacilityListFragment(): FacilityListFragment
}

@Module
internal abstract class MemberListFragmentProvider {

    @ContributesAndroidInjector(modules = [MemberListFragmentModule::class])
    internal abstract fun provideMemberListFragment(): MemberListFragment
}

//endregion

//region FragmentModule

@Module
class FacilityListFragmentModule {
    //

    @Provides
    fun provideFacilityListAdapter():
            FacilityListAdapter = FacilityListAdapter()
}

@Module
class MemberListFragmentModule {
    //
    @Provides
    fun provideMemberListAdapter():
            MemberListAdapter = MemberListAdapter()
}

//endregion