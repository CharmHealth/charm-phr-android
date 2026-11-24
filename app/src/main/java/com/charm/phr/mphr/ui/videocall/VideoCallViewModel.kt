package com.charm.phr.mphr.ui.videocall

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.OpenTokCredentials
import com.charm.phr.mphr.model.TeleHealthCredentials
import com.charm.phr.mphr.model.ZoomCredentials
import com.charm.phr.mphr.model.repository.AppointmentRepository

class VideoCallViewModel(val appointmentRepository: AppointmentRepository) : ViewModel() {

    var appointmentId: String? = null
    var practiceId: String? = null
    var zoomCredentials: ZoomCredentials? = null
    var teleHealthCredentials: TeleHealthCredentials? = null
    var finishOnResume: Boolean = false
    var openTokCredentials: OpenTokCredentials? = null


    var isJoinMeeting = false

    fun getVideoCallCredentialsForConsultation(consultationId: String): LiveData<Any> {
        return appointmentRepository.getVideoCallCredentialsForConsultation(consultationId , practiceId ?: "")
    }

}
