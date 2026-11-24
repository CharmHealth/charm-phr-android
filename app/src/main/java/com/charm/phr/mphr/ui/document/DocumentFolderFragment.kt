package com.charm.phr.mphr.ui.document

import android.app.AlertDialog
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
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentDocumentFolderBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Category
import com.charm.phr.mphr.model.DocumentMetaData
import com.charm.phr.mphr.ui.components.ProgressDialog
import com.charm.phr.mphr.ui.document.document_list_with_folder.DocumentListForFolderActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.RefreshDataListener
import com.charm.phr.mphr.util.UIUtil
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class DocumentFolderFragment : DaggerFragment() , RefreshDataListener {

    lateinit var documentFolderViewModel: DocumentViewModel

    @Inject
    lateinit var documentFolderAdapter: DocumentFolderAdapter

    lateinit var progressDialog: ProgressDialog

    lateinit var imm: InputMethodManager

    val handler = Handler(Looper.getMainLooper())

    val binding: FragmentDocumentFolderBinding get() = _binding!!
    private var _binding: FragmentDocumentFolderBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment using view binding
        _binding = FragmentDocumentFolderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        documentFolderViewModel = ViewModelProvider(requireActivity()).get(DocumentViewModel::class.java)


        binding.documentFolderRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.documentFolderRV.adapter = documentFolderAdapter


        binding.documentFolderSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)


        binding.documentFolderNoDataTV.nodataTextViewHint.visibility = View.VISIBLE
        binding.documentFolderNoDataTV.nodataTextViewHint.setText(R.string.document_folder_add_hint)
        UIUtil.makePlusColorPrimary(binding.documentFolderNoDataTV.nodataTextViewHint)

        imm = this.activity?.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        progressDialog = (this.activity as MDBaseActivity).progressDialog


        documentFolderAdapter.setDocumentSelectListener(object : DocumentFolderAdapter.DocumentFolderSelectListener{
            override fun onDelete(document: Category, position: Int) {
                deleteFolder(document , position)
            }

            override fun onEdit(document: Category, position: Int) {
                showFolderDialog(document)
            }

            override fun onSelect(document: Category) {
                intentToDocumentListForFolderActivity(document)
            }
        })

        documentFolderViewModel.categoryLive.observe(viewLifecycleOwner , Observer {  resp ->

            if (resp != null) {


                if (resp is DocumentMetaData) {
                    val patMedPairList = resp.categoryList

                    binding.documentFolderProgress.root.visibility = View.GONE

                    if (binding.documentFolderSwipeRefreshLayout.isRefreshing){
                        binding.documentFolderSwipeRefreshLayout.isRefreshing = false
                    }

                    val linearLayoutManager = binding.documentFolderRV.layoutManager as LinearLayoutManager
                    linearLayoutManager.scrollToPositionWithOffset(0 ,0  )

                    val size = documentFolderAdapter.setCategories(patMedPairList)
                    if (size == 0) {
                        binding.documentFolderNoDataTV.root.visibility = View.VISIBLE
                        binding.documentFolderRVLayout.visibility = View.GONE
                    }
                    else {
                        binding.documentFolderNoDataTV.root.visibility = View.GONE
                        binding.documentFolderRVLayout.visibility = View.VISIBLE
                        checkToShowBgIcon()
                    }

                }
                else if (resp is APIResponseError) {
                    if (!documentFolderViewModel.isCategoryFetched) {
                        binding.documentFolderNoDataTV.root.visibility = View.GONE
                        binding.documentFolderRVLayout.visibility = View.GONE
                        binding.documentFolderErrorPage.root.visibility = View.VISIBLE
                    }
                }

                documentFolderViewModel.isCategoryFetchComplete = true
            }
        })

        binding.documentFolderRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, scrollX: Int, scrollY: Int) {

                try {

                    val dashboardActivity = activity as DocumentActivity
                    if (scrollY > 0 && dashboardActivity.floatingActionButtonVisiblity() == View.VISIBLE) {
                        dashboardActivity.hideFab()
                    }
                    else if ( RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_FOLDERS,requireContext()) && scrollY < 0 && dashboardActivity.floatingActionButtonVisiblity() != View.VISIBLE) {
                        dashboardActivity.showFab()
                    }

                    /*    handler.removeCallbacks(runnable)
                        handler.postDelayed(runnable, 100)*/
                } catch (e: Exception) {

                }
            }
        })

        if (!documentFolderViewModel.isCategoryFetched){
            refreshData()
        }


        binding.documentFolderSwipeRefreshLayout.setOnRefreshListener {
            binding.documentFolderSwipeRefreshLayout.isRefreshing = true
            documentFolderViewModel.getCategories()
        }


        binding.documentFolderErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.documentFolderErrorPage.root.visibility = View.GONE
            refreshData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showFolderDialog(category: Category? = null){

        val screenWidth = UIUtil.getScreenWidth(this.activity)
        val width = screenWidth - (2 * requireActivity().resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))


        val folderDialog = AddFolderDialog(requireActivity() , width )

        if (category != null) {
            folderDialog.setForEditCategory(category)
        }

        folderDialog.setFolderClickListener(object : AddFolderDialog.AddFolderClickListener{
            override fun onClick(category: Category) {

                val callResp = if (category.categoryId.isNullOrBlank()){
                    documentFolderViewModel.addCategory(category)
                }
                else{
                    documentFolderViewModel.editCategory(category)
                }

                progressDialog.show()
                callResp.observe(this@DocumentFolderFragment , Observer {resp ->

                    if (resp != null){
                        progressDialog.dismiss()
                        if (resp is Boolean){
                            refreshData()
                        }
                        else if (resp is APIResponseError){

                            if (category.categoryId.isNullOrBlank()){
                                UIUtil.showToast(this@DocumentFolderFragment.activity , R.string.error_add_folder_failed ,Toast.LENGTH_LONG)
                            }
                            else{
                                UIUtil.showToast(this@DocumentFolderFragment.activity , R.string.error_add_folder_failed ,Toast.LENGTH_LONG)
                            }
                            showFolderDialog(category)
                        }
                    }
                })
            }
        })

        folderDialog.show()

        folderDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)

    }

    override fun refreshData() {

        documentFolderViewModel.isCategoryFetchComplete = false
        documentFolderViewModel.isCategoryFetched = false
        binding.documentFolderProgress.root.visibility = View.VISIBLE
        documentFolderViewModel.getCategories()
    }


    fun deleteFolder(category: Category, position: Int){
        val builder = AlertDialog.Builder(this.activity)


        builder.setMessage(R.string.document_delete_folder_confirmation_msg)

        builder.setPositiveButton(R.string.yes_str ){_, _ ->
            progressDialog.show()
            documentFolderViewModel.deleteCategory(category).observe(viewLifecycleOwner , Observer { resp ->
                if (resp != null){
                    progressDialog.dismiss()
                    if (resp is Boolean){
                        documentFolderAdapter.removeCategory(position)
                        checkToShowBgIcon()
                        UIUtil.showToast(this.activity , R.string.document_folder_list_delete_success_msg , Toast.LENGTH_LONG)
                    }
                    else if (resp is APIResponseError){
                        UIUtil.showToast(this.activity , R.string.error_document_folder_delete__msg , Toast.LENGTH_LONG)
                    }
                }
            })
        }
        builder.setNegativeButton(R.string.no_str ){_ , _ ->

        }

        builder.show()
    }

    fun intentToDocumentListForFolderActivity(category: Category){

        val intent = Intent(this.context , DocumentListForFolderActivity::class.java)
        intent.putExtra(ModelUtil.documentCategoryIdIntent , category.categoryId)
        intent.putExtra(ModelUtil.documentCategoryNameIntent , category.categoryName)
        intent.putExtra(ModelUtil.documentSharedByMeIntent , category.isSharedByMe)
        intent.putExtra(ModelUtil.documentSharedToMeIntent , category.isSharedWithMe)
        intent.putExtra(ModelUtil.documentCategoryListIntent , documentFolderViewModel.documentTypeList)

        startActivity(intent)
        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun checkToShowBgIcon(){
        val height = UIUtil.getScreenHeight(this.activity)

        val tv = TypedValue()
        var actionBarHeight = 0
        if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }

        val viewHeight = ((height - actionBarHeight)*2) /3

        val footerTreeObserver = binding.documentFolderRV.viewTreeObserver

        footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                try {

                    binding.documentFolderRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                    val documentHeight = binding.documentFolderRV.height ?: 0

                    Log.d("MDHPBgHeights" , "viewHeight -> $viewHeight ; rvHeight -> $documentHeight")

                    if (documentHeight < viewHeight){
                        binding.documentFolderBgLayout.visibility = View.VISIBLE
                    }
                    else{
                        binding.documentFolderBgLayout.visibility = View.GONE
                    }

                }
                catch (e: Exception){

                }
            }
        })
    }

}