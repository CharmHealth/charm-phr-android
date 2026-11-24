package com.charm.phr.mphr.ui.wellness.addWellness

import androidx.lifecycle.LiveData
import com.charm.phr.mphr.model.PatientVitalTracker
import com.charm.phr.mphr.model.TrackerMetricMap
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.util.MDAddBaseViewModel
import org.joda.time.DateTime

class AddWellnessViewModel(private val wellnessRepository: WellnessRepository): MDAddBaseViewModel() {

    var trackerMetricMap: TrackerMetricMap? = null
    var toCheckAlert = true

    var minDate: DateTime = DateTime.now()
    var maxDate: DateTime = DateTime.now()

    fun addTracker(patientVitalTracker: PatientVitalTracker):LiveData<Any>{
        return wellnessRepository.addVitals(patientVitalTracker)
    }


    fun updateWellnessUnit(unitSystem: String): LiveData<Any>{
        return wellnessRepository.updateVitalUnitType(unitSystem)
    }

    fun getWellnessUnit(): LiveData<Any>{
        return wellnessRepository.getVitalUnitType()
    }

   /* fun getCommonData(): LiveData<Any>{
        return wellnessRepository.getVitalsCommonData(practiceId)
    }*/
}