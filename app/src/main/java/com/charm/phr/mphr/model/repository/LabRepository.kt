package com.charm.phr.mphr.model.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.model.MedicalRecordGroup
import com.charm.phr.mphr.util.APIUtil
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.MDCallback
import com.charm.phr.mphr.util.MDCallbackForMisc
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.WebService
import com.google.gson.JsonObject
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LabRepository @Inject constructor(
    private val webService: WebService,
    private val authManager: AuthManager,
    private val executor: Executor,
    private val appContext: Context,
    private val apiUtil: APIUtil
) {


    fun fetchLabResultList(pageNo: Int): LiveData<Any> {

        val fetchLabResultListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val fromRange: Int = ((pageNo - 1) * ModelUtil.listPerPageCount) + 1
                val practiceList = ModelUtil.getEnabledPractices(authManager, ModelUtil.labModelId)

                val labResultList = arrayListOf<MedicalRecordGroup>()

                val practiceSize = practiceList.size

                var respCount = 0

                var isCallbackSent = false

                practiceList.forEach { practiceId ->

                    val fetchLabResultListCall =
                        webService.getLabResultList(patientId, practiceId, fromRange)

                    apiUtil.initiateRequest(
                        fetchLabResultListCall,
                        object : MDCallback(authManager) {
                            override fun onMDSuccess(respObj: JsonObject) {
                                respCount++
                                val labResultTempList = MDParsingHelper.formLabResultList(respObj)

                                labResultTempList.forEach { labResult ->
                                    if (labResult.medicalRecordEntryList.firstOrNull()?.isImportedByPatient == false) {
                                        labResult.practiceId = practiceId
                                    }
                                }

                                labResultList.addAll(labResultTempList)
                                if (respCount == practiceSize && !isCallbackSent) {
                                    val finalLabList = arrayListOf<MedicalRecordGroup>()
                                    labResultList.forEach { labResult ->

                                        if (labResult.medicalRecordEntryList.firstOrNull()?.isImportedByPatient == false) {
                                            finalLabList.add(labResult)
                                        } else {
                                            if (!finalLabList.any { it.groupId == labResult.groupId }) {
                                                finalLabList.add(labResult)
                                            }
                                        }
                                    }

                                    finalLabList.sortByDescending {
                                        it.medicalRecordEntryList.firstOrNull()?.date?.millis ?: 0
                                    }

                                    fetchLabResultListLive.postValue(Pair(finalLabList, false))
                                    isCallbackSent = true
                                }

                            }

                            override fun onMDFailure(error: APIResponseError) {
                                respCount++

                                if (!isCallbackSent) {
                                    fetchLabResultListLive.postValue(error)
                                    isCallbackSent = true
                                }
                            }
                        })
                }

            } else {
                fetchLabResultListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            fetchLabResultListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return fetchLabResultListLive
    }

    fun getLabResultDetail(groupId: String, practiceId: String): LiveData<Any> {

        val labResultDetailLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank() && groupId.isNotBlank()) {

                val labResultDetailCall = if (practiceId.isNotBlank()) {
                    webService.getLabResultDetail(patientId, groupId, practiceId)
                } else {
                    webService.getLabResultDetail(patientId, groupId, null)
                }

                apiUtil.initiateRequest(labResultDetailCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        labResultDetailLive.postValue(MDParsingHelper.formLabResultDetail(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        labResultDetailLive.postValue(error)
                    }

                })
            } else {
                labResultDetailLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        if (patientId.isBlank()) {
                            appContext.getString(R.string.error_patient_id_not_found)
                        } else {
                            appContext.getString(R.string.error_group_id_not_found)
                        }
                    )
                )
            }
        } else {
            labResultDetailLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return labResultDetailLive
    }


    fun getLabResultPDF(groupId: String, practiceId: String): LiveData<Any> {

        val labResultPDFLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank() && groupId.isNotBlank()) {
                val labResultPDFCall = if (practiceId.isNotBlank()) {
                    webService.getLabResultPDF(patientId, groupId, practiceId)
                } else {
                    webService.getLabResultPDF(patientId, groupId, null)
                }

                apiUtil.initiateRequestForMisc(
                    labResultPDFCall,
                    object : MDCallbackForMisc(authManager) {
                        override fun onMDSuccess(respObj: MDFile) {
                            labResultPDFLive.postValue(respObj)
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            labResultPDFLive.postValue(error)
                        }

                    })
            } else {
                labResultPDFLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        if (patientId.isBlank()) {
                            appContext.getString(R.string.error_patient_id_not_found)
                        } else {
                            appContext.getString(R.string.error_group_id_not_found)
                        }
                    )
                )
            }
        } else {
            labResultPDFLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return labResultPDFLive
    }

    fun fetchLabOrderList(pageNo: Int): LiveData<Any> {

        val labOrderListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

//                val fromRange: Int =  ((pageNo - 1)  *  ModelUtil.listPerPageCount) + 1
//                val practiceId = authManager.selectedPatientLive.value?.practiceList?.firstOrNull()?.practiceId ?: ""


                val labOrderListCall = webService.getLabOrderList(patientId)

                apiUtil.initiateRequest(labOrderListCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {

                        val labResultList = MDParsingHelper.formLabOrderList(respObj)

//                        val hasMoreResults = labResultList.size == ModelUtil.listPerPageCount

                        labOrderListLive.postValue(Pair(labResultList, false))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        labOrderListLive.postValue(error)
                    }
                })
            } else {
                labOrderListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            labOrderListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return labOrderListLive
    }

    fun getLabOrderPDF(labOrderId: String): LiveData<Any> {

        val labOrderPDFLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank() && labOrderId.isNotBlank()) {
                val labOrderPDFCall = webService.getLabOrderPDF(patientId, labOrderId)

                apiUtil.initiateRequestForMisc(
                    labOrderPDFCall,
                    object : MDCallbackForMisc(authManager) {
                        override fun onMDSuccess(respObj: MDFile) {
                            labOrderPDFLive.postValue(respObj)
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            labOrderPDFLive.postValue(error)
                        }

                    })
            } else {
                labOrderPDFLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        if (patientId.isBlank()) {
                            appContext.getString(R.string.error_patient_id_not_found)
                        } else {
                            appContext.getString(R.string.error_order_id_not_found)
                        }
                    )
                )
            }
        } else {
            labOrderPDFLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return labOrderPDFLive
    }

    fun getLabParamHistory(paramName: String): LiveData<Any> {

        val labParamHistoryListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                val labParamHistoryCall = webService.getLabParamHistory(patientId, paramName)

                apiUtil.initiateRequest(labParamHistoryCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {

                        val labResultList = MDParsingHelper.formLabParamHistory(respObj)
                        labParamHistoryListLive.postValue(labResultList)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        labParamHistoryListLive.postValue(error)
                    }
                })
            } else {
                labParamHistoryListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            labParamHistoryListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return labParamHistoryListLive
    }


}