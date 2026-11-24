package com.charm.phr.mphr.ui.lab.plotDetailGraph

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.LabParamHistoryPOJO
import com.charm.phr.mphr.model.repository.LabRepository

class LabDetailsGraphViewModel(private val labRepository: LabRepository) : ViewModel() {

    var labParamHistoryPOJO: LabParamHistoryPOJO? = null

    var labParamUnit:String?=null

    var labParamName:String?=null

    var labParamReferenceValue:String?=null

    fun getLabParamHistory(paramName: String): LiveData<Any> {
        return labRepository.getLabParamHistory(paramName)
    }

}