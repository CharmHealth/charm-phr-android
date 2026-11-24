package com.charm.phr.mphr.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.room.Embedded
import androidx.room.Relation
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.database.entity.Reminder
import com.charm.phr.mphr.model.database.entity.ReminderFrequencyMap
import com.charm.phr.mphr.model.database.entity.ReminderHistory
import com.charm.phr.mphr.model.database.entity.ReminderMap
import com.charm.phr.mphr.util.ModelUtil
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.gson.JsonArray
import kotlinx.android.parcel.Parcelize
import okhttp3.ResponseBody
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.io.File

class ReminderPOJO {

    @Embedded
    var reminder: Reminder? = null

    @Relation(parentColumn = "localReminderId", entityColumn = "localReminderId", entity = ReminderMap::class)
    var reminderMap: List<ReminderMap> = ArrayList()

    @Relation(parentColumn = "localReminderId", entityColumn = "localReminderId", entity = ReminderFrequencyMap::class)
    var reminderFrequencyMap: List<ReminderFrequencyMap> = ArrayList()

}

class ReminderFrequencyMapPOJO {

    @Relation(parentColumn = "localReminderId", entityColumn = "localReminderId", entity = ReminderMap::class)
    var reminderMap: ReminderMap? = null

    @Embedded
    var reminderFrequencyMap: ReminderFrequencyMap? = null
}

class ReminderHistoryPOJO {

    @Embedded
    var reminderHistory: ReminderHistory? = null

    @Relation(parentColumn = "localReminderMapId", entityColumn = "localReminderMapId", entity = ReminderMap::class)
    var reminderMap: List<ReminderMap> = ArrayList()

    @Relation(parentColumn = "localFrequencyMapId", entityColumn = "localFrequencyMapId", entity = ReminderFrequencyMap::class)
    var reminderFrequencyMap: List<ReminderFrequencyMap> = ArrayList()

}


class FrequencyAlarmMapCustomPOJO(
    var localFrequencyMapId: Long = 0,
    var scheduledTime: DateTime? = null,
    var reminderId: String? = null,
    var moduleType: String? = null
)

class ReminderCustomPOJO {
    var reminder: Reminder? = null
    var reminderMap: List<ReminderMap> = ArrayList()

    var reminderFrequencyMap: List<ReminderFrequencyMap> = ArrayList()

    var reminderHistory: List<ReminderHistoryPOJO> = ArrayList()

    var selectedModuleObject: Any? = null
}

data class ReminderScheduler(
    var reminderId: String? = null,
    var reminderDateTime: DateTime? = null
    //need to add Unique Notification id
)

data class Patient//
    (
    var practicePatientId: String? = null,
    var fullName: String? = null,
    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    var patientRecordId: String? = null,
    var createdByPractice: Boolean? = true,
    var gender: String? = null,//allowed values - male, female
    var dateOfBirth: LocalDate? = null,
    var bloodGroup: String? = null,
    var dateOfJoining: DateTime? = null,
    var smokingStatus: String? = null,
    var language: String? = null,
    var maritalStatus: String? = null,
    var ssn: String? = null,
    var emergencyContactName: String? = null,
    var emergencyContactNumber: String? = null,
    var emergencyContactExtn: String? = null,
    var isActive: Boolean = true,
    var localUpdateDateTime: DateTime? = null,
    var idIssuingJurisdiction: String? = null,
    var idQualifier: Int? = null,
    var idOfPatient: String? = null,
    var photoURI: String? = null,
    var isPhotoAvailableOnline: Boolean = true,
    var primaryCareProvider: String? = null,
    var secondaryCareProvider: String? = null,
    var caregiverFirstName: String? = null,
    var caregiverLastName: String? = null,
    var caregiverFullName: String? = null,
    var caregiverMobile: String? = null,
    var contactDetails: ContactDetails? = null,
    var postalAddress: PostalAddress? = null,
    var genderIdentity: String? = null,
    var nickName: String? = null,
    var suffix: String? = null,
    var maidenName: String? = null,
    var deceased: Boolean? = null,
    var dod: String? = null,
    var causeOfDeath: String? = null,
    var idQualifiers: JsonArray = JsonArray(),
    var isBluefinEnabled: Boolean = false,
//    var caregivers: List<Caregiver> = listOf(),
//    var guarantor: Guarantor? = null,
    var emailNotification: Boolean? = null,
    var textNotification: Boolean? = null,
    var voiceNotification: Boolean? = null,
    var race: String? = null,
    var ethnicity: String? = null,
    var employmentStatus: String? = null,
    var sexualOrientation: String? = null,
    var motherFirstName: String? = null,
    var motherLastName: String? = null,
    var birthOrder: String? = null,
    var categories: JsonArray = JsonArray(),

    var introduction: String? = null,
    var customField1: String? = null,
    var customField2: String? = null,
    var customField3: String? = null,
    var customField4: String? = null,
    var customField5: String? = null,
    var sourceName: String? = null,
    var sourceValue: String? = null,
    var paymentSource: String? = null,
    var paymentStartDate: String? = null,
    var paymentEndDate: String? = null,
    var externalId: String? = null,

    var facilityListForEdit: JsonArray = JsonArray(),

    var patientImg: File? = null,
    var isPatientUpdated: Boolean = false,

    var isPatientImgToBeDeleted: Boolean = false,

    var age: String? = null,
    var hpModelURL: String? = null,
    var passcode: String? = null,
    var filePath: String? = null,
    var practiceList: ArrayList<PracticeList> = arrayListOf(),

    var unitSystem: String? = null,
    var isHealthPassportAvailable: Boolean = false,
    var sectionConfigList: SectionConfigList? = null,
    var toString: String = "",
    var isSecondaryAccount: Boolean = false


)


data class ContactDetails(//
    //
    var contactDetailsId: String? = null,
    var preferredCommunication: String? = null,
    var emailId: String? = null,
    var landline: String? = null,
    var mobile: String? = null,
    var fax: String? = null,
    var webUrl: String? = null,
    var workPhone: String? = null,
    var workPhoneExtn: String? = null,
    var contactPreference: Char? = null,

    var homePhone: String? = null,
    var primaryPhone: String? = null

)

data class PostalAddress(//
    //
    var postalAddressId: String? = null,
    var line1: String? = null,
    var line2: String? = null,
    var area: String? = null,
    var city: String? = null,
    var district: String? = null,
    var state: String? = null,
    var county: String? = null,
    var country: String? = null,
    var postalCode: String? = null,
    var postbox: String? = null
)


data class PracticeList(//
    var practiceId: String? = null,
    var practiceName: String? = null,
    var isActive: Boolean = true,
    var facilityList: String? = null,
    var isBluefinEnabled: Boolean = false,
    var easyCareURL: String? = null
)

data class APIResponseError
    (
    var charmApiCode: Int? = null,
    var httpStatusCode: Int? = null,
    var errorMsg: String? = null
)

data class Appointment(
//
    //
    var appointmentId: String? = null,
    var appointmentDate: DateTime? = null,
    var appointmentStartTime: DateTime? = null,
    var appointmentEndTime: DateTime? = null,
    var timezone: String? = null,
    var appointmentType: String? = null,
    var statusId: String? = null,
    var reminderType: String? = null,
    var physicianRemark: String? = null,
    var reason: String? = null,
    var physicianId: String? = null,
    var facilityId: String? = null,
    var modifiedDate: DateTime? = null,
    var lastModifiedTime: DateTime? = null,
    //
    var member: Member? = null,
    var patient: Patient? = null,
    var facilityName: String? = null,
    var visitTypeDetail: VisitTypeDetail? = null,

    var appointmentStatus: AppointmentStatus? = null,

    var durationInMinutes: Int? = null,
    var datePreference: String? = null,
    var timePreference: String? = null,
    var practiceId: String? = null,
    var appointmentCode: Int? = null,
    var isQuestionnairePresent: Boolean = false,
    var questionnaireCount: Int = 0,
    var teleMode: String? = null,
    var appointmentCancelOption: String? = null,
)

data class AppointmentStatus(//

    //TODO:clean
    var statusId: String? = null,// "status_id": "100001000000000131",
    var status: String? = null, //  "status": "Confirmed",
    var statusColor: String? = null, //"status_color": "#008000:#ffffff",
    var isSystemGenerated: Boolean? = null, // "is_system_generated": "1",
    var isDeleted: Boolean? = null, //"is_deleted": "0",
    var position: Int? = null, // "position": "0",
    var isDefaultStatus: Boolean? = null //"is_default_status": "0"

)

