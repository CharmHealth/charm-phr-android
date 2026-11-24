package com.charm.phr.mphr.ui.insurance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.PatientInsuranceDetails
import com.charm.phr.mphr.model.repository.PatientRepository

class InsuranceListViewModel(private val patientRepository: PatientRepository): ViewModel() {

    var insuranceListLive =  MediatorLiveData<Any>() //Pair<ArrayList<Messages> , Boolean>

    var isInsuranceListFetched = false
    var insuranceListPageNo = 1
    var insuranceHasMore = true
    var isInsuranceFetchComplete = true

    fun fetchInsuranceList(isFirst: Boolean){
        if ( isFirst ) {
            insuranceListPageNo = 1
            insuranceHasMore = true
        }
        else {
            insuranceListPageNo++
        }

        if (insuranceHasMore){
            insuranceListLive.addSource(patientRepository.fetchInsuranceList( insuranceListPageNo)) {resp ->
                if(resp != null){

                    if (resp is Pair<* , *>) {
                        if (isFirst){
                            isInsuranceListFetched = true
                        }
                        val (list: java.util.ArrayList<PatientInsuranceDetails>, hasMore: Boolean) = resp as Pair<ArrayList<PatientInsuranceDetails> , Boolean>
                        this.insuranceHasMore = hasMore
                        insuranceListLive.postValue(Pair(list, isFirst))
                    }
                    else{
                        insuranceListLive.postValue(resp)
                    }
                }
            }
        }
    }

    fun deleteInsurance(insurance: PatientInsuranceDetails): LiveData<Any>{
        return patientRepository.deleteInsurance(insurance)
    }

}