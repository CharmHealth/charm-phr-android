package com.charm.phr.mphr.ui.message.compose

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentComposeMessageBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.MessageGroup
import com.charm.phr.mphr.model.MessageGroupMap
import com.charm.phr.mphr.model.MessageMembersMap
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.model.MessagesAttachmentsMap
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.Theme.MdTheme
import com.charm.phr.mphr.ui.components.ProgressDialog
import com.charm.phr.mphr.ui.components.compose.MDChip
import com.charm.phr.mphr.ui.components.compose.MDTextView
import com.charm.phr.mphr.ui.health_passport.GridSpacingItemDecoration
import com.charm.phr.mphr.ui.message.MessageActivity
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDBaseActivity.Companion.isPassCodeAllowed
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.UIUtil
import com.google.gson.JsonObject
import dagger.android.support.DaggerFragment
import org.joda.time.DateTime
import javax.inject.Inject

class ComposeMessageFragment : DaggerFragment() {

    val TAG = "ComposeMessageFragment"

    @Inject
    lateinit var pref: SharedPreferences

    @Inject
    lateinit var composeMessageAttachmentAdapter: ComposeMessageAttachmentAdapter

    lateinit var imm: InputMethodManager

    lateinit var binding: FragmentComposeMessageBinding

    lateinit var composeMessageViewModel: ComposeMessageViewModel

    @Inject
    lateinit var authManager: AuthManager

    lateinit var progressDialog: ProgressDialog

    private var maxCharToast: Toast? = null

    private var hasShownMaxToast: Boolean = false

    private val docPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val extension = FileUtil.getMimeType(requireActivity(), uri)

            val fileName = FileUtil.getFileName(uri, requireActivity())

