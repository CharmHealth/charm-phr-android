package com.charm.phr.mphr.ui.reminder

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.model.ReminderCustomPOJO
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.database.entity.FrequencyAlarmMap
import com.charm.phr.mphr.model.database.entity.Medications
import com.charm.phr.mphr.model.database.entity.ReminderHistory
import com.charm.phr.mphr.model.database.entity.Supplements
import com.charm.phr.mphr.model.repository.PrescriptionRepository
import com.charm.phr.mphr.model.repository.ReminderRepository
import com.charm.phr.mphr.util.ModelUtil
import org.joda.time.DateTime

class ReminderViewModel(private val reminderRepository: ReminderRepository, private val prescriptionRepository: PrescriptionRepository) : ViewModel() {

    var reminderPOJO = ReminderPOJO()

    var reminderPOJOList = arrayListOf<ReminderPOJO>()

    var medicationList: ArrayList<Medications?> = arrayListOf()
    var supplementList: ArrayList<Supplements?> = arrayListOf()
    var medicationListTemp: ArrayList<PatMedPOJO> = arrayListOf()
    var supplementListTemp: ArrayList<PatientSupplements> = arrayListOf()
    var currentReminder = hashMapOf<DateTime, ArrayList<ReminderCustomPOJO>>()
    val failedAddReminderHistory = ArrayList<ReminderCustomPOJO>()
    val failedUpdateReminderHistory = ArrayList<ReminderCustomPOJO>()


    var medicationPageNo: Int = 1
    var supplementPageNo: Int = 1

    var allRemindersList = arrayListOf<ReminderCustomPOJO>()

    var filteredRemindersList: ArrayList<ReminderCustomPOJO> = arrayListOf()

    var reminderFilterSelected = ModelUtil.documentTypeAll

    val reminderAddHistoryListToLocalDB = ArrayList<ReminderHistoryPOJO>()
    val reminderUpdateHistoryListToLocalDB = ArrayList<ReminderHistoryPOJO>()

    var addHistoryErrorMsg=""
    var updateHistoryErrorMsg=""

    fun getReminders(patientId: String): LiveData<Any> {
        return reminderRepository.getReminders(null, null, null, patientId)
    }

    fun getModuleRemindersHistory(fromDate: String, toDate: String, type: String): LiveData<Any> {
        return reminderRepository.getModuleRemindersHistory(fromDate, toDate, type)
    }

    fun getReminderPOJOForCurrentDate(patientId: String?): ArrayList<ReminderPOJO>? {
        return reminderRepository.getReminderPOJOForCurrentDate(patientId)
    }

    fun getReminderPOJOForReminderId(reminderId: String): ArrayList<ReminderPOJO>? {
        return reminderRepository.getReminderPOJOForReminderId(reminderId)
    }

    fun getReminderHistoryPOJOForCurrentDate(localReminderMapId: Long): ArrayList<ReminderHistoryPOJO>? {
        return reminderRepository.getReminderHistoryPOJOForCurrentDate(localReminderMapId)
    }

    fun fetchMedicationList(type: String, pageNo: Int): LiveData<Any> {
        return prescriptionRepository.getMedications(type, pageNo,true)
    }

    fun fetchSupplements(type: String, pageNo: Int): LiveData<Any> {
        return prescriptionRepository.getSupplements(type, pageNo,true)
    }

    fun addOrUpdateReminderInLocalDb(reminderPOJO: ArrayList<ReminderPOJO>): Pair<ArrayList<ReminderPOJO>, ArrayList<ReminderPOJO>> {
        return reminderRepository.addOrUpdateReminderInLocalDb(reminderPOJO)
    }

    fun getAllReminders(patientId: String?): List<ReminderPOJO>? {
        return reminderRepository.getAllReminders(patientId)
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

    fun getReminderHistory(reminderId: String): List<ReminderHistory>? {
        return reminderRepository.getReminderHistory(reminderId)
    }

    fun addFrequencyAlarmMap(frequencyAlarmMap: FrequencyAlarmMap): Long {
        return reminderRepository.addFrequencyAlarmMap(frequencyAlarmMap)
    }

    fun updateNotificationId(localReminderMapId: Long, notificationId: String?) {
        return reminderRepository.updateNotificationId(localReminderMapId, notificationId)
    }

    fun addOrUpdateMedicationsInLocalDb(medicationsArrList: ArrayList<Medications>):ArrayList<Medications> {
        return reminderRepository.addOrUpdateMedicationsInLocalDb(medicationsArrList)
    }

    fun addOrUpdateSupplementsInLocalDb(supplementsArrList: ArrayList<Supplements>): ArrayList<Supplements> {
        return reminderRepository.addOrUpdateSupplementsInLocalDb(supplementsArrList)
    }
}