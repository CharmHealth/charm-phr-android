package com.charm.phr.mphr.ui.procedure

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.ProceduresMap
import com.charm.phr.mphr.model.repository.WellnessRepository

class ProcedureViewModel (private val wellnessRepository: WellnessRepository): ViewModel() {

    var procedureListLive =  MediatorLiveData<Any>() //Pair<ArrayList<Messages> , Boolean>

    var isProcedureListFetched = false
    var procedureListPageNo = 1
    var procedureHasMore = true
    var isProcedureFetchComplete = true

    var isHealthPassport = false

    fun fetchProcedureList(isFirst: Boolean){
        if ( isFirst ) {
            procedureListPageNo = 1
            procedureHasMore = true
        }
        else {
            procedureListPageNo++
        }

        if (procedureHasMore){
            procedureListLive.addSource(wellnessRepository.fetchProcedureList( procedureListPageNo , isHealthPassport)) {resp ->
                if(resp != null){

                    if (resp is Pair<* , *>) {
                        if (isFirst){
                            isProcedureListFetched = true
                        }
                        val (list: java.util.ArrayList<ProceduresMap>, hasMore: Boolean) = resp as Pair<ArrayList<ProceduresMap> , Boolean>
                        this.procedureHasMore = hasMore
                        procedureListLive.postValue(Pair(list, isFirst))
                    }
                    else{
                        procedureListLive.postValue(resp)
                    }
                }
            }
        }
    }

    fun deleteProcedure(proceduresMap: ProceduresMap):LiveData<Any>{
        return wellnessRepository.deleteProcedure(proceduresMap)
    }
}