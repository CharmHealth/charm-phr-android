package com.charm.phr.mphr.ui.flowsheet.entry

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
import com.charm.phr.mphr.databinding.ActivityFlowsheetDataBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PHRFlowsheets
import com.charm.phr.mphr.ui.flowsheet.add.AddFlowsheetDataActivity
import com.charm.phr.mphr.ui.swaasa.SwaasaActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.gson.JsonObject
import javax.inject.Inject

class FlowsheetDataActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var flowsheetDataListViewModel: FlowsheetDataViewModel

    @Inject
    lateinit var flowsheetDataListAdapter: FlowsheetDataListAdapter

    lateinit var binding: ActivityFlowsheetDataBinding

    private val addFlowsheetReqId = 2
    private var hasFlowsheetDataChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlowsheetDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        flowsheetDataListViewModel = ViewModelProvider(this, viewModelFactory).get(FlowsheetDataViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.flowsheetDataListToolbar)
        UIUtil.applyViewInsertForBottomView(binding.flowsheetDataListMainLayout)

        val flowsheetStr = intent.getStringExtra(ModelUtil.flowsheetListSelectIntent)

        if (!flowsheetStr.isNullOrBlank()) {
            flowsheetDataListViewModel.flowsheet = MDParsingHelper.formFlowsheetObj(flowsheetStr.parseStringAsJsonObject()?: JsonObject())
            flowsheetDataListViewModel.flowsheet.practiceId = intent.getStringExtra(ModelUtil.practiceIdIntent)
        }

        binding.flowsheetDataListRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.flowsheetDataListRV.adapter = flowsheetDataListAdapter


        binding.flowsheetDataListToolbarText.text = flowsheetDataListViewModel.flowsheet.name

        binding.flowsheetDataListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        flowsheetDataListViewModel.flowsheetDataListLive.observe(this, Observer { resp ->

            if (resp != null) {


                if (resp is ArrayList<*>) {
                    val flowsheetPairList = resp as ArrayList<PHRFlowsheets>


                    binding.flowsheetDataListProgress.root.visibility = View.GONE

                    if (binding.flowsheetDataListSwipeRefreshLayout.isRefreshing) {
                        binding.flowsheetDataListSwipeRefreshLayout.isRefreshing = false
                    }

                    val linearLayoutManager = binding.flowsheetDataListRV.layoutManager as LinearLayoutManager
                    linearLayoutManager.scrollToPositionWithOffset(0, 0)

                    val size = flowsheetDataListAdapter.setFlowsheetEntryList(flowsheetPairList)
                    if (size == 0) {
                        binding.flowsheetDataListNoDataTV.root.visibility = View.VISIBLE
                        binding.flowsheetDataListRV.visibility = View.GONE
                    } else {
                        binding.flowsheetDataListNoDataTV.root.visibility = View.GONE
                        binding.flowsheetDataListRV.visibility = View.VISIBLE

                        checkToShowBgImage()


                    }

                } else if (resp is APIResponseError) {
                    if (!flowsheetDataListViewModel.isFlowsheetDataFetched) {
                        binding.flowsheetDataListNoDataTV.root.visibility = View.GONE
                        binding.flowsheetDataListRV.visibility = View.GONE
                        binding.flowsheetDataListErrorPage.root.visibility = View.VISIBLE
                    }
                }

                flowsheetDataListViewModel.isFlowsheetDataFetchComplete = true
            }
        })


        if (!flowsheetDataListViewModel.isFlowsheetDataFetched) {
            flowsheetDataListViewModel.fetchFlowsheetData()
        }

        flowsheetDataListAdapter.setFlowsheetEntryListSelectListener(object : FlowsheetDataListAdapter.FlowsheetEntryListSelectListener {
            /*  override fun onFlowsheetDataClick(flowsheets: Flowsheets) {

                  val intent = Intent(this@FlowsheetDataActivity, FlowsheetDataActivity::class.java)
                  intent.putExtra(ModelUtil.flowsheetDataListSelectIntent, flowsheets.toString)
                  startActivity(intent)
                  overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
              }*/

            override fun onFlowsheetEntryListClick(phrFlowsheet: PHRFlowsheets) {

                val intent = if (phrFlowsheet.flowsheetEntries.firstOrNull()?.type == ModelUtil.flowsheetEntryTypeSwaasa){
                    Intent(this@FlowsheetDataActivity, SwaasaActivity::class.java)
                }
                else{
                    Intent(this@FlowsheetDataActivity, AddFlowsheetDataActivity::class.java)
                }

                intent.putExtra(ModelUtil.flowsheetDataSelectIntent, phrFlowsheet.toString)
                intent.putExtra(ModelUtil.flowsheetListSelectIntent, flowsheetDataListViewModel.flowsheet.toString)
                intent.putExtra(ModelUtil.practiceIdIntent  , flowsheetDataListViewModel.flowsheet.practiceId)

                if (phrFlowsheet.flowsheetEntries.firstOrNull()?.type == ModelUtil.flowsheetEntryTypeSwaasa){
                    intent.putExtra(ModelUtil.swaasaIsFlowsheetIntent , true)
                }
                startActivityForResult(intent , addFlowsheetReqId )
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        })


        binding.flowsheetDataListSwipeRefreshLayout.setOnRefreshListener {
            binding.flowsheetDataListSwipeRefreshLayout.isRefreshing = true
            flowsheetDataListViewModel.fetchFlowsheetData()
        }


        if (flowsheetDataListViewModel.flowsheet.allowEdit){

            binding.flowsheetDataListRV.addOnScrollListener (object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, scrollX: Int, scrollY: Int) {

                    if (scrollY > 0 && binding.flowsheetDataListAddFab.visibility == View.VISIBLE) {
                        binding.flowsheetDataListAddFab.hide()
                    } else if (scrollY < 0 && binding.flowsheetDataListAddFab.visibility != View.VISIBLE) {
                        binding.flowsheetDataListAddFab.show()
                    }
                }
            })

            binding.flowsheetDataListAddFab.visibility = View.VISIBLE
            binding.flowsheetDataListAddFab.setOnClickListener {

                val intent = Intent(this@FlowsheetDataActivity, AddFlowsheetDataActivity::class.java)
                intent.putExtra(ModelUtil.flowsheetListSelectIntent, flowsheetDataListViewModel.flowsheet.toString)
                intent.putExtra(ModelUtil.practiceIdIntent  , flowsheetDataListViewModel.flowsheet.practiceId)
                startActivityForResult(intent , addFlowsheetReqId )
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
        else{
            binding.flowsheetDataListAddFab.visibility = View.GONE
        }

        binding.flowsheetDataListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.flowsheetDataListErrorPage.root.visibility = View.GONE
            refreshData()
        }

        binding.flowsheetDataListBackBtn.setOnClickListener {
            onBackPressed()
        }

    }


    private fun refreshData() {
        flowsheetDataListViewModel.isFlowsheetDataFetched = false
        flowsheetDataListViewModel.isFlowsheetDataFetchComplete = false
        binding.flowsheetDataListProgress.root.visibility = View.VISIBLE
        flowsheetDataListViewModel.fetchFlowsheetData()
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

            val footerTreeObserver = binding.flowsheetDataListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        binding.flowsheetDataListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val flowsheetHeight = binding.flowsheetDataListRV.height ?: 0

                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $flowsheetHeight")

                        if (flowsheetHeight < viewHeight) {
                            binding.flowsheetDataListBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.flowsheetDataListBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            binding.flowsheetDataListBgLayout.visibility = View.GONE
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == addFlowsheetReqId){
            if (resultCode == Activity.RESULT_OK){
                hasFlowsheetDataChanged = true
                refreshData()
            }
        }
    }

    override fun onBackPressed() {
        if (hasFlowsheetDataChanged) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

}