package com.charm.phr.mphr.ui.document.preview_document

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityPreviewDocumentBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.ui.document.share_document.ShareDocumentActivity
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.PdfUtil
import com.charm.phr.mphr.util.PermissionUtil
import com.charm.phr.mphr.util.ResizeTransformation
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.io.File
import javax.inject.Inject

class PreviewDocumentActivity : MDBaseActivity() {

    private lateinit var previewDocumentViewModel: PreviewDocumentViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var picasso: Picasso

    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    lateinit var binding: ActivityPreviewDocumentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        previewDocumentViewModel = ViewModelProvider(this, viewModelFactory).get(PreviewDocumentViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.previewDocumentToolbar)
        UIUtil.applyViewInsertForBottomView(binding.previewDocumentMainLayout)


        previewDocumentViewModel.documentID = intent.getStringExtra(ModelUtil.documentFileIdIntent) ?: ""

        initializePermissionResultLauncher()

        getPDF()
        binding.previewDocumentErrorPage.errorPageTryAgainBtn.setOnClickListener {
            getPDF()
        }

        binding.previewDocumentBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.previewDocumentShareBtn.setOnClickListener {
            // sharePDF()
            shareDocument()
        }

        binding.previewDocumentDownloadBtn.setOnClickListener {
            PermissionUtil.requestWriteStoragePermission(this, ::permissionAcceptCallBack, requestPermissionLauncher)
        }
    }


    fun shareDocument() {
        val intent = Intent(this.applicationContext, ShareDocumentActivity::class.java)
        val documentToShare = Document(
            fileId = previewDocumentViewModel.documentID,
            fileName = previewDocumentViewModel.filePath.substring(previewDocumentViewModel.filePath.lastIndexOf("/") + 1),
            filePath = previewDocumentViewModel.filePath,
            fileSize = previewDocumentViewModel.fileSize
        )
        intent.putExtra(ModelUtil.documentShareString, MDJsonBuilder.formDocumentListJson(arrayListOf(documentToShare)).toString())
        startActivity(intent)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun sharePDF() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val fileToShare = File(previewDocumentViewModel.filePath ?: "")

        val title = getString(R.string.document_single_str)


        if (fileToShare.exists()) {

            MDBaseActivity.isPassCodeAllowed = false   // preventing from asking passcode
            val fileUri = FileProvider.getUriForFile(applicationContext, resources.getString(R.string.content_provider_authority), fileToShare)
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            shareIntent.type = "application/pdf"
            startActivityForResult((Intent.createChooser(shareIntent, title)), 10)
        } else UIUtil.showToast(this, R.string.document_preview_failed, Toast.LENGTH_SHORT)

    }

    private fun downloadPDF() {


        if (!previewDocumentViewModel.isPDFDownloaded) {

            val folderName = ModelUtil.documentFolder
            val respFile = FileUtil.copyFile(applicationContext, previewDocumentViewModel.filePath, folderName, previewDocumentViewModel.mdFile?.fileName)
            if (respFile?.exists() == true) {
                UIUtil.showToast(this, respFile.path, Toast.LENGTH_SHORT)
                UIUtil.showToast(this, R.string.document_download_success, Toast.LENGTH_SHORT)
                previewDocumentViewModel.isPDFDownloaded = true
            } else {
                UIUtil.showToast(this, R.string.document_download_failed, Toast.LENGTH_SHORT)
            }
        } else {
            UIUtil.showToast(this, R.string.document_already_download, Toast.LENGTH_SHORT)
        }
    }

    private fun getPDF() {

        binding.previewDocumentProgress.root.visibility = View.VISIBLE
        binding.previewDocumentPDFViewer.visibility = View.GONE
        binding.previewDocumentErrorPage.root.visibility = View.GONE
        binding.previewDocumentImgLayout.visibility = View.GONE

        binding.previewDocumentShareBtn.visibility = View.GONE
        previewDocumentViewModel.getDocumentPDF().observe(this, Observer { resp ->

            if (resp != null) {

                binding.previewDocumentProgress.root.visibility = View.GONE
                if (resp is MDFile) {

                    previewDocumentViewModel.mdFile = resp

                    val savedFile = FileUtil.saveDocumentFromResponseBody(resp, applicationContext, ModelUtil.folderTemp)

                    if (savedFile != null) {
                        val fileName = savedFile.name ?: ""

                        previewDocumentViewModel.filePath = savedFile.path
                        previewDocumentViewModel.fileSize = (savedFile.length() / (1024 * 1024)).toString() + " MB"
                        if ((fileName.endsWith("jpg", true) || fileName.endsWith("jpeg", true) || fileName.endsWith("png", true))) {
                            val picLoader = picasso.load(Uri.fromFile(savedFile))
                            picLoader.memoryPolicy(MemoryPolicy.NO_CACHE)
                            picLoader.networkPolicy(NetworkPolicy.NO_CACHE)

                            picLoader.transform(ResizeTransformation(1800))
                                .into(binding.previewDocumentImg, object : Callback {
                                    override fun onSuccess() {
                                        try {
                                            binding.previewDocumentImgLayout.visibility = View.VISIBLE
                                        } catch (e: Exception) {

                                        }

                                    }

                                    override fun onError(e: Exception?) {
                                        binding.previewDocumentNoDataLayout.root.visibility = View.VISIBLE
                                        binding.previewDocumentPDFViewer.visibility = View.GONE
                                        binding.previewDocumentImgLayout.visibility = View.GONE

                                        binding.previewDocumentNoDataLayout.nodataImageView.setImageResource(R.drawable.ic_preview_not_available)
                                        binding.previewDocumentNoDataLayout.nodataTextView.setText(R.string.error_in_loading_document)
                                    }
                                })
                        } else if (fileName.endsWith("pdf", true)) {
                            binding.previewDocumentPDFViewer.visibility = View.VISIBLE
                            val success = PdfUtil.initPdfViewer(
                                binding.previewDocumentPDFViewer,
                                savedFile,
                                this
                            ) {
                                // On error, show error page
                                binding.previewDocumentPDFViewer.visibility = View.GONE
                                binding.previewDocumentNoDataLayout.root.visibility = View.VISIBLE
                                binding.previewDocumentNoDataLayout.nodataImageView.setImageResource(R.drawable.ic_preview_not_available)
                                binding.previewDocumentNoDataLayout.nodataTextView.setText(R.string.error_loading_pdf)
                            }
                            if (!success) {
                                binding.previewDocumentPDFViewer.visibility = View.GONE
                            }
                        } else {
                            binding.previewDocumentNoDataLayout.root.visibility = View.VISIBLE
                            binding.previewDocumentPDFViewer.visibility = View.GONE
                            binding.previewDocumentImgLayout.visibility = View.GONE

                            binding.previewDocumentNoDataLayout.nodataImageView.setImageResource(R.drawable.ic_preview_not_available)
                            binding.previewDocumentNoDataLayout.nodataTextView.setText(R.string.preview_document_invalid_file_str)
                            binding.previewDocumentNoDataLayout.nodataTextViewHint.setText(R.string.preview_document_invalid_file_hint)
                            binding.previewDocumentNoDataLayout.nodataTextViewHint.visibility = View.VISIBLE
                        }
                        binding.previewDocumentShareBtn.visibility = View.VISIBLE
                    } else {
                        binding.previewDocumentErrorPage.root.visibility = View.VISIBLE
                    }

                } else if (resp is APIResponseError) {
                    binding.previewDocumentErrorPage.root.visibility = View.VISIBLE
                }
            }
        })

    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    //region Handling Permission for write storage
    private fun initializePermissionResultLauncher() {
        requestPermissionLauncher = PermissionUtil.getPermissionResultLauncher(
            this,
            ::permissionAcceptCallBack,
            R.string.allow_view_document,
            null, false, ::permissionDeniedCallBack
        )
    }

    private fun permissionAcceptCallBack(permissions: Array<String>) {
        permissions.forEach { permission ->
            when (permission) {
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    downloadPDF()
                }
            }
        }
    }


    private fun permissionDeniedCallBack(permissions: Array<String>) {
        PermissionUtil.requestPermission(permissions, requestPermissionLauncher)
    }
    //endregion
}