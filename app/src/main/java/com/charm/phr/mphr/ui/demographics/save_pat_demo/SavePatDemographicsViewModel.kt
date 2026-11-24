package com.charm.phr.mphr.ui.demographics.save_pat_demo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientCommonData
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.util.ModelUtil

class SavePatDemographicsViewModel(private val patientRepository: PatientRepository): ViewModel() {

    var patient = Patient()

    var isImageCropperFragmentPushed = false

    val fetchMiscLiveData = MediatorLiveData<Any>()

    val usStates = arrayListOf<String>()
    val inStates = arrayListOf<String>()

    var patientCommonData:PatientCommonData? = null

    var isDataSaved = false
    var filePath: String? = null

    fun fetchPatientCommonData():LiveData<Any>{


        fetchMiscLiveData.addSource(patientRepository.getPatientDemographics()){resp ->

            if (resp != null){

                if (resp is Patient){
                    patient = resp

                    var isStatesFetching = false

                    var isCallbackSent = false

                    if (patient.postalAddress?.country == ModelUtil.patientInState || patient.postalAddress?.country == ModelUtil.patientUsState){
                        isStatesFetching = true
                    }

                    fetchMiscLiveData.addSource(patientRepository.getPatientCommonData()){resp ->
                        if (resp != null){

                            if (resp is PatientCommonData){

                                patientCommonData = resp
                                if (!isCallbackSent){

                                    if (isStatesFetching){
                                        if (usStates.isNotEmpty() || inStates.isNotEmpty()){
                                            fetchMiscLiveData.postValue(resp)
                                            isCallbackSent = true
                                        }
                                    }
                                    else{
                                        fetchMiscLiveData.postValue(resp)
                                        isCallbackSent = true
                                    }
                                }
                            }
                            else if (resp is APIResponseError){
                                if (!isCallbackSent){
                                    fetchMiscLiveData.postValue(resp)
                                    isCallbackSent = true
                                }
                            }
                        }

                        fetchMiscLiveData
                    }

                    if (isStatesFetching) {

                        val country = patient.postalAddress?.country
                                ?: ""
                        fetchMiscLiveData.addSource(patientRepository.getStates(country)) { resp ->
                            if (resp != null) {

                                if (resp is ArrayList<*>) {
                                    val statesList = resp as ArrayList<String>
                                    if (country == ModelUtil.patientInState){
                                        inStates.addAll(statesList)
                                    }
                                    else if (country == ModelUtil.patientUsState){
                                        usStates.addAll(statesList)
                                    }

                                    if (!isCallbackSent && patientCommonData != null){
                                        fetchMiscLiveData.postValue(patientCommonData)
                                        isCallbackSent = true
                                    }

                                }
                                else if (resp is APIResponseError){
                                    if (!isCallbackSent){
                                        fetchMiscLiveData.postValue(resp)
                                        isCallbackSent = true
                                    }
                                }
                            }
                        }
                    }

                }
                else if (resp is APIResponseError){
                    fetchMiscLiveData.postValue(resp)
                }
            }
        }





    return fetchMiscLiveData
    }

    fun fetchStates(country: String): LiveData<Any>{
        return patientRepository.getStates(country)
    }

    fun savePatient(patient: Patient): LiveData<Any>{
        return patientRepository.savePatientDemographics(patient)
    }
}