package com.charm.phr.mphr.ui.questionnaire.health_passport

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.databinding.IteemQuestionnaireHpListBinding
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.ui.questionnaire.QuestionnaireActivity

class QuestionnaireListAdapter: RecyclerView.Adapter<QuestionnaireListAdapter.ViewHolder>() {

    private var physicianTemplateList = arrayListOf<PhysicianTemplates>()

    private var questionnaireSelectListener: QuestionnaireActivity.QuestionnaireSelectListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = IteemQuestionnaireHpListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int  = physicianTemplateList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val physicianTemplates = physicianTemplateList[position]
        holder.onBind(physicianTemplates)

        holder.itemView.setOnClickListener {
            questionnaireSelectListener?.onQuestionnaireSelect(physicianTemplates)
        }

    }

    fun setQuestionnaireSelectListener(listener: QuestionnaireActivity.QuestionnaireSelectListener) {
        questionnaireSelectListener = listener
    }

    fun setQuestionnaireList(physicianTemplateList:List<PhysicianTemplates>): Int{
        this.physicianTemplateList.clear()
        this.physicianTemplateList.addAll(physicianTemplateList)
        notifyDataSetChanged()

        return physicianTemplateList.size
    }

    inner class ViewHolder(binding: IteemQuestionnaireHpListBinding): RecyclerView.ViewHolder(binding.root){

        private val questionnaireListNameTV: TextView = binding.questionnaireListHPNameTV


        fun onBind(physicianTemplates: PhysicianTemplates){
            questionnaireListNameTV.text = physicianTemplates.name
        }
    }
}