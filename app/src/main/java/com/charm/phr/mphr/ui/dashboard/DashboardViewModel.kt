package com.charm.phr.mphr.ui.dashboard

import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.Flowsheets
import com.charm.phr.mphr.model.LabParamHistoryPOJO
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.model.MriResultPOJO
import com.charm.phr.mphr.model.PHRBills
import com.charm.phr.mphr.model.PHRFlowsheets
import com.charm.phr.mphr.model.ParamHistoryEntryPOJO
import com.charm.phr.mphr.model.TransfusionLogData
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientVitals
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.model.SectionConfigList
import com.charm.phr.mphr.model.TrackerMetricMap
import com.charm.phr.mphr.model.UnreadCount
import com.charm.phr.mphr.model.repository.AppointmentRepository
import com.charm.phr.mphr.model.repository.BillingRepository
import com.charm.phr.mphr.model.repository.MessageRepository
import com.charm.phr.mphr.model.repository.MiscRepository
import com.charm.phr.mphr.model.repository.PatientRepository
import com.charm.phr.mphr.model.repository.PrescriptionRepository
import com.charm.phr.mphr.model.repository.QuestionnaireRepository
import com.charm.phr.mphr.model.repository.WellnessRepository
import com.charm.phr.mphr.util.ModelUtil
import org.joda.time.DateTime
import org.joda.time.LocalDate

sealed class SectionConfigState {
    object Loading : SectionConfigState()
    data class Success(val sectionConfig: SectionConfigList) : SectionConfigState()
    object Error : SectionConfigState()
}

