package com.charm.phr.mphr.ui.allergy

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAllergyBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PatientAllergy
import com.charm.phr.mphr.ui.allergy.addAllergy.AddAllergyActivity
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.OnMultipleSelectChangeListener
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

/**
 * [AllergyActivity] shows the list of allergies of the selected patient.
 * The view of allergy differs based on [AllergyViewModel.isHealthPassport]
 */

class AllergyActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var allergyViewModel: AllergyViewModel

    @Inject
    lateinit var activeAllergyListRecyclerAdapter: ActiveAllergyListRecyclerAdapter

    private lateinit var binding: ActivityAllergyBinding

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            fetchAllergy()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAllergyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        allergyViewModel = ViewModelProvider(this, viewModelFactory)[AllergyViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.allergyToolbar)
        UIUtil.applyViewInsertForBottomView(binding.allergyMainLayout)

        allergyViewModel.isHealthPassport = intent.getBooleanExtra(ModelUtil.isHealthPassportIntent, false)
        if (allergyViewModel.isHealthPassport) {
            binding.allergyToolbarText.setText(R.string.allergy_health_passport_str)
        } else {
            binding.allergyToolbarText.setText(R.string.allergy_str)
        }
        if (RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_ALLERGIES, applicationContext) && (ModelUtil.isPHREntryEnabled(
                authManager, ModelUtil.allergiesModelId
            ) || allergyViewModel.isHealthPassport)
        ) {
            binding.allergyAddFab.show()
            binding.allergyAddFab.setOnClickListener {
                val intent = Intent(this, AddAllergyActivity::class.java)
                intent.putExtra(ModelUtil.isHealthPassportIntent, allergyViewModel.isHealthPassport)
                resultLauncher.launch(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        } else {
            binding.allergyAddFab.hide()
        }

        allergyViewModel.fetchAllergyList(true)

        setUpRecyclerView()

        setOnClickListener()

        fetchAllergyObserver()

        setUpFilterPopMenu()

    }

    fun setUpRecyclerView() {
        binding.activeAllergyListRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.activeAllergyListRV.adapter = activeAllergyListRecyclerAdapter

        binding.activeAllergyListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        binding.activeAllergyListNoDataTV.nodataTextViewHint.visibility = View.VISIBLE
        binding.activeAllergyListNoDataTV.nodataTextViewHint.setText(R.string.allergy_add_hint)
        UIUtil.makePlusColorPrimary(binding.activeAllergyListNoDataTV.nodataTextViewHint)

        activeAllergyListRecyclerAdapter.setOnMultipleSelectChangeListener(object : OnMultipleSelectChangeListener {
            override fun onMultiSelect(isMultiSelectEnabled: Boolean) {
                onMultiSelectChange(isMultiSelectEnabled)
                binding.activeAllergyListSwipeRefreshLayout.isEnabled = !isMultiSelectEnabled
            }
        })

        activeAllergyListRecyclerAdapter.setOnEditClickListener(object : AllergyListListener {
            override fun onEditClick(patientAllergy: PatientAllergy) {

                val intent = Intent(this@AllergyActivity, AddAllergyActivity::class.java)
                intent.putExtra(ModelUtil.allergyToEditIntent, true)
                intent.putExtra(ModelUtil.allergyEditDataIntent, patientAllergy.toString)
                intent.putExtra(ModelUtil.allergyStatusIntent, patientAllergy.status)
                intent.putExtra(ModelUtil.practiceIdIntent, patientAllergy.practiceId)
                intent.putExtra(ModelUtil.isHealthPassportIntent, allergyViewModel.isHealthPassport)
                resultLauncher.launch(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onDeleteClick(patientAllergy: PatientAllergy, position: Int) {
                deleteAllergy(patientAllergy, position)
            }
        })

        binding.activeAllergyListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, scrollX: Int, scrollY: Int) {

                if (ModelUtil.isPHREntryEnabled(authManager, ModelUtil.allergiesModelId)) {
                    try {

                        if (scrollY > 0 && floatingActionButtonVisiblity() == View.VISIBLE) {
                            hideFab()
                        } else if (RbacUtil.checkPrivilege(
                                RbacUtil.Companion.RBACPrivileges.ADD_ALLERGIES, applicationContext
                            ) && scrollY < 0 && floatingActionButtonVisiblity() != View.VISIBLE
                        ) {
                            showFab()
                        }

                        /*    handler.removeCallbacks(runnable)
                            handler.postDelayed(runnable, 100)*/
                    } catch (e: Exception) {

                    }
                }
            }
        })

    }

    fun setOnClickListener() {
        binding.allergyBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.activeAllergyListSwipeRefreshLayout.setOnRefreshListener {

            binding.activeAllergyListSwipeRefreshLayout.isRefreshing = true
            allergyViewModel.fetchAllergyList(true)
        }


        binding.activeAllergyListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.activeAllergyListErrorPage.root.visibility = View.GONE
            fetchAllergy()
        }
    }

    fun fetchAllergyObserver() {
        allergyViewModel.allergyListLive.observe(this, Observer { resp ->

            if (resp != null) {

                if (resp is Pair<*, *>) {
                    val messagePairList = resp as Pair<ArrayList<PatientAllergy>, Boolean>

                    if (messagePairList.second) {

                        binding.activeAllergyListProgress.root.visibility = View.GONE

                        if (binding.activeAllergyListSwipeRefreshLayout.isRefreshing) {
                            binding.activeAllergyListSwipeRefreshLayout.isRefreshing = false
                        }
                        val size = activeAllergyListRecyclerAdapter.setAllergyList(messagePairList.first)
                        if (size == 0) {
                            binding.activeAllergyListNoDataTV.root.visibility = View.VISIBLE
                            binding.activeAllergyListRV.visibility = View.GONE
                        } else {
                            binding.activeAllergyListNoDataTV.root.visibility = View.GONE
                            binding.activeAllergyListRV.visibility = View.VISIBLE

                            checkToShowBgImage()

                        }
                    } else {

                        activeAllergyListRecyclerAdapter.removeInfiniteScroll()
                        activeAllergyListRecyclerAdapter.addAllergyList(messagePairList.first)
                    }
                } else if (resp is APIResponseError) {
                    if (!allergyViewModel.isActiveAllergyListFetched) {
                        binding.activeAllergyListNoDataTV.root.visibility = View.GONE
                        binding.activeAllergyListRV.visibility = View.GONE
                        binding.activeAllergyListErrorPage.root.visibility = View.VISIBLE
                    } else {
                        activeAllergyListRecyclerAdapter.removeInfiniteScroll()
                    }
                }

            }
        })
    }

    fun onMultiSelectChange(isEnabled: Boolean) {
        allergyViewModel.isMultiSelectEnabled = isEnabled

        if (isEnabled) {
            binding.allergyDeleteBtn.visibility = View.VISIBLE
        } else {
            binding.allergyDeleteBtn.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    /**
     * Method [deleteAllergy] is used to delete the selected allergy.
     * Allergy can be deleted if the selected allergy is added by patient.
     * */

    fun deleteAllergy(patientAllergy: PatientAllergy, position: Int) {
        val builder = AlertDialog.Builder(this)

        builder.setMessage(R.string.allergy_delete_confirmation_msg)
        builder.setPositiveButton(R.string.yes_str) { _, _ ->
            progressDialog.show()
            allergyViewModel.deletePatientAllergy(patientAllergy).observe(this, Observer { resp ->
                if (resp != null) {
                    progressDialog.dismiss()
                    if (resp is Boolean) {
                        deleteAllergy(position)
                        UIUtil.showToast(this, R.string.allergy_delete_success_msg, Toast.LENGTH_LONG)
                    } else if (resp is APIResponseError) {
                        UIUtil.showToast(this, R.string.error_allergy_delete__msg, Toast.LENGTH_LONG)
                    }
                }
            })
        }
        builder.setNegativeButton(R.string.no_str) { _, _ ->

        }

        builder.show()

    }

    fun deleteAllergy(position: Int) {
        try {
            val size = activeAllergyListRecyclerAdapter.removeAllergy(position)
            if (size == 0) {
                binding.activeAllergyListNoDataTV.root.visibility = View.VISIBLE
                binding.activeAllergyListRV.visibility = View.GONE
            } else {
                binding.activeAllergyListNoDataTV.root.visibility = View.GONE
                binding.activeAllergyListRV.visibility = View.VISIBLE
                checkToShowBgImage()
            }
        } catch (e: Exception) {
        }
    }

    fun floatingActionButtonVisiblity(): Int = binding.allergyAddFab.visibility

    fun showFab() {
        binding.allergyAddFab.show()
    }

    fun hideFab() {
        binding.allergyAddFab.hide()
    }

    interface FetchAllergyListener {
        fun fetchAllergy()
    }

    interface AllergyListListener {
        fun onEditClick(patientAllergy: PatientAllergy)
        fun onDeleteClick(patientAllergy: PatientAllergy, position: Int)
    }

    fun fetchAllergy() {
        try {
            when (allergyViewModel.selectedFilter) {
                ModelUtil.allergyStatusActive -> {
                    allergyViewModel.isActiveAllergyListFetched = false
                }

                ModelUtil.allergyStatusInActive -> {
                    allergyViewModel.isInActiveAllergyListFetched = false
                }

                ModelUtil.allergyStatusResolved -> {
                    allergyViewModel.resolvedAllergyListFetched = false
                }
            }

            binding.activeAllergyListProgress.root.visibility = View.VISIBLE
            binding.activeAllergyListNoDataTV.root.visibility = View.GONE
            binding.activeAllergyListRV.visibility = View.GONE
            allergyViewModel.fetchAllergyList(true)
        } catch (e: Exception) {

        }

    }

    private fun checkToShowBgImage() {

        if (!BuildUtil.isCustomApp) {
            val height = UIUtil.getScreenHeight(this)

            val tv = TypedValue()
            var actionBarHeight = 0
            if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            }

            val viewHeight = ((height - actionBarHeight) * 2) / 3

            val footerTreeObserver = binding.activeAllergyListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        binding.activeAllergyListRV.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                        val activeAllergyHeight = binding.activeAllergyListRV.height
                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $activeAllergyHeight")

                        if (activeAllergyHeight < viewHeight) {
                            binding.activeAllergyListHPBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.activeAllergyListHPBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            binding.activeAllergyListHPBgLayout.visibility = View.GONE
        }
    }

    private fun setUpFilterPopMenu() {
        val popupMenu = PopupMenu(this, binding.allergyFilterBtnAnchor, Gravity.START)
        popupMenu.menu.add(ModelUtil.allergyStatusActive)
        popupMenu.menu.add(ModelUtil.allergyStatusInActive)
        popupMenu.menu.add(ModelUtil.allergyStatusResolved)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            allergyViewModel.selectedFilter = menuItem.title?.toString() ?: ""
            onFilterChange()
            return@setOnMenuItemClickListener true
        }
        binding.allergyFilterBtn.setOnClickListener {
            popupMenu.show()
        }
    }

    fun onFilterChange() {
        when (allergyViewModel.selectedFilter) {
            ModelUtil.allergyStatusActive -> {
                if (!allergyViewModel.isActiveAllergyListFetched) {
                    fetchAllergy()
                } else {
                    reRenderAllergyList(allergyViewModel.activeAllergyList)
                }
                binding.allergyStatusTV.setText(R.string.active_allergy_txt)
                activeAllergyListRecyclerAdapter.allergyStatus= ModelUtil.allergyStatusActive
            }

            ModelUtil.allergyStatusInActive -> {
                if (!allergyViewModel.isInActiveAllergyListFetched) {
                    fetchAllergy()
                } else {
                    reRenderAllergyList(allergyViewModel.inActiveAllergyList)
                }
                binding.allergyStatusTV.setText(R.string.inactive_allergy_txt)
                activeAllergyListRecyclerAdapter.allergyStatus= ModelUtil.allergyStatusInActive
            }

            ModelUtil.allergyStatusResolved -> {
                if (!allergyViewModel.resolvedAllergyListFetched) {
                    fetchAllergy()
                } else {
                    reRenderAllergyList(allergyViewModel.resolvedAllergyList)
                }
                binding.allergyStatusTV.setText(R.string.resolved_allergy_txt)
                activeAllergyListRecyclerAdapter.allergyStatus= ModelUtil.allergyStatusResolved
            }
        }
    }

    fun reRenderAllergyList(activeAllergyList: ArrayList<PatientAllergy>) {
        val size = activeAllergyListRecyclerAdapter.setAllergyList(activeAllergyList)
        if (size == 0) {
            binding.activeAllergyListNoDataTV.root.visibility = View.VISIBLE
            binding.activeAllergyListRV.visibility = View.GONE
        } else {
            binding.activeAllergyListNoDataTV.root.visibility = View.GONE
            binding.activeAllergyListRV.visibility = View.VISIBLE

            checkToShowBgImage()

        }
    }

}
