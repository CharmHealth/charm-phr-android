package com.charm.phr.mphr.ui.insurance

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityInsuranceListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PatientInsuranceDetails
import com.charm.phr.mphr.ui.insurance.add.AddInsuranceActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.OnMultipleSelectChangeListener
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.UIUtil.Companion.hideView
import com.charm.phr.mphr.util.applyCloseTransition
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.applyOpenTransition
import javax.inject.Inject

class InsuranceListActivity : MDBaseActivity() {

    lateinit var insuranceListViewModel: InsuranceListViewModel

    @Inject
    lateinit var viewModelProviderFactory: ViewModelProvider.Factory


    @Inject
    lateinit var insuranceListAdapter: InsuranceListAdapter

    private val addOrEditLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            fetchInsurance()
        }
    }

    lateinit var binding: ActivityInsuranceListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsuranceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        insuranceListViewModel = ViewModelProvider(this, viewModelProviderFactory)[InsuranceListViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.insuranceListToolbar)
        UIUtil.applyViewInsertForBottomView(binding.insuranceMainLayout)

        binding.insuranceListRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.insuranceListRV.adapter = insuranceListAdapter

        insuranceListAdapter.setOnMultipleSelectChangeListener(object :
                OnMultipleSelectChangeListener {
            override fun onMultiSelect(isMultiSelectEnabled: Boolean) {
                onMultiSelectChange(isMultiSelectEnabled)
            }
        })

        insuranceListAdapter.setInsuranceSelectListener(object :
                InsuranceListAdapter.InsuranceSelectListener {
            override fun onEditInsurance(insurance: PatientInsuranceDetails) {

                val intent = Intent(this@InsuranceListActivity, AddInsuranceActivity::class.java)
                intent.putExtra(ModelUtil.insuranceToEditIntent, true)
                intent.putExtra(ModelUtil.insuranceEditDataIntent, insurance.toString)
                addOrEditLauncher.launch(intent)
                applyOpenTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onDeleteInsurance(insurance: PatientInsuranceDetails, position: Int) {
                deleteInsurance(insurance, position)
            }
        })
        if(!RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_INSURANCE,this.applicationContext))
        {
            binding.insuranceListAddFab.hideView()
        }
        insuranceListViewModel.insuranceListLive.observe(this) { resp ->

            if (resp != null) {
                if (resp is Pair<*, *>) {
                    val insurancePairList = resp as Pair<ArrayList<PatientInsuranceDetails>, Boolean>

                    if (insurancePairList.second) {

                        binding.insuranceListProgress.root.visibility = View.GONE


                        if (binding.insuranceListSwipeRefreshLayout.isRefreshing) {
                            binding.insuranceListSwipeRefreshLayout.isRefreshing = false
                        }

                        val size = insuranceListAdapter.setInsurances(insurancePairList.first)
                        if (size == 0) {
                            binding.insuranceListNoDataTV.root.visibility = View.VISIBLE
                            binding.insuranceListRV.visibility = View.GONE
                        } else {
                            binding.insuranceListNoDataTV.root.visibility = View.GONE
                            binding.insuranceListRV.visibility = View.VISIBLE
                            checkToShowBgIcon()
                        }
                    } else {

                        insuranceListAdapter.removeInfiniteScroll()
                        insuranceListAdapter.addInsurances(insurancePairList.first)
                    }
                } else if (resp is APIResponseError) {
                    if (!insuranceListViewModel.isInsuranceListFetched) {
                        binding.insuranceListNoDataTV.root.visibility = View.GONE
                        binding.insuranceListRV.visibility = View.GONE
                        binding.insuranceListErrorPage.root.visibility = View.VISIBLE
                        binding.insuranceListProgress.root.visibility = View.GONE
                    }
                }

                insuranceListViewModel.isInsuranceFetchComplete = true
            }
        }


        if (!insuranceListViewModel.isInsuranceListFetched) {
            fetchInsurance()
        }

        binding.insuranceListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, scrollX: Int, scrollY: Int) {
                val lastIndexInScreen =
                        (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()


                Log.d("insuranceListItemCount", insuranceListAdapter.itemCount.toString())
                Log.d(
                        "insuranceIbListComplete",
                        insuranceListViewModel.isInsuranceFetchComplete.toString()
                )

                if ((lastIndexInScreen + 1) >= insuranceListAdapter.itemCount - 5 && insuranceListAdapter.itemCount != 0 && insuranceListViewModel.isInsuranceFetchComplete && insuranceListViewModel.insuranceHasMore) {
                    Log.d("scroll", "now")

                    insuranceListViewModel.isInsuranceFetchComplete = false
                    insuranceListAdapter.addInfiniteScroll()
                    insuranceListViewModel.fetchInsuranceList(false)

                }

                if (ModelUtil.isPHREntryEnabled(authManager, ModelUtil.insuranceModelId) ) {
                    if (scrollY > 0 && binding.insuranceListAddFab.visibility == View.VISIBLE) {
                        binding.insuranceListAddFab.hide()
                    }
                    else if (scrollY < 0 && binding.insuranceListAddFab.visibility != View.VISIBLE && RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_INSURANCE,applicationContext)) {
                        binding.insuranceListAddFab.show()
                    }
                }
            }
        })


        binding.insuranceListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        binding.insuranceListSwipeRefreshLayout.setOnRefreshListener {
            binding.insuranceListSwipeRefreshLayout.isRefreshing = true
            insuranceListViewModel.fetchInsuranceList(true)
        }

        binding.insuranceListBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (ModelUtil.isPHREntryEnabled(authManager, ModelUtil.insuranceModelId) ) {

            binding.insuranceListNoDataTV.nodataTextViewHint.visibility = View.VISIBLE
            binding.insuranceListNoDataTV.nodataTextViewHint.setText(R.string.insurance_add_hint)
            UIUtil.makePlusColorPrimary(binding.insuranceListNoDataTV.nodataTextViewHint)
            if(RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_INSURANCE,this.applicationContext))
                binding.insuranceListAddFab.show()
            binding.insuranceListAddFab.setOnClickListener {

                val intent = Intent(this, AddInsuranceActivity::class.java)
                addOrEditLauncher.launch(intent)
                applyOpenTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        } else {
            binding.insuranceListAddFab.hide()
        }

        binding.insuranceListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.insuranceListErrorPage.root.visibility = View.GONE
            fetchInsurance()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                applyCloseTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        })
    }


    fun onMultiSelectChange(isEnabled: Boolean) {

        if (isEnabled) {
            binding.insuranceListDeleteBtn.visibility = View.VISIBLE
        } else {
            binding.insuranceListDeleteBtn.visibility = View.GONE
        }
    }

    private fun fetchInsurance() {
        insuranceListViewModel.isInsuranceFetchComplete = false
        insuranceListViewModel.isInsuranceListFetched = false
        binding.insuranceListProgress.root.visibility = View.VISIBLE
        binding.insuranceListNoDataTV.root.visibility = View.GONE
        binding.insuranceListRV.visibility = View.GONE
        insuranceListViewModel.fetchInsuranceList(true)

    }

    fun deleteInsurance(insurance: PatientInsuranceDetails, position: Int) {
        val builder = AlertDialog.Builder(this)

        builder.setMessage(R.string.insurance_delete_confirmation_msg)

        builder.setPositiveButton(R.string.yes_str) { _, _ ->
            progressDialog.show()
            insuranceListViewModel.deleteInsurance(insurance).observe(this) { resp ->
                if (resp != null) {
                    progressDialog.dismiss()
                    if (resp is Boolean) {
                        val size = insuranceListAdapter.removeInsurance(position)
                        if (size == 0) {
                            binding.insuranceListNoDataTV.root.visibility = View.VISIBLE
                            binding.insuranceListRV.visibility = View.GONE
                        } else {
                            binding.insuranceListNoDataTV.root.visibility = View.GONE
                            binding.insuranceListRV.visibility = View.VISIBLE
                            checkToShowBgIcon()
                        }


                        UIUtil.showToast(this, R.string.insurance_delete_success_msg, Toast.LENGTH_LONG)
                    } else if (resp is APIResponseError) {
                        UIUtil.showToast(this, R.string.error_insurance_delete__msg, Toast.LENGTH_LONG)
                    }
                }
            }
        }
        builder.setNegativeButton(R.string.no_str) { _, _ ->

        }

        builder.show()

    }

    private fun checkToShowBgIcon() {
        if (!BuildUtil.isCustomApp) {
            val height = UIUtil.getScreenHeight(this)

            val tv = TypedValue()
            var actionBarHeight = 0
            if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            }

            val viewHeight = ((height - actionBarHeight) * 2) / 3

            val footerTreeObserver = binding.insuranceListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {
                        binding.insuranceListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                        val insuranceHeight = binding.insuranceListRV.height
                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $insuranceHeight")

                        if (insuranceHeight < viewHeight) {
                            binding.insuranceListHPBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.insuranceListHPBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            binding.insuranceListHPBgLayout.visibility = View.GONE
        }
    }
}
