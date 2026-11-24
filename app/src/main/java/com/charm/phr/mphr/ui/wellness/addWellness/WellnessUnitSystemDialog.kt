package com.charm.phr.mphr.ui.wellness.addWellness

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.DialogUnitSystemBinding
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil

class WellnessUnitSystemDialog(context: Context, val width: Int) : Dialog(context) {

    var isMetric: Boolean? = null

    private var callback: SelectUnitSystemListener? = null

    lateinit var binding : DialogUnitSystemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogUnitSystemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setCanceledOnTouchOutside(false)

        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding.wellnessUnitSystemCardLayout.layoutParams?.width = width
        binding.wellnessUnitSystemCardLayout.requestLayout()

        binding.wellnessUnitSystemStandardLayout.setOnClickListener {
            isMetric = false
            updateBg()
        }

        binding.wellnessUnitSystemMetricLayout.setOnClickListener {
            isMetric = true
            updateBg()
        }

        binding.wellnessUnitSystemSubmitBtn.setOnClickListener {

            if (isMetric != null){

                val unitSystem =  if (isMetric!!){
                    ModelUtil.wellnessMetric
                }
                else{
                    ModelUtil.wellnessStandard
                }
                callback?.onSelectUnitSystem(unitSystem)
                dismiss()
            }
            else{
                UIUtil.showToast(context , R.string.error_vital_unit_system_select , Toast.LENGTH_LONG)
            }
        }

        updateBg()
    }

    fun setSelectUnitSystemListener(selectUnitSystemListener: SelectUnitSystemListener?){
        callback = selectUnitSystemListener
    }

    interface SelectUnitSystemListener {
        fun onSelectUnitSystem(unitSystem: String)
    }


    private fun updateBg(){

        if (isMetric != null){
            if (isMetric!!){
                binding.wellnessUnitSystemMetricInfo.setTextColor(ContextCompat.getColor(context , R.color.colorPrimary))
                binding.wellnessUnitSystemMetricTV.setTextColor(ContextCompat.getColor(context , R.color.colorPrimary))
                binding.wellnessUnitSystemMetricLayout.setBgColor(R.color.addWellnessChipBgColor)
                binding.wellnessUnitSystemMetricLayout.setStrokeColor(R.color.addWellnessChipBgColor)
                binding.wellnessUnitSystemMetricCheckImg.visibility = View.VISIBLE

                if (binding.wellnessUnitSystemStandardCheckImg.visibility == View.VISIBLE) {
                    binding.wellnessUnitSystemStandardInfo.setTextColor(ContextCompat.getColor(context, R.color.black87))
                    binding.wellnessUnitSystemStandardTV.setTextColor(ContextCompat.getColor(context, R.color.black87))
                    binding.wellnessUnitSystemStandardLayout.setBgColor(android.R.color.white)
                    binding.wellnessUnitSystemStandardLayout.setStrokeColor(R.color.wellnessUnitSystemNormalBorder)
                    binding.wellnessUnitSystemStandardCheckImg.visibility = View.GONE
                }
            }
            else{
                binding.wellnessUnitSystemStandardInfo.setTextColor(ContextCompat.getColor(context , R.color.colorPrimary))
                binding.wellnessUnitSystemStandardTV.setTextColor(ContextCompat.getColor(context , R.color.colorPrimary))
                binding.wellnessUnitSystemStandardLayout.setBgColor(R.color.addWellnessChipBgColor)
                binding.wellnessUnitSystemStandardLayout.setStrokeColor(R.color.addWellnessChipBgColor)
                binding.wellnessUnitSystemStandardCheckImg.visibility = View.VISIBLE

                if (binding.wellnessUnitSystemMetricCheckImg.visibility == View.VISIBLE) {
                    binding.wellnessUnitSystemMetricInfo.setTextColor(ContextCompat.getColor(context, R.color.black87))
                    binding.wellnessUnitSystemMetricTV.setTextColor(ContextCompat.getColor(context, R.color.black87))
                    binding.wellnessUnitSystemMetricLayout.setBgColor(android.R.color.white)
                    binding.wellnessUnitSystemMetricLayout.setStrokeColor(R.color.wellnessUnitSystemNormalBorder)
                    binding.wellnessUnitSystemMetricCheckImg.visibility = View.GONE
                }
            }
        }
    }
}