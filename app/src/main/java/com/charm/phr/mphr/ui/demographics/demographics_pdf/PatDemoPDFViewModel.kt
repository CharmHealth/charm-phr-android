package com.charm.phr.mphr.ui.demographics.demographics_pdf

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.repository.PatientRepository

class PatDemoPDFViewModel(private val patientRepository: PatientRepository): ViewModel() {

    var isHealthCard = false

    var isPDFDownLoaded = false

    var filePath: String? = null

    fun fetchPDF():LiveData<Any>{
        return if (isHealthCard){
            patientRepository.getHealthCardPDF()
        }
        else{
            patientRepository.getPatDemoPDF()
        }
    }
}