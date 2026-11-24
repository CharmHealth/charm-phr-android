package com.charm.phr.mphr.ui.vaccine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemVaccineListBinding
import com.charm.phr.mphr.model.VaccineImmunizationPOJO
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil

class VaccineListAdapter(val authManager: AuthManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var vaccineList: ArrayList<VaccineImmunizationPOJO?> = arrayListOf()

    private var listener: VaccineSelectListener? = null

    private val viewTypeVaccine = 1
    private val viewTypeScroll = 2

    private var allowEdit = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == viewTypeVaccine) {
            ViewHolder(
                ItemVaccineListBinding.inflate(LayoutInflater.from(parent.context), parent, false), allowEdit
            )
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }
    }

    override fun getItemCount(): Int = vaccineList.size

    override fun getItemViewType(position: Int): Int {
        return if (vaccineList[position] != null) {
            viewTypeVaccine
        } else {
            viewTypeScroll
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vaccine = vaccineList[position]

        if (vaccine != null && holder is ViewHolder) {
            holder.onBind(vaccine)
            holder.vaccineListEditBtn.setOnClickListener {
                listener?.onVaccineSelect(vaccine)
            }

            holder.vaccineListShowMore.setOnClickListener {
                if (!vaccine.isViewOpened) {
                    vaccine.isViewOpened = true
                    holder.openItem(vaccine)
                }
            }


            if (vaccine.addedByPatient && allowEdit) {

                holder.itemView.setOnLongClickListener {

                    listener?.onVaccineDelete(vaccine, position)
                    return@setOnLongClickListener true
                }
            } else {
                holder.itemView.setOnLongClickListener {
                    return@setOnLongClickListener true
                }
            }
        }
    }

    fun setVaccineList(vaccineList: ArrayList<VaccineImmunizationPOJO>): Int {
        this.vaccineList.clear()
        this.vaccineList.addAll(vaccineList)
        allowEdit = ModelUtil.isPHREntryEnabled(authManager, ModelUtil.vaccineModelId)
        notifyDataSetChanged()
        return this.vaccineList.size
    }

    fun addVaccineList(vaccineList: ArrayList<VaccineImmunizationPOJO>) {
        this.vaccineList.addAll(vaccineList)
        this.notifyDataSetChanged()
    }

    fun removeVaccine(position: Int): Int {
        vaccineList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, vaccineList.size)

        return vaccineList.size
    }


    fun addInfiniteScroll() {
        vaccineList.add(null)
        notifyItemInserted(vaccineList.size - 1)
    }

    fun removeInfiniteScroll() {
        vaccineList.removeAt(vaccineList.size - 1)
        val scrollPosition = vaccineList.size
        notifyItemRemoved(scrollPosition)
    }

    fun setVaccineSelectListener(vaccineSelectListener: VaccineSelectListener) {
        listener = vaccineSelectListener
    }

    inner class ViewHolder(private val binding: ItemVaccineListBinding, val allowEdit: Boolean) : RecyclerView.ViewHolder(binding.root) {

        private val vaccineListDateTV: TextView = binding.vaccineListDateTV
        private val vaccineListNameTV: TextView = binding.vaccineListNameTV
        private val vaccineListDetailTV: TextView = binding.vaccineListDetailTV
        private val vaccineListProviderNameLayout: LinearLayout = binding.vaccineListProviderNameLayout
        private val vaccineListProviderNameTV: TextView = binding.vaccineListProviderNameTV
        private val vaccineListProviderNameLabelTV: TextView = binding.vaccineListProviderNameLabelTV
        private val vaccineListMoreLayout: LinearLayout = binding.vaccineListMoreLayout
        private val vaccineListMoreTV: TextView = binding.vaccineListMoreTV
        private val vaccineListMoreLabel: TextView = binding.vaccineListMoreLabel

        private val vaccineListConstraintLayout = binding.vaccineListConstraintLayout
        private val vaccineListNotesTV: TextView = binding.vaccineListNotesTV
        val vaccineListEditBtn: ImageView = binding.vaccineListEditBtn

        val vaccineListShowMore = binding.vaccineListShowMore

        fun onBind(vaccine: VaccineImmunizationPOJO) {

            if (vaccine.addedByPatient) {

                vaccineListMoreLabel.setText(R.string.vaccine_manufacturer_label)

                if (allowEdit) {
                    vaccineListConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)
                    vaccineListEditBtn.visibility = View.VISIBLE
                } else {
                    vaccineListConstraintLayout.setBackgroundResource(android.R.color.white)
                    vaccineListEditBtn.visibility = View.GONE
                }
                val immuneMap = vaccine.patientImmunizationMap
                if (immuneMap != null) {

                    if (immuneMap.patientImmunizationHistory?.vaccinesOn != null) {
                        vaccineListDateTV.text = immuneMap.patientImmunizationHistory?.vaccinesOn?.toString(ModelUtil.appointmentSpecificDateFormat())
                        vaccineListDateTV.visibility = View.VISIBLE
                    } else {
                        vaccineListDateTV.visibility = View.GONE
                    }

                    if (!immuneMap.immunizationList?.immunizationName.isNullOrBlank()) {
                        vaccineListNameTV.text = immuneMap.immunizationList?.immunizationName
                        vaccineListNameTV.visibility = View.VISIBLE
                    } else {
                        vaccineListNameTV.visibility = View.GONE
                    }

                    if (!immuneMap.patientImmunizationHistory?.givenBy.isNullOrBlank()) {
                        vaccineListProviderNameTV.text = immuneMap.patientImmunizationHistory?.givenBy
                        vaccineListProviderNameLabelTV.text = itemView.resources.getString(R.string.vaccine_provider_label)
                        vaccineListProviderNameLayout.visibility = View.VISIBLE
                    } else {
                        vaccineListProviderNameLayout.visibility = View.GONE
                    }

                    if (!immuneMap.patientImmunizationHistory?.manufacturer.isNullOrBlank()) {
                        vaccineListMoreTV.text = immuneMap.patientImmunizationHistory?.manufacturer
                        vaccineListMoreLayout.visibility = View.VISIBLE
                    } else {
                        vaccineListMoreLayout.visibility = View.GONE
                    }

                    vaccineListDetailTV.visibility = View.VISIBLE

                    if (!immuneMap.patientImmunizationHistory?.howAdministered.isNullOrBlank() && !immuneMap.immunizationList?.immunizationType.isNullOrBlank()) {

                        val vaccineType = if (immuneMap.immunizationList?.immunizationType == ModelUtil.vaccineTypeAdolescent) {
                            itemView.context.getString(R.string.vaccine_type_adolescent)
                        } else {
                            itemView.context.getString(R.string.vaccine_type_childhood)
                        }

                        vaccineListDetailTV.text = "${immuneMap.patientImmunizationHistory?.howAdministered} , $vaccineType"
                    } else if (!immuneMap.patientImmunizationHistory?.howAdministered.isNullOrBlank()) {
                        vaccineListDetailTV.text = immuneMap.patientImmunizationHistory?.howAdministered
                    } else if (!immuneMap.immunizationList?.immunizationType.isNullOrBlank()) {
                        vaccineListDetailTV.setText(
                            if (immuneMap.immunizationList?.immunizationType == ModelUtil.vaccineTypeAdolescent) {
                                R.string.vaccine_type_adolescent
                            } else {
                                R.string.vaccine_type_childhood
                            }
                        )
                    } else {
                        vaccineListDetailTV.visibility = View.GONE
                    }


                    if (!immuneMap.patientImmunizationHistory?.comments.isNullOrBlank()) {
                        vaccineListNotesTV.text = immuneMap.patientImmunizationHistory?.comments
                        vaccineListNotesTV.visibility = View.VISIBLE
                    } else {
                        vaccineListNotesTV.visibility = View.GONE
                    }

                    if (allowEdit) {
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(vaccineListConstraintLayout)


                        constraintSet.clear(vaccineListProviderNameLayout.id, ConstraintSet.END)

                        /*  if (vaccineListMoreLayout.visibility == View.VISIBLE || vaccineListNotesTV.visibility == View.VISIBLE) {
                              constraintSet.connect(vaccineListProviderNameLayout.id, ConstraintSet.END, vaccineListConstraintLayout.id, ConstraintSet.END)
                          }
                          else{*/
                        constraintSet.connect(vaccineListProviderNameLayout.id, ConstraintSet.END, vaccineListEditBtn.id, ConstraintSet.START)


                        constraintSet.clear(vaccineListMoreLayout.id, ConstraintSet.END)
                        constraintSet.connect(vaccineListMoreLayout.id, ConstraintSet.END, vaccineListEditBtn.id, ConstraintSet.START)

                        constraintSet.applyTo(vaccineListConstraintLayout)
                    } else {
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(vaccineListConstraintLayout)

                        constraintSet.clear(vaccineListProviderNameLayout.id, ConstraintSet.END)
                        constraintSet.connect(vaccineListProviderNameLayout.id, ConstraintSet.END, vaccineListConstraintLayout.id, ConstraintSet.END)

                        constraintSet.clear(vaccineListMoreLayout.id, ConstraintSet.END)
                        constraintSet.connect(vaccineListMoreLayout.id, ConstraintSet.END, vaccineListConstraintLayout.id, ConstraintSet.END)

                        constraintSet.applyTo(vaccineListConstraintLayout)
                    }

                }

            } else {
                vaccineListEditBtn.visibility = View.GONE
                vaccineListMoreLayout.visibility = View.GONE
                vaccineListConstraintLayout.setBackgroundResource(android.R.color.white)

                vaccineListMoreLabel.setText(R.string.practice_str)
                val constraintSet = ConstraintSet()
                constraintSet.clone(vaccineListConstraintLayout)

                constraintSet.clear(vaccineListProviderNameLayout.id, ConstraintSet.END)
                constraintSet.connect(vaccineListProviderNameLayout.id, ConstraintSet.END, vaccineListConstraintLayout.id, ConstraintSet.END)

                constraintSet.clear(vaccineListMoreLayout.id, ConstraintSet.END)
                constraintSet.connect(vaccineListMoreLayout.id, ConstraintSet.END, vaccineListConstraintLayout.id, ConstraintSet.END)

                constraintSet.applyTo(vaccineListConstraintLayout)
                val vaccineMap = vaccine.patientVaccineMap


                if (vaccineMap != null) {

                    if (vaccineMap.dateOfVaccination != null) {
                        vaccineListDateTV.text = vaccineMap.dateOfVaccination?.toString(ModelUtil.appointmentSpecificDateFormat())
                        vaccineListDateTV.visibility = View.VISIBLE
                    } else {
                        vaccineListDateTV.visibility = View.GONE
                    }


                    if (vaccineMap.administeredAmount.isNullOrBlank()) {
                        vaccineListNameTV.text = vaccine.patientVaccineMap?.vaccine?.vaccineName
                    } else {
                        vaccineListNameTV.text =
                            "${vaccine.patientVaccineMap?.vaccine?.vaccineName} ${vaccine.patientVaccineMap?.administeredAmount}"
                    }

                    if (!vaccineMap.orderingProvider?.fullName.isNullOrBlank()) {
                        vaccineListProviderNameTV.text = vaccineMap.orderingProvider?.fullName
                        vaccineListProviderNameLabelTV.text = itemView.resources.getString(R.string.vaccine_prescribed_by_label)
                        vaccineListProviderNameLayout.visibility = View.VISIBLE
                    } else {
                        vaccineListProviderNameLayout.visibility = View.GONE
                    }


                    if (!vaccineMap.vaccineManufactures?.manufacturerName.isNullOrBlank()) {
                        vaccineListMoreTV.text = vaccineMap.vaccineManufactures?.manufacturerName
                        vaccineListMoreLayout.visibility = View.VISIBLE
                    } else {
                        vaccineListMoreLayout.visibility = View.GONE
                    }

                    vaccineListNotesTV.visibility = View.GONE

                    vaccineListDetailTV.visibility = View.VISIBLE

                    if (!vaccineMap.route.isNullOrBlank() && !vaccineMap.site.isNullOrBlank()) {

                        vaccineListDetailTV.text = "${vaccineMap.route} , ${vaccineMap.site}"
                    } else if (!vaccineMap.route.isNullOrBlank()) {
                        vaccineListDetailTV.text = vaccineMap.route
                    } else if (!vaccineMap.site.isNullOrBlank()) {
                        vaccineListDetailTV.text = vaccineMap.site
                    } else {
                        vaccineListDetailTV.visibility = View.GONE
                    }
                }
            }


            if (!vaccine.addedByPatient || (vaccineListNotesTV.visibility == View.GONE && vaccineListMoreLayout.visibility == View.GONE)) {
                vaccine.isViewOpened = true
                vaccineListShowMore.visibility = View.GONE

                val layoutParam = vaccineListEditBtn.layoutParams as ConstraintLayout.LayoutParams
                layoutParam.bottomMargin = 0
                vaccineListEditBtn.layoutParams = layoutParam
            } else {
                if (vaccine.isViewOpened) {
                    openItem(vaccine)
                } else {
                    closeItem(vaccine)
                }
            }
        }

        fun openItem(vaccine: VaccineImmunizationPOJO) {
            vaccineListShowMore.visibility = View.GONE
            if (vaccine.addedByPatient) {
                if (vaccine.patientImmunizationMap?.patientImmunizationHistory?.manufacturer.isNullOrBlank()) {
                    vaccineListMoreLayout.visibility = View.GONE
                } else {
                    vaccineListMoreLayout.visibility = View.VISIBLE
                }

                if (vaccine.patientImmunizationMap?.patientImmunizationHistory?.comments.isNullOrBlank()) {
                    vaccineListNotesTV.visibility = View.GONE
                } else {
                    vaccineListNotesTV.visibility = View.VISIBLE
                }

                val layoutParam = vaccineListEditBtn.layoutParams as ConstraintLayout.LayoutParams
                layoutParam.bottomMargin = 0
                vaccineListEditBtn.layoutParams = layoutParam
            } else {
                vaccineListMoreLayout.visibility = View.GONE
                vaccineListNotesTV.visibility = View.GONE
                val layoutParam = vaccineListEditBtn.layoutParams as ConstraintLayout.LayoutParams
                layoutParam.bottomMargin = vaccineListEditBtn.context.resources.getDimensionPixelSize(R.dimen.card_padding)
                vaccineListEditBtn.layoutParams = layoutParam
            }
        }

        private fun closeItem(vaccine: VaccineImmunizationPOJO) {
            vaccineListShowMore.visibility = View.VISIBLE
            vaccineListMoreLayout.visibility = View.GONE
            vaccineListNotesTV.visibility = View.GONE

            val layoutParam = vaccineListEditBtn.layoutParams as ConstraintLayout.LayoutParams
            layoutParam.bottomMargin = 0
            vaccineListEditBtn.layoutParams = layoutParam
        }
    }

    interface VaccineSelectListener {
        fun onVaccineSelect(vaccine: VaccineImmunizationPOJO)
        fun onVaccineDelete(vaccine: VaccineImmunizationPOJO, position: Int)
    }
}