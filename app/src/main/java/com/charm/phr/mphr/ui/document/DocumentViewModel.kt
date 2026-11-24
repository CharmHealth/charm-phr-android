package com.charm.phr.mphr.ui.document

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.Category
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.model.repository.MiscRepository

class DocumentViewModel(private val miscRepository: MiscRepository): ViewModel() {

    var isHealthPassport = false

    var documentLive =  MediatorLiveData<Any>()
    var isDocumentFetched = false
    var isDocumentFetchComplete = true
    private var documentPageNo = 1
    var documentHasMore = true


    var documentTypeList = arrayListOf<String>()
    var selectedDocumentTypeStr = ""

    var categoryLive =  MediatorLiveData<Any>() //Pair<ArrayList<PatMedPOJO> , Boolean>
    var isCategoryFetched = false
    var isCategoryFetchComplete = true

    var isSelectionMode=false

    var selectedDocumentList:ArrayList<Document> = arrayListOf()

    var isDocumentReadCallbackToBeSent = false

    fun getDocumentList(isFirst: Boolean): LiveData<Any> {

        if (isFirst){
            documentPageNo = 1
            documentHasMore = true
        }
        else{
            documentPageNo++
        }

        if (documentHasMore) {
            documentLive.addSource(miscRepository.getDocuments(isHealthPassport ,documentType = selectedDocumentTypeStr,pageNum = documentPageNo)) {resp->
                if(resp != null){
                    if (resp is Pair<* , *>) {
                        if (isFirst){
                            isDocumentFetched = true
                        }
                        val (list: java.util.ArrayList<Document>, hasMore: Boolean) = resp as Pair<ArrayList<Document> , Boolean>
                        this.documentHasMore = hasMore
                        documentLive.postValue(Pair(list, isFirst))
                    }
                    else{
                        documentLive.postValue(resp)
                    }
                }
            }
        }

        return documentLive
    }

    fun deleteDocument(document:Document): LiveData<Any> {
        return miscRepository.deleteDocument(document)
    }

    fun getCategories(): LiveData<Any>{

        categoryLive.addSource(miscRepository.fetchCategoryList() ){
            categoryLive.postValue(it)
        }

        return categoryLive
    }

    fun editCategory(category: Category): LiveData<Any>{
        return miscRepository.editCategory(category)
    }

    fun deleteCategory(category: Category): LiveData<Any>{
        return miscRepository.deleteCategory(category)
    }

    fun addCategory(category: Category): LiveData<Any>{
        return miscRepository.addCategory(category)
    }
}