package com.charm.phr.mphr.ui.message

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.charm.phr.mphr.util.DisableMultiSelectListener

class MessageViewPagerAdapter(fa: FragmentActivity, val appContext: Context) : FragmentStateAdapter(fa) {


    private var messageItems: ArrayList<Fragment> = arrayListOf(MessageInboxFragment(), MessageSentFragment())

    fun disableMultiSelectAdapter(position: Int) {

        val fragment = messageItems[position]

        if (fragment is DisableMultiSelectListener) {
            fragment.disableMultiSelect()
        }

    }

    fun refetchDataForSent() {
        val messageSentFragment = messageItems[1]
        if (messageSentFragment is MessageSentFragment) {
            messageSentFragment.fetchSentMessage()
        }

    }

    fun deleteMessage(position: Int, currentItem: Int) {
        if (currentItem == 0) {
            val messageInboxFragment = messageItems[0]
            if (messageInboxFragment is MessageInboxFragment) {
                messageInboxFragment.deleteMessage(position)
            }
        } else {
            val messageSentFragment = messageItems[1]
            if (messageSentFragment is MessageSentFragment) {
                messageSentFragment.deleteMessage(position)
            }
        }
    }

    override fun getItemCount(): Int = messageItems.size

    override fun createFragment(position: Int): Fragment = messageItems[position]

}