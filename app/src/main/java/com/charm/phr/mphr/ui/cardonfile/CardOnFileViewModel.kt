package com.charm.phr.mphr.ui.cardonfile

import androidx.lifecycle.LiveData
import com.charm.phr.mphr.model.CardOnFilePOJO
import com.charm.phr.mphr.model.PaymentGatewayToken
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.model.repository.PaymentRepository
import com.charm.phr.mphr.util.MDAddBaseViewModel

class CardOnFileViewModel(val paymentRepository: PaymentRepository) : MDAddBaseViewModel() {

    var bluefinEnabledPractices: List<PracticeList> = listOf()
    var cardOnFilePOJOArrList: ArrayList<CardOnFilePOJO> = ArrayList()
    var practiceCardList: ArrayList<PaymentGatewayToken> = ArrayList()
    var patientCardList: ArrayList<PaymentGatewayToken> = ArrayList()
    var selectedPracticePosition =0
    var isPracticeTabSelected=true

    fun getCardOnFileList(practiceList: List<PracticeList>): LiveData<Any> {
        return paymentRepository.getCardOnFileList(practiceList)
    }

    fun getCardOnFileForPractice(practiceList:PracticeList): LiveData<Any> {
        return paymentRepository.getCardOnFileForPractice(practiceList)
    }

    fun deleteCardOnFile(paymentGatewayToken: PaymentGatewayToken): LiveData<Any> {
        return paymentRepository.deleteCardOnFile(paymentGatewayToken)
    }

}