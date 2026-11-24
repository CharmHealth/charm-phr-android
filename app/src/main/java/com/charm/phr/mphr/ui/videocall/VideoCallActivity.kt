package com.charm.phr.mphr.ui.videocall

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import charmzoom.CharmVideoCallActivity
import charmzoom.CvlModelUtil
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.OpenTokCredentials
import com.charm.phr.mphr.model.TeleHealthCredentials
import com.charm.phr.mphr.model.ZoomCredentials
import com.charm.phr.mphr.ui.questionnaire.QuestionnaireActivity
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.components.ProgressDialog
import com.charm.phr.mphr.ui.components.compose.OpenTokMeetingDialog
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.charm.videocall.databinding.LayoutCvlProgressBarBinding
import javax.inject.Inject

class VideoCallActivity : MDBaseActivity() {
    //MeetingServiceListener, ZoomSDKInitializeListener

    val TAG = "VideoCallActivity"

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var videoCallViewModel: VideoCallViewModel

    lateinit var imm: InputMethodManager

    companion object {
        val ACTION_RETURN_FROM_MEETING = "com.charm.phr.mphr.ui.videoCall"
        val EXTRA_TAB_ID = "tabId"
    }


    lateinit var binding: LayoutCvlProgressBarBinding
    //TODO:Remove hard code value before live build

    private val questionnaireResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                videoCallViewModel.teleHealthCredentials?.let { proceedWithVideoCall(it) }
            } else {
                backPressed()
            }
        }

    //region lifeCycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LayoutCvlProgressBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoCallViewModel = ViewModelProvider(this, viewModelFactory).get(VideoCallViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.zmMeetingWaitingRoomToolbar)
        UIUtil.applyViewInsertForBottomView(binding.zmMeetingConnectingLayout)

        progressDialog = ProgressDialog(this)

        videoCallViewModel.finishOnResume = false

        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        val toFinish =
            intent.getBooleanExtra(com.charm.phr.mphr.zoom.ui.ModelUtil.zmToFinish, false)

//        if (toFinish) {
//            onFinish()
//            return
//        }
//        if (ModelUtil.isVideoCallInProgress()) {
//            UIUtil.showToast(this, R.string.video_call_already_in_meeting_str, Toast.LENGTH_LONG)
//            onBackPressed()
//            return
//        }
        val uri = intent.data
        var meetingId = ""
        var userName = ""
        if (meetingId.isBlank()) {
            meetingId = intent.getStringExtra(ModelUtil.videoMeetingIdIntent) ?: ""
            userName = intent.getStringExtra(ModelUtil.videoMeetingNameIntent) ?: ""
        }
