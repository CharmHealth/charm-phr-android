package com.charm.phr.mphr.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.charm.phr.mphr.databinding.DialogRequestPermissionBinding
import com.charm.phr.mphr.util.MDBaseActivity

class MdRequestPermissionDialog(context: Context, val width: Int) : Dialog(context) {

    private lateinit var dialogRequestPermissionBinding: DialogRequestPermissionBinding
    private var mdRequestPermissionListener: MDRequestPermissionListener? = null
    var headingText = ""
    var subHeadingText = ""
    var positiveBtnText = ""
    var negativeBtnText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        MDBaseActivity.isPassCodeAllowed = false
        dialogRequestPermissionBinding = DialogRequestPermissionBinding.inflate(layoutInflater)
        setContentView(dialogRequestPermissionBinding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogRequestPermissionBinding.reqPermissionDialogLayout.layoutParams?.width = width
        dialogRequestPermissionBinding.reqPermissionDialogLayout.requestLayout()
        renderUI()
        setOnClickListener()
    }

    fun setOnClickListener() {
        dialogRequestPermissionBinding.reqPermissionYesBtn.setOnClickListener {
            mdRequestPermissionListener?.onYesClick()
        }
        dialogRequestPermissionBinding.reqPermissionNoBtn.setOnClickListener {
            mdRequestPermissionListener?.onNoClick()
        }
    }

    fun setRequestPermissionListener(mdRequestPermissionListener: MDRequestPermissionListener) {
        this.mdRequestPermissionListener = mdRequestPermissionListener
    }

    private fun renderUI() {
        dialogRequestPermissionBinding.reqPermissionHeading.text = headingText
        dialogRequestPermissionBinding.reqPermissionText.text = subHeadingText
        dialogRequestPermissionBinding.reqPermissionNoBtn.text = negativeBtnText
        dialogRequestPermissionBinding.reqPermissionYesBtn.text = positiveBtnText
    }

    interface MDRequestPermissionListener {
        fun onYesClick()
        fun onNoClick()
    }
}