            if (extension != null) {
                val filePath = FileUtil.saveAttachment(uri, extension, requireActivity())
                if (!filePath.isNullOrBlank()) {

                    val messageAttachmentMap = MessagesAttachmentsMap()
                    messageAttachmentMap.attachmentName = fileName
                    messageAttachmentMap.attachmentLocation = filePath

                    val attachmentSize =
                        composeMessageAttachmentAdapter.addMessageAttachmentMap(messageAttachmentMap)
                    if (attachmentSize == 0) {
                        binding.composeMessageAttachmentRV.visibility = View.GONE
                    } else {
                        binding.composeMessageAttachmentRV.visibility = View.VISIBLE
                    }
                } else {
                    UIUtil.showToast(
                        requireActivity(),
                        R.string.error_occurred_while_handling_file,
                        Toast.LENGTH_LONG
                    )

                }
            } else {
                UIUtil.showToast(
                    requireActivity(),
                    R.string.error_file_not_supported,
                    Toast.LENGTH_LONG
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentComposeMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        maxCharToast?.cancel()
        maxCharToast = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        composeMessageViewModel =
            ViewModelProvider(requireActivity())[ComposeMessageViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.composeMsgToolbar)
        UIUtil.applyViewInsertForBottomView(binding.composeMsgMainLayout)

        // Observe discardSelectionTrigger to restore selection UI
        composeMessageViewModel.isMembersUpdated.observe(viewLifecycleOwner) { triggered ->
            if (triggered == true) {
                restoreSelectionUI()
                composeMessageViewModel.markRefreshed()
            }
        }

        progressDialog = ProgressDialog(requireContext())

        //Used only for navigation animation handling
        composeMessageViewModel.isFromDashboard =
            requireActivity().intent.getBooleanExtra(
                ModelUtil.messageComposeFromDashboardIntent,
                false
            )

        val alreadyAddedData =
            requireActivity().intent.getStringExtra(ModelUtil.messageDataIntent) ?: ""

        composeMessageViewModel.isReplyOrForward = alreadyAddedData.isNotBlank()

        if (composeMessageViewModel.isReplyOrForward) {

            replyOrForwardHandler(alreadyAddedData)

        } else {

            binding.composeMsgContentET.minHeight =
                resources.getDimensionPixelSize(R.dimen.compose_area_expanded)
            binding.composeMsgAlreadyAddedTxtWebview.visibility = View.GONE

            val composeMsgRestrictionList =
                MDParsingHelper.formComposeMsgRestrictionList(
                    requireActivity().intent.getStringExtra(ModelUtil.messageComposeMsgRestrictionListIntent)
                        ?: ""
                )
            composeMessageViewModel.composeRestrictedList = composeMsgRestrictionList
            val allowedPractices = getAllowedPractices() ?: emptyList()
            composeMessageViewModel.practiceList = ArrayList(allowedPractices)
        }


        binding.composeMsgAttachmentBtn.visibility = View.VISIBLE
        binding.composeMsgAttachmentBtn.setOnClickListener {
            isPassCodeAllowed = false   // preventing from asking passcode
            docPickerLauncher.launch(arrayOf("image/*", "application/pdf"))
        }

        imm = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager


        authManager.selectedPatientLive.observe(viewLifecycleOwner) { patient ->

            if (patient != null) {
                binding.composeMsgFromValueTV.text = patient.fullName
            }
        }

        setAttachmentRV()
        setupObservers()
        updatePracticeList()
        setOnClickListener()
        setUpComposeMsgToComposeView()
    }

    fun restoreSelectionUI() {
        composeMessageViewModel.restoreSelections()
        setUpComposeMsgToComposeView()
    }

    fun replyOrForwardHandler(alreadyAddedData: String) {
        var message: Messages? = null

        try {
            val activityIntent = requireActivity().intent
            if (activityIntent == null) {
                Log.d(TAG, "Activity intent is null in replyOrForwardHandler")
                return
            }

            //Message Types - Reply, Forward, Reply All
            composeMessageViewModel.composeMessageType =
                activityIntent.getStringExtra(ModelUtil.messageComposeTypeIntent)
                    ?: ModelUtil.messageComposeTypeReplyIntent
            //Fixed in reply and forward (refer to previously sent member or group practice)
            val practiceId =
                activityIntent.getStringExtra(ModelUtil.practiceIdIntent) ?: ""
            //Restriction of previously selected practice
            composeMessageViewModel.composeRestrictedList.add(
                ComposeMsgRestriction(
                    noOfCharsAllowed = activityIntent.getIntExtra(
                        ModelUtil.messageNoOfCharAllowedIntent,
                        0
                    ),
                    practiceId = activityIntent.getStringExtra(ModelUtil.practiceIdIntent)
                        ?: ""
                )
            )
            //Get the previously selected practice data from the authManager
            composeMessageViewModel.practiceList = ArrayList(
                authManager.selectedPatientLive.value
                    ?.practiceList
                    ?.filter { it.practiceId == practiceId }
                    ?: emptyList()
            )

            message = MDParsingHelper.formMessageDetail(
                alreadyAddedData.parseStringAsJsonObject() ?: JsonObject(),
                authManager.selectedPatientLive.value?.practicePatientId ?: ""
            )


            message?.messageMembersMapList?.forEach {
                it.member?.practiceId =
                    composeMessageViewModel.practiceList.firstOrNull()?.practiceId ?: ""
            }

            val messageDateStr = activityIntent.getStringExtra(ModelUtil.messageDateIntent)
            if (messageDateStr != null) {
                message?.messageDate = DateTime.parse(
                    messageDateStr,
                    ModelUtil.sqlTimeStampFormattor()
                )
            }

            val content = message?.content ?: ""
            //To add the reply or forward content in the message
            message?.content = getMessageContent(message, content)

            var subject = if ((message?.subject?.length
                    ?: (0 + 5)) <= resources.getInteger(R.integer.composeMsgSubjectMaxLength)
            ) {
                "Fwd: ${message?.subject}"
            } else {
                message?.subject ?: ""
            }

            if (composeMessageViewModel.composeMessageType != ModelUtil.messageComposeTypeForwardIntent) {

                subject = if ((message?.subject?.length
                        ?: (0 + 4)) <= resources.getInteger(R.integer.composeMsgSubjectMaxLength)
                ) {
                    "Re: ${message?.subject}"
                } else {
                    message?.subject ?: ""
                }
            } else {
                val attachmentSize = composeMessageAttachmentAdapter.setMessageAttachmentMapList(
                    message?.messagesAttachmentsMapList ?: arrayListOf()
                )
                if (attachmentSize == 0) {
                    binding.composeMessageAttachmentRV.visibility = View.GONE
                } else {
                    binding.composeMessageAttachmentRV.visibility = View.VISIBLE
                }
            }

            binding.composeMsgSubjectValueTV.setText(subject)

            binding.composeMsgAlreadyAddedTxtWebview.visibility = View.VISIBLE

            binding.composeMsgAlreadyAddedTxtWebview.settings.javaScriptEnabled = true
            binding.composeMsgAlreadyAddedTxtWebview.loadDataWithBaseURL(
                null,
                "<html>${message?.content ?: ""}</html>",
                "text/html",
                "utf-8",
                null
            )

            /*
                        composeMsgWebview.loadData(message?.content , "text/html", "UTF-8")
            */

            composeMessageViewModel.message = message

        } catch (e: Exception) {
            Log.d(TAG, "Error in replyOrForwardHandler: ${e.message}", e)
            UIUtil.showToast(
                requireActivity(),
                R.string.error_occurred_try_again,
                Toast.LENGTH_LONG
            )
        }
    }

    fun getMessageContent(message: Messages?, content: String): String {
        val contentStringBuilder = StringBuilder()
        contentStringBuilder.append("<div><br/></div>")
        contentStringBuilder.append("<div><br/></div>")
        contentStringBuilder.append(
            "<div style=\"float:left; text-align:left; padding-left:10px;\">---- On ${
                message?.messageDate?.toString(
                    ModelUtil.charmMessageDetailContentDateFormatter()
                )
            }  &lt; ${message?.messageRecipient?.fromName} &gt;  wrote ---<br></div>"
        )
        contentStringBuilder.append("<div><br/></div>")
        contentStringBuilder.append("<div><br/></div>")
        contentStringBuilder.append("<div><br/></div>")
        contentStringBuilder.append("<blockquote style=\"border-left: 1px solid #0000FF; padding-left: 6px;\"><div style=\"margin-left: 10px;\">$content</div></blockquote>")

        return contentStringBuilder.toString()
    }

