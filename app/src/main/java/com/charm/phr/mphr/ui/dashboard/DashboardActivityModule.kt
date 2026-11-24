package com.charm.phr.mphr.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.AppointmentRepository
import com.charm.phr.mphr.model.repository.BillingRepository
import com.charm.phr.mphr.model.repository.MessageRepository
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.model.repository.PrescriptionRepository
import com.charm.phr.mphr.model.repository.QuestionnaireRepository
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import com.charm.phr.mphr.util.AuthManager
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class DashboardActivityModule {

    @Provides
    fun provideDashboardViewModel(
        patientRepository: PatientRepository,
        prescriptionRepository: PrescriptionRepository,
        appointmentRepository: AppointmentRepository,
        miscRepository: MiscRepository,
        wellnessRepository: WellnessRepository,
        messageRepository: MessageRepository,
        questionnaireRepository: QuestionnaireRepository,
        billingRepository: BillingRepository
    ): DashboardViewModel = DashboardViewModel(
        patientRepository,
        prescriptionRepository,
        appointmentRepository,
        miscRepository,
        wellnessRepository,
        messageRepository,
        questionnaireRepository,
        billingRepository
    )

    @Provides
    fun provideDashboardViewModelProviderFactory(dashboardViewModel: DashboardViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(dashboardViewModel)

    @Provides
    fun provideDashboardViewPagerAdapter(dashboardActivity: DashboardActivity):
            DashboardViewPagerAdapter = DashboardViewPagerAdapter(dashboardActivity)

    @Provides
    fun provideDashboardNavigationPatListAdapter(
        picasso: Picasso,
        appContext: Context,
        authManager: AuthManager
    ):
            DashboardNavigationPatListAdapter =
        DashboardNavigationPatListAdapter(picasso, appContext, authManager)

}

//region FragmentProvider

@Module
internal abstract class HomeFragmentProvider {

    @ContributesAndroidInjector(modules = [HomeFragmentModule::class])
    internal abstract fun provideHomeFragment(): HomeFragment
}

@Module
internal abstract class HealthDataFragmentProvider {
    @ContributesAndroidInjector(modules = [HealthDataFragmentModule::class])
    internal abstract fun provideWellnessFragment(): HealthDataFragment
}

//endregion

//region FragmentModule

@Module
class HomeFragmentModule {
    //

    @Provides
    fun provideUpcomingAppointmentMonthListAdapter(picasso: Picasso):
            UpcomingAppointmentMonthListAdapter = UpcomingAppointmentMonthListAdapter(picasso)

    @Provides
    fun provideActiveMedicationsListAdapter():
            ActiveMedicationListAdapter = ActiveMedicationListAdapter()

    @Provides
    fun provideUTodayAppointmentListAdapter(picasso: Picasso):
            TodayAppointmentListAdapter = TodayAppointmentListAdapter(picasso)

    @Provides
    fun provideInvoiceListAdapter():
            InvoiceListAdapter = InvoiceListAdapter()

    @Provides
    fun provideMessageListAdapter(picasso: Picasso, authManager: AuthManager):
            RecentMessageListAdapter = RecentMessageListAdapter(picasso, authManager)

    @Provides
    fun provideQuestionnaireListAdapter():
            QuestionnaireListAdapter = QuestionnaireListAdapter()
}

@Module
class HealthDataFragmentModule {
//
}

//endregion