//        videoCallViewModel.isJoinMeeting = intent.getBooleanExtra(ModelUtil.videoJoinMeetingIntent, false)
//        if (!videoCallViewModel.isJoinMeeting) {
//            if (meetingId.isNotBlank()) {
//                videoCallViewModel.isJoinMeeting = true
//            }
//        }
//        if (videoCallViewModel.isJoinMeeting) {
//            binding = ActivityVideoCallBinding.inflate(layoutInflater)
//            setContentView(binding.root)
//            val zoomCredentials = ZoomCredentials()
//            zoomCredentials.webDomain = MDAccountsRelatedUtil.zoomDomain
//            videoCallViewModel.zoomCredentials = zoomCredentials
//
//            binding.videoCallMeetingIdET.addTextChangedListener(object : TextWatcher {
//                private val totalTxtCount = 13 // size of pattern 0000 0000 0000 0000
//                private val dividerModulo = 5 //  means divider position is every 5th symbol beginning with 1
//                private val dividerPosition = dividerModulo - 1 // means divider position is every 4th symbol beginning with 0
//                private val divider = " "
//                override fun afterTextChanged(editable: Editable) {
//                    if (!isInputCorrect(editable, totalTxtCount, dividerModulo, divider)) {
//                        binding.videoCallMeetingIdET.setText(buildCorrectString(getDigitArray(editable), dividerPosition, divider))
//                        binding.videoCallMeetingIdET.setSelection(binding.videoCallMeetingIdET.text?.length ?: 0)
//                    }
//                }
//
//                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                }
//
//                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                }
//
//                private fun isInputCorrect(editable: Editable, totalSymbols: Int, dividerModulo: Int, divider: String): Boolean {
//                    var isCorrect = editable.length <= totalSymbols
//                    val totalDigits = editable.filter { Character.isDigit(it) }.length
//                    for (index in 0 until editable.length) {
//                        isCorrect = if (index > 0 && ((totalDigits <= 10 && (index == 3 || index == 7)) || (totalDigits == 11 && (index == 3 || index == 8)))) {
//                            isCorrect && divider == editable.toString()[index].toString()
//                        } else {
//                            isCorrect && Character.isDigit(editable.toString()[index])
//                        }
//                    }
//                    if (editable.isNotEmpty()) {
//                        isCorrect = isCorrect && divider != editable.toString()[editable.length - 1].toString()
//                    }
//                    Log.d("formatted", "${editable.toString()}")
//                    return isCorrect
//                }
//
//                private fun buildCorrectString(digits: ArrayList<Char>, dividerPosition: Int, divider: String): String {
//                    val formatted: StringBuilder = StringBuilder()
//                    val totalDigits = digits.size
//                    for (index in 0 until totalDigits) {
//                        formatted.append(digits[index])
//                        val tobeCheckedIndex = index + 1
//                        if (index > 0 && ((totalDigits <= 10 && (tobeCheckedIndex == 3 || tobeCheckedIndex == 6)) || (totalDigits == 11 && (tobeCheckedIndex == 3 || tobeCheckedIndex == 7)))) {
//                            formatted.append(divider)
//                        }
//                    }
//                    Log.d("formattedStr", "$formatted")
//                    return formatted.toString().trim();
//                }
//
//                private fun getDigitArray(s: Editable): ArrayList<Char> {
//                    val digits = arrayListOf<Char>()
//                    for (i in 0 until s.length) {
//                        val current = s[i]
//                        if (Character.isDigit(current)) {
//                            digits.add(current)
//                        }
//                    }
//                    return digits
//                }
//            })
//
//            if (meetingId.isNotBlank()) {
//                binding.videoCallMeetingIdET.setText(meetingId)
//            }
//
//            if (userName.isNotBlank()) {
//                binding.videoCallMeetingNameET.setText(userName)
//            }
//
//            if (meetingId.isNotBlank() && userName.isNotBlank()) {
//                onJoinBtnClick()
//            }
//
//            binding.videoCallCloseBtn.setOnClickListener {
//                backPressed()
//            }
//
//            binding.videoCallJoinBtn.setOnClickListener {
//                onJoinBtnClick()
//            }
//
//        } else {
        videoCallViewModel.appointmentId = intent.getStringExtra(ModelUtil.appointmentIdIntent)
        videoCallViewModel.practiceId = intent.getStringExtra(ModelUtil.practiceIdIntent)
        if (authManager.isUserOnline()) {
            getVideoCallCredentialsForConsultation(videoCallViewModel.appointmentId ?: "")
        } else showErrorDialogForVideoCallCredentials(
            videoCallViewModel.appointmentId,
            getString(R.string.no_internet_connection_try_again)
        )
//        }

    }

    override fun onPause() {
        super.onPause()
        isPassCodeAllowed = false
    }

    override fun onResume() {
        super.onResume()
        isPassCodeAllowed = false
        if (videoCallViewModel.finishOnResume) {
            onBackPressed()
        } else {
            videoCallViewModel.finishOnResume = true
        }
        /*  if (ZoomSDK.getInstance().isInitialized) {
              val meetingStatus = ZoomSDK.getInstance().meetingService.meetingStatus
              Log.d(TAG, "onResume $meetingStatus")
              if (meetingStatus ==  MeetingStatus.MEETING_STATUS_DISCONNECTING || meetingStatus ==  MeetingStatus.MEETING_STATUS_IDLE){
                  backPressed()
              }
          }*/

    }

