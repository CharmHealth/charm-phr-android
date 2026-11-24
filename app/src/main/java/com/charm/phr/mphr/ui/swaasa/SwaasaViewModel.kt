package com.charm.phr.mphr.ui.swaasa

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.SwaasaModel
import com.charm.phr.mphr.model.repository.MiscRepository

class SwaasaViewModel (private val miscRepository: MiscRepository): ViewModel(){

    var swaasaId = ""
    var askOTP = false
    var otp = ""

    var isFlowsheet = false

    fun recordAudio(moduleList: List<SwaasaModel>): LiveData<Any>{
        return miscRepository.uploadSwaasaAudio(moduleList, swaasaId , otp)
    }

    fun getSwaasaData(otp: String? = null): LiveData<Any> {

        this.otp = otp ?: ""
        return miscRepository.getSwaasaAudioData(swaasaId , otp)
    }
}