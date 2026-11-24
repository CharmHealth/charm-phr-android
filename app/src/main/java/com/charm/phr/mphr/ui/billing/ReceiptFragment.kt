package com.charm.phr.mphr.ui.billing

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentReceiptBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PHRReceipts
import com.charm.phr.mphr.ui.billing.billingDetail.BillingDetailActivity
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RefreshDataListener
import com.charm.phr.mphr.util.UIUtil
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ReceiptFragment : DaggerFragment(), RefreshDataListener {

    @Inject
    lateinit var receiptListAdapter: ReceiptListAdapter

    lateinit var billingViewModel: BillingViewModel

    lateinit var binding: FragmentReceiptBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReceiptBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        billingViewModel = ViewModelProvider(requireActivity())[BillingViewModel::class.java]

        binding.receiptListRV.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.receiptListRV.adapter = receiptListAdapter

        binding.receiptListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        billingViewModel.receiptListLive.observe(viewLifecycleOwner) { resp ->

            if (resp != null) {

                if (resp is Pair<*, *>) {
                    val receiptPairList = resp as Pair<ArrayList<PHRReceipts>, Boolean>

                    if (receiptPairList.second) {

                        if (binding.receiptListSwipeRefreshLayout.isRefreshing) {
                            binding.receiptListSwipeRefreshLayout.isRefreshing = false
                        }

                        binding.receiptListProgress.root.visibility = View.GONE
                        val size = receiptListAdapter.setReceiptList(receiptPairList.first)
                        if (size == 0) {
                            binding.receiptListNoDataTV.root.visibility = View.VISIBLE
                            binding.receiptListRV.visibility = View.GONE
                        } else {
                            binding.receiptListNoDataTV.root.visibility = View.GONE
                            binding.receiptListRV.visibility = View.VISIBLE
                            checkToShowBgImage()
                        }
                    } else {
                        receiptListAdapter.removeInfiniteScroll()
                        receiptListAdapter.addReceiptList(receiptPairList.first)
                    }
                } else if (resp is APIResponseError) {
                    if (!billingViewModel.isreceiptListFetched) {
                        binding.receiptListNoDataTV.root.visibility = View.GONE
                        binding.receiptListRV.visibility = View.GONE
                        binding.receiptListErrorPageLayout.root.visibility = View.VISIBLE
                    }
                }

                billingViewModel.isReceiptFetchComplete = true
            }
        }


        if (!billingViewModel.isreceiptListFetched) {
            billingViewModel.fetchReceiptsList(true)
        }

        binding.receiptListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastIndexInScreen =
                    (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                if (lastIndexInScreen >= receiptListAdapter.itemCount - 5 && receiptListAdapter.itemCount != 0 && billingViewModel.isReceiptFetchComplete && billingViewModel.receiptHasMore) {
                    Log.d("scroll", "now")
                    billingViewModel.isReceiptFetchComplete = false
                    billingViewModel.fetchReceiptsList(false)

                    receiptListAdapter.addInfiniteScroll()

                }
            }
        })

        receiptListAdapter.setReceiptSelectListener(object :
            ReceiptListAdapter.ReceiptSelectListener {
            override fun onReceiptSelect(phrReceipts: PHRReceipts) {
                billingViewModel.isUpdateCountCallBackSent = true
                val intent = Intent(context, BillingDetailActivity::class.java)
                intent.putExtra(ModelUtil.billingType, ModelUtil.billingTypeReceipt)
                intent.putExtra(ModelUtil.phrReceiptIdIntent, phrReceipts.phrReceiptId)
                startActivity(intent)
                requireActivity().overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )

            }
        })


        binding.receiptListSwipeRefreshLayout.setOnRefreshListener {
            binding.receiptListSwipeRefreshLayout.isRefreshing = true
            billingViewModel.fetchReceiptsList(true)
        }

        binding.receiptListErrorPageLayout.errorPageTryAgainBtn.setOnClickListener {

            billingViewModel.isreceiptListFetched = false
            billingViewModel.isReceiptFetchComplete = false
            binding.receiptListErrorPageLayout.root.visibility = View.GONE
            binding.receiptListProgress.root.visibility = View.VISIBLE
            binding.receiptListRV.visibility = View.GONE
            billingViewModel.fetchReceiptsList(true)
        }

    }

    override fun refreshData() {
        billingViewModel.isreceiptListFetched = false
        billingViewModel.isReceiptFetchComplete = false
        binding.receiptListProgress.root.visibility = View.VISIBLE
        binding.receiptListRV.visibility = View.GONE
        billingViewModel.fetchReceiptsList(true)
    }

    private fun checkToShowBgImage() {

        if (!BuildUtil.isCustomApp) {

            val height = UIUtil.getScreenHeight(this.activity)

            val tv = TypedValue()
            var actionBarHeight = 0
            if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight =
                    TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            }

            val viewHeight = ((height - (2 * actionBarHeight)) * 3) / 5

            val footerTreeObserver = binding.receiptListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        binding.receiptListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val medicationHeight = binding.receiptListRV.height

                        Log.d(
                            "MDHPBgHeights",
                            "viewHeight -> $viewHeight ; rvHeight -> $medicationHeight"
                        )

                        if (medicationHeight < viewHeight) {
                            binding.receiptListBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.receiptListBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            binding.receiptListBgLayout.visibility = View.GONE
        }
    }

}
