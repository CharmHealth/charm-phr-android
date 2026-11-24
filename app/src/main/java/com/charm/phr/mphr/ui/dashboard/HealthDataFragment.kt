package com.charm.phr.mphr.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.Module
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.about.AboutActivity
import com.charm.phr.mphr.ui.allergy.AllergyActivity
import com.charm.phr.mphr.ui.announcement.AnnouncementListActivity
import com.charm.phr.mphr.ui.appointment.AppointmentActivity
import com.charm.phr.mphr.ui.billing.BillingActivity
import com.charm.phr.mphr.ui.cardonfile.CardOnFileActivity
import com.charm.phr.mphr.ui.dashboard.compose.MyHealthScreen
import com.charm.phr.mphr.ui.demographics.PatientDemographicsActivity
import com.charm.phr.mphr.ui.document.DocumentActivity
import com.charm.phr.mphr.ui.flowsheet.FlowsheetListActivity
import com.charm.phr.mphr.ui.insurance.InsuranceListActivity
import com.charm.phr.mphr.ui.lab.LabActivity
import com.charm.phr.mphr.ui.message.MessageActivity
import com.charm.phr.mphr.ui.prescription.PrescriptionActivity
import com.charm.phr.mphr.ui.procedure.ProcedureActivity
import com.charm.phr.mphr.ui.questionnaire.QuestionnaireActivity
import com.charm.phr.mphr.ui.reminder.ReminderActivity
import com.charm.phr.mphr.ui.useful_info.UsefulInfoListActivity
import com.charm.phr.mphr.ui.vaccine.VaccineActivity
import com.charm.phr.mphr.ui.visitSummary.VisitSummaryActivity
import com.charm.phr.mphr.ui.wellness.wellnessList.WellnessListActivity
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.updateModuleVisibility
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Picasso
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class HealthDataFragment : DaggerFragment() {

    @Inject
    lateinit var authManager: AuthManager

    lateinit var dashboardViewModel: DashboardViewModel

    var isViewCreated = false

    val TAG = "HealthDataFragment"

    val handler = Handler(Looper.getMainLooper())

    @Inject
    lateinit var picasso: Picasso

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MPHRTheme {
                    MyHealthScreen(
                        viewModel = dashboardViewModel,
                        engagementList = getEngagementModules(),
                        recordsList = getRecordsModules(),
                        billingList = getBillingModules(),
                        resourcesList = getResourceModules(),
                        onScrollStateChange = { scrollOffset: Int ->
                            onScrollStateChange(scrollOffset)
                        },
                        onClickItem = {
                            onModuleSelect(it)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashboardViewModel = ViewModelProvider(requireActivity()).get(DashboardViewModel::class.java)
        isViewCreated = true

        dashboardViewModel.unreadCountLive.observe(viewLifecycleOwner, Observer { resp ->

            if (resp != null) {
                //TODO:Update unread count

//                healthDataListAdapter.updateUnreadCount(resp)
//                healthDataGridAdapter.updateUnreadCount(resp)
            }
        })

        dashboardViewModel.getUnreadCountLive()

        //In App Rating triggered
        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val criteriaId = sharedPreference.getLong(ModelUtil.isInAppRatingTriggered, 0L)
        if (criteriaId != 0L) {
            UIUtil.askUserForRating(requireActivity(), criteriaId)
            sharedPreference.edit().remove(ModelUtil.isInAppRatingTriggered).commit()
        }

    }

    private fun onScrollStateChange(scrollOffset: Int) {
        try {
            val dashboardActivity = activity as DashboardActivity
            val fabVisible = dashboardActivity.floatingActionButtonVisiiblity() == View.VISIBLE
            val hasPrivileges = dashboardViewModel.isComposeMessageAllowed || dashboardViewModel.isBookAppointmentAllowed
            if (scrollOffset > 0 && fabVisible) {
                dashboardActivity.hideFab()
            } else if (scrollOffset <= 0 && hasPrivileges && !fabVisible) {
                dashboardActivity.showFab()
            }
        } catch (e: Exception) {
            Log.d(TAG, "$e")
        }
    }

    private fun checkAndAddModule(
        moduleId: String,
        privilege: RbacUtil.Companion.RBACPrivileges?,
        moduleIconId: Int,
        moduleName: Int,
        moduleList: ArrayList<Module>,
    ): Module? {
        val hasPrivilege = privilege?.let { RbacUtil.checkPrivilege(it, requireContext()) } ?: true
        if (ModelUtil.isModuleEnabled(authManager.selectedPatientSection, moduleId) && hasPrivilege) {
            val module = Module(moduleId, moduleIconId = moduleIconId, moduleName = moduleName)
            moduleList.add(module)
            return module
        }
        return null
    }

    private fun getRecordsModules(): List<Module> {
        val recordsArrayList: ArrayList<Module> = ArrayList()

        val patient = authManager.selectedPatientLive.value
        val isHealthPassportAvailable = patient?.isHealthPassportAvailable ?: false

        checkAndAddModule(
            ModelUtil.visitSummaryModelId,
            RbacUtil.Companion.RBACPrivileges.VIEW_VISITSUMMARY,
            R.drawable.ic_visit_summary,
            R.string.visit_summary_str,
            recordsArrayList
        )
        checkAndAddModule(
            ModelUtil.documentModelId,
            RbacUtil.Companion.RBACPrivileges.VIEW_DOCUMENTS,
            R.drawable.ic_documents,
            R.string.document_str,
            recordsArrayList
        )

        if (isHealthPassportAvailable) {
            recordsArrayList.add(Module(ModelUtil.healthPassportModelId, R.string.health_passport_str, R.drawable.ic_health_passport))
        }

        checkAndAddModule(
            ModelUtil.medicationModelId,
            RbacUtil.Companion.RBACPrivileges.VIEW_MEDICATIONS,
            R.drawable.ic_prescriptions,
            R.string.prescription_str,
            recordsArrayList
        )
        checkAndAddModule(
            ModelUtil.allergiesModelId,
            RbacUtil.Companion.RBACPrivileges.VIEW_ALLERGIES,
            R.drawable.ic_allergies,
            R.string.allergy_str,
            recordsArrayList
        )
        checkAndAddModule(
            ModelUtil.labModelId,
            RbacUtil.Companion.RBACPrivileges.VIEW_LABRESULTS,
            R.drawable.ic_labs,
            R.string.labs_str,
            recordsArrayList
        )
        checkAndAddModule(
            ModelUtil.procedureModelId,
            RbacUtil.Companion.RBACPrivileges.VIEW_PROCEDURE,
            R.drawable.ic_procedures,
            R.string.procedure_str,
            recordsArrayList
        )
        checkAndAddModule(
            ModelUtil.vaccineModelId,
            RbacUtil.Companion.RBACPrivileges.VIEW_IMMUNE,
            R.drawable.ic_immunisations,
            R.string.vaccine_str,
            recordsArrayList
        )
        checkAndAddModule(
            ModelUtil.insuranceModelId,
            RbacUtil.Companion.RBACPrivileges.VIEW_INSURANCE,
            R.drawable.ic_insurance,
            R.string.insurance_str,
            recordsArrayList
        )
        recordsArrayList.forEach {
            val selectedModule = ModelUtil.getModule(authManager, it.moduleId)
            it.updateModuleVisibility(context, authManager)
            it.position = selectedModule?.sectionOrder ?: 0
            it.moduleNameFromSection = selectedModule?.sectionName
        }
        return recordsArrayList.filter { it.isShown }
    }

    private fun getEngagementModules(): List<Module> {

        val engagementArrayList: ArrayList<Module> = ArrayList()
        checkAndAddModule(
            ModelUtil.appointmentModelId,
            RbacUtil.Companion.RBACPrivileges.VIEW_APPOINTMENT,
            R.drawable.ic_appointment,
            R.string.appointments_str,
            engagementArrayList
        )
        checkAndAddModule(
            ModelUtil.messageModuleId,
            RbacUtil.Companion.RBACPrivileges.VIEW_MESSAGES,
            R.drawable.ic_messages,
            R.string.message_str,
            engagementArrayList
        )
        checkAndAddModule(
            ModelUtil.questionnaireModelId,
            RbacUtil.Companion.RBACPrivileges.VIEW_QUESTIONNAIRE,
            R.drawable.ic_questionnaires,
            R.string.questionnaire_str,
            engagementArrayList
        )
        checkAndAddModule(
            ModelUtil.flowsheetModelId,
            RbacUtil.Companion.RBACPrivileges.VIEW_FLOWSHEETDATA,
            R.drawable.ic_flowsheets,
            R.string.flowsheet_txt,
            engagementArrayList
        )
        checkAndAddModule(
            ModelUtil.wellnessModelId,
            RbacUtil.Companion.RBACPrivileges.VIEW_WELLNESS,
            R.drawable.ic_wellness,
            R.string.wellness_list_toolbar_txt,
            engagementArrayList
        )

        engagementArrayList.add(Module(ModelUtil.reminderModelId, R.string.reminder_str, R.drawable.ic_reminders))

        engagementArrayList.forEach {
            val selectedModule = ModelUtil.getModule(authManager, it.moduleId)
            it.updateModuleVisibility(context, authManager)
            it.position = selectedModule?.sectionOrder ?: 0
            it.moduleNameFromSection = selectedModule?.sectionName
        }

        return engagementArrayList.filter { it.isShown }

    }

    private fun getBillingModules(): List<Module> {

        val billingArrayList: ArrayList<Module> = ArrayList()
        val billingModule = checkAndAddModule(
            ModelUtil.billingModelId,
            RbacUtil.Companion.RBACPrivileges.VIEW_INVOICE,
            R.drawable.ic_invoice,
            R.string.billing_str,
            billingArrayList
        )

        billingArrayList.forEach {
            val selectedModule = ModelUtil.getModule(authManager, it.moduleId)
            it.updateModuleVisibility(context, authManager)
            it.position = selectedModule?.sectionOrder ?: 0
            it.moduleNameFromSection = selectedModule?.sectionName
        }

        //Note: Receipt separation is local module not from web
//        val isBillingEnabled = billingModule?.isShown ?: false
//        if (isBillingEnabled){
//            billingArrayList.add (Module(ModelUtil.receiptModelId, R.string.billing_receipts_str, R.drawable.ic_receipt, isShown = true))
//        }

        val isCardOnFileEnabled = isCardOnFileEnabled()
        if (isCardOnFileEnabled) {
            billingArrayList.add(
                Module(
                    ModelUtil.cardOnFileModelId,
                    R.string.payment_card_on_file,
                    R.drawable.ic_card_on_file,
                    isShown = true
                )
            )
        }

        return billingArrayList.filter { it.isShown }
    }

    private fun getResourceModules(): List<Module> {
        val resourcesList = listOf(
            Module(ModelUtil.usefulInfoModelId, R.string.useful_info_str, R.drawable.ic_useful_info),
            Module(ModelUtil.aboutModelId, R.string.about_str, R.drawable.ic_about)
        )
        resourcesList.forEach {
            val selectedModule = ModelUtil.getModule(authManager, it.moduleId)
            it.updateModuleVisibility(context, authManager)
            it.position = selectedModule?.sectionOrder ?: 0
            it.moduleNameFromSection = selectedModule?.sectionName
        }
        return resourcesList.filter { it.isShown }
    }

    fun onModuleSelect(moduleId: String) {
        try {
            var isWellnessActivity = false
            var isPrescriptionActivity = false
            var isMessageActivity = false
            var isHealthPassport = false
            var isBillingReceiptIntent = false
            var isBillingIntent = false
            var isAppointmentActivity = false
            var isQuestionnaireActivity = false
            var isDocumentActivity = false
            var isVisitSummaryActivity = false
            var isLabActivity = false

            val dashboardActivity = requireActivity() as DashboardActivity

            val classInstance = when (moduleId) {
                ModelUtil.appointmentModelId -> {
                    isAppointmentActivity = true
                    AppointmentActivity::class.java
                }

                ModelUtil.billingModelId -> {
                    isBillingIntent = true
                    BillingActivity::class.java
                }

                ModelUtil.cardOnFileModelId -> CardOnFileActivity::class.java
                ModelUtil.receiptModelId -> {
                    isBillingReceiptIntent = true
                    BillingActivity::class.java
                }

                ModelUtil.messageModuleId -> {
                    isMessageActivity = true
                    MessageActivity::class.java
                }

                ModelUtil.labModelId -> {
                    isLabActivity = true
                    LabActivity::class.java
                }

                ModelUtil.medicationModelId -> {
                    isPrescriptionActivity = true
                    PrescriptionActivity::class.java
                }

                ModelUtil.allergiesModelId -> AllergyActivity::class.java
                ModelUtil.visitSummaryModelId -> {
                    isVisitSummaryActivity = true
                    VisitSummaryActivity::class.java
                }

                ModelUtil.procedureModelId -> ProcedureActivity::class.java
                ModelUtil.questionnaireModelId -> {
                    isQuestionnaireActivity = true
                    QuestionnaireActivity::class.java
                }

                ModelUtil.documentModelId -> {
                    isDocumentActivity = true
                    DocumentActivity::class.java
                }

                ModelUtil.insuranceModelId -> InsuranceListActivity::class.java
                ModelUtil.announcementModelId -> AnnouncementListActivity::class.java
                ModelUtil.usefulInfoModelId -> UsefulInfoListActivity::class.java
                ModelUtil.aboutModelId -> AboutActivity::class.java
                ModelUtil.healthPassportModelId -> {
                    isHealthPassport = true
                    PatientDemographicsActivity::class.java
                }

                ModelUtil.flowsheetModelId -> FlowsheetListActivity::class.java
                ModelUtil.wellnessModelId -> {
                    isWellnessActivity = true
                    WellnessListActivity::class.java
                }

                ModelUtil.reminderModelId -> ReminderActivity::class.java
                else -> VaccineActivity::class.java
            }

            val intent = Intent(requireContext(), classInstance)
            if (isBillingReceiptIntent) {
                intent.putExtra(ModelUtil.billingInvoiceIntent, true)
                dashboardActivity.intentToBillingWithResult(intent)
                activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return
            }
            if (isBillingIntent) {
                dashboardActivity.intentToBillingWithResult(intent)
                activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return
            }
            if (isMessageActivity) {
                intent.putExtra(ModelUtil.messageUnreadCountIntent, dashboardViewModel.unreadCountLive.value?.messageUnreadCount ?: 0)
                dashboardActivity.intentToMessageWithResult(intent)
                activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return
            }
            if (isQuestionnaireActivity) {
                dashboardActivity.intentToQuestionnaireWithResult(intent)
                activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return
            }
            if (isAppointmentActivity) {
                val filteredList = dashboardViewModel.composeMsgRestrictionList.filter { !it.isMessageRestrict && it.allowPHRMessaging }
                intent.putExtra(
                    ModelUtil.messageComposeMsgRestrictionListIntent, MDJsonBuilder.formComposeMsgRestrictionList(filteredList)
                )
                dashboardActivity.intentToAppointmentWithResult(intent)
                activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return
            }
            if (isVisitSummaryActivity || isDocumentActivity || isLabActivity) {
                dashboardActivity.updateUnreadCountLauncher(intent)
                activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                return
            }
            if (isPrescriptionActivity) {
                activity?.startActivityForResult(intent, 7)
            } else if (isHealthPassport) {
                activity?.startActivityForResult(intent, DashboardActivity.patientDemographicsActivityCode)
            } else if (isWellnessActivity) {
                activity?.startActivityForResult(intent, 8)
            } else {
                activity?.startActivityForResult(intent, 0)
            }
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } catch (e: Exception) {

        }
    }

    private fun isCardOnFileEnabled(): Boolean {
        if (BuildUtil.isCustomApp) {
            return false
        }
        val selectedPatient = authManager.selectedPatientLive.value
        if (selectedPatient != null) {
            val practiceList = ModelUtil.getBluefinEnabledPractice(selectedPatient)
            if (!practiceList.isNullOrEmpty()) {
               return  true
            }
        }
        return false
    }

}
