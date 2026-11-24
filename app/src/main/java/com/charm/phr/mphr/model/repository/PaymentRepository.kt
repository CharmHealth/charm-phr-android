package com.charm.phr.mphr.model.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.CardOnFilePOJO
import com.charm.phr.mphr.model.PaymentGatewayToken
import com.charm.phr.mphr.model.PaymentInfo
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.model.TransactionInfo
import com.charm.phr.mphr.util.APIUtil
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.MDCallback
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.checkOnlineAndPatientId
import com.charm.phr.mphr.util.WebService
import com.charm.phr.mphr.util.getString
import com.google.gson.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val webService: WebService,
    private val authManager: AuthManager,
    private val appContext: Context,
    private val apiUtil: APIUtil
) {

    val TAG = "PaymentRepository"

    //region Appointment

    suspend fun saveBluefinCardDetails(paymentInfo: PaymentInfo, groupId: String?): Any? {
        paymentInfo.transactionType = ModelUtil.appointmentTransactionTypeAuthorization
        return payTransaction(paymentInfo, groupId, false, false)
    }

    suspend fun makeBluefinPayment(paymentInfo: PaymentInfo, isStatement: Boolean = false): Any? {
        paymentInfo.transactionType = ModelUtil.appointmentTransactionTypeSale
        paymentInfo.transactionInfo?.practiceId ?: ""
        return payTransaction(paymentInfo, null, true, isStatement)
    }

    suspend fun checkTransactionStatus(paymentInfo: PaymentInfo): Any? {
        val transactionStatusCall = webService.getTransactionStatus(
            paymentInfo.transactionInfo?.patientId ?: "",
            paymentInfo.transactionInfo?.transactionId ?: "",
            paymentInfo.transactionInfo?.practiceId ?: "",
            paymentInfo.transactionInfo?.gateway ?: ""
        )

        val resp = apiUtil.initiateSynchronizeRequest(transactionStatusCall)
        Log.d("MDCallbackPaymentResponse", resp.toString())
        return if (resp is JsonObject) {
            val isEHRReceipt = paymentInfo.transactionInfo?.service == ModelUtil.paymentServiceAppointment
            MDParsingHelper.getReceiptIdForBluefin(resp, isEHRReceipt)
        } else {
            if (resp is APIResponseError) {
                resp.charmApiCode = ModelUtil.errorPaymentSuccessButReceiptFailedCode
            }
            resp
        }
    }

    //endregion

    //region Transaction

    private suspend fun payTransaction(paymentInfo: PaymentInfo, groupId: String?, isFromPayment: Boolean = true, isStatement: Boolean): Any? {

        if (paymentInfo.transactionInfo != null) {

            val transactionResponse = createTransaction(paymentInfo, isStatement)
            if (transactionResponse is JsonObject) {

                val postObj = MDJsonBuilder.formPaymentPostObj(paymentInfo, transactionResponse, groupId)
                Log.d("MDCallbackPaymentPost", postObj.toString())

                val call = webService.postBluefinAPI(transactionResponse.get("bluefin_url")?.asString ?: "", postObj)
                val resp = apiUtil.initiateSynchronizeRequest(call)
                Log.d("MDCallbackPaymentResponse", resp.toString())
                if (resp is JsonObject) {
                    if (isFromPayment) {
                        return checkTransactionStatus(paymentInfo)
                    } else {
                        return true
                    }
                } else {
                    return resp
                }

            } else {
                return transactionResponse
            }
        }
        return APIResponseError(
            null,
            null,
            appContext.getString(R.string.error_transaction_info_not_found)
        )
    }

    private suspend fun createTransaction(paymentInfo: PaymentInfo, isStatement: Boolean): Any? {
        var transactionResponse: Any? = ""
        val transactionInfo = paymentInfo.transactionInfo

        if (transactionInfo != null) {

            val result = checkOnlineAndPatientId(authManager, appContext)
            if (result is APIResponseError) {
                return result
            } else if (result is Pair<*, *>) {
                val patientId = result.first.toString()
                transactionInfo.patientId = patientId

                val postObj = MDJsonBuilder.formTransactionObj(transactionInfo, paymentInfo, isStatement)

                Log.d("MDCallbackPaymentPost", postObj.toString())
                val createTransactionCall = webService.createTransaction(patientId, postObj)

                val resp = apiUtil.initiateSynchronizeRequest(createTransactionCall)
                Log.d("MDCallbackPaymentResponse", resp.toString())
                if (resp is JsonObject) {
                    val outputData = resp.getAsJsonObject("output_string")
                    transactionInfo.transactionId = outputData?.getString("transactionId")
                    transactionResponse = outputData
                } else {
                    transactionResponse = resp
                }
            }
        }
        return transactionResponse
    }


    //endregion

    // region razorpay

    fun createPaymentTransaction(paymentTransaction: TransactionInfo): LiveData<Any> {

        //Note: doing all the check for paymentTransaction in activity it self

        val liveResp = MutableLiveData<Any>()

        liveResp.checkOnlineAndPatientId(authManager, appContext) { patientId, selectedPatient ->
            paymentTransaction.patientId = patientId

            val postObj = MDJsonBuilder.formTransactionObj(paymentTransaction, null, false)


            val createPaymentTransactionCall = webService.createTransaction(patientId, postObj)

            apiUtil.initiateRequest(createPaymentTransactionCall, object : MDCallback(authManager) {

                override fun onMDSuccess(respObj: JsonObject) {

                    val transactionObj = respObj.get("output_string")?.asJsonObject

                    val transactionId = transactionObj?.get("transaction_id")?.asString?.trim()
                    val razorPayOrderId = transactionObj?.get("razorpay_order_id")?.asString?.trim()
                    val clientId = transactionObj?.get("client_id")?.asString
                    val publicToken = transactionObj?.get("public_token")?.asString?.trim()
                    val amount = transactionObj?.get("amount")?.asString

                    if (!publicToken.isNullOrBlank() && !transactionId.isNullOrBlank() && !razorPayOrderId.isNullOrBlank() && !amount.isNullOrBlank()) {

                        paymentTransaction.razorPayOrderId = razorPayOrderId
                        paymentTransaction.clientId = clientId
                        paymentTransaction.publicToken = publicToken
                        paymentTransaction.transactionId = transactionId
                        paymentTransaction.paymentAmount = amount
                        paymentTransaction.mobile = transactionObj.get("mobile")?.asString
                        paymentTransaction.emailId = transactionObj.get("email")?.asString

                        liveResp.value = paymentTransaction

                    } else liveResp.value = APIResponseError(0, null, null)

                }

                override fun onMDFailure(error: APIResponseError) {
                    liveResp.value = APIResponseError(null, null, null)
                }

            })
        }

        return liveResp

    }

    fun updatePaymentTransaction(transactionId: String, practiceID: String, isEHRReceipt: Boolean = true): LiveData<Any> {

        val liveResp = MutableLiveData<Any>()

        liveResp.checkOnlineAndPatientId(authManager, appContext) { patientId, selectedPatient ->

            val createPaymentTransactionCall = webService.updateTransaction(patientId, transactionId, MDJsonBuilder.formUpdateTransactionObj(practiceID))

            apiUtil.initiateRequest(createPaymentTransactionCall, object : MDCallback(authManager) {

                override fun onMDSuccess(respObj: JsonObject) {

                    val code = respObj.get("code")?.asInt
                    val transactionObj = respObj.get("output_string")?.asJsonObject

                    val receiptDetailArr = transactionObj?.getAsJsonArray("receipt_details")

                    if (receiptDetailArr != null && receiptDetailArr.size() > 0) {
                        val receiptDetailObj = receiptDetailArr[0].asJsonObject

                        val receiptKey = if (isEHRReceipt) {
                            "ehr_receipt_id"
                        } else {
                            "phr_receipt_id"
                        }

                        val receiptId = receiptDetailObj?.get(receiptKey)?.asString

                        if (receiptId.isNullOrBlank()) {
                            liveResp.postValue(APIResponseError(code, null, null))
                        } else {
                            liveResp.postValue(receiptId)
                        }

                    } else {
                        liveResp.postValue(APIResponseError(code, null, null))
                    }
                }

                override fun onMDFailure(error: APIResponseError) {
                    liveResp.postValue(error)
                }

            })
        }

        return liveResp

    }

    //endregion

    //region Card on file

    fun getCardOnFileList(practiceList: List<PracticeList>, merchantAccountId: String = ""): LiveData<Any> {

        val liveResponse = MutableLiveData<Any>()
        liveResponse.checkOnlineAndPatientId(authManager, appContext) { patientId, selectedPatient ->
            var respCount = 0
            var isCallbackSent = false
            val cardOnFilePOJOArrList: ArrayList<CardOnFilePOJO> = ArrayList()
            if (practiceList.isEmpty()) {
                liveResponse.postValue(APIResponseError(null, null, appContext.getString(R.string.error_practice_id_not_found)))
            }
            practiceList.forEach {
                val practiceId = it.practiceId ?: ""
                val webCall = webService.getCardOnFileList(patientId, practiceId, merchantAccountId)
                apiUtil.initiateRequest(webCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        respCount++
                        val cardOnFilePOJO = MDParsingHelper.getCardsForPatient(respObj, practiceId)
                        cardOnFilePOJO.practiceId = practiceId
                        cardOnFilePOJOArrList.add(cardOnFilePOJO)
                        if (respCount == practiceList.size && !isCallbackSent) {
                            isCallbackSent = true
                            liveResponse.postValue(cardOnFilePOJOArrList)
                        }
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        respCount++
                        if (!isCallbackSent) {
                            liveResponse.postValue(error)
                            isCallbackSent = true
                        }
                        liveResponse.postValue(error)
                    }
                })
            }

        }

        return liveResponse
    }

    fun getCardOnFileForPractice(practiceList: PracticeList, merchantAccountId: String? = ""): LiveData<Any> {

        val liveResponse = MutableLiveData<Any>()
        liveResponse.checkOnlineAndPatientId(authManager, appContext) { patientId, selectedPatient ->
            val practiceId = practiceList.practiceId ?: ""
            val webCall = webService.getCardOnFileList(patientId, practiceId, merchantAccountId)
            apiUtil.initiateRequest(webCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    val cardOnFilePOJO = MDParsingHelper.getCardsForPatient(respObj, practiceId)
                    cardOnFilePOJO.practiceId = practiceId
                    liveResponse.postValue(cardOnFilePOJO)
                }

                override fun onMDFailure(error: APIResponseError) {
                    liveResponse.postValue(error)
                }
            })
        }
        return liveResponse
    }

    fun getMerchantAccountsForPractice(practiceId: String): LiveData<Any> {
        val liveResponse = MutableLiveData<Any>()
        liveResponse.checkOnlineAndPatientId(authManager, appContext) { patientId, selectedPatient ->
            val webCall = webService.getMerchantAccountForPatient(patientId, practiceId)
            apiUtil.initiateRequest(webCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    liveResponse.postValue(MDParsingHelper.getMerchantAccountList(respObj))
                }

                override fun onMDFailure(error: APIResponseError) {
                    liveResponse.postValue(error)
                }
            })
        }
        return liveResponse
    }

    fun updateCardOnFile(paymentGatewayToken: PaymentGatewayToken): LiveData<Any> {
        val liveResponse = MutableLiveData<Any>()
        liveResponse.checkOnlineAndPatientId(authManager, appContext) { patientId, selectedPatient ->
            val webCall = webService.updateCardOnFile(MDJsonBuilder.formUpdateCardOnFileObj(paymentGatewayToken))
            apiUtil.initiateRequest(webCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    liveResponse.postValue(true)
                }

                override fun onMDFailure(error: APIResponseError) {
                    liveResponse.postValue(error)
                }
            })
        }
        return liveResponse
    }

    fun deleteCardOnFile(paymentGatewayToken: PaymentGatewayToken): LiveData<Any> {
        val liveResponse = MutableLiveData<Any>()
        liveResponse.checkOnlineAndPatientId(authManager, appContext) { patientId, selectedPatient ->
            val webCall = webService.deleteCardOnFile(MDJsonBuilder.formDeleteCardOnFileObj(paymentGatewayToken))
            apiUtil.initiateRequest(webCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    liveResponse.postValue(true)
                }

                override fun onMDFailure(error: APIResponseError) {
                    liveResponse.postValue(error)
                }
            })
        }
        return liveResponse
    }

    fun groupMerchantAndPatients(practiceID: String): LiveData<Any> {
        val liveResponse = MutableLiveData<Any>()
        liveResponse.checkOnlineAndPatientId(authManager, appContext) { patientId, selectedPatient ->
            val webCall = webService.groupMerchantAndPatients(MDJsonBuilder.formGroupMerchantAndPatientsObj(patientId, practiceID))
            apiUtil.initiateRequest(webCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    liveResponse.postValue(MDParsingHelper.getGroupId(respObj))
                }

                override fun onMDFailure(error: APIResponseError) {
                    liveResponse.postValue(error)
                }

            })
        }
        return liveResponse
    }

    //endregion


}