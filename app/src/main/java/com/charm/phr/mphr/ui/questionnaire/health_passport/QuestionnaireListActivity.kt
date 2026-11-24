package com.charm.phr.mphr.ui.questionnaire.health_passport

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityQuestionnaireListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.ui.questionnaire.QuestionnaireActivity
import com.charm.phr.mphr.ui.questionnaire.questionnaire_detail.QuestionnaireDetailActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

class QuestionnaireListActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var questionnaireViewModel: QuestionnaireListViewModel

    @Inject
    lateinit var questionnaireListAdapter: QuestionnaireListAdapter

    lateinit var binding: ActivityQuestionnaireListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuestionnaireListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        questionnaireViewModel = ViewModelProvider(this, viewModelFactory).get(QuestionnaireListViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.questionnaireToolbar)
        UIUtil.applyViewInsertForBottomView(binding.questionnaireListRootLayout)

        binding.questionnaireListRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.questionnaireListRV.adapter = questionnaireListAdapter



        questionnaireListAdapter.setQuestionnaireSelectListener(object :
                QuestionnaireActivity.QuestionnaireSelectListener {

            override fun onQuestionnaireSelect(physicianTemplates: PhysicianTemplates) {

                val intent = Intent(this@QuestionnaireListActivity ,QuestionnaireDetailActivity::class.java )
                intent.putExtra(ModelUtil.isHealthPassportIntent , true)
                intent.putExtra(ModelUtil.questionnairePatQuesMapIdIntent , physicianTemplates.patientQuesMap?.patientQuesMapId)
                intent.putExtra(ModelUtil.questionnaireNameIntent , physicianTemplates.name)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        })



        if (questionnaireViewModel.questionnaireList.isEmpty()) {
            fetchQuestionnaire()
        }

        binding.questionnaireBackBtn.setOnClickListener {
            onBackPressed()
        }



        binding.questionnaireListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.questionnaireListErrorPage.root.visibility = View.GONE
            fetchQuestionnaire()
        }
    }



    private fun fetchQuestionnaire() {
        binding.questionnaireListProgress.root.visibility = View.VISIBLE
        binding.questionnaireListNoDataTV.root.visibility = View.GONE
        binding.questionnaireListRV.visibility = View.GONE
        questionnaireViewModel.fetchQuestionnaireList().observe(this , Observer {resp ->
            if (resp != null) {
                binding.questionnaireListProgress.root.visibility = View.GONE
                if (resp is ArrayList<*>) {
                    val questionnairePairList = resp as ArrayList<PhysicianTemplates>
                    val size = questionnaireListAdapter.setQuestionnaireList(questionnairePairList.filter{ it.name != ModelUtil.questionnaireNameValuePermission })
                    if (size == 0) {
                        binding.questionnaireListNoDataTV.root.visibility = View.VISIBLE
                        binding.questionnaireListRV.visibility = View.GONE
                    } else {
                        binding.questionnaireListNoDataTV.root.visibility = View.GONE
                        binding.questionnaireListRV.visibility = View.VISIBLE

                        if (!BuildUtil.isCustomApp) {
                            val height = UIUtil.getScreenHeight(this)

                            val tv = TypedValue()
                            var actionBarHeight = 0
                            if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
                            }

                            val viewHeight = ((height - actionBarHeight) * 2) / 3

                            val footerTreeObserver = binding.questionnaireListRV.viewTreeObserver

                            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                                override fun onGlobalLayout() {

                                    try {
                                        binding.questionnaireListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                                        val questionnaireHeight = binding.questionnaireListRV.height ?: 0
                                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $questionnaireHeight")

                                        if (questionnaireHeight < viewHeight) {
                                            binding.questionnaireListHPBgLayout.visibility = View.VISIBLE
                                        } else {
                                            binding.questionnaireListHPBgLayout.visibility = View.GONE
                                        }

                                    } catch (e: Exception) {

                                    }
                                }
                            })
                        } else {
                            binding.questionnaireListHPBgLayout.visibility = View.GONE
                        }
                    }

                }
                else if (resp is APIResponseError) {
                    binding.questionnaireListNoDataTV.root.visibility = View.GONE
                    binding.questionnaireListRV.visibility = View.GONE
                    binding.questionnaireListErrorPage.root.visibility = View.VISIBLE
                    binding.questionnaireListProgress.root.visibility = View.GONE
                }
            }
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
