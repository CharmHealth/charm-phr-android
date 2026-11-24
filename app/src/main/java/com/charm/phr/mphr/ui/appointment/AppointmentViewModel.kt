package com.charm.phr.mphr.ui.appointment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.repository.AppointmentRepository
import com.charm.phr.mphr.util.ModelUtil

 class AppointmentViewModel( val appointmentRepository: AppointmentRepository):ViewModel() {

    var currentTabSelected:Int = ModelUtil.appointmentUpcoming

    var isAppointmentFetched = false

    var isUpcomingAppointmentFetched:Boolean? = null
    var isPastAppointmentFetched:Boolean? = null
    var isRequestedAppointmentFetched:Boolean? = null
    var isDeniedAppointmentFetched:Boolean? = null
    var isWaitListAppointmentFetched:Boolean? = null

     var composeRestrictedList: ArrayList<ComposeMsgRestriction> = arrayListOf()
     var isComposeAvailable = false


    /* var isUpcomingAppointmentSuccess:Boolean = false
     var isPastAppointmentSuccess:Boolean = false
     var isRequestedAppointmentSuccess:Boolean = false
     var isDeniedAppointmentSuccess:Boolean = false
     var isWaitListAppointmentSuccess:Boolean = false

*/
     var isAppointmentSuccessCallbackToBeSent = false

    fun fetchUpcomingAppointment():LiveData<Any>{
        return appointmentRepository.getUpcomingAppointment()
    }

    fun fetchPastAppointment():LiveData<Any>{
        return appointmentRepository.getPastAppointment()
    }

    fun fetchRequestedAppointment():LiveData<Any>{
        return appointmentRepository.getRequestAppointment()
    }

    fun fetchDeniedAppointment():LiveData<Any>{
        return appointmentRepository.getDeniedAppointment()
    }

    fun fetchWaitingListAppointment():LiveData<Any>{
        return appointmentRepository.getWaitingListAppointment()
    }

     fun deleteAppointment(appointment: Appointment,reasonToCancel:String?): LiveData<Any>{
         return appointmentRepository.deleteAppointment(appointment,reasonToCancel)
     }

}