package com.charm.phr.mphr.di

import com.charm.phr.mphr.ui.about.AboutActivity
import com.charm.phr.mphr.ui.about.AboutActivityModule
import com.charm.phr.mphr.ui.about.FacilityListFragmentProvider
import com.charm.phr.mphr.ui.about.MemberListFragmentProvider
import com.charm.phr.mphr.ui.allergy.AllergyActivity
import com.charm.phr.mphr.ui.allergy.AllergyActivityModule
import com.charm.phr.mphr.ui.allergy.addAllergy.AddAllergyActivity
import com.charm.phr.mphr.ui.allergy.addAllergy.AddAllergyActivityModule
import com.charm.phr.mphr.ui.announcement.AnnouncementDetailActivity
import com.charm.phr.mphr.ui.announcement.AnnouncementDetailActivityModule
import com.charm.phr.mphr.ui.announcement.AnnouncementListActivity
import com.charm.phr.mphr.ui.announcement.AnnouncementListActivityModule
import com.charm.phr.mphr.ui.appointment.AppointmentActivity
import com.charm.phr.mphr.ui.appointment.AppointmentActivityModule
import com.charm.phr.mphr.ui.appointment.book_appointment.BookAppointmentActivity
import com.charm.phr.mphr.ui.appointment.book_appointment.BookAppointmentActivityModule
import com.charm.phr.mphr.ui.appointment.book_appointment.easytelecare.EasyTeleBookAppointmentActivity
import com.charm.phr.mphr.ui.appointment.book_appointment.easytelecare.EasyTeleBookAppointmentModule
import com.charm.phr.mphr.ui.billing.BillingActivity
import com.charm.phr.mphr.ui.billing.BillingActivityModule
import com.charm.phr.mphr.ui.billing.InvoiceFragmentProvider
import com.charm.phr.mphr.ui.billing.ReceiptFragmentProvider
import com.charm.phr.mphr.ui.billing.billingDetail.BillingDetailActivity
import com.charm.phr.mphr.ui.billing.billingDetail.BillingDetailActivityModule
import com.charm.phr.mphr.ui.cardonfile.CardOnFileActivity
import com.charm.phr.mphr.ui.cardonfile.CardOnFileActivityModule
import com.charm.phr.mphr.ui.cardonfile.addOrEditCof.AddOrEditCardOnFileActivity
import com.charm.phr.mphr.ui.cardonfile.addOrEditCof.AddOrEditCardOnFileActivityModule
import com.charm.phr.mphr.ui.components.mdCamera.ImageCropperFragmentProvider
import com.charm.phr.mphr.ui.components.mdCamera.MdCameraActivity
import com.charm.phr.mphr.ui.components.mdCamera.MdCameraActivityModule
import com.charm.phr.mphr.ui.components.mdCamera.MdCameraFragmentProvider
import com.charm.phr.mphr.ui.components.mdCamera.MdPhotoPreviewFragmentProvider
import com.charm.phr.mphr.ui.contact.add_contact.AddContactActivity
import com.charm.phr.mphr.ui.contact.add_contact.AddContactActivityModule
import com.charm.phr.mphr.ui.contact_us.ContactUsActivity
import com.charm.phr.mphr.ui.contact_us.ContactUsModule
import com.charm.phr.mphr.ui.dashboard.DashboardActivity
import com.charm.phr.mphr.ui.dashboard.DashboardActivityModule
import com.charm.phr.mphr.ui.dashboard.HealthDataFragmentProvider
import com.charm.phr.mphr.ui.dashboard.HomeFragmentProvider
import com.charm.phr.mphr.ui.dashboard.user_profile.UserProfileActivity
import com.charm.phr.mphr.ui.dashboard.user_profile.UserProfileActivityModule
import com.charm.phr.mphr.ui.demographics.PatientDemographicsActivity
import com.charm.phr.mphr.ui.demographics.PatientDemographicsActivityModule
import com.charm.phr.mphr.ui.demographics.demographics_pdf.PatDemoPDFActivity
import com.charm.phr.mphr.ui.demographics.demographics_pdf.PatDemoPDFActivityModule
import com.charm.phr.mphr.ui.demographics.save_pat_demo.SavePatDemographicsActivity
import com.charm.phr.mphr.ui.demographics.save_pat_demo.SavePatDemographicsActivityModule
import com.charm.phr.mphr.ui.document.DocumentActivity
import com.charm.phr.mphr.ui.document.DocumentActivityModule
import com.charm.phr.mphr.ui.document.DocumentFolderFragmentProvider
import com.charm.phr.mphr.ui.document.DocumentListFragmentProvider
import com.charm.phr.mphr.ui.document.add_document.AddDocumentActivity
import com.charm.phr.mphr.ui.document.add_document.AddDocumentActivityModule
import com.charm.phr.mphr.ui.document.document_list_with_folder.DocumentListForFolderActivity
import com.charm.phr.mphr.ui.document.document_list_with_folder.DocumentListForFolderActivityModule
import com.charm.phr.mphr.ui.document.preview_document.PreviewDocumentActivity
import com.charm.phr.mphr.ui.document.preview_document.PreviewDocumentActivityModule
import com.charm.phr.mphr.ui.document.share_document.ShareDocumentActivity
import com.charm.phr.mphr.ui.document.share_document.ShareDocumentActivityModule
import com.charm.phr.mphr.ui.flowsheet.FlowsheetListActivity
import com.charm.phr.mphr.ui.flowsheet.FlowsheetListActivityModule
import com.charm.phr.mphr.ui.flowsheet.add.AddFlowsheetDataActivity
import com.charm.phr.mphr.ui.flowsheet.add.AddFlowsheetDataActivityModule
import com.charm.phr.mphr.ui.flowsheet.entry.FlowsheetDataActivity
import com.charm.phr.mphr.ui.flowsheet.entry.FlowsheetDataActivityModule
import com.charm.phr.mphr.ui.health_passport.HealthPassportActivity
import com.charm.phr.mphr.ui.health_passport.HealthPassportActivityModule
import com.charm.phr.mphr.ui.insurance.InsuranceListActivity
import com.charm.phr.mphr.ui.insurance.InsuranceListActivityModule
import com.charm.phr.mphr.ui.insurance.add.AddInsuranceActivity
import com.charm.phr.mphr.ui.insurance.add.AddInsuranceActivityModule
import com.charm.phr.mphr.ui.lab.LabActivity
import com.charm.phr.mphr.ui.lab.LabActivityModule
import com.charm.phr.mphr.ui.lab.LabOrderListFragmentProvider
import com.charm.phr.mphr.ui.lab.LabResultListFragmentProvider
import com.charm.phr.mphr.ui.lab.detail_pdf.LabDetailPDFActivity
import com.charm.phr.mphr.ui.lab.detail_pdf.LabDetailPDFActivityModule
import com.charm.phr.mphr.ui.lab.plotDetailGraph.LabDetailsGraphActivity
import com.charm.phr.mphr.ui.lab.plotDetailGraph.LabDetailsGraphActivityModule
import com.charm.phr.mphr.ui.lab.resultDetail.LabResultDetailActivity
import com.charm.phr.mphr.ui.lab.resultDetail.LabResultDetailActivityModel
import com.charm.phr.mphr.ui.localauth.AppLockTypeActivity
import com.charm.phr.mphr.ui.localauth.AppLockTypeModule
import com.charm.phr.mphr.ui.localauth.app_lock.AppLockActivity
import com.charm.phr.mphr.ui.localauth.app_lock.AppLockActivityModule
import com.charm.phr.mphr.ui.login.LoginActivity
import com.charm.phr.mphr.ui.login.LoginActivityModule
import com.charm.phr.mphr.ui.message.MessageActivity
import com.charm.phr.mphr.ui.message.MessageActivityModule
import com.charm.phr.mphr.ui.message.MessageInboxFragmentProvider
import com.charm.phr.mphr.ui.message.MessageSentFragmentProvider
import com.charm.phr.mphr.ui.message.compose.ComposeMessageActivity
import com.charm.phr.mphr.ui.message.compose.ComposeMessageActivityModule
import com.charm.phr.mphr.ui.message.compose.ComposeMessageFragmentProvider
import com.charm.phr.mphr.ui.message.messageDetail.MessageDetailActivity
import com.charm.phr.mphr.ui.message.messageDetail.MessageDetailActivityModule
import com.charm.phr.mphr.ui.passcode.PasscodeActivity
import com.charm.phr.mphr.ui.passcode.PasscodeActivityModule
import com.charm.phr.mphr.ui.patientList.PatientListActivity
import com.charm.phr.mphr.ui.patientList.PatientListActivityModule
import com.charm.phr.mphr.ui.physician.PhysicianActivity
import com.charm.phr.mphr.ui.physician.PhysicianActivityModule
import com.charm.phr.mphr.ui.physician.add_physician.AddPhysicianActivity
import com.charm.phr.mphr.ui.physician.add_physician.AddPhysicianActivityModule
import com.charm.phr.mphr.ui.prescription.MedicationFragmentProvider
import com.charm.phr.mphr.ui.prescription.PrescriptionActivity
import com.charm.phr.mphr.ui.prescription.PrescriptionActivityModule
import com.charm.phr.mphr.ui.prescription.SupplementFragmentProvider
import com.charm.phr.mphr.ui.prescription.add_medication.AddMedicationActivity
import com.charm.phr.mphr.ui.prescription.add_medication.AddMedicationActivityModule
import com.charm.phr.mphr.ui.prescription.add_supplement.AddSupplementActivity
import com.charm.phr.mphr.ui.prescription.add_supplement.AddSupplementActivityModule
import com.charm.phr.mphr.ui.problem_list.ActiveProblemListFragmentProvider
import com.charm.phr.mphr.ui.problem_list.HistoryProblemListFragmentProvider
import com.charm.phr.mphr.ui.problem_list.ProblemListActivity
import com.charm.phr.mphr.ui.problem_list.ProblemListActivityModule
import com.charm.phr.mphr.ui.problem_list.add_problem_list.AddProblemListActivity
import com.charm.phr.mphr.ui.problem_list.add_problem_list.AddProblemListActivityModule
import com.charm.phr.mphr.ui.procedure.ProcedureActivity
import com.charm.phr.mphr.ui.procedure.ProcedureActivityModule
import com.charm.phr.mphr.ui.procedure.add_procedure.AddProcedureActivity
import com.charm.phr.mphr.ui.procedure.add_procedure.AddProcedureActivityModule
import com.charm.phr.mphr.ui.questionnaire.QuestionnaireActivity
import com.charm.phr.mphr.ui.questionnaire.QuestionnaireActivityModule
import com.charm.phr.mphr.ui.questionnaire.QuestionnaireCurrentFragmentProvider
import com.charm.phr.mphr.ui.questionnaire.QuestionnairePastFragmentProvider
import com.charm.phr.mphr.ui.questionnaire.health_passport.QuestionnaireListActivity
import com.charm.phr.mphr.ui.questionnaire.health_passport.QuestionnaireListActivityModule
import com.charm.phr.mphr.ui.questionnaire.questionnaire_detail.QuestionnaireDetailActivity
import com.charm.phr.mphr.ui.questionnaire.questionnaire_detail.QuestionnaireDetailActivityModule
import com.charm.phr.mphr.ui.reminder.AllReminderFragmentProvider
import com.charm.phr.mphr.ui.reminder.AllReminderModule
import com.charm.phr.mphr.ui.reminder.CurrentReminderFragmentProvider
import com.charm.phr.mphr.ui.reminder.CurrentReminderModule
import com.charm.phr.mphr.ui.reminder.ReminderActivity
import com.charm.phr.mphr.ui.reminder.ReminderActivityModule
import com.charm.phr.mphr.ui.reminder.add_reminder.AddOrEditReminderActivity
import com.charm.phr.mphr.ui.reminder.add_reminder.AddReminderModule
import com.charm.phr.mphr.ui.reminder.reminder_history.ReminderHistoryActivity
import com.charm.phr.mphr.ui.reminder.reminder_history.ReminderHistoryModule
import com.charm.phr.mphr.ui.reminder.update_reminder.UpdateReminderHistoryActivity
import com.charm.phr.mphr.ui.reminder.update_reminder.UpdateReminderHistoryModule
import com.charm.phr.mphr.ui.support.SupportActivity
import com.charm.phr.mphr.ui.support.SupportActivityModule
import com.charm.phr.mphr.ui.swaasa.SwaasaActivity
import com.charm.phr.mphr.ui.swaasa.SwaasaActivityModule
import com.charm.phr.mphr.ui.tourpage.TourPageActivity
import com.charm.phr.mphr.ui.tourpage.TourPageActivityModule
import com.charm.phr.mphr.ui.useful_info.UsefulInfoDetailActivity
import com.charm.phr.mphr.ui.useful_info.UsefulInfoDetailActivityModule
import com.charm.phr.mphr.ui.useful_info.UsefulInfoListActivity
import com.charm.phr.mphr.ui.useful_info.UsefulInfoListActivityModule
import com.charm.phr.mphr.ui.vaccine.VaccineActivity
import com.charm.phr.mphr.ui.vaccine.VaccineActivityModule
import com.charm.phr.mphr.ui.vaccine.add_vaccine.AddVaccineActivity
import com.charm.phr.mphr.ui.vaccine.add_vaccine.AddVaccineActivityModule
//import com.charm.phr.mphr.ui.videocall.MyMeetingActivity
import com.charm.phr.mphr.ui.videocall.VideoCallActivity
import com.charm.phr.mphr.ui.videocall.VideoCallActivityModule
import com.charm.phr.mphr.ui.videocall.opentok.OpenTokVideoCallActivity
import com.charm.phr.mphr.ui.videocall.opentok.OpenTokVideoCallActivityModule
import com.charm.phr.mphr.ui.visitSummary.VisitSummaryActivity
import com.charm.phr.mphr.ui.visitSummary.VisitSummaryActivityModule
import com.charm.phr.mphr.ui.visitSummary.VisitSummaryListFragmentProvider
import com.charm.phr.mphr.ui.visitSummary.visitSummaryDetail.VisitSummaryDetailActivity
import com.charm.phr.mphr.ui.visitSummary.visitSummaryDetail.VisitSummaryDetailActivityModule
import com.charm.phr.mphr.ui.wellness.addWellness.AddWellnessActivity
import com.charm.phr.mphr.ui.wellness.addWellness.AddWellnessActivityModule
import com.charm.phr.mphr.ui.wellness.wellnessDetail.WellnessDetailActivity
import com.charm.phr.mphr.ui.wellness.wellnessDetail.WellnessDetailActivityModule
import com.charm.phr.mphr.ui.wellness.wellnessDetail.WellnessList1FragmentProvider
import com.charm.phr.mphr.ui.wellness.wellnessList.WellnessListActivity
import com.charm.phr.mphr.ui.wellness.wellnessList.WellnessListActivityModule
import com.charm.phr.mphr.zoom.MyMeetingActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = [LoginActivityModule::class])
    abstract fun bindLoginActivity(): LoginActivity

    @ContributesAndroidInjector(modules = [PatientListActivityModule::class])
    abstract fun bindPatientListActivity(): PatientListActivity

    @ContributesAndroidInjector(modules = [DashboardActivityModule::class, HomeFragmentProvider::class, HealthDataFragmentProvider::class])
    abstract fun bindDashboardActivity(): DashboardActivity

    @ContributesAndroidInjector(modules = [BillingActivityModule::class, InvoiceFragmentProvider::class, ReceiptFragmentProvider::class])
    abstract fun bindBillingActivity(): BillingActivity

    @ContributesAndroidInjector(modules = [BillingDetailActivityModule::class])
    abstract fun bindBillingDetailActivity(): BillingDetailActivity

    @ContributesAndroidInjector(modules = [MessageActivityModule::class, MessageInboxFragmentProvider::class, MessageSentFragmentProvider::class])
    abstract fun bindMessageActivity(): MessageActivity

    @ContributesAndroidInjector(modules = [MessageDetailActivityModule::class])
    abstract fun bindMessageDetailActivity(): MessageDetailActivity

    @ContributesAndroidInjector(modules = [LabActivityModule::class, LabOrderListFragmentProvider::class, LabResultListFragmentProvider::class])
    abstract fun bindLabActivity(): LabActivity

    @ContributesAndroidInjector(modules = [LabResultDetailActivityModel::class])
    abstract fun bindLabResultDetailActivity(): LabResultDetailActivity

    @ContributesAndroidInjector(modules = [AllergyActivityModule::class])
    abstract fun bindAllergyActivity(): AllergyActivity

    @ContributesAndroidInjector(modules = [AddAllergyActivityModule::class])
    abstract fun bindAddAllergyActivity(): AddAllergyActivity

    @ContributesAndroidInjector(modules = [PrescriptionActivityModule::class, MedicationFragmentProvider::class, SupplementFragmentProvider::class])
    abstract fun bindPrescriptionActivity(): PrescriptionActivity

    @ContributesAndroidInjector(modules = [AppointmentActivityModule::class])
    abstract fun bindAppointmentActivity(): AppointmentActivity

    @ContributesAndroidInjector(modules = [VisitSummaryActivityModule::class, VisitSummaryListFragmentProvider::class])
    abstract fun bindVisitSummaryActivity(): VisitSummaryActivity

    @ContributesAndroidInjector(modules = [ProcedureActivityModule::class])
    abstract fun bindProcedureActivity(): ProcedureActivity

    @ContributesAndroidInjector(modules = [PasscodeActivityModule::class])
    abstract fun bindPasscodeActivity(): PasscodeActivity

    @ContributesAndroidInjector(modules = [BookAppointmentActivityModule::class])
    abstract fun bindBookAppointmentActivity(): BookAppointmentActivity

    @ContributesAndroidInjector(modules = [ComposeMessageActivityModule::class, ComposeMessageFragmentProvider::class])
    abstract fun bindComposeMessageActivity(): ComposeMessageActivity

    @ContributesAndroidInjector(modules = [AddProcedureActivityModule::class])
    abstract fun bindAddProcedureActivity(): AddProcedureActivity

    @ContributesAndroidInjector(modules = [VisitSummaryDetailActivityModule::class])
    abstract fun bindVisitSummaryDetailActivity(): VisitSummaryDetailActivity

    @ContributesAndroidInjector(modules = [VaccineActivityModule::class])
    abstract fun bindVaccineActivity(): VaccineActivity

    @ContributesAndroidInjector(modules = [AddVaccineActivityModule::class])
    abstract fun bindAddVaccineActivity(): AddVaccineActivity

    @ContributesAndroidInjector(modules = [AddMedicationActivityModule::class])
    abstract fun bindAddMedicationActivity(): AddMedicationActivity

    @ContributesAndroidInjector(modules = [AddSupplementActivityModule::class])
    abstract fun bindAddSupplementActivity(): AddSupplementActivity

    @ContributesAndroidInjector(modules = [LabDetailPDFActivityModule::class])
    abstract fun bindLabDetailPDFActivity(): LabDetailPDFActivity

    @ContributesAndroidInjector(modules = [AnnouncementListActivityModule::class])
    abstract fun bindAnnouncementListActivity(): AnnouncementListActivity

    @ContributesAndroidInjector(modules = [AnnouncementDetailActivityModule::class])
    abstract fun bindAnnouncementDetailActivity(): AnnouncementDetailActivity

    @ContributesAndroidInjector(modules = [VideoCallActivityModule::class])
    abstract fun bindVideoCallActivity(): VideoCallActivity

    @ContributesAndroidInjector(modules = [UsefulInfoListActivityModule::class])
    abstract fun bindUsefulInfoListActivity(): UsefulInfoListActivity

    @ContributesAndroidInjector(modules = [UsefulInfoDetailActivityModule::class])
    abstract fun bindUsefulInfoDetailActivity(): UsefulInfoDetailActivity

    @ContributesAndroidInjector(modules = [AboutActivityModule::class, FacilityListFragmentProvider::class, MemberListFragmentProvider::class])
    abstract fun bindAboutActivity(): AboutActivity

    @ContributesAndroidInjector(modules = [QuestionnaireActivityModule::class, QuestionnairePastFragmentProvider::class, QuestionnaireCurrentFragmentProvider::class])
    abstract fun bindQuestionnaireActivity(): QuestionnaireActivity

    @ContributesAndroidInjector(modules = [QuestionnaireDetailActivityModule::class])
    abstract fun bindQuestionnaireDetailActivity(): QuestionnaireDetailActivity

    @ContributesAndroidInjector(modules = [TourPageActivityModule::class])
    abstract fun bindTourPageActivity(): TourPageActivity

    @ContributesAndroidInjector(modules = [OpenTokVideoCallActivityModule::class])
    abstract fun bindOpenTokVideoCallActivity(): OpenTokVideoCallActivity

    @ContributesAndroidInjector(modules = [AddProblemListActivityModule::class])
    abstract fun bindAddProblemListActivity(): AddProblemListActivity

    @ContributesAndroidInjector(modules = [AddPhysicianActivityModule::class])
    abstract fun bindAddPhysicianActivity(): AddPhysicianActivity

    @ContributesAndroidInjector(modules = [ProblemListActivityModule::class, ActiveProblemListFragmentProvider::class, HistoryProblemListFragmentProvider::class])
    abstract fun bindProblemListActivity(): ProblemListActivity

    @ContributesAndroidInjector(modules = [PhysicianActivityModule::class])
    abstract fun bindPhysicianActivity(): PhysicianActivity

    @ContributesAndroidInjector(modules = [HealthPassportActivityModule::class])
    abstract fun bindHealthPassportActivity(): HealthPassportActivity

    @ContributesAndroidInjector(modules = [DocumentActivityModule::class, DocumentListFragmentProvider::class, DocumentFolderFragmentProvider::class])
    abstract fun bindDocumentActivity(): DocumentActivity

    @ContributesAndroidInjector(modules = [AddDocumentActivityModule::class])
    abstract fun bindAddDocumentActivity(): AddDocumentActivity

    @ContributesAndroidInjector(modules = [PatientDemographicsActivityModule::class])
    abstract fun bindPatientDemographicsActivity(): PatientDemographicsActivity

    @ContributesAndroidInjector(modules = [SavePatDemographicsActivityModule::class, ImageCropperFragmentProvider::class])
    abstract fun bindSavePatDemographicsActivity(): SavePatDemographicsActivity

    @ContributesAndroidInjector(modules = [QuestionnaireListActivityModule::class])
    abstract fun bindSaveQuestionnaireListActivity(): QuestionnaireListActivity

    @ContributesAndroidInjector(modules = [PatDemoPDFActivityModule::class])
    abstract fun bindPatDemoPDFActivity(): PatDemoPDFActivity

    @ContributesAndroidInjector(modules = [PreviewDocumentActivityModule::class])
    abstract fun bindPreviewDocumentActivity(): PreviewDocumentActivity

    @ContributesAndroidInjector(modules = [MdCameraActivityModule::class, MdCameraFragmentProvider::class, MdPhotoPreviewFragmentProvider::class, ImageCropperFragmentProvider::class])
    abstract fun bindMdCameraActivity(): MdCameraActivity

    @ContributesAndroidInjector(modules = [DocumentListForFolderActivityModule::class])
    abstract fun bindDocumentListForFolderActivity(): DocumentListForFolderActivity

    @ContributesAndroidInjector(modules = [WellnessDetailActivityModule::class, WellnessList1FragmentProvider::class])
    abstract fun bindWellnessDetailActivity(): WellnessDetailActivity

    @ContributesAndroidInjector(modules = [AddWellnessActivityModule::class])
    abstract fun bindAddWellnessActivity(): AddWellnessActivity

    @ContributesAndroidInjector(modules = [WellnessListActivityModule::class])
    abstract fun bindWellnessListActivity(): WellnessListActivity

    @ContributesAndroidInjector(modules = [SwaasaActivityModule::class])
    abstract fun bindSwaasaActivity(): SwaasaActivity


