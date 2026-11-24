package com.charm.phr.mphr.ui.questionnaire

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemQuestionnaireListBinding
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.ui.components.MDmPHRConstraintLayout
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil

class QuestionnaireCurrentListAdapter: RecyclerView.Adapter<QuestionnaireCurrentListAdapter.ViewHolder>() {

    private var physicianTemplateList = arrayListOf<PhysicianTemplates>()

    private var questionnaireSelectListener: QuestionnaireActivity.QuestionnaireSelectListener? = null

    lateinit var binding: ItemQuestionnaireListBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ItemQuestionnaireListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    fun addQuestionnaireList(physicianTemplateList:List<PhysicianTemplates>){
        this.physicianTemplateList.addAll(physicianTemplateList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemQuestionnaireListBinding): RecyclerView.ViewHolder(binding.root) {

        private val questionnaireListNameTV: TextView = binding.questionnaireListNameTV
        private val questionnaireListMoreLayout1: LinearLayout = binding.questionnaireListMoreLayout1
        private val questionnaireListMoreValue1: TextView = binding.questionnaireListMoreValue1
        private val questionnaireListMoreLayout2: LinearLayout = binding.questionnaireListMoreLayout2
        private val questionnaireListMoreValue2: TextView = binding.questionnaireListMoreValue2
        private val questionnaireListMoreLayout3: LinearLayout = binding.questionnaireListMoreLayout3
        private val questionnaireListMoreValue3: TextView = binding.questionnaireListMoreValue3
        private val questionnaireListStatusLayout: MDmPHRConstraintLayout = binding.questionnaireListStatusLayout
        private val questionnaireListStatusTV: TextView = binding.questionnaireListStatusTV

        fun onBind(physicianTemplates: PhysicianTemplates){

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


                if (patientQuesMap.isFilled && patientQuesMap.isSubmitted){
                    questionnaireListStatusLayout.setBgColor(R.color.questionnaireListStatusSubmittedBg)
                    questionnaireListStatusTV.setText(R.string.questionnaire_status_submitted)
                    questionnaireListStatusTV.setTextColor(ContextCompat.getColor(itemView.context , R.color.questionnaireListStatusSubmittedTxtColor))
                }
                else if (!patientQuesMap.isSubmitted && !patientQuesMap.isFilled){
                    questionnaireListStatusLayout.setBgColor(R.color.questionnaireListStatusNewBg)
                    questionnaireListStatusTV.setText(R.string.questionnaire_status_new)
                    questionnaireListStatusTV.setTextColor(ContextCompat.getColor(itemView.context , R.color.questionnaireListStatusNewTxtColor))
                }
                else{
                    questionnaireListStatusLayout.setBgColor(R.color.questionnaireListStatusDraftBg)
                    questionnaireListStatusTV.setText(R.string.questionnaire_status_draft)
                    questionnaireListStatusTV.setTextColor(ContextCompat.getColor(itemView.context , R.color.questionnaireListStatusDraftTxtColor))
                }
            }
        }
    }
}