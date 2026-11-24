package com.charm.phr.mphr.util

import android.content.Context
import android.util.Log
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Allergy
import com.charm.phr.mphr.model.Announcements
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.model.AppointmentConfig
import com.charm.phr.mphr.model.AppointmentStatus
import com.charm.phr.mphr.model.CardOnFilePOJO
import com.charm.phr.mphr.model.Category
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.Consultation
import com.charm.phr.mphr.model.ContactDetails
import com.charm.phr.mphr.model.DataWithId
import com.charm.phr.mphr.model.Diagnosis
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.model.DocumentMetaData
import com.charm.phr.mphr.model.DosageUnit
import com.charm.phr.mphr.model.DrugDetails
import com.charm.phr.mphr.model.Facility
import com.charm.phr.mphr.model.FlowsheetEntries
import com.charm.phr.mphr.model.Flowsheets
import com.charm.phr.mphr.model.HealthMetric
import com.charm.phr.mphr.model.HealthPassportModel
import com.charm.phr.mphr.model.ImmunizationList
import com.charm.phr.mphr.model.InsuranceCommonData
import com.charm.phr.mphr.model.InsuredAndGuarantorDetails
import com.charm.phr.mphr.model.InsuredEmployerDetails
import com.charm.phr.mphr.model.IntakeQuestAllergyEntry
import com.charm.phr.mphr.model.IntakeQuestMedicationEntry
import com.charm.phr.mphr.model.IntakeQuestSupplementEntry
import com.charm.phr.mphr.model.IntakeQuestTemplateEntry
import com.charm.phr.mphr.model.IntakeQuestTemplateLabels
import com.charm.phr.mphr.model.IntakeQuestTemplateOption
import com.charm.phr.mphr.model.IntakeQuestTemplateRating
import com.charm.phr.mphr.model.LabOrderDetail
import com.charm.phr.mphr.model.LabParamHistoryPOJO
import com.charm.phr.mphr.model.Manufacturer
import com.charm.phr.mphr.model.MedicalCondition
import com.charm.phr.mphr.model.MedicalRecordEntry
import com.charm.phr.mphr.model.MedicalRecordGroup
import com.charm.phr.mphr.model.MedicalRecordParamValueMap
import com.charm.phr.mphr.model.MedicalRecordParams
import com.charm.phr.mphr.model.MedicalRecordValues
import com.charm.phr.mphr.model.MedicalRecords
import com.charm.phr.mphr.model.MedicationCommonData
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.MemberAvailability
import com.charm.phr.mphr.model.MemberWorkTiming
import com.charm.phr.mphr.model.MenuList
import com.charm.phr.mphr.model.MerchantAccountInfo
import com.charm.phr.mphr.model.MessageGroup
import com.charm.phr.mphr.model.MessageGroupMap
import com.charm.phr.mphr.model.MessageMembersMap
import com.charm.phr.mphr.model.MessageReceipients
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.model.MessagesAttachmentsMap
import com.charm.phr.mphr.model.Metric
import com.charm.phr.mphr.model.OpenTokCredentials
import com.charm.phr.mphr.model.OptionData
import com.charm.phr.mphr.model.PHRBills
import com.charm.phr.mphr.model.PHRFlowsheets
import com.charm.phr.mphr.model.PHRInvoicesDetail
import com.charm.phr.mphr.model.PHRReceipts
import com.charm.phr.mphr.model.PHRStatementDetail
import com.charm.phr.mphr.model.ParamHistoryEntryPOJO
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientAllergy
import com.charm.phr.mphr.model.PatientAllergyCommonData
import com.charm.phr.mphr.model.PatientCommonData
import com.charm.phr.mphr.model.PatientEncounterList
import com.charm.phr.mphr.model.PatientImmunizationCommonData
import com.charm.phr.mphr.model.PatientImmunizationHistory
import com.charm.phr.mphr.model.PatientImmunizationMap
import com.charm.phr.mphr.model.PatientInsuranceDetails
import com.charm.phr.mphr.model.PatientMedicalCondition
import com.charm.phr.mphr.model.PatientMedications
import com.charm.phr.mphr.model.PatientMedicationsExt
import com.charm.phr.mphr.model.PatientPrescriptionHistory
import com.charm.phr.mphr.model.PatientQuesMap
import com.charm.phr.mphr.model.PatientSupplementHistory
import com.charm.phr.mphr.model.PatientSupplementMap
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.model.PatientVaccineMap
import com.charm.phr.mphr.model.PatientVitalTracker
import com.charm.phr.mphr.model.PatientVitals
import com.charm.phr.mphr.model.PaymentGatewayToken
import com.charm.phr.mphr.model.Pharmacy
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.model.PostalAddress
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.model.PracticePayer
import com.charm.phr.mphr.model.ProceduresList
import com.charm.phr.mphr.model.ProceduresListCommonData
import com.charm.phr.mphr.model.ProceduresMap
import com.charm.phr.mphr.model.QuestionnaireDetailPOJO
import com.charm.phr.mphr.model.QuestionnaireEntryPOJO
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.ReviewMedicalRecordEntries
import com.charm.phr.mphr.model.SOAPTemplateTableCell
import com.charm.phr.mphr.model.SOAPWidget
import com.charm.phr.mphr.model.SOAPWidgetEntry
import com.charm.phr.mphr.model.SOAPWidgetEntryPOJO
import com.charm.phr.mphr.model.SOAPWidgetPOJO
import com.charm.phr.mphr.model.SOAPWidgetProperty
import com.charm.phr.mphr.model.SOAPWidgetSelectableEntry
import com.charm.phr.mphr.model.SectionConfig
import com.charm.phr.mphr.model.SectionConfigList
import com.charm.phr.mphr.model.SectionPracticeMap
import com.charm.phr.mphr.model.ShareDocRestriction
import com.charm.phr.mphr.model.SlotMetaData
import com.charm.phr.mphr.model.StateList
import com.charm.phr.mphr.model.SupplementCommonData
import com.charm.phr.mphr.model.SupplementList
import com.charm.phr.mphr.model.Tracker
import com.charm.phr.mphr.model.TrackerCommonData
import com.charm.phr.mphr.model.TrackerMetricMap
import com.charm.phr.mphr.model.UnreadCount
import com.charm.phr.mphr.model.UsefulInfo
import com.charm.phr.mphr.model.VaccineImmunizationPOJO
import com.charm.phr.mphr.model.VaccinesList
import com.charm.phr.mphr.model.VisitTypeDetail
import com.charm.phr.mphr.model.VisitTypes
import com.charm.phr.mphr.model.ZoomCredentials
import com.charm.phr.mphr.model.TeleHealthCredentials
import com.charm.phr.mphr.model.database.entity.Medications
import com.charm.phr.mphr.model.database.entity.Reminder
import com.charm.phr.mphr.model.database.entity.ReminderFrequencyMap
import com.charm.phr.mphr.model.database.entity.ReminderHistory
import com.charm.phr.mphr.model.database.entity.ReminderMap
import com.charm.phr.mphr.model.database.entity.Supplements
import com.charm.phr.mphr.util.ModelUtil.Companion.isNotJsonNull
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonArray
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.joda.time.DateTime
import org.joda.time.LocalDate

class MDParsingHelper {

