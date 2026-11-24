package com.charm.phr.mphr.service.appointmentReminder

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import androidx.work.Configuration
import com.charm.phr.mphr.MPHRApplication
import com.charm.phr.mphr.R
import com.charm.phr.mphr.util.PermissionUtil
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.SectionConfigList
import com.charm.phr.mphr.model.UnreadCount
import com.charm.phr.mphr.model.repository.AppointmentRepository
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.service.MDBaseJobService
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.MDAccountsRelatedUtil
import com.charm.phr.mphr.util.ModelUtil
import org.joda.time.DateTime
import java.util.concurrent.Executors
import javax.inject.Inject

class AppointmentReminderJobService : MDBaseJobService() {
    //


    /**
     * AppointmentReminderService is first called when user logs in and then periodically called
     * once per 12 hours;
     * Each time when service is called we check whether user is signed in and device is online.
     * Appointment enabled patient is filtered.
     * Upcoming appointment present patient is filtered.
     * Today's and next day's appointment is fetched.
     *
     * notification is shown in the morning if today has any appointment
     *
     * if appointment type is phone call or video consult another notification can be shown
     * when appointment time nears
     * */

    companion object {
        //

        private val TAG = "AppointmentReminderJS"

        val channelId = "com.charm.phr.mphr.service.AppointmentReminder"

        private const val APPOINTMENT_REMINDER_JOB_ID = 39
        private const val APPOINTMENT_REMINDER_JOB_FOR_HOMEPAGE_ID = 40

        fun scheduleAppointmentReminderJobService(context: Context, isPeriodic: Boolean, isHomePage: Boolean = false) {
            //
            /*val jobInfoBuilder = if (isHomePage){
                JobInfo.Builder(APPOINTMENT_REMINDER_JOB_FOR_HOMEPAGE_ID, ComponentName(context, AppointmentReminderJobService::class.java))
            }
            else{
                JobInfo.Builder(APPOINTMENT_REMINDER_JOB_ID, ComponentName(context, AppointmentReminderJobService::class.java))
            }
                    .setRequiresCharging(false)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)


            if (isPeriodic){
                jobInfoBuilder.setPeriodic(TimeUnit.HOURS.toMillis(12))
            }

            val jobScheduler = (context.getSystemService(Context.JOB_SCHEDULER_SERVICE)) as JobScheduler
            val ret = jobScheduler.schedule(jobInfoBuilder.build())

            if (ret == JobScheduler.RESULT_SUCCESS) {

                Log.d(TAG, "schedule()")
            }
            else{
                Log.d(TAG, "failed")
            }
*/
        }
    }

    init {
        Configuration.Builder().setJobSchedulerJobIdRange(ModelUtil.minJobSchedulerJobIdRange, ModelUtil.maxJobSchedulerJobIdRange).build()
    }

    private var jobParams: JobParameters? = null

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var pref: SharedPreferences

    @Inject
    lateinit var appointmentRepository: AppointmentRepository

    @Inject
    lateinit var miscRepository: MiscRepository


    @Inject
    lateinit var mPHRApplication: MPHRApplication

    override fun onStartJob(params: JobParameters?): Boolean {


        jobParams = params

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ModelUtil.createNotificationChannel(
                applicationContext,
                channelId,
                getString(R.string.appointment_reminder_heading),
                getString(R.string.appointment_reminder_msg)
            )
        }

