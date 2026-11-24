package com.charm.phr.mphr.ui.lab.detail_pdf

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityLabDetailPdfBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.PdfUtil
import com.charm.phr.mphr.util.UIUtil
import java.io.File
import javax.inject.Inject

class LabDetailPDFActivity : MDBaseActivity() {

    private lateinit var labDetailPDFViewModel: LabDetailPDFViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var binding: ActivityLabDetailPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabDetailPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        labDetailPDFViewModel = ViewModelProvider(this, viewModelFactory).get(LabDetailPDFViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.labDetailPDFToolbar)
        UIUtil.applyViewInsertForBottomView(binding.labDetailPdfMainLayout)

        labDetailPDFViewModel.isLabOrder = intent.getBooleanExtra(ModelUtil.labIsOrder, false)


        if (labDetailPDFViewModel.isLabOrder) {

            //labDetailPDFToolbarText.setText(R.string.lab_order_full_str)
            binding.labDetailPDFToolbarText.text = intent.getStringExtra(ModelUtil.labOrderNameIntent) ?: ""
            labDetailPDFViewModel.orderId = intent.getStringExtra(ModelUtil.labOrderId) ?: ""
        } else {
            binding.labDetailPDFToolbarText.setText(R.string.lab_report_str)
            labDetailPDFViewModel.groupId = intent.getStringExtra(ModelUtil.labResultGroupId) ?: ""
            labDetailPDFViewModel.practiceId = intent.getStringExtra(ModelUtil.practiceIdIntent) ?: ""
        }



        if (labDetailPDFViewModel.groupId.isNotBlank() || labDetailPDFViewModel.orderId.isNotBlank()) {

            getLabDetailPDF()
            binding.labDetailPDFErrorPage.errorPageTryAgainBtn.setOnClickListener {
                getLabDetailPDF()
            }
        } else {

            binding.labDetailPDFProgress.root.visibility = View.GONE
            binding.labDetailPDFPDFViewer.visibility = View.GONE
            binding.labDetailPDFNoDataTV.root.visibility = View.VISIBLE
            binding.labDetailPDFErrorPage.root.visibility = View.GONE
        }


        binding.labDetailPDFBackBtn.setOnClickListener {
            onBackPressed()
        }
        binding.labDetailPDFShareBtn.setOnClickListener {
            sharePDF()
        }

    }

    private fun sharePDF() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val fileToShare = File(labDetailPDFViewModel.filePath ?: "")


        if (fileToShare.exists()) {

            isPassCodeAllowed = false   // preventing from asking passcode
            val fileUri = FileProvider.getUriForFile(applicationContext, resources.getString(R.string.content_provider_authority), fileToShare)
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            shareIntent.type = "application/pdf"
            startActivityForResult((Intent.createChooser(shareIntent, getString(R.string.lab_report_str))), 10)
        } else UIUtil.showToast(this, getString(R.string.demo_pdf_preview_failed, getString(R.string.lab_report_str)), Toast.LENGTH_SHORT)

    }

    private fun getLabDetailPDF() {

        binding.labDetailPDFProgress.root.visibility = View.VISIBLE
        binding.labDetailPDFPDFViewer.visibility = View.GONE
        binding.labDetailPDFNoDataTV.root.visibility = View.GONE
        binding.labDetailPDFErrorPage.root.visibility = View.GONE



        labDetailPDFViewModel.getLabPDF().observe(this) { resp ->

            if (resp != null) {

                binding.labDetailPDFProgress.root.visibility = View.GONE
                if (resp is MDFile) {

                    val savedFile = FileUtil.savePdfFromResponseBody(resp, applicationContext, ModelUtil.folderTemp)

                    if (savedFile != null) {
                        binding.labDetailPDFPDFViewer.visibility = View.VISIBLE
                        val success = PdfUtil.initPdfViewer(
                            binding.labDetailPDFPDFViewer,
                            savedFile,
                            this
                        ) {
                            // On error, show error page
                            binding.labDetailPDFPDFViewer.visibility = View.GONE
                            binding.labDetailPDFErrorPage.root.visibility = View.VISIBLE
                        }
                        if (success) {
                            labDetailPDFViewModel.filePath = savedFile.path
                        } else {
                            binding.labDetailPDFPDFViewer.visibility = View.GONE
                        }
                    } else {
                        binding.labDetailPDFErrorPage.root.visibility = View.VISIBLE
                    }

                } else if (resp is APIResponseError) {
                    binding.labDetailPDFErrorPage.root.visibility = View.VISIBLE
                }
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
