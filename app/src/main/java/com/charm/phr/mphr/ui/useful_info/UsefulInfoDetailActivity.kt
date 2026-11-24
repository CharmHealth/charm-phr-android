package com.charm.phr.mphr.ui.useful_info

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityUsefulInfoDetailBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import org.joda.time.DateTime
import javax.inject.Inject

class UsefulInfoDetailActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var usefulInfoDetailViewModel: UsefulInfoDetailViewModel

    lateinit var binding: ActivityUsefulInfoDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsefulInfoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usefulInfoDetailViewModel = ViewModelProvider(this, viewModelFactory).get(UsefulInfoDetailViewModel::class.java)

        usefulInfoDetailViewModel.usefulInfo.practiceId = intent.getStringExtra(ModelUtil.practiceIdIntent) ?: ""

        UIUtil.applyViewInsertForToolbar(binding.usefulInfoDetailToolbar)
        UIUtil.applyViewInsertForBottomView(binding.usefulInfoDetailMainLayout)

        if (usefulInfoDetailViewModel.usefulInfo.practiceId.isNullOrBlank()) {

            /*if (isTaskRoot){
                val intent = Intent(this , TourPageActivity::class.java)
                intent.flags
                startActivity(intent)
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            else{*/
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            //}

            return
        }

        usefulInfoDetailViewModel.usefulInfo.time =
            DateTime.parse(intent.getStringExtra(ModelUtil.usefulInfoDateIntent), ModelUtil.charmDisplayDateFormat())
        usefulInfoDetailViewModel.usefulInfo.category = intent.getStringExtra(ModelUtil.usefulInfoCategoryIntent) ?: ""
        usefulInfoDetailViewModel.usefulInfo.infoId = intent.getStringExtra(ModelUtil.usefulInfoIdIntent) ?: ""


        binding.usefulInfoDetailSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        binding.usefulInfoDetailSwipeRefreshLayout.setOnRefreshListener {
            binding.usefulInfoDetailSwipeRefreshLayout.isRefreshing = true
            getAnnouncementDetail(true)
        }

        getAnnouncementDetail()

        binding.usefulInfoDetailErrorPage.errorPageTryAgainBtn.setOnClickListener {
            getAnnouncementDetail()
        }

        binding.usefulInfoDetailBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    fun getAnnouncementDetail(isFromRefresh: Boolean = false) {

        if (!isFromRefresh) {
            binding.usefulInfoDetailProgress.root.visibility = View.VISIBLE
            binding.usefulInfoDetailLayout.visibility = View.GONE
            binding.usefulInfoDetailErrorPage.root.visibility = View.GONE
        }


        usefulInfoDetailViewModel.getUsefulInfoDetail().observe(this, Observer { resp ->

            if (resp != null) {
                binding.usefulInfoDetailProgress.root.visibility = View.GONE

                if (isFromRefresh) {
                    binding.usefulInfoDetailSwipeRefreshLayout.isRefreshing = false
                }
                if (resp is String) {
                    binding.usefulInfoDetailLayout.visibility = View.VISIBLE
                    usefulInfoDetailViewModel.usefulInfo.detailedContent = resp
                    /*
                                        usefulInfoDetailContentTV.text = HtmlCompat.fromHtml(resp , 0).removeSuffix("\n\n")
                                        usefulInfoDetailContentTV.movementMethod = LinkMovementMethod.getInstance()*/

                    // usefulInfoDetailContentTV.settings.javaScriptEnabled = true
                    binding.usefulInfoDetailContentTV.loadDataWithBaseURL(
                        null,
                        "<html style='background-color:#ffffff'>${resp ?: ""}</html>",
                        "text/html",
                        "utf-8",
                        null
                    );


                    binding.usefulInfoDetailContentTV.webViewClient = object : WebViewClient() {
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


                    binding.usefulInfoDetailDateTV.text =
                        usefulInfoDetailViewModel.usefulInfo.time?.toString(ModelUtil.charmDisplayDateFormat())
                    binding.usefulInfoDetailSubjectTV.text = usefulInfoDetailViewModel.usefulInfo.category

                } else if (resp is APIResponseError) {

                    binding.usefulInfoDetailErrorPage.root.visibility = View.VISIBLE

                }
            }
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

    }
}
