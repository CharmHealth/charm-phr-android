package com.charm.phr.mphr.ui.reminder.update_reminder

import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.ReminderCustomPOJO
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.database.entity.Medications
import com.charm.phr.mphr.model.database.entity.Supplements
import com.charm.phr.mphr.model.repository.ReminderRepository

class UpdateReminderHistoryModel(private val reminderRepository: ReminderRepository) : ViewModel() {

    val reminderAddHistoryListToLocalDB = ArrayList<ReminderHistoryPOJO>()
    val failedAddReminderHistory = ArrayList<ReminderCustomPOJO>()

    var reminderPOJOList = arrayListOf<ReminderPOJO>()
    var reminderCustomPOJOList = arrayListOf<ReminderCustomPOJO>()

    val failedUpdateReminderHistory = ArrayList<ReminderCustomPOJO>()
    val reminderUpdateHistoryListToLocalDB = ArrayList<ReminderHistoryPOJO>()

    var addHistoryErrorMsg=""
    var updateHistoryErrorMsg=""

    fun getReminderPOJOForReminderId(reminderId: String): ArrayList<ReminderPOJO>? {
        return reminderRepository.getReminderPOJOForReminderId(reminderId)
    }

    suspend fun addReminderHistory(reminderHistoryPOJO: ReminderHistoryPOJO, reminderId: String, patientId: String): Any? {
        return reminderRepository.addReminderHistory(reminderHistoryPOJO, reminderId, patientId)
    }

    suspend fun updateReminderHistory(reminderHistoryPOJO: ReminderHistoryPOJO, reminderId: String, patientId: String): Any? {
        return reminderRepository.updateReminderHistory(reminderHistoryPOJO, reminderId, patientId)
    }

    fun addOrUpdateReminderHistoryInLocalDb(reminderHistoryPOJO: ArrayList<ReminderHistoryPOJO>): ArrayList<ReminderHistoryPOJO> {
        return reminderRepository.addOrUpdateReminderHistoryInLocalDb(reminderHistoryPOJO)
    }

    fun getMedicationsWithMedicationId(medicationId: String): List<Medications>? {
        return reminderRepository.getMedicationsWithMedicationId(medicationId)
    }

    fun getSupplementsWithSupplementId(supplementId: String): List<Supplements>? {
        return reminderRepository.getSupplementsWithSupplementId(supplementId)
    }
}