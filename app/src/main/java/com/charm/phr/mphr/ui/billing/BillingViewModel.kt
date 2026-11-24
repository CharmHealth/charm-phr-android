package com.charm.phr.mphr.ui.billing

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.PHRBills
import com.charm.phr.mphr.model.PHRReceipts
import com.charm.phr.mphr.model.repository.BillingRepository

class BillingViewModel(private val billingRepository: BillingRepository) : ViewModel() {

    var invoiceListLive = MediatorLiveData<Any>() //Pair<ArrayList<Messages> , Boolean>
    var isinvoiceListFetched = false
    var invoiceListPageNo = 1
    var invoiceHasMore = true
    var isInvoiceFetchComplete = true

    var receiptListLive = MediatorLiveData<Any>() //Pair<ArrayList<Messages> , Boolean>
    var isreceiptListFetched = false
    var receiptListPageNo = 1
    var receiptHasMore = true
    var isReceiptFetchComplete = true

    var isUpdateCountCallBackSent = false


    fun fetchInvoiceList(isFirst: Boolean) {
        if (isFirst) {
            invoiceListPageNo = 1
            invoiceHasMore = true
        } else {
            invoiceListPageNo++
        }

        if (invoiceHasMore) {
            invoiceListLive.addSource(billingRepository.fetchInvoiceList(invoiceListPageNo)) { resp ->
                if (resp != null) {

                    if (resp is Pair<*, *>) {
                        if (isFirst) {
                            isinvoiceListFetched = true
                        }
                        val (list: java.util.ArrayList<PHRBills>, hasMore: Boolean) = resp as Pair<ArrayList<PHRBills>, Boolean>
                        this.invoiceHasMore = hasMore
                        invoiceListLive.postValue(Pair(list, isFirst))
                    } else {
                        invoiceListLive.postValue(resp)
                    }
                }
            }
        }
    }

    fun fetchReceiptsList(isFirst: Boolean) {
        if (isFirst) {
            receiptListPageNo = 1
            receiptHasMore = true
        } else {
            receiptListPageNo++
        }

        if (receiptHasMore) {
            receiptListLive.addSource(billingRepository.fetchReceiptList(receiptListPageNo)) { resp ->
                if (resp != null) {

                    if (resp is Pair<*, *>) {
                        if (isFirst) {
                            isreceiptListFetched = true
                        }
                        val (list: java.util.ArrayList<PHRReceipts>, hasMore: Boolean) = resp as Pair<ArrayList<PHRReceipts>, Boolean>
                        this.receiptHasMore = hasMore
                        receiptListLive.postValue(Pair(list, isFirst))
                    } else {
                        receiptListLive.postValue(resp)
                    }
                }
            }
        }
    }


}