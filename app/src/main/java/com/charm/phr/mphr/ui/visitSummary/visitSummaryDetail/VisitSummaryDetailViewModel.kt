package com.charm.phr.mphr.ui.visitSummary.visitSummaryDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.repository.AppointmentRepository

class VisitSummaryDetailViewModel (private val appointmentRepository: AppointmentRepository): ViewModel() {

    var visitSummaryDocumentId = ""

    var practiceId = ""

    var filePath: String? = null

    fun getVisitSummaryPDF():LiveData<Any>{
        return appointmentRepository.getVisitSummaryPDF(visitSummaryDocumentId , practiceId)
    }

}