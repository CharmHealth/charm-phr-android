package com.charm.phr.mphr.ui.demographics

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.DialogHpPasscodeBinding
import com.charm.phr.mphr.util.UIUtil

class PatientDemographicsPasscodeDialog(context: Context, val width: Int, val passcode: String) : Dialog(context) {
    private lateinit var binding: DialogHpPasscodeBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogHpPasscodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawableResource(android.R.color.transparent)


        binding.patDemographicsPasscodeCard.layoutParams?.width = width
        binding.patDemographicsPasscodeCard.requestLayout()


        binding.patDemographicsPasscodeTV.text = passcode

        binding.patDemographicsPasscodeLayout.setOnClickListener {
            copyPassCode()
        }

        binding.patDemographicsPasscodeIV.setOnClickListener {
            copyPassCode()
        }

    }

    fun copyPassCode() {
        val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Passcode", passcode)
        clipboard.setPrimaryClip(clip)
        UIUtil.showToast(context, context.getString(R.string.passcode_copied_hint), Toast.LENGTH_SHORT)
    }
}