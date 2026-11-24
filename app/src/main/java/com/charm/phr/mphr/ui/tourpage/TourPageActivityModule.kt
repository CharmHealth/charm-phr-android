package com.charm.phr.mphr.ui.tourpage

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.model.repository.ReminderRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class TourPageActivityModule {

    @Provides
    fun provideTourPageViewModel(patientRepository: PatientRepository, miscRepository: MiscRepository,reminderRepository: ReminderRepository): TourPageViewModel = TourPageViewModel(patientRepository , miscRepository,reminderRepository)

    @Provides
    fun provideTourPageViewModelProviderFactory(tourPageViewModel: TourPageViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(tourPageViewModel)

    @Provides
    fun providesTourPageAdapter(appContext: Context):
            TourPageAdapter = TourPageAdapter(appContext)

}