//    private fun onJoinBtnClick() {
//        var isErrorFound = false
//        if (binding.videoCallMeetingIdET.text.toString().isBlank()) {
//            isErrorFound = true
//            UIUtil.showToast(this, R.string.error_empty_meeting_id, Toast.LENGTH_LONG)
//        }
//        if (!isErrorFound) {
//            if (binding.videoCallMeetingNameET.text.toString().isBlank()) {
//                isErrorFound = true
//                UIUtil.showToast(this, R.string.error_empty_user_name, Toast.LENGTH_LONG)
//            }
//        }
//        if (!isErrorFound) {
//            if (ZoomSDK.getInstance().isInitialized) {
//                joinMeetingWithNumber()
//            } else {
//                progressDialog.show()
//                progressDialog.setMessage(R.string.connecting)
//                initZoomSdk()
//            }
//        }
//    }
//
//    private fun checkAndFinishActivity() {
//        if (videoCallViewModel.finishOnResume) {
//            finish()
//            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
//        } else {
//            val toFinish = intent.getBooleanExtra(com.charm.phr.mphr.zoom.ui.ModelUtil.zmToFinish, false)
//            Log.d("MDBaseActivity", "On Resume $toFinish")
//            if (toFinish) {
//                onFinish()
//                return
//            }
//        }
//    }

    override fun onBackPressed() {
        Log.d(TAG, "backpressed")
        backPressed()
    }

    override fun onDestroy() {
//        val zoomSDK = ZoomSDK.getInstance()
//        if (zoomSDK.isInitialized) {
//            val meetingService = zoomSDK.meetingService
//            meetingService.removeListener(this)
//        }
        isPassCodeAllowed = false
        super.onDestroy()
    }

    //endregion

    //region interface implementations

//    override fun onZoomSDKInitializeResult(errorCode: Int, internalErrorCode: Int) {
//        Log.d(TAG, "onZoomSDKInitializeResult, errorCode=$errorCode, internalErrorCode=$internalErrorCode")
//        progressDialog.dismiss()
//        if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
//            showErrorDialogForVideoCallCredentials(videoCallViewModel.appointmentId, getString(R.string.sdk_init_error))
//        } else {
//            registerMeetingServiceListener()
//
//            ZoomSDK.getInstance().zoomUIService.enableMinimizeMeeting(false)
//            ZoomSDK.getInstance().zoomUIService.hideMiniMeetingWindow()
//            ZoomSDK.getInstance().zoomUIService.disablePIPMode(true)
//            ZoomSDK.getInstance().zoomUIService.hideReactionsOnMeetingUI(true)
//            ZoomSDK.getInstance().zoomUIService.hideCloudWhiteboardShareButton(true)
//            ZoomSDK.getInstance().zoomUIService.hideCloudWhiteboardAboutButton(true)
//
//            ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled = false
//            ZoomSDK.getInstance().meetingSettingsHelper.setClaimHostWithHostKeyActionEnabled(false)
//            ZoomSDK.getInstance().meetingSettingsHelper.enable720p(false)
//            ZoomSDK.getInstance().meetingSettingsHelper.enableCloudWhiteboard(false)
//            ZoomSDK.getInstance().meetingSettingsHelper.hiddenQA(true)
//
//            checkAndStartOrJoinMeeting()
//        }
//    }
//
//    override fun onZoomAuthIdentityExpired() {}
//
//    override fun onMeetingStatusChanged(meetingStatus: MeetingStatus?, errorCode: Int, internalErrorCode: Int) {
//        Log.d(TAG, "meetingStatus $meetingStatus errorCode $errorCode")
//        if (meetingStatus == null) return
//        else if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
//            if (progressDialog.isShowing) {
//                progressDialog.dismiss()
//            }
//        }
//        if (meetingStatus == MeetingStatus.MEETING_STATUS_FAILED && errorCode == MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE) {
//            progressDialog.dismiss()
//            showErroDialogForSdkFailure(videoCallViewModel.appointmentId ?: "", getString(R.string.error_occurred_try_again))
//        }
//        if (meetingStatus == MeetingStatus.MEETING_STATUS_IDLE || meetingStatus == MeetingStatus.MEETING_STATUS_FAILED) {
//
//        }
//        if (meetingStatus == MeetingStatus.MEETING_STATUS_DISCONNECTING) {
//            Log.d(TAG, "meeting disconnecting zoom callback")
//            backPressed()
//        }
//
//    }
//
//    override fun onMeetingParameterNotification(p0: MeetingParameter?) {}

//    fun checkAndStartOrJoinMeeting() {
//
//        val zoomSDK = ZoomSDK.getInstance()
//        if (zoomSDK.isInitialized) {
//            if (videoCallViewModel.isJoinMeeting) {
//                joinMeetingWithNumber()
//            } else {
//                startOrJoinMeeting()
//            }
//        } else {
//            initZoomSdk()
//        }
//
//    }

