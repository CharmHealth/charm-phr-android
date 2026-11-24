package com.charm.phr.mphr.ui.prescription


import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemSupplementListBinding
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.OnMultipleSelectChangeListener
import com.charm.phr.mphr.util.UIUtil
import org.joda.time.DateTime


class SupplementListAdapter(val authManager: AuthManager) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var supplementList: ArrayList<PatientSupplements?> = arrayListOf()

    private var supplementListener: SupplementListener? = null
    private var supplementMultiSelectListener: OnMultipleSelectChangeListener? = null

    private var isMultiSelectEnabled = false

    private val viewTypeSupplement = 1
    private val viewTypeScroll = 2

    private var allowEdit = true

    var isFromReminder = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder = if (viewType == viewTypeSupplement) {
            val binding = ItemSupplementListBinding.inflate(inflater, parent, false)
            ViewHolder(binding, allowEdit)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }

        return viewHolder
    }

    override fun getItemCount(): Int = supplementList.size

    override fun getItemViewType(position: Int): Int {
        return if (supplementList[position] != null) {
            viewTypeSupplement
        } else {
            viewTypeScroll
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val supplements = supplementList[position]

        if (supplements != null && holder is ViewHolder) {

            holder.onBind(supplements)
            if (isFromReminder) {
                holder.supplementListRefillBtn.visibility = View.GONE
                holder.supplementListEditBtn.visibility = View.GONE
                holder.itemView.setOnClickListener {
                    supplementListener?.onReminderClick(supplements)
                }
            } else {
                holder.itemView.setOnLongClickListener {
                    if (supplements.isAddedByPatient && allowEdit) {
                        /* if (!isMultiSelectEnabled) {
                             isMultiSelectEnabled = true
                             supplementMultiSelectListener?.onMultiSelect(true)
                             supplements.isSelected = true
                             holder.changeBgToSelected()
                         } else {
                             if (!supplements.isSelected) {
                                 supplements.isSelected = true
                                 holder.changeBgToSelected()
                             } else {
                                 supplements.isSelected = true
                                 holder.changeBgToSelected()
                             }
                         }*/
                        supplementListener?.onDelete(supplements, position)
                    }
                    return@setOnLongClickListener true
                }

                /* holder.itemView.setOnClickListener {
                     if (supplements.isAddedByPatient) {
                         if (isMultiSelectEnabled) {
                             if (supplements.isSelected) {
                                 supplements.isSelected = false
                                 holder.changeBgToUnSelected()
                                 checkAllIsUnchecked()
                             } else {
                                 supplements.isSelected = true
                                 holder.changeBgToSelected()
                             }
                         }

                     }
                 }*/

                holder.supplementListRefillBtn.setOnClickListener {
                    if (!isMultiSelectEnabled) {
                        supplementListener?.onRefill(supplements)
                    }
                }

                holder.supplementListEditBtn.setOnClickListener {
                    if (!isMultiSelectEnabled) {
                        supplementListener?.onEdit(supplements)
                    }
                }

                //            holder.supplementListShowMore.setOnClickListener {
                //                if (!isMultiSelectEnabled) {
                //                    supplements.isViewOpened = true
                //                    holder.openItem(supplements)
                //                }
                //            }
            }
        }
    }

    fun setSupplements(supplementList: ArrayList<PatientSupplements>): Int {
        this.supplementList.clear()
        this.supplementList.addAll(supplementList)
        this.notifyDataSetChanged()
        allowEdit = ModelUtil.isPHREntryEnabled(authManager, ModelUtil.supplementModelId)
        return this.supplementList.size
    }

    fun addSupplements(supplementList: ArrayList<PatientSupplements>) {
        this.supplementList.addAll(supplementList)
        this.notifyDataSetChanged()
    }

    fun addInfiniteScroll() {
        supplementList.add(null)
        notifyItemInserted(supplementList.size - 1)
    }

    fun removeInfiniteScroll() {
        supplementList.removeAt(supplementList.size - 1)
        val scrollPosition = supplementList.size
        notifyItemRemoved(scrollPosition)
    }

    fun removeSupplement(position: Int): Int {
        supplementList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, supplementList.size)

        return supplementList.size
    }

    fun disableMultiSelect() {
        isMultiSelectEnabled = false
        supplementList.forEach { supplement ->
            supplement?.isSelected = false
        }
        notifyDataSetChanged()
    }

    fun setSupplementSelectListener(listener: SupplementListener) {
        supplementListener = listener
    }

    fun setOnMultipleSelectChangeListener(listener: OnMultipleSelectChangeListener) {
        supplementMultiSelectListener = listener
    }

    inner class ViewHolder(val binding: ItemSupplementListBinding, val allowEdit: Boolean) : RecyclerView.ViewHolder(binding.root) {

    private val supplementDateTV: TextView = binding.supplementListDateTV
    private val supplementListNameTV: TextView = binding.supplementListNameTV
    private val supplementListDirectionTV: TextView = binding.supplementListDirectionTV
    private val supplementListMoreLayout1: LinearLayout = binding.supplementListMoreLayout1

    // val supplementListPracticeLayout = binding.supplementListPracticeLayout
    // val supplementListPracticeTV = binding.practiceDetailTV
    private val supplementListMoreLabel1: TextView = binding.supplementListMoreLabel1

    private val supplementListMoreValue1: TextView = binding.supplementListMoreValue1

    val supplementListRefillBtn: TextView = binding.supplementListRefillBtn
    val supplementListEditBtn: ImageView = binding.supplementListEditBtn

    //   val supplementListShowMore: TextView = binding.supplementListShowMore
        private val supplementConstraintLayout: ConstraintLayout =
        binding.supplementListConstraintLayout

        private val handler = Handler(Looper.getMainLooper())

        private val runnable = Runnable {
            supplementConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)
        }

        fun onBind(patientSupplements: PatientSupplements) {

            if (patientSupplements.startDate != null) {
                supplementDateTV.text =
                    patientSupplements.startDate?.toString(ModelUtil.charmDisplayDateFormat())
                supplementDateTV.visibility = View.VISIBLE
            } else {
                supplementDateTV.visibility = View.INVISIBLE
                supplementDateTV.text = DateTime.now().toString(ModelUtil.charmDisplayDateFormat())
            }

            var supplementsName = patientSupplements.supplement?.supplementName

            val noOfIntake =
                patientSupplements.patientSupplementMap?.patientSupplementHistory?.numberOfIntake
            var intakeUnit =
                patientSupplements.patientSupplementMap?.patientSupplementHistory?.intakeUnit ?: ""
            if (intakeUnit == "-") {
                intakeUnit = ""
            }
            if (noOfIntake != null && (noOfIntake.trim().toFloatOrNull() ?: -1f) > 0f) {
                supplementsName += " $noOfIntake $intakeUnit"
            }

            supplementListNameTV.text = supplementsName



            if (!patientSupplements.isAddedByPatient) {


                if (!patientSupplements.allowRefill || patientSupplements.isActive != true || patientSupplements.member?.memberId.isNullOrBlank()) {
                    supplementListRefillBtn.visibility = View.GONE
                } else {
                    supplementListRefillBtn.visibility = View.VISIBLE
                }

                supplementListEditBtn.visibility = View.GONE

                val memberName = UIUtil.formMemberName(patientSupplements.member)
                if (memberName.isNotBlank()) {
                    supplementListMoreLabel1.setText(R.string.prescription_prescribed_by_label)
                    supplementListMoreValue1.text = memberName
                } else {
                    supplementListMoreLayout1.visibility = View.GONE
                }

                //  supplementListPracticeTV.text = ModelUtil.getPracticeDetail(authManager, patientSupplements.practiceId)

                //  supplementListShowMore.visibility = View.VISIBLE
//                if (patientSupplements.isViewOpened) {
//                    openItem(patientSupplements)
//                } else {
//                    closeItem(patientSupplements)
//                }


                val instructions =
                    patientSupplements.patientSupplementMap?.patientSupplementHistory?.instructions
                        ?: ""

                if (instructions.isNotBlank()) {
                    supplementListDirectionTV.text = instructions
                    supplementListDirectionTV.visibility = View.VISIBLE
                } else {
                    supplementListDirectionTV.visibility = View.GONE
                    // removeRefillBottomConstraint()
                }

                supplementConstraintLayout.setBackgroundResource(android.R.color.white)
            } else {

                if (allowEdit) {
                    supplementConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)
                    supplementListEditBtn.visibility = View.VISIBLE
                } else {
                    supplementConstraintLayout.setBackgroundResource(android.R.color.white)
                    supplementListEditBtn.visibility = View.GONE
                }

                supplementListRefillBtn.visibility = View.GONE


                // supplementListPracticeLayout.visibility = View.GONE


                supplementListMoreLabel1.setText(R.string.prescription_stop_date_label)
                supplementListMoreValue1.text =
                    patientSupplements.stopDate?.toString(ModelUtil.charmDisplayDateFormat())

                if (patientSupplements.stopDate == null) {
                    supplementListMoreLayout1.visibility = View.GONE
                }

//                if (patientSupplements.stopDate != null) {
//                    if (patientSupplements.isViewOpened) {
//                        openItem(patientSupplements)
//                    } else {
//                        closeItem(patientSupplements)
//                    }
//                } else {
//                 //   supplementListMoreLayout1.visibility = View.GONE
//                 //   supplementListShowMore.visibility = View.GONE
//                }


                supplementListDirectionTV.text =
                    UIUtil.formDirectionsForSupplement(patientSupplements)
            }

        }


        fun changeBgToSelected() {

            handler.removeCallbacks(runnable)
            supplementConstraintLayout.setBackgroundResource(R.color.cardSelectedBgColor)
        }

        fun changeBgToUnSelected() {

            supplementConstraintLayout.setBackgroundResource(android.R.color.white)
            handler.postDelayed(runnable, ModelUtil.delayedTimingForRippleChange)
        }

