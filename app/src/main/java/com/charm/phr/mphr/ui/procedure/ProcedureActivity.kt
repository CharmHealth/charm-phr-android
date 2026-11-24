package com.charm.phr.mphr.ui.procedure

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityProcedureBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ProceduresMap
import com.charm.phr.mphr.ui.procedure.add_procedure.AddProcedureActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.OnMultipleSelectChangeListener
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

class ProcedureActivity : MDBaseActivity() {
    
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    
    lateinit var procedureViewModel: ProcedureViewModel

    @Inject
    lateinit var procedureListAdapter: ProcedureListAdapter

    private val respFromAddOrEdit = 0

    private lateinit var binding: ActivityProcedureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcedureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        procedureViewModel = ViewModelProvider(this, viewModelFactory).get(ProcedureViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.procedureToolbar)
        UIUtil.applyViewInsertForBottomView(binding.procedureMainLayout)

        procedureViewModel.isHealthPassport = intent.getBooleanExtra(ModelUtil.isHealthPassportIntent , false)

        binding.procedureListRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.procedureListRV.adapter = procedureListAdapter

        binding.procedureListNoDataTV.nodataTextViewHint.visibility = View.VISIBLE

        if (procedureViewModel.isHealthPassport){
            binding.procedureToolbarText.setText(R.string.procedure_health_passport_str)
            binding.procedureListNoDataTV.nodataTextViewHint.setText(R.string.procedure_add_health_passport__hint)
        } else {
            binding.procedureToolbarText.setText(R.string.procedure_str)
            binding.procedureListNoDataTV.nodataTextViewHint.setText(R.string.procedure_add_hint)
        }
        UIUtil.makePlusColorPrimary(binding.procedureListNoDataTV.nodataTextViewHint)

        procedureListAdapter.setOnMultipleSelectChangeListener(object : OnMultipleSelectChangeListener {
            override fun onMultiSelect(isMultiSelectEnabled: Boolean) {
                onMultiSelectChange(isMultiSelectEnabled)
            }
        })

