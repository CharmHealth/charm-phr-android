package com.charm.phr.mphr.ui.reminder.add_reminder

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.database.entity.Medications
import com.charm.phr.mphr.model.database.entity.Supplements
import com.charm.phr.mphr.model.repository.ReminderRepository
import org.joda.time.DateTime

class AddReminderViewModel(private val reminderRepository: ReminderRepository) : ViewModel() {

    var reminderPOJO = ReminderPOJO()

    var reminderTimingMap = hashMapOf<Int, ArrayList<DateTime?>>()

    var selectedWeekDay = arrayListOf(false, false, false, false, false, false, false)

    var isDataEdited=false

    var dosageName=""

    var drugDirection=""

    var startDate:String?=""

    var endDate:String?=""

    var medicationObj: PatMedPOJO=PatMedPOJO()

    var supplementObj: PatientSupplements=PatientSupplements()

    fun addReminder(): LiveData<Any> {
        return reminderRepository.addReminder(reminderPOJO)
    }

    fun updateReminder(): LiveData<Any> {
        return reminderRepository.updateReminder(reminderPOJO)
    }

    fun deleteReminder(): LiveData<Any> {
        return reminderRepository.deleteReminder(
            reminderPOJO.reminder?.reminderId,
            reminderPOJO.reminderMap.firstOrNull()?.moduleId ?: "",
            reminderPOJO.reminderMap.firstOrNull()?.practiceId ?: ""
        )
    }

    fun addOrUpdateMedicationsInLocalDb(medicationsArrList: ArrayList<Medications>):ArrayList<Medications> {
        return reminderRepository.addOrUpdateMedicationsInLocalDb(medicationsArrList)
    }

    fun addOrUpdateSupplementsInLocalDb(supplementsArrList: ArrayList<Supplements>): ArrayList<Supplements> {
        return reminderRepository.addOrUpdateSupplementsInLocalDb(supplementsArrList)
    }

}