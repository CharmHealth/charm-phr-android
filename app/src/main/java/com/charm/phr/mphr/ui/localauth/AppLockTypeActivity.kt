package com.charm.phr.mphr.ui.localauth

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityLocalAuthBinding
import com.charm.phr.mphr.ui.dashboard.DashboardActivity
import com.charm.phr.mphr.ui.passcode.PasscodeActivity
import com.charm.phr.mphr.ui.patientList.PatientListActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.SecurityUtil
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

class AppLockTypeActivity : MDBaseActivity() {

    private lateinit var activityLocalAuthBinding: ActivityLocalAuthBinding

    @Inject
    lateinit var sharedPreference: SharedPreferences

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onBackPressed()
        }
    }

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLocalAuthBinding = ActivityLocalAuthBinding.inflate(layoutInflater)
        setContentView(activityLocalAuthBinding.root)

        UIUtil.applyViewInsertForToolbar(activityLocalAuthBinding.authToolbar)
        UIUtil.applyViewInsertForBottomView(activityLocalAuthBinding.authMainLayout)

        setClickListenerMethods()
    }

    override fun onResume() {
        super.onResume()
        renderUI()
    }
    //endregion

    private fun renderUI() {
        if (intent.getBooleanExtra(ModelUtil.passcodeFromDashboardIntent, false)) {
            activityLocalAuthBinding.authBackBtn.visibility = View.VISIBLE
        }
        if (intent.getBooleanExtra(ModelUtil.isFirstTimeForAppLock, false)) {
            activityLocalAuthBinding.configEditMsg.visibility = View.VISIBLE
            //activityLocalAuthBinding.infoIV.visibility = View.VISIBLE
            if (!sharedPreference.getString(ModelUtil.PASSCODE, "").isNullOrBlank()) {
                activityLocalAuthBinding.charmLockTV.setText(R.string.use_specific_lock)
            }
        }
        activityLocalAuthBinding.apply {
            if (sharedPreference.getString(ModelUtil.applockType, "").equals(SecurityUtil.LockType.SystemLock.toString())) {
                UIUtil.updateSelectionInCardViews(
                    sysLockCV,
                    sysLockIB,
                    R.drawable.ic_checked,
                    charmLockCV,
                    charmLockIB,
                    R.drawable.ic_unchecked
                )
            } else if (sharedPreference.getString(ModelUtil.applockType, "").equals(SecurityUtil.LockType.PinLock.toString())) {
                UIUtil.updateSelectionInCardViews(
                    charmLockCV,
                    charmLockIB,
                    R.drawable.ic_checked,
                    sysLockCV,
                    sysLockIB,
                    R.drawable.ic_unchecked
                )
            } else {
                charmLockCV.isSelected = false
                charmLockCV.setBackgroundResource(R.drawable.ic_card_unselected_bg)
                charmLockIB.setImageResource(R.drawable.ic_unchecked)
                sysLockCV.isSelected = false
                sysLockCV.setBackgroundResource(R.drawable.ic_card_unselected_bg)
                sysLockIB.setImageResource(R.drawable.ic_unchecked)
            }
        }
    }


    private fun setClickListenerMethods() {

        activityLocalAuthBinding.apply {


            authBackBtn.setOnClickListener {
                onBackPressed()
            }

            sysLockCV.setOnClickListener {
                if (!activityLocalAuthBinding.sysLockCV.isSelected) {
                    UIUtil.updateSelectionInCardViews(
                        sysLockCV,
                        sysLockIB,
                        R.drawable.ic_checked,
                        charmLockCV,
                        charmLockIB,
                        R.drawable.ic_unchecked
                    )
                    onSelectionHandler()
                }
                return@setOnClickListener
            }

            charmLockCV.setOnClickListener {
                if (!activityLocalAuthBinding.charmLockCV.isSelected) {
                    UIUtil.updateSelectionInCardViews(
                        charmLockCV,
                        charmLockIB,
                        R.drawable.ic_checked,
                        sysLockCV,
                        sysLockIB,
                        R.drawable.ic_unchecked
                    )

                    onSelectionHandler()
                }
                return@setOnClickListener
            }


        }

    }

    private fun onSelectionHandler() {

        if (activityLocalAuthBinding.charmLockCV.isSelected && sharedPreference.getString(ModelUtil.applockType, "")
                .isNullOrBlank() && !sharedPreference.getString(ModelUtil.PASSCODE, "").isNullOrBlank()
        ) {
            val prefEdit = sharedPreference.edit()
            prefEdit.putString(ModelUtil.applockType, SecurityUtil.LockType.PinLock.toString())
            prefEdit.commit()
            showPatientList()
        } else {
            val intent = Intent(this@AppLockTypeActivity, PasscodeActivity::class.java)
            intent.putExtra(ModelUtil.passCodeType, PasscodeActivity.setPasscode)
            if (activityLocalAuthBinding.sysLockCV.isSelected) {
                intent.putExtra(ModelUtil.applockType, SecurityUtil.LockType.SystemLock.toString())
            } else {
                intent.putExtra(ModelUtil.applockType, SecurityUtil.LockType.PinLock.toString())
            }
            intent.putExtra(ModelUtil.fromLockTypeSelection, true)
            intent.putExtra(ModelUtil.passcodeFromDashboardIntent, getIntent().getBooleanExtra(ModelUtil.passcodeFromDashboardIntent, false))
            if (getIntent().getBooleanExtra(ModelUtil.passcodeFromDashboardIntent, false)) {
                resultLauncher.launch(intent)
            } else {
                startActivity(intent)
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun showPatientList() {
        val patientList = authManager.patientList
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
                UIUtil.showToast(this, getString(R.string.no_data_available), Toast.LENGTH_SHORT)
            }
        }
    }
}