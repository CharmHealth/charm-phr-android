package com.charm.phr.mphr.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.model.FrequencyAlarmMapCustomPOJO
import com.charm.phr.mphr.model.GraphData
import com.charm.phr.mphr.model.Module
import com.charm.phr.mphr.model.PHRBills
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.model.ReminderCustomPOJO
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.SectionConfig
import com.charm.phr.mphr.model.SectionConfigList
import com.charm.phr.mphr.model.database.entity.ReminderFrequencyMap
import com.charm.phr.mphr.model.database.entity.ReminderHistory
import com.charm.phr.mphr.ui.components.MDmPHRConstraintLayout
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import org.joda.time.DateTime
import org.joda.time.Minutes
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.Locale

class ModelUtil {

    companion object {

        const val TAG = "ModelUtil"

        const val mPHRDBName = "mPHRDB" //Old
        const val charmMPHRDb = "charmMPHRDb" //deprecated
        const val charmMPHRDBName = "charmMPHRDBName" //new

        private val twoDecimalFormat = "%.2f"

        const val IAM_AUTHTOKEN = "IAM_AUTHTOKEN"

        /**
         * Exit time for asking passcode
         */
        const val exitTimeInSeconds: String = "exitTimeInSeconds"


        const val addErrorAnimationDelayDuration: Long = 200

        const val prefIsFirstEntry = "isFirstEntry"

        const val prefVitalsUnitSystem = "vitalsUnitSystem"
        const val prefIsLoggedOut = "isLoggedOut"

        //region auth manager
        const val apiTokenFailed = "apiTokenFailed"
        const val isOldAppIntent = "isOldApp"

        //endregion

        //region webcall error msg
        const val io_exception = "Network Error"
        const val socketException = "Timeout Error"
        const val conversionException = "conversion Error"
        const val unknownError = "Error occurred"
        const val requestFailed = "Request failed"

        const val errorInternetConnectionCode = -10
        const val errorPaymentSuccessButReceiptFailedCode = -9

        const val errorProcedureDataExistCode = 5009

        const val errorValueNull = "null"

        const val delayedTimingForRippleChange: Long = 100


        const val mdClickToSelectEdittextCursorDr = "mCursorDrawableRes"

        const val othersValue = "Others"

        const val isHealthPassportIntent = "isHealthPassportIntent"

        const val memberList ="memberList"

        fun dpToPxConversion(dpVal: Int, context: Context) =
            (dpVal * context.resources?.displayMetrics?.density!! + 0.5f).toInt()

        fun spToPxConversion(
            context: Context,
            spValue: Float
        ): Int {
            val fontScale = context.resources.displayMetrics.scaledDensity
            return (spValue * fontScale + 0.5f).toInt()
        }

        //endregion

        //dashboard


        const val appointmentModelId: String = "appointment"
        const val billingModelId: String = "billing"
        const val receiptModelId: String = "receipt" //Custom added, not used in web
        const val cardOnFileModelId: String = "cardonfile" //Custom added, not used in web
        const val messageModuleId: String = "message"
        const val labModelId: String = "lab"
        const val medicationModelId: String = "rx"
        const val supplementModelId: String = "sx"
        const val prescriptionModelId: String = "presription"
        const val allergiesModelId: String = "allergy"
        const val visitSummaryModelId: String = "visit"
        const val procedureModelId: String = "procedure"
        const val vaccineModelId: String = "immunization"
        const val questionnaireModelId: String = "questionnaire"
        const val problemListModelId: String = "condition"
        const val documentModelId: String = "document"
        const val physicianModelId: String = "physician"
        const val permissionsModelId: String = "permission"
        const val wellnessModelId: String = "tracker"
        const val healthPassportModelId: String = "healthPassport"

        const val insuranceModelId: String = "insurance"

        const val flowsheetModelId: String = "symptomTracker"

        const val announcementModelId: String = "announcement"
        const val usefulInfoModelId: String = "usefulInfo"
        const val aboutModelId: String = "about"
        const val supportModelId: String = "support"
        const val reminderModelId: String = "reminder"

        //passcode activity
        const val isRootTask = "isRootTask"
        const val passCodeType = "passCodeType"
        const val blockBackFunction = "blockBackFunction"
        const val userType = "userType"
        const val signedUp = "signedUp"
        const val newUser = "newUser"
        const val isFirst = "isFirst"
        const val freeToSignedUp = "freeToSignedUp"
        const val confirmUserSuccessfullyLoggedIn =
            "confirmUserSuccessfullyLoggedIn" //After verifying passcode confirm user successfully logged in.
        const val freeToSignedUpWithoutLocalData = "freeToSignedUpWithoutLocalData"
        const val requestCodeForForgotPassCode = 11
        const val from = "from"
        const val lockType = "LockType"
        const val applockType = "AppLockType"
        const val passcodeFromDashboardIntent = "passcodeFromDashboard"
        const val fromLockTypeSelection = "fromLockTypeSelection"
        const val isFirstTimeForAppLock = "isFirstTimeForAppLock"
        const val animationDuration = 400L
        const val PASSCODE = "PassCodePref"
        const val DBENCRYPT = "Dbencrypt" //Old
        const val DbEncryptionKey = "DbEncryptionKey" //new
        const val IS_CORRUPTED_DB_DELETED = "isCorruptedDBDeleted"
        /**
         * Number of times user has attempted to reset the pin and entered wrong web password,
         * this is user for forgot passcode flow. Refer Security Util
         */
//        const val numberOfWrongAttemptsToReset = "wrongPasswordAttemptToReset"

        /**
         * Time in milles for user to wait for the next time to reset password.
         * Note:Clear these two params will be clear, just when user logs-in from login activity
         */
//        const val pauseTimeInMillisForResetPin = "pauseTimeInMillisForResetPin"

        /** Tracks consecutive wrong passcode attempts in [checkPasscode] mode. */
        const val WRONG_PASSCODE_ATTEMPT_COUNT = "wrongPasscodeAttemptCount"

        /** Maximum wrong attempts allowed before the user is automatically signed out. */
        const val MAX_WRONG_PASSCODE_ATTEMPTS = 5

        //Patient

        const val patientList = "patientList"
        const val selectedPatientId = "selectedPatientId"

        const val patientSectionPrefix = "patientSection_"

        //Login
        const val patientRequest = "patientRequest"
        const val menuListRequest = "menuListRequest"
        const val patientEmptyRequest = "patientEmptyRequest"

        //MdBase
        const val bufferTimeForBackPress = 10L // in seconds

        //TourPage
        const val tourPageFromSettings = "tourPageFromSettings"
        const val isAppCrashed = "isAppCrashed"

        //HTTP request Time Out
        const val connectionTimeOut = 60L
        const val readTimeOut = 30L
        const val writeTimeOut = 30L

        //region time Stings

        const val morningStartTimeString = "12:00 AM"
        const val noonStartTimeString = "12:00 PM"
        const val eveningStartTimeString = "04:00 PM"
        const val nightStartTimeString = "09:00 PM"

        const val morning = 1
        const val noon = 2
        const val evening = 3
        const val night = 4


        //endregion

        const val getMembersListCountPerPage = 100

        //DateTime
        const val charmDisplayDateFormat = "MMM dd, yyyy"
        const val charmApiDateFormat = "yyyy-MM-dd"
        const val charmDisplayTimeFormat = "hh:mm a"
        const val charmDisplay24HrTimeFormat = "HH:mm"


        const val charmFlowsheetDateTimeFieldFormat = "MMM dd, yyyy HH:mm"

        const val charmDisplayDateMonthDateFormat = "MMM dd"

        const val charmDateFormatForGettingSlots = "MM-dd-yyyy"

        const val displayTimeForAppointmentTimeRangeFormat = "h:m a"

        const val rbacPreferencesString: String = "rbacPreferencesString"

        fun charmDateFormatForVitals() =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.S").withLocale(Locale.ENGLISH)

        fun sqlDateTimeFormatForNormalTiming() =
            DateTimeFormat.forPattern("yyyy-MM-dd hh:mm a").withLocale(
                Locale.ENGLISH
            )

        fun charmDateFormatForMessageList(): DateTimeFormatter =
            DateTimeFormat.forPattern("$charmDisplayDateFormat $charmDisplayTimeFormat").withLocale(
                Locale.ENGLISH
            )

        fun charmDateFormatForSlot(): DateTimeFormatter =
            DateTimeFormat.forPattern("$charmApiDateFormat $charmDisplayTimeFormat").withLocale(
                Locale.ENGLISH
            )

        fun charmApiDateFormat(): DateTimeFormatter =
            DateTimeFormat.forPattern(charmApiDateFormat).withLocale(
                Locale.ENGLISH
            )

        fun charmDisplayTimeFormat(): DateTimeFormatter =
            DateTimeFormat.forPattern(charmDisplayTimeFormat).withLocale(
                Locale.ENGLISH
            )

        fun charmDisplayDateFormat(): DateTimeFormatter =
            DateTimeFormat.forPattern(charmDisplayDateFormat).withLocale(
                Locale.ENGLISH
            )

        fun charmFlowsheetDateTimeFieldFormat(): DateTimeFormatter =
            DateTimeFormat.forPattern(charmFlowsheetDateTimeFieldFormat).withLocale(
                Locale.ENGLISH
            )

        fun displayTimeForAppointmentTimeRangeFormat(): DateTimeFormatter =
            DateTimeFormat.forPattern(displayTimeForAppointmentTimeRangeFormat).withLocale(
                Locale.ENGLISH
            )

        fun charmDisplay24HrTimeFormat(): DateTimeFormatter =
            DateTimeFormat.forPattern(charmDisplay24HrTimeFormat).withLocale(
                Locale.ENGLISH
            )

        fun genericDateDisplayFormattor(): DateTimeFormatter =
            DateTimeFormat.forPattern("MMM dd, yyyy hh:mm a").withLocale(Locale.ENGLISH)

        fun sqlTimeStampFormattor(): DateTimeFormatter =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withLocale(
                Locale.ENGLISH
            )

        fun charmWellnessAddDateYearFormatter(): DateTimeFormatter =
            DateTimeFormat.forPattern("E, MMM dd, yyyy").withLocale(
                Locale.ENGLISH
            )

        fun charmCompleteDateYearFormatter(): DateTimeFormatter =
            DateTimeFormat.forPattern("E, MMM dd, $charmDisplayTimeFormat").withLocale(
                Locale.ENGLISH
            )

        fun charmMessageDetailContentDateFormatter(): DateTimeFormatter =
            DateTimeFormat.forPattern("MMM dd, yyyy HH:mm:ss").withLocale(
                Locale.ENGLISH
            )


        fun charmMessageDetailContentDateInCardFormatter(): DateTimeFormatter =
            DateTimeFormat.forPattern("MMM dd, yyyy , hh:mm a").withLocale(
                Locale.ENGLISH
            )

        fun charmWellnessDateFormatter(): DateTimeFormatter =
            DateTimeFormat.forPattern("MMM dd, yyyy , hh:mm a").withLocale(
                Locale.ENGLISH
            )

        fun charmFlowsheetDataDateFormatter(): DateTimeFormatter =
            DateTimeFormat.forPattern("MMM dd, yyyy hh:mm a").withLocale(
                Locale.ENGLISH
            )

        fun sqlTimeStampForLabResultDateFormattor(): DateTimeFormatter =
            DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").withLocale(
                Locale.ENGLISH
            )

