package com.charm.phr.mphr.ui.physician.add_physician

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.Pharmacy
import com.charm.phr.mphr.model.repository.MiscRepository

class AddPhysicianViewModel (private val miscRepository: MiscRepository): ViewModel() {

    var toEdit = false

    var isHealthPassport = false

    var physicianToBeEditted: Pharmacy  = Pharmacy()

    fun saveContacts(pharmacy: Pharmacy): LiveData<Any> {
        return miscRepository.saveContacts(pharmacy)
    }
}