    fun setAttachmentRV() {
        binding.composeMessageAttachmentRV.layoutManager =
            GridLayoutManager(requireActivity(), 2, RecyclerView.VERTICAL, false)

        binding.composeMessageAttachmentRV.addItemDecoration(
            GridSpacingItemDecoration(
                2,
                resources.getDimensionPixelSize(R.dimen.card_padding),
                resources.getDimensionPixelSize(R.dimen.card_padding),
                includeEdge = true
            )
        )

        binding.composeMessageAttachmentRV.adapter = composeMessageAttachmentAdapter

        composeMessageAttachmentAdapter.setComposeMessageAttachmentSelectListener(object :
            ComposeMessageAttachmentAdapter.ComposeMessageAttachmentListener {
            override fun onAttachmentSelect(messagesAttachmentsMap: MessagesAttachmentsMap) {

            }

            override fun onAttachmentDelete(position: Int) {

                val builder = AlertDialog.Builder(requireActivity())

                builder.setMessage(R.string.compose_message_attachment_remove_str)
                builder.setPositiveButton(R.string.yes_str) { _, _ ->

                    val attachmentSize =
                        composeMessageAttachmentAdapter.deleteMessageAttachmentPosition(position)

                    if (attachmentSize == 0) {
                        binding.composeMessageAttachmentRV.visibility = View.GONE
                    } else {
                        binding.composeMessageAttachmentRV.visibility = View.VISIBLE
                    }
                }
                builder.setNegativeButton(R.string.no_str) { _, _ ->

                }

                builder.show()
            }
        })
    }

