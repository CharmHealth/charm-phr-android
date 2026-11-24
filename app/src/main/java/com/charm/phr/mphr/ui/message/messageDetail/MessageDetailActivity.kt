package com.charm.phr.mphr.ui.message.messageDetail

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityMessageDetailBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.model.MessagesAttachmentsMap
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.ui.dashboard.DashboardActivity
import com.charm.phr.mphr.ui.health_passport.GridSpacingItemDecoration
import com.charm.phr.mphr.ui.message.compose.ComposeMessageActivity
import com.charm.phr.mphr.util.APIList
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.NotificationUtil
import com.charm.phr.mphr.util.PermissionUtil
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import javax.inject.Inject

class MessageDetailActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var messageDetailViewModel: MessageDetailViewModel

    @Inject
    lateinit var messageDetailAttachmentAdapter: MessageDetailAttachmentAdapter

    @Inject
    lateinit var picasso: Picasso

    lateinit var binding: ActivityMessageDetailBinding

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    val TAG = "MessageDetailActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageDetailViewModel = ViewModelProvider(this, viewModelFactory)[MessageDetailViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.messageDetailToolbar)
        UIUtil.applyViewInsertForBottomView(binding.messageDetailMainLayout)

        messageDetailViewModel.messageId = intent.getStringExtra(ModelUtil.messageIdIntent) ?: ""
        messageDetailViewModel.practiceId = intent.getStringExtra(ModelUtil.practiceIdIntent) ?: ""

        checkAndClearMessageNotification()

        if (BuildUtil.isPushNotificationEnabled) {
            val isNotificationPermissionDenied = sharedPreferences.getBoolean(PermissionUtil.isNotificationPermissionDenied, false)
            if(!isNotificationPermissionDenied){
                initializePermissionResultLauncher()
                PermissionUtil.requestNotificationPermission(this, ::permissionAcceptCallBack,
                    requestPermissionLauncher, skipOnDenied = true)
            }
        }

        val patientId = intent.getStringExtra(ModelUtil.patientIdIntent)
        messageDetailViewModel.patientId = if (patientId.isNullOrBlank()) {
            authManager.selectedPatientLive.value?.practicePatientId ?: ""
        } else {
            patientId
        }

        messageDetailViewModel.isNotificationFlow = intent.getBooleanExtra(ModelUtil.isNotificationFlow, false)

        if (messageDetailViewModel.isNotificationFlow) {
            authManager.switchSelectedPatientForId(messageDetailViewModel.patientId)
        }

        val restrictionString = intent.getStringExtra(ModelUtil.messageComposeMsgRestrictionListIntent) ?: ""
        MDParsingHelper.formComposeMsgRestrictionList(restrictionString).firstOrNull()?.let {
            messageDetailViewModel.composeMsgRestriction = it
            messageDetailViewModel.isComposeMsgRestrictionFetched = true
        }

        binding.messageDetailAttachmentRV.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)

        binding.messageDetailAttachmentRV.addItemDecoration(
            GridSpacingItemDecoration(
                2,
                resources.getDimensionPixelSize(R.dimen.card_padding),
                resources.getDimensionPixelSize(R.dimen.card_padding),
                includeEdge = true
            )
        )

        binding.messageDetailAttachmentRV.adapter = messageDetailAttachmentAdapter

        if (messageDetailViewModel.isMessageDetailFetched) {

            if (messageDetailViewModel.messageLiveData.hasActiveObservers()) {
                messageDetailViewModel.messageLiveData.removeObservers(this)
            }
            messageDetailViewModel.messageLiveData.observe(this) { messages ->

                if (messages != null) {
                    setMessageData(messages)
                }
            }
        } else {
            fetchMessageDetailWithComposeRestriction()
        }

        binding.messageDetailErrorPage.errorPageTryAgainBtn.setOnClickListener {
            fetchMessageDetailWithComposeRestriction()
        }

        binding.messageDetailBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.messageDetailReplyBtn.setOnClickListener {

            if (messageDetailViewModel.composeMsgRestriction.isMessageRestrict) {
                UIUtil.showToast(this, R.string.message_restrict_msg, Toast.LENGTH_LONG)
            } else {

                val intent = Intent(this, ComposeMessageActivity::class.java)
                intent.putExtra(
                    ModelUtil.messageDataIntent,
                    messageDetailViewModel.messageLiveData.value?.toString
                )
                intent.putExtra(
                    ModelUtil.messageComposeTypeIntent,
                    ModelUtil.messageComposeTypeReplyIntent
                )
                intent.putExtra(ModelUtil.isNotificationFlow, messageDetailViewModel.isNotificationFlow)
                val messageDateString =
                    messageDetailViewModel.messageLiveData.value?.messageDate?.toString(ModelUtil.sqlTimeStampFormattor()) ?: ""
                intent.putExtra(
                    ModelUtil.messageDateIntent,
                    messageDateString
                )
                intent.putExtra(ModelUtil.practiceIdIntent, messageDetailViewModel.practiceId)
                intent.putExtra(
                    ModelUtil.messageNoOfCharAllowedIntent,
                    messageDetailViewModel.composeMsgRestriction.noOfCharsAllowed
                )
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

        val popupMenu = PopupMenu(this, binding.messageDetailMoreBtnAnchor, Gravity.END)

        popupMenu.inflate(R.menu.menu_message_detail_types)
        popupMenu.setOnMenuItemClickListener { menuItem ->

            val messageDateString =
                messageDetailViewModel.messageLiveData.value?.messageDate?.toString(ModelUtil.sqlTimeStampFormattor()) ?: ""

            when (menuItem.itemId) {
                R.id.messageDetailReplyAllBtn -> {

                    if (messageDetailViewModel.composeMsgRestriction.isMessageRestrict) {
                        UIUtil.showToast(this, R.string.message_restrict_msg, Toast.LENGTH_LONG)
                    } else {

                        val intent = Intent(this, ComposeMessageActivity::class.java)
                        intent.putExtra(
                            ModelUtil.messageDataIntent,
                            messageDetailViewModel.messageLiveData.value?.toString
                        )
                        intent.putExtra(
                            ModelUtil.messageComposeTypeIntent,
                            ModelUtil.messageComposeTypeReplyAllIntent
                        )
                        intent.putExtra(ModelUtil.isNotificationFlow, messageDetailViewModel.isNotificationFlow)
                        intent.putExtra(
                            ModelUtil.messageDateIntent,
                            messageDateString
                        )
                        intent.putExtra(
                            ModelUtil.practiceIdIntent,
                            messageDetailViewModel.practiceId
                        )
                        intent.putExtra(
                            ModelUtil.messageNoOfCharAllowedIntent,
                            messageDetailViewModel.composeMsgRestriction.noOfCharsAllowed
                        )
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }

                R.id.messageDetailForwardBtn -> {
                    if (messageDetailViewModel.composeMsgRestriction.isMessageRestrict) {
                        UIUtil.showToast(this, R.string.message_restrict_msg, Toast.LENGTH_LONG)
                    } else {
                        val intent = Intent(this, ComposeMessageActivity::class.java)
                        intent.putExtra(
                            ModelUtil.messageDataIntent,
                            messageDetailViewModel.messageLiveData.value?.toString
                        )
                        intent.putExtra(
                            ModelUtil.messageComposeTypeIntent,
                            ModelUtil.messageComposeTypeForwardIntent
                        )
                        intent.putExtra(ModelUtil.isNotificationFlow, messageDetailViewModel.isNotificationFlow)
                        intent.putExtra(
                            ModelUtil.messageDateIntent,
                            messageDateString
                        )
                        intent.putExtra(
                            ModelUtil.practiceIdIntent,
                            messageDetailViewModel.practiceId
                        )
                        intent.putExtra(
                            ModelUtil.messageNoOfCharAllowedIntent,
                            messageDetailViewModel.composeMsgRestriction.noOfCharsAllowed
                        )
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }
            }

            return@setOnMenuItemClickListener true
        }


        binding.messageDetailMoreBtn.setOnClickListener {
            popupMenu.show()
        }

        binding.messageDetailDeleteBtn.visibility = View.VISIBLE
        binding.messageDetailDeleteBtn.setOnClickListener {
            deleteMessage()
        }

        messageDetailAttachmentAdapter.setMessageDetailAttachmentSelectListener(object :
            MessageDetailAttachmentAdapter.MessageDetailAttachmentSelectListener {
            override fun onAttachmentSelect(messagesAttachmentsMap: MessagesAttachmentsMap) {

                val fileTemp = FileUtil.getMessageAttachment(this@MessageDetailActivity, messagesAttachmentsMap)

                if (fileTemp != null) {
                    startIntentToOpenAttachment(fileTemp)
                } else {
                    progressDialog.show()
                    messageDetailViewModel.getAttachment(messagesAttachmentsMap.attachmentId ?: "")
                        .observe(this@MessageDetailActivity) { resp ->
                            if (resp != null) {
                                progressDialog.dismiss()

                                if (resp is MDFile) {

                                    val file = FileUtil.saveMessageAttachment(
                                        resp,
                                        messagesAttachmentsMap,
                                        this@MessageDetailActivity.applicationContext
                                    )
                                    if (file != null) {
                                        startIntentToOpenAttachment(file)
                                    }
                                } else if (resp is APIResponseError) {
                                    UIUtil.showToast(
                                        this@MessageDetailActivity,
                                        resp.errorMsg ?: getString(R.string.something_went_wrong),
                                        Toast.LENGTH_LONG
                                    )
                                }
                            }
                        }
                }
            }
        })
    }

    private fun checkAndClearMessageNotification() {
        val localIds = NotificationUtil.getNotificationIds(sharedPreferences, ModelUtil.localMessageNotificationIds)
        if (localIds.isNotEmpty()){
            NotificationUtil.clearNotificationForIds(
                applicationContext, localIds,
                ModelUtil.localMessageNotificationIds
            )
        }
    }

    override fun onResume() {
        super.onResume()

        Log.d(
            TAG, "onResume called isError: isComposeMsgRestrictionError ${messageDetailViewModel.isComposeMsgRestrictionError} " +
                    "isMessageDetailError: ${messageDetailViewModel.isMessageDetailError}"
        )
        if (messageDetailViewModel.isMessageDetailError || messageDetailViewModel.isComposeMsgRestrictionError) {
            fetchMessageDetailWithComposeRestriction()
        } else {
            val messageData = messageDetailViewModel.messageLiveData.value
            if (messageData != null) {
                setMessageData(messageData)
            }
        }
    }

    override fun onBackPressed() {

        if (messageDetailViewModel.isNotificationFlow && isTaskRoot) {
            val intent = Intent(applicationContext, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            return
        }

        setResult(Activity.RESULT_FIRST_USER)
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun startIntentToOpenAttachment(file: File) {
        val fileUri = try {
            FileProvider.getUriForFile(applicationContext, resources.getString(R.string.content_provider_authority), file)
        } catch (e: IllegalArgumentException) {
            UIUtil.showToast(this, R.string.no_app_found_to_open_file, Toast.LENGTH_LONG)
            return
        }

        val isPDF = file.name.endsWith("pdf", true)
        val isImage = file.name.endsWith("jpg", true) || file.name.endsWith("png", true) || file.name.endsWith("jpeg", true)

        val mimeType = when {
            isPDF -> "application/pdf"
            isImage -> "image/*"
            else -> "*/*"
        }

        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(fileUri, mimeType)

        isPassCodeAllowed = false   // preventing from asking passcode
        try {
            startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            isPassCodeAllowed = true  // reset — passcode bypass must not persist on failure
            UIUtil.showToast(this, R.string.no_app_found_to_open_file, Toast.LENGTH_LONG)
        }
    }

    private fun setMessageData(message: Messages) {

        showContent()

//        message.messageDate = messageDetailViewModel.messageDate

        binding.messageDetailFromName.text = message.messageRecipient?.fromName

        var toNameString = ""

        val messagePatientCount = message.messagePatientList.count()

        if (messagePatientCount > 0) {
            message.messagePatientList.forEachIndexed { index, patient ->
                toNameString += patient.fullName
                if (index != messagePatientCount - 1 && messagePatientCount != 1) {
                    toNameString += " , "
                }
            }
        }

        val messageMemberCount = message.messageMembersMapList.filter { it.isReceiver }.count()

        if (messageMemberCount > 0) {
            if (toNameString.isNotBlank()) {
                toNameString += " , "
            }

            message.messageMembersMapList.filter { it.isReceiver }.forEachIndexed { index, member ->
                toNameString += member.member?.fullName
                if (index != messageMemberCount - 1 && messageMemberCount != 1) {
                    toNameString += " , "
                }
            }
        }

        toNameString = getString(R.string.message_detail_to_str, toNameString)


        binding.messageDetailToName.text = toNameString

        binding.messageDetailDateTV.text = message.messageDate?.toString(ModelUtil.charmMessageDetailContentDateInCardFormatter())

        binding.messageDetailSubject.text = message.subject

        val patient = authManager.selectedPatientLive.value



        if (message.messageRecipient?.fromId == patient?.practicePatientId) {

            val initials = UIUtil.formPatientInitials(patient ?: Patient())

            binding.messageDetailProfilePic.visibility = View.INVISIBLE
            binding.messageDetailProfileDummyTV.visibility = View.VISIBLE
            binding.messageDetailProfileDummyTV.text = initials

            if (patient!!.isPhotoAvailableOnline && authManager.isUserOnline()) {
                val picLoader = picasso.load(APIList.getPatientImageURL(patient.practicePatientId))
                val imgSize = resources.getDimensionPixelSize(R.dimen.patient_list_img_size)
                picLoader.resize(imgSize, imgSize).into(binding.messageDetailProfilePic, object : Callback {
                    override fun onSuccess() {
                        binding.messageDetailProfileDummyTV.visibility = View.GONE
                        binding.messageDetailProfilePic.visibility = View.VISIBLE
                    }

                    override fun onError(e: Exception?) {
                    }
                })
            }

        } else {

            val memberName = message.messageRecipient?.fromName ?: ""
            val memInitials = UIUtil.formMemberInitials(memberName)
            binding.messageDetailProfileDummyTV.visibility = View.VISIBLE
            binding.messageDetailProfileDummyTV.text = memInitials
            binding.messageDetailProfilePic.visibility = View.INVISIBLE

            binding.messageDetailProfileDummyTV.visibility = View.VISIBLE
            val picLoader = picasso.load(APIList.getMemberImageURL(message.messageRecipient?.fromId, messageDetailViewModel.practiceId))
            val imgSize = resources.getDimensionPixelSize(R.dimen.patient_list_img_size)

            picLoader.resize(imgSize, imgSize).into(binding.messageDetailProfilePic, object : Callback {
                override fun onSuccess() {
                    binding.messageDetailProfilePic.visibility = View.VISIBLE
                    binding.messageDetailProfileDummyTV.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                }
            })
        }

        /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          Html.fromHtml(message.content, Html.FROM_HTML_MODE_COMPACT)
      }
      else {
          Html.fromHtml(message.content)
      }*/

        //  this function will enable all the links in the content to open in browser
        binding.messageDetailContent.text = HtmlCompat.fromHtml(message.content ?: "", 0)
        binding.messageDetailContent.movementMethod = LinkMovementMethod.getInstance()

        binding.messageDetailWebview.isHorizontalScrollBarEnabled = true
        binding.messageDetailWebview.isVerticalScrollBarEnabled = true

        binding.messageDetailWebview.settings.javaScriptEnabled = true
        binding.messageDetailWebview.loadDataWithBaseURL(
            null,
            "<html style='background-color:#f6f6f8'>${message.content ?: ""}</html>",
            "text/html",
            "utf-8",
            null
        )


        binding.messageDetailWebview.webViewClient = object : WebViewClient() {
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val intent = Intent(Intent.ACTION_VIEW, request?.url)
                startActivity(intent)
                return true
            }
        }


        if (messageDetailViewModel.composeMsgRestriction.allowPHRMessaging) {
            binding.messageDetailMoreBtn.visibility = View.VISIBLE
            binding.messageDetailReplyBtn.visibility = View.VISIBLE
        } else {
            binding.messageDetailMoreBtn.visibility = View.GONE
            binding.messageDetailReplyBtn.visibility = View.GONE
        }

        val attachmentSize = messageDetailAttachmentAdapter.setMessageAttachmentMapList(message.messagesAttachmentsMapList)

        if (attachmentSize == 0) {
            binding.messageDetailAttachmentRV.visibility = View.GONE
        } else {
            binding.messageDetailAttachmentRV.visibility = View.VISIBLE
        }

    }

    private fun deleteMessage() {
        val builder = AlertDialog.Builder(this)

        builder.setMessage(R.string.message_delete_confirmation_msg)
        builder.setPositiveButton(R.string.yes_str) { _, _ ->
            progressDialog.show()
            messageDetailViewModel.deleteMessage(messageDetailViewModel.messageId).observe(this) { resp ->
                if (resp != null) {
                    progressDialog.dismiss()
                    if (resp is Boolean) {
                        UIUtil.showToast(this, R.string.message_delete_success_msg, Toast.LENGTH_LONG)
                        setResult(Activity.RESULT_OK)
                        super.onBackPressed()
                    } else if (resp is APIResponseError) {
                        UIUtil.showToast(this, R.string.error_message_delete__msg, Toast.LENGTH_LONG)
                    }
                }
            }
        }
        builder.setNegativeButton(R.string.no_str) { _, _ ->

        }

        builder.show()

    }

    fun fetchMessageDetailWithComposeRestriction() {

        showLoading()

        val practiceId = messageDetailViewModel.practiceId
        val patientId = messageDetailViewModel.patientId
        val messageId = messageDetailViewModel.messageId

        Log.d(TAG, " fetchMessageDetailWithComposeRestriction practiceId :${practiceId}, patientId: ${patientId}, messageId: ${messageId}")

        if (!messageDetailViewModel.isMessageDetailFetched || messageDetailViewModel.isMessageDetailError) {
            getMessageDetail(messageId, practiceId, patientId)
        }
        if (!messageDetailViewModel.isComposeMsgRestrictionFetched || messageDetailViewModel.isComposeMsgRestrictionError) {
            getMessageRestriction(patientId, practiceId)
        }
    }

    private fun getMessageRestriction(patientId: String, practiceId: String) {
        Log.d(TAG, "getMessageRestriction patientId: $patientId, practiceId: $practiceId")
        messageDetailViewModel.isMessageDetailError = false
        messageDetailViewModel.getComposeMessageRestriction(patientId, practiceId).observe(this) { restriction ->
            if (restriction != null) {
                messageDetailViewModel.isComposeMsgRestrictionFetched = true
                if (restriction is ComposeMsgRestriction) {
                    messageDetailViewModel.composeMsgRestriction = restriction
                } else if (restriction is APIResponseError) {
                    Log.d(TAG, "Error fetching compose restriction: ${restriction.errorMsg}")
                    messageDetailViewModel.isComposeMsgRestrictionError = true
                }
            }
            updateLoadingState()
        }
    }

    private fun getMessageDetail(messageId: String, practiceId: String, patientId: String) {
        messageDetailViewModel.isMessageDetailError = false
        Log.d(TAG, "getMessageDetail messageId: $messageId, practiceId: $practiceId, patientId: $patientId")
        messageDetailViewModel.fetchMessageDetail(messageId, practiceId, patientId).observe(this) { message ->
            if (message != null) {
                messageDetailViewModel.isMessageDetailFetched = true
                if (message is Messages) {
                    messageDetailViewModel.messageLiveData.postValue(message)
                    setMessageData(message)
                } else if (message is APIResponseError) {
                    Log.d(TAG, "Error in fetching message detail: ${message.errorMsg}")
                    messageDetailViewModel.isMessageDetailError = true
                }
            }
            updateLoadingState()
        }
    }

    private fun updateLoadingState() {
        if (messageDetailViewModel.isMessageDetailFetched && messageDetailViewModel.isComposeMsgRestrictionFetched) {
            if (messageDetailViewModel.isMessageDetailError || messageDetailViewModel.isComposeMsgRestrictionError) {
                showError()
            } else {
                showContent()
            }
        } else {
            showLoading()
        }
    }

    private fun showLoading() {
        binding.messageDetailProgress.root.visibility = View.VISIBLE
        binding.messageDetailScrollView.visibility = View.GONE
        binding.messageDetailErrorPage.root.visibility = View.GONE
    }

    private fun showContent() {
        binding.messageDetailProgress.root.visibility = View.GONE
        binding.messageDetailScrollView.visibility = View.VISIBLE
        binding.messageDetailErrorPage.root.visibility = View.GONE
    }

    private fun showError() {
        binding.messageDetailProgress.root.visibility = View.GONE
        binding.messageDetailScrollView.visibility = View.GONE
        binding.messageDetailErrorPage.root.visibility = View.VISIBLE
    }

    private fun initializePermissionResultLauncher() {
        requestPermissionLauncher = PermissionUtil.getPermissionResultLauncher(
            this,
            ::permissionAcceptCallBack,
            R.string.allow_notification_for_message,
            null, false, ::permissionDeniedCallBack
        )
    }

    private fun permissionAcceptCallBack(permissions: Array<String>) {
        permissions.forEach { permission ->
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    //
                }
            }
        }
    }


    private fun permissionDeniedCallBack(permissions: Array<String>) {
    }

}
