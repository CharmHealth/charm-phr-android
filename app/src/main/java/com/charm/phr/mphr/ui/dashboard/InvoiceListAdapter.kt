package com.charm.phr.mphr.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemHomePageInvoiceBinding
import com.charm.phr.mphr.model.PHRBills
import com.charm.phr.mphr.util.ModelUtil

class InvoiceListAdapter : RecyclerView.Adapter<InvoiceListAdapter.ViewHolder>() {

    private val phrBillList: ArrayList<PHRBills> = arrayListOf()
    private var listener: com.charm.phr.mphr.ui.billing.InvoiceListAdapter.InvoiceSelectListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomePageInvoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        val patMedSize = phrBillList.size
        return if (patMedSize > ModelUtil.dashboardMaxInvoiceCount) {
            ModelUtil.dashboardMaxInvoiceCount
        } else {
            patMedSize
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val phrBill = phrBillList[position]
        holder.onBind(phrBill)
        holder.itemView.setOnClickListener {
            listener?.onInvoiceSelect(phrBill)
        }
    }

    fun setInvoiceSelectListener(invoiceSelectListener: com.charm.phr.mphr.ui.billing.InvoiceListAdapter.InvoiceSelectListener) {
        listener = invoiceSelectListener
    }

    fun setInvoiceList(phrBillList: List<PHRBills>): Int {
        this.phrBillList.clear()
        this.phrBillList.addAll(phrBillList)
        notifyDataSetChanged()
        return this.phrBillList.size
    }


    inner class ViewHolder(private val binding: ItemHomePageInvoiceBinding) : RecyclerView.ViewHolder(binding.root) {

        val dateTv = binding.homePageInvoiceDate
        val monthTv = binding.homePageInvoiceMonth
        val headingTv = binding.homePageInvoiceHeading
        val amountTv = binding.homePageInvoiceAmount
        val facilityTv = binding.homePageInvoiceFacilityName
        fun onBind(phrBill: PHRBills) {

            val pair = ModelUtil.getDueAndPaymentRequested(phrBill)
            val dueAmount = pair.first

            dateTv.text = phrBill.billDate?.dayOfMonth?.toString() ?: ""
            monthTv.text = phrBill.billDate?.toString(ModelUtil.monthShortFormat)?.toString() ?: ""

            headingTv.text = itemView.context.getString(R.string.payment_requested)

            amountTv.text = "${phrBill.practiceCurrencySymbol} ${dueAmount}"
            amountTv.setTextColor(ContextCompat.getColor(itemView.context, R.color.allergyListSeveritySevereTxtColor))

            facilityTv.text = phrBill.facility?.facilityName ?: ""

        }


    }


}