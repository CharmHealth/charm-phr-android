package com.charm.phr.mphr.ui.prescription

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.charm.phr.mphr.R
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.ChangeFilterListener
import com.charm.phr.mphr.util.DisableMultiSelectListener
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.RefreshDataListener

class PrescriptionViewPagerAdapter(fa: FragmentActivity, val appContext: Context, val authManager: AuthManager) :
    FragmentStateAdapter(fa) {
    private var prescriptionItems: ArrayList<Fragment> = arrayListOf(MedicationFragment(), SupplementFragment())


    fun updateModel(isFromReminder: Boolean, selectedReminderModuleId: String) {

        prescriptionItems.clear()
        if (isFromReminder) {
            if (selectedReminderModuleId == appContext.getString(R.string.medications_str)) {
                prescriptionItems.add(MedicationFragment())
            } else if (selectedReminderModuleId == appContext.getString(R.string.supplements_str)) {
                prescriptionItems.add(SupplementFragment())
            }
        } else {
            if (BuildUtil.isCustomApp) {
                prescriptionItems.addAll(arrayListOf(MedicationFragment(), SupplementFragment()))
            } else {
                if (ModelUtil.isModuleEnabled(
                        authManager.selectedPatientSection,
                        ModelUtil.medicationModelId
                    ) && RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.VIEW_MEDICATIONS, appContext)
                ) {
                    prescriptionItems.add(MedicationFragment())
                }
                if (ModelUtil.isModuleEnabled(
                        authManager.selectedPatientSection,
                        ModelUtil.supplementModelId
                    ) && RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.VIEW_SUPPLEMENTS, appContext)
                ) {
                    prescriptionItems.add(SupplementFragment())
                }
            }
        }

        notifyDataSetChanged()
    }

    fun disableMultiSelectAdapter(position: Int) {

        val fragment = prescriptionItems[position]

        if (fragment is DisableMultiSelectListener) {
            fragment.disableMultiSelect()
        }

    }

    fun changeFilter(position: Int) {

        val fragment = prescriptionItems[position]

        if (fragment is ChangeFilterListener) {
            fragment.onFilterChange()
        }

    }

    fun refreshData(position: Int) {

        val fragment = prescriptionItems[position]
        if (fragment is RefreshDataListener) {
            fragment.refreshData()
        }
    }

    override fun getItemCount(): Int = prescriptionItems.size

    override fun createFragment(position: Int): Fragment = prescriptionItems[position]
}