package com.charm.phr.mphr.ui.wellness.wellnessList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.repository.WellnessRepository

class WellnessListViewModel(private val wellnessRepository: WellnessRepository): ViewModel() {
    var wellnessTrackerListLive =  MediatorLiveData<Any>() //Pair<ArrayList<PatMedPOJO> , Boolean>
    var isWellnessTrackerListFetched = false

    var isDashboardVitalsUpdated = false

    var wellnessTrackerListStr=""

    fun getWellnessTrackerList(): LiveData<Any>{

        wellnessTrackerListLive.addSource(wellnessRepository.getVitalTrackerList()){resp ->
            if (resp != null){
                wellnessTrackerListLive.postValue(resp)
            }
        }

        return wellnessTrackerListLive
    }
}