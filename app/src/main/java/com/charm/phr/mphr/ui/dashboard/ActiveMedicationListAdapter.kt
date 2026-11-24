package com.charm.phr.mphr.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.databinding.ItemActiveMedicationListBinding
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil.Companion.setRxDisplayName

class ActiveMedicationListAdapter : RecyclerView.Adapter<ActiveMedicationListAdapter.ViewHolder>() {

    interface ActiveMedicationListener {
        fun onMedicationSelected()
    }

    private var listener: ActiveMedicationListener? = null

    private val patMedPOJOList: ArrayList<PatMedPOJO> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveMedicationListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        val patMedSize = patMedPOJOList.size
        return if (patMedSize > ModelUtil.dashboardMaxMedicationCount) {
            ModelUtil.dashboardMaxMedicationCount
        } else {
            patMedSize
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val patMedPOJO = patMedPOJOList[position]
        holder.onBind(patMedPOJO)
        holder.itemView.setOnClickListener {
            listener?.onMedicationSelected()
        }
    }

    fun setActiveMedicationListener(activeMedicationListener: ActiveMedicationListener) {
        this.listener = activeMedicationListener
    }

    fun setPatMedPOJOList(patMedPOJOList: List<PatMedPOJO>): Int {
        this.patMedPOJOList.clear()
        this.patMedPOJOList.addAll(patMedPOJOList)
        notifyDataSetChanged()
        return this.patMedPOJOList.size
    }

    inner class ViewHolder(binding: ItemActiveMedicationListBinding) : RecyclerView.ViewHolder(binding.root) {

        val observedOnTV: TextView = binding.activeMedicationObservedOnTV
        val drugNameTV: TextView = binding.activeMedicationDrugNameTV
        val directionsTV: TextView = binding.activeMedicationDirectionsTV
        // val separator = itemView.activeMedicationSeparator

        fun onBind(patMedPOJO: PatMedPOJO) {

            //  val separatorLP =  separator.layoutParams as ViewGroup.MarginLayoutParams
            // val marginLP =  drugNameTV.layoutParams as ViewGroup.MarginLayoutParams
            if (patMedPOJO.patMed?.startDate != null) {
                observedOnTV.text = patMedPOJO.patMed?.startDate?.toString(ModelUtil.charmDisplayDateFormat())
                observedOnTV.visibility = View.VISIBLE
                // marginLP.topMargin = observedOnTV.context.resources.getDimensionPixelSize(R.dimen.common_card_item_margin_vertical)
                //  separatorLP.topMargin = observedOnTV.context.resources.getDimensionPixelSize(R.dimen.active_medicaton_top_margin_date_separator)

            } else {
                observedOnTV.visibility = View.GONE
                //   marginLP.topMargin = 0
                //  separatorLP.topMargin = observedOnTV.context.resources.getDimensionPixelSize(R.dimen.active_medicaton_top_margin_separator)
            }

            if (patMedPOJO.patMed?.isAddedByPatient == false) {
                directionsTV.text = patMedPOJO.patMedExt.firstOrNull()?.directions
                drugNameTV.setRxDisplayName(patMedPOJO)
            } else {
                drugNameTV.text = patMedPOJO.drugDetail.firstOrNull()?.genericDrugName
                var directions = patMedPOJO.patMedPHRExt.firstOrNull()?.dosageWithUnit ?: ""
                if (directions.isNotBlank()) {
                    directions += ","
                }
                directions += patMedPOJO.patMedPHRExt.firstOrNull()?.intakeRoute ?: ""
                if (directions.isNotBlank()) {
                    directions += ","
                }
                directions += patMedPOJO.patMedPHRExt.firstOrNull()?.intake ?: ""
                if (directions.isNotBlank()) {
                    directions += ","
                }
                directions += patMedPOJO.patMedPHRExt.firstOrNull()?.intakeWhen ?: ""
                directionsTV.text = directions
            }
        }

    }

}

