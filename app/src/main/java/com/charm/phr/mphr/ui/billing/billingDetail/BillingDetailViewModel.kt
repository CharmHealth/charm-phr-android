package com.charm.phr.mphr.ui.billing.billingDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.PHRBills
import com.charm.phr.mphr.model.PHRStatementDetail
import com.charm.phr.mphr.model.PaymentGatewayToken
import com.charm.phr.mphr.model.PaymentInfo
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.model.TransactionInfo
import com.charm.phr.mphr.model.repository.BillingRepository
import com.charm.phr.mphr.model.repository.PaymentRepository
import com.charm.phr.mphr.util.ModelUtil

class BillingDetailViewModel(private val billingRepository: BillingRepository, private val paymentRepository: PaymentRepository) : ViewModel() {

    var billingType = ModelUtil.billingTypeInvoice

    var isStatement = false

    var phrBillId = ""
    var phrReceiptId = ""

    var facilityId = ""
    var practiceId = ""

    var paymentAmt = ""
    var paymentSymbol = ""
    var paymentGateway = ""

    var memberId = ""

    var billingPDFLive = MediatorLiveData<Any>()
    var filePath: String? = null


    var billingPaymentTransactionInfo = TransactionInfo()

    var paymentInfo = PaymentInfo()

    var phrBills = PHRBills()

    var cardOnFileList: ArrayList<PaymentGatewayToken> = arrayListOf()

    var isCardOnFileListFetched=false

    var isPartialPaymentAllowed = false

    var partialPaymentMinAmount = 0.00

    var statementDetail: PHRStatementDetail? = null

    fun getInvoicePDF(): LiveData<Any> {
        billingPDFLive.addSource(billingRepository.fetchInvoicePDF(phrBillId, isStatement)) {
            billingPDFLive.postValue(it)
        }
        return billingPDFLive
    }

    fun getReceiptPDF(): LiveData<Any> {
        billingPDFLive.addSource(billingRepository.fetchReceiptPDF(phrReceiptId)) {
            billingPDFLive.postValue(it)
        }
        return billingPDFLive
    }


    fun createTransactionForRazorPay(transactionInfo: TransactionInfo): LiveData<Any> {
        return paymentRepository.createPaymentTransaction(transactionInfo)
    }

    fun updateTransactionForRazorPay(transactionId: String): LiveData<Any> {
        return paymentRepository.updatePaymentTransaction(transactionId, practiceId, false)
    }


    suspend fun createPaymentForBluefin(): Any? {
        paymentInfo.chargeProcessingType = ModelUtil.appointmentCardProcessingTypeChargeOnly
        return paymentRepository.makeBluefinPayment(paymentInfo, isStatement)
    }

    suspend fun checkTransactionStatus(): Any? {
        return paymentRepository.checkTransactionStatus(paymentInfo)
    }

    fun getCardOnFileForPractice(practiceList: PracticeList, merchantAccountId: String?): LiveData<Any> {
        return paymentRepository.getCardOnFileForPractice(practiceList, merchantAccountId)
    }

    fun getStatementDetail(statementId: String): LiveData<Any> {
        return billingRepository.getStatementDetail(statementId)
    }
}

