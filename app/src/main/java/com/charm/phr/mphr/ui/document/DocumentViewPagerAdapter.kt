package com.charm.phr.mphr.ui.document

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.util.ChangeFilterListener
import com.charm.phr.mphr.util.RefreshDataListener

class DocumentViewPagerAdapter(fa: FragmentActivity, val appContext: Context, val isHealthPassport: Boolean) : FragmentStateAdapter(fa) {
    private var problemItems: ArrayList<Fragment> = if (isHealthPassport) {
        arrayListOf(DocumentListFragment())
    } else {
        arrayListOf(DocumentListFragment(), DocumentFolderFragment())
    }

    fun deleteProblem(document: Document, currentItem: Int) {
        if (currentItem == 0) {
            val documentListFragment = problemItems[0]
            if (documentListFragment is DocumentListFragment) {
                documentListFragment.deleteDocument(document)
            }
        } else {
            /*val messageSentFragment = messageItems[1]
            if (messageSentFragment is MessageSentFragment) {
                messageSentFragment.deleteMessage(position)
            }*/
        }
    }

    fun removeSelectionMode(currentItem: Int) {
        if (currentItem == 0) {
            val documentListFragment = problemItems[0]
            if (documentListFragment is DocumentListFragment && documentListFragment.isVisible) {
                documentListFragment.removeSelectionMode()
            }
        }
    }

    fun changeFilter(position: Int) {

        val fragment = problemItems[position]

        if (fragment is ChangeFilterListener) {
            fragment.onFilterChange()
        }

    }

    fun showFolderDialog() {
        val fragment = problemItems[1]

        if (fragment is DocumentFolderFragment) {
            fragment.showFolderDialog()
        }
    }

    fun refreshData(position: Int) {

        val fragment = problemItems[position]
        if (fragment is RefreshDataListener && fragment.isVisible) {
            fragment.refreshData()
        }
    }

    override fun getItemCount(): Int = problemItems.size

    override fun createFragment(position: Int): Fragment = problemItems[position]
}