package com.charm.phr.mphr.service.reminders

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.FrequencyAlarmMapCustomPOJO
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.database.entity.FrequencyAlarmMap
import com.charm.phr.mphr.model.repository.ReminderRepository
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.MDAccountsRelatedUtil
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.NotificationUtil
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

class ScheduleReminderPerDayWorker(
    ctx: Context,
    params: WorkerParameters,
    val reminderRepository: ReminderRepository,
    val authManager: AuthManager
) :
    Worker(ctx, params) {
    companion object {
        fun scheduleReminderPerDayWorkRequest(context: Context) {
            val periodicWorkRequest = PeriodicWorkRequest.Builder(
                ScheduleReminderPerDayWorker::class.java,
                ModelUtil.reminderFlexTimeRange.toLong(), TimeUnit.HOURS
            ).addTag(ModelUtil.scheduleReminderTAG)
                .build()
            WorkManager.getInstance(context).enqueue(periodicWorkRequest)
        }
    }


    override fun doWork(): Result {
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val userLoggedIn =
            pref.getBoolean(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN, false)
        if (userLoggedIn) {
            val patientList = authManager.patientList
            if (pref.getBoolean(ModelUtil.isReminderDataFetched, false)) {
                //cancel existing alarm as it might lead to duplicates
                cancelPastScheduledAlarm()
                scheduleAlarmForEachPatient(patientList)
            } else {
                //cancel existing alarm as it might lead to duplicates
                cancelPastScheduledAlarm()
                getReminderForAllPatient(patientList)
            }
            pref.edit().putString(
                ModelUtil.lastAlarmScheduleTime,
                DateTime.now().toString(ModelUtil.charmFlowsheetDateTimeFieldFormat())
            ).apply()
        }
        return Result.success()
    }

    private fun scheduleAlarm(patient: Patient) {
        val currentReminder = hashMapOf<DateTime, ArrayList<FrequencyAlarmMapCustomPOJO>>()
        val startDateToSchedule = DateTime.now()
        val endDateToSchedule = DateTime.now().plusHours(ModelUtil.reminderSchedulerTimeRange)
        //Fetch the Reminder which need to be scheduled for next 24 hours
        val currentReminderList = reminderRepository.getReminderPOJOForDateRange(
            startDateToSchedule,
            endDateToSchedule,
            patient.practicePatientId
        )
        currentReminderList?.forEach {
            //traverse and calculate the reminders dateTime
            ModelUtil.scheduleReminder(startDateToSchedule, endDateToSchedule, it, currentReminder)
        }
        //Create alarm for each entry
        scheduleAlarmForEachEntry(currentReminder, patient)
    }

    private fun scheduleAlarmForEachEntry(
        schedulerList: java.util.HashMap<DateTime, ArrayList<FrequencyAlarmMapCustomPOJO>>,
        patient: Patient
    ) {
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
            val notificationId = reminderRepository.addFrequencyAlarmMap(frequencyAlarmMap)
            var reminderArrStr = ""
            frequencyAlarmMapList.forEach { frequencyAlarmMapCustomPOJO ->
                reminderRepository.updateNotificationId(
                    frequencyAlarmMapCustomPOJO.localFrequencyMapId,
                    notificationId.toString()
                )
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
            title = NotificationUtil.getNotificationTitle(
                isMedication,
                isSupplement,
                applicationContext,
                patient
            )
            NotificationUtil.scheduleNotification(
                applicationContext, title, message, time, notificationId.toInt(), reminderArrStr,
                frequencyAlarmMap.scheduledTime?.toString(ModelUtil.charmDisplayTimeFormat)
            )
        }
    }

    private fun getReminderForAllPatient(patientList: ArrayList<Patient>) {
        patientList.forEach { patient ->
            getReminder(patient)
        }
    }

    private fun scheduleAlarmForEachPatient(patientList: ArrayList<Patient>) {
        patientList.forEach { patient ->
            scheduleAlarm(patient)
        }
    }

    private fun getReminder(patient: Patient) {
        //Get All Reminders From Server
        val getReminderResp = reminderRepository.getReminderSynchronize(
            null,
            null,
            null,
            patient.practicePatientId ?: ""
        )
        if (getReminderResp is ArrayList<*>) {
            val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            pref.edit().putBoolean(ModelUtil.isReminderDataFetched, true).apply()
            val list = getReminderResp as ArrayList<ReminderPOJO>
            //Update the existing reminder from server and add new reminders
            reminderRepository.addOrUpdateReminderInLocalDb(list)
            scheduleAlarm(patient)
        }
    }

    private fun cancelScheduledAlarm(scheduledAlarm: ArrayList<FrequencyAlarmMap>) {
        val title = applicationContext.getString(R.string.notification_title)
        val message = applicationContext.getString(R.string.notification_message)
        scheduledAlarm.forEach { frequencyAlarmMap ->
            NotificationUtil.cancelNotification(
                applicationContext,
                title,
                message,
                frequencyAlarmMap.localFrequencyAlarmMapId.toInt()
            )
        }
    }

    private fun cancelPastScheduledAlarm() {
        //Get All the scheduled Alarm from FrequencyAlarmMap
        val scheduledAlarm = reminderRepository.getAllFrequencyAlarmMap()
        //Cancel All the Scheduled Alarm to avoid duplication
        cancelScheduledAlarm(scheduledAlarm as ArrayList<FrequencyAlarmMap>)
        //Remove all the existing alarm from the FrequencyAlarmMap
        reminderRepository.deleteAllFrequencyAlarmMap()
    }

}