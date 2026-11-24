package com.charm.phr.mphr.ui.announcement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAnnouncementListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Announcements
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

/**
 * [AnnouncementListActivity] shows the list of announcements of practices of the selected patient.
 */

class AnnouncementListActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var announcementListViewModel: AnnouncementListViewModel

    @Inject
    lateinit var announcementListListAdapter: AnnouncementListAdapter

    lateinit var binding: ActivityAnnouncementListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAnnouncementListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        announcementListViewModel = ViewModelProvider(this, viewModelFactory)[AnnouncementListViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.announcementListToolbar)
        UIUtil.applyViewInsertForBottomView(binding.announcementListMainLayout)

        binding.announcementListListRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.announcementListListRV.adapter = announcementListListAdapter

        announcementListListAdapter.setAnnouncementListSelectListener(object : AnnouncementListAdapter.AnnouncementListSelectListener {
            override fun onAnnouncementListClick(announcements: Announcements) {

                val intent = Intent(this@AnnouncementListActivity, AnnouncementDetailActivity::class.java)
                intent.putExtra(ModelUtil.announcementIdIntent, announcements.announcementId)
                intent.putExtra(ModelUtil.practiceIdIntent, announcements.practiceId)
                intent.putExtra(ModelUtil.announcementDateIntent, announcements.time?.toString(ModelUtil.charmDisplayDateFormat()))
                intent.putExtra(ModelUtil.announcementSubjectIntent, announcements.subject)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            }
        })

        announcementListViewModel.announcementListLive.observe(this, Observer { resp ->

            if (resp != null) {

                binding.announcementListListProgress.root.visibility = View.GONE
                if (resp is ArrayList<*>) {
                    if (binding.announcementListListSwipeRefreshLayout.isRefreshing) {
                        binding.announcementListListSwipeRefreshLayout.isRefreshing = false
                    }

                    val size = announcementListListAdapter.setAnnouncementList(resp as ArrayList<Announcements>)
                    if (size == 0) {
                        binding.announcementListListNoDataTV.root.visibility = View.VISIBLE
                        binding.announcementListListRV.visibility = View.GONE
                    } else {
                        binding.announcementListListNoDataTV.root.visibility = View.GONE
                        binding.announcementListListRV.visibility = View.VISIBLE
                        checkToShowBgImage()
                    }
                } else if (resp is APIResponseError) {
                    binding.announcementListErrorPage.root.visibility = View.VISIBLE
                    binding.announcementListListRV.visibility = View.GONE
                }

            }
        })


        announcementListViewModel.fetchAnnouncementList()

        binding.announcementListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            fetchAnnouncementList()
        }

        binding.announcementListListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        binding.announcementListListSwipeRefreshLayout.setOnRefreshListener {
            binding.announcementListListSwipeRefreshLayout.isRefreshing = true
            announcementListViewModel.fetchAnnouncementList()
        }

        binding.announcementListBackBtn.setOnClickListener {
            onBackPressed()
        }

    }

    private fun fetchAnnouncementList() {
        binding.announcementListListProgress.root.visibility = View.VISIBLE
        binding.announcementListListNoDataTV.root.visibility = View.GONE
        binding.announcementListErrorPage.root.visibility = View.GONE
        binding.announcementListListRV.visibility = View.GONE
        announcementListViewModel.fetchAnnouncementList()

    }

    private fun checkToShowBgImage() {

        if (!BuildUtil.isCustomApp) {

            val height = UIUtil.getScreenHeight(this)

            val tv = TypedValue()
            var actionBarHeight = 0
            if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            }

            val viewHeight = ((height - actionBarHeight) * 2) / 3

            val footerTreeObserver = binding.announcementListListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    try {
                        binding.announcementListListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                        val announcementHeight = binding.announcementListListRV.height ?: 0
                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $announcementHeight")
                        if (announcementHeight < viewHeight) {
                            binding.announcementListBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.announcementListBgLayout.visibility = View.GONE
                        }
                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            binding.announcementListBgLayout.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