data class VisitTypeDetail(//

    var visitTypeId: String? = null,
    var visitTypeDuration: String? = null,
    var visitTypeColor: String? = null,
    var visitTypeAppointmentMode: String? = null,
    var visitTypeCharge: String? = null,
    var visitType: String? = null
)

@Parcelize
data class CustomMember(
    var memberId: String? = null,
    var fullName: String? = null,
    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    var specialization: String? = null,
    var prefix: String? = null,
    var isSelected: Boolean? = null
):Parcelable

data class Member(//
    //
    var memberId: String? = null,
    var fullName: String? = null,
    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    var deaNumber: String? = null,
    var degree: String? = null,
    var specialization: String? = null,
    var gender: String? = null,//Allowed values male, female
    var dateOfBirth: DateTime? = null,
    var bloodGroup: String? = null,
    var postalAddress: PostalAddress? = null,
    var contactDetails: ContactDetails? = null,
    var prefix: String? = null,
    var npi: String? = null,
    var memberStatus: String? = "Enabled",
    var createdDate: DateTime? = null,
    var idQualifier: Long? = null,
    var id: String? = null,
    var ssn: String? = null,
    var photoURI: String? = null,
    var isPhotoAvailable: Boolean? = null,
    var isMemberOnlineAppEnabled: Boolean? = null,
    var introduction: String? = null,
    var memberOnlineAppTiming: String? = null,
    var memberAvailability: ArrayList<MemberAvailability> = ArrayList(),
    var memberWorkTiming: ArrayList<MemberWorkTiming> = ArrayList(),

    var facilityList: ArrayList<Facility> = arrayListOf(),
    var practiceId: String? = null,
    var practice: PracticeList? = null,
    var isSelected:Boolean=false,

    var nameWithTitle: String? = null,
)

data class MemberWorkTiming(//
    var memberTimingId: String? = null,
    var memberId: String? = null,
    var facilityId: String? = null,
    var dayOfWeek: Int? = null,
    var isVisiting: Boolean? = null,
    var visitNo: String? = null,
    var startTime: String? = null,
    var startSession: String? = null,
    var endTime: String? = null,
    var endSession: String? = null,
    var onlineAppSlot: Boolean? = null,
    var tokenCount: Int? = null,
    var isMemberWorking: Boolean = true
)

data class MemberAvailability(//

    var startTime: DateTime? = null,
    var endTime: DateTime? = null

)


data class SlotMetaData(

//        var memberAvailability: ArrayList<MemberAvailability> = arrayListOf<MemberAvailability>(),
    var memberAvailability: HashMap<DateTime, ArrayList<MemberAvailability>> = HashMap(),
    var showWaitingList: Boolean = false,
    var memberWorkTiming: ArrayList<MemberWorkTiming> = arrayListOf()

)


data class MedicationCommonData(
    var providerList: ArrayList<String> = arrayListOf(),
    var searchString: ArrayList<String> = arrayListOf(),
    var intakeList: ArrayList<DataWithId> = arrayListOf(),
    var intakeWhenList: ArrayList<DataWithId> = arrayListOf(),
    var medNameList: ArrayList<DataWithId> = arrayListOf(),
    var routeList: ArrayList<String> = arrayListOf(),
    var rxUnitList: ArrayList<String> = arrayListOf(),
    var rxStrengthUnitList: ArrayList<String> = arrayListOf(),
    var rxUnitAndName: ArrayList<DataWithId> = arrayListOf()

)

data class SupplementCommonData(
    var manufacturerList: ArrayList<String> = arrayListOf(),
    var searchString: ArrayList<String> = arrayListOf(),
    var intakeList: ArrayList<DataWithId> = arrayListOf(),
    var intakeWhenList: ArrayList<DataWithId> = arrayListOf(),
    var sxUnitList: ArrayList<String> = arrayListOf(),
    var sxStrengthUnitList: ArrayList<String> = arrayListOf(),
    var sxUnitAndName: ArrayList<DataWithId> = arrayListOf()

)


data class DataWithId(

    var key: String = "",
    var name: String = ""
)

data class StateList(

    var countryCode: String = "",
    var stateList: ArrayList<String> = arrayListOf(),
)

data class DataWithBoolean(

    var key: String = "",
    var value: Boolean = false
)

data class PatMedGroupByDrugPOJO(
    val drugId: String,
    val patMedList: ArrayList<PatMedPOJO> = arrayListOf(),
)

data class PatMedPOJO(//
    //
    var patMed: PatientMedications? = null,
    var patMedExt: List<PatientMedicationsExt> = ArrayList(),
    var drugDetail: List<DrugDetails> = ArrayList(),
    var rxSIGs: List<RxSIG> = ArrayList(),
    var patMedPHRExt: List<PatientPrescriptionHistory> = ArrayList(),
    var member: Member? = null,
    var isSelected: Boolean = false,
    var reasonForRefill: String? = null,
    var isHealthPassport: Boolean = false,
    var allowRefill: Boolean = false,
    var status: String? = null,
    var practiceId: String? = null,
    var uiType: String? = null,
    var reminders: List<ReminderPOJO> = ArrayList()
)

data class PatientMedications(//
    //
    var localPatientMedicationId: Long = 0,
    var patientMedicationId: String? = null,
    var dateOfEntry: DateTime? = null,
    var drugDetailsId: String? = null,
    var localDrugDetailsId: Long? = null,
    var practicePatientId: String? = null,
    var localPracticePatientId: Long? = null,
    var startDate: DateTime? = null,
    var stopDate: DateTime? = null,
    var dosageUnitId: String? = null,
    var intakeRoute: String? = null,
    var isActive: Boolean? = null,
    var comment: String? = null,
//    var addedBy: String? = null,
    var addedAs: String? = null,
    var timeOfEntry: DateTime? = null,
    var faxStatus: String? = null,
    var isPositiveIdSigned: Boolean = false,
    var signStatus: Int = 0,
//    var SignedBy: String? = null,
//    var SignedOn: String? = null,
    var transmitStatus: String? = null,
    var facilityId: String? = null,
    var localFacilityId: Long? = null,
    var writtenDate: DateTime? = null,
    var isAddedByPatient: Boolean = true,

    var dataInString: String = "",
    var practiceId: String? = null,
    var reminderDataInString: String? = ""
)

data class PatientMedicationsExt(//
    //
    /*@PrimaryKey*/ var localPatientMedicationId: Long = 0,
    var patientMedicationId: String? = null,
    var dispense: String? = null,
    var refill: String? = null,
    var substituteGeneric: Boolean? = false,
    var manufacturingType: String? = null,
    var directions: String? = null,
    var dispenseUnit: String? = null,
    var directionEdited: Boolean = false,
    var priorAuthorizationNumber: String? = null,
    var priorAuthorizationStatus: String? = null,//A/D/F/N/R - allowed values
    var earliestFillDate: DateTime? = null,
    var daysOfSupply: Int? = null,
    var dispensedCount: Int? = null,
    var isVoid: Boolean = false,
    var internalComments: String? = null,

//        @Ignore
    var containerQuantity: String? = null,
//        @Ignore
    var isContainerProduct: Boolean = false
)

data class DrugDetails(//
    //
    var localDrugDetailsId: Long = 0,
    var drugDetailsId: String? = null,
    var genericDrugId: String? = null,
    var genericProductId: String? = null,
    var tradeNameId: String? = null,
    var genericDrugName: String? = null,
    var genericProductName: String? = null,
    var eRxDisplayName: String? = null,
    var tradeName: String? = null,
    var strengthDescription: String? = null,
    var doseformDescription: String? = null,
    var routeDescription: String? = null,
    var isCustom: Boolean = false,
    var isActive: Boolean = true,
    var isSupplies: Boolean? = null,
    var otc: String? = null,
    var brand: String? = null,
    var isDeleted: Boolean = false,
    var rxnormCode: String? = null,
    var localUpdatedTime: DateTime? = null
)

data class RxSIG(//
    var localPatientMedicationId: Long = 0,
    var patientMedicationId: String? = null,
    var sigCount: Int = 1,
    var dosage: String? = null,
    var sig: String? = null,
    var durationInDays: String? = null,
    var durationUnit: String? = null
)

data class Facility(//
    //
    var facilityId: String? = null,
    var facilityName: String? = null,
    var facilityCode: String? = null,
    var npi: String? = null,
    var postalAddress: PostalAddress? = null,
    var contactDetails: ContactDetails? = null,
    var qualifierId: String? = null,
    var nonNpi: String? = null,
    var facilityLogo: String? = null,
    var status: String = "Enabled",
    var gstin_num: String? = null,
    var facilityTypeCode: Char? = null,
    var timezone: String? = null,

    var practiceId: String? = null,
    var directions: String? = null
)