        fun sqlTimeStampForAnnouncementDateFormattor(): DateTimeFormatter =
            DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").withLocale(
                Locale.ENGLISH
            )

        const val dayOfWeekShortFormDateFormat = "E"

        const val dateOfMonthDateFormat = "dd"
        const val dayOfWeekDateFormat = "EEEE"
        const val monthOfYearDateFormat = "MMMM"
        const val monthAndYearDateFormat = "MMMM YYYY"
        const val monthAbbreviationAndYearDateFormat = "MMM YYYY"
        const val monthShortFormat = "MMM"
        const val monthAndDateFormat = "MMM dd"

        const val yearFormat = "YYYY"
        const val monthAndDateYearFormat = "$monthAndDateFormat $yearFormat"
//        fun commonDateFormatWithSlash(): DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
//        fun yearOnlyDateFormat() = DateTimeFormat.forPattern(yearFormat)

        const val appointmentSpecificDateYearFormat =
            "$dayOfWeekShortFormDateFormat, $monthAndDateFormat $yearFormat"
        const val appointmentSpecificDateFormat =
            "$dayOfWeekShortFormDateFormat, $monthAndDateFormat "

//        fun monthDayTime12HrWithoutYear(): DateTimeFormatter = DateTimeFormat.forPattern("MMM dd, hh:mm a").withLocale(Locale.ENGLISH)
//
//        fun isSameDate(dateTime1: DateTime, dateTime2: DateTime) =
//            dateTime1.toString(commonDateFormatWithSlash()) == dateTime2.toString(commonDateFormatWithSlash())
//
//        fun isSameYear(dateTime1: DateTime, dateTime2: DateTime) =
//            dateTime1.toString(yearOnlyDateFormat()) == dateTime2.toString(yearOnlyDateFormat())


        fun appointmentSpecificDateFormat(): DateTimeFormatter =
            DateTimeFormat.forPattern(appointmentSpecificDateFormat).withLocale(
                Locale.ENGLISH
            )

        fun appointmentSpecificDateYearFormat(): DateTimeFormatter =
            DateTimeFormat.forPattern(appointmentSpecificDateYearFormat).withLocale(
                Locale.ENGLISH
            )

        //use this formator for adding timeStamp in file name
        fun timeStapStringForFile(): DateTimeFormatter =
            DateTimeFormat.forPattern("yyyyMMdd_HHmmssSSS") //SSS is milliseconds


        //File

        const val folderTemp = "Temp"
        const val folderSwaasa = "Swaasa"

        //Appointment
        const val appointmentDatePreferenceAnyDate = "ANY_DATE"
        const val appointmentDatePreferenceSpecificDate = "SPECIFIC_DATE"

        const val appointmentUpcoming = 1
        const val appointmentRequested = 2
        const val appointmentDenied = 3
        const val appointmentWaitingList = 4


        const val appointmentCode = "appointmentCode"

        const val appointmentTimePreferenceAnyTime = "ANY_TIME"
        const val appointmentTimePreferenceTimeRange = "TIME_RANGE"

        const val appointmentIdIntent = "appointmentId"
        const val appointmentIsFromDashboardIntent = "appointmentIsFromDashboard"
        const val consentFormIdsIntent = "consentFormIds"

        const val dashboardMaxInvoiceCount = 2
        const val dashboardMaxMessageCount = 2
        const val dashboardMaxMedicationCount = 4
        const val dashboardMaxQuestionnaireCount = 2

        const val memberNameIntent = "memberName"
        const val appointmentTimeIntent = "appointmentTime"


        const val appointmentTypeInPerson = "In Person"
        const val appointmentTypePhoneCall = "Phone Call"
        const val appointmentTypeVideoCall = "Video Consult"
        const val appointmentTeleModeKiosk = "KIOSK"
        const val appointmentEasyTeleCare = "EasyTeleCare"

        const val appointmentPaymentChargeTypeVisitBased = "VISIT_BASED"

        const val appointmentCardProcessingTypeChargeOnly = "CHARGE_ONLY"
        const val appointmentCardProcessingTypeStoreCharge = "STORE_CHARGE"
        const val appointmentCardProcessingTypeStoreOnly = "STORE_ONLY"

        const val appointmentTransactionTypeSale = "SALE"
        const val appointmentTransactionTypeAuthorization = "AUTHORIZATION"


        const val appointmentGatewayTypeBluefin = "BLUEFIN"
        const val appointmentGatewayTypeRazorpay = "RAZORPAY"

        const val minVideoCallEnableTime = 30

        //Prescription

        val medicationUiTypeOne: String = "Type1"
        const val medicationViewTypeActive = "Active"
        const val medicationViewTypeInactive = "Inactive"
        const val medicationViewTypeAll = "All"

        const val medicationAddedAsMed = "Medication"
        const val medicationAddedAsPrescription = "Prescription"

        //Billing

        const val invoiceStatementStr = "Statement"

        const val billingType = "billingType"
        const val billingTypeInvoice: Long = 1
        const val billingTypeReceipt: Long = 2
        const val billingTypeCreditNote = "Credit Note"

        const val phrBillIdIntent = "phrBillId"
        const val phrReceiptIdIntent = "phrReceiptId"


        const val billingIsStatementIntent = "billingIsStatement"
        const val billingInvoiceIntent = "billingInvoiceIntent"

        const val billingIsPaymentRequested = "billingIsPaymentRequested"
        const val billingDate = "billingDate"
        const val billingFacilityName = "billingFacilityName"
        const val billingInvoiceNum = "billingInvoiceNum"
        const val billingPaymentReqAmt = "billingPaymentReqAmt"
        const val billingPaymentReqCurrency = "billingPaymentReqCurrency"

        const val billingFacilityIdIntent = "billingFacilityId"
        const val billingPracticeIdIntent = "billingPracticeId"
        const val billingPaymentGatewayIntent = "billingPaymentGateway"
        const val billingMemberIdIntent = "billingMemberId"


        fun isAmountValid(amount: String?): Boolean {

            if (!amount.isNullOrBlank()) {

                val amt = amount.toFloatOrNull()
                if (amt != null) {
                    return amt > 0f
                }
            }

            return false
        }

        fun getDueAndPaymentRequested(phrBill: PHRBills): Pair<String?, String?> {
            var dueAmount = phrBill.dueAmount
            var paymentRequested = phrBill.amount

            val dueAmountDouble = phrBill.dueAmount?.toFloatOrNull() ?: 0.0f
            val paymentRequestedFloat = phrBill.amount?.toFloatOrNull() ?: 0.0f
            if (paymentRequestedFloat > dueAmountDouble) {
                paymentRequested = dueAmount
            }

            if (phrBill.billType == billingTypeCreditNote) {
                dueAmount = "0.00"
            }

            return Pair(dueAmount, paymentRequested)
        }

        //Messages

        const val messageStatusSeen = 1
        const val messageStatusUnSeen = 0

        const val messageTypeSent = "sent"
        const val messageTypeInbox = "inbox"

        const val messageUnreadCountIntent = "messageUnreadCount"

        const val messageIdIntent = "messageId"
        const val messageDateIntent = "messageDate"
        const val practiceIdIntent = "practiceId"
        const val patientIdIntent = "patientIdIntent"
        const val messageDataIntent = "messageData"
        const val messagePositionIntent = "messagePosition"
        const val messageReceipientIdIntent = "messageReceipientId"

        const val messageComposeTypeIntent = "messageComposeType"
        const val messageComposeTypeReplyIntent = "messageComposeTypeReply"
        const val messageComposeTypeReplyAllIntent = "messageComposeTypeReplyAll"
        const val messageComposeTypeForwardIntent = "messageComposeTypeForward"
        const val messageNoOfCharAllowedIntent = "messageNoOfCharAllowed"
        const val messageTypeGroup = "Group"


        const val messageComposeMsgRestrictionListIntent = "messageComposeMsgRestrictionList"
        const val messageComposeFromDashboardIntent = "messageComposeFromDashboard"


        const val patientImageUpdated = "PATIENT_IMAGE_UPDATED"



        //Support
        const val supportReport = 1
        const val supportSuggest = 2
        const val supportAskQuestion = 3
        const val supportAppreciate = 4
        const val supportType = "supportType"

        const val supportCategoryReport = "Report a Problem"
        const val supportCategorySuggest = "Suggest a Feature"
        const val supportCategoryAskQuestion = "Ask a Question"
        const val supportCategoryAppreciate = "Appreciate"
        const val supportCategoryAppreciation = "Appreciation"
        const val appSdkVersion = "\n Android SDK version = "
        const val appVersion = "\nApp version = "
        const val supportSubjectPrefix = " Android :: "

        //File Types
        const val imageFileType = "image/"
        const val imageFileTypePNG = "png"
        const val imageFileTypeJPEG = "jpeg"
        const val imageFileTypeJPG = "jpg"
        const val applicationFileType = "application/"
        const val fileTypePDF = "pdf"
        const val contentType = "content-type"

        //REGEX To be used as delimiters to get only File Name from string with FileName.extension
        val fileNameSplitRegex = ("\\.(?=[^\\.]+$)").toRegex()

        fun getDisplayDateForMessage(messageDate: DateTime?): String {
            val currentDate = DateTime.now()

            val yesterdayDate = currentDate.minusDays(1)

            if (messageDate != null) {

                return if (currentDate.dayOfMonth == messageDate.dayOfMonth && currentDate.monthOfYear == messageDate.monthOfYear && currentDate.year == messageDate.year) {
                    messageDate.toString(charmDisplayTimeFormat)
                } else if (yesterdayDate.dayOfMonth == messageDate.dayOfMonth && yesterdayDate.monthOfYear == messageDate.monthOfYear && yesterdayDate.year == messageDate.year) {
                    messageDate.toString(monthAndDateFormat)
                } else if (currentDate.year == messageDate.year) {
                    messageDate.toString(monthAndDateFormat)
                } else {
                    messageDate.toString(charmDisplayDateFormat())
                }
            } else {
                return currentDate.toString(charmDisplayTimeFormat)
            }
        }

        fun getDisplayDateForAppointment(messageDate: DateTime?): String {
            val currentDate = DateTime.now()

            val yesterdayDate = currentDate.minusDays(1)

            if (messageDate != null) {
                return if (currentDate.dayOfMonth == messageDate.dayOfMonth && currentDate.monthOfYear == messageDate.monthOfYear && currentDate.year == messageDate.year) {
                    "Today"
                } else if (currentDate.year == messageDate.year) {
                    messageDate.toString(monthAndDateFormat)
                } else {
                    messageDate.toString(monthAndDateYearFormat)
                }
            } else {
                return currentDate.toString(charmDisplayDateFormat())
            }
        }

        fun getDisplayDayForAppointment(messageDate: DateTime?): String {
            val currentDate = DateTime.now()


            if (messageDate != null) {
                return messageDate.toString(dayOfWeekDateFormat)
            } else {
                return currentDate.toString(dayOfWeekDateFormat)
            }
        }


        fun convertPlainTxtToHtml(plainTxt: String): String {
            val builder = StringBuilder()
            var previousWasASpace = false
            for (char in plainTxt.toCharArray()) {
                if (char == ' ') {
                    if (previousWasASpace) {
                        builder.append("&nbsp;")
                        previousWasASpace = false
                        continue
                    }
                    previousWasASpace = true
                } else {
                    previousWasASpace = false
                }
                when (char) {
                    '<' -> builder.append("&lt;")
                    '>' -> builder.append("&gt;")
                    '&' -> builder.append("&amp;")
                    '"' -> builder.append("&quot;")
                    '\n' -> builder.append("<br>")
                    // We need Tab support here, because we print StackTraces as HTML
                    '\t' -> builder.append("&nbsp; &nbsp; &nbsp;")
                    else -> if (char.code < 128) {
                        builder.append(char)
                    } else {
                        builder.append("&#").append(char.code).append(";")
                    }
                }
            }
            return builder.toString()
        }


        //Lab
        const val labResultsIntepretationAbnormal = "Abnormal"
        const val labResultsIntepretationNormal = "Normal"
        const val labResultsIntepretationLow = "Low"
        const val labResultsIntepretationHigh = "High"


        const val labResultGroupId: String = "labResultGroupId"
        const val labResultLabNameIntent: String = "labResultLabName"
        const val interpretation: String = "interpretation"

        const val labIsOrder: String = "labIsOrder"

        const val labOrderId: String = "labOrderId"

        const val labOrderNameIntent: String = "labOrderName"
        const val labParamNameIntent: String = "labParamName"
        const val labParamReferenceIntent: String = "labParamReferenceRange"
        const val labParamUnitIntent: String = "labParamUnit"

        const val listPerPageCount = 100
        const val documentlistPerPageCount = 20
        const val questionnaireListPerPageCount = 20

        const val documentTypeAll = "All"

        //Allergy

        const val allergySeveritySevere = "Severe"
        const val allergySeverityModerate = "Moderate"
        const val allergySeverityMild = "Mild"


        const val allergyStatusActive = "Active"
        const val allergyStatusInActive = "Inactive"
        const val allergyStatusResolved = "Resolved"


        const val allergyToEditIntent = "allergyToEdit"
        const val allergyEditDataIntent = "allergyEditData"
        const val allergyStatusIntent = "allergyStatusIntent"

        const val modulelNameAllergy = "allergy"


        //Procedure

        const val procedureToEditIntent = "procedureToEdit"
        const val procedureEditDataIntent = "procedureEditData"

        //module
        const val moduleNameProcedure = "procedure"
        const val moduleNameVaccine = "immune"
        const val moduleNameMedication = "medication"
        const val moduleNameSupplement = "supplement"
        const val moduleNameMessage = "message"
        const val moduleNameMedicalCondition = "cond"
        const val moduleNameInsurance = "insurance"

        // ProblemList
        const val problemListToEditIntent = "problemListToEdit"
        const val problemListEditDataIntent = "problemListEditData"

        const val problemListStatusActive = "Active"
        const val problemListStatusInActive = "Inactive"
        const val problemListStatusResolved = "Resolved"

        //Payment

        const val paymentServiceAppointment = "PHR_APPLICATION_APPOINTMENTS"
        const val paymentServiceBilling = "PHR_APPLICATION_BILLING"

        //Vaccine

        const val vaccineToEditIntent = "vaccineToEdit"
        const val vaccineEditDataIntent = "vaccineEditData"

        //medication

        const val medicationToEditIntent = "medicationToEditIntent"
        const val medicationEditDataIntent = "medicationEditDataIntent"

        //supplement


        const val supplementToEditIntent = "supplementToEditIntent"
        const val supplementEditDataIntent = "supplementEditDataIntent"

        //questionnaire


        const val questionnaireNotesTypeLabel = "Label"
        const val questionnaireNotesTypeAllergy = "Allergies"
        const val questionnaireNotesTypeMedication = "Medications"
        const val questionnaireNotesTypeSupplement = "Supplements"
        const val questionnaireNotesTypeRatingScale = "Rating Scale"
        const val questionnaireNotesTypeOptionWithOthers = "Question with Options"
        const val questionnaireNotesTypeOption = "Question with Fixed Options"
        const val questionnaireNotesTypeYesOrNoOption = "Yes/No Question"
        const val questionnaireNotesTypeSimpleQuestion = "Simple Question"
        const val questionnaireNotesTypeQuestion = "Question"
        const val questionnaireNotesTypeDate = "Date"
        const val questionnaireNotesTypeSignature = "Signature"
        const val questionnaireNotesTypePersonalDetails = "Personal Details"
        const val questionnaireNotesTypePrimaryContactDetail = "Primary Contact Details"
        const val questionnaireNotesTypeInsurance = "Primary Insurance Details"
        const val questionnaireNotesTypeSection = "Section"
        const val questionnaireNotesTypeRating = "Rating"

        const val historyOfIllnessSOAPType = "History of Present Illness"
        const val pastMedicalHistorySOAPType = "Past Medical History"
        const val familyHistorySOAPType = "Family History"
        const val socialHistorySOAPType = "Social History"
        const val reviewOfSystemsSOAPType = "Review of Systems"
        const val chiefComplaintsSOAPType = "Chief Complaints"
        const val symptomsSOAPType = "Symptoms"

        val SOAPTypes = arrayListOf(
            historyOfIllnessSOAPType,
            pastMedicalHistorySOAPType,
            familyHistorySOAPType,
            socialHistorySOAPType,
            reviewOfSystemsSOAPType,
            chiefComplaintsSOAPType,
            symptomsSOAPType
        )

        const val soapWidgetHeaderType = "Header"
        const val soapWidgetSimpleQuestionType = "Simple Question"
        const val soapWidgetYesOrNoQuestionType = "Yes/No Question"
        const val soapWidgetRatingScaleType = "Rating Scale"
        const val soapWidgetTextBoxType = "Text Box"
        const val soapWidgetCheckListType = "Check List"
        const val soapWidgetCheckBoxType = "Checkbox"
        const val soapWidgetRadioType = "Radio"
        const val soapWidgetTableType = "Table"
        const val swOptionsSeparatorSymbol = "^^"
        const val swHierarchySymbol = ">>"
        const val appendStringYesComments = "COMMENT_FOR_YES_"
        const val appendStringNoComments = "COMMENT_FOR_NO_"
        const val appendStringCommentsValue = "COMMENT_VALUE_"


        fun swfontSize() = "font-size"
        fun swtextAlign() = "text-align"
        fun swfontWeight() = "font-weight"
        fun swfontStyle() = "font-style"
        fun swtextDecoration() = "text-decoration"


        val questionnaireSupportedWidgets = listOf(
            questionnaireNotesTypeLabel,
            questionnaireNotesTypeAllergy,
            questionnaireNotesTypeMedication,
            questionnaireNotesTypeSupplement,
            questionnaireNotesTypeRatingScale,
            questionnaireNotesTypeOptionWithOthers,
            questionnaireNotesTypeOption,
            questionnaireNotesTypeYesOrNoOption,
            questionnaireNotesTypeSimpleQuestion,
            questionnaireNotesTypeQuestion,
            questionnaireNotesTypeDate,
            questionnaireNotesTypeSignature,
            questionnaireNotesTypePersonalDetails,
            questionnaireNotesTypePrimaryContactDetail,
            questionnaireNotesTypeInsurance,
            questionnaireNotesTypeSection,
            historyOfIllnessSOAPType,
            pastMedicalHistorySOAPType,
            familyHistorySOAPType,
            socialHistorySOAPType,
            reviewOfSystemsSOAPType,
            chiefComplaintsSOAPType,
            symptomsSOAPType,
            questionnaireNotesTypeRating

        )

        const val questionnaireNotesTypeAllergyDrug = "Drug"
        const val questionnaireNotesTypeAllergyDrugFN = "Drug Allergy"

        const val questionnaireNotesTypeAllergyFood = "Food"
        const val questionnaireNotesTypeAllergyFoodFN = "Food Allergy"
        const val questionnaireNotesTypeAllergyEnvironment = "Environmental"
        const val questionnaireNotesTypeAllergyEnvironmentFN = "Environmental Allergy"


        const val questionnairePersonalDetailDeclineToSpecify = "Declined to specify"

        const val questionnaireNotesTypeAllergySeveritySevere = "Severe"

        const val questionnaireLabelAlignmentLeft = "left"
        const val questionnaireLabelAlignmentRight = "right"
        const val questionnaireLabelAlignmentCenter = "center"
        const val questionnaireLabelAlignmentJustify = "justify"

        const val questionnaireLabelAlignmentBold = "Bold"
        const val questionnaireLabelAlignmentItalics = "Italic"
        const val questionnaireLabelAlignmentUnderline = "Underline"


        const val questionnairePhysicianTemplatesIntent = "questionnairePhysicianTemplatesIntent"
        const val questionnaireIsPastIntent = "questionnaireIsPast"


        fun createNumberList(startNumber: Int = 1, endNumber: Int): ArrayList<String> {

            val respList = arrayListOf<String>()

            for (temp in startNumber..endNumber) {

                var prefix = ""

                if (temp < 10) {
                    prefix = "0"
                }

                respList.add("$prefix$temp")
            }

            return respList
        }

        //Document

        const val documentFileIdIntent = "documentFileId"
        const val documentShareString = "DocumentsList"

        const val documentCategoryIdIntent = "documentCategoryId"
        const val documentCategoryNameIntent = "documentCategoryName"
        const val documentSharedByMeIntent = "documentSharedByMe"
        const val documentSharedToMeIntent = "documentSharedToMe"
        const val documentCategoryListIntent = "documentCategoryList"

        const val documentFilterSharedbyme = "Sharedbyme"
        const val documentFilterSharedtome = "Sharedtome"

        const val documentExpiryOneday = "One day"
        const val documentExpiryFivedays = "Five days"
        const val documentExpiryTendays = "Ten days"

        const val documentShareAddContact = "Add Contact"
        const val documentShareNoMember = "No Member"
        const val documentShareAddContactFullName = "Full Name"
        const val documentShareAddContactEmail = "Email"


        //VisitSummary

        const val visitSummaryDocumentIDIntent = "visitSummaryDocumentID"

        //vaccine

        const val vaccineTypeAdolescent = "Vaccines_Adolescent"
        const val vaccineTypeChildhood = "Vaccines_Childhood"

        //Announcement

        const val announcementIdIntent = "announcementIdIntent"
        const val announcementSubjectIntent = "announcementSubjectIntent"
        const val announcementDateIntent = "announcementDateIntent"

        //Announcement

        const val usefulInfoIdIntent = "usefulInfoIdIntent"
        const val usefulInfoCategoryIntent = "usefulInfoCategoryIntent"
        const val usefulInfoDateIntent = "usefulInfoDateIntent"

        //Questionnaire

        const val questionnaireCurrentValue = "Current"
        const val questionnairePastValue = "Past"

        const val questionnairePatQuesMapIdIntent = "questionnairePatQuesMapId"
        const val questionnaireNameIntent = "questionnaireName"

        const val questionnaire3MonthFilter = 1
        const val questionnaire6MonthFilter = 2
        const val questionnaire1YearFilter = 3
        const val questionnaireCustomFilter = 4


        const val questionnaireNameKeyAutism = "autism"
        const val questionnaireNameKeyPastMed = "past_med"
        const val questionnaireNameKeyMedInfo = "med_info"
        const val questionnaireNameKeyCovid = "covid"
        const val questionnaireNameKeyPermission = "permission"

        const val questionnaireNameValueAutism = "Autism"
        const val questionnaireNameValuePastMed = "Past Medical History"
        const val questionnaireNameValueMedInfo = "Medical Information"
        const val questionnaireNameValueCovid = "COVID-19 Screening"
        const val questionnaireNameValuePermission = "Permissions"


        const val questionnaireNoInsuranceResp = "No insurance available"


        //Contacts

        const val contactTypeDoctor = "Doctor"

        //Vitals

        const val vitalBPSystolic = "Systolic"
        const val vitalBPDiastolic = "Diastolic"
        const val vitalBP = "Blood Pressure"
        const val vitalGeneral = "General"
        const val vitalWeight = "Weight"
        const val vitalHeight = "Height"

        //Physician
        const val physicianToEditIntent = "physicianToEdit"
        const val physicianEditDataIntent = "physicianEditData"


        //Insurance

        const val insuranceToEditIntent = "insuranceToEdit"
        const val insuranceEditDataIntent = "insuranceEditDataIntent"

        const val insuranceTypePrimary = "PRIMARY"
        const val insuranceTypeSecondary = "SECONDARY"


        //Swaasa

        const val swaasaIdIntent = "swaasaId"
        const val swaasaShowOtpIntent = "swaasaShowOtp"
        const val swaasaIsFlowsheetIntent = "swaasaIsFlowsheet"

        //region video call

        val videoCallHost = "HOST"
        val videoCallAttendee = "ATTENDEE"


        const val videoJoinMeetingIntent = "videoJoinMeeting"
        const val videoMeetingIdIntent = "videoMeetingId"
        const val videoMeetingNameIntent = "videoMeetingName"
        const val webviewHeader = "WebviewHeader"
        const val videoCallPassword = "VideoCallPassword"



        const val otAPIKeyIntent = "otAPIKeyIntent"
        const val otSessionIdIntent = "otSessionIdIntent"
        const val otTokenIntent = "otTokenIntent"

        //endregion


        //misc

        const val menuList = "menuList"


        //Patient


        const val emailRegex = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})\$"
        const val patientUsState = "us"
        const val patientInState = "in"
        const val patientAuState = "au"
        val patientStateList = listOf(patientUsState, patientInState, patientAuState)

        const val patientEditIntent = "patientEdit"
        const val patientIsHealthCardIntent = "patientIsHealthCard"

        const val patientNotAvailableForPracticeCode = 50085
        const val errorPracticeAccountLoginCode = 5197
        const val errorSecondaryAccount = 50088

        const val healthCardFolder = "Health Card"
        const val demographicsFolder = "Demographics"

        const val documentFolder = "Documents"


        const val genderMale = "Male"
        const val genderFemale = "Female"
        const val genderUnknown = "Unknown"

        //ImageCropper

        const val mdCameraFiles = "mdCameraFiles"
        const val mdCameraCaptureType = "mdCameraCaptureType"
        const val captureTypeSingleShot = "captureTypeSingleShot"
        const val captureTypeMultipleShot = "captureTypeMultipleShot"
        const val allowCompressionForMdCamera = "allowCompressionForMdCamera"

        //wellness

        const val wellnessTrackerIntent = "wellnessTracker"
        const val wellnessTrackerIdIntent = "wellnessTrackerId"
        const val wellnessIsDefaultIntent = "wellnessIsDefault"
        const val wellnessMetricIntent = "wellnessMetric"
        const val wellnessMetricIdIntent = "wellnessMetricId"
        const val wellnessTrackerDetailIntent = "wellnessTrackerDetail"


        const val moduleNameWellness = "tracker"


        const val wellnessGeneralModelId = "General"
        const val wellnessWeightModelId = "Weight"
        const val wellnessHeightModelId = "Height"
        const val wellnessBMIModelId = "BMI"

        const val wellnessOthersModelId = "Others"

        const val wellnessBPModelId = "Blood Pressure"
        const val wellnessPulseModelId = "Pulse Rate"
        const val wellnessTemperatureModelId = "Temperature"
        const val wellnessBloodSugarModelId = "Blood Sugar"

        const val wellnessCholesterolModelId = "Cholesterol"

        const val metricSystolic = "Systolic"
        const val metricDiastolic = "Diastolic"

        const val wellnessDegree = "\u00B0"

        const val metricFasting = "Fasting"
        const val metricBeforeMeal = "Before Meal"
        const val metricPostPrandial = "Post Prandial"
        const val metricRandom = "Random"


        const val metricWalking = "Walking"
        const val metricCycling = "Cycling"
        const val metricSwimming = "Swimming"
        const val metricJogging = "Jogging"
        const val metricTreadmill = "Treadmill"
        const val metricAerobics = "Aerobics"
        const val metricCalBurned = "Calories Burned"
        const val metricCalConsumed = "Calories Consumed"
        const val metricWater = "Water Consumption"
        const val metricSleep = "Sleep"
        const val metricStep = "Steps Count"
        const val metricOxygenSaturation = "Blood oxygen saturation"
        const val wellnessMetric = "Metric"
        const val wellnessStandard = "Standard"

        //Card on File
        const val cardEditIntent = "cardEdit"
        const val cardEditDataIntent = "cardEditData"
        const val cardEditPositionIntent = "cardEditPosition"
        const val cardEditCountryIntent = "cardEditCountry"
        const val cardEditAddressIntent = "cardEditAddress"
        const val cardEditPracticeIdIntent = "cardEditPracticeId"

        const val FERRITIN_PARAM_NAME = "Ferritin"
        const val FERRITIN_UNIT = "ng/mL"
        const val HB_PARAM_NAME = "Hemoglobin"
        const val HB_UNIT = "g/dL"
        const val MRI_CARDIAC_T2_WARNING_THRESHOLD = 20f
        const val MRI_CARDIAC_T2_CRITICAL_THRESHOLD = 10f

        // Thalassemia flowsheet names
        const val FLOWSHEET_TRANSFUSION_LOG = "Thalassemia - Transfusion Log"
        const val FLOWSHEET_FERRITIN_MONITORING = "Thalassemia - Ferritin Monitoring"
        const val FLOWSHEET_MRI_T2_MONITORING = "Thalassemia - MRI T2 Monitoring"

        // Thalassemia flowsheet field names
        const val FLOWSHEET_FIELD_HB = "Post-Transfusion Hb"
        const val FLOWSHEET_FIELD_NEXT_TRANSFUSION_DATE = "Next Transfusion Due"
        const val FLOWSHEET_FIELD_FERRITIN = "SERUM FERRITIN"
        const val FLOWSHEET_FIELD_LIC = "Liver Iron Concentration / LIC"
        const val FLOWSHEET_FIELD_CARDIAC_T2 = "Cardiac T2*"

        // MRI units
        const val MRI_LIC_UNIT = "mg/g"
        const val MRI_CARDIAC_T2_UNIT = "ms"

        // Thalassemia flowsheet SharedPreference flags
        const val TRANSFUSION_LOG_UPDATED = "transfusion_log_updated"
        const val FERRITIN_MONITORING_UPDATED = "ferritin_monitoring_updated"
        const val MRI_T2_MONITORING_UPDATED = "mri_t2_monitoring_updated"
        const val MEDICATION_UPDATED = "medication_updated"


        fun getTwoDecimalStr(value: Float?): String {

            var formatValue = if (value != null) {
                twoDecimalFormat.format(value)
            } else {
                ""
            }
            if (formatValue.endsWith(".00") || formatValue.endsWith(".0") || formatValue.endsWith(".000")) {
                formatValue = formatValue.split(".")[0]
            }

            return formatValue
        }

        fun getTwoDecimalStr(value: Double?): String {

            var formatValue = if (value != null) {
                twoDecimalFormat.format(value)
            } else {
                ""
            }
            if (formatValue.endsWith(".00") || formatValue.endsWith(".0") || formatValue.endsWith(".000")) {
                formatValue = formatValue.split(".")[0]
            }

            return formatValue
        }


        val hasAlphaRegex = ("(?=.*?[A-Za-z]).+").toRegex()

        fun String.hasAlphaRegex(): Boolean {
            if (this.contains(hasAlphaRegex)) {
                return true
            }
            return false
        }

        fun hasAlpha(s: String): Boolean {


            if (s.isBlank()) {
                return true
            }

            for (element in s) {
                val c = element
                if (c.isLetter()) {
                    return true
                }
            }
            return false
        }

        fun getPracticeDetail(authManager: AuthManager, practiceId: String?): String? {
            val practiceList = authManager.selectedPatientLive.value?.practiceList
                ?: arrayListOf()
            return practiceList.firstOrNull { it.practiceId == practiceId }?.facilityList
        }

        fun getActivePractices(
            authManager: AuthManager,
            selectedPatient: Patient? = null
        ): ArrayList<String> {
            val practiceList =
                selectedPatient?.practiceList ?: authManager.selectedPatientLive.value?.practiceList
                ?: arrayListOf<PracticeList>()
            return ArrayList(practiceList.filter { practice -> practice.isActive }
                .mapNotNull { it.practiceId })
        }


        fun isModuleEnabled(
            sectionConfigList: SectionConfigList?,
            moduleId: String,
            selectedPatient: Patient? = null
        ): Boolean {

            val sectionConfig = if (selectedPatient != null) {
                selectedPatient.sectionConfigList
            } else {
                sectionConfigList
            }

            return sectionConfig?.sectionConfigMapList?.mapNotNull {
                it.sectionConfigList.firstOrNull {
                    it.section == moduleId
                }?.isEnabled
            }?.contains(true)
                ?: true
        }

        fun isPHREntryEnabled(authManager: AuthManager, moduleId: String): Boolean {
            return authManager.selectedPatientSection?.sectionConfigMapList?.mapNotNull { it.sectionConfigList.firstOrNull { it.section == moduleId }?.allowPHREntry }
                ?.contains(true)
                ?: true
        }

        fun getEnabledPractices(
            authManager: AuthManager,
            moduleId: String,
            selectedPatient: Patient? = null
        ): List<String> {

            return if (BuildUtil.isCustomApp) {
                authManager.selectedPatientLive.value?.practiceList?.mapNotNull { it.practiceId }
                    ?: arrayListOf()
            } else {

                val sectionConfig = if (selectedPatient != null) {
                    selectedPatient.sectionConfigList
                } else {
                    authManager.selectedPatientSection
                }

                sectionConfig?.sectionConfigMapList?.filter { it.sectionConfigList.firstOrNull { it.section == moduleId }?.isEnabled == true }
                    ?.mapNotNull { it.practiceId }
                    ?: arrayListOf()
            }
        }

        fun getBluefinEnabledPractice(selectedPatient: Patient?): List<PracticeList>? {
            return selectedPatient?.practiceList?.filter { it.isBluefinEnabled }
        }

        fun getPHREntryEnabledPractices(
            authManager: AuthManager,
            moduleId: String
        ): List<PracticeList> {

            return if (BuildUtil.isCustomApp) {
                authManager.selectedPatientLive.value?.practiceList ?: arrayListOf()
            } else {

                val practiceIdList =
                    authManager.selectedPatientSection?.sectionConfigMapList?.filter { it.sectionConfigList.firstOrNull { it.section == moduleId }?.allowPHREntry == true }
                        ?.mapNotNull { it.practiceId }
                        ?: arrayListOf()
                authManager.selectedPatientLive.value?.practiceList?.filter { practice ->
                    practiceIdList.contains(
                        practice.practiceId
                    ) && practice.isActive
                }
                    ?: arrayListOf()
            }

        }

        fun isPracticeEnabled(
            authManager: AuthManager,
            moduleId: String,
            practiceId: String,
            selectedPatient: Patient? = null
        ): Boolean {
            val enabledPractices = getEnabledPractices(authManager, moduleId, selectedPatient)
            return enabledPractices.isEmpty() || enabledPractices.contains(practiceId)
        }

        fun getModule(authManager: AuthManager, moduleId: String): SectionConfig? {
            return authManager.selectedPatientSection?.sectionConfigMapList?.firstOrNull()?.sectionConfigList?.firstOrNull { it.section == moduleId }
        }

        fun isVideoCallInProgress(): Boolean {

            /*    val zoomSDK = ZoomSDK.getInstance()
                if (zoomSDK.isInitialized) {
                    val meetingStatus = zoomSDK.meetingService?.meetingStatus
                    if (meetingStatus != null) {

                        Log.d("MDBaseActivity", meetingStatus.name)
                        if (meetingStatus != MeetingStatus.MEETING_STATUS_IDLE && meetingStatus != MeetingStatus.MEETING_STATUS_DISCONNECTING) {
                            return true
                        }
                    }
                }*/

            return false
        }

        fun setAppointmentView(
            appointmentType: String?,
            teleMode: String?,
            appointmentListVideoCamLayout: MDmPHRConstraintLayout,
            apptDate: DateTime?,
            meetingTypeTextView: TextView,
            meetingTypeImageView: ImageView, timeZoneString: String
        ) {
            val timeZone = UIUtil.getTimeZone(timeZoneString)
            val currentDate = DateTime.now().withZone(timeZone)
            val appointmentDate =
                UIUtil.formatDateWithTimeZone(apptDate?.toString(sqlTimeStampFormattor()), sqlTimeStampFormattor(), timeZoneString) ?: DateTime.now()
            meetingTypeTextView.visibility = View.GONE
            meetingTypeImageView.visibility = View.GONE
            appointmentListVideoCamLayout.visibility = View.GONE
            if (!BuildUtil.isVideoCallEnabled) {
                if (appointmentType == appointmentTypeVideoCall) {
                    return
                }
                renderMeetingTypeUI(
                    meetingTypeTextView,
                    meetingTypeImageView,
                    appointmentType ?: ""
                )
                return
            }
            if (appointmentType == appointmentTypeVideoCall && teleMode != appointmentTeleModeKiosk
            ) {
                if (appointmentDate.isAfterOrEqual(currentDate)) {

                    if (Minutes.minutesBetween(
                            currentDate,
                            appointmentDate
                        ).minutes < minVideoCallEnableTime
                    ) {
                        appointmentListVideoCamLayout.visibility = View.VISIBLE
                    } else {
                        appointmentListVideoCamLayout.visibility = View.GONE
                        renderMeetingTypeUI(
                            meetingTypeTextView,
                            meetingTypeImageView,
                            appointmentType
                        )
                    }
                } else {
                    appointmentListVideoCamLayout.visibility = View.VISIBLE
                }
            } else {
                appointmentListVideoCamLayout.visibility = View.GONE
                renderMeetingTypeUI(
                    meetingTypeTextView,
                    meetingTypeImageView,
                    appointmentType ?: ""
                )
            }
        }

        fun renderMeetingTypeUI(
            meetingTypeTextView: TextView,
            meetingTypeImageView: ImageView,
            meetingType: String
        ) {
            meetingTypeTextView.visibility = View.VISIBLE
            meetingTypeImageView.visibility = View.VISIBLE
            if (meetingType.equals(appointmentTypeVideoCall, true)) {
                meetingTypeTextView.text = appointmentTypeVideoCall
                meetingTypeImageView.setImageResource(R.drawable.ic_video_appointment)
            } else if (meetingType.equals(appointmentTypeInPerson, true)) {
                meetingTypeTextView.text = appointmentTypeInPerson
                meetingTypeImageView.setImageResource(R.drawable.ic_in_person)
            } else if (meetingType.equals(appointmentTypePhoneCall, true)) {
                meetingTypeTextView.text = appointmentTypePhoneCall
                meetingTypeImageView.setImageResource(R.drawable.ic_call)
            } else {
                meetingTypeTextView.visibility = View.GONE
                meetingTypeImageView.visibility = View.GONE
            }
        }

        fun loadProfilePic(
            appointment: Appointment,
            appointmentProfilePic: de.hdodenhof.circleimageview.CircleImageView,
            appointmentProfileDummyTV: TextView,
            imgSize: Int,
            picasso: Picasso
        ) {
            val memInitials = UIUtil.formMemberInitials(UIUtil.formMemberName(appointment.member))
            appointmentProfileDummyTV.visibility = View.VISIBLE
            appointmentProfileDummyTV.text = memInitials
            appointmentProfilePic.visibility = View.GONE

            val picLoader = picasso.load(
                APIList.getMemberImageURL(
                    appointment.member?.memberId,
                    appointment.practiceId
                )
            )


            picLoader.resize(imgSize, imgSize).into(appointmentProfilePic, object :
                Callback {
                override fun onSuccess() {
                    appointmentProfilePic.visibility = View.VISIBLE
                    appointmentProfileDummyTV.visibility = View.GONE
                }

                override fun onError(e: Exception?) {
                }
            })
        }


        // flowsheet

        const val flowsheetEntryTypeVitals = "VITAL"
        const val flowsheetEntryTypelab = "LAB"
        const val flowsheetEntryTypeHeading = "HEADING"
        const val flowsheetEntryTypeInstructions = "INSTRUCTIONS"
        const val flowsheetEntryTypeSwaasa = "SWAASA"

        const val flowsheetFieldTypeNumber = "Number"
        const val flowsheetFieldTypeMultiLine = "Multi Line Text"
        const val flowsheetFieldTypeSingleLine = "Single Line Text"
        const val flowsheetFieldTypeSingleChoice = "Single Choice Option"
        const val flowsheetFieldTypeMultiChoice = "Multi Choice Option"
        const val flowsheetFieldTypeDate = "Date"
        const val flowsheetFieldTypeDateTime = "Date Time"
        const val flowsheetFieldTypeRatingScale = "Rating Scale"


        const val flowsheetListSelectIntent = "flowsheetListSelect"
        const val flowsheetDataSelectIntent = "flowsheetDataSelect"

        //ViewPager2
        const val viewPagerOffScreenPageLimit = 3

        //Reminder

        const val moduleTypeIntent = "ModuleType"
        const val moduleIdIntent = "ModuleId"
        const val moduleDataIntent = "ModuleData"
        const val isAddReminderIntent = "isAddReminder"
        const val frequencyUnitMinute = "minute"
        const val frequencyUnitHour = "hour"
        const val frequencyUnitDay = "day"
        const val frequencyUnitWeek = "week"
        const val frequencyUnitMonth = "month"
        const val frequencyUnitYear = "year"

        //region Notification pref, remove while logout
        const val installationIdForNotification = "installationIdForNotification"
        const val fcmTokenForNotification = "fcmTokenForNotification"
        const val registeredPatientIdForNotification = "registeredPatientIdForNotification"
        const val isDeviceRegisteredForNotification = "isDeviceRegisteredForNotification"
        const val unregisteredDevicePayload = "unregisteredDevicePayload"
        const val localMessageNotificationIds = "localMessageNotificationIds"
        //endregion

        //region Notification intent
        const val notificationId = "NotificationID"
        const val isNotificationFlow = "isNotificationFlow"
        //endregion

        const val reminderMessage = "messageExtra"
        const val reminderTitle = "titleExtra"
        const val reminderModuleId = "moduleId"
        const val isFromReminder = "isFromReminder"
        const val isAddReminder = "isAddReminder"
        const val resModuleObject = "ResponseReminderModuleObject"
        const val resModuleType = "ResponseReminderModuleType"
        const val drugName = "DrugName"
        const val drugDirection = "DrugDirection"
        const val ReminderPOJO = "ReminderPOJO"
        const val ReminderModuleId = "ReminderModuleId"
        const val IsHistoryPresent = "IsHistoryPresent"
        const val ReminderId = "ReminderId"
        const val ReminderTime = "ReminderTime"
        const val ReminderModuleStartDate = "ReminderModuleStartDate"
        const val ReminderModuleEndDate = "ReminderModuleEndDate"
        const val CompletedStatus = "Completed"
        const val ActiveStatus = "Active"
        const val UpcomingStatus = "Upcoming"

        const val isAppointmentReminderPeriodic = "isAppointmentReminderPeriodic"
        const val isPrescriptionReminderSchedulerStarted = "isPrescriptionReminderSchedulerStarted"
        const val isReminderDataFetched = "isReminderDataFetched"
        const val isPeriodic = "isPeriodic"
        const val lastAlarmScheduleTime = "lastAlarmScheduleTime"
        const val minJobSchedulerJobIdRange = 0
        const val maxJobSchedulerJobIdRange = 1000
        const val reminderSchedulerTimeRange = 24
        const val reminderFlexTimeRange = 12

        const val scheduleReminderTAG = "ScheduleReminderPerDayWorker"
        const val getReminderDataTAG = "FetchReminderDataWorker"
        const val registerDeviceWorkerTAG = "RegisterDeviceWorker"
        const val unregisterDeviceWorkerTAG = "UnRegisterDeviceWorker"

        fun scheduleReminder(
            startDateToSchedule: DateTime,
            endDateToSchedule: DateTime,
            reminderPOJO: ReminderPOJO,
            currentReminder: HashMap<DateTime, ArrayList<FrequencyAlarmMapCustomPOJO>>
        ) {
            val frequencyUnit = reminderPOJO.reminder?.frequencyUnit
            //Map that hold key - Day value if day not configured -1 value - Arraylist of frequency map
            val reminderTimingMap = setReminderTimingMap(reminderPOJO.reminderFrequencyMap)
            val endDate = reminderPOJO.reminder?.endDate ?: DateTime.now()
            scheduleReminderForFrequency(
                frequencyUnit,
                reminderTimingMap,
                endDateToSchedule,
                startDateToSchedule,
                currentReminder,
                reminderPOJO
            )
            //Only if reminder available for next day
            if ((!startDateToSchedule.withTimeAtStartOfDay()
                    .isEqual(endDateToSchedule.withTimeAtStartOfDay())) && endDateToSchedule.isBefore(
                    endDate.plusDays(1)
                )
            ) {
                scheduleReminderForFrequency(
                    frequencyUnit,
                    reminderTimingMap,
                    endDateToSchedule,
                    endDateToSchedule,
                    currentReminder,
                    reminderPOJO
                )
            }
        }

        private fun scheduleReminderForFrequency(
            frequencyUnit: String?,
            reminderTimingMap: HashMap<Int, ArrayList<ReminderFrequencyMap?>>,
            schedulerRange: DateTime,
            schedulerDate: DateTime,
            currentReminder: HashMap<DateTime, ArrayList<FrequencyAlarmMapCustomPOJO>>,
            reminderPOJO: ReminderPOJO
        ) {
            when (frequencyUnit) {
                frequencyUnitDay -> {
                    reminderTimingMap[-1]?.forEach {
                        val reminderScheduler =
                            formReminderScheduler(it, schedulerRange, schedulerDate, reminderPOJO)
                        reminderScheduler?.let { frequencyAlarmMap ->
                            frequencyAlarmMap.scheduledTime?.let { scheduledTime ->
                                if (currentReminder.containsKey(scheduledTime)) {
                                    currentReminder[scheduledTime]?.add(frequencyAlarmMap)
                                } else {
                                    currentReminder[scheduledTime] = arrayListOf(frequencyAlarmMap)
                                }
                            }
                        }
                    }
                }

                frequencyUnitWeek -> {
                    val formatDayOfWeek = getFormattedDayOfWeek(schedulerDate)
                    (reminderTimingMap.containsKey(formatDayOfWeek)).let {
                        reminderTimingMap[formatDayOfWeek]?.forEach {
                            val reminderScheduler = formReminderScheduler(
                                it,
                                schedulerRange,
                                schedulerDate,
                                reminderPOJO
                            )
                            reminderScheduler?.let { frequencyAlarmMap ->
                                frequencyAlarmMap.scheduledTime?.let { scheduledTime ->
                                    if (currentReminder.containsKey(scheduledTime)) {
                                        currentReminder[scheduledTime]?.add(frequencyAlarmMap)
                                    } else {
                                        currentReminder[scheduledTime] =
                                            arrayListOf(frequencyAlarmMap)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun formReminderScheduler(
            reminderFrequencyMap: ReminderFrequencyMap?,
            schedulerRange: DateTime,
            schedulerDate: DateTime,
            reminderPOJO: ReminderPOJO
        ): FrequencyAlarmMapCustomPOJO? {
            val startDateStr = schedulerDate.toString(charmDisplayDateFormat())
            val scheduleTime = UIUtil.formatDate(
                "$startDateStr ${reminderFrequencyMap?.time}",
                charmFlowsheetDateTimeFieldFormat()
            )
            return if ((scheduleTime?.isAfterNow == true) && scheduleTime.isBefore(schedulerRange)) {
                val reminderScheduler = FrequencyAlarmMapCustomPOJO()
                reminderScheduler.localFrequencyMapId =
                    reminderFrequencyMap?.localFrequencyMapId ?: 0
                reminderScheduler.scheduledTime = scheduleTime
                reminderScheduler.reminderId = reminderPOJO.reminder?.reminderId
                reminderScheduler.moduleType = reminderPOJO.reminderMap.firstOrNull()?.type
                reminderScheduler
            } else {
                null
            }
        }

        //TODO to be used when n frequency is added
//        fun scheduleReminder(reminderPOJO: ReminderPOJO): ArrayList<FrequencyAlarmMap> {
//            val schedulerRange=DateTime.now().plusHours(26)
//            val reminderSchedulerList = arrayListOf<FrequencyAlarmMap>()
//            val frequencyUnit = reminderPOJO.reminder?.frequencyUnit
//            val frequencyValue = reminderPOJO.reminder?.frequency?.toInt() ?: 0
//            val reminderTimingMap = setReminderTimingMap(reminderPOJO.reminderFrequencyMap)
//            var startDate = reminderPOJO.reminder?.startDate ?: DateTime.now()
//            val endDate = reminderPOJO.reminder?.endDate ?: DateTime.now()
//            when (frequencyUnit) {
//                null -> {
//                    val reminderScheduler = FrequencyAlarmMap()
//                    reminderScheduler.localFrequencyMapId=reminderTimingMap.get(-1)?.get(0)?.localFrequencyMapId?:0
//                    val startTime = reminderTimingMap.get(-1)?.get(0)?.time
//                    var scheduleTime = DateTime.parse("$startDate $startTime", DateTimeFormat.forPattern(charmFlowsheetDateTimeFieldFormat))
//                    reminderScheduler.scheduledTime = scheduleTime
//                    if(scheduleTime.isAfterNow && scheduleTime.isBefore(schedulerRange))
//                    {
//                        reminderSchedulerList.add(reminderScheduler)
//                    }
//                }
//                frequencyUnitMinute -> {
//                    val startDateStr = startDate?.toString(charmDisplayDateFormat)
//                    val endDateStr = endDate?.toString(charmDisplayDateFormat)
//                    val startTime = reminderTimingMap.get(-1)?.get(0)?.time
//                    var scheduleTime = DateTime.parse("$startDateStr $startTime", DateTimeFormat.forPattern(charmFlowsheetDateTimeFieldFormat))
//                    val endDateTime = DateTime.parse("$endDateStr 23:59", DateTimeFormat.forPattern(charmFlowsheetDateTimeFieldFormat))
//                    while (scheduleTime.isBefore(endDateTime)) {
//                        val reminderScheduler = FrequencyAlarmMap()
//                        reminderScheduler.localFrequencyMapId =reminderTimingMap.get(-1)?.get(0)?.localFrequencyMapId?:0
//                        reminderScheduler.scheduledTime = scheduleTime
//                        if(scheduleTime.isAfterNow && scheduleTime.isBefore(schedulerRange))
//                        {
//                            reminderSchedulerList.add(reminderScheduler)
//                        }
//                        scheduleTime = scheduleTime.plusMinutes(frequencyValue)
//                    }
//                }
//                frequencyUnitHour -> {
//
//                    val startDateStr = startDate?.toString(charmDisplayDateFormat)
//                    val endDateStr = endDate?.toString(charmDisplayDateFormat)
//                    val startTime = reminderTimingMap.get(-1)?.get(0)?.time
//                    var scheduleTime = DateTime.parse("$startDateStr $startTime", DateTimeFormat.forPattern(charmFlowsheetDateTimeFieldFormat))
//                    val endDateTime = DateTime.parse("$endDateStr 23:59", DateTimeFormat.forPattern(charmFlowsheetDateTimeFieldFormat))
//                    while (scheduleTime.isBefore(endDateTime)) {
//                        val reminderScheduler = FrequencyAlarmMap()
//                        reminderScheduler.localFrequencyMapId = reminderTimingMap.get(-1)?.get(0)?.localFrequencyMapId?:0
//                        reminderScheduler.scheduledTime = scheduleTime
//                        if(scheduleTime.isAfterNow && scheduleTime.isBefore(schedulerRange))
//                        {
//                            reminderSchedulerList.add(reminderScheduler)
//                        }
//                        scheduleTime = scheduleTime.plusHours(frequencyValue)
//                    }
//                }
//                frequencyUnitDay -> {
//                    while (startDate.isBefore(endDate.plusDays(1))) {
//                        reminderTimingMap[-1]?.forEach {
//                            val reminderScheduler = FrequencyAlarmMap()
//                            reminderScheduler.localFrequencyMapId = it?.localFrequencyMapId?:0
//                            val startDateStr = startDate?.toString(charmDisplayDateFormat)
//                            val scheduleTime = DateTime.parse("$startDateStr ${it?.time}", DateTimeFormat.forPattern(charmFlowsheetDateTimeFieldFormat))
//                            reminderScheduler.scheduledTime = scheduleTime
//                            if(scheduleTime.isAfterNow && scheduleTime.isBefore(schedulerRange))
//                            {
//                                reminderSchedulerList.add(reminderScheduler)
//                            }
//                        }
//                        startDate = startDate.plusDays(frequencyValue + 1)
//                    }
//                }
//                frequencyUnitWeek -> {
//                    while (startDate.isBefore(endDate.plusDays(1))) {
//                        if (reminderTimingMap.containsKey(startDate.dayOfWeek)) {
//                            reminderTimingMap[startDate.dayOfWeek]?.forEach {
//                                val reminderScheduler = FrequencyAlarmMap()
//                                reminderScheduler.localFrequencyMapId =  it?.localFrequencyMapId?:0
//                                val startDateStr = startDate?.toString(charmDisplayDateFormat)
//                                val scheduleTime = DateTime.parse("$startDateStr ${it?.time}", DateTimeFormat.forPattern(charmFlowsheetDateTimeFieldFormat))
//                                reminderScheduler.scheduledTime = scheduleTime
//                                if(scheduleTime.isAfterNow && scheduleTime.isBefore(schedulerRange))
//                                {
//                                    reminderSchedulerList.add(reminderScheduler)
//                                }
//                            }
//                        }
//                        startDate = startDate.plusDays(1)
//                        if (startDate.dayOfWeek == 7) {
//                            startDate = startDate.plusWeeks(frequencyValue)
//                        }
//                    }
//
//                }
//                frequencyUnitMonth -> {
//                    //not supported
//                }
//                frequencyUnitYear -> {
//                    //not supported
//                }
//            }
//            return reminderSchedulerList
//        }

        fun getFormattedDayOfWeek(dateTime: DateTime): Int {
            val weekday = dateTime.dayOfWeek
            return if (weekday == 7) 0 else weekday
        }

        fun setReminderTimingMap(reminderFrequencyMap: List<ReminderFrequencyMap>): HashMap<Int, ArrayList<ReminderFrequencyMap?>> {
            val reminderTimingMap = hashMapOf<Int, ArrayList<ReminderFrequencyMap?>>()
            reminderFrequencyMap.forEach {
                val day = it.day
                if (reminderTimingMap.containsKey(day)) {
                    reminderTimingMap[day]?.add(it)
                } else {
                    reminderTimingMap[day] = arrayListOf(it)
                }
            }
            return reminderTimingMap
        }

        //InAppRating
        const val isInAppRatingTriggered = "isInAppRatingTriggered"


        fun formReminderHistoryPOJO(
            reminderPOJO: ReminderCustomPOJO,
            status: Int,
            scheduledTime: String
        ): ReminderHistoryPOJO {
            val reminderHistoryPOJO = ReminderHistoryPOJO()
            val tempFrequencyMap = reminderPOJO.reminderFrequencyMap.filter {
                UIUtil.formatDate(it.time, charmDisplay24HrTimeFormat())
                    ?.toString(charmDisplayTimeFormat) == scheduledTime
            }
            reminderHistoryPOJO.reminderMap = arrayListOf(reminderPOJO.reminderMap.first())
            reminderHistoryPOJO.reminderFrequencyMap = tempFrequencyMap
            reminderHistoryPOJO.reminderHistory = ReminderHistory()
            reminderHistoryPOJO.reminderHistory?.statusUpdatedTime = DateTime.now()
            reminderHistoryPOJO.reminderHistory?.status = status
            reminderHistoryPOJO.reminderHistory?.reminderTime = tempFrequencyMap.firstOrNull()?.time
            reminderHistoryPOJO.reminderHistory?.reminderMapId = reminderPOJO.reminder?.reminderId
            reminderHistoryPOJO.reminderHistory?.frequencyMapId =
                tempFrequencyMap.firstOrNull()?.frequencyMapId
            return reminderHistoryPOJO
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun createNotificationChannel(
            appContext: Context,
            id: String,
            name: String,
            description: String
        ) {

            val notificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, name, importance)

            channel.description = description
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.setShowBadge(false)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationManager.createNotificationChannel(channel)

        }

        //Google Fit Integration
        const val syncWithGoogleFit = "Sync With Google Fit"

        /* private val charmScopes = "AaaServer.profile.READ,ZohoContacts.userprofilephoto.READ,ZohoDirectory.usersAPI.CREATE,ZohoChat.Channels.ALL,ZohoChat.Chats.READ,ZohoChat.Chats.CREATE,ZohoChat.Chats.UPDATE,ZohoChat.Bots.ALL,ZohoChat.Commands.READ,ZohoChat.Buddies.ALL,ZohoChat.webhooks.CREATE,ZohoChat.Applications.ALL,ZohoDirectory.orgsjoinrequest.READ,ZohoDirectory.orgsjoinrequest.CREATE,ZohoDirectory.orgsjoinrequest.DELETE,ZohoChat.Organisation.READ,ZohoChat.Organisation.CREATE,ZohoChat.Organisation.UPDATE,ZohoChat.Teams.READ,ZohoChat.Messages.ALL,ZohoChat.Attachments.READ,ZohoChat.Attachments.CREATE,ZohoChat.profile.READ,ZohoChat.profile.UPDATE,ZohoChat.PrimeTime.ALL,ZohoChat.smileys.READ,ZohoChat.mobile.UPDATE,ZohoDirectory.ZOHANDLERAPI.CREATE,ZohoChat.ZOHANDLERAPI.CREATE,ZohoChat.messageactions.ALL,ZohoChat.StorageData.ALL,ZohoChat.Storage.ALL,ZohoChat.Reminders.ALL,ZohoDirectory.UserSessionsAPI.CREATE,ZohoDirectory.UserTFAAPI.CREATE,ZohoDirectory.UserLoginHistoryAPI.CREATE,ZohoContacts.userphoto.ALL,ZohoChat.Users.READ,ZohoChat.Departments.ALL"

         private val cliqScopes = "ZohoPeople.dashboard.READ,"                      +    // No I18N
                 "ZohoPeople.forms.READ,"                          +    // No I18N
                 "ZohoPeople.attendance.READ,"                     +    // No I18N
                 "ZohoChat.PrimeTime.CREATE,"                      +    // No I18N
                 "ZohoChat.PrimeTime.READ,"                        +    // No I18N
                 "ZohoChat.PrimeTime.UPDATE,"                      +    // No I18N
                 "ZohoChat.PrimeTime.DELETE,"                      +    // No I18N
                 "ZohoChat.Messageactions.CREATE,"                 +    // No I18N
                 "ZohoChat.Messageactions.DELETE,"                 +    // No I18N
                 "ZohoChat.Applications.ALL,"                      +    // No I18N
                 "ZohoChat.LiveMedia.ALL,"                         +    // No I18N
                 "ZohoChat.Users.READ,"                            +    // No I18N
                 "ZohoWriter.documenteditor.ALL,"                  +    // No I18N
                 "ZohoChat.Departments.ALL,"                       +    // No I18N
                 "ZohoChat.Messageactions.UPDATE,"                 +    // No I18N
                 "ZohoGadgets.translateSync.READ,"                 +    // No I18N
                 "ZohoChat.Chats.DELETE," +
         "DRE.dreapi.CREATE,"                              +     // No I18N
         "DRE.dreapi.DELETE,"                              +     // No I18N
         "DRE.dreapi.READ,"                                +     // No I18N
         "DRE.dreapi.UPDATE,"                              +     // No I18N
         "VirtualOffice.organization.subscriptions.READ,"  +     // No I18N
         "ZohoCalendar.calendar.CREATE,"                   +     // No I18N
         "ZohoCalendar.calendar.DELETE,"                   +     // No I18N
         "ZohoCalendar.calendar.READ,"                     +     // No I18N
         "ZohoCalendar.calendar.UPDATE,"                   +     // No I18N
         "ZohoCalendar.event.CREATE,"                      +     // No I18N
         "ZohoCalendar.event.DELETE,"                      +     // No I18N
         "ZohoCalendar.event.READ,"                        +     // No I18N
         "ZohoCalendar.event.UPDATE,"                      +     // No I18N
         "ZohoChat.Attachments.CREATE,"                    +     // No I18N
         "ZohoChat.Attachments.READ,"                      +     // No I18N
         "ZohoChat.Bots.CREATE,"                           +     // No I18N
         "ZohoChat.Bots.DELETE,"                           +     // No I18N
         "ZohoChat.Bots.READ,"                             +     // No I18N
         "ZohoChat.Bots.UPDATE,"                           +     // No I18N
         "ZohoChat.Buddies.CREATE,"                        +     // No I18N
         "ZohoChat.Buddies.DELETE,"                        +     // No I18N
         "ZohoChat.Buddies.READ,"                          +     // No I18N
         "ZohoChat.Buddies.UPDATE,"                        +     // No I18N
         "ZohoChat.Channels.CREATE,"                       +     // No I18N
         "ZohoChat.Channels.DELETE,"                       +     // No I18N
         "ZohoChat.Channels.READ,"                         +     // No I18N
         "ZohoChat.Channels.UPDATE,"                       +     // No I18N
         "ZohoChat.Chats.CREATE,"                          +     // No I18N
         "ZohoChat.Chats.READ,"                            +     // No I18N
         "ZohoChat.Chats.UPDATE,"                          +     // No I18N
         "ZohoChat.Commands.READ,"                         +     // No I18N
         "ZohoChat.Messageactions.READ,"                   +     // No I18N
         "ZohoChat.Messageactions.UPDATE,"                 +     // No I18N
         "ZohoChat.Messages.CREATE,"                       +     // No I18N
         "ZohoChat.Messages.DELETE,"                       +     // No I18N
         "ZohoChat.Messages.READ,"                         +     // No I18N
         "ZohoChat.Messages.UPDATE,"                       +     // No I18N
         "ZohoChat.Organisation.CREATE,"                   +     // No I18N
         "ZohoChat.Organisation.READ,"                     +     // No I18N
         "ZohoChat.Organisation.UPDATE,"                   +     // No I18N
         "ZohoChat.Reminders.CREATE,"                      +     // No I18N
         "ZohoChat.Reminders.READ,"                        +     // No I18N
         "ZohoChat.Reminders.UPDATE,"                      +     // No I18N
         "ZohoChat.Reminders.DELETE,"                      +     // No I18N
         "ZohoChat.Smileys.READ,"                          +     // No I18N
         "ZohoChat.Storage.CREATE,"                        +     // No I18N
         "ZohoChat.Storage.READ,"                          +     // No I18N
         "ZohoChat.Storage.UPDATE,"                        +     // No I18N
         "ZohoChat.Storage.DELETE,"                        +     // No I18N
         "ZohoChat.StorageData.CREATE,"                    +     // No I18N
         "ZohoChat.StorageData.READ,"                      +     // No I18N
         "ZohoChat.StorageData.UPDATE,"                    +     // No I18N
         "ZohoChat.StorageData.DELETE,"                    +     // No I18N
         "ZohoChat.Teams.READ,"                            +     // No I18N
         "ZohoChat.mobile.UPDATE,"                         +     // No I18N
         "ZohoChat.profile.READ,"                          +     // No I18N
         "ZohoChat.profile.UPDATE,"                        +     // No I18N
         "ZohoChat.webhooks.CREATE,"                       +     // No I18N
         "ZohoDirectory.orgsjoinrequest.CREATE,"           +     // No I18N
         "ZohoDirectory.orgsjoinrequest.DELETE,"           +     // No I18N
         "ZohoDirectory.orgsjoinrequest.READ,"             +     // No I18N
         "ZohoDirectory.users.CREATE,"                     +     // No I18N
         "ZohoPC.files.READ,"                              +     // No I18N
         "ZohoPeople.employee.READ,"                       +     // No I18N
         "ZohoPulse.oembed.READ,"                          +     // No I18N
         "ZohoSearch.securesearch.READ,"                   +     // No I18N
         "ZohoVideo.videoauthapi.CREATE,"                  +     // No I18N
         "ZohoVideo.videoauthapi.DELETE,"                  +     // No I18N
         "ZohoVideo.videoauthapi.READ,"                    +     // No I18N
         "ZohoVideo.videoauthapi.UPDATE,"                  +     // No I18N

         "ZohoProfile.userphoto.UPDATE,"                   +     // No I18N
         "ZohoContacts.userphoto.READ,"                    +     // No I18N

         "ZohoVideo.videochatapi.UPDATE"

         private val scopesToBeRemoved = "DRE.dreapi.ALL," +
                 "VirtualOffice.organization.subscriptions.READ," +
                 "ZohoCalendar.calendar.ALL," +
                 "ZohoCalendar.event.ALL," +
                 "ZohoPC.files.READ," +
                 "ZohoPeople.employee.READ," +
                 "ZohoPulse.oembed.READ," +
                 "ZohoVideo.videoauthapi.ALL," +
                 "ZohoVideo.videochatapi.UPDATE," +
                 "ZohoPeople.attendance.READ," +
                 "ZohoPeople.forms.READ," +
                 "ZohoPeople.dashboard.READ," +
                 "ZohoPulse.blogs.ALL"







         fun showMissingScopes(){

             var cliqScopesList = cliqScopes.split(",").filter { it.isNotBlank() }

             val toBeRemovedScopes = scopesToBeRemoved.split(",").filter { it.isNotBlank() }
             cliqScopesList = cliqScopesList.filter { !toBeRemovedScopes.contains(it) }


             val charmScopeList = charmScopes.split(",").filter { it.isNotBlank() }

             val missingScopes = cliqScopesList.filter {cliq -> !charmScopeList.map { it.toLowerCase() }.contains(cliq.toLowerCase()) }

             Log.d("missingScopesList" , missingScopes.joinToString(", "))
         }
 */

        inline fun MutableLiveData<Any>.checkOnlineAndPatientId(
            authManager: AuthManager,
            appContext: Context,
            block: (String, Patient?) -> Unit
        ) {
            if (!authManager.isUserOnline()) {
                this.postValue(
                    APIResponseError(
                        errorInternetConnectionCode,
                        null,
                        appContext.getString(R.string.internet_connection_error)
                    )
                )
            }
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isBlank()) {
                this.postValue(
                    APIResponseError(
                        null,
                        null,
                        appContext.getString(R.string.error_patient_id_not_found)
                    )
                )
            }
            block.invoke(patientId, authManager.selectedPatientLive.value)
        }

        fun checkOnlineAndPatientId(authManager: AuthManager, appContext: Context): Any {
            if (!authManager.isUserOnline()) {
                return APIResponseError(
                    errorInternetConnectionCode,
                    null,
                    appContext.getString(R.string.internet_connection_error)
                )
            }
            val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
            if (patientId.isBlank()) {
                return APIResponseError(
                    null,
                    null,
                    appContext.getString(R.string.error_patient_id_not_found)
                )
            }
            return Pair(patientId, authManager.selectedPatientLive.value)
        }

        fun JsonElement?.isNotJsonNull(): Boolean {
            return try {
                this != null && !this.isJsonNull
            } catch (e: Exception) {
                Log.e(TAG, Log.getStackTraceString(e))
                false
            }
        }

        fun String?.parseStringAsJsonObject(): JsonObject? {
            if (this.isNullOrBlank()) return null
            try {
                val jsonElement = JsonParser.parseString(this)
                if (jsonElement.isJsonObject && jsonElement.isNotJsonNull()) {
                    return jsonElement.asJsonObject
                }
            } catch (ex: Exception) {
                Log.e(TAG, ex.printStackTrace().toString())
            }
            return null
        }


        fun String?.parseStringAsJsonArray(): JsonArray? {
            if (this.isNullOrBlank()) return null
            try {
                val jsonElement = JsonParser.parseString(this)
                if (jsonElement.isJsonArray && jsonElement.isNotJsonNull()) {
                    return jsonElement.asJsonArray
                }
            } catch (ex: Exception) {
                Log.e(TAG, ex.printStackTrace().toString())
            }
            return null
        }

        fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
            }

        fun getPrivilegeStr(privilegeName: String?): RbacUtil.Companion.RBACPrivileges {
            if (privilegeName?.equals(visitSummaryModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_VISITSUMMARY
            } else if (privilegeName?.equals(appointmentModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_APPOINTMENT
            } else if (privilegeName?.equals(billingModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_INVOICE
            } else if (privilegeName?.equals(messageModuleId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_MESSAGES
            } else if (privilegeName?.equals(labModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_LABRESULTS
            } else if (privilegeName?.equals(prescriptionModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_MEDICATIONS
            } else if (privilegeName?.equals(flowsheetModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_FLOWSHEETDATA
            } else if (privilegeName?.equals(questionnaireModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_QUESTIONNAIRE
            } else if (privilegeName?.equals(insuranceModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_INSURANCE
            } else if (privilegeName?.equals(usefulInfoModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_USEFULINFO
            } else if (privilegeName?.equals(aboutModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_CLINICALSUMMARY
            } else if (privilegeName?.equals(billingModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_INVOICE
            } else if (privilegeName?.equals(healthPassportModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_MEDICATIONS
            } else if (privilegeName?.equals(reminderModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_REMINDER
            } else if (privilegeName?.equals(procedureModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_PROCEDURE
            } else if (privilegeName?.equals(medicationModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_MEDICATIONS
            } else if (privilegeName?.equals(supplementModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_SUPPLEMENTS
            } else if (privilegeName?.equals(wellnessModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_WELLNESS
            } else if (privilegeName?.equals(documentModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_DOCUMENTS
            } else if (privilegeName?.equals(allergiesModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_ALLERGIES
            } else if (privilegeName?.equals(vaccineModelId) == true) {
                return RbacUtil.Companion.RBACPrivileges.VIEW_IMMUNE
            }
            return RbacUtil.Companion.RBACPrivileges.NO_ACCESS
        }

        fun Module.updateModuleVisibility(context: Context?, authManager: AuthManager) {
            this.isShown = when {
                this.moduleId == aboutModelId || this.moduleId == usefulInfoModelId -> {
                    RbacUtil.checkPrivilege(getPrivilegeStr(this.moduleId), context)
                }
                this.moduleId == reminderModelId -> {
                    RbacUtil.checkPrivilege(getPrivilegeStr(this.moduleId), context)
                }
                this.moduleId == healthPassportModelId -> {
                    authManager.selectedPatientLive.value?.isHealthPassportAvailable == true
                }
                else -> {
                    RbacUtil.checkPrivilege(getPrivilegeStr(this.moduleId), context) &&
                            isModuleEnabled(authManager.selectedPatientSection, this.moduleId)
                }
            }
        }

        fun getGraphMinMaxValue(graphData: GraphData): Pair<Float, Float> {
            var minValue = 100000f
            var maxValue = 0f
            graphData.dataList.forEach { iLineData ->
                val entryCount = iLineData.entryCount

                for (index in 0 until entryCount) {
                    val entry = iLineData.getEntryForIndex(index)
                    if (entry != null) {

                        if (minValue > entry.y) {
                            minValue = entry.y
                        }

                        if (maxValue < entry.y) {
                            maxValue = entry.y
                        }
                    }
                }
            }

            if (minValue == maxValue) {
                maxValue += (maxValue * 0.1f)
                minValue -= (minValue * 0.1f)
            }

            if (maxValue > 5) {
                minValue -= (minValue % 5)
                maxValue += (5 - (maxValue % 5))
            } else {
                minValue = 0f
            }

            val range = maxValue - minValue
            val interval = range / 7
            maxValue += interval
            minValue -= interval

            if (minValue < 0) {
                minValue = 0f
            }
            return Pair(minValue, maxValue)
        }
    }
}