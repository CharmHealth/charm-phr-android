package com.charm.phr.mphr.ui.useful_info

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.repository.MiscRepository

class UsefulInfoListViewModel(private val miscRepository: MiscRepository): ViewModel() {

    val usefulInfoListLive = MediatorLiveData<Any>()

    fun fetchUsefulInfoList(){
        usefulInfoListLive.addSource(miscRepository.getAboutDetails(true)) {
            usefulInfoListLive.postValue(it)
        }
    }
}