    companion object {

        //region patient

        fun formPatientList(patientResp: JsonObject): ArrayList<Patient> {

            val patientList = arrayListOf<Patient>()

            /*if (BuildUtil.isCustomApp){
                val patientListObj  = patientResp.getAsJsonObject("output_string")

                if (patientListObj != null){

                    val patientIdList = patientListObj.getAsJsonArray("PROFILE_IDS")

                    patientIdList?.forEach {patientIdEle ->

                        val patientId = patientIdEle

                        val patientObj =   patientListObj.getAsJsonObject(patientId)

                        if (patientObj != null){

                            val patient = Patient()
                            patient.practicePatientId = patientId
                            patient.firstName = patientObj.getString("FIRST_NAME")
                            patient.lastName = patientObj.getString("LAST_NAME")
                            patient.fullName = "${patient.firstName} ${patient.lastName}"

                            val dateStr = patientObj.getString("DATE_OF_BIRTH") ?: ""

                            if (dateStr.isNotBlank()) {
                                patient.dateOfBirth =
                                    LocalDate.parse(dateStr , DateTimeFormat.forPattern(ModelUtil.charmApiDateFormat))
                            }
                            patient.gender = patientObj.getString("GENDER")
                            patient.age = patientObj.getString("AGE")
                            patient.isPhotoAvailableOnline = patientObj.getString("IS_PHOTO")?.asBoolean ?: false

                            patient.practiceList = arrayListOf(PracticeList(practiceId = BuildUtil.practiceId , isActive = true))
                            patientList.add(patient)
                        }
                    }
                }
            }
            else {*/
            val patientArr = patientResp.getAsJsonArray("output_string")

            patientArr.forEach { patientEle ->
                val patientObj = patientEle.asJsonObject
                val patient = Patient()
                patient.practicePatientId = patientObj.getString("PATIENT_ID")// ,  ,  ,
                patient.firstName = patientObj.getString("FIRST_NAME")
                patient.lastName = patientObj.getString("LAST_NAME")
                patient.fullName = "${patient.firstName} ${patient.lastName}"

                patient.isHealthPassportAvailable =
                    patientObj.getBoolean("IS_HEALTH_PASSPORT_AVAILABLE")
                        ?: false

                val dateStr = patientObj.getString("DATE_OF_BIRTH") ?: ""

                if (dateStr.isNotBlank()) {
                    patient.dateOfBirth =
                        LocalDate.parse(
                            dateStr,
                            ModelUtil.charmApiDateFormat()
                        )
                }
                patient.gender = patientObj.getString("GENDER")
                patient.isSecondaryAccount = patientObj.getBoolean("SECONDARY_ACCOUNT") ?: false
                patient.isPhotoAvailableOnline = patientObj.getBoolean("IS_PHOTO_AVAILABLE") ?: true

                val practiceArr = patientObj.getAsJsonArray("PRACTICES_LIST")

                val practiceList = arrayListOf<PracticeList>()

                practiceArr?.forEach { practiceEle ->
                    val practiceObj = practiceEle?.asJsonObject

                    if (practiceObj != null) {//PRACTICE_ID , PRACTICE_NAME
                        val practice = PracticeList()

                        practice.practiceId = practiceObj.getString("PRACTICE_ID")
                        practice.practiceName = practiceObj.getString("PRACTICE_NAME")
                        practice.easyCareURL = practiceObj.getString("EASYCARE_URL")
                        practice.isActive = practiceObj.getBoolean("IS_ACTIVE") ?: false
                        practice.isBluefinEnabled =
                            practiceObj.getBoolean("IS_BLUEFIN_ENABLED") ?: false
                        if (practice.isBluefinEnabled) {
                            patient.isBluefinEnabled = true
                        }
                        practice.facilityList = practiceObj.getString("FACILITIES_LIST")?.split(",")
                            ?.filter { it.isNotBlank() }?.joinToString(", ")

                        if (practice.isActive && ((!BuildUtil.isCustomApp && !practice.practiceId.isNullOrBlank() && !practice.practiceName.isNullOrBlank()) || (BuildUtil.isCustomApp && practice.practiceId == BuildUtil.practiceId))) {
                            // if (practice.practiceId == BuildUtil.practiceId){
                            if (BuildUtil.isCustomApp) {
                                practice.isActive = true
                            }
                            practiceList.add(practice)
                        }
                    }
                }

                if (practiceList.isNotEmpty()) {
                    patient.practiceList = practiceList
                    patientList.add(patient)
                }

            }

            //}

            return patientList
        }

        fun formPatientObj(respObj: JsonObject): Patient {

            val patient = Patient()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {//  ,  ,  ,

                //
                val contactDetails = ContactDetails()
                contactDetails.mobile = outputData.getString("ACC_MOBILE")
                contactDetails.landline = outputData.getString("ACC_LANDLINE")

                patient.bloodGroup = outputData.getString("BLOOD_GROUP")
                patient.dateOfBirth = LocalDate.parse(
                    outputData.getString("DATE_OF_BIRTH"),
                    ModelUtil.charmApiDateFormat()
                )
                patient.emergencyContactExtn = outputData.getString("EMERGENCY_EXTN")
                patient.emergencyContactName = outputData.getString("EMERGENCY_CONTACT_NAME")
                patient.emergencyContactNumber = outputData.getString("EMERGENCY_CONTACT_NUMBER")
                patient.firstName = outputData.getString("FIRST_NAME")
                patient.lastName = outputData.getString("LAST_NAME")
                patient.fullName = "${patient.firstName} ${patient.lastName}"
                patient.gender = outputData.getString("GENDER")
                patient.maidenName = outputData.getString("MAIDEN_NAME")
                patient.maritalStatus = outputData.getString("MARITAL_STATUS")
                patient.middleName = outputData.getString("MIDDLE_NAME")
                patient.nickName = outputData.getString("NICK_NAME")
                patient.practicePatientId = outputData.getString("PATIENT_ID")

                val postalAddress = PostalAddress()//COUNTRY LINE_1 LINE_2 STATE
                postalAddress.city = outputData.getString("CITY")
                postalAddress.country = outputData.getString("COUNTRY")
                postalAddress.line1 = outputData.getString("LINE_1")
                postalAddress.line2 = outputData.getString("LINE_2")
                postalAddress.state = outputData.getString("STATE")
                postalAddress.postalCode = outputData.getString("POSTALCODE")

                val contactObj = outputData.getAsJsonArray("contacts")?.get(0)?.asJsonObject

                if (contactObj != null) {

                    patient.caregiverFirstName = contactObj.getString("FIRST_NAME")
                    patient.caregiverLastName = contactObj.getString("LAST_NAME")
                    patient.caregiverMobile = contactObj.getString("MOBILE")

                    patient.caregiverFullName =
                        ("${patient.caregiverFirstName ?: ""} ${patient.caregiverLastName ?: ""}").trim()

                    if (patient.caregiverMobile == "null") {
                        patient.caregiverMobile = null
                    }
                }

                patient.contactDetails = contactDetails
                patient.postalAddress = postalAddress
                patient.toString = respObj.toString()

            }

            return patient
        }

        fun formPatientCommonData(respObj: JsonObject): PatientCommonData {
            val patientCommonData = PatientCommonData()

            val outputData = respObj.getAsJsonObject("output_string")
            if (outputData != null) {

                val bloodGrpArr = outputData.getAsJsonArray("BLOOD_GROUP")
                val bloodGrpList = arrayListOf<String>()
                bloodGrpArr?.forEach {
                    bloodGrpList.add(it.asString)
                }
                patientCommonData.bloodGrpList = bloodGrpList

                val countryObj = outputData.getAsJsonObject("COUNTRY")?.entrySet()?.iterator()
                val countryList = arrayListOf<DataWithId>()
                countryObj?.forEach {
                    countryList.add(DataWithId(key = it.value?.asString ?: "", name = it.key))
                }
                patientCommonData.country = countryList

                val empStatusArr = outputData.getAsJsonArray("EMPLOYMENT_STATUS")
                val empStatusList = arrayListOf<String>()
                empStatusArr?.forEach {
                    empStatusList.add(it.asString)
                }
                patientCommonData.employmentStatus = empStatusList

                val ethnicityArr = outputData.getAsJsonArray("ETHNICITY")
                val ethnicityList = arrayListOf<String>()
                ethnicityArr?.forEach {
                    ethnicityList.add(it.asString)
                }
                patientCommonData.ethnicity = ethnicityList

                val genderIdentityArr = outputData.getAsJsonArray("GENDER_IDENTITY")
                val genderIdentityList = arrayListOf<String>()
                genderIdentityArr?.forEach {
                    genderIdentityList.add(it.asString)
                }
                patientCommonData.genderIdentity = genderIdentityList

                val maritalStatusArr = outputData.getAsJsonArray("MARITAL_STATUS")
                val maritalStatusList = arrayListOf<String>()
                maritalStatusArr?.forEach {
                    maritalStatusList.add(it.asString)
                }
                patientCommonData.maritalStatus = maritalStatusList

                val raceArr = outputData.getAsJsonArray("RACE")
                val raceList = arrayListOf<String>()
                raceArr?.forEach {
                    raceList.add(it.asString)
                }
                patientCommonData.race = raceList

                val smokingStatusArr = outputData.getAsJsonArray("SMOKING_STATUS")
                val smokingStatusList = arrayListOf<String>()
                smokingStatusArr?.forEach {
                    smokingStatusList.add(it.asString)
                }
                patientCommonData.smokingStatus = smokingStatusList


            }
            return patientCommonData
        }

        fun formLanguageList(respObj: JsonObject): ArrayList<String> {

            val languageList = arrayListOf<String>()

            val outputData = respObj.getAsJsonObject("output_string")
            outputData?.entrySet()?.iterator()?.forEach {
                val language = it.key
                if (!language.isNullOrBlank()) {
                    languageList.add(language)
                }
            }

            return languageList
        }

        fun formPatientSection(respObj: JsonObject): SectionConfigList {

            val sectionConfigList = SectionConfigList()


            val outputDataArr = respObj.getAsJsonArray("output_string")

            outputDataArr?.forEach { outputDataEle ->
                val outputData = outputDataEle?.asJsonObject

                if (outputData != null) {

                    val sectionPracticeMap = SectionPracticeMap()

                    val recordArr = outputData.getAsJsonArray("RECORD")

                    val appointmentEnable = outputData.getBoolean("APPOINTMENT_ENABLED") ?: false


                    recordArr?.forEach { recordEle ->

                        val recordObj = recordEle.asJsonObject
                        if (recordObj != null) {

                            val sectionConfig = SectionConfig()
                            sectionConfig.section = recordObj.getString("SECTION")
                            sectionConfig.sectionName = recordObj.getString("SECTION_NAME")
                            sectionConfig.sectionOrder = recordObj.getInt("SECTION_ORDER") ?: 0
                            sectionConfig.isDefaultEnabled =
                                recordObj.getBoolean("IS_DEFAULT_ENABLED")
                                    ?: false
                            sectionConfig.isEnabled = recordObj.getBoolean("IS_ENABLED") ?: false

                            if (sectionConfig.section == ModelUtil.appointmentModelId) {
                                sectionConfig.allowPHREntry = appointmentEnable
                            } else {
                                sectionConfig.allowPHREntry =
                                    recordObj.getBoolean("ALLOW_PHR_ENTRY")
                                        ?: false
                            }

                            sectionPracticeMap.sectionConfigList.add(sectionConfig)
                            /*  if (sectionConfig.section == ModelUtil.allergiesModelId){
                                 sectionConfig.allowPHREntry = false
                             }

                            if (sectionConfig.section == ModelUtil.supplementModelId){
                                 sectionConfig.isEnabled = false
                             }*/
                        }
                    }

                    val wellnessArr = outputData.getAsJsonArray("WELLNESS")

                    wellnessArr?.forEach { recordEle ->

                        val recordObj = recordEle.asJsonObject
                        if (recordObj != null) {

                            val sectionConfig = SectionConfig()
                            sectionConfig.section = recordObj.getString("SECTION")
                            sectionConfig.sectionName = recordObj.getString("SECTION_NAME")
                            sectionConfig.sectionOrder = recordObj.getInt("SECTION_ORDER") ?: 0
                            sectionConfig.isDefaultEnabled =
                                recordObj.getBoolean("IS_DEFAULT_ENABLED")
                                    ?: false
                            sectionConfig.isEnabled = recordObj.getBoolean("IS_ENABLED") ?: false
                            sectionConfig.allowPHREntry = recordObj.getBoolean("ALLOW_PHR_ENTRY")
                                ?: false

                            sectionPracticeMap.sectionConfigList.add(sectionConfig)

                            /*
                        if (sectionConfig.section == ModelUtil.medicationModelId){
                            sectionConfig.isEnabled = false
                        }*/
                        }
                    }

                    val announcementEnable = outputData.getBoolean("ANNOUNCEMENT_ENABLED") ?: false

                    sectionPracticeMap.practiceId = outputData.getString("practice_id")

                    val sectionConfig = SectionConfig()
                    sectionConfig.section = ModelUtil.announcementModelId
                    sectionConfig.sectionName = "Announcements"
                    sectionConfig.sectionOrder = 1
                    sectionConfig.isDefaultEnabled = announcementEnable
                    sectionConfig.isEnabled = announcementEnable
                    sectionConfig.allowPHREntry = false

                    sectionPracticeMap.sectionConfigList.add(sectionConfig)

                    sectionConfigList.sectionConfigMapList.add(sectionPracticeMap)


                }
            }

            sectionConfigList.toString = respObj.toString()

            return sectionConfigList
        }

        //endregion

        //region Medication


        fun formMedicationList(
            respObj: JsonObject,
            patientId: String,
            isFromReminder: Boolean = false,
            practiceId: String,
            viewTypeStatus: String,
            respCount: Int
        ): ArrayList<PatMedPOJO> {
            val patMedList = arrayListOf<PatMedPOJO>()
            val outputString = respObj.getAsJsonObject("output_string")
            val prescriptionArr = outputString.getAsJsonArray("PRESCRIPTION_LIST")
            val allowRefill = outputString.getBoolean("ALLOW_RX_REFILL") ?: false
            val uiType = outputString.getString("UI_TYPE") ?: ""

            prescriptionArr.forEach { prescriptionEle ->
                val prescriptionObj = prescriptionEle.asJsonObject
                if (prescriptionObj != null) {
                    val patMedPOJO = formMedicationObj(prescriptionObj, patientId)
                    patMedPOJO.practiceId = practiceId
                    patMedPOJO.status = viewTypeStatus
                    patMedPOJO.allowRefill = allowRefill
                    patMedPOJO.uiType = uiType
                    if (patMedPOJO.patMed?.isAddedByPatient == false || (patMedPOJO.patMed?.isAddedByPatient == true && respCount == 1)) {
                        if (!isFromReminder) {
                            patMedList.add(patMedPOJO)
                        } else if (patMedPOJO.reminders.isNotEmpty()) {
                            patMedList.add(patMedPOJO)
                        }
                    }
                }
            }
            return patMedList
        }


        fun formMedicationListForDB(
            patMedList: ArrayList<PatMedPOJO>,
            appContext: Context
        ): ArrayList<Medications> {
            val medicationsList = arrayListOf<Medications>()
            patMedList.forEach { patMedPOJO ->
                val medication = Medications()
                if (patMedPOJO.patMed?.isAddedByPatient == false) {
                    medication.medicationId = patMedPOJO.patMed?.patientMedicationId
                    medication.directions = patMedPOJO.patMedExt.firstOrNull()?.directions
                    medication.genericDrugName = UIUtil.getRxDisplayName(patMedPOJO, appContext)
                } else {
                    medication.medicationId =
                        patMedPOJO.patMedPHRExt.firstOrNull()?.prescriptionHistoryId
                    medication.directions = UIUtil.formDirectionsForMedication(patMedPOJO)
                    var medicationName = patMedPOJO.drugDetail.firstOrNull()?.genericDrugName
                    val noOfIntake = patMedPOJO.patMedPHRExt.firstOrNull()?.noOfIntakes
                    var intakeUnit = patMedPOJO.patMedPHRExt.firstOrNull()?.intakeUnit ?: ""
                    if (intakeUnit == "-") {
                        intakeUnit = ""
                    }
                    if ((noOfIntake != null) && ((noOfIntake.trim().toFloatOrNull() ?: -1f) > 0f)) {
                        medicationName += " $noOfIntake $intakeUnit"
                    }
                    medication.genericDrugName = medicationName
                }
                medication.startDate = patMedPOJO.patMed?.startDate
                medication.endDate = patMedPOJO.patMed?.stopDate
                medication.practicePatientId = patMedPOJO.patMed?.practicePatientId
                medication.practiceId = patMedPOJO.practiceId
                medicationsList.add(medication)
            }
            return medicationsList
        }

        fun formSupplementListForDB(patSupplementList: ArrayList<PatientSupplements>): ArrayList<Supplements> {
            val supplementsList = arrayListOf<Supplements>()
            patSupplementList.forEach { supplementObject ->
                val supplement = Supplements()
                var supplementsName = supplementObject.supplement?.supplementName
                val noOfIntake =
                    supplementObject.patientSupplementMap?.patientSupplementHistory?.numberOfIntake
                var intakeUnit =
                    supplementObject.patientSupplementMap?.patientSupplementHistory?.intakeUnit
                        ?: ""
                if (intakeUnit == "-") {
                    intakeUnit = ""
                }
                if (noOfIntake != null && (noOfIntake.trim().toFloatOrNull() ?: -1f) > 0f) {
                    supplementsName += " $noOfIntake $intakeUnit"
                }
                supplement.genericDrugName = supplementsName ?: ""
                if (!supplementObject.isAddedByPatient) {
                    supplement.directions =
                        supplementObject.patientSupplementMap?.patientSupplementHistory?.instructions
                            ?: ""
                    supplement.supplementId = supplementObject.patientSupplementId
                } else {
                    supplement.directions = UIUtil.formDirectionsForSupplement(supplementObject)
                    supplement.supplementId =
                        supplementObject.patientSupplementMap?.patientSupplementHistory?.supplementHistoryId
                }
                supplement.fromDate =
                    supplementObject.patientSupplementMap?.patientSupplementHistory?.fromDate
                supplement.toDate =
                    supplementObject.patientSupplementMap?.patientSupplementHistory?.toDate
                supplement.practiceId = supplementObject.practiceId
                supplement.practicePatientId = supplementObject.practicePatientId
                supplementsList.add(supplement)
            }
            return supplementsList
        }

        fun formMedicationCommonData(
            medicationCommonDataResp: JsonObject,
            appContext: Context
        ): MedicationCommonData {

            val medicationCommonData = MedicationCommonData()


            val inputStream = appContext.resources.openRawResource(R.raw.prescription_common)
            val inputString = inputStream.bufferedReader().use { it.readText() }
            //Parsing input string to JsonArray
            val localData = inputString.parseStringAsJsonObject()

            val outputData = medicationCommonDataResp.getAsJsonObject("output_string")

            if (outputData != null && localData != null) {
                val providerArr = outputData.getAsJsonArray("PROVIDER_LIST")
                val providerList = arrayListOf<String>()

                providerArr?.forEach {

                    /*val memberObj = it.asJsonObject

                    val member = Member()

                    member.practiceId = memberObj.getString("PRACTICE_ID")
                    member.fullName = memberObj.getString("FULL_NAME")
                    member.specialization = memberObj.getString("MEMBER_ID") //SPECIALIZATION comes in member Id
                    member.prefix = memberObj.getString("PREFIX")*/

                    providerList.add(it.asString)
                }


                val intakeList = arrayListOf<DataWithId>()
                val intakeListArr = localData.getAsJsonArray("frequency")

                intakeListArr?.forEach { intakeListEle ->

                    val intakeListObj = intakeListEle?.asJsonObject
                    if (intakeListObj != null) {

                        val dataWithId = DataWithId()
                        dataWithId.key = intakeListObj.getString("NAME") ?: ""
                        dataWithId.name = dataWithId.key.replaceFirstChar { it.uppercase() }
                        intakeList.add(dataWithId)
                    }
                }

                val intakeWhnList = arrayListOf<DataWithId>()
                val intakeWhnListObj = outputData.getAsJsonObject("INTAKE_WHEN_LIST")

                if (intakeWhnListObj != null) {

                    val intakeWhnKeyValue = intakeWhnListObj.entrySet().iterator()
                    intakeWhnKeyValue.forEach {

                        val dataWithId = DataWithId()
                        dataWithId.key = it?.key ?: ""
                        dataWithId.name = it?.value?.asString ?: ""
                        intakeWhnList.add(dataWithId)

                    }
                }

                val medNameList = arrayListOf<DataWithId>()
                val medNameListObj = outputData.getAsJsonObject("MED_NAMEVSID")

                if (medNameListObj != null) {

                    val medNameKeyValue = medNameListObj.entrySet().iterator()
                    medNameKeyValue.forEach {

                        val dataWithId = DataWithId()
                        dataWithId.key = it?.key ?: ""
                        dataWithId.name = it?.value?.asString ?: ""
                        medNameList.add(dataWithId)

                    }
                }


                val routeArr = localData.getAsJsonArray("route")
                val routeList = arrayListOf<String>()

                routeArr?.forEach {
                    routeList.add(it.asString)
                }

                val rxUnitArr = outputData.getAsJsonArray("RXUNIT_LIST")
                val rxUnitList = arrayListOf<String>()
                val rxStrengthUnitList = arrayListOf<String>()
                val rxStrengthUnitArr = localData.getAsJsonArray("unit")

                rxUnitArr?.forEach {
                    rxUnitList.add(it.asString)
                }

                rxStrengthUnitArr?.forEach {
                    rxStrengthUnitList.add(it.asString)
                }


                val searchStrArr = outputData.getAsJsonArray("SEARCH_STRING")
                val searchStrList = arrayListOf<String>()

                searchStrArr?.forEach { searchStrEle ->
                    val searchStrObj = searchStrEle?.asJsonObject

                    if (searchStrObj != null) {
                        searchStrList.add(searchStrObj.getString("name") ?: "")
                    }
                }


                val unitIdVsNameList = arrayListOf<DataWithId>()
                val unitIdVsNameListObj = outputData.getAsJsonObject("UNITIDVSNAME")

                if (unitIdVsNameListObj != null) {

                    val unitIdVsName = unitIdVsNameListObj.entrySet().iterator()
                    unitIdVsName.forEach {

                        val dataWithId = DataWithId()
                        dataWithId.key = it?.key ?: ""
                        dataWithId.name = it?.value?.asString ?: ""
                        unitIdVsNameList.add(dataWithId)

                    }
                }




                medicationCommonData.intakeList = intakeList
                medicationCommonData.intakeWhenList = intakeWhnList
                medicationCommonData.medNameList = medNameList
                medicationCommonData.providerList = providerList
                medicationCommonData.routeList = routeList
                medicationCommonData.rxUnitList = rxUnitList
                medicationCommonData.rxStrengthUnitList = rxStrengthUnitList
                medicationCommonData.searchString = searchStrList
                medicationCommonData.rxUnitAndName = unitIdVsNameList

            }

            return medicationCommonData
        }


        fun formMedicationObj(prescriptionObj: JsonObject, patientId: String): PatMedPOJO {
            val patMedPOJO = PatMedPOJO()


            val patMedication = PatientMedications()
            patMedication.addedAs = prescriptionObj.getString("ADDED_AS")
            patMedication.isActive = prescriptionObj.getBoolean("IS_ACTIVE")
            patMedication.isAddedByPatient = prescriptionObj.getBoolean("ADDED_BY_PATIENT") ?: false
            patMedication.practicePatientId = patientId

            val startDateStr = prescriptionObj.getString("START_DATE")

            val patientMedicationsPHRExt = PatientPrescriptionHistory()

            if (startDateStr != null) {
                patMedication.startDate = DateTime.parse(
                    startDateStr,
                    ModelUtil.charmDisplayDateFormat()
                )

                patientMedicationsPHRExt.fromDate = patMedication.startDate
            }

            val stopDateStr = prescriptionObj.getString("STOP_DATE")
            if (stopDateStr != null) {
                patMedication.stopDate = DateTime.parse(
                    stopDateStr,
                    ModelUtil.charmDisplayDateFormat()
                )

                patientMedicationsPHRExt.toDate = patMedication.stopDate

            }



            if (!patMedication.isAddedByPatient) {

                patMedication.patientMedicationId =
                    prescriptionObj.getString("PATIENT_MEDICATION_ID")

                patMedication.addedAs = prescriptionObj.getString("ADDED_AS")

                val patMedicationExt = PatientMedicationsExt()
                patMedicationExt.directions = prescriptionObj.getString("DIRECTIONS")
                patMedicationExt.directionEdited =
                    prescriptionObj.getBoolean("DIRECTION_EDITED") ?: false
                patMedicationExt.dispense = prescriptionObj.getString("DISPENSE")
                patMedicationExt.dispenseUnit = prescriptionObj.getString("DISPENSE_UNIT")
                patMedicationExt.refill = prescriptionObj.getString("REFILL")


                val drugDetails = DrugDetails()
                drugDetails.genericDrugName = prescriptionObj.getString("GENERIC_DRUG_NAME")
                drugDetails.tradeName = prescriptionObj.getString("TRADE_NAME")
                drugDetails.drugDetailsId = prescriptionObj.getString("DRUGDETAILS_ID")
                drugDetails.genericProductName = prescriptionObj.getString("GENERIC_PRODUCT_NAME")
                drugDetails.genericProductId = prescriptionObj.getString("GENERIC_PRODUCT_ID")
                drugDetails.eRxDisplayName = prescriptionObj.getString("DRUG_ERX_DISPLAY_NAME")
                drugDetails.isActive = prescriptionObj.getBoolean("IS_ACTIVE") ?: true
                drugDetails.isCustom = prescriptionObj.getBoolean("IS_CUSTOM") ?: false


                val member = Member()
                member.fullName = prescriptionObj.getString("FULL_NAME")
                member.prefix = prescriptionObj.getString("PREFIX")
                member.memberId = prescriptionObj.getString("MEMBER_ID")




                patMedPOJO.drugDetail = arrayListOf(drugDetails)
                patMedPOJO.member = member
                patMedPOJO.patMedExt = arrayListOf(patMedicationExt)


            } else {

                patientMedicationsPHRExt.intake = prescriptionObj.getString("INTAKE")


                patientMedicationsPHRExt.intakeRoute = prescriptionObj.getString("INTAKE_ROUTE")
                patientMedicationsPHRExt.intakeUnit = prescriptionObj.getString("INTAKE_UNIT")
                patientMedicationsPHRExt.intakeWhen = prescriptionObj.getString("INTAKE_WHEN")
                patientMedicationsPHRExt.dosageWithUnit =
                    prescriptionObj.getString("DOSAGE_WITH_UNIT")
                patientMedicationsPHRExt.dosage = prescriptionObj.getString("DOSAGE")
                patientMedicationsPHRExt.dosageUnit = prescriptionObj.getString("DOSAGE_UNIT")
                patientMedicationsPHRExt.noOfIntakes = prescriptionObj.getString("NUMBER_OF_INTAKE")
                patientMedicationsPHRExt.dosageUnitId = prescriptionObj.getString("DOSAGE_UNIT_ID")
                patientMedicationsPHRExt.medicalConditionId =
                    prescriptionObj.getString("MEDICAL_CONDITION_ID")
                patientMedicationsPHRExt.medicalConditionName =
                    prescriptionObj.getString("MEDICAL_CONDITION_NAME")
                patientMedicationsPHRExt.refills = prescriptionObj.getString("NUMBER_OF_REFILLS")
                patientMedicationsPHRExt.prescriptionHistoryId =
                    prescriptionObj.getString("PRESCRIPTION_HISTORY_ID")
                patientMedicationsPHRExt.comments = prescriptionObj.getString("COMMENTS")


                val drugDetails = DrugDetails()
                drugDetails.drugDetailsId = prescriptionObj.getString("PRESCRIPTION_ID")
                drugDetails.genericDrugName = prescriptionObj.getString("PRESCRIPTION_NAME")

                val member = Member()
                member.fullName = prescriptionObj.getString("PRESCRIBED_BY")

                patMedPOJO.patMedPHRExt = arrayListOf(patientMedicationsPHRExt)
                patMedPOJO.drugDetail = arrayListOf(drugDetails)
                patMedPOJO.member = member

            }

            patMedPOJO.patMed = patMedication

            patMedication.dataInString = prescriptionObj.toString()
            val reminder = prescriptionObj.getAsJsonArray("REMINDERS")
            reminder?.forEach {
                val reminderList = arrayListOf<ReminderPOJO>()
                val reminderJson = it as JsonObject
                reminderList.add(formReminderObject(reminderJson, patientId))
                patMedPOJO.reminders = reminderList
            }
            patMedication.reminderDataInString = reminder?.toString()
            return patMedPOJO
        }


        //endregion

        //region Supplement


        fun formSupplementList(
            respObj: JsonObject,
            patientId: String,
            isFromReminder: Boolean,
            practiceId: String,
            respCount: Int
        ): ArrayList<PatientSupplements> {
            val patientSupplementsList = arrayListOf<PatientSupplements>()

            val outputString = respObj.getAsJsonObject("output_string")

            val supplementArr = outputString.getAsJsonArray("SUPPLEMENT_LIST")

            val allowRefill = outputString.getBoolean("ALLOW_SX_REFILL") ?: false

            supplementArr.forEach { supplementEle ->
                val supplementObj = supplementEle.asJsonObject

                val patientSupplements = formSupplementObj(supplementObj, patientId)
                patientSupplements.allowRefill = allowRefill
                patientSupplements.practiceId = practiceId

                if (!patientSupplements.isAddedByPatient || respCount == 1) {
                    if (!isFromReminder) {
                        patientSupplementsList.add(patientSupplements)
                    } else if (patientSupplements.reminders.isNotEmpty()) {
                        patientSupplementsList.add(patientSupplements)
                    }
                }
            }
            return patientSupplementsList
        }

        fun formSupplementObj(supplementObj: JsonObject, patientId: String): PatientSupplements {
            val patientSupplements = PatientSupplements()
            val patientSupplementHistory = PatientSupplementHistory()
            val dosageUnit = DosageUnit()
            val patientSupplementMap = PatientSupplementMap()
            val supplement = SupplementList()
            val manufacturer = Manufacturer()
            val member = Member()


            patientSupplements.isAddedByPatient =
                supplementObj.getBoolean("ADDED_BY_PATIENT") ?: false
            patientSupplements.isActive = supplementObj.getBoolean("IS_ACTIVE")
            patientSupplements.practicePatientId = patientId

            patientSupplements.patientSupplementId =
                supplementObj.getString("PATIENT_SUPPLEMENT_ID")



            patientSupplementHistory.comments = supplementObj.getString("COMMENTS")
            patientSupplementHistory.dosage = supplementObj.getString("DOSAGE")

            dosageUnit.dosageUnitId = supplementObj.getString("DOSAGE_UNIT_ID")

            dosageUnit.dosageUnit = supplementObj.getString("DOSAGE_UNIT")



            patientSupplementHistory.frequency = supplementObj.getString("FREQUENCY")



            if (patientSupplements.isAddedByPatient) {
                patientSupplementHistory.intake = supplementObj.getString("INTAKE")
                patientSupplementHistory.intakeUnit = supplementObj.getString("INTAKE_UNIT")
                patientSupplementHistory.intakeWhen = supplementObj.getString("INTAKE_WHEN")
            } else {
                patientSupplementHistory.instructions = supplementObj.getString("INSTRUCTIONS")
            }

            manufacturer.manufactureName = supplementObj.getString("MANUFACTURER_NAME")
            manufacturer.manufacturerId = supplementObj.getString("MANUFACTURER_ID")

            patientSupplementHistory.numberOfIntake = supplementObj.getString("NUMBER_OF_INTAKE")
            patientSupplementHistory.supplementHistoryId =
                supplementObj.getString("SUPPLEMENT_HISTORY_ID")

            patientSupplementMap.supplementMapId = supplementObj.getString("SUPPLEMENT_MAP_ID")

            supplement.supplementId = supplementObj.getString("SUPPLEMENT_ID")
            supplement.supplementName = supplementObj.getString("SUPPLEMENT_NAME")
            supplement.supplementType = supplementObj.getString("SUPPLEMENT_TYPE")


            val startDateStr = supplementObj.getString("FROM_DATE")
            if (!startDateStr.isNullOrBlank()) {
                patientSupplements.startDate = DateTime.parse(
                    startDateStr,
                    ModelUtil.charmDisplayDateFormat()
                )
                patientSupplementHistory.fromDate = patientSupplements.startDate
            }

            val stopDateStr = supplementObj.getString("TO_DATE")
            if (!stopDateStr.isNullOrBlank()) {
                patientSupplements.stopDate = DateTime.parse(
                    stopDateStr,
                    ModelUtil.charmDisplayDateFormat()
                )
                patientSupplementHistory.toDate = patientSupplements.stopDate
            }

            val memberNameEle = supplementObj.getString("PRESCRIBED_BY")
            member.fullName = memberNameEle


            val memberIdEle = supplementObj.getString("PHYSICIAN_ID")
            member.memberId = memberIdEle


            patientSupplementHistory.dosageUnit = dosageUnit
            patientSupplementMap.patientSupplementHistory = patientSupplementHistory
            patientSupplements.manufacturer = manufacturer
            patientSupplements.supplement = supplement
            patientSupplements.patientSupplementMap = patientSupplementMap
            patientSupplements.member = member
            patientSupplements.toString = supplementObj.toString()

            val reminder = supplementObj.getAsJsonArray("REMINDERS")
            val reminderList = arrayListOf<ReminderPOJO>()
            reminder?.forEach {
                val reminderJson = it as JsonObject
                reminderList.add(formReminderObject(reminderJson, patientId))
            }
            patientSupplements.reminders = reminderList
            patientSupplements.reminderDataInString = reminder?.toString()
            return patientSupplements
        }

        fun formSupplementCommonData(
            supplementCommonDataResp: JsonObject,
            appContext: Context
        ): SupplementCommonData {

            val supplementCommonData = SupplementCommonData()

            val outputData = supplementCommonDataResp.getAsJsonObject("output_string")


            val inputStream = appContext.resources.openRawResource(R.raw.prescription_common)
            val inputString = inputStream.bufferedReader().use { it.readText() }
            //Parsing input string to JsonArray
            val localData = inputString.parseStringAsJsonObject()
            if (outputData != null && localData != null) {


                val intakeList = arrayListOf<DataWithId>()
                val intakeListArr = localData.getAsJsonArray("frequency")

                intakeListArr?.forEach { intakeListEle ->

                    val intakeListObj = intakeListEle?.asJsonObject
                    if (intakeListObj != null) {

                        val dataWithId = DataWithId()
                        dataWithId.key = intakeListObj.getString("NAME") ?: ""
                        dataWithId.name = dataWithId.key.replaceFirstChar { it.uppercase() }
                        intakeList.add(dataWithId)
                    }
                }

                val intakeWhnList = arrayListOf<DataWithId>()
                val intakeWhnListObj = outputData.getAsJsonObject("INTAKE_WHEN_LIST")

                if (intakeWhnListObj != null) {

                    val intakeWhnKeyValue = intakeWhnListObj.entrySet().iterator()
                    intakeWhnKeyValue.forEach {

                        val dataWithId = DataWithId()
                        dataWithId.key = it?.key ?: ""
                        dataWithId.name = it?.value?.asString ?: ""
                        intakeWhnList.add(dataWithId)

                    }
                }

                val manufacturerNameList = arrayListOf<String>()
                val medNameListArr = outputData.getAsJsonArray("MANUIDVSNAME")

                medNameListArr?.forEach { medNameListEle ->

                    val manufacturerObj = medNameListEle?.asJsonObject
                    if (manufacturerObj != null) {
                        manufacturerNameList.add(manufacturerObj.getString("name") ?: "")
                    }
                }


                /* val routeArr = outputData.getAsJsonArray("ROUTE_LIST")
                 val routeList = arrayListOf<String>()

                 routeArr?.forEach {
                     routeList.add(it.asString)
                 }
 */
                val sxUnitArr = outputData.getAsJsonArray("SXUNIT_LIST")
                val sxUnitList = arrayListOf<String>()
                val sxStrengthUnitList = arrayListOf<String>()
                val sxStrengthUnitArr = localData.getAsJsonArray("unit")

                sxUnitArr?.forEach {
                    sxUnitList.add(it.asString)
                }

                sxStrengthUnitArr?.forEach {
                    sxStrengthUnitList.add(it.asString)
                }


                val searchStrArr = outputData.getAsJsonArray("SEARCH_STRING")
                val searchStrList = arrayListOf<String>()

                searchStrArr?.forEach { searchStrEle ->
                    val searchStrObj = searchStrEle?.asJsonObject

                    if (searchStrObj != null) {
                        searchStrList.add(searchStrObj.getString("name") ?: "")
                    }
                }


                val unitIdVsNameList = arrayListOf<DataWithId>()
                val unitIdVsNameListObj = outputData.getAsJsonObject("UNITIDVSNAME")

                if (unitIdVsNameListObj != null) {

                    val unitIdVsName = unitIdVsNameListObj.entrySet().iterator()
                    unitIdVsName.forEach {

                        val dataWithId = DataWithId()
                        dataWithId.key = it?.key ?: ""
                        dataWithId.name = it?.value?.asString ?: ""
                        unitIdVsNameList.add(dataWithId)

                    }
                }




                supplementCommonData.intakeList = intakeList
                supplementCommonData.intakeWhenList = intakeWhnList
                supplementCommonData.manufacturerList = manufacturerNameList
                supplementCommonData.sxUnitList = sxUnitList
                supplementCommonData.sxStrengthUnitList = sxStrengthUnitList
                supplementCommonData.searchString = searchStrList
                supplementCommonData.sxUnitAndName = unitIdVsNameList

            }

            return supplementCommonData
        }


        //endregion

        //region Members


        fun formMembersList(respObj: JsonObject): ArrayList<Member> {
            val memberList = arrayListOf<Member>()

            val outputData = respObj.getAsJsonArray("output_data")

            outputData?.forEach { memberEle ->
                val memberObj = memberEle?.asJsonObject
                if (memberObj != null) {
                    val member = Member()
                    member.prefix = memberObj.getString("PREFIX")
                    member.fullName = memberObj.getString("FULL_NAME")
                    member.memberId = memberObj.getString("MEMBER_ID")
                    member.gender = memberObj.getString("GENDER")
                    member.specialization = memberObj.getString("SPECIALIZATION")
                    memberList.add(member)
                }
            }

            return memberList
        }

        //endregion

        //region Appointment

        fun formUpcomingAppointment(
            authManager: AuthManager,
            respObj: JsonObject,
            patient: Patient? = null
        ): ArrayList<Appointment> {
            val appointmentList = arrayListOf<Appointment>()

            val enabledPracticeList =
                ModelUtil.getEnabledPractices(authManager, ModelUtil.appointmentModelId, patient)

            val appointmentArr = respObj.getAsJsonArray("output_string")

            appointmentArr.forEach { appointmentEle ->


                val appointmentObj = appointmentEle.asJsonObject

                val practiceId = appointmentObj.getString("PRACTICE_ID")


                if (BuildUtil.isCustomApp || (!BuildUtil.isCustomApp && enabledPracticeList.contains(
                        practiceId
                    ))
                ) {


                    val appointment = Appointment()

                    appointment.appointmentDate = UIUtil.formatDate(
                        appointmentObj.getString("APPOINTMENT_DATE"),
                        ModelUtil.sqlTimeStampFormattor()
                    )
                    appointment.statusId = appointmentObj.getString("STATUS_ID")
                    appointment.appointmentType = appointmentObj.getString("APPOINTMENT_TYPE")
                    appointment.reason = appointmentObj.getString("REASON")
                    appointment.appointmentId = appointmentObj.getString("APPOINTMENT_ID")
                    appointment.facilityId = appointmentObj.getString("FACILITY_ID")
                    appointment.facilityName = appointmentObj.getString("FACILITY_NAME")
                    appointment.timezone = appointmentObj.getString("TIMEZONE")
                    appointment.teleMode = appointmentObj.getString("TELE_MODE")
                    appointment.physicianRemark = appointmentObj.getString("PHYSICIAN_REMARK")
                    appointment.appointmentCancelOption =
                        appointmentObj.getString("APPOINTMENT_CANCEL_OPTION")

                    appointment.practiceId = practiceId


                    val questionnaireArrTemp = appointmentObj.getAsJsonArray("FORMS")
                    questionnaireArrTemp?.let { questionnaireArr ->
                        if (questionnaireArr.size() > 0) {
                            appointment.isQuestionnairePresent = true
                            appointment.questionnaireCount = questionnaireArr.size()
                        }
                    }


                    val appointmentStatus = AppointmentStatus()
                    appointmentStatus.status = appointmentObj.getString("STATUS")
                    appointmentStatus.statusId = appointment.statusId

                    val member = Member()
                    member.fullName = appointmentObj.getString("FULL_NAME")
                    member.prefix = appointmentObj.getString("PREFIX")
                    member.memberId = appointmentObj.getString("PHYSICIAN_ID")


                    val visitTypeDetailObj = appointmentObj.getAsJsonObject("VISIT_TYPE_DETAIL")

                    if (visitTypeDetailObj != null) {

                        val visitTypeDetail = VisitTypeDetail()
                        visitTypeDetail.visitType = visitTypeDetailObj.getString("VISIT_TYPE")
                        visitTypeDetail.visitTypeAppointmentMode =
                            visitTypeDetailObj.getString("VISIT_TYPE_APPOINTMENT_MODE")
                        visitTypeDetail.visitTypeCharge =
                            visitTypeDetailObj.getString("VISITTYPE_CHARGE")
                        visitTypeDetail.visitTypeColor =
                            visitTypeDetailObj.getString("VISITTYPE_COLOR")
                        visitTypeDetail.visitTypeDuration =
                            visitTypeDetailObj.getString("VISITTYPE_DURATION")
                        visitTypeDetail.visitTypeId = visitTypeDetailObj.getString("VISITTYPE_ID")

                        appointment.visitTypeDetail = visitTypeDetail
                    }

                    appointment.member = member
                    appointment.appointmentStatus = appointmentStatus
                    appointment.appointmentCode = ModelUtil.appointmentUpcoming

                    appointmentList.add(appointment)
                }
            }

            return appointmentList
        }

        fun formWaitingAppointment(
            authManager: AuthManager,
            respObj: JsonObject
        ): ArrayList<Appointment> {
            val appointmentList = arrayListOf<Appointment>()

            val enabledPracticeList =
                ModelUtil.getEnabledPractices(authManager, ModelUtil.appointmentModelId)

            val appointmentArr = respObj.getAsJsonArray("output_string")

            appointmentArr.forEach { appointmentEle ->

                val appointmentObj = appointmentEle.asJsonObject


                val practiceId = appointmentObj.getString("PRACTICE_ID")
                if (BuildUtil.isCustomApp || (!BuildUtil.isCustomApp && enabledPracticeList.contains(
                        practiceId
                    ))
                ) {


                    val appointment = Appointment()

                    appointment.appointmentCode = ModelUtil.appointmentWaitingList
                    appointment.appointmentType = appointmentObj.getString("APPOINTMENT_TYPE")

                    val datePreference = appointmentObj.getString("DATE_PREFERENCE")

                    appointment.practiceId = practiceId

                    appointment.datePreference = datePreference

                    if (datePreference == ModelUtil.appointmentDatePreferenceSpecificDate) {
                        appointment.appointmentDate = UIUtil.formatDate(
                            appointmentObj.getString("DATE_CHOICE1"),
                            ModelUtil.charmDisplayDateFormat()
                        )
                    } else {
                        appointment.appointmentDate = null
                    }



                    appointment.reason = appointmentObj.getString("REASON")


                    appointment.physicianRemark = appointmentObj.getString("PHYSICIAN_REMARK")


                    val facilityObj = appointmentObj.getAsJsonObject("FACILITY_DETAILS")

                    appointment.facilityName = facilityObj?.getString("FACILITY_NAME")

                    val member = Member()
                    member.fullName = appointmentObj.getString("FULL_NAME")

                    member.memberId = appointmentObj.getString("PHYSICIAN_ID")

                    member.prefix = appointmentObj.getString("PREFIX")


                    appointment.member = member

                    appointment.reminderType = appointmentObj.getString("REMINDER_TYPE")

                    appointment.appointmentId = appointmentObj.getString("REQUEST_ID")
                    appointment.practiceId = appointmentObj.getString("PRACTICE_ID")

                    appointmentList.add(appointment)
                }


            }

            return appointmentList
        }

        fun formRequestedAndDeniedAppointment(
            authManager: AuthManager,
            respObj: JsonObject,
            code: Int
        ): ArrayList<Appointment> {
            val appointmentList = arrayListOf<Appointment>()

            val enabledPracticeList =
                ModelUtil.getEnabledPractices(authManager, ModelUtil.appointmentModelId)

            val appointmentArr = respObj.getAsJsonArray("output_string")

            appointmentArr.forEach { appointmentEle ->

                val appointmentObj = appointmentEle.asJsonObject

                val practiceId = appointmentObj.getString("PRACTICE_ID")
                if (BuildUtil.isCustomApp || (!BuildUtil.isCustomApp && enabledPracticeList.contains(
                        practiceId
                    ))
                ) {

                    val appointment = Appointment()

                    appointment.appointmentCode = code
                    appointment.appointmentType = appointmentObj.getString("APPOINTMENT_TYPE")

                    val datePreference = appointmentObj.getString("DATE_PREFERENCE")
                    val timePreference = appointmentObj.getString("TIME_PREFERENCE")

                    appointment.datePreference = datePreference
                    appointment.timePreference = timePreference
                    appointment.practiceId = appointmentObj.getString("PRACTICE_ID")

                    if (datePreference == ModelUtil.appointmentDatePreferenceSpecificDate) {
                        val dateChoiceEle = appointmentObj.getString("DATE_CHOICE1")
                        appointment.appointmentDate =
                            UIUtil.formatDate(dateChoiceEle, ModelUtil.charmApiDateFormat())
                    } else {
                        appointment.appointmentDate = null
                    }

                    if (timePreference == ModelUtil.appointmentTimePreferenceTimeRange) {

                        val startHour = appointmentObj.getString("START_HOUR")
                        val startMinutes = appointmentObj.getString("START_MINUTES")
                        val startSession = appointmentObj.getString("START_SESSION")

                        val endHour = appointmentObj.getString("END_HOUR")
                        val endMinutes = appointmentObj.getString("END_MINUTES")
                        val endSession = appointmentObj.getString("END_SESSION")

                        appointment.appointmentStartTime = UIUtil.formatDate(
                            "$startHour:$startMinutes $startSession",
                            ModelUtil.displayTimeForAppointmentTimeRangeFormat()
                        )
                        appointment.appointmentEndTime = UIUtil.formatDate(
                            "$endHour:$endMinutes $endSession",
                            ModelUtil.displayTimeForAppointmentTimeRangeFormat()
                        )

                    }
                    appointment.reason = appointmentObj.getString("REASON")

                    appointment.physicianRemark = appointmentObj.getString("PHYSICIAN_REMARK")


                    appointment.facilityId = appointmentObj.getString("FACILITY_ID")
                    appointment.facilityName = appointmentObj.getString("FACILITY_NAME")

                    val member = Member()

                    member.fullName = appointmentObj.getString("FULL_NAME")
                    member.memberId = appointmentObj.getString("PHYSICIAN_ID")


                    member.prefix = appointmentObj.getString("PREFIX")

                    appointment.member = member

                    appointment.reminderType = appointmentObj.getString("REMINDER_TYPE")
                    appointment.practiceId = appointmentObj.getString("PRACTICE_ID")
                    appointment.appointmentId = appointmentObj.getString("REQUEST_ID")
                    appointment.timezone = appointmentObj.getString("TIMEZONE")

                    appointmentList.add(appointment)
                }


            }
            return appointmentList
        }

        fun formFacilityListForAppointment(
            respObj: JsonObject,
            practiceId: String
        ): ArrayList<Facility> {

            val facilityArr = respObj.getAsJsonArray("output_string")

            val facilityList = arrayListOf<Facility>()

            facilityArr?.forEach { facilityEle ->

                val facilityObj = facilityEle?.asJsonObject

                if (facilityObj != null) {
                    val facility = Facility()
                    facility.facilityId = facilityObj.getString("facility_id")
                    facility.facilityName = facilityObj.getString("facility_name")
                    facility.practiceId = practiceId

                    facilityList.add(facility)

                }
            }

            return facilityList
        }


        fun formAppointmentConfig(respObj: JsonObject): AppointmentConfig {
            val appointmentConfig = AppointmentConfig()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {
                appointmentConfig.isEnabled = outputData.getBoolean("is_configured") ?: true

                val showAppOnPatTimeZone =
                    outputData.getBoolean("SHOW_APPT_ON_PAT_TIMEZONE") ?: false
                appointmentConfig.showAppOnPatTimeZone = showAppOnPatTimeZone

                val facTimeZone = outputData.getString("FACILITY_TIMEZONE") ?: ""

                if (facTimeZone.firstOrNull()?.isDigit() == true) {
                    appointmentConfig.currentFacilityTimezone = DateTime.parse(
                        facTimeZone,
                        ModelUtil.sqlTimeStampForLabResultDateFormattor()
                    )
                } else {
                    appointmentConfig.facilityTimezone = facTimeZone
                    appointmentConfig.currentFacilityTimezone = DateTime.parse(
                        outputData.getString("DATE_AT_FACILITY_TIMEZONE")
                            ?: "", ModelUtil.sqlTimeStampForLabResultDateFormattor()
                    )
                }

                appointmentConfig.isTelehealthEnabled = outputData.getBoolean("TELEHEALTH_ENABLED")
                    ?: false

                if (appointmentConfig.isEnabled) {


                    val visitTypesList = arrayListOf<VisitTypes>()

                    val visitTypeArrEle = outputData.getAsJsonArray("visitTypes")

                    if (visitTypeArrEle != null && visitTypeArrEle.isJsonArray) {
                        val visitTypesArr = visitTypeArrEle.asJsonArray

                        visitTypesArr.forEach { visitTypeEle ->
                            val visitTypesObj = visitTypeEle.asJsonObject

                            if (visitTypesObj != null) {
                                val visitTypes = VisitTypes()
                                visitTypes.appointmentMode =
                                    visitTypesObj.getString("APPOINTMENT_TYPE")?.split(" ")
                                        ?.joinToString(" ") { it.replaceFirstChar { it.uppercase() } }
                                visitTypes.duration = visitTypesObj.getInt("DURATION")
                                visitTypes.charge = visitTypesObj.getString("CHARGE")
                                visitTypes.visitType = visitTypesObj.getString("VISIT_TYPE")
                                visitTypes.visittypeId = visitTypesObj.getString("VISITTYPE_ID")
                                visitTypesList.add(visitTypes)

                            }
                        }
                    }

                    appointmentConfig.visitTypeList = visitTypesList
                    appointmentConfig.interval = outputData.getInt("interval") ?: 0
                    appointmentConfig.slotsOpeningPriorHours =
                        outputData.getInt("SLOTS_OPENING_PRIOR_HOURS")
                            ?: 0
                    appointmentConfig.onlineBookingDaysLimit =
                        outputData.getInt("ONLINE_BOOKING_DAYS_LIMIT")
                            ?: 0
                    appointmentConfig.patientNote = outputData.getString("PATIENT_NOTE")
                    appointmentConfig.showVisitDuration =
                        outputData.getBoolean("SHOW_VISIT_DURATION")
                            ?: false


                    /* val memberWorkTimingEle = outputData.getString("MEMBER_WORK_TIMINGS")
                     if (memberWorkTimingEle != null && memberWorkTimingEle.isJsonObject){
                         val memberWorkTimingObj = memberWorkTimingEle.asJsonObject

                     }*/

                }

                val cardProcessEnabled = outputData.getBoolean("IS_CARD_PROCESSING_ENABLED")
                if (cardProcessEnabled != null) {
                    appointmentConfig.isCardProcessingEnabled = cardProcessEnabled
                    if (appointmentConfig.isCardProcessingEnabled) {
                        appointmentConfig.cardProcessingType =
                            outputData.getString("CARD_PROCESSING_TYPE")
                        appointmentConfig.chargeAmount = outputData.getString("CHARGE_AMOUNT")
                        appointmentConfig.chargeType = outputData.getString("CHARGE_TYPE")

                        appointmentConfig.paymentCurrency =
                            outputData.getString("PRACTICE_CURRENCY_CODE")
                        appointmentConfig.paymentCurrencySymbol =
                            outputData.getString("PRACTICE_CURRENCY_SYMBOL")

                        if (appointmentConfig.paymentCurrencySymbol.isNullOrBlank() && BuildUtil.isCustomApp) {
                            appointmentConfig.paymentCurrencySymbol = "₹"
                        }

                        if (appointmentConfig.paymentCurrency.isNullOrBlank() && BuildUtil.isCustomApp) {
                            appointmentConfig.paymentCurrency = "INR"
                        }

                        if (appointmentConfig.chargeType != ModelUtil.appointmentPaymentChargeTypeVisitBased) {
                            if ((appointmentConfig.chargeAmount?.toFloatOrNull() ?: 0) == 0) {
                                appointmentConfig.isCardProcessingEnabled = false
                            }
                        } else {
                            if (appointmentConfig.visitTypeList.isEmpty()) {
                                appointmentConfig.isCardProcessingEnabled = false
                            }
                        }

                        appointmentConfig.merchantAccountId =
                            outputData.getString("MERCHANT_ACCOUNT_ID")

                        appointmentConfig.paymentGateway = outputData.getString("GATEWAY")

                        if (appointmentConfig.paymentGateway == ModelUtil.appointmentGatewayTypeBluefin) {
                            appointmentConfig.paymentCurrencySymbol = "$"
                            appointmentConfig.paymentCurrency = "USD"
                        }
                    }
                }

            }
            return appointmentConfig
        }


        fun getReceiptIdForBluefin(respObj: JsonObject?, isEHRReceipt: Boolean): Any {

            val outputData = respObj?.getAsJsonObject("output_string")

            if (outputData != null) {
                val receptDetails = outputData.getAsJsonArray("receipt_details")
                val receiptDetailEle = receptDetails.lastOrNull()
                if (receiptDetailEle != null) {

                    val receiptDetailObj = receiptDetailEle.asJsonObject
                    if (receiptDetailObj != null) {

                        val receiptKey = if (isEHRReceipt) {
                            "ehr_receipt_id"
                        } else {
                            "phr_receipt_id"
                        }
                        return receiptDetailObj.getString(receiptKey) ?: APIResponseError(
                            charmApiCode = ModelUtil.errorPaymentSuccessButReceiptFailedCode
                        )
                    }
                }
            }
            return APIResponseError(charmApiCode = ModelUtil.errorPaymentSuccessButReceiptFailedCode)
        }

        fun formSlotsList(respObj: JsonObject, slotPickedDate: DateTime): SlotMetaData {


            val slotMetaData = SlotMetaData()

            val outputData = respObj.getAsJsonObject("output_string")
            val hashMap: HashMap<DateTime, ArrayList<MemberAvailability>> =
                slotMetaData.memberAvailability
            if (outputData != null) {

                /*val set = outputData.entrySet()
                set.iterator().forEach {
                    it.value
                }*/
                val availableSlotsArr = outputData.getAsJsonArray("available_slots")

                if (availableSlotsArr != null) {

                    availableSlotsArr.forEach {
                        val entrySet = it?.asJsonObject?.entrySet()?.firstOrNull()
                        val slotsArr = entrySet?.value?.asJsonArray
                        val dateStr = entrySet?.key?.toString() ?: ""
                        val dateTime = DateTime.parse(dateStr, ModelUtil.charmApiDateFormat())
                        val slotsList = arrayListOf<MemberAvailability>()
                        val slotPickedDateStr = dateTime.toString(ModelUtil.charmApiDateFormat())
                        slotsArr?.forEach { slotEle ->
                            val slotObj = slotEle.asJsonObject
                            if (slotObj != null) {
                                val startTimeStr = slotObj.getString("start_time")
                                val endTimeStr = slotObj.getString("end_time")
                                val memberAvailability = MemberAvailability()
                                memberAvailability.startTime = DateTime.parse(
                                    "$slotPickedDateStr $startTimeStr",
                                    ModelUtil.charmDateFormatForSlot()
                                )
                                memberAvailability.endTime = DateTime.parse(
                                    "$slotPickedDateStr $endTimeStr",
                                    ModelUtil.charmDateFormatForSlot()
                                )
                                slotsList.add(memberAvailability)

                            }
                        }
                        hashMap[dateTime] = slotsList
                    }
                    slotMetaData.memberAvailability = hashMap
                }

                slotMetaData.showWaitingList = outputData.getBoolean("SHOW_WAIT_LIST") ?: false

                val workingDays = outputData.getAsJsonObject("working_days")

                val memberWorkTimingList = ArrayList<MemberWorkTiming>()

                for (i in 0..6) {
                    memberWorkTimingList.add(getMemberWorkTimingForSlot(workingDays, i))
                }

                slotMetaData.memberWorkTiming = memberWorkTimingList

            }


//            slotMetaData.memberAvailability = slotsList
            return slotMetaData
        }

        private fun getMemberWorkTimingForSlot(
            workingDaysObj: JsonObject,
            dayOfWeek: Int
        ): MemberWorkTiming {
            val workingDays = workingDaysObj.getBoolean("$dayOfWeek")
            val workingDaysTiming = MemberWorkTiming()
            workingDaysTiming.dayOfWeek = if (dayOfWeek == 0) {
                7
            } else {
                dayOfWeek
            }
            workingDaysTiming.isMemberWorking = workingDays ?: true

            return workingDaysTiming
        }


        fun formBookAppointmentResp(respObj: JsonObject): String {


            val outputData = respObj.getAsJsonObject("output_string")

            return if (outputData != null) {
                outputData.getString("message") ?: ""
            } else {

                respObj.getString("message") ?: ""
            }

        }
        //endregion

        //region Billing

        fun formReceiptList(respObj: JsonObject, practiceId: String): ArrayList<PHRReceipts> {

            val receiptList = arrayListOf<PHRReceipts>()

            val outputData = respObj.getAsJsonObject("output_string")
            if (outputData != null) {
                val receiptArr = outputData.getAsJsonArray("RECEIPT_LIST")

                val facilitiesObj = outputData.getAsJsonObject("FACILITIES_LIST")

                val practiceCurrencySymbol = outputData.getString("PRACTICE_CURRENCY_SYMBOL")

                if (receiptArr != null) {

                    receiptArr.forEach { receiptEle ->
                        val receiptObj = receiptEle.asJsonObject


                        if (receiptObj != null) {
                            val phrReceipts = PHRReceipts()


                            phrReceipts.practiceId = practiceId
                            phrReceipts.receiptAmount = receiptObj.getString("RECEIPT_AMOUNT")
                            phrReceipts.receiptNumber = receiptObj.getString("RECEIPT_NUMBER")
                            phrReceipts.ehrReceiptId = receiptObj.getString("EHR_RECEIPT_ID")
                            phrReceipts.phrReceiptId = receiptObj.getString("PHR_RECEIPT_ID")
                            phrReceipts.sentFromEhr = receiptObj.getBoolean("SENT_FROM_EHR")
                            phrReceipts.isRead = receiptObj.getBoolean("IS_READ") ?: false

                            phrReceipts.details = receiptObj.getString("DETAILS")


                            phrReceipts.status = receiptObj.getString("STATUS")



                            phrReceipts.practiceCurrencySymbol = practiceCurrencySymbol

                            val facilityId = receiptObj.getString("FACILITY_ID")

                            if (!facilityId.isNullOrBlank()) {
                                val facilityObj = facilitiesObj?.getAsJsonObject(facilityId)
                                phrReceipts.facility = formFacilityObj(facilityObj, facilityId)
                            }

                            val billDateStr = receiptObj.getString("RECEIPT_DATE")
                            if (!billDateStr.isNullOrBlank()) {
                                phrReceipts.receiptDate = DateTime.parse(
                                    billDateStr,
                                    ModelUtil.charmDisplayDateFormat()
                                )
                            }

                            receiptList.add(phrReceipts)


                        }
                    }
                }
            }

            return receiptList
        }


        fun formInvoiceList(respObj: JsonObject, practiceId: String): ArrayList<PHRBills> {

            val invoiceList = arrayListOf<PHRBills>()

            val outputData = respObj.getAsJsonObject("output_string")
            if (outputData != null) {
                val invoiceArr = outputData.getAsJsonArray("INVOICES_LIST")

                val facilitiesObj = outputData.getAsJsonObject("FACILITIES_LIST")

                val practiceCurrencySymbol = outputData.getString("PRACTICE_CURRENCY_SYMBOL")

                val paymentGateWay = outputData.getString("GATEWAY")

                val ehrInvoiceArr = outputData.getAsJsonObject("EHR_INVOICES_LIST")

                if (invoiceArr != null) {


                    invoiceArr.forEach { invoiceEle ->
                        val invoiceObj = invoiceEle.asJsonObject
                        if (invoiceObj != null) {
                            val phrBill = PHRBills()

                            phrBill.paymentGateway = paymentGateWay

                            phrBill.isStatement = invoiceObj.getBoolean("IS_STATEMENT") ?: false


                            phrBill.practiceId = practiceId

                            phrBill.memberId = invoiceObj.getString("BILL_PROVIDER_ID")
                            if (phrBill.isStatement) {

                                phrBill.statementId = invoiceObj.getString("STATEMENT_ID")
                                val statementMapObj = outputData.getAsJsonObject("STATEMENT_MAP")
                                if (statementMapObj != null) {
                                    val statementObj =
                                        statementMapObj.getAsJsonObject(phrBill.statementId)
                                    if (statementObj != null) {
                                        phrBill.billAmount =
                                            statementObj.getString("STATEMENT_AMOUNT")
                                        phrBill.dueAmount =
                                            statementObj.getString("CURRENT_STATEMENT_DUE")
                                        phrBill.amount =
                                            statementObj.getString("CURRENT_STATEMENT_PAYMENT_REQUEST")
                                        phrBill.isPaid =
                                            statementObj.getBoolean("IS_STATEMENT_PAID")
                                        phrBill.billNumber = ModelUtil.invoiceStatementStr

                                        val facilityIdList =
                                            statementObj.getAsJsonArray("FACILITY_IDS")

                                        if (facilityIdList.isNotJsonNull() && facilityIdList.size() > 0) {
                                            val facilityId = facilityIdList[0]?.asString ?: ""
                                            val facilityObj =
                                                facilitiesObj?.getAsJsonObject(facilityId)
                                            phrBill.facility =
                                                formFacilityObj(facilityObj, facilityId)
                                        }
                                    }
                                }
                            } else {
                                phrBill.amount = invoiceObj.getString("AMOUNT")
                                phrBill.billAmount = invoiceObj.getString("BILL_AMOUNT")
//                                phrBill.dueAmount = invoiceObj.getString("DUE_AMOUNT")
                                phrBill.billNumber = invoiceObj.getString("BILL_NUMBER")
                                phrBill.ehrBillId = invoiceObj.getString("EHR_BILL_ID")
                                phrBill.phrBillId = invoiceObj.getString("PHR_BILL_ID")

                                val ehrBillObj = ehrInvoiceArr.getAsJsonObject(phrBill.ehrBillId)
                                phrBill.dueAmount = ehrBillObj.getString("DUE") ?: ""
                                phrBill.billType = ehrBillObj.getString("BILL_TYPE") ?: ""
                                val billStatus = ehrBillObj.getString("BILL_STATUS") ?: ""
                                phrBill.isPaid =
                                    billStatus.equals("Paid", true) || billStatus.equals(
                                        "Canceled",
                                        true
                                    )
                                phrBill.status = invoiceObj.getString("STATUS")

                                val facilityId = invoiceObj.getString("FACILITY_ID")

                                if (!facilityId.isNullOrBlank()) {
                                    val facilityObj = facilitiesObj?.getAsJsonObject(facilityId)
                                    phrBill.facility = formFacilityObj(facilityObj, facilityId)
                                }
                            }


                            phrBill.practiceCurrencySymbol = practiceCurrencySymbol


                            val billDateStr = invoiceObj.getString("BILL_DATE")
                            if (!billDateStr.isNullOrBlank()) {
                                phrBill.billDate = DateTime.parse(
                                    billDateStr,
                                    ModelUtil.charmDisplayDateFormat()
                                )
                            }

                            invoiceList.add(phrBill)


                        }
                    }
                }
            }

            return invoiceList
        }


        fun getPHRBillHistoryId(respObj: JsonObject): PHRBills {

            val outputData = respObj.getAsJsonObject("output_string")
            val pHRBillsObj = PHRBills()

            val phrBillHistoryList = outputData.getAsJsonArray("history")

            val phrBillHistory = phrBillHistoryList.lastOrNull()?.asJsonObject

            if (phrBillHistory != null) {


                val sharedByObj = phrBillHistory.getAsJsonObject("shared_by")


                val phrBillId = phrBillHistory.getString("phr_bill_data_id")

                val memberId = sharedByObj?.getString("member_id")

                if (!phrBillId.isNullOrBlank()) {
                    pHRBillsObj.phrBillId = phrBillId
                    pHRBillsObj.memberId = memberId
                    pHRBillsObj.billNumber = outputData.getString("bill_number")
                    pHRBillsObj.merchantAccountId = outputData.getString("merchant_account_id")
                    pHRBillsObj.billAmount = outputData.getString("bill_amount")
                    pHRBillsObj.dueAmount = outputData.getString("due_amount")
                    pHRBillsObj.status = outputData.getString("bill_status")
                    pHRBillsObj.time = outputData.getString("time")
                    pHRBillsObj.isPaid = outputData.getBoolean("is_paid")
                    val facilityJson = outputData.getAsJsonObject("facility")
                    pHRBillsObj.facilityId = facilityJson.getString("facility_id")
                    if (pHRBillsObj.facilityId != null) {
                        val facility = Facility()
                        facility.facilityId = pHRBillsObj.facilityId
                        facility.facilityName = facilityJson.getString("facility_name")
                        facility.facilityCode = facilityJson.getString("facility_code")
                        pHRBillsObj.facility = facility
                    }
                    pHRBillsObj.isPartialPaymentAllowed =
                        outputData.getBoolean("is_partial_payment_allowed")
                    pHRBillsObj.partialPaymentMinAmount =
                        outputData.getString("minimum_payment_amount")
                }
            }

            return pHRBillsObj
        }
        //endregion

        //region Card on file

        fun getCardsForPatient(respObj: JsonObject, practiceId: String): CardOnFilePOJO {

            val cardOnFilePOJO = CardOnFilePOJO()

            val jsonObject = respObj.getAsJsonObject("output_string")
            val postalAddressObj = jsonObject.getAsJsonObject("patient_address")
            val countryJson = jsonObject.getAsJsonObject("country_list")
            cardOnFilePOJO.countryStr = countryJson.toString()
            val allowPracticeCardDeletion =
                jsonObject.getBoolean("allow_prac_card_deletion") ?: false
            if (postalAddressObj != null) {
                val postalAddress = PostalAddress()
                postalAddress.country = postalAddressObj.getString("COUNTRY")
                postalAddress.city = postalAddressObj.getString("CITY")
                postalAddress.state = postalAddressObj.getString("STATE")
                postalAddress.postalCode = postalAddressObj.getString("POSTALCODE")
                postalAddress.line1 = postalAddressObj.getString("LINE_1")
                postalAddress.line2 = postalAddressObj.getString("LINE_2")
                cardOnFilePOJO.postalAddress = postalAddress
            }
            val patientCardJsonArr = jsonObject.getAsJsonArray("patient_cards")
            patientCardJsonArr.forEach {
                val cardObj = it.asJsonObject
                val paymentGatewayToken = getPaymentGatewayToken(cardObj, practiceId)
                paymentGatewayToken.allowPracticeCardDeletion = allowPracticeCardDeletion
                cardOnFilePOJO.patientSavedCardArrayList.add(paymentGatewayToken)
            }
            val practiceCardJsonArr = jsonObject.getAsJsonArray("practice_cards")
            practiceCardJsonArr.forEach {
                val cardObj = it.asJsonObject
                val paymentGatewayToken = getPaymentGatewayToken(cardObj, practiceId)
                paymentGatewayToken.allowPracticeCardDeletion = allowPracticeCardDeletion
                cardOnFilePOJO.practiceSavedCardArrayList.add(paymentGatewayToken)
            }

            return cardOnFilePOJO

        }

        fun getPaymentGatewayToken(cardObj: JsonObject, practiceId: String): PaymentGatewayToken {
            val paymentGatewayToken = PaymentGatewayToken()
            paymentGatewayToken.cardBrand = cardObj.getString("CARD_BRAND")
            paymentGatewayToken.cardDigit = cardObj.getString("CARD_DIGIT")
            paymentGatewayToken.cardNickName = cardObj.getString("CARD_NICK_NAME")
            paymentGatewayToken.expiry = cardObj.getString("EXPIRY_DATE")
            paymentGatewayToken.facilityId = cardObj.getString("FACILITY_ID")
            paymentGatewayToken.hasCardExpired = cardObj.getBoolean("HAS_CARD_EXPIRED") ?: false
            paymentGatewayToken.isPHRCard = cardObj.getBoolean("IS_PHR_CARD") ?: false
            paymentGatewayToken.isPrimaryCard = cardObj.getBoolean("IS_PRIMARY_CARD") ?: false
            paymentGatewayToken.merchantAccountId = cardObj.getString("MERCHANT_ACCOUNT_ID")
            paymentGatewayToken.nameOnCard = cardObj.getString("NAME_ON_CARD")
            paymentGatewayToken.patientId = cardObj.getString("PATIENT_ID")
            paymentGatewayToken.practiceId = practiceId
            paymentGatewayToken.practicePatientId = cardObj.getString("PRACTICE_PATIENT_ID")
            paymentGatewayToken.tokenId = cardObj.getString("TOKEN_ID") //Local DB PK
            paymentGatewayToken.token = cardObj.getString("TOKEN") //bluefin PK
            //Always send tokenId as 12 digit number - if it is less than 12 add 0 to the start
            if (!paymentGatewayToken.token.isNullOrBlank()) {
                if ((paymentGatewayToken.token?.length ?: 0) < 12) {
                    paymentGatewayToken.token = paymentGatewayToken.token?.padStart(12, '0')
                }
            }
            val postalAddress = PostalAddress()
            postalAddress.country = cardObj.getString("COUNTRY")
            postalAddress.city = cardObj.getString("CITY")
            postalAddress.state = cardObj.getString("STATE")
            postalAddress.postalCode = cardObj.getString("ZIP")
            postalAddress.line1 = cardObj.getString("STREET_ADDRESS1")
            postalAddress.line2 = cardObj.getString("STREET_ADDRESS2")
            paymentGatewayToken.postalAddress = postalAddress
            return paymentGatewayToken
        }

        fun getMerchantAccountList(respObj: JsonObject): ArrayList<MerchantAccountInfo> {
            val merchantJsonArr = respObj.getAsJsonArray("output_string")
            val merchantAccountArrayList: ArrayList<MerchantAccountInfo> = ArrayList()
            merchantJsonArr.forEach {
                val merchantAccountInfo = MerchantAccountInfo()
                val merchantObj = it as JsonObject
                merchantAccountInfo.merchantAccountId = merchantObj.getString("merchant_account_id")
                merchantAccountInfo.facilityId = merchantObj.getString("facility_id")
                merchantAccountInfo.title = merchantObj.getString("title")
                merchantAccountArrayList.add(merchantAccountInfo)
            }
            return merchantAccountArrayList
        }

        fun getGroupId(respObj: JsonObject): String {
            val groupJson = respObj.getAsJsonObject("output_string")
            return groupJson.getString("group_id") ?: ""
        }

        fun getStatementDetail(jsonObject: JsonObject): PHRStatementDetail {
            val statementDetail = PHRStatementDetail()
            val outputJson = jsonObject.getAsJsonObject("output_string")
            val invoicesDetailJson = outputJson.getAsJsonArray("invoice_details")
            val invoicesDetails = arrayListOf<PHRInvoicesDetail>()
            invoicesDetailJson.forEach {
                invoicesDetails.add(getInvoiceDetail(it.asJsonObject))
            }
            statementDetail.invoicesDetails = invoicesDetails
            statementDetail.statementId = outputJson.getString("phr_statement_id")
            statementDetail.merchantAccountId = outputJson.getString("merchant_account_id")
            statementDetail.requestedAmount = outputJson.getString("payment_requested")
            val sharedByJson = outputJson.getAsJsonObject("shared_by")
            statementDetail.memberId = sharedByJson.getString("member_id")
            val facilityJson = outputJson.getAsJsonObject("facility")
            statementDetail.facilityId = facilityJson.getString("facility_id")
            statementDetail.isPartialPaymentAllowed =
                outputJson.getBoolean("is_partial_payment_allowed")
            statementDetail.partialPaymentMinAmount = outputJson.getString("minimum_payment_amount")
            statementDetail.isPaid = outputJson.getBoolean("is_paid")
            return statementDetail
        }

        fun getInvoiceDetail(invoiceJson: JsonObject): PHRInvoicesDetail {
            val invoicesDetail = PHRInvoicesDetail()
            invoicesDetail.phrBillNumber = invoiceJson.getString("bill_number")
            invoicesDetail.billAmount = invoiceJson.getString("bill_amount")
            invoicesDetail.dueAmount = invoiceJson.getString("due_amount")
            invoicesDetail.patientRecordId = invoiceJson.getString("patient_record_id")
            invoicesDetail.patientId = invoiceJson.getString("patient_id")
            invoicesDetail.patientName = invoiceJson.getString("patient_name")
            invoicesDetail.paymentRequested = invoiceJson.getString("payment_requested")
            invoicesDetail.status = invoiceJson.getString("bill_status")
            invoicesDetail.billDate =
                DateTime.parse(invoiceJson.getString("bill_date"), ModelUtil.charmApiDateFormat())
            return invoicesDetail
        }

        fun getPostalAddressObject(postalAddressObj: JsonObject): PostalAddress {
            val postalAddress = PostalAddress()
            postalAddress.country = postalAddressObj.getString("COUNTRY")
            postalAddress.city = postalAddressObj.getString("CITY")
            postalAddress.state = postalAddressObj.getString("STATE")
            postalAddress.postalCode = postalAddressObj.getString("POSTALCODE")
            postalAddress.line1 = postalAddressObj.getString("LINE_1")
            postalAddress.line2 = postalAddressObj.getString("LINE_2")
            return postalAddress
        }

        fun getCountryList(countryJson: JsonObject): ArrayList<DataWithId> {
            val countryArr = arrayListOf<DataWithId>()
            countryJson.entrySet()?.forEach {
                countryArr.add(DataWithId(it.value.asString, it.key))
            }
            return countryArr
        }

        //endregion

        //region Questionnaire

        fun formQuestionnaireList(
            respObj: JsonObject,
            practiceId: String? = null,
            appointmentId: String? = null
        ): ArrayList<PhysicianTemplates> {
            val physicianTemplatesList = arrayListOf<PhysicianTemplates>()

            val outputData = respObj.getAsJsonArray("output_string")

            outputData?.forEachIndexed { index, questionnaireEle ->

                val questionnaireObj = questionnaireEle?.asJsonObject

                if (questionnaireObj != null) {


                    val physicianTemplates = formQuestionnaireObj(questionnaireObj, practiceId)

                    if (!appointmentId.isNullOrBlank()) {
                        if (appointmentId == physicianTemplates.patientQuesMap?.appointment?.appointmentId) {
                            physicianTemplatesList.add(physicianTemplates)
                        }
                    } else {
                        physicianTemplatesList.add(physicianTemplates)
                    }
                }
            }

            return physicianTemplatesList
        }

        fun formQuestionnaireHPList(respObj: JsonObject): ArrayList<PhysicianTemplates> {
            val physicianTemplatesList = arrayListOf<PhysicianTemplates>()

            val outputData = respObj.getAsJsonObject("output_string")?.entrySet()?.iterator()

            outputData?.forEach { questionnaireEle ->

                val physicianTemplates = PhysicianTemplates()
                physicianTemplates.isHealthPassport = true
                physicianTemplates.isEditable = true
                physicianTemplates.name = questionnaireEle.key
                physicianTemplates.patientQuesMap =
                    PatientQuesMap(patientQuesMapId = questionnaireEle.value?.asString)
                physicianTemplatesList.add(physicianTemplates)
            }


            for (physicianTemplate in physicianTemplatesList) {

                if (physicianTemplate.name == ModelUtil.questionnaireNameKeyAutism) {
                    physicianTemplate.name = ModelUtil.questionnaireNameValueAutism
                    continue
                } else if (physicianTemplate.name == ModelUtil.questionnaireNameKeyMedInfo) {
                    physicianTemplate.name = ModelUtil.questionnaireNameValueMedInfo
                    continue
                } else if (physicianTemplate.name == ModelUtil.questionnaireNameKeyPastMed) {
                    physicianTemplate.name = ModelUtil.questionnaireNameValuePastMed
                    continue
                } else if (physicianTemplate.name == ModelUtil.questionnaireNameKeyCovid) {
                    physicianTemplate.name = ModelUtil.questionnaireNameValueCovid
                    continue
                } else if (physicianTemplate.name == ModelUtil.questionnaireNameKeyPermission) {
                    physicianTemplate.name = ModelUtil.questionnaireNameValuePermission
                    continue
                }

            }

            return physicianTemplatesList
        }

        fun formQuestionnaireObj(
            questionnaireObj: JsonObject,
            practiceId: String? = null
        ): PhysicianTemplates {

            val physicianTemplates = PhysicianTemplates()
            physicianTemplates.name = questionnaireObj.getString("NAME")
            physicianTemplates.templateId = questionnaireObj.getString("TEMPLATE_ID")
            physicianTemplates.comments = questionnaireObj.getString("COMMENTS")
            physicianTemplates.practiceId = questionnaireObj.getString("PRACTICE_ID") ?: practiceId
            physicianTemplates.type = questionnaireObj.getString("TYPE")

            val member = Member()
            member.fullName = questionnaireObj.getString("FULL_NAME")
            member.prefix = questionnaireObj.getString("PREFIX")
            member.memberId = questionnaireObj.getString("MEMBER_ID")

            val patientQuesMap = PatientQuesMap()
            patientQuesMap.patientQuesMapId = questionnaireObj.getString("PATIENT_QUES_MAP_ID")
            patientQuesMap.allowPhrEditing = questionnaireObj.getBoolean("ALLOW_PHR_EDITING")
                ?: false
            patientQuesMap.isSubmitted = questionnaireObj.getBoolean("IS_SUBMITTED") ?: false
            patientQuesMap.isFilled = questionnaireObj.getBoolean("IS_FILLED") ?: false

            val appointment = Appointment()

            appointment.appointmentId = questionnaireObj.getString("APPOINTMENT_ID")

            val appointmentDate = questionnaireObj.getString("APPOINTMENT_DATE")

            if (!appointmentDate.isNullOrBlank()) {
                appointment.appointmentDate =
                    DateTime.parse(appointmentDate, ModelUtil.charmDisplayDateFormat())
            }

            appointment.facilityId = questionnaireObj.getString("FACILITY_ID")
            appointment.facilityName = questionnaireObj.getString("FACILITY_NAME")

            patientQuesMap.appointment = appointment
            physicianTemplates.member = member
            physicianTemplates.patientQuesMap = patientQuesMap


            physicianTemplates.toString = questionnaireObj.toString()

            return physicianTemplates
        }

        fun fillQuestionnaireAnswers(
            respObj: JsonObject,
            physicianTemplates: PhysicianTemplates
        ): QuestionnaireDetailPOJO {

            val questionnaireDetailPOJO = QuestionnaireDetailPOJO()


            val outputData = /*if (isHealthPassport) {*/respObj.getAsJsonObject("output_string")
            /*} else {
                respObj.getAsJsonObject("questionnaire_with_answers")
            }*/
            if (outputData != null) {

                physicianTemplates.patientQuesMap?.isFilled =
                    outputData.getBoolean("is_filled") ?: false
                physicianTemplates.patientQuesMap?.isSubmitted =
                    outputData.getBoolean("is_submitted") ?: false
                questionnaireDetailPOJO.patientId = outputData.getString("patient_id")
                questionnaireDetailPOJO.templateId = outputData.getString("template_id")
                questionnaireDetailPOJO.patientQuesMapId = outputData.getString("ques_map_id")
                physicianTemplates.patientQuesMap?.allowPhrEditing =
                    outputData.getBoolean("allow_phr_editing") ?: false

                val questionsArr = outputData.getAsJsonArray("questions")


                val questionnaireEntryPOJOList = arrayListOf<QuestionnaireEntryPOJO>()
                questionsArr?.forEach { questionEle ->

                    val questionObj = questionEle?.asJsonObject
                    if (questionObj != null) {

                        val questionnaireEntryPOJO = QuestionnaireEntryPOJO()
                        val intakeQuestTemplateEntry = IntakeQuestTemplateEntry()
                        intakeQuestTemplateEntry.notes = questionObj.getString("notes")
                        intakeQuestTemplateEntry.isMultiChoice =
                            questionObj.getBoolean("is_multi_choice")
                                ?: false
                        intakeQuestTemplateEntry.isDeleted = questionObj.getBoolean("is_deleted")
                            ?: false

                        intakeQuestTemplateEntry.templateId = questionObj.getString("template_id")
                            ?: ""

                        intakeQuestTemplateEntry.isMandatory =
                            questionObj.getBoolean("is_mandatory")
                                ?: false
                        intakeQuestTemplateEntry.position = questionObj.getInt("position")
                        intakeQuestTemplateEntry.entryId = questionObj.getString("entry_id") ?: ""
                        intakeQuestTemplateEntry.notesType = questionObj.getString("notes_type")
                            ?: ""
                        //Added for section
                        intakeQuestTemplateEntry.isCollapsible =
                            questionObj.get("is_collapsible")?.asBoolean ?: true
                        intakeQuestTemplateEntry.expandByDefault =
                            questionObj.get("expand_by_default")?.asBoolean ?: false


                        val optionsArr = questionObj.getAsJsonArray("options")

                        val optionsList = arrayListOf<IntakeQuestTemplateOption>()


                        optionsArr?.forEach { option ->
                            val intakeOption = IntakeQuestTemplateOption()
                            intakeOption.option = option?.asString
                            intakeOption.entryId = intakeQuestTemplateEntry.entryId
                            optionsList.add(intakeOption)
                        }

                        when (intakeQuestTemplateEntry.notesType) {
                            ModelUtil.questionnaireNotesTypeLabel -> {
                                val intakeLabel = IntakeQuestTemplateLabels()

                                intakeLabel.fontWeight = questionObj.getString("font_weight") ?: ""
                                intakeLabel.textDecoration =
                                    questionObj.getString("text_decoration")
                                        ?: ""
                                intakeLabel.fontSize = questionObj.getInt("font_size") ?: 14
                                intakeLabel.fontStyle = questionObj.getString("font_style") ?: ""
                                intakeLabel.textAlign = questionObj.getString("text_align") ?: ""

                                intakeLabel.entryId = intakeQuestTemplateEntry.entryId

                                questionnaireEntryPOJO.questionnaireLabel = intakeLabel
                            }

                            ModelUtil.questionnaireNotesTypeAllergy -> {
                                val questAllergyEntryList = arrayListOf<IntakeQuestAllergyEntry>()

                                val allergyListArr = questionObj.getAsJsonArray("allergy_entries")

                                allergyListArr?.forEach { allergyEle ->

                                    val allergyObj = allergyEle?.asJsonObject

                                    if (allergyObj != null) {

                                        val questAllergyEntry = IntakeQuestAllergyEntry()
                                        questAllergyEntry.allergen =
                                            allergyObj.getString("allergen")
                                        questAllergyEntry.type = allergyObj.getString("type")
                                        questAllergyEntry.reactions =
                                            allergyObj.getString("reactions")
                                        questAllergyEntry.severity =
                                            allergyObj.getString("severity")

                                        questAllergyEntryList.add(questAllergyEntry)

                                    }
                                }
                                questionnaireEntryPOJO.questionnaireAllergyList =
                                    questAllergyEntryList
                            }

                            ModelUtil.questionnaireNotesTypeMedication -> {
                                val questMedicationEntryList =
                                    arrayListOf<IntakeQuestMedicationEntry>()

                                val medicationListArr =
                                    questionObj.getAsJsonArray("medication_entries")

                                medicationListArr?.forEach { medicationEle ->

                                    val medicationObj = medicationEle?.asJsonObject

                                    if (medicationObj != null) {

                                        val questMedicationEntry = IntakeQuestMedicationEntry()
                                        questMedicationEntry.drugName =
                                            medicationObj.getString("medication")
                                        questMedicationEntry.intakeDetails =
                                            medicationObj.getString("intake_details")

                                        questMedicationEntryList.add(questMedicationEntry)

                                    }
                                }
                                questionnaireEntryPOJO.questionnaireMedicationList =
                                    questMedicationEntryList
                            }

                            ModelUtil.questionnaireNotesTypeSupplement -> {
                                val questSupplementEntryList =
                                    arrayListOf<IntakeQuestSupplementEntry>()

                                val supplementListArr =
                                    questionObj.getAsJsonArray("supplement_entries")

                                supplementListArr?.forEach { supplementEle ->

                                    val supplementObj = supplementEle?.asJsonObject

                                    if (supplementObj != null) {

                                        val questSupplementEntry = IntakeQuestSupplementEntry()
                                        questSupplementEntry.supplementName =
                                            supplementObj.getString("supplement")
                                        questSupplementEntry.intakeDetails =
                                            supplementObj.getString("intake_details")

                                        questSupplementEntryList.add(questSupplementEntry)

                                    }
                                }

                                questionnaireEntryPOJO.questionnaireSupplementList =
                                    questSupplementEntryList
                            }

                            ModelUtil.questionnaireNotesTypeRatingScale -> {
                                intakeQuestTemplateEntry.fromScale =
                                    questionObj.getInt("from_scale")
                                intakeQuestTemplateEntry.toScale = questionObj.getInt("to_scale")
                                intakeQuestTemplateEntry.answersList = arrayListOf(
                                    questionObj.getString("answer")
                                        ?: ""
                                )
                            }

                            ModelUtil.questionnaireNotesTypeSignature -> {
                                val signLink = questionObj.getString("signature_url")

                                if (!signLink.isNullOrBlank())
                                    intakeQuestTemplateEntry.answersList = arrayListOf(signLink)
                            }

                            ModelUtil.questionnaireNotesTypeRating -> {
                                val optionsList = arrayListOf<IntakeQuestTemplateRating>()
                                val optionsJson = questionObj.getAsJsonObject("description")
                                optionsJson.keySet().iterator().forEach {
                                    val option = IntakeQuestTemplateRating()
                                    option.option = it
                                    option.optionDescription = optionsJson.getString(it)
                                    optionsList.add(option)
                                }
                                questionnaireEntryPOJO.questionnaireRating = optionsList
                                val answer = questionObj.getString("answer")
                                if (!answer.isNullOrEmpty()) {
                                    intakeQuestTemplateEntry.answersList = arrayListOf(answer)
                                }
                            }

                            else -> {
                                val answer = questionObj.getString("answer") ?: ""

                                intakeQuestTemplateEntry.answersList = arrayListOf(answer)
                            }
                        }

                        if (ModelUtil.SOAPTypes.contains(intakeQuestTemplateEntry.notesType)) {
                            val soapWidgetJson = questionObj.get("soap_widget")?.asJsonObject
                            if (soapWidgetJson != null) {
                                val soapWidgetObj = SOAPWidgetPOJO()
                                val soapWidget = SOAPWidget()
                                soapWidget.widgetModule =
                                    soapWidgetJson.getString("soap_widget_module")
                                soapWidget.widgetName = soapWidgetJson.getString("soap_widget_name")
                                soapWidget.widgetId = soapWidgetJson.getString("soap_widget_id")
                                soapWidget.templateId = soapWidgetJson.getString("soap_template_id")

                                val soapWidgetEntries = soapWidgetJson.get("entries")?.asJsonArray
                                val soapWidgetEntryPOJOList = arrayListOf<SOAPWidgetEntryPOJO>()
                                soapWidgetEntries?.forEach {
                                    val soapEntriesJson = it.asJsonObject
                                    val soapWidgetEntry = SOAPWidgetEntryPOJO()
                                    soapWidgetEntry.soapWidgetEntry = SOAPWidgetEntry()
                                    soapWidgetEntry.soapWidgetEntry?.entryId =
                                        soapEntriesJson.getString("soap_entry_id")
                                    soapWidgetEntry.soapWidgetEntry?.widgetId =
                                        soapEntriesJson.getString("soap_widget_id")
                                    soapWidgetEntry.soapWidgetEntry?.entryType =
                                        soapEntriesJson.getString("soap_entry_type")
                                    soapWidgetEntry.soapWidgetEntry?.entryAnswer =
                                        soapEntriesJson.getString("soap_entry_answer")
                                    soapWidgetEntry.soapWidgetEntry?.entryName =
                                        soapEntriesJson.getString("soap_entry_name")
                                    soapWidgetEntry.soapWidgetEntry?.entryPosition =
                                        soapEntriesJson.getInt("soap_entry_position")
                                    soapWidgetEntry.soapWidgetEntry?.isMandatory =
                                        soapEntriesJson.getBoolean("is_mandatory")
                                    if (soapWidgetEntry.soapWidgetEntry?.entryType.equals(ModelUtil.soapWidgetHeaderType)) {
                                        val soapWidgetPropertyList =
                                            arrayListOf<SOAPWidgetProperty>()
                                        val propertiesJson = soapEntriesJson.getString("PROPERTIES")
                                        if (propertiesJson != null) {
                                            val propertiesArr = propertiesJson.split("^^")
                                            propertiesArr.forEach { property ->
                                                val soapWidgetProperty = SOAPWidgetProperty()
                                                val keyValArr = property.split(":")
                                                soapWidgetProperty.propertyName = keyValArr[0]
                                                soapWidgetProperty.propertyValue = keyValArr[1]
                                                soapWidgetProperty.entryId =
                                                    soapWidgetEntry.soapWidgetEntry?.entryId
                                                soapWidgetPropertyList.add(soapWidgetProperty)
                                            }
                                            soapWidgetEntry.soapWidgetProperty =
                                                soapWidgetPropertyList
                                        }
                                    } else if (soapWidgetEntry.soapWidgetEntry?.entryType.equals(
                                            ModelUtil.soapWidgetTableType
                                        )
                                    ) {
                                        if (soapWidgetEntry.soapWidgetEntry?.entryAnswer.isNullOrBlank()) {
                                            soapWidgetEntry.soapWidgetEntry?.soapWidgetTableData =
                                                getTableData(
                                                    soapWidgetEntry.soapWidgetEntry?.entryName ?: ""
                                                )
                                        } else {
                                            soapWidgetEntry.soapWidgetEntry?.soapWidgetTableData =
                                                getTableData(
                                                    soapWidgetEntry.soapWidgetEntry?.entryAnswer
                                                        ?: ""
                                                )
                                        }

                                    } else if (soapWidgetEntry.soapWidgetEntry?.entryType.equals(
                                            ModelUtil.soapWidgetSimpleQuestionType
                                        ) ||
                                        soapWidgetEntry.soapWidgetEntry?.entryType.equals(ModelUtil.soapWidgetRadioType) ||
                                        soapWidgetEntry.soapWidgetEntry?.entryType.equals(ModelUtil.soapWidgetCheckBoxType) ||
                                        soapWidgetEntry.soapWidgetEntry?.entryType.equals(ModelUtil.soapWidgetRatingScaleType)
                                    ) {
                                        val optionsStr = soapEntriesJson.getString("OPTIONS")
                                        soapWidgetEntry.soapWidgetEntry?.options = optionsStr
                                    }

                                    if (soapWidgetEntry.soapWidgetEntry?.entryType.equals(ModelUtil.soapWidgetRadioType) ||
                                        soapWidgetEntry.soapWidgetEntry?.entryType.equals(ModelUtil.soapWidgetCheckBoxType) ||
                                        soapWidgetEntry.soapWidgetEntry?.entryType.equals(ModelUtil.soapWidgetCheckListType) ||
                                        soapWidgetEntry.soapWidgetEntry?.entryType.equals(ModelUtil.soapWidgetYesOrNoQuestionType)
                                    ) {
                                        val soapWidgetSelectableEntry = SOAPWidgetSelectableEntry()
                                        if (soapWidgetEntry.soapWidgetEntry?.entryType.equals(
                                                ModelUtil.soapWidgetYesOrNoQuestionType
                                            )
                                        ) {
                                            soapWidgetSelectableEntry.isAddCommentsForNo =
                                                soapEntriesJson.getBoolean("ADDITIONAL_COMMENT_FOR_NO")
                                                    ?: false
                                            soapWidgetSelectableEntry.isAddCommentsForYes =
                                                soapEntriesJson.getBoolean("ADDITIONAL_COMMENT_FOR_YES")
                                                    ?: false
                                        } else {
                                            soapWidgetSelectableEntry.isAdditionalComments =
                                                soapEntriesJson.getBoolean("ADDITIONAL_COMMENT")
                                                    ?: false
                                            soapWidgetSelectableEntry.isIncludeOthers =
                                                soapEntriesJson.getBoolean("INCLUDE_OTHERS")
                                                    ?: false
                                            val optionsAL =
                                                if (soapWidgetEntry.soapWidgetEntry?.entryType.equals(
                                                        ModelUtil.soapWidgetCheckListType
                                                    )
                                                ) TemplateUIUtil.getOptionsArrayFromString(
                                                    soapWidgetEntry.soapWidgetEntry?.entryName ?: ""
                                                ) else
                                                    TemplateUIUtil.getOptionsArrayFromString(
                                                        soapWidgetEntry.soapWidgetEntry?.options
                                                            ?: ""
                                                    )
                                            val answerAL = TemplateUIUtil.getAnswerMapFromString(
                                                soapWidgetEntry.soapWidgetEntry?.entryAnswer ?: ""
                                            )
                                            soapWidgetSelectableEntry.optionList =
                                                TemplateUIUtil.getOptionList(optionsAL, answerAL, 0)
                                            if (soapWidgetSelectableEntry.isAdditionalComments && answerAL.isNotEmpty()) {
                                                val comment = answerAL.entries.last().key
                                                if (comment.contains(ModelUtil.appendStringCommentsValue)) {
                                                    soapWidgetSelectableEntry.additionalComments =
                                                        comment.substring(14, comment.length)
                                                    answerAL.remove(comment)
                                                }
                                            }
                                            if (soapWidgetSelectableEntry.isIncludeOthers) {
                                                val optionData = OptionData()
                                                optionData.optionString = ModelUtil.othersValue
                                                optionData.viewLevel = 0
                                                optionData.isOther = true
                                                if (answerAL.isNotEmpty()) {
                                                    val otherValue = answerAL.entries.first().key
                                                    if (otherValue.isNotBlank()) {
                                                        optionData.checked = true
                                                        optionData.otherValue = otherValue
                                                    }
                                                }
                                                soapWidgetSelectableEntry.optionList.add(optionData)
                                            }
                                        }
                                        soapWidgetEntry.soapWidgetEntry?.soapWidgetSelectableEntry =
                                            soapWidgetSelectableEntry
                                    }
                                    soapWidgetEntryPOJOList.add(soapWidgetEntry)
                                }
                                soapWidgetObj.soapWidgetEntryPOJOList = soapWidgetEntryPOJOList
                                soapWidgetObj.soapWidget = soapWidget
                                questionnaireEntryPOJO.soapWidget = soapWidgetObj
                            }
                        }

                        if (intakeQuestTemplateEntry.isMultiChoice) {

                            val answerListArr = questionObj.getAsJsonArray("multiple_answers")

                            val answersList = arrayListOf<String>()

                            answerListArr?.forEach { answerListEle ->
                                val answerListObj = answerListEle?.asJsonObject
                                if (answerListObj != null) {
                                    answersList.add(answerListObj.getString("answer") ?: "")
                                }
                            }

                            intakeQuestTemplateEntry.answersList = answersList
                        }

                        if (intakeQuestTemplateEntry.notesType == ModelUtil.questionnaireNotesTypeYesOrNoOption) {

                            val optionYes = IntakeQuestTemplateOption(
                                option = "Yes",
                                entryId = intakeQuestTemplateEntry.entryId
                            )
                            val optionNo = IntakeQuestTemplateOption(
                                option = "No",
                                entryId = intakeQuestTemplateEntry.entryId
                            )

                            questionnaireEntryPOJO.questionnaireOptions =
                                arrayListOf(optionYes, optionNo)
                        } else {
                            questionnaireEntryPOJO.questionnaireOptions = optionsList
                        }
                        questionnaireEntryPOJO.questionnaireEntry = intakeQuestTemplateEntry



                        if (!intakeQuestTemplateEntry.isDeleted) {
                            questionnaireEntryPOJOList.add(questionnaireEntryPOJO)
                        }
                    }
                }

                questionnaireDetailPOJO.questionnairEntryPOJOList = questionnaireEntryPOJOList
            }


            /*    val questionnaireEntryPOJO = QuestionnaireEntryPOJO(questionnaireEntry = IntakeQuestTemplateEntry(notesType = ModelUtil.questionnaireNotesTypeSignature , notes = "Signature"))

                val questionnaireEntry0POJO = QuestionnaireEntryPOJO(questionnaireEntry = IntakeQuestTemplateEntry(notesType = ModelUtil.questionnaireNotesTypeMedication , notes = "Favorite date?") )
                val questionnaireEntry5POJO = QuestionnaireEntryPOJO(questionnaireEntry = IntakeQuestTemplateEntry(notesType = ModelUtil.questionnaireNotesTypeAllergy , notes = "Supplement") , questionnaireOptions = arrayListOf(IntakeQuestTemplateOption(option = "Drug Allergy")))

                val questionnaireEntry1POJO = QuestionnaireEntryPOJO(questionnaireEntry = IntakeQuestTemplateEntry(notesType = ModelUtil.questionnaireNotesTypeDate , notes = "Favorite date?"))
                val questionnaireEntry2POJO = QuestionnaireEntryPOJO(questionnaireEntry = IntakeQuestTemplateEntry(notesType = ModelUtil.questionnaireNotesTypeSimpleQuestion , notes = "Hiiiii how r u ??" ,answersList = arrayListOf("fyn")))
                val questionnaireEntry3POJO = QuestionnaireEntryPOJO(questionnaireEntry = IntakeQuestTemplateEntry(notesType = ModelUtil.questionnaireNotesTypeOption , notes = "Hiiiii how r u ??" , isMultiChoice = true,answersList = arrayListOf("fyn")) , questionnaireOptions = arrayListOf(IntakeQuestTemplateOption(option = "fyn") ,IntakeQuestTemplateOption(option = "OK") , IntakeQuestTemplateOption(option = "nah") ))

                val optionYes = IntakeQuestTemplateOption(option = "Yes" , entryId = questionnaireEntryPOJO.questionnaireEntry.entryId)
                val optionNo = IntakeQuestTemplateOption(option = "No" , entryId = questionnaireEntryPOJO.questionnaireEntry.entryId)

                val questionnaireEntry4POJO = QuestionnaireEntryPOJO(questionnaireEntry = IntakeQuestTemplateEntry(notesType = ModelUtil.questionnaireNotesTypeYesOrNoOption , notes = "Are you fine ??" ,answersList = arrayListOf("Yes")) , questionnaireOptions = arrayListOf(optionYes , optionNo) )

                questionnaireDetailPOJO.questionnairEntryPOJOList = arrayListOf(questionnaireEntryPOJO,questionnaireEntry0POJO, questionnaireEntry5POJO, questionnaireEntry1POJO , questionnaireEntry2POJO ,questionnaireEntry3POJO , questionnaireEntry4POJO)
              */  return questionnaireDetailPOJO
        }

        private fun getTableData(tableDataString: String): ArrayList<ArrayList<SOAPTemplateTableCell>> {
            val tableDataArr = tableDataString.parseStringAsJsonArray()
            val tableData: ArrayList<ArrayList<SOAPTemplateTableCell>> = arrayListOf()
            tableDataArr?.forEach { rowData ->
                val rowDataArr = rowData.asJsonArray
                val rowDataList: ArrayList<SOAPTemplateTableCell> = arrayListOf()
                rowDataArr.forEach { celldata ->
                    val cellJson = celldata.asJsonObject
                    val cellData = SOAPTemplateTableCell()
                    cellData.value = cellJson.getString("value") ?: ""
                    cellData.colspan = cellJson.getString("colspan") ?: ""
                    cellData.type = cellJson.getString("type") ?: ""
                    rowDataList.add(cellData)
                }
                tableData.add(rowDataList)
            }
            return tableData
        }

        fun formQuestionnaireDetailPOJO(respObj: JsonObject): QuestionnaireDetailPOJO {
            val questionnaireDetailPOJO = QuestionnaireDetailPOJO()

            return questionnaireDetailPOJO
        }

        fun formPrimaryInsuranceForQuestionnaire(respObj: JsonObject): PatientInsuranceDetails {
            val insurance = PatientInsuranceDetails()


            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {
                return formInsuranceObj(outputData)
            }


            return insurance
        }

        fun formPrimaryContactDetailForQuestionnaire(respObj: JsonObject): Patient {
            val patient = Patient()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                patient.caregiverFirstName = outputData.getString("CAREGIVER_FIRST_NAME")
                patient.caregiverLastName = outputData.getString("CAREGIVER_LAST_NAME")
                patient.emergencyContactName = outputData.getString("EMERGENCY_CONTACT_NAME")
                patient.emergencyContactNumber = outputData.getString("EMERGENCY_CONTACT_NUMBER")
                patient.emergencyContactExtn = outputData.getString("EMERGENCY_EXTN")
                patient.practicePatientId = outputData.getString("CAREGIVER_ID")

                val postalAddress = PostalAddress()
                postalAddress.city = outputData.getString("CITY")
                postalAddress.country = outputData.getString("COUNTRY")
                postalAddress.postalAddressId = outputData.getString("POSTALADDRESS_ID")
                postalAddress.line1 = outputData.getString("LINE_1")
                postalAddress.line2 = outputData.getString("LINE_2")
                postalAddress.postalCode = outputData.getString("POSTALCODE")
                postalAddress.postbox = outputData.getString("POSTBOX_NO")
                postalAddress.state = outputData.getString("STATE")


                val contactDetails = ContactDetails()
                contactDetails.emailId = outputData.getString("EMAILID")
                contactDetails.contactDetailsId = outputData.getString("CONTACTDETAILS_ID")
                contactDetails.fax = outputData.getString("FAX")
                contactDetails.homePhone = outputData.getString("LANDLINE")
                contactDetails.mobile = outputData.getString("MOBILE")
                contactDetails.workPhone = outputData.getString("WORKPHONE")
                contactDetails.workPhoneExtn = outputData.getString("WORKPHONE_EXTN")
                contactDetails.primaryPhone = outputData.getString("CONTACT_PREFERENCE")

                patient.contactDetails = contactDetails
                patient.postalAddress = postalAddress

            }

            return patient
        }

