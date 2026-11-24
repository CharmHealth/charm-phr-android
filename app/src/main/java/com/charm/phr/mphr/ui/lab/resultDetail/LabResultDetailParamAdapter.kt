package com.charm.phr.mphr.ui.lab.resultDetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemLabResultDetailParamListBinding
import com.charm.phr.mphr.model.MedicalRecordParamValueMap


class LabResultDetailParamAdapter(private val medicalRecordParamValueMapList: ArrayList<MedicalRecordParamValueMap>) :
    RecyclerView.Adapter<LabResultDetailParamAdapter.ViewHolder>() {

    private var listener: LabResultDetailPlottingListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLabResultDetailParamListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return medicalRecordParamValueMapList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val medicalRecordParamValueMap = medicalRecordParamValueMapList[position]
        holder.onBind(medicalRecordParamValueMap)

    }

    inner class ViewHolder(val binding: ItemLabResultDetailParamListBinding) : RecyclerView.ViewHolder(binding.root) {

        private val labResultDetailParamNameTV: TextView = binding.labResultDetailParamNameTV
        private val labResultDetailParamValueTV: TextView = binding.labResultDetailParamValueTV
        private val labResultDetailParamReferenceTV: TextView = binding.labResultDetailParamReferenceTV
        private val labResultParamInterpretationTV: TextView = binding.labResultParamInterpretationTV
        private val labResultDetailPlottingIV: ImageView = binding.labResultDetailPlottingIV

        fun onBind(medicalRecordParamValueMap: MedicalRecordParamValueMap) {

            labResultDetailParamNameTV.text = medicalRecordParamValueMap.medicalRecordParam?.parameterName

            val recordUnit = medicalRecordParamValueMap.medicalRecordParam?.recordunit ?: ""
            val valueText = "${medicalRecordParamValueMap.medicalRecordValue?.recordvalue} $recordUnit"

            labResultDetailParamValueTV.text = valueText.trim()

            val referenceMax = medicalRecordParamValueMap.medicalRecordValue?.referenceMax
            val referenceMin = medicalRecordParamValueMap.medicalRecordValue?.referenceMin
            val referenceTxt = when {
                !referenceMax.isNullOrBlank() && !referenceMin.isNullOrBlank() -> {
                    itemView.context.getString(
                        R.string.lab_result_reference_txt,
                        referenceMin,
                        referenceMax,
                        recordUnit
                    )
                }

                !referenceMax.isNullOrBlank() -> {
                    itemView.context.getString(
                        R.string.lab_result_reference_max_txt,
                        referenceMax,
                        recordUnit
                    )
                }

                !referenceMin.isNullOrBlank() -> {
                    itemView.context.getString(
                        R.string.lab_result_reference_min_txt,
                        referenceMin,
                        recordUnit
                    )
                }

                else -> {
                    medicalRecordParamValueMap.medicalRecordValue?.range ?: ""
                }
            }

            labResultDetailParamReferenceTV.text = referenceTxt.trim()

            if (referenceTxt.isBlank()) {
                labResultDetailParamReferenceTV.visibility = View.GONE
            } else {
                labResultDetailParamReferenceTV.visibility = View.VISIBLE
            }

            val interpretation = medicalRecordParamValueMap.medicalRecordValue?.interpretation
            if (!interpretation.isNullOrBlank()) {
                labResultParamInterpretationTV.text = interpretation
                labResultParamInterpretationTV.visibility = View.VISIBLE
            } else {
                labResultParamInterpretationTV.visibility = View.GONE
            }

            labResultDetailPlottingIV.visibility = View.VISIBLE
            labResultDetailPlottingIV.setOnClickListener {
                listener?.labResultDetailPlot(
                    medicalRecordParamValueMap.medicalRecordParam?.parameterName ?: "",
                    referenceTxt,
                    medicalRecordParamValueMap.medicalRecordParam?.recordunit
                )
            }
        }
    }

    fun setLabResultDetailPlottingListener(labResultDetailPlottingListener: LabResultDetailPlottingListener) {
        listener = labResultDetailPlottingListener
    }

    interface LabResultDetailPlottingListener {
        fun labResultDetailPlot(paramName: String, referenceText: String?, recordUnit: String?)
    }
}