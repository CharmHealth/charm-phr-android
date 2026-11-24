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
import com.charm.phr.mphr.databinding.FragmentInvoiceBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PHRBills
import com.charm.phr.mphr.ui.billing.billingDetail.BillingDetailActivity
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RefreshDataListener
import com.charm.phr.mphr.util.UIUtil
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class InvoiceFragment : DaggerFragment(), RefreshDataListener {


    @Inject
    lateinit var invoiceListAdapter: InvoiceListAdapter

    lateinit var billingViewModel: BillingViewModel

    private lateinit var binding: FragmentInvoiceBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInvoiceBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        billingViewModel = ViewModelProvider(requireActivity())[BillingViewModel::class.java]

        binding.invoiceListRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.invoiceListRV.adapter = invoiceListAdapter

        binding.invoiceListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)



        binding.invoiceListErrorPageLayout.root.visibility = View.GONE
        billingViewModel.invoiceListLive.observe(viewLifecycleOwner) { resp ->

            if (resp != null) {

                if (resp is Pair<*, *>) {
                    val invoicePairList = resp as Pair<ArrayList<PHRBills>, Boolean>

                    if (invoicePairList.second) {

                        binding.invoiceListProgress.root.visibility = View.GONE

                        if (binding.invoiceListSwipeRefreshLayout.isRefreshing) {
                            binding.invoiceListSwipeRefreshLayout.isRefreshing = false
                        }

                        val size = invoiceListAdapter.setInvoiceList(invoicePairList.first)
                        if (size == 0) {
                            binding.invoiceListNoDataTV.root.visibility = View.VISIBLE
                            binding.invoiceListRV.visibility = View.GONE
                        } else {
                            binding.invoiceListNoDataTV.root.visibility = View.GONE
                            binding.invoiceListRV.visibility = View.VISIBLE

                            checkToShowBgImage()
                        }
                    } else {
                        invoiceListAdapter.removeInfiniteScroll()
                        invoiceListAdapter.addInvoiceList(invoicePairList.first)
                    }
                } else if (resp is APIResponseError) {
                    if (!billingViewModel.isinvoiceListFetched) {
                        binding.invoiceListNoDataTV.root.visibility = View.GONE
                        binding.invoiceListRV.visibility = View.GONE
                        binding.invoiceListErrorPageLayout.root.visibility = View.VISIBLE
                    }
                }

                billingViewModel.isInvoiceFetchComplete = true
            }
        }


        if (!billingViewModel.isinvoiceListFetched) {
            billingViewModel.fetchInvoiceList(true)
        }

        binding.invoiceListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastIndexInScreen = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                if (lastIndexInScreen >= invoiceListAdapter.itemCount - 5 && invoiceListAdapter.itemCount != 0 && billingViewModel.isInvoiceFetchComplete && billingViewModel.invoiceHasMore) {
                    billingViewModel.isInvoiceFetchComplete = false
                    billingViewModel.fetchInvoiceList(false)
                    invoiceListAdapter.addInfiniteScroll()

                }
            }
        })

        invoiceListAdapter.setInvoiceSelectListener(object : InvoiceListAdapter.InvoiceSelectListener {
            override fun onInvoiceSelect(phrBill: PHRBills) {
                val intent = Intent(context, BillingDetailActivity::class.java)
                intent.putExtra(ModelUtil.billingType, ModelUtil.billingTypeInvoice)

                intent.putExtra(ModelUtil.billingIsStatementIntent, phrBill.isStatement)
                intent.putExtra(
                    ModelUtil.phrBillIdIntent, if (phrBill.isStatement) {
                        phrBill.statementId
                    } else {

                        phrBill.phrBillId
                    }
                )
                if (ModelUtil.isAmountValid(phrBill.amount) && phrBill.isPaid == false) {
                    val pair = ModelUtil.getDueAndPaymentRequested(phrBill)
                    val paymentRequested = pair.second
                    intent.putExtra(ModelUtil.billingIsPaymentRequested, true)
                    intent.putExtra(ModelUtil.billingDate, phrBill.billDate?.toString(ModelUtil.charmDisplayDateFormat()))
                    intent.putExtra(ModelUtil.billingFacilityName, phrBill.facility?.facilityName)
                    intent.putExtra(ModelUtil.billingPaymentReqAmt, paymentRequested)
                    intent.putExtra(ModelUtil.billingPaymentReqCurrency, phrBill.practiceCurrencySymbol)
                    intent.putExtra(ModelUtil.billingInvoiceNum, phrBill.billNumber)
                    intent.putExtra(ModelUtil.billingPracticeIdIntent, phrBill.practiceId)
                    intent.putExtra(ModelUtil.billingFacilityIdIntent, phrBill.facility?.facilityId)
                    intent.putExtra(ModelUtil.billingPaymentGatewayIntent, phrBill.paymentGateway)
                    intent.putExtra(ModelUtil.billingMemberIdIntent, phrBill.memberId ?: "")
                }
                if (requireActivity() is BillingActivity) {
                    val billingActivity = requireActivity() as BillingActivity
                    billingActivity.resultLauncher.launch(intent)
                }
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            }

        })

        binding.invoiceListSwipeRefreshLayout.setOnRefreshListener {
            binding.invoiceListSwipeRefreshLayout.isRefreshing = true
            billingViewModel.fetchInvoiceList(true)
        }

        binding.invoiceListErrorPageLayout.errorPageTryAgainBtn.setOnClickListener {

            billingViewModel.isinvoiceListFetched = false
            billingViewModel.isInvoiceFetchComplete = false
            binding.invoiceListErrorPageLayout.root.visibility = View.GONE
            binding.invoiceListProgress.root.visibility = View.VISIBLE
            binding.invoiceListRV.visibility = View.GONE
            billingViewModel.fetchInvoiceList(true)
        }


    }

    override fun refreshData() {
        billingViewModel.isInvoiceFetchComplete = false
        billingViewModel.isinvoiceListFetched = false
        binding.invoiceListProgress.root.visibility = View.VISIBLE
        binding.invoiceListRV.visibility = View.GONE
        billingViewModel.fetchInvoiceList(true)
    }


    private fun checkToShowBgImage() {

        if (!BuildUtil.isCustomApp) {

            val height = UIUtil.getScreenHeight(this.activity)

            val tv = TypedValue()
            var actionBarHeight = 0
            if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            }

            val viewHeight = ((height - (2 * actionBarHeight)) * 3) / 5

            val footerTreeObserver =binding.invoiceListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        binding.invoiceListRV.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val medicationHeight = binding.invoiceListRV.height

                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $medicationHeight")

                        if (medicationHeight < viewHeight) {
                            binding.invoiceBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.invoiceBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            binding.invoiceBgLayout.visibility = View.GONE
        }
    }


}
