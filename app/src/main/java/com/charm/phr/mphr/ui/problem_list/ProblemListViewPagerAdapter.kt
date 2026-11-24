package com.charm.phr.mphr.ui.problem_list

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.charm.phr.mphr.ui.prescription.SupplementFragment
import com.charm.phr.mphr.util.ChangeFilterListener
import com.charm.phr.mphr.util.RefreshDataListener

class ProblemListViewPagerAdapter(fa: FragmentActivity, val appContext: Context, val isHealthPassport: Boolean) : FragmentStateAdapter(fa) {
    private var problemItems: ArrayList<Fragment> = if (isHealthPassport) {
        arrayListOf(ActiveProblemListFragment())
    } else {
        arrayListOf(ActiveProblemListFragment(), SupplementFragment())
    }

    fun deleteProblem(position: Int, currentItem: Int) {
        if (currentItem == 0) {
            val activeProblemListFragment = problemItems[0]
            if (activeProblemListFragment is ActiveProblemListFragment) {
                activeProblemListFragment.deleteProblem(position)
            }
        } else {
            /*val messageSentFragment = messageItems[1]
            if (messageSentFragment is MessageSentFragment) {
                messageSentFragment.deleteMessage(position)
            }*/
        }
    }

    fun changeFilter(position: Int) {

        val fragment = problemItems[position]

        if (fragment is ChangeFilterListener) {
            fragment.onFilterChange()
        }

    }

    fun refreshData(position: Int) {

        val fragment = problemItems[position]
        if (fragment is RefreshDataListener) {
            fragment.refreshData()
        }
    }

    override fun getItemCount(): Int = problemItems.size

    override fun createFragment(position: Int): Fragment = problemItems[position]
}