class DashboardViewModel(
    private val patientRepository: PatientRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val appointmentRepository: AppointmentRepository,
    private val miscRepository: MiscRepository,
    private val wellnessRepository: WellnessRepository,
    private val messageRepository: MessageRepository,
    private val questionnaireRepository: QuestionnaireRepository,
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _sectionConfigState = MutableLiveData<SectionConfigState>()
    val sectionConfigState: LiveData<SectionConfigState> get() = _sectionConfigState

    fun updateSectionConfigState(newState: SectionConfigState) {
        _sectionConfigState.postValue(newState)
    }

    var isUpcomingAppointmentFetch = false
    var isActiveMedicationFetch = false
    var isVitalsFetch = false
    var isInvoiceFetch = false
    var isMessageFetch = false
    var isQuestionnaireFetch = false

    var totalRespCount = 0

    var isDashboardProgressStillPresent = false

    var isPatientVitalsImgChanged = false

    var isfetchFeedsCalled = false

    var upcomingAppointmentList = arrayListOf<Appointment>()
    var activeMedicationList = arrayListOf<PatMedPOJO>()
    var vitalsList = arrayListOf<PatientVitals>()
    var invoiceList = arrayListOf<PHRBills>()
    var messageList = arrayListOf<Messages>()
    var questionnaireList = arrayListOf<PhysicianTemplates>()

    val unreadCountLive = MediatorLiveData<UnreadCount>()


    var trackerList: ArrayList<TrackerMetricMap>? = null


    var isComposeMsgRestrictionFetched = false
    var message: Messages? = null
    var isComposeDetailClicked: Boolean = false
    var selectedMessagePosition: Int = -1

    var composeMsgRestrictionList = arrayListOf<ComposeMsgRestriction>()

    var isComposeAvailable = false


    val fragmentCallbackLive = MutableLiveData<Any>()

    init {
        fragmentCallbackLive.postValue(DashboardViewPagerAdapter.DashboardCallback.PROGRESS)
    }

    var isSelectedPatientFetch = false
    var isPatientFailed = false
    var isComposeMessageAllowed = false
    var isBookAppointmentAllowed = false

    var selectedPatientDetail: Patient? = null

    var patientDetailLive = MediatorLiveData<Any?>()

    val imageReloadKey = mutableIntStateOf(0)

    fun getActiveMedications(): LiveData<Any> {
        return prescriptionRepository.getMedications(ModelUtil.medicationViewTypeActive)
    }

    fun getUpcomingAppointment(): LiveData<Any> {
        return appointmentRepository.getUpcomingAppointment()
    }

    fun getInvoiceList(): LiveData<Any> {
        return billingRepository.fetchInvoiceList(1, true)
    }

    fun getMessageList(): LiveData<Any> {
        return messageRepository.fetchMessageList(ModelUtil.messageTypeInbox, 1)
    }

    fun getCurrentQuestionnaireList(): LiveData<Any> {
        return questionnaireRepository.getQuestionnaireList(
            ModelUtil.questionnaireCurrentValue,
            LocalDate.now().minusMonths(3),
            null
        )
    }

    fun getUnreadCountLive() {
        unreadCountLive.addSource(miscRepository.getUnreadCount()) {

            if (it is UnreadCount) {
                unreadCountLive.postValue(it)
            }
        }
    }

    fun getComposeMsgRestriction(): LiveData<Any> {
        return messageRepository.checkComposeRestriction()
    }

    fun getPatientDetails(): MediatorLiveData<Any?> {

        var isCallbackSent = false

        var sectionConfigList: SectionConfigList? = null

        selectedPatientDetail = null

        patientDetailLive.addSource(miscRepository.getPatientSection()) { resp ->
            updateSectionConfigState(SectionConfigState.Loading)
            if (resp != null) {

                if (resp is SectionConfigList) {
                    sectionConfigList = resp

                    if (selectedPatientDetail != null && !isCallbackSent) {

                        Log.d("MDProgress", "value posted")

                        patientDetailLive.postValue(sectionConfigList)
                        updateSectionConfigState(
                            SectionConfigState.Success(
                                sectionConfigList ?: SectionConfigList()
                            )
                        )
                        isCallbackSent = true
                    }
                } else if (resp is APIResponseError) {
                    updateSectionConfigState(SectionConfigState.Error)
                    if (!isCallbackSent) {
                        patientDetailLive.postValue(resp)
                        isCallbackSent = true
                    }
                }
            }
        }

        patientDetailLive.addSource(patientRepository.getPatientDemographics()) { resp ->

            if (resp != null) {

                if (resp is Patient) {
                    selectedPatientDetail = resp
                    if (sectionConfigList != null && !isCallbackSent) {
                        Log.d("MDProgress", "value posted")
                        patientDetailLive.postValue(sectionConfigList)
                        isCallbackSent = true
                    }
                } else if (resp is APIResponseError) {
                    if (!isCallbackSent) {
                        patientDetailLive.postValue(resp)
                        isCallbackSent = true
                    }
                }
            }
        }


        return patientDetailLive
    }


    fun getVitals(): LiveData<Any> {
        return wellnessRepository.getVitalsForDashboard()
    }

    fun getTrackerList(): LiveData<Any> {
        return wellnessRepository.getVitalTrackerList()
    }

    fun getQuestionnaireHPList() {
        questionnaireRepository.getQuestionnaireHPList(true)
    }

    fun getVitalUnitSystem() {
        wellnessRepository.getVitalUnitType()
    }

    fun deleteAppointment(appointment: Appointment, reasonToCancel: String?): LiveData<Any> {
        return appointmentRepository.deleteAppointment(appointment, reasonToCancel)
    }

    var isIronTrendFetched = false
    var ironTrendHistoryPOJO: LabParamHistoryPOJO? = null

    var isLastLabResultsFetched = false
    var lastFerritinPOJO: LabParamHistoryPOJO? = null

    fun getLastFerritinResult(flowsheetId: String, practiceId: String): LiveData<Any> {
        val liveData = MediatorLiveData<Any>()
        val flowsheet = Flowsheets(flowSheetId = flowsheetId, practiceId = practiceId)
        liveData.addSource(questionnaireRepository.getFlowsheetEntryList(flowsheet)) { resp ->
            if (resp is ArrayList<*>) {
                val entries = resp as ArrayList<PHRFlowsheets>

                val sortedEntries = entries.sortedBy { it.date?.millis ?: 0L }

                val historyEntries = arrayListOf<ParamHistoryEntryPOJO>()
                sortedEntries.forEach { flowsheetEntry ->
                    val ferritinField = flowsheetEntry.flowsheetEntries?.firstOrNull {
                        it.name.equals(ModelUtil.FLOWSHEET_FIELD_FERRITIN, ignoreCase = true)
                    }
                    if (ferritinField?.value != null) {
                        historyEntries.add(
                            ParamHistoryEntryPOJO(
                                date = flowsheetEntry.date,
                                value = ferritinField.value
                            )
                        )
                    }
                }
                val latest = entries.maxByOrNull { it.date?.millis ?: 0L }
                val latestFerritinEntry = latest?.flowsheetEntries?.firstOrNull {
                    it.name.equals(ModelUtil.FLOWSHEET_FIELD_FERRITIN, ignoreCase = true)
                }

                val pojo = LabParamHistoryPOJO(
                    paramName = ModelUtil.FERRITIN_PARAM_NAME,
                    paramUnit = latestFerritinEntry?.unit?.takeIf { it.isNotBlank() }
                        ?: ModelUtil.FERRITIN_UNIT,
                    isStringValue = false
                )
                pojo.paramHistoryEntryList.addAll(historyEntries)

                ironTrendHistoryPOJO = pojo
                isIronTrendFetched = true

                liveData.postValue(pojo)
            } else {
                liveData.postValue(resp)
            }
        }
        return liveData
    }
    // endregion

    // region Last MRI Result
    var isLastMriResultFetched = false
    var lastMriResultPOJO: MriResultPOJO? = null

    fun getLastMriResult(flowsheetId: String, practiceId: String): LiveData<Any> {
        val liveData = MediatorLiveData<Any>()
        val flowsheet = Flowsheets(flowSheetId = flowsheetId, practiceId = practiceId)
        liveData.addSource(questionnaireRepository.getFlowsheetEntryList(flowsheet)) { resp ->
            if (resp is ArrayList<*>) {
                val entries = resp as ArrayList<PHRFlowsheets>
                val latest = entries.maxByOrNull { it.date?.millis ?: 0L }
                val licEntry = latest?.flowsheetEntries?.firstOrNull {
                    it.name.equals(ModelUtil.FLOWSHEET_FIELD_LIC, ignoreCase = true)
                }
                val t2Entry = latest?.flowsheetEntries?.firstOrNull {
                    it.name.equals(ModelUtil.FLOWSHEET_FIELD_CARDIAC_T2, ignoreCase = true)
                }
                val mri = MriResultPOJO(
                    date = latest?.date,
                    lic = licEntry?.value,
                    licUnit = licEntry?.unit?.takeIf { it.isNotBlank() } ?: ModelUtil.MRI_LIC_UNIT,
                    cardiacT2Star = t2Entry?.value?.toFloatOrNull(),
                    cardiacT2StarUnit = t2Entry?.unit?.takeIf { it.isNotBlank() }
                        ?: ModelUtil.MRI_CARDIAC_T2_UNIT
                )
                liveData.postValue(mri)
            } else {
                liveData.postValue(resp)
            }
        }
        return liveData
    }
    // endregion

    // region Thalassemia Flowsheet IDs
    var isThalassemiaFlowsheetsFetched = false
    var transfusionLogFlowsheetId: String? = null
    var transfusionLogPracticeId: String? = null
    var transfusionFlowsheet: Flowsheets? = null
    var ferritinMonitoringFlowsheetId: String? = null
    var ferritinMonitoringPracticeId: String? = null
    var ferritinFlowsheet: Flowsheets? = null
    var mriT2MonitoringFlowsheetId: String? = null
    var mriT2MonitoringPracticeId: String? = null
    var mriFlowsheet: Flowsheets? = null

    fun resetThalassemiaCache() {
        isLastLabResultsFetched = false
        lastFerritinPOJO = null
        ironTrendHistoryPOJO = null
        isIronTrendFetched = false
        isTransfusionLogFetched = false
        transfusionLogData = null
        isLastMriResultFetched = false
        lastMriResultPOJO = null
        isThalassemiaFlowsheetsFetched = false
        transfusionFlowsheet = null
        ferritinFlowsheet = null
        mriFlowsheet = null
    }

    fun getThalassemiaFlowsheetIds(): LiveData<Any> {
        val liveData = MediatorLiveData<Any>()
        liveData.addSource(questionnaireRepository.getFlowsheetList(1)) { resp ->
            if (resp is Pair<*, *>) {
                val flowsheetList = resp.first as? ArrayList<Flowsheets> ?: arrayListOf()
                flowsheetList.forEach { flowsheet ->
                    when (flowsheet.name) {
                        ModelUtil.FLOWSHEET_TRANSFUSION_LOG -> {
                            transfusionLogFlowsheetId = flowsheet.flowSheetId
                            transfusionLogPracticeId = flowsheet.practiceId
                            transfusionFlowsheet = flowsheet
                        }

                        ModelUtil.FLOWSHEET_FERRITIN_MONITORING -> {
                            ferritinMonitoringFlowsheetId = flowsheet.flowSheetId
                            ferritinMonitoringPracticeId = flowsheet.practiceId
                            ferritinFlowsheet = flowsheet
                        }

                        ModelUtil.FLOWSHEET_MRI_T2_MONITORING -> {
                            mriT2MonitoringFlowsheetId = flowsheet.flowSheetId
                            mriT2MonitoringPracticeId = flowsheet.practiceId
                            mriFlowsheet = flowsheet
                        }
                    }
                }
                isThalassemiaFlowsheetsFetched = true
                liveData.postValue(true)
            } else if (resp is APIResponseError) {
                liveData.postValue(resp)
            }
        }
        return liveData
    }
    // endregion

    // region Transfusion Log (Next Transfusion Due + Post Transfusion Hb — single API call)
    var isTransfusionLogFetched = false
    var transfusionLogData: TransfusionLogData? = null

    fun getTransfusionLogData(flowsheetId: String, practiceId: String): LiveData<Any> {
        val liveData = MediatorLiveData<Any>()
        val flowsheet = Flowsheets(flowSheetId = flowsheetId, practiceId = practiceId)
        liveData.addSource(questionnaireRepository.getFlowsheetEntryList(flowsheet)) { resp ->
            if (resp is ArrayList<*>) {
                val entries = resp as ArrayList<PHRFlowsheets>

                val latest = entries.maxByOrNull { it.date?.millis ?: 0L }

                val nextDateEntry = latest?.flowsheetEntries?.firstOrNull {
                    it.name.equals(
                        ModelUtil.FLOWSHEET_FIELD_NEXT_TRANSFUSION_DATE,
                        ignoreCase = true
                    )
                }
                val nextDateMillis = nextDateEntry?.value?.let { parseFlowsheetDateField(it) }

                // Extract Post Transfusion Hb
                val hbEntry = latest?.flowsheetEntries?.firstOrNull {
                    it.name.equals(ModelUtil.FLOWSHEET_FIELD_HB, ignoreCase = true)
                }

                val data = TransfusionLogData(
                    nextTransfusionDateMillis = nextDateMillis,
                    hbValue = hbEntry?.value,
                    hbUnit = hbEntry?.unit?.trim()?.trim('(', ')')?.trim()
                        ?.takeIf { it.isNotBlank() } ?: ModelUtil.HB_UNIT,
                    entryDate = latest?.date
                )
                liveData.postValue(data)
            } else if (resp is APIResponseError) {
                liveData.postValue(resp)
            } else {
                liveData.postValue(resp)
            }
        }
        return liveData
    }

    private fun parseFlowsheetDateField(dateStr: String): Long? {
        val formatters = listOf(
            ModelUtil.charmDisplayDateFormat(),
            ModelUtil.charmFlowsheetDateTimeFieldFormat(),
            ModelUtil.charmFlowsheetDataDateFormatter()
        )
        for (formatter in formatters) {
            try {
                return DateTime.parse(dateStr, formatter).millis
            } catch (e: Exception) {
                // try next format
            }
        }
        return null
    }
    // endregion

}