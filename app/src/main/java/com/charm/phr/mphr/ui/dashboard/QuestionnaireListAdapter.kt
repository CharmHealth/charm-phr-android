package com.charm.phr.mphr.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemHomePageQuestionnaireListBinding
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.ui.components.MDmPHRConstraintLayout
import com.charm.phr.mphr.ui.questionnaire.QuestionnaireActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil

class QuestionnaireListAdapter : RecyclerView.Adapter<QuestionnaireListAdapter.ViewHolder>() {

    private val questionnaireList: ArrayList<PhysicianTemplates> = arrayListOf()
    private var questionnaireSelectListener: QuestionnaireActivity.QuestionnaireSelectListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomePageQuestionnaireListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        val questionnaireListSize = questionnaireList.size
        return if (questionnaireListSize > ModelUtil.dashboardMaxQuestionnaireCount) {
            ModelUtil.dashboardMaxQuestionnaireCount
        } else {
            questionnaireListSize
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val physicianTemplates = questionnaireList[position]
        holder.onBind(physicianTemplates)
        holder.itemView.setOnClickListener {
            questionnaireSelectListener?.onQuestionnaireSelect(physicianTemplates)
        }
    }

    fun setQuestionnaireSelectListener(listener: QuestionnaireActivity.QuestionnaireSelectListener) {
        questionnaireSelectListener = listener
    }

    fun setQuestionnaireList(physicianTemplates: List<PhysicianTemplates>): Int {
        this.questionnaireList.clear()
        this.questionnaireList.addAll(physicianTemplates)
        notifyDataSetChanged()
        return this.questionnaireList.size
    }


    inner class ViewHolder(private val binding: ItemHomePageQuestionnaireListBinding) : RecyclerView.ViewHolder(binding.root) {

        private val questionnaireListNameTV: TextView = binding.questionnaireListNameTV
        private val questionnaireListMoreLayout1: LinearLayout = binding.questionnaireListMoreLayout1
        private val questionnaireListMoreValue1: TextView = binding.questionnaireListMoreValue1
        private val questionnaireListMoreLayout2: LinearLayout = binding.questionnaireListMoreLayout2
        private val questionnaireListMoreValue2: TextView = binding.questionnaireListMoreValue2
        private val questionnaireListMoreLayout3: LinearLayout = binding.questionnaireListMoreLayout3
        private val questionnaireListMoreValue3: TextView = binding.questionnaireListMoreValue3
        private val questionnaireListStatusLayout: MDmPHRConstraintLayout = binding.questionnaireListStatusLayout
        private val questionnaireListStatusTV: TextView = binding.questionnaireListStatusTV

        fun onBind(physicianTemplates: PhysicianTemplates) {

            questionnaireListNameTV.text = physicianTemplates.name

            val patientQuesMap = physicianTemplates.patientQuesMap

            if (patientQuesMap != null) {

                val facilityName = patientQuesMap.appointment?.facilityName
                if (!facilityName.isNullOrBlank()) {
                    questionnaireListMoreLayout1.visibility = View.VISIBLE
                    questionnaireListMoreValue1.text = facilityName
                } else {
                    questionnaireListMoreLayout1.visibility = View.GONE
                }

                val memberName = UIUtil.formMemberName(physicianTemplates.member)
                if (memberName.isNotBlank()) {
                    questionnaireListMoreLayout2.visibility = View.VISIBLE
                    questionnaireListMoreValue2.text = memberName
                } else {
                    questionnaireListMoreLayout2.visibility = View.GONE
                }

                val appointmentDate = patientQuesMap.appointment?.appointmentDate
                if (appointmentDate != null) {
                    questionnaireListMoreLayout3.visibility = View.VISIBLE
                    questionnaireListMoreValue3.text =
                            appointmentDate.toString(ModelUtil.charmDisplayDateFormat())
                } else {
                    questionnaireListMoreLayout3.visibility = View.GONE
                }


                if (patientQuesMap.isFilled && patientQuesMap.isSubmitted) {
                    questionnaireListStatusLayout.setBgColor(R.color.questionnaireListStatusSubmittedBg)
                    questionnaireListStatusTV.setText(R.string.questionnaire_status_submitted)
                    questionnaireListStatusTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.questionnaireListStatusSubmittedTxtColor))
                } else if (!patientQuesMap.isSubmitted && !patientQuesMap.isFilled) {
                    questionnaireListStatusLayout.setBgColor(R.color.questionnaireListStatusNewBg)
                    questionnaireListStatusTV.setText(R.string.questionnaire_status_new)
                    questionnaireListStatusTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.questionnaireListStatusNewTxtColor))
                } else {
                    questionnaireListStatusLayout.setBgColor(R.color.questionnaireListStatusDraftBg)
                    questionnaireListStatusTV.setText(R.string.questionnaire_status_draft)
                    questionnaireListStatusTV.setTextColor(ContextCompat.getColor(itemView.context, R.color.questionnaireListStatusDraftTxtColor))
                }
            }
        }

    }


}