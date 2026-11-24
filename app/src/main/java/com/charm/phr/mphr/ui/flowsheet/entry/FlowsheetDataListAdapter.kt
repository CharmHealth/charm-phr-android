package com.charm.phr.mphr.ui.flowsheet.entry

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.databinding.ItemFlowsheetDataListBinding
import com.charm.phr.mphr.model.PHRFlowsheets
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.setVerticalItemMargin

class FlowsheetDataListAdapter : RecyclerView.Adapter<FlowsheetDataListAdapter.ViewHolder>() {

    private val flowsheetDataList = arrayListOf<PHRFlowsheets>()

    private var listener: FlowsheetEntryListSelectListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFlowsheetDataListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = flowsheetDataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val phrFlowsheet = flowsheetDataList[position]

        holder.bindView(phrFlowsheet)

        holder.itemView.setOnClickListener {
            listener?.onFlowsheetEntryListClick(phrFlowsheet)
        }
    }

    fun setFlowsheetEntryList(flowsheetDataList: ArrayList<PHRFlowsheets>): Int {
        this.flowsheetDataList.clear()
        this.flowsheetDataList.addAll(flowsheetDataList)
        notifyDataSetChanged()

        return flowsheetDataList.size
    }


    inner class ViewHolder(val binding: ItemFlowsheetDataListBinding) : RecyclerView.ViewHolder(binding.root) {

    private val flowsheetEntryCardView: CardView = binding.flowsheetDataListCardView
    private val flowsheetDataListDateTV: TextView = binding.flowsheetDataListDateTV

        fun bindView(phrFlowsheet: PHRFlowsheets) {

            val index = flowsheetDataList.indexOf(phrFlowsheet)
            setVerticalItemMargin(flowsheetEntryCardView , index , itemCount)
            
            flowsheetDataListDateTV.text = phrFlowsheet.date?.toString(ModelUtil.charmFlowsheetDataDateFormatter())
        }

    }

    fun setFlowsheetEntryListSelectListener(flowsheetDataListSelectListener: FlowsheetEntryListSelectListener) {
        listener = flowsheetDataListSelectListener
    }

    interface FlowsheetEntryListSelectListener {
        fun onFlowsheetEntryListClick(phrFlowsheet: PHRFlowsheets)
    }

}