        fun formPrimaryDetailForQuestionnaire(respObj: JsonObject): Pair<Patient, ArrayList<String>?> {

            val patient = Patient()
            val requiredTabs = arrayListOf<String>()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                patient.bloodGroup = outputData.getString("BLOOD_GROUP")
                patient.emergencyContactName = outputData.getString("EMERGENCY_CONTACT_NAME")
                patient.emergencyContactNumber = outputData.getString("EMERGENCY_CONTACT_NUMBER")
                patient.emergencyContactExtn = outputData.getString("EMERGENCY_EXTN")
                patient.employmentStatus = outputData.getString("EMPLOYMENT_STATUS")
                patient.ethnicity = outputData.getString("ETHNICITY")
                patient.firstName = outputData.getString("FIRST_NAME")
                patient.gender = outputData.getString("GENDER")
                patient.genderIdentity = outputData.getString("GENDER_IDENTITY")
                patient.language = outputData.getString("LANGUAGE")
                patient.lastName = outputData.getString("LAST_NAME")
                patient.maritalStatus = outputData.getString("MARITAL_STATUS")
                patient.practicePatientId = outputData.getString("PRACTICE_PATIENT_ID")
                patient.race = outputData.getString("RACE")
                patient.smokingStatus = outputData.getString("SMOKING_STATUS")
                val dob = outputData.getString("DOB")
                if (!dob.isNullOrBlank()) {
                    patient.dateOfBirth = LocalDate.parse(dob, ModelUtil.charmDisplayDateFormat())
                }
                val requiredTabArr = outputData.getAsJsonArray("requiredTabs")
                if (requiredTabArr.isNotJsonNull()) {
                    requiredTabArr.forEach {
                        requiredTabs.add(it.asString)
                    }
                }
            }
            return Pair(patient, requiredTabs)
        }
        //endregion

        //region Flowsheet

        fun formFlowsheetList(respObj: JsonObject): ArrayList<Flowsheets> {
            val flowsheetList = arrayListOf<Flowsheets>()

            val outputData = respObj.getAsJsonArray("output_string")

            outputData?.forEach { flowsheetArrEle ->
                val flowsheetArrObj = flowsheetArrEle?.asJsonObject
                if (flowsheetArrObj != null) {

                    val flowsheetArr = flowsheetArrObj.getAsJsonArray("flowsheets")

                    val practiceId = flowsheetArrObj.getString("practice_id")

                    flowsheetArr?.forEach { flowsheetEle ->

                        val flowsheetObj = flowsheetEle?.asJsonObject
                        if (flowsheetObj != null) {

                            val flowsheet = formFlowsheetObj(flowsheetObj)
                            flowsheet.practiceId = practiceId
                            flowsheetList.add(flowsheet)

                        }
                    }
                }
            }


            return flowsheetList
        }

        fun formFlowsheetObj(flowsheetObj: JsonObject): Flowsheets {
            val flowsheets = Flowsheets()

            flowsheets.allowEdit = flowsheetObj.getBoolean("allow_patient_editing") ?: false
            flowsheets.fillFrequency = flowsheetObj.getString("fill_frequency")
            flowsheets.flowSheetId = flowsheetObj.getString("flowsheet_id")
            flowsheets.name = flowsheetObj.getString("flowsheet_name")

            val flowSheetParamsArr = flowsheetObj.getAsJsonArray("flowsheet_params")
            flowSheetParamsArr?.forEach { paramEle ->

                val paramObj = paramEle?.asJsonObject

                if (paramObj != null) {
                    val flowsheetEntries = FlowsheetEntries()
                    flowsheetEntries.flowSheetEntryId = paramObj.getString("flowsheet_entry_id")
                    flowsheetEntries.name = paramObj.getString("flowsheet_entry_name")
                    flowsheetEntries.type = paramObj.getString("flowsheet_entry_type")
                    flowsheetEntries.fieldType = paramObj.getString("field_type")
                    flowsheetEntries.option = paramObj.getString("field_options")
                    flowsheetEntries.addAdditionalNotes =
                        paramObj.getBoolean("add_additional_notes")
                            ?: false
                    flowsheetEntries.additionalNotesLabel =
                        paramObj.getString("additional_notes_label")
                    flowsheetEntries.unit = paramObj.getString("field_unit")
                    flowsheetEntries.position = paramObj.getInt("position")

                    flowsheets.flowsheetEntries.add(flowsheetEntries)
                }
            }

            flowsheets.toString = flowsheetObj.toString()

            return flowsheets
        }

        fun formFlowsheetEntryList(respObj: JsonObject): ArrayList<PHRFlowsheets> {
            val flowsheetList = arrayListOf<PHRFlowsheets>()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                val flowsheetData = outputData.getAsJsonArray("flowsheet_data")
                flowsheetData?.forEach { flowsheetEle ->
                    val flowsheetObj = flowsheetEle?.asJsonObject
                    if (flowsheetObj != null) {
                        flowsheetList.add(formFlowsheetEntryObj(flowsheetObj))
                    }
                }

            }
            return flowsheetList
        }

        fun formFlowsheetEntryObj(flowsheetObj: JsonObject): PHRFlowsheets {
            val flowsheets = PHRFlowsheets()

            val date = flowsheetObj.getString("date")
            if (!date.isNullOrBlank()) {
                flowsheets.date = DateTime.parse(date, ModelUtil.charmFlowsheetDataDateFormatter())
            }

            flowsheets.phrFlowsheetId = flowsheetObj.getString("flowsheet_data_id")


            val flowSheetParamsArr = flowsheetObj.getAsJsonArray("data_entries")
            flowSheetParamsArr?.forEach { paramEle ->

                val paramObj = paramEle?.asJsonObject

                if (paramObj != null) {
                    val flowsheetEntries = FlowsheetEntries()
                    flowsheetEntries.flowSheetEntryId = paramObj.getString("flowsheet_entry_id")
                    flowsheetEntries.name = paramObj.getString("flowsheet_entry_name")
                    flowsheetEntries.type = paramObj.getString("flowsheet_entry_type")
                    flowsheetEntries.fieldType = paramObj.getString("field_type")

                    flowsheetEntries.value = paramObj.getString("field_value")
                    flowsheetEntries.unit = paramObj.getString("field_unit")
                    flowsheetEntries.comments = paramObj.getString("comments")
                    flowsheetEntries.position = paramObj.getInt("position")

                    flowsheets.flowsheetEntries.add(flowsheetEntries)
                }
            }

            flowsheets.toString = flowsheetObj.toString()
            return flowsheets
        }

        //endregion

        //region Message

        fun formMessageList(
            respObj: JsonObject,
            messageType: String,
            practiceId: String
        ): ArrayList<Messages> {

            val messageList = ArrayList<Messages>()

            val messageArr = respObj.getAsJsonArray("output_string")

            messageArr?.forEach { messageEle ->
                val messageObj = messageEle.asJsonObject

                val messages = Messages()
                messages.practicePatientId = messageObj.getString("PRACTICE_PATIENT_ID")

                val messageDateStr = messageObj.getString("MESSAGE_DATE") ?: ""
                val messageTimeStr = messageObj.getString("MESSAGE_TIME") ?: ""

                messages.messageDate = DateTime.parse(
                    "$messageDateStr $messageTimeStr",
                    ModelUtil.charmDateFormatForMessageList()
                )
                messages.priority = messageObj.getString("PRIORITY")
                messages.messageId = messageObj.getString("MESSAGE_ID")
                messages.subject = messageObj.getString("SUBJECT")
                messages.type = messageObj.getString("TYPE")
                messages.hasAttachments = messageObj.getBoolean("HAS_ATTACHMENTS")

                messages.practiceId = practiceId

                val messageReceipients = MessageReceipients()
                messageReceipients.toSpace = messageObj.getString("TO_SPACE")
                messageReceipients.fromSpace = messageObj.getString("FROM_SPACE")
                messageReceipients.fromName = messageObj.getString("FROM_NAME")
                messageReceipients.fromId = messageObj.getString("FROM_ID")
                messageReceipients.status = messageObj.getInt("STATUS")
                    ?: ModelUtil.messageStatusUnSeen
                messageReceipients.toName = messageObj.getString("TO_NAME")
                messageReceipients.messageId = messageObj.getString("MESSAGE_ID")
                messageReceipients.messageRecipientId = messageObj.getString("MESSAGE_RECIPIENT_ID")
                messageReceipients.toId = messageObj.getString("TO_ID")


                if (messageType == ModelUtil.messageTypeSent) {
                    messages.practiceId = messageReceipients.toSpace
                } else {
                    messages.practiceId = messageReceipients.fromSpace
                }

                messages.messageRecipient = messageReceipients

                messageList.add(messages)
            }

            return messageList
        }

        fun formMessageDetail(respObj: JsonObject, patientId: String): Messages? {


            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                val messages = Messages()

                messages.messageId = outputData.getString("message_id")
                messages.messageDate = UIUtil.convertToDateTimeWithoutFormator(outputData.getString("date") ?: "")
                messages.subject = outputData.getString("subject")
                messages.content = outputData.getString("content")
                messages.type = outputData.getString("type")
                messages.ownerId = outputData.getString("owner_id")

                val senderObj = outputData.getAsJsonObject("sender")

                val messageReceipients = MessageReceipients()
                messageReceipients.fromName = senderObj.getString("sender_name")
                messageReceipients.fromId = senderObj.getString("sender_id")

                val attachmentArr = outputData.getAsJsonArray("attachments")

                val attachmentsMapList = ArrayList<MessagesAttachmentsMap>()
                attachmentArr?.forEach { attachmentMapEle ->
                    val attachmentObj = attachmentMapEle.asJsonObject
                    val attachmentsMap = MessagesAttachmentsMap()
                    attachmentsMap.attachmentId = attachmentObj.getString("attachment_id")
                    attachmentsMap.attachmentName = attachmentObj.getString("attachment_name")
                    attachmentsMap.messageId = messages.messageId
                    attachmentsMapList.add(attachmentsMap)
                }

                val receiverObj = outputData.getAsJsonObject("receivers")
                val patientArr = receiverObj?.getAsJsonArray("patients")

                val patientList = ArrayList<Patient>()

                patientArr?.forEach { patientEle ->

                    val patientObj = patientEle.asJsonObject
                    val patient = Patient()

                    patient.practicePatientId = patientObj.getString("patient_id")
                    patient.fullName = patientObj.getString("patient_name")

                    patientList.add(patient)
                }

                val memberArr = receiverObj?.getAsJsonArray("members")

                val messageMemberMap = ArrayList<MessageMembersMap>()

                memberArr?.forEach { memberEle ->
                    val memberObj = memberEle.asJsonObject
                    val messageMember = MessageMembersMap()
                    val member = Member()

                    member.memberId = memberObj.getString("member_id")
                    member.fullName = memberObj.getString("member_name")

                    messageMember.additionalMemberId = member.memberId
                    messageMember.messageId = messages.messageId
                    messageMember.member = member

                    if (!messageMemberMap.any { it.member?.memberId == member.memberId }) {
                        messageMemberMap.add(messageMember)
                    }
                }


                if (messageReceipients.fromId != patientId) {
                    if (messages.type == ModelUtil.messageTypeGroup) {
                        val groupMap = MessageGroupMap()
                        val messageGroup = MessageGroup()
                        messageGroup.groupId = messageReceipients.fromId
                        messageGroup.groupName = messageReceipients.fromName
                        groupMap.group = messageGroup
                        groupMap.groupId = messageReceipients.fromId
                        groupMap.messageId = messages.messageId
                        messages.messageGroupMapList.add(groupMap)
                    } else {
                        val messageMember = MessageMembersMap()
                        val member = Member()

                        member.memberId = messageReceipients.fromId
                        member.fullName = messageReceipients.fromName

                        messageMember.additionalMemberId = member.memberId
                        messageMember.messageId = messages.messageId
                        messageMember.member = member

                        messageMember.isReceiver = false

                        messageMemberMap.add(messageMember)
                    }

                }

                messages.messageRecipient = messageReceipients
                messages.messagePatientList = patientList
                messages.messageMembersMapList = messageMemberMap
                messages.messagesAttachmentsMapList = attachmentsMapList
                messages.toString = respObj.toString()

                return messages
            }
            return null
        }

        fun formMemberListForMessage(respObj: JsonObject, practiceId: String): ArrayList<Member> {

            val memberList = arrayListOf<Member>()

            val facilityMemberArr = respObj.getAsJsonArray("facility_members")
            facilityMemberArr?.forEach { facilityMemberEle ->
                val facilityMemberObj = facilityMemberEle.asJsonObject

                if (facilityMemberObj != null) {
                    val member = Member()
                    member.firstName = facilityMemberObj.getString("first_name")
                    member.lastName = facilityMemberObj.getString("last_name")
                    member.fullName = facilityMemberObj.getString("full_name")
                    member.gender = facilityMemberObj.getString("gender")
                    member.prefix = facilityMemberObj.getString("prefix")
                    member.specialization = facilityMemberObj.getString("specialization")
                    member.memberId = facilityMemberObj.getString("member_id")
                    member.degree = facilityMemberObj.getString("degree")

                    member.practiceId = practiceId

                    memberList.add(member)
                }
            }

            return memberList
        }

        fun parseGroupAndMemberList(
            respObj: JsonObject,
            practiceId: String
        ): Pair<List<MessageGroup>, List<Member>> {
            val groupList = mutableListOf<MessageGroup>()
            val memberList = mutableListOf<Member>()
            val outputString = respObj.getAsJsonObject("output_string")
            val groupArr = outputString?.getAsJsonArray("group_list")
            groupArr?.forEach { groupEle ->
                val groupObj = groupEle?.asJsonObject
                if (groupObj != null) {
                    val group = MessageGroup(
                        groupName = groupObj.getString("GROUP_NAME") ?: "",
                        groupDesc = groupObj.getString("GROUP_DESC") ?: "",
                        type = groupObj.getString("TYPE") ?: "",
                        groupId = groupObj.getString("GROUP_ID") ?: ""
                    )
                    groupList.add(group)
                }
            }
            val memberArr = outputString?.getAsJsonArray("physician_list")
            memberArr?.forEach { memberEle ->
                val memberObj = memberEle?.asJsonObject
                if (memberObj != null) {
                    val member = Member()
                    member.fullName = memberObj.getString("MEMBER_NAME")
                    member.memberId = memberObj.getString("MEMBER_ID")
                    member.prefix = memberObj.getString("PREFIX")
                    member.degree = memberObj.getString("DEGREE")
                    member.specialization = memberObj.getString("SPECIALIZATION")
                    member.gender = memberObj.getString("GENDER")
                    member.firstName = memberObj.getString("FIRST_NAME")
                    member.lastName = memberObj.getString("LAST_NAME")
                    member.photoURI = memberObj.getString("PROFILE_PHOTO")
                    member.nameWithTitle = memberObj.getString("MEMBER_NAME_WITH_TITLE")
                    member.practiceId = practiceId
                    memberList.add(member)
                }
            }
            return Pair(groupList, memberList)
        }


        fun formComposeMsgRestriction(
            respObj: JsonObject,
            practiceId: String
        ): ComposeMsgRestriction {

            val composeMsgRestriction = ComposeMsgRestriction()

            composeMsgRestriction.practiceId = practiceId

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                composeMsgRestriction.allowPHRMessaging = outputData.getBoolean("allow_phr_messaging") ?: true
                composeMsgRestriction.isMessageRestrict = outputData.getBoolean("is_message_restrict") ?: false
                composeMsgRestriction.noOfCharsAllowed = outputData.getInt("no_of_chars_allowed") ?: 0
            }

            return composeMsgRestriction
        }

        fun getAttachmentId(respObj: JsonObject): String {

            val attachmentObj = respObj.getAsJsonObject("attachment")

            if (attachmentObj != null) {

                return attachmentObj.getString("attachment_id") ?: ""
            }

            return ""
        }

        //endregion

        //region Lab

        fun formLabResultList(respObj: JsonObject): ArrayList<MedicalRecordGroup> {
            val medicalRecordGroupList: ArrayList<MedicalRecordGroup> = arrayListOf()

            val outputData = respObj.getAsJsonObject("output_string")
            if (outputData != null) {
                val medicalRecordGroupArr = outputData.getAsJsonArray("DATA_LIST")

                medicalRecordGroupArr?.forEach { medicalRecordGroupEle ->
                    val medicalRecordGroupObj = medicalRecordGroupEle?.asJsonObject
                    if (medicalRecordGroupObj != null) {

                        val isAddedByPatient = medicalRecordGroupObj.getBoolean("ADDED_BY_PATIENT")
                        val groupId = medicalRecordGroupObj.getString("group_id")
                        val resultDateStr = medicalRecordGroupObj.getString("result_date")
                        val resultDate = if (!resultDateStr.isNullOrBlank()) {
                            DateTime.parse(
                                resultDateStr,
                                ModelUtil.sqlTimeStampForLabResultDateFormattor()
                            )
                        } else {
                            null
                        }

                        val medicalRecordGroup = MedicalRecordGroup()
                        medicalRecordGroup.labName = medicalRecordGroupObj.getString("lab_name")
                        medicalRecordGroup.groupId = groupId

                        val medicalRecordEntryList: ArrayList<MedicalRecordEntry> = arrayListOf()

                        val medicalRecordEntryArr = medicalRecordGroupObj.getAsJsonArray("records")

                        medicalRecordEntryArr?.forEach { medicalRecordEntryEle ->
                            val medicalRecordEntryObj = medicalRecordEntryEle?.asJsonObject
                            if (medicalRecordEntryObj != null) {

                                val medicalRecordEntry = MedicalRecordEntry()


                                medicalRecordEntry.interpretation =
                                    medicalRecordEntryObj.getString("interpretation")
                                        ?: ""
                                medicalRecordEntry.isImportedByPatient = isAddedByPatient

                                val medicalRecord = MedicalRecords()
                                medicalRecord.medicalRecordName =
                                    medicalRecordEntryObj.getString("record_name")
                                        ?: ""

                                medicalRecordEntry.medicalRecord = medicalRecord
                                medicalRecordEntry.date = resultDate
                                medicalRecordEntryList.add(medicalRecordEntry)
                            }
                        }

                        medicalRecordGroup.medicalRecordEntryList = medicalRecordEntryList

                        medicalRecordGroupList.add(medicalRecordGroup)
                    }
                }
            }
            return medicalRecordGroupList
        }

        fun formLabResultDetail(respObj: JsonObject): MedicalRecordGroup {
            val medicalRecordGroup = MedicalRecordGroup()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {
                medicalRecordGroup.reportStatus = outputData.getString("RESULT_STATUS")
                medicalRecordGroup.groupId = outputData.getString("group_id")
                medicalRecordGroup.labId = outputData.getString("lab_id")
                medicalRecordGroup.labName = outputData.getString("lab_name")

                val resultDateStr = outputData.getString("result_date") ?: ""

                val resultDate = if (resultDateStr.isNotBlank()) {
                    DateTime.parse(resultDateStr, ModelUtil.sqlTimeStampForLabResultDateFormattor())
                } else {
                    null
                }


                val medicalRecordEntries = ArrayList<MedicalRecordEntry>()

                val medicalRecordEntryArr = outputData.getAsJsonArray("records")

                medicalRecordEntryArr?.forEach { medicalRecordEntryEle ->

                    val medicalRecordEntryObj = medicalRecordEntryEle.asJsonObject
                    if (medicalRecordEntryObj != null) {
                        val medicalRecordEntry = MedicalRecordEntry()

                        medicalRecordEntry.medicalRecordEntryId =
                            medicalRecordEntryObj.getString("entry_id")
                        medicalRecordEntry.isPDFAvailable =
                            medicalRecordEntryObj.getBoolean("is_pdf_avail")
                                ?: false

                        val interpretation = medicalRecordEntryObj.getString("interpretation") ?: ""

                        medicalRecordEntry.isAbnormalResult =
                            interpretation.equals(ModelUtil.labResultsIntepretationAbnormal, true)

                        val medicalRecord = MedicalRecords()
                        medicalRecord.medicalRecordName =
                            medicalRecordEntryObj.getString("record_name")
                                ?: ""
                        medicalRecord.medicalRecordId = medicalRecordEntryObj.getString("record_id")
                            ?: ""

                        medicalRecordEntry.medicalRecord = medicalRecord
                        medicalRecordEntry.date = resultDate
                        medicalRecordEntry.isViewOpened = true


                        val medicalRecordParamArr =
                            medicalRecordEntryObj.getAsJsonArray("parameters")

                        val medicalRecordParamValueMapList = ArrayList<MedicalRecordParamValueMap>()

                        medicalRecordParamArr?.forEach { medicalRecordParamEle ->

                            val medicalRecordParamObj = medicalRecordParamEle?.asJsonObject
                            if (medicalRecordParamObj != null) {
                                val mrParamValueMap = MedicalRecordParamValueMap()

                                val medicalRecordValue = MedicalRecordValues()

                                val medicalRecordParam = MedicalRecordParams()

                                medicalRecordParam.medicalRecordParamId =
                                    medicalRecordParamObj.getString("param_id")
                                medicalRecordParam.parameterName =
                                    medicalRecordParamObj.getString("param_name")
                                medicalRecordParam.recordunit =
                                    medicalRecordParamObj.getString("unit")
                                        ?: ""

                                medicalRecordValue.interpretation =
                                    medicalRecordParamObj.getString("interpretation")
                                medicalRecordValue.referenceMax =
                                    medicalRecordParamObj.getString("ref_max")
                                medicalRecordValue.referenceMin =
                                    medicalRecordParamObj.getString("ref_min")
                                medicalRecordValue.recordvalue =
                                    medicalRecordParamObj.getString("param_value")

                                medicalRecordValue.range = medicalRecordParamObj.getString("range")
                                medicalRecordValue.medicalRecordParamId

                                mrParamValueMap.medicalRecordParam = medicalRecordParam
                                mrParamValueMap.medicalRecordValue = medicalRecordValue

                                medicalRecordParamValueMapList.add(mrParamValueMap)
                            }
                        }

                        medicalRecordEntry.mrParamValueMapList = medicalRecordParamValueMapList

                        medicalRecordEntries.add(medicalRecordEntry)
                    }
                }

                medicalRecordGroup.medicalRecordEntryList = medicalRecordEntries

                val messageToPatientObj = outputData.getString("message_to_patient")
                val reviewerName = outputData.getString("reviewer_name")

                if (messageToPatientObj != null || !reviewerName.isNullOrBlank()) {
                    val reviewMedicalRecordEntries = ReviewMedicalRecordEntries()
                    reviewMedicalRecordEntries.messageToPatient = messageToPatientObj
                    reviewMedicalRecordEntries.reviewerName = reviewerName
                    medicalRecordGroup.medicalRecordEntryList.firstOrNull()?.reviewMedicalRecordEntry =
                        reviewMedicalRecordEntries
                }

            }
            return medicalRecordGroup
        }


        fun formLabOrderList(respObj: JsonObject): ArrayList<LabOrderDetail> {

            val outputJsonObj = respObj.getJsonObject("output_string")
            val practiceIdVsMemberObj = outputJsonObj?.getJsonObject("practice_id_vs_mem_obj")
            val labOrderListArray = outputJsonObj?.getJsonArray("lab_order_list")

            val labOrderArrayList = ArrayList<LabOrderDetail>()
            labOrderListArray?.forEach {
                val labOrderDetail = getLabOrderDetail(it as JsonObject, practiceIdVsMemberObj)
                labOrderArrayList.add(labOrderDetail)
            }

            return labOrderArrayList

        }

        fun formLabParamHistory(respObj: JsonObject): LabParamHistoryPOJO {
            val labParamHistoryPOJO = LabParamHistoryPOJO()
            val outputJsonObj = respObj.getJsonObject("output_string")
            labParamHistoryPOJO.paramName = outputJsonObj?.getString("PARAM_NAME")
            labParamHistoryPOJO.paramUnit = outputJsonObj?.getString("UNIT")
            val historyJson = outputJsonObj?.getAsJsonObject("PARAM_HISTORY")
            labParamHistoryPOJO.isStringValue = historyJson?.getBoolean("IS_STRING_VALUE")
            val historyArray = historyJson?.getJsonArray("HISTORY")
            val paramHistoryEntryList: ArrayList<ParamHistoryEntryPOJO> = ArrayList()
            historyArray?.forEach {
                val historyEntryJson = it as JsonObject
                val paramHistoryEntryPOJO = ParamHistoryEntryPOJO()
                paramHistoryEntryPOJO.value = historyEntryJson.getString("VALUE")
                paramHistoryEntryPOJO.date = DateTime.parse(
                    historyEntryJson.getString("DATE"),
                    ModelUtil.genericDateDisplayFormattor()
                )
                paramHistoryEntryPOJO.note = historyEntryJson.getString("NOTE")
                paramHistoryEntryList.add(paramHistoryEntryPOJO)
            }
            labParamHistoryPOJO.paramHistoryEntryList.addAll(paramHistoryEntryList)
            return labParamHistoryPOJO
        }

        private fun getLabOrderDetail(
            jsonObject: JsonObject,
            practiceIdVsMemberObj: JsonObject?
        ): LabOrderDetail {

            val labOrderDetail = LabOrderDetail()

            labOrderDetail.fileSize = jsonObject.getString("FILE_SIZE")
            labOrderDetail.isDeleted = jsonObject.getString("IS_DELETED")
            labOrderDetail.practiceId = jsonObject.getString("PRACTICE_ID")
            labOrderDetail.senderId = jsonObject.getString("SENDER_ID")
            labOrderDetail.sharedDate = jsonObject.getString("SHARED_DATE")
            labOrderDetail.ehrFileEntryId = jsonObject.getString("EHR_FILE_ENTRY_ID")
            labOrderDetail.receiverId = jsonObject.getString("RECEIVER_ID")
            labOrderDetail.fileId = jsonObject.getString("FILE_ID")
            labOrderDetail.fileName = jsonObject.getString("FILE_NAME")
            labOrderDetail.uploadedBy = jsonObject.getString("UPLOADED_BY")
            val dateStr = jsonObject.getString("DATE") ?: ""

            if (dateStr.isNotBlank()) {
                labOrderDetail.date =
                    UIUtil.formatDate(
                        dateStr,
                        ModelUtil.charmApiDateFormat()
                    )
            }
            if (jsonObject.getString("READ_STATUS") == "true") {
                labOrderDetail.readStatus = true
            }
            labOrderDetail.timeOfUpload = jsonObject.getString("TIME_OF_UPLOAD")
            labOrderDetail.fileType = jsonObject.getString("FILE_TYPE")
            labOrderDetail.messageToPatient = jsonObject.getString("MESSAGE_TO_PATIENT")
            labOrderDetail.phrFileMapId = jsonObject.getString("PHR_FILE_MAP_ID")
            if (practiceIdVsMemberObj != null) {
                val practiceIdMemberObj =
                    practiceIdVsMemberObj.getJsonObject(labOrderDetail.practiceId ?: "")
                val memberObj = practiceIdMemberObj?.getJsonObject(labOrderDetail.uploadedBy ?: "")
                if (memberObj != null) {
                    val member = Member()
                    member.memberId = labOrderDetail.uploadedBy
                    member.fullName = memberObj.getString("MEMBER_NAME")
                    member.prefix = memberObj.getString("PREFIX")
                    member.degree = memberObj.getString("DEGREE")
                    member.specialization = memberObj.getString("SPECIALIZATION")
                    labOrderDetail.member = member
                }
            }

            return labOrderDetail

        }

        //endregion

        //region Allergy

        fun formPatientAllergyList(respObj: JsonObject, status: String): ArrayList<PatientAllergy> {
            val patientAllergyList = arrayListOf<PatientAllergy>()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {
                val patientAllergyArr = outputData.getAsJsonArray("DATA_LIST")

                patientAllergyArr?.forEach { patientAllergyEle ->

                    val patientAllergyObj = patientAllergyEle?.asJsonObject

                    if (patientAllergyObj != null) {

                        val patientAllergy = formPatientAllergyObj(patientAllergyObj, status)
                        patientAllergyList.add(patientAllergy)

                    }

                }
            }

            return patientAllergyList
        }


        fun formPatientAllergyObj(patientAllergyObj: JsonObject, status: String): PatientAllergy {
            val patientAllergy = PatientAllergy()
            patientAllergy.addedByPatient = patientAllergyObj.getBoolean("ADDED_BY_PATIENT")
                ?: false
            patientAllergy.allergyMapId = patientAllergyObj.getString("ALLERGY_MAP_ID") ?: ""

            val observedDateEle = patientAllergyObj.getString("OBSERVED_DATE")
            if (observedDateEle != null)
                patientAllergy.observedDate = DateTime.parse(
                    observedDateEle,
                    ModelUtil.charmDisplayDateFormat()
                )
            val resolvedDateEle = patientAllergyObj.getString("RESOLVED_DATE")
            if (resolvedDateEle != null) {
                patientAllergy.resolvedDate = DateTime.parse(
                    resolvedDateEle,
                    ModelUtil.charmDisplayDateFormat()
                )
            }


            patientAllergy.reactions = patientAllergyObj.getString("REACTIONS")

            patientAllergy.severity = patientAllergyObj.getString("SEVERITY") ?: ""
            patientAllergy.status = status

            val allergy = Allergy()
            allergy.allergy = patientAllergyObj.getString("ALLERGY") ?: ""
            allergy.allergyId = patientAllergyObj.getString("ALLERGY_ID") ?: ""
            allergy.allergyType = patientAllergyObj.getString("ALLERGY_TYPE") ?: ""

            patientAllergy.allergy = allergy

            patientAllergy.toString = patientAllergyObj.toString()

            return patientAllergy
        }

        fun formPatientAllergyCommonData(respObj: JsonObject): PatientAllergyCommonData {

            val patAllergyCommonData = PatientAllergyCommonData()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {
                val allergySeverityArr = outputData.getAsJsonArray("ALLERGY_SEVERITY")
                val allergySeverityList = arrayListOf<String>()

                allergySeverityArr?.forEach {
                    allergySeverityList.add(it.asString)
                }

                val allergyStatusArr = outputData.getAsJsonArray("ALLERGY_STATUS")
                val allergyStatusList = arrayListOf<String>()

                allergyStatusArr?.forEach {
                    allergyStatusList.add(it.asString)
                }

                val allergyTypeArr = outputData.getAsJsonArray("ALLERGY_TYPE")
                val allergyTypeList = arrayListOf<String>()

                allergyTypeArr?.forEach {
                    allergyTypeList.add(it.asString)
                }

                patAllergyCommonData.allergySeverityList = allergySeverityList
                patAllergyCommonData.allergyStatusList = allergyStatusList
                patAllergyCommonData.allergyTypeList = allergyTypeList

            }

            return patAllergyCommonData
        }

        //endregion

        //region Announcements

        fun formAnnouncementList(respObj: JsonObject): ArrayList<Announcements> {

            val announcementList = arrayListOf<Announcements>()

            val announcementListObj = respObj.getAsJsonObject("output_string")

            if (announcementListObj != null) {

                val annoucementSet = announcementListObj.entrySet().iterator()

                annoucementSet.forEach {
                    val practiceAnnouncementId = it.key

                    val announcementObj = it.value?.asJsonObject

                    if (announcementObj != null) {

                        val announcements = Announcements()
                        announcements.announcementId = announcementObj.getString("ANNOUNCEMENT_ID")
                        announcements.content = announcementObj.getString("CONTENT")
                        announcements.isDeleted = announcementObj.getBoolean("IS_DELETED")
                        announcements.isRead = announcementObj.getBoolean("IS_READ") ?: false
                        announcements.practiceAnnouncementId = practiceAnnouncementId
                        announcements.practiceId = announcementObj.getString("PRACTICE_ID")
                        announcements.senderId = announcementObj.getString("SENDER_ID")
                        announcements.status = announcementObj.getString("STATUS")
                        announcements.subject = announcementObj.getString("SUBJECT")
                        announcements.time = DateTime.parse(
                            announcementObj.getString("TIME"),
                            ModelUtil.sqlTimeStampForAnnouncementDateFormattor()
                        )

                        if (announcements.isDeleted != true) {
                            announcementList.add(announcements)
                        }
                    }
                }
            }


            return announcementList
        }


        fun formAnnouncementDetailStr(respObj: JsonObject): String {

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {
                return outputData.getString("ANNOUNCEMENT_CONTENT") ?: ""
            }


            return ""
        }

        //endregion

        //region About

        fun formUsefulInfoList(respObj: JsonObject): ArrayList<UsefulInfo> {

            val usefulInfoList = arrayListOf<UsefulInfo>()

            val outputData = respObj.getAsJsonObject("output_string")


            if (outputData != null) {

                val practiceId = outputData.getString("PRACTICE_ID")

                val usefulInfoArr = outputData.getAsJsonArray("USEFULINFO_LIST")

                usefulInfoArr?.forEach { usefulInfoEle ->

                    val usefulInfoObj = usefulInfoEle?.asJsonObject
                    if (usefulInfoObj != null) {
                        val usefulInfo = UsefulInfo()
                        usefulInfo.infoId = usefulInfoObj.getString("Info_id")
                        val timeStr = usefulInfoObj.getString("Time")?.split(" ")
                        if (timeStr != null) {
                            usefulInfo.time =
                                DateTime.parse(timeStr[0], ModelUtil.charmApiDateFormat())
                        }
                        usefulInfo.category = usefulInfoObj.getString("Category")

                        usefulInfo.practiceId = practiceId
                        usefulInfoList.add(usefulInfo)

                    }
                }

            }

            return usefulInfoList
        }

        fun formAboutDetail(respObj: JsonObject): Pair<ArrayList<Facility>, ArrayList<Member>> {
            val facilityList = arrayListOf<Facility>()
            val memberList = arrayListOf<Member>()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                val practiceId = outputData.getString("PRACTICE_ID")


                val facilityArr = outputData.getAsJsonArray("FACILITIES_LIST")

                facilityArr?.forEach { facilityEle ->

                    val facilityObj = facilityEle?.asJsonObject
                    if (facilityObj != null) {

                        val facility = Facility()

                        facility.facilityName = facilityObj.getString("FACILITY_NAME")
                        facility.facilityId = facilityObj.getString("FACILITY_ID")
                        facility.directions = facilityObj.getString("DIRECTION")

                        val postalAddress = PostalAddress()

                        val line1 = facilityObj.getString("LINE_1") ?: ""
                        if (line1.isNotBlank() && !line1.equals("null", true)) {
                            postalAddress.line1 = line1
                        }

                        val line2 = facilityObj.getString("LINE_2") ?: ""
                        if (line2.isNotBlank() && !line2.equals("null", true)) {
                            postalAddress.line2 = line2
                        }

                        val contactDetails = ContactDetails()
                        val landline = facilityObj.getString("LANDLINE") ?: ""
                        if (landline.isNotBlank() && !landline.equals("null", true)) {
                            contactDetails.landline = landline
                        }
                        facility.postalAddress = postalAddress
                        facility.contactDetails = contactDetails

                        facility.practiceId = practiceId

                        facilityList.add(facility)

                    }
                }

                val providerArr = outputData.getAsJsonArray("PROVIDERS_LIST")

                providerArr?.forEach { providerEle ->

                    val providerObj = providerEle?.asJsonObject

                    if (providerObj != null) {

                        val member = Member()

                        member.fullName = providerObj.getString("FULL_NAME")
                        member.prefix = providerObj.getString("PREFIX")

                        val degree = providerObj.getString("DEGREE") ?: ""
                        if (degree.isNotBlank() && !degree.equals("null", true)) {
                            member.degree = degree
                        }


                        val specialization = providerObj.getString("SPECIALIZATION") ?: ""
                        if (specialization.isNotBlank() && !specialization.equals("null", true)) {
                            member.specialization = specialization
                        }

                        member.practiceId = practiceId

                        memberList.add(member)

                    }
                }
            }



            return Pair(facilityList, memberList)
        }

        fun formUsefulInfoDetailStr(respObj: JsonObject): String {

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {
                return outputData.getString("DTAILED_INFO") ?: ""
            }

            return ""
        }

        //endregion

        //region misc

        fun formUnreadCountObj(respObj: JsonObject): UnreadCount {

            val unreadCount = UnreadCount()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                unreadCount.billingCount = outputData.getInt("BILLING_COUNT") ?: 0
                unreadCount.labUnreadCount = outputData.getInt("LAB_UNREAD_COUNT") ?: 0
                unreadCount.messageUnreadCount = outputData.getInt("MESSAGE_UNREAD_COUNT") ?: 0
                unreadCount.practiceAnnouncementCount =
                    outputData.getInt("PRACTICE_ANNOUNCEMENT_COUNT")
                        ?: 0
                unreadCount.questionnaireUnfilledCount = outputData.getInt("QUES_UNFILLED_COUNT")
                    ?: 0
                unreadCount.documentUnreadCount = outputData.getInt("SHARED_FILES_UNREAD_COUNT")
                    ?: 0
                unreadCount.upcomingAppointmentCount =
                    outputData.getInt("UPCOMING_APPOINTMENT_COUNT")
                        ?: 0
                unreadCount.visitSummaryUnreadCount =
                    outputData.getInt("VISIT_SUMMARY_UNREAD_COUNT")
                        ?: 0

            }

            return unreadCount
        }

        fun formMenuListObjFromWeb(respObj: JsonObject): MenuList {

            val menuList = MenuList()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                val configuredMenuList = outputData.getAsJsonObject("CONFIGURED_MENU_LIST")
                if (configuredMenuList != null) {

                    menuList.appointment = (configuredMenuList.getInt("APPOINTMENTS_ORDER")
                        ?: 1) == 1
                    menuList.billing = (configuredMenuList.getInt("BILLING_ORDER") ?: 1) == 1
                    menuList.message = (configuredMenuList.getInt("MEESAGES_ORDER") ?: 1) == 1
                    menuList.lab = (configuredMenuList.getInt("LAB_ORDER") ?: 1) == 1
                    menuList.prescription = (configuredMenuList.getInt("RX_ORDER") ?: 1) == 1
                    menuList.visitSummary = (configuredMenuList.getInt("VISIT_SUMMARY_ORDER")
                        ?: 1) == 1
                    menuList.announcement = (configuredMenuList.getInt("ANNOUNCEMENT_ORDER")
                        ?: 1) == 1
                    menuList.usefulInfo = (configuredMenuList.getInt("USEFUL_INFO_ORDER") ?: 1) == 1
                    menuList.about = (configuredMenuList.getInt("ABOUT_ORDER") ?: 1) == 1

                }
            }

            return menuList
        }


        fun formMenuListObj(respString: String): MenuList {

            val menuList = MenuList()

            try {

                val respObj = respString.parseStringAsJsonObject()

                if (respObj != null) {

                    menuList.appointment = respObj.getBoolean("appointment") ?: true
                    menuList.billing = respObj.getBoolean("billing") ?: true
                    menuList.message = respObj.getBoolean("message") ?: true
                    menuList.lab = respObj.getBoolean("lab") ?: true
                    menuList.prescription = respObj.getBoolean("prescription") ?: true
                    menuList.visitSummary = respObj.getBoolean("visitSummary") ?: true
                    menuList.usefulInfo = respObj.getBoolean("usefulInfo") ?: true
                    menuList.announcement = respObj.getBoolean("announcement") ?: true
                    menuList.about = respObj.getBoolean("about") ?: true
                }
            } catch (e: Exception) {
                Log.d("menuListException", e.toString())
            }

            return menuList
        }


        fun formFacilityObj(facilityObj: JsonObject?, facilityId: String = ""): Facility? {
            if (facilityObj != null) {

                val facility = Facility()
                facility.facilityId = if (facilityId.isBlank()) {
                    facilityObj.getString("FACILITY_ID")
                } else {
                    facilityId
                }
                facility.facilityName = facilityObj.getString("FACILITY_NAME")

                return facility
            }

            return null
        }

        fun formMemberObj(memberObj: JsonObject?, memberId: String): Member? {
            if (memberObj != null) {

                val member = Member()
                member.memberId = memberId
                member.fullName = memberObj.getString("MEMBER_NAME")
                member.degree = memberObj.getString("DEGREE")
                member.specialization = memberObj.getString("SPECIALIZATION")
                member.prefix = memberObj.getString("PREFIX")

                return member
            }

            return null
        }

        fun formHealthPassportURLObj(respObj: JsonObject): HealthPassportModel {

            val healthPassport = HealthPassportModel()
            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {
                healthPassport.url = outputData.getString("health_url")
                healthPassport.qrCode = outputData.getString("url_qr")
                healthPassport.passcode = outputData.getString("passcode")
            }

            return healthPassport
        }

        //endregion

        //region Visit Summary

        fun formVisitSummaryList(
            authManager: AuthManager,
            respObj: JsonObject
        ): ArrayList<PatientEncounterList> {
            val visitSummaryList = arrayListOf<PatientEncounterList>()

            val outputString = respObj.getAsJsonObject("output_string")

            val enabledPracticeList =
                ModelUtil.getEnabledPractices(authManager, ModelUtil.visitSummaryModelId)
            if (outputString != null) {

                val consultationArr = outputString.getAsJsonArray("PAST_ENCOUNTER_LIST")
                val facilityMap = outputString.getAsJsonObject("CONSULTATION_VS_FACILITY")
                val memberMap = outputString.getAsJsonObject("MEMBER_ID_VS_DETAIL")

                consultationArr?.forEach { consultationEle ->
                    val consultationObj = consultationEle?.asJsonObject

                    if (consultationObj != null) {

                        val practiceId = consultationObj.getString("PRACTICE_ID")

                        if (BuildUtil.isCustomApp || (!BuildUtil.isCustomApp && enabledPracticeList.contains(
                                practiceId
                            ))
                        ) {

                            val visitSummary = PatientEncounterList()
                            visitSummary.date = DateTime.parse(
                                consultationObj.getString("CONSULTATION_DATE"),
                                ModelUtil.charmDisplayDateFormat()
                            )
                            visitSummary.readStatus = consultationObj.getBoolean("READ_STATUS")
                                ?: false
                            visitSummary.patientEncounterId =
                                consultationObj.getString("PATIENT_ENCOUNTER_ID")

                            val consultationId =
                                consultationObj.getString("PRACTICE_CONSULTATION_ID")
                                    ?: ""
                            val facilityObj = facilityMap.getAsJsonObject(consultationId)

                            visitSummary.reason = consultationObj.getString("REASON")


                            visitSummary.practiceId = practiceId


                            val fileId = consultationObj.getString("DOCUMENT_ID") ?: ""

                            if (!fileId.contains(ModelUtil.errorValueNull, true)) {
                                visitSummary.fileId = fileId
                            }


                            visitSummary.facility = formFacilityObj(facilityObj)


                            val memberId = consultationObj.getString("MEMBER_ID") ?: ""
                            val memberObj = memberMap.getAsJsonObject(memberId)

                            visitSummary.member = formMemberObj(memberObj, memberId)

                            val consultation = Consultation(consultationId = consultationId)
                            visitSummary.consultation = consultation

                            visitSummaryList.add(visitSummary)
                        }
                    }
                }

            }

            return visitSummaryList
        }

        //endregion

        //region Procedure

        fun formProcedureList(
            respObj: JsonObject,
            isHealthPassport: Boolean
        ): ArrayList<ProceduresMap> {
            val proceduresList = arrayListOf<ProceduresMap>()

            val outputData = respObj.getAsJsonArray("output_string")

            outputData?.forEach { procedureEle ->

                val procedureObj = procedureEle?.asJsonObject
                if (procedureObj != null) {
                    val procedure = formProcedureObj(procedureObj)
                    procedure.isHealthPassport = isHealthPassport
                    proceduresList.add(procedure)
                }

            }


            return proceduresList
        }

        fun formProcedureObj(procedureObj: JsonObject): ProceduresMap {

            val procedureMap = ProceduresMap()
            val procedure = ProceduresList()

            procedureMap.fromDate = DateTime.parse(
                procedureObj.getString("FROM_DATE"),
                ModelUtil.charmDisplayDateFormat()
            )
            procedure.procedure = procedureObj.getString("PROCEDURE")
            procedure.procedureId = procedureObj.getString("PROCEDURE_ID")
            procedure.procedureType = procedureObj.getString("PROCEDURE_TYPE")

            procedureMap.proceduresMapId = procedureObj.getString("PROCEDURE_MAP_ID")

            procedureMap.notes = procedureObj.getString("NOTES")


            procedureMap.toString = procedureObj.toString()
            procedureMap.procedure = procedure

            return procedureMap
        }

        fun formProcedureCommonData(respObj: JsonObject): ProceduresListCommonData {

            val procedureCommonData = ProceduresListCommonData()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {
                val procedureTypeArr = outputData.getAsJsonArray("PROCEDURE_TYPE")
                val procedureTypeList = arrayListOf<String>()

                procedureTypeArr?.forEach {
                    procedureTypeList.add(it.asString)
                }

                procedureCommonData.procedureTypeList = procedureTypeList
            }

            return procedureCommonData
        }

        //endregion

        //region Insurance


        fun formInsuranceList(respObj: JsonObject): ArrayList<PatientInsuranceDetails> {
            val patInsuranceDetailList = arrayListOf<PatientInsuranceDetails>()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {
                val insuranceArr = outputData.getAsJsonArray("INSURANCE_LIST")
                insuranceArr?.forEach { insuranceEle ->

                    val insuranceObj = insuranceEle?.asJsonObject
                    if (insuranceObj != null) {
                        patInsuranceDetailList.add(formInsuranceObj(insuranceObj))
                    }
                }
            }

            return patInsuranceDetailList
        }

        fun formInsuranceObj(insuranceObj: JsonObject): PatientInsuranceDetails {
            val patInsuranceDetails = PatientInsuranceDetails()
            patInsuranceDetails.patientInsuranceDetailsId = insuranceObj.getString("INSURED_ID")
            patInsuranceDetails.insuranceCompanyDetailsId =
                insuranceObj.getString("INSURANCE_COMPANY_DETAILS_ID")
            patInsuranceDetails.insuredDetailsId = insuranceObj.getString("INSURED_DETAILS_ID")
            patInsuranceDetails.practicePatientId = insuranceObj.getString("PATIENT_ID")
            patInsuranceDetails.type = insuranceObj.getString("TYPE")
            patInsuranceDetails.isPrimary = insuranceObj.getBoolean("IS_PRIMARY") ?: false
            patInsuranceDetails.policyNumber = insuranceObj.getString("ID_NUMBER")
            patInsuranceDetails.copay = insuranceObj.getString("COPAY")
            patInsuranceDetails.deductable = insuranceObj.getString("DEDUCTABLE")
            patInsuranceDetails.policyDeductible = insuranceObj.getString("POLICY_DEDUCTIBLE")
            patInsuranceDetails.groupNumber = insuranceObj.getString("POLICY_GROUP_NUMBER")
            patInsuranceDetails.comment = insuranceObj.getString("COMMENT")
            patInsuranceDetails.insurancePlanType = insuranceObj.getString("INSURANCE_TYPE")
            patInsuranceDetails.insurancePlanName = insuranceObj.getString("PLAN_NAME")
            //TODO after server fix issue
//            patInsuranceDetails.isNewPayerAllowed =
//                insuranceObj.getBoolean("is_add_new_payer_allowed")?:true

            val planFromDateStr = insuranceObj.getString("PLAN_FROM_DATE")
            if (!planFromDateStr.isNullOrBlank()) {
                patInsuranceDetails.policyStartDate =
                    DateTime.parse(planFromDateStr, ModelUtil.charmDisplayDateFormat())
            }
            val planToDateStr = insuranceObj.getString("PLAN_TO_DATE")
            if (!planToDateStr.isNullOrBlank()) {
                patInsuranceDetails.policyEndDate =
                    DateTime.parse(planToDateStr, ModelUtil.charmDisplayDateFormat())
            }

            val insuredAndGuarantorDetails = InsuredAndGuarantorDetails()
            insuredAndGuarantorDetails.insuredGuarantorDetailsId =
                insuranceObj.getString("INSURED_GUARANTOR_DETAILS_ID")
            insuredAndGuarantorDetails.lastName = insuranceObj.getString("LAST_NAME")
            insuredAndGuarantorDetails.firstName = insuranceObj.getString("FIRST_NAME")
            insuredAndGuarantorDetails.gender = insuranceObj.getString("GENDER")
            insuredAndGuarantorDetails.relationship = insuranceObj.getString("RELATIONSHIP")
            insuredAndGuarantorDetails.ssn = insuranceObj.getString("SSN")

            val dobStr = insuranceObj.getString("DATE_OF_BIRTH")
            if (!dobStr.isNullOrBlank()) {
                insuredAndGuarantorDetails.dateOfBirth =
                    DateTime.parse(dobStr, ModelUtil.charmDisplayDateFormat())
            }

            val insuredEmployerDetails = InsuredEmployerDetails()
            insuredEmployerDetails.employerDetailsId = insuranceObj.getString("EMPLOYER_DETAILS_ID")
            insuredEmployerDetails.name = insuranceObj.getString("EMPLOYER_NAME")
            insuredEmployerDetails.contactLastName = insuranceObj.getString("CONTACT_LAST_NAME")
            insuredEmployerDetails.contactFirstName = insuranceObj.getString("CONTACT_FIRST_NAME")

            val practicePayer = PracticePayer()
            practicePayer.practicePayerId = insuranceObj.getString("PRACTICE_PAYER_ID")
            practicePayer.payerName = insuranceObj.getString("PAYER_NAME")
            practicePayer.knownName = insuranceObj.getString("KNOWN_NAME")
            practicePayer.payerId = insuranceObj.getString("PAYER_ID")
            practicePayer.practicePayerId = insuranceObj.getString("PRACTICE_PAYER_ID")


            val payerPostalAddress = PostalAddress()
            payerPostalAddress.postalAddressId = insuranceObj.getString("POSTALADDRESS_ID")
            payerPostalAddress.line1 = insuranceObj.getString("PAYER_ADDR_LIINE1")
            payerPostalAddress.city = insuranceObj.getString("PAYER_CITY")
            payerPostalAddress.state = insuranceObj.getString("PAYERSTATE")
            payerPostalAddress.country = insuranceObj.getString("PAYERCOUNTRY")
            payerPostalAddress.postalCode = insuranceObj.getString("PAYERZIP")

            val contactDetails = ContactDetails()
            contactDetails.contactDetailsId = insuranceObj.getString("CONTACTDETAILS_ID")
            contactDetails.homePhone = insuranceObj.getString("LANDLINE")
            contactDetails.mobile = insuranceObj.getString("MOBILE")

            val postalAddress = PostalAddress()
            postalAddress.postalAddressId = insuranceObj.getString("POSTALADDRESS_ID")
            postalAddress.line1 = insuranceObj.getString("LINE_1")
            postalAddress.line2 = insuranceObj.getString("LINE_2")
            postalAddress.city = insuranceObj.getString("CITY")
            postalAddress.state = insuranceObj.getString("STATE")
            postalAddress.country = insuranceObj.getString("COUNTRY")
            postalAddress.postalCode = insuranceObj.getString("POSTALCODE")


            patInsuranceDetails.insuredAndGuarantorDetails = insuredAndGuarantorDetails
            insuredAndGuarantorDetails.insuredEmployerDetails = insuredEmployerDetails
            insuredAndGuarantorDetails.postalAddress = postalAddress
            insuredAndGuarantorDetails.contactDetails = contactDetails
            patInsuranceDetails.practicePayer = practicePayer
            practicePayer.postalAddress = payerPostalAddress

            patInsuranceDetails.toString = insuranceObj.toString()

            if (patInsuranceDetails.isNewPayerAllowed == false) {
                //Get PayerList from Server
                val payerArray = insuranceObj.getJsonArray("payers_json_data")
                val payerList = arrayListOf<PracticePayer>()
                payerArray?.forEach {
                    val payerJson = it as JsonObject
                    payerList.add(formPracticePayer(payerJson))
                }
                patInsuranceDetails.practicePayerList.addAll(payerList)
            }
            return patInsuranceDetails
        }

        private fun formPracticePayer(practicePayerJson: JsonObject): PracticePayer {
            val practicePayer = PracticePayer()
            practicePayer.practicePayerId = practicePayerJson.getString("PRACTICE_PAYER_ID")
            practicePayer.payerName = practicePayerJson.getString("PAYER_NAME")
            practicePayer.knownName = practicePayerJson.getString("KNOWN_NAME")
            practicePayer.payerId = practicePayerJson.getString("PAYER_ID")
            return practicePayer
        }

        private fun getStateListForCountry(stateListObj: JsonObject): ArrayList<StateList> {
            val stateListArr: ArrayList<StateList> = arrayListOf()
            stateListObj.keySet().iterator().forEach {
                val stateList = StateList()
                stateList.countryCode = it
                val stateArr = stateListObj.getAsJsonArray(it)
                stateArr?.forEach { stateEle ->
                    val state = stateEle?.asString
                    if (!state.isNullOrBlank()) {
                        stateList.stateList.add(state)
                    }
                }
                stateListArr.add(stateList)
            }
            return stateListArr
        }

        fun formInsuranceCommonData(respObj: JsonObject): InsuranceCommonData {
            val insuranceCommonData = InsuranceCommonData()

            val outputData = respObj.getAsJsonObject("output_string")
            if (outputData != null) {

                val insuranceTypeArr = outputData.getAsJsonArray("INSURANCE_TYPES")
                insuranceTypeArr?.forEach { insuranceTypeEle ->
                    val type = insuranceTypeEle?.asString
                    if (!type.isNullOrBlank()) {
                        insuranceCommonData.insuranceTypes.add(type)
                    }
                }

                val relationshipArr = outputData.getAsJsonArray("RELATIONSHIP_LIST")
                relationshipArr?.forEach { relationshipEle ->
                    val relationship = relationshipEle?.asString
                    if (!relationship.isNullOrBlank()) {
                        insuranceCommonData.relationships.add(relationship)
                    }
                }

                val stateListObj = outputData.getAsJsonObject("COUNTRYVSSTATEMAP")

                if (stateListObj != null) {
                    insuranceCommonData.stateList = getStateListForCountry(stateListObj)
                }

                val countryObj = outputData.getAsJsonObject("COUNTRY_LIST")?.entrySet()?.iterator()
                countryObj?.forEach {
                    insuranceCommonData.country.add(
                        DataWithId(
                            key = it.value?.asString
                                ?: "", name = it.key
                        )
                    )
                }

                insuranceCommonData.isUSUser = outputData.getBoolean("IS_US_USER") ?: false
            }

            return insuranceCommonData
        }

        fun formPayerList(respObj: JsonObject): ArrayList<PracticePayer> {

            val payerList = arrayListOf<PracticePayer>()

            val outputData = respObj.getAsJsonArray("output_string")

            outputData?.forEach { payerEle ->

                val payerObj = payerEle?.asJsonObject
                if (payerObj != null) {

                    val practicePayer = PracticePayer()
                    practicePayer.practicePayerId = payerObj.getString("PRACTICE_PAYER_ID")
                    practicePayer.payerName = payerObj.getString("PAYER_NAME")
                    practicePayer.payerId = payerObj.getString("CODE")

                    val postalAddress = PostalAddress()
                    postalAddress.line1 = payerObj.getString("LINE_1")
                    postalAddress.line2 = payerObj.getString("LINE_2")
                    postalAddress.city = payerObj.getString("CITY")
                    postalAddress.country = payerObj.getString("COUNTRY")
                    postalAddress.state = payerObj.getString("STATE")
                    postalAddress.postalCode = payerObj.getString("ZIPCODE")
                    postalAddress.country = payerObj.getString("COUNTRY")

                    practicePayer.postalAddress = postalAddress
                    payerList.add(practicePayer)

                }
            }

            return payerList
        }

        //endregion

        //region Vaccine

        fun formVaccineList(vaccineListResp: JsonObject): ArrayList<VaccineImmunizationPOJO> {

            val vaccineList = arrayListOf<VaccineImmunizationPOJO>()

            val outputData = vaccineListResp.getAsJsonObject("output_string")


            if (outputData != null) {

                val vaccineArr = outputData.getAsJsonArray("DATA_LIST")

                vaccineArr?.forEach { vaccineEle ->

                    val vaccineObj = vaccineEle?.asJsonObject

                    if (vaccineObj != null) {

                        val vaccineImmunizationPOJO = formVaccineObj(vaccineObj)
                        vaccineList.add(vaccineImmunizationPOJO)

                    }
                }
            }


            return vaccineList

        }

        fun formVaccineObj(vaccineObj: JsonObject): VaccineImmunizationPOJO {
            val vaccineImmunizationPOJO = VaccineImmunizationPOJO()

            vaccineImmunizationPOJO.addedByPatient = vaccineObj.getBoolean("ADDED_BY_PATIENT")
                ?: false

            if (vaccineImmunizationPOJO.addedByPatient) {

                val immuneMap = PatientImmunizationMap()

                val immunization = ImmunizationList()
                val immuneHistory = PatientImmunizationHistory()

                immunization.immunizationName = vaccineObj.getString("IMMUNIZATION_NAME")
                immunization.immunizationType = vaccineObj.getString("IMMUNIZATION_TYPE")
                immunization.immunizationId = vaccineObj.getString("IMMUNIZATION_ID")


                immuneHistory.immunizationHistoryId =
                    vaccineObj.getString("IMMUNIZATION_HISTORY_ID")

                val notesEle = vaccineObj.getString("COMMENTS")
                immuneHistory.comments = notesEle


                val givenByEle = vaccineObj.getString("GIVEN_BY")
                immuneHistory.givenBy = givenByEle


                val howAdministeredEle = vaccineObj.getString("HOW_ADMINISTERED")
                immuneHistory.howAdministered = howAdministeredEle


                val manufacturerEle = vaccineObj.getString("MANUFACTURER")
                immuneHistory.manufacturer = manufacturerEle


                val dateOfVaccinationStr = vaccineObj.getString("DATE_OF_IMMUNIZATION") ?: ""
                if (dateOfVaccinationStr.isNotBlank()) {
                    immuneHistory.vaccinesOn =
                        DateTime.parse(dateOfVaccinationStr, ModelUtil.charmDisplayDateFormat())
                }

                immuneMap.patientImmunizationHistory = immuneHistory
                immuneMap.immunizationList = immunization



                vaccineImmunizationPOJO.patientImmunizationMap = immuneMap

            } else {

                val vaccineMap = PatientVaccineMap()
                val vaccine = VaccinesList()

                vaccine.vaccineName = vaccineObj.getString("VACCINE_NAME")

                vaccineMap.administeredAmount = vaccineObj.getString("ADMINISTERED_AMOUNT")

                val dateOfVaccinationStr = vaccineObj.getString("DATE_OF_IMMUNIZATION") ?: ""

                if (dateOfVaccinationStr.isNotBlank()) {
                    vaccineMap.dateOfVaccination =
                        DateTime.parse(dateOfVaccinationStr, ModelUtil.charmDisplayDateFormat())
                }




                vaccineMap.orderingProvider = Member(
                    fullName = vaccineObj.getString("ORDERING_PROVIDER")
                        ?: ""
                )

                vaccineMap.site = vaccineObj.getString("SITE")
                vaccineMap.route = vaccineObj.getString("ROUTE")

                vaccineMap.vaccine = vaccine

                vaccineImmunizationPOJO.patientVaccineMap = vaccineMap
            }

            vaccineImmunizationPOJO.toString = vaccineObj.toString()

            return vaccineImmunizationPOJO
        }

        fun formVaccineCommonData(vaccineCommonDataResp: JsonObject): PatientImmunizationCommonData {

            val vaccineCommonData = PatientImmunizationCommonData()

            val outputData = vaccineCommonDataResp.getAsJsonObject("output_string")

            if (outputData != null) {
                val providerArr = outputData.getAsJsonArray("PROVIDERS_LIST")
                val providerList = arrayListOf<Member>()

                providerArr?.forEach {

                    val memberObj = it.asJsonObject

                    val member = Member()

                    member.practiceId = memberObj.getString("PRACTICE_ID")
                    member.fullName = memberObj.getString("FULL_NAME")
                    member.specialization =
                        memberObj.getString("MEMBER_ID") //SPECIALIZATION comes in member Id
                    member.prefix = memberObj.getString("PREFIX")

                    providerList.add(member)
                }

                val administeredArr = outputData.getAsJsonArray("ADMINISTERED_LIST")
                val administeredList = arrayListOf<String>()

                administeredArr?.forEach {
                    administeredList.add(it.asString)
                }

                val searchStrArr = outputData.getAsJsonArray("SEARCH_STRING")
                val searchStrList = arrayListOf<String>()

                searchStrArr?.forEach { searchStrEle ->
                    val searchStrObj = searchStrEle?.asJsonObject

                    if (searchStrObj != null) {
                        searchStrList.add(searchStrObj.getString("name") ?: "")
                    }
                }

                vaccineCommonData.providerList = providerList
                vaccineCommonData.administeredList = administeredList
                vaccineCommonData.searchString = searchStrList

            }

            return vaccineCommonData
        }
        //endregion

        //region Contacts

        fun formContactList(respObj: JsonObject): ArrayList<Pharmacy> {


            val contactList = arrayListOf<Pharmacy>()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {
                val contactType = outputData.getString("CONTACT_TYPE")

                val medContactArr = outputData.getAsJsonArray("MEDCONTACT_LIST")

                medContactArr?.forEach { medContactEle ->

                    val medContactObj = medContactEle?.asJsonObject

                    if (medContactObj != null) {

                        contactList.add(formContactObj(medContactObj, contactType))
                    }
                }
            }

            return contactList

        }

        fun formContactObj(medContactObj: JsonObject, contactType: String?): Pharmacy {
            val pharmacy = Pharmacy()

            pharmacy.pharmacyId = medContactObj.getString("PHARMACY_ID")
            pharmacy.pharmacyName = medContactObj.getString("PHARMACY_NAME")
            pharmacy.patientPharmaMapId = medContactObj.getString("PATIENT_PHARMA_MAP_ID")
            pharmacy.patientId = medContactObj.getString("PATIENT_ID")
            pharmacy.contactType = contactType

            val contactDetails = ContactDetails()

            val mobileObj = medContactObj.getString("MOBILE")

            contactDetails.mobile = mobileObj ?: ""


            pharmacy.contactDetails = contactDetails

            pharmacy.toString = medContactObj.toString()
            return pharmacy
        }

        fun formContactListForSearch(respObj: JsonObject): ArrayList<Pharmacy> {
            val contactList = ArrayList<Pharmacy>()

            val outputData = respObj.getAsJsonArray("Patient_Details")

            outputData?.forEach { pharmacyEle ->

                val pharmacyObj = pharmacyEle?.asJsonObject

                if (pharmacyObj != null) {

                    val emailId = pharmacyObj.getString("EMAIL_ID")
                    if (!emailId.isNullOrBlank()) {
                        val pharmacy = Pharmacy()
                        pharmacy.contactDetails = ContactDetails(emailId = emailId)
                        pharmacy.pharmacyName = pharmacyObj.getString("NAME")
                        pharmacy.patientPharmaMapId = pharmacyObj.getString("ID")
                        contactList.add(pharmacy)
                    }
                }
            }

            return contactList
        }

        fun formContactListFromOthersAdd(respObj: JsonObject, contactList: List<Pharmacy>) {

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                val respContactArr = outputData.getAsJsonArray("new_contacts")

                respContactArr?.forEachIndexed { index, respContactEle ->

                    val respContactId = respContactEle?.asString

                    if (!respContactId.isNullOrBlank()) {
                        contactList[index].patientPharmaMapId = respContactId
                    }

                }
            }
        }

        fun formContactArrayFromOthersAdd(respObj: JsonObject): Any? {

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                return outputData.getAsJsonArray("new_contacts")

            }
            return APIResponseError()
        }

        //endregion

        //region Documents

        fun formDocumentList(respObj: JsonObject, isHealthPassport: Boolean): ArrayList<Document> {

            val documentList = arrayListOf<Document>()

            val outputData = respObj.getAsJsonArray("output_string")

            outputData?.forEach { documentEle ->

                val documentObj = documentEle?.asJsonObject

                if (documentObj != null) {
                    val category = Category()

                    category.categoryId = documentObj.getString("CATEGORY_ID")
                    category.categoryName = documentObj.getString("CATEGORY_NAME")
                    category.categoryDescription = documentObj.getString("CATEGORY_DESCRIPTION")


                    val document = Document()
                    document.fileId = documentObj.getString("FILE_ID")
                    document.fileName = documentObj.getString("FILE_NAME")
                    document.fileSize = documentObj.getString("FILE_SIZE")
                    document.fileType = documentObj.getString("FILE_TYPE")
                    document.timeOfUpload =
                        DateTime(documentObj.getString("TIME_OF_UPLOAD")?.toLongOrNull())
                    document.contentType = documentObj.getString("CONTENT_TYPE")
                    document.readStatus =
                        documentObj.getBoolean("READ_STATUS") ?: document.readStatus

                    document.senderId = documentObj.getString("SENDER_ID")
                    document.sentBy = documentObj.getString("SENT_BY")


                    document.isAddedByPatient = document.sentBy.isNullOrBlank()
                    document.isHealthPassport = isHealthPassport

                    val dateObj = documentObj.getString("DATE")

                    if (!dateObj.isNullOrBlank()) {
                        document.date = DateTime.parse(dateObj, ModelUtil.charmDisplayDateFormat())
                    }
                    document.category = category
                    documentList.add(document)

                    val sharedToArr = documentObj.getAsJsonArray("SHARED_BY_ME_PRAC_NAME")

                    sharedToArr?.forEach {
                        val sharedtoPrac = it?.asString

                        if (!sharedtoPrac.isNullOrBlank()) {
                            document.sharedToList.add(sharedtoPrac)
                        }
                    }
                }
            }

            return documentList

        }

        fun formCategoryList(respObj: JsonObject): DocumentMetaData {

            val documentMetaData = DocumentMetaData()


            try {

                val outputData = respObj.getAsJsonArray("output_string")?.get(0)

                outputData?.asJsonArray?.forEach { categoryEle ->

                    val categoryObj = categoryEle?.asJsonObject

                    if (categoryObj != null) {
                        val category = Category()

                        category.categoryId = categoryObj.getString("CATEGORY_ID")
                        category.categoryName = categoryObj.getString("CATEGORY_NAME")


                        if (!category.categoryId.isNullOrBlank()) {
                            category.createdOn =
                                DateTime(categoryObj.getString("CREATED_ON")?.toLongOrNull())
                            category.modifiedOn =
                                DateTime(categoryObj.getString("MODIFIED_ON")?.toLongOrNull())
                            category.isDeleted = categoryObj.getBoolean("IS_DELETED") ?: false
                        }

                        documentMetaData.categoryList.add(category)
                    }
                }
            } catch (e: IllegalStateException) {
                Log.d("MdCallback", "$e")
            }


            try {
                val typeArr = respObj.getAsJsonArray("output_string")?.get(1)?.asJsonArray
                documentMetaData.typeList.addAll(typeArr?.mapNotNull { it.asString }
                    ?: arrayListOf())
            } catch (e: IllegalStateException) {
                Log.d("MdCallback", "$e")
            }
            return documentMetaData

        }

        fun formMemberList(respObj: JsonObject): ArrayList<Member> {
            val memberList = ArrayList<Member>()

            val outputData = respObj.getAsJsonArray("output_string")

            outputData?.forEach { memberEle ->
                val memberObj = memberEle?.asJsonObject
                if (memberObj != null) {
                    val member = Member()
                    member.fullName = memberObj.getString("FULL_NAME")
                    member.memberId = memberObj.getString("MEMBER_ID")
                    member.prefix = memberObj.getString("PREFIX")
                    member.specialization = memberObj.getString("SPECIALIZATION")
                    member.practiceId = memberObj.getString("PRACTICE_ID")
                    member.practice = PracticeList(
                        practiceId = member.practiceId,
                        practiceName = memberObj.getString("PRACTICE_NAME")
                    )
                    member.fullName = memberObj.getString("FULL_NAME")

                    memberList.add(member)
                }
            }

            return memberList
        }

        fun formShareDocRestriction(respObj: JsonObject, practiceId: String): ShareDocRestriction {

            val shareDocRestriction = ShareDocRestriction()

            shareDocRestriction.practiceId = practiceId

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                shareDocRestriction.isShareAllowed =
                    outputData.getBoolean("is_document_share_allowed")
                        ?: true
            }

            return shareDocRestriction
        }
        //endregion

        //region Medical Condition

        fun formMedicalConditionList(respObj: JsonObject): ArrayList<PatientMedicalCondition> {

            val medConditionList = arrayListOf<PatientMedicalCondition>()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                val medConditionArr = outputData.getAsJsonArray("DATA_LIST")

                medConditionArr?.forEach { medConditionEle ->

                    val medConditionObj = medConditionEle?.asJsonObject
                    if (medConditionObj != null) {

                        medConditionList.add(formMedicalConditionObj(medConditionObj))
                    }
                }
            }

            return medConditionList
        }

        fun formMedicalConditionObj(medConditionObj: JsonObject): PatientMedicalCondition {

            val patMedCondition = PatientMedicalCondition()

            val diagnosisObj = medConditionObj.getString("DIAGNOSIS")

            if (!diagnosisObj.isNullOrBlank()) {
                patMedCondition.isAddedByPatient = false

                val dateObj = medConditionObj.getString("FROM_DATE")
                if (!dateObj.isNullOrBlank()) {
                    patMedCondition.dateOfOnset =
                        DateTime.parse(dateObj, ModelUtil.charmDisplayDateFormat())
                }

                val diagnosis = Diagnosis()
                diagnosis.diagnosisLong = diagnosisObj
                patMedCondition.diagnosis = diagnosis

            } else {
                patMedCondition.isAddedByPatient = true

                patMedCondition.medicalConditionMapId =
                    medConditionObj.getString("MEDICAL_CONDITION_MAP_ID")
                patMedCondition.notes = medConditionObj.getString("NOTES")

                val dateObj = medConditionObj.getString("DATE_OF_ONSET")
                if (!dateObj.isNullOrBlank()) {
                    patMedCondition.dateOfOnset =
                        DateTime.parse(dateObj, ModelUtil.charmDisplayDateFormat())
                }

                val medCondition = MedicalCondition()
                medCondition.medicalConditionId = medConditionObj.getString("MEDICAL_CONDITION_ID")
                medCondition.medicalConditionName =
                    medConditionObj.getString("MEDICAL_CONDITION_NAME")

                patMedCondition.medicalCondition = medCondition

            }

            patMedCondition.status = medConditionObj.getString("STATUS")

            patMedCondition.toString = medConditionObj.toString()


            return patMedCondition
        }

        fun formMedicalConditionCommonData(respObj: JsonObject): ArrayList<MedicalCondition> {

            val medConditionList = arrayListOf<MedicalCondition>()
            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                val searchStrArr = outputData.getAsJsonArray("SEARCH_STRING")

                searchStrArr?.forEach { medConditionEle ->

                    val medConditionObj = medConditionEle?.asJsonObject

                    if (medConditionObj != null) {

                        val medCondition = MedicalCondition()
                        medCondition.medicalConditionId = medConditionObj.getString("id")
                        medCondition.medicalConditionName = medConditionObj.getString("name")

                        medConditionList.add(medCondition)
                    }
                }
            }

            return medConditionList
        }

        //endregion

        fun getStateList(respObj: JsonObject): ArrayList<String> {

            val stateList = arrayListOf<String>()
            val outputData = respObj.getAsJsonArray("output_string")

            outputData?.forEach {
                stateList.add(it.asString)
            }

            return stateList
        }

        //region Vitals

        fun getVitalsForDashboard(respObj: JsonObject): ArrayList<PatientVitals> {

            val patientVitalsList = arrayListOf<PatientVitals>()
            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                val vitalsObj = outputData.getAsJsonObject("VITALS")

                if (vitalsObj != null) {
                    val bpArray = vitalsObj.getAsJsonArray("Blood Pressure")
                    bpArray?.forEach { bpEle ->

                        val bpObj = bpEle?.asJsonObject

                        if (bpObj != null) {
                            val patientVitals = PatientVitals()

                            val bpDay = bpObj.getString("DAY") ?: ""
                            val bpMonth = bpObj.getString("MONTH") ?: ""
                            val bpYear = bpObj.getString("YEAR") ?: ""

                            patientVitals.dateOfEntry = DateTime.parse(
                                "$bpMonth $bpDay, $bpYear",
                                ModelUtil.charmDisplayDateFormat()
                            )
                            patientVitals.trackedValue = bpObj.getString("TRACKED_VALUE")
                            patientVitals.metricName = bpObj.getString("METRIC_NAME")
                            patientVitals.trackerName = ModelUtil.vitalBP
                            patientVitals.trackedValueUnit = bpObj.getString("TRACKED_VALUE_UNIT")

                            patientVitalsList.add(patientVitals)
                        }
                    }

                    val generalArray = vitalsObj.getAsJsonArray("General")
                    generalArray?.forEach { generalEle ->

                        val generalObj = generalEle?.asJsonObject

                        if (generalObj != null) {
                            val patientVitals = PatientVitals()

                            val bpDay = generalObj.getString("DAY") ?: ""
                            val bpMonth = generalObj.getString("MONTH") ?: ""
                            val bpYear = generalObj.getString("YEAR") ?: ""

                            patientVitals.dateOfEntry = DateTime.parse(
                                "$bpMonth $bpDay, $bpYear",
                                ModelUtil.charmDisplayDateFormat()
                            )

                            patientVitals.metricName = generalObj.getString("METRIC_NAME")
                            patientVitals.trackerName = ModelUtil.vitalGeneral
                            patientVitals.trackedValueUnit =
                                generalObj.getString("TRACKED_VALUE_UNIT")
                            patientVitals.trackedValue = generalObj.getString("TRACKED_VALUE")
                            patientVitalsList.add(patientVitals)
                        }
                    }
                }
            }

            return patientVitalsList
        }


        fun getVitalUnitType(respObj: JsonObject): String {
            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {
                return outputData.getString("unit_system") ?: ""
            }

            return ""
        }

        fun getVitalTrackerList(respObj: JsonObject): ArrayList<TrackerMetricMap> {
            val patVitalTrackerList = arrayListOf<TrackerMetricMap>()
            val outputData = respObj.getAsJsonArray("output_string")

            outputData?.forEach { trackerEle ->

                val trackerObj = trackerEle?.asJsonObject
                if (trackerObj != null) {
                    val trackerMetricMap = getVitalTrackerObj(trackerObj)
                    patVitalTrackerList.add(trackerMetricMap)
                }
            }

            return patVitalTrackerList
        }

        fun getVitalTrackerObj(trackerObj: JsonObject, metricId: String? = null): TrackerMetricMap {
            val trackerMetricMap = TrackerMetricMap()
            val tracker = Tracker()

            tracker.trackerId = trackerObj.getString("tracker_id")
            tracker.trackerName = trackerObj.getString("tracker_name")
            tracker.isDefaultTracker = trackerObj.getBoolean("is_default_tracker") ?: false

            val metricArr = trackerObj.getAsJsonArray("metric_list")

            metricArr?.forEach { metricEle ->
                val metricObj = metricEle?.asJsonObject
                if (metricObj != null) {

                    val metric = Metric()
                    metric.metricId = metricObj.getString("metric_id")
                    metric.metricName = metricObj.getString("metric_name")
                    metric.metricUnit = metricObj.getString("metric_unit")
                    metric.isDefaultMetric = metricObj.getBoolean("is_default_metric") ?: false

                    if ((tracker.isDefaultTracker && tracker.trackerName?.contains(
                            ModelUtil.wellnessGeneralModelId,
                            true
                        ) == true) || metricId.isNullOrBlank() || metric.metricId == metricId
                    ) {
                        trackerMetricMap.metricList.add(metric)
                    }
                }
            }

            trackerMetricMap.tracker = tracker
            trackerMetricMap.toString = trackerObj.toString()

            return trackerMetricMap
        }

        /*
                fun getVitalDetailList(respObj: JsonObject, trackerId: String , metric: String): ArrayList<PatientVitalTracker> {

                    val patVitalTrackerList = arrayListOf<PatientVitalTracker>()
                    val outputData = respObj.getAsJsonObject("output_string")

                    if (outputData != null) {

                        val trackerName = outputData.getString("TRACKER_NAME")
                        val tracker = Tracker(trackerId, trackerName)

                        val trackerHistoryArr = outputData.getAsJsonArray("TRACKER_HISTORY")

                        trackerHistoryArr?.forEach { trackerHistoryEle ->

                            val trackerHistoryObj = trackerHistoryEle?.asJsonObject

                            if (trackerHistoryObj != null) {
                                val date = trackerHistoryObj.getString("DATE") ?: ""

                                val patientVitalTracker = PatientVitalTracker()

                                patientVitalTracker.tracker = tracker
                                patientVitalTracker.dateOfEntry = DateTime.parse(date, ModelUtil.charmWellnessDateFormatter())

                                val vitalEntry = trackerHistoryObj.getAsJsonObject("ENTRY")

                                if (vitalEntry != null) {
                                    val patientVitalList = arrayListOf<PatientVitals>()

                                    val patientVitalArr = vitalEntry.entrySet().iterator()

                                    patientVitalArr.forEach { patientVitalMap ->

                                        val patientVitals = PatientVitals()

                                        val metricName = patientVitalMap.key

                                        if (metricName.contains("(") && metricName.contains(")")) {
                                            val metricList = metricName.split("(")
                                            patientVitals.metricName = metricList[0]

                                            if (metricList.size == 2) {
                                                patientVitals.trackedValueUnit = metricList[1].split(")")[0]
                                            }
                                        } else {
                                            patientVitals.metricName = metricName
                                        }

                                        patientVitals.trackedValue = patientVitalMap.value?.asString

                                        if (metric.isBlank() || metric.equals(patientVitals.metricName , true)) {
                                            patientVitalList.add(patientVitals)
                                        }
                                    }

                                    patientVitalTracker.patientVitalsList = patientVitalList
                                }

                                patVitalTrackerList.add(patientVitalTracker)
                            }
                        }
                    }

                    return patVitalTrackerList
                }
        */


        fun getMetricDetailList(
            respObj: JsonObject,
            trackerId: String,
            metricId: String
        ): ArrayList<PatientVitalTracker> {
            val outputData = respObj.getAsJsonObject("output_string")

            val tempPatientVitalsList = ArrayList<PatientVitals>()
            val tracker = Tracker()
            tracker.trackerId = trackerId
            if (outputData != null) {
                val metricHistoryArr = outputData.getAsJsonArray("metric_history")
                val metricName = outputData.getString("metric_name")
                metricHistoryArr?.forEach { metricHistoryEle ->
                    val metricHistoryObj = metricHistoryEle?.asJsonObject

                    if (metricHistoryObj != null) {
                        tempPatientVitalsList.add(
                            getPatientVitals(
                                metricHistoryObj,
                                metricName,
                                metricId,
                                trackerId
                            )
                        )
                    }
                }
            }
            return getPatientVitalTrackerList(tempPatientVitalsList, tracker)
        }

        fun getPatientVitalTrackerList(
            tempPatientVitalsList: ArrayList<PatientVitals>,
            tracker: Tracker
        ): ArrayList<PatientVitalTracker> {
            val patVitalTrackerList = arrayListOf<PatientVitalTracker>()
            while (tempPatientVitalsList.isNotEmpty()) {
                val tempPatientVitals = tempPatientVitalsList.first()

                val grpOfPatVitals =
                    tempPatientVitalsList.filter { it.dateOfEntry?.millis == tempPatientVitals.dateOfEntry?.millis }

                val patientVitalTracker = PatientVitalTracker()
                patientVitalTracker.tracker = tracker
                patientVitalTracker.patientVitalsList.addAll(grpOfPatVitals)
                patientVitalTracker.dateOfEntry = DateTime(tempPatientVitals.dateOfEntry?.millis)

                patVitalTrackerList.add(patientVitalTracker)
                tempPatientVitalsList.removeAll(grpOfPatVitals)
            }

            patVitalTrackerList.sortByDescending { it.dateOfEntry?.millis }

            return patVitalTrackerList
        }

        fun getPatientVitals(
            metricHistoryObj: JsonObject,
            metricName: String?,
            metricId: String?,
            trackerId: String
        ): PatientVitals {
            val patientVitals = PatientVitals()
            patientVitals.metricName = metricName
            patientVitals.metricId = metricId
            patientVitals.trackerId = trackerId
            patientVitals.trackedValue =
                metricHistoryObj.getString("tracked_value")
            patientVitals.trackedValueUnit =
                metricHistoryObj.getString("tracked_unit")
            patientVitals.notes = metricHistoryObj.getString("notes")
            patientVitals.entryId = metricHistoryObj.getString("entry_id")
            patientVitals.dateOfEntry = DateTime.parse(
                metricHistoryObj.getString("date_of_entry"),
                ModelUtil.charmDateFormatForVitals()
            )
            return patientVitals
        }

        fun getVitalDetailList(
            respObj: JsonObject,
            trackerId: String,
            metric: String
        ): ArrayList<PatientVitalTracker> {

            val patVitalTrackerList = arrayListOf<PatientVitalTracker>()
            val outputData = respObj.getAsJsonObject("output_string")

            val tempPatientVitalsList = ArrayList<PatientVitals>()

            val tracker = Tracker()
            tracker.trackerId = trackerId

            if (outputData != null) {


                val vitalHistoryArr = outputData.getAsJsonArray("history")

                tracker.trackerName = outputData.getString("tracker_name")

                vitalHistoryArr?.forEach { vitalHistoryEle ->

                    val vitalHistoryObj = vitalHistoryEle?.asJsonObject

                    if (vitalHistoryObj != null) {
                        val metricId = vitalHistoryObj.getString("metric_id")
                        val metricUnit = vitalHistoryObj.getString("metric_current_unit")
                        val metricName = vitalHistoryObj.getString("metric_name")

                        val metricHistoryArr = vitalHistoryObj.getAsJsonArray("metric_history")

                        metricHistoryArr?.forEach { metricHistoryEle ->
                            val metricHistoryObj = metricHistoryEle?.asJsonObject

                            if (metricHistoryObj != null) {

                                val patientVitals = getPatientVitals(
                                    metricHistoryObj,
                                    metricName,
                                    metricId,
                                    trackerId
                                )

                                if (metric.isBlank() || metric == patientVitals.metricId) {
                                    tempPatientVitalsList.add(patientVitals)
                                }
                                // tempPatientVitalsList.add(patientVitals)
                            }
                        }
                    }

                }
            }

            return getPatientVitalTrackerList(tempPatientVitalsList, tracker)
        }


        fun getHealthMetricUnitList(respObj: JsonObject): ArrayList<String> {

            val healthMetricUnitList = arrayListOf<String>()
            val outputData = respObj.getAsJsonArray("output_string")

            outputData?.forEach {
                healthMetricUnitList.add(it?.asString ?: "")
            }

            return healthMetricUnitList
        }

        fun getHealthMetricList(respObj: JsonObject): ArrayList<HealthMetric> {

            val healthMetricList = arrayListOf<HealthMetric>()
            val outputData = respObj.getAsJsonArray("output_string")

            outputData?.forEach { healthMetricEle ->

                val healthMetricObj = healthMetricEle?.asJsonObject

                if (healthMetricObj != null) {
                    val healthMetric = HealthMetric()
                    healthMetric.description = healthMetricObj.getString("DESCRIPTION")
                    healthMetric.healthMetricId = healthMetricObj.getString("HEALTH_METRIC_ID")
                    healthMetric.healthMetricName = healthMetricObj.getString("HEALTH_METRIC_NAME")
                    healthMetric.healthMetricUnit = healthMetricObj.getString("HEALTH_METRIC_UNIT")

                    healthMetricList.add(healthMetric)
                }
            }

            return healthMetricList
        }

        fun getWellnessCommonData(respObj: JsonObject): TrackerCommonData {
            val trackerCommonData = TrackerCommonData()

            val outputData = respObj.getAsJsonObject("output_string")

            if (outputData != null) {

                val existTracker = outputData.getAsJsonObject("EXIST_TRACKER")

                if (existTracker != null) {
                    val trackerList = existTracker.entrySet().iterator()

                    trackerList.forEach {

                        val trackerObj = it.value?.asJsonObject

                        if (trackerObj != null) {

                            val tracker = Tracker()
                            tracker.trackerId = trackerObj.getString("TRACKER_ID")
                            tracker.trackerUnitSystem = trackerObj.getString("TRACKER_UNIT_SYSTEM")
                            tracker.trackerName = it.key
                            trackerCommonData.trackerList.add(tracker)
                        }
                    }
                }

                val healthMetricArr = outputData.getAsJsonArray("METRICS_LIST")

                healthMetricArr?.forEach { healthMetricEle ->

                    val healthMetricObj = healthMetricEle?.asJsonObject

                    if (healthMetricObj != null) {
                        val healthMetric = HealthMetric()

                        healthMetric.healthMetricId = healthMetricObj.getString("HEALTH_METRIC_ID")
                        healthMetric.healthMetricName =
                            healthMetricObj.getString("HEALTH_METRIC_NAME")
                        healthMetric.healthMetricUnit =
                            healthMetricObj.getString("HEALTH_METRIC_UNIT")
                        healthMetric.description = healthMetricObj.getString("DESCRIPTION")

                        trackerCommonData.healthMetricList.add(healthMetric)

                    }
                }

            }

            return trackerCommonData
        }
        //endregion

        //region shared pref db parse


        fun formPatientList(patientListStr: String): ArrayList<Patient> {

            val patientList = arrayListOf<Patient>()

            try {

                val patientArr = patientListStr.parseStringAsJsonArray()

                patientArr?.forEach { patientEle ->
                    val patientObj = patientEle.asJsonObject
                    val patient = Patient()

                    patient.practicePatientId = patientObj.getString("patient_id")
                    patient.firstName = patientObj.getString("first_name")
                    patient.lastName = patientObj.getString("last_name")
                    patient.fullName = "${patient.firstName} ${patient.lastName}"

                    patient.isHealthPassportAvailable =
                        patientObj.getBoolean("is_health_passport_available")
                            ?: false

                    val dateStr = patientObj.getString("date_of_birth") ?: ""

                    if (dateStr.isNotBlank()) {
                        patient.dateOfBirth =
                            LocalDate.parse(
                                dateStr,
                                ModelUtil.charmApiDateFormat()
                            )
                    }
                    patient.gender = patientObj.getString("gender")
                    patient.unitSystem = patientObj.getString("unit_system")

                    patient.isPhotoAvailableOnline = patientObj.getBoolean("is_photo_available")
                        ?: true
                    patientList.add(patient)

                    val practiceArr = patientObj.getAsJsonArray("practice_list")

                    val practiceList = arrayListOf<PracticeList>()

                    practiceArr?.forEach { practiceEle ->
                        val practiceObj = practiceEle?.asJsonObject

                        if (practiceObj != null) {
                            val practice = PracticeList()
                            practice.practiceId = practiceObj.getString("practice_id")
                            practice.practiceName = practiceObj.getString("practice_name")
                            practice.isActive = practiceObj.getBoolean("is_active") ?: false
                            practice.facilityList = practiceObj.getString("facility_list")
                            practice.isBluefinEnabled =
                                practiceObj.getBoolean("IS_BLUEFIN_ENABLED") ?: false
                            if (practice.isBluefinEnabled) {
                                patient.isBluefinEnabled = true
                            }

                            practiceList.add(practice)
                        }
                    }

                    if (practiceList.isNotEmpty()) {
                        patient.practiceList = practiceList

                        if (!patientList.mapNotNull { it.practicePatientId }
                                .contains(patient.practicePatientId)) {
                            patientList.add(patient)
                        }
                    }
                }

            } catch (e: Exception) {
                Log.d("patientParseException", e.toString())
            }
            return patientList
        }


        fun formComposeMsgRestrictionList(respStr: String): ArrayList<ComposeMsgRestriction> {

            val composeMsgRestrictionList = arrayListOf<ComposeMsgRestriction>()

            try {
                val composeMsgRestrictionArr = respStr.parseStringAsJsonArray()

                composeMsgRestrictionArr?.forEach { composeMsgRestrictionEle ->

                    val composeMsgRestrictionObj = composeMsgRestrictionEle?.asJsonObject

                    if (composeMsgRestrictionObj != null) {
                        val composeMsgRestriction = ComposeMsgRestriction()
                        composeMsgRestriction.allowPHRMessaging =
                            composeMsgRestrictionObj.getBoolean("allowPHRMessaging")
                                ?: true
                        composeMsgRestriction.isMessageRestrict =
                            composeMsgRestrictionObj.getBoolean("isMessageRestrict")
                                ?: true
                        composeMsgRestriction.noOfCharsAllowed =
                            composeMsgRestrictionObj.getInt("noOfCharsAllowed")
                                ?: 0
                        composeMsgRestriction.practiceId =
                            composeMsgRestrictionObj.getString("practiceId")
                                ?: ""
                        composeMsgRestrictionList.add(composeMsgRestriction)
                    }
                }

            } catch (e: Exception) {
                Log.d("cmpMsgRstParseException", e.toString())
            }

            return composeMsgRestrictionList
        }

        //endregion

        //region video call

        fun parseZoomCredential(jsonObject: JsonObject): Any {

            val teleHealthObj = jsonObject.getAsJsonObject("telehealth")

            val platform = teleHealthObj?.getString("platform")

            val isConsentFormAvailable = teleHealthObj.getBoolean("is_consent_form_available") ?: false
            val consentFormIds = arrayListOf<String>()
            if (isConsentFormAvailable) {
                val consentFormIdsArr = teleHealthObj.getAsJsonArray("consent_form_ids")
                consentFormIdsArr?.forEach { ele ->
                    val id = ele?.asString
                    if (!id.isNullOrBlank()) {
                        consentFormIds.add(id)
                    }
                }
            }

            if (platform.equals("OPENTOK")) {
                val openTokCredentials = OpenTokCredentials()
                openTokCredentials.joinUrl = teleHealthObj?.getString("join_url")
                openTokCredentials.password = teleHealthObj?.getString("meeting_password")
                return TeleHealthCredentials(
                    credentials = openTokCredentials,
                    isConsentFormAvailable = isConsentFormAvailable,
                    consentFormIds = consentFormIds
                )
            } else /*if (platform.equals("ZOOM"))*/ {
                val meetingObj = teleHealthObj.getAsJsonObject("meeting")
                val zoomDomain = meetingObj.getString("ZOOM_DOMAIN")?.trim()
                val userType = meetingObj.getString("USER_TYPE")?.trim()
                val memberName = meetingObj.getString("MEMBER_NAME")?.trim()
                val userId = meetingObj.getString("USER_ID")?.trim()
                val meetingId = meetingObj.getString("ID")?.trim()

                val zak = meetingObj.getString("ZAK")?.trim()
                val sdkJWT = meetingObj.getString("SDK_JWT")

                if (!meetingId.isNullOrBlank() && !sdkJWT.isNullOrBlank()) {

                    val zoomCredentials = ZoomCredentials()

                    zoomCredentials.webDomain = zoomDomain
                    zoomCredentials.userType = userType
                    zoomCredentials.userId = userId
                    zoomCredentials.meetingId = meetingId
                    zoomCredentials.zak = zak
                    zoomCredentials.sdkJwt = sdkJWT

                    return TeleHealthCredentials(
                        credentials = zoomCredentials,
                        isConsentFormAvailable = isConsentFormAvailable,
                        consentFormIds = consentFormIds
                    )
                }
            }
            return APIResponseError()
        }

        //endregion

        //region support
        fun parseSupportDocumentId(respObj: JsonObject): String {
            var fileIds = ""
            val attachmentArr = respObj.getAsJsonArray("attachments")
            attachmentArr.forEach {
                val attachmentJson = it.asJsonObject
                fileIds = fileIds + attachmentJson.getString("file_id") + ","
            }
            return fileIds
        }

        //endregion
        //region Reminder
        private fun formReminderObject(reminderJson: JsonObject, patientId: String): ReminderPOJO {
            val reminderPOJO = ReminderPOJO()
            val reminderObj = Reminder()
            val reminderMap = ReminderMap()
            reminderMap.moduleId = reminderJson.getString("module_id")
            reminderObj.addedBy = reminderJson.getString("added_by")
            reminderMap.practiceId = reminderJson.getString("practice_id")
            reminderMap.patientId = patientId
            reminderMap.type = reminderJson.getString("type")
            reminderObj.reminderId = reminderJson.getString("reminder_id")
            val startDateStr = reminderJson.getString("start_date")
            if (startDateStr != null) {
                reminderObj.startDate = UIUtil.formatDate(
                    startDateStr,
                    ModelUtil.charmDisplayDateFormat()
                )
            }
            val endDateStr = reminderJson.getString("end_date")
            endDateStr?.let {
                reminderObj.endDate = UIUtil.formatDate(
                    endDateStr,
                    ModelUtil.charmDisplayDateFormat()
                )
            }
            val startDate = reminderObj.startDate?.withTimeAtStartOfDay() ?: DateTime.now()
                .withTimeAtStartOfDay()
            val endDate =
                reminderObj.endDate?.withTimeAtStartOfDay() ?: DateTime.now().withTimeAtStartOfDay()
            val currentDate = DateTime.now().withTimeAtStartOfDay()
            if (endDate.isBefore(currentDate)) {
                reminderObj.reminderStatus = ModelUtil.CompletedStatus
            } else if (startDate.isAfter(currentDate)) {
                reminderObj.reminderStatus = ModelUtil.UpcomingStatus
            } else {
                reminderObj.reminderStatus = ModelUtil.ActiveStatus
            }
            reminderMap.reminderId = reminderJson.getString("reminder_id")
            val reminderFrequencyMapList = arrayListOf<ReminderFrequencyMap>()
            val frequencyObj = reminderJson.getJsonObject("frequency")
            val durationArr = frequencyObj?.getJsonArray("duration")
            durationArr?.forEach {
                val durationJson = it as JsonObject
                val day = durationJson.getString("day")
                val time = durationJson.getJsonArray("time")
                time?.forEach {
                    val timeJson = it as JsonObject
                    val key = timeJson.keySet().firstOrNull() ?: ""
                    val reminderFrequencyMap = ReminderFrequencyMap()
                    reminderFrequencyMap.reminderId = reminderObj.reminderId
                    reminderFrequencyMap.frequencyMapId = key
                    if (day.equals("not_configured")) {
                        reminderFrequencyMap.day = -1
                    } else {
                        reminderFrequencyMap.day = day?.toInt() ?: -1
                    }
                    reminderFrequencyMap.time = timeJson.getString(key)
                    reminderFrequencyMapList.add(reminderFrequencyMap)

                }
            }
            reminderObj.frequencyUnit = frequencyObj?.getString("frequency_unit")
            reminderObj.frequency = frequencyObj?.getString("frequency_value")
            reminderObj.isDeleted = reminderJson.getBoolean("is_deleted") ?: false
            reminderPOJO.reminder = reminderObj
            reminderPOJO.reminderFrequencyMap = reminderFrequencyMapList
            reminderPOJO.reminderMap = arrayListOf(reminderMap)
            return reminderPOJO
        }

        fun formReminderObjectWithLocalId(reminderJson: JsonObject): ReminderPOJO {
            val reminderPOJO = ReminderPOJO()
            val reminderObj = Reminder()
            val reminderMap = ReminderMap()
            reminderMap.moduleId = reminderJson.getString("module_id")
            reminderObj.addedBy = reminderJson.getString("added_by")
            reminderMap.practiceId = reminderJson.getString("practice_id")
            reminderMap.type = reminderJson.getString("type")
            reminderMap.localReminderMapId = reminderJson.get("localReminderMapId").asLong
            reminderObj.reminderId = reminderJson.getString("reminder_id")
            val startDateStr = reminderJson.getString("start_date")
            startDateStr?.let {
                reminderObj.startDate = UIUtil.formatDate(
                    startDateStr,
                    ModelUtil.charmDisplayDateFormat()
                )
            }
            val endDateStr = reminderJson.getString("end_date")
            endDateStr?.let {
                reminderObj.endDate = UIUtil.formatDate(
                    endDateStr,
                    ModelUtil.charmDisplayDateFormat()
                )
            }
            val currentDate = DateTime.now().withTimeAtStartOfDay()
            if (reminderObj.endDate?.isBefore(currentDate) == true) {
                reminderObj.reminderStatus = ModelUtil.CompletedStatus
            } else if (reminderObj.startDate?.isAfter(currentDate) == true) {
                reminderObj.reminderStatus = ModelUtil.UpcomingStatus
            } else {
                reminderObj.reminderStatus = ModelUtil.ActiveStatus
            }
            reminderMap.reminderId = reminderJson.getString("reminder_id")
            val reminderFrequencyMapList = arrayListOf<ReminderFrequencyMap>()
            val frequencyObj = reminderJson.getAsJsonObject("frequency")
            val durationArr = frequencyObj.getAsJsonArray("duration")
            durationArr.forEach {
                val durationJson = it as JsonObject
                val day = durationJson.getString("day")
                val time = durationJson.getString("time")
                val localFrequencyMapId = durationJson.get("localFrequencyMapId")
                val reminderFrequencyMap = ReminderFrequencyMap()
                reminderFrequencyMap.reminderId = reminderObj.reminderId
                if (day.equals("not_configured")) {
                    reminderFrequencyMap.day = -1
                } else {
                    reminderFrequencyMap.day = day?.toInt() ?: -1
                }
                reminderFrequencyMap.time = time
                reminderFrequencyMap.localFrequencyMapId = localFrequencyMapId.asLong
                reminderFrequencyMap.frequencyMapId = durationJson.getString("frequencyMapId")
                reminderFrequencyMapList.add(reminderFrequencyMap)
            }
            reminderObj.frequencyUnit = frequencyObj.getString("frequency_unit")
            reminderObj.frequency = frequencyObj.getString("frequency_value")
            reminderObj.isDeleted = reminderJson.getBoolean("is_deleted") ?: false
            reminderPOJO.reminder = reminderObj
            reminderPOJO.reminderFrequencyMap = reminderFrequencyMapList
            reminderPOJO.reminderMap = arrayListOf(reminderMap)
            return reminderPOJO
        }


        fun parseReminderObject(respObj: JsonObject, patientId: String): ArrayList<ReminderPOJO>? {
            val reminderPOJOList = arrayListOf<ReminderPOJO>()
            val reminderArr = respObj.getJsonArray("output_string")
            if (reminderArr != null) {
                reminderArr.forEach {
                    val reminderJson = it as JsonObject

                    reminderPOJOList.add(formReminderObject(reminderJson, patientId))
                }
            } else {
                return null
            }
            return reminderPOJOList
        }

        fun parseReminderId(respObj: JsonObject): String? {
            val reminderResponse = respObj.getJsonObject("output_string")
            return reminderResponse?.getString("reminder_id")
        }

        fun parseReminderErrorMessage(respObj: JsonObject): String? {
            val reminderResponse = respObj.getJsonObject("output_string")
            return reminderResponse?.getString("message")
        }

        fun parseHistoryId(respObj: JsonObject): String? {
            val reminderResponse = respObj.getJsonObject("output_string")
            return reminderResponse?.getString("reminder_history_id")
        }

        fun parseHistoryErrorMessage(respObj: JsonObject): String? {
            val reminderResponse = respObj.getJsonObject("output_string")
            return reminderResponse?.getString("message")
        }

        fun parseUpdateHistory(respObj: JsonObject): Boolean {
            val reminderResponse = respObj.getJsonObject("output_string")
            return reminderResponse?.getString("result").equals("Success")
        }

        fun parseModuleReminderHistoryObject(respObj: JsonObject): ArrayList<ReminderHistoryPOJO> {
            val reminderHistoryPOJOList = ArrayList<ReminderHistoryPOJO>()
            val reminderArr = respObj.getJsonArray("output_string")
            reminderArr?.forEach { reminderObject ->
                val reminderHistoryJsonArr = reminderObject as JsonArray
                reminderHistoryJsonArr.forEach {
                    val reminderHistoryJson = it as JsonObject
                    val reminderMap = ReminderMap()
                    reminderMap.moduleId = reminderHistoryJson.getString("module_id")
                    reminderMap.type = reminderHistoryJson.getString("type")
                    reminderMap.reminderId = reminderHistoryJson.getString("reminder_id")
                    val historyArr = reminderHistoryJson.getJsonArray("history")
                    historyArr?.forEach { historyObj ->
                        val reminderHistoryPOJO = ReminderHistoryPOJO()
                        reminderHistoryPOJO.reminderMap = arrayListOf(reminderMap)
                        val historyJson = historyObj as JsonObject
                        val historyObject = ReminderHistory()
                        historyObject.reminderMapId = reminderMap.reminderId
                        historyObject.historyId = historyJson.getString("history_id")
                        historyObject.reminderTime = historyJson.getString("reminder_time")
                        historyObject.status = historyJson.getInt("status")
                        val statusUpdatedDate = historyJson.getString("status_updated_date")
                        val statusUpdatedTime = historyJson.getString("status_updated_time")
                        historyObject.statusUpdatedTime = UIUtil.formatDate(
                            "$statusUpdatedDate $statusUpdatedTime",
                            ModelUtil.charmFlowsheetDateTimeFieldFormat()
                        )
                        val reminderFrequencyMap = ReminderFrequencyMap()
                        reminderFrequencyMap.day = historyJson.getInt("reminder_day") ?: -1
                        reminderFrequencyMap.time = historyJson.getString("reminder_time")
                        reminderHistoryPOJO.reminderHistory = historyObject
                        reminderHistoryPOJO.reminderFrequencyMap = arrayListOf(reminderFrequencyMap)
                        reminderHistoryPOJOList.add(reminderHistoryPOJO)
                    }
                }
            }
            return reminderHistoryPOJOList
        }

        fun parseReminderHistoryObject(
            respObj: JsonObject,
            reminderPOJO: ReminderPOJO
        ): ArrayList<ReminderHistoryPOJO>? {
            val reminderHistoryPOJOList = ArrayList<ReminderHistoryPOJO>()
            val historyArr = respObj.getJsonArray("output_string")
            if (historyArr != null) {
                historyArr.forEach {
                    val reminderHistoryPOJO = ReminderHistoryPOJO()
                    val reminderMap = ReminderMap()
                    reminderMap.moduleId = reminderPOJO.reminderMap.firstOrNull()?.moduleId ?: ""
                    reminderMap.type = reminderPOJO.reminderMap.firstOrNull()?.type ?: ""
                    reminderMap.reminderId = reminderPOJO.reminder?.reminderId ?: ""
                    reminderMap.practiceId =
                        reminderPOJO.reminderMap.firstOrNull()?.practiceId ?: ""
                    reminderMap.localReminderMapId =
                        reminderPOJO.reminderMap.firstOrNull()?.localReminderMapId ?: 0
                    reminderHistoryPOJO.reminderMap = arrayListOf(reminderMap)

                    val historyJson = it as JsonObject
                    val historyObject = ReminderHistory()
                    historyObject.historyId = historyJson.getString("history_id")
                    historyObject.status = historyJson.getInt("status")
                    historyObject.reminderMapId = reminderMap.reminderId
                    historyObject.reminderTime = historyJson.getString("reminder_time")
                    val statusUpdatedDate = historyJson.getString("status_updated_date")
                    val statusUpdatedTime = historyJson.getString("status_updated_time")
                    historyObject.statusUpdatedTime = UIUtil.formatDate(
                        "$statusUpdatedDate $statusUpdatedTime",
                        ModelUtil.charmFlowsheetDateTimeFieldFormat()
                    )
                    val reminderFrequencyMap = ReminderFrequencyMap()
                    val day = historyJson.getString("reminder_day")
                    if (day.equals("not_configured")) {
                        reminderFrequencyMap.day = -1
                    } else {
                        reminderFrequencyMap.day = day?.toInt() ?: -1
                    }
                    reminderFrequencyMap.time = historyJson.getString("reminder_time")
                    val reminderHistoryFrequencyMap = reminderPOJO.reminderFrequencyMap.filter {
                        it.day == reminderFrequencyMap.day && it.time == reminderFrequencyMap.time
                    }
                    reminderFrequencyMap.localFrequencyMapId =
                        reminderHistoryFrequencyMap.firstOrNull()?.localFrequencyMapId ?: 0
                    reminderHistoryPOJO.reminderHistory = historyObject
                    reminderHistoryPOJO.reminderFrequencyMap = arrayListOf(reminderFrequencyMap)
                    reminderHistoryPOJOList.add(reminderHistoryPOJO)

                }
            } else return null
            return reminderHistoryPOJOList
        }

        //endregion



    }


}
