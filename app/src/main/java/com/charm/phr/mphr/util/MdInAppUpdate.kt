package com.charm.phr.mphr.util

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.charm.phr.mphr.R
import com.zoho.apptics.appupdates.AppticsInAppUpdates
import com.zoho.apptics.common.AppticsSettings

abstract class MdInAppUpdateActivity : MDBaseActivity() {

    val mdTAG = "MdInAppUpdate"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildUtil.isCustomApp) {
            return
        }

        try {
            AppticsSettings.setPopupThemeRes(R.style.mdAppticsUpdateTheme)
            AppticsInAppUpdates.checkAndShowVersionAlert(this@MdInAppUpdateActivity, object : AppticsInAppUpdates.OnCompleteListener {
                override fun onComplete() {
                    Log.d(mdTAG, "AppticsInAppUpdates onComplete")
                }
            })

        } catch (e: Exception) {
            Log.d(mdTAG, "Apptics checkAndShowVersionAlert ${e.toString()}")
        }

    }

/*
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

    }

    override fun onResume() {
        super.onResume()

        Log.d(mdTAG, "AppticsInAppUpdates onResume")

    }
*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(mdTAG, "AppticsInAppUpdates onActivityResult")
        if (BuildUtil.isCustomApp) {
            return
        }
        AppticsInAppUpdates.doOnActivityResult(requestCode, resultCode)
    }

/*
    override fun onDestroy() {
        super.onDestroy()
        Log.d(mdTAG, "AppticsInAppUpdates onDestroy")
        //we are not calling this in onDestroy
        //appUpdateAlert.doOnActivityDestroy();
    }
*/

}