data class PatientPrescriptionHistory(//
    var dosageWithUnit: String? = null,
    var intake: String? = null,
    var intakeRoute: String? = null,
    var intakeWhen: String? = null,
    var intakeUnit: String? = null,
    var noOfIntakes: String? = null,
    var dosage: String? = null,
    var dosageUnit: String? = null,
    var dosageUnitId: String? = null,

    var fromDate: DateTime? = null,
    var toDate: DateTime? = null,
    var medicalConditionId: String? = null,
    var medicalConditionName: String? = null,
    var refills: String? = null,
    var comments: String? = null,

    var patientId: String? = null,

    var prescriptionHistoryId: String? = null
)

data class PHRBills(
    var phrBillId: String? = null,
    var patientPracticeMapId: String? = null,
    var ehrBillId: String? = null,
    var billNumber: String? = null,
    var billDate: DateTime? = null,
    var amount: String? = null,
    var billAmount: String? = null,
    var dueAmount: String? = null,
    var billType: String? = null,
    var status: String? = null,
    var facilityId: String? = null,
    var facility: Facility? = null,
    var sharedBy: String? = null,
    var time: String? = null,
    var isPaid: Boolean? = null,
    var isRead: Boolean? = null,
    var isStatement: Boolean = false,
    var statementId: String? = null,
    var practiceCurrencySymbol: String? = null,
    var practiceId: String? = null,
    var paymentGateway: String? = null,
    var memberId: String? = null,
    var merchantAccountId: String? = null,
    var isPartialPaymentAllowed: Boolean? = null,
    var partialPaymentMinAmount: String? = null

)

data class PHRInvoicesDetail(
    var phrBillNumber: String? = null,
    var billAmount: String? = null,
    var dueAmount: String? = null,
    var billDate: DateTime? = null,
    var patientRecordId: String? = null,
    var patientId: String? = null,
    var patientName: String? = null,
    var paymentRequested: String? = null,
    var status: String? = null
)


data class PHRStatementDetail(
    var invoicesDetails: ArrayList<PHRInvoicesDetail>? = null,
    var isPartialPaymentAllowed: Boolean? = null,
    var partialPaymentMinAmount: String? = null,
    var statementId: String? = null,
    var merchantAccountId: String? = null,
    var requestedAmount: String? = null,
    var memberId: String? = null,
    var isPaid: Boolean? = null,
    var facilityId: String? = null
)


data class PHRReceipts(
    var phrReceiptId: String? = null,
    var patientPracticeMapId: String? = null,
    var ehrReceiptId: String? = null,
    var receiptNumber: String? = null,
    var receiptDate: DateTime? = null,
    var receiptAmount: String? = null,
    var usedAmount: String? = null,
    var status: String? = null,
    var details: String? = null,
    var facilityId: String? = null,
    var facility: Facility? = null,
    var sharedBy: String? = null,
    var time: String? = null,
    var filePath: String? = null,
    var isRead: Boolean = false,
    var sentFromEhr: Boolean? = null,
    var practiceCurrencySymbol: String? = null,
    var practiceId: String? = null
)

data class PHRReceiptData(
    var phrReceiptDataId: String? = null,
    var phrReceiptId: String? = null,
    var filePath: String? = null,
    var receiptAmount: String? = null,
    var time: String? = null,
    var sharedBy: String? = null
)

data class Messages(
    var messageId: String? = null,
    var subject: String? = null,
    var content: String? = null,//contentFile
    var type: String? = null,
    var priority: String? = null,
    var hasAttachments: Boolean? = null,
    var practicePatientId: String? = null,
    var messageDate: DateTime? = null,//time
    var messageRecipient: MessageReceipients? = null,
    var isSelected: Boolean = false,
    var messagesAttachmentsMapList: ArrayList<MessagesAttachmentsMap> = arrayListOf(),
    var messageMembersMapList: ArrayList<MessageMembersMap> = arrayListOf(),
    var messageGroupMapList: ArrayList<MessageGroupMap> = arrayListOf(),
    var messagePatientList: ArrayList<Patient> = arrayListOf(),
    var toString: String = "",

    var practiceId: String? = null,
    var ownerId: String?=null
)

data class MessageReceipients(
    var messageRecipientId: String? = null,
    var messageId: String? = null,
    var fromId: String? = null,
    var fromSpace: String? = null,
    var fromName: String? = null,
    var toId: String? = null,
    var toSpace: String? = null,
    var toName: String? = null,
    var status: Int = ModelUtil.messageStatusUnSeen
)

data class MessagesAttachmentsMap(
    var attachmentId: String? = null,
    var messageId: String? = null,
    var attachmentName: String? = null,
    var attachmentLocation: String? = null
)

data class MessageMembersMap(
    var mapId: String? = null,
    var messageId: String? = null,
    var additionalMemberId: String? = null,
    var member: Member? = null,
    var isReceiver: Boolean = true
)

data class MessageGroupMap(
    var mapId: String? = null,
    var messageId: String? = null,
    var groupId: String? = null,
    var group: MessageGroup? = null,
    var isReceiver: Boolean = true
)


data class ComposeMsgRestriction(

    var isMessageRestrict: Boolean = false,
    var allowPHRMessaging: Boolean = true,
    var noOfCharsAllowed: Int = 0,
    var practiceId: String = ""


)

data class ShareDocRestriction(
    var isShareAllowed: Boolean = false,
    var practiceId: String = ""
)

//region Lab

data class Lab(//LabList
    var labId: String? = null,
    var labName: String? = null,
    var labDisplayName: String? = null,
    var isApproved: Boolean? = null,
    var addedBy: String? = null
)

data class MedicalRecords(
    var medicalRecordId: String? = null,
    var medicalRecordName: String? = null,
    var medicalRecordDisplayName: String? = null,
    var labId: String? = null,
    var isApproved: Boolean? = null,
    var addedBy: String? = null
)


data class MedicalRecordParams(
    var medicalRecordParamId: String? = null,
    var medicalRecordId: String? = null,
    var parameterName: String? = null,
    var parameterType: String? = null,
    var parameterSubType: String? = null,
    var recordunit: String? = null,
    var referenceMax: String? = null,
    var referenceMin: String? = null,
    var testNo: String? = null
)

data class MedicalRecordGroup(
    var groupId: String? = null,
    var reportStatus: String? = null,
    var labReferenceNumber: String? = null,
    var comments: String? = null,
    var isElectronicallyReceived: Boolean? = null,
    var additionalDetails: String? = null,
    var messageControlId: String? = null,
    var sendingFacility: String? = null,
    var pathologyReviewStatus: String? = null,
    var labName: String? = null,
    var labId: String? = null,
    var labOrderId: String? = null,
    var medicalRecordEntryList: ArrayList<MedicalRecordEntry> = arrayListOf(),
    var isViewOpened: Boolean = false,
    var practiceId: String? = null
)

data class MedicalRecordEntry(
    var medicalRecordEntryId: String? = null,
    var medicalRecordId: String? = null,
    var patientId: String? = null,
    var date: DateTime? = null,
    var importedTime: String? = null,
    var isPDFAvailable: Boolean = false, //custom
    var pdfDocument: String? = null,
    var importedBy: String? = null,
    var isAbnormalResult: Boolean? = null,
    var isImportedByPatient: Boolean? = null,
    var medicalRecord: MedicalRecords? = null,
    var mrParamValueMapList: ArrayList<MedicalRecordParamValueMap> = arrayListOf(),   // mr => medicalRecord
    var reviewMedicalRecordEntry: ReviewMedicalRecordEntries? = null,
    var isViewOpened: Boolean = false,
    var viewHeight: Int = 0,
    var interpretation: String? = null
)

data class ConsultationMedRecordsMap(
    var consultationMedMapId: String? = null,
    var medicalRecordEntryId: String? = null,
    var consultationId: String? = null
)

data class ReviewMedicalRecordEntries(
    var reviewMedicalRecordEntryId: String? = null,
    var medicalRecordEntryId: String? = null,
    var reviewerId: String? = null,
    var reviewerName: String? = null,
    var status: String? = null,
    var reviewComments: String? = null,
    var messageToPatient: String? = null,
    var userMedicalRecordEntryId: String? = null
)

data class PatientPracticeMedRecords(
    var patientPracticeMedRecordId: String? = null,
    var practiceId: String? = null,
    var medicalRecordEntryId: String? = null
)

