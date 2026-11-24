package com.charm.phr.mphr.ui.billing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemReceiptListBinding
import com.charm.phr.mphr.model.PHRReceipts
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil

class ReceiptListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var receiptList: ArrayList<PHRReceipts?> = arrayListOf()

    private var listener: ReceiptSelectListener? = null

    private val viewTypeReceipt = 1
    private val viewTypeScroll = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val v = if (viewType == viewTypeReceipt) {
            val binding = ItemReceiptListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }
        return v
    }

    override fun getItemCount(): Int = receiptList.size

    override fun getItemViewType(position: Int): Int {
        return if (receiptList[position] != null) {
            viewTypeReceipt
        } else {
            viewTypeScroll
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val phrReceipt = receiptList[position]

        if (phrReceipt != null && holder is ViewHolder) {
            holder.onBind(phrReceipt)
            holder.itemView.setOnClickListener {
                if (!phrReceipt.isRead) {
                    phrReceipt.isRead = true
                    holder.changeItemToRead()
                }
                listener?.onReceiptSelect(phrReceipt)
            }
        }
    }

    fun setReceiptList(receiptList: ArrayList<PHRReceipts>): Int {
        this.receiptList.clear()
        this.receiptList.addAll(receiptList)
        notifyDataSetChanged()
        return this.receiptList.size
    }

    fun addReceiptList(receiptList: ArrayList<PHRReceipts>) {
        this.receiptList.addAll(receiptList)
        this.notifyDataSetChanged()
    }

    fun addInfiniteScroll() {
        receiptList.add(null)
        notifyItemInserted(receiptList.size - 1)
    }

    fun removeInfiniteScroll() {
        receiptList.removeAt(receiptList.size - 1)
        val scrollPosition = receiptList.size
        notifyItemRemoved(scrollPosition)
    }

    fun setReceiptSelectListener(receiptSelectListener: ReceiptSelectListener) {
        listener = receiptSelectListener
    }

    inner class ViewHolder(binding: ItemReceiptListBinding) : RecyclerView.ViewHolder(binding.root) {

        val billDateTV: TextView = binding.receiptDateTV
        val facilityNameTV: TextView = binding.receiptFacilityNameTV
        val billNumberTV: TextView = binding.receiptBillNumTV
        val receiptAmtPaidTV: TextView = binding.receiptAmtPaidTV
        val receiptAmtValueTV: TextView = binding.receiptAmtValueTV
        val receiptAddedFromTV: TextView = binding.receiptAddedFromTV
        val receiptAddedFromLayout = binding.receiptAddedFromLayout
        val addedFromPhr = binding.root.context.getString(R.string.added_in_phr_str)


        fun onBind(phrReceipts: PHRReceipts) {

            if (phrReceipts.isRead) {
                changeItemToRead()
            } else {
                TextViewCompat.setTextAppearance(facilityNameTV, R.style.itemIsNotRead)
                TextViewCompat.setTextAppearance(receiptAmtPaidTV, R.style.itemIsNotRead)
            }

            billDateTV.text = phrReceipts.receiptDate?.toString(ModelUtil.charmDisplayDateFormat())
            facilityNameTV.text = phrReceipts.facility?.facilityName
            billNumberTV.text = phrReceipts.receiptNumber
            receiptAmtValueTV.text = "${phrReceipts.practiceCurrencySymbol} ${phrReceipts.receiptAmount}"
            if (phrReceipts.sentFromEhr == false) {
                receiptAddedFromLayout.visibility = View.VISIBLE
                receiptAddedFromTV.text = addedFromPhr
            }
        }

        fun changeItemToRead() {
            TextViewCompat.setTextAppearance(facilityNameTV, R.style.itemIsRead)
            TextViewCompat.setTextAppearance(receiptAmtPaidTV, R.style.itemIsRead)
        }

    }

    interface ReceiptSelectListener {
        fun onReceiptSelect(phrReceipts: PHRReceipts)
    }
}