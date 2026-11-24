package com.charm.phr.mphr.ui.lab.resultDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.MedicalRecordGroup
import com.charm.phr.mphr.model.repository.LabRepository

class LabResultDetailViewModel(private val labRepository: LabRepository) : ViewModel() {

    var groupId: String = ""

    var practiceId: String = ""

    var interpretation:String = ""

    var isMedicalRecordDetailFetch = false

    var labResultDetailLive = MutableLiveData<MedicalRecordGroup>()

    fun getLabResultDetail(): LiveData<Any> {
        return labRepository.getLabResultDetail(groupId, practiceId)
    }

}