data class MedicalRecordShareStatus(
    var medicalRecordId: String? = null,
    var sharedBy: String? = null,
    var sharedTo: String? = null
)

data class MedicalRecordValues(
    var medicalRecordEntryId: String? = null,
    var medicalRecordParamId: String? = null,
    var recordvalue: String? = null,
    var referenceMax: String? = null,
    var referenceMin: String? = null,
    var interpretation: String? = null,

    var range: String? = null
)


data class MedicalRecordParamValueMap(
    var medicalRecordParam: MedicalRecordParams? = null,
    var medicalRecordValue: MedicalRecordValues? = null,
    var viewHeight: Int = 0
)

data class LabOrder(
    //
    var labOrderId: String? = null,
    var practicePatientId: String? = null,
    var localPracticePatientId: Long? = null,
    var memberId: String? = null,
    var localMemberId: Long? = null,
    var date: DateTime? = null,
    var facilityId: String? = null,
    var localFacilityId: Long? = null,
    var status: Int? = 0, //0 -> Pending / 1 -> Partial/ 2 -> Completed
    var consultationId: String? = null,
    var localConsultationId: Long? = null,
    var labNotes: String? = null,
    var intraOfficeNotes: String? = null,
    var labReferenceNumber: Long? = null,
    var specimenCollectionDate: DateTime? = null,
    var additionalComments: String? = null,
    var timeOfEntry: DateTime? = null,
    var lastModifiedTime: String? = null,

    var member: Member? = null,

    var pdfName: String? = null
)

data class LabOrderTestMap(
    //
    var localOrderTestMapId: Long = 0,
    var orderTestMapId: String? = null,
    var labOrderId: String? = null,
    var localLabOrderId: Long? = null,
    var medicalRecordId: String? = null,
    var localMedicalRecordId: Long? = null,
    var labId: String? = null,
    var localLabId: Long? = null,
    var status: Int = 0, //Status of the Test Result - 1 -> Completed / 0 -> Pending
    var specimenConditionTemperature: String? = null,
    var testType: String? = null,
    var comment: String? = null,
    var labRecordId: String? = null,
    var localLabRecordId: Long? = null,
    var profileIndicator: Boolean? = false,
    var zSegmentAoeMap: String? = null,
    var specimenType: String? = null,
    var labName: String? = null,
    var medicalRecordName: String? = null,
    var labRecordCode: String? = null

)

data class LabOrderPOJO(
    var labOrder: LabOrder? = null,
    var labOrderTestMapList: ArrayList<LabOrderTestMap> = arrayListOf()
)

data class LabOrderDetail(

    var fileSize: String? = null,
    var isDeleted: String? = null,
    var practiceId: String? = null,
    var senderId: String? = null,
    var sharedDate: String? = null,
    var ehrFileEntryId: String? = null,
    var receiverId: String? = null,
    var fileId: String? = null,
    var fileName: String? = null,
    var uploadedBy: String? = null,
    var date: DateTime? = null,
    var readStatus: Boolean = false,
    var timeOfUpload: String? = null,
    var fileType: String? = null,
    var messageToPatient: String? = null,
    var phrFileMapId: String? = null,
    var member: Member? = null,
    var isViewOpened: Boolean = false
)

//endregion

data class Allergy(
    //
    var localAllergyId: Long = 0,
    var allergyId: String? = null,
    var allergy: String? = null,
    var allergyType: String? = null,
    var allergyOtherType: String? = null,
    var localUpdatedTime: DateTime? = null
)

data class PatientAllergy(
    //
    var allergyMapId: String? = null,
    var allergyId: String? = null,
    var localAllergyId: Long? = null,
    var practicePatientId: String? = null,
    var localPracticePatientId: Long? = null,
    var severity: String? = null,
    var reactions: String? = null,
    var dateOfEntry: DateTime? = null,
    var observedDate: DateTime? = null,
    var resolvedDate: DateTime? = null,
    var status: String? = null,//isActive: Boolean
    var addedByPatient: Boolean = false,
    var allergy: Allergy? = null,
    var isSelected: Boolean = false,
    var isHealthPassport: Boolean = false,
    var practiceId: String? = null,
    var toString: String = "",
    var isViewOpened: Boolean = false
)

data class PatientAllergyCommonData(
    var allergyTypeList: ArrayList<String> = arrayListOf(),
    var allergySeverityList: ArrayList<String> = arrayListOf(),
    var allergyStatusList: ArrayList<String> = arrayListOf()
)

data class PatientSupplementMap(
    var supplementMapId: String? = null,
    var patientId: String? = null,
    var supplementId: String? = null,
    var manufacturerId: String? = null,
    var patientSupplementHistory: PatientSupplementHistory? = null
)

data class PatientSupplementHistory(
    var supplementHistoryId: String? = null,
    var supplementMapId: String? = null,
    var fromDate: DateTime? = null,
    var toDate: DateTime? = null,
    var dosage: String? = null,
    var dosageUnitId: String? = null,
    var frequency: String? = null,
    var intake: String? = null,
    var intakeUnit: String? = null,
    var monitoringPeriod: String? = null,
    var intakeRoute: String? = null,
    var comments: String? = null,
    var numberOfRefills: String? = null,
    var durationInDays: String? = null,
    var intakeWhen: String? = null,
    var numberOfIntake: String? = null,
    var dosageUnit: DosageUnit? = null,

    var instructions: String? = null
)

data class DosageUnit(
    var dosageUnitId: String? = null,
    var dosageUnit: String? = null
)

data class SupplementList(
    var supplementId: String? = null,
    var supplementName: String? = null,
    var supplementType: String? = null,
    var manufacturerId: String? = null,
    var manufacturerSpecific: Boolean? = null,
    var patientCount: String? = null,
    var approvalStatus: String? = null,
    var addedBy: String? = null
)

data class PatSupplementsGroupPOJO(
    val supplementId: String,
    val patientSupplementList: ArrayList<PatientSupplements> = arrayListOf(),
)

data class PatientSupplements(
    var patientSupplementId: String? = null,
    var dateOfEntry: DateTime? = null,
    var supplementId: String? = null,
    var manufacturerId: String? = null,
    var practicePatientId: String? = null,
    var dosage: String? = null,
    var dosageUnitId: String? = null,
    var intake: String? = null,
    var startDate: DateTime? = null,
    var stopDate: DateTime? = null,
    var isActive: Boolean? = null,
    var supplement: SupplementList? = null,
    var patientSupplementMap: PatientSupplementMap? = null,
    var manufacturer: Manufacturer? = null,
    var isAddedByPatient: Boolean = false,
    var member: Member? = null,
    var isViewOpened: Boolean = false,
    var isSelected: Boolean = false,

    var allowRefill: Boolean = false,
    var reasonForRefill: String? = null,
    var practiceId: String? = null,
    var toString: String = "",
    var reminders: List<ReminderPOJO> = ArrayList(),
    var reminderDataInString: String? = null
)

data class Manufacturer(
    var manufacturerId: String? = null,
    var manufactureName: String? = null
)

data class PatientEncounterList(
    var patientEncounterId: String? = null,
    var practiceId: String? = null,
    var physicianId: String? = null,
    var patientId: String? = null,
    var date: DateTime? = null,
    var reason: String? = null,
    var fileId: String? = null,
    var consultation: Consultation? = null,
    var member: Member? = null,
    var facility: Facility? = null,
    var readStatus: Boolean = false
)

data class Consultation(
    //
    var consultationId: String? = null,
    var isApproved: Boolean? = false,
    var createdBy: String? = null,
    var localCreatedBy: Long? = null,
    var approvedBy: String? = null,
    var localApprovedBy: Long? = null,
    var timeOfCreation: DateTime? = null,
    var isRenewal: Boolean? = false,
    var isPaperRecord: Boolean? = false,
    var consultationTitle: String? = null,
    var chartNotesType: String? = null,
    var isBillingNeeded: Boolean? = true,
    var isBilled: Boolean? = false,
    var isOtherInvBilled: Boolean? = false,
    var visitTypeId: String? = null,
    var pricingProfileId: String? = null
)

data class ProceduresList(//PHRProceduresList
    var procedureId: String? = null,
    var procedure: String? = null,
    var procedureType: String? = null,
    var otherType: String? = null
)

data class ProceduresMap(//PHRProceduresMap
    var proceduresMapId: String? = null,
    var procedureId: String? = null,
    var practicePatientId: String? = null,
    var fromDate: DateTime? = null,
    var toDate: DateTime? = null,
    var dateOfEntry: DateTime? = null,
    var notes: String? = null,
    var isSelected: Boolean = false,
    var isHealthPassport: Boolean = false,
    var procedure: ProceduresList? = null,
    var toString: String = ""
)

