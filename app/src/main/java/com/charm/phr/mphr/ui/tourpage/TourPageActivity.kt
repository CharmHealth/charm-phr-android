package com.charm.phr.mphr.ui.tourpage

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityTourPageBinding
import com.charm.phr.mphr.databinding.LayoutTourPageItemBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.MenuList
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.service.appointmentReminder.AppointmentReminderJobService
import com.charm.phr.mphr.ui.localauth.AppLockTypeActivity
import com.charm.phr.mphr.ui.login.LoginActivity
import com.charm.phr.mphr.ui.passcode.PasscodeActivity
import com.charm.phr.mphr.ui.swaasa.SwaasaActivity
import com.charm.phr.mphr.ui.videocall.VideoCallActivity
import com.charm.phr.mphr.ui.videocall.opentok.OpenTokVideoCallActivity
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.KeyStoreUtil
import com.charm.phr.mphr.util.MDAccountsRelatedUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.SecurityUtil
import com.charm.phr.mphr.util.SecurityUtil.isLocalAuthConfigured
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.getString
import com.zoho.apptics.logger.AppticsLogger
import com.charm.phr.mphr.util.AuthenticationUtil
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.lang.reflect.InvocationTargetException
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TourPageActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreferences

    @Inject
    lateinit var tourPageAdapter: TourPageAdapter

    private lateinit var tourPageViewModel: TourPageViewModel

    private lateinit var binding: ActivityTourPageBinding

    private var isUserSuccessfullyLoggedIn = false

    var timer: Timer? = null

    val TAG = "TourPageActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTourPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UIUtil.applyViewInsertForBottomView(binding.tourPageBtnContainer)

        val toSwitch =
            intent.getBooleanExtra(com.charm.phr.mphr.zoom.ui.ModelUtil.zmSwitchScreen, false)

        Log.d("MDLogout", "tourpage $toSwitch")

        if (toSwitch) {
            finish()
            return
        }

        tourPageViewModel =
            ViewModelProvider(this, viewModelFactory).get(TourPageViewModel::class.java)

//        checkAndDeleteCorruptedDB() TODO: Remove in next release
        checkAndDeleteOldDb()

//        isPassCodeAllowed = false
//
//        val intent = Intent(this, SupportActivity::class.java)
//        startActivity(intent)
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//

        isUserSuccessfullyLoggedIn =
            sharedPreference.getBoolean(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN, false)


        binding.tourViewPager.adapter = tourPageAdapter

        binding.tourPageTabLayout.setViewPager(binding.tourViewPager)

        if (BuildUtil.isThalassemia) {
            binding.tourViewPager.setCurrentItem(0, false)
            binding.tourViewPager.setOnTouchListener { _, _ -> true }
            binding.tourPageTabLayout.visibility = View.GONE
        }

        binding.tourViewPager.setPageTransformer(false) { view, position ->
            val itemBinding = LayoutTourPageItemBinding.bind(view)
            // Get page index from tag
            val pageWidth = itemBinding.root.getWidth()
            val pageWidthTimesPosition = pageWidth * position

            if (position <= -1.0f || position >= 1.0f) {
                // Page is not visible -- stop any running animations
            } else if (position == 0.0f) {
                // Page is selected -- reset any views if necessary
            } else {
                // Page is currently being swiped -- perform animations here
                val profile = itemBinding.tpItemImg
                profile?.translationX = pageWidthTimesPosition * 0.9f
                val card = itemBinding.tp1TextViewContainer
                card?.translationX = pageWidthTimesPosition * 0.2f
            }
        }

        binding.tourPageSignInBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

