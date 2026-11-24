package com.charm.phr.mphr.ui.problem_list

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityProblemListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PatientMedicalCondition
import com.charm.phr.mphr.ui.problem_list.add_problem_list.AddProblemListActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class ProblemListActivity : MDBaseActivity() {

    @Inject
    lateinit var problemListViewPagerAdapter: ProblemListViewPagerAdapter


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var problemListViewModel: ProblemListViewModel

    private var problemSubTitles: ArrayList<String> = arrayListOf()

    lateinit var binding: ActivityProblemListBinding

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProblemListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        problemListViewModel = ViewModelProvider(this, viewModelFactory).get(ProblemListViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.problemListToolbar)
        UIUtil.applyViewInsertForBottomView(binding.problemListMainLayout)

        problemListViewModel.isHealthPassport = intent.getBooleanExtra(ModelUtil.isHealthPassportIntent, false)
        problemSubTitles = if (problemListViewModel.isHealthPassport) {

            arrayListOf(getString(R.string.problem_list_active_str))
        } else {

            arrayListOf(
                getString(R.string.problem_list_active_str), getString(
                    R.string.problem_list_history_str
                )
            )
        }
        binding.problemListViewPager.offscreenPageLimit = ModelUtil.viewPagerOffScreenPageLimit
        binding.problemListViewPager.adapter = problemListViewPagerAdapter
        TabLayoutMediator(binding.problemListTabLayout, binding.problemListViewPager) { tab, position ->
            tab.text = problemSubTitles[position]
        }.attach()

        if (problemListViewModel.isHealthPassport) {
            binding.problemListTabLayout.visibility = View.GONE
            binding.problemListToolbarText.setText(R.string.problem_list_health_passport_str)
        } else {
            binding.problemListToolbarText.setText(R.string.problem_list_str)
            binding.problemListTabLayout.visibility = View.VISIBLE
        }

        binding.problemListBackBtn.setOnClickListener {
            onBackPressed()
        }


        binding.problemListAddBtn.setOnClickListener {

            val intent = Intent(this, AddProblemListActivity::class.java)
            intent.putExtra(ModelUtil.isHealthPassportIntent, problemListViewModel.isHealthPassport)
            startActivityForResult(intent, 0)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                problemListViewPagerAdapter.refreshData(binding.problemListViewPager.currentItem)
            }
        }
    }


    fun deleteProblem(patientMedicalCondition: PatientMedicalCondition, position: Int) {
        val builder = AlertDialog.Builder(this)

        if (problemListViewModel.isHealthPassport) {
            builder.setMessage(R.string.problem_list_health_passport_delete_confirmation_msg)
        } else {
            builder.setMessage(R.string.problem_list_delete_confirmation_msg)
        }
        builder.setPositiveButton(R.string.yes_str) { _, _ ->
            progressDialog.show()
            problemListViewModel.deleteProblem(patientMedicalCondition).observe(this, Observer { resp ->
                if (resp != null) {
                    progressDialog.dismiss()
                    if (resp is Boolean) {
                        problemListViewPagerAdapter.deleteProblem(position, binding.problemListViewPager?.currentItem ?: 0)

                        if (problemListViewModel.isHealthPassport) {
                            UIUtil.showToast(this, R.string.problem_list_health_passport_delete_success_msg, Toast.LENGTH_LONG)
                        } else {
                            UIUtil.showToast(this, R.string.problem_list_delete_success_msg, Toast.LENGTH_LONG)
                        }
                    } else if (resp is APIResponseError) {

                        if (problemListViewModel.isHealthPassport) {
                            UIUtil.showToast(this, R.string.error_problem_list_health_passport_delete__msg, Toast.LENGTH_LONG)
                        } else {
                            UIUtil.showToast(this, R.string.error_problem_list_delete__msg, Toast.LENGTH_LONG)
                        }
                    }
                }
            })
        }
        builder.setNegativeButton(R.string.no_str) { _, _ ->

        }

        builder.show()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}