    fun setOnClickListener() {

        binding.composeMsgSendBtn.setOnClickListener {
            validateMessage()
        }

        binding.composeMsgBackBtn.setOnClickListener {
            val activity = requireActivity() as? ComposeMessageActivity
            activity?.onBackPressed()
        }

        binding.composeMsgContentET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateCharacterCount()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        binding.composeMsgErrorPage.errorPageTryAgainBtn.setOnClickListener {
            getMemberList()
        }

    }

    @OptIn(ExperimentalLayoutApi::class)
    private fun setUpComposeMsgToComposeView() {
        binding.composeMsgToCV.setViewCompositionStrategy(
            androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.composeMsgToCV.setContent {
            val selectedMembers = composeMessageViewModel.tempSelectedMemberList
            val selectedGroups = composeMessageViewModel.tempSelectedMessageGroupList
            val isGroupMessageReply =
                composeMessageViewModel.isReplyOrForward && composeMessageViewModel.composeMessageType != ModelUtil.messageComposeTypeForwardIntent && composeMessageViewModel.message?.type == ModelUtil.messageTypeGroup && !composeMessageViewModel.isReplyToRestrictedGroup

            MPHRTheme {
                Column(
                    modifier = Modifier
                        .heightIn(
                            min = 41.dp
                        )
                        .clickable {
                            if (isGroupMessageReply) {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.group_reply_restriction_msg,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                intentToSearchProviderFragment()
                            }
                        }
                ) {
                    FlowRow(
                        modifier = Modifier
                            .padding(
                                start = MdTheme.dimension.horizontalPadding,
                                end = MdTheme.dimension.horizontalPadding,
                                top = MdTheme.dimension.horizontalPadding,
                                bottom = MdTheme.dimension.halfCardMargin
                            )
                            .fillMaxWidth(), Arrangement.spacedBy(8.dp), Arrangement.spacedBy(8.dp)

                    ) {
                        selectedMembers.forEach { member ->
                            MDChip(
                                chipText = member.nameWithTitle ?: "",
                                onRemoveHandler = {
                                    composeMessageViewModel.toggleMemberSelection(
                                        member
                                    )
                                }
                            )
                        }
                        selectedGroups.forEach { group ->
                            MDChip(
                                chipText = group.groupName ?: "",
                                onRemoveHandler = {
                                    if (isGroupMessageReply) {
                                        Toast.makeText(
                                            requireContext(),
                                            R.string.group_reply_restriction_msg,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        composeMessageViewModel.toggleMessageGroupSelection(group)
                                    }
                                },
                                showRemoveIcon = !isGroupMessageReply
                            )
                        }
                        if (!isGroupMessageReply) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, colorResource(R.color.colorPrimary)),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .height(32.dp)
                                    .clickable { intentToSearchProviderFragment() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp, vertical = 0.dp)
                                        .height(32.dp)
                                ) {
                                    Icon(
                                        modifier = Modifier.size(16.dp),
                                        painter = painterResource(R.drawable.ic_add_person),
                                        contentDescription = "Add",
                                        tint = colorResource(R.color.colorPrimary),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    MDTextView(
                                        value =getString(R.string.add_str),
                                        style = MdTheme.typography.semiBold16TextStyle.copy(
                                            color = colorResource(R.color.colorPrimary)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun intentToSearchProviderFragment() {
        val activity = requireActivity() as? ComposeMessageActivity
        composeMessageViewModel.backupSelections()
        activity?.showSearchProviderFragment()
    }

    private fun getMemberList() {
        binding.composeMsgErrorPage.root.visibility = View.GONE
        binding.composeMsgAttachmentBtn.visibility = View.GONE
        binding.composeMsgAttachmentBtn.visibility = View.GONE
        binding.composeMsgSendBtn.visibility = View.GONE
        progressDialog.show()
        progressDialog.setMessage(R.string.loading_data)

        composeMessageViewModel.memberListStartIndex = 0
        composeMessageViewModel.clearMemberList()
        composeMessageViewModel.getMemberList()

    }

    private fun setupObservers() {
        composeMessageViewModel.memberListLiveResponse.observe(viewLifecycleOwner) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is Pair<*, *>) {
                    composeMessageViewModel.clearMemberList()
                    composeMessageViewModel.clearMessageGroupList()
                    composeMessageViewModel.setMemberList(resp.second as ArrayList<Member>)
                    composeMessageViewModel.setMessageGroupList(resp.first as ArrayList<MessageGroup>)
                    binding.composeMsgErrorPage.root.visibility = View.GONE
                    binding.composeMsgAttachmentBtn.visibility = View.VISIBLE
                    binding.composeMsgSendBtn.visibility = View.VISIBLE

                    if (composeMessageViewModel.isReplyOrForward && composeMessageViewModel.composeMessageType != ModelUtil.messageComposeTypeForwardIntent) {
                        setSelectedMembersAndGroupsFromReply()
                    }
                    setUpComposeMsgToComposeView()
                } else if (resp is APIResponseError) {
                    Log.d(TAG, "APIResponseError")
                    binding.composeMsgScrollview.visibility = View.GONE
                    binding.composeMsgErrorPage.root.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setSelectedMembersAndGroupsFromReply() {
        if (composeMessageViewModel.message?.type == ModelUtil.messageTypeGroup) {
            composeMessageViewModel.message?.messageGroupMapList?.firstOrNull()?.group?.let {
                composeMessageViewModel.isReplyToRestrictedGroup =
                    !composeMessageViewModel.toggleMessageGroupSelection(it)
            }
            if (composeMessageViewModel.message?.messageRecipient?.fromId == (authManager.selectedPatientLive.value?.practicePatientId
                    ?: "")
            ) {
                //as the patient(current user) is the sender, we need to select group details from  member list as server send the group details in member list
                val groupList = arrayListOf<Member>()
                composeMessageViewModel.message?.messageMembersMapList?.mapNotNull { it.member }
                    ?.forEach { member ->
                        if (!groupList.any { it.memberId == member.memberId }) {
                            groupList.add(member)
                        }
                    }
                val group = MessageGroup()
                group.groupId = groupList.firstOrNull()?.memberId ?: ""
                group.groupName = groupList.firstOrNull()?.fullName ?: ""
                composeMessageViewModel.isReplyToRestrictedGroup =
                    !composeMessageViewModel.toggleMessageGroupSelection(group)
            }
        } else {
            if (composeMessageViewModel.message?.messageRecipient?.fromId == (authManager.selectedPatientLive.value?.practicePatientId
                    ?: "")
            ) {
                //as the patient(current user) is the sender, we need to select all the members from member list
                val memberList = arrayListOf<Member>()
                composeMessageViewModel.message?.messageMembersMapList?.mapNotNull { it.member }
                    ?.forEach { member ->
                        if (!memberList.any { it.memberId == member.memberId }) {
                            memberList.add(member)
                            if (!composeMessageViewModel.toggleMemberSelection(member)) {
                                composeMessageViewModel.isReplyToRestrictedMember = true
                            }
                        }
                    }
            } else {
                if (composeMessageViewModel.composeMessageType == ModelUtil.messageComposeTypeReplyIntent) {
                    //for reply, we need to select all the members except the sender which is marked using isReceiver flag
                    val selectedMemberList =
                        ArrayList(composeMessageViewModel.message?.messageMembersMapList?.filter { !it.isReceiver }
                            ?.mapNotNull { it.member }
                            ?: arrayListOf())
                    selectedMemberList.forEach {
                        if (!composeMessageViewModel.toggleMemberSelection(it)) {
                            composeMessageViewModel.isReplyToRestrictedMember = true
                        }
                    }
                } else {
                    //for forward, we need to select all the members from the message
                    val memberList = arrayListOf<Member>()
                    composeMessageViewModel.message?.messageMembersMapList?.mapNotNull { it.member }
                        ?.forEach { member ->
                            if (!memberList.any { it.memberId == member.memberId }) {
                                memberList.add(member)
                                if (!composeMessageViewModel.toggleMemberSelection(member)) {
                                    composeMessageViewModel.isReplyToRestrictedMember = true
                                }
                            }
                        }
                }
            }
        }
        if (composeMessageViewModel.isReplyToRestrictedMember) {
            Toast.makeText(
                requireActivity(),
                R.string.reply_restricted_member_error,
                Toast.LENGTH_LONG
            ).show()
        }
        if (composeMessageViewModel.isReplyToRestrictedGroup) {
            Toast.makeText(
                requireActivity(),
                R.string.reply_restricted_group_error,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun validateMessage() {
        val selectedMember = composeMessageViewModel.memberList.filter { it.isSelected }
        val selectedGroup = composeMessageViewModel.messageGroupList.filter { it.isSelected }
        if (selectedMember.isEmpty() && selectedGroup.isEmpty()) {
            UIUtil.showToast(
                requireActivity(),
                R.string.message_compose_empty_to_address,
                Toast.LENGTH_LONG
            )
            return
        }

        /* if (composeMsgContentET.text.toString().isBlank()){
             UIUtil.showToast(this , R.string.message_compose_empty_content, Toast.LENGTH_LONG)
             return
         }*/

        if (binding.composeMsgSubjectValueTV.text.toString().isBlank()) {

            val builder = AlertDialog.Builder(requireActivity())

            builder.setMessage(R.string.message_empty_subject_msg)
            builder.setPositiveButton(R.string.yes_str) { _, _ ->
                formMessage(selectedMember, selectedGroup)
            }
            builder.setNegativeButton(R.string.no_str) { _, _ ->

            }

            builder.show()
        } else {
            formMessage(selectedMember, selectedGroup)
        }

    }

    private fun formMessage(selectedMemberList: List<Member>, selectedGroup: List<MessageGroup>) {
        val messages = Messages()
        messages.subject = binding.composeMsgSubjectValueTV.text.toString()
        messages.content = changeToHtml(binding.composeMsgContentET.text.toString())
        messages.practicePatientId = authManager.selectedPatientLive.value?.practicePatientId

        val messageMemberMapList = arrayListOf<MessageMembersMap>()

        selectedMemberList.forEach { selectedMember ->
            val messageMembersMap = MessageMembersMap()
            messageMembersMap.member = selectedMember
            messageMembersMap.additionalMemberId = selectedMember.memberId

            messageMemberMapList.add(messageMembersMap)
        }
        val messageGroupMapList = arrayListOf<MessageGroupMap>()
        selectedGroup.forEach { group ->
            val messageGroupMap = MessageGroupMap()
            messageGroupMap.group = group
            messageGroupMap.groupId = group.groupId

            messageGroupMapList.add(messageGroupMap)
        }

        messages.practiceId = composeMessageViewModel.selectedPractice.practiceId


        messages.messagesAttachmentsMapList =
            composeMessageAttachmentAdapter.getMessageAttachmentList()

        messages.messageMembersMapList = messageMemberMapList
        messages.messageGroupMapList = messageGroupMapList
        if (composeMessageViewModel.isReplyOrForward) {
            if (composeMessageViewModel.composeMessageType != ModelUtil.messageComposeTypeForwardIntent && !composeMessageViewModel.isReplyToRestrictedGroup) {
                messages.ownerId = composeMessageViewModel.message?.ownerId
            }
        }
        sendMessage(messages)
    }

    private fun sendMessage(messages: Messages) {
        progressDialog.show()
        progressDialog.setMessage(R.string.sending_data_str)
        composeMessageViewModel.sendMessage(messages).observe(requireActivity()) { resp ->
            if (resp != null) {
                progressDialog.dismiss()

                if (resp is Boolean) {
                    UIUtil.showToast(
                        requireActivity(),
                        R.string.message_compose_success,
                        Toast.LENGTH_LONG
                    )

                    requireActivity().setResult(RESULT_OK)

                    if (composeMessageViewModel.isNotificationFlow) {
                        //Note: Tightly coupled with ComposeMessageActivity
                        val activity = requireActivity() as? ComposeMessageActivity
                        activity?.onBackPressed()
                    } else if (composeMessageViewModel.isReplyOrForward) {
                        val intent = Intent(
                            requireActivity().applicationContext,
                            MessageActivity::class.java
                        )
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        requireActivity().overridePendingTransition(
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                        )
                    } else {
                        val activity = requireActivity() as? ComposeMessageActivity
                        activity?.onBackPressed()
                    }

                } else if (resp is APIResponseError) {
                    UIUtil.showToast(
                        requireActivity(),
                        R.string.message_compose_failure,
                        Toast.LENGTH_LONG
                    )
                }
            }
        }
    }

    private fun changeToHtml(s: String): String {
        val builder = StringBuilder()
        var previousWasASpace = false
        for (c in s.toCharArray()) {
            if (c == ' ') {
                if (previousWasASpace) {
                    builder.append("&nbsp;")
                    previousWasASpace = false
                    continue
                }
                previousWasASpace = true
            } else {
                previousWasASpace = false
            }
            when (c) {
                '<' -> builder.append("&lt;")
                '>' -> builder.append("&gt;")
                '&' -> builder.append("&amp;")
                '"' -> builder.append("&quot;")
                '\n' -> builder.append("<br/>")
                // We need Tab support here, because we print StackTraces as HTML
                '\t' -> builder.append("&nbsp; &nbsp; &nbsp;")
                else -> if (c.code < 128) {
                    builder.append(c)
                } else {
                    builder.append("&#").append(c.code).append(";")
                }
            }
        }

        val builderString = builder.toString()

        val builderStringArr = builderString.split("<br/>")

        val respBuilder = StringBuilder()

        val builderStringArrLastIndex = builderStringArr.lastIndex

        builderStringArr.forEachIndexed { index, temp ->

            if (index == builderStringArrLastIndex) {
                if (temp.isNotBlank())
                    respBuilder.append("<div>$temp</div>")
            } else {
                respBuilder.append("<div>$temp<br/></div>")
            }
        }

        var finalResp = respBuilder.toString()

        if (composeMessageViewModel.isReplyOrForward) {
            finalResp += composeMessageViewModel.message?.content
        }

        return finalResp
    }

    private fun updateMaxLength() {

        hasShownMaxToast = false
        val filters = binding.composeMsgContentET.filters.toCollection(ArrayList())
        filters.removeAll { it is InputFilter.LengthFilter }
        val maxChars = composeMessageViewModel.currentNumberOfCharacters
        if (maxChars > 0) {
            filters.add(object : InputFilter.LengthFilter(maxChars) {
                override fun filter(
                    source: CharSequence?,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
                ): CharSequence? {
                    val result = super.filter(source, start, end, dest, dstart, dend)
                    val incoming = (end - start)
                    val kept = result?.length ?: incoming
                    if (kept < incoming && !hasShownMaxToast) {
                        hasShownMaxToast = true
                        showMaxCharToast(maxChars)
                    }
                    return result
                }
            })
        }
        binding.composeMsgContentET.filters = filters.toTypedArray()
    }

    private fun showMaxCharToast(maxChars: Int) {
        val ctx = activity ?: return
        try {
            maxCharToast?.cancel()
            maxCharToast = Toast.makeText(
                ctx,
                getString(R.string.compose_message_max_char_reached, maxChars),
                Toast.LENGTH_SHORT
            ).also { it.show() }
        } catch (e: Resources.NotFoundException) {
            Log.d(TAG, e.toString())
        } catch (e: IllegalStateException) {
            Log.d(TAG, e.toString())
        }
    }

    private fun updateCharacterCount() {

        if (composeMessageViewModel.currentNumberOfCharacters == 0) {
            if (binding.composeMsgCharCountSeparator.isVisible) {
                binding.composeMsgCharCountSeparator.visibility = View.GONE
                binding.composeMsgCharCountTV.visibility = View.GONE
            }
        } else {
            if (!binding.composeMsgCharCountSeparator.isVisible) {
                binding.composeMsgCharCountSeparator.visibility = View.VISIBLE
                binding.composeMsgCharCountTV.visibility = View.VISIBLE
            }
            val charCount = binding.composeMsgContentET.text.toString().length
            if (charCount < composeMessageViewModel.currentNumberOfCharacters) {
                hasShownMaxToast = false
            }
            binding.composeMsgCharCountTV.text = UIUtil.makeCharCount(
                requireActivity(),
                "$charCount",
                "${composeMessageViewModel.currentNumberOfCharacters}"
            )
        }
    }

    private fun hideMultiPracticeUI() {
        binding.composeMsgAttachmentBtn.visibility = View.GONE
        binding.composeMsgSendBtn.visibility = View.GONE
        binding.composeMsgErrorPage.root.visibility = View.GONE
        binding.composeMsgProgress.root.visibility = View.GONE


        binding.composeMsgToLabelTV.visibility = View.GONE
        binding.composeMsgToCV.visibility = View.GONE
        binding.composeMsgToSeparator.visibility = View.GONE
        binding.composeMsgSubjectLabelTV.visibility = View.GONE
        binding.composeMsgSubjectValueTV.visibility = View.GONE
        binding.messageComposeSeparator.visibility = View.GONE
        binding.composeMsgContentET.visibility = View.GONE
        binding.composeMsgAlreadyAddedTxtWebview.visibility = View.GONE
        binding.composeMsgCharCountTV.visibility = View.GONE
        binding.composeMsgCharCountSeparator.visibility = View.GONE
        binding.composeMessageAttachmentRV.visibility = View.GONE
    }

    private fun showMultiPracticeUI() {
        binding.composeMsgAttachmentBtn.visibility = View.VISIBLE
        binding.composeMsgSendBtn.visibility = View.VISIBLE

        binding.composeMsgToLabelTV.visibility = View.VISIBLE
        binding.composeMsgToCV.visibility = View.VISIBLE
        binding.composeMsgToSeparator.visibility = View.VISIBLE
        binding.composeMsgSubjectLabelTV.visibility = View.VISIBLE
        binding.composeMsgSubjectValueTV.visibility = View.VISIBLE
        binding.messageComposeSeparator.visibility = View.VISIBLE
        binding.composeMsgContentET.visibility = View.VISIBLE
        binding.composeMsgAlreadyAddedTxtWebview.visibility = View.VISIBLE
        binding.composeMsgCharCountTV.visibility = View.VISIBLE
        binding.composeMsgCharCountSeparator.visibility = View.VISIBLE
        binding.composeMessageAttachmentRV.visibility = View.VISIBLE
    }

    private fun updatePracticeList() {
        if (composeMessageViewModel.practiceList.size == 1) {
            binding.composeMsgPracticeValueET.visibility = View.GONE
            binding.composeMsgPracticeValueTV.visibility = View.VISIBLE
            updateSelectedPractice(composeMessageViewModel.practiceList.first())
        } else {
            binding.composeMsgPracticeValueET.visibility = View.VISIBLE
            binding.composeMsgPracticeValueTV.visibility = View.GONE
            val practiceListStr =
                composeMessageViewModel.practiceList.mapNotNull { practice -> practice.practiceName }
            binding.composeMsgPracticeValueET.setItems(practiceListStr, requireActivity())
            binding.composeMsgPracticeValueET.setOnItemClickListener { position, str ->
                updateSelectedPractice(
                    composeMessageViewModel.practiceList[position]
                )
                showMultiPracticeUI()
            }
            hideMultiPracticeUI()
        }
    }

    fun updateSelectedPractice(practice: PracticeList) {
        composeMessageViewModel.selectedPractice = practice
        binding.composeMsgPracticeValueTV.text = practice.practiceName
        composeMessageViewModel.currentNumberOfCharacters =
            composeMessageViewModel.composeRestrictedList.firstOrNull { it.practiceId == practice.practiceId }?.noOfCharsAllowed
                ?: 0

        updateMaxLength()
        updateCharacterCount()
        getMemberList()
    }

    fun getAllowedPractices(): List<PracticeList>? {
        val allowedPractice = composeMessageViewModel.composeRestrictedList
            .filter { it.allowPHRMessaging && !it.isMessageRestrict }
            .map { it.practiceId }

        return authManager.selectedPatientLive.value?.practiceList?.filter {
            allowedPractice.contains(
                it.practiceId
            )
        }
    }

}
