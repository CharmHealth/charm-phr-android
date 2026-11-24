package com.charm.phr.mphr.ui.login

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityLoginBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.MenuList
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.database.mPHRDB
import com.charm.phr.mphr.service.appointmentReminder.AppointmentReminderJobService
import com.charm.phr.mphr.service.reminders.FetchReminderDataWorker
import com.charm.phr.mphr.service.reminders.ScheduleReminderPerDayWorker
import com.charm.phr.mphr.ui.localauth.AppLockTypeActivity
import com.charm.phr.mphr.ui.passcode.PasscodeActivity
import com.charm.phr.mphr.ui.tourpage.TourPageActivity
import com.charm.phr.mphr.util.AppticsUtil
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.KeyStoreUtil
import com.charm.phr.mphr.util.MDAccountsRelatedUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MdEvent
import com.charm.phr.mphr.util.MdEventGroup
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.SecurityUtil
import com.charm.phr.mphr.util.SecurityUtil.isLocalAuthConfigured
import com.charm.phr.mphr.util.AuthenticationUtil
import com.charm.phr.mphr.util.AuthenticationUtil.AuthFailure
import com.charm.phr.mphr.util.AuthenticationUtil.SignInResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginActivity : MDBaseActivity() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreferences

    @Inject
    lateinit var db: mPHRDB

    private lateinit var loginViewModel: LoginViewModel

    val TAG = "LoginActivity"

    private var isUserSuccessfullyLoggedIn = false

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)

