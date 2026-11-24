package com.charm.phr.mphr.ui.health_passport

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.repository.QuestionnaireRepository

class HealthPassportViewModel(private val questionnaireRepository: QuestionnaireRepository): ViewModel() {

    var isMedicationUpdated = false

    fun getQuestionnaireList():LiveData<Any>{
        return questionnaireRepository.getQuestionnaireHPList(true)
    }
}