//    fun startOrJoinMeeting() {
//
//        val userId = videoCallViewModel.zoomCredentials?.userId
//        val zoomToken = videoCallViewModel.zoomCredentials?.zoomToken
//        val meetingId = videoCallViewModel.zoomCredentials?.meetingId
//        val displayName = videoCallViewModel.zoomCredentials?.displayName
//            ?: authManager.selectedPatientLive.value?.fullName ?: "User"
//        val userType = videoCallViewModel.zoomCredentials?.userType ?: ModelUtil.videoCallAttendee
//        val zak = videoCallViewModel.zoomCredentials?.zak
//
//        videoCallViewModel.finishOnResume = false
//
//        if (userId != null && zoomToken != null && meetingId != null && displayName != null) {
//            val zoomSDK = ZoomSDK.getInstance()
//            if (!zoomSDK.isInitialized) {
//                Toast.makeText(this, R.string.sdk_init_error, Toast.LENGTH_LONG).show()
//                showErroDialogForSdkFailure(videoCallViewModel.appointmentId!!, getString(R.string.error_occurred_try_again))
//                return
//            }
//
//            hideUIElements()
//            val meetingService = zoomSDK.meetingService
//            if (userType == ModelUtil.videoCallHost) {
//                val opts = StartMeetingOptions()
//                opts.setMeetingParams()
//                val params = StartMeetingParamsWithoutLogin()
////                params.userId = userId
//                params.meetingNo = meetingId
//                params.displayName = displayName
//                params.userType = MeetingService.USER_TYPE_API_USER
//                params.zoomAccessToken = zak
//                val ret = meetingService.startMeetingWithParams(this, params, opts)
//                zoomRectStatus(ret)
//                Log.d(TAG, "onClickBtnStartMeeting, ret=$ret")
//            } else {
//                val opts = JoinMeetingOptions()
//                opts.setMeetingParams()
//                val params = JoinMeetingParams()
//                params.displayName = displayName
//                params.meetingNo = meetingId
////                params.password = meetingPassword
//                val ret = meetingService.joinMeetingWithParams(this, params, opts)
//                zoomRectStatus(ret)
//                Log.d(TAG, "onClickBtnStartMeeting, ret=$ret")
//            }
//            progressDialog.dismiss()
//        }
//    }

//    fun hideUIElements() {
//        val zoomSDK = ZoomSDK.getInstance()
//        if (zoomSDK.isInitialized) {
//            val zoomUIService = zoomSDK.zoomUIService
//            zoomUIService.hideReactionsOnMeetingUI(true)
//            zoomUIService.enableJavaScriptForShareWebView(false)
//        }
//    }
//
//    fun joinMeetingWithNumber() {
//        val zoomSDK = ZoomSDK.getInstance()
//        videoCallViewModel.finishOnResume = false
//        if (!zoomSDK.isInitialized) {
//            Toast.makeText(this, R.string.sdk_init_error, Toast.LENGTH_LONG).show()
//            showErroDialogForSdkFailure("", getString(R.string.error_occurred_try_again))
//            return
//        }
//        val meetingService = zoomSDK.meetingService
//        val opts = JoinMeetingOptions()
//        opts.setMeetingParams()
//        val params = JoinMeetingParams()
//        params.meetingNo = binding.videoCallMeetingIdET.text.toString().split(" ").joinToString(separator = "")
//        params.displayName = binding.videoCallMeetingNameET.text.toString()
//        //   params.password = videoCallMeetingPwdET.text.toString()
//        val ret = meetingService.joinMeetingWithParams(this, params, opts)
//        zoomRectStatus(ret)
//    }

//    fun initZoomSdk(appointmentId: String = "") {
//        val zoomSDK = ZoomSDK.getInstance()
//        val webDomain = videoCallViewModel.zoomCredentials?.webDomain
//        val sdkJwt = videoCallViewModel.zoomCredentials?.sdkJwt ?: ""
//        if (sdkJwt.isNotBlank() && webDomain != null) {
//            val params = ZoomSDKInitParams()
//            params.jwtToken = sdkJwt
//            params.domain = webDomain
//            params.autoRetryVerifyApp = true
//            params.enableLog = false
//            zoomSDK.initialize(this, this, params)
//        } else {
//            progressDialog.dismiss()
//            showErrorDialogForVideoCallCredentials(appointmentId, getString(R.string.something_went_wrong))
//        }
//    }

