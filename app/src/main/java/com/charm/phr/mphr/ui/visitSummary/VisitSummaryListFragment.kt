package com.charm.phr.mphr.ui.visitSummary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentVisitSummaryListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PatientEncounterList
import com.charm.phr.mphr.ui.visitSummary.visitSummaryDetail.VisitSummaryDetailActivity
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class VisitSummaryListFragment : DaggerFragment() {
    
    @Inject
    lateinit var visitSummaryListAdapter: VisitSummaryListAdapter

    private lateinit var visitSummaryViewModel: VisitSummaryViewModel

    private var _binding: FragmentVisitSummaryListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVisitSummaryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        visitSummaryViewModel = ViewModelProvider(requireActivity()).get(VisitSummaryViewModel::class.java)
        
        binding.visitSummaryListRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.visitSummaryListRV.adapter = visitSummaryListAdapter


        binding.visitSummaryListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        visitSummaryViewModel.visitSummaryListLive.observe(viewLifecycleOwner , Observer {  resp ->

            if (resp != null) {


                if (resp is Pair<*, *>) {
                    val visitSummaryPairList = resp as Pair<ArrayList<PatientEncounterList>, Boolean>

                    if (visitSummaryPairList.second) {

                        binding.visitSummaryListProgress.root.visibility = View.GONE

                        if (binding.visitSummaryListSwipeRefreshLayout.isRefreshing){
                            binding.visitSummaryListSwipeRefreshLayout.isRefreshing = false
                        }

                        val size = visitSummaryListAdapter.setVisitSummaryList(visitSummaryPairList.first)
                        if (size == 0) {
                            binding.visitSummaryListNoDataTV.root.visibility = View.VISIBLE
                            binding.visitSummaryListRV.visibility = View.GONE
                        } else {
                            binding.visitSummaryListNoDataTV.root.visibility = View.GONE
                            binding.visitSummaryListRV.visibility = View.VISIBLE
                            checkToShowBgImage()
                        }
                    } else {
                        visitSummaryListAdapter.removeInfiniteScroll()
                        visitSummaryListAdapter.addVisitSummaryList(visitSummaryPairList.first)
                    }
                } else if (resp is APIResponseError) {
                    if (!visitSummaryViewModel.isvisitSummaryListFetched) {
                        binding.visitSummaryListNoDataTV.root.visibility = View.GONE
                        binding.visitSummaryListRV.visibility = View.GONE
                        binding.visitSummaryListErrorPage.root.visibility = View.VISIBLE
                    }
                }

                visitSummaryViewModel.isVisitSummaryFetchComplete = true
            }
        })


        if (!visitSummaryViewModel.isvisitSummaryListFetched){
            visitSummaryViewModel.fetchVisitSummaryList( true)
        }

        binding.visitSummaryListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastIndexInScreen = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                if (lastIndexInScreen >=  visitSummaryListAdapter.itemCount - 5 &&  visitSummaryListAdapter.itemCount != 0 && visitSummaryViewModel.isVisitSummaryFetchComplete && visitSummaryViewModel.visitSummaryHasMore) {
                    visitSummaryViewModel.isVisitSummaryFetchComplete = false
                    visitSummaryViewModel.fetchVisitSummaryList(false)
                    visitSummaryListAdapter.addInfiniteScroll()

                }
            }
        })

        visitSummaryListAdapter.setVisitSummarySelectListener(object : VisitSummaryListAdapter.VisitSummarySelectListener{
            override fun onVisitSummarySelect(visitSummary: PatientEncounterList) {

                val intent = Intent(this@VisitSummaryListFragment.context , VisitSummaryDetailActivity::class.java)
                intent.putExtra(ModelUtil.visitSummaryDocumentIDIntent, visitSummary.patientEncounterId);
                /* if (!visitSummary.fileId.isNullOrBlank()) {
                     intent.putExtra(ModelUtil.visitSummaryDocumentIDIntent, visitSummary.patientEncounterId)
                 }
                 else{
                     intent.putExtra(ModelUtil.visitSummaryDocumentIDIntent, "")
                 }*/
                intent.putExtra(ModelUtil.practiceIdIntent , visitSummary.practiceId)
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        })

        binding.visitSummaryListSwipeRefreshLayout.setOnRefreshListener {

            binding.visitSummaryListSwipeRefreshLayout.isRefreshing = true
            visitSummaryViewModel.fetchVisitSummaryList( true)
        }

        binding.visitSummaryListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.visitSummaryListErrorPage.root.visibility = View.GONE
            binding.visitSummaryListProgress.root.visibility = View.VISIBLE
            visitSummaryViewModel.isVisitSummaryFetchComplete = false
            visitSummaryViewModel.isvisitSummaryListFetched = false
            visitSummaryViewModel.fetchVisitSummaryList( true)
        }
    }

    private fun checkToShowBgImage() {

        if (!BuildUtil.isCustomApp) {
            val height = UIUtil.getScreenHeight(this.activity)

            val tv = TypedValue()
            var actionBarHeight = 0
            if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            }

            val viewHeight = ((height - actionBarHeight) * 2) / 3

            val footerTreeObserver = binding.visitSummaryListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        binding.visitSummaryListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val supplementHeight = binding.visitSummaryListRV.height ?: 0

                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $supplementHeight")

                        if (supplementHeight < viewHeight) {
                            binding.visitSummaryListBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.visitSummaryListBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        }
        else{
            binding.visitSummaryListBgLayout.visibility = View.GONE
        }

    }

}
