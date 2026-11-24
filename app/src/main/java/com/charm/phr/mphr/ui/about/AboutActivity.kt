package com.charm.phr.mphr.ui.about

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAboutBinding
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

/**
 * [AboutActivity]  shows the facility and member list of the practices of the selected patient.
 */
class AboutActivity : MDBaseActivity() {

    @Inject
    lateinit var aboutViewPagerAdapter: AboutViewPagerAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var aboutViewModel: AboutViewModel

    private lateinit var binding: ActivityAboutBinding

    private var aboutSubTitles: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        aboutViewModel = ViewModelProvider(this, viewModelFactory).get(AboutViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.aboutToolbar)
        UIUtil.applyViewInsertForBottomView(binding.aboutMainLayout)


        aboutSubTitles = arrayListOf(
            getString(R.string.about_facilities_str), getString(
                R.string.about_providers_str
            )
        )
        binding.aboutViewPager.offscreenPageLimit = ModelUtil.viewPagerOffScreenPageLimit
        binding.aboutViewPager.adapter = aboutViewPagerAdapter
        TabLayoutMediator(binding.aboutTabLayout, binding.aboutViewPager) { tab, position ->
            tab.text = aboutSubTitles[position]
        }.attach()
        aboutViewModel.fetchAboutList()

        binding.aboutBackBtn.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


}
