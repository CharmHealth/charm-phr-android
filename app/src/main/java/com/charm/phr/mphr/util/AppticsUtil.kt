package com.charm.phr.mphr.util

import android.app.Application
import androidx.preference.PreferenceManager
import com.zoho.apptics.analytics.AppticsEvents
import com.zoho.apptics.common.Apptics
import com.zoho.apptics.logger.AppticsLogger
import com.zoho.apptics.rateus.AppticsInAppRatings

object AppticsUtil {

    const val TAG = "AppticsUtil"

    fun addEvent(mdEventGroup: String, mdEvent: String) {
        if (BuildUtil.isCustomApp) return
        AppticsEvents.addEvent(mdEvent, mdEventGroup)
    }

    private fun setInAppRatingCustomUiCallback(application: Application) {
        AppticsInAppRatings.customUiCallback = { _, criteriaId ->
            val pref = PreferenceManager.getDefaultSharedPreferences(application.applicationContext)
            pref.edit().putLong(ModelUtil.isInAppRatingTriggered, criteriaId).commit()
        }
    }

    fun sendStats(
        criteriaId: Long,
        popupAction: AppticsInAppRatings.PopupAction,
        popupSource: AppticsInAppRatings.PopupSource
    ) {
        if (BuildUtil.isCustomApp) return
        AppticsInAppRatings.sendStats(criteriaId)
    }

    private fun enableAppticsLogger() {
        if (!AppticsLogger.isEnabled()) {
            AppticsLogger.enable()
        }
    }

    private fun disableAppticsLogger() {
        if (AppticsLogger.isEnabled()) {
            AppticsLogger.disable()
        }
    }

    fun initApptics(application: Application) {
        Apptics.resetOnDCSwitch = true
        Apptics.init(application)
        setInAppRatingCustomUiCallback(application)
        enableAppticsLogger()
    }

}

object MdEventGroup {
    const val Reminder = "Reminder"
    const val Account = "Account"
    const val Message = "Message"
    const val Appointment = "Appointment"
}

object MdEvent {
    const val BookAppointmentOnSuccess = "BookAppointmentOnSuccess"
    const val BookAppointmentOnFailure = "BookAppointmentOnFailure"
    const val SendMessageOnSuccess = "SendMessageOnSuccess"
    const val SendMessageOnFailure = "SendMessageOnFailure"
    const val LoginOnSuccess = "LoginOnSuccess"
    const val LogoutOnSuccess = "LogoutOnSuccess"
    const val LogoutOnKeyStoreCorrupt = "LogoutOnKeyStoreCorrupt"
    const val AddReminderOnSuccess = "AddReminderOnSuccess"
    const val AddReminderOnFailure = "AddReminderFailure"
    const val UpdateReminderOnSuccess = "UpdateReminderOnSuccess"
    const val UpdateReminderOnFailure = "UpdateReminderOnFailure"
}

