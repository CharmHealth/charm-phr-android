package com.charm.phr.mphr.ui.document.document_list_with_folder


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityDocumentListForFolderBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.ui.document.DocumentListAdapter
import com.charm.phr.mphr.ui.document.add_document.AddDocumentActivity
import com.charm.phr.mphr.ui.document.preview_document.PreviewDocumentActivity
import com.charm.phr.mphr.ui.document.share_document.ShareDocumentActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.RefreshDataListener
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

class DocumentListForFolderActivity : MDBaseActivity(), RefreshDataListener {


    lateinit var documentListForFolderViewModel: DocumentListForFolderViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var documentListAdapter: DocumentListAdapter


    lateinit var binding: ActivityDocumentListForFolderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentListForFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        documentListForFolderViewModel = ViewModelProvider(this, viewModelFactory)[DocumentListForFolderViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.documentListForFolderToolbar)
        UIUtil.applyViewInsertForBottomView(binding.documentMainLayout)


        documentListForFolderViewModel.isSharedByMe = intent.getBooleanExtra(ModelUtil.documentSharedByMeIntent, false)
        documentListForFolderViewModel.isSharedToMe = intent.getBooleanExtra(ModelUtil.documentSharedToMeIntent, false)
        documentListForFolderViewModel.categoryId = intent.getStringExtra(ModelUtil.documentCategoryIdIntent) ?: ""
        documentListForFolderViewModel.categoryName = intent.getStringExtra(ModelUtil.documentCategoryNameIntent) ?: ""

        documentListForFolderViewModel.documentTypeList = intent.getStringArrayListExtra(ModelUtil.documentCategoryListIntent) ?: arrayListOf()
        documentListForFolderViewModel.isSharedByMe = intent.getBooleanExtra(ModelUtil.documentSharedByMeIntent, false)

        binding.documentListForFolderRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.documentListForFolderRV.adapter = documentListAdapter
        documentListAdapter.isSharedByMe = documentListForFolderViewModel.isSharedByMe

        val isCategory = setToolBarText()


