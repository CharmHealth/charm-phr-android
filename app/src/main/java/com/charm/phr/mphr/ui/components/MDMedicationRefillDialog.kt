package com.charm.phr.mphr.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import com.charm.phr.mphr.databinding.DialogMdMedicationRefillBinding

class MDMedicationRefillDialog(context: Context ,val width: Int) : Dialog(context) {

    private var medicationRefillClick: MedicationRefillClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding = DialogMdMedicationRefillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setCancelable(false)
        setCanceledOnTouchOutside(false)


        window?.setBackgroundDrawableResource(android.R.color.transparent)



        binding.medicationRefillDialogLayout.layoutParams?.width = width
        binding.medicationRefillDialogLayout.requestLayout()


        binding.medicationRefillCancelBtn.setOnClickListener {
            dismiss()
        }

        binding.medicationRefillBtn.setOnClickListener {
            val refillReasonStr = binding.medicationRefillDialogReasonET.text.toString()
            if (refillReasonStr.isNotBlank()){
                medicationRefillClick?.onMedicationRefillClick(refillReasonStr)
                dismiss()
            }
        }
    }

    fun setMedicationRefillClickListener(listener: MedicationRefillClickListener){
        medicationRefillClick = listener
    }

    interface MedicationRefillClickListener{
       fun onMedicationRefillClick(refill: String)
    }
}