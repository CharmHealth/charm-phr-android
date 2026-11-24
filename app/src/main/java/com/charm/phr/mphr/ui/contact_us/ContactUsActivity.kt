package com.charm.phr.mphr.ui.contact_us

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityContactUsBinding
import com.charm.phr.mphr.ui.message.compose.ComposeMessageActivity
import com.charm.phr.mphr.ui.support.SupportActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.UIUtil.Companion.getHighlightedText
import javax.inject.Inject

class ContactUsActivity : MDBaseActivity() {

    private lateinit var contactUsViewModel: ContactUsViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityContactUsBinding

    //region LifeCycle Methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityContactUsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        contactUsViewModel = ViewModelProvider(this, viewModelFactory)[ContactUsViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.contactUsToolbar)
        UIUtil.applyViewInsertForBottomView(binding.contactUsMainLayout)

        contactUsViewModel.apply {
            messageComposeMsgRestriction = intent.getStringExtra(ModelUtil.messageComposeMsgRestrictionListIntent) ?: ""
        }

        renderUi()

        setClickListenerMethods()

    }

    override fun onResume() {
        super.onResume()
        if (contactUsViewModel.isFromIntent) {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    //endregion

    private fun renderUi() {
        binding.apply {
            val formattedCharmMsg = getString(R.string.msg_to_charm, getString(R.string.app_name))
            val techSupportStart = formattedCharmMsg.indexOf("technical support")
            val techSupportEnd = if (techSupportStart >= 0) techSupportStart + "technical support".length else formattedCharmMsg.length
            val messageToCharmText = SpannableString(formattedCharmMsg)
            if (techSupportStart >= 0) {
                messageToCharmText.setSpan(
                    android.text.style.ForegroundColorSpan(androidx.core.content.ContextCompat.getColor(this@ContactUsActivity, R.color.black87)),
                    techSupportStart,
                    techSupportEnd,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            charmMsgTV.text = messageToCharmText
            val highlightedPracticeText = getHighlightedText(R.string.msg_to_practice_str, 14, 30, R.color.black87)
            practiceMsgTV.text = highlightedPracticeText
            UIUtil.updateSelectionInCardViews(
                contactUsPracticeCV, contactPracticeIB, R.drawable.ic_checked, contactUsCharmCV, contactCharmIB, R.drawable.ic_unchecked
            )
        }
    }

    private fun setClickListenerMethods() {

        binding.apply {

            contactUsBackBtn.setOnClickListener {
                onBackPressed()
            }

            contactUsPracticeCV.setOnClickListener {
                if (it.isSelected) {
                    return@setOnClickListener
                }
                UIUtil.updateSelectionInCardViews(
                    contactUsPracticeCV, contactPracticeIB, R.drawable.ic_checked, contactUsCharmCV, contactCharmIB, R.drawable.ic_unchecked
                )

            }

            contactUsCharmCV.setOnClickListener {
                if (it.isSelected) {
                    return@setOnClickListener
                }
                UIUtil.updateSelectionInCardViews(
                    contactUsCharmCV, contactCharmIB, R.drawable.ic_checked, contactUsPracticeCV, contactPracticeIB, R.drawable.ic_unchecked
                )

            }

            proceedBtn.setOnClickListener {

                if (contactPracticeIB.isSelected) {
                    val isMessageAvailable = ModelUtil.isModuleEnabled(authManager.selectedPatientSection, ModelUtil.messageModuleId)
                    if (!isMessageAvailable) {
                        UIUtil.showToast(applicationContext, R.string.error_message_restriction, Toast.LENGTH_LONG)
                    } else {
                        val intent = Intent(this@ContactUsActivity, ComposeMessageActivity::class.java).apply {
                            putExtra(ModelUtil.messageComposeMsgRestrictionListIntent, contactUsViewModel.messageComposeMsgRestriction)
                            putExtra(ModelUtil.messageComposeFromDashboardIntent, true)
                        }
                        startActivity(intent)
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                } else {
                    val intent = Intent(this@ContactUsActivity, SupportActivity::class.java).apply {
                        contactUsViewModel.isFromIntent = true
                    }
                    startActivity(intent)
                    finish()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }

            }
        }

    }

}