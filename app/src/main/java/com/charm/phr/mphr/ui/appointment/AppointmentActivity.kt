package com.charm.phr.mphr.ui.appointment

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAppointmentBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.appointment.book_appointment.BookAppointmentActivity
import com.charm.phr.mphr.ui.components.MDCancelAppointmentDialog
import com.charm.phr.mphr.ui.components.compose.MdCustomMessageDialog
import com.charm.phr.mphr.ui.components.compose.MdMessageActionCustomDialog
import com.charm.phr.mphr.ui.message.compose.ComposeMessageActivity
import com.charm.phr.mphr.ui.questionnaire.QuestionnaireActivity
import com.charm.phr.mphr.ui.videocall.VideoCallActivity
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Picasso
import javax.inject.Inject


/**
 * [AppointmentActivity] shows the list of appointments of the selected patient.
 * Upcoming ,Past , Requested, Denied and Wait list are the types of Appointments shown here
 */
class AppointmentActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var appointmentViewModel: AppointmentViewModel

    @Inject
    lateinit var upcomingAppointmentListAdapter: UpcomingAppointmentListAdapter

    @Inject
    lateinit var pastAppointmentListAdapter: PastAppointmentListAdapter

    @Inject
    lateinit var requestedAppointmentListAdapter: RequestedAppointmentListAdapter

    @Inject
    lateinit var deniedAppointmentListAdapter: DeniedAppointmentListAdapter

    @Inject
    lateinit var waitListAppointmentListAdapter: WaitListAppointmentListAdapter

    private lateinit var activityAppointmentBinding: ActivityAppointmentBinding

    @Inject
    lateinit var picasso: Picasso

    private val TAG = "AppointmentActivity"
