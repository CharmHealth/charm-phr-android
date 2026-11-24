package com.charm.phr.mphr.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.annotation.StringRes
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.DialogProgressBinding

class ProgressDialog(context: Context) : Dialog(context) {

    val TAG = "ProgressDialog"
    var messageId = R.string.loading_data
    private var binding: DialogProgressBinding = DialogProgressBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        binding.progressText.setText(messageId)

        //NOTE:Progress dialog width to be updated if thw app supports tablets

    }

    fun setMessage(@StringRes messageId: Int) {
        this.messageId = messageId

        try {
            binding.progressText.setText(messageId)
        } catch (e: NullPointerException) {
                Log.d(TAG, "message: $e")
        }
    }

}
