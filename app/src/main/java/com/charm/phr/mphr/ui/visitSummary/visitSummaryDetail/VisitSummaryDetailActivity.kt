package com.charm.phr.mphr.ui.visitSummary.visitSummaryDetail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityVisitSummaryDetailBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.PdfUtil
import com.charm.phr.mphr.util.UIUtil
import java.io.File
import javax.inject.Inject

class VisitSummaryDetailActivity : MDBaseActivity() {


    private lateinit var visitSummaryDetailViewModel: VisitSummaryDetailViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var binding: ActivityVisitSummaryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVisitSummaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        visitSummaryDetailViewModel = ViewModelProvider(this, viewModelFactory).get(VisitSummaryDetailViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.visitSummaryDetailToolbar)
        UIUtil.applyViewInsertForBottomView(binding.visitSummaryDetailMainLayout)

        visitSummaryDetailViewModel.visitSummaryDocumentId = intent.getStringExtra(ModelUtil.visitSummaryDocumentIDIntent) ?: ""
        visitSummaryDetailViewModel.practiceId = intent.getStringExtra(ModelUtil.practiceIdIntent) ?: ""


        if (visitSummaryDetailViewModel.visitSummaryDocumentId.isNotBlank()) {


            getVisitSummaryDetail()
            binding.visitSummaryDetailErrorPage.errorPageTryAgainBtn.setOnClickListener {
                getVisitSummaryDetail()
            }
        } else {

            binding.visitSummaryDetailProgress.root.visibility = View.GONE
            binding.visitSummaryDetailPDFViewer.visibility = View.GONE
            binding.visitSummaryDetailNoDataTV.root.visibility = View.VISIBLE
            binding.visitSummaryDetailErrorPage.root.visibility = View.GONE
        }


        binding.visitSummaryDetailBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.visitSummaryDetailPDFShareBtn.setOnClickListener {
            sharePDF()
        }

    }

    private fun sharePDF() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val fileToShare = File(visitSummaryDetailViewModel.filePath ?: "")


        if (fileToShare.exists()) {

            isPassCodeAllowed = false   // preventing from asking passcode
            val fileUri = FileProvider.getUriForFile(applicationContext, resources.getString(R.string.content_provider_authority), fileToShare)
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            shareIntent.type = "application/pdf"
            startActivityForResult((Intent.createChooser(shareIntent, getString(R.string.visit_summary_str))), 10)
        } else UIUtil.showToast(this, getString(R.string.demo_pdf_preview_failed, getString(R.string.visit_summary_str)), Toast.LENGTH_SHORT)

    }


    private fun getVisitSummaryDetail() {

        binding.visitSummaryDetailProgress.root.visibility = View.VISIBLE
        binding.visitSummaryDetailPDFViewer.visibility = View.GONE
        binding.visitSummaryDetailNoDataTV.root.visibility = View.GONE
        binding.visitSummaryDetailErrorPage.root.visibility = View.GONE

        visitSummaryDetailViewModel.getVisitSummaryPDF().observe(this, Observer { resp ->

            if (resp != null) {

                binding.visitSummaryDetailProgress.root.visibility = View.GONE
                if (resp is MDFile) {

                    val savedFile = FileUtil.savePdfFromResponseBody(resp, applicationContext, ModelUtil.folderTemp)

                    if (savedFile != null) {
                        binding.visitSummaryDetailPDFViewer.visibility = View.VISIBLE
                        val success = PdfUtil.initPdfViewer(
                            binding.visitSummaryDetailPDFViewer,
                            savedFile,
                            this
                        ) {
                            // On error, show error page
                            binding.visitSummaryDetailPDFViewer.visibility = View.GONE
                            binding.visitSummaryDetailErrorPage.root.visibility = View.VISIBLE
                        }
                        if (success) {
                            visitSummaryDetailViewModel.filePath = savedFile.path
                        } else {
                            binding.visitSummaryDetailPDFViewer.visibility = View.GONE
                        }
                    } else {
                        binding.visitSummaryDetailErrorPage.root.visibility = View.VISIBLE
                    }

                } else if (resp is APIResponseError) {
                    binding.visitSummaryDetailErrorPage.root.visibility = View.VISIBLE
                }
            }
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
