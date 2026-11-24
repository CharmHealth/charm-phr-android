package com.charm.phr.mphr.ui.lab

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
import com.charm.phr.mphr.databinding.FragmentLabResultListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.MedicalRecordGroup
import com.charm.phr.mphr.ui.lab.resultDetail.LabResultDetailActivity
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class LabResultListFragment : DaggerFragment() {

    lateinit var labViewModel: LabViewModel

    @Inject
    lateinit var labResultListAdapter: LabResultListAdapter

    private var _binding: FragmentLabResultListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using ViewBinding
        _binding = FragmentLabResultListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        labViewModel = ViewModelProvider(requireActivity()).get(LabViewModel::class.java)

        binding.labResultListRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.labResultListRV.adapter = labResultListAdapter

        binding.labResultListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        labViewModel.labResultListLive.observe(viewLifecycleOwner, Observer { resp ->

            if (resp != null) {

                if (resp is Pair<*, *>) {
                    val labResultPairList = resp as Pair<ArrayList<MedicalRecordGroup>, Boolean>

                    if (labResultPairList.second) {

                        binding.labResultListProgress.root.visibility = View.GONE

                        if (binding.labResultListSwipeRefreshLayout.isRefreshing) {
                            binding.labResultListSwipeRefreshLayout.isRefreshing = false
                        }

                        val size = labResultListAdapter.setLabResultList(labResultPairList.first)
                        if (size == 0) {
                            binding.labResultListNoDataTV.root.visibility = View.VISIBLE
                            binding.labResultListRV.visibility = View.GONE
                        } else {
                            binding.labResultListNoDataTV.root.visibility = View.GONE
                            binding.labResultListRV.visibility = View.VISIBLE
                            checkToShowBgImage()
                        }
                    } else {
                        labResultListAdapter.removeInfiniteScroll()
                        labResultListAdapter.addLabResultList(labResultPairList.first)
                    }
                } else if (resp is APIResponseError) {
                    if (!labViewModel.islabResultListFetched) {
                        binding.labResultListNoDataTV.root.visibility = View.GONE
                        binding.labResultListRV.visibility = View.GONE
                        binding.labResultListErrorPage.root.visibility = View.VISIBLE
                    }
                }

                labViewModel.islabResultFetchComplete = true
            }
        })


        if (!labViewModel.islabResultListFetched) {
            labViewModel.fetchLabResultList(true)
        }

        binding.labResultListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastIndexInScreen = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                if (lastIndexInScreen >= labResultListAdapter.itemCount - 5 && labResultListAdapter.itemCount != 0 && labViewModel.islabResultFetchComplete && labViewModel.labResultHasMore) {
                    labViewModel.islabResultFetchComplete = false
                    labViewModel.fetchLabResultList(false)
                    labResultListAdapter.addInfiniteScroll()

                }
            }
        })

        labResultListAdapter.setLabResultClickListener(object : LabResultListAdapter.LabResultClickListener {
            override fun onLabResultClick(medicalRecordGroup: MedicalRecordGroup) {
                val intent = Intent(this@LabResultListFragment.activity, LabResultDetailActivity::class.java)
                intent.putExtra(ModelUtil.labResultGroupId, medicalRecordGroup.groupId)
                intent.putExtra(ModelUtil.labResultLabNameIntent, medicalRecordGroup.labName)
                intent.putExtra(ModelUtil.practiceIdIntent, medicalRecordGroup.practiceId)
                intent.putExtra(ModelUtil.interpretation, medicalRecordGroup.medicalRecordEntryList.firstOrNull()?.interpretation)

                startActivity(intent)
                activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            }
        })

        binding.labResultListSwipeRefreshLayout.setOnRefreshListener {

            binding.labResultListSwipeRefreshLayout.isRefreshing = true
            labViewModel.fetchLabResultList(true)

        }

        binding.labResultListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.labResultListErrorPage.root.visibility = View.GONE
            binding.labResultListProgress.root.visibility = View.VISIBLE
            labViewModel.islabResultListFetched = false
            labViewModel.islabResultFetchComplete = false
            labViewModel.fetchLabResultList(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

            val footerTreeObserver = binding.labResultListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        binding.labResultListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val supplementHeight = binding.labResultListRV.height ?: 0

                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $supplementHeight")

                        if (supplementHeight < viewHeight) {
                            binding.labResultListBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.labResultListBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            binding.labResultListBgLayout.visibility = View.GONE
        }

    }

}
