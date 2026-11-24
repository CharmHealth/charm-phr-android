package com.charm.phr.mphr.ui.billing

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityBillingBinding
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class BillingActivity : MDBaseActivity() {

    @Inject
    lateinit var billingViewPagerAdapter: BillingViewPagerAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var billingViewModel: BillingViewModel

    private lateinit var binding: ActivityBillingBinding

    private var billingSubTitles: ArrayList<String> = arrayListOf()

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            billingViewModel.isUpdateCountCallBackSent = true
            billingViewPagerAdapter.refreshData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBillingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        billingViewModel = ViewModelProvider(this, viewModelFactory)[BillingViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.billingToolbar)
        UIUtil.applyViewInsertForBottomView(binding.billingMainLayout)

        billingSubTitles = arrayListOf(getString(R.string.billing_invoices_str), getString(R.string.billing_receipts_str))
        binding.billingViewPager.offscreenPageLimit = ModelUtil.viewPagerOffScreenPageLimit
        binding.billingViewPager.adapter = billingViewPagerAdapter
        TabLayoutMediator(binding.billingTabLayout, binding.billingViewPager) { tab, position ->
            tab.text = billingSubTitles[position]
        }.attach()

        binding.billingBackBtn.setOnClickListener {
            onBackPressed()
        }


        //TODO: Billing pay integration to be done

    }

    override fun onBackPressed() {
        if (billingViewModel.isUpdateCountCallBackSent) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
