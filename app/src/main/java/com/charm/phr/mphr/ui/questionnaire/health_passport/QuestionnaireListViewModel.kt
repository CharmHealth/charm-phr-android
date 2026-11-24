package com.charm.phr.mphr.ui.questionnaire.health_passport

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.model.repository.QuestionnaireRepository

class QuestionnaireListViewModel(private val questionnaireRepository: QuestionnaireRepository): ViewModel() {

    val questionnaireList = arrayListOf<PhysicianTemplates>()

    fun fetchQuestionnaireList():LiveData<Any>{
        return questionnaireRepository.getQuestionnaireHPList()
    }
}