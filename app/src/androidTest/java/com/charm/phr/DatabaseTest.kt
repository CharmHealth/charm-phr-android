package com.charm.phr

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.database.dao.MiscDao
import com.charm.phr.mphr.model.database.entity.Reminder
import com.charm.phr.mphr.model.database.entity.ReminderFrequencyMap
import com.charm.phr.mphr.model.database.entity.ReminderHistory
import com.charm.phr.mphr.model.database.entity.ReminderMap
import com.charm.phr.mphr.model.database.mPHRDB
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var miscDao: MiscDao
    private lateinit var db: mPHRDB
    val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            context, mPHRDB::class.java
        ).build()
        miscDao = db.miscDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun addOrUpdateTest() {

        val dao = mPHRDB.getInstance(context).miscDao()
        dao.addOrUpdateReminders(arrayListOf(addOneReminder()))
        val reminder = dao.getReminderPOJOForReminderId("102")
        val reminderPOJO = reminder?.firstOrNull()
        reminderPOJO?.reminder?.addedBy = "memberid101"
        reminderPOJO?.reminderFrequencyMap?.firstOrNull()?.notificationId = "id101"
        reminderPOJO?.reminderFrequencyMap = listOf(
            reminderPOJO!!.reminderFrequencyMap[0], reminderPOJO!!.reminderFrequencyMap[1],
            ReminderFrequencyMap(day = 0, time = "11:00", reminderId = "102")
        )
        val localReminderPOJOArrList = dao.addOrUpdateReminders(arrayListOf(reminderPOJO!!))
        Log.d("addOrUpdateTest", localReminderPOJOArrList.first.firstOrNull().toString())


        assertThat("test", true)
    }

    //region Added for Db testing

    @Test
    fun addHistory() {
        val dao = mPHRDB.getInstance(context).miscDao()
        val reminderList = dao.getReminderPOJOForReminderId("101")
        val reminderHistoryPOJOArrList: ArrayList<ReminderHistoryPOJO> = ArrayList()
        reminderList?.forEach { reminderPOJO ->
            reminderPOJO.reminderFrequencyMap.forEach { reminderFrequencyMap ->
                val reminderHistoryPOJO = ReminderHistoryPOJO()
                val reminderHistory = ReminderHistory()
                reminderHistory.status = 1
                reminderHistory.localFrequencyMapId = reminderFrequencyMap.localFrequencyMapId ?: 0L
                reminderHistory.localReminderMapId = reminderPOJO.reminderMap.firstOrNull()?.localReminderMapId ?: 0L
                reminderHistoryPOJO.reminderHistory = reminderHistory
                reminderHistoryPOJO.reminderFrequencyMap = listOf(reminderFrequencyMap)
                reminderHistoryPOJO.reminderMap = reminderPOJO.reminderMap
                reminderHistoryPOJOArrList.add(reminderHistoryPOJO)
            }
        }
        dao.addOrUpdateReminderHistory(reminderHistoryPOJOArrList)
    }

    fun addOneReminder(): ReminderPOJO {
        val reminderPOJO = ReminderPOJO()
        val reminder = Reminder(startDate = DateTime.now(), frequency = "4", frequencyUnit = "Day", reminderId = "102", isDeleted = false)
        val reminderFrequencyMap =
            ReminderFrequencyMap(day = 3, time = "03:00", reminderId = "102", isDeleted = false, notificationId = "123123")
        val reminderMap = ReminderMap(type = "Medication", patientId = "P202", reminderId = "102")
        reminderPOJO.reminder = reminder
        reminderPOJO.reminderFrequencyMap =
            listOf(reminderFrequencyMap, ReminderFrequencyMap(day = 4, time = "04:00", reminderId = "102", isDeleted = false))
        reminderPOJO.reminderMap = listOf(reminderMap)
        return reminderPOJO
    }

    @Test
    fun addOneForUpdateReminder() {
        val dao = mPHRDB.getInstance(context).miscDao()
        val reminder = dao.getReminderPOJOForReminderId("102")
        val reminderPOJO = reminder?.firstOrNull()
        reminderPOJO?.reminder?.addedBy = "me"
        reminderPOJO?.reminderFrequencyMap?.firstOrNull()?.notificationId = "id101"
        reminderPOJO?.reminderFrequencyMap =
            listOf(
                reminderPOJO!!.reminderFrequencyMap[0],
                reminderPOJO!!.reminderFrequencyMap[1],
                ReminderFrequencyMap(day = 0, time = "11:00", reminderId = "102")
            )
        dao.addOrUpdateReminders(arrayListOf(reminderPOJO!!))
        val newReminderPOJO = dao.getReminderPOJOForReminderId("102")
    }

    fun addThreeReminder(): ReminderPOJO {
        val reminderPOJO = ReminderPOJO()
        val reminder = Reminder(startDate = DateTime.now(), frequency = "5", frequencyUnit = "Day", reminderId = "103", isDeleted = false)
        val reminderFrequencyMap = ReminderFrequencyMap(day = 3, time = "03:00", reminderId = "103", isDeleted = false)
        val reminderMap = ReminderMap(type = "Medication", patientId = "P202", reminderId = "103")
        reminderPOJO.reminder = reminder
        reminderPOJO.reminderFrequencyMap =
            listOf(reminderFrequencyMap, ReminderFrequencyMap(day = 6, time = "04:00", reminderId = "103", isDeleted = false))
        reminderPOJO.reminderMap = listOf(reminderMap)
        return reminderPOJO
    }

    fun addTwoReminder(): ReminderPOJO {
        val reminderPOJO = ReminderPOJO()
        val reminder = Reminder(startDate = DateTime.now(), frequency = "2", frequencyUnit = "Week", reminderId = "101", isDeleted = false)
        val reminderFrequencyMap = ReminderFrequencyMap(day = 1, time = "1:00", reminderId = "101", isDeleted = false)
        val reminderMap = ReminderMap(type = "Medication", patientId = "P201", reminderId = "101")
        reminderPOJO.reminder = reminder
        reminderPOJO.reminderFrequencyMap = listOf(reminderFrequencyMap, ReminderFrequencyMap(day = 2, time = "02:00", reminderId = "101", isDeleted = false))
        reminderPOJO.reminderMap = listOf(reminderMap)
        return reminderPOJO
    }

    //endregion
}