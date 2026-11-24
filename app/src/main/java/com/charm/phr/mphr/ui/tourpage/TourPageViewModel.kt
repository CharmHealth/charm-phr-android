package com.charm.phr.mphr.ui.tourpage

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.database.entity.FrequencyAlarmMap
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.model.repository.ReminderRepository

class TourPageViewModel (private val patientRepository: PatientRepository, private val miscRepository: MiscRepository,private val reminderRepository: ReminderRepository) : ViewModel() {

    var isAuthTokenFailed: Boolean = false


    var currentPage: Int = 0

    var isPatientsResponseReceived:Boolean = false
    var isMenuListResponseReceived:Boolean = false


    var isPatientsFetchSuccess:Boolean = false
    var isMenuListFetchSuccess:Boolean = false


    var isErrorDialogDisplayed = false

    var isEnhancePageCancel=false

    var isFirstTimePageLoaded=true

    fun fetchPatients(): LiveData<Any> {
        return patientRepository.getPatientList()
    }



    fun fetchMenuList(): LiveData<Any> {
        return miscRepository.getMenulist()
    }

    fun getAllFrequencyAlarmMap(): List<FrequencyAlarmMap>? {
        return reminderRepository.getAllFrequencyAlarmMap()
    }
}