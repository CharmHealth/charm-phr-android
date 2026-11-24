package com.charm.phr.mphr.model.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Announcements
import com.charm.phr.mphr.model.Category
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.model.DocumentMetaData
import com.charm.phr.mphr.model.Facility
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientMedicalCondition
import com.charm.phr.mphr.model.Pharmacy
import com.charm.phr.mphr.model.ShareDocRestriction
import com.charm.phr.mphr.model.SupportData
import com.charm.phr.mphr.model.SupportDocument
import com.charm.phr.mphr.model.SwaasaModel
import com.charm.phr.mphr.model.UnreadCount
import com.charm.phr.mphr.model.UsefulInfo
import com.charm.phr.mphr.util.APIUtil
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDCallback
import com.charm.phr.mphr.util.MDCallbackForMisc
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.WebService
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.joda.time.DateTime
import retrofit2.Call
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MiscRepository @Inject constructor(
    private val webService: WebService,
    private val authManager: AuthManager,
    private val executor: Executor,
    private val appContext: Context,
    private val apiUtil: APIUtil
) {

    var contactListCall: Call<JsonElement>? = null

    val TAG = "MiscRepository"

    //region Announcements

    fun getAnnouncementList(): LiveData<Any> {
        val announcementlistLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val announcementList = arrayListOf<Announcements>()

                val practiceList = ModelUtil.getEnabledPractices(authManager, ModelUtil.announcementModelId)
                val practiceSize = practiceList.size

                var respCount = 0

                var isCallbackSent = false

                practiceList.forEach { practiceId ->


                    val announcementListCall = webService.getAnnouncementList(patientId, practiceId)

                    apiUtil.initiateRequest(announcementListCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {
                            respCount++
                            announcementList.addAll(MDParsingHelper.formAnnouncementList(respObj))
                            if (respCount == practiceSize && !isCallbackSent) {
                                isCallbackSent = true
                                announcementList.sortByDescending { it.time?.millis ?: 0 }
                                announcementlistLive.postValue(announcementList)
                            }
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            if (!isCallbackSent) {
                                isCallbackSent = true
                                announcementlistLive.postValue(error)
                            }
                        }

                    })
                }

                if (practiceList.isEmpty()) {
                    announcementlistLive.postValue(announcementList)
                }
            } else {
                announcementlistLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            announcementlistLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return announcementlistLive
    }

    fun getAnnouncementDetail(practiceAnnouncementId: String,practiceId: String): LiveData<Any> {
        val announcementDetailLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val announcementDetailCall = webService.getAnnouncementDetail(practiceAnnouncementId,practiceId)

            apiUtil.initiateRequest(announcementDetailCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {

                    val announcementList = MDParsingHelper.formAnnouncementDetailStr(respObj)


                    announcementDetailLive.postValue(announcementList.trim())
                }

                override fun onMDFailure(error: APIResponseError) {
                    announcementDetailLive.postValue(error)
                }
            })

        } else {
            announcementDetailLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return announcementDetailLive
    }

    fun getAboutDetails(isUsefulInfo: Boolean = true): LiveData<Any> {
        val announcementlistLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {
                val usefulInfoList = arrayListOf<UsefulInfo>()

                val facilityList = arrayListOf<Facility>()

                val memberList = arrayListOf<Member>()


                val practiceList = ModelUtil.getActivePractices(authManager)
                val practiceSize = practiceList.size

                var respCount = 0

                var isCallbackSent = false

                practiceList.forEach { practiceId ->


                    val aboutCall = webService.getAboutDetail(practiceId, patientId)

                    apiUtil.initiateRequest(aboutCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {
                            respCount++

                            if (isUsefulInfo) {
                                usefulInfoList.addAll(MDParsingHelper.formUsefulInfoList(respObj))
                            } else {
                                val pair = MDParsingHelper.formAboutDetail(respObj)
                                facilityList.addAll(pair.first)
                                memberList.addAll(pair.second)
                            }

                            if (respCount == practiceSize && !isCallbackSent) {
                                isCallbackSent = true
                                announcementlistLive.postValue(
                                    if (isUsefulInfo) {
                                        usefulInfoList
                                    } else {
                                        Pair(facilityList, memberList)
                                    }
                                )
                            }
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            if (!isCallbackSent) {
                                isCallbackSent = true
                                announcementlistLive.postValue(error)
                            }
                        }

                    })
                }

                if (practiceList.isEmpty()) {
                    announcementlistLive.postValue(
                        if (isUsefulInfo) {
                            usefulInfoList
                        } else {
                            Pair(facilityList, memberList)
                        }
                    )
                }
            } else {
                announcementlistLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            announcementlistLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return announcementlistLive
    }

    fun getUsefulInfoDetail(infoId: String, practiceId: String): LiveData<Any> {
        val usefulInfoDetailLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val usefulInfoDetailCall = webService.getUsefulInfoDetail(infoId, practiceId)

            apiUtil.initiateRequest(usefulInfoDetailCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {

                    val usefulInfoDetail = MDParsingHelper.formUsefulInfoDetailStr(respObj)

                    usefulInfoDetailLive.postValue(usefulInfoDetail.trim())
                }

                override fun onMDFailure(error: APIResponseError) {
                    usefulInfoDetailLive.postValue(error)
                }
            })

        } else {
            usefulInfoDetailLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return usefulInfoDetailLive
    }


    //endregion

    //region Contacts

    fun fetchContacts(contactType: String): LiveData<Any> {
        val contactListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val contactListCall = webService.getContactList(patientId, contactType)

                apiUtil.initiateRequest(contactListCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        contactListLive.postValue(MDParsingHelper.formContactList(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        contactListLive.postValue(error)
                    }
                })
            } else {
                contactListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            contactListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return contactListLive
    }

    fun getContactForSearch(): LiveData<Any> {
        val contactListLive = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                contactListCall?.cancel()
                contactListCall = webService.getContactForSearch(patientId)

                apiUtil.initiateRequest(contactListCall!!, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        contactListLive.postValue(MDParsingHelper.formContactListForSearch(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        contactListLive.postValue(error)
                    }
                })
            } else {
                contactListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            contactListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return contactListLive
    }

    fun saveContacts(pharmacy: Pharmacy): LiveData<Any> {
        val saveContactLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                pharmacy.patientId = patientId

                val postBody = MDJsonBuilder.formSaveContactObj(pharmacy)

                val saveContactCall = webService.saveContact(postBody)

                apiUtil.initiateRequest(saveContactCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        saveContactLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        saveContactLive.postValue(error)
                    }
                })
            } else {
                saveContactLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            saveContactLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return saveContactLive
    }

    fun saveContactsList(pharmacy: Pharmacy): LiveData<Any> {
        val saveContactLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                pharmacy.patientId = patientId

                val postBody = MDJsonBuilder.formSaveContactObj(pharmacy)

                val saveContactCall = webService.saveContact(postBody)

                apiUtil.initiateRequest(saveContactCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        saveContactLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        saveContactLive.postValue(error)
                    }
                })
            } else {
                saveContactLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            saveContactLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return saveContactLive
    }

    fun deleteContacts(pharmacy: Pharmacy): LiveData<Any> {
        val deleteContactLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val deleteContactCall = webService.deleteContact(patientId, pharmacy.pharmacyId ?: "")

                apiUtil.initiateRequest(deleteContactCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        deleteContactLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        deleteContactLive.postValue(error)
                    }
                })
            } else {
                deleteContactLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            deleteContactLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return deleteContactLive
    }

    //endregion

    //region Documents

    fun fetchCategoryList(): LiveData<Any> {
        val categoryListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val practiceList = ModelUtil.getEnabledPractices(authManager, ModelUtil.medicationModelId)


                val practiceSize = practiceList.size
                var respCount = 0
                var isCallbackSent = false


                val documentMetaData = DocumentMetaData()

                documentMetaData.typeList.add(ModelUtil.documentTypeAll)

                practiceList.forEach { practiceId ->

                    val categoryListCall = webService.getCategoryList(patientId, practiceId)

                    apiUtil.initiateRequest(categoryListCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {
                            val tempDocumentMetaData = MDParsingHelper.formCategoryList(respObj)

                            respCount++

                            if (documentMetaData.categoryList.isEmpty()) {
                                documentMetaData.categoryList.addAll(tempDocumentMetaData.categoryList)
                            }

                            documentMetaData.typeList.addAll(tempDocumentMetaData.typeList)

                            if (respCount == practiceSize && !isCallbackSent) {
                                isCallbackSent = true

                                if (documentMetaData.typeList.size == 1) {
                                    documentMetaData.typeList.clear()
                                }
                                categoryListLive.postValue(documentMetaData)
                            }
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            if (!isCallbackSent) {
                                isCallbackSent = true
                                categoryListLive.postValue(error)
                            }
                        }
                    })
                }
            } else {
                categoryListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            categoryListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return categoryListLive
    }

    fun addCategory(category: Category): LiveData<Any> {
        val addCategoryLive = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {

            val postObj = MDJsonBuilder.formCategoryObj(category)

            val addCategoryCall = webService.addCategory(postObj)
            apiUtil.initiateRequest(addCategoryCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    addCategoryLive.postValue(true)
                }

                override fun onMDFailure(error: APIResponseError) {
                    addCategoryLive.postValue(error)
                }
            })
        } else {
            addCategoryLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return addCategoryLive
    }

    fun editCategory(category: Category): LiveData<Any> {
        val editCategoryLive = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {

            val postObj = MDJsonBuilder.formCategoryObj(category)

            val addCategoryCall = webService.editCategory(postObj)
            apiUtil.initiateRequest(addCategoryCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    editCategoryLive.postValue(true)
                }

                override fun onMDFailure(error: APIResponseError) {
                    editCategoryLive.postValue(error)
                }
            })
        } else {
            editCategoryLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return editCategoryLive
    }

    fun deleteCategory(category: Category): LiveData<Any> {
        val deleteCategoryLive = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {

            val postObj = MDJsonBuilder.formDeleteCategoryObj(category)

            val addCategoryCall = webService.deleteCategory(postObj, category.categoryId ?: "")
            apiUtil.initiateRequest(addCategoryCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    deleteCategoryLive.postValue(true)
                }

                override fun onMDFailure(error: APIResponseError) {
                    deleteCategoryLive.postValue(error)
                }
            })
        } else {
            deleteCategoryLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return deleteCategoryLive
    }

    fun getMembersForDocumentShare(): LiveData<Any> {
        val memberListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val contactListCall = webService.getMemberList(patientId)

                apiUtil.initiateRequest(contactListCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        memberListLive.postValue(MDParsingHelper.formMemberList(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        memberListLive.postValue(error)
                    }
                })
            } else {
                memberListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            memberListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return memberListLive
    }


    fun getDocuments(isHealthPassport: Boolean, filter: String? = null, categoryId: String? = null, documentType: String, pageNum: Int = 1): LiveData<Any> {
        val documentListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val fromRange: Int = ((pageNum - 1) * ModelUtil.documentlistPerPageCount) + 1
                val contactListCall = webService.getDocumentList(
                    patientId, filter ?: "", categoryId
                        ?: "", documentType, fromRange
                )


                apiUtil.initiateRequest(contactListCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        val documentList = MDParsingHelper.formDocumentList(respObj, isHealthPassport)

                        Log.d("documentListSize", "${documentList.size}")
                        documentListLive.postValue(Pair(documentList, documentList.size == ModelUtil.documentlistPerPageCount))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        documentListLive.postValue(error)
                    }
                })
            } else {
                documentListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            documentListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return documentListLive
    }

    fun uploadDocument(document: Document): LiveData<Any> {
        val uploadDocumentLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val postBody = MDJsonBuilder.formDocumentObj(document)

                if (!postBody.isNullOrEmpty()) {
                    val uploadDocumentCall = webService.uploadDocument(postBody, patientId)

                    apiUtil.initiateRequest(uploadDocumentCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {
                            uploadDocumentLive.postValue(true)
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            uploadDocumentLive.postValue(error)
                        }
                    })
                } else {
                    uploadDocumentLive.postValue(
                        APIResponseError(
                            null,
                            null,
                            appContext.getString(R.string.error_add_document_req_failed)
                        )
                    )
                }

            } else {
                uploadDocumentLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            uploadDocumentLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return uploadDocumentLive
    }

    fun getDocumentPDF(documentId: String): LiveData<Any> {
        val getDocumentPDFLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val getDocumentPDFCall = webService.getDocument(patientId, documentId)

                apiUtil.initiateRequestForMisc(getDocumentPDFCall, object : MDCallbackForMisc(authManager) {
                    override fun onMDSuccess(respObj: MDFile) {
                        getDocumentPDFLive.postValue(respObj)
                    }


                    override fun onMDFailure(error: APIResponseError) {
                        getDocumentPDFLive.postValue(error)
                    }
                })
            } else {
                getDocumentPDFLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            getDocumentPDFLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return getDocumentPDFLive
    }

    fun deleteDocument(document: Document): LiveData<Any> {
        val deleteDocumentLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val deleteDocumentCall = webService.deleteDocument(patientId, document.fileId ?: "")

                apiUtil.initiateRequest(deleteDocumentCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        deleteDocumentLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        deleteDocumentLive.postValue(error)
                    }
                })
            } else {
                deleteDocumentLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            deleteDocumentLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return deleteDocumentLive
    }

    fun shareDocumentToMembers(documents: ArrayList<Document>, memberList: ArrayList<Member>): LiveData<Any> {
        val shareDocumentLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                val postObj = MDJsonBuilder.formShareDocumentToMembersObj(documents, memberList)
                val shareDocumentCall = webService.shareDocumentToMembers(patientId, postObj)

                apiUtil.initiateRequest(shareDocumentCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        shareDocumentLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        shareDocumentLive.postValue(error)
                    }
                })
            } else {
                shareDocumentLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            shareDocumentLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return shareDocumentLive
    }

    fun saveContactList(contactList: ArrayList<Pharmacy>): LiveData<Any> {
        val saveContactListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                contactList.forEach { it.patientId = patientId }
                val addContactObj = MDJsonBuilder.formContactObjList(contactList)
                val addContactCall = webService.saveContactList(patientId,addContactObj)
                apiUtil.initiateRequest(addContactCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        val responseArray = MDParsingHelper.formContactArrayFromOthersAdd(respObj)
                        saveContactListLive.postValue(responseArray)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        saveContactListLive.postValue(error)
                    }
                })
            }
        } else {
            saveContactListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return saveContactListLive
    }

    fun shareDocumentToOthers(documents: ArrayList<Document>, contactList: ArrayList<Pharmacy>, expiryDate: DateTime): LiveData<Any> {
        val shareDocumentLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                contactList.forEach { it.patientId = patientId }

                val addContactList = contactList.filter { it.patientPharmaMapId.isNullOrBlank() }

                if (addContactList.isNotEmpty()) {
                    val addContactObj = MDJsonBuilder.formContactObjList(addContactList)
                    val addContactCall = webService.saveContactList(patientId,addContactObj)
                    apiUtil.initiateRequest(addContactCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {

                            MDParsingHelper.formContactListFromOthersAdd(respObj, addContactList)
                            val postObj = MDJsonBuilder.formShareDocumentToOthersObj(documents, contactList, expiryDate)
                            val shareDocumentCall = webService.shareDocumentToOthers(patientId, postObj)

                            apiUtil.initiateRequest(shareDocumentCall, object : MDCallback(authManager) {
                                override fun onMDSuccess(respObj: JsonObject) {
                                    shareDocumentLive.postValue(true)
                                }

                                override fun onMDFailure(error: APIResponseError) {
                                    shareDocumentLive.postValue(error)
                                }
                            })
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            shareDocumentLive.postValue(error)
                        }
                    })
                } else {

                    val postObj = MDJsonBuilder.formShareDocumentToOthersObj(documents, contactList, expiryDate)
                    val shareDocumentCall = webService.shareDocumentToOthers(patientId, postObj)

                    apiUtil.initiateRequest(shareDocumentCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {
                            shareDocumentLive.postValue(true)
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            shareDocumentLive.postValue(error)
                        }
                    })
                }

            } else {
                shareDocumentLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            shareDocumentLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return shareDocumentLive
    }

    fun checkShareDocumentRestriction(): LiveData<Any> {

        val shareDocRestrictionLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {


                val practiceList = ModelUtil.getEnabledPractices(authManager, ModelUtil.documentModelId)
                val practiceSize = practiceList.size
                val composeMsgRestrictionList = arrayListOf<ShareDocRestriction>()
                var respCount = 0
                var isCallbackSent = false

                practiceList.forEach { practiceId ->

                    val shareDocRestrictionCall =
                        webService.shareDocumentRestriction(patientId, practiceId)

                    apiUtil.initiateRequest(shareDocRestrictionCall, object : MDCallback(authManager) {

                        override fun onMDSuccess(respObj: JsonObject) {
                            respCount++
                            composeMsgRestrictionList.add(
                                MDParsingHelper.formShareDocRestriction(
                                    respObj,
                                    practiceId
                                )
                            )

                            if (respCount == practiceSize && !isCallbackSent) {
                                isCallbackSent = true
                                shareDocRestrictionLive.postValue(composeMsgRestrictionList)
                            }
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            respCount++

                            if (!isCallbackSent) {

                                isCallbackSent = true
                                shareDocRestrictionLive.postValue(error)
                            }
                        }
                    })
                }

            } else {
                shareDocRestrictionLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }

        } else {
            shareDocRestrictionLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return shareDocRestrictionLive
    }

    //endregion

    //region Medical Condition

    fun getMedConditionCommonData(practiceId: String): LiveData<Any> {
        val medCondCommonDataLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val medCondCommonDataCall = webService.getCommonData(ModelUtil.moduleNameMedicalCondition, patientId, practiceId)
                apiUtil.initiateRequest(medCondCommonDataCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        medCondCommonDataLive.postValue(MDParsingHelper.formMedicalConditionCommonData(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        medCondCommonDataLive.postValue(error)
                    }
                })
            } else {
                medCondCommonDataLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            medCondCommonDataLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return medCondCommonDataLive
    }

    fun getMedConditionList(status: String, isHealthPassport: Boolean): LiveData<Any> {
        val medCondListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {


                val medConditionList = arrayListOf<PatientMedicalCondition>()

                val practiceList = if (isHealthPassport) {
                    arrayListOf("")
                } else {
                    ModelUtil.getActivePractices(authManager)
                }

                val practiceSize = practiceList.size

                var respCount = 0

                var isCallbackSent = false

                practiceList.forEach { practiceId ->

                    val medCondListCall = webService.getMedicalConditionList(patientId, status, practiceId)

                    apiUtil.initiateRequest(medCondListCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {
                            respCount++
                            medConditionList.addAll(MDParsingHelper.formMedicalConditionList(respObj))
                            if (respCount == practiceSize && !isCallbackSent) {
                                isCallbackSent = true


                                val finalMedConditionList = arrayListOf<PatientMedicalCondition>()

                                medConditionList.forEach { medCondition ->
                                    medCondition.isHealthPassport = isHealthPassport
                                    if (!medCondition.isAddedByPatient) {
                                        finalMedConditionList.add(medCondition)
                                    } else {
                                        if (!finalMedConditionList.any { it.medicalConditionMapId == medCondition.medicalConditionMapId }) {
                                            finalMedConditionList.add(medCondition)
                                        }
                                    }
                                }

                                if (respCount > 1) {
                                    finalMedConditionList.sortByDescending {
                                        it.dateOfOnset?.millis ?: 0
                                    }
                                }

                                medCondListLive.postValue(finalMedConditionList)
                            }
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            if (!isCallbackSent) {
                                isCallbackSent = true
                                medCondListLive.postValue(error)
                            }
                        }

                    })
                }

                if (practiceList.isEmpty()) {
                    medCondListLive.postValue(medConditionList)
                }
            } else {
                medCondListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            medCondListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return medCondListLive
    }

    fun saveMedicalCondition(patientMedicalCondition: PatientMedicalCondition): LiveData<Any> {
        val uploadDocumentLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                patientMedicalCondition.patientId = patientId

                val saveMedicalConditionCall = if (patientMedicalCondition.medicalConditionMapId.isNullOrBlank()) {
                    val postBody = MDJsonBuilder.formAddMedConditionObj(patientMedicalCondition)
                    webService.addMedicalCondition(postBody)
                } else {
                    val postBody = MDJsonBuilder.formEditMedConditionObj(patientMedicalCondition)
                    webService.editMedicalCondition(postBody)
                }

                apiUtil.initiateRequest(saveMedicalConditionCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        uploadDocumentLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        uploadDocumentLive.postValue(error)
                    }
                })
            } else {
                uploadDocumentLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            uploadDocumentLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return uploadDocumentLive
    }

    fun deleteMedicalCondition(patientMedicalCondition: PatientMedicalCondition): LiveData<Any> {
        val deleteMedicalConditionLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                patientMedicalCondition.patientId = patientId

                val postObj = MDJsonBuilder.formDeleteMedConditionObj(patientMedicalCondition)
                val deleteMedicalConditionCall = webService.deleteMedicalCondition(postObj)

                apiUtil.initiateRequest(deleteMedicalConditionCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        deleteMedicalConditionLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        deleteMedicalConditionLive.postValue(error)
                    }
                })
            } else {
                deleteMedicalConditionLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            deleteMedicalConditionLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return deleteMedicalConditionLive
    }

    //endregion

    //region Send support

    fun sendSupport(supportData: SupportData, fileIds: String): LiveData<Any> {
        val liveData = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {
            val postObj = MDJsonBuilder.formSupportData(supportData, fileIds)
            val sendSupportCall = webService.sendSupport(postObj)
            apiUtil.initiateRequest(sendSupportCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    liveData.postValue(true)
                }

                override fun onMDFailure(error: APIResponseError) {
                    liveData.postValue(error)
                }
            })
        } else {
            liveData.postValue(APIResponseError(ModelUtil.errorInternetConnectionCode, null, appContext.getString(R.string.internet_connection_error)))
        }
        return liveData
    }

    fun formRequestBodyForSupportDocuments(
        viewModelScope: CoroutineScope,
        liveData: MutableLiveData<Any>,
        fileList: ArrayList<SupportDocument>
    ): LiveData<Any> {
        val listOfListFile = arrayListOf<MultipartBody.Part>()
        try {
            viewModelScope.launch(Dispatchers.IO) {
                for (supportDocument in fileList) {
                    val multipartBody = createRequestBodyForSupportDocument(supportDocument)
                    if (multipartBody != null) {
                        listOfListFile.add(multipartBody)
                    }
                }
                uploadFileForSupport(listOfListFile, liveData)
            }
        } catch (ex: TimeoutCancellationException) {
            Log.e(TAG, ex.printStackTrace().toString())
            liveData.postValue(APIResponseError(null, null, appContext.getString(R.string.error_occurred_while_handling_file)))
        } catch (ex: Exception) {
            Log.e(TAG, ex.printStackTrace().toString())
            liveData.postValue(APIResponseError(null, null, appContext.getString(R.string.error_occurred_while_handling_file)))
        }
        return liveData
    }

    fun createRequestBodyForSupportDocument(supportDocument: SupportDocument): MultipartBody.Part? {
        val fileReq = FileUtil.createRequestBodyForFile(
            supportDocument.filePath
                ?: ""
        )
        return fileReq?.let {
            MultipartBody.Part.createFormData("file", supportDocument.fileName, it)
        }
    }

    private fun uploadFileForSupport(listOfFiles: List<MultipartBody.Part>, liveData: MutableLiveData<Any>) {
        if (authManager.isUserOnline()) {
            val uploadFilesCall = webService.uploadFilesForSupport(listOfFiles)
            apiUtil.initiateRequest(uploadFilesCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    val fileIds = MDParsingHelper.parseSupportDocumentId(respObj)
                    liveData.postValue(fileIds)
                }

                override fun onMDFailure(error: APIResponseError) {
                    liveData.postValue(error)
                }
            })
        } else {
            liveData.postValue(APIResponseError(ModelUtil.errorInternetConnectionCode, null, appContext.getString(R.string.internet_connection_error)))
        }
    }

    //endregion

    //region Misc


    fun getUnreadCount(selectedPatient: Patient? = null): LiveData<Any> {
        val getUnreadCountLive = MutableLiveData<Any>()


        if (authManager.isUserOnline()) {

            val patientId = selectedPatient?.practicePatientId ?: authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {


                val unreadCountList = arrayListOf<UnreadCount>()


                val practiceList = ModelUtil.getActivePractices(authManager, selectedPatient)
                val practiceSize = practiceList.size

                var respCount = 0

                var isCallbackSent = false


                practiceList.forEach { practiceId ->
                    val getUnreadCountCall = webService.getUnreadCount(patientId, practiceId)

                    apiUtil.initiateRequest(getUnreadCountCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {


                            respCount++
                            val unreadCount = MDParsingHelper.formUnreadCountObj(respObj)
                            unreadCount.practiceId = practiceId
                            unreadCountList.add(unreadCount)

                            if (respCount == practiceSize && !isCallbackSent) {

                                val unreadCountOfAllPractice = UnreadCount()
                                unreadCountOfAllPractice.billingCount =
                                    unreadCountList.filter { ModelUtil.isPracticeEnabled(authManager, ModelUtil.billingModelId, practiceId, selectedPatient) }
                                        .map { it.billingCount }.sum()
                                unreadCountOfAllPractice.documentUnreadCount =
                                    unreadCountList.filter { ModelUtil.isPracticeEnabled(authManager, ModelUtil.documentModelId, practiceId, selectedPatient) }
                                        .map { it.documentUnreadCount }.sum()
                                unreadCountOfAllPractice.labUnreadCount =
                                    unreadCountList.filter { ModelUtil.isPracticeEnabled(authManager, ModelUtil.labModelId, practiceId, selectedPatient) }
                                        .map { it.labUnreadCount }.sum()
                                unreadCountOfAllPractice.messageUnreadCount =
                                    unreadCountList.filter { ModelUtil.isPracticeEnabled(authManager, ModelUtil.messageModuleId, practiceId, selectedPatient) }
                                        .map { it.messageUnreadCount }.sum()
                                unreadCountOfAllPractice.practiceAnnouncementCount = unreadCountList.map { it.practiceAnnouncementCount }.sum()
                                unreadCountOfAllPractice.questionnaireUnfilledCount = unreadCountList.filter {
                                    ModelUtil.isPracticeEnabled(
                                        authManager,
                                        ModelUtil.questionnaireModelId,
                                        practiceId,
                                        selectedPatient
                                    )
                                }.map { it.questionnaireUnfilledCount }.sum()
                                unreadCountOfAllPractice.upcomingAppointmentCount = unreadCountList.filter {
                                    ModelUtil.isPracticeEnabled(
                                        authManager,
                                        ModelUtil.appointmentModelId,
                                        practiceId,
                                        selectedPatient
                                    )
                                }.map { it.upcomingAppointmentCount }.sum()
                                unreadCountOfAllPractice.visitSummaryUnreadCount = unreadCountList.filter {
                                    ModelUtil.isPracticeEnabled(
                                        authManager,
                                        ModelUtil.visitSummaryModelId,
                                        practiceId,
                                        selectedPatient
                                    )
                                }.map { it.visitSummaryUnreadCount }.sum()

                                getUnreadCountLive.postValue(unreadCountOfAllPractice)
                                isCallbackSent = true
                            }
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            respCount++
                            if (!isCallbackSent) {
                                getUnreadCountLive.postValue(error)
                                isCallbackSent = true
                            }
                        }
                    })
                }

            } else {
                getUnreadCountLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            getUnreadCountLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return getUnreadCountLive
    }

    fun getMenulist(): LiveData<Any> {
        val getUnreadCountLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val practiceId = if (BuildUtil.isCustomApp) {
                BuildUtil.practiceId
            } else {
                authManager.selectedPatientLive.value?.practiceList?.firstOrNull()?.practiceId ?: ""
            }


            val getMenuListCall = webService.getMenuList(practiceId)

            apiUtil.initiateRequest(getMenuListCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    val unreadCount = MDParsingHelper.formMenuListObjFromWeb(respObj)
                    getUnreadCountLive.postValue(unreadCount)
                }

                override fun onMDFailure(error: APIResponseError) {
                    getUnreadCountLive.postValue(error)
                }
            })
        } else {
            getUnreadCountLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return getUnreadCountLive
    }

    fun getPatientSection(selectedPatient: Patient? = null): LiveData<Any> {
        val patientSectionLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = selectedPatient?.practicePatientId ?: authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val patientSectionCall = webService.getPatientSection(patientId)
                apiUtil.initiateRequest(patientSectionCall, object : MDCallback(authManager) {

                    override fun onMDSuccess(respObj: JsonObject) {
                        val sectionConfigList = MDParsingHelper.formPatientSection(respObj)
                        patientSectionLive.postValue(sectionConfigList)

                        if (selectedPatient == null) {
                            authManager.updateSelectedPatientSection(patientId, sectionConfigList)
                        }
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        patientSectionLive.postValue(error)
                    }
                })
            } else {
                patientSectionLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            patientSectionLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return patientSectionLive
    }

    fun getPatientSectionSynchronize(selectedPatient: Patient? = null): Any? {
        if (authManager.isUserOnline()) {

            val patientId = selectedPatient?.practicePatientId ?: authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val patientSectionCall = webService.getPatientSection(patientId)
                val resp = authManager.synchronizeAPICall(patientSectionCall)
                Log.d("MDCallbackResponse", resp.toString())
                return if (resp is JsonObject) {
                    return MDParsingHelper.formPatientSection(resp)
                } else {
                    resp
                }
            } else {
                return APIResponseError(null, null, appContext.getString(R.string.error_patient_id_not_found))
            }
        } else {
            return APIResponseError(ModelUtil.errorInternetConnectionCode, null, appContext.getString(R.string.internet_connection_error))
        }
    }

    //endregion

    //region Swaasa

    fun getSwaasaAudioData(swaasaId: String, otp: String? = null): LiveData<Any> {
        val getSwaasaLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            authManager.skipAuth = !otp.isNullOrBlank()

            apiUtil.initiateRequest(webService.getSwaasaData(swaasaId, otp), object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    getSwaasaLive.postValue(true)
                }

                override fun onMDFailure(error: APIResponseError) {
                    getSwaasaLive.postValue(error)
                }
            })
        } else {
            getSwaasaLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return getSwaasaLive
    }

    fun uploadSwaasaAudio(moduleList: List<SwaasaModel>, audioRecordId: String, otp: String? = null): LiveData<Any> {
        val swaasaAudioLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val file = FileUtil.getSwaasaFile(appContext)
            if (file != null) {

                val fbody = FileUtil.createRequestBodyForFile(file)

                if (fbody != null) {

                    val fileMultiPartBody =
                        MultipartBody.Part.createFormData("file", file.name, fbody)


                    val postObj = MDJsonBuilder.formSwaasaObj(moduleList)
                    authManager.skipAuth = !otp.isNullOrBlank()


                    val swaasaAudioCall = webService.recordAudio(fileMultiPartBody, postObj, audioRecordId)
                    apiUtil.initiateRequest(swaasaAudioCall, object : MDCallback(authManager) {

                        override fun onMDSuccess(respObj: JsonObject) {
                            swaasaAudioLive.postValue(true)
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            swaasaAudioLive.postValue(error)
                        }
                    })
                } else {
                    swaasaAudioLive.postValue(
                        APIResponseError(
                            null,
                            null,
                            appContext.getString(R.string.error_swaasa_file_not_found)
                        )
                    )
                }
            } else {
                swaasaAudioLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_swaasa_empty_file)
                    )
                )
            }

        } else {
            swaasaAudioLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return swaasaAudioLive
    }

    //endregion

    fun fetchPrivileges(): LiveData<Pair<Boolean, JsonArray?>> {

        val priObj = MutableLiveData<Pair<Boolean, JsonArray?>>()

        if (authManager.isUserOnline()) {

            val fetchPriCall = webService.fetchPrivileges()
            //Harcoded data for test
            val respPri = JsonObject()
            val priArr = JsonArray()
            priArr.add("full_access")
//           priArr.add("view_appointment")
//           priArr.add("view_questionnaire")
//           priArr.add("view_visitsummary")
//           priArr.add("view_invoice")
//           priArr.add("view_messages")
//           priArr.add("view_medications")
//           priArr.add("view_supplements")
//           priArr.add("view_reports")
//           priArr.add("view_documents")
//           priArr.add("view_allergies")
//           priArr.add("view_immune")
//           priArr.add("view_wellness")
//           priArr.add("view_labresults")
//           priArr.add("view_procedure")
//           priArr.add("view_usefulinfo")
//           priArr.add("view_clinicalsummary")
//           priArr.add("view_insurance")
//           priArr.add("view_flowsheetdata")
//           priArr.add("add_wellness")
//           priArr.add("add_insurance")
//           priArr.add("add_immune")
//           priArr.add("add_procedure")
//           priArr.add("add_allergies")
//           priArr.add("add_supplements")
//           priArr.add("add_messages")
//           priArr.add("add_folder")
//           priArr.add("add_appointment")
            respPri.add("user_privilages", priArr)
            if (respPri != null && priArr != null) {

                RbacUtil.savePrivilegesFromJson(priArr, appContext)
                authManager.setPrivilegeData()

                priObj.postValue(Pair(true, priArr))

            } else priObj.postValue(Pair(false, null))

            //Need to uncomment after API is Live
//            apiUtil.initiateRequest(fetchPriCall , object : MDCallback(authManager){
//                 override fun onMDSuccess(respObj: JsonObject) {
//                    val respPri = respObj.get("user_privilages")?.asJsonObject
//                    val priArr = respPri?.get("privilages")?.asJsonArray
//
//                    if (respPri != null && priArr != null) {
//
//                        RbacUtil.savePrivilegesFromJson(priArr, appContext)
//                        authManager.setPrivilegeData()
//
//                        priObj.postValue( Pair(true, priArr))
//
//                    } else priObj.postValue( Pair(false, null))
//                }
//
//                override fun onMDFailure(error: APIResponseError) {
//                    priObj.postValue(Pair(false, null))
//                }
//
//            })
//
//
//        } else {
//            priObj.value = Pair(false, null)
//        }

        }
        return priObj

    }
}