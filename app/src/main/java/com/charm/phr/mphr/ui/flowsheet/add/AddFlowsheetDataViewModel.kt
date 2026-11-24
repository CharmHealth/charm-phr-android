package com.charm.phr.mphr.ui.flowsheet.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.Flowsheets
import com.charm.phr.mphr.model.PHRFlowsheets
import com.charm.phr.mphr.model.repository.QuestionnaireRepository

class AddFlowsheetDataViewModel(private val questionnaireRepository: QuestionnaireRepository): ViewModel() {

    var flowsheet = Flowsheets()
    var phrFlowsheets: PHRFlowsheets? = null

    var toCheckVitalAlert = true

    fun updateFlowsheet(phrFlowsheet: PHRFlowsheets):LiveData<Any>{

        return if (phrFlowsheets?.phrFlowsheetId.isNullOrBlank()){
            questionnaireRepository.addFlowsheetEntry(phrFlowsheet)
        }
        else{
            questionnaireRepository.editFlowsheetEntry(phrFlowsheet)
        }
    }
}