data class ProceduresListCommonData(
    var procedureTypeList: ArrayList<String> = arrayListOf()
)

data class Module(
    var moduleId: String = "",
    @StringRes var moduleName: Int = 0,
    @DrawableRes var moduleIconId: Int = 0,
    var unreadCount: Int = 0,
    var moduleServerId: String? = null,
    var isShown: Boolean = true,
    var moduleNameFromSection: String? = null,
    var position: Int = 0,
    var changeBg: Boolean = false
)

data class AppointmentRequest(//
    var requestId: String? = null,
    var practicePatientId: String? = null,
    var physicianId: String? = null,
    var reason: String? = null,
    var appointmentType: String? = null,
    var appointmentStatus: String? = null,
    var timezone: String? = null,
    var physicianRemark: String? = null,
    var reminderOptions: String? = null,
    var reminderType: String? = null,
    var datePreference: String? = null,
    var dateChoice1: DateTime? = null,
    var dateChoice2: DateTime? = null,
    var dateChoice3: DateTime? = null,
    var timePreference: String? = null,
    var startHour: String? = null,
    var startMinutes: String? = null,
    var startSession: String? = null,
    var endHour: String? = null,
    var endMinutes: String? = null,
    var endSession: String? = null,
    var addAsWaitingList: Boolean = false,
    var facility: Facility? = null,
    var member: Member? = null,
    var isEnabled: Boolean = false,
    var visitType: VisitTypes? = null,
    var duration: Int? = null,
    var practiceId: String? = null,
    var receiptId: String? = null,


    var paymentInfo: PaymentInfo? = null

)


data class AppointmentConfig( //OnlineAppConfiguration
    var appointmentConfigId: String? = null,
    var providerId: String? = null,
    var facilityId: String? = null,
    var appSlotNewPatient: String? = null,
    var appSlotOldPatient: String? = null,
    var appApprovalPreferences: String? = null,
    var isEnabled: Boolean = true,
    var visitTypeIds: String? = null,
    var showVisittype: Boolean? = null,
    var showVisitDuration: Boolean = false,
    var isCardProcessingEnabled: Boolean = false,
    var cardProcessingType: String? = null,
    var chargeType: String? = null,
    var chargeAmount: String? = null,
    var patientNote: String? = null,
    var allowAddWaitlist: Boolean? = null,
    var isTokenBased: Boolean? = null,
    var slotsOpeningPriorHours: Int? = null,
    var onlineBookingDaysLimit: Int? = null,
    var questionnaireId: String? = null,
    var paymentGateway: String? = null,

    var currentFacilityTimezone: DateTime? = null,
    var facilityTimezone: String? = null,
    var isTelehealthEnabled: Boolean = false,
    var visitTypeList: ArrayList<VisitTypes> = arrayListOf(),
    var interval: Int = 0,
    var memberWorkTimingList: ArrayList<MemberWorkTiming> = arrayListOf(),
    var merchantAccountId: String? = null,
    var paymentCurrencySymbol: String? = "Rs",
    var paymentCurrency: String? = "INR",

    var showAppOnPatTimeZone: Boolean = false

)

data class VisitTypes(//
    var visittypeId: String? = null,
    var visitType: String? = null,
    var duration: Int? = null,
    var visittypeColor: String? = null,
    var charge: String? = null,
    var appointmentMode: String? = null,
    var isDeleted: Boolean? = null
)

data class AppointmentVisitMap(
    var appointmentId: String? = null,
    var visittypeId: String? = null
)


data class PaymentInfo(//
    var cardNumber: String? = null,
    var cardHolderName: String? = null,
    var cardExpMonth: String? = null,
    var cardExpYear: String? = null,
    var cardCvv: String? = null,
    var chargeAmount: String? = null,
    var token: String? = null,
    var chargeProcessingType: String? = null,
    var chargeType: String? = null,
    var transactionType: String? = null,
    var transactionInfo: TransactionInfo? = null,
    var isPrimaryCard: Boolean = false,
    var postalAddress: PostalAddress? = null,
    var cardNickName: String? = null
)

data class TransactionInfo(//
    var patientId: String? = null,
    var facilityId: String? = null,
    var practiceId: String? = null,
    var memberId: String? = null,
    var merchantAccountId: String? = null,
    var billId: String? = null,
    var service: String? = null,
    var paymentAmount: String? = null,
    var currentDate: DateTime? = null,
    var transactionId: String? = null,
    var razorPayOrderId: String? = null,
    var razorPayPaymentId: String? = null,
    var clientId: String? = null,
    var publicToken: String? = null,
    var paymentAntFromAPI: String? = null,
    var emailId: String? = null,
    var mobile: String? = null,
    var gateway: String? = null

)

class PaymentGatewayToken { //PaymentGatewayTokens
    var tokenId: String? = null
    var merchantAccountId: String? = null
    var facilityId: String? = null
    var patientId: String? = null
    var cardDigit: String? = null
    var expiry: String? = null // in mmyy format
    var cardBrand: String? = null
    var isRecurringToken: Boolean = false
    var nameOnCard: String? = null
    var cardNickName: String? = null
    var expiryDate: DateTime? = null
    var createdMerchantTxnId: String? = null
    var lastSuccessfulTxnId: String? = null
    var lastSuccessfulTxnDate: String? = null
    var lastFailedTxnDate: String? = null
    var lastFailedTxnStatus: String? = null
    var isPHRCard: Boolean = false
    var isPrimaryCard: Boolean = false
    var postalAddress: PostalAddress? = null
    var cardGroupId: String? = null
    var practiceId: String? = null
    var allowPracticeCardDeletion: Boolean = false
    var hasCardExpired: Boolean = false
    var practicePatientId: String? = null
    var token: String? = null
}

class PatientCardGroup {
    var cardGroupId: String? = null
    var practicePatientId: String? = null
}

class MerchantAccountInfo {
    var merchantAccountId: String? = null
    var type: String? = null
    var facilityId: String? = null
    var gateway: String? = null
    var title: String? = null
    var accountId: String? = null
    var apiKey: String? = null
    var group: String? = null
    var profileId: String? = null
    var addedBy: String? = null
    var tokenType: String? = null
    var expiresIn: String? = null
    var accessToken: String? = null
}

class CardOnFilePOJO( //custom POJO
    var patientSavedCardArrayList: ArrayList<PaymentGatewayToken> = ArrayList(),
    var practiceSavedCardArrayList: ArrayList<PaymentGatewayToken> = ArrayList(),
    var practiceId: String? = null,
    var countryStr: String? = null,
    var postalAddress: PostalAddress? = null
)

data class VaccinesList(
    var vaccineId: String? = null,
    var cvxCode: String? = null,
    var vaccineName: String? = null,
    var vaccineDetails: String? = null,
    var addedBy: String? = null
)

data class PatientVaccineMap(
    var vaccineMapId: String? = null,
    var patientId: String? = null,
    var vaccineId: String? = null,
    var dateOfVaccination: DateTime? = null,
    var administeredAmount: String? = null,
    var administeredUnits: String? = null,
    var vaccineLotNumber: String? = null,
    var vaccineManufacturerId: String? = null,
    var vaccineNotes: String? = null,

    var vaccine: VaccinesList? = null,
    var vaccineManufactures: VaccineManufactures? = null,
    var orderingProvider: Member? = null,

    var route: String? = null,
    var site: String? = null
)

data class VaccineManufactures(
    var vaccineManufacturerId: String? = null,
    var manufacturerName: String? = null,
    var manufacturerCode: String? = null
)


data class ImmunizationList(
    var immunizationId: String? = null,
    var immunizationName: String? = null,
    var immunizationType: String? = null,
    var patientCount: String? = null,
    var approvalStatus: String? = null,
    var addedBy: String? = null
)

data class PatientImmunizationMap(
    var immunizationMapId: String? = null,
    var patientId: String? = null,
    var immunizationId: String? = null,
    var immunizationList: ImmunizationList? = null,
    var patientImmunizationHistory: PatientImmunizationHistory? = null
)

data class PatientImmunizationHistory(
    var immunizationHistoryId: String? = null,
    var immunizationMapId: String? = null,
    var vaccinesOn: DateTime? = null,
    var givenBy: String? = null,
    var howAdministered: String? = null,
    var manufacturer: String? = null,
    var comments: String? = null
)


