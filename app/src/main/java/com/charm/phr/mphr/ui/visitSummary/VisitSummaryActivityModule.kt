package com.charm.phr.mphr.ui.visitSummary

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.AppointmentRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class VisitSummaryActivityModule {

    @Provides
    fun provideVisitSummaryViewModel(appointmentRepository: AppointmentRepository):
            VisitSummaryViewModel = VisitSummaryViewModel(appointmentRepository)

    @Provides
    fun provideVisitSummaryViewModelProvider(visitSummaryViewModel: VisitSummaryViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(visitSummaryViewModel)
}

@Module
abstract class VisitSummaryListFragmentProvider {

    @ContributesAndroidInjector(modules = [VisitSummaryListFragmentModule::class])
    internal abstract fun provideSummaryListFragment(): VisitSummaryListFragment
}

//endregion

//region FragmentModule

@Module
class VisitSummaryListFragmentModule {
    //

    @Provides
    fun provideVisitSummaryListAdapter():
            VisitSummaryListAdapter = VisitSummaryListAdapter()

}