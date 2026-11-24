package com.charm.phr.mphr

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.StrictMode
import androidx.preference.PreferenceManager
import androidx.work.Configuration
import com.charm.phr.mphr.di.DaggerAppComponent
import com.charm.phr.mphr.service.appointmentReminder.AppointmentReminderJobService
import com.charm.phr.mphr.service.reminders.FetchReminderDataWorker
import com.charm.phr.mphr.service.reminders.ScheduleReminderPerDayWorker
import com.charm.phr.mphr.util.AppticsUtil
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.ui.login.LoginActivity
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.AuthenticationUtil
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDAccountsRelatedUtil
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.NotificationUtil
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import net.danlew.android.joda.JodaTimeAndroid
import java.io.File
import javax.inject.Inject

class MPHRApplication : DaggerApplication(), Configuration.Provider {

    var currentActivity: Activity? = null

    @Inject
    lateinit var _workerConfiguration: Configuration

    @Inject
    lateinit var authManager: AuthManager

    override val workManagerConfiguration: Configuration
        get() = _workerConfiguration

    override fun onCreate() {
        super.onCreate()

        JodaTimeAndroid.init(this)

        System.loadLibrary("sqlcipher")

        deleteTemporaryFolders()

        FileUtil.deleteSwaasaFiles(applicationContext)

        initAppticsSdk()

        addAppointmentScheduler()

        AuthManager.oAuthAppInit(this)
        observeCharmAuthSession()

        initAppLock()

        createNotificationChannels()

//        setStrictModeForDebug()

    }

    /**
     * When the server ends the sign-in (revoked, user deactivated or blocked): log out and show
     * the login page with the session alert.
     */
    private fun observeCharmAuthSession() {
        if (!BuildUtil.isOAuthEnabled) return
        AuthenticationUtil.observeSessionEnded {
            val pref = PreferenceManager.getDefaultSharedPreferences(this@MPHRApplication)
            if (!pref.getBoolean(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN, false)) return@observeSessionEnded
            if (currentActivity is LoginActivity) return@observeSessionEnded
            authManager.logout()
            val intent = Intent(this@MPHRApplication, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(ModelUtil.apiTokenFailed, true)
            startActivity(intent)
        }
    }

    private fun setStrictModeForDebug() {
        if (BuildConfig.DEBUG == false) {
            return
        }

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )

        val vmPolicyBuilder = StrictMode.VmPolicy.Builder()
            .detectAll()
            .penaltyLog()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            vmPolicyBuilder.detectNonSdkApiUsage()
        }

        StrictMode.setVmPolicy(vmPolicyBuilder.build())

    }

    private fun createNotificationChannels() {

        NotificationUtil.createNotificationChannels(this)

    }

    private fun initAppLock() {

//        appLock = AppLock.getInstance(this, ApplockListener(), ThemeUtil(), R.drawable.ic_launcher)
//        appLock = AppLock.getInstance(this, R.mipmap.ic_launcher)
    }

    private fun deleteTemporaryFolders() {
        val tempDir = File(applicationContext.filesDir, ModelUtil.folderTemp)
        if (tempDir.exists()) {
            FileUtil.deleteChildInDirectory(tempDir)
        } else {
            tempDir.mkdir()
        }
    }

    private fun addAppointmentScheduler() {

        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val userLoggedIn = pref.getBoolean(MDAccountsRelatedUtil.IS_USER_SUCCESSFULLY_LOGGED_IN, false)
        if (!userLoggedIn) {
            return
        }

        if (pref.getBoolean(ModelUtil.isAppointmentReminderPeriodic, false)) {
            AppointmentReminderJobService.scheduleAppointmentReminderJobService(this, true)
        }
        if (!pref.getBoolean(ModelUtil.isPrescriptionReminderSchedulerStarted, false)) {
            FetchReminderDataWorker.fetchReminderDataRequest(this)
            ScheduleReminderPerDayWorker.scheduleReminderPerDayWorkRequest(this)
            pref.edit().putBoolean(ModelUtil.isPrescriptionReminderSchedulerStarted, true).apply()
        }
        AuthManager.oAuthAppInit(this)

    }

    private fun initAppticsSdk() {
        if (BuildUtil.isCustomApp) {
            return
        }
        AppticsUtil.initApptics(this)

    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }
}