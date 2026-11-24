package com.charm.phr.mphr.ui.lab.resultDetail

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityLabResultDetailBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.MedicalRecordGroup
import com.charm.phr.mphr.ui.lab.detail_pdf.LabDetailPDFActivity
import com.charm.phr.mphr.ui.lab.plotDetailGraph.LabDetailsGraphActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.UIUtil.Companion.setInterpretationColor
import javax.inject.Inject

class LabResultDetailActivity : MDBaseActivity() {

    @Inject
    lateinit var labResultDetailListAdapter: LabResultDetailListAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityLabResultDetailBinding

    private lateinit var labResultDetailViewModel: LabResultDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabResultDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        labResultDetailViewModel = ViewModelProvider(this, viewModelFactory)[LabResultDetailViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.labResultDetailToolbar)
        UIUtil.applyViewInsertForBottomView(binding.labResultDetailMainLayout)

        labResultDetailViewModel.groupId = intent.getStringExtra(ModelUtil.labResultGroupId) ?: ""
        labResultDetailViewModel.practiceId = intent.getStringExtra(ModelUtil.practiceIdIntent) ?: ""
        labResultDetailViewModel.interpretation = intent.getStringExtra(ModelUtil.interpretation) ?: ""

        binding.labResultDetailToolbarTxtTV.text = intent.getStringExtra(ModelUtil.labResultLabNameIntent) ?: ""

        binding.labResultDetailRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.labResultDetailRV.adapter = labResultDetailListAdapter

        if (labResultDetailViewModel.isMedicalRecordDetailFetch) {

            if (labResultDetailViewModel.labResultDetailLive.hasActiveObservers()) {
                labResultDetailViewModel.labResultDetailLive.removeObservers(this)
            }
            labResultDetailViewModel.labResultDetailLive.observe(this) { labResultList ->

                if (labResultList != null) {
                    setLabResultDetail(labResultList)
                }
            }
        } else {
            fetchLabResultDetail()
        }


        binding.labResultDetailBackBtn.setOnClickListener {
            onBackPressed()
        }


        binding.labResultDetailErrorPage.errorPageTryAgainBtn.setOnClickListener {
            labResultDetailViewModel.isMedicalRecordDetailFetch = false
            binding.labResultDetailErrorPage.root.visibility = View.GONE
            fetchLabResultDetail()
        }

        labResultDetailListAdapter.setLabResultDetailListAdapterListener(object :
            LabResultDetailListAdapter.LabResultDetailListAdapterListener {
            override fun notifyLabResultHeightIsTaken() {

                handler.postDelayed({
                    binding.labResultDetailProgress.root.visibility = View.GONE
                }, 300)
            }

            override fun labResultDetailPlot(
                paramName: String, referenceText: String?, recordUnit: String?
            ) {
                intentToLabResultDetailPlot(paramName, referenceText, recordUnit)
            }
        })

    }

    private fun fetchLabResultDetail() {
        binding.labResultDetailProgress.root.visibility = View.VISIBLE
        binding.labResultDetailScroll.visibility = View.GONE

        labResultDetailViewModel.getLabResultDetail().observe(this) { resp ->
            if (resp != null) {
                if (resp is MedicalRecordGroup) {
                    labResultDetailViewModel.isMedicalRecordDetailFetch = true
                    labResultDetailViewModel.labResultDetailLive.postValue(resp)
                    setLabResultDetail(resp)
                } else if (resp is APIResponseError) {
                    binding.labResultDetailProgress.root.visibility = View.GONE
                    binding.labResultDetailScroll.visibility = View.GONE
                    binding.labResultDetailErrorPage.root.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setLabResultDetail(labResultDetail: MedicalRecordGroup) {

        binding.labResultDetailProgress.root.visibility = View.GONE
        binding.labResultDetailScroll.visibility = View.VISIBLE

        labResultDetailListAdapter.setMedicalRecordEntries(labResultDetail.medicalRecordEntryList)

        setResultHeaderCard(labResultDetail)

        binding.labResultMessageToPatientCard.visibility = View.VISIBLE

//        if (!labResultDetail.medicalRecordEntryList.firstOrNull()?.reviewMedicalRecordEntry?.messageToPatient.isNullOrBlank()) {
//            labResultMessageToPatientTV.text =
//                labResultDetail.medicalRecordEntryList.firstOrNull()?.reviewMedicalRecordEntry?.messageToPatient
//        } TODO:Message to patient here move to new place

        binding.labResultViewFullReportBtn.setOnClickListener {
            val intent = Intent(this, LabDetailPDFActivity::class.java)
            intent.putExtra(ModelUtil.labResultGroupId, labResultDetail.groupId)
            intent.putExtra(ModelUtil.practiceIdIntent, labResultDetailViewModel.practiceId)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    fun setResultHeaderCard(labResultDetail: MedicalRecordGroup) {

        val medicalRecordEntry = labResultDetail.medicalRecordEntryList.firstOrNull()

        val reviewedBy = medicalRecordEntry?.reviewMedicalRecordEntry?.reviewerName
        if (!reviewedBy.isNullOrBlank()) {
            binding.lrReviewedByValueTv.text = reviewedBy
        } else {
            binding.lrReviewedByLabelTv.visibility = View.GONE
            binding.lrReviewedByValueTv.visibility = View.GONE
        }

        val resultDate = medicalRecordEntry?.date?.toString(ModelUtil.charmDisplayDateFormat)
        if (!resultDate.isNullOrBlank()) {
            binding.lrDateValueTv.text = resultDate
        } else {
            binding.lrDateLabelTv.visibility = View.GONE
            binding.lrDateValueTv.visibility = View.GONE
        }

        val resultStatus = labResultDetail.reportStatus
        if (!resultStatus.isNullOrBlank()) {
            binding.lrResultStatusValueTv.text = resultStatus
        } else {
            binding.lrResultStatusValueTv.visibility = View.GONE
            binding.lrResultStatusLabelTv.visibility = View.GONE
        }

        val physicianAdvice = medicalRecordEntry?.reviewMedicalRecordEntry?.messageToPatient
        if (!physicianAdvice.isNullOrBlank()) {
            binding.lrPhysicianAdviceValueTv.text = physicianAdvice
        } else {
            binding.lrPhysicianAdviceValueTv.visibility = View.GONE
            binding.lrPhysicianAdviceLabelTv.visibility = View.GONE
        }

        val interpretationValue = labResultDetailViewModel.interpretation
        if (interpretationValue.isNotBlank()) {
            binding.lrInterpretationValueTv.setInterpretationColor(interpretationValue)
            binding.lrInterpretationValueTv.text = interpretationValue
        } else {
            binding.lrInterpretationLabelTv.visibility = View.GONE
            binding.lrInterpretationValueTv.visibility = View.GONE
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun intentToLabResultDetailPlot(
        paramName: String, referenceText: String?, recordUnit: String?
    ) {
        val intent = Intent(this, LabDetailsGraphActivity::class.java)
        intent.putExtra(ModelUtil.labParamNameIntent, paramName)
        intent.putExtra(ModelUtil.labParamReferenceIntent, referenceText)
        intent.putExtra(ModelUtil.labParamUnitIntent, recordUnit)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
