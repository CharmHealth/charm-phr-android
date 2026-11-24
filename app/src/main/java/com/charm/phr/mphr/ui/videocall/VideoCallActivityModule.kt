package com.charm.phr.mphr.ui.videocall

import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.model.repository.AppointmentRepository
import com.charm.phr.mphr.ui.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class VideoCallActivityModule {

    @Provides
    fun provideVideoCallViewModel(appointmentRepository: AppointmentRepository): VideoCallViewModel = VideoCallViewModel(appointmentRepository)

    @Provides
    fun provideTourPageViewModelProviderFactory(videoCallViewModel: VideoCallViewModel):
            ViewModelProvider.Factory = ViewModelProviderFactory(videoCallViewModel)

}