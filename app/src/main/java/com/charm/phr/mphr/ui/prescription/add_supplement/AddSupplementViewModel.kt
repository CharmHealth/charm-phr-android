package com.charm.phr.mphr.ui.prescription.add_supplement

import androidx.lifecycle.LiveData
import com.charm.phr.mphr.model.DataWithId
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.model.SupplementCommonData
import com.charm.phr.mphr.model.repository.PrescriptionRepository
import com.charm.phr.mphr.util.MDAddBaseViewModel

class AddSupplementViewModel(private val prescriptionRepository: PrescriptionRepository): MDAddBaseViewModel() {

    var supplementToBeEditted = PatientSupplements()


    var selectedIntake = DataWithId()
    var selectedIntakeWhen = DataWithId()
    var selectedDosageUnit = DataWithId()




    var supplementCommonData = SupplementCommonData()

    fun getSupplementCommonData(): LiveData<Any> {
        return prescriptionRepository.fetchSupplementCommonData(practiceId)
    }

    fun addSupplement(patientSupplements: PatientSupplements): LiveData<Any> {
        return prescriptionRepository.addSupplement(patientSupplements)
    }

    fun editSupplement(patientSupplements: PatientSupplements): LiveData<Any> {
        return prescriptionRepository.editSupplement(patientSupplements)
    }
}