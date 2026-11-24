package com.charm.phr.mphr.ui.document.document_list_with_folder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.util.ModelUtil

class DocumentListForFolderViewModel(private val miscRepository: MiscRepository): ViewModel() {

    var isSharedByMe = false
    var isSharedToMe = false
    var categoryId = ""
    var categoryName = ""

    var isHealthPassport = false

    var documentLive =  MediatorLiveData<Any>()
    var isDocumentFetched = false
    var isDocumentFetchComplete = true
    private var documentPageNo = 1
    var documentHasMore = true

    var documentTypeList = arrayListOf<String>()

    var selectedDocumentTypeStr = ""

    var isSelectionMode=false

    var selectedDocumentList:ArrayList<Document> = arrayListOf()


    fun getDocumentList(isFirst: Boolean): LiveData<Any>{
        val filter = if (isSharedByMe){
            ModelUtil.documentFilterSharedbyme
        }
        else if (isSharedToMe){
            ModelUtil.documentFilterSharedtome
        }
        else{
            ""
        }
        if (isFirst){
            documentPageNo = 1
            documentHasMore = true
        }
        else{
            documentPageNo++
        }

        if (documentHasMore) {
            documentLive.addSource(miscRepository.getDocuments(false ,filter = filter ,categoryId = categoryId ,documentType = selectedDocumentTypeStr ,pageNum = documentPageNo)) {resp->
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

        return documentLive
    }

    fun deleteDocument(document: Document): LiveData<Any>{
        return miscRepository.deleteDocument(document)
    }
}