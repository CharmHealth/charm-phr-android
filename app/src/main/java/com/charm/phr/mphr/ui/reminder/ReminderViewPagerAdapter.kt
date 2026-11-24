package com.charm.phr.mphr.ui.reminder

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.ChangeFilterListener
import com.charm.phr.mphr.util.RefreshDataListener

class ReminderViewPagerAdapter(fa: FragmentActivity, val appContext: Context, val authManager: AuthManager) :
    FragmentStateAdapter(fa) {

    private var reminderItems: ArrayList<Fragment> = arrayListOf(CurrentReminderFragment(), AllReminderFragment())

    override fun getItemCount(): Int {
        return reminderItems.size
    }

    override fun createFragment(position: Int): Fragment {
        return reminderItems[position]
    }

    //Method to refresh each Fragment reminder List when reminder is added,edited or deleted
    fun refreshData() {
        reminderItems.forEach {
            if (it is RefreshDataListener) {
                it.refreshData()
            }
        }
    }

    fun onFilterChange(position: Int)
    {
        val selectedFragment=reminderItems[position]
        if (selectedFragment is ChangeFilterListener) {
            selectedFragment.onFilterChange()
        }
    }
}