package com.charm.phr.mphr.ui.insurance.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.charm.phr.mphr.model.InsuranceCommonData
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientInsuranceDetails
import com.charm.phr.mphr.model.PracticePayer
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.util.MDAddBaseViewModel

class AddInsuranceViewModel(private val patientRepository: PatientRepository): MDAddBaseViewModel() {


    val payerList = arrayListOf<PracticePayer>()
    var insuranceToBeEditted = PatientInsuranceDetails()

    val payerListLive = MediatorLiveData<Any>()


    var patientDetail: Patient = Patient()

    var insuranceCommonData = InsuranceCommonData()
    fun getInsuranceCommonData(): LiveData<Any> {
        return patientRepository.getInsuranceCommonData (practiceId)
    }

    fun addInsurance(insurance: PatientInsuranceDetails): LiveData<Any>{
        return patientRepository.addInsurance(insurance)
    }

    fun editInsurance(insurance: PatientInsuranceDetails): LiveData<Any>{
        return patientRepository.editInsurance(insurance)
    }

    fun getPatientDetail(): LiveData<Any>{
        return patientRepository.getPatientDemographics()
    }

    fun getPayerList(searchStr: String){
        payerListLive.addSource(patientRepository.getPayerList(searchStr , practiceId)){resp ->
            if (resp != null){
                payerListLive.postValue(resp)
            }
        }
    }
}