        val executor = Executors.newCachedThreadPool()
        executor.execute {
            checkAndStartSync()
        }
        return super.onStartJob(params)
    }

    private fun checkAndStartSync() {

        Log.d(TAG, "checkForStartingSync()")

        val signedInUser = pref.getBoolean(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN, false)

        if (signedInUser) {
            if (authManager.isUserOnline()) {
                startSyncingData()
            } else {
                Log.d(TAG, "Device offline")
                stopThisService()
            }
        } else {
            Log.d(TAG, "not a signedInUser")
            stopThisService()
        }

    }

    private fun startSyncingData() {

        Log.d(TAG, " start appointment sync ")

        Looper.prepare()
        Handler(Looper.getMainLooper()).post {
            checkAppointmentIsEnabled()
        }

        Looper.loop()
    }

    private fun checkAppointmentIsEnabled() {


        Log.d(TAG, "checkAppointmentIsEnabled()")

        val totalCount = authManager.patientList.size
        var totalResp = 0
        var isErrorOccured = false

        val patientListWithEnabledAppointment = arrayListOf<Patient>()

        authManager.patientList.forEach { patient ->

            Log.d(TAG, "SectionConfig Called :${patient.practicePatientId}")

            val patientSectionCall = miscRepository.getPatientSection(patient)

            patientSectionCall.observeForever { resp ->
                if (resp != null) {
                    if (resp is SectionConfigList) {
                        totalResp++
                        if (!isErrorOccured) {
                            Log.d(TAG, "SectionConfig Fetched :${patient.practicePatientId}")

                            patient.sectionConfigList = resp

                            if (ModelUtil.isModuleEnabled(authManager.selectedPatientSection, ModelUtil.appointmentModelId, patient)) {
                                patientListWithEnabledAppointment.add(patient)
                            }

                            if (totalCount == totalResp) {
                                if (patientListWithEnabledAppointment.isNotEmpty()) {
                                    getAppointmentCount(patientListWithEnabledAppointment)
                                } else {
                                    Log.d(TAG, "no patients found with appointment enabled")
                                    stopThisService()
                                }
                            }
                        } //Todo can stop service here in else case
                    } else if (resp is APIResponseError) {
                        totalResp++
                        if (!isErrorOccured) {
                            Log.d(TAG, "SectionConfig failed :${patient.practicePatientId}")
                            isErrorOccured = true
                            stopThisService()
                        }

                    }
                    patientSectionCall.removeObservers(this)
                }
            }
        }
    }

    private fun getAppointmentCount(patientList: ArrayList<Patient>) {

        val totalCount = patientList.size
        var totalResp = 0
        var isErrorOccured = false

        Log.d(TAG, "getAppointmentCount()")

        val patientListWithAppointment = arrayListOf<Patient>()

        patientList.forEach { patient ->

            Log.d(TAG, "Appointment Count Called :${patient.practicePatientId}")

            val unreadCountCall = miscRepository.getUnreadCount(patient)
            unreadCountCall.observeForever(Observer { resp ->
                if (resp != null) {
                    if (resp is UnreadCount) {
                        Log.d(TAG, "Appointment Count fetched :${patient.practicePatientId}")
                        totalResp++
                        if (!isErrorOccured) {
                            if (resp.upcomingAppointmentCount > 0) {
                                patientListWithAppointment.add(patient)
                            }

                            if (totalCount == totalResp) {
                                if (patientListWithAppointment.isNotEmpty()) {
                                    getAppointmentList(patientListWithAppointment)
                                } else {
                                    Log.d(TAG, "no patients found with upcoming appointment count")
                                    stopThisService()
                                }
                            }
                        }
                    } else if (resp is APIResponseError) {
                        totalResp++
                        if (!isErrorOccured) {
                            Log.d(TAG, "Appointment Count failed :${patient.practicePatientId}")
                            isErrorOccured = true
                            stopThisService()
                        }
                    }
                    unreadCountCall.removeObservers(this)
                }
            })
        }

    }

    private fun getAppointmentList(patientList: ArrayList<Patient>) {

        Log.d(TAG, "getAppointmentList()")

        val totalCount = patientList.size
        var totalResp = 0
        var isErrorOccured = false

        val appointmentList = arrayListOf<Appointment>()
        val nextDay = DateTime.now().withTimeAtStartOfDay().plusDays(1)

        patientList.forEach { patient ->

            Log.d(TAG, "Appointment list called :${patient.practicePatientId}")

            val appointmentListCall = appointmentRepository.getUpcomingAppointment(patient)
            appointmentListCall.observeForever(Observer { resp ->
                if (resp != null) {
                    if (resp is ArrayList<*>) {
                        Log.d(TAG, "Appointment list fetched :${patient.practicePatientId}")
                        totalResp++
                        if (!isErrorOccured) {

                            val tempAppointmentList = resp as ArrayList<Appointment>

                            tempAppointmentList.forEach { appointment ->
                                if (nextDay.isAfter(appointment.appointmentDate)) {
                                    appointment.patient = patient
                                    appointmentList.add(appointment)
                                }
                            }

                            if (totalCount == totalResp) {
                                if (appointmentList.isNotEmpty()) {
                                    updateAlarm(appointmentList)
                                } else {
                                    Log.d(TAG, "no appointments found to schedule")
                                    stopThisService()
                                }
                            }
                        }
                    } else if (resp is APIResponseError) {
                        totalResp++
                        if (!isErrorOccured) {
                            Log.d(TAG, "Appointment list failed :${patient.practicePatientId}")
                            isErrorOccured = true
                            stopThisService()
                        }
                    }
                    appointmentListCall.removeObservers(this)
                }
            })
        }
    }

    private fun updateAlarm(appointmentList: ArrayList<Appointment>) {

        Log.d(TAG, "${appointmentList.size} count of appointments found")
        //add,update the alarm which is in the appointmentList
        //remove the alarms which are not in the list

        appointmentList.forEach {
            val intent = Intent(applicationContext, NotificationBroadcastReceiver::class.java)
            val title = this.applicationContext.getString(R.string.app_name)
            val message = "You have an up coming appointment"
            intent.putExtra(titleExtra, title)
            intent.putExtra(messageExtra, message)

            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                notificationID,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val time = it.appointmentStartTime?.minusHours(2)?.millis
            if (time != null) {
                if (PermissionUtil.canScheduleExactAlarms(applicationContext)) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        time,
                        pendingIntent
                    )
                }
            }

        }

        stopThisService()
    }

    private fun stopThisService() {
        Log.d(TAG, "stopThisService() stopSelf()")


        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        if (!pref.getBoolean(ModelUtil.isAppointmentReminderPeriodic, false)) {
            pref.edit().putBoolean(ModelUtil.isAppointmentReminderPeriodic, true).commit()
            scheduleAppointmentReminderJobService(mPHRApplication, true)
        }

        jobFinished(jobParams, false)
    }


}

const val notificationID = 1
const val channelID = "channel1"
const val titleExtra = "titleExtra"
const val messageExtra = "messageExtra"

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(intent.getStringExtra(titleExtra))
            .setContentText(intent.getStringExtra(messageExtra))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (PermissionUtil.checkPermission(context, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS))) {
            manager.notify(notificationID, notification)
        }
    }

}