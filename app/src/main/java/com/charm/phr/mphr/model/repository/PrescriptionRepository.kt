package com.charm.phr.mphr.model.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.model.database.mPHRDB
import com.charm.phr.mphr.util.APIUtil
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.MDCallback
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.WebService
import com.google.gson.JsonObject
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrescriptionRepository @Inject constructor(
    private val webService: WebService,
    private val authManager: AuthManager,
    private val executor: Executor,
    private val appContext: Context,
    private val apiUtil: APIUtil,
    private val db: mPHRDB,
    private val reminderRepository: ReminderRepository
) {

    fun getMedications(viewType: String, pageNo: Int = 0, isFromReminder: Boolean = false): LiveData<Any> {
        val medicationLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                var fromRange: Int = ((pageNo - 1) * ModelUtil.listPerPageCount) + 1

                if (fromRange < 1) {
                    fromRange = 1
                }

                val viewTypeStatus = if (viewType == ModelUtil.medicationViewTypeAll) {
                    ""
                } else {
                    viewType
                }

                val practiceList = ModelUtil.getEnabledPractices(authManager, ModelUtil.medicationModelId)

                val practiceSize = practiceList.size

                var respCount = 0

                var isCallbackSent = false

                val medicationList = arrayListOf<PatMedPOJO>()


                practiceList.forEach { practiceId ->
                    val medicationCall = webService.getPrescription(patientId, practiceId, viewTypeStatus, fromRange)

                    apiUtil.initiateRequest(medicationCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {
                            respCount++
                            medicationList.addAll(
                                MDParsingHelper.formMedicationList(
                                    respObj,
                                    patientId,
                                    isFromReminder,
                                    practiceId,
                                    viewTypeStatus,
                                    respCount
                                )
                            )
                            if (respCount == practiceSize && !isCallbackSent) {
                                // medicationList.sortByDescending { it.patMed?.startDate?.millis ?: 0 }
//                                val medicationsList = MDParsingHelper.formMedicationListForDB(finalMedicationList, appContext)
//                                db.miscDao().addOrUpdateMedications(medicationsList, viewType, patientId, practiceId)
                                medicationLive.postValue(Pair(medicationList, false))
                                isCallbackSent = true
                            }

                        }

                        override fun onMDFailure(error: APIResponseError) {
                            respCount++
                            if (!isCallbackSent) {
                                medicationLive.postValue(error)
                                isCallbackSent = true
                            }
                        }
                    })
                }
            } else {
                medicationLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            medicationLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return medicationLive
    }

    fun getMedicationsSynchronize(viewType: String, pageNo: Int = 0, isHealthPassport: Boolean, patient: Patient, isFromReminder: Boolean = false): Any? {
        if (authManager.isUserOnline()) {
            val patientId = patient.practicePatientId
            if (patientId?.isNotBlank() == true) {

                var fromRange: Int = ((pageNo - 1) * ModelUtil.listPerPageCount) + 1

                if (fromRange < 1) {
                    fromRange = 1
                }

                val viewTypeStatus = if (viewType == ModelUtil.medicationViewTypeAll) {
                    ""
                } else {
                    viewType
                }

                val practiceList = if (isHealthPassport) {
                    arrayListOf("")
                } else {
                    ModelUtil.getEnabledPractices(authManager, ModelUtil.medicationModelId, patient)
                }

                val practiceSize = practiceList.size

                var respCount = 0

                val medicationList = arrayListOf<PatMedPOJO>()


                practiceList.forEach { practiceId ->
                    val medicationCall = webService.getPrescription(patientId, practiceId, viewTypeStatus, fromRange)
                    val resp = authManager.synchronizeAPICall(medicationCall)
                    if (resp is JsonObject) {
                        respCount++
                        medicationList.addAll(
                            MDParsingHelper.formMedicationList(
                                resp,
                                patientId,
                                isFromReminder,
                                practiceId,
                                viewTypeStatus,
                                respCount
                            )
                        )
                        if (respCount == practiceSize) {
                            return Pair(medicationList, false)
                        }
                    } else {
                        return resp
                    }

                }
            } else {
                return APIResponseError(null, null, appContext.getString(R.string.error_patient_id_not_found))
            }
        } else {
            return APIResponseError(ModelUtil.errorInternetConnectionCode, null, appContext.getString(R.string.internet_connection_error))
        }
        return null
    }

    fun refillMedication(medication: PatMedPOJO): LiveData<Any> {
        val refillMedicationLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val practiceId = medication.practiceId

                medication.patMed?.practiceId = practiceId

                val postObj = MDJsonBuilder.formRefillMedicationObj(medication, authManager.selectedPatientLive.value!!)

                val medicationCall = webService.refillPrescriptionn(patientId, postObj)

                apiUtil.initiateRequest(medicationCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        refillMedicationLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        refillMedicationLive.postValue(error)
                    }
                })
            } else {
                refillMedicationLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            refillMedicationLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return refillMedicationLive
    }

    fun fetchMedicationCommonData(practiceId: String): LiveData<Any> {

        val medicationCommonDataLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val medicationCommonDataCall = webService.getCommonData(ModelUtil.moduleNameMedication, patientId, practiceId)
                apiUtil.initiateRequest(medicationCommonDataCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        medicationCommonDataLive.postValue(MDParsingHelper.formMedicationCommonData(respObj, appContext))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        medicationCommonDataLive.postValue(error)
                    }

                })
            } else {
                medicationCommonDataLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            medicationCommonDataLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return medicationCommonDataLive
    }

    fun addMedication(patMedPOJO: PatMedPOJO): LiveData<Any> {

        val addMedicationLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                patMedPOJO.patMedPHRExt.firstOrNull()?.patientId = patientId

                val postObj = MDJsonBuilder.formAddMedicationObj(patMedPOJO)

                Log.d("MDJsonFormation", postObj.toString())

                val addMedicationCall = webService.addMedication(patientId, postObj)

                apiUtil.initiateRequest(addMedicationCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        addMedicationLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        addMedicationLive.postValue(error)
                    }

                })

            } else {
                addMedicationLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            addMedicationLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return addMedicationLive
    }

    fun editMedication(patMedPOJO: PatMedPOJO): LiveData<Any> {

        val editMedicationLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                patMedPOJO.patMedPHRExt.firstOrNull()?.patientId = patientId

                val postObj = MDJsonBuilder.formEditMedicationObj(patMedPOJO)

                Log.d("MDJsonFormation", postObj.toString())

                val editMedicationCall = webService.editMedication(patientId, postObj)

                apiUtil.initiateRequest(editMedicationCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        editMedicationLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        editMedicationLive.postValue(error)
                    }

                })

            } else {
                editMedicationLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            editMedicationLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return editMedicationLive
    }


    fun deleteMedication(patMedPOJO: PatMedPOJO): LiveData<Any> {

        val editMedicationLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                patMedPOJO.patMedPHRExt.firstOrNull()?.patientId = patientId

                val postObj = MDJsonBuilder.formDeleteMedicationObj(patMedPOJO)

                Log.d("MDJsonFormation", postObj.toString())

                val editMedicationCall = webService.deleteMedication(patientId, postObj)

                apiUtil.initiateRequest(editMedicationCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        patMedPOJO.reminders.forEach { reminder ->
                            if (reminder.reminder?.isDeleted == false) {
                                val reminderPOJO = reminderRepository.getReminderPOJOForReminderId(reminder.reminder?.reminderId ?: "")
                                reminderRepository.deleteReminderFromLocalDB(reminderPOJO?.firstOrNull())
                            }
                        }
                        editMedicationLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        editMedicationLive.postValue(error)
                    }

                })

            } else {
                editMedicationLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            editMedicationLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return editMedicationLive
    }


    fun getSupplements(viewType: String, pageNo: Int, isFromReminder: Boolean = false): LiveData<Any> {
        val medicationLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val fromRange: Int = ((pageNo - 1) * ModelUtil.listPerPageCount) + 1

                val viewTypeStatus = if (viewType == ModelUtil.medicationViewTypeAll) {
                    ""
                } else {
                    viewType
                }

                val practiceList = ModelUtil.getEnabledPractices(authManager, ModelUtil.supplementModelId)
                val practiceSize = practiceList.size

                var respCount = 0

                var isCallbackSent = false

                val supplementList = arrayListOf<PatientSupplements>()


                practiceList.forEach { practiceId ->

                    val supplementCall = webService.getSupplementList(patientId, practiceId, viewTypeStatus, fromRange)

                    apiUtil.initiateRequest(supplementCall, object : MDCallback(authManager) {
                        override fun onMDSuccess(respObj: JsonObject) {
                            respCount++

                            supplementList.addAll(MDParsingHelper.formSupplementList(respObj, patientId, isFromReminder, practiceId, respCount))

                            if (respCount == practiceSize && !isCallbackSent) {

//                                val supplementsToDBList = MDParsingHelper.formSupplementListForDB(finalSupplementList)
//                                db.miscDao().addOrUpdateSupplements(supplementsToDBList, viewType, patientId, practiceId)

                                medicationLive.postValue(Pair(supplementList, false))
                                isCallbackSent = true
                            }


                        }

                        override fun onMDFailure(error: APIResponseError) {
                            respCount++
                            if (!isCallbackSent) {
                                medicationLive.postValue(error)
                                isCallbackSent = true
                            }
                        }
                    })
                }


            } else {
                medicationLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            medicationLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return medicationLive
    }

    fun getSupplementsSynchronize(viewType: String, pageNo: Int, patient: Patient, isFromReminder: Boolean = false): Any? {
        if (authManager.isUserOnline()) {
            val patientId = patient.practicePatientId
            if (patientId?.isNotBlank() == true) {

                val fromRange: Int = ((pageNo - 1) * ModelUtil.listPerPageCount) + 1

                val viewTypeStatus = if (viewType == ModelUtil.medicationViewTypeAll) {
                    ""
                } else {
                    viewType
                }

                val practiceList = ModelUtil.getEnabledPractices(authManager, ModelUtil.supplementModelId, patient)
                val practiceSize = practiceList.size

                var respCount = 0


                val supplementList = arrayListOf<PatientSupplements>()


                practiceList.forEach { practiceId ->

                    val supplementCall = webService.getSupplementList(patientId, practiceId, viewTypeStatus, fromRange)
                    val respObj = authManager.synchronizeAPICall(supplementCall)
                    Log.d("MDCallbackResponse", respObj.toString())
                    if (respObj is JsonObject) {
                        respCount++
                        supplementList.addAll(MDParsingHelper.formSupplementList(respObj, patientId, isFromReminder, practiceId, respCount))

                        if (respCount == practiceSize) {
                            return Pair(supplementList, false)
                        }
                    } else {
                        return respObj
                    }
                }


            } else {
                return APIResponseError(null, null, appContext.getString(R.string.error_patient_id_not_found))
            }
        } else {
            return APIResponseError(ModelUtil.errorInternetConnectionCode, null, appContext.getString(R.string.internet_connection_error))
        }
        return null
    }


    fun fetchSupplementCommonData(practiceId: String): LiveData<Any> {

        val supplementCommonDataLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val supplementCommonDataCall = webService.getCommonData(ModelUtil.moduleNameSupplement, patientId, practiceId)
                apiUtil.initiateRequest(supplementCommonDataCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        supplementCommonDataLive.postValue(MDParsingHelper.formSupplementCommonData(respObj, appContext))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        supplementCommonDataLive.postValue(error)
                    }

                })
            } else {
                supplementCommonDataLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            supplementCommonDataLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return supplementCommonDataLive
    }

    fun addSupplement(patientSupplements: PatientSupplements): LiveData<Any> {

        val addSupplementLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                patientSupplements.practicePatientId = patientId

                val postObj = MDJsonBuilder.formAddSupplementObj(patientSupplements)

                Log.d("MDJsonFormation", postObj.toString())

                val addSupplementCall = webService.addSupplement(patientId, postObj)

                apiUtil.initiateRequest(addSupplementCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        addSupplementLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        addSupplementLive.postValue(error)
                    }

                })

            } else {
                addSupplementLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            addSupplementLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return addSupplementLive
    }

    fun editSupplement(patientSupplements: PatientSupplements): LiveData<Any> {

        val editSupplementLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                patientSupplements.practicePatientId = patientId

                val postObj = MDJsonBuilder.formEditSupplementObj(patientSupplements)

                Log.d("MDJsonFormation", postObj.toString())

                val editSupplementCall = webService.editSupplement(patientId, postObj)

                apiUtil.initiateRequest(editSupplementCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        editSupplementLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        editSupplementLive.postValue(error)
                    }

                })

            } else {
                editSupplementLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            editSupplementLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return editSupplementLive
    }

    fun deleteSupplement(patientSupplements: PatientSupplements): LiveData<Any> {

        val editSupplementLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                patientSupplements.practicePatientId = patientId

                val postObj = MDJsonBuilder.formDeleteSupplementObj(patientSupplements)

                Log.d("MDJsonFormation", postObj.toString())

                val editSupplementCall = webService.deleteSupplement(patientId, postObj)

                apiUtil.initiateRequest(editSupplementCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        patientSupplements.reminders.forEach { reminder ->
                            if (reminder.reminder?.isDeleted == false) {
                                val reminderPOJO = reminderRepository.getReminderPOJOForReminderId(reminder.reminder?.reminderId ?: "")
                                reminderRepository.deleteReminderFromLocalDB(reminderPOJO?.firstOrNull())
                            }
                        }
                        editSupplementLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        editSupplementLive.postValue(error)
                    }

                })

            } else {
                editSupplementLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            editSupplementLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return editSupplementLive
    }

    fun refillSupplements(patientSupplements: PatientSupplements): LiveData<Any> {
        val refillSupplementLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val postObj = MDJsonBuilder.formRefillSupplementObj(patientSupplements, authManager.selectedPatientLive.value!!)
                val supplementCall = webService.refillPrescriptionn(patientId, postObj)

                apiUtil.initiateRequest(supplementCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        refillSupplementLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        refillSupplementLive.postValue(error)
                    }
                })
            } else {
                refillSupplementLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            refillSupplementLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }
        return refillSupplementLive
    }

    private fun deleteMedicationsInLocalDb(medicationsId: String) {
        return db.miscDao().deleteMedicationsWithMedicationId(medicationsId)
    }

    private fun deleteSupplementsInLocalDb(supplementsId: String) {
        return db.miscDao().deleteSupplementsWithSupplementId(supplementsId)
    }


}
