package com.charm.phr.mphr.ui.document

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityDocumentBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.model.DocumentMetaData
import com.charm.phr.mphr.ui.document.add_document.AddDocumentActivity
import com.charm.phr.mphr.ui.document.share_document.ShareDocumentActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class DocumentActivity : MDBaseActivity() {

    @Inject
    lateinit var documentViewPagerAdapter: DocumentViewPagerAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var documentViewModel: DocumentViewModel

    private var problemSubTitles: ArrayList<String> = arrayListOf()

    private lateinit var binding: ActivityDocumentBinding

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDocumentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        documentViewModel = ViewModelProvider(this, viewModelFactory)[DocumentViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.documentToolbar)
        UIUtil.applyViewInsertForBottomView(binding.documentMainLayout)


        documentViewModel.isHealthPassport = intent.getBooleanExtra(ModelUtil.isHealthPassportIntent, false)

        binding.documentViewPager.offscreenPageLimit = ModelUtil.viewPagerOffScreenPageLimit
        binding.documentViewPager.adapter = documentViewPagerAdapter
        problemSubTitles = if (documentViewModel.isHealthPassport) {

            arrayListOf(getString(R.string.document_all_str))
        } else {

            arrayListOf(
                getString(R.string.document_all_str), getString(
                    R.string.document_folder_str
                )
            )
        }
        TabLayoutMediator(binding.documentTabLayout, binding.documentViewPager) { tab, position ->
            tab.text = problemSubTitles[position]
        }.attach()

        if (documentViewModel.isHealthPassport) {
            binding.documentTabLayout.visibility = View.GONE
            binding.documentToolbarText.setText(R.string.document_str)
        } else {
            binding.documentToolbarText.setText(R.string.document_str)
            binding.documentTabLayout.visibility = View.VISIBLE
        }

        binding.documentBackBtn.setOnClickListener {
            onBackPressed()
        }
        if (binding.documentViewPager.currentItem == 0) {
            if (!RbacUtil.checkPrivilege(
                    RbacUtil.Companion.RBACPrivileges.ADD_DOCUMENTS,
                    applicationContext
                )
            ) {
                binding.documentAddBtn.hide()
            }
        } else {
            if (!RbacUtil.checkPrivilege(
                    RbacUtil.Companion.RBACPrivileges.ADD_FOLDERS,
                    applicationContext
                )
            ) {
                binding.documentAddBtn.hide()
            }
        }
        binding.documentAddBtn.setOnClickListener {

            if (binding.documentViewPager.currentItem == 0) {
                intentToAddDocument()
            } else {
                documentViewPagerAdapter.showFolderDialog()
            }
        }

        val popupMenu = PopupMenu(this, binding.documentFilterBtnAnchor, Gravity.START)

        documentViewModel.categoryLive.observe(this) { resp ->

            if (resp != null) {

                if (resp is DocumentMetaData) {

                    documentViewModel.documentTypeList = resp.typeList

                    popupMenu.menu.clear()
                    documentViewModel.documentTypeList.forEach {
                        popupMenu.menu.add(it)
                    }
                    updateTypeFilter(binding.documentViewPager.currentItem)
                }
            }
        }

        documentViewModel.documentLive.observe(this) { resp ->

            if (resp != null) {

                if (resp is Pair<*, *>) {

                    val documentPair = resp as Pair<ArrayList<Document>, Boolean>
                    val documentList = documentPair.first

                    if (documentPair.second) {

                        if (documentList.isEmpty()) {
                            if (documentViewModel.documentTypeList.isEmpty()) {
                                binding.documentFilterBtn.visibility = View.GONE
                            } else {

                                if (documentViewModel.selectedDocumentTypeStr.isNotBlank()) {
                                    binding.documentFilterBtn.visibility = View.VISIBLE
                                } else {
                                    binding.documentFilterBtn.visibility = View.GONE
                                }
                            }
                        } else {
                            if (documentViewModel.documentTypeList.isEmpty()) {
                                binding.documentFilterBtn.visibility = View.GONE
                            } else {
                                binding.documentFilterBtn.visibility = View.VISIBLE
                            }
                        }
                    }

                }
            }
        }
        binding.documentViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                updateTypeFilter(position)
                if (position == 1 && documentViewModel.isSelectionMode) {
                    renderUnSelectUI(0)
                }
            }
        })
        popupMenu.setOnMenuItemClickListener { menuItem ->

            documentViewModel.selectedDocumentTypeStr =
                if (menuItem.title == ModelUtil.documentTypeAll) {
                    ""
                } else {
                    menuItem.title?.toString() ?: ""
                }

            documentViewPagerAdapter.refreshData(binding.documentViewPager.currentItem)
            return@setOnMenuItemClickListener true
        }

        binding.documentFilterBtn.setOnClickListener {
            popupMenu.show()
        }

        setOnClickListener()
        documentViewModel.getCategories()
    }

    fun setOnClickListener() {
        binding.documentDeleteBtn.setOnClickListener {
            if (documentViewModel.selectedDocumentList.size > 1) {
                UIUtil.showToast(
                    this,
                    getString(R.string.select_one_doc_delete_multi_not_allowed),
                    Toast.LENGTH_SHORT
                )
            } else {
                val practiceAddedDoc =
                    documentViewModel.selectedDocumentList.filter { !it.isAddedByPatient }
                if (practiceAddedDoc.isNotEmpty()) {
                    UIUtil.showToast(
                        this,
                        getString(R.string.document_shared_by_practice_caanot_be_deleted),
                        Toast.LENGTH_SHORT
                    )
                } else {
                    documentViewModel.selectedDocumentList.forEach {
                        deleteDocument(it)
                    }
                }
            }
        }
        binding.documentShareBtn.setOnClickListener {
            shareDocuments()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                documentViewPagerAdapter.refreshData(binding.documentViewPager.currentItem)
            }
        }
    }

    private fun shareDocuments() {
        val intent = Intent(this.applicationContext, ShareDocumentActivity::class.java)
        intent.putExtra(
            ModelUtil.documentShareString,
            MDJsonBuilder.formDocumentListJson(documentViewModel.selectedDocumentList).toString()
        )
        startActivity(intent)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun updateTypeFilter(position: Int) {
        if (position == 0) {
            if (documentViewModel.documentTypeList.isEmpty()) {
                binding.documentFilterBtn.visibility = View.GONE
            } else {
                binding.documentFilterBtn.visibility = View.VISIBLE
            }
            if (!RbacUtil.checkPrivilege(
                    RbacUtil.Companion.RBACPrivileges.ADD_DOCUMENTS,
                    applicationContext
                )
            ) {
                binding.documentAddBtn.hide()
            } else {
                binding.documentAddBtn.show()
            }
        } else {
            binding.documentFilterBtn.visibility = View.GONE
            if (!RbacUtil.checkPrivilege(
                    RbacUtil.Companion.RBACPrivileges.ADD_FOLDERS,
                    applicationContext
                )
            ) {
                binding.documentAddBtn.hide()
            } else {
                binding.documentAddBtn.show()
            }
        }
    }

    fun deleteDocument(document: Document) {
        val builder = AlertDialog.Builder(this)


        builder.setMessage(R.string.document_delete_confirmation_msg)

        builder.setPositiveButton(R.string.yes_str) { _, _ ->
            progressDialog.show()
            documentViewModel.deleteDocument(document).observe(this) { resp ->
                if (resp != null) {
                    progressDialog.dismiss()
                    if (resp is Boolean) {
                        renderUnSelectUI(binding.documentViewPager.currentItem)
                        documentViewPagerAdapter.deleteProblem(
                            document,
                            binding.documentViewPager.currentItem
                        )
                        UIUtil.showToast(
                            this,
                            R.string.document_list_delete_success_msg,
                            Toast.LENGTH_LONG
                        )
                    } else if (resp is APIResponseError) {
                        UIUtil.showToast(
                            this,
                            R.string.error_document_delete__msg,
                            Toast.LENGTH_LONG
                        )
                    }
                }
            }
        }
        builder.setNegativeButton(R.string.no_str) { _, _ ->

        }

        builder.show()
    }

    fun floatingActionButtonVisiblity(): Int = binding.documentAddBtn.visibility

    fun showFab() {
        binding.documentAddBtn.show()
    }

    fun hideFab() {
        binding.documentAddBtn.hide()
    }


    private fun intentToAddDocument() {

        val intent = Intent(this, AddDocumentActivity::class.java)
        intent.putExtra(ModelUtil.isHealthPassportIntent, documentViewModel.isHealthPassport)
        startActivityForResult(intent, 0)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onBackPressed() {
        if (documentViewModel.isSelectionMode) {
            renderUnSelectUI(binding.documentViewPager.currentItem)
        } else {
            if (documentViewModel.isDocumentReadCallbackToBeSent) {
                setResult(RESULT_OK)
            }
            super.onBackPressed()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    fun renderUnSelectUI(currentItem: Int) {
        documentViewModel.isSelectionMode = false
        binding.documentBackBtn.setImageResource(R.drawable.ic_arrow_back_white)
        binding.documentToolbarText.setText(R.string.document_str)
        binding.documentDeleteBtn.visibility = View.GONE
        binding.documentShareBtn.visibility = View.GONE
        binding.documentFilterBtn.visibility = View.VISIBLE
        documentViewPagerAdapter.removeSelectionMode(currentItem)
    }

}
