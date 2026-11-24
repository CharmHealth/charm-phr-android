package com.charm.phr.mphr.ui.physician

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.Pharmacy
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.util.ModelUtil

class PhysicianViewModel(private val miscRepository: MiscRepository): ViewModel() {

    var physicianList = arrayListOf<Pharmacy>()


    fun getPhysicianList():LiveData<Any>{
        return miscRepository.fetchContacts(ModelUtil.contactTypeDoctor)
    }

    fun deletePhysician(pharmacy: Pharmacy):LiveData<Any>{
        return miscRepository.deleteContacts(pharmacy)
    }
}