//        UIUtil.applyViewInsertForFullView(binding.loginWebView)

        loginViewModel.isAuthTokenFailed = intent.getBooleanExtra(ModelUtil.apiTokenFailed, false)

        isUserSuccessfullyLoggedIn =
            sharedPreference.getBoolean(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN, false)

        authManager.questionnaireHPList = null
        when {

            loginViewModel.isAuthTokenFailed -> {
                val builder = AlertDialog.Builder(this)
                if (intent.getBooleanExtra(ModelUtil.isOldAppIntent, false)) {
                    builder.setMessage(R.string.mphr_dialog_session_exceeded)
                } else {
                    builder.setMessage(R.string.deleted_authtoken_string)
                }
                builder.setPositiveButton(R.string.ok_str) { _, _ ->
                    startSignIn()
                }
                builder.show()
            }

            isUserSuccessfullyLoggedIn -> {
                authManager.authToken = KeyStoreUtil.getAuthToken(applicationContext)
                if (authManager.authToken.isNullOrBlank() || !BuildUtil.isOAuthEnabled) {
                    onStartAlreadyLogin()
                } else {
                    binding.loginProgressView.root.visibility = View.GONE
                    binding.loginWebView.visibility = View.GONE
                    binding.loginFailureLayout.visibility = View.VISIBLE
                    binding.loginErrorMsgTV.setText(R.string.auth_logout_msg)
                    binding.loginRetryBtn.visibility = View.GONE
                    authManager.logout()
                    authManager.authToken = null
                    lifecycleScope.launch {
                        delay(5000)
                        val intent = Intent(this@LoginActivity, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                }
            }

            else -> {
                startSignIn()
            }
        }

        binding.loginRetryBtn.setOnClickListener {
            startSignIn()
        }

    }

    private fun onStartAlreadyLogin() {
        authManager.updateAuthStatus()

        if (!progressDialog.isShowing) {
            showMdProgressDialog()
        }
        fetchPatients()
        if (BuildUtil.isCustomApp) {
            fetchMenuList()
        } else {
            authManager.menuList = MenuList()
            loginViewModel.isMenuListResponseReceived = true
            loginViewModel.isMenuListFetchSuccess = true

        }
    }

    private fun startSignIn() {

        binding.loginFailureLayout.visibility = View.GONE
        binding.loginWebView.visibility = View.GONE
        binding.loginProgressView.root.visibility = View.VISIBLE
        lifecycleScope.launch {
            // Opens CHIS's sign-in page in a Custom Tab; suspends until the user finishes.
            when (val result = AuthenticationUtil.signIn(this@LoginActivity)) {
                SignInResult.Success -> {
                    if (!BuildUtil.isCustomApp && !BuildUtil.isThalassemia) {
                        AppticsUtil.addEvent(MdEventGroup.Account, MdEvent.LoginOnSuccess)
                    }
                    loginSuccess()
                }
                SignInResult.Cancelled -> finish()
                SignInResult.AlreadyInProgress -> Unit // a sign-in tab is already open
                is SignInResult.Failed -> showSignInFailedDialog(result)
            }
        }
    }

    private fun showSignInFailedDialog(result: SignInResult.Failed) {
        if (isFinishing || isDestroyed) return
        binding.loginProgressView.root.visibility = View.GONE
        val message = when (result.failure) {
            AuthFailure.ACCOUNT_BLOCKED -> getString(R.string.charm_auth_account_cannot_sign_in)
            AuthFailure.NETWORK -> getString(R.string.io_exception)
            // Build settings missing or wrong: the message names each one (development/QA builds).
            AuthFailure.MISCONFIGURED -> result.message ?: getString(R.string.something_went_wrong)
            else -> getString(R.string.something_went_wrong)
        }
        val dialog = AlertDialog.Builder(this@LoginActivity)
            .setMessage(message)
            .setPositiveButton(R.string.ok_str) { dia, _ ->
                dia.dismiss()
                // Back to the welcome (tour) page; its Login button starts sign-in again.
                backToTourPage()
            }
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun backToTourPage() {
        if (isTaskRoot) {
            // Opened on its own (e.g. after a logout cleared the task): start the tour page.
            val intent = Intent(this, TourPageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun loginSuccess() {

        try {
            db.clearAllTables()
        } catch (ex: Exception) {
            Log.e(TAG, ex.printStackTrace().toString())
        }

        authManager.updateAuthStatus()
        showMdProgressDialog()

        fetchPatients(true)
        if (BuildUtil.isCustomApp) {
            fetchMenuList()
        } else {
            authManager.menuList = MenuList()
            loginViewModel.isMenuListResponseReceived = true
            loginViewModel.isMenuListFetchSuccess = true

        }
    }


    private fun fetchPatients(isAfterLogin: Boolean = false) {
        loginViewModel.fetchPatients().observe(this) {
            if (it != null) {
                loginViewModel.isPatientsResponseReceived = true

                if (it is ArrayList<*>) {
                    loginViewModel.isPatientsFetchSuccess = true
                    authManager.patientList = it as ArrayList<Patient>
                    if (authManager.patientList.size == 1 && authManager.patientList.firstOrNull()?.isSecondaryAccount == true) {
                        if (!loginViewModel.isErrorDialogDisplayed) {
                            dialogForRequestFailure(
                                ModelUtil.patientRequest, ModelUtil.errorSecondaryAccount
                            )
                        }
                    } else {
                        if (isAfterLogin) {
                            if (!sharedPreference.getBoolean(
                                    ModelUtil.isAppointmentReminderPeriodic, false
                                )
                            ) {
                                AppointmentReminderJobService.scheduleAppointmentReminderJobService(
                                    this, false
                                )
                            }
                            if (!sharedPreference.getBoolean(
                                    ModelUtil.isPrescriptionReminderSchedulerStarted, false
                                )
                            ) {
                                FetchReminderDataWorker.fetchReminderDataRequest(this)
                                ScheduleReminderPerDayWorker.scheduleReminderPerDayWorkRequest(this)
                                sharedPreference.edit().putBoolean(
                                    ModelUtil.isPrescriptionReminderSchedulerStarted, true
                                ).commit()
                            }
                        }

                        checkWhetherAllDataIsReceived()
                    }
                } else if (it is APIResponseError) {
                    if (!loginViewModel.isErrorDialogDisplayed) {
                        dialogForRequestFailure(ModelUtil.patientRequest, it.charmApiCode ?: 0)
                    }

                    Log.d("patientListFail1", it.errorMsg ?: "")
                }

            }
        }
    }

    private fun fetchMenuList() {
        loginViewModel.fetchMenuList().observe(this) {
            if (it != null) {
                loginViewModel.isMenuListResponseReceived = true

                if (it is MenuList) {
                    loginViewModel.isMenuListFetchSuccess = true
                    authManager.menuList = it

                    checkWhetherAllDataIsReceived()

                } else if (it is APIResponseError) {

                    if (!loginViewModel.isErrorDialogDisplayed) {
                        dialogForRequestFailure(ModelUtil.menuListRequest, it.charmApiCode ?: 0)
                    }

                    Log.d("patientListFail2", it.errorMsg ?: "")
                }

            }
        }
    }

    fun dialogForRequestFailure(type: String, charmAPICode: Int) {

        loginViewModel.isErrorDialogDisplayed = true
        progressDialog.dismiss()

        authManager.updateAuthStatus()

        if (type == ModelUtil.patientEmptyRequest || charmAPICode == ModelUtil.patientNotAvailableForPracticeCode || charmAPICode == ModelUtil.errorPracticeAccountLoginCode || charmAPICode == ModelUtil.errorSecondaryAccount || charmAPICode == 0) {
            binding.loginWebView.visibility = View.GONE
            binding.loginFailureLayout.visibility = View.VISIBLE
            binding.loginRetryBtn.visibility = View.GONE
            binding.loginProgressView.root.visibility = View.GONE
            binding.loginNotSubscribedImg.visibility = View.VISIBLE
            if (charmAPICode == ModelUtil.errorPracticeAccountLoginCode) {
                binding.loginErrorMsgTV.setText(R.string.error_practice_login)
            } else if (charmAPICode == ModelUtil.errorSecondaryAccount) {
                binding.loginErrorMsgTV.text = getString(R.string.error_secondary_patient, getString(R.string.app_name))
            } else {
                binding.loginErrorMsgTV.text = getString(R.string.error_no_patients_found, getString(R.string.app_name))
            }
            authManager.logout()

            lifecycleScope.launch {
                delay(5000)
                val intent = Intent(applicationContext, TourPageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        } else {

            if (type == ModelUtil.menuListRequest && loginViewModel.isPatientsFetchSuccess && authManager.patientList.isEmpty() && BuildUtil.isCustomApp) {
                dialogForRequestFailure(ModelUtil.patientEmptyRequest, charmAPICode)
            } else {


                binding.loginErrorPage.root.visibility = View.VISIBLE


                binding.loginErrorPage.errorPageTryAgainBtn.setOnClickListener {
                    loginViewModel.isErrorDialogDisplayed = false
                    binding.loginErrorPage.root.visibility = View.GONE
                    progressDialog.setMessage(R.string.loading_data)
                    showMdProgressDialog()

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

        if (loginViewModel.isPatientsResponseReceived && loginViewModel.isMenuListResponseReceived) {

            if (!loginViewModel.isPatientsFetchSuccess) {
                loginViewModel.isPatientsResponseReceived = false
                dialogForRequestFailure(ModelUtil.patientRequest, 0)
            } else if (!loginViewModel.isMenuListFetchSuccess) {
                loginViewModel.isMenuListResponseReceived = false
                dialogForRequestFailure(ModelUtil.menuListRequest, 0)
            } else {
                progressDialog.dismiss()
                if (authManager.patientList.isEmpty()) {
                    dialogForRequestFailure(ModelUtil.patientEmptyRequest, 0)
                } else {
                    intentToMainPage()
                }
            }
        }
    }

    private fun intentToMainPage() {
        progressDialog.dismiss()
        val prefEdit = sharedPreference.edit()
        prefEdit.putBoolean(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN, true)
        prefEdit.putString(
            ModelUtil.patientList, MDJsonBuilder.formPatientList(authManager.patientList)
        )
        prefEdit.putString(ModelUtil.menuList, MDJsonBuilder.formMenuList(authManager.menuList))
        prefEdit.commit()

        lifecycleScope.launch {
            delay(200)
            if (isLocalAuthConfigured() && sharedPreference.getString(ModelUtil.applockType, "")
                    .isNullOrBlank()
            ) {
                val intent = Intent(this@LoginActivity, AppLockTypeActivity::class.java)
                intent.putExtra(ModelUtil.isFirst, true)
                intent.putExtra(ModelUtil.isFirstTimeForAppLock, true)
                startActivity(intent)
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            } else {
                val intent = Intent(this@LoginActivity, PasscodeActivity::class.java)
                intent.putExtra(ModelUtil.isFirst, true)
                if (!sharedPreference.getString(ModelUtil.PASSCODE, "").isNullOrBlank()) {
                    intent.putExtra(ModelUtil.passCodeType, PasscodeActivity.checkPasscode)
                } else {
                    intent.putExtra(ModelUtil.applockType, SecurityUtil.LockType.PinLock.toString())
                    intent.putExtra(ModelUtil.passCodeType, PasscodeActivity.setPasscode)
                }
                startActivity(intent)
                finish()
                if (sharedPreference.getString(ModelUtil.PASSCODE, "").isNullOrBlank()) {
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        }
    }

}
