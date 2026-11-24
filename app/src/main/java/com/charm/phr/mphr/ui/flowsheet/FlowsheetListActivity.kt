package com.charm.phr.mphr.ui.flowsheet

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
import com.charm.phr.mphr.databinding.ActivityFlowsheetListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Flowsheets
import com.charm.phr.mphr.ui.flowsheet.entry.FlowsheetDataActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

class FlowsheetListActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var flowsheetListViewModel: FlowsheetListViewModel

    @Inject
    lateinit var flowsheetListAdapter: FlowsheetListAdapter

    lateinit var binding: ActivityFlowsheetListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlowsheetListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        flowsheetListViewModel = ViewModelProvider(this, viewModelFactory).get(FlowsheetListViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.flowsheetListToolbar)
        UIUtil.applyViewInsertForBottomView(binding.flowsheetListMainLayout)

        binding.flowsheetListRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.flowsheetListRV.adapter = flowsheetListAdapter


        binding.flowsheetListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        flowsheetListViewModel.flowsheetListLive.observe(this, Observer { resp ->

            if (resp != null) {


                if (resp is Pair<*, *>) {
                    val flowsheetPairList = resp as Pair<ArrayList<Flowsheets>, Boolean>

                    if (flowsheetPairList.second) {

                        binding.flowsheetListProgress.root.visibility = View.GONE

                        if (binding.flowsheetListSwipeRefreshLayout.isRefreshing) {
                            binding.flowsheetListSwipeRefreshLayout.isRefreshing = false
                        }

                        val linearLayoutManager = binding.flowsheetListRV.layoutManager as LinearLayoutManager
                        linearLayoutManager.scrollToPositionWithOffset(0, 0)

                        val size = flowsheetListAdapter.setFlowsheetList(flowsheetPairList.first)
                        if (size == 0) {
                            binding.flowsheetListNoDataTV.root.visibility = View.VISIBLE
                            binding.flowsheetListRV.visibility = View.GONE
                        } else {
                            binding.flowsheetListNoDataTV.root.visibility = View.GONE
                            binding.flowsheetListRV.visibility = View.VISIBLE

                            checkToShowBgImage()


                        }
                    } else {

                        flowsheetListAdapter.removeInfiniteScroll()
                        flowsheetListAdapter.addFlowsheetList(flowsheetPairList.first)
                    }
                } else if (resp is APIResponseError) {
                    if (!flowsheetListViewModel.isFlowsheetListFetched) {
                        binding.flowsheetListNoDataTV.root.visibility = View.GONE
                        binding.flowsheetListRV.visibility = View.GONE
                        binding.flowsheetListErrorPage.root.visibility = View.VISIBLE
                    }
                }

                flowsheetListViewModel.isFlowsheetFetchComplete = true
            }
        })


        if (!flowsheetListViewModel.isFlowsheetListFetched) {
            flowsheetListViewModel.fetchFlowsheetList(true)
        }

        flowsheetListAdapter.setFlowsheetListSelectListener(object :FlowsheetListAdapter.FlowsheetListSelectListener{
            override fun onFlowsheetListClick(flowsheets: Flowsheets) {

                val intent = Intent(this@FlowsheetListActivity , FlowsheetDataActivity::class.java)
                intent.putExtra(ModelUtil.flowsheetListSelectIntent , flowsheets.toString)
                intent.putExtra(ModelUtil.practiceIdIntent , flowsheets.practiceId)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            }
        })

        binding.flowsheetListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastIndexInScreen = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()


                Log.d("messageIbListItemCount", flowsheetListAdapter.itemCount.toString())
                Log.d("messageIbListComplete", flowsheetListViewModel.isFlowsheetFetchComplete.toString())

                if ((lastIndexInScreen + 1) >= flowsheetListAdapter.itemCount - 5 && flowsheetListAdapter.itemCount != 0 && flowsheetListViewModel.isFlowsheetFetchComplete && flowsheetListViewModel.flowsheetHasMore) {
                    Log.d("scroll", "now")

                    flowsheetListViewModel.isFlowsheetFetchComplete = false
                    flowsheetListAdapter.addInfiniteScroll()
                    flowsheetListViewModel.fetchFlowsheetList(false)

                }
            }
        })

        binding.flowsheetListSwipeRefreshLayout.setOnRefreshListener {
            binding.flowsheetListSwipeRefreshLayout.isRefreshing = true
            flowsheetListViewModel.fetchFlowsheetList(true)
        }

        binding.flowsheetListBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.flowsheetListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.flowsheetListErrorPage.root.visibility = View.GONE
            refreshData()
        }



    }


    private fun refreshData() {
        flowsheetListViewModel.isFlowsheetListFetched = false
        flowsheetListViewModel.isFlowsheetFetchComplete = false
        binding.flowsheetListProgress.root.visibility = View.VISIBLE
        flowsheetListViewModel.fetchFlowsheetList(true)
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

            val footerTreeObserver = binding.flowsheetListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        binding.flowsheetListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val flowsheetHeight = binding.flowsheetListRV.height ?: 0

                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $flowsheetHeight")

                        if (flowsheetHeight < viewHeight) {
                            binding.flowsheetListBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.flowsheetListBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            binding.flowsheetListBgLayout.visibility = View.GONE
        }

    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}