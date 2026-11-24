package com.charm.phr.mphr.ui.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.model.repository.MessageRepository
import com.charm.phr.mphr.util.ModelUtil

class MessageViewModel(private val messageRepository: MessageRepository) : ViewModel(){



    var messageSentListLive =  MediatorLiveData<Any>() //Pair<ArrayList<Messages> , Boolean>
    var isMessageSentListFetched = false
    var messageSentListPageNo = 1
    var messageSentHasMore = true
    var isMessageSentFetchComplete = true

    var messageInboxListLive =  MediatorLiveData<Any>() //Pair<ArrayList<Messages> , Boolean>
    var isMessageInboxListFetched = false
    var messageInboxListPageNo = 1
    var messageInboxHasMore = true
    var isMessageInboxFetchComplete = true

    var isMultiSelectEnabled = false


    var composeMsgRestrictionList = arrayListOf<ComposeMsgRestriction>()


    var isComposeViewShown = true
    var isComposeMsgRestrictionFetched = false
    var isComposeBtnClicked = false
    var isComposeDetailClicked = false

    var message = Messages()

    var isComposeAvailable = true

    var selectedMessagePosition = -1

    var unreadCount = 0

    fun fetchMessageList(messageType: String , isFirst: Boolean){

        if (messageType == ModelUtil.messageTypeSent){
            if ( isFirst ) {
                messageSentListPageNo = 1
                messageSentHasMore = true
            }
            else {
                messageSentListPageNo++
            }

            if (messageSentHasMore){
                messageSentListLive.addSource(messageRepository.fetchMessageList(messageType , messageSentListPageNo)) {resp ->
                    if(resp != null){

                        if (resp is Pair<* , *>) {
                            if (isFirst){
                                isMessageSentListFetched = true
                            }
                            val (list: java.util.ArrayList<Messages>, hasMore: Boolean) = resp as Pair<ArrayList<Messages> , Boolean>
                            this.messageSentHasMore = hasMore
                            messageSentListLive.postValue(Pair(list, isFirst))
                        }
                        else{
                            messageSentListLive.postValue(resp)
                        }
                    }
                }
            }
        }
        else{
            if ( isFirst ) {
                messageInboxListPageNo = 1
                messageInboxHasMore = true
            }
            else {
                messageInboxListPageNo++
            }

            if (messageInboxHasMore){
                messageInboxListLive.addSource(messageRepository.fetchMessageList(messageType , messageInboxListPageNo)) {resp ->
                    if(resp != null){

                        if (resp is Pair<* , *>) {
                            if (isFirst){
                                isMessageInboxListFetched = true
                            }
                            val (list: java.util.ArrayList<Messages>, hasMore: Boolean) = resp as Pair<ArrayList<Messages> , Boolean>
                            this.messageInboxHasMore = hasMore
                            messageInboxListLive.postValue(Pair(list, isFirst))
                        }
                        else{
                            messageInboxListLive.postValue(resp)
                        }
                    }
                }
            }
        }
    }

    fun getComposeMsgRestriction():LiveData<Any>{
        return messageRepository.checkComposeRestriction()
    }

    fun deleteMessage(messageReceipientId: String,practiceId:String): LiveData<Any>{
        return messageRepository.deleteMessage(messageReceipientId,practiceId)
    }
}