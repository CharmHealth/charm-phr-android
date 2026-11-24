package com.charm.phr.mphr.ui.wellness.wellnessList

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityWellnessListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Module
import com.charm.phr.mphr.model.TrackerMetricMap
import com.charm.phr.mphr.ui.wellness.wellnessDetail.WellnessDetailActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

class WellnessListActivity : MDBaseActivity() {

    @Inject
    lateinit var wellnessDataListAdapter: WellnessDataListAdapter

    lateinit var wellnessListViewModel: WellnessListViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var binding: ActivityWellnessListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWellnessListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wellnessListViewModel = ViewModelProvider(this, viewModelFactory).get(WellnessListViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.wellnessListToolbar)
        UIUtil.applyViewInsertForBottomView(binding.wellnessListMainLayout)

        wellnessDataListAdapter.setModuleSelectListener(object : WellnessDataListAdapter.ModuleSelectListener {
            override fun onModuleSelect(module: Module, position: Int) {

//                if(module.moduleId.equals(ModelUtil.syncWithGoogleFit))
//                {
//                    val intent = Intent(this@WellnessListActivity , GoogleFitSyncActivity::class.java)
//                    intent.putExtra(ModelUtil.wellnessTrackerDetailIntent ,wellnessListViewModel.wellnessTrackerListStr)
//                    startActivity(intent)
//                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//                }
//                else
//                {
                    val moduleId = module.moduleId
                    val trackersplit = module.moduleServerId?.split(",")
                    val intent = Intent(this@WellnessListActivity , WellnessDetailActivity::class.java)


                    val trackerId = trackersplit?.get(0) ?: ""
                    var metricId = ""

                    if (trackersplit != null && trackersplit.size > 1) {
                        metricId = trackersplit[1]
                    }

                    if (position != 2) {

                        if (position == 1) {
                            intent.putExtra(ModelUtil.wellnessTrackerIntent, ModelUtil.wellnessOthersModelId)

                            if (!module.moduleNameFromSection.isNullOrBlank()) {
                                intent.putExtra(ModelUtil.wellnessMetricIntent, module.moduleNameFromSection)
                            } else {
                                intent.putExtra(ModelUtil.wellnessMetricIntent, moduleId)
                            }
                        } else {
                            intent.putExtra(ModelUtil.wellnessTrackerIntent, moduleId)
                        }

                        intent.putExtra(ModelUtil.wellnessIsDefaultIntent, true)
                    }



                    intent.putExtra(ModelUtil.wellnessTrackerIdIntent, trackerId)
                    intent.putExtra(ModelUtil.wellnessMetricIdIntent, metricId)


                    val vitalMetricMap = wellnessDataListAdapter.trackerMetricMapList.firstOrNull { it.tracker?.trackerId == trackerId }
                    if (vitalMetricMap != null) {
                        intent.putExtra(ModelUtil.wellnessTrackerDetailIntent, vitalMetricMap.toString)
                    }

                    startActivityForResult(intent, 0)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//                }
            }
        })

        binding.wellnessListRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.wellnessListRV.adapter = wellnessDataListAdapter


        binding.wellnessListBackBtn.setOnClickListener {
            onBackPressed()
        }


        if (!wellnessListViewModel.isWellnessTrackerListFetched) {
            getWellnessTrackerList()
        }


        binding.wellnessListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            getWellnessTrackerList()
        }


        wellnessListViewModel.wellnessTrackerListLive.observe(this, Observer { resp ->

            if (resp != null) {
                binding.wellnessListProgress.root.visibility = View.GONE
                if (resp is Pair<*, *>) {
                    val responseList=resp.first as ArrayList<TrackerMetricMap>
                    wellnessListViewModel.wellnessTrackerListStr=resp.second as String
                    val size = wellnessDataListAdapter.setWellnessData(responseList)

                    if (size == 0) {
                        binding.wellnessListRV.visibility = View.GONE
                        binding.wellnessListNoDataLayout.root.visibility = View.VISIBLE
                    } else {
                        binding.wellnessListRV.visibility = View.VISIBLE
                        binding.wellnessListNoDataLayout.root.visibility = View.GONE
                    }
                    wellnessListViewModel.isWellnessTrackerListFetched = true
                } else if (resp is APIResponseError) {
                    binding.wellnessListErrorPage.root.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun getWellnessTrackerList() {

        binding.wellnessListRV.visibility = View.GONE
        binding.wellnessListProgress.root.visibility = View.VISIBLE
        binding.wellnessListNoDataLayout.root.visibility = View.GONE
        binding.wellnessListErrorPage.root.visibility = View.GONE
        wellnessListViewModel.getWellnessTrackerList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                wellnessListViewModel.isDashboardVitalsUpdated = true
            }
        }
    }

    override fun onBackPressed() {
        if (wellnessListViewModel.isDashboardVitalsUpdated) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}