package com.charm.phr.mphr.ui.lab

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class LabViewPagerAdapter(fa: FragmentActivity, val appContext: Context) : FragmentStateAdapter(fa) {

    private var labItems: ArrayList<Fragment> = arrayListOf(LabResultListFragment(), LabOrderListFragment())

    override fun getItemCount(): Int = labItems.size

    override fun createFragment(position: Int): Fragment = labItems[position]
}