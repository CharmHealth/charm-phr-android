package com.charm.phr.mphr.model.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.model.MessagesAttachmentsMap
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.util.APIUtil
import com.charm.phr.mphr.util.AppticsUtil
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDCallback
import com.charm.phr.mphr.util.MDCallbackForMisc
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.MdEvent
import com.charm.phr.mphr.util.MdEventGroup
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.WebService
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val webService: WebService,
    private val authManager: AuthManager,
    private val executor: Executor,
    private val appContext: Context,
    private val apiUtil: APIUtil
) {


    fun fetchMessageList(messageType: String, pageNo: Int): LiveData<Any> {

        val fetchMessageListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                //val fromRange: Int =  ((pageNo - 1)  *  ModelUtil.listPerPageCount) + 1

                val practiceList =
                    ModelUtil.getEnabledPractices(authManager, ModelUtil.messageModuleId)
                val practiceSize = practiceList.size

                var patientMessageList = arrayListOf<Messages>()
                var respCount = 0

                var isCallbackSent = false

                practiceList.forEach { practiceId ->
                    val fetchMessageListCall =
                        webService.getMessageList(patientId, practiceId, messageType, 1)

                    apiUtil.initiateRequest(fetchMessageListCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {

                            respCount++

                            val messageList =
                                MDParsingHelper.formMessageList(respObj, messageType, practiceId)

                            patientMessageList.addAll(messageList)

                            if (respCount == practiceSize && !isCallbackSent) {

                                patientMessageList =
                                    ArrayList(patientMessageList.sortedByDescending {
                                        it.messageDate?.millis ?: 0
                                    })

                                fetchMessageListLive.postValue(Pair(patientMessageList, false))
                                isCallbackSent = true
                            }
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            respCount++
                            if (!isCallbackSent) {
                                fetchMessageListLive.postValue(error)
                                isCallbackSent = true
                            }
                        }
                    })
                }


            } else {
                fetchMessageListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            fetchMessageListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return fetchMessageListLive
    }

    fun fetchMessageDetail(messageId: String, practiceId: String, patientId: String): LiveData<Any> {

        val fetchMessageDetailLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            if (patientId.isNotBlank()) {

                val fetchMessageDetailCall =
                    webService.getMessageDetail(messageId, patientId, practiceId)

                apiUtil.initiateRequest(fetchMessageDetailCall, object : MDCallback(authManager) {

                    override fun onMDSuccess(respObj: JsonObject) {
                        fetchMessageDetailLive.postValue(
                            MDParsingHelper.formMessageDetail(
                                respObj, authManager.selectedPatientLive.value?.practicePatientId
                                    ?: ""
                            )
                        )
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        fetchMessageDetailLive.postValue(error)
                    }
                })
            } else {
                fetchMessageDetailLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            fetchMessageDetailLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return fetchMessageDetailLive
    }

    fun getMemberListForMessage(practiceIdList: ArrayList<String>, startIndex: Int): LiveData<Any> {

        val getMemberListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {


                val practiceList = if (practiceIdList.isEmpty()) {

                    val temp = authManager.selectedPatientLive.value?.practiceList
                        ?: arrayListOf<PracticeList>()

                    ArrayList(temp.filter { practice -> practice.isActive })
                } else {
                    authManager.selectedPatientLive.value?.practiceList?.filter {
                        practiceIdList.contains(
                            it.practiceId
                        )
                    }
                        ?: arrayListOf()
                }

                val practiceSize = practiceList.size


                val memberList = arrayListOf<Member>()

                var respCount = 0

                var isCallbackSent = false

                practiceList.forEach { practice ->

                    val practiceId = practice.practiceId ?: ""

                    val getMemberListCall = webService.getMembersForMessages(
                        practiceId,
                        patientId,
                        startIndex,
                        ModelUtil.getMembersListCountPerPage
                    )

                    apiUtil.initiateRequest(getMemberListCall, object : MDCallback(authManager) {

                        override fun onMDSuccess(respObj: JsonObject) {
                            respCount++
                            memberList.addAll(
                                MDParsingHelper.formMemberListForMessage(
                                    respObj,
                                    practiceId
                                )
                            )

                            if (respCount == practiceSize && !isCallbackSent) {
                                isCallbackSent = true
                                getMemberListLive.postValue(memberList)
                            }
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            respCount++

                            if (!isCallbackSent) {

                                Log.d(
                                    "messageRepo",
                                    "practiceId -> $practiceId ; code -> ${error.charmApiCode} ; message -> ${error.errorMsg}"
                                )
                                isCallbackSent = true
                                getMemberListLive.postValue(error)
                            }
                        }
                    })
                }
            } else {
                getMemberListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }

        } else {
            getMemberListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return getMemberListLive
    }

    fun getMemberListForMessageWithGroup(practiceId: String, startIndex: Int): LiveData<Any> {

        val getMemberListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val memberList = arrayListOf<Member>()

                var respCount = 0

                var isCallbackSent = false

                //Pagination not implemented fetch entire list in single call
                val getMemberListCall = webService.getMembersForMessagesWithGroup(
                    patientId,
                    practiceId,
                    null,
                    null,
                    isAscending = true,
                    searchString = null
                )

                apiUtil.initiateRequest(getMemberListCall, object : MDCallback(authManager) {

                    override fun onMDSuccess(respObj: JsonObject) {
                        val result = MDParsingHelper.parseGroupAndMemberList(
                            respObj,
                            practiceId
                        )
                        getMemberListLive.postValue(result)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        respCount++

                        if (!isCallbackSent) {

                            Log.d(
                                "messageRepo",
                                "practiceId -> $practiceId ; code -> ${error.charmApiCode} ; message -> ${error.errorMsg}"
                            )
                            isCallbackSent = true
                            getMemberListLive.postValue(error)
                        }
                    }
                })
            } else {
                getMemberListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }

        } else {
            getMemberListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return getMemberListLive
    }


    fun sendMessage(messages: Messages): LiveData<Any> {

        val sendMessageLive = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {


            if (messages.messagesAttachmentsMapList.isEmpty()) {
                sendMessageRequest(messages, sendMessageLive)
            } else {

                val attachmentTobeFetchedList =
                    messages.messagesAttachmentsMapList.filter { !it.attachmentId.isNullOrBlank() }


                val filesTobeFetched = arrayListOf<MessagesAttachmentsMap>()

                attachmentTobeFetchedList.forEach {
                    val file = FileUtil.getMessageAttachment(appContext, it)

                    if (file != null) {
                        it.attachmentLocation = file.path ?: ""
                    } else {
                        filesTobeFetched.add(it)
                    }
                }



                if (filesTobeFetched.isEmpty()) {
                    addAttachments(messages, sendMessageLive)
                } else {

                    val requestCount = filesTobeFetched.size

                    var respCount = 0

                    var isRespSent = false

                    filesTobeFetched.forEach { messagesAttachmentsMap ->

                        val messageAttachmentCall = webService.getMessageAttachment(
                            messagesAttachmentsMap.messageId
                                ?: "",
                            messagesAttachmentsMap.attachmentId ?: "",
                            messages.practicePatientId ?: "",
                            messages.practiceId ?: ""
                        )

                        apiUtil.initiateRequestForMisc(
                            messageAttachmentCall,
                            object : MDCallbackForMisc(authManager) {
                                override fun onMDSuccess(respObj: MDFile) {

                                    val file = FileUtil.saveMessageAttachment(
                                        respObj,
                                        messagesAttachmentsMap,
                                        appContext
                                    )
                                    if (file != null) {
                                        messagesAttachmentsMap.attachmentLocation = file.path
                                        messagesAttachmentsMap.attachmentName = file.name
                                        respCount++

                                        if (respCount == requestCount && !isRespSent) {
                                            addAttachments(messages, sendMessageLive)
                                        }
                                    } else {
                                        respCount++
                                        if (!isRespSent) {
                                            isRespSent = true
                                            sendMessageLive.postValue(APIResponseError())
                                        }
                                    }
                                }

                                override fun onMDFailure(error: APIResponseError) {
                                    respCount++
                                    if (!isRespSent) {
                                        isRespSent = true
                                        sendMessageLive.postValue(error)
                                    }
                                }
                            })
                    }
                }
            }
        } else {
            sendMessageLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return sendMessageLive
    }

    private fun addAttachments(messages: Messages, sendMessageLive: MutableLiveData<Any>) {

        val messagesAttachmentsMapList = messages.messagesAttachmentsMapList

        var respCount = 0
        val requestCount = messagesAttachmentsMapList.size
        var isRespSent = false

        messagesAttachmentsMapList.forEach { messagesAttachmentsMap ->
            val fileReq = FileUtil.createRequestBodyForFile(
                messagesAttachmentsMap.attachmentLocation
                    ?: ""
            )
            val fileMultiPartBody =
                MultipartBody.Part.createFormData(
                    "file",
                    messagesAttachmentsMap.attachmentName,
                    fileReq!!
                )
            val addAttachmentCall = webService.addMessageAttachment(fileMultiPartBody)

            apiUtil.initiateRequest(addAttachmentCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    messagesAttachmentsMap.attachmentId = MDParsingHelper.getAttachmentId(respObj)
                    respCount++
                    if (respCount == requestCount && !isRespSent) {
                        sendMessageRequest(messages, sendMessageLive)
                    }
                }

                override fun onMDFailure(error: APIResponseError) {
                    respCount++
                    if (!isRespSent) {
                        isRespSent = true
                        sendMessageLive.postValue(error)
                    }
                }

            })
        }

    }

    private fun sendMessageRequest(messages: Messages, sendMessageLive: MutableLiveData<Any>) {


        val postObj = MDJsonBuilder.formMessageObj(messages)

        Log.d("MdCallback", postObj.toString())

        val sendMessageCall = webService.sendMessage(postObj)

        apiUtil.initiateRequest(sendMessageCall, object : MDCallback(authManager) {

            override fun onMDSuccess(respObj: JsonObject) {
                sendMessageLive.postValue(true)
                AppticsUtil.addEvent(MdEventGroup.Message, MdEvent.SendMessageOnSuccess)
            }

            override fun onMDFailure(error: APIResponseError) {
                sendMessageLive.postValue(error)
                AppticsUtil.addEvent(MdEventGroup.Message, MdEvent.SendMessageOnFailure)
            }
        })
    }

    fun checkComposeRestriction(): LiveData<Any> {

        val checkComposeRestrictionLive = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {


                val practiceList =
                    ModelUtil.getEnabledPractices(authManager, ModelUtil.messageModuleId)
                val practiceSize = practiceList.size
                val composeMsgRestrictionList = arrayListOf<ComposeMsgRestriction>()
                var respCount = 0
                var isCallbackSent = false

                practiceList.forEach { practiceId ->

                    val getComposeMsgRestrictionCall =
                        webService.getComposeMessageRestriction(patientId, practiceId)

                    apiUtil.initiateRequest(
                        getComposeMsgRestrictionCall,
                        object : MDCallback(authManager) {

                            override fun onMDSuccess(respObj: JsonObject) {
                                respCount++
                                composeMsgRestrictionList.add(
                                    MDParsingHelper.formComposeMsgRestriction(
                                        respObj,
                                        practiceId
                                    )
                                )

                                if (respCount == practiceSize && !isCallbackSent) {
                                    isCallbackSent = true
                                    checkComposeRestrictionLive.postValue(
                                        composeMsgRestrictionList
                                    )
                                }
                            }

                            override fun onMDFailure(error: APIResponseError) {
                                respCount++

                                if (!isCallbackSent) {

                                    isCallbackSent = true
                                    checkComposeRestrictionLive.postValue(error)
                                }
                            }
                        })
                }

            } else {
                checkComposeRestrictionLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }

        } else {
            checkComposeRestrictionLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return checkComposeRestrictionLive
    }

    fun getComposeMessageRestrictionForPracticeId(patientId: String, practiceId: String): LiveData<Any> {
        val responseLive = MutableLiveData<Any>()
        val call = webService.getComposeMessageRestriction(patientId, practiceId)
        apiUtil.initiateRequest(call, object : MDCallback(authManager) {
            override fun onMDSuccess(respObj: JsonObject) {
                responseLive.value = MDParsingHelper.formComposeMsgRestriction(respObj, practiceId)
            }

            override fun onMDFailure(error: APIResponseError) {
                responseLive.value = error
            }
        })
        return responseLive
    }

    fun deleteMessage(messageReceipientId: String, practiceId: String): LiveData<Any> {
        val deleteMessageLive = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val deleteMessageCall =
                    webService.deleteMessage(messageReceipientId, patientId, practiceId)

                apiUtil.initiateRequest(deleteMessageCall, object : MDCallback(authManager) {

                    override fun onMDSuccess(respObj: JsonObject) {
                        deleteMessageLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        deleteMessageLive.postValue(error)
                    }
                })
            } else {
                deleteMessageLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            deleteMessageLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return deleteMessageLive
    }


    fun getAttachment(messageId: String, attachmentId: String, practiceId: String): LiveData<Any> {

        val messageAttachmentLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                if (messageId.isNotBlank() && attachmentId.isNotBlank()) {
                    val messageAttachmentCall = webService.getMessageAttachment(
                        messageId,
                        attachmentId,
                        patientId,
                        practiceId
                    )

                    apiUtil.initiateRequestForMisc(
                        messageAttachmentCall,
                        object : MDCallbackForMisc(authManager) {
                            override fun onMDSuccess(respObj: MDFile) {
                                messageAttachmentLive.postValue(respObj)
                            }

                            override fun onMDFailure(error: APIResponseError) {
                                messageAttachmentLive.postValue(error)
                            }

                        })
                } else {
                    messageAttachmentLive.postValue(
                        APIResponseError(
                            null,
                            null,
                            if (messageId.isBlank()) {
                                appContext.getString(R.string.error_message_id_not_found)
                            } else {
                                appContext.getString(R.string.error_attachment_id_not_found)
                            }
                        )
                    )
                }
            } else {
                messageAttachmentLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            messageAttachmentLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return messageAttachmentLive
    }


}