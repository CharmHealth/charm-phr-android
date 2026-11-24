package com.charm.phr.mphr.ui.announcement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAnnouncementDetailBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * [AnnouncementDetailActivity] shows the detailed content of the selected announcement.
 */
class AnnouncementDetailActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var announcementDetailViewModel: AnnouncementDetailViewModel

    private lateinit var binding: ActivityAnnouncementDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAnnouncementDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        announcementDetailViewModel = ViewModelProvider(this, viewModelFactory)[AnnouncementDetailViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.announcementDetailToolbar)
        UIUtil.applyViewInsertForBottomView(binding.announcementMainLayout)

        announcementDetailViewModel.announcement.time = DateTime.parse(intent.getStringExtra(ModelUtil.announcementDateIntent), ModelUtil.charmDisplayDateFormat())
        announcementDetailViewModel.announcement.subject = intent.getStringExtra(ModelUtil.announcementSubjectIntent) ?: ""
        announcementDetailViewModel.announcement.announcementId = intent.getStringExtra(ModelUtil.announcementIdIntent) ?: ""
        announcementDetailViewModel.announcement.practiceId = intent.getStringExtra(ModelUtil.practiceIdIntent) ?: ""


        binding.announcementDetailSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        binding.announcementDetailSwipeRefreshLayout.setOnRefreshListener {
            binding.announcementDetailSwipeRefreshLayout.isRefreshing = true
            getAnnouncementDetail(true)
        }

        getAnnouncementDetail()

        binding.announcementDetailErrorPage.errorPageTryAgainBtn.setOnClickListener {
            getAnnouncementDetail()
        }

        binding.announcementDetailBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    fun getAnnouncementDetail(isFromRefresh: Boolean = false) {

        if (!isFromRefresh) {
            binding.announcementDetailProgress.root.visibility = View.VISIBLE
            binding.announcementDetailLayout.visibility = View.GONE
            binding.announcementDetailErrorPage.root.visibility = View.GONE
        }

        announcementDetailViewModel.getAnnouncementDetail().observe(this, Observer { resp ->

            if (resp != null) {
                binding.announcementDetailProgress.root.visibility = View.GONE

                if (isFromRefresh) {
                    binding.announcementDetailSwipeRefreshLayout.isRefreshing = false
                }
                if (resp is String) {
                    binding.announcementDetailLayout.visibility = View.VISIBLE
                    announcementDetailViewModel.announcement.detailedContent = resp

                    //  announcementDetailContentTV.settings.javaScriptEnabled = true
                    binding.announcementDetailContentTV.loadDataWithBaseURL(null, "<html style='background-color:#ffffff'>${resp ?: ""}</html>", "text/html", "utf-8", null);

                    binding.announcementDetailContentTV.webViewClient = object : WebViewClient() {
                        override fun onPageCommitVisible(view: WebView?, url: String?) {
                            super.onPageCommitVisible(view, url)
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            view?.context?.startActivity(
                                Intent(Intent.ACTION_VIEW, request?.url)
                            )
                            return true

                        }
                    }

                    binding.announcementDetailDateTV.text = announcementDetailViewModel.announcement.time?.toString(ModelUtil.charmDisplayDateFormat())
                    binding.announcementDetailSubjectTV.text = announcementDetailViewModel.announcement.subject

                } else if (resp is APIResponseError) {

                    binding.announcementDetailErrorPage.root.visibility = View.VISIBLE

                }
            }
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
