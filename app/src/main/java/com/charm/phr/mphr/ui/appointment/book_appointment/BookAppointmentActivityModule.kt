package com.charm.phr.mphr.ui.appointment.book_appointment

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.AppointmentRepository
import com.charm.phr.mphr.model.repository.PaymentRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides


@Module
class BookAppointmentActivityModule {

    @Provides
    fun provideBookAppointmentViewModel(appointmentRepository: AppointmentRepository , paymentRepository: PaymentRepository):
            BookAppointmentViewModel = BookAppointmentViewModel(appointmentRepository , paymentRepository)

    @Provides
    fun provideBookAppointmentViewModelProvider(bookAppointmentViewModel: BookAppointmentViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(bookAppointmentViewModel)
}