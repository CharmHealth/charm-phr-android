package com.charm.phr.mphr.ui.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentAllReminderBinding
import com.charm.phr.mphr.model.ReminderCustomPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.ui.components.ProgressDialog
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.ChangeFilterListener
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RefreshDataListener
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class AllReminderFragment : DaggerFragment(), RefreshDataListener, ChangeFilterListener {

    private lateinit var fragmentAllReminderBinding: FragmentAllReminderBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var reminderViewModel: ReminderViewModel

    lateinit var progressDialog: ProgressDialog

    @Inject
    lateinit var allReminderListAdapter: AllReminderListAdapter

    @Inject
    lateinit var authManager: AuthManager


    //region Lifecycle methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        fragmentAllReminderBinding = FragmentAllReminderBinding.inflate(inflater, container, false)
        return fragmentAllReminderBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reminderViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ReminderViewModel::class.java]
        progressDialog = ProgressDialog(requireContext())
        initData()
        getReminderPOJO()
        setUpAllReminderListFragment()
        fragmentAllReminderBinding.allReminderListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        fragmentAllReminderBinding.allReminderListSwipeRefreshLayout.setOnRefreshListener {
            fragmentAllReminderBinding.allReminderListSwipeRefreshLayout.isRefreshing = true
            refreshData()
        }
    }

    //endregion

    //region override methods
    override fun refreshData() {
        reminderViewModel.allRemindersList = arrayListOf()
        getReminderPOJO()
    }

    override fun onFilterChange() {
        getFilteredList()
    }
    //endregion

    private fun initData() {
        reminderViewModel.allRemindersList = arrayListOf()
    }


    private fun createReminderMappingTypeObject(reminderCustomPOJO: ReminderCustomPOJO) {
        when (reminderCustomPOJO.reminderMap.first().type) {
            ModelUtil.medicationAddedAsMed -> {
                val filteredMedication = reminderViewModel.medicationList.filter {
                    it?.medicationId == reminderCustomPOJO.reminderMap.firstOrNull()?.moduleId

                }
                reminderCustomPOJO.selectedModuleObject = filteredMedication.firstOrNull()
            }

            ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() } -> {
                val filteredSupplement = reminderViewModel.supplementList.filter {
                    it?.supplementId == reminderCustomPOJO.reminderMap.firstOrNull()?.moduleId
                }
                reminderCustomPOJO.selectedModuleObject = filteredSupplement.firstOrNull()
            }
        }
    }

    //region render UI
    private fun setUpAllReminderListFragment() {
        fragmentAllReminderBinding.allReminderListRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        fragmentAllReminderBinding.allReminderListRV.adapter = allReminderListAdapter
        allReminderListAdapter.setAllReminderListAdapterListener(object : AllReminderListAdapter.AllReminderListAdapterListener {
            override fun onEditClick(reminderPOJO: ReminderPOJO, drugName: String, drugDirection: String, startDate: String, endDate: String) {
                val reminderActivity = requireActivity()
                reminderViewModel.reminderPOJO = reminderPOJO
                val reminderHistoryList = reminderViewModel.getReminderHistory(reminderPOJO.reminder?.reminderId ?: "")
                if (reminderActivity is ReminderActivity) {
                    reminderActivity.showEditReminderFragment(drugName, drugDirection, startDate, reminderHistoryList?.isNotEmpty() ?: false,endDate)
                }
            }

            override fun onHistoryClick(reminderPOJO: ReminderPOJO, drugName: String, drugDirection: String) {
                if (requireActivity() is ReminderActivity) {
                    val reminderActivity = requireActivity() as ReminderActivity
                    reminderActivity.showReminderHistory(reminderPOJO, drugName, drugDirection)
                }
            }

        })

        fragmentAllReminderBinding.allReminderListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, scrollX: Int, scrollY: Int) {
                try {
                    val reminderActivity = activity as ReminderActivity
                    if (scrollY > 0 && reminderActivity.floatingActionButtonVisibility() == View.VISIBLE) {
                        reminderActivity.hideFab()
                    } else if (scrollY < 0 && reminderActivity.floatingActionButtonVisibility() != View.VISIBLE
                    ) {
                        reminderActivity.showFab()
                    }
                } catch (e: Exception) {

                }
            }
        })
    }

    private fun renderReminderView() {
        fragmentAllReminderBinding.allReminderListSwipeRefreshLayout.isRefreshing = false
        if (reminderViewModel.filteredRemindersList.isEmpty()) {
            fragmentAllReminderBinding.allReminderTV.visibility = View.GONE
            fragmentAllReminderBinding.allReminderListRV.visibility = View.GONE
            fragmentAllReminderBinding.reminderAllListNoDataTV.root.visibility = View.VISIBLE
            fragmentAllReminderBinding.reminderAllListNoDataTV.nodataTextViewHint.visibility = View.VISIBLE
            fragmentAllReminderBinding.reminderAllListNoDataTV.nodataTextView.text =
                getString(
                    R.string.no_reminders_available,
                    if (reminderViewModel.reminderFilterSelected == ModelUtil.documentTypeAll) "" else reminderViewModel.reminderFilterSelected
                )
            fragmentAllReminderBinding.reminderAllListNoDataTV.nodataTextViewHint.setText(R.string.reminders_add_hint)
        } else {
            allReminderListAdapter.setAllRemindersList(reminderViewModel.filteredRemindersList)
            fragmentAllReminderBinding.allReminderTV.visibility = View.VISIBLE
            fragmentAllReminderBinding.allReminderTV.text = getString(R.string.reminder_count, reminderViewModel.reminderFilterSelected)
            fragmentAllReminderBinding.allReminderListRV.visibility = View.VISIBLE
            fragmentAllReminderBinding.reminderAllListNoDataTV.root.visibility = View.GONE
        }
    }
    //endregion

    //region Fetch Data


    private fun getReminderPOJO() {
        val patientId = authManager.selectedPatientLive.value?.practicePatientId
        val reminderPOJOList = reminderViewModel.getAllReminders(patientId)
        reminderPOJOList?.forEach { reminderPOJO ->
            val customReminderPOJO = ReminderCustomPOJO()
            customReminderPOJO.reminder = reminderPOJO.reminder
            customReminderPOJO.reminderMap = reminderPOJO.reminderMap
            customReminderPOJO.reminderFrequencyMap = reminderPOJO.reminderFrequencyMap
            createReminderMappingTypeObject(customReminderPOJO)
            if (customReminderPOJO.selectedModuleObject != null) {
                reminderViewModel.allRemindersList.add(customReminderPOJO)
            }
        }
        getFilteredList()
    }

    private fun getFilteredList() {
        reminderViewModel.filteredRemindersList.clear()
        when (reminderViewModel.reminderFilterSelected) {
            ModelUtil.CompletedStatus -> {
                reminderViewModel.filteredRemindersList = reminderViewModel.allRemindersList.filter {
                    it.reminder?.reminderStatus == ModelUtil.CompletedStatus
                } as ArrayList<ReminderCustomPOJO>
            }

            ModelUtil.UpcomingStatus -> {
                reminderViewModel.filteredRemindersList = reminderViewModel.allRemindersList.filter {
                    it.reminder?.reminderStatus == ModelUtil.UpcomingStatus
                } as ArrayList<ReminderCustomPOJO>
            }

            ModelUtil.ActiveStatus -> {
                reminderViewModel.filteredRemindersList = reminderViewModel.allRemindersList.filter {
                    it.reminder?.reminderStatus == ModelUtil.ActiveStatus
                } as ArrayList<ReminderCustomPOJO>
            }

            else -> {
                reminderViewModel.filteredRemindersList.addAll(reminderViewModel.allRemindersList)
            }
        }
        renderReminderView()
    }

    //endregion
}