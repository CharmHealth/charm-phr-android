package com.charm.phr.mphr.ui.demographics

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.HealthPassportModel
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.repository.PatientRepository

class PatientDemographicsViewModel(private val patientRepository: PatientRepository): ViewModel() {

    var demographicsLive = MediatorLiveData<Any>()

    var patient = Patient()

    var setResultOK = false

    fun fetchDemographics(){

        var isRespSent = false

        var patient: Patient? = null
        var hpModel: HealthPassportModel? = null

        demographicsLive.addSource(patientRepository.getPatientDemographics()){resp ->

            if (resp is Patient){
                patient = resp

                if (hpModel != null && !isRespSent){
                    patient!!.hpModelURL = hpModel!!.url
                    patient!!.passcode = hpModel!!.passcode
                    demographicsLive.postValue(patient)
                    isRespSent = true
                }
            }
            else if (resp is APIResponseError){
                if (!isRespSent){
                    demographicsLive.postValue(resp)
                    isRespSent = true
                }
            }
        }

        demographicsLive.addSource(patientRepository.getHealthPassportURL()){resp ->

            if (resp is HealthPassportModel){
                hpModel = resp

                if (!isRespSent && patient != null){
                    patient!!.hpModelURL = hpModel!!.url
                    patient!!.passcode = hpModel!!.passcode
                    demographicsLive.postValue(patient)
                    isRespSent = true
                }
            }
            else if (resp is APIResponseError){
                if (!isRespSent){
                    demographicsLive.postValue(resp)
                    isRespSent = true
                }
            }
        }

    }
}