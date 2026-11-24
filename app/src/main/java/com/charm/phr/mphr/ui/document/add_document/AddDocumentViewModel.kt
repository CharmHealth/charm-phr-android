package com.charm.phr.mphr.ui.document.add_document

import androidx.lifecycle.LiveData
import com.charm.phr.mphr.model.Category
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.util.MDAddBaseViewModel

class AddDocumentViewModel(private val miscRepository: MiscRepository): MDAddBaseViewModel() {

    var isHealthPassport = false
    var document = Document()

    var categoryList = arrayListOf<Category>()

    var selectedCategory = Category()

    fun getCategories():LiveData<Any>{
        return miscRepository.fetchCategoryList()
    }

    fun addDocument(document:Document):LiveData<Any>{
        return miscRepository.uploadDocument(document)
    }
}