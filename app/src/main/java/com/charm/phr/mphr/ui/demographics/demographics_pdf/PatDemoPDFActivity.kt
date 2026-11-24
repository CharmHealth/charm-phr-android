package com.charm.phr.mphr.ui.demographics.demographics_pdf

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityPatDemoPdfBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.PdfUtil
import com.charm.phr.mphr.util.UIUtil
import java.io.File
import javax.inject.Inject

class PatDemoPDFActivity : MDBaseActivity() {

    private lateinit var binding: ActivityPatDemoPdfBinding

    private lateinit var patDemoPDFViewModel: PatDemoPDFViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatDemoPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patDemoPDFViewModel = ViewModelProvider(this , viewModelFactory).get(PatDemoPDFViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.patDemoPDFToolbar)
        UIUtil.applyViewInsertForBottomView(binding.patDemoPDFMainLayout)

        patDemoPDFViewModel.isHealthCard = intent.getBooleanExtra(ModelUtil.patientIsHealthCardIntent  , false)


        if (patDemoPDFViewModel.isHealthCard){

            binding.patDemoPDFToolbarText.setText(R.string.health_card_str)
        }
        else {
            binding.patDemoPDFToolbarText.setText(R.string.health_passport_pdf_str)
        }



        getPDF()
        binding.patDemoPDFErrorPage.errorPageTryAgainBtn.setOnClickListener {
            getPDF()
        }



        binding.patDemoPDFBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.patDemoPDFShareBtn.setOnClickListener {
            sharePDF()
        }

        binding.patDemoPDFDownloadBtn.setOnClickListener {
            downloadPDF()
        }
    }

    private fun sharePDF(){
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val fileToShare = File(patDemoPDFViewModel.filePath ?: "")

        val title= if (patDemoPDFViewModel.isHealthCard){
            getString(R.string.health_card_pdf_str)
        }
        else{
            getString(R.string.demographics_pdf_str)
        }

        if (fileToShare.exists()) {

            MDBaseActivity.isPassCodeAllowed = false   // preventing from asking passcode
            val fileUri = FileProvider.getUriForFile(applicationContext, resources.getString(R.string.content_provider_authority), fileToShare)
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            shareIntent.type = "application/pdf"
            startActivityForResult((Intent.createChooser(shareIntent, title)), 10)
        }  else UIUtil.showToast(this@PatDemoPDFActivity, getString( R.string.demo_pdf_preview_failed , title), Toast.LENGTH_SHORT)

    }
    private fun downloadPDF(){
        val title = if (patDemoPDFViewModel.isHealthCard) {
            getString(R.string.health_card_pdf_str)
        } else {
            getString(R.string.demographics_pdf_str)
        }

        if (!patDemoPDFViewModel.isPDFDownLoaded) {

            val folderName = if (patDemoPDFViewModel.isHealthCard) {
                ModelUtil.healthCardFolder
            } else {
                ModelUtil.demographicsFolder
            }
            val respFile = FileUtil.copyFile(applicationContext, patDemoPDFViewModel.filePath, folderName)
            if (respFile?.exists() == true) {
                UIUtil.showToast(this, getString(R.string.demo_pdf_download_success, title), Toast.LENGTH_SHORT)
                patDemoPDFViewModel.isPDFDownLoaded = true
            } else {
                UIUtil.showToast(this, getString(R.string.demo_pdf_download_failed, title), Toast.LENGTH_SHORT)
            }
        }
        else{
            UIUtil.showToast(this, getString(R.string.demo_pdf_already_download, title), Toast.LENGTH_SHORT)
        }
    }


    private fun getPDF(){

        binding.patDemoPDFProgress.root.visibility = View.VISIBLE
        binding.patDemoPDFPDFViewer.visibility = View.GONE
        binding.patDemoPDFErrorPage.root.visibility = View.GONE



        patDemoPDFViewModel.fetchPDF().observe(this , Observer {resp ->

            if (resp != null) {

                binding.patDemoPDFProgress.root.visibility = View.GONE
                if (resp is MDFile){

                    val savedFile = FileUtil.savePdfFromResponseBody(resp  , applicationContext , ModelUtil.folderTemp)

                    if (savedFile != null){
                        binding.patDemoPDFPDFViewer.visibility = View.VISIBLE
                        val success = PdfUtil.initPdfViewer(
                            binding.patDemoPDFPDFViewer,
                            savedFile,
                            this
                        ) {
                            // On error, show error page
                            binding.patDemoPDFPDFViewer.visibility = View.GONE
                            binding.patDemoPDFErrorPage.root.visibility = View.VISIBLE
                        }
                        if (success) {
                            patDemoPDFViewModel.filePath = savedFile.path
                            binding.patDemoPDFShareBtn.visibility = View.VISIBLE
                            //patDemoPDFDownloadBtn.visibility = View.VISIBLE
                        } else {
                            binding.patDemoPDFPDFViewer.visibility = View.GONE
                        }
                    }
                    else{
                        binding.patDemoPDFErrorPage.root.visibility = View.VISIBLE
                    }

                }
                else if (resp is APIResponseError){
                    binding.patDemoPDFErrorPage.root.visibility = View.VISIBLE
                }
            }
        })

    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}
