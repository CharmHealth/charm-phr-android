package com.charm.phr.mphr.ui.about

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AboutViewPagerAdapter(fa: FragmentActivity, val appContext: Context) : FragmentStateAdapter(fa) {
    private var aboutItems: ArrayList<Fragment> = arrayListOf(FacilityListFragment(), MemberListFragment())

    override fun getItemCount(): Int {
        return aboutItems.size
    }

    override fun createFragment(position: Int): Fragment {
        return aboutItems[position]
    }

}