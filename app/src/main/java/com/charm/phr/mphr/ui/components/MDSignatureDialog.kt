package com.charm.phr.mphr.ui.components

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.DialogMdSignatureBinding
import com.charm.phr.mphr.util.UIUtil

class MDSignatureDialog(context: Context, val width: Int, val height: Int) : Dialog(context) {

    private var signatureClickListener: SignatureClickListener? = null

    lateinit var binding: DialogMdSignatureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogMdSignatureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setCancelable(false)
        setCanceledOnTouchOutside(false)

        window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.signatureDialogLayout.layoutParams?.width = width
        binding.questionnaireDetailItemSignaturePad.layoutParams?.height = height
        clearSignature()
        binding.signatureDialogLayout.requestLayout()
        setOnClickListener()
    }

    fun setOnClickListener() {
        binding.signatureClose.setOnClickListener {
            this.dismiss()
        }

        binding.questionnaireDetailItemSignatureSaveBtn.setOnClickListener {
            if (binding.questionnaireDetailItemSignaturePad.isEmpty) {
                UIUtil.showToast(this.context, R.string.error_empty_signature, Toast.LENGTH_LONG)
            } else {
                this.dismiss()
                signatureClickListener?.saveSignature(
                    binding.questionnaireDetailItemSignaturePad.transparentSignatureBitmap
                )

            }
        }

        binding.questionnaireDetailItemSignatureClearBtn.setOnClickListener {
            clearSignature()
        }
    }

    fun clearSignature() {
        binding.questionnaireDetailItemSignaturePad.clear()
    }

    fun setSignatureClickListener(listener: SignatureClickListener) {
        signatureClickListener = listener
    }

    interface SignatureClickListener {
        fun saveSignature( transparentSignatureBitmap: Bitmap)
    }
}