//    private fun registerMeetingServiceListener() {
//        val zoomSDK = ZoomSDK.getInstance()
//        val meetingService = zoomSDK.meetingService
//        meetingService?.addListener(this)
//    }

    //endregion

    //region other methods

    private fun getVideoCallCredentialsForConsultation(appointmentId: String) {

        try {
//            progressDialog.show()
//            progressDialog.setMessage(R.string.connecting)
            videoCallViewModel.getVideoCallCredentialsForConsultation(appointmentId)
                .observe(this, Observer {
                    if (it != null) {
                        if (it is TeleHealthCredentials) {
                            videoCallViewModel.teleHealthCredentials = it
                            val isConsentFormAvailable = it.isConsentFormAvailable
                            val consentFormIds = it.consentFormIds
                            if (isConsentFormAvailable && consentFormIds.isNotEmpty()) {
                                val questionnaireIntent = Intent(this, QuestionnaireActivity::class.java)
                                questionnaireIntent.putExtra(ModelUtil.appointmentIdIntent, videoCallViewModel.appointmentId)
                                questionnaireIntent.putStringArrayListExtra(ModelUtil.consentFormIdsIntent, consentFormIds)
                                questionnaireResultLauncher.launch(questionnaireIntent)
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                return@Observer
                            }
                            proceedWithVideoCall(it)
                        } else {
                            progressDialog.dismiss()
                            showErrorDialogForVideoCallCredentials(
                                appointmentId,
                                getString(R.string.request_failed_try_again)
                            )
                        }
                    }
                })
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }

    }

    private fun proceedWithVideoCall(teleHealthCredentials: TeleHealthCredentials) {
        when (val credentials = teleHealthCredentials.credentials) {
            is ZoomCredentials -> {
                videoCallViewModel.zoomCredentials = credentials
                val displayName = videoCallViewModel.zoomCredentials?.displayName
                    ?: authManager.selectedPatientLive.value?.fullName ?: "User"
                val color = ContextCompat.getColor(this, R.color.toolbarBg)
                val internalIntent = Intent(applicationContext, CharmVideoCallActivity::class.java)
                internalIntent.putExtra(CvlModelUtil.cvlWebDomain, videoCallViewModel.zoomCredentials?.webDomain)
                internalIntent.putExtra(CvlModelUtil.cvlMeetingNumIntent, videoCallViewModel.zoomCredentials?.meetingId)
                internalIntent.putExtra(CvlModelUtil.cvlUserIdIntent, videoCallViewModel.zoomCredentials?.userId)
                internalIntent.putExtra(CvlModelUtil.cvlUserTypeIntent, videoCallViewModel.zoomCredentials?.userType ?: ModelUtil.videoCallAttendee)
                internalIntent.putExtra(CvlModelUtil.cvlUserDisplayName, displayName)
                internalIntent.putExtra(CvlModelUtil.cvlThemeColor, color)
                internalIntent.putExtra(CvlModelUtil.cvlZakIntent, videoCallViewModel.zoomCredentials?.zak)
                internalIntent.putExtra(CvlModelUtil.cvlSdkJwt, videoCallViewModel.zoomCredentials?.sdkJwt)
                startActivity(internalIntent)
                overridePendingTransition(0, 0)
            }
            is OpenTokCredentials -> {
                opentokWorkFlow(credentials)
            }
        }
    }

    private fun opentokWorkFlow(it: OpenTokCredentials) {
        videoCallViewModel.openTokCredentials = it
        if (it.password.isNullOrBlank()) {
            intentJoinMeetingViaBrowser()
        } else {
            setContent {
                MPHRTheme {
                    ShowOpenTokMeetingDialog(it.password ?: "")
                }
            }
        }
    }

    private fun intentJoinMeetingViaBrowser() {
        val joinURL = videoCallViewModel.openTokCredentials?.joinUrl ?: ""
        UIUtil.openUrlInChromeInAppStack(this, joinURL)
        progressDialog.dismiss()
        finish()
    }

    @Composable
    private fun ShowOpenTokMeetingDialog(passcodeString: String) {
        var showDialog by remember { mutableStateOf(true) }
        if (showDialog) {

            OpenTokMeetingDialog(
                passcode = passcodeString,
                onCopyAndJoinClicked = {

                    val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("password", passcodeString)
                    clipboardManager.setPrimaryClip(clip)
                    showDialog = false
                    intentJoinMeetingViaBrowser()
                },
                onDismiss = {
                    showDialog = false
                    progressDialog.dismiss()
                    finish()
                }
            )
        }
    }

