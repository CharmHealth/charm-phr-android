package com.charm.phr.mphr.ui.support

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charm.phr.mphr.model.SupportData
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.util.ModelUtil

class SupportViewModel(private val miscRepository: MiscRepository) : ViewModel() {

    var currentTabSelected: Int = ModelUtil.supportAppreciate

    var supportData = SupportData()

    var appreciationList: ArrayList<String> = arrayListOf()

    var isValueChanged=false

    fun submitSupportData(fileIds: String): LiveData<Any> {
        return miscRepository.sendSupport(supportData, fileIds)
    }

    fun uploadSupportDocuments(): LiveData<Any> {
        val liveData = MutableLiveData<Any>()
        return miscRepository.formRequestBodyForSupportDocuments(viewModelScope, liveData, supportData.fileList)
    }
}