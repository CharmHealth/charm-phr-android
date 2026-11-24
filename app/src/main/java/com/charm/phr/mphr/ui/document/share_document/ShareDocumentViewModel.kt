package com.charm.phr.mphr.ui.document.share_document

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.Pharmacy
import com.charm.phr.mphr.model.repository.MiscRepository
import org.joda.time.DateTime

class ShareDocumentViewModel(val miscRepository: MiscRepository) : ViewModel() {

    var senderId = ""
    var shareDocumentList = arrayListOf<Document>()
    var memberList = arrayListOf<Member>()

    var selectedMemberList = arrayListOf<Member>()
    var memberListForList = arrayListOf<Member>()

    var pharmacyMemberList = arrayListOf<Pharmacy>()

    var selectedPharmacyMemberList = arrayListOf<Pharmacy>()

    var pharmacyMemberListForList = arrayListOf<Pharmacy>()

    var documentShareRestrictionList= arrayListOf<String>()

    fun getMembers(): LiveData<Any> {
        return miscRepository.getMembersForDocumentShare()
    }

    fun getDocumentShareRestriction(): LiveData<Any> {
        return miscRepository.checkShareDocumentRestriction()
    }

    fun getPharmacyMembers(): LiveData<Any> {
        return miscRepository.getContactForSearch()
    }

    fun shareDocumentForMember(): LiveData<Any> {
        return miscRepository.shareDocumentToMembers(documents = shareDocumentList, memberList = selectedMemberList)
    }

    fun shareDocumentForOthers(expiryDate: DateTime): LiveData<Any> {
        return miscRepository.shareDocumentToOthers(documents = shareDocumentList, contactList = selectedPharmacyMemberList, expiryDate = expiryDate)
    }


}