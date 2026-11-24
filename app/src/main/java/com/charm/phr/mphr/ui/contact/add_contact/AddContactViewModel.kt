package com.charm.phr.mphr.ui.contact.add_contact

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.Pharmacy
import com.charm.phr.mphr.model.repository.MiscRepository

class AddContactViewModel(val miscRepository: MiscRepository): ViewModel() {

    var contactList:ArrayList<Pharmacy> = arrayListOf()

    var isValueChanged=false

    fun saveContacts(): LiveData<Any> {
        return miscRepository.saveContactList(contactList)
    }
}