//        fun openItem(patientSupplements: PatientSupplements) {
//
//
//            val refillAndEditParams = supplementListRefillAndEditLayout.layoutParams as ConstraintLayout.LayoutParams
//            refillAndEditParams.bottomMargin = itemView.resources.getDimensionPixelSize(R.dimen.card_padding)
//            supplementListRefillAndEditLayout.layoutParams = refillAndEditParams
//
//            supplementListShowMore.visibility = View.GONE
//            if (!patientSupplements.isAddedByPatient) {
//                if (UIUtil.formMemberName(patientSupplements.member).isNotBlank()) {
//                    supplementListMoreLayout1.visibility = View.VISIBLE
//                } else {
//                    supplementListMoreLayout1.visibility = View.GONE
//                }
//
//                //  supplementListPracticeLayout.visibility = View.VISIBLE
//            } else {
//
//                supplementListMoreLayout1.visibility = View.VISIBLE
//                //  supplementListPracticeLayout.visibility = View.GONE
//            }
//
//            // checkAndSetRefillConstraint(patientSupplements, true)
//
//        }
//
//        fun closeItem(patientSupplements: PatientSupplements) {
//            try {
//                val refillAndEditParams = supplementListRefillAndEditLayout.layoutParams as ConstraintLayout.LayoutParams
//                refillAndEditParams.bottomMargin = 0
//                supplementListRefillAndEditLayout.layoutParams = refillAndEditParams
//
//                supplementListMoreLayout1.visibility = View.GONE
//                supplementListShowMore.visibility = View.VISIBLE
//                // supplementListPracticeLayout.visibility = View.GONE
//
//                //  checkAndSetRefillConstraint(patientSupplements, false)
//
//            } catch (e: Exception) {
//
//            }
//        }

