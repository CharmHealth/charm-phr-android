package com.charm.phr.mphr.ui.lab

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentLabOrderListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.LabOrderDetail
import com.charm.phr.mphr.ui.lab.detail_pdf.LabDetailPDFActivity
import com.charm.phr.mphr.util.ModelUtil
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class LabOrderListFragment : DaggerFragment() {

    lateinit var labViewModel: LabViewModel

    @Inject
    lateinit var labOrderListAdapter: LabOrderListAdapter

    lateinit var binding: FragmentLabOrderListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLabOrderListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        labViewModel = ViewModelProvider(requireActivity())[LabViewModel::class.java]

        binding.labOrderListRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.labOrderListRV.adapter = labOrderListAdapter

        binding.labOrderListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        labViewModel.labOrderListLive.observe(viewLifecycleOwner) { resp ->

            if (resp != null) {

                if (resp is Pair<*, *>) {
                    val labOrderPairList = resp as Pair<ArrayList<LabOrderDetail>, Boolean>

                    if (labOrderPairList.second) {

                        binding.labOrderListProgress.root.visibility = View.GONE

                        if (binding.labOrderListSwipeRefreshLayout.isRefreshing) {
                            binding.labOrderListSwipeRefreshLayout.isRefreshing = false
                        }

                        val size = labOrderListAdapter.setLabOrderList(labOrderPairList.first)
                        if (size == 0) {
                            binding.labOrderListNoDataTV.root.visibility = View.VISIBLE
                            binding.labOrderListRV.visibility = View.GONE
                        } else {
                            binding.labOrderListNoDataTV.root.visibility = View.GONE
                            binding.labOrderListRV.visibility = View.VISIBLE
                        }
                    } else {
                        labOrderListAdapter.removeInfiniteScroll()
                        labOrderListAdapter.addLabOrderList(labOrderPairList.first)
                    }
                } else if (resp is APIResponseError) {
                    if (!labViewModel.islabOrderListFetched) {
                        binding.labOrderListNoDataTV.root.visibility = View.VISIBLE
                        binding.labOrderListRV.visibility = View.GONE
                    }
                }

                labViewModel.islabOrderFetchComplete = true
            }
        }


        if (!labViewModel.islabOrderListFetched) {
            labViewModel.fetchLabOrderList(true)
        }

        binding.labOrderListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastIndexInScreen = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                if (lastIndexInScreen >= labOrderListAdapter.itemCount - 5 && labOrderListAdapter.itemCount != 0 && labViewModel.islabOrderFetchComplete && labViewModel.labOrderHasMore) {
                    labViewModel.islabOrderFetchComplete = false
                    labViewModel.fetchLabOrderList(false)
                    labOrderListAdapter.addInfiniteScroll()

                }
            }
        })

        labOrderListAdapter.setLabOrderClickListener(object : LabOrderListAdapter.LabOrderClickListener {
            override fun onLabOrderClick(labOrderDetail: LabOrderDetail) {
                val intent = Intent(this@LabOrderListFragment.activity, LabDetailPDFActivity::class.java)
                intent.putExtra(ModelUtil.labOrderId, labOrderDetail.fileId)
                intent.putExtra(ModelUtil.labIsOrder, true)
                intent.putExtra(ModelUtil.labOrderNameIntent, labOrderDetail.fileName)
                startActivity(intent)
            }
        })

        binding.labOrderListSwipeRefreshLayout.setOnRefreshListener {

            binding.labOrderListSwipeRefreshLayout.isRefreshing = true
            labViewModel.fetchLabOrderList(true)

        }
    }

}
