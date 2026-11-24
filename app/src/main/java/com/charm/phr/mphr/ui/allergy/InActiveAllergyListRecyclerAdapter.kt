package com.charm.phr.mphr.ui.allergy

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemInactiveAllergyListBinding
import com.charm.phr.mphr.model.PatientAllergy
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.OnMultipleSelectChangeListener

class InActiveAllergyListRecyclerAdapter(val authManager: AuthManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: ArrayList<PatientAllergy?> = arrayListOf()

    private var viewTypeAllergy = 1
    private var viewTypeScroll = 2

    private var isMultiSelectEnabled = false

    private var inActiveAllergyListMultiSelectListener: OnMultipleSelectChangeListener? = null

    private var editClickListener: AllergyActivity.AllergyListListener? = null

    private var allowEdit = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == viewTypeAllergy) {
            val binding = ItemInactiveAllergyListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding, allowEdit)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val patAllergy = items[position]

        if (patAllergy != null && holder is ViewHolder) {
            holder.onBind(patAllergy)

            holder.itemView.setOnLongClickListener {
                if ((patAllergy.addedByPatient && allowEdit) || patAllergy.isHealthPassport) {/* if (!isMultiSelectEnabled) {
                         isMultiSelectEnabled = true
                         inActiveAllergyListMultiSelectListener?.onMultiSelect(true)
                         patAllergy.isSelected = true
                         holder.changeBgToSelected()
                     } else {
                         if (!patAllergy.isSelected) {
                             patAllergy.isSelected = true
                             holder.changeBgToSelected()
                         } else {
                             patAllergy.isSelected = true
                             holder.changeBgToSelected()
                         }
                     }*/
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
        allowEdit = ModelUtil.isPHREntryEnabled(authManager, ModelUtil.allergiesModelId)
        notifyDataSetChanged()
        return this.items.size
    }

    fun removeAllergy(position: Int): Int {
        items.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, items.size)

        return items.size
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
            inActiveAllergyListMultiSelectListener?.onMultiSelect(false)
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
        inActiveAllergyListMultiSelectListener = listener
    }

    fun setOnEditClickListener(listener: AllergyActivity.AllergyListListener) {
        editClickListener = listener
    }

    inner class ViewHolder(binding: ItemInactiveAllergyListBinding, val allowEdit: Boolean) : RecyclerView.ViewHolder(binding.root) {

        private val allergyListDateTV = binding.inactiveAllergyListDateTV
        private val allergyListNameAndTypeTV = binding.inactiveAllergyListNameAndTypeTV
        private val allergyListSeverityLayout = binding.inactiveAllergyListSeverityLayout
        private val allergyListSeverityTV = binding.inactiveAllergyListSeverityTV
        private val allergyListSeverityLabel = binding.inactiveAllergyListSeverityLabel
        val allergyListEditBtn: ImageView = binding.inactiveAllergyListEditBtn
        private val allergyListConstraintLayout = binding.inactiveAllergyListConstraintLayout

        private val handler = Handler(Looper.getMainLooper())
        private val runnable = Runnable {
            allergyListConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)
        }

        fun onBind(patAllergy: PatientAllergy) {

            if (patAllergy.observedDate == null) {
                allergyListDateTV.visibility = View.GONE
            } else {
                allergyListDateTV.visibility = View.VISIBLE
                allergyListDateTV.text = patAllergy.observedDate?.toString(ModelUtil.charmDisplayDateFormat())
            }
            allergyListNameAndTypeTV.text =
                itemView.context.getString(R.string.allergy_name_and_type_template_str, patAllergy.allergy?.allergyType, patAllergy.allergy?.allergy)


            when (patAllergy.severity) {
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



            if (patAllergy.addedByPatient && allowEdit) {

                allergyListConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)

                allergyListEditBtn.visibility = View.VISIBLE
            } else {
                allergyListConstraintLayout.setBackgroundResource(android.R.color.white)
                allergyListEditBtn.visibility = View.GONE
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