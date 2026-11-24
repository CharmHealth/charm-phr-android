package com.charm.phr.mphr.model.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.FrequencyAlarmMapCustomPOJO
import com.charm.phr.mphr.model.ReminderFrequencyMapPOJO
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.database.entity.FrequencyAlarmMap
import com.charm.phr.mphr.model.database.entity.Medications
import com.charm.phr.mphr.model.database.entity.ReminderFrequencyMap
import com.charm.phr.mphr.model.database.entity.ReminderHistory
import com.charm.phr.mphr.model.database.entity.Supplements
import com.charm.phr.mphr.model.database.mPHRDB
import com.charm.phr.mphr.util.APIUtil
import com.charm.phr.mphr.util.AppticsUtil
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.MDCallback
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.MdEvent
import com.charm.phr.mphr.util.MdEventGroup
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.NotificationUtil
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.WebService
import com.google.gson.JsonObject

import org.joda.time.DateTime
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by deepaj-6611 on 10/11/22.
 */

@Singleton
class ReminderRepository @Inject constructor(
    private val webService: WebService,
    private val authManager: AuthManager,
    private val executor: Executor,
    private val appContext: Context,
    private val db: mPHRDB,
    private val apiUtil: APIUtil,
) {

    fun getReminders(
        practiceId: String?,
        type: String?,
        moduleId: String?,
        patientId: String
    ): LiveData<Any> {
        val liveData = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {
            if (patientId.isNotBlank()) {
                val apiCall = webService.getReminders(patientId, practiceId, type, moduleId)
                apiUtil.initiateRequest(apiCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        val respJsonObj = MDParsingHelper.parseReminderObject(respObj, patientId)
                        if (respJsonObj != null) {
                            liveData.postValue(respJsonObj)
                        } else {
                            val error = APIResponseError()
                            error.errorMsg = MDParsingHelper.parseReminderErrorMessage(respObj)
                            liveData.postValue(error)
                        }
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        liveData.postValue(error)
                    }
                })
            } else {
                liveData.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            liveData.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return liveData
    }

    fun getReminderSynchronize(
        practiceId: String?,
        type: String?,
        moduleId: String?,
        patientId: String
    ): Any? {
        if (authManager.isUserOnline()) {
            if (patientId.isNotBlank()) {
                val apiCall = webService.getReminders(patientId, practiceId, type, moduleId)
                val resp = authManager.synchronizeAPICall(apiCall)
                Log.d("MDCallbackResponse", resp.toString())
                return if (resp is JsonObject) {
                    return MDParsingHelper.parseReminderObject(resp, patientId)
                } else {
                    resp
                }
            } else {
                return APIResponseError(
                    null,
                    null,
                    appContext.getString(R.string.error_patient_id_not_found)
                )
            }
        } else {
            return APIResponseError(
                ModelUtil.errorInternetConnectionCode,
                null,
                appContext.getString(R.string.internet_connection_error)
            )
        }
    }

    fun addReminder(reminderPOJO: ReminderPOJO): LiveData<Any> {
        val liveData = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {
            val postObj = MDJsonBuilder.formReminderJsonObj(reminderPOJO)
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {
                val apiCall = webService.addReminder(patientId, postObj)
                apiUtil.initiateRequest(apiCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        AppticsUtil.addEvent(MdEventGroup.Reminder, MdEvent.AddReminderOnSuccess)
                        reminderPOJO.reminder?.reminderId = MDParsingHelper.parseReminderId(respObj)
                        if (reminderPOJO.reminder?.reminderId != null) {
                            val getRemindersCall = webService.getReminders(
                                patientId,
                                reminderPOJO.reminderMap.firstOrNull()?.practiceId,
                                reminderPOJO.reminderMap.firstOrNull()?.type,
                                reminderPOJO.reminderMap.firstOrNull()?.moduleId
                            )
                            apiUtil.initiateRequest(
                                getRemindersCall,
                                object : MDCallback(authManager) {
                                    override fun onMDSuccess(respObj: JsonObject) {
                                        val respJsonObj =
                                            MDParsingHelper.parseReminderObject(respObj, patientId)
                                        if (respJsonObj != null) {
                                            addOrUpdateReminderInLocalDb(respJsonObj)
                                            addReminderScheduleNotification(reminderPOJO)
                                            liveData.postValue(true)
                                        } else {
                                            val error = APIResponseError()
                                            error.errorMsg =
                                                MDParsingHelper.parseReminderErrorMessage(respObj)
                                            liveData.postValue(error)
                                        }
                                    }

                                    override fun onMDFailure(error: APIResponseError) {
                                        liveData.postValue(error)
                                    }
                                })
                        } else {
                            AppticsUtil.addEvent(MdEventGroup.Reminder, MdEvent.AddReminderOnFailure)
                            val error = APIResponseError()
                            error.errorMsg = MDParsingHelper.parseReminderErrorMessage(respObj)
                            liveData.postValue(error)
                        }
                    }

                    override fun onMDFailure(error: APIResponseError) {

                        liveData.postValue(error)
                    }
                })
            } else {
                liveData.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            liveData.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return liveData
    }

    fun addReminderScheduleNotification(reminderPOJO: ReminderPOJO) {
        val reminderPOJOList = getReminderPOJOForReminderId(reminderPOJO.reminder?.reminderId ?: "")
        val updatedReminderPOJO = reminderPOJOList?.firstOrNull() ?: reminderPOJO
        val startDate = updatedReminderPOJO.reminder?.startDate
        val endDate = updatedReminderPOJO.reminder?.endDate
        if (startDate?.withTimeAtStartOfDay()?.isEqual(
                DateTime.now().withTimeAtStartOfDay()
            ) == true || endDate?.withTimeAtStartOfDay()
                ?.isEqual(
                    DateTime.now().withTimeAtStartOfDay()
                ) == true || (startDate?.isBeforeNow == true && endDate?.isAfterNow == true)
        ) {
            val currentReminder = hashMapOf<DateTime, ArrayList<FrequencyAlarmMapCustomPOJO>>()
            val startDateToSchedule = DateTime.now()
            val pref = PreferenceManager.getDefaultSharedPreferences(appContext)
            val lastScheduledTimeStr = pref.getString(ModelUtil.lastAlarmScheduleTime, "")
            var endDateToSchedule = UIUtil.formatDate(
                lastScheduledTimeStr,
                ModelUtil.charmFlowsheetDateTimeFieldFormat()
            ) ?: DateTime.now()
            endDateToSchedule = endDateToSchedule.plusHours(ModelUtil.reminderSchedulerTimeRange)
            ModelUtil.scheduleReminder(
                startDateToSchedule,
                endDateToSchedule,
                updatedReminderPOJO,
                currentReminder
            )
            scheduleAlarmForEachEntry(currentReminder)
        }
    }

    fun updateReminderScheduleNotification(
        reminderPOJO: ReminderPOJO,
        existingReminderPOJO: ReminderPOJO
    ) {
        val reminderPOJOList = getReminderPOJOForReminderId(reminderPOJO.reminder?.reminderId ?: "")
        val updatedReminderPOJO = reminderPOJOList?.firstOrNull() ?: reminderPOJO
        val updatedId = updatedReminderPOJO.reminderFrequencyMap.map { it.localFrequencyMapId }
        val oldId = existingReminderPOJO.reminderFrequencyMap.map { it.localFrequencyMapId }
        val extraAddedList =
            updatedReminderPOJO.reminderFrequencyMap.filter { it.localFrequencyMapId !in oldId }
        val deletedList =
            existingReminderPOJO.reminderFrequencyMap.filter { it.localFrequencyMapId !in updatedId }
        if (deletedList.isNotEmpty()) {
            cancelExistingAlarm(deletedList)
        }
        if (extraAddedList.isNotEmpty()) {
            val startDate = updatedReminderPOJO.reminder?.startDate
            val endDate = updatedReminderPOJO.reminder?.endDate
            val startDay = DateTime.now().withTimeAtStartOfDay()
            if (startDate?.isEqual(startDay) == true || endDate?.isEqual(startDay) == true || (startDate?.isBeforeNow == true && endDate?.isAfterNow == true)) {
                val currentReminder = hashMapOf<DateTime, ArrayList<FrequencyAlarmMapCustomPOJO>>()
                val startDateToSchedule = DateTime.now()
                val pref = PreferenceManager.getDefaultSharedPreferences(appContext)
                val lastScheduledTimeStr = pref.getString(ModelUtil.lastAlarmScheduleTime, "")
                var endDateToSchedule = UIUtil.formatDate(
                    lastScheduledTimeStr,
                    ModelUtil.charmFlowsheetDateTimeFieldFormat()
                ) ?: DateTime.now()
                endDateToSchedule =
                    endDateToSchedule.plusHours(ModelUtil.reminderSchedulerTimeRange)
                val tempReminderPOJO = ReminderPOJO()
                tempReminderPOJO.reminder = updatedReminderPOJO.reminder
                tempReminderPOJO.reminderMap = updatedReminderPOJO.reminderMap
                tempReminderPOJO.reminderFrequencyMap = extraAddedList
                ModelUtil.scheduleReminder(
                    startDateToSchedule,
                    endDateToSchedule,
                    tempReminderPOJO,
                    currentReminder
                )
                scheduleAlarmForEachEntry(currentReminder)
            }
        }
    }

    private fun scheduleAlarmForEachEntry(schedulerList: java.util.HashMap<DateTime, ArrayList<FrequencyAlarmMapCustomPOJO>>) {
        var title = appContext.getString(R.string.notification_title)
        val message = appContext.getString(R.string.notification_message)
        val patient = authManager.selectedPatientLive.value
        schedulerList.forEach {
            var isMedication = false
            var isSupplement = false
            val customFrequencyAlarmMap = it.value.firstOrNull()
            val time = it.key
            val existNotificationId = getNotificationId(time, patient?.practicePatientId)
            var reminderArrStr = ""
            if (existNotificationId != null) {
                val reminderFrequencyMapList =
                    getReminderFrequencyMap(existNotificationId.toString())
                reminderFrequencyMapList?.forEach { reminderFrequencyMap ->
                    reminderArrStr += reminderFrequencyMap.reminderFrequencyMap?.reminderId + ","
                    when (reminderFrequencyMap.reminderMap?.type) {
                        ModelUtil.medicationAddedAsMed -> {
                            isMedication = true
                        }

                        ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() } -> {
                            isSupplement = true
                        }
                    }
                }
                reminderArrStr += customFrequencyAlarmMap?.reminderId + ","
                NotificationUtil.cancelNotification(
                    appContext,
                    title,
                    message,
                    existNotificationId.toInt()
                )
                title = NotificationUtil.getNotificationTitle(
                    isMedication,
                    isSupplement,
                    appContext,
                    patient
                )
                NotificationUtil.scheduleNotification(
                    appContext,
                    title,
                    message,
                    time.millis,
                    existNotificationId.toInt(),
                    reminderArrStr,
                    time.toString(ModelUtil.charmDisplayTimeFormat)
                )
                updateNotificationId(
                    customFrequencyAlarmMap?.localFrequencyMapId ?: 0,
                    existNotificationId.toString()
                )
            } else {
                val frequencyAlarmMap = FrequencyAlarmMap()
                frequencyAlarmMap.scheduledTime = time
                frequencyAlarmMap.patientId = patient?.practicePatientId
                val notificationId = addFrequencyAlarmMap(frequencyAlarmMap)
                updateNotificationId(
                    customFrequencyAlarmMap?.localFrequencyMapId ?: 0,
                    notificationId.toString()
                )
                reminderArrStr += customFrequencyAlarmMap?.reminderId + ","
                when (customFrequencyAlarmMap?.moduleType) {
                    ModelUtil.medicationAddedAsMed -> {
                        isMedication = true
                    }

                    ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() } -> {
                        isSupplement = true
                    }
                }
                title = NotificationUtil.getNotificationTitle(
                    isMedication,
                    isSupplement,
                    appContext,
                    patient
                )
                NotificationUtil.scheduleNotification(
                    appContext, title, message, time.millis, notificationId.toInt(), reminderArrStr,
                    frequencyAlarmMap.scheduledTime?.toString(ModelUtil.charmDisplayTimeFormat)
                )
            }
        }
    }

    fun updateReminder(reminderPOJO: ReminderPOJO): LiveData<Any> {
        val liveData = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            val reminderId = reminderPOJO.reminder?.reminderId ?: ""
            val postJson = MDJsonBuilder.formReminderJsonObj(reminderPOJO)
            if (patientId.isNotBlank()) {
                val apiCall = webService.updateReminder(patientId, reminderId, postJson)
                apiUtil.initiateRequest(apiCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        AppticsUtil.addEvent(MdEventGroup.Reminder, MdEvent.UpdateReminderOnSuccess)
                        if (MDParsingHelper.parseUpdateHistory(respObj)) {
                            val getRemindersCall = webService.getReminders(
                                patientId,
                                reminderPOJO.reminderMap.firstOrNull()?.practiceId,
                                reminderPOJO.reminderMap.firstOrNull()?.type,
                                reminderPOJO.reminderMap.firstOrNull()?.moduleId
                            )
                            apiUtil.initiateRequest(
                                getRemindersCall,
                                object : MDCallback(authManager) {
                                    override fun onMDSuccess(respObj: JsonObject) {
                                        val respJsonObj =
                                            MDParsingHelper.parseReminderObject(respObj, patientId)
                                        if (respJsonObj != null) {
                                            val existingReminderPOJOList =
                                                getReminderPOJOForReminderId(
                                                    reminderPOJO.reminder?.reminderId ?: ""
                                                )
                                            addOrUpdateReminderInLocalDb(respJsonObj)
                                            updateReminderScheduleNotification(
                                                reminderPOJO,
                                                existingReminderPOJOList?.firstOrNull()
                                                    ?: reminderPOJO
                                            )
                                            liveData.postValue(true)
                                        } else {
                                            val error = APIResponseError()
                                            error.errorMsg =
                                                MDParsingHelper.parseReminderErrorMessage(respObj)
                                            liveData.postValue(error)
                                        }
                                    }

                                    override fun onMDFailure(error: APIResponseError) {
                                        liveData.postValue(error)
                                    }
                                })
                        } else {
                            val error = APIResponseError()
                            error.errorMsg = MDParsingHelper.parseReminderErrorMessage(respObj)
                            liveData.postValue(error)
                        }
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        AppticsUtil.addEvent(MdEventGroup.Reminder, MdEvent.UpdateReminderOnFailure)
                        liveData.postValue(error)
                    }
                })
            } else {
                liveData.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            liveData.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return liveData
    }

    fun deleteReminder(reminderId: String?, moduleId: String?, practiceId: String?): LiveData<Any> {
        val liveData = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                val apiCall = webService.deleteReminder(
                    patientId,
                    reminderId ?: "",
                    moduleId ?: "",
                    practiceId ?: ""
                )
                apiUtil.initiateRequest(apiCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        if (MDParsingHelper.parseUpdateHistory(respObj)) {
                            val reminderPOJO = getReminderPOJOForReminderId(reminderId ?: "")
                            deleteReminderFromLocalDB(reminderPOJO?.firstOrNull())
                            liveData.postValue(true)
                        } else {
                            val error = APIResponseError()
                            error.errorMsg = MDParsingHelper.parseReminderErrorMessage(respObj)
                            liveData.postValue(error)
                        }
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        liveData.postValue(error)
                    }
                })
            } else {
                liveData.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            liveData.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return liveData
    }

    fun deleteReminderFromLocalDB(reminderPOJO: ReminderPOJO?) {
        if (reminderPOJO != null) {
            val reminderPOJOFromDB =
                getReminderPOJOForReminderId(reminderPOJO.reminder?.reminderId ?: "")
            cancelExistingAlarm(
                reminderPOJOFromDB?.firstOrNull()?.reminderFrequencyMap
                    ?: reminderPOJO.reminderFrequencyMap
            )
            //delete the  reminder
            deleteReminderInLocalDb(reminderPOJO)
            deleteReminderHistory(reminderPOJO.reminder?.reminderId ?: "")
        }
    }

    private fun cancelExistingAlarm(reminderFrequencyMap: List<ReminderFrequencyMap>) {
        //Get the alarm already set for the existing reminder
        var title = appContext.getString(R.string.notification_title)
        val message = appContext.getString(R.string.notification_message)
        val patient = authManager.selectedPatientLive.value
        val frequencyAlarmMapListToDelete = arrayListOf<FrequencyAlarmMap>()
        reminderFrequencyMap.forEach {
            var reminderArrStr = ""
            it.notificationId?.let { notificationId ->
                val reminderFrequencyMapList = getReminderFrequencyMap(notificationId)
                val frequencyAlarmMapList = getFrequencyAlarmMap(notificationId.toLong())
                val time = frequencyAlarmMapList?.firstOrNull()?.scheduledTime
                NotificationUtil.cancelNotification(
                    appContext,
                    title,
                    message,
                    notificationId.toInt()
                )
                if (!reminderFrequencyMapList.isNullOrEmpty()) {
                    if (reminderFrequencyMapList.size == 1 && reminderFrequencyMapList.first().reminderFrequencyMap?.reminderId == it.reminderId) {
                        frequencyAlarmMapList?.firstOrNull()
                            ?.let { it1 -> frequencyAlarmMapListToDelete.add(it1) }
                    } else {
                        var isMedication = false
                        var isSupplement = false
                        reminderFrequencyMapList.forEach { reminderFrequencyMap ->
                            if (reminderFrequencyMap.reminderFrequencyMap?.localFrequencyMapId != it.localFrequencyMapId) {
                                reminderArrStr += reminderFrequencyMap.reminderFrequencyMap?.reminderId + ","
                                when (reminderFrequencyMap.reminderMap?.type) {
                                    ModelUtil.medicationAddedAsMed -> {
                                        isMedication = true
                                    }

                                    ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() } -> {
                                        isSupplement = true
                                    }
                                }
                            }
                        }
                        title = NotificationUtil.getNotificationTitle(
                            isMedication,
                            isSupplement,
                            appContext,
                            patient
                        )
                        NotificationUtil.scheduleNotification(
                            appContext,
                            title,
                            message,
                            time?.millis ?: 0,
                            notificationId.toInt(),
                            reminderArrStr,
                            time?.toString(ModelUtil.charmDisplayTimeFormat)
                        )
                    }
                } else if (!frequencyAlarmMapList.isNullOrEmpty()) {
                    frequencyAlarmMapList.firstOrNull()
                        ?.let { it1 -> frequencyAlarmMapListToDelete.add(it1) }
                }
            }
        }
        if (frequencyAlarmMapListToDelete.isNotEmpty()) {
            deleteFrequencyAlarmMap(frequencyAlarmMapListToDelete)
        }
    }

    suspend fun addReminderHistory(
        reminderHistoryPOJO: ReminderHistoryPOJO,
        reminderId: String,
        patientId: String
    ): Any? {
        if (authManager.isUserOnline()) {
            val postObj = MDJsonBuilder.formAddReminderHistoryJsonObj(reminderHistoryPOJO)
            Log.d("MDCallbackPost", postObj.toString())
            if (patientId.isNotBlank()) {
                val apiCall = webService.addReminderHistory(patientId, reminderId, postObj)
                val resp = apiUtil.initiateSynchronizeRequest(apiCall)
                Log.d("MDCallbackResponse", resp.toString())
                return if (resp is JsonObject) {
                    reminderHistoryPOJO.reminderHistory?.historyId =
                        MDParsingHelper.parseHistoryId(resp)
                    if (reminderHistoryPOJO.reminderHistory?.historyId != null) {
                        reminderHistoryPOJO
                    } else {
                        val error = APIResponseError()
                        error.errorMsg = MDParsingHelper.parseHistoryErrorMessage(resp)
                        error
                    }
                } else {
                    resp
                }
            } else {
                return APIResponseError(
                    null,
                    null,
                    appContext.getString(R.string.error_patient_id_not_found)
                )
            }
        } else {
            return APIResponseError(
                ModelUtil.errorInternetConnectionCode,
                null,
                appContext.getString(R.string.internet_connection_error)
            )
        }
    }

    suspend fun updateReminderHistory(
        reminderHistoryPOJO: ReminderHistoryPOJO,
        reminderId: String,
        patientId: String
    ): Any? {
        if (authManager.isUserOnline()) {
            val postObj = MDJsonBuilder.formUpdateReminderHistoryJsonObj(reminderHistoryPOJO)
            Log.d("MDCallbackPost", postObj.toString())
            if (patientId.isNotBlank()) {
                val apiCall = webService.updateReminderHistory(
                    patientId,
                    reminderId,
                    reminderHistoryPOJO.reminderHistory?.historyId ?: "",
                    postObj
                )
                val resp = apiUtil.initiateSynchronizeRequest(apiCall)
                return if (resp is JsonObject) {
                    Log.d("MDCallbackResponse", resp.toString())
                    if (MDParsingHelper.parseUpdateHistory(resp)) {
                        reminderHistoryPOJO
                    } else {
                        val error = APIResponseError()
                        error.errorMsg = MDParsingHelper.parseHistoryErrorMessage(resp)
                        error
                    }
                } else {
                    resp
                }
            } else {
                return APIResponseError(
                    null,
                    null,
                    appContext.getString(R.string.error_patient_id_not_found)
                )
            }
        } else {
            return APIResponseError(
                ModelUtil.errorInternetConnectionCode,
                null,
                appContext.getString(R.string.internet_connection_error)
            )
        }
    }

    fun getRemindersHistory(
        fromRange: Int,
        toRange: Int,
        isAsc: Boolean,
        reminderPOJO: ReminderPOJO
    ): LiveData<Any> {
        val liveData = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                val apiCall = webService.getRemindersHistory(
                    patientId,
                    reminderPOJO.reminder?.reminderId ?: "",
                    reminderPOJO.reminderMap.firstOrNull()?.moduleId ?: "",
                    fromRange,
                    toRange,
                    isAsc,
                    reminderPOJO.reminderMap.firstOrNull()?.practiceId ?: ""
                )
                apiUtil.initiateRequest(apiCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        val respHistoryObj =
                            MDParsingHelper.parseReminderHistoryObject(respObj, reminderPOJO)
                        if (respHistoryObj != null) {
                            liveData.postValue(respHistoryObj ?: "")
                        } else {
                            val error = APIResponseError()
                            error.errorMsg = MDParsingHelper.parseReminderErrorMessage(respObj)
                            liveData.postValue(error)
                        }
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        liveData.postValue(error)
                    }
                })
            } else {
                liveData.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            liveData.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return liveData
    }

    fun getModuleRemindersHistory(
        fromDate: String,
        toDate: String,
        type: String,
        isAsc: Boolean = true
    ): LiveData<Any> {
        val liveData = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                val practiceList =
                    ModelUtil.getEnabledPractices(authManager, ModelUtil.medicationModelId)
                val practiceSize = practiceList.size

                var respCount = 0

                var isCallbackSent = false
                val reminderHistoryList = arrayListOf<ReminderHistoryPOJO>()
                practiceList.forEach { practiceId ->
                    val apiCall = webService.getModuleRemindersHistory(
                        patientId,
                        fromDate,
                        toDate,
                        type,
                        isAsc,
                        practiceId
                    )
                    apiUtil.initiateRequest(apiCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {
                            respCount++
                            val respJsonObj =
                                MDParsingHelper.parseModuleReminderHistoryObject(respObj)
                            reminderHistoryList.addAll(respJsonObj)
                            if (respCount == practiceSize && !isCallbackSent) {
                                liveData.postValue(reminderHistoryList)
                                isCallbackSent = true
                            }
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            respCount++
                            if (!isCallbackSent) {
                                liveData.postValue(error)
                                isCallbackSent = true
                            }
                        }
                    })
                }
            } else {
                liveData.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            liveData.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return liveData
    }

    fun addOrUpdateReminderInLocalDb(reminderPOJO: ArrayList<ReminderPOJO>): Pair<ArrayList<ReminderPOJO>, ArrayList<ReminderPOJO>> {
        return db.miscDao().addOrUpdateReminders(reminderPOJO)
    }

    fun addOrUpdateReminderHistoryInLocalDb(reminderHistoryPOJOArrList: ArrayList<ReminderHistoryPOJO>): ArrayList<ReminderHistoryPOJO> {
        return db.miscDao().addOrUpdateReminderHistory(reminderHistoryPOJOArrList)
    }

    fun getReminderPOJOForReminderId(reminderId: String): ArrayList<ReminderPOJO>? {
        return db.miscDao().getReminderPOJOForReminderId(reminderId) as? ArrayList<ReminderPOJO>
    }

    private fun deleteReminderInLocalDb(reminderPOJO: ReminderPOJO): Boolean {
        return db.miscDao().deleteReminderPOJO(reminderPOJO)
    }

    fun getReminderPOJOForCurrentDate(practicePatientId: String?): ArrayList<ReminderPOJO>? {
        return db.miscDao().getReminderPOJOForCurrentDate(
            DateTime.now().withTimeAtStartOfDay(),
            practicePatientId
        ) as? ArrayList<ReminderPOJO>
    }

    fun getReminderPOJOForDateRange(
        startDateTime: DateTime,
        endDateTime: DateTime,
        practicePatientId: String?
    ): ArrayList<ReminderPOJO>? {
        return db.miscDao().getReminderPOJOForDateRange(
            startDateTime,
            endDateTime,
            practicePatientId
        ) as? ArrayList<ReminderPOJO>
    }


    fun getReminderHistoryPOJOForCurrentDate(localReminderMapId: Long): ArrayList<ReminderHistoryPOJO>? {
        return db.miscDao().getReminderHistoryPOJOForCurrentDate(
            DateTime.now().withTimeAtStartOfDay(),
            DateTime.now().plusDays(1).withTimeAtStartOfDay(),
            localReminderMapId
        ) as? ArrayList<ReminderHistoryPOJO>
    }

    fun getAllReminders(patientId: String?): List<ReminderPOJO>? {
        return db.miscDao().getAllReminders(patientId ?: "")
    }

    fun getAllFrequencyAlarmMap(): List<FrequencyAlarmMap>? {
        return db.miscDao().getAllFrequencyAlarmMap()
    }

    fun deleteAllFrequencyAlarmMap() {
        return db.miscDao().deleteAllFrequencyAlarmMap()
    }

    fun addFrequencyAlarmMap(frequencyAlarmMap: FrequencyAlarmMap): Long {
        return db.miscDao().addFrequencyAlarmMap(frequencyAlarmMap)
    }

    fun getMedicationsWithMedicationId(medicationId: String): List<Medications>? {
        return db.miscDao().getMedicationsWithMedicationId(medicationId)
    }

    fun getSupplementsWithSupplementId(supplementId: String): List<Supplements>? {
        return db.miscDao().getSupplementsWithSupplementId(supplementId)
    }

    fun updateNotificationId(localReminderMapId: Long, notificationId: String?) {
        return db.miscDao()
            .updateNotificationIdInFrequencyMap(localReminderMapId, notificationId)
    }

    private fun getReminderFrequencyMap(notificationId: String): ArrayList<ReminderFrequencyMapPOJO>? {
        return db.miscDao()
            .getReminderFrequencyMapForNotificationId(notificationId) as? ArrayList<ReminderFrequencyMapPOJO>
    }

    private fun getNotificationId(reminderTime: DateTime, practicePatientId: String?): Long? {
        return db.miscDao().getNotificationIdForReminderTime(reminderTime, practicePatientId)
    }

    private fun deleteFrequencyAlarmMap(frequencyAlarmMaps: ArrayList<FrequencyAlarmMap>) {
        return db.miscDao().deleteFrequencyAlarmMap(frequencyAlarmMaps)
    }

    private fun getFrequencyAlarmMap(localFrequencyAlarmMapId: Long): List<FrequencyAlarmMap>? {
        return db.miscDao().getFrequencyAlarmMapForNotificationId(localFrequencyAlarmMapId)
    }

    private fun deleteReminderHistory(reminderId: String) {
        return db.miscDao().deleteReminderHistoryForReminderId(reminderId)
    }

    fun getReminderHistory(reminderId: String): List<ReminderHistory>? {
        return db.miscDao().getReminderHistoryForReminderId(reminderId)
    }

    fun getSupplements(): List<Supplements> {
        return db.miscDao().getSupplements()
    }

    fun getMedications(): List<Medications> {
        return db.miscDao().getMedications()
    }

    fun addOrUpdateMedicationsInLocalDb(medicationsArrList: ArrayList<Medications>): ArrayList<Medications> {
        return db.miscDao().addOrUpdateMedications(medicationsArrList)
    }

    fun addOrUpdateSupplementsInLocalDb(supplementsArrList: ArrayList<Supplements>): ArrayList<Supplements> {
        return db.miscDao().addOrUpdateSupplements(supplementsArrList)
    }


}
