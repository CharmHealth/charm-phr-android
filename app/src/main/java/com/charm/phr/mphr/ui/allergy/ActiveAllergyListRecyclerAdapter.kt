package com.charm.phr.mphr.ui.allergy

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemActiveAllergyListBinding
import com.charm.phr.mphr.model.PatientAllergy
import com.charm.phr.mphr.ui.components.MDmPHRConstraintLayout
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.OnMultipleSelectChangeListener

class ActiveAllergyListRecyclerAdapter(val authManager: AuthManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: ArrayList<PatientAllergy?> = arrayListOf()

    private var viewTypeAllergy = 1
    private var viewTypeScroll = 2

    private var isMultiSelectEnabled = false

    private var activeAllergyListMultiSelectListener: OnMultipleSelectChangeListener? = null

    private var editClickListener: AllergyActivity.AllergyListListener? = null

    private var allowEdit = true

    var allergyStatus = ModelUtil.allergyStatusActive

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == viewTypeAllergy) {
            val binding = ItemActiveAllergyListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding, allowEdit)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val patAllergy = items[position]

        if (patAllergy != null && holder is ViewHolder) {
            holder.onBind(patAllergy)

            holder.itemView.setOnLongClickListener {

                if ((patAllergy.addedByPatient && allowEdit) || patAllergy.isHealthPassport) {
                    editClickListener?.onDeleteClick(patAllergy, position)
                }

                return@setOnLongClickListener true
            }


            holder.itemView.setOnClickListener {

                if (patAllergy.isHealthPassport) {
                    editClickListener?.onEditClick(patAllergy)
                }
            }

            holder.allergyListEditBtn.setOnClickListener {
                if (!isMultiSelectEnabled) {
                    editClickListener?.onEditClick(patAllergy)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return if (items[position] != null) {
            viewTypeAllergy
        } else {
            viewTypeScroll
        }
    }

    fun addAllergyList(items: ArrayList<PatientAllergy>): Int {
        this.items.addAll(items)
        notifyDataSetChanged()
        return this.items.size
    }

    fun setAllergyList(items: ArrayList<PatientAllergy>): Int {
        this.items.clear()
        this.items.addAll(items)
        allowEdit = ModelUtil.isPHREntryEnabled(authManager, ModelUtil.allergiesModelId) && (ModelUtil.allergyStatusInActive != allergyStatus)
        notifyDataSetChanged()
        return this.items.size
    }

    fun addInfiniteScroll() {
        items.add(null)
        notifyItemInserted(items.size - 1)
    }

    fun removeInfiniteScroll() {
        if (items[items.size - 1] == null) {
            items.removeAt(items.size - 1)
            val scrollPosition = items.size
            notifyItemRemoved(scrollPosition)
        }
    }

    fun removeAllergy(position: Int): Int {
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, items.size)

        return items.size
    }

    private fun checkAllIsUnchecked() {

        var atleastOneIsSelected = false
        for (patAllergy in items) {
            if (patAllergy?.isSelected == true) {
                atleastOneIsSelected = true
                break
            }
        }

        if (!atleastOneIsSelected) {
            isMultiSelectEnabled = false
            activeAllergyListMultiSelectListener?.onMultiSelect(false)
        }
    }

    fun disableMultiSelect() {
        isMultiSelectEnabled = false
        items.forEach { message ->
            message?.isSelected = false
        }
        notifyDataSetChanged()
    }

    fun setOnMultipleSelectChangeListener(listener: OnMultipleSelectChangeListener) {
        activeAllergyListMultiSelectListener = listener
    }

    fun setOnEditClickListener(listener: AllergyActivity.AllergyListListener) {
        editClickListener = listener
    }

    inner class ViewHolder(binding: ItemActiveAllergyListBinding, val allowEdit: Boolean) : RecyclerView.ViewHolder(binding.root) {


        private val allergyListDateTV = binding.activeAllergyListDateTV
        private val allergyListNameAndTypeTV = binding.activeAllergyListNameAndTypeTV
        private val allergyListReactionsTV = binding.activeAllergyListReactionsTV
        private val allergyListSeverityLayout = binding.activeAllergyListSeverityLayout
        private val allergyListSeverityTV = binding.activeAllergyListSeverityTV
        val allergyListEditBtn = binding.activeAllergyListEditBtn
        private val allergyListConstraintLayout = binding.activeAllergyListConstraintLayout
        private val inactiveAllergyListSeverityLayout = binding.inactiveAllergyListSeverityLayout
        private val inactiveAllergyListSeverityTV = binding.inactiveAllergyListSeverityTV
        private val inactiveAllergyListSeverityLabel = binding.inactiveAllergyListSeverityLabel
        private val allergyListLayout = binding.activeAllergyListLayout
        private val resolvedOnTV = binding.resolvedOnTV
        private val resolvedOnDateTV = binding.resolvedOnDateTV


        private val allergyListHPDateTV = binding.activeAllergyListHPDateTV
        private val allergyListHPNameAndTypeTV = binding.activeAllergyListHPNameAndTypeTV
        private val allergyListHPSeverityTV = binding.activeAllergyListHPSeverityTV
        private val activeAllergyListHPLayout = binding.activeAllergyListHPLayout

        private val handler = Handler(Looper.getMainLooper())
        private val runnable = Runnable {
            allergyListConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)
        }

        fun onBind(patAllergy: PatientAllergy) {

            if (patAllergy.isHealthPassport) {

                allergyListLayout.visibility = View.GONE
                activeAllergyListHPLayout.visibility = View.VISIBLE

                if (patAllergy.observedDate == null) {
                    allergyListHPDateTV.visibility = View.GONE
                } else {
                    allergyListHPDateTV.visibility = View.VISIBLE
                    allergyListHPDateTV.text = patAllergy.observedDate?.toString(ModelUtil.charmDisplayDateFormat())
                }
                allergyListHPNameAndTypeTV.text =
                    itemView.context.getString(R.string.allergy_name_and_type_template_str, patAllergy.allergy?.allergyType, patAllergy.allergy?.allergy)

                when (patAllergy.severity) {
                    ModelUtil.allergySeverityMild -> {
                        allergyListHPSeverityTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.allergyListSeverityMildTxtColor))
                        allergyListHPSeverityTV.setText(R.string.allergy_severity_mild_str)
                    }

                    ModelUtil.allergySeverityModerate -> {
                        allergyListHPSeverityTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.allergyListSeverityModerateTxtColor))
                        allergyListHPSeverityTV.setText(R.string.allergy_severity_moderate_str)

                    }

                    ModelUtil.allergySeveritySevere -> {
                        allergyListHPSeverityTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.allergyListSeveritySevereTxtColor))
                        allergyListHPSeverityTV.setText(R.string.allergy_severity_severe_str)
                    }
                }
                allergyListConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)

            } else {

                allergyListLayout.visibility = View.VISIBLE
                activeAllergyListHPLayout.visibility = View.GONE

                if (patAllergy.observedDate == null) {
                    allergyListDateTV.visibility = View.GONE
                } else {
                    allergyListDateTV.visibility = View.VISIBLE
                    allergyListDateTV.text = patAllergy.observedDate?.toString(ModelUtil.charmDisplayDateFormat())
                }
                allergyListNameAndTypeTV.text =
                    itemView.context.getString(R.string.allergy_name_and_type_template_str, patAllergy.allergy?.allergyType, patAllergy.allergy?.allergy)

                when (allergyStatus) {
                    ModelUtil.allergyStatusActive -> {
                        allergyListSeverityLayout.visibility = View.VISIBLE
                        inactiveAllergyListSeverityLayout.visibility = View.GONE
                        inactiveAllergyListSeverityLabel.visibility = View.GONE
                        setSeverityLayout(patAllergy.severity, allergyListSeverityLayout, allergyListSeverityTV)
                        resolvedOnTV.visibility = View.GONE
                        resolvedOnDateTV.visibility = View.GONE
                    }

                    ModelUtil.allergyStatusInActive -> {
                        allergyListSeverityLayout.visibility = View.GONE
                        inactiveAllergyListSeverityLayout.visibility = View.VISIBLE
                        inactiveAllergyListSeverityLabel.visibility = View.VISIBLE
                        setSeverityLayout(patAllergy.severity, inactiveAllergyListSeverityLayout, inactiveAllergyListSeverityTV)
                        resolvedOnTV.visibility = View.GONE
                        resolvedOnDateTV.visibility = View.GONE
                    }

                    ModelUtil.allergyStatusResolved -> {
                        allergyListSeverityLayout.visibility = View.VISIBLE
                        inactiveAllergyListSeverityLayout.visibility = View.GONE
                        inactiveAllergyListSeverityLabel.visibility = View.GONE
                        allergyListSeverityLayout.setBgColor(R.color.milky_green)
                        allergyListSeverityTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.onlineGreen))
                        allergyListSeverityTV.setText(R.string.resolved_txt)
                        if (patAllergy.resolvedDate == null) {
                            resolvedOnTV.visibility = View.GONE
                            resolvedOnDateTV.visibility = View.GONE
                        } else {
                            resolvedOnTV.visibility = View.VISIBLE
                            resolvedOnDateTV.visibility = View.VISIBLE
                            resolvedOnDateTV.text = patAllergy.resolvedDate?.toString(ModelUtil.charmDisplayDateFormat())
                        }

                    }
                }

                if (allergyStatus == ModelUtil.allergyStatusActive) {
                    if (patAllergy.reactions.isNullOrBlank()) {
                        allergyListReactionsTV.visibility = View.GONE
                    } else {
                        allergyListReactionsTV.visibility = View.VISIBLE
                        allergyListReactionsTV.text = patAllergy.reactions
                    }
                } else {
                    allergyListReactionsTV.visibility = View.GONE
                }

                if (patAllergy.addedByPatient && allowEdit) {
                    allergyListConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)
                    allergyListEditBtn.visibility = View.VISIBLE
                } else {
                    allergyListConstraintLayout.setBackgroundResource(android.R.color.white)
                    allergyListEditBtn.visibility = View.GONE
                }
            }

        }

        fun setSeverityLayout(severity: String?, allergyListSeverityLayout: MDmPHRConstraintLayout, allergyListSeverityTV: TextView) {
            when (severity) {
                ModelUtil.allergySeverityMild -> {
                    allergyListSeverityLayout.setBgColor(R.color.allergyListSeverityMildBg)
                    allergyListSeverityTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.allergyListSeverityMildTxtColor))
                    allergyListSeverityTV.setText(R.string.allergy_severity_mild_str)
                }

                ModelUtil.allergySeverityModerate -> {
                    allergyListSeverityLayout.setBgColor(R.color.allergyListSeverityModerateBg)
                    allergyListSeverityTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.allergyListSeverityModerateTxtColor))
                    allergyListSeverityTV.setText(R.string.allergy_severity_moderate_str)

                }

                ModelUtil.allergySeveritySevere -> {
                    allergyListSeverityLayout.setBgColor(R.color.allergyListSeveritySevereBg)
                    allergyListSeverityTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.allergyListSeveritySevereTxtColor))
                    allergyListSeverityTV.setText(R.string.allergy_severity_severe_str)
                }
            }
        }

        fun changeBgToSelected() {
            handler.removeCallbacks(runnable)
            allergyListConstraintLayout.setBackgroundResource(R.color.cardSelectedBgColor)
        }

        fun changeBgToUnSelected() {
            allergyListConstraintLayout.setBackgroundResource(android.R.color.white)
            handler.postDelayed(runnable, ModelUtil.delayedTimingForRippleChange)
        }
    }

}