package com.charm.phr.mphr.ui.passcode

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityPasscodeBinding
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.ui.components.CircleView
import com.charm.phr.mphr.ui.dashboard.DashboardActivity
import com.charm.phr.mphr.ui.localauth.AppLockTypeActivity
import com.charm.phr.mphr.ui.patientList.PatientListActivity
import com.charm.phr.mphr.ui.swaasa.SwaasaActivity
import com.charm.phr.mphr.ui.tourpage.TourPageActivity
import com.charm.phr.mphr.util.AppLockUtil
import com.charm.phr.mphr.util.MDAccountsRelatedUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.SecurityUtil
import com.charm.phr.mphr.util.SecurityUtil.isLocalAuthConfigured
import com.charm.phr.mphr.util.SecurityUtil.presentLockScreen
import com.charm.phr.mphr.util.UIUtil
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PasscodeActivity : MDBaseActivity() {


    companion object {

        const val setPasscode: Int = 0
        const val checkPasscode: Int = 1
        const val resetPasscode: Int = 2

    }

    private val correctStatusColor = -0x9e3aa0 //0xFFFF0000
    private val passcodeLength = 5
    private val TAG = "PasscodeActivity"

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var passCodeViewModel: PasscodeViewModel

    @Inject
    lateinit var executor: Executor


    @Inject
    lateinit var pref: SharedPreferences

    @Inject
    lateinit var appLockUtil: AppLockUtil

    private lateinit var binding: ActivityPasscodeBinding


    //region activity lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPasscodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        passCodeViewModel = ViewModelProvider(this, viewModelFactory).get(PasscodeViewModel::class.java)

        UIUtil.applyViewInsertForFullView(binding.passcodeMainConstrainLayout)

        // if (!resources.getBoolean(R.bool.isTenInchTablet)) {
        binding.passcodeCardView.elevation = 0f
        binding.passcodeHeaderDisplayTV.visibility = View.GONE
        binding.passcodeSeparatorLine.visibility = View.GONE
        binding.passcodeMainConstrainLayout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))

        //}

        passCodeViewModel.passcodeType = intent.getIntExtra(ModelUtil.passCodeType, checkPasscode)
        passCodeViewModel.userType = intent.getStringExtra(ModelUtil.userType)
        passCodeViewModel.isFirstTime = intent.getBooleanExtra(ModelUtil.isFirst, true)
        passCodeViewModel.blockBackFunction = intent.getBooleanExtra(ModelUtil.blockBackFunction, false)
        passCodeViewModel.from = intent.getStringExtra(ModelUtil.from) ?: ""
        passCodeViewModel.confirmUserSuccessfullyLoggedIn = intent.getBooleanExtra(ModelUtil.confirmUserSuccessfullyLoggedIn, false)

        passCodeViewModel.allowToStartActivity = true
        passCodeViewModel.fetchPrivileges()
        if (passCodeViewModel.passcodeType == setPasscode) {
            if (intent.getStringExtra(ModelUtil.applockType).equals(SecurityUtil.LockType.SystemLock.toString())) {
                passCodeViewModel.lockType = SecurityUtil.LockType.SystemLock
            } else {
                passCodeViewModel.lockType = SecurityUtil.LockType.PinLock
            }
        } else {
            if (pref.getString(ModelUtil.applockType, "").equals(SecurityUtil.LockType.SystemLock.toString())) {
                passCodeViewModel.lockType = SecurityUtil.LockType.SystemLock
            } else {
                passCodeViewModel.lockType = SecurityUtil.LockType.PinLock
            }
        }


        if (passCodeViewModel.lockType == SecurityUtil.LockType.SystemLock) {
            if (intent.getBooleanExtra(ModelUtil.passcodeFromDashboardIntent, false) && passCodeViewModel.passcodeType == checkPasscode) {
                binding.systemLockHeadingTv.setText(R.string.identity_verification)
                binding.systemLockSubHeadingTv.setText(R.string.charm_mPHR_lock_verification_lock_type)
                binding.localAuthLoginButton.setText(R.string.verify)
            } else {
                val appName = getString(R.string.app_name)
                binding.systemLockHeadingTv.text = getString(R.string.charm_mPHR_locked, appName)
                binding.systemLockSubHeadingTv.text = getString(R.string.unlock_charm_mPHR_msg, appName)
            }
            binding.passcodeCardView.isVisible = false
            binding.systemLockMainLayout.isVisible = true
            checkAndPresentLocalAuth()

            binding.localAuthLoginButton.setOnClickListener {
                checkAndPresentLocalAuth()
            }

            return
        } else {
            binding.passcodeCardView.isVisible = true
            binding.systemLockMainLayout.isVisible = false
        }

        binding.errorHintTextView.visibility = View.GONE


        when {
            passCodeViewModel.passcodeType == setPasscode -> {

                binding.passCodeHint.text = getString(R.string.create_new_passcode)
                binding.passcodeSignOutTV.visibility = View.GONE
                // toolbarTV.text = getString(R.string.set_passcode)

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                handler.postDelayed({
                    imm.showSoftInput(
                        binding.invisible,
                        InputMethodManager.SHOW_IMPLICIT
                    )
                }, 400)
            }

            passCodeViewModel.passcodeType == checkPasscode -> {

                if (intent.getBooleanExtra(ModelUtil.passcodeFromDashboardIntent, false)) {
                    binding.passcodeSignOutTV.visibility = View.GONE
                } else {
                    binding.passcodeSignOutTV.visibility = View.VISIBLE
                }
                /* toolbarTV.text = getString(R.string.passcode)

                 passcodeToolbar.visibility = View.VISIBLE*/
                binding.passCodeHint.text = getString(R.string.enter_passcode)


                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                handler.postDelayed({
                    imm.showSoftInput(
                        binding.invisible,
                        InputMethodManager.SHOW_IMPLICIT
                    )
                }, 400)


            }

            passCodeViewModel.passcodeType == resetPasscode -> {


                binding.passCodeHint.text = getString(R.string.enter_old_passcode)
                //passcodeToolbar.visibility = View.VISIBLE
                if (intent.getBooleanExtra(ModelUtil.passcodeFromDashboardIntent, false)) {
                    binding.passcodeSignOutTV.visibility = View.GONE
                } else {
                    binding.passcodeSignOutTV.visibility = View.VISIBLE
                }

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                handler.postDelayed({
                    imm.showSoftInput(
                        binding.invisible,
                        InputMethodManager.SHOW_IMPLICIT
                    )
                }, 400)

            }

        }


        //MDBaseActivity -
        hideOnScrollIsAllowed = false

        /* if (isTaskRoot) {
             //Hiding back button on root task
             passcodeToolbar.visibility = View.GONE
         }
 */

        //region listeners

        binding.passcodeMainConstrainLayout.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            if (passCodeViewModel.allowKeyBordPopUp) {
                imm.showSoftInput(
                    binding.invisible,
                    InputMethodManager.SHOW_IMPLICIT
                )
            }
        }

        binding.passcodeSignOutTV.setOnClickListener {

            val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        dialog.dismiss()
                        authManager.logout()
                        val intent = Intent(applicationContext, TourPageActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    }

                    DialogInterface.BUTTON_NEGATIVE -> {
                        dialog.dismiss()
                    }
                }
            }

            val builder = AlertDialog.Builder(this@PasscodeActivity)
            builder.setMessage(getString(R.string.alert_sign_out)).setPositiveButton(R.string.yes_str, dialogClickListener)
                .setNegativeButton(R.string.no_str, dialogClickListener).show()
        }

        /* passCodeBackButton.setOnClickListener {
             onBackPressed()
         }*/

        binding.invisible.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(editable: Editable?) {
                if (!editable.isNullOrBlank()) {

                    addPasscode(editable.toString())
                    if (binding.layoutPsd.childCount == passcodeLength) {
                        check()
                    }
                } else {
                    clearChar()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })


        //endregion
    }

    override fun onDestroy() {
        //MDBaseActivity
        hideOnScrollIsAllowed = true
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (!passCodeViewModel.blockBackFunction) {
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

        } else {

            if (passCodeViewModel.pcLastUserBackPressedTime + TimeUnit.SECONDS.toMillis(ModelUtil.bufferTimeForBackPress) > System.currentTimeMillis()) {
                //When User pressed back button again with in 10 sec
                finishAffinity()

            } else {
                UIUtil.showToast(baseContext, getString(R.string.press_once_again_to_exit), Toast.LENGTH_SHORT)
            }

            passCodeViewModel.pcLastUserBackPressedTime = System.currentTimeMillis()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ModelUtil.requestCodeForForgotPassCode && resultCode == RESULT_OK) {
            this.finish()
        }
    }

    //endregion

    //region other methods

    private fun addPasscode(passcode: String) {
        val passcodeCharArr = passcode.toCharArray()
        clearChar()
        try {
            for (passcodeCharIndex in passcodeCharArr.indices) {
                addChar(passcodeCharArr[passcodeCharIndex].toString().toInt(), passcodeCharIndex)
            }
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    private fun addChar(number: Int, index: Int): Boolean {

        if (binding.layoutPsd.childCount >= passcodeLength) {
            return false
        }

        val view = binding.underLine.getChildAt(binding.layoutPsd.childCount)
        view.visibility = View.INVISIBLE
        val psdView = CircleView(baseContext)
        val size = dpToPx(8f)
        val params = LinearLayout.LayoutParams(size, size)
        params.setMargins(size, 0, size, 0)
        psdView.layoutParams = params
        psdView.color = ContextCompat.getColor(applicationContext, R.color.colorPrimaryDark)
        psdView.tag = number
        binding.layoutPsd.addView(psdView)

        binding.underLine.getChildAt(index + 1)?.setBackgroundColor(ContextCompat.getColor(this, R.color.passcodeSelectedDashColor))

        return true
    }

    private fun dpToPx(valueInDp: Float): Int {
        val metrics = resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics).toInt()
    }

    private fun clearChar() {
        binding.layoutPsd.removeAllViews()
        for (posn in 0 until binding.underLine.childCount) {
            binding.underLine.getChildAt(posn).visibility = View.VISIBLE
            binding.underLine.getChildAt(posn).setBackgroundColor(ContextCompat.getColor(this, R.color.passcodeUnSelectedDashColor))
        }
        binding.underLine.getChildAt(0).setBackgroundColor(ContextCompat.getColor(this, R.color.passcodeSelectedDashColor))

    }

    /*private fun deleteChar() {

        val childCount = layoutPsd.childCount
        if (childCount <= 0) {
            return
        }
        val view = underLine.getChildAt(childCount - 1)
        view.visibility = View.VISIBLE
        layoutPsd.removeViewAt(childCount - 1)
    }*/

    private fun getPasscodeFromView(): String {

        val sb = StringBuilder()
        val childCount = binding.layoutPsd.childCount
        for (i in 0 until childCount) {
            val child = binding.layoutPsd.getChildAt(i)
            val num = child.tag as Int
            sb.append(num)
        }
        return sb.toString()
    }

    private fun check() {

        val passcode = getPasscodeFromView()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        binding.errorHintTextView.text = " "
        binding.errorHintTextView.visibility = View.GONE

        // SetPassCode - FirstTime
        if (passCodeViewModel.passcodeType == setPasscode && !passCodeViewModel.secondTime) {
            binding.passCodeHint.text = getString(R.string.re_enter_new_passcode)
            passCodeViewModel.secondTime = true
            passCodeViewModel.firstTimeVal = passcode
            clearChar()
            binding.invisible.setText("")
            return
        }

        if (passCodeViewModel.passcodeType == resetPasscode && !passCodeViewModel.secondTime && passCodeViewModel.isFirstTime) {
            if (passcode == appLockUtil.mdDecrypt(applicationContext, ModelUtil.PASSCODE, authManager)) {
                runWrongAnimation(getString(R.string.enter_new_passcode), getString(R.string.same_pin_cannot_used))
                return
            }
            binding.passCodeHint.text = getString(R.string.re_enter_new_passcode)
            passCodeViewModel.secondTime = true
            passCodeViewModel.firstTimeVal = passcode
            clearChar()
            binding.invisible.setText("")
            return
        }


        //SetPassCode - Second Time
        if (passCodeViewModel.passcodeType == setPasscode) {
            if (passCodeViewModel.firstTimeVal == passcode) {
                if (passCodeViewModel.allowToStartActivity) {
                    val prefEdit = pref.edit()
                    prefEdit.putString(ModelUtil.applockType, SecurityUtil.LockType.PinLock.toString())
                    prefEdit.commit()
                    correctAnimation(getString(R.string.passcode_created_successfully))
                    imm.hideSoftInputFromWindow(binding.invisible.windowToken, 0)
                    appLockUtil.mdEncrypt(passcode, applicationContext, ModelUtil.PASSCODE, authManager)
                    passCodeViewModel.allowToStartActivity = false
                }

            } else {
                passCodeViewModel.isFirstTime = true
                passCodeViewModel.secondTime = false
                runWrongAnimation(getString(R.string.create_new_passcode))
            }

            return
        }

        if (passCodeViewModel.passcodeType == checkPasscode) {
            if (passcode == appLockUtil.mdDecrypt(applicationContext, ModelUtil.PASSCODE, authManager)) {

                if (passCodeViewModel.allowToStartActivity) {
                    correctAnimation(getString(R.string.passcode_is_correct))
                    passCodeViewModel.allowToStartActivity = false
                    imm.hideSoftInputFromWindow(binding.invisible.windowToken, 0)
                }

            } else {
                handleWrongPasscode()
                return
            }
        }

        if (passCodeViewModel.passcodeType == resetPasscode) {

            if (!passCodeViewModel.isFirstTime && !passCodeViewModel.secondTime) {

                if (passcode == appLockUtil.mdDecrypt(applicationContext, ModelUtil.PASSCODE, authManager)) {
                    binding.passCodeHint.text = getString(R.string.enter_new_passcode)
                    binding.passcodeSignOutTV.visibility = View.GONE
                    passCodeViewModel.isFirstTime = true
                    clearChar()
                    binding.invisible.setText("")
                    imm.showSoftInput(binding.invisible, InputMethodManager.SHOW_IMPLICIT)
                    return
                } else {
                    runWrongAnimation(getString(R.string.enter_old_passcode))
                    imm.showSoftInput(binding.invisible, InputMethodManager.SHOW_IMPLICIT)
                    return
                }
            }

            if (passCodeViewModel.secondTime) {
                if (passCodeViewModel.firstTimeVal == passcode) {
                    appLockUtil.mdEncrypt(passcode, applicationContext, ModelUtil.PASSCODE, authManager)
                    if (passCodeViewModel.allowToStartActivity) {
                        correctAnimation(getString(R.string.passcode_changed_successfully))
                        passCodeViewModel.allowToStartActivity = false
                    }
                    imm.hideSoftInputFromWindow(binding.invisible.windowToken, 0)


                } else {
                    passCodeViewModel.isFirstTime = true
                    passCodeViewModel.secondTime = false
                    runWrongAnimation(getString(R.string.enter_new_passcode))
                    return
                }

            }
        }

    }

    private fun setBgForPassCode(color: Int) {
        val childCount = binding.layoutPsd.childCount
        for (i in 0 until childCount) {
            (binding.layoutPsd.getChildAt(i) as CircleView).color = color
        }
    }


    //endregion

    //region animation

    private fun correctAnimation(hint: String) {

        passCodeViewModel.allowKeyBordPopUp = false
        binding.cursor.translationX = 0f
        binding.cursor.visibility = View.VISIBLE
        binding.passcodeSignOutTV.visibility = View.GONE
        binding.cursor.animate().setDuration(600).translationX(binding.layoutPsd.width.toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    binding.cursor.visibility = View.INVISIBLE
                    setBgForPassCode(correctStatusColor)
                    binding.passCodeHint.text = hint

                    afterPasscodeSuccess()
                }
            })
            .start()
    }

    fun afterPasscodeSuccess() {
        pref.edit().putInt(ModelUtil.WRONG_PASSCODE_ATTEMPT_COUNT, 0).apply()

                    passCodeViewModel.allowKeyBordPopUp = false


                    //Login user -> Login already -> Yes to continue and verify the password->after verifying
                    if (passCodeViewModel.passcodeType == checkPasscode && passCodeViewModel.confirmUserSuccessfullyLoggedIn) {
                        val prefEditor = pref.edit()
                        prefEditor.putBoolean(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN, true)
                        prefEditor.commit()


                    }

                    //region rootTask

                    val patientList = authManager.patientList

                    if (isTaskRoot) //NOTE: While starting application,this is the root activity(Week Logic - change it later)
                    {
                        if (passCodeViewModel.passcodeType == setPasscode) {

                            succcessIntent(patientList)

                        } else if (passCodeViewModel.passcodeType == checkPasscode) {

                            if (!passCodeViewModel.isFirstTime) {
                                finish()
                            } else {
                                succcessIntent(patientList)
                            }
                        } else if (passCodeViewModel.passcodeType == resetPasscode) {
                            //reset passcode wont be occuring on root task
                            finish()
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                        }

                    }
                    //endregion

                    //region non root task
                    else {
                        //Not a root task
                        when {

                            passCodeViewModel.passcodeType == checkPasscode -> {

                                if (intent.getStringExtra(ModelUtil.swaasaIdIntent).isNullOrBlank()&& !intent.getBooleanExtra(
                            ModelUtil.passcodeFromDashboardIntent,
                            false
                        )) {
                                    finish()
                                    overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down)
                                } else {
                                    succcessIntent(patientList)
                                }

                            }

                            passCodeViewModel.passcodeType == resetPasscode -> {

                                finish()
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                            }

                            else -> {

                                /*  if (passCodeViewModel.from == ModelUtil.fromSettings) {
                                      val intent = Intent(this@PassCodeActivity, PasscodeSettingsActivity::class.java)
                                      intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                      startActivity(intent)
                                      finish()
                                      overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

                                  } else {*/
if (intent.getBooleanExtra(ModelUtil.passcodeFromDashboardIntent, false)) {
                        setResult(RESULT_OK)
                        finish()
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    } else {
                                succcessIntent(patientList)}


                                //  }

                            }}
                        }

                    //endregion

                    //xml reference: change here - 105
                    //from 10px  to  105 dp
                    /*
                     passCodeTick.visibility = View.VISIBLE

                    val anim = ValueAnimator.ofInt(10,ModelUtil.dpToPxConversion(105,this@PasscodeActivity))


                    anim.addUpdateListener { valueAnimator ->
                        val value = valueAnimator.animatedValue as Int
                        val layoutParams = passCodeTick.layoutParams
                        layoutParams.height = value
                        layoutParams.width = value
                        passCodeTick.layoutParams = layoutParams
                    }
                    anim.duration = ModelUtil.animationDuration
                    anim.start()

                    anim.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)

            }


                    })*/

    }

    private fun succcessIntent(patientList: ArrayList<Patient>) {

        authManager.patientList = patientList

        val swaasaId = intent.getStringExtra(ModelUtil.swaasaIdIntent)

        if (!swaasaId.isNullOrBlank()) {
            val intent = Intent(this, SwaasaActivity::class.java)
            intent.putExtra(ModelUtil.swaasaIdIntent, swaasaId)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }else if (pref.getString(ModelUtil.applockType, "").isNullOrBlank()) {
            val intent = Intent(this, AppLockTypeActivity::class.java)
            intent.putExtra(ModelUtil.isFirst, true)
            intent.putExtra(ModelUtil.isFirstTimeForAppLock, true)
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } else if (intent.getBooleanExtra(ModelUtil.passcodeFromDashboardIntent, false)) {
            val intent = Intent(this, AppLockTypeActivity::class.java)
            intent.putExtra(ModelUtil.passcodeFromDashboardIntent, true)
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } else if (isLocalAuthConfigured() && pref.getString(ModelUtil.applockType, "").isNullOrBlank()) {
            val intent = Intent(this, AppLockTypeActivity::class.java)
            intent.putExtra(ModelUtil.isFirst, true)
            startActivity(intent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } else {

            when {
                patientList.size > 1 -> {

                    val intent = Intent(applicationContext, PatientListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }

                patientList.size > 0 -> {
                    authManager.updateSelectedPatient(patientList.first())
                    val intent = Intent(applicationContext, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }

                else -> {
                    //if facility list = 0
                    UIUtil.showToast(this@PasscodeActivity, getString(R.string.no_data_available), Toast.LENGTH_SHORT)
                }
            }
        }
    }

    private fun shakeAnimator(view: View): Animator {
        return ObjectAnimator
            .ofFloat(view, "translationX", 0F, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
            .setDuration(500)
    }

    private fun handleWrongPasscode() {
        val count = pref.getInt(ModelUtil.WRONG_PASSCODE_ATTEMPT_COUNT, 0) + 1
        pref.edit().putInt(ModelUtil.WRONG_PASSCODE_ATTEMPT_COUNT, count).apply()
        val remaining = ModelUtil.MAX_WRONG_PASSCODE_ATTEMPTS - count

        if (remaining <= 0) {
            binding.invisible.isEnabled = false
            UIUtil.showToast(this, getString(R.string.max_incorrect_passcode_attempts_reached), Toast.LENGTH_LONG)
            runWrongAnimation(getString(R.string.enter_passcode))
            handler.postDelayed({
                pref.edit().putInt(ModelUtil.WRONG_PASSCODE_ATTEMPT_COUNT, 0).commit()
                authManager.logout()
                val intent = Intent(applicationContext, TourPageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }, 700)
        } else {
            // Show remaining attempts warning only after 2 wrong attempts
            if (count >= 2) {
                UIUtil.showToast(
                    this,
                    resources.getQuantityString(R.plurals.wrong_passcode_attempts_remaining, remaining, remaining),
                    Toast.LENGTH_LONG
                )
            }
            runWrongAnimation(getString(R.string.enter_passcode))
        }
    }

    private fun runWrongAnimation(hint: String, errorHint: String = getString(R.string.passcode_did_not_match)) {
        binding.cursor.translationX = 0f
        binding.cursor.visibility = View.VISIBLE
        binding.cursor.animate()
            .translationX(binding.layoutPsd.width.toFloat())
            .setDuration(600)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    binding.errorHintTextView.visibility = View.VISIBLE
                    binding.errorHintTextView.text = errorHint
                    binding.passCodeHint.text = hint
                    binding.cursor.visibility = View.INVISIBLE
                    val animator = shakeAnimator(binding.layoutPsd)
                    animator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            clearChar()
                            binding.invisible.setText("")
                        }
                    })
                    animator.start()
                }
            })
            .start()

    }

    //endregion

    private fun checkAndPresentLocalAuth() {

        if (this.isLocalAuthConfigured()) {
            if (passCodeViewModel.passcodeType == setPasscode) {
                this.presentLockScreen(instanceOfBiometricPrompt(), description = getString(R.string.unlock_charm_mPHR_description))
            } else if (intent.getBooleanExtra(ModelUtil.passcodeFromDashboardIntent, false)) {
                this.presentLockScreen(instanceOfBiometricPrompt(), title = getString(R.string.charm_mPHR_lock_verification))
            } else {
                this.presentLockScreen(instanceOfBiometricPrompt())
            }
        } else {
            UIUtil.showToast(this, getString(R.string.no_system_lock_configured), Toast.LENGTH_LONG)
            authManager.logout()
        }

    }

    private fun instanceOfBiometricPrompt(): BiometricPrompt {

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                setExitTime()
                @SuppressLint("RestrictedApi")
                if (ERROR_USER_CANCELED == errorCode) {
                    runOnUiThread {
                        if (intent.getBooleanExtra(ModelUtil.fromLockTypeSelection, false)) {
                            this@PasscodeActivity.onBackPressed()
                        }
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                setExitTime()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                setExitTime()
                if (passCodeViewModel.passcodeType == setPasscode) {
                    val prefEdit = pref.edit()
                    prefEdit.putString(ModelUtil.applockType, SecurityUtil.LockType.SystemLock.toString())
                    prefEdit.putString(ModelUtil.PASSCODE, "")
                    prefEdit.commit()
                }
                afterPasscodeSuccess()
            }
        }

        return BiometricPrompt(this, executor, callback)
    }

    fun setExitTime() {
        if (isPassCodeAllowed) {
            exitTime = System.currentTimeMillis() / 1000
            pref.edit().putLong(ModelUtil.exitTimeInSeconds, exitTime).commit()

        } else {
            pref.edit().putLong(ModelUtil.exitTimeInSeconds, 0).commit()
        }
    }


}


