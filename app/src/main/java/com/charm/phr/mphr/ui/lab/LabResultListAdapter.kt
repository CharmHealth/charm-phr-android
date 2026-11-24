package com.charm.phr.mphr.ui.lab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.databinding.ItemLabResultListBinding
import com.charm.phr.mphr.model.MedicalRecordGroup
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil.Companion.setInterpretationColor

class LabResultListAdapter(val authManager: AuthManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var medicalRecordGroupList: ArrayList<MedicalRecordGroup?> = arrayListOf()

    private var labResultClickListener: LabResultClickListener? = null

    private val viewTypeMedicalRecordGroup = 1
    private val viewTypeScroll = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == viewTypeMedicalRecordGroup) {
            val binding = ItemLabResultListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }
    }

    override fun getItemCount(): Int = medicalRecordGroupList.size

    override fun getItemViewType(position: Int): Int {
        return if (medicalRecordGroupList[position] != null) {
            viewTypeMedicalRecordGroup
        } else {
            viewTypeScroll
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val medicalRecordGroup = medicalRecordGroupList[position]

        if (holder is ViewHolder && medicalRecordGroup != null) {

            holder.onBind(medicalRecordGroup)

            holder.itemView.setOnClickListener {
                labResultClickListener?.onLabResultClick(medicalRecordGroup)
            }

            holder.labResultShowMore.setOnClickListener {

                if (!medicalRecordGroup.isViewOpened) {
                    medicalRecordGroup.isViewOpened = true
                    holder.openItem(medicalRecordGroup)
                }
            }
        }
    }

    fun setLabResultList(medicalRecordGroupList: ArrayList<MedicalRecordGroup>): Int {
        this.medicalRecordGroupList.clear()
        this.medicalRecordGroupList.addAll(medicalRecordGroupList)
        notifyDataSetChanged()
        return this.medicalRecordGroupList.size
    }

    fun addLabResultList(medicalRecordGroupList: ArrayList<MedicalRecordGroup>) {
        this.medicalRecordGroupList.addAll(medicalRecordGroupList)
        this.notifyDataSetChanged()
    }

    fun addInfiniteScroll() {
        medicalRecordGroupList.add(null)
        notifyItemInserted(medicalRecordGroupList.size - 1)
    }

    fun removeInfiniteScroll() {
        medicalRecordGroupList.removeAt(medicalRecordGroupList.size - 1)
        val scrollPosition = medicalRecordGroupList.size
        notifyItemRemoved(scrollPosition)
    }

    fun setLabResultClickListener(listener: LabResultClickListener) {
        labResultClickListener = listener
    }

    inner class ViewHolder(val binding: ItemLabResultListBinding) : RecyclerView.ViewHolder(binding.root) {

    private val labResultDateTV: TextView = binding.labResultDateTV
    private val labResultLabNameTV: TextView = binding.labResultLabNameTV
    private val labResultTestNameTV: TextView = binding.labResultTestNameTV
    private val labResultInterpretationTv: TextView = binding.labResultInterpretationTv
    private val labResultEditBtn: ImageView = binding.labResultEditBtn
    val labResultShowMore: TextView = binding.labResultShowMore
    private val labResultStatusLayout: LinearLayout = binding.labResultStatusLayout


        private val labResultStatusTV = binding.labResultStatusTV

        fun onBind(medicalRecordGroup: MedicalRecordGroup) {

            val date = medicalRecordGroup.medicalRecordEntryList.firstOrNull()?.date?.toString(ModelUtil.charmDisplayDateFormat())
            if (date?.isNotBlank() == true) {
                labResultDateTV.text = date
            } else {
                labResultDateTV.visibility = View.GONE
            }
            labResultLabNameTV.text = medicalRecordGroup.labName

            val testCount = medicalRecordGroup.medicalRecordEntryList.size

            if (testCount <= 2) {
                var testNameStr = medicalRecordGroup.medicalRecordEntryList[0].medicalRecord?.medicalRecordName ?: ""

                if (testCount == 2) {
                    testNameStr += "\n${medicalRecordGroup.medicalRecordEntryList[1].medicalRecord?.medicalRecordName ?: ""}"
                }

                labResultTestNameTV.text = testNameStr

            } else {

                val testNameStr = "${medicalRecordGroup.medicalRecordEntryList[0].medicalRecord?.medicalRecordName ?: ""}\n${medicalRecordGroup.medicalRecordEntryList[1].medicalRecord?.medicalRecordName ?: ""}"
                labResultTestNameTV.text = testNameStr

            }

            if (!medicalRecordGroup.reportStatus.isNullOrBlank() && !medicalRecordGroup.reportStatus.equals("final", true)) {
                labResultStatusTV.text = medicalRecordGroup.reportStatus
            } else {
                labResultStatusTV.text = ""
            }


            if (medicalRecordGroup.medicalRecordEntryList.firstOrNull()?.isImportedByPatient == true) {


                if (!labResultStatusTV.text.isNullOrBlank() || testCount > 2) {
                    if (medicalRecordGroup.isViewOpened) {
                        openItem(medicalRecordGroup)
                    } else {
                        closeItem()
                    }
                } else {
                    medicalRecordGroup.isViewOpened = true
                    labResultStatusLayout.visibility = View.GONE
                    labResultShowMore.visibility = View.GONE
                }

                labResultEditBtn.visibility = View.GONE
                labResultEditBtn.setOnClickListener {}
                labResultInterpretationTv.visibility = View.GONE

            } else {

                labResultInterpretationTv.visibility = View.VISIBLE
                labResultEditBtn.visibility = View.GONE


                val interpretation = medicalRecordGroup.medicalRecordEntryList.firstOrNull()?.interpretation ?: ""
                if (interpretation.isNotBlank()) {
                    labResultInterpretationTv.text = interpretation
                    labResultInterpretationTv.setInterpretationColor(interpretation)
                }

                labResultStatusLayout.visibility = View.GONE

//                if (medicalRecordGroup.isViewOpened) {
//                    openItem(medicalRecordGroup)
//                } else {
//                    closeItem()
//                }
            }

        }

        fun openItem(medicalRecordGroup: MedicalRecordGroup) {

            labResultShowMore.visibility = View.GONE
            labResultTestNameTV.text = medicalRecordGroup.medicalRecordEntryList.mapNotNull { it.medicalRecord?.medicalRecordName }.joinToString("\n")

            if (labResultStatusTV.text.isNullOrBlank()) {
                labResultStatusLayout.visibility = View.GONE
            } else {
                labResultStatusLayout.visibility = View.VISIBLE
            }
        }

        fun closeItem() {
            labResultShowMore.visibility = View.VISIBLE
            labResultStatusLayout.visibility = View.GONE
        }
    }

    interface LabResultClickListener {
        fun onLabResultClick(medicalRecordGroup: MedicalRecordGroup)
    }
}