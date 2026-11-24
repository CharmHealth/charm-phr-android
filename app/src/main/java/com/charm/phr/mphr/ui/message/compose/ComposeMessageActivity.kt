package com.charm.phr.mphr.ui.message.compose

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityComposeMessageBinding
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.PermissionUtil
import javax.inject.Inject

class ComposeMessageActivity : MDBaseActivity() {

    val TAG = "ComposeMessageActivity"

    lateinit var composeMessageViewModel: ComposeMessageViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    lateinit var binding: ActivityComposeMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComposeMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        composeMessageViewModel = ViewModelProvider(this, viewModelFactory)[ComposeMessageViewModel::class.java]

        composeMessageViewModel.isNotificationFlow = intent.getBooleanExtra(ModelUtil.isNotificationFlow, false)

        if (BuildUtil.isPushNotificationEnabled) {
            val isNotificationPermissionDenied = sharedPreferences.getBoolean(PermissionUtil.isNotificationPermissionDenied, false)
            if (!isNotificationPermissionDenied) {
                initializePermissionResultLauncher()
                PermissionUtil.requestNotificationPermission(
                    this, ::permissionAcceptCallBack,
                    requestPermissionLauncher, skipOnDenied = true
                )
            }
        }

        showComposeMessageFragment()

    }

    override fun onBackPressed() {

        val selectedFragment = supportFragmentManager.findFragmentById(R.id.composeMessageActivityLayout)
        if (selectedFragment is SearchProviderFragment && composeMessageViewModel.showBackPressDialog) {
            showBackPressDialogue()
        } else {
            super.onBackPressed()
            if (composeMessageViewModel.isFromDashboard && selectedFragment is ComposeMessageFragment) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            } else {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }

    fun showBackPressDialogue() {
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    composeMessageViewModel.showBackPressDialog = false
                    onBackPressed()
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                    composeMessageViewModel.showBackPressDialog = false
                    composeMessageViewModel.triggerRefresh()
                    onBackPressed()
                }
            }
        }
       AlertDialog.Builder(this).setMessage(R.string.discard_selection_msg)
            .setPositiveButton(R.string.save_str, dialogClickListener)
            .setNegativeButton(R.string.discard, dialogClickListener)
            .show()
    }

    private fun showComposeMessageFragment() {
        val composeMessageFragment = ComposeMessageFragment()
        val ft = supportFragmentManager.beginTransaction()
        ft.add(
            R.id.composeMessageActivityLayout,
            composeMessageFragment,
            "composeMessageFragment"
        )
        ft.commit()
    }

    fun showSearchProviderFragment() {

        val searchProviderFragment = SearchProviderFragment()
        supportFragmentManager.beginTransaction().apply {
            add(
                R.id.composeMessageActivityLayout,
                searchProviderFragment,
                "searchProviderFragment"
            )
            hide(supportFragmentManager.findFragmentByTag("composeMessageFragment")!!)
            addToBackStack(null)
            commit()
        }
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
