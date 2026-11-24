package com.charm.phr.mphr.ui.reminder.reminder_history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.repository.ReminderRepository
import org.joda.time.DateTime


class ReminderHistoryViewModel(private val reminderRepository: ReminderRepository) : ViewModel() {
    var reminderPOJO = ReminderPOJO()
    var fromRange = 0
    var toRange = 50
    val reminderHistoryPOJOArrList: ArrayList<ReminderHistoryPOJO> = arrayListOf()

    var reminderHistoryDateGrouping = hashMapOf<DateTime, ArrayList<ReminderHistoryPOJO>>()

    var selectedWeekDay = arrayListOf(false, false, false, false, false, false, false)

    var reminderTimingSet = mutableSetOf<DateTime>()

    fun getReminderHistory(): LiveData<Any> {
        return reminderRepository.getRemindersHistory(
            fromRange,
            toRange,
            true,
            reminderPOJO
        )
    }

    fun addOrUpdateReminderHistoryInLocalDb(reminderHistoryPOJOArrList: ArrayList<ReminderHistoryPOJO>): ArrayList<ReminderHistoryPOJO> {
        return reminderRepository.addOrUpdateReminderHistoryInLocalDb(reminderHistoryPOJOArrList)
    }
}