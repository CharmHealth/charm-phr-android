package com.charm.phr.mphr.ui.wellness.wellnessDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.PatientVitalTracker
import com.charm.phr.mphr.model.TrackerMetricMap
import com.charm.phr.mphr.model.repository.WellnessRepository

class WellnessDetailViewModel(private val wellnessRepository: WellnessRepository): ViewModel() {

    var wellnessTracker = ""
    var wellnessMetric = ""

    var trackerId = ""
    var metricId = ""

    var isDefault = false
    var isWellnessDetailFetch = false
    var trackerMetricMap: TrackerMetricMap? = null
    var wellnessGridViewSpan = 0
    var isWellnessDetailResponseSuccess = false
    var isDashboardVitalsUpdated = false

    var wellnessDetailLive = MutableLiveData<ArrayList<PatientVitalTracker>>()


    fun getWellnessDetail(): LiveData<Any>{
        return wellnessRepository.getVitalDetailList(trackerId , metricId)
    }

    fun getMetricDetail(): LiveData<Any>{
        return wellnessRepository.getMetricDetailList(trackerId , metricId)
    }

}