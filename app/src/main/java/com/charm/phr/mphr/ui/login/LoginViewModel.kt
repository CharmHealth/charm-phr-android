package com.charm.phr.mphr.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.model.repository.PatientRepository

class LoginViewModel (private val patientRepository: PatientRepository , private val miscRepository: MiscRepository) :ViewModel() {

    var isAuthTokenFailed: Boolean = false


    var isPatientsResponseReceived:Boolean = false
    var isMenuListResponseReceived:Boolean = false


    var isPatientsFetchSuccess:Boolean = false
    var isMenuListFetchSuccess:Boolean = false


    var isErrorDialogDisplayed = false


    fun fetchPatients():LiveData<Any>{
        return patientRepository.getPatientList()
    }



    fun fetchMenuList(): LiveData<Any>{
        return miscRepository.getMenulist()
    }
}