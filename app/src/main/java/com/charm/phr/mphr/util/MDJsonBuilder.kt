package com.charm.phr.mphr.util

import com.charm.phr.mphr.model.AppointmentRequest
import com.charm.phr.mphr.model.Category
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.model.HealthMetric
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.MenuList
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.model.NotificationRegister
import com.charm.phr.mphr.model.PHRFlowsheets
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientAllergy
import com.charm.phr.mphr.model.PatientInsuranceDetails
import com.charm.phr.mphr.model.PatientMedicalCondition
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.model.PatientVitalTracker
import com.charm.phr.mphr.model.PaymentGatewayToken
import com.charm.phr.mphr.model.PaymentInfo
import com.charm.phr.mphr.model.Pharmacy
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.model.PostalAddress
import com.charm.phr.mphr.model.ProceduresMap
import com.charm.phr.mphr.model.QuestionnaireDetailPOJO
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.SupportData
import com.charm.phr.mphr.model.SupportDetails
import com.charm.phr.mphr.model.SwaasaModel
import com.charm.phr.mphr.model.TransactionInfo
import com.charm.phr.mphr.model.VaccineImmunizationPOJO
import com.charm.phr.mphr.util.ModelUtil.Companion.isNotJsonNull
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import org.joda.time.DateTime
import java.io.File
import java.util.Locale

class MDJsonBuilder {

