package com.charm.phr.mphr.ui.problem_list

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentActiveProblemListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PatientMedicalCondition
import com.charm.phr.mphr.ui.components.ProgressDialog
import com.charm.phr.mphr.ui.problem_list.add_problem_list.AddProblemListActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RefreshDataListener
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.UIUtil
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class ActiveProblemListFragment : DaggerFragment()  , RefreshDataListener {



    lateinit var problemListViewModel: ProblemListViewModel

    @Inject
    lateinit var problemListAdapter: ProblemListAdapter

    lateinit var progressDialog: ProgressDialog

    lateinit var imm: InputMethodManager

    val handler = Handler(Looper.getMainLooper())

    private var _binding: FragmentActiveProblemListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentActiveProblemListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        problemListViewModel = ViewModelProvider(requireActivity()).get(ProblemListViewModel::class.java)

        binding.problemListRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.problemListRV.adapter = problemListAdapter


        binding.problemListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)


        binding.problemListNoDataTV.nodataTextViewHint.visibility = View.VISIBLE
        binding.problemListNoDataTV.nodataTextViewHint.setText(R.string.problem_list_add_hint)
        UIUtil.makePlusColorPrimary(binding.problemListNoDataTV.nodataTextViewHint)


        imm = this.activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager

        progressDialog = (this.activity as MDBaseActivity).progressDialog

        problemListAdapter.setProcedureSelectListener(object : ProblemListAdapter.ProblemListSelectListener{
            override fun onProblemClick(patMedCondition: PatientMedicalCondition) {
                val intent = Intent(this@ActiveProblemListFragment.context , AddProblemListActivity::class.java)
                intent.putExtra(ModelUtil.problemListToEditIntent , true)
                intent.putExtra(ModelUtil.problemListEditDataIntent , patMedCondition.toString)
                intent.putExtra(ModelUtil.isHealthPassportIntent , problemListViewModel.isHealthPassport)
                activity?.startActivityForResult(intent , 0)
                activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            }

            override fun onProblemDelete(patMedCondition: PatientMedicalCondition , position: Int) {
                val problemListActivity = activity
                if (problemListActivity is ProblemListActivity){
                    problemListActivity.deleteProblem(patMedCondition , position)
                }
            }
        })
        
        problemListViewModel.problemListLive.observe(viewLifecycleOwner , Observer {  resp ->

            if (resp != null) {


                if (resp is ArrayList<*>) {
                    val patMedPairList = resp as ArrayList<PatientMedicalCondition>


                    binding.problemListProgress.root.visibility = View.GONE

                        if (binding.problemListSwipeRefreshLayout.isRefreshing){
                            binding.problemListSwipeRefreshLayout.isRefreshing = false
                        }

                        val linearLayoutManager = binding.problemListRV.layoutManager as LinearLayoutManager
                        linearLayoutManager.scrollToPositionWithOffset(0 ,0  )

                        val size = problemListAdapter.setProblems(patMedPairList)
                        if (size == 0) {
                            binding.problemListNoDataTV.root.visibility = View.VISIBLE
                            binding.problemListRV.visibility = View.GONE
                        } else {
                            binding.problemListNoDataTV.root.visibility = View.GONE
                            binding.problemListRV.visibility = View.VISIBLE

                            if (problemListViewModel.isHealthPassport && !BuildUtil.isCustomApp) {

                                val height = UIUtil.getScreenHeight(this.activity)

                                val tv = TypedValue()
                                var actionBarHeight = 0
                                if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                                    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
                                }

                                val viewHeight = ((height - actionBarHeight)*2) /3

                                val footerTreeObserver = binding.problemListRV.viewTreeObserver

                                footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                                    override fun onGlobalLayout() {

                                        try {

                                            binding.problemListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                                            val problemHeight = binding.problemListRV.height ?: 0
                                            Log.d("MDHPBgHeights" , "viewHeight -> $viewHeight ; rvHeight -> $problemHeight")

                                            if (problemHeight < viewHeight){
                                                binding.problemListHPBgLayout.visibility = View.VISIBLE
                                            }
                                            else{
                                                binding.problemListHPBgLayout.visibility = View.GONE
                                            }

                                        }
                                        catch (e: Exception){

                                        }
                                    }
                                })
                            } else {
                                binding.problemListHPBgLayout.visibility = View.GONE
                            }

                        }

                }
                else if (resp is APIResponseError) {
                    if (!problemListViewModel.isProblemListFetched) {
                        binding.problemListNoDataTV.root.visibility = View.GONE
                        binding.problemListRV.visibility = View.GONE
                        binding.problemListErrorPage.root.visibility = View.VISIBLE
                    }
                }

                problemListViewModel.isProblemListFetchComplete = true
            }
        })


        if (!problemListViewModel.isProblemListFetched){
            problemListViewModel.getActiveProblemList()
        }


        binding.problemListSwipeRefreshLayout.setOnRefreshListener {
            binding.problemListSwipeRefreshLayout.isRefreshing = true
            problemListViewModel.getActiveProblemList()
        }


        binding.problemListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.problemListErrorPage.root.visibility = View.GONE
            refreshData()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun deleteProblem(position: Int){
        val size = problemListAdapter.removeProblem(position)
        if (size == 0) {
            binding.problemListNoDataTV.root.visibility = View.VISIBLE
            binding.problemListRV.visibility = View.GONE
        } else {
            binding.problemListNoDataTV.root.visibility = View.GONE
            binding.problemListRV.visibility = View.VISIBLE
        }
    }

    override fun refreshData() {
        problemListViewModel.isProblemListFetched = false
        problemListViewModel.isProblemListFetchComplete = false
        binding.problemListProgress.root.visibility = View.VISIBLE
        problemListViewModel.getActiveProblemList()
    }

}
