package com.charm.phr.mphr.ui.appointment.book_appointment.easytelecare

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityShowWebViewBinding
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil

class EasyTeleBookAppointmentActivity : MDBaseActivity() {
    private lateinit var binding: ActivityShowWebViewBinding
    private lateinit var backPressAlertDialogBuilder: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UIUtil.applyViewInsertForToolbar(binding.bookAppointmentToolbar)
        UIUtil.applyViewInsertForBottomView(binding.easyTeleMainLayout)

        binding.bookAppointmentToolbarText.text = intent.getStringExtra(ModelUtil.webviewHeader)
        loadWebView()
        setOnClickListener()
        setBackPressAlert()
    }

    override fun onBackPressed() {
        if (binding.bookAppointmentWebView.canGoBack()) {
            binding.bookAppointmentWebView.goBack()
        } else {
            super.onBackPressed()
            if (intent.getBooleanExtra(ModelUtil.appointmentIsFromDashboardIntent, false)) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            } else {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }

    private fun setBackPressAlert() {
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    super.onBackPressed()
                    if (intent.getBooleanExtra(ModelUtil.appointmentIsFromDashboardIntent, false)) {
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    }
                }

                DialogInterface.BUTTON_NEGATIVE -> {

                }
            }
        }
        backPressAlertDialogBuilder = AlertDialog.Builder(this)
        backPressAlertDialogBuilder.setMessage(R.string.are_you_sure_to_exit)
            .setPositiveButton(R.string.yes_str, dialogClickListener)
            .setNegativeButton(R.string.no_str, dialogClickListener)
    }

    private fun loadWebView() {
        binding.bookAppointmentWebView.settings.javaScriptEnabled = true
        binding.bookAppointmentProgress.root.visibility = View.VISIBLE
        val url = intent.getStringExtra(ModelUtil.appointmentEasyTeleCare)
        if (!url.isNullOrBlank()) {
            binding.bookAppointmentWebView.loadUrl(url)
        }

        binding.bookAppointmentWebView.webViewClient = object : WebViewClient() {
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                binding.bookAppointmentProgress.root.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true

            }
        }
    }

    fun setOnClickListener() {
        binding.bookAppointmentBackBtn.setOnClickListener {
            onBackPressed()
        }
        binding.bookAppCloseBtn.setOnClickListener {
            backPressAlertDialogBuilder.show()
        }

    }

}