//    @ContributesAndroidInjector(modules = [InitAuthSDKActivityModule::class])
//    abstract fun bindInitAuthSDKActivity(): InitAuthSDKActivity
//
//    @ContributesAndroidInjector(modules = [APIUserStartJoinMeetingActivityModule::class])
//    abstract fun bindAPIUserStartJoinMeetingActivity(): APIUserStartJoinMeetingActivity

//    @ContributesAndroidInjector(modules = [MyMeetingActivityModule::class])
//    abstract fun bindMyMeetingActivity(): MyMeetingActivity


    @ContributesAndroidInjector(modules = [FlowsheetListActivityModule::class])
    abstract fun bindFlowsheetListActivity(): FlowsheetListActivity

    @ContributesAndroidInjector(modules = [FlowsheetDataActivityModule::class])
    abstract fun bindFlowsheetDataActivity(): FlowsheetDataActivity

    @ContributesAndroidInjector(modules = [AddFlowsheetDataActivityModule::class])
    abstract fun bindAddFlowsheetDataActivity(): AddFlowsheetDataActivity

    @ContributesAndroidInjector(modules = [InsuranceListActivityModule::class])
    abstract fun bindInsuranceListActivity(): InsuranceListActivity

    @ContributesAndroidInjector(modules = [AddInsuranceActivityModule::class])
    abstract fun bindAddInsuranceActivity(): AddInsuranceActivity

    @ContributesAndroidInjector(modules = [SupportActivityModule::class])
    abstract fun bindSupportActivity(): SupportActivity

    @ContributesAndroidInjector(modules = [ReminderActivityModule::class, CurrentReminderFragmentProvider::class, CurrentReminderModule::class, AllReminderFragmentProvider::class, AllReminderModule::class])
    abstract fun bindReminderActivity(): ReminderActivity

    @ContributesAndroidInjector(modules = [AddReminderModule::class])
    abstract fun bindAddOrEditReminderActivity(): AddOrEditReminderActivity

    @ContributesAndroidInjector(modules = [ShareDocumentActivityModule::class])
    abstract fun bindShareDocumentActivity(): ShareDocumentActivity

    @ContributesAndroidInjector(modules = [AddContactActivityModule::class])
    abstract fun bindAddContactActivity(): AddContactActivity

    @ContributesAndroidInjector(modules = [CardOnFileActivityModule::class])
    abstract fun bindCardOnFileActivity(): CardOnFileActivity

    @ContributesAndroidInjector(modules = [ReminderHistoryModule::class])
    abstract fun bindReminderHistoryActivity(): ReminderHistoryActivity

    @ContributesAndroidInjector(modules = [AddOrEditCardOnFileActivityModule::class])
    abstract fun bindAddOrEditCardOnFileActivity(): AddOrEditCardOnFileActivity

    @ContributesAndroidInjector(modules = [ContactUsModule::class])
    abstract fun bindContactUsActivity(): ContactUsActivity

//    @ContributesAndroidInjector(modules = [GoogleFitSyncModule::class])
//    abstract fun bindGoogleFitSyncActivity(): GoogleFitSyncActivity

    @ContributesAndroidInjector(modules = [AppLockTypeModule::class])
    abstract fun bindLocalAuthActivity(): AppLockTypeActivity

    @ContributesAndroidInjector(modules = [AppLockActivityModule::class])
    abstract fun bindSecuritySettingActivity(): AppLockActivity

    @ContributesAndroidInjector(modules = [UpdateReminderHistoryModule::class])
    abstract fun bindUpdateReminderHistoryActivity(): UpdateReminderHistoryActivity

    @ContributesAndroidInjector(modules = [EasyTeleBookAppointmentModule::class])
    abstract fun bindEasyTeleBookAppointmentActivity(): EasyTeleBookAppointmentActivity

    @ContributesAndroidInjector(modules = [LabDetailsGraphActivityModule::class])
    abstract fun bindLabDetailsGraphActivity(): LabDetailsGraphActivity

    @ContributesAndroidInjector(modules = [UserProfileActivityModule::class])
    abstract fun bindUserProfileActivity(): UserProfileActivity

}