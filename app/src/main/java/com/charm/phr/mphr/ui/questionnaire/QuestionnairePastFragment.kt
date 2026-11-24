package com.charm.phr.mphr.ui.questionnaire

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentQuestionnairePastBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.ui.questionnaire.questionnaire_detail.QuestionnaireDetailActivity
import com.charm.phr.mphr.util.ChangeFilterListener
import com.charm.phr.mphr.util.ModelUtil
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class QuestionnairePastFragment : DaggerFragment() , ChangeFilterListener {

    lateinit var questionnaireViewModel: QuestionnaireViewModel

    @Inject
    lateinit var questionnairePastListAdapter: QuestionnairePastListAdapter

    private var _binding: FragmentQuestionnairePastBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentQuestionnairePastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questionnaireViewModel = ViewModelProvider(requireActivity()).get(QuestionnaireViewModel::class.java)

        binding.questionnairePastListRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.questionnairePastListRV.adapter = questionnairePastListAdapter

        onFilterChange()
        questionnaireViewModel.questionnaireFilterChangeLive.observe(viewLifecycleOwner , Observer {resp ->
            if (resp != null){
                onFilterChange()
            }
        })

        questionnaireViewModel.questionnairePastListLive.observe(viewLifecycleOwner ,  Observer {resp ->
            if (resp != null){
                binding.questionnairePastListProgress.root.visibility = View.GONE

                if (resp is Boolean){

                    val questionnaireList = questionnaireViewModel.getQuestionnaireItem(1 , false)
                    val size = questionnairePastListAdapter.setQuestionnaireList(questionnaireList)
                    if (size != 0){
                        questionnaireViewModel.questionnairePastListHasMore = questionnaireViewModel.questionnairePastList.size > ModelUtil.questionnaireListPerPageCount
                        binding.questionnairePastListRV.visibility = View.VISIBLE
                        binding.questionnairePastListNoDataTV.root.visibility = View.GONE
                    }
                    else{
                        binding.questionnairePastListRV.visibility = View.GONE
                        binding.questionnairePastListNoDataTV.root.visibility = View.VISIBLE
                    }

                }
                else if (resp is APIResponseError){
                    binding.questionnairePastListErrorPageLayout.root.visibility = View.VISIBLE
                }
            }
        })

        questionnairePastListAdapter.setQuestionnaireSelectListener(object : QuestionnaireActivity.QuestionnaireSelectListener{
            override fun onQuestionnaireSelect(physicianTemplates: PhysicianTemplates) {
                val intent = Intent(this@QuestionnairePastFragment.activity , QuestionnaireDetailActivity::class.java)
                intent.putExtra(ModelUtil.questionnairePhysicianTemplatesIntent , physicianTemplates.toString)
                intent.putExtra(ModelUtil.questionnaireIsPastIntent , true)
                activity?.startActivityForResult(intent , 0)
                activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        })


        binding.questionnairePastListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastIndexInScreen = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()



                if ((lastIndexInScreen + 1) >=  questionnairePastListAdapter.itemCount - 5 &&  questionnairePastListAdapter.itemCount != 0  && questionnaireViewModel.questionnairePastListHasMore) {
                    Log.d("scroll", "now")
                    val questionnaireList = questionnaireViewModel.getQuestionnaireItem(1 )
                    questionnairePastListAdapter.addQuestionnaireList(questionnaireList)
                    questionnaireViewModel.questionnairePastListHasMore = questionnairePastListAdapter.itemCount != questionnaireViewModel.questionnairePastList.size
                }
            }
        })

        binding.questionnairePastListErrorPageLayout.errorPageTryAgainBtn.setOnClickListener {
            onFilterChange()
            questionnaireViewModel.fetchQuestionnairePast()
        }
    }

    override fun onFilterChange() {

        binding.questionnairePastListProgress.root.visibility = View.VISIBLE
        binding.questionnairePastListRV.visibility = View.GONE
        binding.questionnairePastListErrorPageLayout.root.visibility = View.GONE
        binding.questionnairePastListNoDataTV.root.visibility = View.GONE

    }


}
