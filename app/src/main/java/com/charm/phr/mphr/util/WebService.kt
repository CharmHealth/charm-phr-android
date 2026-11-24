package com.charm.phr.mphr.util

import android.util.Log
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.util.ModelUtil.Companion.isNotJsonNull
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url
import java.io.IOException
import java.net.SocketTimeoutException

interface WebService {

    //region Patient

    @GET("api/phr/v1/patientsPracticeList")//"/api/phr/v1/patients"
    fun getPatientList(): Call<JsonElement>

    @GET("api/phr/v1/patients/{patient_id}/demographics")
    fun getPatientDemographics(@Path("patient_id") patientId: String): Call<JsonElement>

    @GET("api/phr/v2/patients/{patient_id}/section/configuration")
    fun getPatientSection(@Path("patient_id") patientId: String): Call<JsonElement>

    @POST("api/phr/v1/patients/{patient_id}/demographics")
    fun savePatientDemographics(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @Multipart
    @POST("api/phr/v2/patients/{patient_id}/profile/photo")
    fun uploadPatientProfile(@Path("patient_id") patientId: String, @Part profilePic: MultipartBody.Part): Call<JsonElement>

    @GET("api/phr/v1/static")
    fun getPatientCommonData(): Call<JsonElement>

    @GET("api/phr/v1/languages")
    fun getLanguages(): Call<JsonElement>


    //endregion

    //region Appointment

    @GET("/api/phr/v1/patients/{patient_id}/appointments/upcoming")
    fun getUpcomingAppointment(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/appointments/requested")
    fun getRequestedAppointment(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/appointments/denied")
    fun getDeniedAppointment(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/appointments/past")
    fun getPastAppointment(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/appointments/waitlist")
    fun getWaitingListAppointment(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @GET("/api/phr/v1/providers/{member_id}/appointmenconfig")
    fun getAppointmentConfig(
        @Path("member_id") memberId: String,
        @Query("facility_id") facilityId: String,
        @Query("practice_id") practiceId: String,
        @Query("patient_id") patientId: String
    ): Call<JsonElement>

    @GET("/api/phr/v1/providers/{member_id}/slots")
    fun getAppointmentSlots(
        @Path("member_id") memberId: String,
        @Query("practice_id") practiceId: String,
        @Query("facility_id") facilityId: String,
        @Query("patient_id") patientId: String,
        @Query("from_date") fromDate: String,
        @Query("to_date") toDate: String = fromDate,
        @Query("visit_type_id") visitTypeId: String = "",
        @Query("interval") interval: Int
    ): Call<JsonElement>

    @GET("/api/phr/v1/practices/{practice_id}/providers?is_app_booking=true")
    fun getMemberListForAppointment(
        @Path("practice_id") practiceId: String,
        @Query("facility_id") facilityId: String,
        @Query("patient_id") patientId: String,
        @Query("start_index") startIndex: Int,
        @Query("count") count: Int
    ): Call<JsonElement>


    @GET("/api/phr/v1/practices/{practice_id}/facilities")
    fun getFacilityListForAppointment(@Path("practice_id") practiceId: String, @Query("patient_id") patientId: String): Call<JsonElement>

    @POST("/api/phr/v1/patients/{patient_id}/appointments/request")
    fun requestAppointment(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @POST("/api/phr/v1/patients/{patient_id}/appointments")
    fun bookAppointment(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @POST("/api/phr/v1/appointments/waitlists")
    fun requestWaitListAppointment(@Body postObj: JsonObject): Call<JsonElement>

    @DELETE("api/phr/v1/appointments/request/{appointment_id}")
    fun deleteAppointment(@Path("appointment_id") appointmentId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @POST("api/phr/v1/appointments/waitlists/{practice_id}/{appointment_id}/cancel")
    fun cancelWaitingList(@Path("appointment_id") appointmentId: String, @Path("practice_id") practiceId: String): Call<JsonElement>

    @POST("api/phr/v1/appointments/{appointment_id}/cancel")
    fun cancelAppointment(
        @Path("appointment_id") appointmentId: String,
        @Query("practice_id") practiceId: String,
        @Query("patient_id") patientId: String,
        @Query("reason") reason: String
    ): Call<JsonElement>


    @GET("/api/phr/v2/patients/{patient_id}/appointments/{appointment_id}/video")
    fun getVideoCallCredentialsForConsultation(
        @Path("patient_id") patientId: String,
        @Path("appointment_id") appointmentId: String,
        @Query("practice_id") practiceId: String
    ): Call<JsonElement>


    //endregion


    //region medications

    @GET("/api/phr/v1/patients/{patient_id}/prescription")
    fun getPrescription(
        @Path("patient_id") patientId: String,
        @Query("practice_id") practiceId: String,
        @Query("view") view: String = ModelUtil.medicationViewTypeActive,
        @Query("from_range") fromRange: Int,
        @Query("to_range") toRange: Int = ModelUtil.listPerPageCount
    ): Call<JsonElement>


    @POST("api/phr/v2/patients/{patient_id}/medications")
    fun addMedication(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @PUT("api/phr/v2/patients/{patient_id}/medications")
    fun editMedication(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @HTTP(method = "DELETE", path = "api/phr/v1/patients/{patient_id}/medications", hasBody = true)
    fun deleteMedication(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @POST("api/phr/v1/patients/{patient_id}/supplement")
    fun addSupplement(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @PUT("api/phr/v1/patients/{patient_id}/supplement")
    fun editSupplement(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @HTTP(method = "DELETE", path = "api/phr/v1/patients/{patient_id}/supplement", hasBody = true)
    fun deleteSupplement(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @POST("api/phr/v2/patients/{patient_id}/refill")
    fun refillPrescriptionn(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    //endregion

    //region supplements

    @GET("/api/phr/v1/patients/{patient_id}/supplement")
    fun getSupplementList(
        @Path("patient_id") patientId: String,
        @Query("practice_id") practiceId: String,
        @Query("view") view: String = ModelUtil.medicationViewTypeActive,
        @Query("from_range") fromRange: Int,
        @Query("to_range") toRange: Int = ModelUtil.listPerPageCount
    ): Call<JsonElement>
    //endregion

    //region billing

    @GET("/api/phr/v1/patients/{patient_id}/invoice")
    fun getInvoiceList(
        @Path("patient_id") patientId: String,
        @Query("practice_id") practiceId: String,
        @Query("from_range") fromRange: Int,
        @Query("to_range") toRange: Int = ModelUtil.listPerPageCount
    ): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/statements/{statement_id}")
    fun getStatementDetail(@Path("patient_id") patientId: String, @Path("statement_id") statementId: String): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/bills/{phr_bill_id}")
    fun getInvoiceDetails(@Path("patient_id") patientId: String, @Path("phr_bill_id") phrBillId: String): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/bills/{phr_bill_id}/history/{phr_bill_data_id}/download")
    fun getInvoiceDetailPDF(@Path("patient_id") patientId: String, @Path("phr_bill_id") phrBillId: String, @Path("phr_bill_data_id") phrBillDataId: String):
            Call<ResponseBody>

    @GET("/api/phr/v1/patients/{patient_id}/receipt")
    fun getReceiptsList(
        @Path("patient_id") patientId: String,
        @Query("practice_id") practiceId: String,
        @Query("from_range") fromRange: Int,
        @Query("to_range") toRange: Int = ModelUtil.listPerPageCount
    ): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/receipts/{phr_receipt_id}/download")
    fun getReceiptPDF(@Path("patient_id") patientId: String, @Path("phr_receipt_id") phrReceiptId: String): Call<ResponseBody>


    @GET("/api/phr/v2/patients/{patient_id}/invoices/{statement_id}?is_statement=true&format=pdf")
    fun getStatementPDF(@Path("patient_id") patientId: String, @Path("statement_id") statementId: String): Call<ResponseBody>
    //endregion

    //region Message

    @GET("/api/phr/v1/patients/{patient_id}/messages")
    fun getMessageList(
        @Path("patient_id") patientId: String,
        @Query("practice_id") practiceId: String,
        @Query("message_type") messageType: String,
        @Query("from_range") fromRange: Int,
        @Query("to_range") toRange: Int = ModelUtil.listPerPageCount
    ): Call<JsonElement>

    @GET("/api/phr/v1/messages/{message_id}")
    fun getMessageDetail(
        @Path("message_id") messageId: String,
        @Query("patient_id") patientId: String,
        @Query("practice_id") practiceId: String
    ): Call<JsonElement>

    @GET("/api/phr/v1/practices/{practice_id}/providers?phr_message_preference=true")
    fun getMembersForMessages(
        @Path("practice_id") practiceId: String,
        @Query("patient_id") patientId: String,
        @Query("start_index") startIndex: Int,
        @Query("count") count: Int
    ): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/messages/providers")
    fun getMembersForMessagesWithGroup(
        @Path("patient_id") patientId: String,
        @Query("practice_id") practiceId: String,
        @Query("start_index") startIndex: Int?,
        @Query("count") count: Int?,
        @Query("is_asc") isAscending: Boolean,
        @Query("searchString") searchString: String?,
    ): Call<JsonElement>

    @POST("/api/phr/v1/messages")
    fun sendMessage(@Body postObj: JsonObject): Call<JsonElement>

    @GET("/api/phr/v2/patients/{patient_id}/message/restriction")
    fun getComposeMessageRestriction(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @DELETE("api/phr/v1/messages/{message_receipient_id}")
    fun deleteMessage(
        @Path("message_receipient_id") messageReceipientId: String,
        @Query("patient_id") patientId: String,
        @Query("practice_id") practiceId: String
    ): Call<JsonElement>

    @GET("api/phr/v1/messages/{message_id}/attachments/{attachment_id}/download")
    fun getMessageAttachment(
        @Path("message_id") messageId: String,
        @Path("attachment_id") attachmentId: String,
        @Query("patient_id") patientId: String,
        @Query("practice_id") practiceId: String
    ): Call<ResponseBody>


    @Multipart
    @POST("api/phr/v1/messages/attachments/file")
    fun addMessageAttachment(@Part file: MultipartBody.Part): Call<JsonElement>

    //endregion

    //region Labx

    @GET("/api/phr/v1/patients/{patient_id}/lab")
    fun getLabResultList(
        @Path("patient_id") patientId: String,
        @Query("practice_id") practiceId: String,
        @Query("from_range") fromRange: Int,
        @Query("to_range") toRange: Int = ModelUtil.listPerPageCount
    ): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/labresults/{group_id}/details")
    fun getLabResultDetail(
        @Path("patient_id") patientId: String,
        @Path("group_id") groupId: String,
        @Query("practice_id") practiceId: String?
    ): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/labresults/{group_id}/downloadpdf?is_download=true")
    fun getLabResultPDF(@Path("patient_id") patientId: String, @Path("group_id") groupId: String, @Query("practice_id") practiceId: String?): Call<ResponseBody>

    @GET("/api/phr/v1/patients/{patient_id}/lab/order")
    fun getLabOrderList(
        @Path("patient_id") patientId: String
    ): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/lab/order/{lab_order_id}?download=false")//?is_download=false
    fun getLabOrderPDF(@Path("patient_id") patientId: String, @Path("lab_order_id") labOrderId: String): Call<ResponseBody>

    @GET("/api/phr/v1/patients/{patient_id}/lab/param")
    fun getLabParamHistory(@Path("patient_id") patientId: String, @Query("param_name") paramName: String?): Call<JsonElement>


    //endregion

    //region Allergy

    @GET("/api/phr/v1/patients/{patient_id}/allergy")
    fun getPatAllergyList(
        @Path("patient_id") patientId: String,
        @Query("practice_id") practiceId: String,
        @Query("view") status: String,
        @Query("from_range") fromRange: Int,
        @Query("to_range") toRange: Int = ModelUtil.listPerPageCount
    ): Call<JsonElement>


    @POST("api/phr/v1/allergen")
    fun addPatAllergy(@Body postObj: JsonObject): Call<JsonElement>

    @PUT("/api/phr/v1/allergy")
    fun editPatAllergy(@Body postObj: JsonObject): Call<JsonElement>

    @HTTP(method = "DELETE", path = "/api/phr/v1/allergy", hasBody = true)
    fun deletePatAllergy(@Body postObj: JsonObject): Call<JsonElement>


    //endregion

    //region Visit Summary

    @GET("/api/phr/v2/patients/{patient_id}/visitsummary")
    fun getVisitSummary(
        @Path("patient_id") patientId: String,
        @Query("practice_id") practiceId: String,
        @Query("start_index") fromRange: Int,
        @Query("count") toRange: Int = ModelUtil.listPerPageCount
    ): Call<JsonElement>


    @GET("/api/phr/v2/patients/{patient_id}/visitsummary/{pat_encounter_id}?download=true")
    fun getVisitSummaryPDF(
        @Path("patient_id") patientId: String,
        @Path("pat_encounter_id") patEncounterId: String,
        @Query("practice_id") practiceId: String
    ): Call<ResponseBody>
    //endregion


    //region Procedure

    @GET("/api/phr/v1/patients/{patient_id}/procedure")
    fun getProcedures(@Path("patient_id") patientId: String /*, @Query("from_range") fromRange: Int , @Query("to_range") toRange: Int = ModelUtil.listPerPageCount*/): Call<JsonElement>

    @POST("/api/phr/v2/procedure")
    fun addProcedure(@Body postObj: JsonObject): Call<JsonElement>

    @PUT("/api/phr/v2/procedure")
    fun editProcedure(@Body postObj: JsonObject): Call<JsonElement>

    @HTTP(method = "DELETE", path = "/api/phr/v1/procedure", hasBody = true)
    fun deleteProcedure(@Body postObj: JsonObject): Call<JsonElement>

    //endregion


    //region Vaccine


    @GET("/api/phr/v1/patients/{patient_id}/immune")
    fun getVaccineList(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @POST("/api/phr/v1/immune")
    fun addVaccine(@Body postObj: JsonObject): Call<JsonElement>

    @PUT("/api/phr/v1/immune")
    fun editVaccine(@Body postObj: JsonObject): Call<JsonElement>

    @HTTP(method = "DELETE", path = "/api/phr/v1/immune", hasBody = true)
    fun deleteVaccine(@Body postObj: JsonObject): Call<JsonElement>

    //endregion

    //region Announcement

    @GET("/api/phr/v1/patients/{patient_id}/announcements")
    fun getAnnouncementList(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @GET("/api/phr/v1/announcements/{announcement_id}")
    fun getAnnouncementDetail(@Path("announcement_id") practiceAnnouncementId: String, @Query("practice_id") practiceId: String): Call<JsonElement>
    //endregion

    //region About

    @GET("/api/phr/v1/practices/{practice_id}/about")
    fun getAboutDetail(@Path("practice_id") practiceId: String, @Query("patient_id") patientId: String): Call<JsonElement>

    @GET("/api/phr/v1/usefulinfo/{info_id}")
    fun getUsefulInfoDetail(@Path("info_id") infoId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    //endregion

    //region support

    @POST("/api/phr/v1/support")
    fun sendSupport(@Body postObj: JsonObject): Call<JsonElement>


    @Multipart
    @POST("/api/phr/v1/secure_sharing")
    fun uploadFilesForSupport(@Part file: List<MultipartBody.Part>): Call<JsonElement>

    //endregion

    //region Questionnaire


    @GET("/api/phr/v2/patients/{patient_id}/forms/list")
    fun getQuestionnaireMultiPracticeList(
        @Path("patient_id") patientId: String,
        @Query("forms_type") type: String,
        @Query("fromDate") fromDateString: String,
        @Query("toDate") toDateStr: String
    ): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/forms")
    fun getQuestionnaireList(
        @Path("patient_id") patientId: String,
        @Query("forms_type") type: String,
        @Query("practice_id") practiceId: String
    ): Call<JsonElement>

    @GET("api/phr/v2/health/passport/{patient_id}/questionnaire")
    fun getQuestionnaireHPList(@Path("patient_id") patientId: String): Call<JsonElement>

    @GET("/api/phr/v2/questionnaire/{questionnaire_id}")
    fun getQuestionnaireDetail(@Path("questionnaire_id") patientId: String): Call<JsonElement>

    @GET("api/phr/v2/patients/{patient_id}/forms/answer/{ques_map_id}")
    fun getQuestionnaireAnswer(
        @Path("patient_id") patientId: String,
        @Path("ques_map_id") quesMapId: String,
        @Query("practice_id") practiceId: String
    ): Call<JsonElement>

    @GET("api/phr/v2/questionnaire/personal/{patient_id}/answer/{ques_map_id}")
    fun getQuestionnaireAnswerForHealthPassport(@Path("patient_id") patientId: String, @Path("ques_map_id") quesMapId: String): Call<JsonElement>

    @GET("api/phr/v1/patients/{patient_id}/insurance/primary")
    fun getPrimaryInsuranceForQuestionnaire(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>


    @GET("api/phr/v1/patients/{patient_id}/primarycontact")
    fun getPrimaryContactDetForQuestionnaire(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @GET("api/phr/v1/patients/{patient_id}/personaldetail")
    fun getPrimaryDetForQuestionnaire(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @POST("api/phr/v2/forms/answers")
    fun postQuestionnaireAnswer(@Body postObj: JsonObject): Call<JsonElement>

    @POST("api/phr/v2/forms/secondary/answers")
    fun postQuestionnaireWidgetAnswer(@Body postObj: JsonObject): Call<JsonElement>


    @POST("api/phr/v2/questionnaire/personal/answers")
    fun postQuestionnaireAnswerForHealthPassport(@Body postObj: JsonObject): Call<JsonElement>

    @Multipart
    @POST("/api/phr/v2/patients/{patient_id}/forms/signature/{ques_map_id}/{entry_id}")
    fun postQuestionnaireSignature(
        @Path("ques_map_id") patQuesMapId: String,
        @Path("entry_id") entryId: String,
        @Path("patient_id") patientId: String,
        @Part file: List<MultipartBody.Part>,
        @Query("practice_id") practiceId: String
    ): Call<JsonElement>

    @GET("/api/phr/v2/patients/{patient_id}/forms/signature/{ques_map_id}/{entry_id}")//?is_download=false
    fun getQuestionnaireSignature(
        @Path("patient_id") patientId: String,
        @Path("ques_map_id") patQuesMapId: String,
        @Path("entry_id") entryId: String,
        @Query("practice_id") practiceId: String
    ): Call<ResponseBody>


    @DELETE("api/phr/v2/forms/signature/{ques_map_id}/{entry_id}")
    fun deleteQuestionnaireSignature(
        @Path("ques_map_id") patQuesMapId: String,
        @Path("entry_id") entryId: String,
        @Query("patient_id") patientId: String,
        @Query("practice_id") practiceId: String
    ): Call<JsonElement>

    //endregion

    //region Flowsheets

    @GET("api/phr/v1/patients/{patient_id}/flowsheets")
    fun getFlowsheetList(@Path("patient_id") patientId: String /*, @Query("from_date")fromDate: String , @Query("to_date")  toDate: String*/): Call<JsonElement>


    @GET("api/phr/v1/patients/{patient_id}/practices/{practice_id}/flowsheets/{flowsheet_id}/entries")
    fun getFlowsheetEntryList(
        @Path("patient_id") patientId: String,
        @Path("practice_id") practiceId: String,
        @Path("flowsheet_id") flowsheetId: String
    ): Call<JsonElement>

    @PUT("api/phr/v1/patients/{patient_id}/practices/{practice_id}/flowsheets/{flowsheet_data_id}")
    fun updateFlowsheetEntry(
        @Path("patient_id") patientId: String,
        @Path("practice_id") practiceId: String,
        @Path("flowsheet_data_id") flowsheetId: String,
        @Body postObj: JsonObject
    ): Call<JsonElement>

    @POST("api/phr/v1/patients/{patient_id}/practices/{practice_id}/flowsheets")
    fun addFlowsheetEntry(@Path("patient_id") patientId: String, @Path("practice_id") practiceId: String, @Body postObj: JsonObject): Call<JsonElement>

    @DELETE("api/phr/v1/patients/{patient_id}/practices/{practice_id}/flowsheets/{flowsheet_id}/{flowsheet_data_id}")
    fun deleteFlowsheetEntry(
        @Path("patient_id") patientId: String,
        @Path("practice_id") practiceId: String,
        @Path("flowsheet_id") flowsheetId: String,
        @Path("flowsheet_data_id") phrFlowsheetDataId: String
    ): Call<JsonElement>

    //endregion

    //region Contacts

    @GET("/api/phr/v2/patients/{patient_id}/contacts")
    fun getContactList(@Path("patient_id") patientId: String, @Query("contact_type") contactType: String): Call<JsonElement>

    @GET("/api/phr/v1/patients/{patient_id}/getpharmacydetails")
    fun getContactForSearch(@Path("patient_id") patientId: String): Call<JsonElement>

    @POST("/api/phr/v1/contacts")
    fun saveContact(@Body postObj: JsonObject): Call<JsonElement>

    @POST("/api/phr/v1/patients/{patient_id}/contacts/multiple")
    fun saveContactList(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @DELETE("/api/phr/v1/patients/{patient_id}/contacts/{pharmacy_id}")
    fun deleteContact(@Path("patient_id") patientId: String, @Path("pharmacy_id") pharmacyId: String): Call<JsonElement>

    //endregion

    //region Document

    @GET("api/phr/v1/patients/{patient_id}/documents")
    fun getDocumentList(
        @Path("patient_id") patientId: String,
        @Query("filter") filter: String,
        @Query("category_id") categoryId: String,
        @Query("type") documentType: String,
        @Query("from_range") fromRange: Int,
        @Query("to_range") toRange: Int = ModelUtil.documentlistPerPageCount
    ): Call<JsonElement>


    @GET("api/phr/v1/category")
    fun getCategoryList(@Query("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @POST("api/phr/v1/category")
    fun addCategory(@Body postObj: JsonObject): Call<JsonElement>

    @PUT("api/phr/v1/category")
    fun editCategory(@Body postObj: JsonObject): Call<JsonElement>

    @HTTP(method = "DELETE", path = "api/phr/v1/category/{category_id}", hasBody = true)
    fun deleteCategory(@Body postObj: JsonObject, @Path("category_id") categoryId: String): Call<JsonElement>


    @GET("api/phr/v1/patients/{patient_id}/members")
    fun getMemberList(@Path("patient_id") patientId: String): Call<JsonElement>


    @Multipart
    @POST("api/phr/v2/patients/{patient_id}/document/upload")
    fun uploadDocument(@Part file: List<MultipartBody.Part>, @Path("patient_id") patientId: String): Call<JsonElement>

    @GET("/api/phr/v2/patients/{patient_id}/file/{file_id}?download=false")
    fun getDocument(@Path("patient_id") patientId: String, @Path("file_id") fileId: String): Call<ResponseBody>


    @DELETE("api/phr/v2/patients/{patient_id}/document")
    fun deleteDocument(@Path("patient_id") patientId: String, @Query("document_id") fileId: String): Call<JsonElement>

    @GET("api/phr/v2/patients/{patient_id}/document/restriction")
    fun shareDocumentRestriction(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @POST("api/phr/v1/patients/{patient_id}/sharedocuments")
    fun shareDocumentToOthers(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @POST("api/phr/v1/patients/{patient_id}/share")
    fun shareDocumentToMembers(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    //endregion

    //region MedicalCondition

    @GET("api/phr/v1/patients/{patient_id}/condition")
    fun getMedicalConditionList(
        @Path("patient_id") patientId: String,
        @Query("status") status: String,
        @Query("practice_id") practiceId: String
    ): Call<JsonElement>

    @POST("api/phr/v2/cond")
    fun addMedicalCondition(@Body postObj: JsonObject): Call<JsonElement>

    @PUT("api/phr/v2/cond")
    fun editMedicalCondition(@Body postObj: JsonObject): Call<JsonElement>

    @HTTP(method = "DELETE", path = "api/phr/v1/cond", hasBody = true)
    fun deleteMedicalCondition(@Body postObj: JsonObject): Call<JsonElement>

    //endregion

    //region Misc

    @GET("/api/phr/v1/patients/{patient_id}/phrunreadcount")
    fun getUnreadCount(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>


    @GET("/api/phr/v1/practices/{practice_id}/menulist")
    fun getMenuList(@Path("practice_id") practiceId: String): Call<JsonElement>

    //endregion

    //region logout


    //endregion

    //region Payment

    @POST("/api/phr/v1/payments/{patient_id}/createTransaction")
    fun createTransaction(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @PUT("/api/phr/v1/payments/{patient_id}/transactions/{transaction_id}")
    fun updateTransaction(@Path("patient_id") patientId: String, @Path("transaction_id") transactionId: String, @Body postObj: JsonObject): Call<JsonElement>


    @GET("api/phr/v1/payments/{patient_id}/transactions/{transaction_id}/status")
    fun getTransactionStatus(
        @Path("patient_id") patientId: String,
        @Path("transaction_id") transactionId: String,
        @Query("practice_id") practiceId: String,
        @Query("gateway") gatewayInfo: String
    ): Call<JsonElement>
    //endregion

    //region Card on file

    @GET("api/phr/v1/patients/{patient_id}/cards") //merchant_id,validCardOnly
    fun getCardOnFileList(
        @Path("patient_id") patientId: String,
        @Query("practice_id") practiceId: String,
        @Query("merchant_id") merchant_id: String?
    ): Call<JsonElement>

    @GET("api/phr/v1/patients/{patient_id}/merchant-account")
    fun getMerchantAccountForPatient(@Path("patient_id") patientId: String, @Query("practice_id") practiceId: String): Call<JsonElement>

    @POST("api/phr/v1/card/group")
    fun groupMerchantAndPatients(@Body postObj: JsonObject): Call<JsonElement>

    @PUT("api/phr/v1/card")
    fun updateCardOnFile(@Body postObj: JsonObject): Call<JsonElement>

    @HTTP(method = "DELETE", path = "api/phr/v1/card", hasBody = true)
    fun deleteCardOnFile(@Body postObj: JsonObject): Call<JsonElement>

    //endregion

    //region vitals

    @GET("api/phr/v2/patients/{patient_id}/vitals")
    fun getLatestVitals(@Path("patient_id") patientId: String): Call<JsonElement>

    @GET("api/phr/v2/patients/{patient_id}/tracker")
    fun getVitalTrackers(@Path("patient_id") patientId: String): Call<JsonElement>

    @GET("api/phr/v2/patients/{patient_id}/unit")
    fun getVitalUnitType(@Path("patient_id") patientId: String): Call<JsonElement>

    @POST("api/phr/v2/patients/unit")
    fun updateVitalUnitType(@Body postObj: JsonObject): Call<JsonElement>


    @GET("api/phr/v2/patients/{patient_id}/tracker/{tracker_id}")
    fun getVitalDetailList(@Path("patient_id") patientId: String, @Path("tracker_id") trackerId: String): Call<JsonElement>

    @GET("api/phr/v2/patients/{patient_id}/metric/{metric_id}")
    fun getMetricDetailList(@Path("patient_id") patientId: String, @Path("metric_id") metricId: String): Call<JsonElement>

    /* @GET("api/phr/v1/tracker/{tracker_id}")
     fun getVitalMetric(@Path("tracker_id") trackerId: String): Call<JsonElement>
 */
    @GET("api/phr/v1/patients/{patient_id}/healthmetricunits")
    fun getHealthMetricUnitList(@Path("patient_id") patientId: String): Call<JsonElement>

    @GET("api/phr/v1/patients/{patient_id}/healthmetrics")
    fun getHealthMetricList(@Path("patient_id") patientId: String): Call<JsonElement>

    @POST("api/phr/v1/healthmetric")
    fun addHealthMetric(@Body postObj: JsonObject): Call<JsonElement>

    @PUT("api/phr/v1/healthmetric")
    fun updateHealthMetric(@Body postObj: JsonObject): Call<JsonElement>

    @HTTP(method = "DELETE", path = "api/phr/v1/healthmetric", hasBody = true)
    fun deleteHealthMetric(@Body postObj: JsonObject): Call<JsonElement>

    @POST("api/phr/v2/tracker")
    fun addVital(@Body postObj: JsonObject): Call<JsonElement>


    //endregion

    //region Insurance

    @GET("api/phr/v1/patients/{patient_id}/insurance")
    fun getInsurances(@Path("patient_id") patientId: String): Call<JsonElement>

    @POST("api/phr/v1/patients/{patient_id}/insurance")
    fun addInsurance(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @PUT("api/phr/v1/patients/{patient_id}/insurance")
    fun editInsurance(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @HTTP(path = "api/phr/v1/insurance", method = "DELETE", hasBody = true)
    fun deleteInsurance(@Body postObj: JsonObject): Call<JsonElement>

    @GET("api/phr/v1/patients/{patient_id}/payername")
    fun getPayerList(
        @Path("patient_id") patientId: String,
        @Query("practice_id") practiceId: String,
        @Query("searchString") searchStr: String
    ): Call<JsonElement>

    //endregion

    //region reminders

    @GET("api/phr/v1/patients/{patient_id}/reminder")
    fun getReminders(
        @Path("patient_id") patientId: String,
        @Query("practice_id") practiceId: String?,
        @Query("type") type: String?,
        @Query("module_id") moduleId: String?,
        @Query("include_deleted") includeDeleted: Boolean = true,
    ): Call<JsonElement>
//    <param name="practice_id" type="long" min-occurrences="0" max-occurrences="1" max-len="19" />
//    <param name="type" regex="reminderModules" min-occurrences="0" max-occurrences="1" max-len="11" />
//    <param name="module_id" type="long" min-occurrences="0" max-occurrences="1" max-len="19" /> TODO: Remove after testing

    @POST("api/phr/v1/patients/{patient_id}/reminder")
    fun addReminder(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @PUT("api/phr/v1/patients/{patient_id}/reminder/{reminder_id}")
    fun updateReminder(@Path("patient_id") patientId: String, @Path("reminder_id") reminderId: String, @Body postObj: JsonObject): Call<JsonElement>

    @DELETE("api/phr/v1/patients/{patient_id}/reminder/{reminder_id}")
    fun deleteReminder(
        @Path("patient_id") patientId: String,
        @Path("reminder_id") reminderId: String,
        @Query("module_id") moduleId: String,
        @Query("practice_id") practiceId: String,
    ): Call<JsonElement>

//    <param name="module_id" type="long" min-occurrences="1" max-occurrences="1" max-len="19" />
//    <param name="practice_id" type="long" min-occurrences="0" max-occurrences="1" max-len="19" />

    @POST("api/phr/v1/patients/{patient_id}/reminder/{reminder_id}/history")
    fun addReminderHistory(@Path("patient_id") patientId: String, @Path("reminder_id") reminderId: String, @Body postObj: JsonObject): Call<JsonElement>

    @PUT("api/phr/v1/patients/{patient_id}/reminder/{reminder_id}/history/{history_id}")
    fun updateReminderHistory(
        @Path("patient_id") patientId: String,
        @Path("reminder_id") reminderId: String,
        @Path("history_id") historyId: String,
        @Body postObj: JsonObject
    ): Call<JsonElement>

    @GET("api/phr/v1/patients/{patient_id}/reminder/{reminder_id}/history")
    fun getRemindersHistory(
        @Path("patient_id") patientId: String,
        @Path("reminder_id") reminderId: String,
        @Query("module_id") moduleId: String,
        @Query("from_range") fromRange: Int,
        @Query("to_count") toCount: Int,
        @Query("is_asc") isAsc: Boolean,
        @Query("practice_id") practiceId: String,
    ): Call<JsonElement>

    @GET("api/phr/v1/patients/{patient_id}/reminder/history")
    fun getModuleRemindersHistory(
        @Path("patient_id") patientId: String,
        @Query("from_date") fromDate: String,
        @Query("to_date") toDate: String,
        @Query("type") moduleType: String,
        @Query("is_asc") isAsc: Boolean,
        @Query("practice_id") practiceId: String,
    ): Call<JsonElement>
    //endregion

    //region misc

    @GET("/api/phr/v1/{module_name}/commondata")
    fun getCommonData(
        @Path("module_name") moduleName: String,
        @Query("patient_id") patientId: String,
        @Query("practice_id") practiceId: String
    ): Call<JsonElement>

    @POST("api/phr/v2/health/passport/{patient_id}/populate")
    fun populatePassport(@Path("patient_id") patientId: String): Call<JsonElement>

    @GET("api/phr/v1/country/{country}")
    fun getStateList(@Path("country") country: String): Call<JsonElement>

    @POST
    @FormUrlEncoded
    fun postBluefinAPI(@Url bluefinAPI: String, @FieldMap postObj: HashMap<String, Any>): Call<JsonElement>

    @GET("api/phr/v2/health/passport/{patient_id}/url")
    fun getHealthPassportURL(@Path("patient_id") patientId: String): Call<JsonElement>

    @GET("api/phr/v1/patients/{patient_id}/healthpassport/pdf")
    fun getPatDemoPDF(@Path("patient_id") patientId: String): Call<ResponseBody>

    @GET("api/phr/v2/health/passport/{patient_id}/url")
    fun getHealthCardPDF(@Path("patient_id") patientId: String): Call<ResponseBody>


    @Multipart
    @POST("api/phr/v2/swaasaai/{id}")//"/api/phr/v1/patients"
    fun recordAudio(
        @Part file: MultipartBody.Part,
        @Part(value = "data") jsonObject: JsonObject,
        @Path("id") id: String
    ): Call<JsonElement>

    @GET("api/phr/v2/swaasaai/{id}")
    fun getSwaasaData(@Path("id") id: String, @Query("pass_code") otp: String?): Call<JsonElement>

    //endregion

    //region Push notification

    @GET("/api/phr/v1/patients/{patient_id}/notification/insid")
    fun getInstallationId(@Path("patient_id") patientId: String): Call<JsonElement>

    @POST("/api/phr/v1/patients/{patient_id}/notification/register")
    fun registerDevice(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    @POST("/api/phr/v1/patients/{patient_id}/notification/unregister")
    fun unRegisterDevice(@Path("patient_id") patientId: String, @Body postObj: JsonObject): Call<JsonElement>

    //endregion

    @GET("api/phr/v2/userPrivileges")
    fun fetchPrivileges(): Call<JsonElement>
}


class APIList {
    companion object {

        private val patientPhotoUrl1 = "/api/phr/v1/patients/"
        private val patientPhotoURL2 = "/downloadPhoto?photo_size=120_120"

        private val memberPhotoUrl1 = "/api/phr/v1/providers/"
        private val memberPhotoURL2 = "/downloadPhoto?practice_id="

        private val questionnaireSignatureURL1 = "/api/phr/v2/patients/"
        private val questionnaireSignatureURL2 = "/forms/signature/"
        private val questionnaireSignatureURL3 = "/"
        private val questionnaireSignatureURL4 = "?practice_id="

        fun getPatientImageURL(patientId: String?)
                : String = MDAccountsRelatedUtil.baseUrl + patientPhotoUrl1 + patientId + patientPhotoURL2

        fun getMemberImageURL(memberId: String?, practiceId: String?)
                : String = MDAccountsRelatedUtil.baseUrl + memberPhotoUrl1 + memberId + memberPhotoURL2 + practiceId

        fun getQuestionnaireSignature(patientId: String?, patQuesMapId: String?, entryId: String?, practiceId: String?)
                : String =
            MDAccountsRelatedUtil.baseUrl + questionnaireSignatureURL1 + patientId + questionnaireSignatureURL2 + patQuesMapId + questionnaireSignatureURL3 + entryId + questionnaireSignatureURL4 + practiceId
    }
}

/**
 * Note:
 * Service methods callback should be pass [forService] boolean as true.
 */
abstract class MDCallback(val authManager: AuthManager, val forService: Boolean = false) :
    Callback<JsonElement> {

    override fun onFailure(call: Call<JsonElement>, t: Throwable) {
        Log.d("MdCallback", "URL :${call.request().url}")
        Log.d("MdCallback", "onFailure $t")

        val apiResponseError = APIResponseError(
            null, null, when (t) {
                is IOException -> ModelUtil.io_exception
                is SocketTimeoutException -> ModelUtil.socketException
                else -> ModelUtil.conversionException
            }
        )
        onMDFailure(apiResponseError)
    }

    override fun onResponse(call: Call<JsonElement>, webResp: retrofit2.Response<JsonElement>) {

        Log.d("MdCallback", "call :${call.request()}")
        Log.d("MdCallback", "URL :${call.request().url.toString()}")
        Log.d("MdCallback", "webResp Code :${webResp.code()}")



        try {

            if (webResp.isSuccessful) {

                Log.d(
                    "MdCallback",
                    "contentType ${webResp.raw().header("content-type")}"

                )


                if (webResp.raw().header("content-type")
                        ?.contains("application/json") == true || (webResp.body() is JsonElement && webResp.body()?.asJsonObject is JsonObject)
                ) {


                    call.request().url?.toString()


                    val responseBody = webResp.body()?.asJsonObject

                    val code = responseBody?.get("code")?.asInt
                    val isSuccess = responseBody?.get("isSuccess")?.asBoolean ?: false
                    if ((code == 0 || isSuccess) && (responseBody != null)) {
                        Log.d("MdCallback", "onResponse :$responseBody")
                        onMDSuccess(responseBody)

                    } else {

                        Log.d("MdCallback", "onResponse code non zero:$responseBody")

                        val errorMsg: String =
                            responseBody?.get("message")?.asString ?: ModelUtil.requestFailed
                        onMDFailure(APIResponseError(code, webResp.code(), errorMsg))

                    }
                } else {

                    val errorBody = webResp.errorBody()?.string().parseStringAsJsonObject()
                    var jsonObject: JsonObject? = null
                    if (errorBody.isNotJsonNull()) {
                        jsonObject = errorBody?.asJsonObject
                    }
                    val code = jsonObject?.get("code")?.asInt

                    val message = jsonObject?.get("message")?.asString


                    onMDFailure(APIResponseError(code, webResp.code(), "$message"))
                }

            } else {


                if (webResp.raw().header("content-type")?.contains("application/json") == true) {

                    val errorBody = webResp.errorBody()?.string().parseStringAsJsonObject()
                    var jsonObject: JsonObject? = null
                    if (errorBody.isNotJsonNull()) {
                        jsonObject = errorBody?.asJsonObject
                    }
                    val code = jsonObject?.get("code")?.asInt

                    val message = jsonObject?.get("message")?.asString

                    Log.d("MdCallback", "request failed:$webResp")

                    Log.d("MdCallback", "request failed code:$code -> $message")

                    if (forService)
                        onMDFailure(APIResponseError(0, webResp.code(), ModelUtil.requestFailed))
                    else
                        onMDFailure(authManager.apiErrorResponseHandlerForJsonElement(webResp, code, message))
                } else {
                    onMDFailure(authManager.apiErrorResponseHandlerForJsonElement(webResp, -1, null))
                }

            }
        } catch (e: Exception) {
            onMDFailure(APIResponseError(0, null, ModelUtil.requestFailed))
        }
    }

    abstract fun onMDSuccess(respObj: JsonObject)

    abstract fun onMDFailure(error: APIResponseError)

}

abstract class MDCallbackForMisc(val authManager: AuthManager, val forService: Boolean = false) :
    Callback<ResponseBody> {

    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
        Log.d("MdCallback", "onFailure $t")
        Log.d("MdCallback", "URL: ${call.request().url}")

        val apiResponseError = APIResponseError(
            null, null, when (t) {
                is IOException -> ModelUtil.io_exception
                is SocketTimeoutException -> ModelUtil.socketException
                else -> ModelUtil.conversionException
            }
        )
        onMDFailure(apiResponseError)
    }

    override fun onResponse(call: Call<ResponseBody>, webResp: retrofit2.Response<ResponseBody>) {
        Log.d("MdCallback", "URL :${call.request().url.toString()}")

        try {

            if (webResp.isSuccessful) {

                Log.d(
                    "MdCallback",
                    "contentType ${webResp.raw().header("content-type")}"
                )
                Log.d(
                    "MdCallback",
                    "fileName ${webResp.raw().header("Content-Disposition")}"
                )
                if ((webResp.raw().header("content-type")?.contains("application/json", ignoreCase = true) == false) && (webResp.raw().header("content-type")
                        ?.contains("text/html", ignoreCase = true) == false)
                ) {

                    val responseBody = webResp.body()

                    var contentDisposition = webResp.raw().header("Content-Disposition") ?: ""

                    contentDisposition = contentDisposition.removePrefix("attachment; filename=\"")

                    val fileName = contentDisposition.removeSuffix("\"")

                    val fileNameList = fileName.split(".")
                    val extension = fileNameList[fileNameList.size - 1]

                    if (responseBody != null) {

                        val mdFile = MDFile()
                        mdFile.responseBody = responseBody
                        mdFile.extension = extension
                        mdFile.fileName = fileName
                        onMDSuccess(mdFile)
                    } else {
                        onMDFailure(APIResponseError(null, null, null))
                    }

                    /* val code = responseBody?.get("code")?.asInt
                if (code == 0) {
                    Log.d("MdCallback", "onResponse :$responseBody")
                    onMDSuccess(responseBody)

                } else {

                    Log.d("MdCallback", "onResponse code non zero:$responseBody")

                    val errorMsg: String =
                        responseBody?.get("message")?.asString ?: ModelUtil.requestFailed
                    onMDFailure(APIResponseError(code, webResp.code(), errorMsg))

                }*/
                } else {
                    onMDFailure(authManager.apiErrorResponseHandlerForResponseBody(webResp))
                }

            } else {

                val errorBody = webResp.errorBody()?.string().parseStringAsJsonObject()
                var jsonObject: JsonObject? = null
                if (errorBody.isNotJsonNull()) {
                    jsonObject = errorBody?.asJsonObject
                }
                val code = jsonObject?.get("code")?.asInt

                Log.d("MdCallback", "request failed:$webResp")

                if (forService)
                    onMDFailure(APIResponseError(0, null, ModelUtil.requestFailed))
                else
                    onMDFailure(authManager.apiErrorResponseHandlerForResponseBody(webResp, code))

            }
        } catch (e: Exception) {
            onMDFailure(APIResponseError(0, null, ModelUtil.requestFailed))
        }
    }

    abstract fun onMDSuccess(respObj: MDFile)

    abstract fun onMDFailure(error: APIResponseError)

}

interface DisableMultiSelectListener {
    fun disableMultiSelect()
}

interface ChangeFilterListener {
    fun onFilterChange()
}

interface RefreshDataListener {
    fun refreshData()
}

interface OnMultipleSelectChangeListener {
    fun onMultiSelect(isMultiSelectEnabled: Boolean)
}
