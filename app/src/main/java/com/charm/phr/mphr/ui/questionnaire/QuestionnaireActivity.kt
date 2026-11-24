package com.charm.phr.mphr.ui.questionnaire

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityQuestionnaireBinding
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class QuestionnaireActivity : MDBaseActivity() {

    @Inject
    lateinit var questionnaireViewPagerAdapter: QuestionnaireViewPagerAdapter


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var questionnaireViewModel: QuestionnaireViewModel

    private var questionnaireSubTitles: ArrayList<String> = arrayListOf()

    lateinit var binding: ActivityQuestionnaireBinding

    private var consentFormIds: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionnaireBinding.inflate(layoutInflater)
        setContentView(binding.root)

        questionnaireViewModel =
            ViewModelProvider(this, viewModelFactory).get(QuestionnaireViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.questionnaireToolbar)
        UIUtil.applyViewInsertForBottomView(binding.questionnaireMainLayout)

        questionnaireViewModel.appointmentId = intent.getStringExtra(ModelUtil.appointmentIdIntent)
        questionnaireViewModel.isMultiPractice =
            ModelUtil.getEnabledPractices(authManager, ModelUtil.questionnaireModelId).size > 1

        binding.questionnaireViewPager.offscreenPageLimit = ModelUtil.viewPagerOffScreenPageLimit
        binding.questionnaireViewPager.adapter = questionnaireViewPagerAdapter
        questionnaireSubTitles = arrayListOf(
            getString(R.string.questionnaire_current_label), getString(
                R.string.questionnaire_past_label
            )
        )
        TabLayoutMediator(
            binding.questionnaireTabLayout,
            binding.questionnaireViewPager
        ) { tab, position ->
            tab.text = questionnaireSubTitles[position]
        }.attach()

        val consentFormIds = intent.getStringArrayListExtra(ModelUtil.consentFormIdsIntent)
        this.consentFormIds = consentFormIds

        if (!questionnaireViewModel.appointmentId.isNullOrBlank()) {
            binding.questionnaireTabLayout.visibility = View.GONE
        }

        if (!consentFormIds.isNullOrEmpty()) {
            questionnaireViewModel.consentFormIds = consentFormIds
            binding.questionnaireToolbarText.text = getString(R.string.consent_form)
            binding.questionnaireInfoLayout.visibility = View.VISIBLE
            binding.questionnaireJoinVideoCallBtn.isEnabled = false
            if (BuildUtil.isVideoCallEnabled) {
                binding.questionnaireJoinVideoCallBtn.visibility = View.VISIBLE
            } else {
                binding.questionnaireJoinVideoCallBtn.visibility = View.GONE
            }
            questionnaireViewModel.fetchConsentFormQuestionnaires()
        } else {
            questionnaireViewModel.fetchQuestionnaireList()
        }

        questionnaireViewModel.questionnaireCurrentListLive.observe(this, Observer { resp ->
            if (!questionnaireViewModel.appointmentId.isNullOrBlank() && resp is Boolean && resp) {
                val allFilled = questionnaireViewModel.questionnaireCurrentList.isNotEmpty() &&
                        questionnaireViewModel.questionnaireCurrentList.all {
                            it.patientQuesMap?.isFilled == true && it.patientQuesMap?.isSubmitted == true
                        }
                binding.questionnaireJoinVideoCallBtn.isEnabled = allFilled
            }
        })

        binding.questionnaireBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.questionnaireJoinVideoCallBtn.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(resultCode)
                questionnaireViewModel.updateFilter()
                if (!consentFormIds.isNullOrEmpty()) {
                    questionnaireViewModel.fetchConsentFormQuestionnaires()
                }else{
                    questionnaireViewModel.fetchQuestionnaireList()
                }
            }
        }
    }

    interface QuestionnaireSelectListener {
        fun onQuestionnaireSelect(physicianTemplates: PhysicianTemplates)
    }
}


