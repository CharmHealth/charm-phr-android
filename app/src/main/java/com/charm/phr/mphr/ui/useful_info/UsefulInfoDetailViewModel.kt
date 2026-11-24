package com.charm.phr.mphr.ui.useful_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.UsefulInfo
import com.charm.phr.mphr.model.repository.MiscRepository

class UsefulInfoDetailViewModel (private val miscRepository: MiscRepository): ViewModel() {

    val usefulInfo = UsefulInfo()

    fun getUsefulInfoDetail(): LiveData<Any>{
        return miscRepository.getUsefulInfoDetail(usefulInfo.infoId ?: "" , usefulInfo.practiceId ?: "" )

    }
}