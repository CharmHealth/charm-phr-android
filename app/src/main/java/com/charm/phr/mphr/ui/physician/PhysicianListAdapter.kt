package com.charm.phr.mphr.ui.physician

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemPhysicianListBinding
import com.charm.phr.mphr.model.Pharmacy
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil

class PhysicianListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var physicianList:ArrayList<Pharmacy?> = arrayListOf()

    private var physicianSelectListener: PhysicianListSelectListener? = null

    private val viewTypePhysicianList = 1
    private val viewTypeScroll = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder =if (viewType == viewTypePhysicianList) {
            val binding = ItemPhysicianListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }

        return viewHolder
    }

    override fun getItemCount(): Int = physicianList.size

    override fun getItemViewType(position: Int): Int {
        return if (physicianList[position] != null){
            viewTypePhysicianList
        }
        else {
            viewTypeScroll
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val problems = physicianList[position]

        if (problems != null && holder is ViewHolder) {

            holder.onBind(problems)
            /*holder.itemView.setOnLongClickListener {
                if (!isMultiSelectEnabled) {
                    isMultiSelectEnabled = true
                    problemMultiSelectListener?.onMultiSelect(true)
                    problems.isSelected = true
                    holder.changeBgToSelected()
                }
                else {
                    if (!problems.isSelected) {
                        problems.isSelected = true
                        holder.changeBgToSelected()
                    } else {
                        problems.isSelected = true
                        holder.changeBgToSelected()
                    }
                }

                return@setOnLongClickListener true
            }*/


            holder.itemView.setOnClickListener {
                /* if (isMultiSelectEnabled) {
                     if (problems.isSelected) {
                         problems.isSelected = false
                         holder.changeBgToUnSelected()
                         checkAllIsUnchecked()
                     } else {
                         problems.isSelected = true
                         holder.changeBgToSelected()
                     }
                 } else {*/
                physicianSelectListener?.onEdit(problems)
                
                // }
            }

            holder.itemView.setOnLongClickListener {
                physicianSelectListener?.onDelete(problems, position)
                
                return@setOnLongClickListener true
            }

            holder.physicianMobileImg.setOnClickListener {
                physicianSelectListener?.onCall(problems)
            }
        }
    }

    fun setPhysician(physicianList: ArrayList<Pharmacy>): Int{
        this.physicianList.clear()
        this.physicianList.addAll(physicianList)
        this.notifyDataSetChanged()
        return this.physicianList.size
    }



    fun removePhysician(position: Int): Int{
        this.physicianList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, physicianList.size);

        return  physicianList.size
    }

    fun addInfiniteScroll(){
        physicianList.add(null)
        notifyItemInserted(physicianList.size - 1)
    }

    fun removeInfiniteScroll() {
        physicianList.removeAt(physicianList.size - 1)
        val scrollPosition =  physicianList.size
        notifyItemRemoved(scrollPosition)
    }

    fun setPhysicianSelectListener(listener: PhysicianListSelectListener){
        physicianSelectListener = listener
    }

    inner class ViewHolder(private val binding: ItemPhysicianListBinding) : RecyclerView.ViewHolder(binding.root) {

        val physicianNameTV: TextView = binding.physicianNameTV
        val physicianMobileTV: TextView = binding.physicianMobileTV
        val problemConstraintLayout: ConstraintLayout = binding.physicianConstraintLayout
        val physicianMobileImg = binding.physicianMobileImg

        private val handler = Handler(Looper.getMainLooper())
        private val runnable = Runnable {
            problemConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)
        }

        fun onBind(pharmacy: Pharmacy) {
            physicianNameTV.text = pharmacy.pharmacyName
            physicianMobileTV.text = pharmacy.contactDetails?.mobile

            if (pharmacy.contactDetails?.mobile.isNullOrBlank()) {
                physicianMobileImg.visibility = View.GONE
            } else {
                physicianMobileImg.visibility = View.VISIBLE
            }
        }

        fun changeBgToSelected() {
            handler.removeCallbacks(runnable)
            problemConstraintLayout.setBackgroundResource(R.color.cardSelectedBgColor)
        }

        fun changeBgToUnSelected() {
            problemConstraintLayout.setBackgroundResource(android.R.color.white)
            handler.postDelayed(runnable, ModelUtil.delayedTimingForRippleChange)
        }

    }

    interface PhysicianListSelectListener {
        fun onEdit(pharmacy: Pharmacy)
        fun onDelete(pharmacy: Pharmacy , position: Int)
        fun onCall(pharmacy: Pharmacy)
    }

}