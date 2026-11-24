package com.charm.phr.mphr.ui.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentCurrentReminderBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ReminderCustomPOJO
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.ui.components.ProgressDialog
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RefreshDataListener
import com.charm.phr.mphr.util.UIUtil
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import javax.inject.Inject

class CurrentReminderFragment : DaggerFragment(), RefreshDataListener {

    private lateinit var fragmentCurrentReminderBinding: FragmentCurrentReminderBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var reminderViewModel: ReminderViewModel

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var currentReminderListAdapter: CurrentReminderListAdapter

    lateinit var progressDialog: ProgressDialog

    //region override method
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        fragmentCurrentReminderBinding =
            FragmentCurrentReminderBinding.inflate(inflater, container, false)
        return fragmentCurrentReminderBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reminderViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[ReminderViewModel::class.java]
        progressDialog = ProgressDialog(requireContext())
        initData()
        getReminderPOJOForCurrentDate()
        setUpCurrentReminderListFragment()
        fragmentCurrentReminderBinding.currentReminderListSwipeRefreshLayout.setColorSchemeResources(
            R.color.colorAccent
        )
        fragmentCurrentReminderBinding.currentReminderListSwipeRefreshLayout.setOnRefreshListener {
            fragmentCurrentReminderBinding.currentReminderListSwipeRefreshLayout.isRefreshing = true
            refreshData()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    override fun refreshData() {
        reminderViewModel.currentReminder = hashMapOf()
        getReminderPOJOForCurrentDate()
    }

    //endregion

    private fun onBackClick() {
        requireActivity().onBackPressed()
    }

    private fun setUpCurrentReminderListFragment() {
        fragmentCurrentReminderBinding.currentReminderListRV.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        fragmentCurrentReminderBinding.currentReminderListRV.adapter = currentReminderListAdapter
        currentReminderListAdapter.setCurrentReminderListAdapterListener(object :
            CurrentReminderListAdapter.CurrentReminderListAdapterListener {
            override fun onEditClick(
                reminderPOJO: ReminderPOJO,
                drugName: String,
                drugDirection: String,
                startDate: String,
                endDate: String
            ) {
                val reminderActivity = activity
                reminderViewModel.reminderPOJO = reminderPOJO
                val reminderHistoryList =
                    reminderViewModel.getReminderHistory(reminderPOJO.reminder?.reminderId ?: "")
                if (reminderActivity is ReminderActivity) {
                    reminderActivity.showEditReminderFragment(
                        drugName,
                        drugDirection,
                        startDate,
                        reminderHistoryList?.isNotEmpty() ?: false,
                        endDate
                    )
                }
            }

            override fun onSkipClick(
                reminderPOJO: ArrayList<ReminderCustomPOJO>,
                scheduledTime: String,
                position: Int
            ) {
                performOnClickAction(reminderPOJO, arrayListOf(), scheduledTime, position, 0)
            }

            override fun onTakenClick(
                reminderPOJO: ArrayList<ReminderCustomPOJO>,
                skipReminderPOJO: ArrayList<ReminderCustomPOJO>,
                scheduledTime: String,
                position: Int
            ) {
                performOnClickAction(reminderPOJO, skipReminderPOJO, scheduledTime, position, 1)
            }

            override fun onHistoryClick(
                reminderPOJO: ReminderPOJO,
                drugName: String,
                drugDirection: String
            ) {
                if (requireActivity() is ReminderActivity) {
                    val reminderActivity = requireActivity() as ReminderActivity
                    reminderActivity.showReminderHistory(reminderPOJO, drugName, drugDirection)
                }
            }

        })

        fragmentCurrentReminderBinding.currentReminderListRV.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
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


    //region Updating reminder History
    private fun performOnClickAction(
        reminderPOJO: ArrayList<ReminderCustomPOJO>,
        skipReminderPOJO: ArrayList<ReminderCustomPOJO>,
        scheduledTime: String,
        position: Int,
        status: Int
    ) {
        reminderViewModel.reminderAddHistoryListToLocalDB.clear()
        reminderViewModel.reminderUpdateHistoryListToLocalDB.clear()
        val reminderPOJOToAdd: ArrayList<ReminderCustomPOJO> = arrayListOf()
        val reminderPOJOToUpdate: ArrayList<ReminderCustomPOJO> = arrayListOf()
        reminderPOJO.forEach {
            if (it.reminderHistory.isEmpty()) {
                it.reminderHistory =
                    arrayListOf(ModelUtil.formReminderHistoryPOJO(it, status, scheduledTime))
                reminderPOJOToAdd.add(it)
            } else {
                if (it.reminderHistory.first().reminderHistory?.status != status) {
                    it.reminderHistory.first().reminderHistory?.status = status
                    it.reminderHistory.first().reminderHistory?.statusUpdatedTime = DateTime.now()
                    reminderPOJOToUpdate.add(it)
                }
            }
        }
        if (status == 1) {
            if (skipReminderPOJO.isNotEmpty()) {
                skipReminderPOJO.forEach {
                    it.reminderHistory =
                        arrayListOf(ModelUtil.formReminderHistoryPOJO(it, 0, scheduledTime))
                    reminderPOJOToAdd.add(it)
                }
            }
        }
        if (reminderPOJOToAdd.isNotEmpty()) {
            addReminderHistory(reminderPOJOToAdd, scheduledTime, position, reminderPOJOToUpdate)
        } else if (reminderPOJOToUpdate.isNotEmpty()) {
            updateReminderHistory(reminderPOJOToUpdate, scheduledTime, position)
        }
    }


    private fun addReminderHistory(
        reminderPOJO: ArrayList<ReminderCustomPOJO>,
        scheduledTime: String,
        position: Int,
        reminderPOJOToUpdate: ArrayList<ReminderCustomPOJO>
    ) {
        reminderViewModel.failedAddReminderHistory.clear()
        reminderViewModel.addHistoryErrorMsg = ""
        progressDialog.show()
        progressDialog.setMessage(R.string.adding_reminder_history)
        lifecycleScope.launch(Dispatchers.IO) {
            reminderPOJO.forEach {
                val reminderHistoryPOJO = it.reminderHistory.first()
                withContext(Dispatchers.IO) {
                    addReminderHistory(
                        reminderHistoryPOJO,
                        it
                    )
                }
            }

            requireActivity().runOnUiThread {
                showResultAdd(scheduledTime, position, reminderPOJOToUpdate)
            }
        }
    }

    private fun showResultUpdate(scheduledTime: String, position: Int) {
        progressDialog.dismiss()
        if (reminderViewModel.failedUpdateReminderHistory.isEmpty()) {
            UIUtil.showToast(
                requireActivity(),
                getString(R.string.reminder_update_success_msg),
                Toast.LENGTH_SHORT
            )
            val responseReminderHistoryPOJO =
                reminderViewModel.addOrUpdateReminderHistoryInLocalDb(reminderViewModel.reminderUpdateHistoryListToLocalDB)
            currentReminderListAdapter.updateReminderHistory(position, responseReminderHistoryPOJO)
        } else {
            UIUtil.showRetryAndCancelDialog(
                requireActivity(),
                reminderViewModel.updateHistoryErrorMsg.ifBlank { getString(R.string.requestFailedTryAgain) },
                R.string.retry_str,
                false
            ) { isRetry ->
                if (isRetry) {
                    val listToRetry = arrayListOf<ReminderCustomPOJO>()
                    listToRetry.addAll(reminderViewModel.failedUpdateReminderHistory)
                    updateReminderHistory(listToRetry, scheduledTime, position)
                }
            }
        }
    }

    private fun updateReminderHistory(
        reminderPOJO: ArrayList<ReminderCustomPOJO>,
        scheduledTime: String,
        position: Int
    ) {
        reminderViewModel.failedUpdateReminderHistory.clear()
        reminderViewModel.updateHistoryErrorMsg = ""
        progressDialog.show()
        progressDialog.setMessage(R.string.updating_reminder_history)
        lifecycleScope.launch(Dispatchers.IO) {
            reminderPOJO.forEach {
                val reminderHistoryPOJO = it.reminderHistory.firstOrNull()
                reminderHistoryPOJO?.let { _ ->
                    withContext(Dispatchers.IO) {
                        updateReminderHistory(
                            reminderHistoryPOJO,
                            it
                        )
                    }
                }
            }

            requireActivity().runOnUiThread {
                showResultUpdate(scheduledTime, position)
            }
        }
    }

    private fun showResultAdd(
        scheduledTime: String,
        position: Int,
        reminderPOJOToUpdate: ArrayList<ReminderCustomPOJO>
    ) {
        progressDialog.dismiss()
        if (reminderViewModel.failedAddReminderHistory.isEmpty()) {
            UIUtil.showToast(
                requireActivity(),
                getString(R.string.reminder_history_add_success_msg),
                Toast.LENGTH_SHORT
            )
            val responseReminderHistoryPOJO =
                reminderViewModel.addOrUpdateReminderHistoryInLocalDb(reminderViewModel.reminderAddHistoryListToLocalDB)
            currentReminderListAdapter.updateReminderHistory(position, responseReminderHistoryPOJO)
            if (reminderPOJOToUpdate.isNotEmpty()) {
                updateReminderHistory(reminderPOJOToUpdate, scheduledTime, position)
            }
        } else {
            UIUtil.showRetryAndCancelDialog(
                requireActivity(),
                reminderViewModel.addHistoryErrorMsg.ifBlank { getString(R.string.requestFailedTryAgain) },
                R.string.retry_str,
                false
            ) { isRetry ->
                if (isRetry) {
                    val listToRetry = arrayListOf<ReminderCustomPOJO>()
                    listToRetry.addAll(reminderViewModel.failedAddReminderHistory)
                    addReminderHistory(listToRetry, scheduledTime, position, reminderPOJOToUpdate)
                }
            }
        }
    }

    private suspend fun addReminderHistory(
        reminderHistoryPOJO: ReminderHistoryPOJO,
        reminderPOJO: ReminderCustomPOJO
    ) {

        val addReminderHistoryResp = reminderViewModel.addReminderHistory(
            reminderHistoryPOJO,
            reminderPOJO.reminder?.reminderId ?: "",
            reminderPOJO.reminderMap.firstOrNull()?.patientId ?: ""
        )
        if (addReminderHistoryResp is ReminderHistoryPOJO) {
            reminderViewModel.reminderAddHistoryListToLocalDB.add(addReminderHistoryResp)
        } else {
            reminderViewModel.failedAddReminderHistory.add(reminderPOJO)
            if (addReminderHistoryResp is APIResponseError) {
                reminderViewModel.addHistoryErrorMsg =
                    addReminderHistoryResp.errorMsg ?: getString(R.string.something_went_wrong)
            }
        }

    }

    private suspend fun updateReminderHistory(
        reminderHistoryPOJO: ReminderHistoryPOJO,
        reminderPOJO: ReminderCustomPOJO
    ) {

        val updateReminderHistoryResp = reminderViewModel.updateReminderHistory(
            reminderHistoryPOJO,
            reminderPOJO.reminder?.reminderId ?: "",
            reminderPOJO.reminderMap.firstOrNull()?.patientId ?: ""
        )
        if (updateReminderHistoryResp is ReminderHistoryPOJO) {
            reminderViewModel.reminderUpdateHistoryListToLocalDB.add(updateReminderHistoryResp)
        } else {
            reminderViewModel.failedUpdateReminderHistory.add(reminderPOJO)
            if (updateReminderHistoryResp is APIResponseError) {
                reminderViewModel.updateHistoryErrorMsg =
                    updateReminderHistoryResp.errorMsg ?: getString(R.string.something_went_wrong)
            }
        }

    }
    //endregion

    //region Fetch Data
    private fun initData() {
        reminderViewModel.currentReminder = hashMapOf()
    }

    private fun getReminderPOJOForCurrentDate() {
        //Method to get the reminder for current Date from local DB
        val patientId = authManager.selectedPatientLive.value?.practicePatientId
        val currentDateList = reminderViewModel.getReminderPOJOForCurrentDate(patientId)
        currentDateList?.forEach { reminderPOJO ->
            val selectedModuleObject = createReminderMappingTypeObject(reminderPOJO)
            if (selectedModuleObject != null) {
                val reminderHistory = getReminderHistory(reminderPOJO)
                groupReminderByTime(reminderPOJO, selectedModuleObject, reminderHistory)
            }
        }
        val sortedCurrentReminder = reminderViewModel.currentReminder.entries.sortedBy {
            it.key
        }
        fragmentCurrentReminderBinding.currentReminderCount.text =
            getString(R.string.reminder_count, sortedCurrentReminder.size.toString())
        fragmentCurrentReminderBinding.currentReminderListSwipeRefreshLayout.isRefreshing = false
        if (sortedCurrentReminder.isNotEmpty()) {
            currentReminderListAdapter.setCurrentReminders(sortedCurrentReminder)
            fragmentCurrentReminderBinding.currentReminderCount.visibility = View.VISIBLE
            fragmentCurrentReminderBinding.currentReminderListRV.visibility = View.VISIBLE
            fragmentCurrentReminderBinding.reminderCurrentListNoDataTV.root.visibility = View.GONE
            fragmentCurrentReminderBinding.reminderCurrentListNoDataTV.nodataTextViewHint.visibility =
                View.GONE
        } else {
            fragmentCurrentReminderBinding.currentReminderCount.visibility = View.GONE
            fragmentCurrentReminderBinding.currentReminderListRV.visibility = View.GONE
            fragmentCurrentReminderBinding.reminderCurrentListNoDataTV.root.visibility =
                View.VISIBLE
            fragmentCurrentReminderBinding.reminderCurrentListNoDataTV.nodataTextViewHint.visibility =
                View.VISIBLE
            val reminderActivity = activity as ReminderActivity
            reminderActivity.showFab()
            fragmentCurrentReminderBinding.reminderCurrentListNoDataTV.nodataTextView.text =
                getString(R.string.no_reminders_available_today)
            fragmentCurrentReminderBinding.reminderCurrentListNoDataTV.nodataTextViewHint.setText(R.string.reminders_add_hint)
        }
    }

    private fun getReminderHistory(reminderCustomPOJO: ReminderPOJO): ArrayList<ReminderHistoryPOJO>? {

        return reminderViewModel.getReminderHistoryPOJOForCurrentDate(
            reminderCustomPOJO.reminderMap.firstOrNull()?.localReminderMapId ?: 0
        )

    }

    private fun createReminderMappingTypeObject(reminderCustomPOJO: ReminderPOJO): Any? {
        val reminderMap = reminderCustomPOJO.reminderMap.firstOrNull()
        when (reminderMap?.type) {
            ModelUtil.medicationAddedAsMed -> {
                return reminderViewModel.medicationList.firstOrNull {
                    it?.medicationId == reminderMap.moduleId
                }
            }

            ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() } -> {
                return reminderViewModel.supplementList.firstOrNull {
                    it?.supplementId == reminderMap.moduleId
                }
            }
        }
        return null
    }

    private fun groupReminderByTime(
        reminderPOJO: ReminderPOJO,
        selectedModuleObject: Any,
        reminderHistory: ArrayList<ReminderHistoryPOJO>?
    ) {
        val frequencyUnit = reminderPOJO.reminder?.frequencyUnit
        val reminderTimingMap = ModelUtil.setReminderTimingMap(reminderPOJO.reminderFrequencyMap)
        val currentDate = DateTime.now()
        when (frequencyUnit) {
            null, ModelUtil.frequencyUnitDay -> {
                reminderTimingMap[-1]?.forEach {
                    val customReminderPOJO = getCustomReminderPOJO(
                        reminderPOJO,
                        selectedModuleObject,
                        reminderHistory,
                        it?.time
                    )
                    val timeFormat =
                        UIUtil.formatDate(it?.time, ModelUtil.charmDisplay24HrTimeFormat())
                            ?: DateTime.now()
                    if (reminderViewModel.currentReminder.containsKey(timeFormat)) {
                        reminderViewModel.currentReminder[timeFormat]?.add(customReminderPOJO)
                    } else {
                        reminderViewModel.currentReminder[timeFormat] =
                            arrayListOf(customReminderPOJO)
                    }
                }
            }

            ModelUtil.frequencyUnitWeek -> {
                val formatDayOfWeek = ModelUtil.getFormattedDayOfWeek(currentDate)
                if (reminderTimingMap.containsKey(formatDayOfWeek)) {
                    reminderTimingMap[formatDayOfWeek]?.forEach {
                        val customReminderPOJO = getCustomReminderPOJO(
                            reminderPOJO,
                            selectedModuleObject,
                            reminderHistory,
                            it?.time
                        )
                        val timeFormat =
                            UIUtil.formatDate(it?.time, ModelUtil.charmDisplay24HrTimeFormat())
                                ?: DateTime.now()
                        if (reminderViewModel.currentReminder.containsKey(timeFormat)) {
                            reminderViewModel.currentReminder[timeFormat]?.add(customReminderPOJO)
                        } else {
                            reminderViewModel.currentReminder[timeFormat] =
                                arrayListOf(customReminderPOJO)
                        }
                    }
                }
            }
        }
    }

    private fun getCustomReminderPOJO(
        reminderPOJO: ReminderPOJO,
        selectedModuleObject: Any,
        reminderHistory: ArrayList<ReminderHistoryPOJO>?,
        time: String?
    ): ReminderCustomPOJO {
        val customReminderPOJO = ReminderCustomPOJO()
        customReminderPOJO.reminder = reminderPOJO.reminder
        customReminderPOJO.reminderMap = reminderPOJO.reminderMap
        customReminderPOJO.reminderFrequencyMap = reminderPOJO.reminderFrequencyMap
        customReminderPOJO.selectedModuleObject = selectedModuleObject
        if (!reminderHistory.isNullOrEmpty()) {
            customReminderPOJO.reminderHistory = reminderHistory.filter { reminderHistoryPOJO ->
                reminderHistoryPOJO.reminderHistory?.reminderTime == time
            }
        }
        return customReminderPOJO
    }
    //endregion


}