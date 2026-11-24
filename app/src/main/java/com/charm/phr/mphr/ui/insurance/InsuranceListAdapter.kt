package com.charm.phr.mphr.ui.insurance

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemInsuranceListBinding
import com.charm.phr.mphr.model.PatientInsuranceDetails
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.OnMultipleSelectChangeListener
import com.charm.phr.mphr.util.UIUtil
import java.util.Locale

class InsuranceListAdapter(val authManager: AuthManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var insuranceList: ArrayList<PatientInsuranceDetails?> = arrayListOf()

    private var insuranceSelectListener: InsuranceSelectListener? = null
    private var insuranceMultiSelectListener: OnMultipleSelectChangeListener? = null

    private var isMultiSelectEnabled = false

    private val viewTypeInsurance = 1
    private val viewTypeScroll = 2

    private var allowEdit = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemInsuranceListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return if (viewType == viewTypeInsurance) {
            ViewHolder(binding)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }
    }

    override fun getItemCount(): Int = insuranceList.size

    override fun getItemViewType(position: Int): Int {
        return if (insuranceList[position] != null) {
            viewTypeInsurance
        } else {
            viewTypeScroll
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val insurances = insuranceList[position]

        if (insurances != null && holder is ViewHolder) {

            holder.onBind(insurances)
            /*holder.itemView.setOnLongClickListener {
                if (!isMultiSelectEnabled) {
                    isMultiSelectEnabled = true
                    insuranceMultiSelectListener?.onMultiSelect(true)
                    insurances.isSelected = true
                    holder.changeBgToSelected()
                }
                else {
                    if (!insurances.isSelected) {
                        insurances.isSelected = true
                        holder.changeBgToSelected()
                    } else {
                        insurances.isSelected = true
                        holder.changeBgToSelected()
                    }
                }

                return@setOnLongClickListener true
            }*/



            if (allowEdit) {

                holder.insuranceConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)

                holder.itemView.setOnLongClickListener {
                    insuranceSelectListener?.onDeleteInsurance(insurances, position)
                    return@setOnLongClickListener true
                }
                holder.itemView.setOnClickListener {
                    /* if (isMultiSelectEnabled) {
                    if (insurances.isSelected) {
                        insurances.isSelected = false
                        holder.changeBgToUnSelected()
                        checkAllIsUnchecked()
                    } else {
                        insurances.isSelected = true
                        holder.changeBgToSelected()
                    }
                } else {*/
                    insuranceSelectListener?.onEditInsurance(insurances)
                    // }
                }
            } else {
                holder.insuranceConstraintLayout.setBackgroundResource(android.R.color.white)

                holder.itemView.setOnLongClickListener {
                    return@setOnLongClickListener true
                }
                holder.itemView.setOnClickListener {

                }
            }
        }
    }

    fun setInsurances(insuranceList: ArrayList<PatientInsuranceDetails>): Int {
        this.insuranceList.clear()
        this.insuranceList.addAll(insuranceList)

        allowEdit = ModelUtil.isPHREntryEnabled(authManager, ModelUtil.insuranceModelId)
        this.notifyDataSetChanged()
        return this.insuranceList.size
    }

    fun addInsurances(insuranceList: ArrayList<PatientInsuranceDetails>) {
        this.insuranceList.addAll(insuranceList)
        this.notifyDataSetChanged()
    }

    fun addInfiniteScroll() {
        insuranceList.add(null)
        notifyItemInserted(insuranceList.size - 1)
    }

    fun removeInfiniteScroll() {
        insuranceList.removeAt(insuranceList.size - 1)
        val scrollPosition = insuranceList.size
        notifyItemRemoved(scrollPosition)
    }

    fun removeInsurance(position: Int): Int {
        insuranceList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, insuranceList.size)

        return insuranceList.size
    }

    private fun checkAllIsUnchecked() {

        var atleastOneIsSelected = false
        for (insurances in insuranceList) {
            if (insurances?.isSelected == true) {
                atleastOneIsSelected = true
                break
            }
        }

        if (!atleastOneIsSelected) {
            isMultiSelectEnabled = false
            insuranceMultiSelectListener?.onMultiSelect(false)
        }
    }

    fun disableMultiSelect() {
        isMultiSelectEnabled = false
        insuranceList.forEach { insurance ->
            insurance?.isSelected = false
        }
        notifyDataSetChanged()
    }

    fun setInsuranceSelectListener(listener: InsuranceSelectListener) {
        insuranceSelectListener = listener
    }

    fun setOnMultipleSelectChangeListener(listener: OnMultipleSelectChangeListener) {
        insuranceMultiSelectListener = listener
    }

    inner class ViewHolder(private val binding: ItemInsuranceListBinding) : RecyclerView.ViewHolder(binding.root) {


        val insuranceConstraintLayout = binding.insuranceListConstraintLayout
        val insuranceName = binding.insuranceListNameTV
        val insurancePlanLabel = binding.insuranceListPlanLabel
        val insurancePlanTV = binding.insuranceListPlanValueTV
        val insurancePolicyGrpNumLabel = binding.insuranceListPolicyGrpNumLabel
        val insurancePolicyGrpNumTV = binding.insuranceListPolicyGrpNumValueTV
        val insuranceInsuredPersonLabel = binding.insuranceListInsuredPersonLabel
        val insuranceInsuredPersonTV = binding.insuranceListInsuredPersonValueTV

        val insuranceMemberLabel = binding.insuranceListMemberLabel
        val insuranceMemberTV = binding.insuranceListMemberValueTV

        val insuranceDateTV = binding.insuranceListDateTV

        private val handler = Handler(Looper.getMainLooper())
        private val runnable = Runnable {
            insuranceConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)
        }

        fun onBind(insurance: PatientInsuranceDetails) {


            val type = if (insurance.type.isNullOrBlank()) {
                itemView.context.getString(R.string.insurance_type_secondary)
            } else {
                insurance.type?.lowercase(Locale.ENGLISH)?.replaceFirstChar{ it.uppercase() } ?: ""
            }


            val insuranceTypeChange = UIUtil.onInsuranceTypeChange( insurance.insurancePlanType ?: "")

            if (insurance.policyNumber.isNullOrBlank()){
                insuranceMemberLabel.visibility = View.GONE
                insuranceMemberTV.visibility = View.GONE
            }
            else {
                insuranceMemberLabel.setText(insuranceTypeChange.memberIdLabel)
                insuranceMemberTV.text = insurance.policyNumber

                insuranceMemberLabel.visibility = View.VISIBLE
                insuranceMemberTV.visibility = View.VISIBLE
            }



            insuranceName.text = if (insurance.practicePayer?.payerName.isNullOrBlank()) {
                type
            } else {
                itemView.context.getString(R.string.insurance_name_value, type, insurance.practicePayer?.payerName)
            }

            if (!insurance.insurancePlanType.isNullOrBlank()) {
                insurancePlanLabel.visibility = View.VISIBLE
                insurancePlanTV.visibility = View.VISIBLE
                insurancePlanTV.text = insurance.insurancePlanType
            } else {
                insurancePlanLabel.visibility = View.GONE
                insurancePlanTV.visibility = View.GONE
            }


            if (!insurance.groupNumber.isNullOrBlank()) {
                insurancePolicyGrpNumLabel.visibility = View.VISIBLE
                insurancePolicyGrpNumTV.visibility = View.VISIBLE
                insurancePolicyGrpNumTV.text = insurance.groupNumber
            } else {
                insurancePolicyGrpNumLabel.visibility = View.GONE
                insurancePolicyGrpNumTV.visibility = View.GONE
            }


            val insuredPerson = "${insurance.insuredAndGuarantorDetails?.firstName ?: ""} ${insurance.insuredAndGuarantorDetails?.lastName ?: ""}"
            if (insuredPerson.isNotBlank()) {
                insuranceInsuredPersonLabel.visibility = View.VISIBLE
                insuranceInsuredPersonTV.visibility = View.VISIBLE
                insuranceInsuredPersonTV.text = insuredPerson.trim()
            } else {
                insuranceInsuredPersonLabel.visibility = View.GONE
                insuranceInsuredPersonTV.visibility = View.GONE
            }


            if (insurance.policyStartDate != null && insurance.policyEndDate != null) {
                insuranceDateTV.visibility = View.VISIBLE
                insuranceDateTV.text = UIUtil.makeInsuranceValidDateTxt(itemView.context, insurance.policyStartDate, insurance.policyEndDate)
            } else {
                insuranceDateTV.visibility = View.GONE
            }

            if (insurance.isSelected) {
                changeBgToSelected()
            } else {
                changeBgToUnSelected()
            }
        }

        fun changeBgToSelected() {

            handler.removeCallbacks(runnable)
            insuranceConstraintLayout.setBackgroundResource(R.color.cardSelectedBgColor)
        }

        fun changeBgToUnSelected() {
            insuranceConstraintLayout.setBackgroundResource(android.R.color.white)
            handler.postDelayed(runnable, ModelUtil.delayedTimingForRippleChange)
        }

    }

    interface InsuranceSelectListener {
        fun onEditInsurance(insurance: PatientInsuranceDetails)
        fun onDeleteInsurance(insurance: PatientInsuranceDetails, position: Int)
    }

}