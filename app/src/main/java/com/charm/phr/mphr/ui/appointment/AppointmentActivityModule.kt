package com.charm.phr.mphr.ui.appointment

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.AppointmentRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides

@Module
class AppointmentActivityModule {

    @Provides
    fun provideAppointmentViewModel(appointmentRepository: AppointmentRepository):
            AppointmentViewModel = AppointmentViewModel(appointmentRepository)

    @Provides
    fun provideAppointmentViewModelProviderFactory(appointmentViewModel: AppointmentViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(appointmentViewModel)

    @Provides
    fun provideUpcomingAppointmentListAdapter(picasso: Picasso):
            UpcomingAppointmentListAdapter = UpcomingAppointmentListAdapter(picasso)

    @Provides
    fun provideRequestedAppointmentListAdapter(picasso: Picasso):
            RequestedAppointmentListAdapter = RequestedAppointmentListAdapter(picasso)

    @Provides
    fun provideDeniedAppointmentListAdapter(picasso: Picasso):
            DeniedAppointmentListAdapter = DeniedAppointmentListAdapter(picasso)

    @Provides
    fun provideWaitListAppointmentListAdapter(picasso: Picasso):
            WaitListAppointmentListAdapter = WaitListAppointmentListAdapter(picasso)

    @Provides
    fun providePastAppointmentListAdapter(picasso: Picasso):
            PastAppointmentListAdapter = PastAppointmentListAdapter(picasso)
}