package com.charm.phr.mphr.ui.flowsheet

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.Flowsheets
import com.charm.phr.mphr.model.repository.QuestionnaireRepository

class FlowsheetListViewModel(private val questionnaireRepository: QuestionnaireRepository): ViewModel() {

    var flowsheetListLive =  MediatorLiveData<Any>()
    var isFlowsheetListFetched = false
    private var flowsheetListPageNo = 1
    var flowsheetHasMore = true
    var isFlowsheetFetchComplete = true

    fun fetchFlowsheetList(isFirst: Boolean) {
        if ( isFirst ) {
            flowsheetListPageNo = 1
            flowsheetHasMore = true
        }
        else {
            flowsheetListPageNo++
        }

        if (flowsheetHasMore){
            flowsheetListLive.addSource(questionnaireRepository.getFlowsheetList(  flowsheetListPageNo )) {resp ->
                if(resp != null){
                    if (resp is Pair<* , *>) {
                        if (isFirst){
                            isFlowsheetListFetched = true
                        }
                        val (list: java.util.ArrayList<Flowsheets>, hasMore: Boolean) = resp as Pair<ArrayList<Flowsheets> , Boolean>
                        this.flowsheetHasMore = hasMore
                        flowsheetListLive.postValue(Pair(list, isFirst))
                    }
                    else{
                        flowsheetListLive.postValue(resp)
                    }
                }
            }
        }

    }
}