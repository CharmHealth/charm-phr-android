package com.charm.phr.mphr.ui.message

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityMessageBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.ui.message.compose.ComposeMessageActivity
import com.charm.phr.mphr.ui.message.messageDetail.MessageDetailActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.PermissionUtil
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class MessageActivity : MDBaseActivity() {

    @Inject
    lateinit var messageViewPagerAdapter: MessageViewPagerAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var messageSubTitles: ArrayList<String> = arrayListOf()

    lateinit var messageViewModel: MessageViewModel

    private lateinit var binding: ActivityMessageBinding

    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        messageViewModel = ViewModelProvider(this, viewModelFactory)[MessageViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.messageToolbar)
        UIUtil.applyViewInsertForBottomView(binding.messageMainLayout)

        messageViewModel.unreadCount = intent.getIntExtra(ModelUtil.messageUnreadCountIntent, 0)

        messageSubTitles = arrayListOf(
            getString(R.string.message_inbox_str), getString(
                R.string.message_sent_str
            )
        )
        binding.messageViewPager.offscreenPageLimit = ModelUtil.viewPagerOffScreenPageLimit
        binding.messageViewPager.adapter = messageViewPagerAdapter
        TabLayoutMediator(binding.messageTabLayout, binding.messageViewPager) { tab, position ->
            tab.text = messageSubTitles[position]
        }.attach()

        val messageInboxTab = binding.messageTabLayout.getTabAt(0)
        messageInboxTab?.setCustomView(R.layout.layout_badge_for_tabs)
        messageInboxTab?.customView?.findViewById<TextView>(R.id.tabBadgeTxtTV)?.setText(R.string.message_inbox_str)

        val messageSentTab = binding.messageTabLayout.getTabAt(1)
        messageSentTab?.setCustomView(R.layout.layout_badge_for_tabs)
        messageSentTab?.customView?.findViewById<TextView>(R.id.tabBadgeTxtTV)?.setText(R.string.message_sent_str)
        messageSentTab?.customView?.findViewById<TextView>(R.id.tabBadgeTV)?.visibility = View.GONE


        updateTab(binding.messageTabLayout.getTabAt(0), messageViewModel.unreadCount)

        binding.messageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                if (messageViewModel.isMultiSelectEnabled) {
                    onMultiSelectChange(false)
                    if (position == 0) {
                        messageViewPagerAdapter.disableMultiSelectAdapter(1)
                    } else {
                        messageViewPagerAdapter.disableMultiSelectAdapter(0)
                    }
                }
            }
        })
        binding.messageBackBtn.setOnClickListener {
            onBackPressed()
        }

        if (!RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_MESSAGES, applicationContext)) {
            binding.messageComposeBtn.hide()
        }

        binding.messageComposeBtn.setOnClickListener {
            composeMsgBtnClick()
        }

        fetchComposeRestriction()

        if (BuildUtil.isPushNotificationEnabled) {
            val isNotificationPermissionDenied = sharedPreferences.getBoolean(PermissionUtil.isNotificationPermissionDenied, false)
            if(!isNotificationPermissionDenied){
                initializePermissionResultLauncher()
                PermissionUtil.requestNotificationPermission(this, ::permissionAcceptCallBack,
                    requestPermissionLauncher, skipOnDenied = true)
            }
        }

    }

    @SuppressLint("RestrictedApi")
    fun fetchComposeRestriction() {

        messageViewModel.getComposeMsgRestriction().observe(this) { resp ->

            if (resp != null) {


                if (resp is ArrayList<*>) {
                    messageViewModel.isComposeMsgRestrictionFetched = true
                    messageViewModel.composeMsgRestrictionList = resp as ArrayList<ComposeMsgRestriction>

                    if (messageViewModel.composeMsgRestrictionList.any { it.allowPHRMessaging }) {
                        messageViewModel.isComposeViewShown = true
                        if (RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_MESSAGES, applicationContext))
                            binding.messageComposeBtn.visibility = View.VISIBLE
                        messageViewModel.isComposeAvailable = messageViewModel.composeMsgRestrictionList.any { !it.isMessageRestrict && it.allowPHRMessaging }
                    } else {
                        messageViewModel.isComposeViewShown = false
                        binding.messageComposeBtn.visibility = View.GONE
                    }

                    if (messageViewModel.isComposeBtnClicked) {
                        messageViewModel.isComposeBtnClicked = false
                        progressDialog.dismiss()
                        composeMsgBtnClick()
                    } else if (messageViewModel.isComposeDetailClicked) {
                        messageViewModel.isComposeDetailClicked = false
                        progressDialog.dismiss()


                        intentToMessageDetailActivity()

                    } else {
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    }
                } else if (resp is APIResponseError) {

                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    val builder = AlertDialog.Builder(this)

                    builder.setMessage(R.string.error_occurred_try_again)
                    builder.setPositiveButton(R.string.retry_str) { _, _ ->
                        progressDialog.show()
                        fetchComposeRestriction()

                    }
                    builder.setNegativeButton(R.string.cancel_str) { _, _ ->
                        onBackPressed()
                    }
                    builder.show()
                }

            }
        }

    }

    private fun intentToMessageDetailActivity() {
        val messages = messageViewModel.message

        val intent = Intent(this, MessageDetailActivity::class.java)
        intent.putExtra(ModelUtil.messageIdIntent, messages.messageId)
        intent.putExtra(ModelUtil.practiceIdIntent, messages.practiceId)
        intent.putExtra(
            ModelUtil.messageDateIntent,
            messages.messageDate?.toString(ModelUtil.sqlTimeStampFormattor())
        )

        val filteredArray = ArrayList(messageViewModel.composeMsgRestrictionList.filter { it.practiceId == messages.practiceId })

        if (filteredArray.isEmpty()) {
            filteredArray.add(ComposeMsgRestriction(practiceId = messages.practiceId ?: ""))
        }

        intent.putExtra(ModelUtil.messageComposeMsgRestrictionListIntent, MDJsonBuilder.formComposeMsgRestrictionList(filteredArray))
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                messageViewPagerAdapter.refetchDataForSent()
            }
        } else if (requestCode == 2 && resultCode == Activity.RESULT_CANCELED) {
            messageViewPagerAdapter.refetchDataForSent()
        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            messageViewPagerAdapter.deleteMessage(messageViewModel.selectedMessagePosition, binding.messageViewPager.currentItem)
        }
    }

    fun updateBadge(toIncrement: Boolean) {
        setResult(Activity.RESULT_OK)
        if (toIncrement) {
            messageViewModel.unreadCount++
            binding.messageTabLayout.getTabAt(0)?.customView?.findViewById<TextView>(R.id.tabBadgeTV)?.text = "${messageViewModel.unreadCount}"
        } else {
            if (messageViewModel.unreadCount > 0) {
                messageViewModel.unreadCount--
            }

            updateTab(binding.messageTabLayout.getTabAt(0), messageViewModel.unreadCount)
        }


    }

    private fun updateTab(tab: TabLayout.Tab?, count: Int) {

        if (count > 0) {

            if (count > 99) {
                tab?.customView?.findViewById<TextView>(R.id.tabBadgeTV)?.setText(R.string.dashboard_max_notification)
            } else {
                tab?.customView?.findViewById<TextView>(R.id.tabBadgeTV)?.text = "$count"
            }

            tab?.customView?.findViewById<TextView>(R.id.tabBadgeTV)?.visibility = View.VISIBLE
        } else {
            tab?.customView?.findViewById<TextView>(R.id.tabBadgeTV)?.visibility = View.GONE
        }
    }


    fun onMultiSelectChange(isEnabled: Boolean) {
        messageViewModel.isMultiSelectEnabled = isEnabled

        if (isEnabled) {
            binding.messageDeleteBtn.visibility = View.VISIBLE
        } else {
            binding.messageDeleteBtn.visibility = View.GONE
        }
    }

    fun deleteMessage(messages: Messages, position: Int) {
        val builder = AlertDialog.Builder(this)

        builder.setMessage(R.string.message_delete_confirmation_msg)
        builder.setPositiveButton(R.string.yes_str) { _, _ ->
            progressDialog.show()
            messageViewModel.deleteMessage(messages.messageId ?: "",messages.practiceId?:"").observe(this) { resp ->
                if (resp != null) {
                    progressDialog.dismiss()
                    if (resp is Boolean) {
                        messageViewPagerAdapter.deleteMessage(position, binding.messageViewPager.currentItem)
                        UIUtil.showToast(this, R.string.message_delete_success_msg, Toast.LENGTH_LONG)
                    } else if (resp is APIResponseError) {
                        UIUtil.showToast(this, R.string.error_message_delete__msg, Toast.LENGTH_LONG)
                    }
                }
            }
        }
        builder.setNegativeButton(R.string.no_str) { _, _ ->

        }

        builder.show()

    }

    fun showFab() {
        binding.messageComposeBtn.show()
    }

    fun hideFab() {
        binding.messageComposeBtn.hide()
    }

    private fun composeMsgBtnClick() {
        if (messageViewModel.isComposeMsgRestrictionFetched) {
            if (messageViewModel.isComposeAvailable) {
                val intent = Intent(this, ComposeMessageActivity::class.java)
                val filteredList = messageViewModel.composeMsgRestrictionList.filter { !it.isMessageRestrict && it.allowPHRMessaging }
                intent.putExtra(ModelUtil.messageComposeMsgRestrictionListIntent, MDJsonBuilder.formComposeMsgRestrictionList(filteredList))
                startActivityForResult(intent, 0)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                UIUtil.showToast(this, R.string.message_restrict_msg, Toast.LENGTH_LONG)
            }
        } else {
            messageViewModel.isComposeBtnClicked = true
            progressDialog.show()
        }
    }

    fun floatingActionButtonVisiblity(): Int {
        return binding.messageComposeBtn.visibility
    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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

    interface MessageListener {
        fun onMessageSelect(messages: Messages, itemView: View, position: Int)
        fun onMessageDelete(messages: Messages, position: Int)
    }
}