data class VaccineImmunizationPOJO(

    var addedByPatient: Boolean = true,
    var patientImmunizationMap: PatientImmunizationMap? = null,
    var patientVaccineMap: PatientVaccineMap? = null,
    var practiceId: String? = null,
    var toString: String = "",
    var isViewOpened: Boolean = false
)

data class PatientImmunizationCommonData(

    var administeredList: ArrayList<String> = arrayListOf(),
    var providerList: ArrayList<Member> = arrayListOf(),
    var searchString: ArrayList<String> = arrayListOf()

)


data class Announcements(

    var announcementId: String? = null,
    var content: String? = null,
    var detailedContent: String? = null,
    var isDeleted: Boolean? = null,
    var isRead: Boolean = false,
    var practiceAnnouncementId: String? = null,
    var practiceId: String? = null,
    var senderId: String? = null,
    var status: String? = null,
    var subject: String? = null,
    var time: DateTime? = null

)


data class UsefulInfo(

    var infoId: String? = null,
    var content: String? = null,
    var detailedContent: String? = null,
    var isDeleted: Boolean? = null,
    var isRead: Boolean = false,
    var practiceAnnouncementId: String? = null,
    var practiceId: String? = null,
    var senderId: String? = null,
    var status: String? = null,
    var category: String? = null,
    var time: DateTime? = null
)

data class UnreadCount(

    var billingCount: Int = 0,
    var labUnreadCount: Int = 0,
    var messageUnreadCount: Int = 0,
    var practiceAnnouncementCount: Int = 0,
    var questionnaireUnfilledCount: Int = 0,
    var documentUnreadCount: Int = 0, //SHARED_FILES_UNREAD_COUNT
    var upcomingAppointmentCount: Int = 0,
    var visitSummaryUnreadCount: Int = 0,
    var practiceId: String? = null

)

data class MenuList(
    var appointment: Boolean = true,
    var billing: Boolean = true,
    var message: Boolean = true,
    var lab: Boolean = true,
    var visitSummary: Boolean = true,
    var prescription: Boolean = true,
    var announcement: Boolean = true,
    var usefulInfo: Boolean = true,
    var about: Boolean = true

)

data class SectionPracticeMap(//
    var sectionConfigList: ArrayList<SectionConfig> = arrayListOf(),
    var practiceId: String? = null
)

data class SectionConfigList(//
    var sectionConfigMapList: ArrayList<SectionPracticeMap> = arrayListOf(),
    var toString: String = ""
)

data class SectionConfig(//
    var sectionName: String? = null,
    var allowPHREntry: Boolean = false,
    var isDefaultEnabled: Boolean = false,
    var sectionOrder: Int = 0,
    var isEnabled: Boolean = false,
    var section: String? = null
)

data class PatientQuesMap(
    var patientQuesMapId: String? = null,
    var appointmentId: String? = null,
    var practicePatientId: String? = null,
    var templateId: String? = null,
    var isFilled: Boolean = false,
    var allowPhrEditing: Boolean = false,
    var isSubmitted: Boolean = false,
    var memberId: String? = null,
    var facilityId: String? = null,
    var providerId: String? = null,
    var departmentId: String? = null,
    var lastModifiedTime: String? = null,
    var timeOfSharing: String? = null,
    var randomKey: String? = null,
    var appointment: Appointment? = null
)

data class PhysicianTemplates(
    var templateId: String? = null,
    var memberId: String? = null,
    var name: String? = null,
    var type: String? = null,
    var timeOfCreation: String? = null,
    var comments: String? = null,
    var tags: String? = null,
    var isPracticeTemplate: Boolean? = null,
    var isDeleted: Boolean? = null,
    var isEditable: Boolean? = null,
    var customFunction: String? = null,
    var layout: String? = null,
    var patientQuesMap: PatientQuesMap? = null,
    var member: Member? = null,
    var practiceId: String? = null,
    var toString: String = "",
    var isHealthPassport: Boolean = false
)

data class ZoomCredentials(

    var webDomain: String? = null,
    var userId: String? = null,
    var userType: String? = null,
    var meetingId: String? = null,
    var displayName: String? = null,
    var zak: String? = null,
    var sdkJwt: String? = null

)

data class OpenTokCredentials(
    var joinUrl: String? = null,
    var password: String? = null
)

data class TeleHealthCredentials(
    val credentials: Any,
    val isConsentFormAvailable: Boolean = false,
    val consentFormIds: ArrayList<String> = arrayListOf()
)


data class IntakeQuestTemplateEntry(

    var entryId: String = "",
    var templateId: String = "",
    var notes: String? = null,
    var notesType: String? = null,
    var isMultiChoice: Boolean = false,
    var fromScale: Int? = null,
    var ratingDescription: ArrayList<String> = arrayListOf(),
    var toScale: Int? = null,
    var position: Int? = null,
    var isDeleted: Boolean = false,
    var isMandatory: Boolean = false,
    var answersList: ArrayList<String>? = arrayListOf(),

    var toBeDeleted: Boolean = false,
    var isCollapsible: Boolean = true,
    var expandByDefault: Boolean = false
)


data class IntakeQuestTemplateOption(
    var optionId: String = "",
    var entryId: String = "",
    var option: String? = null
)

data class SOAPTemplateTableCell(
    var type: String = "",
    var value: String = "",
    var colspan: String = ""
)

data class IntakeQuestTemplateRating(
    var option: String? = null,
    var optionDescription: String? = null,
)

data class IntakeQuestTemplateLabels(
    var labelId: String = "",
    var entryId: String = "",
    var fontSize: Int = 14,
    var fontStyle: String = "",
    var fontWeight: String = "",
    var textAlign: String = "",
    var textDecoration: String = ""
)

data class IntakeQuestAllergyEntry(
    var allergen: String? = null,
    var severity: String? = null,
    var type: String? = null,
    var reactions: String? = null
)

data class IntakeQuestMedicationEntry(
    var drugName: String? = null,
    var intakeDetails: String? = null
)

data class IntakeQuestSupplementEntry(
    var supplementName: String? = null,
    var intakeDetails: String? = null
)

data class QuestionnaireEntryPOJO(
    var questionnaireEntry: IntakeQuestTemplateEntry = IntakeQuestTemplateEntry(),
    var questionnaireOptions: ArrayList<IntakeQuestTemplateOption> = arrayListOf(),
    var questionnaireRating: ArrayList<IntakeQuestTemplateRating> = arrayListOf(),
    var questionnaireLabel: IntakeQuestTemplateLabels = IntakeQuestTemplateLabels(),
    var questionnaireAllergyList: ArrayList<IntakeQuestAllergyEntry> = arrayListOf(),
    var questionnaireMedicationList: ArrayList<IntakeQuestMedicationEntry> = arrayListOf(),
    var questionnaireSupplementList: ArrayList<IntakeQuestSupplementEntry> = arrayListOf(),
    var questionnairePersonalDetails: Patient? = null,
    var questionnairePatContacts: Patient? = null,
    var questionnaireInsurance: PatientInsuranceDetails? = null,
    var soapWidget: SOAPWidgetPOJO = SOAPWidgetPOJO()

)

data class QuestionnaireDetailPOJO(

    var questionnairEntryPOJOList: ArrayList<QuestionnaireEntryPOJO> = arrayListOf(),
    var patientId: String? = null,
    var providerId: String? = null,
    var departmentId: String? = null,
    var patientQuesMapId: String? = null,
    var templateId: String? = null,
    var patientQuesMap: PatientQuesMap? = null,

    var saveNormalQuestionnaire: Boolean = true

)

data class PatientVitals(
    var trackerName: String? = null,
    var metricName: String? = null,
    var dateOfEntry: DateTime? = null,
    var trackerId: String? = null,
    var healthMetricId: String? = null,
    var trackedValue: String? = null,
    var trackedValueUnit: String? = null,
    var notes: String? = null,
    var entryId: String? = null,
    var metricId: String? = null
)

data class PatientVitalTracker(
    var tracker: Tracker? = null,
    var patientVitalsList: ArrayList<PatientVitals> = arrayListOf(),
    var dateOfEntry: DateTime? = null,
    var patientId: String? = null,
    var notes: String? = null
)


data class Metric(
    var metricId: String? = null,
    var metricName: String? = null,
    var metricUnit: String? = null,
    var isDefaultMetric: Boolean = false
)

data class HealthMetric(
    var healthMetricId: String? = null,
    var healthMetricName: String? = null,
    var healthMetricUnit: String? = null,

    var description: String? = null,
    var patientId: String? = null
)

data class Tracker(
    var trackerId: String? = null,
    var trackerName: String? = null,
    var trackerUnitSystem: String? = null,
    var isDefaultTracker: Boolean = false
)

