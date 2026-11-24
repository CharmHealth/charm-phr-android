package com.charm.phr.mphr.ui.vaccine.add_vaccine

import androidx.lifecycle.LiveData
import com.charm.phr.mphr.model.VaccineImmunizationPOJO
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.util.MDAddBaseViewModel

class AddVaccineViewModel (private val wellnessRepository: WellnessRepository):MDAddBaseViewModel() {


    var vaccineToBeEditted = VaccineImmunizationPOJO()


    fun addVaccine(vaccineImmunizationPOJO: VaccineImmunizationPOJO):LiveData<Any>{
        return wellnessRepository.addVaccine(vaccineImmunizationPOJO)
    }

    fun editVaccine(vaccineImmunizationPOJO: VaccineImmunizationPOJO): LiveData<Any> {
        return wellnessRepository.editVaccine(vaccineImmunizationPOJO)
    }

    fun getVaccineCommonData(): LiveData<Any>{
        return wellnessRepository.fetchVaccineListCommonData(practiceId)
    }

}