package com.charm.phr.mphr.ui.message.messageDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.model.repository.MessageRepository

class MessageDetailViewModel(val messageRepository: MessageRepository) : ViewModel() {

    var messageId: String = ""
    var practiceId: String = ""
    var patientId: String = ""
    var isNotificationFlow: Boolean = false

    var isMessageDetailFetched = false
    var isComposeMsgRestrictionFetched = false
    var isMessageDetailError = false
    var isComposeMsgRestrictionError = false

    var composeMsgRestriction = ComposeMsgRestriction()

    var messageLiveData = MutableLiveData<Messages>()

    fun fetchMessageDetail(messageId: String, practiceId: String, patientId: String): LiveData<Any> {
        return messageRepository.fetchMessageDetail(messageId, practiceId, patientId)
    }

    fun getComposeMessageRestriction(patientId: String, practiceId: String):LiveData<Any>{
        return messageRepository.getComposeMessageRestrictionForPracticeId(patientId, practiceId)
    }

    //if notification flow, we need to fetch message detail and message restriction

    fun deleteMessage(messageReceipientId: String): LiveData<Any> {
        return messageRepository.deleteMessage(messageReceipientId, practiceId)
    }

    fun getAttachment(attachmentId: String): LiveData<Any> {
        return messageRepository.getAttachment(messageId, attachmentId, practiceId)
    }

}