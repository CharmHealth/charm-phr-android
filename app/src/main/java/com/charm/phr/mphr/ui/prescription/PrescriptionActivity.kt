package com.charm.phr.mphr.ui.prescription

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityPrescriptionBinding
import com.charm.phr.mphr.ui.prescription.add_medication.AddMedicationActivity
import com.charm.phr.mphr.ui.prescription.add_supplement.AddSupplementActivity
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class PrescriptionActivity : MDBaseActivity() {

    @Inject
    lateinit var prescriptionViewPagerAdapter: PrescriptionViewPagerAdapter

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    lateinit var binding: ActivityPrescriptionBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var prescriptionViewModel: PrescriptionViewModel

    private var prescriptionSubTitles: ArrayList<String> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prescriptionViewModel = ViewModelProvider(this, viewModelFactory).get(PrescriptionViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.prescriptionToolbar)
        UIUtil.applyViewInsertForBottomView(binding.prescriptionMainLayout)

        prescriptionViewModel.isFromReminder = intent.getBooleanExtra(ModelUtil.isFromReminder, false)
        prescriptionViewModel.selectedReminderModelId = intent.getStringExtra(ModelUtil.reminderModuleId)
        updateSubTitles()
        prescriptionViewPagerAdapter.updateModel(
            prescriptionViewModel.isFromReminder,
            prescriptionViewModel.selectedReminderModelId ?: ""
        )
        binding.prescriptionViewPager.offscreenPageLimit = ModelUtil.viewPagerOffScreenPageLimit
        binding.prescriptionViewPager.adapter = prescriptionViewPagerAdapter
        TabLayoutMediator(binding.prescriptionTabLayout, binding.prescriptionViewPager) { tab, position ->
            tab.text = prescriptionSubTitles[position]
        }.attach()

        if (prescriptionViewModel.isFromReminder) {
            binding.prescriptionToolbarText.setText(R.string.add_reminder)
            binding.prescriptionAddBtn.hide()
            binding.prescriptionTabLayout.visibility = View.GONE
            binding.prescriptionFilterBtn.visibility = View.GONE
        } else {


                if (ModelUtil.isModuleEnabled(authManager.selectedPatientSection, ModelUtil.medicationModelId) && ModelUtil.isModuleEnabled(
                        authManager.selectedPatientSection,
                        ModelUtil.supplementModelId
                    )
                ) {

                    binding.prescriptionViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageScrollStateChanged(state: Int) {

                        }

                        override fun onPageScrolled(
                            position: Int,
                            positionOffset: Float,
                            positionOffsetPixels: Int
                        ) {
                        }

                        override fun onPageSelected(position: Int) {

                            if (position == 0) {
                                if (ModelUtil.isPHREntryEnabled(
                                        authManager,
                                        ModelUtil.medicationModelId
                                    ) && RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_MEDICATIONS, applicationContext)
                                ) {
                                    binding.prescriptionAddBtn.show()
                                } else {
                                    binding.prescriptionAddBtn.hide()
                                }
                            } else {
                                if (ModelUtil.isPHREntryEnabled(
                                        authManager,
                                        ModelUtil.supplementModelId
                                    ) && RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_SUPPLEMENTS, applicationContext)
                                ) {
                                    binding.prescriptionAddBtn.show()
                                } else {
                                    binding.prescriptionAddBtn.hide()
                                }
                            }
                        }
                    })
                    if (ModelUtil.isPHREntryEnabled(
                            authManager,
                            ModelUtil.medicationModelId
                        ) && RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_MEDICATIONS, applicationContext)
                    ) {
                        binding.prescriptionAddBtn.show()
                    } else {
                        binding.prescriptionAddBtn.hide()
                    }

                    binding.prescriptionAddBtn.setOnClickListener {

                        if (binding.prescriptionViewPager.currentItem == 0) {
                            val intent = Intent(this, AddMedicationActivity::class.java)
//                            intent.putExtra(ModelUtil.isHealthPassportIntent, prescriptionViewModel.isHealthPassport)
                            startActivityForResult(intent, 0)
                        } else {
                            val intent = Intent(this, AddSupplementActivity::class.java)
                            startActivityForResult(intent, 0)
                        }
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }

                    binding.prescriptionTabLayout.visibility = View.VISIBLE
                    binding.prescriptionToolbarText.setText(R.string.prescription_str)
                    binding.prescriptionFilterBtn.visibility = View.VISIBLE
                } else if (ModelUtil.isModuleEnabled(authManager.selectedPatientSection, ModelUtil.medicationModelId)) {

                    binding.prescriptionTabLayout.visibility = View.GONE
                    binding.prescriptionToolbarText.setText(R.string.medications_str)
                    binding.prescriptionFilterBtn.visibility = View.VISIBLE

                    if (ModelUtil.isPHREntryEnabled(authManager, ModelUtil.medicationModelId)) {

                        binding.prescriptionAddBtn.setOnClickListener {

                            val intent = Intent(this, AddMedicationActivity::class.java)
//                            intent.putExtra(ModelUtil.isHealthPassportIntent, prescriptionViewModel.isHealthPassport)
                            startActivityForResult(intent, 0)
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                        if (RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_MEDICATIONS, applicationContext))
                            binding.prescriptionAddBtn.show()
                    } else {
                        binding.prescriptionAddBtn.hide()
                    }
                } else if (ModelUtil.isModuleEnabled(authManager.selectedPatientSection, ModelUtil.supplementModelId)) {

                    binding.prescriptionTabLayout.visibility = View.GONE
                    binding.prescriptionToolbarText.setText(R.string.supplements_str)
                    binding.prescriptionFilterBtn.visibility = View.VISIBLE

                    if (ModelUtil.isPHREntryEnabled(authManager, ModelUtil.supplementModelId)) {

                        binding.prescriptionAddBtn.setOnClickListener {
                            val intent = Intent(this, AddSupplementActivity::class.java)
                            startActivityForResult(intent, 0)
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                        if (RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_SUPPLEMENTS, applicationContext))
                            binding.prescriptionAddBtn.show()
                    } else {
                        binding.prescriptionAddBtn.hide()
                    }
                }

        }

        binding.prescriptionBackBtn.setOnClickListener {
            onBackPressed()
        }


        val popupMenu = PopupMenu(this, binding.prescriptionFilterBtnAnchor, Gravity.START)
        popupMenu.inflate(R.menu.menu_prescription_status)
        popupMenu.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.prescriptionStatusAll -> {
                    onFilterClicked(ModelUtil.medicationViewTypeAll)
                }

                R.id.prescriptionStatusActive -> {
                    onFilterClicked(ModelUtil.medicationViewTypeActive)
                }

                R.id.prescriptionStatusInActive -> {
                    onFilterClicked(ModelUtil.medicationViewTypeInactive)
                }
            }

            return@setOnMenuItemClickListener true
        }

        binding.prescriptionFilterBtn.setOnClickListener {
            popupMenu.show()
        }

    }

    fun onMultiSelectChange(isEnabled: Boolean) {
        prescriptionViewModel.isMultiSelectEnabled = isEnabled

        if (isEnabled) {
            binding.prescriptionDeleteBtn.visibility = View.VISIBLE
            binding.prescriptionFilterBtn.visibility = View.GONE
        } else {
            binding.prescriptionDeleteBtn.visibility = View.GONE
            binding.prescriptionFilterBtn.visibility = View.VISIBLE

        }
    }

    private fun onFilterClicked(status: String) {
        if (binding.prescriptionViewPager.currentItem == 0) {
            if (status != prescriptionViewModel.medicationStatus) {
                prescriptionViewModel.medicationStatus = status
                prescriptionViewModel.isMedicationFetchComplete = false
                prescriptionViewModel.isMedicationListFetched = false
                prescriptionViewPagerAdapter.disableMultiSelectAdapter(0)
                prescriptionViewPagerAdapter.changeFilter(0)

            }

        } else {
            if (status != prescriptionViewModel.supplementStatus) {
                prescriptionViewModel.supplementStatus = status
                prescriptionViewModel.isSupplementFetchComplete = false
                prescriptionViewModel.isSupplementListFetched = false
                prescriptionViewPagerAdapter.disableMultiSelectAdapter(1)
                prescriptionViewPagerAdapter.changeFilter(1)

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                prescriptionViewPagerAdapter.refreshData(binding.prescriptionViewPager.currentItem)

                if (binding.prescriptionViewPager.currentItem == 0) {
                    prescriptionViewModel.isMedicationUpdated = true
                    sharedPreferences.edit().putBoolean(ModelUtil.MEDICATION_UPDATED, true).apply()
                }
            }
        }
    }

    fun floatingActionButtonVisiblity(): Int = binding.prescriptionAddBtn.visibility

    fun showFab() {
        binding.prescriptionAddBtn.show()
    }

    fun hideFab() {
        binding.prescriptionAddBtn.hide()
    }


    override fun onBackPressed() {
        if (prescriptionViewModel.isMedicationUpdated) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun updateSubTitles() {
        prescriptionSubTitles.clear()
        if (BuildUtil.isCustomApp) {
            prescriptionSubTitles.addAll(
                arrayListOf(
                    getString(R.string.medications_str), getString(
                        R.string.supplements_str
                    )
                )
            )
        } else {
            if (ModelUtil.isModuleEnabled(
                    authManager.selectedPatientSection,
                    ModelUtil.medicationModelId
                ) && RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.VIEW_MEDICATIONS, applicationContext)
            ) {
                prescriptionSubTitles.add(getString(R.string.medications_str))
            }
            if (ModelUtil.isModuleEnabled(
                    authManager.selectedPatientSection,
                    ModelUtil.supplementModelId
                ) && RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.VIEW_SUPPLEMENTS, applicationContext)
            ) {
                prescriptionSubTitles.add(getString(R.string.supplements_str))
            }
        }

    }

    fun reminderResultCallBack(selectedObjectStr: String, practiceId: String?) {
        val respIntent = Intent()
        respIntent.putExtra(ModelUtil.resModuleObject, selectedObjectStr)
        respIntent.putExtra(ModelUtil.practiceIdIntent, practiceId)
        respIntent.putExtra(ModelUtil.reminderModuleId, prescriptionViewModel.selectedReminderModelId)
        setResult(Activity.RESULT_OK, respIntent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down)
    }
}