//        fun checkAndSetRefillConstraint(patientSupplements: PatientSupplements, isOpen: Boolean) {
//            val instructions = patientSupplements.patientSupplementMap?.patientSupplementHistory?.instructions ?: ""
//            if (isOpen) {
//                if (instructions.isBlank()) {
//                    removeRefillBottomConstraint()
//                    supplementConstraintLayout.connectConstraint(supplementListPracticeLayout.id, ConstraintSet.TOP, supplementListRefillAndEditLayout.id, ConstraintSet.BOTTOM)
//                } else {
//                    addRefillBottomConstraint()
//                    supplementConstraintLayout.connectConstraint(supplementListPracticeLayout.id, ConstraintSet.TOP, supplementListMoreLayout1.id, ConstraintSet.BOTTOM)
//                }
//            } else {
//                if (instructions.isNotBlank()) {
//                    addRefillBottomConstraint()
//                    supplementListRefillAndEditLayout.updateLayoutParams<ConstraintLayout.LayoutParams> { verticalBias = 1f }
//                } else {
//                    removeRefillBottomConstraint()
//                }
//            }
//        }
//
//        fun removeRefillBottomConstraint() {
//            supplementConstraintLayout.removeConstraint(supplementListRefillAndEditLayout.id, ConstraintSet.BOTTOM)
//        }
//
//        fun addRefillBottomConstraint() {
//            supplementConstraintLayout.connectConstraint(supplementListRefillAndEditLayout.id, ConstraintSet.BOTTOM, supplementListPracticeLayout.id, ConstraintSet.TOP)
//        }

    }


    interface SupplementListener {
        fun onEdit(supplements: PatientSupplements)

        fun onRefill(supplements: PatientSupplements)

        fun onDelete(supplements: PatientSupplements, position: Int)

        fun onReminderClick(supplements: PatientSupplements)
    }

}