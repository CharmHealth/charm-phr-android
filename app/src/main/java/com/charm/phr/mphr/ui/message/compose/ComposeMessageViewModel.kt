package com.charm.phr.mphr.ui.message.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.MessageGroup
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.model.repository.MessageRepository
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil

class ComposeMessageViewModel(private val messageRepository: MessageRepository) : ViewModel() {

    val TAG = "ComposeMessageViewModel"

    var practiceList: ArrayList<PracticeList> = arrayListOf()

    var selectedPractice = PracticeList()
    var memberListLiveResponse: MediatorLiveData<Any?> = MediatorLiveData()
    var memberListStartIndex = 1
    var isMemberListFetched: Boolean = false

    var isFromDashboard = false

    var isNotificationFlow: Boolean = false

    var composeRestrictedList: ArrayList<ComposeMsgRestriction> = arrayListOf()


    var currentNumberOfCharacters = 0


    var isReplyOrForward = false

    var alreadyAddedDataContent = ""

    var composeMessageType = ModelUtil.messageComposeTypeReplyIntent

    var message: Messages? = null

    var showBackPressDialog = false

    var isReplyToRestrictedGroup = false
    var isReplyToRestrictedMember = false

    private val _isMembersUpdated = MutableLiveData<Boolean>()
    val isMembersUpdated: LiveData<Boolean> = _isMembersUpdated

    fun triggerRefresh() {
        _isMembersUpdated.value = true
    }

    fun markRefreshed() {
        _isMembersUpdated.value = false
    }


    // Cache for member lists per practice
    private val memberCache: MutableMap<String, List<Member>> = mutableMapOf()

    // Cache for group lists per practice
    private val groupCache: MutableMap<String, List<MessageGroup>> = mutableMapOf()

    fun getMemberList() {
        val practiceId = selectedPractice.practiceId ?: ""
        // Check cache first
        if (memberCache.containsKey(practiceId) && groupCache.containsKey(practiceId)) {
            val cachedMembers = memberCache[practiceId]
            val cachedGroup = groupCache[practiceId]
            memberListLiveResponse.postValue(Pair(cachedGroup, cachedMembers))
            return
        }
        memberListLiveResponse.addSource(
            messageRepository.getMemberListForMessageWithGroup(
                practiceId,
                memberListStartIndex
            )
        ) { response ->
            if (response != null) {
                if (response is Pair<*, *>) {
                    val members = response.second as? List<Member>
                    val groups = response.first as? List<MessageGroup>
                    if (members != null) {
                        memberCache[practiceId] = members
                    }
                    if (groups != null) {
                        groupCache[practiceId] = groups
                    }
                    memberListLiveResponse.postValue(response)
                } else {
                    memberListStartIndex = 0
                    memberListLiveResponse.postValue(response)
                }
            }
        }
    }


    fun sendMessage(messages: Messages): LiveData<Any> {
        return messageRepository.sendMessage(messages)
    }

    var searchQuery by mutableStateOf("")

    fun updateSearchQuery(query: String) {
        searchQuery = query
        updateFilteredMemberList()
        updateFilteredMessageGroupList()
    }

    //region Member list management
    private val _memberList = mutableStateListOf<Member>()
    val memberList: List<Member> get() = _memberList

    fun setMemberList(memberList: List<Member>) {
        _memberList.addAll(memberList)
    }

    fun clearMemberList() {
        _memberList.clear()
    }

    val tempSelectedMemberList: List<Member>
        get() = _memberList.filter { it.isSelected }

    fun toggleMemberSelection(member: Member): Boolean {
        val index = _memberList.indexOfFirst { it.memberId == member.memberId }
        if (index >= 0) {
            _memberList[index] = _memberList[index].copy(isSelected = !member.isSelected)
        }else{
            return false
        }
        updateFilteredMemberList()
        return true
    }


    var filteredMemberList = mutableStateListOf<Member>()

    fun updateFilteredMemberList() {
        filteredMemberList.clear()
        if (searchQuery.isBlank()) {
            filteredMemberList.addAll(_memberList)
        } else {
            filteredMemberList.addAll(
                _memberList.filter { member ->
                    val memberName = UIUtil.formMemberName(member.prefix, member.fullName ?: "")
                    memberName.contains(searchQuery, ignoreCase = true)
                }
            )
        }
    }
    //endregion


    //region MessageGroup list management
    private val _messageGroupList = mutableStateListOf<MessageGroup>()
    val messageGroupList: List<MessageGroup> get() = _messageGroupList

    fun setMessageGroupList(groups: List<MessageGroup>) {
        _messageGroupList.addAll(groups)
    }

    fun clearMessageGroupList() {
        _messageGroupList.clear()
    }


    var filteredMessageGroupList = mutableStateListOf<MessageGroup>()


    fun updateFilteredMessageGroupList() {
        filteredMessageGroupList.clear()
        if (searchQuery.isBlank()) {
            filteredMessageGroupList.addAll(_messageGroupList)
        } else {
            filteredMessageGroupList.addAll(
                _messageGroupList.filter { group ->
                    group.groupName?.contains(searchQuery, ignoreCase = true) == true
                }
            )
        }
    }

    val tempSelectedMessageGroupList: List<MessageGroup>
        get() = _messageGroupList.filter { it.isSelected }

    fun toggleMessageGroupSelection(group: MessageGroup): Boolean {
        val index = _messageGroupList.indexOfFirst { it.groupId == group.groupId }
        if (index >= 0) {
            _messageGroupList[index] = _messageGroupList[index].copy(isSelected = !group.isSelected)
        } else {
            return false
        }
        updateFilteredMessageGroupList()
        return true
    }
    //endregion


    //region Backup and Restore Selection State when navigating back without done click
    // Backup lists for selection state
    private var backupMemberList: List<Member> = emptyList()
    private var backupMessageGroupList: List<MessageGroup> = emptyList()

    fun backupSelections() {
        // Deep copy to preserve selection state
        backupMemberList = memberList.map { it.copy() }
        backupMessageGroupList = messageGroupList.map { it.copy() }
    }

    fun restoreSelections() {
        // Restore selection state from backup
        if (backupMemberList.isNotEmpty()) {
            memberList.forEach { member ->
                val backup = backupMemberList.find { it.memberId == member.memberId }
                member.isSelected = backup?.isSelected ?: false
            }
        }
        if (backupMessageGroupList.isNotEmpty()) {
            messageGroupList.forEach { group ->
                val backup = backupMessageGroupList.find { it.groupId == group.groupId }
                group.isSelected = backup?.isSelected ?: false
            }
        }
    }
    //endregion

}