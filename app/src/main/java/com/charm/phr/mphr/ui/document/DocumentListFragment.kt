package com.charm.phr.mphr.ui.document

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
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityDocumentBinding
import com.charm.phr.mphr.databinding.FragmentDocumentListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.ui.components.ProgressDialog
import com.charm.phr.mphr.ui.document.preview_document.PreviewDocumentActivity
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.RefreshDataListener
import com.charm.phr.mphr.util.UIUtil
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class DocumentListFragment : DaggerFragment()  , RefreshDataListener {



    lateinit var documentViewModel: DocumentViewModel

    @Inject
    lateinit var documentAdapter: DocumentListAdapter

    @Inject
    lateinit var authManager: AuthManager
    lateinit var progressDialog: ProgressDialog

    val handler = Handler(Looper.getMainLooper())

    private var _binding: FragmentDocumentListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDocumentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        documentViewModel = ViewModelProvider(requireActivity()).get(DocumentViewModel::class.java)

        binding.documentRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.documentRV.adapter = documentAdapter


        binding.documentSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)


        binding.documentNoDataTV.nodataTextViewHint.visibility = View.VISIBLE
        binding.documentNoDataTV.nodataTextViewHint.setText(R.string.document_add_hint)
        UIUtil.makePlusColorPrimary(binding.documentNoDataTV.nodataTextViewHint)



        progressDialog = (this.activity as MDBaseActivity).progressDialog

        documentAdapter.setDocumentSelectListener(object : DocumentListAdapter.DocumentSelectListener{

            val documentActivity = activity as DocumentActivity
            val activityBinding = ActivityDocumentBinding.bind(documentActivity.findViewById(R.id.documentMainLayout))

            override fun onDelete(document: Document, position: Int) {

                if (documentActivity is DocumentActivity){
                    documentActivity.deleteDocument(document)
                }
            }

            override fun onSelect(document: Document,position: Int) {
                if(documentViewModel.isSelectionMode)
                {
                    documentViewModel.selectedDocumentList.add(document)
                    activityBinding?.documentToolbarText?.setText(documentViewModel.selectedDocumentList.size.toString())
                }
                else
                {
                    if (!document.readStatus) {
                        documentViewModel.isDocumentReadCallbackToBeSent = true
                    }
                    val intent = Intent(this@DocumentListFragment.context ,PreviewDocumentActivity::class.java )
                    intent.putExtra(ModelUtil.documentFileIdIntent , document.fileId)
                    startActivity(intent)
                    activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }

            override fun onUnSelect(document: Document,position: Int) {

                if(documentViewModel.isSelectionMode)
                {
                    documentViewModel.selectedDocumentList.remove(document)
                    if(documentViewModel.selectedDocumentList.size ==0)
                    {
                        renderUnSelectUI()
                    }
                    else
                    {
                        activityBinding?.documentToolbarText?.setText(documentViewModel.selectedDocumentList.size.toString())
                    }
                }
                else
                {
                    if (!document.readStatus) {
                        documentViewModel.isDocumentReadCallbackToBeSent = true
                    }
                    val intent = Intent(this@DocumentListFragment.context ,PreviewDocumentActivity::class.java )
                    intent.putExtra(ModelUtil.documentFileIdIntent , document.fileId)
                    startActivity(intent)
                    activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }

            override fun onSelection(document: Document, position: Int) {
                if(!documentViewModel.isSelectionMode)
                {
                    documentViewModel.isSelectionMode=true
                    documentAdapter.isSelectionMode=true
                    documentViewModel.selectedDocumentList.clear()
                    documentViewModel.selectedDocumentList.add(document)
                    activityBinding?.documentBackBtn?.setImageResource(R.drawable.ic_close_white)
                    activityBinding?.documentToolbarText?.setText(documentViewModel.selectedDocumentList.size.toString())
                    activityBinding?.documentDeleteBtn?.visibility=View.VISIBLE
                    activityBinding?.documentShareBtn?.visibility=View.VISIBLE
                    activityBinding?.documentFilterBtn?.visibility=View.GONE
                }
                else
                {
                    documentViewModel.selectedDocumentList.add(document)
                    activityBinding?.documentToolbarText?.setText(documentViewModel.selectedDocumentList.size.toString())
                }
            }

            override fun showToastMessage() {
                UIUtil.showToast(requireContext(), getString(R.string.select_max_document_error), Toast.LENGTH_LONG)
            }

        })

        documentViewModel.documentLive.observe(viewLifecycleOwner , Observer {  resp ->

            if (resp != null) {

                if (resp is Pair<*,*>) {

                    updateStatus()
                    val documentPair = resp as Pair<ArrayList<Document?> ,  Boolean>

                    val patMedPairList = documentPair.first

                    if (documentPair.second) {//isFirst

                        binding.documentProgress.root.visibility = View.GONE

                        if (binding.documentSwipeRefreshLayout.isRefreshing) {
                            binding.documentSwipeRefreshLayout.isRefreshing = false
                        }

                        val linearLayoutManager = binding.documentRV.layoutManager as LinearLayoutManager
                        linearLayoutManager.scrollToPositionWithOffset(0, 0)

                        val size = documentAdapter.setDocuments(patMedPairList)
                        if (size == 0) {
                            binding.documentNoDataTV.root.visibility = View.VISIBLE
                            binding.documentRVLayout.visibility = View.GONE
                        }
                        else {
                            binding.documentNoDataTV.root.visibility = View.GONE
                            binding.documentRVLayout.visibility = View.VISIBLE
                            checkToShowBgIcon()
                        }
                    }
                    else{
                        documentAdapter.removeInfiniteScroll()
                        documentAdapter.addDocuments(patMedPairList)
                        checkToShowBgIcon()
                    }
                }
                else if (resp is APIResponseError) {
                    if (!documentViewModel.isDocumentFetched) {
                        binding.documentNoDataTV.root.visibility = View.GONE
                        binding.documentRVLayout.visibility = View.GONE
                        binding.documentErrorPage.root.visibility = View.VISIBLE
                    }
                }
                documentViewModel.isDocumentFetchComplete = true
            }
        })


        if (!documentViewModel.isDocumentFetched){
            refreshData()
        }

        binding.documentSwipeRefreshLayout.setOnRefreshListener {
            binding.documentSwipeRefreshLayout.isRefreshing = true
            documentViewModel.getDocumentList(true)
            renderUnSelectUI()
        }

        binding.documentRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, scrollX: Int, scrollY: Int) {
                val lastIndexInScreen = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()


                Log.d("messageIbListItemCount" , documentAdapter.itemCount.toString())
                Log.d("documentScroll" , "lastIndex -> $lastIndexInScreen; itemCount -> ${documentAdapter.itemCount}; isDocumentFetchComplete -> ${ documentViewModel.isDocumentFetchComplete}; hasmore -> ${documentViewModel.documentHasMore}")

                if ((lastIndexInScreen + 1) >=  documentAdapter.itemCount - 5 &&  documentAdapter.itemCount != 0 && documentViewModel.isDocumentFetchComplete && documentViewModel.documentHasMore) {
                    Log.d("scroll", "now")

                    documentViewModel.isDocumentFetchComplete = false
                    documentAdapter.addInfiniteScroll()
                    documentViewModel.getDocumentList(false)

                }

                try {

                    val dashboardActivity = activity as DocumentActivity
                    if (scrollY > 0 && dashboardActivity.floatingActionButtonVisiblity() == View.VISIBLE) {
                        dashboardActivity.hideFab()
                    }
                    else if (RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_DOCUMENTS,requireContext()) && scrollY < 0 && dashboardActivity.floatingActionButtonVisiblity() != View.VISIBLE) {
                        dashboardActivity.showFab()
                    }

                    /*    handler.removeCallbacks(runnable)
                        handler.postDelayed(runnable, 100)*/
                } catch (e: Exception) {

                }
            }
        })



        binding.documentErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.documentErrorPage.root.visibility = View.GONE
            refreshData()
        }
    }

    override fun onResume() {
        super.onResume()
        if(documentViewModel.isSelectionMode)
        {
            renderUnSelectUI()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun deleteDocument(document: Document){
        val size = documentAdapter.removeDocument(document)
        if (size == 0) {
            binding.documentNoDataTV.root.visibility = View.VISIBLE
            binding.documentRVLayout.visibility = View.GONE
        }
        else {
            binding.documentNoDataTV.root.visibility = View.GONE
            binding.documentRVLayout.visibility = View.VISIBLE
            checkToShowBgIcon()
        }
        documentAdapter.isSelectionMode=false
    }

    fun removeSelectionMode()
    {
        val documentList=documentAdapter.documentList.clone() as ArrayList<Document?>
        documentAdapter.setDocuments(documentList)
        documentAdapter.isSelectionMode=false
    }

   fun renderUnSelectUI()
   {
       val activity = activity as DocumentActivity
       val activityBinding = ActivityDocumentBinding.bind(activity.findViewById(R.id.documentMainLayout))
       documentViewModel.isSelectionMode=false
       activityBinding?.documentBackBtn?.setImageResource(R.drawable.ic_arrow_back_white)
       activityBinding?.documentToolbarText?.setText(R.string.document_str)
       activityBinding?.documentDeleteBtn?.visibility=View.GONE
       activityBinding?.documentShareBtn?.visibility=View.GONE
       activityBinding?.documentFilterBtn?.visibility=View.VISIBLE
       removeSelectionMode()
   }

    override fun refreshData() {
        documentViewModel.isDocumentFetched = false
        documentViewModel.isDocumentFetchComplete = false
        binding.documentProgress.root.visibility = View.VISIBLE
        documentViewModel.getDocumentList(true)
    }

    private fun checkToShowBgIcon(){
        val height = UIUtil.getScreenHeight(this.activity)

        val tv = TypedValue()
        var actionBarHeight = 0
        if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }

        val viewHeight = ((height - actionBarHeight)*2) /3

        val footerTreeObserver = binding.documentRV.viewTreeObserver

        footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                try {

                    binding.documentRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                    val documentHeight = binding.documentRV.height ?: 0

                    Log.d("MDHPBgHeights" , "viewHeight -> $viewHeight ; rvHeight -> $documentHeight")

                    if (documentHeight < viewHeight && !BuildUtil.isCustomApp){
                        binding.documentHPBgLayout.visibility = View.VISIBLE
                    }
                    else{
                        binding.documentHPBgLayout.visibility = View.GONE
                    }

                }
                catch (e: Exception){

                }
            }
        })
    }

    private fun updateStatus(){
        if (documentViewModel.selectedDocumentTypeStr.isBlank()){
            binding.documentTypeTV.setText(R.string.all_documents_str)
        }
        else{
            binding.documentTypeTV.text = documentViewModel.selectedDocumentTypeStr
        }
    }
}
