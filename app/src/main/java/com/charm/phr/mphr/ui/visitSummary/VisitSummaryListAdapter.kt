package com.charm.phr.mphr.ui.visitSummary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemVisitSummaryListBinding
import com.charm.phr.mphr.model.PatientEncounterList
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import org.joda.time.DateTime

class VisitSummaryListAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var visitSummaryList: ArrayList<PatientEncounterList?> = arrayListOf()

    private var listener: VisitSummarySelectListener? = null

    private val viewTypeVisitSummary = 1
    private val viewTypeScroll = 2

    private var currentDate = DateTime.now()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == viewTypeVisitSummary) {
            val binding = ItemVisitSummaryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }
    }

    override fun getItemCount(): Int = visitSummaryList.size

    override fun getItemViewType(position: Int): Int {
        return if (visitSummaryList[position] != null){
            viewTypeVisitSummary
        }
        else{
            viewTypeScroll
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val visitSummary = visitSummaryList[position]

        if (visitSummary != null && holder is ViewHolder) {
            holder.onBind(visitSummary , currentDate)
            holder.itemView.setOnClickListener {
                listener?.onVisitSummarySelect(visitSummary)
                TextViewCompat.setTextAppearance(holder.visitSummaryListDateTV , R.style.itemIsRead)
                TextViewCompat.setTextAppearance(holder.visitSummaryListMemberAndFacilityNameTV , R.style.itemIsRead)
            }
        }
    }

    fun setVisitSummaryList(visitSummaryList:ArrayList<PatientEncounterList>):Int {
        this.visitSummaryList.clear()
        this.visitSummaryList.addAll(visitSummaryList)
        notifyDataSetChanged()
        currentDate = DateTime.now()
        return this.visitSummaryList.size
    }

    fun addVisitSummaryList(visitSummaryList: ArrayList<PatientEncounterList>){
        this.visitSummaryList.addAll(visitSummaryList)
        this.notifyDataSetChanged()
    }

    fun addInfiniteScroll(){
        visitSummaryList.add(null)
        notifyItemInserted(visitSummaryList.size - 1)
    }

    fun removeInfiniteScroll() {
        visitSummaryList.removeAt(visitSummaryList.size - 1)
        val scrollPosition =  visitSummaryList.size
        notifyItemRemoved(scrollPosition)
    }

    fun setVisitSummarySelectListener(visitSummarySelectListener: VisitSummarySelectListener){
        listener = visitSummarySelectListener
    }

    inner class ViewHolder(private val binding: ItemVisitSummaryListBinding): RecyclerView.ViewHolder(binding.root) {

    val visitSummaryListDateTV: TextView = binding.visitSummaryListDateTV
    val visitSummaryListMemberAndFacilityNameTV: TextView = binding.visitSummaryListMemberAndFacilityNameTV
    val visitSummaryListReasonTV: TextView = binding.visitSummaryListReasonTV


        fun onBind(visitSummary: PatientEncounterList , currentDate: DateTime){

            visitSummaryListDateTV.text = if (currentDate.year == visitSummary.date?.year){
                visitSummary.date?.toString(ModelUtil.appointmentSpecificDateFormat())
            }
            else{
                visitSummary.date?.toString(ModelUtil.appointmentSpecificDateYearFormat())
            }

            if (!visitSummary.facility?.facilityName.isNullOrBlank() && !visitSummary.member?.fullName.isNullOrBlank()) {

                visitSummaryListMemberAndFacilityNameTV.text = itemView.context.getString(
                    R.string.appointment_list_str_template,
                    visitSummary.facility?.facilityName,
                    UIUtil.formMemberName(visitSummary.member)
                )

            }
            else if (!visitSummary.member?.fullName.isNullOrBlank()){
                visitSummaryListMemberAndFacilityNameTV.text = UIUtil.formMemberName(visitSummary.member)
            }
            else if (!visitSummary.facility?.facilityName.isNullOrBlank()){
                visitSummaryListMemberAndFacilityNameTV.text = visitSummary.facility?.facilityName
            }
            else{
                visitSummaryListMemberAndFacilityNameTV.text = ""
            }


            if (visitSummary.reason.isNullOrBlank()) {
                visitSummaryListReasonTV.visibility = View.GONE
            }
            else{
                visitSummaryListReasonTV.visibility = View.VISIBLE

                val reasonStr = itemView.context.getString(R.string.two_string_template ,itemView.context.getString(R.string.book_appointment_reason_str) ,  visitSummary.reason)
                visitSummaryListReasonTV.text = UIUtil.makeFirstWordFontMedium(itemView.context , reasonStr)
            }
            if (!visitSummary.readStatus){
                TextViewCompat.setTextAppearance(visitSummaryListDateTV , R.style.itemIsNotRead)
                TextViewCompat.setTextAppearance(visitSummaryListMemberAndFacilityNameTV , R.style.itemIsNotRead)

            }
            else{
                TextViewCompat.setTextAppearance(visitSummaryListDateTV , R.style.itemIsRead)
                TextViewCompat.setTextAppearance(visitSummaryListMemberAndFacilityNameTV , R.style.itemIsRead)

            }

        }

    }

    interface VisitSummarySelectListener {
        fun onVisitSummarySelect(visitSummary: PatientEncounterList)
    }
}