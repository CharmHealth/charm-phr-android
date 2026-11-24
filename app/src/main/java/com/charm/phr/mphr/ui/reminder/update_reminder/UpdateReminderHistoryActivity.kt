package com.charm.phr.mphr.ui.reminder.update_reminder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityUpdateReminderHistoryBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ReminderCustomPOJO
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.ui.components.MDReminderStatusUpdateDialog
import com.charm.phr.mphr.ui.dashboard.DashboardActivity
import com.charm.phr.mphr.util.APIUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDCallback
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import javax.inject.Inject


class UpdateReminderHistoryActivity : MDBaseActivity() {

    private lateinit var activityUpdateReminderHistoryBinding: ActivityUpdateReminderHistoryBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var updateReminderHistoryModel: UpdateReminderHistoryModel

    val TAG = "UpdateReminderHistoryActivity"

    @Inject
    lateinit var apiUtil: APIUtil

    //region LifeCycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            finish()
            intentToDashBoardPage()
        } else {
            super.onBackPressed()
        }
    }
    //endregion

    //region Data Rendering
    private fun handleIntent(intent: Intent?) {
        activityUpdateReminderHistoryBinding = ActivityUpdateReminderHistoryBinding.inflate(layoutInflater)
        setContentView(activityUpdateReminderHistoryBinding.root)
        updateReminderHistoryModel = ViewModelProvider(this, viewModelFactory)[UpdateReminderHistoryModel::class.java]

        UIUtil.applyViewInsertForToolbar(activityUpdateReminderHistoryBinding.reminderHistoryToolbar)
        UIUtil.applyViewInsertForBottomView(activityUpdateReminderHistoryBinding.reminderHistoryMainLayout)

        val reminderIds = intent?.getStringExtra(ModelUtil.ReminderId)
        val reminderTime = intent?.getStringExtra(ModelUtil.ReminderTime)
        clearData()
        getReminderDetails(reminderIds, reminderTime)
    }

    private fun clearData() {
        updateReminderHistoryModel.reminderAddHistoryListToLocalDB.clear()
        updateReminderHistoryModel.reminderUpdateHistoryListToLocalDB.clear()
        updateReminderHistoryModel.reminderPOJOList.clear()
        updateReminderHistoryModel.reminderCustomPOJOList.clear()
        updateReminderHistoryModel.failedAddReminderHistory.clear()
        updateReminderHistoryModel.failedUpdateReminderHistory.clear()
    }

    private fun getReminderDetails(reminderIds: String?, reminderTime: String?) {
        val reminderIdArr = reminderIds?.split(",")
        reminderIdArr?.forEach { reminderId ->
            if (reminderId.isNotBlank()) {
                getReminderDetail(reminderId)
            }
        }
        if (updateReminderHistoryModel.reminderPOJOList.isNotEmpty()) {
            setUpMDReminderStatusUpdateDialog(reminderTime)
        } else {
            UIUtil.showToast(this, getString(R.string.reminder_load_error), Toast.LENGTH_SHORT)
            onBackPressed()
        }
    }

    private fun getReminderDetail(reminderId: String?) {
        val reminderObject =
            updateReminderHistoryModel.getReminderPOJOForReminderId(reminderId ?: "")?.firstOrNull()
        reminderObject?.let {
            if (authManager.selectedPatientLive.value?.practicePatientId == null) {
                val patient =
                    authManager.patientList.firstOrNull { patient -> patient.practicePatientId == reminderObject.reminderMap.firstOrNull()?.patientId }
                patient?.let { authManager.updateSelectedPatient(patient) }
            }
            if (reminderObject.reminder?.reminderId.isNullOrEmpty()) {
                reminderObject.reminder?.reminderId = reminderId
            }
            val customObject = formReminderCustomPOJO(reminderObject)
            if (customObject.selectedModuleObject != null) {
                updateReminderHistoryModel.reminderPOJOList.add(reminderObject)
                updateReminderHistoryModel.reminderCustomPOJOList.add(customObject)
            }
        }
    }

    private fun formReminderCustomPOJO(reminderObject: ReminderPOJO): ReminderCustomPOJO {
        val reminderCustomPOJO = ReminderCustomPOJO()
        reminderCustomPOJO.reminderMap = reminderObject.reminderMap
        reminderCustomPOJO.reminderFrequencyMap = reminderObject.reminderFrequencyMap
        reminderCustomPOJO.reminder = reminderObject.reminder
        when (reminderObject.reminderMap.first().type) {
            ModelUtil.medicationAddedAsMed -> {
                reminderCustomPOJO.selectedModuleObject =
                    updateReminderHistoryModel.getMedicationsWithMedicationId(
                        reminderObject.reminderMap.first().moduleId ?: ""
                    )?.firstOrNull()
            }

            ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() } -> {
                reminderCustomPOJO.selectedModuleObject =
                    updateReminderHistoryModel.getSupplementsWithSupplementId(
                        reminderObject.reminderMap.first().moduleId ?: ""
                    )?.firstOrNull()
            }
        }
        return reminderCustomPOJO
    }

    private fun setUpMDReminderStatusUpdateDialog(reminderTime: String?) {
        val screenWidth = UIUtil.getScreenWidth(this)
        val width =
            screenWidth - (2 * activityUpdateReminderHistoryBinding.root.context.resources.getDimensionPixelSize(
                R.dimen.common_dialog_horizontal_margin
            ))
        val mDReminderStatusUpdateDialog =
            MDReminderStatusUpdateDialog(activityUpdateReminderHistoryBinding.root.context, width)
        mDReminderStatusUpdateDialog.setReminderStatusUpdateDialogListener(object :
            MDReminderStatusUpdateDialog.ReminderStatusUpdateDialogListener {
            override fun onEditClick(
                reminderPOJO: ReminderPOJO,
                drugName: String,
                drugDirection: String,
                startDate: String,
                endDate: String
            ) {
                //Not required
            }

            override fun onSkipClick(reminderPOJO: ArrayList<ReminderCustomPOJO>) {
                if (isTaskRoot) {
                    handleServerCall(reminderPOJO, arrayListOf(), 0, reminderTime ?: "")
                } else {
                    performOnClickAction(reminderPOJO, arrayListOf(), 0, reminderTime ?: "")
                }
            }

            override fun onTakenClick(
                takenReminderPOJO: ArrayList<ReminderCustomPOJO>,
                skipReminderPOJO: ArrayList<ReminderCustomPOJO>
            ) {
                if (isTaskRoot) {
                    handleServerCall(takenReminderPOJO, skipReminderPOJO, 1, reminderTime ?: "")
                } else {
                    performOnClickAction(takenReminderPOJO, skipReminderPOJO, 1, reminderTime ?: "")
                }
            }

            override fun onHistoryClick(
                reminderPOJO: ReminderPOJO,
                drugName: String,
                drugDirection: String
            ) {
                //Not required
            }

            override fun onBackPress() {
                onBackPressed()
            }
        })

        mDReminderStatusUpdateDialog.setSource(updateReminderHistoryModel.reminderCustomPOJOList)
        mDReminderStatusUpdateDialog.scheduledTime = reminderTime ?: ""
        if (updateReminderHistoryModel.reminderCustomPOJOList.size > 1) {
            mDReminderStatusUpdateDialog.isFromSelectALl = true
        }
        mDReminderStatusUpdateDialog.isFromStatusUpdate = true
        mDReminderStatusUpdateDialog.isFromNotificationUpdate = true
        mDReminderStatusUpdateDialog.setCanceledOnTouchOutside(false)
        mDReminderStatusUpdateDialog.show()
    }
    //endregion

    //region History Update
    private fun handleServerCall(
        reminderPOJO: ArrayList<ReminderCustomPOJO>,
        skipReminderPOJO: ArrayList<ReminderCustomPOJO>,
        status: Int,
        scheduledTime: String
    ) {
        progressDialog.show()
        progressDialog.setMessage(R.string.sending_data_str)
        apiUtil.getToken(object : MDCallback(authManager) {
            override fun onMDSuccess(respObj: JsonObject) {
                progressDialog.dismiss()
                performOnClickAction(reminderPOJO, skipReminderPOJO, status, scheduledTime)
            }

            override fun onMDFailure(error: APIResponseError) {
                progressDialog.dismiss()
                UIUtil.showRetryAndCancelDialog(
                    this@UpdateReminderHistoryActivity,
                    getString(R.string.requestFailedTryAgain),
                    R.string.retry_str,
                    false
                ) { isRetry ->
                    if (isRetry) {
                        handleServerCall(reminderPOJO, skipReminderPOJO, status, scheduledTime)
                    } else {
                        onBackPressed()
                    }
                }
            }
        })
    }

    private fun performOnClickAction(
        reminderPOJO: ArrayList<ReminderCustomPOJO>,
        skipReminderPOJO: ArrayList<ReminderCustomPOJO>,
        status: Int,
        scheduledTime: String
    ) {
        updateReminderHistoryModel.reminderAddHistoryListToLocalDB.clear()
        updateReminderHistoryModel.reminderUpdateHistoryListToLocalDB.clear()
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
            addReminderHistory(reminderPOJOToAdd, scheduledTime, reminderPOJOToUpdate)
        }
    }

    private fun addReminderHistory(
        reminderPOJO: ArrayList<ReminderCustomPOJO>,
        scheduledTime: String,
        reminderPOJOToUpdate: ArrayList<ReminderCustomPOJO>
    ) {
        updateReminderHistoryModel.failedAddReminderHistory.clear()
        updateReminderHistoryModel.addHistoryErrorMsg = ""
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

            runOnUiThread {
                showResultAdd(scheduledTime, reminderPOJOToUpdate)
            }
        }
    }

    private fun showResultAdd(
        scheduledTime: String,
        reminderPOJOToUpdate: ArrayList<ReminderCustomPOJO>
    ) {
        progressDialog.dismiss()
        if (updateReminderHistoryModel.failedAddReminderHistory.isEmpty()) {
            UIUtil.showToast(
                this,
                getString(R.string.reminder_history_add_success_msg),
                Toast.LENGTH_SHORT
            )
            updateReminderHistoryModel.addOrUpdateReminderHistoryInLocalDb(
                updateReminderHistoryModel.reminderAddHistoryListToLocalDB
            )
            if (reminderPOJOToUpdate.isNotEmpty()) {
                updateReminderHistory(reminderPOJOToUpdate, scheduledTime)
            } else {
                onBackPressed()
            }
        } else {
            UIUtil.showRetryAndCancelDialog(
                this,
                updateReminderHistoryModel.addHistoryErrorMsg.ifBlank { getString(R.string.requestFailedTryAgain) },
                R.string.retry_str,
                false
            ) { isRetry ->
                if (isRetry) {
                    val listToRetry = arrayListOf<ReminderCustomPOJO>()
                    listToRetry.addAll(updateReminderHistoryModel.failedAddReminderHistory)
                    addReminderHistory(listToRetry, scheduledTime, reminderPOJOToUpdate)
                } else {
                    onBackPressed()
                }
            }
        }
    }

    private suspend fun addReminderHistory(
        reminderHistoryPOJO: ReminderHistoryPOJO,
        reminderPOJO: ReminderCustomPOJO
    ) {

        val addReminderHistoryResp = updateReminderHistoryModel.addReminderHistory(
            reminderHistoryPOJO,
            reminderPOJO.reminder?.reminderId ?: "",
            reminderPOJO.reminderMap.firstOrNull()?.patientId ?: ""
        )
        if (addReminderHistoryResp is ReminderHistoryPOJO) {
            updateReminderHistoryModel.reminderAddHistoryListToLocalDB.add(addReminderHistoryResp)
        } else {
            updateReminderHistoryModel.failedAddReminderHistory.add(reminderPOJO)
            if (addReminderHistoryResp is APIResponseError) {
                updateReminderHistoryModel.addHistoryErrorMsg =
                    addReminderHistoryResp.errorMsg ?: getString(R.string.something_went_wrong)
            }
        }

    }

    private fun showResultUpdate(scheduledTime: String) {
        progressDialog.dismiss()
        if (updateReminderHistoryModel.failedUpdateReminderHistory.isEmpty()) {
            UIUtil.showToast(
                this,
                getString(R.string.reminder_update_success_msg),
                Toast.LENGTH_SHORT
            )
            updateReminderHistoryModel.addOrUpdateReminderHistoryInLocalDb(
                updateReminderHistoryModel.reminderUpdateHistoryListToLocalDB
            )
            onBackPressed()
        } else {
            UIUtil.showRetryAndCancelDialog(
                this,
                updateReminderHistoryModel.updateHistoryErrorMsg.ifBlank { getString(R.string.requestFailedTryAgain) },
                R.string.retry_str,
                false
            ) { isRetry ->
                if (isRetry) {
                    val listToRetry = arrayListOf<ReminderCustomPOJO>()
                    listToRetry.addAll(updateReminderHistoryModel.failedUpdateReminderHistory)
                    updateReminderHistory(listToRetry, scheduledTime)
                } else {
                    onBackPressed()
                }
            }
        }
    }

    private fun updateReminderHistory(
        reminderPOJO: ArrayList<ReminderCustomPOJO>,
        scheduledTime: String
    ) {
        updateReminderHistoryModel.failedUpdateReminderHistory.clear()
        updateReminderHistoryModel.updateHistoryErrorMsg = ""
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

            runOnUiThread {
                showResultUpdate(scheduledTime)
            }
        }
    }

    private suspend fun updateReminderHistory(
        reminderHistoryPOJO: ReminderHistoryPOJO,
        reminderPOJO: ReminderCustomPOJO
    ) {

        val updateReminderHistoryResp = updateReminderHistoryModel.updateReminderHistory(
            reminderHistoryPOJO,
            reminderPOJO.reminder?.reminderId ?: "",
            reminderPOJO.reminderMap.firstOrNull()?.patientId ?: ""
        )
        if (updateReminderHistoryResp is ReminderHistoryPOJO) {
            updateReminderHistoryModel.reminderUpdateHistoryListToLocalDB.add(
                updateReminderHistoryResp
            )
        } else {
            updateReminderHistoryModel.failedUpdateReminderHistory.add(reminderPOJO)
            if (updateReminderHistoryResp is APIResponseError) {
                updateReminderHistoryModel.updateHistoryErrorMsg =
                    updateReminderHistoryResp.errorMsg ?: getString(R.string.something_went_wrong)
            }
        }

    }
    //endregion


    private fun intentToDashBoardPage() {
        val intent = Intent(applicationContext, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}