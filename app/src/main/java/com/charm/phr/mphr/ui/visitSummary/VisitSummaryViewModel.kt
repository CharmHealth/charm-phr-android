package com.charm.phr.mphr.ui.visitSummary

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.PatientEncounterList
import com.charm.phr.mphr.model.repository.AppointmentRepository

class VisitSummaryViewModel(private val appointmentRepository: AppointmentRepository):ViewModel() {

    var visitSummaryListLive =  MediatorLiveData<Any>() //Pair<ArrayList<Messages> , Boolean>
    var isvisitSummaryListFetched = false
    var visitSummaryListPageNo = 1
    var visitSummaryHasMore = true
    var isVisitSummaryFetchComplete = true



    
    fun fetchVisitSummaryList(isFirst: Boolean){
        if ( isFirst ) {
            visitSummaryListPageNo = 1
            visitSummaryHasMore = true
        }
        else {
            visitSummaryListPageNo++
        }

        if (visitSummaryHasMore){
            visitSummaryListLive.addSource(appointmentRepository.fetchVisitSummaryList( visitSummaryListPageNo)) {resp ->
                if(resp != null){

                    if (resp is Pair<* , *>) {
                        if (isFirst){
                            isvisitSummaryListFetched = true
                        }
                        val (list: java.util.ArrayList<PatientEncounterList>, hasMore: Boolean) = resp as Pair<ArrayList<PatientEncounterList> , Boolean>
                        this.visitSummaryHasMore = hasMore
                        visitSummaryListLive.postValue(Pair(list, isFirst))
                    }
                    else{
                        visitSummaryListLive.postValue(resp)
                    }
                }
            }
        }
    }
}