data class TrackerMetricMap(
    var tracker: Tracker? = null,
    var metricList: ArrayList<Metric> = arrayListOf(),
    var toString: String = ""
)

data class WellnessGridEntry(
    var entry: String? = null,
    var isHeading: Boolean = false
)

data class TourPageModel(
    @StringRes var headingId: Int = 0,
    @StringRes var contentId: Int = 0,
    @DrawableRes var sourceId: Int = 0
)

data class Pharmacy(
    var pharmacyId: String? = null,
    var pharmacyName: String? = null,
    var contactType: String? = null,
    var patientPharmaMapId: String? = null,
    var contactDetails: ContactDetails? = null,
    var postalAddress: PostalAddress? = null,
    var patientId: String? = null,
    var isHealthPassport: Boolean = false,
    var toString: String = ""
)

data class Document(
    var fileId: String? = null,
    var fileName: String? = null,
    var fileSize: String? = null,
    var fileType: String? = null,
    var timeOfUpload: DateTime? = null,
    var filePath: String? = null,
    var isHealthPassport: Boolean = false,
    var category: Category? = null,
    var contentType: String? = null,
    var date: DateTime? = null,
    var isAddedByPatient: Boolean = false,

    var sentBy: String? = null,
    var senderId: String? = null, //sender's practice id
    var sharedToList: ArrayList<String> = arrayListOf(),
    var readStatus: Boolean = true


)

data class Category(
    var categoryId: String? = null,
    var categoryName: String? = null,
    var categoryDescription: String? = null,
    var createdOn: DateTime? = null,
    var modifiedOn: DateTime? = null,
    var isDeleted: Boolean = false,


    var isSharedWithMe: Boolean = false,
    var isSharedByMe: Boolean = false
)

data class DocumentMetaData(
    var categoryList: ArrayList<Category> = arrayListOf(),
    var typeList: ArrayList<String> = arrayListOf()
)

data class PatientMedicalCondition(
    var medicalConditionMapId: String? = null,
    var dateOfOnset: DateTime? = null,
    var status: String? = null,
    var notes: String? = null,
    var isHealthPassport: Boolean = false,
    var patientId: String? = null,
    var medicalCondition: MedicalCondition? = null,
    var toString: String = "",

    var isAddedByPatient: Boolean = false,
    var diagnosis: Diagnosis? = null

)

data class MedicalCondition(
    var medicalConditionId: String? = null,
    var medicalConditionName: String? = null
)

data class HealthPassportModel(
    var url: String? = null,
    var qrCode: String? = null,
    var passcode: String? = null
)

data class Diagnosis(
    //
    var diagnosisId: String? = null,
    var diagnosisLong: String? = null,
    var diagnosisShort: String? = null,
    var code: String? = null,
    var codeType: String? = null,
    var isApproved: Boolean? = false,
    var codeVersion: String? = null,
    var localUpdatedTime: DateTime? = null
)

data class PatientCommonData(

    var bloodGrpList: ArrayList<String> = arrayListOf(),
    var country: ArrayList<DataWithId> = arrayListOf(),
    var employmentStatus: ArrayList<String> = arrayListOf(),
    var ethnicity: ArrayList<String> = arrayListOf(),
    var genderIdentity: ArrayList<String> = arrayListOf(),
    var maritalStatus: ArrayList<String> = arrayListOf(),
    var race: ArrayList<String> = arrayListOf(),
    var smokingStatus: ArrayList<String> = arrayListOf()

)

data class MDFile(
    var responseBody: ResponseBody? = null,
    var fileName: String? = null,
    var extension: String? = null
)

data class GraphData(
    var dataList: ArrayList<ILineDataSet> = arrayListOf(),
    var unitTxt: ArrayList<String> = arrayListOf(),
    var dateList: ArrayList<DateTime> = arrayListOf(),
    var lineNameList: ArrayList<String> = arrayListOf()
)

data class WellnessData(
    @StringRes var headingName: Int = 0,
    var moduleList: ArrayList<Module> = arrayListOf<Module>()
)

data class TrackerCommonData(
    var trackerList: ArrayList<Tracker> = arrayListOf(),
    var healthMetricList: ArrayList<HealthMetric> = arrayListOf()
)

data class Flowsheets(
    var flowSheetId: String? = null,
    var name: String? = null,
    var createdBy: String? = null,
    var createdOn: String? = null,
    var lastModifiedOn: DateTime? = null,
    var dataInRows: Boolean? = null,
    var showTime: Boolean? = null,
    var isAscending: Boolean? = null,
    var assignToAllPatients: Boolean? = null,
    var isDeleted: Boolean? = null,
    var shareToAllPatients: Boolean? = null,
    var fillFrequency: String? = null,
    var flowsheetEntries: ArrayList<FlowsheetEntries> = arrayListOf(),

    var practiceId: String? = null,
    var allowEdit: Boolean = false,
    var toString: String = ""
)

data class InsuranceTypeChange(
    var memberIdLabel: Int = R.string.add_insurance_id_label,
    var planName: String = ""
)

data class FlowsheetEntries(
    var flowSheetEntryId: String? = null,
    var flowSheetId: String? = null,
    var name: String? = null,
    var type: String? = null,
    var position: Int? = null,
    var unit: String? = null,
    var option: String? = null,
    var fieldType: String? = null,
    var addAdditionalNotes: Boolean = false,
    var additionalNotesLabel: String? = null,
    var value: String? = null,
    var comments: String? = null

)

data class PHRFlowsheets(
    var phrFlowsheetId: String? = null,
    var practicePatientId: String? = null,
    var flowSheetId: String? = null,
    var time: String? = null,
    var date: DateTime? = null,
    var flowsheetEntries: ArrayList<FlowsheetEntries> = arrayListOf(),

    var practiceId: String? = null,
    var toString: String? = null
)

/*data class PHRFlowsheetEntries (
        var phrFlowsheetEntryId: String? = null ,
        var phrFlowsheetId: String? = null ,
        var name: String? = null ,
        var entryType: String? = null ,
        var value: String? = null ,
        var unit: String? = null ,
        var vitalId: String? = null ,
        var comments: String? = null ,
        var customFlag: String? = null,

        var position: Int? = null,
        var fieldType: String? = null

        )*/


class MDEntry(x: Float, y: Float, data: Any?) : Entry(x, y, data) {

    var unitPosn = 0
}

class ScalePickerModel(
    var label: String? = null,
    var unit: String? = null,
    var minValue: Int = 0,
    var maxValue: Int = 200,
    var selectedValue: Int = 0

)

data class SwaasaModel(
    var keyName: String = "",
    @StringRes var string: Int = R.string.swaasa_questionnaire_label_1,
    var isSelected: Boolean = false
)

data class FlowsheetVital(
    var name: String? = null,
    var unit: String? = null,
    var value: String? = null,
    var maxValue: Int = 300,
    var isDecimal: Boolean = true
)

data class SupportDetails(
    var subject: String? = null,
    var messageContent: String? = null,
    var messageContentType: String? = null,
    var category: String? = null,
    var fileIds: ArrayList<String> = arrayListOf()
)

data class InsuranceCompanyDetails(
    var insuranceCompanyDetailsId: String? = null,
    var insuranceCompanyCode: String? = null,
    var eligibilityPayerId: String? = null,
    var insuranceCompanyName: String? = null,
    var knownName: String? = null,
    var groupType: String? = null,
    var contactPerson: String? = null,
    var contactdetailsId: String? = null,
    var postaladdressId: String? = null,
    var is270Supported: Boolean = false,
    var isClaimSubmissionSupported: Boolean = true
)

data class InsuredAndGuarantorDetails(
    var insuredGuarantorDetailsId: String? = null,
    var lastName: String? = null,
    var firstName: String? = null,
    var middleName: String? = null,
    var contactdetailsId: String? = null,
    var postaladdressId: String? = null,
    var dateOfBirth: DateTime? = null,
    var gender: String? = null, //male ,female ,unknown ,other
    var relationship: String? = null,
    var ssn: String? = null,
    var maritalStatus: String? = null,
    var employmentStatus: String? = null,
    var contactDetails: ContactDetails? = null,
    var postalAddress: PostalAddress? = null,
    var insuredEmployerDetails: InsuredEmployerDetails? = null
)

data class InsuredEmployerDetails(
    var employerDetailsId: String? = null,
    var insuredGuarantorDetailsId: String? = null,
    var name: String? = null,
    var contactLastName: String? = null,
    var contactFirstName: String? = null,
    var contactdetailsId: String? = null,
    var postaladdressId: String? = null
)

