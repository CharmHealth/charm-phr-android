package com.charm.phr.mphr.ui.allergy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.PatientAllergy
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.util.ModelUtil

class AllergyViewModel(private val wellnessRepository: WellnessRepository) : ViewModel() {

    var allergyListLive = MediatorLiveData<Any>() //Pair<ArrayList<Messages> , Boolean>
    var allergyListPageNo = 1
    var allergyHasMore = true

    var isActiveAllergyListFetched = false

    var isInActiveAllergyListFetched = false

    var resolvedAllergyListFetched = false


    var isMultiSelectEnabled = false

    var isHealthPassport = false

    var selectedFilter = ModelUtil.allergyStatusActive

     var activeAllergyList: ArrayList<PatientAllergy> = arrayListOf()
     var inActiveAllergyList: ArrayList<PatientAllergy> = arrayListOf()
     var resolvedAllergyList: ArrayList<PatientAllergy> = arrayListOf()


    fun fetchAllergyList( isFirst: Boolean) {
        if (isFirst) {
            allergyListPageNo = 1
            allergyHasMore = true
        } else {
            allergyListPageNo++
        }
        //allergyHasMore not implemented for each type separately as  hardcoded to false in response from repo
        if (allergyHasMore) {
            allergyListLive.addSource(wellnessRepository.fetchPatAllergiesList(selectedFilter, allergyListPageNo, isHealthPassport)) { resp ->
                if (resp != null) {

                    if (resp is Pair<*, *>) {
                        val (list: java.util.ArrayList<PatientAllergy>, hasMore: Boolean) = resp as Pair<ArrayList<PatientAllergy>, Boolean>
                        if (isFirst) {
                            setAllergyDataFetched(list)
                        }
                        this.allergyHasMore = hasMore
                        allergyListLive.postValue(Pair(list, isFirst))
                    } else {
                        allergyListLive.postValue(resp)
                    }
                }
            }
        }
    }

    fun setAllergyDataFetched(list: java.util.ArrayList<PatientAllergy>) {
        when (selectedFilter) {
            ModelUtil.allergyStatusActive ->{
                activeAllergyList.clear()
                activeAllergyList.addAll(list)
                isActiveAllergyListFetched = true
            }
            ModelUtil.allergyStatusInActive -> {
                inActiveAllergyList.clear()
                inActiveAllergyList.addAll(list)
                isInActiveAllergyListFetched = true
            }
            ModelUtil.allergyStatusResolved -> {
                resolvedAllergyList.clear()
                resolvedAllergyList.addAll(list)
                resolvedAllergyListFetched = true
            }
        }
    }

    fun deletePatientAllergy(patientAllergy: PatientAllergy): LiveData<Any> {
        return wellnessRepository.deletePatAllergy(patientAllergy)
    }
}