package com.charm.phr.mphr.ui.lab

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityLabBinding
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class LabActivity : MDBaseActivity() {

    private lateinit var binding: ActivityLabBinding

    @Inject
    lateinit var labViewPagerAdapter: LabViewPagerAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var labViewModel: LabViewModel
    private var labSubTitles: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabBinding.inflate(layoutInflater)
        setContentView(binding.root)


        labViewModel = ViewModelProvider(this, viewModelFactory).get(LabViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.labToolbar)
        UIUtil.applyViewInsertForBottomView(binding.labMainLayout)

        labSubTitles = arrayListOf(
            getString(R.string.lab_result_str), getString(
                R.string.lab_order_str
            )
        )
        binding.labViewPager.offscreenPageLimit = ModelUtil.viewPagerOffScreenPageLimit
        binding.labViewPager.adapter = labViewPagerAdapter
        TabLayoutMediator(binding.labTabLayout, binding.labViewPager) { tab, position ->
            tab.text = labSubTitles[position]
        }.attach()

        binding.labTabLayout.visibility = View.VISIBLE

        binding.labBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
