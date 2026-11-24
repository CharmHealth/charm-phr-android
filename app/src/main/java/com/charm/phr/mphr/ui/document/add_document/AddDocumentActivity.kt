package com.charm.phr.mphr.ui.document.add_document

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAddDocumentBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Category
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.model.DocumentMetaData
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDAddBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

class AddDocumentActivity : MDAddBaseActivity<AddDocumentViewModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var binding: ActivityAddDocumentBinding

    private val docPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {

            val extension = FileUtil.getMimeType(this, uri)

            val fileName = FileUtil.getFileName(uri, this) ?: ""

            if (extension != null) {
                val filePath = FileUtil.saveAttachment(uri, extension, this)
                if (!filePath.isNullOrBlank()) {


                    val document = Document()
                    document.fileName = fileName
                    document.filePath = filePath
                    addViewModel.document = document

                    binding.addDocumentNameET.setText(fileName.split(".").dropLast(1).joinToString("."))

                    fetchCategories()
                } else {
                    UIUtil.showToast(this, R.string.error_occurred_while_handling_file, Toast.LENGTH_LONG)
                    onBackPressed()
                }
            } else {
                UIUtil.showToast(this, R.string.error_file_not_supported, Toast.LENGTH_LONG)
                onBackPressed()
            }
        }else{
            Handler(Looper.getMainLooper()).post {
                onBackPressed()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UIUtil.applyViewInsertForToolbar(binding.addDocumentToolbar)
        UIUtil.applyViewInsertForBottomView(binding.addDocumentRootLayout)

        UIUtil.makeTextViewMandatory(binding.addDocumentNameLayout)
        UIUtil.makeTextViewMandatory(binding.addDocumentTypeLayout)

        addViewModel.isHealthPassport = intent.getBooleanExtra(ModelUtil.isHealthPassportIntent, false)


        if (addViewModel.isHealthPassport) {
            binding.addDocumentTypeLayout.hint = getString(R.string.document_type_hp_hint)
        }



        binding.addDocumentTypeET.setOnItemClickListener { position, str ->
            addViewModel.selectedCategory = addViewModel.categoryList[position]
        }



        binding.addDocumentErrorPage.errorPageTryAgainBtn.setOnClickListener {
            fetchCategories(false)
        }

        binding.addDocumentBtn.setOnClickListener {

            val document = validateData()

            if (document != null) {

                progressDialog.show()
                addViewModel.addDocument(document).observe(this, Observer { resp ->

                    progressDialog.dismiss()
                    if (resp != null) {
                        if (resp is Boolean) {
                            addViewModel.isDataAdded = true
                            setResult(RESULT_OK)
                            onBackPressed()

                        } else if (resp is APIResponseError) {
                            UIUtil.showToast(this, R.string.error_add_document_req_failed, Toast.LENGTH_LONG)
                        }
                    }
                })
            }
        }

        binding.addDocumentBackBtn.setOnClickListener {
            onBackClick()
        }


        binding.addDocumentToolbar.postDelayed({ //delaying because onResume will be called after onCreate so isPassCodeAllowed will be set to true onResume
            isPassCodeAllowed = false   // preventing from asking passcode
            docPickerLauncher.launch(arrayOf("image/*", "application/pdf"))
        }, 200)

    }

    override fun onResume() {
        super.onResume()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun onBackClick() {
        backPressAlertDialogBuilder.show()
    }

    private fun validateData(): Document? {

        val documentName = binding.addDocumentNameET.text.toString().trim()
        val documentType = binding.addDocumentTypeET.text.toString().trim()


        if (isTextInputLayoutBlank(binding.addDocumentNameLayout, binding.addDocumentScrollView, R.string.error_empty_document_name)) {
            return null
        }
        if (isTextInputLayoutBlank(binding.addDocumentTypeLayout, binding.addDocumentScrollView, R.string.error_empty_document_type)) {
            return null
        }

        val document = addViewModel.document

        val file = FileUtil.renameFile(addViewModel.document.filePath, documentName)

        document.filePath = file.path
        document.fileName = file.name
        document.category = addViewModel.selectedCategory

        document.isHealthPassport = addViewModel.isHealthPassport
        return document
    }

    private fun fetchCategories(showDialog: Boolean = true) {

        if (showDialog) {
            progressDialog.show()
        } else {
            binding.addDocumentProgress.root.visibility = View.VISIBLE
            binding.addDocumentErrorPage.root.visibility = View.GONE
            binding.addDocumentMainLayout.visibility = View.GONE
        }
        addViewModel.getCategories().observe(this, Observer { resp ->

            if (resp != null) {

                progressDialog.dismiss()
                binding.addDocumentProgress.root.visibility = View.GONE


                if (resp is DocumentMetaData) {

                    binding.addDocumentMainLayout.visibility = View.VISIBLE
                    val categoryList = resp.categoryList

                    addViewModel.categoryList = categoryList
                    binding.addDocumentTypeET.setItems(categoryList.mapNotNull { it.categoryName }, this)

                    binding.addDocumentTypeET.setAnchorView(binding.addDocumentTypeLayout)

                    val categoryId = intent.getStringExtra(ModelUtil.documentCategoryIdIntent)
                    if (!categoryId.isNullOrBlank()) {
                        val category = Category()
                        category.categoryId = categoryId
                        category.categoryName = intent.getStringExtra(ModelUtil.documentCategoryNameIntent)

                        addViewModel.selectedCategory = category
                        binding.addDocumentTypeET.setText(category.categoryName)
                    }


                } else if (resp is APIResponseError) {
                    binding.addDocumentErrorPage.root.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun checkDataIsAvailable(): Boolean {
        return false
    }

    override fun checkDataIsEditted(): Boolean {
        return false
    }

    override fun getViewModel(): AddDocumentViewModel {
        return ViewModelProvider(this, viewModelFactory)[AddDocumentViewModel::class.java]
    }
}