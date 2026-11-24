package com.charm.phr.mphr.ui.vaccine

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
import com.charm.phr.mphr.databinding.ActivityVaccineBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.VaccineImmunizationPOJO
import com.charm.phr.mphr.ui.vaccine.add_vaccine.AddVaccineActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

class VaccineActivity : MDBaseActivity() {


    @Inject
    lateinit var vaccineListAdapter: VaccineListAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var vaccineViewModel: VaccineViewModel

    private val respFromAddOrEdit = 1

    private lateinit var binding: ActivityVaccineBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVaccineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        vaccineViewModel = ViewModelProvider(this, viewModelFactory).get(VaccineViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.vaccineToolbar)
        UIUtil.applyViewInsertForBottomView(binding.vaccineListMainLayout)

        binding.vaccineListRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.vaccineListRV.adapter = vaccineListAdapter


        binding.vaccineListNoDataTV.nodataTextViewHint.visibility = View.VISIBLE
        binding.vaccineListNoDataTV.nodataTextViewHint.setText(R.string.vaccine_add_hint)
        UIUtil.makePlusColorPrimary(binding.vaccineListNoDataTV.nodataTextViewHint)



        binding.vaccineListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        vaccineViewModel.vaccineListLive.observe(this, Observer { resp ->

            if (resp != null) {


                if (resp is Pair<*, *>) {
                    val vaccinePairList = resp as Pair<ArrayList<VaccineImmunizationPOJO>, Boolean>

                    if (vaccinePairList.second) {

                        if (binding.vaccineListSwipeRefreshLayout.isRefreshing) {
                            binding.vaccineListSwipeRefreshLayout.isRefreshing = false
                        }

                        binding.vaccineListProgress.root.visibility = View.GONE
                        val size = vaccineListAdapter.setVaccineList(vaccinePairList.first)
                        if (size == 0) {
                            binding.vaccineListNoDataTV.root.visibility = View.VISIBLE
                            binding.vaccineListRV.visibility = View.GONE
                        } else {
                            binding.vaccineListNoDataTV.root.visibility = View.GONE
                            binding.vaccineListRV.visibility = View.VISIBLE
                            checkToShowBgImage()
                        }
                    } else {
                        vaccineListAdapter.removeInfiniteScroll()
                        vaccineListAdapter.addVaccineList(vaccinePairList.first)
                    }
                } else if (resp is APIResponseError) {
                    if (!vaccineViewModel.isVaccineListFetched) {
                        binding.vaccineListNoDataTV.root.visibility = View.GONE
                        binding.vaccineListRV.visibility = View.GONE
                        binding.vaccineListErrorPage.root.visibility = View.VISIBLE

                    }
                }

                vaccineViewModel.isVaccineFetchComplete = true
            }
        })


        if (!vaccineViewModel.isVaccineListFetched) {
            vaccineViewModel.fetchVaccineList(true)
        }

        binding.vaccineListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastIndexInScreen = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                if (lastIndexInScreen >= vaccineListAdapter.itemCount - 5 && vaccineListAdapter.itemCount != 0 && vaccineViewModel.isVaccineFetchComplete && vaccineViewModel.vaccineHasMore) {
                    vaccineViewModel.isVaccineFetchComplete = false
                    vaccineViewModel.fetchVaccineList(false)
                    vaccineListAdapter.addInfiniteScroll()

                }
            }
        })

        vaccineListAdapter.setVaccineSelectListener(object : VaccineListAdapter.VaccineSelectListener {
            override fun onVaccineSelect(vaccine: VaccineImmunizationPOJO) {

                val intent = Intent(this@VaccineActivity, AddVaccineActivity::class.java)
                intent.putExtra(ModelUtil.vaccineToEditIntent, true)
                intent.putExtra(ModelUtil.vaccineEditDataIntent, vaccine.toString)
                intent.putExtra(ModelUtil.practiceIdIntent, vaccine.practiceId)
                startActivityForResult(intent, respFromAddOrEdit)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onVaccineDelete(vaccine: VaccineImmunizationPOJO, position: Int) {
                deleteVaccine(vaccine, position)
            }
        })

        binding.vaccineListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        binding.vaccineListSwipeRefreshLayout.setOnRefreshListener {
            binding.vaccineListSwipeRefreshLayout.isRefreshing = true
            vaccineViewModel.fetchVaccineList(true)
        }


        if (ModelUtil.isPHREntryEnabled(authManager, ModelUtil.vaccineModelId) && RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_IMMUNE,this.applicationContext)) {
            binding.vaccineListAddFab.show()
            binding.vaccineListAddFab.setOnClickListener {
                val intent = Intent(this@VaccineActivity, AddVaccineActivity::class.java)
                startActivityForResult(intent, respFromAddOrEdit)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        } else {
            binding.vaccineListAddFab.hide()
        }

        binding.vaccineBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.vaccineListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.vaccineListErrorPage.root.visibility = View.GONE
            binding.vaccineListProgress.root.visibility = View.VISIBLE
            binding.vaccineListNoDataTV.root.visibility = View.GONE
            vaccineViewModel.isVaccineFetchComplete = false
            vaccineViewModel.isVaccineListFetched = false
            vaccineViewModel.fetchVaccineList(true)

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == respFromAddOrEdit) {
            if (resultCode == Activity.RESULT_OK) {
                binding.vaccineListErrorPage.root.visibility = View.GONE
                binding.vaccineListProgress.root.visibility = View.VISIBLE
                binding.vaccineListNoDataTV.root.visibility = View.GONE
                vaccineViewModel.isVaccineFetchComplete = false
                vaccineViewModel.isVaccineListFetched = false
                vaccineViewModel.fetchVaccineList(true)
            }
        }
    }

    fun deleteVaccine(vaccineImmunizationPOJO: VaccineImmunizationPOJO, position: Int) {
        val builder = AlertDialog.Builder(this)

        builder.setMessage(R.string.vaccine_delete_confirmation_msg)

        builder.setPositiveButton(R.string.yes_str) { _, _ ->
            progressDialog.show()
            vaccineViewModel.deleteVaccine(vaccineImmunizationPOJO).observe(this, Observer { resp ->
                if (resp != null) {
                    progressDialog.dismiss()
                    if (resp is Boolean) {
                        val size = vaccineListAdapter.removeVaccine(position)
                        if (size == 0) {
                            binding.vaccineListNoDataTV.root.visibility = View.VISIBLE
                            binding.vaccineListRV.visibility = View.GONE
                        } else {
                            binding.vaccineListNoDataTV.root.visibility = View.GONE
                            binding.vaccineListRV.visibility = View.VISIBLE
                            checkToShowBgImage()
                        }


                        UIUtil.showToast(this, R.string.vaccine_delete_success_msg, Toast.LENGTH_LONG)

                    } else if (resp is APIResponseError) {

                        UIUtil.showToast(this, R.string.error_vaccine_delete__msg, Toast.LENGTH_LONG)

                    }
                }
            })
        }
        builder.setNegativeButton(R.string.no_str) { _, _ ->

        }

        builder.show()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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

            val footerTreeObserver = binding.vaccineListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        binding.vaccineListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val supplementHeight = binding.vaccineListRV.height ?: 0

                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $supplementHeight")

                        if (supplementHeight < viewHeight) {
                            binding.vaccineListBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.vaccineListBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            binding.vaccineListBgLayout.visibility = View.GONE
        }

    }

}
