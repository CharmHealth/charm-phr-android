package com.charm.phr.mphr.ui.procedure

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemProcedureListBinding
import com.charm.phr.mphr.model.ProceduresMap
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.OnMultipleSelectChangeListener

class ProcedureListAdapter(val authManager: AuthManager) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var procedureList:ArrayList<ProceduresMap?> = arrayListOf()

    private var procedureSelectListener: ProcedureSelectListener? = null
    private var procedureMultiSelectListener: OnMultipleSelectChangeListener? = null

    private var isMultiSelectEnabled = false

    private val viewTypeProcedure = 1
    private val viewTypeScroll = 2

    private var allowEdit = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder =if (viewType == viewTypeProcedure) {
            val binding = ItemProcedureListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }

        return viewHolder
    }

    override fun getItemCount(): Int = procedureList.size

    override fun getItemViewType(position: Int): Int {
        return if (procedureList[position] != null){
            viewTypeProcedure
        }
        else {
            viewTypeScroll
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val procedures = procedureList[position]

        if (procedures != null && holder is ViewHolder) {

            holder.onBind(procedures)
            /*holder.itemView.setOnLongClickListener {
                if (!isMultiSelectEnabled) {
                    isMultiSelectEnabled = true
                    procedureMultiSelectListener?.onMultiSelect(true)
                    procedures.isSelected = true
                    holder.changeBgToSelected()
                }
                else {
                    if (!procedures.isSelected) {
                        procedures.isSelected = true
                        holder.changeBgToSelected()
                    } else {
                        procedures.isSelected = true
                        holder.changeBgToSelected()
                    }
                }

                return@setOnLongClickListener true
            }*/



            if (allowEdit) {

                holder.procedureConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)

                holder.itemView.setOnLongClickListener {
                    procedureSelectListener?.onDeleteProcedure(procedures , position)
                    return@setOnLongClickListener true
                }
                holder.itemView.setOnClickListener {
                    /* if (isMultiSelectEnabled) {
                    if (procedures.isSelected) {
                        procedures.isSelected = false
                        holder.changeBgToUnSelected()
                        checkAllIsUnchecked()
                    } else {
                        procedures.isSelected = true
                        holder.changeBgToSelected()
                    }
                } else {*/
                    procedureSelectListener?.onEditProcedure(procedures)
                    // }
                }
            }
            else{
                holder.procedureConstraintLayout.setBackgroundResource(android.R.color.white)

                holder.itemView.setOnLongClickListener {
                    return@setOnLongClickListener true
                }
                holder.itemView.setOnClickListener {

                }
            }
        }
    }

    fun setProcedures(procedureList: ArrayList<ProceduresMap>): Int{
        this.procedureList.clear()
        this.procedureList.addAll(procedureList)

        allowEdit = ModelUtil.isPHREntryEnabled(authManager , ModelUtil.procedureModelId)
        this.notifyDataSetChanged()
        return this.procedureList.size
    }

    fun addProcedures(procedureList: ArrayList<ProceduresMap>){
        this.procedureList.addAll(procedureList)
        this.notifyDataSetChanged()
    }

    fun addInfiniteScroll(){
        procedureList.add(null)
        notifyItemInserted(procedureList.size - 1)
    }

    fun removeInfiniteScroll() {
        procedureList.removeAt(procedureList.size - 1)
        val scrollPosition =  procedureList.size
        notifyItemRemoved(scrollPosition)
    }

    fun removeProcedure(position: Int): Int{
        procedureList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, procedureList.size)

        return procedureList.size
    }

    private fun checkAllIsUnchecked(){

        var atleastOneIsSelected = false
        for(procedures in procedureList){
            if (procedures?.isSelected == true){
                atleastOneIsSelected = true
                break
            }
        }

        if (!atleastOneIsSelected){
            isMultiSelectEnabled = false
            procedureMultiSelectListener?.onMultiSelect(false)
        }
    }

    fun disableMultiSelect(){
        isMultiSelectEnabled = false
        procedureList.forEach{procedure->
            procedure?.isSelected = false
        }
        notifyDataSetChanged()
    }

    fun setProcedureSelectListener(listener: ProcedureSelectListener){
        procedureSelectListener = listener
    }

    fun setOnMultipleSelectChangeListener(listener: OnMultipleSelectChangeListener){
        procedureMultiSelectListener = listener
    }

    inner class ViewHolder(private val binding: ItemProcedureListBinding): RecyclerView.ViewHolder(binding.root) {

        val procedureDateTV: TextView = binding.procedureListDateTV
        val procedureListNameTV: TextView = binding.procedureListNameTV
        val procedureListReasonTV: TextView = binding.procedureListReasonTV
        val procedureConstraintLayout: ConstraintLayout = binding.procedureListConstraintLayout
        val procedureLayout: ConstraintLayout = binding.procedureListLayout

        val procedureListHPLayout: ConstraintLayout = binding.procedureListHPLayout
        val problemHPDateTV: TextView = binding.procedureListHPDateTV
        val procedureListHPNameTV: TextView = binding.procedureListHPNameTV
        val procedureListHPTypeTV: TextView = binding.procedureListHPTypeTV

        private val handler = Handler(Looper.getMainLooper())
        private val runnable = Runnable {
            procedureConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)
        }

        fun onBind(procedures: ProceduresMap){

            if (procedures.isHealthPassport){
                procedureLayout.visibility = View.GONE
                procedureListHPLayout.visibility = View.VISIBLE

                problemHPDateTV.text = procedures.fromDate?.toString(ModelUtil.charmDisplayDateFormat())
                procedureListHPNameTV.text = procedures.procedure?.procedure ?: ""
                procedureListHPTypeTV.text = procedures.procedure?.procedureType ?: ""
            }
            else {

                procedureLayout.visibility = View.VISIBLE
                procedureListHPLayout.visibility = View.GONE
                procedureDateTV.text = procedures.fromDate?.toString(ModelUtil.charmDisplayDateFormat())
                procedureListNameTV.text = itemView.context.getString(R.string.two_string_template, procedures.procedure?.procedureType, procedures.procedure?.procedure)


                if (procedures.notes.isNullOrBlank()) {
                    procedureListReasonTV.visibility = View.GONE
                } else {
                    procedureListReasonTV.visibility = View.VISIBLE
                    procedureListReasonTV.text = procedures.notes
                }
            }



            if (procedures.isSelected){
                changeBgToSelected()
            }
            else{
                changeBgToUnSelected()
            }
        }

        fun changeBgToSelected(){

            handler.removeCallbacks(runnable)
            procedureConstraintLayout.setBackgroundResource( R.color.cardSelectedBgColor)
        }

        fun changeBgToUnSelected(){
            procedureConstraintLayout.setBackgroundResource(android.R.color.white)
            handler.postDelayed(runnable , ModelUtil.delayedTimingForRippleChange)
        }

    }

    interface ProcedureSelectListener {
        fun onEditProcedure(proceduresMap: ProceduresMap)
        fun onDeleteProcedure(proceduresMap: ProceduresMap , position: Int)
    }

}