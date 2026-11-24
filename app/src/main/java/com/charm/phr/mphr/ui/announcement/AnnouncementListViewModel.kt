package com.charm.phr.mphr.ui.announcement

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.repository.MiscRepository

class AnnouncementListViewModel(private val miscRepository: MiscRepository): ViewModel() {

    val announcementListLive = MediatorLiveData<Any>()

    fun fetchAnnouncementList(){
        announcementListLive.addSource(miscRepository.getAnnouncementList()) {
            announcementListLive.postValue(it)
        }
    }
}

