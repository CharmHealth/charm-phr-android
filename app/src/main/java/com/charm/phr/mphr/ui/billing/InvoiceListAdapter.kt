package com.charm.phr.mphr.ui.billing

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemInvoiceListBinding
import com.charm.phr.mphr.model.PHRBills
import com.charm.phr.mphr.ui.components.MDmPHRConstraintLayout
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil

class InvoiceListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var invoiceList: ArrayList<PHRBills?> = arrayListOf()

    private var listener: InvoiceSelectListener? = null

    private val viewTypeInvoice = 1
    private val viewTypeScroll = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == viewTypeInvoice) {
            val binding =
                ItemInvoiceListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }
    }

    override fun getItemCount(): Int = invoiceList.size

    override fun getItemViewType(position: Int): Int {
        return if (invoiceList[position] != null) {
            viewTypeInvoice
        } else {
            viewTypeScroll
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val phrBill = invoiceList[position]

        if (phrBill != null && holder is ViewHolder) {
            holder.onBind(phrBill)
            holder.itemView.setOnClickListener {
                listener?.onInvoiceSelect(phrBill)
            }
        }
    }

    fun setInvoiceList(invoiceList: ArrayList<PHRBills>): Int {
        this.invoiceList.clear()
        this.invoiceList.addAll(invoiceList)
        notifyDataSetChanged()
        return this.invoiceList.size
    }

    fun addInvoiceList(invoiceList: ArrayList<PHRBills>) {
        this.invoiceList.addAll(invoiceList)
        this.notifyDataSetChanged()
    }

    fun addInfiniteScroll() {
        invoiceList.add(null)
        notifyItemInserted(invoiceList.size - 1)
    }

    fun removeInfiniteScroll() {
        invoiceList.removeAt(invoiceList.size - 1)
        val scrollPosition = invoiceList.size
        notifyItemRemoved(scrollPosition)
    }

    fun setInvoiceSelectListener(invoiceSelectListener: InvoiceSelectListener) {
        listener = invoiceSelectListener
    }

    inner class ViewHolder(binding: ItemInvoiceListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val billDateTV: TextView = binding.invoiceDateTV
        private val facilityNameTV: TextView = binding.invoiceFacilityNameTV
        private val billNumberTV: TextView = binding.invoiceBillNumTV
        private val invoiceAmtLabel1TV = binding.invoiceAmtLabel1
        private val invoiceAmtValue1TV: TextView = binding.invoiceAmtValue1
        private val invoiceAmtLabel2TV: TextView = binding.invoiceAmtLabel2
        private val invoiceAmtValue2TV: TextView = binding.invoiceAmtValue2
        private val invoiceStatusLayout: LinearLayout = binding.invoiceStatusLayout

        @SuppressLint("SetTextI18n")
        fun onBind(phrBill: PHRBills) {

            billDateTV.text = phrBill.billDate?.toString(ModelUtil.charmDisplayDateFormat())

            facilityNameTV.text = phrBill.facility?.facilityName

            billNumberTV.text = phrBill.billNumber

            if (phrBill.status == "Canceled") {
                invoiceStatusLayout.visibility = View.VISIBLE
            } else {
                invoiceStatusLayout.visibility = View.GONE
            }

            val pair = ModelUtil.getDueAndPaymentRequested(phrBill)
            val dueAmount = pair.first
            val paymentRequested = pair.second


            if (ModelUtil.isAmountValid(phrBill.amount) && phrBill.isPaid == false) {
                invoiceAmtLabel1TV.setText(R.string.due_at_present)
                invoiceAmtLabel2TV.setText(R.string.payment_requested)

                invoiceAmtValue1TV.text = "${phrBill.practiceCurrencySymbol} $dueAmount"
                invoiceAmtValue2TV.text = "${phrBill.practiceCurrencySymbol} $paymentRequested"


                TextViewCompat.setTextAppearance(facilityNameTV, R.style.itemIsNotRead)
                TextViewCompat.setTextAppearance(invoiceAmtLabel1TV, R.style.itemIsNotRead)
                TextViewCompat.setTextAppearance(invoiceAmtLabel2TV, R.style.itemIsNotRead)
                TextViewCompat.setTextAppearance(invoiceAmtValue1TV, R.style.itemIsNotRead)
                TextViewCompat.setTextAppearance(
                    invoiceAmtValue2TV,
                    R.style.invoiceDueAmtTxtAppearance
                )
                TextViewCompat.setTextAppearance(billDateTV, R.style.itemIsNotRead)
            } else {

                invoiceAmtLabel1TV.setText(R.string.invoice_bill_amt_info_str)
                invoiceAmtLabel2TV.setText(R.string.due_at_present)

                invoiceAmtValue1TV.text = "${phrBill.practiceCurrencySymbol} ${phrBill.billAmount}"
                invoiceAmtValue2TV.text = "${phrBill.practiceCurrencySymbol} ${dueAmount}"

                TextViewCompat.setTextAppearance(facilityNameTV, R.style.itemIsRead)
                TextViewCompat.setTextAppearance(invoiceAmtLabel1TV, R.style.itemIsRead)
                TextViewCompat.setTextAppearance(invoiceAmtLabel2TV, R.style.itemIsRead)
                TextViewCompat.setTextAppearance(invoiceAmtValue1TV, R.style.itemIsRead)
                TextViewCompat.setTextAppearance(
                    invoiceAmtValue2TV,
                    R.style.invoiceAmtTxtAppearance
                )
                TextViewCompat.setTextAppearance(billDateTV, R.style.itemIsRead)

            }

        }

    }

    interface InvoiceSelectListener {
        fun onInvoiceSelect(phrBill: PHRBills)
    }
}