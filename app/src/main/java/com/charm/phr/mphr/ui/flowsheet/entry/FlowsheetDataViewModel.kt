package com.charm.phr.mphr.ui.flowsheet.entry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.Flowsheets
import com.charm.phr.mphr.model.repository.QuestionnaireRepository

class FlowsheetDataViewModel(val questionnaireRepository: QuestionnaireRepository): ViewModel() {

    var flowsheetDataListLive =  MediatorLiveData<Any>()
    var isFlowsheetDataFetched = false
    var isFlowsheetDataFetchComplete = true

    var flowsheet = Flowsheets()

    fun fetchFlowsheetData():LiveData<Any>{
        flowsheetDataListLive.addSource(questionnaireRepository.getFlowsheetEntryList(flowsheet)){resp ->
            if(resp != null){
                flowsheetDataListLive.postValue(resp)
            }
        }
        return flowsheetDataListLive
    }
}