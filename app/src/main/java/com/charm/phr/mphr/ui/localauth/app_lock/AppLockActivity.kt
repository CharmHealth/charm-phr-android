package com.charm.phr.mphr.ui.localauth.app_lock

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivitySecuritySettingBinding
import com.charm.phr.mphr.databinding.ItemHealthListBinding
import com.charm.phr.mphr.ui.passcode.PasscodeActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.SecurityUtil
import com.charm.phr.mphr.util.SecurityUtil.isLocalAuthConfigured
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject


class AppLockActivity : MDBaseActivity() {

    private lateinit var activitySecuritySettingBinding: ActivitySecuritySettingBinding

    @Inject
    lateinit var sharedPreference: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activitySecuritySettingBinding = ActivitySecuritySettingBinding.inflate(layoutInflater)
        setContentView(activitySecuritySettingBinding.root)

        UIUtil.applyViewInsertForToolbar(activitySecuritySettingBinding.passcodeSettingToolbar)
        UIUtil.applyViewInsertForBottomView(activitySecuritySettingBinding.passcodeSettingMainLayout)

        setOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        renderUI()
    }

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun setOnClickListener() {
        activitySecuritySettingBinding.passcodeSettingBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun renderUI() {
        activitySecuritySettingBinding.passcodeSettingLV.removeAllViews()
        if (sharedPreference.getString(ModelUtil.applockType, "") == (SecurityUtil.LockType.PinLock.toString()) || sharedPreference.getString(
                ModelUtil.applockType,
                ""
            ).isNullOrBlank()
        ) {
            val changePinView = ItemHealthListBinding.inflate(
                LayoutInflater.from(activitySecuritySettingBinding.passcodeSettingLV.context),
                activitySecuritySettingBinding.passcodeSettingLV,
                false
            )
            changePinView.healthListIcon.setImageResource(R.drawable.ic_pin_icon)
            changePinView.healthListIcon.layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, resources.displayMetrics).toInt()
            changePinView.healthListIcon.layoutParams.width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, resources.displayMetrics).toInt()
            changePinView.healthListName.setText(R.string.lock_screen_pin)
            changePinView.healthListNotificationTV.visibility = View.GONE
            activitySecuritySettingBinding.passcodeSettingLV.addView(changePinView.root)
            changePinView.root.setOnClickListener {
                val intent = Intent(this, PasscodeActivity::class.java)
                intent.putExtra(ModelUtil.passCodeType, PasscodeActivity.resetPasscode)
                intent.putExtra(ModelUtil.passcodeFromDashboardIntent, true)
                intent.putExtra(ModelUtil.isFirst, false)
                startActivity(intent)
            }
        }
        if (isLocalAuthConfigured()) {
            val changeLockTypeView = ItemHealthListBinding.inflate(
                LayoutInflater.from(activitySecuritySettingBinding.passcodeSettingLV.context),
                activitySecuritySettingBinding.passcodeSettingLV,
                false
            )
            changeLockTypeView.root.setOnClickListener {
                val intent = Intent(this, PasscodeActivity::class.java)
                intent.putExtra(ModelUtil.passCodeType, PasscodeActivity.checkPasscode)
                intent.putExtra(ModelUtil.isFirst, false)
                intent.putExtra(ModelUtil.passcodeFromDashboardIntent, true)
                startActivity(intent)
            }
            changeLockTypeView.healthListIcon.setImageResource(R.drawable.ic_lock)
            changeLockTypeView.healthListIcon.layoutParams.height =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, resources.displayMetrics).toInt()
            changeLockTypeView.healthListIcon.layoutParams.width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, resources.displayMetrics).toInt()
            changeLockTypeView.healthListIcon.requestLayout()
            changeLockTypeView.healthListName.setText(R.string.change_lock_screen_configuration)
            changeLockTypeView.healthListNotificationTV.visibility = View.GONE
            activitySecuritySettingBinding.passcodeSettingLV.addView(changeLockTypeView.root)
        }
    }
}