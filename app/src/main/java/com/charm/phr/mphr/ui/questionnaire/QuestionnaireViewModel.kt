package com.charm.phr.mphr.ui.questionnaire

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.model.repository.QuestionnaireRepository
import com.charm.phr.mphr.util.ModelUtil
import org.joda.time.LocalDate

class QuestionnaireViewModel(private val questionnaireRepository: QuestionnaireRepository) :
    ViewModel() {

    val questionnaireCurrentListLive = MediatorLiveData<Any>()
    val questionnairePastListLive = MediatorLiveData<Any>()

    val questionnaireCurrentList = arrayListOf<PhysicianTemplates>()
    val questionnairePastList = arrayListOf<PhysicianTemplates>()

    var questionnaireCurrentListPage = 1
    var questionnairePastListPage = 1

    var questionnaireCurrentListHasMore = false
    var questionnairePastListHasMore = false


    var questtionnaireFromDateTime: LocalDate? = LocalDate.now().minusMonths(3)
    var questtionnaireToDateTime: LocalDate? = LocalDate.now()

    var currentSelected = ModelUtil.questionnaire3MonthFilter

    val questionnaireFilterChangeLive = MutableLiveData<Any>()

    var appointmentId: String? = null

    var consentFormIds: ArrayList<String> = arrayListOf()

    var isMultiPractice = false

    fun fetchQuestionnaireList() {
        fetchQuestionnaireCurrent()
        fetchQuestionnairePast()
    }

    fun fetchConsentFormQuestionnaires() {
        questionnaireCurrentListLive.addSource(
            questionnaireRepository.getQuestionnaireList(
                ModelUtil.questionnaireCurrentValue,
                null,
                null,
                appointmentId
            )
        ) { resp ->
            if (resp != null) {
                if (resp is ArrayList<*>) {
                    val filtered = (resp as ArrayList<*>)
                        .filterIsInstance<PhysicianTemplates>()
                        .filter { consentFormIds.contains(it.patientQuesMap?.patientQuesMapId) }
                    questionnaireCurrentList.clear()
                    questionnaireCurrentList.addAll(filtered)
                    questionnaireCurrentListLive.postValue(true)
                } else {
                    questionnaireCurrentListLive.postValue(resp)
                }
            }
        }
    }


    fun updateFilter() {
        questionnaireFilterChangeLive.postValue(true)
    }

    fun fetchQuestionnaireCurrent() {


        if (!isMultiPractice) {
            questtionnaireFromDateTime = null
            questtionnaireToDateTime = null
        }

        questionnaireCurrentListLive.addSource(
            questionnaireRepository.getQuestionnaireList(
                ModelUtil.questionnaireCurrentValue,
                questtionnaireFromDateTime,
                questtionnaireToDateTime,
                appointmentId
            )
        ) { resp ->

            if (resp != null) {
                if (resp is ArrayList<*>) {
                    questionnaireCurrentList.clear()
                    questionnaireCurrentList.addAll(resp as ArrayList<PhysicianTemplates>)
                    questionnaireCurrentListLive.postValue(true)
                } else {
                    questionnaireCurrentListLive.postValue(resp)
                }
            }

        }
    }

    fun getQuestionnaireItem(index: Int, toIncrement: Boolean = true): List<PhysicianTemplates> {


        return if (index == 0) {

            if (questionnaireCurrentList.isEmpty()) {
                return listOf<PhysicianTemplates>()
            }
            if (toIncrement) {
                questionnaireCurrentListPage++
            }
            val fromIndex =
                (questionnaireCurrentListPage - 1) * ModelUtil.questionnaireListPerPageCount
            var toIndex = (questionnaireCurrentListPage * ModelUtil.questionnaireListPerPageCount)

            if (toIndex > questionnaireCurrentList.size) {
                toIndex = questionnaireCurrentList.size

                if (toIndex < 0) {
                    toIndex = 0
                }
            }
            questionnaireCurrentList.subList(fromIndex, toIndex)
        } else {
            if (questionnairePastList.isEmpty()) {
                return listOf<PhysicianTemplates>()
            }
            if (toIncrement) {
                questionnairePastListPage++
            }
            val fromIndex =
                (questionnairePastListPage - 1) * ModelUtil.questionnaireListPerPageCount
            var toIndex = (questionnairePastListPage * ModelUtil.questionnaireListPerPageCount)

            if (toIndex > questionnairePastList.size) {
                toIndex = questionnairePastList.size

                if (toIndex < 0) {
                    toIndex = 0
                }
            }
            questionnairePastList.subList(fromIndex, toIndex)
        }

    }

    fun fetchQuestionnairePast() {

        if (!isMultiPractice) {
            questtionnaireFromDateTime = null
            questtionnaireToDateTime = null
        }

        questionnairePastListLive.addSource(
            questionnaireRepository.getQuestionnaireList(
                ModelUtil.questionnairePastValue,
                questtionnaireFromDateTime,
                questtionnaireToDateTime
            )
        ) { resp ->
            if (resp != null) {
                if (resp is ArrayList<*>) {
                    questionnairePastList.clear()
                    questionnairePastList.addAll(resp as ArrayList<PhysicianTemplates>)
                    questionnairePastListLive.postValue(true)
                } else {
                    questionnairePastListLive.postValue(resp)
                }
            }
        }
    }
}