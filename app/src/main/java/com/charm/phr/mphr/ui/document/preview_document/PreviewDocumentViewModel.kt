package com.charm.phr.mphr.ui.document.preview_document

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.model.repository.MiscRepository

class PreviewDocumentViewModel(private val miscRepository: MiscRepository): ViewModel() {

    var documentID = ""
    var filePath = ""
    var fileSize = ""

    var isPDFDownloaded = false

    var mdFile: MDFile? = null

    fun getDocumentPDF():LiveData<Any>{
        return  miscRepository.getDocumentPDF(documentID)
    }
}