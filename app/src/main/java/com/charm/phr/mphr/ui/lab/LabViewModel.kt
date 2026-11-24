package com.charm.phr.mphr.ui.lab

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.LabOrderDetail
import com.charm.phr.mphr.model.MedicalRecordGroup
import com.charm.phr.mphr.model.repository.LabRepository

class LabViewModel(private val labRepository: LabRepository) : ViewModel() {

    var labResultListLive = MediatorLiveData<Any>() //Pair<ArrayList<Messages> , Boolean>
    var islabResultListFetched = false
    var labResultListPageNo = 1
    var labResultHasMore = true
    var islabResultFetchComplete = true

    var labOrderListLive = MediatorLiveData<Any>() //Pair<ArrayList<Messages> , Boolean>
    var islabOrderListFetched = false
    var labOrderListPageNo = 1
    var labOrderHasMore = true
    var islabOrderFetchComplete = true

    fun fetchLabResultList(isFirst: Boolean) {
        if (isFirst) {
            labResultListPageNo = 1
            labResultHasMore = true
        } else {
            labResultListPageNo++
        }

        if (labResultHasMore) {
            labResultListLive.addSource(labRepository.fetchLabResultList(labResultListPageNo)) { resp ->
                if (resp != null) {

                    if (resp is Pair<*, *>) {
                        if (isFirst) {
                            islabResultListFetched = true
                        }
                        val (list: java.util.ArrayList<MedicalRecordGroup>, hasMore: Boolean) = resp as Pair<ArrayList<MedicalRecordGroup>, Boolean>
                        this.labResultHasMore = hasMore
                        labResultListLive.postValue(Pair(list, isFirst))
                    } else {
                        labResultListLive.postValue(resp)
                    }
                }
            }
        }
    }

    fun fetchLabOrderList(isFirst: Boolean) {
        if (isFirst) {
            labOrderListPageNo = 1
            labOrderHasMore = true
        } else {
            labOrderListPageNo++
        }

        if (labOrderHasMore) {
            labOrderListLive.addSource(labRepository.fetchLabOrderList(labOrderListPageNo)) { resp ->
                if (resp != null) {

                    if (resp is Pair<*, *>) {
                        if (isFirst) {
                            islabOrderListFetched = true
                        }
                        val (list: java.util.ArrayList<LabOrderDetail>, hasMore: Boolean) = resp as Pair<ArrayList<LabOrderDetail>, Boolean>
                        this.labOrderHasMore = hasMore
                        labOrderListLive.postValue(Pair(list, isFirst))
                    } else {
                        labOrderListLive.postValue(resp)
                    }
                }
            }
        }
    }

}