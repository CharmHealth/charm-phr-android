package com.charm.phr.mphr.ui.vaccine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.VaccineImmunizationPOJO
import com.charm.phr.mphr.model.repository.WellnessRepository

class VaccineViewModel(private val wellnessRepository: WellnessRepository): ViewModel() {


    var vaccineListLive =  MediatorLiveData<Any>() //Pair<ArrayList<Messages> , Boolean>
    var isVaccineListFetched = false
    var vaccineListPageNo = 1
    var vaccineHasMore = true
    var isVaccineFetchComplete = true




    fun fetchVaccineList(isFirst: Boolean){
        if ( isFirst ) {
            vaccineListPageNo = 1
            vaccineHasMore = true
        }
        else {
            vaccineListPageNo++
        }

        if (vaccineHasMore){
            vaccineListLive.addSource(wellnessRepository.fetchVaccineList( vaccineListPageNo)) {resp ->
                if(resp != null){

                    if (resp is Pair<* , *>) {
                        if (isFirst){
                            isVaccineListFetched = true
                        }
                        val (list: java.util.ArrayList<VaccineImmunizationPOJO>, hasMore: Boolean) = resp as Pair<ArrayList<VaccineImmunizationPOJO> , Boolean>
                        this.vaccineHasMore = hasMore
                        vaccineListLive.postValue(Pair(list, isFirst))
                    }
                    else{
                        vaccineListLive.postValue(resp)
                    }
                }
            }
        }
    }

    fun deleteVaccine(vaccineImmunizationPOJO: VaccineImmunizationPOJO):LiveData<Any>{
        return wellnessRepository.deleteVaccine(vaccineImmunizationPOJO)
    }


 
}