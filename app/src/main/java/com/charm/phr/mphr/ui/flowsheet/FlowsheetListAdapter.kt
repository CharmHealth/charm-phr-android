package com.charm.phr.mphr.ui.flowsheet

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemFlowsheetListBinding
import com.charm.phr.mphr.model.Flowsheets
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.setVerticalItemMargin

class FlowsheetListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val flowsheetList = arrayListOf<Flowsheets?>()

    private var listener: FlowsheetListSelectListener? = null


    private val viewTypeFlowsheet = 1
    private val viewTypeScroll = 2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {
        return if (viewType == viewTypeFlowsheet) {
            val binding = ItemFlowsheetListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }
    }

    override fun getItemCount(): Int = flowsheetList.size

    override fun getItemViewType(position: Int): Int {
        return if (flowsheetList[position] != null){
            viewTypeFlowsheet
        }
        else{
            viewTypeScroll
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val flowsheets = flowsheetList[position]

        if (flowsheets != null && holder is FlowsheetListAdapter.ViewHolder) {
            holder.bindView(flowsheets)
            holder.itemView.setOnClickListener {
                listener?.onFlowsheetListClick(flowsheets)
            }
        }
    }

    fun setFlowsheetList(flowsheetList: ArrayList<Flowsheets>): Int {
        this.flowsheetList.clear()
        this.flowsheetList.addAll(flowsheetList)
        notifyDataSetChanged()

        return flowsheetList.size
    }

    fun addFlowsheetList(flowsheetList: ArrayList<Flowsheets>){
        this.flowsheetList.addAll(flowsheetList)
        this.notifyDataSetChanged()
    }

    fun removeFlowsheet(position: Int): Int{
        flowsheetList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, flowsheetList.size)

        return flowsheetList.size
    }


    fun addInfiniteScroll(){
        flowsheetList.add(null)
        notifyItemInserted(flowsheetList.size - 1)
    }

    fun removeInfiniteScroll() {
        flowsheetList.removeAt(flowsheetList.size - 1)
        val scrollPosition =  flowsheetList.size
        notifyItemRemoved(scrollPosition)
    }


    inner class ViewHolder(private val binding: ItemFlowsheetListBinding) : RecyclerView.ViewHolder(binding.root) {

    private val flowsheetCardView: CardView = binding.flowsheetListCardView
    private val flowsheetListName: TextView = binding.flowsheetListNameTV
    private val flowsheetListFrequency: TextView = binding.flowsheetListFrequencyTV

        fun bindView(flowsheets: Flowsheets) {

            val index = flowsheetList.indexOf(flowsheets)
            setVerticalItemMargin(flowsheetCardView , index , itemCount)//TODO: Add in every list view
            flowsheetListName.text = flowsheets.name
            flowsheetListFrequency.text = UIUtil.makeSpannableStringForFlowsheetFrequency(itemView.context ,  itemView.context.getString(R.string.flowsheet_list_frequency_msg, flowsheets.fillFrequency))
        }

    }

    fun setFlowsheetListSelectListener(flowsheetListSelectListener: FlowsheetListSelectListener) {
        listener = flowsheetListSelectListener
    }

    interface FlowsheetListSelectListener {
        fun onFlowsheetListClick(flowsheets: Flowsheets)
    }

}

