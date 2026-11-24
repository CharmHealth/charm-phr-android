package com.charm.phr.mphr.ui.problem_list

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemActiveProblemListBinding
import com.charm.phr.mphr.model.PatientMedicalCondition
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil

class ProblemListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var problemList:ArrayList<PatientMedicalCondition?> = arrayListOf()

    private var problemSelectListener: ProblemListSelectListener? = null

    private val viewTypeProblemList = 1
    private val viewTypeScroll = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder =if (viewType == viewTypeProblemList) {
            val binding = ItemActiveProblemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }

        return viewHolder
    }

    override fun getItemCount(): Int = problemList.size

    override fun getItemViewType(position: Int): Int {
        return if (problemList[position] != null){
            viewTypeProblemList
        }
        else {
            viewTypeScroll
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val problems = problemList[position]

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
                if (problems.isAddedByPatient) {
                    problemSelectListener?.onProblemClick(problems)
                }
                // }
            }

            holder.itemView.setOnLongClickListener {
                if (problems.isAddedByPatient) {
                    problemSelectListener?.onProblemDelete(problems, position)
                }
                return@setOnLongClickListener true
            }
        }
    }

    fun setProblems(problemList: ArrayList<PatientMedicalCondition>): Int{
        this.problemList.clear()
        this.problemList.addAll(problemList)
        this.notifyDataSetChanged()
        return this.problemList.size
    }

    fun addProblems(problemList: ArrayList<PatientMedicalCondition>){
        this.problemList.addAll(problemList)
        this.notifyDataSetChanged()
    }

    fun removeProblem(position: Int): Int{
        this.problemList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, problemList.size);

        return  problemList.size
    }

    fun addInfiniteScroll(){
        problemList.add(null)
        notifyItemInserted(problemList.size - 1)
    }

    fun removeInfiniteScroll() {
        problemList.removeAt(problemList.size - 1)
        val scrollPosition =  problemList.size
        notifyItemRemoved(scrollPosition)
    }

    fun setProcedureSelectListener(listener: ProblemListSelectListener){
        problemSelectListener = listener
    }

    inner class ViewHolder(private val binding: ItemActiveProblemListBinding): RecyclerView.ViewHolder(binding.root) {

        val problemDateTV: TextView = binding.problemListDateTV
        val problemListNameTV: TextView = binding.problemListNameTV
        val problemListReasonTV: TextView = binding.problemListReasonTV
        val problemConstraintLayout: ConstraintLayout = binding.problemListConstraintLayout
        val problemListLayout: ConstraintLayout = binding.problemListLayout
        val problemListHPLayout: ConstraintLayout = binding.problemListHPLayout
        val problemHPDateTV: TextView = binding.problemListHPDateTV
        val problemListHPNameTV: TextView = binding.problemListHPNameTV
        val problemListHPStatusTV: TextView = binding.problemListHPStatusTV

        private val handler = Handler(Looper.getMainLooper())
        private val runnable = Runnable {
            problemConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)
        }

        fun onBind(problems: PatientMedicalCondition){


            if (problems.isHealthPassport){
                problemListLayout.visibility = View.GONE
                problemListHPLayout.visibility = View.VISIBLE
                problemHPDateTV.text = problems.dateOfOnset?.toString(ModelUtil.charmDisplayDateFormat())
                problemListHPNameTV.text =  problems.medicalCondition?.medicalConditionName
                problemListHPStatusTV.text =  problems.status
            }
            else{

                problemListLayout.visibility = View.VISIBLE
                problemListHPLayout.visibility = View.GONE

                problemDateTV.text = problems.dateOfOnset?.toString(ModelUtil.charmDisplayDateFormat())


                problemListNameTV.text = if (problems.isAddedByPatient) {
                    problems.medicalCondition?.medicalConditionName
                }
                else{
                    problems.diagnosis?.diagnosisLong
                }

                if (problems.notes.isNullOrBlank()){
                    problemListReasonTV.visibility = View.GONE
                }
                else{
                    problemListReasonTV.visibility = View.VISIBLE
                    problemListReasonTV.text = problems.notes
                }

            }
        }

        fun changeBgToSelected(){

            handler.removeCallbacks(runnable)
            problemConstraintLayout.setBackgroundResource( R.color.cardSelectedBgColor)
        }

        fun changeBgToUnSelected(){
            problemConstraintLayout.setBackgroundResource(android.R.color.white)
            handler.postDelayed(runnable , ModelUtil.delayedTimingForRippleChange)
        }

    }

    interface ProblemListSelectListener {
        fun onProblemClick(patMedCondition: PatientMedicalCondition)
        fun onProblemDelete(patMedCondition: PatientMedicalCondition , position: Int)
    }

}