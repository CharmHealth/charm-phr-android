package com.charm.phr.mphr.ui.prescription.add_medication

import androidx.lifecycle.LiveData
import com.charm.phr.mphr.model.DataWithId
import com.charm.phr.mphr.model.MedicationCommonData
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.repository.PrescriptionRepository
import com.charm.phr.mphr.util.MDAddBaseViewModel

class AddMedicationViewModel(private val prescriptionRepository: PrescriptionRepository): MDAddBaseViewModel() {

    var isHealthPassport = false

    var medicationToBeEditted = PatMedPOJO()

    var selectedMedConditionRoute = DataWithId()

    var selectedIntake = DataWithId()
    var selectedIntakeWhen = DataWithId()
    var selectedDosageUnit = DataWithId()




    var medicationCommonData = MedicationCommonData()

    fun getMedicationCommonData(): LiveData<Any> {
        return prescriptionRepository.fetchMedicationCommonData(practiceId)
    }

    fun addMedication(medPOJO: PatMedPOJO): LiveData<Any> {
        return prescriptionRepository.addMedication(medPOJO)
    }

    fun editMedication(medPOJO: PatMedPOJO): LiveData<Any> {
        return prescriptionRepository.editMedication(medPOJO)
    }
}