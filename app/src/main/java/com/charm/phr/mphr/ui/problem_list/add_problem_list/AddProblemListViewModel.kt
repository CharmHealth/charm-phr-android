package com.charm.phr.mphr.ui.problem_list.add_problem_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.MedicalCondition
import com.charm.phr.mphr.model.PatientMedicalCondition
import com.charm.phr.mphr.model.repository.MiscRepository

class AddProblemListViewModel (private val miscRepository: MiscRepository): ViewModel() {

    var toEdit = false

    var isHealthPassport = false

    var problemListToBeEditted = PatientMedicalCondition()

    var medicalConditionList = arrayListOf<MedicalCondition>()

    var practiceId: String = ""

    fun getMedConditionCommonData(): LiveData<Any> {
        return miscRepository.getMedConditionCommonData(practiceId)
    }

    fun saveMedCondition(patientMedicalCondition: PatientMedicalCondition): LiveData<Any> {
        return  miscRepository.saveMedicalCondition(patientMedicalCondition)
    }
}