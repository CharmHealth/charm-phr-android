package com.charm.phr.mphr.model.database.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.charm.phr.mphr.model.ReminderFrequencyMapPOJO
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.database.entity.FrequencyAlarmMap
import com.charm.phr.mphr.model.database.entity.Medications
import com.charm.phr.mphr.model.database.entity.Reminder
import com.charm.phr.mphr.model.database.entity.ReminderFrequencyMap
import com.charm.phr.mphr.model.database.entity.ReminderHistory
import com.charm.phr.mphr.model.database.entity.ReminderMap
import com.charm.phr.mphr.model.database.entity.Supplements
import com.charm.phr.mphr.util.ModelUtil
import org.joda.time.DateTime

@Dao
abstract class MiscDao {

    //EXPLAIN QUERY PLAN
    /**
     * Method to Add or Update Reminders returns pair of addedReminders and FrequencyMap which needs deletion
     */
    @Transaction
    open fun addOrUpdateReminders(reminderPOJOArrayList: ArrayList<ReminderPOJO>): Pair<ArrayList<ReminderPOJO>, ArrayList<ReminderPOJO>> {

        val localFrequencyMapForDelete: ArrayList<ReminderFrequencyMap> = ArrayList()
        val newlyAddedReminders: ArrayList<ReminderPOJO> = ArrayList()

        reminderPOJOArrayList.forEach { reminderPOJO ->
            var localReminderId = 0L

            //region reminder
            val reminderId = reminderPOJO.reminder?.reminderId ?: ""
            val localReminderPOJO = getReminderPOJOForReminderId(reminderId)?.firstOrNull()
            if (reminderPOJO.reminder?.isDeleted == true) {
                //For Deleted reminders
                if (localReminderPOJO != null) {
                    deleteReminderPOJO(localReminderPOJO)
                }
            } else {
                if (localReminderPOJO != null) {
                    localReminderId = localReminderPOJO.reminder?.localReminderId ?: 0L
                    reminderPOJO.reminder?.localReminderId = localReminderId
                    reminderPOJO.reminder?.let {
                        Log.d("MiscDao", "")
                        internalUpdateReminder(it)
                    }
                } else {
                    reminderPOJO.reminder?.let { reminder ->
                        localReminderId = insertReminder(reminder)
                        reminder.localReminderId = localReminderId
                        newlyAddedReminders.add(reminderPOJO)
                    }
                }
                //endregion

                //region reminderMap
                reminderPOJO.reminderMap.firstOrNull()?.let { reminderMap ->
                    if (localReminderPOJO != null) {
                        localReminderPOJO.reminderMap.firstOrNull()?.localReminderMapId?.let {
                            reminderMap.localReminderId = localReminderId
                            reminderMap.localReminderMapId = it
                        }
                        internalUpdateReminderMap(reminderMap)
                    } else {
                        reminderMap.localReminderId = localReminderId
                        reminderMap.reminderId = reminderPOJO.reminder?.reminderId
                        reminderMap.localReminderMapId = internalInsertReminderMap(reminderMap)
                    }
                }
                //endregion

                //region reminderFrequencyMap
                val usedLocalFmIndexArrList: ArrayList<Int> = ArrayList()
                reminderPOJO.reminderFrequencyMap.forEach { reminderFrequencyMap ->
                    var hasLocalMatch = false
                    val fmDateTimeStr = getDateTimeStringForFrequencyMap(reminderFrequencyMap)
                    localReminderPOJO?.reminderFrequencyMap?.forEachIndexed inner@{ index, localReminderFrequencyMap ->
                        val fmLocalDateTimeStr =
                            getDateTimeStringForFrequencyMap(localReminderFrequencyMap)
                        if (fmDateTimeStr != null && fmLocalDateTimeStr != null && fmDateTimeStr == fmLocalDateTimeStr) {
                            reminderFrequencyMap.localReminderId = localReminderId
                            reminderFrequencyMap.localFrequencyMapId =
                                localReminderFrequencyMap.localFrequencyMapId
                            reminderFrequencyMap.notificationId =
                                localReminderFrequencyMap.notificationId
                            internalUpdateReminderFrequencyMap(reminderFrequencyMap)
                            usedLocalFmIndexArrList.add(index)
                            hasLocalMatch = true
                            return@inner
                        }
                    }

                    if (!hasLocalMatch) {
                        reminderFrequencyMap.localReminderId = localReminderId
                        reminderFrequencyMap.reminderId = reminderPOJO.reminder?.reminderId
                        reminderFrequencyMap.localFrequencyMapId =
                            internalInsertReminderFrequencyMap(reminderFrequencyMap)
                    }
                }

                localReminderPOJO?.reminderFrequencyMap?.forEachIndexed { index, localReminderFrequencyMap ->
                    if (!usedLocalFmIndexArrList.contains(index)) {
                        localFrequencyMapForDelete.add(localReminderFrequencyMap)
                    }
                }

                internalDeleteReminderFrequencyMap(localFrequencyMapForDelete)
                //endregion
            }
        }

        return Pair(reminderPOJOArrayList, newlyAddedReminders)

    }

