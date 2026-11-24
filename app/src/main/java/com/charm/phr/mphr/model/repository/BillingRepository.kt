package com.charm.phr.mphr.model.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.model.PHRBills
import com.charm.phr.mphr.model.PHRReceipts
import com.charm.phr.mphr.util.APIUtil
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.MDCallback
import com.charm.phr.mphr.util.MDCallbackForMisc
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.checkOnlineAndPatientId
import com.charm.phr.mphr.util.WebService
import com.google.gson.JsonObject
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    private val webService: WebService,
    private val authManager: AuthManager,
    private val executor: Executor,
    private val appContext: Context,
    private val apiUtil: APIUtil
) {


    fun fetchInvoiceList(pageNo: Int, isUnPaid: Boolean? = null): LiveData<Any> {
        val fetchInvoiceLive = MutableLiveData<Any>()


        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val fromRange: Int = ((pageNo - 1) * ModelUtil.listPerPageCount) + 1
                val practiceList = ModelUtil.getEnabledPractices(authManager, ModelUtil.billingModelId)

                /*authManager.selectedPatientLive.value?.practiceList ?: arrayListOf<PracticeList>()

                practiceList = ArrayList( practiceList.filter{practice -> practice.isActive})*/


                val practiceSize = practiceList.size

                var respCount = 0

                var isCallbackSent = false

                val phrBillList = arrayListOf<PHRBills>()

                practiceList.forEach { practiceId ->

                    val fetchInvoiceCall = webService.getInvoiceList(patientId, practiceId, fromRange)

                    apiUtil.initiateRequest(fetchInvoiceCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {

                            respCount++
                            val invoiceList = MDParsingHelper.formInvoiceList(respObj, practiceId)

                            phrBillList.addAll(invoiceList)
                            if (respCount == practiceSize && !isCallbackSent) {
                                phrBillList.sortByDescending { it.billDate?.millis ?: 0 }
                                fetchInvoiceLive.postValue(Pair(phrBillList, false))
                                isCallbackSent = true
                            }

                        }

                        override fun onMDFailure(error: APIResponseError) {

                            respCount++
                            if (!isCallbackSent) {
                                fetchInvoiceLive.postValue(error)
                                isCallbackSent = true
                            }
                        }

                    })
                }


            } else {
                fetchInvoiceLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            fetchInvoiceLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return fetchInvoiceLive
    }

    fun getStatementDetail(statementId: String): LiveData<Any> {
        val liveResponse = MutableLiveData<Any>()
        liveResponse.checkOnlineAndPatientId(authManager, appContext) { patientId, selectedPatient ->
            val webCall = webService.getStatementDetail(patientId, statementId)
            apiUtil.initiateRequest(webCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    liveResponse.postValue(MDParsingHelper.getStatementDetail(respObj))
                }

                override fun onMDFailure(error: APIResponseError) {
                    liveResponse.postValue(error)
                }
            })

        }
        return liveResponse
    }

    fun fetchInvoicePDF(phrBillId: String, isStatement: Boolean): LiveData<Any> {
        val fetchInvoicePDFLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {


                if (isStatement) {

                    val getStatementPDF = webService.getStatementPDF(patientId, phrBillId)

                    apiUtil.initiateRequestForMisc(getStatementPDF, object : MDCallbackForMisc(authManager) {
                        override fun onMDSuccess(respObj: MDFile) {
                            fetchInvoicePDFLive.postValue(respObj)
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            fetchInvoicePDFLive.postValue(error)
                        }

                    })

                } else {
                    val fetchInvoiceDetailCall = webService.getInvoiceDetails(patientId, phrBillId)
                    apiUtil.initiateRequest(fetchInvoiceDetailCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {

                            val phrBillsObj = MDParsingHelper.getPHRBillHistoryId(respObj)


                            val fetchInvoicePDFCall = webService.getInvoiceDetailPDF(patientId, phrBillId, phrBillsObj.phrBillId ?: "")

                            apiUtil.initiateRequestForMisc(fetchInvoicePDFCall, object : MDCallbackForMisc(authManager) {
                                override fun onMDSuccess(respObj: MDFile) {
                                    fetchInvoicePDFLive.postValue(Pair(respObj, phrBillsObj))
                                }

                                override fun onMDFailure(error: APIResponseError) {
                                    fetchInvoicePDFLive.postValue(Pair(error, phrBillsObj))
                                }

                            })


                        }

                        override fun onMDFailure(error: APIResponseError) {
                            fetchInvoicePDFLive.postValue(error)
                        }

                    })
                }


            } else {
                fetchInvoicePDFLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            fetchInvoicePDFLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }



        return fetchInvoicePDFLive
    }

    fun fetchReceiptList(pageNo: Int): LiveData<Any> {
        val fetchReceiptLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val fromRange: Int = ((pageNo - 1) * ModelUtil.listPerPageCount) + 1

                val practiceList = ModelUtil.getEnabledPractices(authManager, ModelUtil.billingModelId)
                val practiceSize = practiceList.size
                var respCount = 0
                var isCallbackSent = false
                val phrReceiptsList = arrayListOf<PHRReceipts>()

                practiceList.forEach { practiceId ->
                    val fetchReceiptCall = webService.getReceiptsList(patientId, practiceId, fromRange)

                    apiUtil.initiateRequest(fetchReceiptCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {
                            respCount++
                            val receiptList = MDParsingHelper.formReceiptList(respObj, practiceId)
                            phrReceiptsList.addAll(receiptList)

                            if (respCount == practiceSize && !isCallbackSent) {
                                phrReceiptsList.sortByDescending { it.receiptDate?.millis ?: 0 }
                                fetchReceiptLive.postValue(Pair(phrReceiptsList, false))
                                isCallbackSent = true
                            }
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            respCount++

                            if (!isCallbackSent) {
                                fetchReceiptLive.postValue(error)
                                isCallbackSent = true
                            }
                        }

                    })
                }

            } else {
                fetchReceiptLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            fetchReceiptLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return fetchReceiptLive
    }


    fun fetchReceiptPDF(phrReceiptId: String): LiveData<Any> {
        val fetchReceiptPDFLive = MutableLiveData<Any>()


        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {
                val fetchReceiptPDFCall = webService.getReceiptPDF(patientId, phrReceiptId)

                apiUtil.initiateRequestForMisc(fetchReceiptPDFCall, object : MDCallbackForMisc(authManager) {
                    override fun onMDSuccess(respObj: MDFile) {
                        fetchReceiptPDFLive.postValue(respObj)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        fetchReceiptPDFLive.postValue(error)
                    }

                })
            } else {
                fetchReceiptPDFLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            fetchReceiptPDFLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return fetchReceiptPDFLive
    }

}