    companion object {

        private var bluefinReqParams = arrayListOf(
            "account_id",
            "hash",
            "timestamp",
            "success_url",
            "decline_url",
            "transaction_type",
            "tender_type",
            "card_number",
            "card_verification",
            "first_name",
            "last_name",
            "street_address1",
            "street_address2",
            "city",
            "state",
            "country",
            "zip",
            "response_format",
            "custom_data",
            "custom_id"
        ) //No I18N


        //region Patient

        fun formPatientObj(patient: Patient): JsonObject {

            val postObj = JsonObject()

            //   postObj.addProperty("patient_id", patient.practicePatientId)
            postObj.addProperty("first_name", patient.firstName)
            postObj.addProperty("last_name", patient.lastName)
            postObj.addProperty("date", patient.dateOfBirth?.dayOfMonth)
            postObj.addProperty("month", patient.dateOfBirth?.monthOfYear)
            postObj.addProperty("year", patient.dateOfBirth?.year)
            postObj.addProperty("gender", patient.gender)
            postObj.addProperty("emergency_contact_name", patient.emergencyContactName)
            postObj.addProperty("emergency_contact_number", patient.emergencyContactNumber)
            postObj.addProperty("blood_group", patient.bloodGroup)

            val contactDetails = patient.contactDetails
            if (contactDetails != null) {
                postObj.addProperty("acc_landline", contactDetails.landline)
                postObj.addProperty("acc_mobile", contactDetails.mobile)
            }

            val postalAddress = patient.postalAddress
            if (postalAddress != null) {
                postObj.addProperty("line_1", postalAddress.line1)
                postObj.addProperty("line_2", postalAddress.line2)
                postObj.addProperty("city", postalAddress.city)
                postObj.addProperty("state", postalAddress.state)
                postObj.addProperty("country", postalAddress.country)
            }


            val contactObj = JsonObject()
            contactObj.addProperty("first_name", patient.caregiverFirstName)
            contactObj.addProperty("last_name", patient.caregiverLastName)
            contactObj.addProperty("mobile", patient.caregiverMobile)
            contactObj.addProperty("contact_type", "primary")

            val contactsArr = JsonArray()
            contactsArr.add(contactObj)
            postObj.add("contacts", contactsArr)

            return postObj
        }

        //endregion

        //region Appointment

        fun formAppointmentObj(appointmentRequest: AppointmentRequest): JsonObject {
            val postObj = JsonObject()

            if (appointmentRequest.addAsWaitingList) {
                postObj.addProperty("patient_id", appointmentRequest.practicePatientId)
            }
            postObj.addProperty("facility_id", appointmentRequest.facility?.facilityId)
            postObj.addProperty("practice_id", appointmentRequest.practiceId)
            postObj.addProperty("reason", appointmentRequest.reason)

            if (!appointmentRequest.receiptId.isNullOrBlank()) {
                postObj.addProperty("receipt_id", appointmentRequest.receiptId)
            }

            if (appointmentRequest.isEnabled) {

                if (appointmentRequest.addAsWaitingList) {
                    postObj.addProperty("mode", appointmentRequest.appointmentType)
                    postObj.addProperty("member_id", appointmentRequest.member?.memberId)
                    postObj.addProperty(
                        "date_option1",
                        appointmentRequest.dateChoice1?.toString(ModelUtil.charmApiDateFormat())
                    )

                } else {
                    postObj.addProperty("appointment_type", appointmentRequest.appointmentType)
                    postObj.addProperty("physician_id", appointmentRequest.member?.memberId)

                    postObj.addProperty(
                        "physician_name",
                        UIUtil.formMemberName(appointmentRequest.member)
                    )
                    postObj.addProperty("facility_name", appointmentRequest.facility?.facilityName)
                    postObj.addProperty("duration", appointmentRequest.duration)
                    postObj.addProperty("repeat_type", "ONETIME")
                    postObj.addProperty("double_booking", false)
                    postObj.addProperty(
                        "appointment_date_str",
                        appointmentRequest.dateChoice1?.toString(ModelUtil.charmDisplayDateFormat())
                    )
                    postObj.addProperty("appointment_status", "Confirmed")
                    postObj.addProperty("start_hour", appointmentRequest.startHour)
                    postObj.addProperty("start_minutes", appointmentRequest.startMinutes)
                    postObj.addProperty("start_session", appointmentRequest.startSession)
                }
                if (appointmentRequest.visitType != null) {
                    postObj.addProperty("visit_type_id", appointmentRequest.visitType?.visittypeId)
                }

            } else {
                postObj.addProperty("physician_id", appointmentRequest.member?.memberId)

                postObj.addProperty("appointment_type", appointmentRequest.appointmentType)
                postObj.addProperty("reminder_type", appointmentRequest.reminderType)
                postObj.addProperty("date_preference", appointmentRequest.datePreference)

                if (appointmentRequest.datePreference == ModelUtil.appointmentDatePreferenceSpecificDate) {
                    postObj.addProperty(
                        "date_choice1",
                        appointmentRequest.dateChoice1?.toString(ModelUtil.charmDisplayDateFormat())
                    )
                }

                postObj.addProperty("time_preference", appointmentRequest.timePreference)

                if (appointmentRequest.timePreference == ModelUtil.appointmentTimePreferenceTimeRange) {
                    postObj.addProperty("start_hour", appointmentRequest.startHour)
                    postObj.addProperty("start_minutes", appointmentRequest.startMinutes)
                    postObj.addProperty("start_session", appointmentRequest.startSession)

                    postObj.addProperty("end_hour", appointmentRequest.endHour)
                    postObj.addProperty("end_minutes", appointmentRequest.endMinutes)
                    postObj.addProperty("end_session", appointmentRequest.endSession)
                }
            }




            return postObj
        }

        //endregion

        //region Message

        fun formMessageObj(messages: Messages): JsonObject {

            val postObj = JsonObject()

            postObj.addProperty("patient_id", messages.practicePatientId)
            postObj.addProperty("practice_id", messages.practiceId)
            postObj.addProperty("subject", messages.subject ?: "")
            postObj.addProperty("content", messages.content)

            val memberArr = JsonArray()

            messages.messageMembersMapList.forEach { messageMemberMap ->
                val memberObj = JsonObject()
                memberObj.addProperty("member_id", messageMemberMap.member?.memberId)
                memberArr.add(memberObj)
            }

            val attachmentsMapArr = JsonArray()

            messages.messagesAttachmentsMapList.forEach { messageAttachmentMap ->

                val attachmentObj = JsonObject()
                attachmentObj.addProperty("attachment_id", messageAttachmentMap.attachmentId)
                attachmentsMapArr.add(attachmentObj)
            }

            postObj.add("receivers", memberArr)
            if (messages.messageGroupMapList.isNotEmpty()) {
                postObj.addProperty("group_id", messages.messageGroupMapList.first().groupId)
                if (messages.ownerId != null) {
                    postObj.addProperty("owner_id", messages.ownerId)
                }
            }

            if (attachmentsMapArr.size() > 0) {
                postObj.add("attachments", attachmentsMapArr)
            }

            return postObj
        }

        //endregion

        //region Questionnaire


        fun formQuestionnaireWidgetDetailObj(
            physicianTemplates: PhysicianTemplates,
            questionnaireDetailPOJO: QuestionnaireDetailPOJO
        ): JsonObject {
            val postObj = JsonObject()

            val isFilled = physicianTemplates.patientQuesMap?.isFilled ?: false

            postObj.addProperty("template_id", physicianTemplates.templateId)
            postObj.addProperty("patient_id", physicianTemplates.patientQuesMap?.practicePatientId)

            postObj.addProperty("is_submitted", physicianTemplates.patientQuesMap?.isSubmitted)
            postObj.addProperty("ques_map_id", physicianTemplates.patientQuesMap?.patientQuesMapId)
            postObj.addProperty("practice_id", physicianTemplates.practiceId)

            questionnaireDetailPOJO.questionnairEntryPOJOList.forEach { questionnaireEntryPOJO ->

                when {
                    questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypePersonalDetails -> {


                        val patient = questionnaireEntryPOJO.questionnairePersonalDetails

                        if (patient != null) {
                            val personalObj = JsonObject()
                            personalObj.addProperty("PER_FIRST_NAME", patient.firstName)
                            personalObj.addProperty("PER_LAST_NAME", patient.lastName)
                            personalObj.addProperty(
                                "DOB",
                                patient.dateOfBirth?.toString(ModelUtil.charmApiDateFormat())
                            )
                            personalObj.addProperty(
                                "PER_GENDER",
                                patient.gender?.lowercase(Locale.ENGLISH)
                            )
                            personalObj.addProperty("PER_GENDER_IDENTITY", patient.genderIdentity)
                            personalObj.addProperty("PER_BLOOD_GROUP", patient.bloodGroup)
                            personalObj.addProperty("PER_RACE", patient.race)
                            personalObj.addProperty("PER_ETHNICITY", patient.ethnicity)
                            personalObj.addProperty("PER_EMP_STATUS", patient.employmentStatus)
                            personalObj.addProperty("PER_MARITAL_STATUS", patient.maritalStatus)
                            personalObj.addProperty("PER_SMOKING_STATUS", patient.smokingStatus)
                            personalObj.addProperty("PER_LANGUAGE", patient.language)

                            postObj.add("personal", personalObj)
                        }
                    }

                    questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypePrimaryContactDetail -> {

                        val patContactObj = JsonObject()

                        val patient = questionnaireEntryPOJO.questionnairePatContacts
                        if (patient != null) {

                            patContactObj.addProperty(
                                "PER_EMERGENCY_CONTACT_NAME",
                                patient.emergencyContactName
                            )
                            patContactObj.addProperty(
                                "PER_EMERGENCY_CONTACT_NUMBER",
                                patient.emergencyContactNumber
                            )
                            patContactObj.addProperty(
                                "PER_EMERGENCY_EXTN",
                                patient.emergencyContactExtn
                            )
                            // patContactObj.addProperty("PER_CAREGIVER_ID", patient.practicePatientId) //caregiver id is given here
                            val contactDetails = patient.contactDetails

                            if (contactDetails != null) {
                                patContactObj.addProperty("PER_EMAILID", contactDetails.emailId)
                                patContactObj.addProperty("PER_LAND_LINE", contactDetails.homePhone)
                                patContactObj.addProperty("PER_MOBILE", contactDetails.mobile)
                                patContactObj.addProperty("PER_WORKPHONE", contactDetails.workPhone)
                                patContactObj.addProperty(
                                    "PER_WORKPHONE_EXTN",
                                    contactDetails.workPhoneExtn
                                )
                                patContactObj.addProperty("PER_FAX", contactDetails.fax)

                                if (!contactDetails.primaryPhone.isNullOrBlank()) {
                                    patContactObj.addProperty(
                                        "PER_CONTACT_PREFERENCE",
                                        contactDetails.primaryPhone?.get(0)?.toString()
                                            ?.uppercase(Locale.ENGLISH)
                                    )
                                } else {
                                    patContactObj.addProperty("PER_CONTACT_PREFERENCE", "")
                                }

                                patContactObj.addProperty(
                                    "PER_CONTACTDETAILS_ID",
                                    contactDetails.contactDetailsId
                                )
                            }

                            val postalAddress = patient.postalAddress

                            if (postalAddress != null) {
                                patContactObj.addProperty("PER_ADDRESS_LINE1", postalAddress.line1)
                                patContactObj.addProperty("PER_ADDRESS_LINE2", postalAddress.line2)
                                patContactObj.addProperty("PER_CITY", postalAddress.city)
                                patContactObj.addProperty("PER_STATE", postalAddress.state)
                                patContactObj.addProperty("PER_COUNTRY", postalAddress.country)
                                patContactObj.addProperty(
                                    "PER_POSTALCODE",
                                    postalAddress.postalCode
                                )
                                patContactObj.addProperty("PER_POSTBOX_NO", postalAddress.postbox)
                                patContactObj.addProperty(
                                    "PER_POSTALADDRESS_ID",
                                    postalAddress.postalAddressId
                                )
                            }

                            postObj.add("contact", patContactObj)

                        }
                    }

                    questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypeInsurance -> {

                        val patientInsuranceDetails = questionnaireEntryPOJO.questionnaireInsurance


                        if (patientInsuranceDetails != null) {
                            patientInsuranceDetails.practicePatientId =
                                physicianTemplates.patientQuesMap?.practicePatientId
                            if (patientInsuranceDetails.isAvailable) {
                                postObj.add(
                                    "insurance",
                                    formAddInsuranceObj(patientInsuranceDetails, true)
                                )
                            } else {
                                postObj.addProperty("no_insurance", true)
                            }
                        }
                    }
                }
            }

            return postObj
        }

        fun formQuestionnaireDetailAnsObj(
            physicianTemplates: PhysicianTemplates,
            questionnaireDetailPOJO: QuestionnaireDetailPOJO
        ): JsonObject {

            val postObj = JsonObject()

            val isFilled = physicianTemplates.patientQuesMap?.isFilled ?: false

            postObj.addProperty("template_id", physicianTemplates.templateId)
            postObj.addProperty("patient_id", physicianTemplates.patientQuesMap?.practicePatientId)

            postObj.addProperty("is_submitted", physicianTemplates.patientQuesMap?.isSubmitted)
            postObj.addProperty("ques_map_id", physicianTemplates.patientQuesMap?.patientQuesMapId)

            if (!physicianTemplates.isHealthPassport) {
                postObj.addProperty("practice_id", physicianTemplates.practiceId)
                postObj.addProperty("member_id", physicianTemplates.member?.memberId)
                //postObj.addProperty("facility_id" , physicianTemplates.patientQuesMap?.facilityId)
            }

            val answersArr = JsonArray()
            questionnaireDetailPOJO.questionnairEntryPOJOList.forEach { questionnaireEntryPOJO ->

                val entryId = questionnaireEntryPOJO.questionnaireEntry.entryId
                when {
                    questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypeSupplement -> {
                        val answerObj = JsonObject()
                        answerObj.addProperty("entry_id", entryId)
                        val supplementArr = JsonArray()

                        questionnaireEntryPOJO.questionnaireSupplementList.forEach { intakeQuestSupplementEntry ->

                            val supplementObj = JsonObject()
                            supplementObj.addProperty(
                                "supplement",
                                intakeQuestSupplementEntry.supplementName
                            )
                            supplementObj.addProperty(
                                "intake_details",
                                intakeQuestSupplementEntry.intakeDetails
                            )

                            supplementArr.add(supplementObj)

                        }

                        answerObj.add("supplement_entries", supplementArr)

                        if (supplementArr.size() > 0 || isFilled) {
                            answersArr.add(answerObj)
                        }
                    }

                    questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypeMedication -> {
                        val answerObj = JsonObject()
                        answerObj.addProperty("entry_id", entryId)
                        val medicationArr = JsonArray()

                        questionnaireEntryPOJO.questionnaireMedicationList.forEach { intakeQuestMedicationEntry ->

                            val medicationObj = JsonObject()
                            medicationObj.addProperty(
                                "medication",
                                intakeQuestMedicationEntry.drugName
                            )
                            medicationObj.addProperty(
                                "intake_details",
                                intakeQuestMedicationEntry.intakeDetails
                            )

                            medicationArr.add(medicationObj)

                        }

                        answerObj.add("medication_entries", medicationArr)

                        if (medicationArr.size() > 0 || isFilled) {
                            answersArr.add(answerObj)
                        }
                    }

                    questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypeAllergy -> {
                        val answerObj = JsonObject()
                        answerObj.addProperty("entry_id", entryId)
                        val allergyArr = JsonArray()

                        questionnaireEntryPOJO.questionnaireAllergyList.forEach { intakeQuestAllergyEntry ->

                            val allergyObj = JsonObject()
                            allergyObj.addProperty("allergen", intakeQuestAllergyEntry.allergen)
                            allergyObj.addProperty("type", intakeQuestAllergyEntry.type)
                            allergyObj.addProperty("severity", intakeQuestAllergyEntry.severity)
                            allergyObj.addProperty("reactions", intakeQuestAllergyEntry.reactions)

                            allergyArr.add(allergyObj)

                        }

                        answerObj.add("allergy_entries", allergyArr)

                        if (allergyArr.size() > 0 || isFilled) {
                            answersArr.add(answerObj)
                        }
                    }

                    questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypeSignature -> {}

                    questionnaireEntryPOJO.questionnaireEntry.isMultiChoice -> {
                        val multipleAnsObj = JsonObject()
                        multipleAnsObj.addProperty("entry_id", entryId)
                        val multipleAnsArr = JsonArray()

                        questionnaireEntryPOJO.questionnaireEntry.answersList?.forEach { answer ->
                            val answerObj = JsonObject()
                            answerObj.addProperty("answer", answer)
                            multipleAnsArr.add(answerObj)
                        }

                        multipleAnsObj.add("multiple_answers", multipleAnsArr)
                        answersArr.add(multipleAnsObj)

                    }

                    questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypePersonalDetails -> {


                    }

                    questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypePrimaryContactDetail -> {

                    }

                    questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypeInsurance -> {

                    }

                    ModelUtil.SOAPTypes.contains(questionnaireEntryPOJO.questionnaireEntry.notesType) -> {
                        val answerObj = JsonObject()
                        answerObj.addProperty(
                            "entry_id",
                            questionnaireEntryPOJO.questionnaireEntry.entryId
                        )

                        val soapObj = JsonObject()
                        soapObj.addProperty(
                            "soap_widget_id",
                            questionnaireEntryPOJO.soapWidget.soapWidget?.widgetId
                        )
                        soapObj.addProperty(
                            "soap_template_id",
                            questionnaireEntryPOJO.soapWidget.soapWidget?.templateId
                        )
                        val entryArr = JsonArray()
                        questionnaireEntryPOJO.soapWidget.soapWidgetEntryPOJOList.forEach { soapWidgetEntryPOJO ->
                            if (soapWidgetEntryPOJO.soapWidgetEntry?.entryAnswer != null) {
                                val entryObj = JsonObject()
                                entryObj.addProperty(
                                    "soap_entry_id",
                                    soapWidgetEntryPOJO.soapWidgetEntry?.entryId
                                )
                                entryObj.addProperty(
                                    "soap_entry_value",
                                    soapWidgetEntryPOJO.soapWidgetEntry?.entryAnswer
                                )
                                if (soapWidgetEntryPOJO.soapWidgetEntry?.entryType == ModelUtil.soapWidgetRatingScaleType || soapWidgetEntryPOJO.soapWidgetEntry?.entryType == ModelUtil.soapWidgetYesOrNoQuestionType) {
                                    if (soapWidgetEntryPOJO.soapWidgetEntry?.entryAnswer?.isNotBlank() == true) {
                                        entryArr.add(entryObj)
                                    }
                                } else {
                                    entryArr.add(entryObj)
                                }
                            }
                        }
                        soapObj.add("soap_widget_answers", entryArr)
                        answerObj.add("soap_entries", soapObj)
                        answersArr.add(answerObj)
                    }

                    else -> {


                        val answer =
                            questionnaireEntryPOJO.questionnaireEntry.answersList?.firstOrNull()
                                ?: ""
                        val answerObj = JsonObject()
                        answerObj.addProperty(
                            "entry_id",
                            questionnaireEntryPOJO.questionnaireEntry.entryId
                        )

                        answerObj.addProperty("answer", answer)
                        if (questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypeYesOrNoOption || questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypeDate || questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypeRatingScale || questionnaireEntryPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypeRating) {
                            if (answer.isNotBlank()) {
                                answersArr.add(answerObj)
                            }
                        } else {
                            answersArr.add(answerObj)
                        }

                    }
                }
            }
            postObj.add("answers", answersArr)

            return postObj
        }

        fun formSignatureObj(
            filePath: String?,
            practiceID: String?
        ): ArrayList<MultipartBody.Part> {

            /*val postObj = JsonObject()

            postObj.addProperty("document" , FileUtil.readFileToString(document.filePath ?:""))
            postObj.addProperty("health_passport" ,document.isHealthPassport)
            postObj.addProperty("category_name" , document.category?.categoryName)


            if (!document.category?.categoryId.isNullOrBlank())
                postObj.addProperty("category_id" , document.category?.categoryId)

            return postObj*/

            val fileReq = FileUtil.createRequestBodyForFile(
                filePath
                    ?: ""
            )
            val fileMultiPartBody = MultipartBody.Part.createFormData(
                "attachment",
                File(filePath ?: "").name,
                fileReq!!
            )

            val listOFPart = arrayListOf<MultipartBody.Part>()
            listOFPart.add(fileMultiPartBody)
            //   listOFPart.add(MultipartBody.Part.createFormData("practice_id" , practiceID ?: ""))


            return listOFPart
        }

        //endregion

        //region Flowsheet

        fun formFlowsheetEntryObj(phrFlowsheets: PHRFlowsheets): JsonObject {
            val postObj = JsonObject()

            postObj.addProperty("flowsheet_id", phrFlowsheets.flowSheetId)
            postObj.addProperty(
                "date",
                phrFlowsheets.date?.toString(ModelUtil.sqlTimeStampFormattor())
            )

            val entryArr = JsonArray()

            phrFlowsheets.flowsheetEntries.filter { !it.value.isNullOrBlank() }
                .forEach { phrFlowsheetEntry ->

                    val entryObj = JsonObject()
                    entryObj.addProperty("value", phrFlowsheetEntry.value)
                    entryObj.addProperty("unit", phrFlowsheetEntry.unit)
                    entryObj.addProperty("flowsheet_entry_id", phrFlowsheetEntry.flowSheetEntryId)
                    if (!phrFlowsheetEntry.comments.isNullOrBlank()) {
                        entryObj.addProperty("comments", phrFlowsheetEntry.comments)
                    }
                    entryArr.add(entryObj)

                }

            postObj.add("flowsheet_entries", entryArr)

            return postObj
        }

        //endregion

        //region Prescription

        fun formRefillMedicationObj(medPOJO: PatMedPOJO, patient: Patient): JsonObject {
            val postObj = JsonObject()

            postObj.addProperty("physician_id", medPOJO.member?.memberId)
            postObj.addProperty("practice_id", medPOJO.patMed?.practiceId)
            postObj.addProperty("refill_type", "Prescription")
            postObj.addProperty("refill_id", medPOJO.patMed?.patientMedicationId)

            if (!medPOJO.reasonForRefill.isNullOrBlank()) {
                postObj.addProperty("reason", medPOJO.reasonForRefill)
            }

            return postObj
        }


        fun formAddMedicationObj(medPOJO: PatMedPOJO): JsonObject {

            val postObj = JsonObject()

            val patMedPHRExt = medPOJO.patMedPHRExt.firstOrNull()

            if (patMedPHRExt != null) {

                postObj.addProperty("dosage", patMedPHRExt.dosage)
                postObj.addProperty("dosage_unit_id", patMedPHRExt.dosageUnitId)
                postObj.addProperty(
                    "from_date",
                    patMedPHRExt.fromDate?.toString(ModelUtil.charmDisplayDateFormat())
                )
                postObj.addProperty("intake", patMedPHRExt.intake)
                postObj.addProperty("intake_route", patMedPHRExt.intakeRoute)
                postObj.addProperty("intake_unit", patMedPHRExt.intakeUnit)
                postObj.addProperty("medical_condition_id", patMedPHRExt.medicalConditionId)
                postObj.addProperty("number_of_intake", patMedPHRExt.noOfIntakes)
                postObj.addProperty(
                    "to_date",
                    patMedPHRExt.toDate?.toString(ModelUtil.charmDisplayDateFormat())
                )
                // postObj.addProperty("patient_id", patMedPHRExt.patientId)

                if (medPOJO.isHealthPassport) {
                    postObj.addProperty("Health_Passport", medPOJO.isHealthPassport)
                    postObj.addProperty("refills", patMedPHRExt.refills ?: "-1")

                } else {
                    postObj.addProperty("refills", patMedPHRExt.refills)
                    postObj.addProperty("comments", patMedPHRExt.comments)
                    postObj.addProperty("intake_when", patMedPHRExt.intakeWhen)
                    postObj.addProperty("practice_id", medPOJO.practiceId)
                    postObj.addProperty("is_active", true)
                    postObj.addProperty("prescription_quantity", "")
                }
            }

            val drugDetails = medPOJO.drugDetail.firstOrNull()

            if (drugDetails != null) {
                postObj.addProperty("medication_name", drugDetails.genericDrugName)
            }

            val memberName = UIUtil.formMemberName(medPOJO.member)
            postObj.addProperty("prescribed_by", memberName)


            return postObj
        }

        fun formEditMedicationObj(medPOJO: PatMedPOJO): JsonObject {

            val postObj = JsonObject()

            val patMedPHRExt = medPOJO.patMedPHRExt.firstOrNull()

            if (patMedPHRExt != null) {

                postObj.addProperty("dosage", patMedPHRExt.dosage)
                postObj.addProperty("dosage_unit_id", patMedPHRExt.dosageUnitId)
                postObj.addProperty(
                    "from_date",
                    patMedPHRExt.fromDate?.toString(ModelUtil.charmDisplayDateFormat())
                )
                postObj.addProperty("intake", patMedPHRExt.intake)
                postObj.addProperty("intake_route", patMedPHRExt.intakeRoute)
                postObj.addProperty("intake_unit", patMedPHRExt.intakeUnit)
                postObj.addProperty("number_of_intake", patMedPHRExt.noOfIntakes)
                postObj.addProperty(
                    "to_date",
                    patMedPHRExt.toDate?.toString(ModelUtil.charmDisplayDateFormat())
                )
                // postObj.addProperty("patient_id", patMedPHRExt.patientId)
                postObj.addProperty("prescription_history_id", patMedPHRExt.prescriptionHistoryId)


                if (medPOJO.isHealthPassport) {
                    postObj.addProperty("Health_Passport", medPOJO.isHealthPassport)
                } else {
                    postObj.addProperty("intake_when", patMedPHRExt.intakeWhen)
                    postObj.addProperty("refills", patMedPHRExt.refills)
                    postObj.addProperty("comments", patMedPHRExt.comments)

                }

            }

            val memberName = UIUtil.formMemberName(medPOJO.member)


            postObj.addProperty("prescribed_by", memberName)




            return postObj
        }

        fun formDeleteMedicationObj(medPOJO: PatMedPOJO): JsonObject {

            val postObj = JsonObject()

            val patMedPHRExt = medPOJO.patMedPHRExt.firstOrNull()

            if (patMedPHRExt != null) {


                //   postObj.addProperty("patient_id", patMedPHRExt.patientId)
                postObj.addProperty("prescription_history_id", patMedPHRExt.prescriptionHistoryId)
            }

            return postObj
        }


        fun formAddSupplementObj(patientSupplements: PatientSupplements): JsonObject {

            val postObj = JsonObject()


            // postObj.addProperty("patient_id", patientSupplements.practicePatientId)


            val patSupplementHistory =
                patientSupplements.patientSupplementMap?.patientSupplementHistory

            if (patSupplementHistory != null) {

                val dosageUnit = patSupplementHistory.dosageUnit

                if (dosageUnit != null) {
                    postObj.addProperty("dosage_unit_id", dosageUnit.dosageUnitId)
                }

                postObj.addProperty("comments", patSupplementHistory.comments)
                postObj.addProperty("dosage", patSupplementHistory.dosage)
                postObj.addProperty(
                    "from_date",
                    patSupplementHistory.fromDate?.toString(ModelUtil.charmDisplayDateFormat())
                )
                postObj.addProperty("intake", patSupplementHistory.intake)
                postObj.addProperty("intake_unit", patSupplementHistory.intakeUnit)
                postObj.addProperty("intake_when", patSupplementHistory.intakeWhen)
                postObj.addProperty("number_of_intake", patSupplementHistory.numberOfIntake)
                postObj.addProperty(
                    "to_date",
                    patSupplementHistory.toDate?.toString(ModelUtil.charmDisplayDateFormat())
                )
            }

            val supplement = patientSupplements.supplement

            if (supplement != null) {

                postObj.addProperty("supplement_name", supplement.supplementName)
            }


            postObj.addProperty(
                "manufacturer_name",
                patientSupplements.manufacturer?.manufactureName
            )





            postObj.addProperty("practice_id", patientSupplements.practiceId)



            return postObj
        }

        fun formEditSupplementObj(patientSupplements: PatientSupplements): JsonObject {

            val postObj = JsonObject()


            // postObj.addProperty("patient_id", patientSupplements.practicePatientId)


            val patSupplementHistory =
                patientSupplements.patientSupplementMap?.patientSupplementHistory

            if (patSupplementHistory != null) {

                val dosageUnit = patSupplementHistory.dosageUnit

                if (dosageUnit != null) {
                    postObj.addProperty("dosage_unit_id", dosageUnit.dosageUnitId)
                }

                postObj.addProperty("comments", patSupplementHistory.comments)
                postObj.addProperty("dosage", patSupplementHistory.dosage)
                postObj.addProperty(
                    "from_date",
                    patSupplementHistory.fromDate?.toString(ModelUtil.charmDisplayDateFormat())
                )
                postObj.addProperty("intake", patSupplementHistory.intake)
                postObj.addProperty("intake_unit", patSupplementHistory.intakeUnit)
                postObj.addProperty("intake_when", patSupplementHistory.intakeWhen)
                postObj.addProperty("number_of_intake", patSupplementHistory.numberOfIntake)
                postObj.addProperty(
                    "to_date",
                    patSupplementHistory.toDate?.toString(ModelUtil.charmDisplayDateFormat())
                )
                postObj.addProperty(
                    "supplement_history_id",
                    patSupplementHistory.supplementHistoryId
                )
            }

            postObj.addProperty(
                "manufacturer_name",
                patientSupplements.manufacturer?.manufactureName
            )

            return postObj
        }

        fun formDeleteSupplementObj(patientSupplements: PatientSupplements): JsonObject {

            val postObj = JsonObject()


            // postObj.addProperty("patient_id", patientSupplements.practicePatientId)


            val patSupplementHistory =
                patientSupplements.patientSupplementMap?.patientSupplementHistory

            if (patSupplementHistory != null) {

                postObj.addProperty(
                    "supplement_history_id",
                    patSupplementHistory.supplementHistoryId
                )
            }

            return postObj
        }

        fun formRefillSupplementObj(
            patientSupplements: PatientSupplements,
            patient: Patient
        ): JsonObject {
            val postObj = JsonObject()

            postObj.addProperty("physician_id", patientSupplements.member?.memberId)
            postObj.addProperty("practice_id", patientSupplements.practiceId)
            postObj.addProperty("refill_type", "Supplement")
            postObj.addProperty("refill_id", patientSupplements.patientSupplementId)
            if (!patientSupplements.reasonForRefill.isNullOrBlank()) {
                postObj.addProperty("reason", patientSupplements.reasonForRefill)
            }

            return postObj
        }

        //endregion

        /*
        *   comments: "test"
            dosage: "3"
            dosage_unit_id: "101000000002325"
            from_date: "Feb 05, 2020"
            intake: "once a day"
            intake_route: "compounding"
            intake_unit: "day(s)"
            intake_when: "after a meal"
            is_active: "true"
            medical_condition_id: "101000000002543"
            medication_name: "paracetemol"
            number_of_intake: "2"
            patient_id: "98000000081177"
            practice_id: "257000000071003"
            prescribed_by: "Dr. William Aberthnet"
            prescription_quantity: ""
            refills: "4"
            to_date: "Feb 05, 2020"
* */
        //region Allergy

        fun formAddPatAllergyObj(patientAllergy: PatientAllergy): JsonObject {

            val reqObj = JsonObject()

            reqObj.addProperty("allergy", patientAllergy.allergy?.allergy)
            reqObj.addProperty("allergy_type", patientAllergy.allergy?.allergyType)
            reqObj.addProperty(
                "observed_date",
                patientAllergy.observedDate?.toString(ModelUtil.charmDisplayDateFormat())
            )
            reqObj.addProperty("reactions", patientAllergy.reactions)
            reqObj.addProperty("severity", patientAllergy.severity)
            reqObj.addProperty("status", patientAllergy.status)
            if (patientAllergy.status == ModelUtil.allergyStatusResolved) {
                reqObj.addProperty(
                    "resolved_date",
                    patientAllergy.resolvedDate?.toString(ModelUtil.charmDisplayDateFormat())
                )
            }

            reqObj.addProperty("patient_id", patientAllergy.practicePatientId)

            if (!patientAllergy.allergy?.allergyOtherType.isNullOrBlank()) {
                reqObj.addProperty("other_type", patientAllergy.allergy?.allergyOtherType)
            }

            if (patientAllergy.isHealthPassport) {
                reqObj.addProperty("Health_Passport", patientAllergy.isHealthPassport)
            }

            return reqObj
        }

        fun formEditPatAllergyObj(patientAllergy: PatientAllergy): JsonObject {

            val reqObj = JsonObject()

            reqObj.addProperty(
                "observed_date",
                patientAllergy.observedDate?.toString(ModelUtil.charmDisplayDateFormat())
            )
            reqObj.addProperty("reactions", patientAllergy.reactions)
            reqObj.addProperty("severity", patientAllergy.severity)
            reqObj.addProperty("status", patientAllergy.status)
            reqObj.addProperty("patient_id", patientAllergy.practicePatientId)
            reqObj.addProperty("allergy_id", patientAllergy.allergy?.allergyId)
            reqObj.addProperty("allergy_map_id", patientAllergy.allergyMapId)

            if (patientAllergy.isHealthPassport) {
                reqObj.addProperty("Health_Passport", patientAllergy.isHealthPassport)
            }
            return reqObj
        }


        fun formDeletePatAllergyObj(patientAllergy: PatientAllergy): JsonObject {

            val reqObj = JsonObject()

            reqObj.addProperty("patient_id", patientAllergy.practicePatientId)
            reqObj.addProperty("allergy_map_id", patientAllergy.allergyMapId)


            return reqObj
        }


        //endregion

        //region Procedure

        fun formAddProcedureObj(procedure: ProceduresMap): JsonObject {

            val reqObj = JsonObject()

            reqObj.addProperty("procedure", procedure.procedure?.procedure)
            reqObj.addProperty("procedure_type", procedure.procedure?.procedureType)
            reqObj.addProperty(
                "from_date",
                procedure.fromDate?.toString(ModelUtil.charmDisplayDateFormat())
            )
            reqObj.addProperty("notes", procedure.notes)
            reqObj.addProperty("patient_id", procedure.practicePatientId)

            if (!procedure.procedure?.otherType.isNullOrBlank()) {
                reqObj.addProperty("other_type", procedure.procedure?.otherType)
            }

            if (procedure.isHealthPassport) {
                reqObj.addProperty("health_passport", procedure.isHealthPassport)
            }
            return reqObj
        }

        fun formEditProcedureObj(procedure: ProceduresMap): JsonObject {

            val reqObj = JsonObject()

            reqObj.addProperty("procedure_map_id", procedure.proceduresMapId)
            reqObj.addProperty(
                "from_date",
                procedure.fromDate?.toString(ModelUtil.charmDisplayDateFormat())
            )
            reqObj.addProperty("notes", procedure.notes)
            reqObj.addProperty("patient_id", procedure.practicePatientId)

            if (procedure.isHealthPassport) {
                reqObj.addProperty("health_passport", procedure.isHealthPassport)
            }

            return reqObj
        }

        fun formDeleteProcedureObj(procedure: ProceduresMap): JsonObject {

            val reqObj = JsonObject()

            reqObj.addProperty("patient_id", procedure.practicePatientId)
            reqObj.addProperty("procedure_map_id", procedure.proceduresMapId)


            return reqObj
        }


        //endregion

        //region Vaccine


        fun formAddVaccineobj(immunizationPOJO: VaccineImmunizationPOJO): JsonObject {

            val reqObj = JsonObject()

            reqObj.addProperty(
                "immune_name",
                immunizationPOJO.patientImmunizationMap?.immunizationList?.immunizationName
            )
            reqObj.addProperty(
                "immune_type",
                immunizationPOJO.patientImmunizationMap?.immunizationList?.immunizationType
            )
            reqObj.addProperty(
                "how_administered",
                immunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.howAdministered
            )
            reqObj.addProperty(
                "given_by",
                immunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.givenBy
            )
            reqObj.addProperty(
                "manufacturer",
                immunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.manufacturer
            )
            reqObj.addProperty(
                "comments",
                immunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.comments
            )
            reqObj.addProperty(
                "from_date",
                immunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.vaccinesOn?.toString(
                    ModelUtil.charmDisplayDateFormat()
                )
            )
            reqObj.addProperty("patient_id", immunizationPOJO.patientImmunizationMap?.patientId)

            return reqObj
        }

        fun formEditVaccineobj(immunizationPOJO: VaccineImmunizationPOJO): JsonObject {

            val reqObj = JsonObject()

            reqObj.addProperty(
                "how_administered",
                immunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.howAdministered
            )
            reqObj.addProperty(
                "given_by",
                immunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.givenBy
            )
            reqObj.addProperty(
                "manufacturer",
                immunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.manufacturer
            )
            reqObj.addProperty(
                "comments",
                immunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.comments
            )
            reqObj.addProperty(
                "from_date",
                immunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.vaccinesOn?.toString(
                    ModelUtil.charmDisplayDateFormat()
                )
            )
            reqObj.addProperty("patient_id", immunizationPOJO.patientImmunizationMap?.patientId)
            reqObj.addProperty(
                "immune_history_id",
                immunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.immunizationHistoryId
            )

            return reqObj
        }

        fun formDeleteVaccineobj(immunizationPOJO: VaccineImmunizationPOJO): JsonObject {

            val reqObj = JsonObject()
            reqObj.addProperty("patient_id", immunizationPOJO.patientImmunizationMap?.patientId)
            reqObj.addProperty(
                "immune_history_id",
                immunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.immunizationHistoryId
            )

            return reqObj
        }


        //endregion

        //region Vitals

        fun formAddVitalObj(patVitalTracker: PatientVitalTracker): JsonObject {
            val postObj = JsonObject()

            postObj.addProperty(
                "date_of_entry",
                patVitalTracker.dateOfEntry?.toString(ModelUtil.sqlTimeStampFormattor())
            )
            postObj.addProperty("notes", patVitalTracker.notes)
            postObj.addProperty("patient_id", patVitalTracker.patientId)
            postObj.addProperty("tracker_id", patVitalTracker.tracker?.trackerId)
            //   postObj.addProperty("tracker_name" , patVitalTracker.tracker?.trackerName)

            val metricsArr = JsonArray()

            patVitalTracker.patientVitalsList.forEach { patientVitals ->
                val metricsObj = JsonObject()
                metricsObj.addProperty("metric_id", patientVitals.metricId)
                if (BuildUtil.isThalassemia) {
                    metricsObj.addProperty("tracked_unit", patientVitals.trackedValueUnit)
                }
                metricsObj.addProperty("tracked_value", patientVitals.trackedValue)
                metricsArr.add(metricsObj)
            }

            postObj.add("tracked_entry", metricsArr)

            return postObj
        }

        fun formAddMultipleVitalObj(
            patVitalTrackerList: ArrayList<PatientVitalTracker>,
            patientId: String
        ): JsonObject {
            val postObj = JsonObject()
            val tempPatVitalTracker = patVitalTrackerList.firstOrNull()
            postObj.addProperty(
                "date_of_entry",
                tempPatVitalTracker?.dateOfEntry?.toString(ModelUtil.sqlTimeStampFormattor())
            )
            postObj.addProperty("notes", tempPatVitalTracker?.notes)
            postObj.addProperty("patient_id", patientId)
            val trackerArr = JsonArray()
            patVitalTrackerList.forEach { patVitalTracker ->
                val trackerJson = JsonObject()
                trackerJson.addProperty("tracker_id", patVitalTracker.tracker?.trackerId)
                val metricsArr = JsonArray()

                patVitalTracker.patientVitalsList.forEach { patientVitals ->
                    val metricsObj = JsonObject()
                    metricsObj.addProperty("metric_id", patientVitals.metricId)
                    // metricsObj.addProperty("tracked_unit" , patientVitals.trackedValueUnit)
                    metricsObj.addProperty("tracked_value", patientVitals.trackedValue)
                    metricsArr.add(metricsObj)
                }

                trackerJson.add("tracked_entry", metricsArr)
                trackerArr.add(trackerJson)
            }
            postObj.add("multi_tracker_entry", trackerArr)
            return postObj
        }

        fun formUpdateVitalUnitObj(unitSystem: String, patientId: String): JsonObject {
            val postObj = JsonObject()

            postObj.addProperty("unit_system", unitSystem)
            postObj.addProperty("patient_id", patientId)

            return postObj
        }


        fun formHealthMetricObj(healthMetric: HealthMetric): JsonObject {
            val postObj = JsonObject()

            if (!healthMetric.healthMetricId.isNullOrBlank()) {
                postObj.addProperty("health_metric_id", healthMetric.healthMetricId)
            }
            postObj.addProperty("description", healthMetric.description)
            postObj.addProperty("patient_id", healthMetric.patientId)
            postObj.addProperty("health_metric_unit", healthMetric.healthMetricUnit)
            postObj.addProperty("health_metric_name", healthMetric.healthMetricName)

            return postObj
        }

        fun formDeleteHealthMetricObj(healthMetric: HealthMetric): JsonObject {
            val postObj = JsonObject()

            postObj.addProperty("health_metric_id", healthMetric.healthMetricId)
            postObj.addProperty("patient_id", healthMetric.patientId)

            return postObj
        }
        //endregion

        //region Payment

        fun formTransactionObj(
            transactionInfo: TransactionInfo,
            paymentInfo: PaymentInfo?,
            isStatement: Boolean
        ): JsonObject {
            val postObj = JsonObject()

            //postObj.addProperty("patient_id", transactionInfo.patientId)
            postObj.addProperty("practice_id", transactionInfo.practiceId)
            postObj.addProperty("facility_id", transactionInfo.facilityId)


            if (!transactionInfo.memberId.isNullOrBlank()) {
                postObj.addProperty("member_id", transactionInfo.memberId)
            }


            if (!transactionInfo.merchantAccountId.isNullOrBlank()) {
                postObj.addProperty("merchant_account_id", transactionInfo.merchantAccountId)
            }

            if (!transactionInfo.billId.isNullOrBlank()) {
                if (isStatement) {
                    postObj.addProperty("statement_id", transactionInfo.billId)
                } else {
                    postObj.addProperty("bill_id", transactionInfo.billId)
                }
            }

            postObj.addProperty("service", transactionInfo.service)
            if (paymentInfo?.transactionType == ModelUtil.appointmentTransactionTypeAuthorization) {
                postObj.addProperty("amount", "0.00")
            } else {
                postObj.addProperty("amount", transactionInfo.paymentAmount)
            }
            postObj.addProperty("gateway", transactionInfo.gateway)
            postObj.addProperty("_", DateTime.now().millis)
            postObj.addProperty("transaction_type", paymentInfo?.transactionType)

            val formDataJson = JsonObject()
            if (paymentInfo?.transactionType == ModelUtil.appointmentTransactionTypeSale) {
                val billingJson = JsonObject()
                billingJson.addProperty("CLEAR_AMOUNT", paymentInfo.transactionInfo?.paymentAmount)
                billingJson.addProperty("BILL_ID", paymentInfo.transactionInfo?.billId)
                val jsonArr = JsonArray()
                jsonArr.add(billingJson)
                formDataJson.add("invoices", jsonArr)
                formDataJson.addProperty("payment_from", "Patient")
                formDataJson.addProperty("transaction_amount", paymentInfo.chargeAmount)
            }
            //added to save card nick name
            if (paymentInfo?.cardNickName != null || paymentInfo?.postalAddress != null) {
                if (paymentInfo.cardNickName != null) {
                    formDataJson.addProperty("card_nick_name", paymentInfo.cardNickName)
                }
                if (paymentInfo.postalAddress != null) {
                    formDataJson.addProperty("street_address1", paymentInfo.postalAddress?.line1)
                    formDataJson.addProperty("street_address2", paymentInfo.postalAddress?.line2)
                    formDataJson.addProperty("city", paymentInfo.postalAddress?.city)
                    formDataJson.addProperty("state", paymentInfo.postalAddress?.state)
                    formDataJson.addProperty("country", paymentInfo.postalAddress?.country)
                    formDataJson.addProperty("zip", paymentInfo.postalAddress?.postalCode)
                }
            }
            formDataJson.addProperty("token_id", paymentInfo?.token)
            formDataJson.addProperty("transaction_type", paymentInfo?.transactionType)

            postObj.addProperty("custom_data", formDataJson.toString())

            return postObj

        }

        fun formUpdateTransactionObj(practiceID: String): JsonObject {
            val postObj = JsonObject()
            postObj.addProperty("practice_id", practiceID)
            postObj.addProperty("gateway", ModelUtil.appointmentGatewayTypeRazorpay)

            return postObj
        }

        fun formPaymentPostObj(
            paymentInfo: PaymentInfo,
            responseTransactionObj: JsonObject,
            groupId: String?
        ): HashMap<String, Any> {


            val postObj = JsonObject()


            bluefinReqParams.forEach {
                val jsonEle = responseTransactionObj.get(it)
                if (jsonEle != null && jsonEle.isNotJsonNull()) {
                    if (it == "custom_data") {
                        val jsonObject = jsonEle.asString.parseStringAsJsonObject()
                        if (paymentInfo.transactionType == ModelUtil.appointmentTransactionTypeSale) {
                            val billingJson = JsonObject()
                            billingJson.addProperty(
                                "CLEAR_AMOUNT",
                                paymentInfo.transactionInfo?.paymentAmount
                            )
                            billingJson.addProperty("BILL_ID", paymentInfo.transactionInfo?.billId)
                            val jsonArr = JsonArray()
                            jsonArr.add(billingJson)
                            jsonObject?.add("invoices", jsonArr)
                            jsonObject?.addProperty("payment_from", "Patient")
                            jsonObject?.addProperty("transaction_amount", paymentInfo.chargeAmount)
                            jsonObject?.addProperty(
                                "custom_id",
                                responseTransactionObj.get("transactionId")?.asString
                            )
                        } else {
                            jsonObject?.addProperty("is_phr_card", true)
                            jsonObject?.addProperty("is_primary_card", paymentInfo.isPrimaryCard)
                            if (!groupId.isNullOrEmpty()) {
                                jsonObject?.addProperty("group_id", groupId)
                            }
                        }
                        postObj.addProperty(it, jsonObject.toString())
                    } else {
                        postObj.add(it, jsonEle)
                    }
                }
            }

            postObj.addProperty("account_id", responseTransactionObj.get("accountId")?.asString)
            //  postObj.addProperty("transaction_amount" , responseTransactionObj.get("amount")?.asString)
            postObj.addProperty("custom_id", responseTransactionObj.get("transactionId")?.asString)

            postObj.addProperty("transaction_type", paymentInfo.transactionType)
            postObj.addProperty("tender_type", "CARD")

            if (paymentInfo.transactionType == ModelUtil.appointmentTransactionTypeSale) {
                postObj.addProperty("transaction_amount", paymentInfo.chargeAmount)
            } else {
                postObj.addProperty("transaction_amount", "0.00")
            }

            //  responseTransactionObj.addProperty("facility_id" , paymentInfo.transactionInfo?.facilityId)
            // responseTransactionObj.addProperty("member_id" , paymentInfo.transactionInfo?.memberId)


            if (paymentInfo.token?.isNotBlank() == true) {
                postObj.addProperty("token_id", paymentInfo.token)
                postObj.addProperty("reissue", "true")
            } else {
                postObj.addProperty("card_number", paymentInfo.cardNumber)
                postObj.addProperty("card_verification", paymentInfo.cardCvv)
                //     responseTransactionObj.addProperty("exp_month" , paymentInfo.cardExpMonth)
                //    responseTransactionObj.addProperty("exp_year" , paymentInfo.cardExpYear)
                postObj.addProperty(
                    "card_expiration",
                    paymentInfo.cardExpMonth + (paymentInfo.cardExpYear ?: "").substring(2)
                )
                val caregiverArr = paymentInfo.cardHolderName?.split(" ") ?: arrayListOf()
                postObj.addProperty("first_name", caregiverArr[0].trim())
                postObj.addProperty("last_name", caregiverArr.drop(1).joinToString(" "))
            }

            if (paymentInfo.postalAddress != null) {
                postObj.addProperty("street_address1", paymentInfo.postalAddress?.line1)
                postObj.addProperty("street_address2", paymentInfo.postalAddress?.line2)
                postObj.addProperty("country", paymentInfo.postalAddress?.country)
                postObj.addProperty("state", paymentInfo.postalAddress?.state)
                postObj.addProperty("city", paymentInfo.postalAddress?.city)
                postObj.addProperty("zip", paymentInfo.postalAddress?.postalCode)
            }


            return Gson().fromJson(postObj.toString(), HashMap::class.java) as HashMap<String, Any>
        }

        //endregion

        //region Card on file

        fun formPaymentGatewayTokenObj(paymentGatewayToken: PaymentGatewayToken): JsonObject {
            val postObj = JsonObject()
            postObj.addProperty("CARD_BRAND", paymentGatewayToken.cardBrand)
            postObj.addProperty("CARD_DIGIT", paymentGatewayToken.cardDigit)
            postObj.addProperty("CARD_NICK_NAME", paymentGatewayToken.cardNickName)
            postObj.addProperty("EXPIRY_DATE", paymentGatewayToken.expiry)
            postObj.addProperty("FACILITY_ID", paymentGatewayToken.facilityId)
            postObj.addProperty("HAS_CARD_EXPIRED", paymentGatewayToken.hasCardExpired)
            postObj.addProperty("IS_PHR_CARD", paymentGatewayToken.isPHRCard)
            postObj.addProperty("IS_PRIMARY_CARD", paymentGatewayToken.isPrimaryCard)
            postObj.addProperty("MERCHANT_ACCOUNT_ID", paymentGatewayToken.merchantAccountId)
            postObj.addProperty("NAME_ON_CARD", paymentGatewayToken.nameOnCard)
            postObj.addProperty("PATIENT_ID", paymentGatewayToken.patientId)
            postObj.addProperty("practice_id", paymentGatewayToken.practiceId)
            postObj.addProperty("PRACTICE_PATIENT_ID", paymentGatewayToken.practicePatientId)
            postObj.addProperty("TOKEN_ID", paymentGatewayToken.tokenId)
            postObj.addProperty("TOKEN", paymentGatewayToken.token)
            postObj.addProperty("STREET_ADDRESS1", paymentGatewayToken.postalAddress?.line1)
            postObj.addProperty("STREET_ADDRESS2", paymentGatewayToken.postalAddress?.line2)
            postObj.addProperty("COUNTRY", paymentGatewayToken.postalAddress?.country)
            postObj.addProperty("STATE", paymentGatewayToken.postalAddress?.state)
            postObj.addProperty("CITY", paymentGatewayToken.postalAddress?.city)
            postObj.addProperty("ZIP", paymentGatewayToken.postalAddress?.postalCode)
            return postObj
        }

        fun formUpdateCardOnFileObj(paymentGatewayToken: PaymentGatewayToken): JsonObject {
            val postObj = JsonObject()
            postObj.addProperty("patient_id", paymentGatewayToken.patientId)
            postObj.addProperty("practice_id", paymentGatewayToken.practiceId)
            postObj.addProperty("token_id", paymentGatewayToken.tokenId)
            postObj.addProperty("is_primary_card", paymentGatewayToken.isPrimaryCard)
            postObj.addProperty("nick_name", paymentGatewayToken.cardNickName)
            postObj.addProperty("street_address1", paymentGatewayToken.postalAddress?.line1)
            postObj.addProperty("street_address2", paymentGatewayToken.postalAddress?.line2)
            postObj.addProperty("country", paymentGatewayToken.postalAddress?.country)
            postObj.addProperty("state", paymentGatewayToken.postalAddress?.state)
            postObj.addProperty("city", paymentGatewayToken.postalAddress?.city)
            postObj.addProperty("zipcode", paymentGatewayToken.postalAddress?.postalCode)
            return postObj
        }

        fun formDeleteCardOnFileObj(paymentGatewayToken: PaymentGatewayToken): JsonObject {
            val postObj = JsonObject()
            postObj.addProperty("patient_id", paymentGatewayToken.patientId)
            postObj.addProperty("practice_id", paymentGatewayToken.practiceId)
            postObj.addProperty("token_id", paymentGatewayToken.tokenId)
            return postObj
        }

        fun formAddressJson(address: PostalAddress?): JsonObject {
            val postObj = JsonObject()
            postObj.addProperty("LINE_1", address?.line1)
            postObj.addProperty("LINE_2", address?.line2)
            postObj.addProperty("COUNTRY", address?.country)
            postObj.addProperty("STATE", address?.state)
            postObj.addProperty("CITY", address?.city)
            postObj.addProperty("POSTALCODE", address?.postalCode)
            return postObj
        }

        fun formGroupMerchantAndPatientsObj(patientId: String, practiceId: String): JsonObject {
            val postObj = JsonObject()
            postObj.addProperty("patient_id", patientId)
            postObj.addProperty("practice_id", practiceId)
            return postObj
        }
        //endregion

        //region Contacts

        fun formSaveContactObj(pharmacy: Pharmacy): JsonObject {
            val postObj = JsonObject()

            postObj.addProperty("Pharmacy_Name", pharmacy.pharmacyName)
            //  postObj.addProperty("patient_id", pharmacy.patientId)
            postObj.addProperty("Mobile", pharmacy.contactDetails?.mobile)
            postObj.addProperty("Health_Passport", pharmacy.isHealthPassport)
            postObj.addProperty("Contact_Type", pharmacy.contactType)

            /*
            */

            if (!pharmacy.pharmacyId.isNullOrBlank()) {
                postObj.addProperty("Pharmacy_Id", pharmacy.pharmacyId)
            }

            return postObj
        }

        fun formContactObjList(pharmacyList: List<Pharmacy>): JsonObject {

            val postObj = JsonObject()

            val contactArr = JsonArray()

            pharmacyList.forEach { pharmacy ->

                val contactObj = JsonObject()
                contactObj.addProperty("Contact_Type", "Other")
                contactObj.addProperty("EmailID", pharmacy.contactDetails?.emailId)
                contactObj.addProperty("Pharmacy_Name", pharmacy.pharmacyName)
                // contactObj.addProperty("patient_id", pharmacy.patientId)
                contactArr.add(contactObj)
            }

            postObj.add("contacts", contactArr)

            return postObj
        }

        fun formPharmacyObjList(pharmacyList: List<Pharmacy>): JsonObject {

            val postObj = JsonObject()

            val contactArr = JsonArray()

            pharmacyList.forEach { pharmacy ->
                val contactObj = JsonObject()
                contactObj.addProperty("EMAIL_ID", pharmacy.contactDetails?.emailId)
                contactObj.addProperty("NAME", pharmacy.pharmacyName)
                contactObj.addProperty("ID", pharmacy.patientPharmaMapId)
                contactArr.add(contactObj)
            }

            postObj.add("Patient_Details", contactArr)

            return postObj
        }

        //endregion

        // region Document

        fun formDocumentListJson(documentList: ArrayList<Document>): JsonObject {

            val outputStringJson = JsonObject()
            val jsonArray = JsonArray()

            documentList.forEach { document ->

                val documentObj = JsonObject()

                val category = document.category
                documentObj.addProperty("CATEGORY_ID", category?.categoryId ?: "")
                documentObj.addProperty("CATEGORY_NAME", category?.categoryName ?: "")
                documentObj.addProperty("CATEGORY_DESCRIPTION", category?.categoryDescription ?: "")

                documentObj.addProperty("FILE_ID", document.fileId ?: "")
                documentObj.addProperty("FILE_NAME", document.fileName ?: "")
                documentObj.addProperty("FILE_SIZE", document.fileSize ?: "")
                documentObj.addProperty("FILE_TYPE", document.fileType ?: "")

                documentObj.addProperty(
                    "TIME_OF_UPLOAD",
                    document.timeOfUpload?.millis?.toString() ?: ""
                )
                documentObj.addProperty("CONTENT_TYPE", document.contentType ?: "")
                documentObj.addProperty("READ_STATUS", document.readStatus)
                documentObj.addProperty("SENDER_ID", document.senderId)
                documentObj.addProperty("SENT_BY", document.sentBy)
                documentObj.addProperty(
                    "DATE",
                    document.date?.toString(ModelUtil.charmDisplayDateFormat())
                )

                val sharedJsonArray = JsonArray()
                val joinString = document.sharedToList.joinToString(", ")
                sharedJsonArray.add(joinString)
                documentObj.add("SHARED_BY_ME_PRAC_NAME", sharedJsonArray)

                jsonArray.add(documentObj)
            }

            outputStringJson.add("output_string", jsonArray)

            return outputStringJson

        }

        fun formDocumentObj(document: Document): ArrayList<MultipartBody.Part>? {

            /*val postObj = JsonObject()

            postObj.addProperty("document" , FileUtil.readFileToString(document.filePath ?:""))
            postObj.addProperty("health_passport" ,document.isHealthPassport)
            postObj.addProperty("category_name" , document.category?.categoryName)


            if (!document.category?.categoryId.isNullOrBlank())
                postObj.addProperty("category_id" , document.category?.categoryId)

            return postObj*/

            val fileReq = FileUtil.createRequestBodyForFile(
                document.filePath
                    ?: ""
            )
            fileReq?.let {
                val fileMultiPartBody =
                    MultipartBody.Part.createFormData("document", document.fileName, fileReq)

                val listOFPart = arrayListOf<MultipartBody.Part>()
                listOFPart.add(fileMultiPartBody)
                listOFPart.add(
                    MultipartBody.Part.createFormData(
                        "health_passport",
                        document.isHealthPassport.toString()
                    )
                )
                listOFPart.add(
                    MultipartBody.Part.createFormData(
                        "category_name",
                        document.category?.categoryName ?: ""
                    )
                )

                if (!document.category?.categoryId.isNullOrBlank()) {
                    listOFPart.add(
                        MultipartBody.Part.createFormData(
                            "category_id",
                            document.category?.categoryId!!
                        )
                    )
                }
                return listOFPart
            }
            return null
        }

        fun formCategoryObj(category: Category): JsonObject {
            val postObj = JsonObject()

            if (!category.categoryId.isNullOrBlank()) {
                postObj.addProperty("category_id", category.categoryId)
            }

            postObj.addProperty("name", category.categoryName)
            postObj.addProperty("description", category.categoryDescription)
            return postObj
        }

        fun formDeleteCategoryObj(category: Category): JsonObject {
            val postObj = JsonObject()
            postObj.addProperty("category_id", category.categoryId)
            return postObj
        }

        fun formShareDocumentToOthersObj(
            documents: ArrayList<Document>,
            contactList: ArrayList<Pharmacy>,
            expiryDate: DateTime
        ): JsonObject {
            val postObj = JsonObject()

            val documentArr = JsonArray()
            documents.forEach {
                documentArr.add(it.fileId)
            }
            postObj.add("Document_id", documentArr)

            val pharmacyArr = JsonArray()
            contactList.forEach {
                pharmacyArr.add(it.patientPharmaMapId)
            }
            postObj.add("Pharmacy_id", pharmacyArr)

            postObj.addProperty("expiry", expiryDate.toString(ModelUtil.charmDisplayDateFormat()))
            postObj.addProperty("access_limit", "-1")
            return postObj
        }

        fun formMemberArray(memberList: ArrayList<Member>): JsonArray {
            val toListArr = JsonArray()
            var toListArrMap = hashMapOf<String, JsonArray>()
            memberList.forEach {
                if (toListArrMap.containsKey(it.practiceId)) {
                    toListArrMap.get(it.practiceId)?.add(it.memberId)
                } else {
                    val memberArr = JsonArray()
                    memberArr.add(it.memberId)
                    toListArrMap.put(it.practiceId ?: "", memberArr)
                }
            }
            toListArrMap.forEach {
                val toListObj = JsonObject()
                toListObj.addProperty("practice_id", it.key)
                toListObj.add("members_to_notify", it.value)

                toListArr.add(toListObj)
            }
            return toListArr
        }

        fun formShareDocumentToMembersObj(
            documents: ArrayList<Document>,
            memberList: ArrayList<Member>
        ): JsonObject {
            val postObj = JsonObject()

            val documentArr = JsonArray()
            documents.forEach {
                documentArr.add(it.fileId)
            }
            postObj.add("document_list", documentArr)

            val memberAr = JsonArray()

            memberList.forEach {
                memberAr.add(it.memberId)
            }

            val toListArr = formMemberArray(memberList)

            postObj.add("to_list", toListArr)

            return postObj
        }


        //region support

        fun formSendSupportJsonObject(supportDetails: SupportDetails): JsonObject {

            val jsonObject = JsonObject()
            jsonObject.addProperty("subject", supportDetails.subject)
            jsonObject.addProperty("msg_content", supportDetails.messageContent)
            jsonObject.addProperty("category", supportDetails.category)
            val fileIdsString = supportDetails.fileIds.joinToString(",")
            jsonObject.addProperty("file_ids", fileIdsString)
            jsonObject.addProperty("msg_content_type", supportDetails.messageContentType)
            jsonObject.addProperty("is_mobile", "true")
            return jsonObject

        }


        //endregion

        // region Medical Condition

        fun formAddMedConditionObj(patientMedicalCondition: PatientMedicalCondition): JsonObject {

            val postObj = JsonObject()

            postObj.addProperty("health_passport", patientMedicalCondition.isHealthPassport)
            postObj.addProperty(
                "condition_id",
                patientMedicalCondition.medicalCondition?.medicalConditionId
            )
            postObj.addProperty(
                "condition_name",
                patientMedicalCondition.medicalCondition?.medicalConditionName
            )
            postObj.addProperty(
                "from_date",
                patientMedicalCondition.dateOfOnset?.toString(ModelUtil.charmDisplayDateFormat())
            )
            postObj.addProperty("condition_status", patientMedicalCondition.status)
            postObj.addProperty("patient_id", patientMedicalCondition.patientId)

            if (!patientMedicalCondition.isHealthPassport) {
                postObj.addProperty("notes", patientMedicalCondition.notes)
            }

            return postObj
        }

        fun formEditMedConditionObj(patientMedicalCondition: PatientMedicalCondition): JsonObject {

            val postObj = JsonObject()

            postObj.addProperty("health_passport", patientMedicalCondition.isHealthPassport)
            postObj.addProperty("cond_map_id", patientMedicalCondition.medicalConditionMapId)
            postObj.addProperty(
                "from_date",
                patientMedicalCondition.dateOfOnset?.toString(ModelUtil.charmDisplayDateFormat())
            )
            postObj.addProperty("condition_status", patientMedicalCondition.status)
            postObj.addProperty("patient_id", patientMedicalCondition.patientId)

            if (!patientMedicalCondition.isHealthPassport) {
                postObj.addProperty("notes", patientMedicalCondition.notes)
            }

            return postObj
        }

        fun formDeleteMedConditionObj(patientMedicalCondition: PatientMedicalCondition): JsonObject {

            val postObj = JsonObject()

            postObj.addProperty("cond_map_id", patientMedicalCondition.medicalConditionMapId)
            postObj.addProperty("patient_id", patientMedicalCondition.patientId)

            return postObj
        }

        //endregion

        //region Insurance

        fun formAddInsuranceObj(
            insurance: PatientInsuranceDetails,
            sendType: Boolean = false
        ): JsonObject {
            val postObj = JsonObject()


            if (!insurance.patientInsuranceDetailsId.isNullOrBlank()) {
                postObj.addProperty("INSURED_ID", insurance.patientInsuranceDetailsId)
            }

            if (sendType) {
                postObj.addProperty("TYPE", insurance.type)

            } else {
                postObj.addProperty("IS_PRIMARY", insurance.isPrimary)
                postObj.addProperty("practice_id", insurance.practiceId)
                //   postObj.addProperty("patient_id", insurance.practicePatientId)

            }
            postObj.addProperty("INSURANCE_TYPE", insurance.insurancePlanType)
            postObj.addProperty("PLAN_NAME", insurance.insurancePlanName)
            postObj.addProperty("ID_NUMBER", insurance.policyNumber)
            postObj.addProperty(
                "from_date",
                insurance.policyStartDate?.toString(ModelUtil.charmDisplayDateFormat())
            )
            postObj.addProperty(
                "to_date",
                insurance.policyEndDate?.toString(ModelUtil.charmDisplayDateFormat())
            )
            postObj.addProperty("POLICY_GROUP_NUMBER", insurance.groupNumber)
            postObj.addProperty("COPAY", insurance.copay)
            postObj.addProperty("DEDUCTABLE", insurance.deductable)
            postObj.addProperty("COMMENT", insurance.comment)


            val payer = insurance.practicePayer

            if (payer != null) {
                postObj.addProperty("COMPANY_NAME", payer.payerName)
                postObj.addProperty("COMPANY_CODE", payer.payerId ?: "")

                val payerAddress = payer.postalAddress
                if (payerAddress != null) {
                    postObj.addProperty("PAYER_ADDR_LIINE1", payerAddress.line1)
                    postObj.addProperty("PAYER_CITY", payerAddress.city)
                    postObj.addProperty("PAYERCOUNTRY", payerAddress.country)
                    postObj.addProperty("PAYERSTATE", payerAddress.state)
                    postObj.addProperty("PAYERZIP", payerAddress.postalCode)
                }
            }

            val insuredAndGuarantorDetails = insurance.insuredAndGuarantorDetails

            if (insuredAndGuarantorDetails != null) {
                postObj.addProperty("RELATIONSHIP", insuredAndGuarantorDetails.relationship)
                postObj.addProperty("FIRST_NAME", insuredAndGuarantorDetails.firstName)
                postObj.addProperty("LAST_NAME", insuredAndGuarantorDetails.lastName)
                postObj.addProperty(
                    "GENDER",
                    insuredAndGuarantorDetails.gender?.lowercase(Locale.ENGLISH)
                )
                postObj.addProperty(
                    "birth_date",
                    insuredAndGuarantorDetails.dateOfBirth?.toString(ModelUtil.charmDisplayDateFormat())
                )


                val address = insuredAndGuarantorDetails.postalAddress

                if (address != null) {
                    postObj.addProperty("LINE_1", address.line1)
                    postObj.addProperty("LINE_2", address.line2)
                    postObj.addProperty("COUNTRY", address.country)
                    postObj.addProperty("STATE", address.state)
                    postObj.addProperty("CITY", address.city)
                    postObj.addProperty("POSTALCODE", address.postalCode)

                }

                val contact = insuredAndGuarantorDetails.contactDetails

                if (contact != null) {
                    postObj.addProperty("LANDLINE", contact.homePhone)
                    postObj.addProperty("MOBILE", contact.mobile)
                }


                val insuredEmployerDetails = insuredAndGuarantorDetails.insuredEmployerDetails
                if (insuredEmployerDetails != null) {
                    postObj.addProperty("EMPLOYER_NAME", insuredEmployerDetails.name)
                }
            }


            return postObj
        }


        fun formDeleteInsuranceObj(patientInsuranceDetails: PatientInsuranceDetails): JsonObject {
            val postObj = JsonObject()

            postObj.addProperty("insured_id", patientInsuranceDetails.patientInsuranceDetailsId)
            postObj.addProperty("patient_id", patientInsuranceDetails.practicePatientId)

            return postObj
        }
        //endregion

        //region Swaasa
        fun formSwaasaObj(moduleList: List<SwaasaModel>): JsonObject {

            val postObj = JsonObject()
            moduleList.forEach { model ->
                postObj.addProperty(model.keyName, model.isSelected)
            }
            return postObj
        }
        //endregion

        //region shared pref db parse

        fun formPatientList(patientList: ArrayList<Patient>): String {

            val patientArr = JsonArray()

            patientList.forEach { patient ->
                val patientObj = JsonObject()
                patientObj.addProperty("patient_id", patient.practicePatientId)
                patientObj.addProperty("first_name", patient.firstName)
                patientObj.addProperty("last_name", patient.lastName)
                if (patient.dateOfBirth != null) {
                    patientObj.addProperty(
                        "date_of_birth",
                        patient.dateOfBirth?.toString(ModelUtil.charmApiDateFormat)
                    )
                } else {
                    patientObj.addProperty("date_of_birth", "")
                }

                patientObj.addProperty(
                    "is_health_passport_available",
                    patient.isHealthPassportAvailable
                )
                patientObj.addProperty("gender", patient.gender)

                if (patient.age != null) {
                    patientObj.addProperty("age", patient.age)
                } else {
                    patientObj.addProperty("age", "")
                }

                if (!patient.bloodGroup.isNullOrBlank()) {
                    patientObj.addProperty("blood_group", patient.bloodGroup)
                } else {
                    patientObj.addProperty("blood_group", "")
                }

                patientObj.addProperty("is_photo_available", patient.isPhotoAvailableOnline)

                if (patient.unitSystem != null) {
                    patientObj.addProperty("unit_system", patient.unitSystem)
                }

                val practiceArr = JsonArray()

                patient.practiceList.forEach { practice ->

                    val practiceObj = JsonObject()
                    practiceObj.addProperty("practice_id", practice.practiceId)
                    practiceObj.addProperty("practice_name", practice.practiceName)
                    practiceObj.addProperty("is_active", practice.isActive)
                    practiceObj.addProperty("facility_list", practice.facilityList)
                    practiceObj.addProperty("IS_BLUEFIN_ENABLED", practice.isBluefinEnabled)

                    practiceArr.add(practiceObj)
                }

                patientObj.add("practice_list", practiceArr)
                patientArr.add(patientObj)
            }

            return patientArr.toString()
        }

        fun formMenuList(menuList: MenuList): String {

            val menuObj = JsonObject()

            menuObj.addProperty("appointment", menuList.appointment)
            menuObj.addProperty("billing", menuList.billing)
            menuObj.addProperty("message", menuList.message)
            menuObj.addProperty("lab", menuList.lab)
            menuObj.addProperty("prescription", menuList.prescription)
            menuObj.addProperty("visitSummary", menuList.visitSummary)
            menuObj.addProperty("announcement", menuList.announcement)
            menuObj.addProperty("about", menuList.about)
            menuObj.addProperty("usefulInfo", menuList.usefulInfo)

            return menuObj.toString()
        }

        fun formComposeMsgRestrictionList(composeMsgRestrictionList: List<ComposeMsgRestriction>): String {

            val composeMsgRestrictionArr = JsonArray()

            composeMsgRestrictionList.forEach { composeMsgRestriction ->

                val composeMsgRestrictionObj = JsonObject()

                composeMsgRestrictionObj.addProperty(
                    "allowPHRMessaging",
                    composeMsgRestriction.allowPHRMessaging
                )
                composeMsgRestrictionObj.addProperty(
                    "isMessageRestrict",
                    composeMsgRestriction.isMessageRestrict
                )
                composeMsgRestrictionObj.addProperty(
                    "noOfCharsAllowed",
                    composeMsgRestriction.noOfCharsAllowed
                )
                composeMsgRestrictionObj.addProperty("practiceId", composeMsgRestriction.practiceId)

                composeMsgRestrictionArr.add(composeMsgRestrictionObj)
            }

            return composeMsgRestrictionArr.toString()
        }

        //endregion

        fun formSupportData(supportData: SupportData, fileIds: String): JsonObject {

            val postObj = JsonObject()

            postObj.addProperty("subject", supportData.subject)
            postObj.addProperty("msg_content", supportData.description)
            postObj.addProperty("category", supportData.category)
            postObj.addProperty(
                "msg_content_type",
                "text"
            ) //allowed values (text|html) default value text
            postObj.addProperty("is_mobile", true)
            if (fileIds.isNotEmpty()) {
                postObj.addProperty("file_ids", fileIds)
            }
            return postObj
        }

        //region Reminders

        fun formReminderJsonObj(reminderPOJO: ReminderPOJO): JsonObject {
            val postObj = JsonObject()
            postObj.addProperty(
                "start_date",
                reminderPOJO.reminder?.startDate?.toString(ModelUtil.charmDisplayDateFormat())
            )
            if (reminderPOJO.reminderMap.firstOrNull()?.practiceId != null)
                postObj.addProperty(
                    "practice_id",
                    reminderPOJO.reminderMap.firstOrNull()?.practiceId
                )
            val frequencyObj = JsonObject()
            frequencyObj.addProperty(
                "frequency_value",
                reminderPOJO.reminder?.frequency?.toIntOrNull() ?: 0
            )
            if (reminderPOJO.reminder?.frequencyUnit != null)
                frequencyObj.addProperty("frequency_unit", reminderPOJO.reminder?.frequencyUnit)
            val durationArr = JsonArray()
            val reminderFrequencyMap = hashMapOf<Int, JsonArray>()
            reminderPOJO.reminderFrequencyMap.forEach {
                if (reminderFrequencyMap.containsKey(it.day)) {
                    reminderFrequencyMap[it.day]?.add(it.time)
                } else {
                    val timeArr = JsonArray()
                    timeArr.add(it.time)
                    reminderFrequencyMap[it.day] = timeArr
                }
            }
            reminderFrequencyMap.forEach {
                val durationJson = JsonObject()
                if (it.key == -1) {
                    durationJson.addProperty("day", "not_configured")
                } else {
                    durationJson.addProperty("day", it.key)
                }
                durationJson.add("time", it.value)
                durationArr.add(durationJson)
            }
            frequencyObj.add("duration", durationArr)
            postObj.add("frequency", frequencyObj)
            if (reminderPOJO.reminder?.endDate != null) {
                postObj.addProperty(
                    "end_date",
                    reminderPOJO.reminder?.endDate?.toString(ModelUtil.charmDisplayDateFormat())
                )
            }
            postObj.addProperty("module_id", reminderPOJO.reminderMap.firstOrNull()?.moduleId)
            postObj.addProperty("type", reminderPOJO.reminderMap.firstOrNull()?.type)
            return postObj
        }

        fun formReminderJsonObjWithLocalId(reminderPOJO: ReminderPOJO): JsonObject {
            val postObj = JsonObject()
            postObj.addProperty(
                "start_date",
                reminderPOJO.reminder?.startDate?.toString(ModelUtil.charmDisplayDateFormat())
            )
            if (reminderPOJO.reminderMap.firstOrNull()?.practiceId != null)
                postObj.addProperty(
                    "practice_id",
                    reminderPOJO.reminderMap.firstOrNull()?.practiceId
                )
            val frequencyObj = JsonObject()
            frequencyObj.addProperty(
                "frequency_value",
                reminderPOJO.reminder?.frequency?.toIntOrNull() ?: 0
            )
            if (reminderPOJO.reminder?.frequencyUnit != null)
                frequencyObj.addProperty("frequency_unit", reminderPOJO.reminder?.frequencyUnit)
            val durationArr = JsonArray()
            reminderPOJO.reminderFrequencyMap.forEach {
                val durationJson = JsonObject()
                if (it.day == -1) {
                    durationJson.addProperty("day", "not_configured")
                } else {
                    durationJson.addProperty("day", it.day)
                }
                durationJson.addProperty("time", it.time)
                durationJson.addProperty("localFrequencyMapId", it.localFrequencyMapId)
                durationJson.addProperty("frequencyMapId", it.frequencyMapId)
                durationArr.add(durationJson)
            }
            frequencyObj.add("duration", durationArr)
            postObj.add("frequency", frequencyObj)
            if (reminderPOJO.reminder?.endDate != null) {
                postObj.addProperty(
                    "end_date",
                    reminderPOJO.reminder?.endDate?.toString(ModelUtil.charmDisplayDateFormat())
                )
            }
            postObj.addProperty("module_id", reminderPOJO.reminderMap.firstOrNull()?.moduleId)
            postObj.addProperty("type", reminderPOJO.reminderMap.firstOrNull()?.type)
            postObj.addProperty(
                "localReminderMapId",
                reminderPOJO.reminderMap.firstOrNull()?.localReminderMapId
            )
            postObj.addProperty("localReminderId", reminderPOJO.reminder?.localReminderId)
            postObj.addProperty("reminderId", reminderPOJO.reminder?.reminderId)
            return postObj
        }


        fun formAddReminderHistoryJsonObj(reminderHistoryPOJO: ReminderHistoryPOJO): JsonObject {
            val postObj = JsonObject()
            postObj.addProperty(
                "module_id",
                reminderHistoryPOJO.reminderMap.firstOrNull()?.moduleId
            )
            postObj.addProperty("type", reminderHistoryPOJO.reminderMap.firstOrNull()?.type)
            postObj.addProperty("status", reminderHistoryPOJO.reminderHistory?.status)
            postObj.addProperty(
                "status_updated_date",
                reminderHistoryPOJO.reminderHistory?.statusUpdatedTime?.toString(ModelUtil.charmDisplayDateFormat())
            )
            postObj.addProperty(
                "status_updated_time",
                reminderHistoryPOJO.reminderHistory?.statusUpdatedTime?.toString(ModelUtil.charmDisplay24HrTimeFormat())
            )
//            if (reminderHistoryPOJO.reminderFrequencyMap.firstOrNull()?.day == -1) {
//                postObj.addProperty("reminder_day", "not_configured")
//
//            } else {
//                postObj.addProperty("reminder_day", reminderHistoryPOJO.reminderFrequencyMap.firstOrNull()?.day)
//
//            }
            postObj.addProperty(
                "reminder_time",
                reminderHistoryPOJO.reminderFrequencyMap.firstOrNull()?.time
            )
            postObj.addProperty(
                "frequency_id",
                reminderHistoryPOJO.reminderFrequencyMap.firstOrNull()?.frequencyMapId
            )
            return postObj
        }

        fun formUpdateReminderHistoryJsonObj(reminderHistoryPOJO: ReminderHistoryPOJO): JsonObject {
            val postObj = JsonObject()
            postObj.addProperty("status", reminderHistoryPOJO.reminderHistory?.status)
            postObj.addProperty(
                "status_updated_date",
                reminderHistoryPOJO.reminderHistory?.statusUpdatedTime?.toString(ModelUtil.charmDisplayDateFormat())
            )
            postObj.addProperty(
                "status_updated_time",
                reminderHistoryPOJO.reminderHistory?.statusUpdatedTime?.toString(ModelUtil.charmDisplay24HrTimeFormat())
            )
            return postObj
        }
        //endregion

        fun getRegisterDeviceObj(notificationRegister: NotificationRegister): JsonObject {

            val postObj = JsonObject()
            postObj.addProperty("nfid", notificationRegister.nfid)
            postObj.addProperty("installation_id", notificationRegister.installationId)
            postObj.addProperty("os_info", notificationRegister.osInfo)
            postObj.addProperty("device_info", notificationRegister.deviceInfo)
            postObj.addProperty("device_uid", notificationRegister.deviceUniqueId)
            postObj.addProperty("app_version", notificationRegister.appVersion)
            postObj.addProperty("mode", "0") //Android = 0 dont' change
            postObj.addProperty("apns_mode", "PRD")
            return postObj
        }

        fun unRegisterDeviceObj(patientId: String, practiceId: String): JsonObject {
            val postObj = JsonObject()
            postObj.addProperty("patient_id", patientId)
            postObj.addProperty("practice_id", practiceId)
            return postObj
        }
    }
}