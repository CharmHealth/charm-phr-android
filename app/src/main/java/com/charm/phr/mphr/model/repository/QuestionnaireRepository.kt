package com.charm.phr.mphr.model.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Flowsheets
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.model.PHRFlowsheets
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.model.QuestionnaireDetailPOJO
import com.charm.phr.mphr.model.QuestionnaireEntryPOJO
import com.charm.phr.mphr.util.APIUtil
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.MDCallback
import com.charm.phr.mphr.util.MDCallbackForMisc
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.WebService
import com.google.gson.JsonObject
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionnaireRepository @Inject constructor(
    private val webService: WebService,
    private val authManager: AuthManager,
    private val executor: Executor,
    private val appContext: Context,
    private val apiUtil: APIUtil
) {

    //region Questionnaire

    fun getQuestionnaireList(
        type: String,
        fromDate: LocalDate?,
        toDate: LocalDate?,
        appointmentId: String? = null
    ): LiveData<Any> {
        val questionnaireListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                var practiceId: String? = null

                val questionnaireListCall = if (fromDate != null) {
                    webService.getQuestionnaireMultiPracticeList(
                        patientId,
                        type, /*fromDate.toString(ModelUtil.charmDisplayDateFormat())*/
                        "",/* toDate.toString(ModelUtil.charmDisplayDateFormat())*/
                        ""
                    )
                } else {
                    practiceId =
                        ModelUtil.getEnabledPractices(authManager, ModelUtil.questionnaireModelId)
                            .firstOrNull()
                    webService.getQuestionnaireList(patientId, type, practiceId ?: "")
                }

                apiUtil.initiateRequest(questionnaireListCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        questionnaireListLive.postValue(
                            MDParsingHelper.formQuestionnaireList(
                                respObj,
                                practiceId,
                                appointmentId
                            )
                        )
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        questionnaireListLive.postValue(error)
                    }

                })
            } else {
                questionnaireListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }

        } else {
            questionnaireListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return questionnaireListLive
    }

    fun getQuestionnaireHPList(storeInAuthManager: Boolean = false): LiveData<Any> {
        val questionnaireListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val questionnaireListCall = webService.getQuestionnaireHPList(patientId)
                apiUtil.initiateRequest(questionnaireListCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        val questionnaireList = MDParsingHelper.formQuestionnaireHPList(respObj)
                        if (questionnaireList.isEmpty()) {
                            val populateCall = webService.populatePassport(patientId)
                            apiUtil.initiateRequest(populateCall, object : MDCallback(authManager) {
                                override fun onMDSuccess(respObj: JsonObject) {
                                    val questionnaireList2Call =
                                        webService.getQuestionnaireHPList(patientId)
                                    apiUtil.initiateRequest(
                                        questionnaireList2Call,
                                        object : MDCallback(authManager) {
                                        override fun onMDSuccess(respObj: JsonObject) {
                                            questionnaireListLive.postValue(questionnaireList)
                                            if (storeInAuthManager) {
                                                authManager.questionnaireHPList =
                                                        questionnaireList
                                            }
                                        }

                                        override fun onMDFailure(error: APIResponseError) {
                                            questionnaireListLive.postValue(error)
                                        }

                                    })
                                }

                                override fun onMDFailure(error: APIResponseError) {
                                    questionnaireListLive.postValue(error)
                                }
                            })
                        } else {
                            questionnaireListLive.postValue(questionnaireList)
                            if (storeInAuthManager) {
                                authManager.questionnaireHPList = questionnaireList
                            }
                        }
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        questionnaireListLive.postValue(error)
                    }

                })


            } else {
                questionnaireListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }

        } else {
            questionnaireListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return questionnaireListLive
    }

    fun getPrimaryInsuranceForQuestionnaire(physicianTemplates: PhysicianTemplates): LiveData<Any> {

        val insuranceLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                val insuranceCall = webService.getPrimaryInsuranceForQuestionnaire(
                    patientId, physicianTemplates.practiceId
                        ?: ""
                )

                apiUtil.initiateRequest(insuranceCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        insuranceLive.postValue(
                            MDParsingHelper.formPrimaryInsuranceForQuestionnaire(
                                respObj
                            )
                        )
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        insuranceLive.postValue(error)
                    }
                })
            } else {
                insuranceLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            insuranceLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return insuranceLive
    }

    fun getPrimaryDetailsForQuestionnaire(physicianTemplates: PhysicianTemplates): LiveData<Any> {

        val patDetailLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                val patDetailCall = webService.getPrimaryDetForQuestionnaire(
                    patientId, physicianTemplates.practiceId
                        ?: ""
                )

                apiUtil.initiateRequest(patDetailCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        patDetailLive.postValue(
                            MDParsingHelper.formPrimaryDetailForQuestionnaire(
                                respObj
                            )
                        )
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        patDetailLive.postValue(error)
                    }
                })
            } else {
                patDetailLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            patDetailLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return patDetailLive
    }

    fun getPrimaryContactDetForQuestionnaire(physicianTemplates: PhysicianTemplates): LiveData<Any> {

        val patDetailLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                val patDetailCall = webService.getPrimaryContactDetForQuestionnaire(
                    patientId, physicianTemplates.practiceId
                        ?: ""
                )

                apiUtil.initiateRequest(patDetailCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        patDetailLive.postValue(
                            MDParsingHelper.formPrimaryContactDetailForQuestionnaire(
                                respObj
                            )
                        )
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        patDetailLive.postValue(error)
                    }
                })
            } else {
                patDetailLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            patDetailLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return patDetailLive
    }


    fun getQuestionnaireDetail(physicianTemplates: PhysicianTemplates): LiveData<Any> {
        val questionnaireDetailLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val patientQuesMapId = physicianTemplates.patientQuesMap?.patientQuesMapId
                if (patientQuesMapId.isNullOrBlank()) {
                    val questionnaireDetailCall = webService.getQuestionnaireDetail(
                        physicianTemplates.templateId
                            ?: ""
                    )

                    apiUtil.initiateRequest(questionnaireDetailCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {
                            questionnaireDetailLive.postValue(
                                MDParsingHelper.formQuestionnaireDetailPOJO(
                                    respObj
                                )
                            )
                        }

                            override fun onMDFailure(error: APIResponseError) {
                                questionnaireDetailLive.postValue(error)
                            }

                        })
                } else {
                    val questionnaireAnswerCall = if (physicianTemplates.isHealthPassport) {
                        webService.getQuestionnaireAnswerForHealthPassport(
                            patientId,
                            patientQuesMapId
                        )
                    } else {
                        webService.getQuestionnaireAnswer(
                            patientId, patientQuesMapId, physicianTemplates.practiceId
                                ?: ""
                        )
                    }

                    apiUtil.initiateRequest(questionnaireAnswerCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {
                            questionnaireDetailLive.postValue(
                                MDParsingHelper.fillQuestionnaireAnswers(
                                    respObj,
                                    physicianTemplates
                                    )
                                )
                            }

                            override fun onMDFailure(error: APIResponseError) {
                                questionnaireDetailLive.postValue(error)
                            }

                        })
                    //}
                }
            } else {
                questionnaireDetailLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            questionnaireDetailLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }



        return questionnaireDetailLive
    }

    fun postQuestionnaireAnswer(
        physicianTemplates: PhysicianTemplates,
        questionnaireDetailPOJO: QuestionnaireDetailPOJO
    ): LiveData<Any> {

        val questionnairePostLive = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                physicianTemplates.patientQuesMap?.practicePatientId = patientId


                val isSecondaryQuestionnaireAvailable =
                    questionnaireDetailPOJO.questionnairEntryPOJOList.any { it.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypePersonalDetails || it.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypePrimaryContactDetail || it.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypeInsurance }


                if (isSecondaryQuestionnaireAvailable && !questionnaireDetailPOJO.saveNormalQuestionnaire) {
                    postSecondaryQuestionnaire(
                        questionnairePostLive,
                        physicianTemplates,
                        questionnaireDetailPOJO
                    )
                } else {

                    val postObj = MDJsonBuilder.formQuestionnaireDetailAnsObj(
                        physicianTemplates,
                        questionnaireDetailPOJO
                    )

                    val questionnaireAnswerCall = if (physicianTemplates.isHealthPassport) {
                        webService.postQuestionnaireAnswerForHealthPassport(postObj)
                    } else {
                        webService.postQuestionnaireAnswer(postObj)
                    }

                    Log.d("MdCallback", "questionnaire Ans Start")
                    apiUtil.initiateRequest(
                        questionnaireAnswerCall,
                        object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {
                            if (isSecondaryQuestionnaireAvailable) {
                                postSecondaryQuestionnaire(
                                        questionnairePostLive,
                                        physicianTemplates,
                                        questionnaireDetailPOJO
                                    )
                            } else {
                                questionnairePostLive.postValue(true)
                            }
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            questionnairePostLive.postValue(error)
                        }
                    })
                }
            } else {
                questionnairePostLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            questionnairePostLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return questionnairePostLive
    }

    private fun postSecondaryQuestionnaire(
        questionnairePostLive: MutableLiveData<Any>,
        physicianTemplates: PhysicianTemplates,
        questionnaireDetailPOJO: QuestionnaireDetailPOJO
    ) {

        Log.d("MdCallback", "questionnaire Widget Start")

        val postObj = MDJsonBuilder.formQuestionnaireWidgetDetailObj(
            physicianTemplates,
            questionnaireDetailPOJO
        )

        val questionnaireWidgetCall = webService.postQuestionnaireWidgetAnswer(postObj)
        apiUtil.initiateRequest(questionnaireWidgetCall, object : MDCallback(authManager) {
            override fun onMDSuccess(respObj: JsonObject) {
                questionnairePostLive.postValue(true)
            }

            override fun onMDFailure(error: APIResponseError) {
                questionnairePostLive.postValue(error)
            }
        })
    }


    fun addSignatureWidgetForQuestionnaire(
        physicianTemplates: PhysicianTemplates,
        signatureEntry: QuestionnaireEntryPOJO
    ): LiveData<Any> {

        val signatureLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val multipart = MDJsonBuilder.formSignatureObj(
                    signatureEntry.questionnaireEntry.answersList?.firstOrNull(),
                    physicianTemplates.practiceId
                )
                val signaturePost = webService.postQuestionnaireSignature(
                    physicianTemplates.patientQuesMap?.patientQuesMapId
                        ?: "",
                    signatureEntry.questionnaireEntry.entryId,
                    patientId,
                    multipart,
                    physicianTemplates.practiceId
                        ?: ""
                )

                apiUtil.initiateRequest(signaturePost, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        signatureLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        signatureLive.postValue(error)
                    }

                })

            } else {
                signatureLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            signatureLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return signatureLive
    }

    fun getSignature(
        physicianTemplates: PhysicianTemplates,
        signatureEntry: QuestionnaireEntryPOJO
    ): LiveData<Any> {

        val signatureLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val signaturePost = webService.getQuestionnaireSignature(
                    patientId,
                    physicianTemplates.patientQuesMap?.patientQuesMapId
                        ?: "",
                    signatureEntry.questionnaireEntry.entryId,
                    physicianTemplates.practiceId
                        ?: ""
                )

                apiUtil.initiateRequestForMisc(
                    signaturePost,
                    object : MDCallbackForMisc(authManager) {

                        override fun onMDSuccess(respObj: MDFile) {

                        }

                        override fun onMDFailure(error: APIResponseError) {
                            signatureLive.postValue(error)
                        }

                    })

            } else {
                signatureLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            signatureLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return signatureLive
    }

    //endregion

    //region Flowsheet

    fun getFlowsheetList(page: Int): LiveData<Any> {

        val flowSheetListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                val fromDate = DateTime.now().minusMonths(page)
                val toDate = DateTime.now().minusMonths(page - 1)

                val flowSheetListCall = webService.getFlowsheetList(patientId)
                apiUtil.initiateRequest(flowSheetListCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        val flowsheetList = MDParsingHelper.formFlowsheetList(respObj)
                        flowSheetListLive.postValue(Pair(flowsheetList, false))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        flowSheetListLive.postValue(error)
                    }
                })

            } else {
                flowSheetListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }

        } else {
            flowSheetListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return flowSheetListLive
    }

    fun getFlowsheetEntryList(flowsheets: Flowsheets): LiveData<Any> {

        val flowSheetListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val flowSheetListCall = webService.getFlowsheetEntryList(
                    patientId, flowsheets.practiceId
                        ?: "", flowsheets.flowSheetId ?: ""
                )
                apiUtil.initiateRequest(flowSheetListCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        flowSheetListLive.postValue(MDParsingHelper.formFlowsheetEntryList(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        flowSheetListLive.postValue(error)
                    }
                })

            } else {
                flowSheetListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }

        } else {
            flowSheetListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return flowSheetListLive
    }

    fun addFlowsheetEntry(phrFlowsheets: PHRFlowsheets): LiveData<Any> {

        val flowSheetListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val postObj = MDJsonBuilder.formFlowsheetEntryObj(phrFlowsheets)

                val flowSheetListCall = webService.addFlowsheetEntry(
                    patientId, phrFlowsheets.practiceId
                        ?: "", postObj
                )
                apiUtil.initiateRequest(flowSheetListCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        flowSheetListLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        flowSheetListLive.postValue(error)
                    }
                })

            } else {
                flowSheetListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }

        } else {
            flowSheetListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return flowSheetListLive
    }

    fun editFlowsheetEntry(phrFlowsheets: PHRFlowsheets): LiveData<Any> {

        val flowSheetListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val postObj = MDJsonBuilder.formFlowsheetEntryObj(phrFlowsheets)

                val flowSheetListCall = webService.updateFlowsheetEntry(
                    patientId, phrFlowsheets.practiceId
                        ?: "", phrFlowsheets.phrFlowsheetId ?: "", postObj
                )

                apiUtil.initiateRequest(flowSheetListCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        flowSheetListLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        flowSheetListLive.postValue(error)
                    }
                })

            } else {
                flowSheetListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }

        } else {
            flowSheetListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return flowSheetListLive
    }

    fun deleteFlowsheetEntry(phrFlowsheets: PHRFlowsheets): LiveData<Any> {

        val flowSheetListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val flowSheetListCall = webService.deleteFlowsheetEntry(
                    patientId, phrFlowsheets.practiceId
                        ?: "", phrFlowsheets.flowSheetId ?: "", phrFlowsheets.phrFlowsheetId ?: ""
                )

                apiUtil.initiateRequest(flowSheetListCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        flowSheetListLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        flowSheetListLive.postValue(error)
                    }
                })

            } else {
                flowSheetListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }

        } else {
            flowSheetListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return flowSheetListLive
    }

    //endregion

}