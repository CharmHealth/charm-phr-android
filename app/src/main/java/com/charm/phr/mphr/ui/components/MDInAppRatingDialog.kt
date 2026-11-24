package com.charm.phr.mphr.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.DialogMdInAppRatingBinding
import com.charm.phr.mphr.util.AppticsUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.zoho.apptics.rateus.AppticsInAppRatings

class MDInAppRatingDialog(context: Context, val width: Int) : Dialog(context) {

    private lateinit var dialogMdInAppRatingBinding: DialogMdInAppRatingBinding
    private var mdInAppRatingDialogListener: MDInAppRatingDialogListener? = null
    var headingText = ""
    var subText: String = ""
    var image: Int? = null
    var criteriaId:Long?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        MDBaseActivity.isPassCodeAllowed = false
        dialogMdInAppRatingBinding = DialogMdInAppRatingBinding.inflate(layoutInflater)
        setContentView(dialogMdInAppRatingBinding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogMdInAppRatingBinding.inAppRatingDialogLayout.layoutParams?.width = width
        dialogMdInAppRatingBinding.inAppRatingDialogLayout.requestLayout()
        renderUI()
        setOnClickListener()

    }

    private fun renderUI() {
        dialogMdInAppRatingBinding.inAppRatingHeading.text = headingText
        dialogMdInAppRatingBinding.inAppRatingText.text = subText
        dialogMdInAppRatingBinding.inAppRatingIV.setImageResource(image ?: R.drawable.ic_in_app_rating_image)
    }

    fun setOnClickListener() {
        dialogMdInAppRatingBinding.inAppRatingYesBtn.setOnClickListener {
            mdInAppRatingDialogListener?.onYesClick()
        }
        dialogMdInAppRatingBinding.inAppRatingNoBtn.setOnClickListener {
            mdInAppRatingDialogListener?.onNoClick()
        }
    }

    fun setRatingDialogListener(mdInAppRatingDialogListener: MDInAppRatingDialogListener) {
        this.mdInAppRatingDialogListener = mdInAppRatingDialogListener
    }

    interface MDInAppRatingDialogListener {
        fun onYesClick()
        fun onNoClick()
    }

    override fun cancel() {
        super.cancel()
        criteriaId?.let{
            AppticsUtil.sendStats(it, AppticsInAppRatings.PopupAction.LATER_CLICKED, AppticsInAppRatings.PopupSource.DYNAMIC)
        }
    }
}