package com.charm.phr.mphr.ui.questionnaire

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.charm.phr.mphr.util.ChangeFilterListener
import com.charm.phr.mphr.util.DisableMultiSelectListener
import com.charm.phr.mphr.util.RefreshDataListener

class QuestionnaireViewPagerAdapter(fa: FragmentActivity, val appContext: Context) : FragmentStateAdapter(fa) {
    private var questionnaireItems: ArrayList<Fragment> = arrayListOf(QuestionnaireCurrentFragment(), QuestionnairePastFragment())


    fun disableMultiSelectAdapter(position: Int) {

        val fragment = questionnaireItems[position]

        if (fragment is DisableMultiSelectListener) {
            fragment.disableMultiSelect()
        }

    }

    fun changeFilter() {

        questionnaireItems.forEach { fragment ->
            if (fragment is ChangeFilterListener) {
                fragment.onFilterChange()
            }
        }


    }

    fun refreshData(position: Int) {

        val fragment = questionnaireItems[position]
        if (fragment is RefreshDataListener) {
            fragment.refreshData()
        }
    }

    override fun getItemCount(): Int = questionnaireItems.size

    override fun createFragment(position: Int): Fragment = questionnaireItems[position]
}