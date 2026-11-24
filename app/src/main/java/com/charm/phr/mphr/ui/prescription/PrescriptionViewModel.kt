package com.charm.phr.mphr.ui.prescription

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.model.PatMedGroupByDrugPOJO
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.PatSupplementsGroupPOJO
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.model.repository.PrescriptionRepository
import com.charm.phr.mphr.util.ModelUtil

class PrescriptionViewModel(private val prescriptionRepository: PrescriptionRepository): ViewModel() {

    var medicationListLive =  MediatorLiveData<Any>() //Pair<ArrayList<PatMedPOJO> , Boolean>
    var isMedicationListFetched = false
    private var medicationListPageNo = 1
    var medicationHasMore = true
    var isMedicationFetchComplete = true
    var medicationStatus = ModelUtil.medicationViewTypeActive


    var supplementListLive =  MediatorLiveData<Any>() //Pair<ArrayList<PatMedPOJO> , Boolean>
    var isSupplementListFetched = false
    var supplementListPageNo = 1
    var supplementHasMore = true
    var isSupplementFetchComplete = true
    var supplementStatus = ModelUtil.medicationViewTypeActive

    var isMultiSelectEnabled = false

    var isMedicationUpdated = false

    var isFromReminder=false
    var selectedReminderModelId:String?=null

    var medicationListDrugGrouped= mutableStateListOf<PatMedGroupByDrugPOJO>()
    var supplementsGroupedList= mutableStateListOf<PatSupplementsGroupPOJO>()

    fun fetchMedicationList(isFirst: Boolean){
        if ( isFirst ) {
            medicationListPageNo = 1
            medicationHasMore = true
        }
        else {
            medicationListPageNo++
        }

        if (medicationHasMore){
            medicationListLive.addSource(prescriptionRepository.getMedications(medicationStatus , medicationListPageNo)) {resp ->
                if(resp != null){
                    if (resp is Pair<* , *>) {
                        if (isFirst){
                            isMedicationListFetched = true
                        }
                        val (list: java.util.ArrayList<Messages>, hasMore: Boolean) = resp as Pair<ArrayList<Messages> , Boolean>
                        this.medicationHasMore = hasMore
                        medicationListLive.postValue(Pair(list, isFirst))
                    }
                    else{
                        medicationListLive.postValue(resp)
                    }
                }
            }
        }

    }

    fun refillMedication(medication: PatMedPOJO):LiveData<Any>{
        return prescriptionRepository.refillMedication(medication)
    }

    fun refillSupplement(patientSupplements: PatientSupplements):LiveData<Any>{
        return prescriptionRepository.refillSupplements(patientSupplements)
    }

    fun fetchSupplements(isFirst: Boolean){
        if ( isFirst ) {
            supplementListPageNo = 1
            supplementHasMore = true
        }
        else {
            supplementListPageNo++
        }

        if (supplementHasMore){
            supplementListLive.addSource(prescriptionRepository.getSupplements(supplementStatus , supplementListPageNo)) { resp ->
                if(resp != null){
                    if (resp is Pair<* , *>) {
                        if (isFirst){
                            isSupplementListFetched = true
                        }
                        val (list: java.util.ArrayList<PatientSupplements>, hasMore: Boolean) = resp as Pair<ArrayList<PatientSupplements> , Boolean>
                        this.supplementHasMore = hasMore
                        supplementListLive.postValue(Pair(list, isFirst))
                    }
                    else{
                        supplementListLive.postValue(resp)
                    }
                }
            }
        }

    }

    fun deleteMedication(patMedPOJO: PatMedPOJO):LiveData<Any>{
        return prescriptionRepository.deleteMedication(patMedPOJO)
    }

    fun deleteSupplement(patientSupplements: PatientSupplements):LiveData<Any>{
        return prescriptionRepository.deleteSupplement(patientSupplements)
    }


}

