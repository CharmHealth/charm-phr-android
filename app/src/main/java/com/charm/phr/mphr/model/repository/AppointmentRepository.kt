package com.charm.phr.mphr.model.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.model.AppointmentRequest
import com.charm.phr.mphr.model.Facility
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.util.APIUtil
import com.charm.phr.mphr.util.AppticsUtil
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDCallback
import com.charm.phr.mphr.util.MDCallbackForMisc
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.MdEvent
import com.charm.phr.mphr.util.MdEventGroup
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.WebService
import com.google.gson.JsonObject
import org.joda.time.DateTime
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(
    private val webService: WebService,
    private val authManager: AuthManager,
    private val executor: Executor,
    private val appContext: Context,
    private val apiUtil: APIUtil
) {

    //region Appointment

    fun getUpcomingAppointment(patient: Patient? = null): LiveData<Any> {
        val upcomingAppointmentLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {


            val patientId = patient?.practicePatientId ?: authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {
                val practiceId = if (BuildUtil.isCustomApp) {
                    authManager.selectedPatientLive.value?.practiceList?.firstOrNull()?.practiceId ?: ""
                } else {
                    ""
                }

                val upcomingAppointmentCall = webService.getUpcomingAppointment(patientId, practiceId)

                apiUtil.initiateRequest(upcomingAppointmentCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        upcomingAppointmentLive.postValue(
                            MDParsingHelper.formUpcomingAppointment(
                                authManager,
                                respObj, patient
                            )
                        )
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        upcomingAppointmentLive.postValue(error)
                    }
                })
            } else {
                upcomingAppointmentLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            upcomingAppointmentLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return upcomingAppointmentLive
    }

    fun getRequestAppointment(): LiveData<Any> {
        val requestAppointmentLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {


            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {
                val practiceId = if (BuildUtil.isCustomApp) {
                    authManager.selectedPatientLive.value?.practiceList?.firstOrNull()?.practiceId ?: ""
                } else {
                    ""
                }
                val requestedAppointmentCall = webService.getRequestedAppointment(patientId, practiceId)

                apiUtil.initiateRequest(requestedAppointmentCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        requestAppointmentLive.postValue(
                            MDParsingHelper.formRequestedAndDeniedAppointment(
                                authManager,
                                respObj, ModelUtil.appointmentRequested
                            )
                        )

                    }

                    override fun onMDFailure(error: APIResponseError) {
                        requestAppointmentLive.postValue(error)
                    }
                })
            } else {
                requestAppointmentLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            requestAppointmentLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return requestAppointmentLive
    }

    fun getDeniedAppointment(): LiveData<Any> {
        val deniedAppointmentLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {


            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {
                val practiceId = if (BuildUtil.isCustomApp) {
                    authManager.selectedPatientLive.value?.practiceList?.firstOrNull()?.practiceId ?: ""
                } else {
                    ""
                }
                val deniedAppointmentCall = webService.getDeniedAppointment(patientId, practiceId)

                apiUtil.initiateRequest(deniedAppointmentCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        deniedAppointmentLive.postValue(
                            MDParsingHelper.formRequestedAndDeniedAppointment(
                                authManager,
                                respObj, ModelUtil.appointmentDenied
                            )
                        )

                    }

                    override fun onMDFailure(error: APIResponseError) {
                        deniedAppointmentLive.postValue(error)
                    }
                })
            } else {
                deniedAppointmentLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            deniedAppointmentLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return deniedAppointmentLive
    }

    fun getPastAppointment(): LiveData<Any> {
        val pastAppointmentLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {
                val practiceId = if (BuildUtil.isCustomApp) {
                    authManager.selectedPatientLive.value?.practiceList?.firstOrNull()?.practiceId ?: ""
                } else {
                    ""
                }
                val pastAppointmentCall = webService.getPastAppointment(patientId, practiceId)

                apiUtil.initiateRequest(pastAppointmentCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        val appointmentList = MDParsingHelper.formUpcomingAppointment(
                            authManager,
                            respObj
                        )
                        appointmentList.sortByDescending { it.appointmentDate?.millis ?: 0 }
                        pastAppointmentLive.postValue(
                            appointmentList
                        )

                    }

                    override fun onMDFailure(error: APIResponseError) {
                        pastAppointmentLive.postValue(error)
                    }
                })
            } else {
                pastAppointmentLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            pastAppointmentLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return pastAppointmentLive
    }

    fun getWaitingListAppointment(): LiveData<Any> {
        val waitingListAppointmentLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {


            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val practiceId = if (BuildUtil.isCustomApp) {
                    authManager.selectedPatientLive.value?.practiceList?.firstOrNull()?.practiceId ?: ""
                } else {
                    ""
                }

                val waitingListAppointmentCall = webService.getWaitingListAppointment(patientId, practiceId)

                apiUtil.initiateRequest(waitingListAppointmentCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        waitingListAppointmentLive.postValue(
                            MDParsingHelper.formWaitingAppointment(
                                authManager,
                                respObj
                            )
                        )

                    }

                    override fun onMDFailure(error: APIResponseError) {
                        waitingListAppointmentLive.postValue(error)
                    }
                })
            } else {
                waitingListAppointmentLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            waitingListAppointmentLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return waitingListAppointmentLive
    }


    fun getFacilityList(selectedPractice: PracticeList): LiveData<Any> {
        val facilityListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {


                /*val facilityList = arrayListOf<Facility>()

                val practiceList = ModelUtil.getPHREntryEnabledPractices(authManager , ModelUtil.appointmentModelId)
                val practiceSize = practiceList.size

                var respCount = 0

                var isCallbackSent = false

                practiceList.forEach { practice ->*/

                val practiceId = selectedPractice.practiceId ?: ""

                val facilityListCall = webService.getFacilityListForAppointment(practiceId, patientId)

                apiUtil.initiateRequest(facilityListCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        facilityListLive.postValue(MDParsingHelper.formFacilityListForAppointment(respObj, practiceId))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        facilityListLive.postValue(error)
                    }

                })
                //}


                /*val membersListCall = webService.getMemberListForReqAppointment(patientId)
                membersListCall.enqueue(object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        membersListLive.postValue(
                            MDParsingHelper.formMembersList(
                                respObj
                            )
                        )

                    }

                    override fun onMDFailure(error: APIResponseError) {
                        membersListLive.postValue(error)
                    }
                })*/
                /*  val facilityList = arrayListOf<Facility>()


                  val member2 = Member(fullName = "Dr. Balaji  Ramamurti" , memberId = "1884000000097175" , practiceId = "257000000071003", facilityList = arrayListOf(
                      Facility(facilityId = "1884000000374011" , facilityName = "Charm Health Clinic") ,
                      Facility(facilityId = "1884000000375191" , facilityName = "Malar Hospital")
                  ))

                  val member3 = Member(fullName = "david sandy" , memberId = "1884000006869461" , practiceId = "257000000071003", facilityList = arrayListOf(
                      Facility(facilityId = "1884000000374011" , facilityName = "Charm Health Clinic")
                  ))

                  val facility =  Facility(facilityId = "1884000000374011" , facilityName = "Charm Health Clinic")
                  val facility2 = Facility(facilityId = "1884000000375191" , facilityName = "Malar Hospital")
                  val facility3 = Facility(facilityId = "1884000002728033" , facilityName = "Physical therapy center")

                  facilityList.add(facility)
                  facilityList.add(facility2)
                  facilityList.add(facility3)

                  facilityListLive.postValue(facilityList)
  */
            } else {
                facilityListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            facilityListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return facilityListLive
    }


    fun getMembersList(facility: Facility, startIndex: Int): LiveData<Any> {
        val membersListLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val facilityId = facility.facilityId ?: ""

            if (facilityId.isNotBlank()) {
                val practiceId = facility.practiceId ?: ""

                val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

                val membersListCall =
                    webService.getMemberListForAppointment(practiceId, facilityId, patientId, startIndex, ModelUtil.getMembersListCountPerPage)
                apiUtil.initiateRequest(membersListCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        membersListLive.postValue(
                            MDParsingHelper.formMemberListForMessage(
                                respObj, practiceId
                            )
                        )

                    }

                    override fun onMDFailure(error: APIResponseError) {
                        membersListLive.postValue(error)
                    }
                })

                /* val memberList = arrayListOf<Member>()
                 val member = Member(fullName = "Dr. Akshay Kumar" , memberId = "1884000015157025" , practiceId = "257000000071003", facilityList = arrayListOf(
                     Facility(facilityId = "1884000000374011" , facilityName = "Charm Health Clinic") ,
                     Facility(facilityId = "1884000000375191" , facilityName = "Malar Hospital")
                 ))

                 val member2 = Member(fullName = "Dr. Balaji  Ramamurti" , memberId = "1884000000097175" , practiceId = "257000000071003", facilityList = arrayListOf(
                     Facility(facilityId = "1884000000374011" , facilityName = "Charm Health Clinic") ,
                     Facility(facilityId = "1884000000375191" , facilityName = "Malar Hospital"),
                     Facility(facilityId = "1884000002728033" , facilityName = "Physical therapy center")
                 ))

                 val member3 = Member(fullName = "david sandy" , memberId = "1884000006869461" , practiceId = "257000000071003", facilityList = arrayListOf(
                     Facility(facilityId = "1884000000374011" , facilityName = "Charm Health Clinic")
                 ))

                 memberList.add(member3)
                 memberList.add(member)
                 memberList.add(member2)

                 membersListLive.postValue(memberList)
 */
            } else {
                membersListLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_facility_id_not_found)
                    )
                )
            }
        } else {
            membersListLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }


        return membersListLive
    }

    fun getAppointmentConfig(memberId: String, facility: Facility): LiveData<Any> {
        val appointmentConfigLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val facilityId = facility.facilityId ?: ""

            val practiceId = facility.practiceId ?: ""

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            val appointmentConfigCall = webService.getAppointmentConfig(memberId, facilityId, practiceId, patientId)
            apiUtil.initiateRequest(appointmentConfigCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {

                    val appointmentConfig = MDParsingHelper.formAppointmentConfig(respObj)
                    appointmentConfigLive.postValue(appointmentConfig)
                }

                override fun onMDFailure(error: APIResponseError) {
                    appointmentConfigLive.postValue(error)
                }

            })


        } else {
            appointmentConfigLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return appointmentConfigLive
    }

    //Note we are fetching a month slots data to show available slot days in UI, so once fetched for a month DO NOT call again for that month.
    fun getAppointmentSlots(memberId: String, facility: Facility, fromDate: DateTime, visitTypeId: String = "", interval: Int): LiveData<Any> {
        val appointmentSlotsLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val fromDateStr = fromDate.toString(ModelUtil.charmDateFormatForGettingSlots)
            val toDateStr = fromDate.dayOfMonth().withMaximumValue().toString(ModelUtil.charmDateFormatForGettingSlots)
            fromDate.dayOfMonth().withMaximumValue()
//            val toDateStr = slotPickedDate.toString(ModelUtil.charmDateFormatForGettingSlots)

            val facilityId = facility.facilityId ?: ""
            val practiceId = facility.practiceId ?: ""
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            val appointmentSlotsCall = webService.getAppointmentSlots(
                memberId,
                practiceId,
                facilityId,
                patientId,
                fromDate = fromDateStr,
                toDate = toDateStr,
                visitTypeId = visitTypeId,
                interval = interval
            )
            apiUtil.initiateRequest(appointmentSlotsCall, object : MDCallback(authManager) {
                override fun onMDSuccess(respObj: JsonObject) {
                    val appointmentSlots = MDParsingHelper.formSlotsList(respObj, fromDate)
                    appointmentSlotsLive.postValue(appointmentSlots)
                }

                override fun onMDFailure(error: APIResponseError) {
                    appointmentSlotsLive.postValue(error)
                }

            })
        } else {
            appointmentSlotsLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return appointmentSlotsLive
    }

    fun bookAppointment(appointmentRequest: AppointmentRequest): LiveData<Any> {
        val appointmentConfigLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                appointmentRequest.practicePatientId = patientId


                val postObj = MDJsonBuilder.formAppointmentObj(appointmentRequest)


                Log.d("MdCallback", postObj.toString())

                val addAppointmentCall = if (appointmentRequest.isEnabled) {
                    if (appointmentRequest.addAsWaitingList) {
                        webService.requestWaitListAppointment(postObj)
                    } else {
                        webService.bookAppointment(patientId, postObj)
                    }
                } else {
                    webService.requestAppointment(patientId, postObj)
                }

                apiUtil.initiateRequest(addAppointmentCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        appointmentConfigLive.postValue(MDParsingHelper.formBookAppointmentResp(respObj))
                        AppticsUtil.addEvent(MdEventGroup.Appointment, MdEvent.BookAppointmentOnSuccess)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        appointmentConfigLive.postValue(error)
                        AppticsUtil.addEvent(MdEventGroup.Appointment, MdEvent.BookAppointmentOnFailure)
                    }

                })
            } else {
                appointmentConfigLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            appointmentConfigLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return appointmentConfigLive
    }

    fun deleteAppointment(appointment: Appointment, reasonToCancel: String?): LiveData<Any> {
        val deleteAppointmentLive = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val deleteAppointmentCall = if (appointment.appointmentCode == ModelUtil.appointmentUpcoming) {
                    webService.cancelAppointment(appointment.appointmentId ?: "", appointment.practiceId ?: "", patientId, reasonToCancel ?: "")
                } else if (appointment.appointmentCode == ModelUtil.appointmentWaitingList) {
                    webService.cancelWaitingList(appointment.appointmentId ?: "", appointment.practiceId ?: "")
                } else {
                    webService.deleteAppointment(appointment.appointmentId ?: "", appointment.practiceId ?: "")
                }

                apiUtil.initiateRequest(deleteAppointmentCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {
                        deleteAppointmentLive.postValue(true)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        deleteAppointmentLive.postValue(error)
                    }

                })
            } else {
                deleteAppointmentLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            deleteAppointmentLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return deleteAppointmentLive
    }


    //endregion

    //region Visit Summary

    fun fetchVisitSummaryList(pageNo: Int): LiveData<Any> {
        val fetchVisitSummaryLive = MutableLiveData<Any>()


        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val fromRange: Int = ((pageNo - 1) * ModelUtil.listPerPageCount) + 1


                val practiceId = if (BuildUtil.isCustomApp) {
                    BuildUtil.practiceId
                } else {
                    ""
                }

                val fetchVisitSummaryCall = webService.getVisitSummary(patientId, practiceId, fromRange)

                apiUtil.initiateRequest(fetchVisitSummaryCall, object : MDCallback(authManager) {
                    override fun onMDSuccess(respObj: JsonObject) {

                        val visitSummaryList = MDParsingHelper.formVisitSummaryList(authManager, respObj)
                        val hasMoreSummary = visitSummaryList.size == ModelUtil.listPerPageCount

                        fetchVisitSummaryLive.postValue(Pair(visitSummaryList, hasMoreSummary))
                    }

                    override fun onMDFailure(error: APIResponseError) {

                        fetchVisitSummaryLive.postValue(error)
                    }

                })
            } else {
                fetchVisitSummaryLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            fetchVisitSummaryLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return fetchVisitSummaryLive
    }


    fun getVisitSummaryPDF(patientEncounterId: String, practiceId: String): LiveData<Any> {
        val getVisitSummaryPDFLive = MutableLiveData<Any>()


        if (authManager.isUserOnline()) {

            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val getVisitSummaryPDFCall = webService.getVisitSummaryPDF(patientId, patientEncounterId, practiceId)

                apiUtil.initiateRequestForMisc(getVisitSummaryPDFCall, object : MDCallbackForMisc(authManager) {
                    override fun onMDSuccess(respObj: MDFile) {
                        getVisitSummaryPDFLive.postValue(respObj)
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        getVisitSummaryPDFLive.postValue(error)
                    }

                })

            } else {
                getVisitSummaryPDFLive.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
        } else {
            getVisitSummaryPDFLive.postValue(
                APIResponseError(
                    ModelUtil.errorInternetConnectionCode, null,
                    appContext.getString(R.string.internet_connection_error)
                )
            )
        }

        return getVisitSummaryPDFLive
    }

    //endregion

    //video consultation

    fun getVideoCallCredentialsForConsultation(consultationId: String, practiceId: String): LiveData<Any> {

        val liveData = MutableLiveData<Any>()

        if (authManager.isUserOnline()) {
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""

            if (patientId.isNotBlank()) {

                val getVideoCallCredentialsCall = webService.getVideoCallCredentialsForConsultation(patientId, consultationId, practiceId)

                apiUtil.initiateRequest(getVideoCallCredentialsCall, object : MDCallback(authManager) {

                    override fun onMDSuccess(respObj: JsonObject) {

                        liveData.postValue(MDParsingHelper.parseZoomCredential(respObj))
                    }

                    override fun onMDFailure(error: APIResponseError) {
                        liveData.postValue(error)
                    }

                })
            } else {
                liveData.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }

        } else liveData.value = APIResponseError()

        return liveData

    }

    //endregion

}