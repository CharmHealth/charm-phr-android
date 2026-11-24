package com.charm.phr.mphr.ui.about

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.repository.MiscRepository

class AboutViewModel(private val miscRepository: MiscRepository): ViewModel() {

    val aboutListLive = MediatorLiveData<Any>()

    fun fetchAboutList(){
        aboutListLive.addSource(miscRepository.getAboutDetails(false)) {
            aboutListLive.postValue(it)
        }
    }
}