package com.charm.phr.mphr.ui.problem_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.PatientMedicalCondition
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.util.ModelUtil

class ProblemListViewModel(private val miscRepository: MiscRepository): ViewModel() {

    var isHealthPassport = false

    var problemListLive =  MediatorLiveData<Any>() //Pair<ArrayList<PatMedPOJO> , Boolean>
    var isProblemListFetched = false
    var problemListPageNo = 1
    var problemListHasMore = true
    var isProblemListFetchComplete = true


    fun getActiveProblemList():LiveData<Any>{

        problemListLive.addSource(miscRepository.getMedConditionList(ModelUtil.problemListStatusActive , isHealthPassport) ){
            problemListLive.postValue(it)
        }

         return problemListLive
    }

    fun deleteProblem(patientMedicalCondition: PatientMedicalCondition): LiveData<Any>{
        return miscRepository.deleteMedicalCondition(patientMedicalCondition)
    }

}