package charmzoom

import android.R
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.charm.videocall.databinding.LayoutCvlProgressBarBinding
import us.zoom.sdk.JoinMeetingOptions
import us.zoom.sdk.JoinMeetingParams
import us.zoom.sdk.MeetingOptions
import us.zoom.sdk.MeetingParameter
import us.zoom.sdk.MeetingService
import us.zoom.sdk.MeetingServiceListener
import us.zoom.sdk.MeetingStatus
import us.zoom.sdk.StartMeetingOptions
import us.zoom.sdk.StartMeetingParamsWithoutLogin
import us.zoom.sdk.ZoomError
import us.zoom.sdk.ZoomSDK
import us.zoom.sdk.ZoomSDKInitParams
import us.zoom.sdk.ZoomSDKInitializeListener


class CharmVideoCallActivity : AppCompatActivity(), MeetingServiceListener, ZoomSDKInitializeListener {

    val TAG = "CharmVideoCallActivity"

    lateinit var binding: LayoutCvlProgressBarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutCvlProgressBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        setToolBarInsert()
        setBottomBarInsert()

        val themeColor = intent.getIntExtra(CvlModelUtil.cvlThemeColor, Color.BLUE)
        binding.zmMeetingWaitingRoomToolbar.setBackgroundColor(themeColor)
        initZoomSdk()


    }

    private fun setBottomBarInsert() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.zmMeetingConnectingLayout) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemBars()
            )
            val view = binding.zmMeetingConnectingLayout
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom + bars.bottom)

            insets
        }
        ViewCompat.requestApplyInsets(binding.zmMeetingConnectingLayout)
    }

    private fun setToolBarInsert() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.zmMeetingWaitingRoomToolbar) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemBars()
            )
            val view = binding.zmMeetingWaitingRoomToolbar

            val typedArray = view.context.theme.obtainStyledAttributes(intArrayOf(R.attr.actionBarSize))
            val actionBarSize = typedArray.getDimensionPixelSize(0, 0)
            typedArray.recycle()

            view.layoutParams.height = actionBarSize + bars.top
            view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.paddingBottom)

            insets
        }
        ViewCompat.requestApplyInsets(binding.zmMeetingWaitingRoomToolbar)
    }

    fun initZoomSdk() {

        val zoomSDK = ZoomSDK.getInstance()
        Log.d(TAG, "Zoom SDK Version : ${zoomSDK.getVersion(this)}")
        val webDomain = intent.getStringExtra(CvlModelUtil.cvlWebDomain)
        val cvlJWT = intent.getStringExtra(CvlModelUtil.cvlSdkJwt)

        val params = ZoomSDKInitParams()
        params.domain = webDomain
        params.jwtToken = cvlJWT
//        params.autoRetryVerifyApp = true
//        params.enableLog = true
        zoomSDK.initialize(this, this, params)

    }

    override fun onMeetingStatusChanged(meetingStatus: MeetingStatus?, errorCode: Int, internalErrorCode: Int) {

        Log.d(TAG, "meetingStatus $meetingStatus errorCode $errorCode")

        if (meetingStatus == null) return
//        if (meetingStatus == MeetingStatus.MEETING_STATUS_FAILED && errorCode == MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE) {
////            showErroDialogForSdkFailure(videoCallViewModel.consultationId, getString(R.string.errorOccurredTryAgain))
//        }

        if (meetingStatus == MeetingStatus.MEETING_STATUS_FAILED) {
            startOrJoinMeeting()
        }
        /* if (meetingStatus == MeetingStatus.MEETING_STATUS_CONNECTING) {
             showMeetingUI()
         }*/
        if (meetingStatus == MeetingStatus.MEETING_STATUS_DISCONNECTING) {
            backPressed()
            //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        /* refreshUI()*/

    }

    private fun backPressed() {
        super.onBackPressed()
        ZoomSDK.getInstance().inMeetingService.leaveCurrentMeeting(true)
        finish()
    }

    override fun onDestroy() {
        val zoomSDK = ZoomSDK.getInstance()
        if (zoomSDK.isInitialized) {
            val meetingService = zoomSDK.meetingService
            meetingService.removeListener(this)
        }
        super.onDestroy()
    }


    override fun onMeetingParameterNotification(p0: MeetingParameter?) {

    }

    override fun onZoomSDKInitializeResult(errorCode: Int, internalErrorCode: Int) {

        Log.d(TAG, "onZoomSDKInitializeResult, errorCode=$errorCode, internalErrorCode=$internalErrorCode")

//        progressDialog.dismiss()

        if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
//            showErrorDialogForVideoCallCredentials(videoCallViewModel.consultationId, getString(R.string.sdkInitError))
        } else {

            registerMeetingServiceListener()

            ZoomSDK.getInstance().zoomUIService.enableMinimizeMeeting(false)
            ZoomSDK.getInstance().zoomUIService.hideMiniMeetingWindow()
            ZoomSDK.getInstance().zoomUIService.disablePIPMode(true)
            ZoomSDK.getInstance().zoomUIService.hideReactionsOnMeetingUI(true)
            ZoomSDK.getInstance().zoomUIService.hideCloudWhiteboardShareButton(true)
            ZoomSDK.getInstance().zoomUIService.hideCloudWhiteboardAboutButton(true)

            ZoomSDK.getInstance().meetingSettingsHelper.isCustomizedMeetingUIEnabled = false
            ZoomSDK.getInstance().meetingSettingsHelper.setClaimHostWithHostKeyActionEnabled(false)
            ZoomSDK.getInstance().meetingSettingsHelper.enable720p(false)
            ZoomSDK.getInstance().meetingSettingsHelper.enableCloudWhiteboard(false)
            ZoomSDK.getInstance().meetingSettingsHelper.hiddenQA(true)


            ZoomSDK.getInstance().meetingSettingsHelper.enableShowMyMeetingElapseTime(true)


            startOrJoinMeeting()
        }

    }

