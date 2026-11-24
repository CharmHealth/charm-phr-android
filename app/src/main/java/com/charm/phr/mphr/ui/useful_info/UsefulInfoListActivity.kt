package com.charm.phr.mphr.ui.useful_info

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
import com.charm.phr.mphr.databinding.ActivityUsefulInfoListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.UsefulInfo
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

class UsefulInfoListActivity : MDBaseActivity() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var usefulInfoListViewModel: UsefulInfoListViewModel

    @Inject
    lateinit var usefulInfoListListAdapter: UsefulInfoListAdapter

    lateinit var binding: ActivityUsefulInfoListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsefulInfoListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usefulInfoListViewModel = ViewModelProvider(this, viewModelFactory).get(UsefulInfoListViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.usefulInfoListToolbar)
        UIUtil.applyViewInsertForBottomView(binding.usefulInfoListMainLayout)

        binding.usefulInfoListListRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.usefulInfoListListRV.adapter = usefulInfoListListAdapter



        usefulInfoListListAdapter.setUsefulInfoListSelectListener(object :
            UsefulInfoListAdapter.UsefulInfoListSelectListener {

            override fun onUsefulInfoListClick(usefulInfo: UsefulInfo) {

                val intent = Intent(this@UsefulInfoListActivity , UsefulInfoDetailActivity::class.java)
                intent.putExtra(ModelUtil.usefulInfoIdIntent , usefulInfo.infoId)
                intent.putExtra(ModelUtil.usefulInfoDateIntent , usefulInfo.time?.toString(ModelUtil.charmDisplayDateFormat()))
                intent.putExtra(ModelUtil.usefulInfoCategoryIntent , usefulInfo.category)
                intent.putExtra(ModelUtil.practiceIdIntent , usefulInfo.practiceId)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            }
        })

        usefulInfoListViewModel.usefulInfoListLive.observe(this, Observer { resp ->

            if (resp != null) {

                binding.usefulInfoListListProgress.root.visibility = View.GONE
                if (resp is ArrayList<*>) {
                    if (binding.usefulInfoListListSwipeRefreshLayout.isRefreshing){
                        binding.usefulInfoListListSwipeRefreshLayout.isRefreshing = false
                    }

                    val size = usefulInfoListListAdapter.setUsefulInfoList(resp as ArrayList<UsefulInfo>)
                    if (size == 0) {
                        binding.usefulInfoListListNoDataTV.root.visibility = View.VISIBLE
                        binding.usefulInfoListListRV.visibility = View.GONE
                    }
                    else {
                        binding.usefulInfoListListNoDataTV.root.visibility = View.GONE
                        binding.usefulInfoListListRV.visibility = View.VISIBLE
                        checkToShowBgImage()
                    }
                }
                else if (resp is APIResponseError) {
                    binding.usefulInfoListErrorPage.root.visibility = View.VISIBLE
                    binding.usefulInfoListListRV.visibility = View.GONE
                }

            }
        })


        usefulInfoListViewModel.fetchUsefulInfoList()

        binding.usefulInfoListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            fetchAnnouncementList()
        }




        binding.usefulInfoListListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        binding.usefulInfoListListSwipeRefreshLayout.setOnRefreshListener {
            binding.usefulInfoListListSwipeRefreshLayout.isRefreshing = true
            usefulInfoListViewModel.fetchUsefulInfoList( )
        }

        binding.usefulInfoListBackBtn.setOnClickListener {
            onBackPressed()
        }


    }

    private fun fetchAnnouncementList() {
        binding.usefulInfoListListProgress.root.visibility = View.VISIBLE
        binding.usefulInfoListListNoDataTV.root.visibility = View.GONE
        binding.usefulInfoListErrorPage.root.visibility = View.GONE
        binding.usefulInfoListListRV.visibility = View.GONE
        usefulInfoListViewModel.fetchUsefulInfoList()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun checkToShowBgImage() {

        if (!BuildUtil.isCustomApp){
        val height = UIUtil.getScreenHeight(this)

        val tv = TypedValue()
        var actionBarHeight = 0
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }

        val viewHeight = ((height - actionBarHeight)*2) /3

        val footerTreeObserver = binding.usefulInfoListListRV.viewTreeObserver

        footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                try {

                    binding.usefulInfoListListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                    val announcementHeight = binding.usefulInfoListListRV.height ?: 0

                    Log.d("MDHPBgHeights" , "viewHeight -> $viewHeight ; rvHeight -> $announcementHeight")

                    if (announcementHeight < viewHeight){
                        binding.usefulInfoListBgLayout.visibility = View.VISIBLE
                    }
                    else{
                        binding.usefulInfoListBgLayout.visibility = View.GONE
                    }

                }
                catch (e: Exception){

                }
            }
        })
        }
        else{
            binding.usefulInfoListBgLayout.visibility = View.GONE
        }

    }

}