//    fun MeetingOptions.setMeetingParams() {
//        this.no_driving_mode = true
//        this.no_meeting_end_message = true;
//        this.no_invite = true
//        this.no_share = false
//        this.no_disconnect_audio = true
//        this.no_dial_in_via_phone = true
//        this.no_dial_out_to_phone = true
//        this.no_bottom_toolbar = false
//        this.no_titlebar = false
//
//    }

    fun zoomRectStatus(statusCode: Int) {
        when (statusCode) {
            0 -> { //success do nothing
            }

            99 -> { //error
                showErrorDialogForVideoCallCredentials(
                    videoCallViewModel.appointmentId,
                    getString(R.string.error_occurred_try_again)
                )
            }

            100 -> { // error URL encode displayName failed
                showErrorDialogForVideoCallCredentials(
                    videoCallViewModel.appointmentId,
                    getString(R.string.error_occurred_try_again)
                )
            }

            101 -> { //joining already started meeting - do nothing
            }
        }
    }

    private fun onFinish() {
//        MeetingWindowHelper.getInstance().hiddenMeetingWindow(true);
        /*
                val intent = Intent(this, UsefulInfoDetailActivity::class.java);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
                try {
                    pendingIntent.send(getApplicationContext(), 0, null, null, null, null, null);
                } catch (e: PendingIntent.CanceledException) {
                    Log.d("PendingIntent", e.toString());//No i18N
                }
                finishAffinity()*/
    }

    private fun backPressed() {

        super.onBackPressed()
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        /* if (videoCallViewModel.isJoinMeeting) {
             super.onBackPressed()
             overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down)
         } else {
             super.onBackPressed()
             overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
         }*/
    }

    private fun showErrorDialogForVideoCallCredentials(appointmentId: String?, msg: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogListener = DialogInterface.OnClickListener { _, buttonType ->
            when (buttonType) {
                DialogInterface.BUTTON_POSITIVE -> {
                    getVideoCallCredentialsForConsultation(appointmentId ?: "")
//                    if (appointmentId.isNullOrBlank()) {
//                        if (ZoomSDK.getInstance().isInitialized) {
//                            checkAndStartOrJoinMeeting()
//                        } else {
//                            progressDialog.show()
//                            progressDialog.setMessage(R.string.connecting)
//                            initZoomSdk()
//                        }
//                    } else {
//                        getVideoCallCredentialsForConsultation(appointmentId)
//                    }
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                    Log.d(TAG, "alert cancel")
                    backPressed()
                }
            }
        }
        dialogBuilder.setMessage(msg)
        dialogBuilder.setPositiveButton(R.string.retry_str, dialogListener)
        dialogBuilder.setNegativeButton(R.string.cancel_str, dialogListener)
        dialogBuilder.setCancelable(false)
        dialogBuilder.show()
    }

//    fun showErroDialogForSdkFailure(appointmentId: String, msg: String) {
//        val dialogBuilder = AlertDialog.Builder(this)
//        val dialogListener = DialogInterface.OnClickListener { _, buttonType ->
//            when (buttonType) {
//                DialogInterface.BUTTON_POSITIVE -> {
//                    initZoomSdk(appointmentId)
//                }
//
//                DialogInterface.BUTTON_NEGATIVE -> {
//                    Log.d(TAG, "alert")
//                    backPressed()
//                }
//            }
//        }
//        dialogBuilder.setMessage(msg)
//        dialogBuilder.setPositiveButton(R.string.retry_str, dialogListener)
//        dialogBuilder.setNegativeButton(R.string.cancel_str, dialogListener)
//        dialogBuilder.setCancelable(false)
//        dialogBuilder.show()
//    }

    //endregion

}