//    private fun startOrJoinMeeting(){
//        //need to remove the hardcoded
////        val meetingId = "96022025893"
////        val userId = "wpMTt4qVQkeKCj_D6Md07A"
////        val zak =
////            "eyJ0eXAiOiJKV1QiLCJzdiI6IjAwMDAwMSIsInptX3NrbSI6InptX28ybSIsImFsZyI6IkhTMjU2In0.eyJhdWQiOiJjbGllbnRzbSIsInVpZCI6IndwTVR0NHFWUWtlS0NqX0Q2TWQwN0EiLCJpc3MiOiJ3ZWIiLCJzayI6IjAiLCJzdHkiOjk5LCJ3Y2QiOiJhdzEiLCJjbHQiOjAsImV4cCI6MTY3OTIwNDU4NSwiaWF0IjoxNjcxNDI4NTg1LCJhaWQiOiJ4ZlQwbjZFX1JVT3E2ckNVUzFhczNnIiwiY2lkIjoiIn0.HY6i7Dgz42m1qT_5Id65qCyeVaOyiac0U5KCHiYQk50"
////        val userType = ModelUtil.cvlUserTypeHost
////        val displayName = "DR.Test"
////        val themeColor = "#4074D9"
////        val startTime = "10:30 AM"
////        val topic = "John"
//
//
//        val meetingId =intent.getStringExtra(charmzoom.CvlModelUtil.cvlMeetingNumIntent)
//        val userId =intent.getStringExtra(charmzoom.CvlModelUtil.cvlUserIdIntent)
//        val zak =intent.getStringExtra(charmzoom.CvlModelUtil.cvlZakIntent)
//        val userType =intent.getStringExtra(charmzoom.CvlModelUtil.cvlUserTypeIntent)
//        val displayName =intent.getStringExtra(charmzoom.CvlModelUtil.cvlUserDisplayName)
//        val themeColor=intent.getStringExtra(charmzoom.CvlModelUtil.cvlThemeColor)
//        val startTime =intent.getStringExtra(charmzoom.CvlModelUtil.cvlStartTime)
//        val topic =intent.getStringExtra(charmzoom.CvlModelUtil.cvlTopic)
//
//        val intent = Intent(this, APIUserStartJoinMeetingActivity::class.java)
//        intent.putExtra(charmzoom.CvlModelUtil.cvlMeetingNumIntent, meetingId)
//        intent.putExtra(charmzoom.CvlModelUtil.cvlUserIdIntent, userId)
//        intent.putExtra(charmzoom.CvlModelUtil.cvlZakIntent, zak)
//        intent.putExtra(charmzoom.CvlModelUtil.cvlUserTypeIntent, userType)
//        intent.putExtra(charmzoom.CvlModelUtil.cvlUserDisplayName, displayName)
//        intent.putExtra(charmzoom.CvlModelUtil.cvlThemeColor, themeColor)
//        intent.putExtra(charmzoom.CvlModelUtil.cvlStartTime, startTime)
//        intent.putExtra(charmzoom.CvlModelUtil.cvlTopic, topic)
//        startActivity(intent)
//    }

    fun startOrJoinMeeting() {

        val zoomSDK = ZoomSDK.getInstance()
        if (!zoomSDK.isInitialized) {
            Toast.makeText(this, "ZoomSDK has not been initialized successfully", Toast.LENGTH_LONG).show()
            return
        }


        val meetingId = intent.getStringExtra(CvlModelUtil.cvlMeetingNumIntent)
        val userId = intent.getStringExtra(CvlModelUtil.cvlUserIdIntent)
        val zak = intent.getStringExtra(CvlModelUtil.cvlZakIntent)
        val userType = intent.getStringExtra(CvlModelUtil.cvlUserTypeIntent) ?: ""
        val displayName = intent.getStringExtra(CvlModelUtil.cvlUserDisplayName)
        val startTime = intent.getStringExtra(CvlModelUtil.cvlStartTime)
        val topic = intent.getStringExtra(CvlModelUtil.cvlTopic)

        val meetingService = zoomSDK.meetingService
        val isHost = (userType == CvlModelUtil.videoCallHost)


        if (isHost) {
            val opts = StartMeetingOptions()
            opts.setMeetingParams()
            val params = StartMeetingParamsWithoutLogin()
//            params.userId = userId
            params.zoomAccessToken = zak
            params.userType = MeetingService.USER_TYPE_API_USER
            params.meetingNo = meetingId
            params.displayName = displayName
            val ret = meetingService.startMeetingWithParams(this, params, opts)
            Log.d(TAG, "onClickBtnStartMeeting, ret=$ret")
        } else {
            val opts = JoinMeetingOptions()
            opts.setMeetingParams()
            val params = JoinMeetingParams()
//            params.userId = userId
//            params.zoomAccessToken = zak
//            params.userType = MeetingService.USER_TYPE_API_USER
            params.meetingNo = meetingId
            params.displayName = displayName
            val ret = meetingService.joinMeetingWithParams(this, params, opts)
            Log.d(TAG, "onClickBtnStartMeeting, ret=$ret")

        }
//        progressDialog.dismiss()

    }

    fun MeetingOptions.setMeetingParams() {
        this.no_driving_mode = true
        this.no_meeting_end_message = true
        this.no_invite = true
        this.no_share = true
        this.no_disconnect_audio = true
        this.no_dial_in_via_phone = true
        this.no_dial_out_to_phone = true
        this.no_bottom_toolbar = false
        this.no_titlebar = false

    }


    override fun onZoomAuthIdentityExpired() {

    }

    private fun registerMeetingServiceListener() {
        val zoomSDK = ZoomSDK.getInstance()
        val meetingService = zoomSDK.meetingService
        meetingService?.addListener(this)
    }


}