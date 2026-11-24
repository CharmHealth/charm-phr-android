package com.charm.phr.mphr.ui.questionnaire.questionnaire_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.InsuranceCommonData
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientCommonData
import com.charm.phr.mphr.model.PatientInsuranceDetails
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.model.PracticePayer
import com.charm.phr.mphr.model.QuestionnaireDetailPOJO
import com.charm.phr.mphr.model.QuestionnaireEntryPOJO
import com.charm.phr.mphr.model.StateList
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.model.repository.QuestionnaireRepository
import com.charm.phr.mphr.util.ModelUtil
import java.util.concurrent.Executor

class QuestionnaireDetailViewModel(
    private val questionnaireRepository: QuestionnaireRepository,
    private val patientRepository: PatientRepository,
    private val executor: Executor
) : ViewModel() {


    var isHealthPassport = false
    var physicianTemplates = PhysicianTemplates()
    var questionnaireDetailPOJO = QuestionnaireDetailPOJO()
    var isDataEditAllowed = false
    var isPast = false
    var isSaved = false

    val questionnaireDetailLive = MediatorLiveData<Any>()

    var patientCommonData: PatientCommonData? = null
    var personalDetail: Patient? = null
    var patContactDetails: Patient? = null
    var stateList: ArrayList<StateList> = arrayListOf()
    val languageList = arrayListOf<String>()
    val sliderIsChangedList = arrayListOf<Int>()

    var insuranceCommonData: InsuranceCommonData? = null
    var patientInsuranceDetails: PatientInsuranceDetails? = null

    val payerList = arrayListOf<PracticePayer>()
    val payerListLive = MediatorLiveData<Any>()

    var patientDetailForInsurance: Patient? = null

    var isSaveCallInProgress = false

    var isParentSection = false

    val tagToIdMap = linkedMapOf<String, String>()
    val soapTagToIdMap = linkedMapOf<String, LinkedHashMap<String, String>>()
    var personalDetRequiredTabs = ArrayList<String>()

    fun fetchQuestionnaireDetails(): LiveData<Any> {

        questionnaireDetailLive.addSource(
            questionnaireRepository.getQuestionnaireDetail(
                physicianTemplates
            )
        ) { resp ->
            if (resp != null) {
                if (resp is QuestionnaireDetailPOJO) {
                    questionnaireDetailPOJO = resp

                    var totalRespCount = 0
                    var isPersonalDetail = false
                    var isPatContact = false
                    var isInsurance = false
                    var callbackCount = 0
                    var isCallbackSent = false


                    if (resp.questionnairEntryPOJOList.any { it.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypePersonalDetails }) {
                        totalRespCount += 3
                        isPersonalDetail = true
                    }

                    if (resp.questionnairEntryPOJOList.any { it.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypePrimaryContactDetail }) {

                        isPatContact = true

                        if (isPersonalDetail) {
                            totalRespCount++
                        } else {
                            totalRespCount += 2
                        }
                    }

                    if (resp.questionnairEntryPOJOList.any { it.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypeInsurance }) {
                        isInsurance = true
                        totalRespCount += 2
                    }


                    if (totalRespCount == 0) {
                        questionnaireDetailLive.postValue(true)
                    }

                    if (isPatContact || isPersonalDetail) {

                        questionnaireDetailLive.addSource(patientRepository.getPatientCommonData()) { commonResp ->
                            if (commonResp != null) {

                                if (commonResp is PatientCommonData) {
                                    patientCommonData = commonResp
                                    if (++callbackCount == totalRespCount && !isCallbackSent) {
                                        isCallbackSent = true
                                        questionnaireDetailLive.postValue(true)
                                    }
                                } else if (commonResp is APIResponseError) {
                                    if (!isCallbackSent) {
                                        isCallbackSent = true
                                        questionnaireDetailLive.postValue(commonResp)
                                    }
                                }
                            }
                        }

                        if (isPersonalDetail) {

                            questionnaireDetailLive.addSource(patientRepository.getLanguages()) { languageResp ->
                                if (languageResp != null) {

                                    if (languageResp is ArrayList<*>) {
                                        languageList.clear()
                                        languageList.addAll(languageResp as ArrayList<String>)

                                        if (++callbackCount == totalRespCount && !isCallbackSent) {
                                            isCallbackSent = true
                                            questionnaireDetailLive.postValue(true)
                                        }
                                    } else if (languageResp is APIResponseError) {
                                        if (!isCallbackSent) {
                                            isCallbackSent = true
                                            questionnaireDetailLive.postValue(languageResp)
                                        }
                                    }
                                }
                            }

                            questionnaireDetailLive.addSource(
                                questionnaireRepository.getPrimaryDetailsForQuestionnaire(
                                    physicianTemplates
                                )
                            ) { commonResp ->
                                if (commonResp != null) {
                                    if (commonResp is Pair<*, *>) {
                                        val patient = commonResp.first
                                        val requiredTabs = commonResp.second
                                        if (patient is Patient) {
                                            personalDetail = patient
                                            if (++callbackCount == totalRespCount && !isCallbackSent) {
                                                isCallbackSent = true
                                                questionnaireDetailLive.postValue(true)
                                            }
                                        }
                                        if (requiredTabs is ArrayList<*>) {
                                            personalDetRequiredTabs = requiredTabs as ArrayList<String>
                                        }
                                    } else if (commonResp is APIResponseError) {
                                        if (!isCallbackSent) {
                                            isCallbackSent = true
                                            questionnaireDetailLive.postValue(commonResp)
                                        }
                                    }
                                }
                            }
                        }

                        if (isPatContact) {
                            questionnaireDetailLive.addSource(
                                questionnaireRepository.getPrimaryContactDetForQuestionnaire(
                                    physicianTemplates
                                )
                            ) { commonResp ->
                                if (commonResp != null) {

                                    if (commonResp is Patient) {
                                        patContactDetails = commonResp

                                        val country = commonResp.postalAddress?.country
                                        if (!country.isNullOrBlank() && ModelUtil.patientStateList.contains(country)) {
                                            questionnaireDetailLive.addSource(
                                                patientRepository.getStates(
                                                    country
                                                )
                                            ) { stateResp ->
                                                if (stateResp != null) {
                                                    if (stateResp is ArrayList<*>) {
                                                        val statesList =
                                                            stateResp as ArrayList<String>
                                                        val currentList = stateList.filter { it.countryCode == country }
                                                        if (currentList.isNotEmpty()) {
                                                            stateList.remove(currentList.first())
                                                        }
                                                        val resStateList = StateList()
                                                        resStateList.stateList = statesList
                                                        resStateList.countryCode = country
                                                        stateList.add(resStateList)

                                                        if (++callbackCount == totalRespCount && !isCallbackSent) {
                                                            isCallbackSent = true
                                                            questionnaireDetailLive.postValue(true)
                                                        }
                                                    } else if (stateResp is APIResponseError) {
                                                        if (!isCallbackSent) {
                                                            isCallbackSent = true
                                                            questionnaireDetailLive.postValue(
                                                                stateResp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        } else {

                                            if (++callbackCount == totalRespCount && !isCallbackSent) {
                                                isCallbackSent = true
                                                questionnaireDetailLive.postValue(true)
                                            }
                                        }
                                    } else if (commonResp is APIResponseError) {
                                        if (!isCallbackSent) {
                                            isCallbackSent = true
                                            questionnaireDetailLive.postValue(commonResp)
                                        }
                                    }
                                }
                            }
                        }

                    }

                    if (isInsurance) {

                        questionnaireDetailLive.addSource(
                            patientRepository.getInsuranceCommonData(
                                physicianTemplates.practiceId ?: ""
                            )
                        ) { commonResp ->

                            if (commonResp != null) {
                                if (commonResp is InsuranceCommonData) {
                                    insuranceCommonData = commonResp
                                    if (++callbackCount == totalRespCount && !isCallbackSent) {
                                        isCallbackSent = true
                                        questionnaireDetailLive.postValue(true)
                                    }
                                } else if (commonResp is APIResponseError) {
                                    if (!isCallbackSent) {
                                        isCallbackSent = true
                                        questionnaireDetailLive.postValue(commonResp)
                                    }
                                }
                            }
                        }

                        questionnaireDetailLive.addSource(
                            questionnaireRepository.getPrimaryInsuranceForQuestionnaire(
                                physicianTemplates
                            )
                        ) { commonResp ->

                            if (commonResp != null) {
                                if (commonResp is PatientInsuranceDetails) {
                                    patientInsuranceDetails = commonResp
                                    if (++callbackCount == totalRespCount && !isCallbackSent) {
                                        isCallbackSent = true
                                        questionnaireDetailLive.postValue(true)
                                    }
                                } else if (commonResp is APIResponseError) {
                                    if (!isCallbackSent) {
                                        isCallbackSent = true
                                        questionnaireDetailLive.postValue(commonResp)
                                    }
                                }
                            }
                        }
                    }
                } else if (resp is APIResponseError) {
                    questionnaireDetailLive.postValue(resp)
                }
            }
        }

        return questionnaireDetailLive
    }

    fun saveQuestionnaireDetailPOJO(
        isSubmitted: Boolean,
        questionnaireDetailPOJO: QuestionnaireDetailPOJO
    ): LiveData<Any> {
        physicianTemplates.patientQuesMap?.isSubmitted = isSubmitted
        return questionnaireRepository.postQuestionnaireAnswer(
            physicianTemplates,
            questionnaireDetailPOJO
        )
    }

    fun addSignature(signatureEntry: QuestionnaireEntryPOJO): LiveData<Any> {
        return questionnaireRepository.addSignatureWidgetForQuestionnaire(
            physicianTemplates,
            signatureEntry
        )
    }


    fun fetchStates(country: String): LiveData<Any> {
        return patientRepository.getStates(country)
    }

    fun getPayerList(searchStr: String) {
        payerListLive.addSource(
            patientRepository.getPayerList(
                searchStr,
                physicianTemplates.practiceId ?: ""
            )
        ) { resp ->
            if (resp != null) {
                payerListLive.postValue(resp)
            }
        }
    }

    fun getPatientDetail(): LiveData<Any> {
        return patientRepository.getPatientDemographics()
    }

}


