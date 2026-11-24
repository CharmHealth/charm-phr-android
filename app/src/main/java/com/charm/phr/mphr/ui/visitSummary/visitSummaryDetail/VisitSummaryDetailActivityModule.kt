package com.charm.phr.mphr.ui.visitSummary.visitSummaryDetail

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.AppointmentRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class VisitSummaryDetailActivityModule {

    @Provides
    fun provideVisitSummaryDetailViewModel(appointmentRepository: AppointmentRepository):
            VisitSummaryDetailViewModel = VisitSummaryDetailViewModel(appointmentRepository)

    @Provides
    fun provideVisitSummaryDetailViewModelFactory(visitSummaryDetailViewModel: VisitSummaryDetailViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory<VisitSummaryDetailViewModel>(visitSummaryDetailViewModel)
}