//    private var isPastAppointmentOpened = false

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                setResult(Activity.RESULT_OK, data)
                appointmentViewModel.isAppointmentSuccessCallbackToBeSent = true
                val respAppointmentCode = data.getIntExtra(ModelUtil.appointmentCode, ModelUtil.appointmentUpcoming)

                if (respAppointmentCode == ModelUtil.appointmentUpcoming) {
                    appointmentViewModel.isUpcomingAppointmentFetched = null
                    appointmentViewModel.isAppointmentFetched = false

                    if (appointmentViewModel.currentTabSelected == ModelUtil.appointmentUpcoming) {
                        showAppointment(true)
                    }
                } else if (respAppointmentCode == ModelUtil.appointmentRequested) {
                    appointmentViewModel.isRequestedAppointmentFetched = null

                    if (appointmentViewModel.currentTabSelected == ModelUtil.appointmentRequested) {
                        showRequestedAppointment(true)
                    }
                } else {
                    appointmentViewModel.isWaitListAppointmentFetched = null

                    if (appointmentViewModel.currentTabSelected == ModelUtil.appointmentWaitingList) {
                        showWaitingListAppointment(true)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityAppointmentBinding = ActivityAppointmentBinding.inflate(layoutInflater)
        setContentView(activityAppointmentBinding.root)

        appointmentViewModel = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(activityAppointmentBinding.appointmentToolbar)
        UIUtil.applyViewInsertForBottomView(activityAppointmentBinding.appointmentMainView)

        activityAppointmentBinding.appointmentListUpcomingAppointmentRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        activityAppointmentBinding.appointmentListUpcomingAppointmentRV.adapter = upcomingAppointmentListAdapter
        // appointmentListUpcomingAppointmentRV.addItemDecoration(SimpleDividerItemDecoration(this))

        activityAppointmentBinding.appointmentListPastAppointmentRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        activityAppointmentBinding.appointmentListPastAppointmentRV.adapter = pastAppointmentListAdapter
        // appointmentListPastAppointmentRV.addItemDecoration(SimpleDividerItemDecoration(this))

        activityAppointmentBinding.appointmentListDeniedAppointmentRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        activityAppointmentBinding.appointmentListDeniedAppointmentRV.adapter = deniedAppointmentListAdapter
        // appointmentListDeniedAppointmentRV.addItemDecoration(SimpleDividerItemDecoration(this))

        activityAppointmentBinding.appointmentListRequestedAppointmentRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        activityAppointmentBinding.appointmentListRequestedAppointmentRV.adapter = requestedAppointmentListAdapter
        // appointmentListRequestedAppointmentRV.addItemDecoration(SimpleDividerItemDecoration(this))

        activityAppointmentBinding.appointmentListWaitingListAppointmentRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        activityAppointmentBinding.appointmentListWaitingListAppointmentRV.adapter = waitListAppointmentListAdapter
        // appointmentListWaitingListAppointmentRV.addItemDecoration(SimpleDividerItemDecoration(this))

        UIUtil.makePlusColorPrimary(activityAppointmentBinding.appointmentListAppointmentNoDataInfoTV)

        val composeMsgRestrictionList =
            MDParsingHelper.formComposeMsgRestrictionList(intent.getStringExtra(ModelUtil.messageComposeMsgRestrictionListIntent) ?: "")
        appointmentViewModel.composeRestrictedList = composeMsgRestrictionList
        appointmentViewModel.isComposeAvailable = composeMsgRestrictionList.any { !it.isMessageRestrict && it.allowPHRMessaging }


        if (BuildUtil.isCustomApp) {
            activityAppointmentBinding.appointmentBgLayout.visibility = View.GONE
        }

        showAppointment(true)

        activityAppointmentBinding.appointmentListAppointmentTabBtn.setOnClickListener {
            showAppointment()
        }

        activityAppointmentBinding.appointmentListRequestedTabBtn.setOnClickListener {
            showRequestedAppointment()
        }

        activityAppointmentBinding.appointmentListDeniedTabBtn.setOnClickListener {
            showDeniedAppointment()
        }

        activityAppointmentBinding.appointmentErrorPage.errorPageTryAgainBtn.layoutParams?.height =
            resources.getDimensionPixelSize(R.dimen.appointment_error_button_height)
        activityAppointmentBinding.appointmentErrorPage.errorPageTryAgainBtn.requestLayout()

        /*
                appointmentListDeniedNoDataImg.setElevationDp(100f)
        */

        activityAppointmentBinding.appointmentListWaitingListTabBtn.setOnClickListener {
            showWaitingListAppointment()
        }
        if (!RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_APPOINTMENT, applicationContext)) {
            activityAppointmentBinding.appointmentBookFab.hide()
        }
        activityAppointmentBinding.appointmentBookFab.setOnClickListener {
            val intent = Intent(this, BookAppointmentActivity::class.java)
            resultLauncher.launch(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        }

        activityAppointmentBinding.appointmentBackBtn.setOnClickListener {
            onBackPressed()
        }

//        pastAppointmentVisibilitySwitch.setOnClickListener {
//            animatePastAppointmentList()
//        }

//        pastAppointmentHeadingTV.setOnClickListener {
//            animatePastAppointmentList()
//        }

        activityAppointmentBinding.appointmentErrorPage.errorPageTryAgainBtn.setOnClickListener {

            activityAppointmentBinding.appointmentErrorPage.root.visibility = View.GONE

            val respAppointmentCode = appointmentViewModel.currentTabSelected

            if (respAppointmentCode == ModelUtil.appointmentUpcoming) {
                appointmentViewModel.isUpcomingAppointmentFetched = null
                appointmentViewModel.isPastAppointmentFetched = null
                appointmentViewModel.isAppointmentFetched = false

                if (appointmentViewModel.currentTabSelected == ModelUtil.appointmentUpcoming) {
                    showAppointment(true)
                }
            } else if (respAppointmentCode == ModelUtil.appointmentRequested) {
                appointmentViewModel.isRequestedAppointmentFetched = null

                if (appointmentViewModel.currentTabSelected == ModelUtil.appointmentRequested) {
                    showRequestedAppointment(true)
                }
            } else if (respAppointmentCode == ModelUtil.appointmentDenied) {
                appointmentViewModel.isDeniedAppointmentFetched = null
                if (appointmentViewModel.currentTabSelected == ModelUtil.appointmentDenied) {
                    showDeniedAppointment(true)
                }
            } else {
                appointmentViewModel.isWaitListAppointmentFetched = null
                if (appointmentViewModel.currentTabSelected == ModelUtil.appointmentWaitingList) {
                    showWaitingListAppointment(true)
                }
            }
        }

        setOnScrollChangeListener()

        upcomingAppointmentListAdapter.setVideoCallBtnClickListener(object : UpcomingAppointmentListAdapter.OnClickListener {
            override fun onVideoCallBtnClickListener(appointment: Appointment) {

                if (!ModelUtil.isVideoCallInProgress()) {
                    val intent = Intent(this@AppointmentActivity, VideoCallActivity::class.java)
                    intent.putExtra(ModelUtil.appointmentIdIntent, appointment.appointmentId)
                    intent.putExtra(ModelUtil.practiceIdIntent, appointment.practiceId)
                    intent.putExtra(ModelUtil.appointmentTimeIntent, appointment.appointmentDate?.toString(ModelUtil.charmCompleteDateYearFormatter()))
                    intent.putExtra(ModelUtil.memberNameIntent, UIUtil.formMemberName(appointment.member))
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    UIUtil.showToast(this@AppointmentActivity, R.string.video_call_already_in_meeting_str, Toast.LENGTH_LONG)
                }
            }

            override fun onQuestionnaireClickListener(appointment: Appointment) {

                val intent = Intent(this@AppointmentActivity, QuestionnaireActivity::class.java)
                intent.putExtra(ModelUtil.appointmentIdIntent, appointment.appointmentId)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onCardClick() {
                //
            }

            override fun onDelete(appointment: Appointment, position: Int, parentPosition: Int, isFromToday: Boolean) {
                if (appointment.appointmentCancelOption == null) {
                    showCancelAppointmentComposeView(getString(R.string.cancel_appointment_restriction_msg))
                } else if (appointment.appointmentCancelOption?.isEmpty() == true) {
                    deleteUpcomingAppointment(appointment, position)
                } else {
                    showCancelAppointmentComposeView(getString(R.string.appointment_cancel_error_msg, appointment.appointmentCancelOption))
                }
            }
        })

        requestedAppointmentListAdapter.setAppointmentDeleteListener(object : AppointmentDeleteListener {
            override fun onDelete(appointment: Appointment, position: Int) {
                showConfirmDeleteAppointment(appointment, position)
            }
        })

        deniedAppointmentListAdapter.setAppointmentDeleteListener(object : AppointmentDeleteListener {
            override fun onDelete(appointment: Appointment, position: Int) {
                showConfirmDeleteAppointment(appointment, position)
            }
        })

        waitListAppointmentListAdapter.setAppointmentDeleteListener(object : AppointmentDeleteListener {
            override fun onDelete(appointment: Appointment, position: Int) {
                showConfirmDeleteAppointment(appointment, position)
            }
        })

        if (!BuildUtil.isCustomApp) {
            if (RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_APPOINTMENT, applicationContext) && ModelUtil.isPHREntryEnabled(
                    authManager,
                    ModelUtil.appointmentModelId
                )
            ) {
                activityAppointmentBinding.appointmentBookFab.visibility = View.VISIBLE
            } else {
                activityAppointmentBinding.appointmentBookFab.visibility = View.GONE
            }
        }
    }

    private fun showCancelAppointmentComposeView(message: String) {
        activityAppointmentBinding.appointmentComposeview.visibility = View.VISIBLE
        activityAppointmentBinding.appointmentComposeview.setContent {
            var showDialog by remember { mutableStateOf(true) }
            if (showDialog) {
                MPHRTheme {
                    MdMessageActionCustomDialog(
                        messageText = message,
                        primaryButtonText = getString(R.string.contact_practice),
                        secondaryButtonText = getString(R.string.close_str),
                        primaryAction = {
                            showDialog = false
                            activityAppointmentBinding.appointmentComposeview.setContent { }
                            activityAppointmentBinding.appointmentComposeview.visibility = View.GONE
                            contactPractice()
                        },
                        onDismiss = {
                            showDialog = false
                            activityAppointmentBinding.appointmentComposeview.setContent { }
                            activityAppointmentBinding.appointmentComposeview.visibility = View.GONE
                        }
                    )
                }
            }
        }
    }

    private fun showContactPracticeMessageDialog(message: String) {
        activityAppointmentBinding.appointmentComposeview.visibility = View.VISIBLE
        activityAppointmentBinding.appointmentComposeview.setContent {
            var showDialog by remember { mutableStateOf(true) }
            if (showDialog) {
                MPHRTheme {
                    MdCustomMessageDialog(
                        messageText = message,
                        onDismiss = {
                            showDialog = false
                            activityAppointmentBinding.appointmentComposeview.setContent { }
                            activityAppointmentBinding.appointmentComposeview.visibility = View.GONE
                        }
                    )
                }
            }
        }
    }

    private fun contactPractice() {
        if (((ModelUtil.isModuleEnabled(
                authManager.selectedPatientSection, ModelUtil.messageModuleId
            ) && appointmentViewModel.isComposeAvailable) && (RbacUtil.checkPrivilege(
                RbacUtil.Companion.RBACPrivileges.ADD_MESSAGES, applicationContext
            ))
                    )
        ) {
            intentToContactPractice()
        } else {
            showContactPracticeMessageDialog(getString(R.string.message_restriction_msg))
        }
    }

    private fun intentToContactPractice() {
        val intent = Intent(this, ComposeMessageActivity::class.java)
        val filteredList = appointmentViewModel.composeRestrictedList.filter { !it.isMessageRestrict && it.allowPHRMessaging }
        intent.putExtra(ModelUtil.messageComposeMsgRestrictionListIntent, MDJsonBuilder.formComposeMsgRestrictionList(filteredList))
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun setOnScrollChangeListener() {
        activityAppointmentBinding.appointmentListUpcomingAppointmentRV.setOnScrollChangeListener { _: View?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            onScrollListener(scrollY, oldScrollY)
        }

        activityAppointmentBinding.appointmentListPastAppointmentRV.setOnScrollChangeListener { _: View?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            onScrollListener(scrollY, oldScrollY)
        }
        activityAppointmentBinding.appointmentListRequestedAppointmentRV.setOnScrollChangeListener { _: View?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            onScrollListener(scrollY, oldScrollY)
        }
        activityAppointmentBinding.appointmentListWaitingListAppointmentRV.setOnScrollChangeListener { _: View?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            onScrollListener(scrollY, oldScrollY)
        }
        activityAppointmentBinding.appointmentListDeniedAppointmentRV.setOnScrollChangeListener { _: View?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            onScrollListener(scrollY, oldScrollY)
        }
    }

    private fun onScrollListener(scrollY: Int, oldScrollY: Int) {
        try {

            if (scrollY > oldScrollY && activityAppointmentBinding.appointmentBookFab.visibility == View.VISIBLE) {
                activityAppointmentBinding.appointmentBookFab.hide()
            } else if (RbacUtil.checkPrivilege(
                    RbacUtil.Companion.RBACPrivileges.ADD_APPOINTMENT,
                    applicationContext
                ) && scrollY < oldScrollY && activityAppointmentBinding.appointmentBookFab.visibility != View.VISIBLE
            ) {
                activityAppointmentBinding.appointmentBookFab.show()
            }
            /*
                            handler.removeCallbacks(runnable)
                            handler.postDelayed( runnable , 100)*/
        } catch (e: Exception) {

        }
    }


    /**
     * Method to show Upcoming and requested appointments of the selected patients
     * Other type of appointments are hidden
     **/
    private fun showAppointment(isFirst: Boolean = false) {
        activityAppointmentBinding.appointmentListRequestedTab.visibility = View.GONE
        activityAppointmentBinding.appointmentListDeniedTab.visibility = View.GONE
        activityAppointmentBinding.appointmentListWaitingListTab.visibility = View.GONE


        focusOnView(activityAppointmentBinding.appointmentTabLayout, activityAppointmentBinding.appointmentListAppointmentTabBtn)
        Log.d(TAG, "${activityAppointmentBinding.appointmentListAppointmentTabBtn.left}")
        activityAppointmentBinding.appointmentListAppointmentTabBtn.setChipBackgroundColorResource(R.color.appointmentSelectedTab)
        activityAppointmentBinding.appointmentListAppointmentTabBtn.setTextColor(ContextCompat.getColor(this, R.color.appointmentSelectedTabTxtColor))
        activityAppointmentBinding.appointmentListAppointmentTabBtn.typeface = UIUtil.getFont(this, R.font.open_sans_medium)
        val isCurrentTabSelected = changeTabSelected(ModelUtil.appointmentUpcoming)

        if (!isCurrentTabSelected || isFirst) {
            if (!appointmentViewModel.isAppointmentFetched) {
                activityAppointmentBinding.appointmentListAppointmentTab.visibility = View.GONE
                activityAppointmentBinding.appointmentProgress.root.visibility = View.VISIBLE
                if (appointmentViewModel.isUpcomingAppointmentFetched == null) {
                    appointmentViewModel.fetchUpcomingAppointment().observe(this) { resp ->
                        if (resp != null) {
                            if (resp is ArrayList<*>) {
                                val appointmentList = resp as ArrayList<Appointment>

                                upcomingAppointmentListAdapter.setAppointments(appointmentList)

                                if (appointmentList.isEmpty()) {
                                    activityAppointmentBinding.appointmentListAppointmentNoDataLayout.visibility = View.VISIBLE
                                    activityAppointmentBinding.appointmentListUpcomingAppointmentRV.visibility = View.GONE
                                    // appointmentListUpcomingAppointmentHeadingTV.visibility = View.GONE

                                } else {
                                    activityAppointmentBinding.appointmentListAppointmentNoDataLayout.visibility = View.GONE
                                    activityAppointmentBinding.appointmentListUpcomingAppointmentRV.visibility = View.VISIBLE
                                    // appointmentListUpcomingAppointmentHeadingTV.visibility = View.VISIBLE


                                }

                                appointmentViewModel.isUpcomingAppointmentFetched = true
                            } else if (resp is APIResponseError) {
                                appointmentViewModel.isUpcomingAppointmentFetched = false
                            }

                            if (appointmentViewModel.isUpcomingAppointmentFetched != null && appointmentViewModel.isPastAppointmentFetched != null) {
                                activityAppointmentBinding.appointmentListAppointmentTab.visibility = View.VISIBLE
                                appointmentViewModel.isAppointmentFetched = true
                                if (appointmentViewModel.isPastAppointmentFetched == false || appointmentViewModel.isUpcomingAppointmentFetched == false) {
                                    /*appointmentListPastAppointmentLayout.visibility = View.GONE

                                    appointmentProgress.visibility = View.GONE*/
                                    activityAppointmentBinding.appointmentListAppointmentTab.visibility = View.GONE
                                    activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                                    activityAppointmentBinding.appointmentErrorPage.root.visibility = View.VISIBLE
                                } else {
//                                 if (appointmentListPastAppointmentLayout.visibility == View.VISIBLE){
                                    activityAppointmentBinding.appointmentListPastAppointmentRV.viewTreeObserver.addOnGlobalLayoutListener(object :
                                        ViewTreeObserver.OnGlobalLayoutListener {

                                        override fun onGlobalLayout() {
                                            // Ensure you call it only once :
                                            activityAppointmentBinding.appointmentListPastAppointmentRV.viewTreeObserver.removeOnGlobalLayoutListener(
                                                this
                                            )
                                            val appointmentRVHeight = activityAppointmentBinding.appointmentListUpcomingAppointmentRV.height
                                            val height = UIUtil.getScreenHeight(this@AppointmentActivity)

                                            val appointmentViewMaxHeight = height * 40 / 100

                                            if (appointmentRVHeight > appointmentViewMaxHeight) {
                                                val params = LinearLayout.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    appointmentViewMaxHeight
                                                )
                                                activityAppointmentBinding.appointmentListUpcomingAppointmentRV.layoutParams = params
                                            }
                                        }
                                    })


                                    handler.postDelayed({
                                        activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE

                                    }, 50)
                                }

                                if (appointmentViewModel.isUpcomingAppointmentFetched == false || appointmentViewModel.isPastAppointmentFetched == false) {
                                    /*appointmentListAppointmentNoDataLayout.visibility = View.VISIBLE
                                    appointmentListUpcomingAppointmentRV.visibility = View.GONE*/

                                    activityAppointmentBinding.appointmentListAppointmentTab.visibility = View.GONE
                                    activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                                    activityAppointmentBinding.appointmentErrorPage.root.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }

                if (appointmentViewModel.isPastAppointmentFetched == null) {
                    appointmentViewModel.fetchPastAppointment().observe(this) { resp ->
                        if (resp != null) {
                            if (resp is ArrayList<*>) {
                                val appointmentList = resp as ArrayList<Appointment>

                                pastAppointmentListAdapter.addAppointments(appointmentList)

//                                if (appointmentList.isEmpty()) {
//                                    appointmentListPastAppointmentLayout.visibility = View.GONE
//                                } else {
//                                    appointmentListPastAppointmentLayout.visibility = View.VISIBLE
//                                }

                                appointmentViewModel.isPastAppointmentFetched = true
                            } else if (resp is APIResponseError) {
                                appointmentViewModel.isPastAppointmentFetched = false
                            }

                            if (appointmentViewModel.isUpcomingAppointmentFetched != null && appointmentViewModel.isPastAppointmentFetched != null) {
                                activityAppointmentBinding.appointmentListAppointmentTab.visibility = View.VISIBLE
                                appointmentViewModel.isAppointmentFetched = true
                                if (appointmentViewModel.isPastAppointmentFetched == false || appointmentViewModel.isUpcomingAppointmentFetched == false) {
                                    /* appointmentListPastAppointmentLayout.visibility = View.GONE
                                     appointmentProgress.visibility = View.GONE*/
                                    activityAppointmentBinding.appointmentListAppointmentTab.visibility = View.GONE
                                    activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                                    activityAppointmentBinding.appointmentErrorPage.root.visibility = View.VISIBLE
                                } else {
//                                    if (appointmentListPastAppointmentLayout.visibility == View.VISIBLE){
                                    activityAppointmentBinding.appointmentListPastAppointmentRV.viewTreeObserver.addOnGlobalLayoutListener(object :
                                        ViewTreeObserver.OnGlobalLayoutListener {

                                        override fun onGlobalLayout() {
                                            // Ensure you call it only once :
                                            activityAppointmentBinding.appointmentListPastAppointmentRV.viewTreeObserver.removeOnGlobalLayoutListener(
                                                this
                                            )
                                            val appointmentRVHeight = activityAppointmentBinding.appointmentListUpcomingAppointmentRV.height
                                            val height = UIUtil.getScreenHeight(this@AppointmentActivity)

                                            val appointmentViewMaxHeight = height * 40 / 100

                                            if (appointmentRVHeight > appointmentViewMaxHeight) {
                                                val params = LinearLayout.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    appointmentViewMaxHeight
                                                )
                                                activityAppointmentBinding.appointmentListUpcomingAppointmentRV.layoutParams = params
                                            }

                                            handler.postDelayed({
                                                activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE

                                            }, 50)
                                        }
                                    })
                                }

                                if (appointmentViewModel.isUpcomingAppointmentFetched == false || appointmentViewModel.isPastAppointmentFetched == false) {
                                    /*appointmentListAppointmentNoDataLayout.visibility = View.VISIBLE
                                    appointmentListUpcomingAppointmentRV.visibility = View.GONE*/

                                    activityAppointmentBinding.appointmentListAppointmentTab.visibility = View.GONE
                                    activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                                    activityAppointmentBinding.appointmentErrorPage.root.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }
            } else {
                activityAppointmentBinding.appointmentListAppointmentTab.visibility = View.VISIBLE
                upcomingAppointmentListAdapter.notifyDataSetChanged()
                activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
            }
        }
    }

    /**
     * Method to show requested appointments of the selected patients
     * Other type of appointments are hidden
     **/
    private fun showRequestedAppointment(isFirst: Boolean = false) {
        activityAppointmentBinding.appointmentListAppointmentTab.visibility = View.GONE
        activityAppointmentBinding.appointmentListDeniedTab.visibility = View.GONE
        activityAppointmentBinding.appointmentListWaitingListTab.visibility = View.GONE

        Log.d(TAG, "${activityAppointmentBinding.appointmentListRequestedTabBtn.left}")

        focusOnView(activityAppointmentBinding.appointmentTabLayout, activityAppointmentBinding.appointmentListRequestedTabBtn)

        activityAppointmentBinding.appointmentListRequestedTabBtn.setChipBackgroundColorResource(R.color.appointmentSelectedTab)
        activityAppointmentBinding.appointmentListRequestedTabBtn.setTextColor(ContextCompat.getColor(this, R.color.appointmentSelectedTabTxtColor))
        activityAppointmentBinding.appointmentListRequestedTabBtn.typeface = UIUtil.getFont(this, R.font.open_sans_medium)

        val isCurrentTabSelected = changeTabSelected(ModelUtil.appointmentRequested)

        if (!isCurrentTabSelected || isFirst) {
            if (appointmentViewModel.isRequestedAppointmentFetched == null) {
                activityAppointmentBinding.appointmentListRequestedTab.visibility = View.GONE
                activityAppointmentBinding.appointmentProgress.root.visibility = View.VISIBLE
                appointmentViewModel.fetchRequestedAppointment().observe(this) { resp ->
                    if (resp != null) {
                        activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                        if (appointmentViewModel.currentTabSelected == ModelUtil.appointmentRequested) {
                            activityAppointmentBinding.appointmentListRequestedTab.visibility = View.VISIBLE
                        } else {
                            activityAppointmentBinding.appointmentListRequestedTab.visibility = View.GONE
                        }

                        if (resp is ArrayList<*>) {
                            val appointmentList = resp as ArrayList<Appointment>
                            requestedAppointmentListAdapter.addAppointments(appointmentList)

                            if (appointmentList.isEmpty()) {
                                activityAppointmentBinding.appointmentListRequestedNoDataLayout.visibility = View.VISIBLE
                                activityAppointmentBinding.appointmentListRequestedAppointmentRV.visibility = View.GONE
                                // appointmentListRequestedAppointmentHeadingTV.visibility = View.GONE

                            } else {
                                activityAppointmentBinding.appointmentListRequestedNoDataLayout.visibility = View.GONE
                                activityAppointmentBinding.appointmentListRequestedAppointmentRV.visibility = View.VISIBLE
                                //appointmentListRequestedAppointmentHeadingTV.visibility = View.VISIBLE

                            }




                            appointmentViewModel.isRequestedAppointmentFetched = true
                        } else if (resp is APIResponseError) {
                            appointmentViewModel.isRequestedAppointmentFetched = false
                            activityAppointmentBinding.appointmentListRequestedTab.visibility = View.GONE
                            activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                            activityAppointmentBinding.appointmentErrorPage.root.visibility = View.VISIBLE
                        }
                    }
                }
            } else {
                if (appointmentViewModel.isRequestedAppointmentFetched == true) {
                    activityAppointmentBinding.appointmentListRequestedTab.visibility = View.VISIBLE
                    activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                } else {
                    activityAppointmentBinding.appointmentListRequestedTab.visibility = View.GONE
                    activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                    activityAppointmentBinding.appointmentErrorPage.root.visibility = View.VISIBLE
                }
            }
        }

    }

    /**
     * Method to show denied appointments of the selected patients
     * Other type of appointments are hidden
     **/
    private fun showDeniedAppointment(isFirst: Boolean = false) {
        activityAppointmentBinding.appointmentListAppointmentTab.visibility = View.GONE
        activityAppointmentBinding.appointmentListRequestedTab.visibility = View.GONE
        activityAppointmentBinding.appointmentListWaitingListTab.visibility = View.GONE


        focusOnView(activityAppointmentBinding.appointmentTabLayout, activityAppointmentBinding.appointmentListDeniedTabBtn)

        Log.d(TAG, "${activityAppointmentBinding.appointmentListDeniedTabBtn.left}")

        activityAppointmentBinding.appointmentListDeniedTabBtn.setChipBackgroundColorResource(R.color.appointmentSelectedTab)
        activityAppointmentBinding.appointmentListDeniedTabBtn.setTextColor(ContextCompat.getColor(this, R.color.appointmentSelectedTabTxtColor))
        activityAppointmentBinding.appointmentListDeniedTabBtn.typeface = UIUtil.getFont(this, R.font.open_sans_medium)

        val isCurrentTabSelected = changeTabSelected(ModelUtil.appointmentDenied)

        if (!isCurrentTabSelected || isFirst) {
            if (appointmentViewModel.isDeniedAppointmentFetched == null) {
                activityAppointmentBinding.appointmentListDeniedTab.visibility = View.GONE
                activityAppointmentBinding.appointmentProgress.root.visibility = View.VISIBLE
                appointmentViewModel.fetchDeniedAppointment().observe(this) { resp ->
                    if (resp != null) {
                        activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                        if (appointmentViewModel.currentTabSelected == ModelUtil.appointmentDenied) {
                            activityAppointmentBinding.appointmentListDeniedTab.visibility = View.VISIBLE
                        } else {
                            activityAppointmentBinding.appointmentListDeniedTab.visibility = View.GONE
                        }
                        if (resp is ArrayList<*>) {
                            val appointmentList = resp as ArrayList<Appointment>
                            deniedAppointmentListAdapter.addAppointments(appointmentList)

                            if (appointmentList.isEmpty()) {
                                activityAppointmentBinding.appointmentListDeniedNoDataLayout.visibility = View.VISIBLE
                                activityAppointmentBinding.appointmentListDeniedAppointmentRV.visibility = View.GONE
                                //  appointmentListDeniedAppointmentHeadingTV.visibility = View.GONE

                            } else {
                                activityAppointmentBinding.appointmentListDeniedNoDataLayout.visibility = View.GONE
                                activityAppointmentBinding.appointmentListDeniedAppointmentRV.visibility = View.VISIBLE
                                // appointmentListDeniedAppointmentHeadingTV.visibility = View.VISIBLE
                            }

                            appointmentViewModel.isDeniedAppointmentFetched = true
                        } else if (resp is APIResponseError) {
                            appointmentViewModel.isDeniedAppointmentFetched = false

                            activityAppointmentBinding.appointmentListDeniedTab.visibility = View.GONE
                            activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                            activityAppointmentBinding.appointmentErrorPage.root.visibility = View.VISIBLE
                        }
                    }
                }
            } else {

                if (appointmentViewModel.isDeniedAppointmentFetched == true) {
                    activityAppointmentBinding.appointmentListDeniedTab.visibility = View.VISIBLE
                    activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                } else {
                    activityAppointmentBinding.appointmentListDeniedTab.visibility = View.GONE
                    activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                    activityAppointmentBinding.appointmentErrorPage.root.visibility = View.VISIBLE
                }
            }
        }

    }

    /**
     * Method to show waiting lists of the selected patients
     * Other type of appointments are hidden
     **/
    private fun showWaitingListAppointment(isFirst: Boolean = false) {
        activityAppointmentBinding.appointmentListAppointmentTab.visibility = View.GONE
        activityAppointmentBinding.appointmentListDeniedTab.visibility = View.GONE
        activityAppointmentBinding.appointmentListRequestedTab.visibility = View.GONE


        focusOnView(activityAppointmentBinding.appointmentTabLayout, activityAppointmentBinding.appointmentListWaitingListTabBtn)

        Log.d(TAG, "${activityAppointmentBinding.appointmentListWaitingListTabBtn.left}")

        activityAppointmentBinding.appointmentListWaitingListTabBtn.setChipBackgroundColorResource(R.color.appointmentSelectedTab)
        activityAppointmentBinding.appointmentListWaitingListTabBtn.setTextColor(ContextCompat.getColor(this, R.color.appointmentSelectedTabTxtColor))
        activityAppointmentBinding.appointmentListWaitingListTabBtn.typeface = UIUtil.getFont(this, R.font.open_sans_medium)

        val isCurrentTabSelected = changeTabSelected(ModelUtil.appointmentWaitingList)

        if (!isCurrentTabSelected || isFirst) {
            if (appointmentViewModel.isWaitListAppointmentFetched == null) {
                activityAppointmentBinding.appointmentListWaitingListTab.visibility = View.GONE
                activityAppointmentBinding.appointmentProgress.root.visibility = View.VISIBLE
                appointmentViewModel.fetchWaitingListAppointment().observe(this) { resp ->
                    if (resp != null) {
                        activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                        if (appointmentViewModel.currentTabSelected == ModelUtil.appointmentWaitingList) {
                            activityAppointmentBinding.appointmentListWaitingListTab.visibility = View.VISIBLE
                        } else {
                            activityAppointmentBinding.appointmentListWaitingListTab.visibility = View.GONE
                        }
                        if (resp is ArrayList<*>) {
                            val appointmentList = resp as ArrayList<Appointment>
                            waitListAppointmentListAdapter.addAppointments(appointmentList)

                            if (appointmentList.isEmpty()) {
                                activityAppointmentBinding.appointmentListWaitingListNoDataLayout.visibility = View.VISIBLE
                                activityAppointmentBinding.appointmentListWaitingListAppointmentRV.visibility = View.GONE
                                activityAppointmentBinding.appointmentListWaitingListAppointmentHeadingTV.visibility = View.GONE
                            } else {
                                activityAppointmentBinding.appointmentListWaitingListNoDataLayout.visibility = View.GONE
                                activityAppointmentBinding.appointmentListWaitingListAppointmentRV.visibility = View.VISIBLE
                                activityAppointmentBinding.appointmentListWaitingListAppointmentHeadingTV.visibility = View.VISIBLE
                            }

                            appointmentViewModel.isWaitListAppointmentFetched = true
                        } else if (resp is APIResponseError) {
                            appointmentViewModel.isWaitListAppointmentFetched = false
                            activityAppointmentBinding.appointmentListWaitingListTab.visibility = View.GONE
                            activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                            activityAppointmentBinding.appointmentErrorPage.root.visibility = View.VISIBLE
                        }
                    }
                }
            } else {
                if (appointmentViewModel.isWaitListAppointmentFetched == true) {
                    activityAppointmentBinding.appointmentListWaitingListTab.visibility = View.VISIBLE
                    activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                } else {
                    activityAppointmentBinding.appointmentListWaitingListTab.visibility = View.GONE
                    activityAppointmentBinding.appointmentProgress.root.visibility = View.GONE
                    activityAppointmentBinding.appointmentErrorPage.root.visibility = View.VISIBLE
                }
            }
        }

    }


    /**
     * Method to toggle the selected tabs of appointment
     **/
    private fun changeTabSelected(selectedTab: Int): Boolean {

        val isCurrentTabSelected = appointmentViewModel.currentTabSelected == selectedTab
        if (appointmentViewModel.currentTabSelected != selectedTab) {

            val typefaceNormal = UIUtil.getFont(this, R.font.open_sans)

            when (appointmentViewModel.currentTabSelected) {
                ModelUtil.appointmentRequested -> {
                    activityAppointmentBinding.appointmentListRequestedTabBtn.setChipBackgroundColorResource(R.color.appointmentUnSelectedTab)
                    activityAppointmentBinding.appointmentListRequestedTabBtn.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.appointmentUnSelectedTabTxtColor
                        )
                    )
                    activityAppointmentBinding.appointmentListRequestedTabBtn.typeface = typefaceNormal

                }

                ModelUtil.appointmentDenied -> {
                    activityAppointmentBinding.appointmentListDeniedTabBtn.setChipBackgroundColorResource(R.color.appointmentUnSelectedTab)
                    activityAppointmentBinding.appointmentListDeniedTabBtn.setTextColor(ContextCompat.getColor(this, R.color.appointmentUnSelectedTabTxtColor))
                    activityAppointmentBinding.appointmentListDeniedTabBtn.typeface = typefaceNormal

                }

                ModelUtil.appointmentWaitingList -> {
                    activityAppointmentBinding.appointmentListWaitingListTabBtn.setChipBackgroundColorResource(R.color.appointmentUnSelectedTab)
                    activityAppointmentBinding.appointmentListWaitingListTabBtn.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.appointmentUnSelectedTabTxtColor
                        )
                    )
                    activityAppointmentBinding.appointmentListWaitingListTabBtn.typeface = typefaceNormal


                }

                else -> {
                    activityAppointmentBinding.appointmentListAppointmentTabBtn.setChipBackgroundColorResource(R.color.appointmentUnSelectedTab)
                    activityAppointmentBinding.appointmentListAppointmentTabBtn.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.appointmentUnSelectedTabTxtColor
                        )
                    )
                    activityAppointmentBinding.appointmentListAppointmentTabBtn.typeface = typefaceNormal


                }
            }

            appointmentViewModel.currentTabSelected = selectedTab
        }

        return isCurrentTabSelected
    }

    /**
     * Method to give focus to the selected view if the view is hidden inside scroll
     **/
    private fun focusOnView(scroll: HorizontalScrollView, view: View) {
        handler.post {
            val vLeft = view.left
            val vRight = view.right
            val sWidth = scroll.width
            scroll.smoothScrollTo((vLeft + vRight - sWidth) / 2, 0)
        }
    }


    /**
     * Method to toggle(show/hide) Past Appointment list in appointment tab
     **/
//    private fun animatePastAppointmentList(){
//
//        val pastAppointmentHeight :Int
//
//        if (!isPastAppointmentOpened){
//            pastAppointmentHeight = pastAppointmentRVHeight
//            pastAppointmentVisibilitySwitch.animate().rotation(180f).start()
//            isPastAppointmentOpened = true
//            fetchPastAppointment()
//        }
//        else{
//            pastAppointmentHeight = 1
//            pastAppointmentVisibilitySwitch.animate().rotation(0f).start()
//            isPastAppointmentOpened = false
//        }
//
//
//
//        val valueAnimator = ValueAnimator.ofInt(appointmentListPastAppointmentRV.measuredHeight, pastAppointmentHeight)
//        valueAnimator.addUpdateListener {valueAnimatorValue ->
//            val animatedValue = valueAnimatorValue.animatedValue as Int
//            appointmentListPastAppointmentRV.layoutParams.height = animatedValue
//            appointmentListPastAppointmentRV.requestLayout()
//        }
//        valueAnimator.duration = 400
//        valueAnimator.start()
//    }


    private fun deleteUpcomingAppointment(appointment: Appointment, position: Int) {
        val screenWidth = UIUtil.getScreenWidth(this)
        val width =
            screenWidth - (2 * this.resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))
        val builder = MDCancelAppointmentDialog(this, width, picasso)
        builder.appointment = appointment
        builder.setCancelAppointmentListener(object : MDCancelAppointmentDialog.CancelAppointmentListener {
            override fun onCancelAppointmentClick(reason: String) {
                deleteAppointment(appointment, position, reason)
            }
        })
        builder.show()
    }

    private fun deleteAppointment(appointment: Appointment, position: Int, reasonToCancel: String? = null) {
        progressDialog.show()
        appointmentViewModel.deleteAppointment(appointment, reasonToCancel).observe(this) { resp ->

            if (resp != null) {
                progressDialog.dismiss()
                if (resp is Boolean) {

                    when (appointment.appointmentCode) {
                        ModelUtil.appointmentUpcoming -> {
                            appointmentViewModel.isAppointmentSuccessCallbackToBeSent = true
                            val size = upcomingAppointmentListAdapter.removeAppointment(position)
                            activityAppointmentBinding.appointmentListUpcomingAppointmentRV.viewTreeObserver.addOnGlobalLayoutListener(object :
                                ViewTreeObserver.OnGlobalLayoutListener {
                                override fun onGlobalLayout() {
                                    activityAppointmentBinding.appointmentListUpcomingAppointmentRV.viewTreeObserver.removeOnGlobalLayoutListener(this)

                                    val appointmentRVHeight = activityAppointmentBinding.appointmentListUpcomingAppointmentRV.height
                                    val height = UIUtil.getScreenHeight(this@AppointmentActivity)

                                    val appointmentViewMaxHeight = height * 40 / 100

                                    if (appointmentRVHeight > appointmentViewMaxHeight) {
                                        val params = LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            appointmentViewMaxHeight
                                        )
                                        activityAppointmentBinding.appointmentListUpcomingAppointmentRV.layoutParams = params
                                    } else {
                                        val params = LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                        )
                                        activityAppointmentBinding.appointmentListUpcomingAppointmentRV.layoutParams = params
                                    }

                                }
                            })
                            if (size == 0) {
                                activityAppointmentBinding.appointmentListAppointmentNoDataLayout.visibility = View.VISIBLE
                                activityAppointmentBinding.appointmentListUpcomingAppointmentRV.visibility = View.GONE
                                //  appointmentListRequestedAppointmentHeadingTV.visibility = View.GONE

                            } else {
                                activityAppointmentBinding.appointmentListAppointmentNoDataLayout.visibility = View.GONE
                                activityAppointmentBinding.appointmentListUpcomingAppointmentRV.visibility = View.VISIBLE
                                // appointmentListRequestedAppointmentHeadingTV.visibility = View.VISIBLE

                            }

                            UIUtil.showToast(this, R.string.appointment_cancel_success_msg, Toast.LENGTH_LONG)
                        }

                        ModelUtil.appointmentRequested -> {
                            val size = requestedAppointmentListAdapter.removeAppointment(position)
                            if (size == 0) {
                                activityAppointmentBinding.appointmentListRequestedNoDataLayout.visibility = View.VISIBLE
                                activityAppointmentBinding.appointmentListRequestedAppointmentRV.visibility = View.GONE
                                //  appointmentListRequestedAppointmentHeadingTV.visibility = View.GONE

                            } else {
                                activityAppointmentBinding.appointmentListRequestedNoDataLayout.visibility = View.GONE
                                activityAppointmentBinding.appointmentListRequestedAppointmentRV.visibility = View.VISIBLE
                                // appointmentListRequestedAppointmentHeadingTV.visibility = View.VISIBLE

                            }

                            UIUtil.showToast(this, R.string.appointment_delete_success_msg, Toast.LENGTH_LONG)
                        }

                        ModelUtil.appointmentDenied -> {
                            val size = deniedAppointmentListAdapter.removeAppointment(position)
                            if (size == 0) {
                                activityAppointmentBinding.appointmentListDeniedNoDataLayout.visibility = View.VISIBLE
                                activityAppointmentBinding.appointmentListDeniedAppointmentRV.visibility = View.GONE
                                //  appointmentListDeniedAppointmentHeadingTV.visibility = View.GONE

                            } else {
                                activityAppointmentBinding.appointmentListDeniedNoDataLayout.visibility = View.GONE
                                activityAppointmentBinding.appointmentListDeniedAppointmentRV.visibility = View.VISIBLE
                                //  appointmentListDeniedAppointmentHeadingTV.visibility = View.VISIBLE
                            }
                            UIUtil.showToast(this, R.string.appointment_delete_success_msg, Toast.LENGTH_LONG)
                        }

                        else -> {
                            val size = waitListAppointmentListAdapter.removeAppointment(position)
                            if (size == 0) {
                                activityAppointmentBinding.appointmentListWaitingListNoDataLayout.visibility = View.VISIBLE
                                activityAppointmentBinding.appointmentListWaitingListAppointmentRV.visibility = View.GONE
                                activityAppointmentBinding.appointmentListWaitingListAppointmentHeadingTV.visibility = View.GONE

                            } else {
                                activityAppointmentBinding.appointmentListWaitingListNoDataLayout.visibility = View.GONE
                                activityAppointmentBinding.appointmentListWaitingListAppointmentRV.visibility = View.VISIBLE
                                activityAppointmentBinding.appointmentListWaitingListAppointmentHeadingTV.visibility = View.VISIBLE
                            }
                            UIUtil.showToast(this, R.string.appointment_cancel_success_msg, Toast.LENGTH_LONG)
                        }
                    }
                } else if (resp is APIResponseError) {

                    if (appointment.appointmentCode == ModelUtil.appointmentWaitingList || appointment.appointmentCode == ModelUtil.appointmentUpcoming) {
                        UIUtil.showToast(this, R.string.error_appointment_cancel_msg, Toast.LENGTH_LONG)
                    } else {
                        UIUtil.showToast(this, R.string.error_appointment_delete_msg, Toast.LENGTH_LONG)
                    }
                }
            }
        }
    }


    private fun showConfirmDeleteAppointment(appointment: Appointment, position: Int) {

        val builder = AlertDialog.Builder(this)


        if (appointment.appointmentCode == ModelUtil.appointmentWaitingList) {
            builder.setMessage(R.string.appointment_wait_list_delete_confirmation_msg)
        } else {
            builder.setMessage(R.string.appointment_delete_confirmation_msg)
        }
        builder.setPositiveButton(R.string.yes_str) { _, _ ->
            deleteAppointment(appointment, position)
        }

        builder.setNegativeButton(R.string.no_str) { _, _ ->

        }

        val alertDialog = builder.create()
        alertDialog.setOnShowListener { dialog ->
            val context = this@AppointmentActivity
            val negButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_NEGATIVE)
            negButton.setBackgroundResource(R.drawable.item_ripple_dashboard_navigation_background)
            negButton.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(20, 20, 20, 20)

            val posButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            posButton.setBackgroundResource(R.drawable.item_ripple_dashboard_navigation_background)
            posButton.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
            val posParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            posParams.setMargins(0, 0, 20, 0)
            negButton.layoutParams = posParams
        }

        alertDialog.show()
    }


    override fun onBackPressed() {

        if (appointmentViewModel.isAppointmentSuccessCallbackToBeSent) {
            setResult(Activity.RESULT_OK)
        }

        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    interface AppointmentDeleteListener {
        fun onDelete(appointment: Appointment, position: Int)
    }
}
