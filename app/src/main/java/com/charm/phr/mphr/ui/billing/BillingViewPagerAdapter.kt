package com.charm.phr.mphr.ui.billing

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.charm.phr.mphr.util.RefreshDataListener

class BillingViewPagerAdapter(fa: FragmentActivity, val appContext: Context) : FragmentStateAdapter(fa) {


    private var billingItems: ArrayList<Fragment> = arrayListOf(InvoiceFragment(), ReceiptFragment())

    fun refreshData() {

        billingItems.forEach { fragment ->
            if (fragment is RefreshDataListener) {
                fragment.refreshData()
            }
        }
    }

    override fun getItemCount(): Int = billingItems.size

    override fun createFragment(position: Int): Fragment = billingItems[position]

}