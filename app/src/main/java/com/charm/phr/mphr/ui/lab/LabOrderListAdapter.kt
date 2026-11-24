package com.charm.phr.mphr.ui.lab

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemLabOrderListBinding
import com.charm.phr.mphr.model.LabOrderDetail
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil

class LabOrderListAdapter(val authManager: AuthManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var labOrderDetailList: ArrayList<LabOrderDetail?> = arrayListOf()

    private var labResultClickListener: LabOrderClickListener? = null

    private val viewTypeLabOrderPOJO = 1
    private val viewTypeScroll = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == viewTypeLabOrderPOJO) {
            val binding = ItemLabOrderListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }
    }

    override fun getItemCount(): Int = labOrderDetailList.size

    override fun getItemViewType(position: Int): Int {
        return if (labOrderDetailList[position] != null) {
            viewTypeLabOrderPOJO
        } else {
            viewTypeScroll
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val labOrderDetail = labOrderDetailList[position]

        if (holder is ViewHolder && labOrderDetail != null) {

            holder.onBind(labOrderDetail)

            holder.itemView.setOnClickListener {
                if (!labOrderDetail.readStatus) {
                    labOrderDetail.readStatus = true
                    holder.changeItemToRead()
                }
                labResultClickListener?.onLabOrderClick(labOrderDetail)
            }
        }
    }

    fun setLabOrderList(labOrderPOJOList: ArrayList<LabOrderDetail>): Int {
        this.labOrderDetailList.clear()
        this.labOrderDetailList.addAll(labOrderPOJOList)
        notifyDataSetChanged()
        return this.labOrderDetailList.size
    }

    fun addLabOrderList(labOrderPOJOList: ArrayList<LabOrderDetail>) {
        this.labOrderDetailList.addAll(labOrderPOJOList)
        this.notifyDataSetChanged()
    }

    fun addInfiniteScroll() {
        labOrderDetailList.add(null)
        notifyItemInserted(labOrderDetailList.size - 1)
    }

    fun removeInfiniteScroll() {
        labOrderDetailList.removeAt(labOrderDetailList.size - 1)
        val scrollPosition = labOrderDetailList.size
        notifyItemRemoved(scrollPosition)
    }

    fun setLabOrderClickListener(listener: LabOrderClickListener) {
        labResultClickListener = listener
    }

    inner class ViewHolder(val binding: ItemLabOrderListBinding) : RecyclerView.ViewHolder(binding.root) {

    private val labOrderDateTV: TextView = binding.labOrderDateTV
    private val labOrderLabPDFNameTV: TextView = binding.labOrderLabPDFNameTV
    private val labOrderMemberNameTV: TextView = binding.labOrderMemberNameTV

        fun onBind(labOrderDetail: LabOrderDetail) {

            labOrderDateTV.text = labOrderDetail.date?.toString(
                ModelUtil.charmDisplayDateFormat()
            )


            labOrderLabPDFNameTV.text = labOrderDetail.fileName

            labOrderMemberNameTV.text = UIUtil.formMemberName(labOrderDetail.member)

            if (labOrderDetail.readStatus) {
                changeItemToRead()
            } else {
                changeItemToNotRead()
            }

        }

        fun changeItemToRead() {
            TextViewCompat.setTextAppearance(labOrderLabPDFNameTV, R.style.itemIsRead)
            TextViewCompat.setTextAppearance(labOrderMemberNameTV, R.style.itemIsRead)
            TextViewCompat.setTextAppearance(labOrderDateTV, R.style.itemIsRead)
        }

        private fun changeItemToNotRead() {
            TextViewCompat.setTextAppearance(labOrderLabPDFNameTV, R.style.itemIsNotRead)
            TextViewCompat.setTextAppearance(labOrderMemberNameTV, R.style.itemIsNotRead)
            TextViewCompat.setTextAppearance(labOrderDateTV, R.style.itemIsNotRead)

        }

    }

    interface LabOrderClickListener {
        fun onLabOrderClick(labOrderDetail: LabOrderDetail)
    }
}