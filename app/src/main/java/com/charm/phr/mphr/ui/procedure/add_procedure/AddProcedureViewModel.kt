package com.charm.phr.mphr.ui.procedure.add_procedure

import androidx.lifecycle.LiveData
import com.charm.phr.mphr.model.ProceduresMap
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.util.MDAddBaseViewModel

class AddProcedureViewModel (private val wellnessRepository: WellnessRepository): MDAddBaseViewModel(){

    var procedureToBeEditted = ProceduresMap()

    var isHealthPassport = false

    fun getProcedureCommonData():LiveData<Any>{
        return wellnessRepository.getProcedureCommonData(practiceId)
    }

    fun addProcedure(proceduresMap: ProceduresMap):LiveData<Any>{
        return wellnessRepository.addProcedure(proceduresMap)
    }

    fun editProcedure(proceduresMap: ProceduresMap):LiveData<Any>{
        return wellnessRepository.editProcedure(proceduresMap)
    }
}