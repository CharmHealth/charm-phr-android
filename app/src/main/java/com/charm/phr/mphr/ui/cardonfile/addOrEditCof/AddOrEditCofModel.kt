package com.charm.phr.mphr.ui.cardonfile.addOrEditCof

import androidx.lifecycle.LiveData
import com.charm.phr.mphr.model.DataWithId
import com.charm.phr.mphr.model.MerchantAccountInfo
import com.charm.phr.mphr.model.PaymentGatewayToken
import com.charm.phr.mphr.model.PaymentInfo
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.model.repository.PaymentRepository
import com.charm.phr.mphr.util.MDAddBaseViewModel

class AddOrEditCofModel(private val paymentRepository: PaymentRepository, val patientRepository: PatientRepository) : MDAddBaseViewModel() {

    var selectedPaymentGatewayToken: PaymentGatewayToken? = null

    var bluefinEnabledPractices: List<PracticeList> = listOf()

    var countryList = arrayListOf<DataWithId>()

    var merchantAccountInfo = arrayListOf<MerchantAccountInfo>()

    var addSuccessCount=0

    var isDataEdited = false

    var selectedPracticePosition=0

    var errorMessages:String?=null

    fun updateCardOnFile(paymentGatewayToken: PaymentGatewayToken): LiveData<Any> {
        return paymentRepository.updateCardOnFile(paymentGatewayToken)
    }


    fun getMerchantAccountsForPractice(practiceId: String): LiveData<Any> {
        return paymentRepository.getMerchantAccountsForPractice(practiceId)
    }

    suspend fun saveCardDetails(paymentInfo: PaymentInfo, groupId: String): Any?{
        return paymentRepository.saveBluefinCardDetails(paymentInfo, groupId)
    }

    fun groupMerchantAndPatients(): LiveData<Any> {
        return paymentRepository.groupMerchantAndPatients(selectedPaymentGatewayToken?.practiceId ?: "")
    }
}