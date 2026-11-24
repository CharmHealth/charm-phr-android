package com.charm.phr.mphr.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class InstallReferrerReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val referrer = intent.getStringExtra("referrer")

       // UIUtil.showToast(context , "broadcast referrer -> $referrer" , Toast.LENGTH_LONG)
        Log.d("referrerBroadCast" , referrer ?: "")
    }
}