data class InsuranceCardPhotoCopies(
    var insuranceCardCopyId: String? = null,
    var firstPageFilePath: String? = null,
    var firstPageFileName: String? = null,
    var fpUpdatedTime: String? = null,
    var lastPageFilePath: String? = null,
    var lastPageFileName: String? = null,
    var lpUpdatedTime: String? = null
)

data class PatientInsuranceDetails(
    var patientInsuranceDetailsId: String? = null,
    var insuredDetailsId: String? = null,
    var practicePatientId: String? = null,
    var insuranceCompanyDetailsId: String? = null,
    var insurancePlanName: String? = null,
    var insurancePlanType: String? = null,
    var type: String? = null,
    var policyNumber: String? = null,
    var isPatientUniqueMid: Boolean = true,
    var policyStartDate: DateTime? = null,
    var policyEndDate: DateTime? = null,
    var copay: String? = null,
    var deductable: String? = null,
    var coinsurance: String? = null,
    var additionalPrDetails: String? = null,
    var policyDeductible: String? = null,
    var groupNumber: String? = null,
    var comment: String? = null,
    var priorAuthNumber: String? = null,
    var priorAuthType: String? = null,
    var priorAuthEndDate: DateTime? = null,
    var memberId: String? = null,
    var groupName: String? = null,
    var insuranceCardCopyId: String? = null,
    var isActive: Boolean = true,
    var addedDate: String? = null,
    var lastModifiedDate: String? = null,
    var addedBy: String? = null,
    var lastModifiedBy: String? = null,
    var addedThrough: String? = null,
    var lastModifiedThrough: String? = null,
    var insuredAndGuarantorDetails: InsuredAndGuarantorDetails? = null,
    var practicePayer: PracticePayer? = null,
    var practicePayerList: ArrayList<PracticePayer> = arrayListOf(),

    var isSelected: Boolean = false,
    var isPrimary: Boolean = false,
    var practiceId: String? = null,
    var isAvailable: Boolean = true,
    var toString: String = "",
    var isNewPayerAllowed: Boolean = true
)

data class PatientInsurancePriorAuthInfo(
    var patInsPriorAuthInfoId: String? = null,
    var patientInsuranceDetailsId: String? = null,
    var priorAuthNumber: String? = null,
    var priorAuthType: String = "G1", //G1 ,9F ,EW ,X4
    var priorAuthStartDate: DateTime? = null,
    var priorAuthEndDate: DateTime? = null,
    var specialityInfo: String? = null
)

data class PracticePayer( //PracticePayerList
    var practicePayerId: String? = null,
    var payerId: String? = null,
    var eligibilityPayerId: String? = null,
    var payerName: String? = null,
    var knownName: String? = null,
    var groupType: String? = null,
    var contactPerson: String? = null,
    var contactdetailsId: String? = null,
    var postaladdressId: String? = null,
    var is270Supported: Boolean = false,
    var isActive: Boolean = true,
    var isVerified: Boolean = false,
    var payerEobTypeId: String? = null,
    var payerEobTypeId2: String? = null,
    var contactDetails: ContactDetails? = null,
    var postalAddress: PostalAddress? = null
)

data class InsuranceCommonData(

    var country: ArrayList<DataWithId> = arrayListOf(),
    var relationships: ArrayList<String> = arrayListOf(),
    var insuranceTypes: ArrayList<String> = arrayListOf(),
    var isUSUser: Boolean = false,
    var stateList: ArrayList<StateList> = arrayListOf()
)

data class SupportData(
    var subject: String = "",
    var description: String = "",
    var category: String = "",
    var fileList: ArrayList<SupportDocument> = arrayListOf()
)


data class SupportDocument(
    var fileName: String = "",
    var fileType: String = "",
    var filePath: String = "",
    var fileId: String = ""
)


//"nfid" :"123456",
//"installation_id" :"234567",
//"os_info" : "8.0.1",
//"device_info": "SM-A52",
//"app_version" : "1.1",
//"device_uid" : "123-456",
//"mode" : 0

data class PushNotification(
    var nfid: String? = null,
    var installationId: String? = null,
    var osInfo: String? = null,
    var deviceInfo: String? = null,
    var appVersion: String? = null,
    var deviceUid: String? = null,
    var mode: String? = null,
)

/*data class FlowsheetCustomParamMap (
        var flowSheetEntryId: String? = null ,
        var vitalId: String? = null ,
        var customVitals: CustomVitals? = null
)
data class CustomVitals (
        var vitalId: String? = null ,
        var name: String? = null ,
        var type: String? = null ,
        var unit: String? = null ,
        var isDeleted: Boolean? = null ,
        var options: String? = null ,
        var isVital: Boolean? = null ,
        var displayView: String? = null ,
        var includeAdditionalNotes: Boolean? = null ,
        var commentsPlaceholder: String? = null
)

data class FlowsheetLabParamMap (
        var flowSheetEntryId: String? = null ,
        var medicalRecordParamId: String? = null ,
        var medicalRecordId: String? = null
)*/

data class SOAPWidgetEntry(
    var entryId: String? = null,
    var localWidgetId: Long? = null,
    var widgetId: String? = null,
    var entryName: String? = null,
    var entryType: String? = null,
    var entryPosition: Int? = null,
    var customText: String? = null,
    var entryAnswer: String? = null,
    var isMandatory: Boolean? = null,
    var options: String? = null,
    var soapWidgetSelectableEntry: SOAPWidgetSelectableEntry? = null,
    var soapWidgetTableData: ArrayList<ArrayList<SOAPTemplateTableCell>> = ArrayList()
)

data class SOAPWidgetSelectableEntry(
    var isAdditionalComments: Boolean = false,
    var isIncludeOthers: Boolean = false,
    var isAddCommentsForYes: Boolean = false,
    var isAddCommentsForNo: Boolean = false,
    var optionList: ArrayList<OptionData> = ArrayList(),
    var additionalComments: String? = null
)

data class SOAPWidgetProperty(
    var propertyId: String? = null,
    var localEntryId: Long? = null,
    var entryId: String? = null,
    var propertyName: String? = null,
    var propertyValue: String? = null
)

data class SOAPWidgetEntryPOJO(
    var soapWidgetEntry: SOAPWidgetEntry? = null,
    var soapWidgetProperty: List<SOAPWidgetProperty> = ArrayList()
)

data class SOAPWidgetPOJO(
    var soapWidget: SOAPWidget? = null,
    var soapWidgetEntryPOJOList: List<SOAPWidgetEntryPOJO> = ArrayList()
)

data class SOAPWidget(
    var widgetId: String? = null,
    var widgetName: String? = null,
    var widgetModule: String? = null,
    var tags: String? = null,
    var layout: String? = null,
    var templateId: String? = null
)

@Parcelize
class OptionData(
    var optionString: String? = null,
    val isChecked: Boolean = false,
    var childrenOptionList: List<OptionData> = ArrayList(),
    var viewLevel: Int = 0,
    var isOther: Boolean = false,
    var otherValue: String? = null
) : Parcelable {
    var checked by mutableStateOf(isChecked)
}

data class LabParamHistoryPOJO(
    var paramName: String? = null,
    var isStringValue: Boolean? = null,
    var paramHistoryEntryList: ArrayList<ParamHistoryEntryPOJO> = ArrayList(),
    var paramUnit: String? = null
)

data class ParamHistoryEntryPOJO(
    var date: DateTime? = null,
    var note: String? = null,
    var value: String? = null
)

data class FloatingActionMenuItem(
    val label: String,
    val icon: Int,
    val onClick: () -> Unit,
    val backgroundColor: Color = Color.Blue,
    val contentColor: Color = Color.White
)

data class MessageGroup(
    var groupName: String?=null,
    var groupDesc: String?=null,
    var type: String?=null,
    var groupId: String?=null,
    var isSelected: Boolean=false
)

data class NotificationRegister (
      var nfid: String? = null,
      var installationId: String? = null,
      var osInfo: String? = null,
      var deviceInfo: String? = null,
      var deviceUniqueId: String? = null,
      var appVersion: String? = null,
)

data class MriResultPOJO(
    var date: org.joda.time.DateTime? = null,
    var lic: String? = null,
    var licUnit: String? = null,
    var cardiacT2Star: Float? = null,
    var cardiacT2StarUnit: String? = null
)

data class TransfusionLogData(
    var nextTransfusionDateMillis: Long? = null,
    var hbValue: String? = null,
    var hbUnit: String? = null,
    var entryDate: DateTime? = null
)

