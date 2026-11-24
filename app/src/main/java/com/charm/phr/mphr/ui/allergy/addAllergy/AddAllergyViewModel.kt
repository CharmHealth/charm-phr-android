package com.charm.phr.mphr.ui.allergy.addAllergy

import androidx.lifecycle.LiveData
import com.charm.phr.mphr.model.PatientAllergy
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.util.MDAddBaseViewModel

class AddAllergyViewModel(private val wellnessRepository: WellnessRepository): MDAddBaseViewModel() {

    var patientAllergyToBeEditted = PatientAllergy()

    var isHealthPassport = false

    fun getPatAllergyCommonData():LiveData<Any>{
        return wellnessRepository.getPatAllergyCommonData(practiceId)
    }

    fun addPatientAllergy(patientAllergy: PatientAllergy): LiveData<Any>{
        return wellnessRepository.addPatAllergy(patientAllergy)
    }

    fun editPatientAllergy(patientAllergy: PatientAllergy): LiveData<Any>{
        return wellnessRepository.editPatAllergy(patientAllergy)
    }


}