        procedureListAdapter.setProcedureSelectListener(object : ProcedureListAdapter.ProcedureSelectListener {
            override fun onEditProcedure(proceduresMap: ProceduresMap) {
                val intent = Intent(this@ProcedureActivity , AddProcedureActivity::class.java)
                intent.putExtra(ModelUtil.procedureToEditIntent , true)
                intent.putExtra(ModelUtil.procedureEditDataIntent , proceduresMap.toString)
                intent.putExtra(ModelUtil.isHealthPassportIntent , procedureViewModel.isHealthPassport)
                startActivityForResult(intent , respFromAddOrEdit)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onDeleteProcedure(proceduresMap: ProceduresMap , position: Int) {
                deleteProcedure(proceduresMap , position)
            }
        })

        procedureViewModel.procedureListLive.observe(this, Observer { resp ->
            if (resp != null) {
                if (resp is Pair<*, *>) {
                    val procedurePairList = resp as Pair<ArrayList<ProceduresMap>, Boolean>
                    if (procedurePairList.second) {
                        binding.procedureListProgress.root.visibility = View.GONE
                        if (binding.procedureListSwipeRefreshLayout.isRefreshing) {
                            binding.procedureListSwipeRefreshLayout.isRefreshing = false
                        }
                        val size = procedureListAdapter.setProcedures(procedurePairList.first)
                        if (size == 0) {
                            binding.procedureListNoDataTV.root.visibility = View.VISIBLE
                            binding.procedureListRV.visibility = View.GONE
                        } else {
                            binding.procedureListNoDataTV.root.visibility = View.GONE
                            binding.procedureListRV.visibility = View.VISIBLE
                            checkToShowBgIcon()
                        }
                    } else {
                        procedureListAdapter.removeInfiniteScroll()
                        procedureListAdapter.addProcedures(procedurePairList.first)
                    }
                } else if (resp is APIResponseError) {
                    if (!procedureViewModel.isProcedureListFetched) {
                        binding.procedureListNoDataTV.root.visibility = View.GONE
                        binding.procedureListRV.visibility = View.GONE
                        binding.procedureListErrorPage.root.visibility = View.VISIBLE
                        binding.procedureListProgress.root.visibility = View.GONE
                    }
                }
                procedureViewModel.isProcedureFetchComplete = true
            }
        })

        if (!procedureViewModel.isProcedureListFetched) {
            procedureViewModel.fetchProcedureList( true)
        }

        binding.procedureListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, scrollX: Int, scrollY: Int) {
                val lastIndexInScreen = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                Log.d("procedureListItemCount", procedureListAdapter.itemCount.toString())
                Log.d("procedureIbListComplete", procedureViewModel.isProcedureFetchComplete.toString())
                if ((lastIndexInScreen + 1) >= procedureListAdapter.itemCount - 5 && procedureListAdapter.itemCount != 0 && procedureViewModel.isProcedureFetchComplete && procedureViewModel.procedureHasMore) {
                    Log.d("scroll", "now")
                    procedureViewModel.isProcedureFetchComplete = false
                    procedureListAdapter.addInfiniteScroll()
                    procedureViewModel.fetchProcedureList(false)
                }
                if (ModelUtil.isPHREntryEnabled(authManager , ModelUtil.procedureModelId) && RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_PROCEDURE,applicationContext)) {
                    if (scrollY > 0 && binding.procedureListAddFab.visibility == View.VISIBLE) {
                        binding.procedureListAddFab.hide()
                    } else if (scrollY < 0 && binding.procedureListAddFab.visibility != View.VISIBLE) {
                        binding.procedureListAddFab.show()
                    }
                }
            }
        })

        binding.procedureListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        binding.procedureListSwipeRefreshLayout.setOnRefreshListener {
            binding.procedureListSwipeRefreshLayout.isRefreshing = true
            procedureViewModel.fetchProcedureList( true)
        }

        binding.procedureBackBtn.setOnClickListener {
            onBackPressed()
        }

        if (ModelUtil.isPHREntryEnabled(authManager , ModelUtil.procedureModelId) && RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_PROCEDURE,this.applicationContext)) {
            binding.procedureListAddFab.show()
            binding.procedureListAddFab.setOnClickListener {
                val intent = Intent(this, AddProcedureActivity::class.java)
                intent.putExtra(ModelUtil.isHealthPassportIntent, procedureViewModel.isHealthPassport)
                startActivityForResult(intent, respFromAddOrEdit)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        } else {
            binding.procedureListAddFab.hide()
        }

        binding.procedureListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.procedureListErrorPage.root.visibility = View.GONE
            fetchProcedure()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == respFromAddOrEdit){
            if (resultCode == Activity.RESULT_OK){
                fetchProcedure()
            }
        }
    }

    fun onMultiSelectChange(isEnabled:Boolean){
        if (isEnabled){
            binding.procedureDeleteBtn.visibility = View.VISIBLE
        } else {
            binding.procedureDeleteBtn.visibility = View.GONE
        }
    }

    private fun fetchProcedure() {
        procedureViewModel.isProcedureFetchComplete = false
        procedureViewModel.isProcedureListFetched = false
        binding.procedureListProgress.root.visibility = View.VISIBLE
        binding.procedureListNoDataTV.root.visibility = View.GONE
        binding.procedureListRV.visibility = View.GONE
        procedureViewModel.fetchProcedureList(true)
    }

    fun deleteProcedure(proceduresMap: ProceduresMap, position: Int){
        val builder = AlertDialog.Builder(this)
        if (procedureViewModel.isHealthPassport){
            builder.setMessage(R.string.procedure_health_passport_delete_confirmation_msg)
        } else {
            builder.setMessage(R.string.procedure_delete_confirmation_msg)
        }
        builder.setPositiveButton(R.string.yes_str ){_, _ ->
            progressDialog.show()
            procedureViewModel.deleteProcedure(proceduresMap).observe(this , Observer {resp ->
                if (resp != null){
                    progressDialog.dismiss()
                    if (resp is Boolean){
                        val size =  procedureListAdapter.removeProcedure(position)
                        if (size == 0) {
                            binding.procedureListNoDataTV.root.visibility = View.VISIBLE
                            binding.procedureListRV.visibility = View.GONE
                        } else {
                            binding.procedureListNoDataTV.root.visibility = View.GONE
                            binding.procedureListRV.visibility = View.VISIBLE
                            checkToShowBgIcon()
                        }
                        if (procedureViewModel.isHealthPassport){
                            UIUtil.showToast(this, R.string.procedure_health_passport_delete_success_msg, Toast.LENGTH_LONG)
                        } else {
                            UIUtil.showToast(this, R.string.procedure_delete_success_msg, Toast.LENGTH_LONG)
                        }
                    } else if (resp is APIResponseError) {
                        if (procedureViewModel.isHealthPassport){
                            UIUtil.showToast(this, R.string.error_health_passport_procedure_delete__msg, Toast.LENGTH_LONG)
                        } else {
                            UIUtil.showToast(this, R.string.error_procedure_delete__msg, Toast.LENGTH_LONG)
                        }
                    }
                }
            })
        }
        builder.setNegativeButton(R.string.no_str) { _, _ -> }
        builder.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun checkToShowBgIcon(){
        if (!BuildUtil.isCustomApp) {
            val height = UIUtil.getScreenHeight(this)
            val tv = TypedValue()
            var actionBarHeight = 0
            if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            }
            val viewHeight = ((height - actionBarHeight) * 2) / 3
            val footerTreeObserver = binding.procedureListRV.viewTreeObserver
            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    try {
                        binding.procedureListRV.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                        val procedureHeight = binding.procedureListRV.height
                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $procedureHeight")
                        if (procedureHeight < viewHeight) {
                            binding.procedureListHPBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.procedureListHPBgLayout.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                    }
                }
            })
        } else {
            binding.procedureListHPBgLayout.visibility = View.GONE
        }
    }
}
