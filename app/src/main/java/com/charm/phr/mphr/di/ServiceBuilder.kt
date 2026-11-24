package com.charm.phr.mphr.di

import com.charm.phr.mphr.service.appointmentReminder.AppointmentReminderJobService
import com.charm.phr.mphr.service.appointmentReminder.AppointmentReminderJobServiceModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilder {

    @ContributesAndroidInjector(modules = [AppointmentReminderJobServiceModule::class])
    abstract fun bindAppointmentReminderJobService(): AppointmentReminderJobService
}