    private fun getDateTimeStringForFrequencyMap(reminderFrequencyMap: ReminderFrequencyMap): String? {
        val time = reminderFrequencyMap.time ?: ""
        val day = reminderFrequencyMap.day.toString()
        return if (time.isNotBlank() && day.isNotBlank()) {
            day + time
        } else {
            null
        }
    }

    /**
     * Method to delete Reminders
     */
    @Transaction
    open fun deleteReminderPOJO(reminderPOJO: ReminderPOJO): Boolean {
        try {
            //Delete Associated Medications and Supplements
            val reminderList =
                getReminderPOJOForModuleId(reminderPOJO.reminderMap.firstOrNull()?.moduleId ?: "")
            reminderList?.size?.let { reminderListSize ->
                if (reminderListSize <= 1) {
                    if (reminderPOJO.reminderMap.firstOrNull()?.type == ModelUtil.medicationAddedAsMed) {
                        deleteMedicationsWithMedicationId(
                            reminderPOJO.reminderMap.firstOrNull()?.moduleId ?: ""
                        )
                    } else if (reminderPOJO.reminderMap.firstOrNull()?.type == ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() }) {
                        deleteSupplementsWithSupplementId(
                            reminderPOJO.reminderMap.firstOrNull()?.moduleId ?: ""
                        )
                    }
                }
            }
            reminderPOJO.reminderFrequencyMap.let { reminderFrequencyMaps ->
                reminderFrequencyMaps.forEach {
                    internalDeleteReminderFrequencyMap(arrayListOf(it))
                }
            }
            reminderPOJO.reminderMap.let { reminderMaps ->
                reminderMaps.forEach {
                    internalDeleteReminderMap(it)
                }
            }
            reminderPOJO.reminder?.let { reminder ->
                internalDeleteReminder(reminder)
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Method to get ReminderPOJO for a reminderId
     */
    @Transaction
    @Query(
        "SELECT * FROM Reminder INNER JOIN ReminderFrequencyMap ON ReminderFrequencyMap.localReminderId = Reminder.localReminderId " +
                "INNER JOIN ReminderMap ON ReminderMap.localReminderId = Reminder.localReminderId " +
                "WHERE Reminder.reminderId = :reminderId GROUP BY Reminder.reminderId"
    )
    abstract fun getReminderPOJOForReminderId(reminderId: String): List<ReminderPOJO>?

    /**
     * Method to get ReminderPOJO for a moduleId
     */
    @Transaction
    @Query(
        "SELECT * FROM Reminder INNER JOIN ReminderFrequencyMap ON ReminderFrequencyMap.localReminderId = Reminder.localReminderId " +
                "INNER JOIN ReminderMap ON ReminderMap.localReminderId = Reminder.localReminderId " +
                "WHERE ReminderMap.moduleId = :moduleId GROUP BY Reminder.reminderId"
    )
    abstract fun getReminderPOJOForModuleId(moduleId: String): List<ReminderPOJO>?

    /**
     * Method to get ReminderPOJO for a notificationId
     */
    @Transaction
    @Query(
        "SELECT * FROM  ReminderFrequencyMap INNER JOIN ReminderMap on ReminderMap.localReminderId = ReminderFrequencyMap.localReminderId " +
                "WHERE ReminderFrequencyMap.notificationId = :notificationId"
    )
    abstract fun getReminderFrequencyMapForNotificationId(notificationId: String): List<ReminderFrequencyMapPOJO>?

    /**
     * Method to get ReminderHistoryPOJO for a reminderId
     */
    @Transaction
    @Query(
        "SELECT * FROM ReminderHistory WHERE ReminderHistory.reminderMapId = :reminderId"
    )
    abstract fun getReminderHistoryForReminderId(reminderId: String): List<ReminderHistory>?


    /**
     * Method to get ReminderHistory for a reminderId
     */
    @Transaction
    @Query(
        "DELETE  FROM ReminderHistory WHERE ReminderHistory.reminderMapId = :reminderId"
    )
    abstract fun deleteReminderHistoryForReminderId(reminderId: String)


    /**
     * Method to get ReminderHistoryPOJO for a historyId
     */
    @Transaction
    @Query(
        "SELECT * FROM ReminderHistory INNER JOIN ReminderFrequencyMap on ReminderHistory.localFrequencyMapId = ReminderFrequencyMap.localFrequencyMapId " +
                "INNER JOIN ReminderMap on ReminderMap.localReminderMapId = ReminderHistory.localReminderMapId " +
                "WHERE ReminderHistory.historyId = :historyId GROUP BY ReminderHistory.historyId"
    )
    abstract fun getReminderHistoryPOJOForHistoryId(historyId: String): List<ReminderHistoryPOJO>?


    /**
     * Method to add or update Reminder History
     */
    @Transaction
    open fun addOrUpdateReminderHistory(reminderHistoryPOJOArrList: ArrayList<ReminderHistoryPOJO>): ArrayList<ReminderHistoryPOJO> {

        //ReminderMap and ReminderFrequencyMap will already exist when adding history so mandating respective local ids while adding and updating Reminder History.

        val locallyAddedReminderHistoryArrList: ArrayList<ReminderHistoryPOJO> = ArrayList()

        reminderHistoryPOJOArrList.forEach { reminderHistoryPOJO ->
            val reminderHistory = reminderHistoryPOJO.reminderHistory
            //region reminder
            val historyId = reminderHistory?.historyId ?: ""
            val localReminderHistoryPOJO =
                getReminderHistoryPOJOForHistoryId(historyId)?.firstOrNull()
            if (reminderHistory?.isDeleted == true) {
                if (localReminderHistoryPOJO != null) {
                    internalDeleteReminderHistory(reminderHistory)
                }
            } else {
                val reminderFrequencyMap = reminderHistoryPOJO.reminderFrequencyMap.firstOrNull()
                if (reminderFrequencyMap != null && reminderFrequencyMap.localFrequencyMapId != 0L) {
                    reminderHistory?.localFrequencyMapId =
                        reminderFrequencyMap.localFrequencyMapId
                } else {
                    return@forEach
                }
                val reminderMap = reminderHistoryPOJO.reminderMap.firstOrNull()
                if (reminderMap != null && reminderMap.localReminderMapId != 0L) {
                    reminderHistory?.localReminderMapId = reminderMap.localReminderMapId ?: 0L
                } else {
                    return@forEach
                }

                if (localReminderHistoryPOJO != null) {
                    reminderHistory?.localHistoryId =
                        localReminderHistoryPOJO.reminderHistory?.localHistoryId ?: 0L
                }
                if (reminderHistory != null) {
                    if (reminderHistory.localHistoryId != 0L) {
                        internalUpdateReminderHistory(reminderHistory)
                    } else {
                        reminderHistory.localHistoryId =
                            internalInsertReminderHistory(reminderHistory)
                    }
                    locallyAddedReminderHistoryArrList.add(reminderHistoryPOJO)
                } else {
                    return@forEach
                }
            }
        }
        return locallyAddedReminderHistoryArrList
    }

    /**
     * Method to delete reminderFrequencyMap
     */
    @Transaction
    open fun deleteReminderFrequencyMapList(reminderFrequencyMapArrList: ArrayList<ReminderFrequencyMap>) {
        return internalDeleteReminderFrequencyMap(reminderFrequencyMapArrList)
    }

    /**
     * Method to add FrequencyAlarmMap
     */
    @Transaction
    open fun addFrequencyAlarmMap(frequencyAlarmMap: FrequencyAlarmMap): Long {
        return internalInsertFrequencyAlarmMap(frequencyAlarmMap)
    }

    /**
     * Method to delete all FrequencyAlarmMap
     */
    @Transaction
    open fun deleteAllFrequencyAlarmMap() {
        getAllFrequencyAlarmMap()?.forEach {
            internalDeleteFrequencyAlarmMap(it)
        }
    }

    /**
     * Method to delete list of FrequencyAlarmMap
     */
    @Transaction
    open fun deleteFrequencyAlarmMap(frequencyAlarmMap: ArrayList<FrequencyAlarmMap>) {
        frequencyAlarmMap.forEach {
            internalDeleteFrequencyAlarmMap(it)
        }
    }

    /**
     * Method to update Notification Id in ReminderFrequencyMap
     */
    @Transaction
    @Query("UPDATE ReminderFrequencyMap SET notificationId = :notificationId  WHERE localFrequencyMapId = :localReminderMapId")
    abstract fun updateNotificationIdInFrequencyMap(
        localReminderMapId: Long,
        notificationId: String?
    )

    /**
     * Method to get total number of reminder scheduled for that notification
     */
    @Transaction
    open fun getNotificationIdForReminderTime(
        reminderTime: DateTime,
        practicePatientId: String?
    ): Long? {
        return getNotificationIdForReminderTime(reminderTime.millis, practicePatientId)
    }

    @Transaction
    @Query("SELECT localFrequencyAlarmMapId FROM FrequencyAlarmMap WHERE scheduledTime = :reminderTime AND patientId = :practicePatientId")
    abstract fun getNotificationIdForReminderTime(
        reminderTime: Long,
        practicePatientId: String?
    ): Long?

    @Transaction
    @Query(
        "SELECT * FROM Reminder INNER JOIN ReminderMap ON ReminderMap.localReminderId = Reminder.localReminderId " +
                "INNER JOIN ReminderFrequencyMap ON ReminderFrequencyMap.localReminderId = Reminder.localReminderId " +
                "WHERE (Reminder.startDate = :currentDateInMillis OR Reminder.endDate = :currentDateInMillis OR (:currentDateInMillis BETWEEN Reminder.startDate AND Reminder.endDate)) AND ReminderMap.patientId = :practicePatientId  GROUP BY Reminder.localReminderId"
    )
    abstract fun getReminderPOJOForCurrentDateInMillis(
        currentDateInMillis: Long,
        practicePatientId: String?
    ): List<ReminderPOJO>?

    @Transaction
    open fun getReminderPOJOForCurrentDate(
        date: DateTime,
        practicePatientId: String?
    ): List<ReminderPOJO>? {
        return getReminderPOJOForCurrentDateInMillis(date.millis, practicePatientId)
    }

    @Transaction
    @Query(
        "SELECT * FROM Reminder INNER JOIN ReminderMap ON ReminderMap.localReminderId = Reminder.localReminderId " +
                "INNER JOIN ReminderFrequencyMap ON ReminderFrequencyMap.localReminderId = Reminder.localReminderId " +
                "WHERE (Reminder.startDate = :startDateTime OR Reminder.endDate = :startDateTime OR Reminder.startDate = :endDateTime OR Reminder.endDate = :endDateTime OR (:startDateTime BETWEEN Reminder.startDate AND Reminder.endDate) OR (:endDateTime BETWEEN Reminder.startDate AND Reminder.endDate)) AND ReminderMap.patientId = :practicePatientId GROUP BY Reminder.localReminderId"
    )
    abstract fun getReminderPOJOForDateRangeInMillis(
        startDateTime: Long,
        endDateTime: Long,
        practicePatientId: String?
    ): List<ReminderPOJO>?

    @Transaction
    open fun getReminderPOJOForDateRange(
        startDateTime: DateTime,
        endDateTime: DateTime,
        practicePatientId: String?
    ): List<ReminderPOJO>? {
        return getReminderPOJOForDateRangeInMillis(
            startDateTime.millis,
            endDateTime.millis,
            practicePatientId
        )
    }

    @Transaction
    @Query(
        "SELECT * FROM ReminderHistory INNER JOIN ReminderMap ON ReminderMap.localReminderMapId = ReminderHistory.localReminderMapId " +
                "INNER JOIN ReminderFrequencyMap ON ReminderFrequencyMap.localFrequencyMapId = ReminderHistory.localFrequencyMapId " +
                "WHERE (ReminderHistory.statusUpdatedTime BETWEEN :startOfDay AND :endOfDay )and ReminderHistory.localReminderMapId = :localReminderMapId   GROUP BY ReminderHistory.localHistoryId"
    )
    abstract fun getReminderHistoryPOJOForCurrentDate(
        startOfDay: Long,
        endOfDay: Long,
        localReminderMapId: Long
    ): List<ReminderHistoryPOJO>?

    @Transaction
    open fun getReminderHistoryPOJOForCurrentDate(
        startOfDay: DateTime,
        endOfDay: DateTime,
        localReminderMapId: Long
    ): List<ReminderHistoryPOJO>? {
        return getReminderHistoryPOJOForCurrentDate(
            startOfDay.millis,
            endOfDay.millis,
            localReminderMapId
        )
    }

    /**
     * Method to get  FrequencyAlarmMap with notificationId
     */
    @Transaction
    @Query("SELECT * FROM FrequencyAlarmMap WHERE localFrequencyAlarmMapId = :localFrequencyAlarmMapId ")
    abstract fun getFrequencyAlarmMapForNotificationId(localFrequencyAlarmMapId: Long): List<FrequencyAlarmMap>?


    /**
     * Method to get all FrequencyAlarmMap
     */
    @Transaction
    @Query("SELECT * FROM FrequencyAlarmMap")
    abstract fun getAllFrequencyAlarmMap(): List<FrequencyAlarmMap>?

    /**
     * Method to get all reminder
     */
    @Transaction
    @Query(
        "SELECT * FROM Reminder INNER JOIN ReminderMap ON ReminderMap.localReminderId = Reminder.localReminderId " +
                "INNER JOIN ReminderFrequencyMap ON ReminderFrequencyMap.localReminderId = Reminder.localReminderId " +
                "where ReminderMap.patientId= :patientId GROUP BY Reminder.localReminderId ORDER BY Reminder.localReminderId DESC"
    )
    abstract fun getAllReminders(patientId: String): List<ReminderPOJO>?


    /**
     * Method to add Medications
     */
    @Transaction
    open fun addMedications(medications: Medications): Long {
        return internalInsertMedications(medications)
    }

    /**
     * Method to delete all Medications
     */
    @Transaction
    open fun deleteAllMedications() {
        val medicationsPOJOList = getAllMedications()
        medicationsPOJOList?.forEach { medicationsPOJO ->
            internalDeleteMedications(medicationsPOJO)
        }
    }

    /**
     * Method to get all Medications
     */
    @Transaction
    @Query("SELECT * FROM Medications")
    abstract fun getAllMedications(): List<Medications>?

    /**
     * Method to get Medications for a medicationId
     */
    @Transaction
    @Query(
        "SELECT * FROM Medications WHERE Medications.medicationId = :medicationId"
    )
    abstract fun getMedicationsWithMedicationId(medicationId: String): List<Medications>?

    /**
     * Method to get Medications for a medicationId
     */
    @Transaction
    @Query(
        "DELETE FROM Medications WHERE Medications.medicationId = :medicationId"
    )
    abstract fun deleteMedicationsWithMedicationId(medicationId: String)

    /**
     * Method to get Medications
     */
    @Transaction
    @Query(
        "SELECT * FROM Medications"
    )
    abstract fun getMedications(): List<Medications>

    /**
     * Method to add or update Medications
     */
    @Transaction
    open fun addOrUpdateMedications(
        medicationsArrList: ArrayList<Medications>
    ): ArrayList<Medications> {

        val locallyAddedMedicationsArrList: ArrayList<Medications> = ArrayList()


        medicationsArrList.forEach { medications ->

            val medicationId = medications.medicationId ?: ""
            val localMedicationPOJO = getMedicationsWithMedicationId(medicationId)?.firstOrNull()
            if (medications.isDeleted) {
                if (localMedicationPOJO != null) {
                    // internalDeleteMedications(medications)
                    medications.isDeleted = true
                    internalUpdateMedications(medications)
                }
            } else {
                if (localMedicationPOJO != null) {
                    medications.localMedicationId = localMedicationPOJO.localMedicationId
                }
                if (medications.localMedicationId != 0L) {
                    internalUpdateMedications(medications)
                } else {
                    medications.localMedicationId = internalInsertMedications(medications)
                }
                locallyAddedMedicationsArrList.add(medications)
            }
        }
        return locallyAddedMedicationsArrList
    }

    /**
     *
     * Method to get Supplements for a supplementId
     */
    @Transaction
    @Query(
        "SELECT * FROM Supplements WHERE Supplements.supplementId = :supplementId"
    )
    abstract fun getSupplementsWithSupplementId(supplementId: String): List<Supplements>?

    /**
     *
     * Method to delete Supplements for a supplementId
     */
    @Transaction
    @Query(
        "DELETE FROM Supplements WHERE Supplements.supplementId = :supplementId"
    )
    abstract fun deleteSupplementsWithSupplementId(supplementId: String)

    /**
     * Method to get Supplements for a supplementId
     */
    @Transaction
    @Query(
        "SELECT * FROM Supplements "
    )
    abstract fun getSupplements(): List<Supplements>


    /**
     * Method to add or update Supplements
     */
    @Transaction
    open fun addOrUpdateSupplements(supplementsArrList: ArrayList<Supplements>): ArrayList<Supplements> {

        val locallyAddedSupplementsArrList: ArrayList<Supplements> = ArrayList()
        supplementsArrList.forEach { supplements ->

            val supplementId = supplements.supplementId ?: ""
            val localSupplementPOJO = getSupplementsWithSupplementId(supplementId)?.firstOrNull()
            if (supplements.isDeleted) {
                if (localSupplementPOJO != null) {
                    //  internalDeleteSupplements(supplements)
                    supplements.isDeleted = true
                    internalUpdateSupplements(supplements)
                }
            } else {
                if (localSupplementPOJO != null) {
                    supplements.localSupplementId = localSupplementPOJO.localSupplementId
                }
                if (supplements.localSupplementId != 0L) {
                    internalUpdateSupplements(supplements)
                } else {
                    supplements.localSupplementId = internalInsertSupplements(supplements)
                }
                locallyAddedSupplementsArrList.add(supplements)
            }
        }
        return locallyAddedSupplementsArrList
    }


    //region Internal Methods

    @Insert
    abstract fun insertReminder(reminder: Reminder): Long

    @Update
    abstract fun internalUpdateReminder(reminder: Reminder)

    @Delete
    abstract fun internalDeleteReminder(reminder: Reminder)

    @Insert
    abstract fun internalInsertReminderMap(reminderMap: ReminderMap): Long

    @Update
    abstract fun internalUpdateReminderMap(reminderMap: ReminderMap)

    @Delete
    abstract fun internalDeleteReminderMap(reminderMap: ReminderMap)

    @Insert
    abstract fun internalInsertReminderFrequencyMap(reminderFrequencyMap: ReminderFrequencyMap): Long

    @Update
    abstract fun internalUpdateReminderFrequencyMap(reminderFrequencyMap: ReminderFrequencyMap)

    @Delete
    abstract fun internalDeleteReminderFrequencyMap(reminderFrequencyMapArrList: ArrayList<ReminderFrequencyMap>)

    @Insert
    abstract fun internalInsertReminderHistory(reminderHistory: ReminderHistory): Long

    @Update
    abstract fun internalUpdateReminderHistory(reminderHistory: ReminderHistory)

    @Delete
    abstract fun internalDeleteReminderHistory(reminderHistory: ReminderHistory)

    @Insert
    abstract fun internalInsertFrequencyAlarmMap(frequencyAlarmMap: FrequencyAlarmMap): Long

    @Update
    abstract fun internalUpdateFrequencyAlarmMap(frequencyAlarmMap: FrequencyAlarmMap)

    @Delete
    abstract fun internalDeleteFrequencyAlarmMap(frequencyAlarmMap: FrequencyAlarmMap)


    @Insert
    abstract fun internalInsertMedications(medications: Medications): Long

    @Update
    abstract fun internalUpdateMedications(medications: Medications)

    @Delete
    abstract fun internalDeleteMedications(medications: Medications)

    @Insert
    abstract fun internalInsertSupplements(supplements: Supplements): Long

    @Update
    abstract fun internalUpdateSupplements(supplements: Supplements)

    @Delete
    abstract fun internalDeleteSupplements(supplements: Supplements)


    //endregion


}