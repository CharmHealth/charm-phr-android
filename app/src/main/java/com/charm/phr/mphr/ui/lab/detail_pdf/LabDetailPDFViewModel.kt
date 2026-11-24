package com.charm.phr.mphr.ui.lab.detail_pdf

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.repository.LabRepository

class LabDetailPDFViewModel(private val labRepository: LabRepository) : ViewModel() {

    var isLabOrder = false

    var groupId: String = ""

    var practiceId: String = ""

    var orderId: String = ""

    var filePath: String? = null

    fun getLabPDF(): LiveData<Any> {
        return if (isLabOrder) {
            labRepository.getLabOrderPDF(orderId)
        } else {
            labRepository.getLabResultPDF(groupId, practiceId)
        }
    }
}