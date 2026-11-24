package com.charm.phr.mphr.ui.dashboard


import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentHomeBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.LabParamHistoryPOJO
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.model.MriResultPOJO
import com.charm.phr.mphr.model.PHRBills
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.PatientQuesMap
import com.charm.phr.mphr.model.PatientVitals
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.model.SectionConfigList
import com.charm.phr.mphr.model.TrackerMetricMap
import com.charm.phr.mphr.model.TransfusionLogData
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.appointment.AppointmentActivity
import com.charm.phr.mphr.ui.appointment.UpcomingAppointmentListAdapter
import com.charm.phr.mphr.ui.billing.BillingActivity
import com.charm.phr.mphr.ui.billing.billingDetail.BillingDetailActivity
import com.charm.phr.mphr.ui.components.MDCancelAppointmentDialog
import com.charm.phr.mphr.ui.components.compose.MdCustomMessageDialog
import com.charm.phr.mphr.ui.components.compose.MdMessageActionCustomDialog
import com.charm.phr.mphr.ui.dashboard.compose.LastMriResultCard
import com.charm.phr.mphr.ui.dashboard.compose.MriCardiacAlert
import com.charm.phr.mphr.ui.dashboard.compose.TransfusionBannerCard
import com.charm.phr.mphr.ui.dashboard.compose.transfusionCountdown
import com.charm.phr.mphr.ui.message.MessageActivity
import com.charm.phr.mphr.ui.message.compose.ComposeMessageActivity
import com.charm.phr.mphr.ui.message.messageDetail.MessageDetailActivity
import com.charm.phr.mphr.ui.prescription.PrescriptionActivity
import com.charm.phr.mphr.ui.questionnaire.QuestionnaireActivity
import com.charm.phr.mphr.ui.questionnaire.questionnaire_detail.QuestionnaireDetailActivity
import com.charm.phr.mphr.ui.videocall.VideoCallActivity
import com.charm.phr.mphr.ui.wellness.addWellness.AddWellnessActivity
import com.charm.phr.mphr.ui.wellness.wellnessDetail.WellnessDetailActivity
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.UIUtil.Companion.hideView
import com.squareup.picasso.Picasso
import dagger.android.support.DaggerFragment
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : DaggerFragment() {


    lateinit var dashboardViewModel: DashboardViewModel


    @Inject
    lateinit var upcomingAppointmentMonthListAdapter: UpcomingAppointmentMonthListAdapter

    @Inject
    lateinit var activeMedicationListAdapter: ActiveMedicationListAdapter

    @Inject
    lateinit var invoiceListAdapter: InvoiceListAdapter

    @Inject
    lateinit var recentMessageListAdapter: RecentMessageListAdapter

    @Inject
    lateinit var questionnaireListAdapter: QuestionnaireListAdapter

    @Inject
    lateinit var todayAppointmentListAdapter: TodayAppointmentListAdapter

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var sharedPreferences: SharedPreferences


    val handler = Handler(Looper.getMainLooper())

    private var totalReqCount = 6

    @Inject
    lateinit var picasso: Picasso

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using view binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashboardViewModel =
            ViewModelProvider(requireActivity()).get(DashboardViewModel::class.java)
        binding.homeUpcomingAppointmentRV.layoutManager =
            LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)
        binding.homeUpcomingAppointmentRV.adapter = upcomingAppointmentMonthListAdapter

        binding.homeTodayAppointmentRV.layoutManager =
            LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)
        binding.homeTodayAppointmentRV.adapter = todayAppointmentListAdapter


        binding.homeActiveMedicationsRV.layoutManager =
            LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)
        binding.homeActiveMedicationsRV.adapter = activeMedicationListAdapter

        binding.homeInvoiceRV.layoutManager =
            LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)
        binding.homeInvoiceRV.adapter = invoiceListAdapter

        binding.homeRecentMessageRV.layoutManager =
            LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)
        binding.homeRecentMessageRV.adapter = recentMessageListAdapter

        binding.homeQuestionnaireRV.layoutManager =
            LinearLayoutManager(this.activity, RecyclerView.VERTICAL, false)
        binding.homeQuestionnaireRV.adapter = questionnaireListAdapter

        binding.homeProgress.root.visibility = View.VISIBLE
        binding.homeScrollView.visibility = View.GONE
        binding.homeErrorLayout.root.visibility = View.GONE

        binding.homeScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->

            Log.d(
                "MDHomeScroll",
                "scrollX -> $scrollX , scrollY -> $scrollY , oldScrollX -> $oldScrollX , oldScrollY -> $oldScrollY"
            )
            try {

                val dashboardActivity = activity as DashboardActivity
                val hasPrivileges =
                    dashboardViewModel.isComposeMessageAllowed || dashboardViewModel.isBookAppointmentAllowed
                if (scrollY > oldScrollY && dashboardActivity.floatingActionButtonVisiiblity() == View.VISIBLE) {
                    dashboardActivity.hideFab()
                } else if (hasPrivileges && scrollY < oldScrollY && dashboardActivity.floatingActionButtonVisiiblity() != View.VISIBLE
                ) {
                    dashboardActivity.showFab()
                }
                /*
                                handler.removeCallbacks(runnable)
                                handler.postDelayed( runnable , 100)*/
            } catch (e: Exception) {

            }
        }

        /* binding.homeScrollView.setOnTouchListener { v, event ->
             if (event.action == MotionEvent.ACTION_UP) {
                 handler.removeCallbacks(runnable)
                 handler.postDelayed( runnable , 100)
             }
             false;
         }
 */


        /*
                binding.homeScrollView.setOnScrollStoppedListener(object : MDNestedScrollView.OnScrollStoppedListener{
                    override fun onScrollStopped() {
                        try {

                            val dashboardActivity = activity as DashboardActivity
                            if (dashboardActivity.floatingActionButtonVisiiblity() != View.VISIBLE) {
                                dashboardActivity.showFab();
                            }
                        }
                        catch (e: Exception){

                        }
                    }
                })
        */


        val onClickListener = object : UpcomingAppointmentListAdapter.OnClickListener {
            override fun onVideoCallBtnClickListener(appointment: Appointment) {

                try {
                    if (!ModelUtil.isVideoCallInProgress()) {
                        val intent =
                            Intent(this@HomeFragment.activity, VideoCallActivity::class.java)
                        intent.putExtra(ModelUtil.appointmentIdIntent, appointment.appointmentId)
                        intent.putExtra(
                            ModelUtil.appointmentTimeIntent,
                            appointment.appointmentDate?.toString(ModelUtil.charmCompleteDateYearFormatter())
                        )
                        intent.putExtra(
                            ModelUtil.memberNameIntent,
                            UIUtil.formMemberName(appointment.member)
                        )
                        intent.putExtra(ModelUtil.practiceIdIntent, appointment.practiceId)

                        startActivity(intent)
                    } else {
                        UIUtil.showToast(
                            this@HomeFragment.context,
                            R.string.video_call_already_in_meeting_str,
                            Toast.LENGTH_LONG
                        )
                    }
                } catch (e: Exception) {

                }

            }

            override fun onQuestionnaireClickListener(appointment: Appointment) {
                val intent = Intent(this@HomeFragment.activity, QuestionnaireActivity::class.java)
                intent.putExtra(ModelUtil.appointmentIdIntent, appointment.appointmentId)
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onCardClick() {
                val intent = Intent(this@HomeFragment.activity, AppointmentActivity::class.java)
                val filteredList =
                    dashboardViewModel.composeMsgRestrictionList.filter { !it.isMessageRestrict && it.allowPHRMessaging }
                intent.putExtra(
                    ModelUtil.messageComposeMsgRestrictionListIntent,
                    MDJsonBuilder.formComposeMsgRestrictionList(filteredList)
                )
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onDelete(
                appointment: Appointment,
                position: Int,
                parentPosition: Int,
                isFromToday: Boolean
            ) {
                if (appointment.appointmentCancelOption == null) {
                    showCancelAppointmentComposeView(getString(R.string.cancel_appointment_restriction_msg))
                } else if (appointment.appointmentCancelOption?.isEmpty() == true) {
                    deleteUpcomingAppointment(appointment, position, parentPosition, isFromToday)
                } else {
                    showCancelAppointmentComposeView(
                        getString(
                            R.string.appointment_cancel_error_msg,
                            appointment.appointmentCancelOption
                        )
                    )
                }
            }
        }

        upcomingAppointmentMonthListAdapter.setListener(onClickListener)
        todayAppointmentListAdapter.setOnClickListener(onClickListener)

        if (!RbacUtil.checkPrivilege(
                RbacUtil.Companion.RBACPrivileges.ADD_APPOINTMENT,
                requireContext()
            )
        ) {
            binding.homeUpcomingAppointmentBookBtn.hideView()
        }
        binding.homeUpcomingAppointmentBookBtn.setOnClickListener {
            try {
                val dashboardActivity = activity
                if (dashboardActivity is DashboardActivity) {

                    dashboardActivity.intentToBookAppointment()
                }
            } catch (e: Exception) {

            }
        }

        binding.homeActiveMedicationShowMoreTV.setOnClickListener {
            try {
                intentToPrescriptionActivity()
            } catch (e: Exception) {

            }
        }

        binding.homeRecentMessageShowMoreTV.setOnClickListener {
            try {
                val intent = Intent(requireActivity(), MessageActivity::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            } catch (e: Exception) {

            }
        }

        binding.homeInvoiceShowMoreTV.setOnClickListener {
            try {
                val intent = Intent(requireActivity(), BillingActivity::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            } catch (e: Exception) {

            }
        }

        binding.homeQuestionnaireShowMoreTV.setOnClickListener {
            try {
                val intent = Intent(requireActivity(), QuestionnaireActivity::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            } catch (e: Exception) {

            }
        }


        recentMessageListAdapter.setMessageSelectListener(object : MessageActivity.MessageListener {
            override fun onMessageSelect(messages: Messages, itemView: View, position: Int) {

                if (dashboardViewModel.isComposeMsgRestrictionFetched) {

                    val intent =
                        Intent(this@HomeFragment.context, MessageDetailActivity::class.java)
                    intent.putExtra(ModelUtil.messageIdIntent, messages.messageId)
                    intent.putExtra(ModelUtil.practiceIdIntent, messages.practiceId)
                    intent.putExtra(
                        ModelUtil.messageDateIntent,
                        messages.messageDate?.toString(ModelUtil.sqlTimeStampFormattor())
                    )

                    val filteredArray =
                        ArrayList(dashboardViewModel.composeMsgRestrictionList.filter { it.practiceId == messages.practiceId })

                    if (filteredArray.isEmpty()) {
                        filteredArray.add(
                            ComposeMsgRestriction(
                                practiceId = messages.practiceId
                                    ?: ""
                            )
                        )
                    }

                    dashboardViewModel.selectedMessagePosition = position
                    intent.putExtra(
                        ModelUtil.messageReceipientIdIntent,
                        messages.messageRecipient?.messageRecipientId
                    )
                    intent.putExtra(
                        ModelUtil.messageComposeMsgRestrictionListIntent,
                        MDJsonBuilder.formComposeMsgRestrictionList(filteredArray)
                    )
                    requireActivity().startActivityForResult(intent, DashboardActivity.messageCode)
                    requireActivity().overridePendingTransition(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                } else {
                    dashboardViewModel.message = messages
                    dashboardViewModel.isComposeDetailClicked = true
                    val messagesActivity = activity
                    if (messagesActivity is DashboardActivity) {
                        messagesActivity.progressDialog.show()
                    }
                }
            }

            override fun onMessageDelete(messages: Messages, position: Int) {

            }
        })

        invoiceListAdapter.setInvoiceSelectListener(object :
            com.charm.phr.mphr.ui.billing.InvoiceListAdapter.InvoiceSelectListener {
            override fun onInvoiceSelect(phrBill: PHRBills) {

                val intent = Intent(context, BillingDetailActivity::class.java)
                intent.putExtra(ModelUtil.billingType, ModelUtil.billingTypeInvoice)
                intent.putExtra(ModelUtil.billingIsStatementIntent, phrBill.isStatement)
                intent.putExtra(
                    ModelUtil.phrBillIdIntent, if (phrBill.isStatement) {
                        phrBill.statementId
                    } else {
                        phrBill.phrBillId
                    }
                )
                if (ModelUtil.isAmountValid(phrBill.amount) && phrBill.isPaid == false) {
                    val pair = ModelUtil.getDueAndPaymentRequested(phrBill)
                    val paymentRequested = pair.second
                    intent.putExtra(ModelUtil.billingIsPaymentRequested, true)
                    intent.putExtra(
                        ModelUtil.billingDate,
                        phrBill.billDate?.toString(ModelUtil.charmDisplayDateFormat())
                    )
                    intent.putExtra(ModelUtil.billingFacilityName, phrBill.facility?.facilityName)
                    intent.putExtra(ModelUtil.billingPaymentReqAmt, phrBill.amount)
                    intent.putExtra(
                        ModelUtil.billingPaymentReqCurrency,
                        phrBill.practiceCurrencySymbol
                    )
                    intent.putExtra(ModelUtil.billingInvoiceNum, phrBill.billNumber)
                    intent.putExtra(ModelUtil.billingPracticeIdIntent, phrBill.practiceId)
                    intent.putExtra(ModelUtil.billingFacilityIdIntent, phrBill.facility?.facilityId)
                    intent.putExtra(ModelUtil.billingPaymentGatewayIntent, phrBill.paymentGateway)
                    intent.putExtra(ModelUtil.billingMemberIdIntent, phrBill.memberId ?: "")
                }
                activity?.startActivityForResult(intent, 0)
                activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            }

        })

        questionnaireListAdapter.setQuestionnaireSelectListener(object :
            QuestionnaireActivity.QuestionnaireSelectListener {
            override fun onQuestionnaireSelect(physicianTemplates: PhysicianTemplates) {
                val intent = Intent(activity!!, QuestionnaireDetailActivity::class.java)
                intent.putExtra(
                    ModelUtil.questionnairePhysicianTemplatesIntent,
                    physicianTemplates.toString
                )
                activity!!.startActivity(intent)
                activity!!.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        })

        activeMedicationListAdapter.setActiveMedicationListener(object :
            ActiveMedicationListAdapter.ActiveMedicationListener {
            override fun onMedicationSelected() {
                intentToPrescriptionActivity()
            }

        })

        dashboardViewModel.fragmentCallbackLive.observe(viewLifecycleOwner, Observer { resp ->
            if (resp != null) {
                if (resp is SectionConfigList) {
                    startPopulate(resp, true)
                } else if (resp is DashboardViewPagerAdapter.DashboardCallback) {
                    if (resp == DashboardViewPagerAdapter.DashboardCallback.PROGRESS) {
                        showProgress()
                    }
                }
            } else {
                startPopulate(null, true)
            }
        })

        if (authManager.selectedPatientSection != null) {
            fetchFeeds()
        }

        binding.homeErrorLayout.errorPageTryAgainBtn.setOnClickListener {
            fetchFeeds()
        }


        //TODO remove
        /* val intent = Intent(this@HomeFragment.activity, BookAppointmentActivity::class.java)
         startActivity(intent)
         activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)*/
    }

    fun intentToPrescriptionActivity() {
        try {
            val intent = Intent(requireActivity(), PrescriptionActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        } catch (e: Exception) {

        }
    }

    override fun onResume() {
        super.onResume()

        Log.d("MDResume", "Yes")

        todayAppointmentListAdapter.notifyDataSetChanged()

        val prefs = sharedPreferences
        if (prefs.getBoolean(ModelUtil.MEDICATION_UPDATED, false)) {
            prefs.edit().putBoolean(ModelUtil.MEDICATION_UPDATED, false).apply()
            dashboardViewModel.isActiveMedicationFetch = false
            dashboardViewModel.activeMedicationList = arrayListOf()
            val isMedicationEnabled = RbacUtil.checkPrivilege(
                RbacUtil.Companion.RBACPrivileges.VIEW_MEDICATIONS, context
            ) && ModelUtil.isModuleEnabled(
                authManager.selectedPatientSection, ModelUtil.medicationModelId
            )
            dashboardViewModel.getActiveMedications()
                .observe(viewLifecycleOwner, Observer { resp ->
                    if (resp is Pair<*, *>) {
                        dashboardViewModel.isActiveMedicationFetch = true
                        dashboardViewModel.activeMedicationList = resp.first as ArrayList<PatMedPOJO>
                        setActiveMedicationList(isMedicationEnabled)
                    }
                })
        }

        if (BuildUtil.isThalassemia) {
            val prefs = sharedPreferences

            if (prefs.getBoolean(ModelUtil.TRANSFUSION_LOG_UPDATED, false)) {
                prefs.edit().putBoolean(ModelUtil.TRANSFUSION_LOG_UPDATED, false).apply()
                dashboardViewModel.isTransfusionLogFetched = false
                dashboardViewModel.transfusionLogData = null
                val id = dashboardViewModel.transfusionLogFlowsheetId
                val practiceId = dashboardViewModel.transfusionLogPracticeId
                if (!id.isNullOrBlank() && !practiceId.isNullOrBlank()) {
                    fetchTransfusionLogData(id, practiceId)
                }
            }

            if (prefs.getBoolean(ModelUtil.FERRITIN_MONITORING_UPDATED, false)) {
                prefs.edit().putBoolean(ModelUtil.FERRITIN_MONITORING_UPDATED, false).apply()
                dashboardViewModel.isLastLabResultsFetched = false
                dashboardViewModel.lastFerritinPOJO = null
                dashboardViewModel.ironTrendHistoryPOJO = null
                dashboardViewModel.isIronTrendFetched = false
                val id = dashboardViewModel.ferritinMonitoringFlowsheetId
                val practiceId = dashboardViewModel.ferritinMonitoringPracticeId
                if (!id.isNullOrBlank() && !practiceId.isNullOrBlank()) {
                    fetchLastFerritinResult(id, practiceId)
                }
            }

            if (prefs.getBoolean(ModelUtil.MRI_T2_MONITORING_UPDATED, false)) {
                prefs.edit().putBoolean(ModelUtil.MRI_T2_MONITORING_UPDATED, false).apply()
                dashboardViewModel.isLastMriResultFetched = false
                dashboardViewModel.lastMriResultPOJO = null
                val id = dashboardViewModel.mriT2MonitoringFlowsheetId
                val practiceId = dashboardViewModel.mriT2MonitoringPracticeId
                if (!id.isNullOrBlank() && !practiceId.isNullOrBlank()) {
                    fetchLastMriResult(id, practiceId)
                }
            }
        }
    }

    fun fetchFeeds() {

        if (dashboardViewModel.isfetchFeedsCalled) {
            return
        }
        dashboardViewModel.isfetchFeedsCalled = true

        val isAppointmentEnabled = RbacUtil.checkPrivilege(
            RbacUtil.Companion.RBACPrivileges.VIEW_APPOINTMENT,
            context
        ) && ModelUtil.isModuleEnabled(
            authManager.selectedPatientSection,
            ModelUtil.appointmentModelId
        )
        val isMedicationEnabled = RbacUtil.checkPrivilege(
            RbacUtil.Companion.RBACPrivileges.VIEW_MEDICATIONS,
            context
        ) && ModelUtil.isModuleEnabled(
            authManager.selectedPatientSection,
            ModelUtil.medicationModelId
        )
        val isVitalEnabled = RbacUtil.checkPrivilege(
            RbacUtil.Companion.RBACPrivileges.VIEW_WELLNESS,
            context
        ) && ModelUtil.isModuleEnabled(
            authManager.selectedPatientSection,
            ModelUtil.wellnessModelId
        )
        val isInvoiceEnabled =
            RbacUtil.checkPrivilege(
                RbacUtil.Companion.RBACPrivileges.VIEW_INVOICE,
                context
            ) && ModelUtil.isModuleEnabled(
                authManager.selectedPatientSection,
                ModelUtil.billingModelId
            )
        val isMessageEnabled = RbacUtil.checkPrivilege(
            RbacUtil.Companion.RBACPrivileges.VIEW_MESSAGES,
            context
        ) && ModelUtil.isModuleEnabled(
            authManager.selectedPatientSection,
            ModelUtil.messageModuleId
        )
        val isQuestionnaireEnabled = RbacUtil.checkPrivilege(
            RbacUtil.Companion.RBACPrivileges.VIEW_QUESTIONNAIRE,
            context
        ) && ModelUtil.isModuleEnabled(
            authManager.selectedPatientSection,
            ModelUtil.questionnaireModelId
        )

        if (!isAppointmentEnabled) {
            dashboardViewModel.isUpcomingAppointmentFetch = true
            dashboardViewModel.upcomingAppointmentList = arrayListOf()
        }
        if (!isMedicationEnabled) {
            dashboardViewModel.isActiveMedicationFetch = true
            dashboardViewModel.activeMedicationList = arrayListOf()
        }
        if (!isVitalEnabled) {
            dashboardViewModel.isVitalsFetch = true
            dashboardViewModel.vitalsList = arrayListOf()
        }
        if (!isInvoiceEnabled) {
            dashboardViewModel.isInvoiceFetch = true
            dashboardViewModel.invoiceList = arrayListOf()
        }
        if (!isMessageEnabled) {
            dashboardViewModel.isMessageFetch = true
            dashboardViewModel.messageList = arrayListOf()
        }
        if (!isQuestionnaireEnabled) {
            dashboardViewModel.isQuestionnaireFetch = true
            dashboardViewModel.questionnaireList = arrayListOf()
        }

        if (BuildUtil.isThalassemia) {
            binding.homeThalassemiaLayout.visibility = View.VISIBLE
            fetchThalassemiaFlowsheetIds()
        }

        if (dashboardViewModel.isActiveMedicationFetch && dashboardViewModel.isUpcomingAppointmentFetch && dashboardViewModel.isVitalsFetch && dashboardViewModel.isInvoiceFetch && dashboardViewModel.isMessageFetch && dashboardViewModel.isQuestionnaireFetch) {
            binding.homeProgress.root.visibility = View.GONE
            binding.homeScrollView.visibility = View.VISIBLE
            binding.homeErrorLayout.root.visibility = View.GONE
            setActiveMedicationList(isMedicationEnabled)
            setUpcomingAppointment(isAppointmentEnabled)
            setVitals(isVitalEnabled)
            setInvoice(isInvoiceEnabled)
            setMessage(isMessageEnabled)
            setQuestionnaire(isQuestionnaireEnabled)
            dashboardViewModel.isDashboardProgressStillPresent = false
            dashboardViewModel.isfetchFeedsCalled = false

            /*  binding.homeScrollView.post {
                  binding.homeScrollView.smoothScrollTo(0 , 0)
              }*/
        } else {

            dashboardViewModel.totalRespCount = 0

            binding.homeProgress.root.visibility = View.VISIBLE
            binding.homeScrollView.visibility = View.GONE


            dashboardViewModel.isDashboardProgressStillPresent = true

            if (!dashboardViewModel.isQuestionnaireFetch) {
                dashboardViewModel.getCurrentQuestionnaireList()
                    .observe(viewLifecycleOwner, Observer { resp ->
                        if (resp != null) {
                            dashboardViewModel.totalRespCount++
                            if (resp is ArrayList<*>) {
                                dashboardViewModel.isQuestionnaireFetch = true
                                val physicianTemplates = resp as ArrayList<PhysicianTemplates>
                                dashboardViewModel.questionnaireList.clear()
                                dashboardViewModel.questionnaireList.addAll(physicianTemplates.filter { physicianTemplate ->
                                    val patientQuesMap = physicianTemplate.patientQuesMap
                                        ?: PatientQuesMap()
                                    patientQuesMap.appointment?.appointmentId.isNullOrBlank() && (!patientQuesMap.isSubmitted && !patientQuesMap.isFilled)
                                })
                                setQuestionnaire(isQuestionnaireEnabled)
                            } else if (resp is APIResponseError) {

                            }
                            checkAllDataIsReceived()
                        }
                    })
            } else {
                dashboardViewModel.totalRespCount++
                setQuestionnaire(isQuestionnaireEnabled)
            }

            if (!dashboardViewModel.isMessageFetch) {
                dashboardViewModel.getMessageList().observe(viewLifecycleOwner, Observer { resp ->
                    if (resp != null) {
                        dashboardViewModel.totalRespCount++
                        if (resp is Pair<*, *>) {
                            dashboardViewModel.isMessageFetch = true

                            val messages = resp.first as ArrayList<Messages>
                            dashboardViewModel.messageList.clear()
                            val unreadMessages =
                                messages.filter { it.messageRecipient?.status == 0 }
                            dashboardViewModel.messageList.addAll(unreadMessages)
                            setMessage(isMessageEnabled)
                        } else if (resp is APIResponseError) {
                        }
                        checkAllDataIsReceived()
                    }
                })
            } else {
                dashboardViewModel.totalRespCount++
                setMessage(isMessageEnabled)
            }

            if (!dashboardViewModel.isInvoiceFetch) {
                dashboardViewModel.getInvoiceList().observe(viewLifecycleOwner, Observer { resp ->
                    if (resp != null) {
                        dashboardViewModel.totalRespCount++
                        if (resp is Pair<*, *>) {
                            dashboardViewModel.isInvoiceFetch = true

                            val phrBills = resp.first as ArrayList<PHRBills>
                            dashboardViewModel.invoiceList.clear()
                            dashboardViewModel.invoiceList.addAll(phrBills.filter { bill ->
                                ModelUtil.isAmountValid(bill.amount) && bill.isPaid == false
                            })

                            setInvoice(isInvoiceEnabled)
                        } else if (resp is APIResponseError) {
                        }

                        checkAllDataIsReceived()
                    }
                })
            } else {
                dashboardViewModel.totalRespCount++
                setInvoice(isInvoiceEnabled)
            }

            if (!dashboardViewModel.isActiveMedicationFetch) {

                dashboardViewModel.getActiveMedications()
                    .observe(viewLifecycleOwner, Observer { resp ->
                        if (resp != null) {
                            dashboardViewModel.totalRespCount++
                            if (resp is Pair<*, *>) {
                                dashboardViewModel.isActiveMedicationFetch = true
                                val activeMedicationList = resp.first as ArrayList<PatMedPOJO>
                                dashboardViewModel.activeMedicationList = activeMedicationList


                            } else if (resp is APIResponseError) {
                            }

                            setActiveMedicationList(isMedicationEnabled)


                            checkAllDataIsReceived()
                        }
                    })
            } else {
                dashboardViewModel.totalRespCount++
                setActiveMedicationList(isMedicationEnabled)
            }

            if (!dashboardViewModel.isVitalsFetch) {
                dashboardViewModel.getVitals().observe(viewLifecycleOwner, Observer { resp ->

                    if (resp != null) {
                        dashboardViewModel.totalRespCount++

                        if (resp is ArrayList<*>) {
                            dashboardViewModel.isVitalsFetch = true
                            dashboardViewModel.vitalsList = resp as ArrayList<PatientVitals>
                        } else if (resp is APIResponseError) {
                        }

                        setVitals(isVitalEnabled)

                        checkAllDataIsReceived()
                    }
                })
            } else {
                dashboardViewModel.totalRespCount++
                setVitals(isVitalEnabled)
            }

            if (!dashboardViewModel.isUpcomingAppointmentFetch) {
                dashboardViewModel.getUpcomingAppointment()
                    .observe(viewLifecycleOwner, Observer { resp ->
                        if (resp != null) {

                            dashboardViewModel.totalRespCount++

                            if (resp is ArrayList<*>) {
                                dashboardViewModel.isUpcomingAppointmentFetch = true
                                val upcomingAppointmentList = resp as ArrayList<Appointment>
                                dashboardViewModel.upcomingAppointmentList = upcomingAppointmentList
                            } else if (resp is APIResponseError) {
                            }

                            setUpcomingAppointment(isAppointmentEnabled)
                            checkAllDataIsReceived()
                        }

                    })
            } else {
                dashboardViewModel.totalRespCount++
                setUpcomingAppointment(isAppointmentEnabled)
            }

            checkAllDataIsReceived()
        }
    }

    private fun setQuestionnaire(isQuestionnaireEnabled: Boolean) {

        Log.d("isQuestionnaireEnabled", isQuestionnaireEnabled.toString())

        if (isQuestionnaireEnabled) {
            // add show card
            val questionnaireList = dashboardViewModel.questionnaireList

            if (questionnaireList.isNotEmpty()) {

                binding.homeQuestionnaireNoDataLayout.visibility = View.GONE
                binding.homeQuestionnaireCard.visibility = View.VISIBLE
                questionnaireListAdapter.setQuestionnaireList(questionnaireList)
                binding.homeQuestionnaireShowMoreTV.visibility = View.VISIBLE

            } else {
                binding.homeQuestionnaireCard.visibility = View.GONE
            }
        } else {
            // add hide card
            binding.homeQuestionnaireCard.visibility = View.GONE
        }
    }

    private fun setMessage(isMessageEnabled: Boolean) {

        Log.d("isMessageEnabled", isMessageEnabled.toString())

        if (isMessageEnabled) {
            // add show card
            val messageList = dashboardViewModel.messageList

            if (messageList.isNotEmpty()) {

                binding.homeRecentMessageCard.visibility = View.VISIBLE
                recentMessageListAdapter.setMessageList(messageList)
                binding.homeRecentMessageRV.visibility = View.VISIBLE
                binding.homeRecentMessageNoDataLayout.visibility = View.GONE
                binding.homeRecentMessageShowMoreTV.visibility = View.VISIBLE

            } else {
                binding.homeRecentMessageCard.visibility = View.GONE
            }

        } else {
            // add hide card
            binding.homeRecentMessageCard.visibility = View.GONE
        }
    }

    private fun setInvoice(isInvoiceEnabled: Boolean) {

        Log.d("isInvoiceEnabled", isInvoiceEnabled.toString())

        if (isInvoiceEnabled) {
            // add show card
            val invoiceList = dashboardViewModel.invoiceList

            if (invoiceList.isNotEmpty()) {
                binding.homeInvoiceNoDataLayout.visibility = View.GONE
                binding.homeInvoiceCard.visibility = View.VISIBLE
                invoiceListAdapter.setInvoiceList(invoiceList)
                binding.homeInvoiceShowMoreTV.visibility = View.VISIBLE

            } else {
                binding.homeInvoiceCard.visibility = View.GONE
            }
        } else {
            // add hide card
            binding.homeInvoiceCard.visibility = View.GONE
        }
    }

    private fun setVitals(isVitalEnabled: Boolean) {

        Log.d("isVitalsEnabled", isVitalEnabled.toString())
        if (isVitalEnabled) {

            binding.homeVitalsLayout.visibility = View.VISIBLE

            val vitalsList = dashboardViewModel.vitalsList

            val weight = vitalsList.firstOrNull { it.metricName.equals("weight", true) }


            binding.homeVital1Layout.setOnClickListener {
                if (weight != null) {
                    intentToWellnessDetail(ModelUtil.wellnessWeightModelId)
                } else {
                    intentToAddWellness(ModelUtil.wellnessWeightModelId)
                }
            }


            if (weight != null) {
                binding.homeVital1ValueTV.text = weight.trackedValue
                binding.homeVital1UnitTV.text = weight.trackedValueUnit
                binding.homeVital1DateTV.text =
                    weight.dateOfEntry?.toString(ModelUtil.charmDisplayDateFormat())
            } else {
                binding.homeVital1ValueTV.visibility = View.GONE
                UIUtil.removeViewPaddings(binding.homeVital1ValueTV)
                UIUtil.removeViewPaddings(binding.homeVital1UnitTV)
                binding.homeVital1UnitTV.setText(R.string.vital_empty_txt)
                binding.homeVital1DateTV.setText(R.string.vital_empty_txt)
            }

            val height = vitalsList.firstOrNull { it.metricName.equals("height", true) }
            binding.homeVital2Layout.setOnClickListener {
                if (height != null) {
                    intentToWellnessDetail(ModelUtil.wellnessHeightModelId)
                } else {
                    intentToAddWellness(ModelUtil.wellnessHeightModelId)
                }
            }

            if (height != null) {
                binding.homeVital2ValueTV.text = height.trackedValue
                binding.homeVital2UnitTV.text = height.trackedValueUnit
                binding.homeVital2DateTV.text =
                    height.dateOfEntry?.toString(ModelUtil.charmDisplayDateFormat())
            } else {
                binding.homeVital2ValueTV.visibility = View.GONE
                UIUtil.removeViewPaddings(binding.homeVital2ValueTV)
                UIUtil.removeViewPaddings(binding.homeVital2UnitTV)
                binding.homeVital2UnitTV.setText(R.string.vital_empty_txt)
                binding.homeVital2DateTV.setText(R.string.vital_empty_txt)
            }

            val systol = vitalsList.firstOrNull { it.metricName.equals("Systolic", true) }
            val diastol = vitalsList.firstOrNull { it.metricName.equals("Diastolic", true) }

            binding.homeVital3Layout.setOnClickListener {
                if (systol != null || diastol != null) {
                    intentToWellnessDetail(ModelUtil.wellnessBPModelId)
                } else {
                    intentToAddWellness(ModelUtil.wellnessBPModelId)
                }
            }

            if (systol != null || diastol != null) {
                binding.homeVital3ValueTV.text =
                    "${systol?.trackedValue ?: "-"}/${diastol?.trackedValue ?: "-"}"
                binding.homeVital3UnitTV.text =
                    systol?.trackedValueUnit ?: diastol?.trackedValueUnit ?: ""
                binding.homeVital3DateTV.text =
                    systol?.dateOfEntry?.toString(ModelUtil.charmDisplayDateFormat())
                        ?: diastol?.dateOfEntry?.toString(ModelUtil.charmDisplayDateFormat())
            } else {
                binding.homeVital3ValueTV.visibility = View.GONE
                UIUtil.removeViewPaddings(binding.homeVital3ValueTV)
                UIUtil.removeViewPaddings(binding.homeVital3UnitTV)
                binding.homeVital3UnitTV.setText(R.string.vital_empty_txt)
                binding.homeVital3DateTV.setText(R.string.vital_empty_txt)
            }
        } else {
            binding.homeVitalsLayout.visibility = View.GONE
        }

    }

    private fun intentToWellnessDetail(vitalType: String) {
        val dashboardActivity = activity as? DashboardActivity ?: return
        getTrackerList(vitalType) { trackerMetricMap ->
            if (trackerMetricMap != null) {
                val intent = Intent(dashboardActivity, WellnessDetailActivity::class.java)

                var trackerModelId = ModelUtil.wellnessGeneralModelId
                if (vitalType == ModelUtil.wellnessBPModelId) {
                    trackerModelId = vitalType
                } else {
                    intent.putExtra(ModelUtil.wellnessMetricIntent, vitalType)
                }
                intent.putExtra(ModelUtil.wellnessTrackerIntent, trackerModelId)
                intent.putExtra(
                    ModelUtil.wellnessTrackerIdIntent,
                    trackerMetricMap.tracker?.trackerId
                )
                intent.putExtra(ModelUtil.wellnessIsDefaultIntent, true)
                intent.putExtra(ModelUtil.wellnessTrackerDetailIntent, trackerMetricMap.toString)
                dashboardActivity.intentToWellnessWithResult(intent)
                dashboardActivity.overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            }

        }
    }

    private fun intentToAddWellness(vitalType: String) {
        val dashboardActivity = activity as? DashboardActivity ?: return
        getTrackerList(vitalType) { trackerMetricMap ->
            if (trackerMetricMap != null) {
                val intent = Intent(activity, AddWellnessActivity::class.java)
                intent.putExtra(ModelUtil.wellnessTrackerDetailIntent, trackerMetricMap.toString)
                dashboardActivity.intentToWellnessWithResult(intent)
                dashboardActivity.overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            }
        }
    }

    private fun getTrackerList(vitalType: String, success: ((tracker: TrackerMetricMap?) -> Unit)) {
        if (dashboardViewModel.trackerList != null) {
            val tracker = getSelectedTracker(vitalType)
            success(tracker)
        } else {
            val dashboardActivity = activity
            if (dashboardActivity is DashboardActivity) {
                dashboardActivity.progressDialog.show()
            }

            dashboardViewModel.getTrackerList().observe(viewLifecycleOwner, Observer { resp ->
                if (resp != null) {

                    if (dashboardActivity is DashboardActivity) {
                        dashboardActivity.progressDialog.dismiss()
                    }

                    if (resp is Pair<*, *> && resp.first is ArrayList<*>) {
                        dashboardViewModel.trackerList = resp.first as ArrayList<TrackerMetricMap>
                        val tracker = getSelectedTracker(vitalType)
                        success(tracker)
                    } else if (resp is APIResponseError) {

                        val alertDialogBuilder = AlertDialog.Builder(activity)
                        alertDialogBuilder.setMessage(R.string.error_wellness_tracker_failed)
                        alertDialogBuilder.setPositiveButton(R.string.retry_str) { _, _ ->
                            getTrackerList(vitalType, success)
                        }

                        alertDialogBuilder.setNegativeButton(R.string.cancel_str) { _, _ ->
                        }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.setCancelable(false)
                        alertDialog.setCanceledOnTouchOutside(false)
                        alertDialog.show()
                    }

                }
            })
        }
    }

    private fun getSelectedTracker(vitalType: String): TrackerMetricMap? {
        dashboardViewModel.trackerList?.let { trackerList ->

            val tracker = trackerList.firstOrNull {


                if (vitalType == ModelUtil.wellnessBPModelId) {
                    it.tracker?.isDefaultTracker == true && it.tracker?.trackerName == ModelUtil.wellnessBPModelId
                } else {
                    it.tracker?.isDefaultTracker == true && it.tracker?.trackerName == ModelUtil.wellnessGeneralModelId
                }
            }
            if (tracker != null) {
                return tracker
            }
        }

        return null
    }

    private fun setUpcomingAppointment(isAppointmentEnabled: Boolean) {

        Log.d("isAppointmentEnabled", isAppointmentEnabled.toString())

        if (isAppointmentEnabled) {
            val size = dashboardViewModel.upcomingAppointmentList.size

            if (size == 0) {
                binding.homeUpcomingAppointmentCard.visibility = View.VISIBLE
                binding.homeUpcomingAppointmentNoDataLayout.visibility = View.VISIBLE

                if (RbacUtil.checkPrivilege(
                        RbacUtil.Companion.RBACPrivileges.ADD_APPOINTMENT,
                        requireContext()
                    ) && ModelUtil.isPHREntryEnabled(
                        authManager,
                        ModelUtil.appointmentModelId
                    )
                ) {
                    binding.homeUpcomingAppointmentBookBtn.visibility = View.VISIBLE
                } else {
                    binding.homeUpcomingAppointmentBookBtn.visibility = View.GONE
                }

                binding.homeUpcomingAppointmentRV.visibility = View.GONE
                binding.homeTodayAppointmentCard.visibility = View.GONE
            } else {

                val todaysAppointment = arrayListOf<Appointment>()
                val upcomingAppointment = arrayListOf<Appointment>()
                val currentDate = DateTime.now()


                binding.homeUpcomingAppointmentNoDataLayout.visibility = View.GONE
                binding.homeUpcomingAppointmentRV.visibility = View.VISIBLE

                dashboardViewModel.upcomingAppointmentList.forEach({
                    if (it.appointmentDate?.toLocalDate()
                            ?.isBefore(currentDate.toLocalDate()) == true || it.appointmentDate?.toLocalDate()
                            ?.isEqual(currentDate.toLocalDate()) == true
                    ) {
                        todaysAppointment.add(it)
                    } else {
                        upcomingAppointment.add(it)
                    }
                })
                val todayAppointmentSize =
                    todayAppointmentListAdapter.setAppointmentList(todaysAppointment)

                if (todayAppointmentSize == 0) {
                    binding.homeTodayAppointmentCard.visibility = View.GONE
                } else {
                    binding.homeTodayAppointmentCard.visibility = View.VISIBLE
                }

                val upcomingAppointmentSize =
                    upcomingAppointmentMonthListAdapter.setAppointmentList(upcomingAppointment)

                if (upcomingAppointmentSize == 0) {
                    binding.homeUpcomingAppointmentCard.visibility = View.GONE
                } else {
                    binding.homeUpcomingAppointmentCard.visibility = View.VISIBLE
                }
            }
        } else {
            binding.homeTodayAppointmentCard.visibility = View.GONE
            binding.homeUpcomingAppointmentCard.visibility = View.GONE
        }
    }

    private fun setActiveMedicationList(isMedicationEnabled: Boolean) {

        if (isMedicationEnabled) {

            val activeMedicationList = dashboardViewModel.activeMedicationList
            val isMedicationListIsEmpty = activeMedicationList.isEmpty()
            if (isMedicationListIsEmpty) {
                binding.homeActiveMedicationsCard.visibility = View.GONE
                return
            }
            // Group medications by drugDetailsId and take the first medication from each group
            val groupedMedications = activeMedicationList
                .groupBy { it.drugDetail.first().drugDetailsId ?: "" }
                .map { it.value.first() }
            activeMedicationListAdapter.setPatMedPOJOList(groupedMedications)
            binding.homeActiveMedicationsCard.visibility = View.VISIBLE
            binding.homeActiveMedicationsNoDataLayout.visibility = View.GONE
            binding.homeActiveMedicationsRV.visibility = View.VISIBLE
            binding.homeActiveMedicationShowMoreTV.visibility = View.VISIBLE

        } else {
            binding.homeActiveMedicationsCard.visibility = View.GONE
        }
    }

    private fun checkAllDataIsReceived() {

        if (dashboardViewModel.totalRespCount == totalReqCount) {
            binding.homeProgress.root.visibility = View.GONE
            dashboardViewModel.isDashboardProgressStillPresent = false
            if (dashboardViewModel.isActiveMedicationFetch && dashboardViewModel.isUpcomingAppointmentFetch && dashboardViewModel.isVitalsFetch && dashboardViewModel.isInvoiceFetch && dashboardViewModel.isMessageFetch && dashboardViewModel.isQuestionnaireFetch) {
                binding.homeScrollView.visibility = View.VISIBLE
                binding.homeScrollView.post {
                    binding.homeScrollView.smoothScrollTo(0, binding.homeScrollView.top)
                }
            } else {
                binding.homeErrorLayout.root.visibility = View.VISIBLE
            }
            dashboardViewModel.isfetchFeedsCalled = false
        }
    }

    fun blockHomeScrollView() {
        binding.homeScrollView.setScrollingEnable(false)
    }

    fun unblockHomeScrollView() {
        binding.homeScrollView.setScrollingEnable(true)
    }

    private fun showProgress() {
        binding.homeProgress.root.visibility = View.VISIBLE
        binding.homeScrollView.visibility = View.GONE
    }

    private fun startPopulate(sectionConfig: SectionConfigList?, isFromWeb: Boolean) {
        fetchFeeds()
    }

    //region Cancel Appointment
    private fun deleteUpcomingAppointment(
        appointment: Appointment,
        position: Int,
        parentPosition: Int,
        isFromToday: Boolean
    ) {
        val screenWidth = UIUtil.getScreenWidth(requireActivity())
        val width =
            screenWidth - (2 * this.resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))
        val builder = MDCancelAppointmentDialog(requireActivity(), width, picasso)
        builder.appointment = appointment
        builder.setCancelAppointmentListener(object :
            MDCancelAppointmentDialog.CancelAppointmentListener {
            override fun onCancelAppointmentClick(reason: String) {
                deleteAppointment(appointment, position, reason, parentPosition, isFromToday)
            }
        })
        builder.show()
    }

    private fun showCancelAppointmentComposeView(message: String) {
        binding.appointmentCV.visibility = View.VISIBLE
        binding.appointmentCV.setContent {
            var showDialog by remember { mutableStateOf(true) }
            if (showDialog) {
                MPHRTheme {
                    MdMessageActionCustomDialog(
                        messageText = message,
                        primaryButtonText = getString(R.string.contact_practice),
                        secondaryButtonText = getString(R.string.close_str),
                        primaryAction = {
                            showDialog = false
                            binding.appointmentCV.setContent { }
                            binding.appointmentCV.visibility = View.GONE
                            contactPractice()
                        },
                        onDismiss = {
                            showDialog = false
                            binding.appointmentCV.setContent { }
                            binding.appointmentCV.visibility = View.GONE
                        }
                    )
                }
            }
        }
    }

    private fun showContactPracticeMessageDialog(message: String) {
        binding.appointmentCV.visibility = View.VISIBLE
        binding.appointmentCV.setContent {
            var showDialog by remember { mutableStateOf(true) }
            if (showDialog) {
                MPHRTheme {
                    MdCustomMessageDialog(
                        messageText = message,
                        onDismiss = {
                            showDialog = false
                            binding.appointmentCV.setContent { }
                            binding.appointmentCV.visibility = View.GONE
                        }
                    )
                }
            }
        }
    }

    private fun contactPractice() {
        if (((ModelUtil.isModuleEnabled(
                authManager.selectedPatientSection, ModelUtil.messageModuleId
            ) && dashboardViewModel.isComposeAvailable) && (RbacUtil.checkPrivilege(
                RbacUtil.Companion.RBACPrivileges.ADD_MESSAGES, requireActivity().applicationContext
            ))
                    )
        ) {
            intentToContactPractice()
        } else {
            showContactPracticeMessageDialog(getString(R.string.message_restriction_msg))
        }
    }

    private fun intentToContactPractice() {
        val intent = Intent(requireActivity(), ComposeMessageActivity::class.java)
        val filteredList =
            dashboardViewModel.composeMsgRestrictionList.filter { !it.isMessageRestrict && it.allowPHRMessaging }
        intent.putExtra(
            ModelUtil.messageComposeMsgRestrictionListIntent,
            MDJsonBuilder.formComposeMsgRestrictionList(filteredList)
        )
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun deleteAppointment(
        appointment: Appointment,
        position: Int,
        reasonToCancel: String? = null,
        parentPosition: Int,
        isFromToday: Boolean
    ) {
        val dashboardActivity = requireActivity()
        if (dashboardActivity is DashboardActivity) {
            dashboardActivity.progressDialog.show()
        }
        dashboardViewModel.deleteAppointment(appointment, reasonToCancel).observe(this) { resp ->

            if (resp != null) {
                if (dashboardActivity is DashboardActivity) {
                    dashboardActivity.progressDialog.dismiss()
                }
                if (resp is Boolean) {
                    if (isFromToday) {
                        val size = todayAppointmentListAdapter.removeAppointment(position)

                        if (size == 0) {
                            binding.homeTodayAppointmentCard.visibility = View.GONE
                        } else {
                            binding.homeTodayAppointmentCard.visibility = View.VISIBLE
                        }

                    } else {
                        val upcomingAppointmentSize =
                            upcomingAppointmentMonthListAdapter.removeAppointment(
                                parentPosition,
                                position
                            )

                        if (upcomingAppointmentSize == 0) {
                            binding.homeUpcomingAppointmentCard.visibility = View.GONE
                        } else {
                            binding.homeUpcomingAppointmentCard.visibility = View.VISIBLE
                        }
                    }
                    UIUtil.showToast(
                        requireActivity(),
                        R.string.appointment_cancel_success_msg,
                        Toast.LENGTH_LONG
                    )
                } else if (resp is APIResponseError) {
                    UIUtil.showToast(
                        requireActivity(),
                        R.string.error_appointment_cancel_msg,
                        Toast.LENGTH_LONG
                    )
                }
            }
        }
    }
    //endregion

    //region Thalassemia Flowsheet IDs

    private fun fetchThalassemiaFlowsheetIds() {
        if (dashboardViewModel.isThalassemiaFlowsheetsFetched) {
            onFlowsheetIdsResolved()
            return
        }
        dashboardViewModel.getThalassemiaFlowsheetIds().observe(viewLifecycleOwner) { resp ->
            if (resp is Boolean && resp) {
                onFlowsheetIdsResolved()
            }
        }
    }

    private fun navigateToFlowsheet(flowsheets: com.charm.phr.mphr.model.Flowsheets) {
        val dashboardActivity = activity as? DashboardActivity ?: return
        val intent = Intent(
            dashboardActivity,
            com.charm.phr.mphr.ui.flowsheet.entry.FlowsheetDataActivity::class.java
        )
        intent.putExtra(ModelUtil.flowsheetListSelectIntent, flowsheets.toString)
        intent.putExtra(ModelUtil.practiceIdIntent, flowsheets.practiceId)
        dashboardActivity.intentToFlowsheetWithResult(intent)
        dashboardActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun onFlowsheetIdsResolved() {
        val transfusionId = dashboardViewModel.transfusionLogFlowsheetId
        val transfusionPracticeId = dashboardViewModel.transfusionLogPracticeId
        val ferritinId = dashboardViewModel.ferritinMonitoringFlowsheetId
        val ferritinPracticeId = dashboardViewModel.ferritinMonitoringPracticeId
        val mriId = dashboardViewModel.mriT2MonitoringFlowsheetId
        val mriPracticeId = dashboardViewModel.mriT2MonitoringPracticeId

        if (!transfusionId.isNullOrBlank() && !transfusionPracticeId.isNullOrBlank()) {
            fetchTransfusionLogData(transfusionId, transfusionPracticeId)
            binding.homeHbLayout.setOnClickListener {
                dashboardViewModel.transfusionFlowsheet?.let { navigateToFlowsheet(it) }
            }
            binding.homeTransfusionBannerWrapper.setOnClickListener {
                dashboardViewModel.transfusionFlowsheet?.let { navigateToFlowsheet(it) }
            }
        }
        if (!ferritinId.isNullOrBlank() && !ferritinPracticeId.isNullOrBlank()) {
            fetchLastFerritinResult(ferritinId, ferritinPracticeId)
            binding.homeFerritinLayout.setOnClickListener {
                dashboardViewModel.ferritinFlowsheet?.let { navigateToFlowsheet(it) }
            }
        }
        binding.homeFerritinLayout.setOnClickListener {
            dashboardViewModel.ferritinFlowsheet?.let { navigateToFlowsheet(it) }
        }
        if (!mriId.isNullOrBlank() && !mriPracticeId.isNullOrBlank()) {
            fetchLastMriResult(mriId, mriPracticeId)
            binding.homeLastMriWrapper.setOnClickListener {
                dashboardViewModel.mriFlowsheet?.let { navigateToFlowsheet(it) }
            }
        } else {
            renderLastMriResult(MriResultPOJO())
        }
    }

    //endregion

    //region Iron Trend (Ferritin)

    private fun renderIronTrend(labParamHistoryPOJO: LabParamHistoryPOJO) {
        val entries = labParamHistoryPOJO.paramHistoryEntryList
        binding.homeIronTrendChart.fitScreen()
        binding.homeIronTrendChart.clear()
        if (labParamHistoryPOJO.isStringValue == true || entries.isEmpty()) {
            binding.homeIronTrendCard.visibility = View.GONE
        } else {
            binding.homeIronTrendCard.visibility = View.VISIBLE
            binding.homeIronTrendEmptyTV.visibility = View.GONE
            binding.homeIronTrendChart.visibility = View.VISIBLE
            val unit =
                labParamHistoryPOJO.paramUnit?.takeIf { it.isNotBlank() } ?: ModelUtil.FERRITIN_UNIT
            val graphData = UIUtil.buildLabParamGraphData(
                requireContext(),
                entries,
                ModelUtil.FERRITIN_PARAM_NAME,
                unit
            )
            UIUtil.drawLabParamLineChart(requireContext(), binding.homeIronTrendChart, graphData)
        }
    }

    //endregion

    //region Last Lab Results (Ferritin)

    private fun fetchLastFerritinResult(flowsheetId: String, practiceId: String) {
        if (dashboardViewModel.lastFerritinPOJO != null) {
            dashboardViewModel.lastFerritinPOJO?.let { renderFerritinResult(it) }
            dashboardViewModel.ironTrendHistoryPOJO?.let { renderIronTrend(it) }
            return
        }
        dashboardViewModel.getLastFerritinResult(flowsheetId, practiceId)
            .observe(viewLifecycleOwner) { resp ->
                if (resp is LabParamHistoryPOJO) {
                    dashboardViewModel.isLastLabResultsFetched = true
                    dashboardViewModel.lastFerritinPOJO = resp
                    renderFerritinResult(resp)
                    renderIronTrend(resp)
                }
            }
    }

    private fun renderFerritinResult(pojo: LabParamHistoryPOJO) {
        val latest = pojo.paramHistoryEntryList.maxByOrNull { it.date?.millis ?: 0L }
        if (latest?.value != null) {
            binding.homeFerritinValueTV.text = latest.value
            binding.homeFerritinUnitTV.text =
                pojo.paramUnit?.takeIf { it.isNotBlank() } ?: ModelUtil.FERRITIN_UNIT
            binding.homeFerritinDateTV.text =
                latest.date?.toString(ModelUtil.charmDisplayDateFormat()) ?: ""
        }
    }

    //endregion

    //region Last MRI Result

    private fun fetchLastMriResult(flowsheetId: String, practiceId: String) {
        if (dashboardViewModel.isLastMriResultFetched) {
            dashboardViewModel.lastMriResultPOJO?.let { renderLastMriResult(it) }
            return
        }
        dashboardViewModel.getLastMriResult(flowsheetId, practiceId)
            .observe(viewLifecycleOwner) { resp ->
                if (resp is MriResultPOJO) {
                    dashboardViewModel.isLastMriResultFetched = true
                    dashboardViewModel.lastMriResultPOJO = resp
                    renderLastMriResult(resp)
                } else {
                    binding.homeLastMriWrapper.visibility = View.GONE
                }
            }
    }

    private fun renderLastMriResult(mri: MriResultPOJO) {
        val hasData = mri.date != null || mri.lic != null || mri.cardiacT2Star != null
        val dateText = mri.date?.toString(ModelUtil.charmDisplayDateFormat())
            ?: getString(R.string.no_data_available)
        val licValue = mri.lic ?: getString(R.string.no_data_available)
        val licUnit = mri.licUnit?.takeIf { it.isNotBlank() } ?: ModelUtil.MRI_LIC_UNIT
        val t2Value = mri.cardiacT2Star?.let {
            if (it % 1 == 0f) it.toInt().toString() else it.toString()
        } ?: getString(R.string.no_data_available)
        val t2Unit =
            mri.cardiacT2StarUnit?.takeIf { it.isNotBlank() } ?: ModelUtil.MRI_CARDIAC_T2_UNIT
        val alert = when {
            mri.cardiacT2Star == null -> MriCardiacAlert.NONE
            mri.cardiacT2Star!! < ModelUtil.MRI_CARDIAC_T2_CRITICAL_THRESHOLD -> MriCardiacAlert.CRITICAL
            mri.cardiacT2Star!! < ModelUtil.MRI_CARDIAC_T2_WARNING_THRESHOLD -> MriCardiacAlert.WARNING
            else -> MriCardiacAlert.NONE
        }
        binding.homeLastMriCard.setViewCompositionStrategy(
            androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.homeLastMriCard.setContent {
            MPHRTheme {
                LastMriResultCard(
                    hasData = hasData,
                    dateText = dateText,
                    licValue = licValue,
                    licUnit = licUnit,
                    cardiacT2Value = t2Value,
                    cardiacT2Unit = t2Unit,
                    cardiacAlert = alert
                )
            }
        }
    }

    //endregion

    //region Next Transfusion Banner

    private fun fetchTransfusionLogData(flowsheetId: String, practiceId: String) {
        if (dashboardViewModel.isTransfusionLogFetched) {
            dashboardViewModel.transfusionLogData?.let { renderTransfusionLogData(it) }
            return
        }
        dashboardViewModel.getTransfusionLogData(flowsheetId, practiceId)
            .observe(viewLifecycleOwner) { resp ->
                if (resp is TransfusionLogData) {
                    dashboardViewModel.isTransfusionLogFetched = true
                    dashboardViewModel.transfusionLogData = resp
                    renderTransfusionLogData(resp)
                }
            }
    }

    private fun renderTransfusionLogData(data: TransfusionLogData) {
        renderTransfusionBanner(data.nextTransfusionDateMillis)
        binding.homeLabResultsLayout.visibility = View.VISIBLE
        if (!data.hbValue.isNullOrBlank()) {
            binding.homeHbValueTV.text = data.hbValue
            binding.homeHbUnitTV.text =
                data.hbUnit?.trim()?.trim('(', ')')?.trim()?.takeIf { it.isNotBlank() }
                    ?: ModelUtil.HB_UNIT
            binding.homeHbDateTV.text =
                data.entryDate?.toString(ModelUtil.charmDisplayDateFormat()) ?: ""
        }
    }

    fun renderTransfusionBanner(dueDateMillis: Long?) {
        if (dueDateMillis == null) {
            binding.homeTransfusionBannerWrapper.visibility = View.GONE
            return
        }

        val dueDate = org.joda.time.LocalDate(dueDateMillis)
        val today = org.joda.time.LocalDate.now()
        val daysRemaining = org.joda.time.Days.daysBetween(today, dueDate).days

        val dateText = dueDate.toString(ModelUtil.charmDisplayDateFormat())

        binding.homeTransfusionBannerWrapper.visibility = View.VISIBLE
        binding.homeTransfusionBannerCard.setViewCompositionStrategy(
            androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.homeTransfusionBannerCard.setContent {
            MPHRTheme {
                TransfusionBannerCard(
                    dueDateText = dateText,
                    countdown = transfusionCountdown(daysRemaining)
                )
            }
        }
    }

    //endregion


}