        binding.documentListForFolderSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)


        binding.documentListForFolderNoDataTV.nodataTextViewHint.visibility = View.GONE
        binding.documentListForFolderNoDataTV.nodataTextViewHint.setText(R.string.document_add_hint)
        UIUtil.makePlusColorPrimary(binding.documentListForFolderNoDataTV.nodataTextViewHint)




        documentListAdapter.setDocumentSelectListener(object : DocumentListAdapter.DocumentSelectListener {


            override fun onDelete(document: Document, position: Int) {
                deleteDocument(document)
            }

            override fun onSelect(document: Document, position: Int) {

                if (documentListForFolderViewModel.isSelectionMode) {
                    documentListForFolderViewModel.selectedDocumentList.add(document)
                    binding.documentListForFolderToolbarText.setText(documentListForFolderViewModel.selectedDocumentList.size.toString())
                } else {
                    val intent = Intent(this@DocumentListForFolderActivity, PreviewDocumentActivity::class.java)
                    intent.putExtra(ModelUtil.documentFileIdIntent, document.fileId)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }

            override fun onUnSelect(document: Document, position: Int) {
                if (documentListForFolderViewModel.isSelectionMode) {
                    documentListForFolderViewModel.selectedDocumentList.remove(document)
                    if (documentListForFolderViewModel.selectedDocumentList.isEmpty()) {
                        removeSelectionUI()
                    } else {
                        binding.documentListForFolderToolbarText.setText(documentListForFolderViewModel.selectedDocumentList.size.toString())
                    }
                } else {
                    val intent = Intent(this@DocumentListForFolderActivity, PreviewDocumentActivity::class.java)
                    intent.putExtra(ModelUtil.documentFileIdIntent, document.fileId)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }

            override fun onSelection(document: Document, position: Int) {
                if (!documentListForFolderViewModel.isSelectionMode) {
                    documentListForFolderViewModel.isSelectionMode = true
                    documentListAdapter.isSelectionMode = true
                    documentListForFolderViewModel.selectedDocumentList.clear()
                    documentListForFolderViewModel.selectedDocumentList.add(document)
                    binding.documentListForFolderBackBtn.setImageResource(R.drawable.ic_close_white)
                    binding.documentListForFolderToolbarText.setText(documentListForFolderViewModel.selectedDocumentList.size.toString())
                    if (documentListForFolderViewModel.isSharedByMe || documentListForFolderViewModel.isSharedToMe) {
                        binding.documentListForFolderDeleteBtn.visibility = View.GONE
                    } else {
                        binding.documentListForFolderDeleteBtn.visibility = View.VISIBLE
                    }

                    binding.documentListForFolderShareBtn.visibility = View.VISIBLE
                    binding.documentListForFolderFilterBtn.visibility = View.GONE
                } else {
                    documentListForFolderViewModel.selectedDocumentList.add(document)
                    binding.documentListForFolderToolbarText.setText(documentListForFolderViewModel.selectedDocumentList.size.toString())
                }
            }

            override fun showToastMessage() {
                UIUtil.showToast(applicationContext, getString(R.string.select_max_document_error), Toast.LENGTH_LONG)
            }

        })

        binding.documentListForFolderRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, scrollX: Int, scrollY: Int) {
                val lastIndexInScreen = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()


                Log.d("messageIbListItemCount", documentListAdapter.itemCount.toString())
                Log.d(
                    "documentScroll",
                    "lastIndex -> $lastIndexInScreen; itemCount -> ${documentListAdapter.itemCount}; isDocumentFetchComplete -> ${documentListForFolderViewModel.isDocumentFetchComplete}; hasmore -> ${documentListForFolderViewModel.documentHasMore}"
                )

                if ((lastIndexInScreen + 1) >= documentListAdapter.itemCount - 5 && documentListAdapter.itemCount != 0 && documentListForFolderViewModel.isDocumentFetchComplete && documentListForFolderViewModel.documentHasMore) {
                    Log.d("scroll", "now")

                    documentListForFolderViewModel.isDocumentFetchComplete = false
                    documentListAdapter.addInfiniteScroll()
                    documentListForFolderViewModel.getDocumentList(false)

                }
                if (!RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_FOLDERS, applicationContext)) {
                    binding.documentListForFolderAddBtn.hide()
                }
                if (isCategory) {
                    if (scrollY > 0 && binding.documentListForFolderAddBtn.visibility == View.VISIBLE) {
                        binding.documentListForFolderAddBtn.hide()
                    } else if (scrollY < 0 && binding.documentListForFolderAddBtn.visibility != View.VISIBLE && RbacUtil.checkPrivilege(
                            RbacUtil.Companion.RBACPrivileges.ADD_FOLDERS,
                            applicationContext
                        )
                    ) {
                        binding.documentListForFolderAddBtn.show()
                    }
                }
            }
        })

        documentListForFolderViewModel.documentLive.observe(this, Observer { resp ->

            if (resp != null) {

                if (resp is Pair<*, *>) {

                    val documentPair = resp as Pair<ArrayList<Document?>, Boolean>
                    val patMedPairList = documentPair.first

                    if (documentPair.second) {//isFirst

                        updateDocumentType()

                        binding.documentListForFolderProgress.root.visibility = View.GONE

                        if (binding.documentListForFolderSwipeRefreshLayout.isRefreshing) {
                            binding.documentListForFolderSwipeRefreshLayout.isRefreshing = false
                        }

                        val linearLayoutManager = binding.documentListForFolderRV.layoutManager as LinearLayoutManager
                        linearLayoutManager.scrollToPositionWithOffset(0, 0)

                        val size = documentListAdapter.setDocuments(patMedPairList)
                        if (size == 0) {
                            binding.documentListForFolderNoDataTV.root.visibility = View.VISIBLE
                            binding.documentListForFolderRV.visibility = View.GONE

                            if (documentListForFolderViewModel.documentTypeList.isEmpty()) {
                                binding.documentListForFolderFilterBtn.visibility = View.GONE
                            } else {

                                if (documentListForFolderViewModel.selectedDocumentTypeStr.isNotBlank()) {
                                    binding.documentListForFolderFilterBtn.visibility = View.VISIBLE
                                } else {
                                    binding.documentListForFolderFilterBtn.visibility = View.GONE
                                }
                            }

                        } else {

                            if (documentListForFolderViewModel.documentTypeList.isEmpty()) {
                                binding.documentListForFolderFilterBtn.visibility = View.GONE
                            } else {
                                binding.documentListForFolderFilterBtn.visibility = View.VISIBLE
                            }

                            binding.documentListForFolderNoDataTV.root.visibility = View.GONE
                            binding.documentListForFolderRV.visibility = View.VISIBLE
                        }
                    } else {
                        documentListAdapter.removeInfiniteScroll()
                        documentListAdapter.addDocuments(patMedPairList)
                    }
                } else if (resp is APIResponseError) {
                    if (!documentListForFolderViewModel.isDocumentFetched) {
                        binding.documentListForFolderNoDataTV.root.visibility = View.GONE
                        binding.documentListForFolderRV.visibility = View.GONE
                        binding.documentListForFolderErrorPage.root.visibility = View.VISIBLE
                    }
                }

                documentListForFolderViewModel.isDocumentFetchComplete = true
            }
        })


        if (!documentListForFolderViewModel.isDocumentFetched) {
            documentListForFolderViewModel.getDocumentList(true)
        }

        if (isCategory && RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_FOLDERS, applicationContext)) {
            binding.documentListForFolderAddBtn.show()

            binding.documentListForFolderAddBtn.setOnClickListener {
                intentToAddDocument()
            }
        } else {
            binding.documentListForFolderAddBtn.hide()
        }


        binding.documentListForFolderSwipeRefreshLayout.setOnRefreshListener {
            binding.documentListForFolderSwipeRefreshLayout.isRefreshing = true
            documentListForFolderViewModel.getDocumentList(true)
            removeSelectionUI()
        }

        binding.documentListForFolderBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.documentListForFolderErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.documentListForFolderErrorPage.root.visibility = View.GONE
            refreshData()
        }



        if (documentListForFolderViewModel.documentTypeList.isNotEmpty()) {
            val popupMenu = PopupMenu(this, binding.documentListForFolderFilterBtnAnchor, Gravity.START)
            popupMenu.menu.clear()

            documentListForFolderViewModel.documentTypeList.forEach {
                popupMenu.menu.add(it)
            }

            popupMenu.setOnMenuItemClickListener { menuItem ->

                documentListForFolderViewModel.selectedDocumentTypeStr = if (menuItem.title == ModelUtil.documentTypeAll) {
                    ""
                } else {
                    menuItem.title?.toString() ?: ""
                }
                refreshData()
                return@setOnMenuItemClickListener true
            }


            binding.documentListForFolderFilterBtn.setOnClickListener {
                popupMenu.show()
            }

            binding.documentListForFolderFilterBtn.visibility = View.VISIBLE
        } else {
            binding.documentListForFolderFilterBtn.visibility = View.GONE
        }

        setOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        if (documentListForFolderViewModel.isSelectionMode) {
            removeSelectionUI()
        }
    }

    fun removeSelectionMode() {
        val documentList = documentListAdapter.documentList.clone() as ArrayList<Document?>
        documentListAdapter.setDocuments(documentList)
        documentListAdapter.isSelectionMode = false
    }

    fun setToolBarText(): Boolean {
        if (documentListForFolderViewModel.isSharedByMe) {
            binding.documentListForFolderToolbarText.setText(R.string.document_folder_shared_by_me_str)
            return false
        } else if (documentListForFolderViewModel.isSharedToMe) {
            binding.documentListForFolderToolbarText.setText(R.string.document_folder_shared_with_me_str)
            return false
        } else {
            binding.documentListForFolderToolbarText.text = documentListForFolderViewModel.categoryName
            return true
        }
    }

    fun setOnClickListener() {
        binding.documentListForFolderDeleteBtn.setOnClickListener {
            if (documentListForFolderViewModel.selectedDocumentList.size > 1) {
                UIUtil.showToast(this, getString(R.string.select_one_doc_delete_multi_not_allowed), Toast.LENGTH_SHORT)
            } else {
                val practiceAddedDoc = documentListForFolderViewModel.selectedDocumentList.filter { it?.isAddedByPatient == false }
                if (practiceAddedDoc.isNotEmpty()) {
                    UIUtil.showToast(this, getString(R.string.document_shared_by_practice_caanot_be_deleted), Toast.LENGTH_SHORT)
                } else {
                    documentListForFolderViewModel.selectedDocumentList.forEach {
                        deleteDocument(it)
                    }
                }
            }
        }
        binding.documentListForFolderShareBtn.setOnClickListener {
            shareDocuments()
        }
    }

    fun shareDocuments() {
        val intent = Intent(this.applicationContext, ShareDocumentActivity::class.java)
        intent.putExtra(ModelUtil.documentShareString, MDJsonBuilder.formDocumentListJson(documentListForFolderViewModel.selectedDocumentList).toString())
        startActivity(intent)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    fun deleteDocument(document: Document) {
        val builder = AlertDialog.Builder(this)


        builder.setMessage(R.string.document_delete_confirmation_msg)

        builder.setPositiveButton(R.string.yes_str) { _, _ ->
            progressDialog.show()
            documentListForFolderViewModel.deleteDocument(document).observe(this, Observer { resp ->
                if (resp != null) {
                    progressDialog.dismiss()
                    if (resp is Boolean) {
                        removeSelectionUI()
                        documentListAdapter.removeDocument(document)

                        UIUtil.showToast(this, R.string.document_list_delete_success_msg, Toast.LENGTH_LONG)
                    } else if (resp is APIResponseError) {
                        UIUtil.showToast(this, R.string.error_document_delete__msg, Toast.LENGTH_LONG)
                    }
                }
            })
        }
        builder.setNegativeButton(R.string.no_str) { _, _ ->

        }

        builder.show()
    }

    override fun refreshData() {
        documentListForFolderViewModel.isDocumentFetched = false
        documentListForFolderViewModel.isDocumentFetchComplete = false
        binding.documentListForFolderProgress.root.visibility = View.VISIBLE
        binding.documentListForFolderFilterBtn.visibility = View.GONE
        documentListForFolderViewModel.getDocumentList(true)
    }

    fun removeSelectionUI() {
        documentListForFolderViewModel.isSelectionMode = false
        binding.documentListForFolderBackBtn?.setImageResource(R.drawable.ic_arrow_back_white)
        setToolBarText()
        binding.documentListForFolderDeleteBtn?.visibility = View.GONE
        binding.documentListForFolderShareBtn?.visibility = View.GONE
        binding.documentListForFolderFilterBtn.visibility = View.VISIBLE
        removeSelectionMode()
    }

    override fun onBackPressed() {
        if (documentListForFolderViewModel.isSelectionMode) {
            removeSelectionUI()
        } else {
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

    }

    private fun updateDocumentType() {
        if (documentListForFolderViewModel.selectedDocumentTypeStr.isNotBlank()) {
            binding.documentListForFolderTypeTV.text = documentListForFolderViewModel.selectedDocumentTypeStr
        } else {
            binding.documentListForFolderTypeTV.setText(R.string.all_documents_str)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                refreshData()
            }
        }
    }

    private fun intentToAddDocument() {

        val intent = Intent(this, AddDocumentActivity::class.java)
        intent.putExtra(ModelUtil.isHealthPassportIntent, documentListForFolderViewModel.isHealthPassport)
        intent.putExtra(ModelUtil.documentCategoryIdIntent, documentListForFolderViewModel.categoryId)
        intent.putExtra(ModelUtil.documentCategoryNameIntent, documentListForFolderViewModel.categoryName)
        startActivityForResult(intent, 0)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }


}