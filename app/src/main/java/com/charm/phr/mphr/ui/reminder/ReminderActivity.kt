package com.charm.phr.mphr.ui.reminder

import MdFloatingActionMenu
import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityReminderBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.FloatingActionMenuItem
import com.charm.phr.mphr.model.FrequencyAlarmMapCustomPOJO
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.database.entity.FrequencyAlarmMap
import com.charm.phr.mphr.model.database.entity.Medications
import com.charm.phr.mphr.model.database.entity.Supplements
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.prescription.PrescriptionActivity
import com.charm.phr.mphr.ui.reminder.add_reminder.AddOrEditReminderActivity
import com.charm.phr.mphr.ui.reminder.reminder_history.ReminderHistoryActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.NotificationUtil
import com.charm.phr.mphr.util.PermissionUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.tabs.TabLayoutMediator
import org.joda.time.DateTime
import javax.inject.Inject

class ReminderActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var reminderViewModel: ReminderViewModel

    private lateinit var activityReminderBinding: ActivityReminderBinding

    @Inject
    lateinit var reminderViewPagerAdapter: ReminderViewPagerAdapter

    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    var isMenuExpanded: MutableState<Boolean?> = mutableStateOf(null)

    private var selectedModuleResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            showAddReminderFragment(result.data)
        }
    }

    private var addOrEditReminderResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val type = result.data?.getStringExtra(ModelUtil.resModuleType)
            if (type == ModelUtil.medicationAddedAsMed) {
                val medObj = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(ModelUtil.resModuleObject, Medications::class.java)
                } else {
                    result.data?.getParcelableExtra(ModelUtil.resModuleObject)
                }
                if (medObj != null) {
                    reminderViewModel.medicationList.add(medObj)
                }
            } else if (type == ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() }) {
                val supplementObj = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra(ModelUtil.resModuleObject, Supplements::class.java)
                } else {
                    result.data?.getParcelableExtra(ModelUtil.resModuleObject)
                }
                if (supplementObj != null) {
                    reminderViewModel.supplementList.add(supplementObj)
                }
            }

            reminderViewPagerAdapter.refreshData()
        }
    }

    //region override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityReminderBinding = ActivityReminderBinding.inflate(layoutInflater)
        setContentView(activityReminderBinding.root)

        UIUtil.applyViewInsertForToolbar(activityReminderBinding.reminderListToolbar)
        UIUtil.applyViewInsertForBottomView(activityReminderBinding.reminderMainLayout)

        initializePermissionResultLauncher()
        reminderViewModel = ViewModelProvider(this, viewModelFactory)[ReminderViewModel::class.java]
        setUpFilterPopMenu()
        setOnclickListener()
        progressDialog.show()
        progressDialog.setMessage(R.string.loading_reminder)
        fetchMedicationList()
        PermissionUtil.requestNotificationPermission(this, ::permissionAcceptCallBack, requestPermissionLauncher)
        setFloatingButtonContent()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    //endregion

    private fun setOnclickListener() {
        activityReminderBinding.reminderListBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setUpFilterPopMenu() {
        val popupMenu = PopupMenu(this, activityReminderBinding.reminderFilterBtnAnchor, Gravity.START)
        popupMenu.menu.add(ModelUtil.documentTypeAll)
        popupMenu.menu.add(ModelUtil.ActiveStatus)
        popupMenu.menu.add(ModelUtil.UpcomingStatus)
        popupMenu.menu.add(ModelUtil.CompletedStatus)

        popupMenu.setOnMenuItemClickListener { menuItem ->

            reminderViewModel.reminderFilterSelected = menuItem.title?.toString() ?: ""
            reminderViewPagerAdapter.onFilterChange(1)
            return@setOnMenuItemClickListener true
        }
        activityReminderBinding.reminderFilterBtn.setOnClickListener {
            popupMenu.show()
        }
    }

    private fun renderTabView() {
        activityReminderBinding.reminderViewPager.offscreenPageLimit = ModelUtil.viewPagerOffScreenPageLimit
        activityReminderBinding.reminderViewPager.adapter = reminderViewPagerAdapter
        val reminderSubTitles = arrayListOf(getString(R.string.today_str), getString(R.string.all_str))
        TabLayoutMediator(activityReminderBinding.reminderTabLayout, activityReminderBinding.reminderViewPager) { tab, position ->
            tab.text = reminderSubTitles[position]
        }.attach()
        activityReminderBinding.reminderViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                activityReminderBinding.reminderFabAdd.visibility = View.VISIBLE
                if (position == 0) {
                    activityReminderBinding.reminderFilterBtn.visibility = View.GONE
                } else {
                    activityReminderBinding.reminderFilterBtn.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setFloatingButtonContent() {
        val items: ArrayList<FloatingActionMenuItem> = arrayListOf()
        activityReminderBinding.reminderFabAdd.setContent {
            items.add(
                FloatingActionMenuItem(
                    icon = R.drawable.ic_tablet,
                    label = getString(R.string.medications_str),
                    backgroundColor = colorResource(R.color.white),
                    onClick = {
                        showSelectedReminderModuleItem(getString(R.string.medications_str))
                    },
                    contentColor = colorResource(R.color.colorPrimary)
                )
            )
            items.add(
                FloatingActionMenuItem(
                    icon = R.drawable.ic_supplements,
                    label = getString(R.string.supplements_str),
                    backgroundColor = colorResource(R.color.white),
                    onClick = {
                        showSelectedReminderModuleItem(getString(R.string.supplements_str))
                    },
                    contentColor = colorResource(R.color.colorPrimary)
                )
            )
            MPHRTheme {
                MdFloatingActionMenu(
                    items = items,
                    isMenuExpanded = isMenuExpanded,
                    onMenuExpandedChange = {
                        isMenuExpanded.value = it
                    }
                )
            }
        }
    }

    //region Intent Methods
    private fun showSelectedReminderModuleItem(moduleId: String) {
        val intent = Intent(this, PrescriptionActivity::class.java)
        intent.putExtra(ModelUtil.isFromReminder, true)
        intent.putExtra(ModelUtil.reminderModuleId, moduleId)
        selectedModuleResultLauncher.launch(intent)
    }

    private fun showAddReminderFragment(data: Intent?) {
        val intent = Intent(this, AddOrEditReminderActivity::class.java)
        intent.putExtra(ModelUtil.isAddReminder, true)
        intent.putExtra(ModelUtil.resModuleObject, data?.getStringExtra(ModelUtil.resModuleObject))
        intent.putExtra(ModelUtil.practiceIdIntent, data?.getStringExtra(ModelUtil.practiceIdIntent))
        intent.putExtra(ModelUtil.reminderModuleId, data?.getStringExtra(ModelUtil.reminderModuleId))
        addOrEditReminderResultLauncher.launch(intent)

    }

    fun showEditReminderFragment(name: String, direction: String, startDate: String, isHistoryPresent: Boolean, endDate: String) {
        val intent = Intent(this, AddOrEditReminderActivity::class.java)
        intent.putExtra(ModelUtil.isAddReminder, false)
        intent.putExtra(ModelUtil.drugName, name)
        intent.putExtra(ModelUtil.drugDirection, direction)
        intent.putExtra(ModelUtil.ReminderModuleStartDate, startDate)
        intent.putExtra(ModelUtil.ReminderModuleEndDate, endDate)
        intent.putExtra(ModelUtil.IsHistoryPresent, isHistoryPresent)
        val reminderJson = MDJsonBuilder.formReminderJsonObjWithLocalId(reminderViewModel.reminderPOJO)
        intent.putExtra(ModelUtil.ReminderPOJO, reminderJson.toString())
        intent.putExtra(ModelUtil.ReminderModuleId, reminderViewModel.reminderPOJO.reminder?.reminderId)
        addOrEditReminderResultLauncher.launch(intent)
    }

    fun showReminderHistory(reminderPOJO: ReminderPOJO, drugName: String, drugDirection: String) {
        val intent = Intent(this, ReminderHistoryActivity::class.java)
        intent.putExtra(ModelUtil.drugName, drugName)
        intent.putExtra(ModelUtil.drugDirection, drugDirection)
        val reminderJson = MDJsonBuilder.formReminderJsonObjWithLocalId(reminderPOJO)
        intent.putExtra(ModelUtil.ReminderPOJO, reminderJson.toString())
        intent.putExtra(ModelUtil.ReminderModuleId, reminderPOJO.reminder?.reminderId)
        startActivity(intent)
    }
    //endregion

    //region Server API
    private fun getReminders() {
        reminderViewModel.getReminders(authManager.selectedPatientLive.value?.practicePatientId ?: "").observe(this) { resp ->
            if (resp != null) {
                if (resp is ArrayList<*>) {
                    val reminderPOJOList = resp as ArrayList<ReminderPOJO>
                    reminderViewModel.reminderPOJOList.clear()
                    val reminderAddResponse = reminderViewModel.addOrUpdateReminderInLocalDb(reminderPOJOList)
                    reminderViewModel.reminderPOJOList.addAll(reminderAddResponse.first)
                    scheduleReminder(reminderAddResponse.second)
                    //Get current day history and update in DB
                    getModuleRemindersHistory(
                        DateTime.now().toString(ModelUtil.charmDisplayDateFormat()),
                        DateTime.now().toString(ModelUtil.charmDisplayDateFormat()),
                        ModelUtil.medicationAddedAsMed
                    )
                    getModuleRemindersHistory(
                        DateTime.now().toString(ModelUtil.charmDisplayDateFormat()),
                        DateTime.now().toString(ModelUtil.charmDisplayDateFormat()),
                        ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() })
                    renderTabView()
                } else if (resp is APIResponseError) {
                    progressDialog.dismiss()
                    UIUtil.showRetryAndCancelDialog(this, resp.errorMsg ?: getString(R.string.requestFailedTryAgain), R.string.retry_str, false) { isRetry ->
                        if (isRetry) {
                            progressDialog.show()
                            progressDialog.setMessage(R.string.loading_reminder)
                            getReminders()
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }

        }
    }

    private fun scheduleReminder(newReminders: ArrayList<ReminderPOJO>) {
        //Create notification for newly added reminder from other resources
        val currentReminder = hashMapOf<DateTime, ArrayList<FrequencyAlarmMapCustomPOJO>>()
        val startDateToSchedule = DateTime.now()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val lastScheduledTimeStr = pref.getString(ModelUtil.lastAlarmScheduleTime, "")
        var endDateToSchedule = UIUtil.formatDate(lastScheduledTimeStr, ModelUtil.charmFlowsheetDateTimeFieldFormat()) ?: DateTime.now()
        endDateToSchedule = endDateToSchedule.plusHours(ModelUtil.reminderSchedulerTimeRange)
        newReminders.forEach { reminder ->
            ModelUtil.scheduleReminder(startDateToSchedule, endDateToSchedule, reminder, currentReminder)
        }
        //Create alarm for each entry
        scheduleAlarmForEachEntry(currentReminder, authManager.selectedPatientLive.value ?: Patient())
    }

    private fun getModuleRemindersHistory(fromDate: String, toDate: String, type: String) {
        reminderViewModel.getModuleRemindersHistory(fromDate, toDate, type).observe(this) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is ArrayList<*>) {
                    val reminderHistoryPOJOList = resp as ArrayList<ReminderHistoryPOJO>
                    if (reminderHistoryPOJOList.isNotEmpty()) {
                        reminderHistoryPOJOList.forEach {
                            val reminderObject = reminderViewModel.getReminderPOJOForReminderId(it.reminderHistory?.reminderMapId ?: "")?.firstOrNull()
                            if (reminderObject != null) {
                                it.reminderFrequencyMap =
                                    reminderObject.reminderFrequencyMap.filter { reminderFrequencyMap -> reminderFrequencyMap.time == it.reminderHistory?.reminderTime }
                                it.reminderHistory?.frequencyMapId = it.reminderFrequencyMap.firstOrNull()?.frequencyMapId
                                it.reminderMap = reminderObject.reminderMap
                            }
                        }
                        reminderViewModel.addOrUpdateReminderHistoryInLocalDb(reminderHistoryPOJOList)
                    }
                } else if (resp is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(this, R.string.requestFailedTryAgain, R.string.retry_str, false) { isRetry ->
                        if (isRetry) {
                            progressDialog.show()
                            progressDialog.setMessage(R.string.loading_reminder)
                            getModuleRemindersHistory(fromDate, toDate, type)
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }

        }
    }

    private fun fetchMedicationList() {
        reminderViewModel.fetchMedicationList(ModelUtil.medicationViewTypeAll, reminderViewModel.medicationPageNo)
            .observe(this) { medicationListRes ->
                if (medicationListRes != null) {
                    if (medicationListRes is Pair<*, *>) {
                        reminderViewModel.medicationListTemp.addAll(medicationListRes.first as ArrayList<PatMedPOJO>)
                        if (medicationListRes.second as Boolean) {
                            reminderViewModel.medicationPageNo++
                            fetchMedicationList()
                        } else {
                            reminderViewModel.medicationList.addAll(
                                reminderViewModel.addOrUpdateMedicationsInLocalDb(
                                    MDParsingHelper.formMedicationListForDB(
                                        reminderViewModel.medicationListTemp,
                                        this
                                    )
                                )
                            )
                            fetchSupplementList()
                        }
                    } else {
                        progressDialog.dismiss()
                        UIUtil.showRetryAndCancelDialog(this, R.string.requestFailedTryAgain, R.string.retry_str, false) { isRetry ->
                            if (isRetry) {
                                progressDialog.show()
                                progressDialog.setMessage(R.string.loading_reminder)
                                fetchMedicationList()
                            } else {
                                onBackPressed()
                            }
                        }
                    }
                }
            }
    }


    private fun fetchSupplementList() {
        reminderViewModel.fetchSupplements(ModelUtil.medicationViewTypeAll, reminderViewModel.supplementPageNo)
            .observe(this) { supplementListRes ->
                if (supplementListRes != null) {
                    if (supplementListRes is Pair<*, *>) {
                        reminderViewModel.supplementListTemp.addAll(supplementListRes.first as ArrayList<PatientSupplements>)
                        if (supplementListRes.second as Boolean) {
                            reminderViewModel.supplementPageNo++
                            fetchSupplementList()
                        } else {
                            reminderViewModel.supplementList.addAll(
                                reminderViewModel.addOrUpdateSupplementsInLocalDb(
                                    MDParsingHelper.formSupplementListForDB(
                                        reminderViewModel.supplementListTemp
                                    )
                                )
                            )
                            //Get all the reminder from server and add or update in local DB
                            getReminders()
                        }
                    } else {
                        progressDialog.dismiss()
                        UIUtil.showRetryAndCancelDialog(this, R.string.requestFailedTryAgain, R.string.retry_str, false) { isRetry ->
                            if (isRetry) {
                                progressDialog.show()
                                progressDialog.setMessage(R.string.loading_reminder)
                                fetchSupplementList()
                            } else {
                                onBackPressed()
                            }
                        }
                    }
                }
            }
    }
    //endregion

    fun showFab() {
        activityReminderBinding.reminderFabAdd.visibility = View.VISIBLE
    }

    fun hideFab() {
        activityReminderBinding.reminderFabAdd.visibility = View.GONE
    }

    fun floatingActionButtonVisibility(): Int = activityReminderBinding.reminderFabAdd.visibility


    private fun scheduleAlarmForEachEntry(schedulerList: HashMap<DateTime, ArrayList<FrequencyAlarmMapCustomPOJO>>, patient: Patient) {
        var title: String
        val message = applicationContext.getString(R.string.notification_message)
        schedulerList.forEach {
            var isMedication = false
            var isSupplement = false
            val frequencyAlarmMapList = it.value
            val frequencyAlarmMap = FrequencyAlarmMap()
            frequencyAlarmMap.scheduledTime = frequencyAlarmMapList.firstOrNull()?.scheduledTime
            frequencyAlarmMap.patientId = patient.practicePatientId
            val time = frequencyAlarmMap.scheduledTime?.millis ?: 0
            val notificationId = reminderViewModel.addFrequencyAlarmMap(frequencyAlarmMap)
            var reminderArrStr = ""
            frequencyAlarmMapList.forEach { frequencyAlarmMapCustomPOJO ->
                reminderViewModel.updateNotificationId(frequencyAlarmMapCustomPOJO.localFrequencyMapId, notificationId.toString())
                reminderArrStr += frequencyAlarmMapCustomPOJO.reminderId + ","
                when (frequencyAlarmMapCustomPOJO.moduleType) {
                    ModelUtil.medicationAddedAsMed -> {
                        isMedication = true
                    }

                    ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() } -> {
                        isSupplement = true
                    }
                }
            }
            title = NotificationUtil.getNotificationTitle(isMedication, isSupplement, applicationContext, patient)
            NotificationUtil.scheduleNotification(
                applicationContext, title, message, time, notificationId.toInt(), reminderArrStr,
                frequencyAlarmMap.scheduledTime?.toString(ModelUtil.charmDisplayTimeFormat)
            )
        }
    }

    //region Handling Permission for Notification
    private fun initializePermissionResultLauncher() {
        requestPermissionLauncher = PermissionUtil.getPermissionResultLauncher(
            this,
            ::permissionAcceptCallBack,
            R.string.allow_notification,
            null, false, ::permissionDeniedCallBack
        )
    }

    private fun permissionAcceptCallBack(permissions: Array<String>) {
        permissions.forEach { permission ->
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    if (!PermissionUtil.canScheduleExactAlarms(applicationContext)) {
                        UIUtil.showAlarmAndReminderDialog(this)
                    }
                }
            }
        }
    }


    private fun permissionDeniedCallBack(permissions: Array<String>) {
        PermissionUtil.requestPermission(permissions, requestPermissionLauncher)
    }
    //endregion
}

