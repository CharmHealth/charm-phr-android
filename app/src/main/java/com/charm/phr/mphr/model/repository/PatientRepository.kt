package com.charm.phr.mphr.model.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientInsuranceDetails
import com.charm.phr.mphr.util.APIUtil
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDCallback
import com.charm.phr.mphr.util.MDCallbackForMisc
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.WebService
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Call
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepository @Inject constructor(private val webService: WebService, private val authManager: AuthManager, private val executor: Executor, private val appContext: Context, private val apiUtil: APIUtil, private val sharedPreferences: SharedPreferences) {

    private var getPayersCall: Call<JsonElement>? = null

    fun getPatientList(storePatient: Boolean = false): LiveData<Any> {

        val patientListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {


            val patientListCall = webService.getPatientList()


            apiUtil.initiateRequest(patientListCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {

                    val patientList = MDParsingHelper.formPatientList(respObj)

                    patientList.forEach { patient ->
                        val localPatient = authManager.patientList.firstOrNull { it.practicePatientId == patient.practicePatientId }
                        patient.unitSystem = localPatient?.unitSystem
                    }


                    if (storePatient) {
                        val prefEdit = sharedPreferences.edit()
                        prefEdit.putString(ModelUtil.patientList, MDJsonBuilder.formPatientList(patientList))
                        prefEdit.apply()
                        authManager.patientList = patientList

                        val patientId = authManager.selectedPatientLive.value?.practicePatientId
                        val selectedPatient = patientList.firstOrNull { it.practicePatientId == patientId }
                        if (selectedPatient != null) {
                            authManager.updateSelectedPatient(selectedPatient)
                        }
                    }
                    patientListLive.postValue(patientList)
                }

                override fun onMDFailure(error: APIResponseError) {

                    patientListLive.postValue(error)
                }

            })
        } else {
            patientListLive.postValue(APIResponseError(ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
            )
            )
        }


        return patientListLive
    }

    fun getStates(country: String): LiveData<Any> {
        val savePatientLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val savePatientCall = webService.getStateList(country)

            apiUtil.initiateRequest(savePatientCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    savePatientLive.postValue(MDParsingHelper.getStateList(respObj))
                }

                override fun onMDFailure(error: APIResponseError) {
                    savePatientLive.postValue(error)
                }

            })
        } else {
            savePatientLive.postValue(APIResponseError(ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
            )
            )
        }


        return savePatientLive

    }

    fun getPatientCommonData(): LiveData<Any> {
        val savePatientLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val savePatientCall = webService.getPatientCommonData()

            apiUtil.initiateRequest(savePatientCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    savePatientLive.postValue(MDParsingHelper.formPatientCommonData(respObj))
                }

                override fun onMDFailure(error: APIResponseError) {
                    savePatientLive.postValue(error)
                }

            })
        } else {
            savePatientLive.postValue(APIResponseError(ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
            )
            )
        }


        return savePatientLive

    }

    fun getLanguages(): LiveData<Any> {
        val languagesLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val languagesCall = webService.getLanguages()

            apiUtil.initiateRequest(languagesCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    languagesLive.postValue(MDParsingHelper.formLanguageList(respObj))
                }

                override fun onMDFailure(error: APIResponseError) {
                    languagesLive.postValue(error)
                }

            })
        } else {
            languagesLive.postValue(APIResponseError(ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
            )
            )
        }


        return languagesLive

    }

    fun savePatientDemographics(patient: Patient): LiveData<Any> {
        val savePatientLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val postObj = MDJsonBuilder.formPatientObj(patient)

            val savePatientCall = webService.savePatientDemographics(patient.practicePatientId
                    ?: "", postObj)


            apiUtil.initiateRequest(savePatientCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    if (patient.filePath.isNullOrBlank()) {
                        savePatientLive.postValue(true)
                        getPatientList(true)
                    } else {
                        val fileReq = FileUtil.createRequestBodyForFile(patient.filePath
                                ?: "")

                        val filePathList = patient.filePath?.split("/") ?: arrayListOf()
                        val fileMultiPartBody =
                                MultipartBody.Part.createFormData("photo", filePathList[filePathList.size - 1], fileReq!!)

                        val uploadImageCall = webService.uploadPatientProfile(patient.practicePatientId
                                ?: "", fileMultiPartBody)

                        apiUtil.initiateRequest(uploadImageCall, object : MDCallback(authManager) {
                            override fun onMDSuccess(respObj: JsonObject) {
                                savePatientLive.postValue(true)
                                getPatientList(true)
                            }

                            override fun onMDFailure(error: APIResponseError) {
                                savePatientLive.postValue(error)
                            }
                        })

                    }
                }

                override fun onMDFailure(error: APIResponseError) {
                    savePatientLive.postValue(error)
                }

            })
        } else {
            savePatientLive.postValue(APIResponseError(ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
            )
            )
        }


        return savePatientLive

    }

    fun getPatientDemographics(): LiveData<Any> {
        val getPatientLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val fetchPatientCall = webService.getPatientDemographics(patientId)

                apiUtil.initiateRequest(fetchPatientCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        getPatientLive.postValue(MDParsingHelper.formPatientObj(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        getPatientLive.postValue(error)
                    }

                })
            } else {
                getPatientLive.postValue(
                        APIResponseError(
                                null,
                                null,
                                appContext.getString(R.string.error_patient_id_not_found)
                        )
                )
            }
        } else {
            getPatientLive.postValue(APIResponseError(ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
            )
            )
        }
        return getPatientLive
    }

    fun getHealthPassportURL(): LiveData<Any> {
        val healthPassportURLLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {


                val healthPassportURLCall = webService.getHealthPassportURL(patientId)

                apiUtil.initiateRequest(healthPassportURLCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        healthPassportURLLive.postValue(MDParsingHelper.formHealthPassportURLObj(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        healthPassportURLLive.postValue(error)
                    }
                })
            } else {
                healthPassportURLLive.postValue(
                        APIResponseError(
                                null,
                                null,
                                appContext.getString(R.string.error_patient_id_not_found)
                        )
                )
            }
        } else {
            healthPassportURLLive.postValue(
                    APIResponseError(
                            ModelUtil.errorInternetConnectionCode,
                            null,
                            appContext.getString(R.string.internet_connection_error)
                    )
            )
        }
        return healthPassportURLLive
    }

    fun getPatDemoPDF(): LiveData<Any> {
        val patDemoPDFLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {


                val patDemoPDFCall = webService.getPatDemoPDF(patientId)

                apiUtil.initiateRequestForMisc(patDemoPDFCall, object : MDCallbackForMisc(authManager) {

                    override fun onMDSuccess(respObj: MDFile) {
                        patDemoPDFLive.postValue(respObj)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        patDemoPDFLive.postValue(error)
                    }
                })
            } else {
                patDemoPDFLive.postValue(
                        APIResponseError(
                                null,
                                null,
                                appContext.getString(R.string.error_patient_id_not_found)
                        )
                )
            }
        } else {
            patDemoPDFLive.postValue(
                    APIResponseError(
                            ModelUtil.errorInternetConnectionCode,
                            null,
                            appContext.getString(R.string.internet_connection_error)
                    )
            )
        }
        return patDemoPDFLive
    }

    fun getHealthCardPDF(): LiveData<Any> {
        val healthCardPDFLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                val healthCardPDFCall = webService.getPatDemoPDF(patientId)

                apiUtil.initiateRequestForMisc(healthCardPDFCall, object : MDCallbackForMisc(authManager) {

                    override fun onMDSuccess(respObj: MDFile) {
                        healthCardPDFLive.postValue(respObj)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        healthCardPDFLive.postValue(error)
                    }
                })
            } else {
                healthCardPDFLive.postValue(
                        APIResponseError(
                                null,
                                null,
                                appContext.getString(R.string.error_patient_id_not_found)
                        )
                )
            }
        } else {
            healthCardPDFLive.postValue(
                    APIResponseError(
                            ModelUtil.errorInternetConnectionCode,
                            null,
                            appContext.getString(R.string.internet_connection_error)
                    )
            )
        }
        return healthCardPDFLive
    }


    //region insurance

    fun fetchInsuranceList(pageNo: Int): LiveData<Any> {
        val fetchInsuranceLive = MutableLiveData<Any>()


        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val fromRange: Int = ((pageNo - 1) * ModelUtil.listPerPageCount) + 1
                val fetchInsuranceCall = webService.getInsurances(patientId)

                apiUtil.initiateRequest(fetchInsuranceCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {

                        val insuranceList = MDParsingHelper.formInsuranceList(respObj)
                        fetchInsuranceLive.postValue(Pair(insuranceList, false))
                    }

                    override fun onMDFailure(error: APIResponseError) {

                        fetchInsuranceLive.postValue(error)
                    }

                })
            } else {
                fetchInsuranceLive.postValue(
                        APIResponseError(
                                null,
                                null,
                                appContext.getString(R.string.error_patient_id_not_found)
                        )
                )
            }
        } else {
            fetchInsuranceLive.postValue(APIResponseError(ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
            )
            )
        }

        return fetchInsuranceLive
    }

    fun getInsuranceCommonData(practiceId: String): LiveData<Any> {
        val insuranceCommonDataLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {


                val insuranceCommonDataCall = webService.getCommonData(ModelUtil.moduleNameInsurance, patientId, practiceId)
                apiUtil.initiateRequest(insuranceCommonDataCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        insuranceCommonDataLive.postValue(MDParsingHelper.formInsuranceCommonData(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        insuranceCommonDataLive.postValue(error)
                    }

                })
            } else {
                insuranceCommonDataLive.postValue(
                        APIResponseError(
                                null,
                                null,
                                appContext.getString(R.string.error_patient_id_not_found)
                        )
                )
            }
        } else {
            insuranceCommonDataLive.postValue(
                    APIResponseError(
                            ModelUtil.errorInternetConnectionCode,
                            null,
                            appContext.getString(R.string.internet_connection_error)
                    )
            )
        }

        return insuranceCommonDataLive
    }

    fun addInsurance(insurance: PatientInsuranceDetails): LiveData<Any> {
        val addInsuranceLive = MutableLiveData<Any>()


        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {


                insurance.practicePatientId = patientId
                val postObj = MDJsonBuilder.formAddInsuranceObj(insurance)


                val addInsuranceCall = webService.addInsurance(patientId, postObj)

                apiUtil.initiateRequest(addInsuranceCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        addInsuranceLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        addInsuranceLive.postValue(error)
                    }
                })
            } else {
                addInsuranceLive.postValue(
                        APIResponseError(
                                null,
                                null,
                                appContext.getString(R.string.error_patient_id_not_found)
                        )
                )
            }
        } else {
            addInsuranceLive.postValue(
                    APIResponseError(
                            ModelUtil.errorInternetConnectionCode,
                            null,
                            appContext.getString(R.string.internet_connection_error)
                    )
            )
        }
        return addInsuranceLive
    }

    fun editInsurance(insurance: PatientInsuranceDetails): LiveData<Any> {
        val editInsuranceLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {


                insurance.practicePatientId = patientId
                val postObj = MDJsonBuilder.formAddInsuranceObj(insurance)


                val editInsuranceCall = webService.editInsurance(patientId, postObj)

                apiUtil.initiateRequest(editInsuranceCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        editInsuranceLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        editInsuranceLive.postValue(error)
                    }
                })
            } else {
                editInsuranceLive.postValue(
                        APIResponseError(
                                null,
                                null,
                                appContext.getString(R.string.error_patient_id_not_found)
                        )
                )
            }
        } else {
            editInsuranceLive.postValue(
                    APIResponseError(
                            ModelUtil.errorInternetConnectionCode,
                            null,
                            appContext.getString(R.string.internet_connection_error)
                    )
            )
        }
        return editInsuranceLive
    }

    fun deleteInsurance(insurance: PatientInsuranceDetails): LiveData<Any> {
        val deleteInsuranceLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {


                insurance.practicePatientId = patientId
                val postObj = MDJsonBuilder.formDeleteInsuranceObj(insurance)


                val deleteInsuranceCall = webService.deleteInsurance(postObj)

                apiUtil.initiateRequest(deleteInsuranceCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        deleteInsuranceLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        deleteInsuranceLive.postValue(error)
                    }
                })
            } else {
                deleteInsuranceLive.postValue(
                        APIResponseError(
                                null,
                                null,
                                appContext.getString(R.string.error_patient_id_not_found)
                        )
                )
            }
        } else {
            deleteInsuranceLive.postValue(
                    APIResponseError(
                            ModelUtil.errorInternetConnectionCode,
                            null,
                            appContext.getString(R.string.internet_connection_error)
                    )
            )
        }
        return deleteInsuranceLive
    }

    fun getPayerList(searchStr: String, practiceId: String): LiveData<Any> {

        val payerListLive = MutableLiveData<Any>()
        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isNotBlank()) {

                getPayersCall?.cancel()

                getPayersCall = webService.getPayerList(patientId, practiceId, searchStr)
                apiUtil.initiateRequest(getPayersCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        payerListLive.postValue(MDParsingHelper.formPayerList(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        payerListLive.postValue(error)
                    }
                })
            } else {
                payerListLive.postValue(
                        APIResponseError(
                                null,
                                null,
                                appContext.getString(R.string.error_patient_id_not_found)
                        )
                )
            }

        } else {
            payerListLive.postValue(
                    APIResponseError(
                            ModelUtil.errorInternetConnectionCode,
                            null,
                            appContext.getString(R.string.internet_connection_error)
                    )
            )
        }

        return payerListLive
    }


    //endregion


}