//        binding.buildIndicatorTv.setOnClickListener {
//            PermissionUtil.requestCameraPermission(this, ::permissionAcceptCallBack, requestPermissionLauncher)
//        }

        binding.tourPageJoinBtn.visibility = View.GONE

        if (BuildUtil.isCustomApp) {
            val window = this.window
            window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window?.setStatusBarColor(
                ContextCompat.getColor(
                    this.applicationContext,
                    R.color.translucent
                )
            )
            window?.statusBarColor = ContextCompat.getColor(this, R.color.colorTourpage)
        } else if (BuildUtil.isVideoCallEnabled) {
            binding.tourPageJoinBtn.setOnClickListener {
                val intent = Intent(this, VideoCallActivity::class.java)
                intent.putExtra(ModelUtil.videoJoinMeetingIntent, true)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_up, android.R.anim.fade_out)
            }
        }

        if (isUserSuccessfullyLoggedIn && BuildUtil.isOAuthEnabled) {
            // Wait for the stored sign-in to load the stored session (local storage, quick) before going on.
            lifecycleScope.launch {
                if (!AuthenticationUtil.awaitSignedIn()) {
                    Log.d(
                        "MDLogout",
                        "Auth state conflict: app logged in but CharmAuth not signed in. Logging out."
                    )
                    showSessionExpiredDialog()
                } else {
                    continueOnCreate()
                }
            }
            return
        }
        continueOnCreate()
    }

    private fun continueOnCreate() {
        val prevVersion = sharedPreference.getBoolean(ModelUtil.prefIsFirstEntry, true)

        if (prevVersion && !BuildUtil.isCustomApp) {

            sharedPreference.edit().putBoolean(ModelUtil.prefIsFirstEntry, false).commit()

            binding.tpProgressView.visibility = View.VISIBLE
            val referrerClient = InstallReferrerClient.newBuilder(this).build()
            referrerClient.startConnection(object : InstallReferrerStateListener {

                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            try {
                                // Connection established.
                                val referrer = referrerClient.installReferrer.installReferrer


                                Log.d("InstallReferrerDebug", "install referrer -> $referrer")
                                if (!referrer.isNullOrBlank()) {
                                    parseFromReferrer(referrer)
                                } else {
                                    afterOnCreateCallback()
                                }
                                referrerClient.endConnection()
                            } catch (ex: Exception) {
                                Log.e(TAG, ex.toString())
                            } catch (ex: InvocationTargetException) {
                                Log.e(TAG, ex.cause?.printStackTrace().toString())
                            }
                        }

                        InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                            // API not available on the current Play Store app.
                            afterOnCreateCallback()
                            Log.d("InstallReferrerDebug", "feature not supported")
                        }

                        InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                            // Connection couldn't be established.
                            afterOnCreateCallback()
                            Log.d("InstallReferrerDebug", "service unavailable")
                        }
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                    Log.d("InstallReferrerDebug", "disconnected")
                    afterOnCreateCallback()
                }
            })

        } else {

            afterOnCreateCallback()
        }
    }

    fun checkAndDeleteOldDb() {

        val oldDb = ModelUtil.mPHRDBName
        val deprecatedDb = ModelUtil.charmMPHRDb
        try {
            val oldDbFile = applicationContext.getDatabasePath(oldDb)
            if (oldDbFile.exists()) {
                applicationContext.deleteDatabase(oldDb)
            }
            val deprecatedDbFile = applicationContext.getDatabasePath(deprecatedDb)
            if (deprecatedDbFile.exists()) {
                applicationContext.deleteDatabase(deprecatedDb)
            }
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }

    }

    private fun checkAndDeleteCorruptedDB() {
        val isDbDeleted = sharedPreference.getBoolean(ModelUtil.IS_CORRUPTED_DB_DELETED, false)
        if (!isDbDeleted) {
            try {
                tourPageViewModel.getAllFrequencyAlarmMap()
            } catch (e: Exception) {
                if (!BuildUtil.isCustomApp) AppticsLogger.e(TAG, e.stackTraceToString())
                this.applicationContext.deleteDatabase(ModelUtil.charmMPHRDBName)
            }
            val prefEdit = sharedPreference.edit()
            prefEdit.putBoolean(ModelUtil.IS_CORRUPTED_DB_DELETED, true)
            prefEdit.commit()
        }
    }

    private fun parseFromReferrer(jsonStr: String) {

        try {
            val jsonObj = jsonStr.parseStringAsJsonObject()
            Log.d("InstallReferrerDebug", "$jsonObj")
            if (jsonObj != null) {
                val type = jsonObj.getString("TYPE")
                if (type.isNullOrBlank() || type == "ZOOM") {
                    val meetingId = jsonObj.getString("ID")
                    if (!meetingId.isNullOrBlank() && BuildUtil.isVideoCallEnabled) {
                        val intent = Intent(this, VideoCallActivity::class.java)
                        intent.putExtra(ModelUtil.videoMeetingIdIntent, meetingId)
                        intent.putExtra(ModelUtil.videoMeetingNameIntent, jsonObj.getString("NAME"))
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val swaasaId = jsonObj.getString("ID")
                    if (!swaasaId.isNullOrBlank()) {
                        intentToSwaasaActivity(swaasaId)
                    }
                }
            } else {
                afterOnCreateCallback()
            }
        } catch (e: Exception) {
            afterOnCreateCallback()
            Log.d("InstallReferrerDebug", "$e")
        }
    }

    private fun intentToOpentokActivity() {

        if (!BuildUtil.isVideoCallEnabled) {
            return
        }

        val intent = Intent(this, OpenTokVideoCallActivity::class.java)
        intent.putExtra(ModelUtil.otAPIKeyIntent, "")
        intent.putExtra(ModelUtil.otSessionIdIntent, "")
        intent.putExtra(ModelUtil.otTokenIntent, "")
        startActivity(intent)
    }

    private fun afterOnCreateCallback() {

        binding.tpProgressView.visibility = View.GONE
        binding.tpFailureLayout.visibility = View.GONE

        val uri = intent.data

        var meetingId = ""
        var userName = ""
        var swaasaId = ""

        if (uri != null) {
            Log.d("MDVideoCall", uri.host ?: "")
            Log.d("MDVideoCall", "${uri.queryParameterNames?.joinToString(", ")}")

            if (uri.host == MDAccountsRelatedUtil.hostName) {

                if (uri.path?.contains("/mphr/video.sas") == true) {
                    meetingId = uri.getQueryParameter("ID") ?: ""
                    userName = uri.getQueryParameter("NAME") ?: ""
                } else if (uri.path?.contains("swaasa.sas") == true) {
                    swaasaId = uri.getQueryParameter("ID") ?: ""
                }
            }
        }

        if (meetingId.isNotBlank() && BuildUtil.isVideoCallEnabled) {
            val intent = Intent(this, VideoCallActivity::class.java)
            intent.putExtra(ModelUtil.videoMeetingIdIntent, meetingId)
            intent.putExtra(ModelUtil.videoMeetingNameIntent, userName)
            startActivity(intent)
            finish()
        } else {

            if (isUserSuccessfullyLoggedIn) {

                authManager.authToken = KeyStoreUtil.getAuthToken(applicationContext)
                if (authManager.authToken.isNullOrBlank() || !BuildUtil.isOAuthEnabled) {

                    if (swaasaId.isNotBlank()) {
                        intentToMainPage(swaasaId)
                    } else {
                        onStartAlreadyLogin()
                    }
                } else {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                if (swaasaId.isNotBlank()) {
                    intentToSwaasaActivity(swaasaId)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setAutoScrollToPager()
        if (!tourPageViewModel.isFirstTimePageLoaded && isEnhanceNeeded()) {
            finish()
        }
        tourPageViewModel.isFirstTimePageLoaded = false
    }

    override fun onPause() {
        super.onPause()
        checkAndCancelTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        checkAndCancelTimer()
    }

    private fun onStartAlreadyLogin() {
        authManager.updateAuthStatus()

        binding.tpProgressView.visibility = View.VISIBLE
        binding.tpFailureLayout.visibility = View.GONE

        fetchPatients()

        if (BuildUtil.isCustomApp) {
            fetchMenuList()
        } else {
            authManager.menuList = MenuList()
            tourPageViewModel.isMenuListResponseReceived = true
            tourPageViewModel.isMenuListFetchSuccess = true

        }
    }


    private fun fetchPatients() {
        tourPageViewModel.fetchPatients().observe(this) {
            if (it != null) {
                tourPageViewModel.isPatientsResponseReceived = true

                if (it is ArrayList<*>) {
                    tourPageViewModel.isPatientsFetchSuccess = true
                    authManager.patientList = it as ArrayList<Patient>

                    if (!sharedPreference.getBoolean(
                            ModelUtil.isAppointmentReminderPeriodic,
                            false
                        )
                    ) {
                        AppointmentReminderJobService.scheduleAppointmentReminderJobService(
                            this,
                            false
                        )
                    }

                    checkWhetherAllDataIsReceived()

                } else if (it is APIResponseError) {

                    if (!tourPageViewModel.isErrorDialogDisplayed) {
                        dialogForRequestFailure(ModelUtil.patientRequest, it.charmApiCode ?: 0)
                    }

                    Log.d("patientListFail", it.errorMsg ?: "")
                }

            }
        }
    }

    private fun fetchMenuList() {
        tourPageViewModel.fetchMenuList().observe(this) {
            if (it != null) {
                tourPageViewModel.isMenuListResponseReceived = true

                if (it is MenuList) {
                    tourPageViewModel.isMenuListFetchSuccess = true
                    authManager.menuList = it

                    checkWhetherAllDataIsReceived()

                } else if (it is APIResponseError) {

                    if (!tourPageViewModel.isErrorDialogDisplayed) {
                        dialogForRequestFailure(ModelUtil.menuListRequest, it.charmApiCode ?: 0)
                    }

                    Log.d("patientListFail", it.errorMsg ?: "")
                }

            }
        }
    }

    private fun dialogForRequestFailure(type: String, charmAPICode: Int) {

        tourPageViewModel.isErrorDialogDisplayed = true
        progressDialog.dismiss()

        authManager.updateAuthStatus()

        if (type == ModelUtil.patientEmptyRequest || charmAPICode == ModelUtil.patientNotAvailableForPracticeCode) {
            binding.tpFailureLayout.visibility = View.VISIBLE
            binding.tpRetryBtn.visibility = View.GONE
            binding.tpProgressView.visibility = View.GONE
            binding.tpNotSubscribedImg.visibility = View.VISIBLE
            binding.tpErrorMsgTV.text =
                getString(R.string.error_no_patients_found, getString(R.string.app_name))

            handler.postDelayed({
                authManager.logout()

                val intent = if (BuildUtil.isCustomApp) {
                    Intent(applicationContext, LoginActivity::class.java)
                } else {
                    Intent(applicationContext, TourPageActivity::class.java)
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }, 5000)
        } else {
            if (type == ModelUtil.menuListRequest && tourPageViewModel.isPatientsFetchSuccess && authManager.patientList.isEmpty() && BuildUtil.isCustomApp) {
                dialogForRequestFailure(ModelUtil.patientEmptyRequest, charmAPICode)
            } else {

                binding.tpError.root.visibility = View.VISIBLE
                binding.tpError.errorPageTryAgainBtn.setOnClickListener {
                    tourPageViewModel.isErrorDialogDisplayed = false
                    binding.tpError.root.visibility = View.GONE
                    progressDialog.setMessage(R.string.loading_data)
                    progressDialog.show()
                    when (type) {
                        ModelUtil.patientRequest -> {
                            fetchPatients()
                        }

                        ModelUtil.menuListRequest -> {
                            fetchMenuList()
                        }
                    }
                }
            }
        }


    }

    fun checkWhetherAllDataIsReceived() {
        if (tourPageViewModel.isPatientsResponseReceived && tourPageViewModel.isMenuListResponseReceived) {
            if (!tourPageViewModel.isPatientsFetchSuccess) {
                tourPageViewModel.isPatientsResponseReceived = false
                dialogForRequestFailure(ModelUtil.patientRequest, 0)
            } else if (!tourPageViewModel.isMenuListFetchSuccess) {
                tourPageViewModel.isMenuListResponseReceived = false
                dialogForRequestFailure(ModelUtil.menuListRequest, 0)
            } else {
                progressDialog.dismiss()
                if (authManager.patientList.isEmpty()) {
                    dialogForRequestFailure(ModelUtil.patientEmptyRequest, 0)
                } else {
                    enhanceOAuthScope()
                }
            }
        }
    }

    private fun intentToMainPage(swaasaId: String? = null) {

        val prefEdit = sharedPreference.edit()
        prefEdit.putBoolean(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN, true)
        prefEdit.putString(
            ModelUtil.patientList,
            MDJsonBuilder.formPatientList(authManager.patientList)
        )
        prefEdit.putString(ModelUtil.menuList, MDJsonBuilder.formMenuList(authManager.menuList))
        prefEdit.commit()

        handler.postDelayed({
            if (isLocalAuthConfigured() && sharedPreference.getString(ModelUtil.applockType, "")
                    .isNullOrBlank() && sharedPreference.getString(ModelUtil.PASSCODE, "")
                    .isNullOrBlank()
            ) {
                val intent = Intent(this, AppLockTypeActivity::class.java)
                intent.putExtra(ModelUtil.isFirst, true)
                intent.putExtra(ModelUtil.isFirstTimeForAppLock, true)
                startActivity(intent)
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            } else if (!isLocalAuthConfigured() && sharedPreference.getString(
                    ModelUtil.applockType,
                    ""
                ).equals(SecurityUtil.LockType.SystemLock.toString())
            ) {
                UIUtil.showMdAlertDialog(
                    this,
                    R.string.logout_error_msg,
                    false,
                    R.string.log_out,
                    R.string.cancel_str
                ) { response ->
                    if (response) {
                        logoutAndShowLoginPage()
                    } else {
                        finish()
                    }
                }
            } else {
                val intent = Intent(this, PasscodeActivity::class.java)
                intent.putExtra(ModelUtil.isFirst, true)
                if (sharedPreference.getString(ModelUtil.applockType, "")
                        .equals(SecurityUtil.LockType.SystemLock.toString()) || !sharedPreference.getString(
                        ModelUtil.PASSCODE, ""
                    ).isNullOrBlank()
                ) {
                    intent.putExtra(ModelUtil.passCodeType, PasscodeActivity.checkPasscode)
                } else {
                    intent.putExtra(ModelUtil.applockType, SecurityUtil.LockType.PinLock.toString())
                    intent.putExtra(ModelUtil.passCodeType, PasscodeActivity.setPasscode)
                }
                intent.putExtra(ModelUtil.swaasaIdIntent, swaasaId)
                startActivity(intent)
                finish()
                if (sharedPreference.getString(ModelUtil.PASSCODE, "").isNullOrBlank()) {
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        }, 200)
    }

    private fun intentToSwaasaActivity(swaasaId: String) {

        val intent = Intent(this, SwaasaActivity::class.java)
        intent.putExtra(ModelUtil.swaasaIdIntent, swaasaId)
        intent.putExtra(ModelUtil.swaasaShowOtpIntent, true)
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    //region otherMethods

    fun setAutoScrollToPager() {

        if (BuildUtil.isThalassemia) {
            binding.tourViewPager.setCurrentItem(0, false)
            return
        }

        timer = Timer()
        tourPageViewModel.currentPage = 0

        val update = Runnable {
            try {
                if (tourPageViewModel.currentPage == tourPageAdapter.tourList.size) {
                    tourPageViewModel.currentPage = 0
                }
                binding.tourViewPager?.setScrollDurationFactor(6.0) // makes the animation 8 times slow
                binding.tourViewPager?.setCurrentItem(tourPageViewModel.currentPage++, true)
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
            }
        }

        timer?.schedule(object : TimerTask() {
            override fun run() {
                handler.post(update)
            }
        }, 0, TimeUnit.SECONDS.toMillis(5))

    }

    fun checkAndCancelTimer() {
        if (timer != null) {
            timer?.cancel()
            binding.tourViewPager?.setScrollDurationFactor(2.0) // makes the animation 2 times slow
            timer = null
        }
    }

    fun isEnhanceNeeded(): Boolean {
        if (BuildUtil.isCustomApp || BuildUtil.isThalassemia) return false
        return false
    }

    private fun enhanceOAuthScope() {
        // Scopes are fixed in the CharmAuth configuration; nothing to enhance.
        intentToMainPage()
    }

    fun logoutAndShowLoginPage() {
        authManager.logout()
        handler.post {
            binding.tpProgressView.visibility = View.GONE
            binding.tourPageSignInBtn.callOnClick()
        }
    }

    private fun showSessionExpiredDialog() {
        // Keep tour page UI visible in the background (no progress / no error layout)
        binding.tpProgressView.visibility = View.GONE
        binding.tpFailureLayout.visibility = View.GONE

        val dialog = AlertDialog.Builder(this)
            .setMessage(R.string.session_expired_login_again)
            .setCancelable(false)
            .setPositiveButton(R.string.ok_str) { d, _ ->
                d.dismiss()
                logoutAndShowLoginPage()
            }
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

}