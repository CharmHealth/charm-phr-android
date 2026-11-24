package com.charm.phr.mphr.ui.announcement

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.Announcements
import com.charm.phr.mphr.model.repository.MiscRepository

class AnnouncementDetailViewModel (private val miscRepository: MiscRepository): ViewModel() {

    val announcement = Announcements()

    fun getAnnouncementDetail(): LiveData<Any>{
        return miscRepository.getAnnouncementDetail(announcement.announcementId ?: "",announcement.practiceId?:"")
    }
}