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
import com.charm.phr.mphr.databinding.FragmentQuestionnaireCurrentBinding
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
class QuestionnaireCurrentFragment : DaggerFragment() , ChangeFilterListener {

    private var _binding: FragmentQuestionnaireCurrentBinding? = null
    private val binding get() = _binding!!

    lateinit var questionnaireViewModel: QuestionnaireViewModel

    @Inject
    lateinit var questionnaireCurrentListAdapter: QuestionnaireCurrentListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using view binding
        _binding = FragmentQuestionnaireCurrentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questionnaireViewModel = ViewModelProvider( requireActivity()).get(QuestionnaireViewModel::class.java)

        binding.questionnaireCurrentListRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.questionnaireCurrentListRV.adapter = questionnaireCurrentListAdapter

        onFilterChange()

        questionnaireViewModel.questionnaireFilterChangeLive.observe(viewLifecycleOwner , Observer {resp ->
            if (resp != null){
                onFilterChange()
            }
        })
        questionnaireViewModel.questionnaireCurrentListLive.observe(viewLifecycleOwner ,  Observer {resp ->
            if (resp != null){
                binding.questionnaireCurrentListProgress.root.visibility = View.GONE

                if (resp is Boolean){

                    val questionnaireList = questionnaireViewModel.getQuestionnaireItem(0 , false)
                    val size = questionnaireCurrentListAdapter.setQuestionnaireList(questionnaireList)
                    if (size != 0){
                        questionnaireViewModel.questionnaireCurrentListHasMore = questionnaireViewModel.questionnaireCurrentList.size > ModelUtil.questionnaireListPerPageCount
                        binding.questionnaireCurrentListRV.visibility = View.VISIBLE
                        binding.questionnaireCurrentListNoDataTV.root.visibility = View.GONE
                    }
                    else{
                        binding.questionnaireCurrentListRV.visibility = View.GONE
                        binding.questionnaireCurrentListNoDataTV.root.visibility = View.VISIBLE
                    }

                }
                else if (resp is APIResponseError){
                    binding.questionnaireCurrentListErrorPageLayout.root.visibility = View.VISIBLE
                }
            }
        })

        questionnaireCurrentListAdapter.setQuestionnaireSelectListener(object : QuestionnaireActivity.QuestionnaireSelectListener{
            override fun onQuestionnaireSelect(physicianTemplates: PhysicianTemplates) {

                val intent = Intent(this@QuestionnaireCurrentFragment.activity , QuestionnaireDetailActivity::class.java)
                intent.putExtra(ModelUtil.questionnairePhysicianTemplatesIntent , physicianTemplates.toString)
                activity?.startActivityForResult(intent , 0)
                activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        })


        binding.questionnaireCurrentListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastIndexInScreen = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()



                if ((lastIndexInScreen + 1) >=  questionnaireCurrentListAdapter.itemCount - 5 &&  questionnaireCurrentListAdapter.itemCount != 0  && questionnaireViewModel.questionnaireCurrentListHasMore) {
                    Log.d("scroll", "now")
                    val questionnaireList = questionnaireViewModel.getQuestionnaireItem(0 )
                    questionnaireCurrentListAdapter.addQuestionnaireList(questionnaireList)
                    questionnaireViewModel.questionnaireCurrentListHasMore = questionnaireCurrentListAdapter.itemCount != questionnaireViewModel.questionnaireCurrentList.size
                }
            }
        })
        binding.questionnaireCurrentListErrorPageLayout.errorPageTryAgainBtn.setOnClickListener {

            onFilterChange()
            questionnaireViewModel.fetchQuestionnaireCurrent()
        }
    }

    override fun onFilterChange() {

        binding.questionnaireCurrentListProgress.root.visibility = View.VISIBLE
        binding.questionnaireCurrentListRV.visibility = View.GONE
        binding.questionnaireCurrentListErrorPageLayout.root.visibility = View.GONE
        binding.questionnaireCurrentListNoDataTV.root.visibility = View.GONE

    }

}
