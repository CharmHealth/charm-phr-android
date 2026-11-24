package com.charm.phr.mphr.model.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.HealthMetric
import com.charm.phr.mphr.model.PatientAllergy
import com.charm.phr.mphr.model.PatientVitalTracker
import com.charm.phr.mphr.model.ProceduresMap
import com.charm.phr.mphr.model.VaccineImmunizationPOJO
import com.charm.phr.mphr.util.APIUtil
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.MDCallback
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.checkOnlineAndPatientId
import com.charm.phr.mphr.util.WebService
import com.google.gson.JsonObject
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WellnessRepository @Inject constructor(
    private val webService: WebService,
    private val authManager: AuthManager,
    private val executor: Executor,
    private val appContext: Context,
    private val apiUtil: APIUtil,
    private val sharedPreferences: SharedPreferences
) {

    //region Allergy

    fun fetchPatAllergiesList(allergyStatus: String, pageNo: Int, isHealthPassport: Boolean = false): LiveData<Any> {

        val patAllergiesListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val fromRange: Int = ((pageNo - 1) * ModelUtil.listPerPageCount) + 1


                val practiceList = if (isHealthPassport) {
                    arrayListOf("")
                } else {
                    ModelUtil.getEnabledPractices(authManager, ModelUtil.allergiesModelId)
                }
                val practiceSize = practiceList.size

                var respCount = 0

                var isCallbackSent = false

                val allergyList = arrayListOf<PatientAllergy>()

                practiceList.forEach { practiceId ->

                    val patAllergiesListCall = webService.getPatAllergyList(patientId, practiceId, allergyStatus, fromRange)

                    apiUtil.initiateRequest(patAllergiesListCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {

                            respCount++
                            val patAllergyList = MDParsingHelper.formPatientAllergyList(respObj, allergyStatus)

                            patAllergyList.forEach {
                                it.practiceId = practiceId
                                it.isHealthPassport = isHealthPassport
                            }

                            allergyList.addAll(patAllergyList)

                            if (!isCallbackSent && respCount == practiceSize) {

                                val finalAllergyList = arrayListOf<PatientAllergy>()


                                allergyList.forEach { allergy ->

                                    if (!allergy.addedByPatient) {
                                        finalAllergyList.add(allergy)
                                    } else {
                                        if (!finalAllergyList.any { it.allergyMapId == allergy.allergyMapId }) {
                                            finalAllergyList.add(allergy)
                                        }
                                    }
                                }

                                patAllergiesListLive.postValue(Pair(finalAllergyList, false))
                            }
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            respCount++
                            if (!isCallbackSent) {
                                patAllergiesListLive.postValue(error)
                                isCallbackSent = true
                            }
                        }
                    })
                }


            } else {
                patAllergiesListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            patAllergiesListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return patAllergiesListLive
    }


    fun getPatAllergyCommonData(practiceId: String): LiveData<Any> {
        val patAllergiesCommonDataLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val patAllergiesCommonDataCall = webService.getCommonData(ModelUtil.modulelNameAllergy, patientId, practiceId)

                apiUtil.initiateRequest(patAllergiesCommonDataCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {

                        val patAllergyCommonData = MDParsingHelper.formPatientAllergyCommonData(respObj)


                        patAllergiesCommonDataLive.postValue(patAllergyCommonData)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        patAllergiesCommonDataLive.postValue(error)
                    }
                })
            } else {
                patAllergiesCommonDataLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            patAllergiesCommonDataLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return patAllergiesCommonDataLive
    }

    fun addPatAllergy(patientAllergy: PatientAllergy): LiveData<Any> {
        val addPatAllergyLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                patientAllergy.practicePatientId = patientId

                val postObj = MDJsonBuilder.formAddPatAllergyObj(patientAllergy)

                val addPatAllergyCall = webService.addPatAllergy(postObj)

                apiUtil.initiateRequest(addPatAllergyCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        addPatAllergyLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        addPatAllergyLive.postValue(error)
                    }
                })
            } else {
                addPatAllergyLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            addPatAllergyLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return addPatAllergyLive
    }


    fun editPatAllergy(patientAllergy: PatientAllergy): LiveData<Any> {
        val editPatAllergyLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                patientAllergy.practicePatientId = patientId

                val postObj = MDJsonBuilder.formEditPatAllergyObj(patientAllergy)

                val editPatAllergyCall = webService.editPatAllergy(postObj)

                apiUtil.initiateRequest(editPatAllergyCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        editPatAllergyLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        editPatAllergyLive.postValue(error)
                    }
                })
            } else {
                editPatAllergyLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            editPatAllergyLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return editPatAllergyLive
    }


    fun deletePatAllergy(patientAllergy: PatientAllergy): LiveData<Any> {
        val deletePatAllergyLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                patientAllergy.practicePatientId = patientId

                val postObj = MDJsonBuilder.formDeletePatAllergyObj(patientAllergy)

                val deletePatAllergyCall = webService.deletePatAllergy(postObj)

                apiUtil.initiateRequest(deletePatAllergyCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        deletePatAllergyLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        deletePatAllergyLive.postValue(error)
                    }
                })
            } else {
                deletePatAllergyLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            deletePatAllergyLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return deletePatAllergyLive
    }

    //endregion

    //region Procedure

    fun fetchProcedureList(pageNo: Int, isHealthPassport: Boolean): LiveData<Any> {
        val fetchProcedureLive = MutableLiveData<Any>()


        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val fromRange: Int = ((pageNo - 1) * ModelUtil.listPerPageCount) + 1

                val fetchProcedureCall = webService.getProcedures(patientId)

                apiUtil.initiateRequest(fetchProcedureCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {

                        val procedureList = MDParsingHelper.formProcedureList(respObj, isHealthPassport)
                        val hasMoreProcedure = procedureList.size == ModelUtil.listPerPageCount

                        fetchProcedureLive.postValue(Pair(procedureList, false))
                    }

                    override fun onMDFailure(error: APIResponseError) {

                        fetchProcedureLive.postValue(error)
                    }

                })
            } else {
                fetchProcedureLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            fetchProcedureLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return fetchProcedureLive
    }

    fun getProcedureCommonData(practiceId: String): LiveData<Any> {
        val procedureCommonDataLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {


                val procedureCommonDataCall = webService.getCommonData(ModelUtil.moduleNameProcedure, patientId, practiceId)
                apiUtil.initiateRequest(procedureCommonDataCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        val procedureCommonData = MDParsingHelper.formProcedureCommonData(respObj)
                        procedureCommonDataLive.postValue(procedureCommonData)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        procedureCommonDataLive.postValue(error)
                    }
                })
            } else {
                procedureCommonDataLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            procedureCommonDataLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return procedureCommonDataLive
    }

    fun addProcedure(procedure: ProceduresMap): LiveData<Any> {
        val addProcedureLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                procedure.practicePatientId = patientId

                val postObj = MDJsonBuilder.formAddProcedureObj(procedure)

                val addProcedureCall = webService.addProcedure(postObj)

                apiUtil.initiateRequest(addProcedureCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        addProcedureLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        addProcedureLive.postValue(error)
                    }
                })
            } else {
                addProcedureLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            addProcedureLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return addProcedureLive
    }


    fun editProcedure(procedure: ProceduresMap): LiveData<Any> {
        val editProcedureLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                procedure.practicePatientId = patientId

                val postObj = MDJsonBuilder.formEditProcedureObj(procedure)

                val editProcedureCall = webService.editProcedure(postObj)

                apiUtil.initiateRequest(editProcedureCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        editProcedureLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        editProcedureLive.postValue(error)
                    }
                })
            } else {
                editProcedureLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            editProcedureLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return editProcedureLive
    }

    fun deleteProcedure(procedure: ProceduresMap): LiveData<Any> {
        val deleteProcedureLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                procedure.practicePatientId = patientId

                val postObj = MDJsonBuilder.formDeleteProcedureObj(procedure)

                val deleteProcedureCall = webService.deleteProcedure(postObj)

                apiUtil.initiateRequest(deleteProcedureCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        deleteProcedureLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        deleteProcedureLive.postValue(error)
                    }
                })
            } else {
                deleteProcedureLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            deleteProcedureLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return deleteProcedureLive
    }

    //endregion

    //region Vaccine
    fun fetchVaccineList(pageNo: Int): LiveData<Any> {
        val vaccineListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val fromRange: Int = ((pageNo - 1) * ModelUtil.listPerPageCount) + 1

                val practiceList = ModelUtil.getEnabledPractices(authManager, ModelUtil.vaccineModelId)
                val practiceSize = practiceList.size

                val patientVaccineList = arrayListOf<VaccineImmunizationPOJO>()
                var respCount = 0

                var isCallbackSent = false

                practiceList.forEach { practiceId ->

                    val vaccineListCall = webService.getVaccineList(patientId, practiceId)

                    apiUtil.initiateRequest(vaccineListCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {

                            respCount++
                            val vaccineList = MDParsingHelper.formVaccineList(respObj)

                            vaccineList.forEach {
                                it.practiceId = practiceId
                            }

                            patientVaccineList.addAll(vaccineList)

                            if (respCount == practiceSize && !isCallbackSent) {
                                val finalVaccineList = arrayListOf<VaccineImmunizationPOJO>()


                                patientVaccineList.forEach { vaccine ->

                                    if (!vaccine.addedByPatient) {
                                        finalVaccineList.add(vaccine)
                                    } else {
                                        if (!finalVaccineList.any { it.patientImmunizationMap?.patientImmunizationHistory?.immunizationHistoryId == vaccine.patientImmunizationMap?.patientImmunizationHistory?.immunizationHistoryId }) {
                                            finalVaccineList.add(vaccine)
                                        }
                                    }
                                }

                                finalVaccineList.sortByDescending {
                                    if (it.addedByPatient) {
                                        it.patientImmunizationMap?.patientImmunizationHistory?.vaccinesOn?.millis ?: 0
                                    } else {
                                        it.patientVaccineMap?.dateOfVaccination?.millis ?: 0
                                    }
                                }

                                vaccineListLive.postValue(Pair(finalVaccineList, false))
                            }
                        }

                        override fun onMDFailure(error: APIResponseError) {
                            respCount++
                            if (!isCallbackSent) {
                                isCallbackSent = true
                                vaccineListLive.postValue(error)
                            }
                        }
                    })
                }


            } else {
                vaccineListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            vaccineListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return vaccineListLive
    }


    fun fetchVaccineListCommonData(practiceId: String): LiveData<Any> {

        val vaccineListCommonDataLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val vaccineListCommonDataCall = webService.getCommonData(ModelUtil.moduleNameVaccine, patientId, practiceId)
                apiUtil.initiateRequest(vaccineListCommonDataCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        vaccineListCommonDataLive.postValue(MDParsingHelper.formVaccineCommonData(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        vaccineListCommonDataLive.postValue(error)
                    }

                })
            } else {
                vaccineListCommonDataLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            vaccineListCommonDataLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return vaccineListCommonDataLive
    }


    fun addVaccine(vaccineImmunizationPOJO: VaccineImmunizationPOJO): LiveData<Any> {

        val addVaccineLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                vaccineImmunizationPOJO.patientImmunizationMap?.patientId = patientId

                val postObj = MDJsonBuilder.formAddVaccineobj(vaccineImmunizationPOJO)

                val addVaccineCall = webService.addVaccine(postObj)

                apiUtil.initiateRequest(addVaccineCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        addVaccineLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        addVaccineLive.postValue(error)
                    }

                })

            } else {
                addVaccineLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            addVaccineLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return addVaccineLive
    }

    fun editVaccine(vaccineImmunizationPOJO: VaccineImmunizationPOJO): LiveData<Any> {

        val editVaccineLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                vaccineImmunizationPOJO.patientImmunizationMap?.patientId = patientId

                val postObj = MDJsonBuilder.formEditVaccineobj(vaccineImmunizationPOJO)

                val editVaccineCall = webService.editVaccine(postObj)

                apiUtil.initiateRequest(editVaccineCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        editVaccineLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        editVaccineLive.postValue(error)
                    }

                })

            } else {
                editVaccineLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            editVaccineLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return editVaccineLive
    }

    fun deleteVaccine(vaccineImmunizationPOJO: VaccineImmunizationPOJO): LiveData<Any> {

        val deleteVaccineLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                vaccineImmunizationPOJO.patientImmunizationMap?.patientId = patientId

                val postObj = MDJsonBuilder.formDeleteVaccineobj(vaccineImmunizationPOJO)


                val deleteVaccineCall = webService.deleteVaccine(postObj)

                apiUtil.initiateRequest(deleteVaccineCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        deleteVaccineLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        deleteVaccineLive.postValue(error)
                    }

                })

            } else {
                deleteVaccineLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            deleteVaccineLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return deleteVaccineLive
    }


    //endregion


    //region Vitals

    fun getVitalsForDashboard(): LiveData<Any> {
        val vitalsDashboardLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val vitalsDashboardCall = webService.getLatestVitals(patientId)

                apiUtil.initiateRequest(vitalsDashboardCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        vitalsDashboardLive.postValue(MDParsingHelper.getVitalsForDashboard(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        vitalsDashboardLive.postValue(error)
                    }
                })

            } else {
                vitalsDashboardLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            vitalsDashboardLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return vitalsDashboardLive
    }


    fun getVitalTrackerList(): LiveData<Any> {
        val vitalTrackerListLive = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                apiUtil.initiateRequest(webService.getVitalTrackers(patientId), object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        vitalTrackerListLive.postValue(Pair(MDParsingHelper.getVitalTrackerList(respObj), respObj.toString()))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        vitalTrackerListLive.postValue(error)
                    }
                })
            } else {
                vitalTrackerListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            vitalTrackerListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return vitalTrackerListLive
    }

    fun getVitalUnitType(): LiveData<Any> {
        val vitalUnitTypeLive = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {
                apiUtil.initiateRequest(webService.getVitalUnitType(patientId), object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {

                        val unitSystem = MDParsingHelper.getVitalUnitType(respObj)
                        authManager.patientList.forEach {
                            if (it.practicePatientId == patientId) {
                                it.unitSystem = unitSystem
                            }
                        }

                        val prefEdit = sharedPreferences.edit()
                        prefEdit.putString(ModelUtil.patientList, MDJsonBuilder.formPatientList(authManager.patientList))
                        prefEdit.commit()

                        vitalUnitTypeLive.postValue(unitSystem)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        vitalUnitTypeLive.postValue(error)
                    }
                })
            } else {
                vitalUnitTypeLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            vitalUnitTypeLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return vitalUnitTypeLive

    }

    fun updateVitalUnitType(unitSystem: String): LiveData<Any> {
        val vitalUnitTypeLive = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val postObj = MDJsonBuilder.formUpdateVitalUnitObj(unitSystem, patientId)
                apiUtil.initiateRequest(webService.updateVitalUnitType(postObj), object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {

                        authManager.patientList.forEach {
                            if (it.practicePatientId == patientId) {
                                it.unitSystem = unitSystem
                            }
                        }

                        val prefEdit = sharedPreferences.edit()
                        prefEdit.putString(ModelUtil.patientList, MDJsonBuilder.formPatientList(authManager.patientList))
                        prefEdit.commit()

                        vitalUnitTypeLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        vitalUnitTypeLive.postValue(error)
                    }
                })
            } else {
                vitalUnitTypeLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            vitalUnitTypeLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return vitalUnitTypeLive

    }

    fun getVitalDetailList(trackerId: String, metric: String): LiveData<Any> {

        val vitalDetailLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val vitalDetailCall = webService.getVitalDetailList(patientId, trackerId)
                apiUtil.initiateRequest(vitalDetailCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        vitalDetailLive.postValue(MDParsingHelper.getVitalDetailList(respObj, trackerId, metric))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        vitalDetailLive.postValue(error)
                    }
                })
            } else {
                vitalDetailLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            vitalDetailLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return vitalDetailLive
    }

    fun getMetricDetailList(trackerId: String, metricId: String): LiveData<Any> {

        val vitalDetailLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val vitalDetailCall = webService.getMetricDetailList(patientId, metricId)
                apiUtil.initiateRequest(vitalDetailCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        vitalDetailLive.postValue(MDParsingHelper.getMetricDetailList(respObj, trackerId, metricId))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        vitalDetailLive.postValue(error)
                    }
                })
            } else {
                vitalDetailLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            vitalDetailLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return vitalDetailLive
    }

    fun getHealthMetricUnitList(): LiveData<Any> {
        val healthMetricUnitLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {


                val healthMetricUnitCall = webService.getHealthMetricUnitList(patientId)
                apiUtil.initiateRequest(healthMetricUnitCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        healthMetricUnitLive.postValue(MDParsingHelper.getHealthMetricUnitList(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        healthMetricUnitLive.postValue(error)
                    }
                })
            } else {
                healthMetricUnitLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            healthMetricUnitLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return healthMetricUnitLive
    }

    fun addVitals(patientVitalTracker: PatientVitalTracker): LiveData<Any> {
        val addVitalsLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                patientVitalTracker.patientId = patientId

                val postObj = MDJsonBuilder.formAddVitalObj(patientVitalTracker)

                val addVitalsCall = webService.addVital(postObj)
                apiUtil.initiateRequest(addVitalsCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        addVitalsLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        addVitalsLive.postValue(error)
                    }
                })
            } else {
                addVitalsLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            addVitalsLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return addVitalsLive
    }

    fun addMultipleVitals(patientVitalTrackerList: ArrayList<PatientVitalTracker>): LiveData<Any> {
        val addVitalsLive = MutableLiveData<Any>()
        addVitalsLive.checkOnlineAndPatientId(authManager, appContext) { patientId, selectedPatient ->

            val postObj = MDJsonBuilder.formAddMultipleVitalObj(patientVitalTrackerList, patientId)

            val addVitalsCall = webService.addVital(postObj)
            apiUtil.initiateRequest(addVitalsCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    addVitalsLive.postValue(true)
                }

                override fun onMDFailure(error: APIResponseError) {
                    addVitalsLive.postValue(error)
                }
            })
        }
        return addVitalsLive
    }

    fun addHealthMetric(healthMetric: HealthMetric): LiveData<Any> {
        val addHealthMetricLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                healthMetric.patientId = patientId

                val postObj = MDJsonBuilder.formHealthMetricObj(healthMetric)

                val addHealthMetricCall = webService.addHealthMetric(postObj)
                apiUtil.initiateRequest(addHealthMetricCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        addHealthMetricLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        addHealthMetricLive.postValue(error)
                    }
                })
            } else {
                addHealthMetricLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            addHealthMetricLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return addHealthMetricLive
    }

    fun updateHealthMetric(healthMetric: HealthMetric): LiveData<Any> {
        val updateHealthMetricLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                healthMetric.patientId = patientId

                val postObj = MDJsonBuilder.formHealthMetricObj(healthMetric)

                val addHealthMetricCall = webService.updateHealthMetric(postObj)
                apiUtil.initiateRequest(addHealthMetricCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        updateHealthMetricLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        updateHealthMetricLive.postValue(error)
                    }
                })
            } else {
                updateHealthMetricLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            updateHealthMetricLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return updateHealthMetricLive
    }

    fun deleteHealthMetric(healthMetric: HealthMetric): LiveData<Any> {
        val deleteHealthMetric = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                healthMetric.patientId = patientId

                val postObj = MDJsonBuilder.formDeleteHealthMetricObj(healthMetric)

                val deleteHealthMetricCall = webService.deleteHealthMetric(postObj)
                apiUtil.initiateRequest(deleteHealthMetricCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        deleteHealthMetric.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        deleteHealthMetric.postValue(error)
                    }
                })
            } else {
                deleteHealthMetric.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            deleteHealthMetric.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return deleteHealthMetric
    }

    fun getHealthMetricList(): LiveData<Any> {
        val getHealthMetricLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val addHealthMetricCall = webService.getHealthMetricList(patientId)
                apiUtil.initiateRequest(addHealthMetricCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        getHealthMetricLive.postValue(MDParsingHelper.getHealthMetricList(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        getHealthMetricLive.postValue(error)
                    }
                })
            } else {
                getHealthMetricLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            getHealthMetricLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return getHealthMetricLive
    }

    fun getVitalsCommonData(practiceId: String): LiveData<Any> {
        val getVitalsCommonDataLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val addHealthMetricCall = webService.getCommonData(ModelUtil.moduleNameWellness, patientId, practiceId)
                apiUtil.initiateRequest(addHealthMetricCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        getVitalsCommonDataLive.postValue(MDParsingHelper.getWellnessCommonData(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        getVitalsCommonDataLive.postValue(error)
                    }
                })
            } else {
                getVitalsCommonDataLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            getVitalsCommonDataLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return getVitalsCommonDataLive
    }

    //endregion
}


