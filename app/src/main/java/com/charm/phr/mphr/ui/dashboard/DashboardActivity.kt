package com.charm.phr.mphr.ui.dashboard


import MdFloatingActionMenu
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import coil.ImageLoader
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityDashboardBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.FloatingActionMenuItem
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.SectionConfigList
import com.charm.phr.mphr.model.UnreadCount
import com.charm.phr.mphr.service.notification.RegisterDeviceWorker
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.Theme.MdColors
import com.charm.phr.mphr.ui.Theme.MdTheme
import com.charm.phr.mphr.ui.announcement.AnnouncementListActivity
import com.charm.phr.mphr.ui.appointment.book_appointment.BookAppointmentActivity
import com.charm.phr.mphr.ui.dashboard.compose.PatientImageItem
import com.charm.phr.mphr.ui.dashboard.user_profile.UserProfileActivity
import com.charm.phr.mphr.ui.message.compose.ComposeMessageActivity
import com.charm.phr.mphr.ui.message.messageDetail.MessageDetailActivity
import com.charm.phr.mphr.util.APIList
import com.charm.phr.mphr.util.AuthStatus
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MdInAppUpdateActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.NotificationUtil
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.UIUtil.Companion.hideView
import com.charm.phr.mphr.util.UIUtil.Companion.showView
import com.squareup.picasso.Picasso
import javax.inject.Inject

class DashboardActivity : MdInAppUpdateActivity() {

    lateinit var dashboardViewModel: DashboardViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var dashboardViewPagerAdapter: DashboardViewPagerAdapter

//    @Inject
//    lateinit var dashboardNavigationPatListAdapter: DashboardNavigationPatListAdapter

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var sharedPreference: SharedPreferences

    var pixelDensity: Float = 0f
    var mainActionFabClick: View? = null
    var isMyInfoOpened = false
    var isNavPatientListOpened = false

    companion object {
        const val patientDemographicsActivityCode = 6
        const val medicationCode = 7
        const val wellnessCode = 8
        const val invoiceCode = 9
        const val messageCode = 10
        const val questionnaireCode = 10
    }

    object DashBoardResult {
        val onSwitchPatient = 1001
        val onPatImageChange = 1002
    }

    private lateinit var binding: ActivityDashboardBinding

    var isMenuExpanded: MutableState<Boolean?> = mutableStateOf(null)

    private var appointmentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                dashboardViewModel.getUnreadCountLive()
                dashboardViewModel.isUpcomingAppointmentFetch = false
                dashboardViewModel.isUpcomingAppointmentFetch = false
                dashboardViewPagerAdapter.updateFeeds()
            }
        }

    private var billingResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                dashboardViewModel.getUnreadCountLive()
                dashboardViewModel.isInvoiceFetch = false
                dashboardViewPagerAdapter.updateFeeds()
            }
        }

    private var updateUnreadCountLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                dashboardViewModel.getUnreadCountLive()
                dashboardViewPagerAdapter.updateFeeds()
            }
        }


    private var wellnessResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                dashboardViewModel.getUnreadCountLive()
                dashboardViewModel.isVitalsFetch = false
                dashboardViewPagerAdapter.updateFeeds()
            }
        }

    private var messageResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                dashboardViewModel.getUnreadCountLive()
                dashboardViewModel.isMessageFetch = false
                dashboardViewPagerAdapter.updateFeeds()
            }
        }

    private var questionnaireResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                dashboardViewModel.getUnreadCountLive()
                dashboardViewModel.isQuestionnaireFetch = false
                dashboardViewPagerAdapter.updateFeeds()
            }
        }

    private var resultLauncherPatientDemographicsActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUpdated =
                    result.data?.getBooleanExtra(ModelUtil.patientImageUpdated, false) == true
                if (imageUpdated) {
                    imageLoader.diskCache?.clear()
                    imageLoader.memoryCache?.clear()
                    dashboardViewModel.imageReloadKey.intValue++
                }
                dashboardViewModel.getUnreadCountLive()
                dashboardViewModel.isPatientVitalsImgChanged = true
                dashboardViewModel.isUpcomingAppointmentFetch = false
                binding.dashboardBottomSheet.selectedItemId = R.id.menuHome
            }
        }


    fun intentToBillingWithResult(intent: Intent) {
        billingResultLauncher.launch(intent)
    }

    fun updateUnreadCountLauncher(intent: Intent) {
        updateUnreadCountLauncher.launch(intent)
    }

    fun intentToMessageWithResult(intent: Intent) {
        messageResultLauncher.launch(intent)
    }

    fun intentToAppointmentWithResult(intent: Intent) {
        appointmentResultLauncher.launch(intent)
    }

    fun intentToQuestionnaireWithResult(intent: Intent) {
        questionnaireResultLauncher.launch(intent)
    }

    fun intentToWellnessWithResult(intent: Intent) {
        wellnessResultLauncher.launch(intent)
    }

    private var flowsheetResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                dashboardViewModel.resetThalassemiaCache()
                dashboardViewPagerAdapter.updateFeeds()
            }
        }

    fun intentToFlowsheetWithResult(intent: Intent) {
        flowsheetResultLauncher.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dashboardViewModel = ViewModelProvider(this, viewModelFactory)[DashboardViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.dashboardToolbar)
        //BottomNavigationView provide bottom padding so no need to apply bottom view insert

        pixelDensity = resources.displayMetrics.density

        val patientObj = authManager.selectedPatientLive.value
        val patFilteredList =
            authManager.patientList.filter { it.practicePatientId != patientObj?.practicePatientId && !patientObj?.practicePatientId.isNullOrBlank() }

//        dashboardNavigationPatListAdapter.setItems(ArrayList(patFilteredList))
        binding.dashboardViewPager.adapter = dashboardViewPagerAdapter
        binding.dashboardViewPager.isUserInputEnabled = true
//        binding.dashboardNavigationPatientListRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
//        binding.dashboardNavigationPatientListRV.adapter = dashboardNavigationPatListAdapter

        dashboardViewModel.fragmentCallbackLive.postValue(DashboardViewPagerAdapter.DashboardCallback.PROGRESS)

        dashboardViewModel.getUnreadCountLive()

        checkAndRegisterForNotification()

        authManager.selectedPatientLive.observe(this) { patient ->

            if (patient != null) {
                dashboardViewModel.imageReloadKey.intValue++

                binding.dashboardToolbarText.text =
                    getString(R.string.dashboard_toolbar_heading, patient.fullName)
//                navigationHeaderBinding.dashboardNavigationPatientName.text = patient.fullName

                val initials = UIUtil.formPatientInitials(patient)

//                navigationHeaderBinding.dashboardNavigationPatientDummyPicTxt.visibility = View.VISIBLE
//                navigationHeaderBinding.dashboardNavigationPatientDummyPicTxt.text = initials

                if (patient.unitSystem == null) {
                    dashboardViewModel.getVitalUnitSystem()
                }

                dashboardViewModel.trackerList = null

                binding.dashboardToolbarAnnoucementImg.visibility = View.GONE
                binding.dashboardToolbarAnnouncementUnreadCount.visibility = View.GONE

                val filteredList2 =
                    authManager.patientList.filter { it.practicePatientId != patient.practicePatientId }

//                dashboardNavigationPatListAdapter.setItems(ArrayList(filteredList2))
                dashboardViewModel.isActiveMedicationFetch = false
                dashboardViewModel.isUpcomingAppointmentFetch = false
                dashboardViewModel.isVitalsFetch = false
                dashboardViewModel.isInvoiceFetch = false
                dashboardViewModel.isQuestionnaireFetch = false
                dashboardViewModel.isMessageFetch = false


                Log.d("MDProgress", "progress Called")

//                    if (patient.isHealthPassportAvailable) {
//                        binding.dashboardNavigationHealthPassportMenu.visibility = View.VISIBLE
//                    } else {
//                        binding.dashboardNavigationHealthPassportMenu.visibility = View.GONE
//                    }

                dashboardViewModel.fragmentCallbackLive.postValue(DashboardViewPagerAdapter.DashboardCallback.PROGRESS)

                binding.patientImageView.visibility = View.INVISIBLE

                setPatientImage(patient, onClick = {
                    val intent = Intent(this, UserProfileActivity::class.java)
                    val filteredList =
                        dashboardViewModel.composeMsgRestrictionList.filter { !it.isMessageRestrict && it.allowPHRMessaging }
                    intent.putExtra(
                        ModelUtil.messageComposeMsgRestrictionListIntent,
                        MDJsonBuilder.formComposeMsgRestrictionList(filteredList)
                    )
                    intent.putExtra(ModelUtil.messageComposeFromDashboardIntent, true)
                    resultLauncherPatientDemographicsActivity.launch(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                })
                getPatientSection()

                binding.dashboardToolbarAnnoucementImg.setOnClickListener {
                    val intent =
                        Intent(applicationContext, AnnouncementListActivity::class.java)
                    updateUnreadCountLauncher.launch(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }

            } else {
                authManager.restoreSavedData()
            }
        }

        binding.dashboardViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> binding.dashboardBottomSheet.selectedItemId = R.id.menuHome
                    1 -> binding.dashboardBottomSheet.selectedItemId = R.id.menuHealthData
                }
            }
        })

        authManager.authStatusLive.observe(this) {
            if (it == AuthStatus.USER_ONLINE) {
                fetchComposeRestriction()
            }
        }

        dashboardViewModel.unreadCountLive.observe(this) { resp ->

            if (resp != null) {

                updateAnnouncement(resp)
            }

        }

        binding.dashboardBottomSheet.setOnItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.menuHome -> {
                    binding.dashboardViewPager.setCurrentItem(0, true)
                    return@setOnItemSelectedListener true
                }

                R.id.menuHealthData -> {
                    binding.dashboardViewPager.setCurrentItem(1, true)
                    return@setOnItemSelectedListener true
                }

                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }

//        binding.dashboardToolbarPatImg.setOnClickListener {
//            //intent to compose fragment
//            val intent = Intent(this, UserProfileActivity::class.java)
//            resultLauncherPatientDemographicsActivity.launch(intent)
//            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
//        }
        binding.dashboardBottomSheet.visibility = View.VISIBLE


    }

    // Checks if the device is registered for notifications and registers if not
    private fun checkAndRegisterForNotification() {
        if (!BuildUtil.isPushNotificationEnabled) return
        val isDeviceRegistered = NotificationUtil.isDeviceRegisteredForNotifications(applicationContext)
        if (!isDeviceRegistered){
            RegisterDeviceWorker.enqueueRegisterDeviceWork(applicationContext)
        }
    }

    private fun setPatientImage(patient: Patient, onClick: () -> Unit) {
        binding.patientImageView.setContent {
            MPHRTheme {
                RenderPatientImage(patient, onClick = onClick)

            }
        }
    }

    private fun setFloatingButtonContent() {
        val items: ArrayList<FloatingActionMenuItem> = arrayListOf()
        binding.dashboardFabAdd.setContent {
            if (dashboardViewModel.isComposeMessageAllowed) {
                items.add(
                    FloatingActionMenuItem(
                        icon = R.drawable.ic_message_white,
                        label = getString(R.string.dashboard_compose_msg_fab),
                        backgroundColor = colorResource(R.color.dashboardComposeMessageFabColor),
                        onClick = {
                            intentToComposeMessage()
                        },
                        contentColor = colorResource(R.color.white)
                    )
                )
            }
            if (dashboardViewModel.isBookAppointmentAllowed) {
                items.add(
                    FloatingActionMenuItem(
                        icon = R.drawable.ic_appointment_white,
                        label = getString(R.string.book_appointment_str),
                        backgroundColor = colorResource(R.color.dashboardBookAppointmentFabColor),
                        onClick = {
                            intentToBookAppointment()
                        },
                        contentColor = colorResource(R.color.white)
                    )
                )
            }
            MPHRTheme {
                MdFloatingActionMenu(
                    items = items,
                    isMenuExpanded = isMenuExpanded,
                    modifier = Modifier,
                    onMenuExpandedChange = {
                        isMenuExpanded.value = it
                    }
                )
            }
        }
    }

    @Composable
    private fun RenderPatientImage(patient: Patient, onClick: () -> Unit) {

        val mutableInteractionSource = remember {
            MutableInteractionSource()
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MdColors.White)
                .clickable(
                    interactionSource = mutableInteractionSource,
                    indication = ripple(bounded = true)
                ) {
                    onClick()
                }
        ) {
            val hasImage = patient.isPhotoAvailableOnline
            val imageUrl = APIList.getPatientImageURL(patient.practicePatientId)
            val imageSize = dimensionResource(R.dimen.dashboard_toolbar_pat_img_size)
            PatientImageItem(
                hasImage = hasImage,
                imageUrl = imageUrl,
                imageLoader = imageLoader,
                imgSize = imageSize,
                selectedPatient = patient,
                textStyle = MdTheme.typography.semiBold14TextStyle.copy(letterSpacing = 1.sp),
                imageReloadKey = dashboardViewModel.imageReloadKey.intValue
            )
        }
    }

    @Composable
    @Preview(showBackground = true)
    fun PreviewPatientImage() {
        RenderPatientImage(
            Patient(
                firstName = "Lewis",
                lastName = "Hamilton",
                fullName = "Lewis Hamilton"
            ), onClick = {})
    }


    fun intentToBookAppointment() {
        val intent = Intent(this, BookAppointmentActivity::class.java)
        intent.putExtra(ModelUtil.appointmentIsFromDashboardIntent, true)
        appointmentResultLauncher.launch(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun intentToComposeMessage() {
        val intent = Intent(this, ComposeMessageActivity::class.java)
        val filteredList =
            dashboardViewModel.composeMsgRestrictionList.filter { !it.isMessageRestrict && it.allowPHRMessaging }
        intent.putExtra(
            ModelUtil.messageComposeMsgRestrictionListIntent,
            MDJsonBuilder.formComposeMsgRestrictionList(filteredList)
        )
        intent.putExtra(ModelUtil.messageComposeFromDashboardIntent, true)
        appointmentResultLauncher.launch(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onBackPressed() {
        if (binding.dashboardDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.dashboardDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

//        val rectangle = Rect()
//        val window = window
//        window.decorView.getWindowVisibleDisplayFrame(rectangle)
//        val statusBarHeight = rectangle.top
//
//        val screenWidth = UIUtil.getScreenWidth(this)
//        val width = (screenWidth * 3) / 4
//
//        if (statusBarHeight != 0) {
//            binding.dashboardNavigationView.setPadding(
//                binding.dashboardNavigationView.paddingLeft, statusBarHeight - 35, binding.dashboardNavigationView.paddingRight, binding.dashboardNavigationView.paddingBottom
//            )
//        }
//
//        if (width != 0) {
//            val param = binding.dashboardNavigationView.layoutParams as ViewGroup.LayoutParams
//            param.width = width
//            binding.dashboardNavigationView.layoutParams = param
//        }

    }

//    private fun loadPatientImg(patient: Patient, navigationHeaderBinding: LayoutDashboardNavigationHeaderBinding) {
//        val dashboardNavigationImgSize = resources.getDimensionPixelSize(R.dimen.patient_list_img_size)
//
//        val patientImageURl = APIList.getPatientImageURL(patient.practicePatientId)
//        Log.d("MDPatLoad", "URL -> $patientImageURl")
//
//        val dashboardNavImgLoader = picasso.load(patientImageURl)
//        navigationHeaderBinding.dashboardNavigationPatientImg.visibility = View.INVISIBLE
//        navigationHeaderBinding.dashboardNavigationPatientDummyPicTxt.visibility = View.VISIBLE
//
//        if (dashboardViewModel.isPatientVitalsImgChanged) {
//            dashboardNavImgLoader.networkPolicy(NetworkPolicy.NO_CACHE)
//            dashboardNavImgLoader.memoryPolicy(MemoryPolicy.NO_CACHE)
//        }
//
//        dashboardNavImgLoader.resize(dashboardNavigationImgSize, dashboardNavigationImgSize)
//            .into(navigationHeaderBinding.dashboardNavigationPatientImg, object : Callback {
//
//                override fun onSuccess() {
//                    try {
//                        navigationHeaderBinding.dashboardNavigationPatientImg.visibility = View.VISIBLE
//                        navigationHeaderBinding.dashboardNavigationPatientDummyPicTxt.visibility = View.GONE
//                    } catch (e: Exception) {
//
//                    }
//                }
//
//                override fun onError(e: Exception?) {
//                }
//            })
//
//    }

    private fun fetchComposeRestriction() {

        dashboardViewModel.getComposeMsgRestriction().observe(this) { resp ->

            if (resp != null) {

                if (resp is ArrayList<*>) {

                    dashboardViewModel.isComposeMsgRestrictionFetched = true

                    val composeMsgRestrictionList = resp as ArrayList<ComposeMsgRestriction>
                    dashboardViewModel.composeMsgRestrictionList = composeMsgRestrictionList
                    dashboardViewModel.isComposeAvailable =
                        composeMsgRestrictionList.any { !it.isMessageRestrict && it.allowPHRMessaging }

                    binding.patientImageView.visibility = View.VISIBLE

                    updateFab()

                    if (dashboardViewModel.isComposeDetailClicked) {
                        dashboardViewModel.isComposeDetailClicked = false
                        progressDialog.dismiss()
                        val messages = dashboardViewModel.message

                        val intent = Intent(this, MessageDetailActivity::class.java)
                        intent.putExtra(ModelUtil.messageIdIntent, messages?.messageId)
                        intent.putExtra(ModelUtil.practiceIdIntent, messages?.practiceId)
                        intent.putExtra(
                            ModelUtil.messageDateIntent,
                            messages?.messageDate?.toString(ModelUtil.sqlTimeStampFormattor())
                        )

                        val filteredArray =
                            ArrayList(dashboardViewModel.composeMsgRestrictionList.filter { it.practiceId == messages?.practiceId })

                        if (filteredArray.isEmpty()) {
                            filteredArray.add(
                                ComposeMsgRestriction(
                                    practiceId = messages?.practiceId ?: ""
                                )
                            )
                        }

                        intent.putExtra(
                            ModelUtil.messageComposeMsgRestrictionListIntent,
                            MDJsonBuilder.formComposeMsgRestrictionList(filteredArray)
                        )
                        messageResultLauncher.launch(intent)
                    } else {
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                    }

                } else if (resp is APIResponseError) {

                    binding.patientImageView.visibility = View.VISIBLE

                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    val builder = AlertDialog.Builder(this)

                    builder.setMessage(R.string.error_occurred_try_again)
                    builder.setPositiveButton(R.string.retry_str) { _, _ ->
                        progressDialog.show()
                        fetchComposeRestriction()
                    }
                    builder.setNegativeButton(R.string.cancel_str) { _, _ ->
                        dashboardViewModel.isComposeMsgRestrictionFetched = true
                    }
                    builder.show()
                }
            }

        }
    }

    fun floatingActionButtonVisiiblity(): Int {
        return binding.dashboardFabAdd.visibility
    }

    fun showFab() {
        //    binding.dashboardFabAdd.animate().alpha(1f).setDuration(100).withStartAction { binding.dashboardFabAdd.visibility = View.VISIBLE }.start()
        binding.dashboardFabAdd.visibility = View.VISIBLE
        binding.dashboardFabMainRl.visibility = View.VISIBLE
    }

    fun hideFab() {
        //    binding.dashboardFabAdd.animate().alpha(0f).setDuration(100).withEndAction { binding.dashboardFabAdd.visibility = View.INVISIBLE }.start()
        binding.dashboardFabAdd.visibility = View.GONE
        binding.dashboardFabMainRl.visibility = View.GONE
    }

    fun setEnabledAnimView(view: View, isEnabled: Boolean) {
        try {
            // view.setEnabled(isEnabled)
        } catch (e: Exception) {
            //       PrintStackTrack.printStackTrack(e)
        }
    }

    private fun updateFab() {
        //Check Message module enabled and check for Role base access control for Add Message
        if (((ModelUtil.isModuleEnabled(
                authManager.selectedPatientSection, ModelUtil.messageModuleId
            ) && dashboardViewModel.isComposeAvailable) && (RbacUtil.checkPrivilege(
                RbacUtil.Companion.RBACPrivileges.ADD_MESSAGES, applicationContext
            )))
        ) {
            dashboardViewModel.isComposeMessageAllowed = true
        } else {
            dashboardViewModel.isComposeMessageAllowed = false
        }
        //Check Appointment module enable and also check for Role base access control for Add Appointment
        if (((ModelUtil.isModuleEnabled(
                authManager.selectedPatientSection, ModelUtil.appointmentModelId
            )) && (RbacUtil.checkPrivilege(
                RbacUtil.Companion.RBACPrivileges.ADD_APPOINTMENT, applicationContext
            )))
        ) {
            dashboardViewModel.isBookAppointmentAllowed = true
        } else {
            dashboardViewModel.isBookAppointmentAllowed = false
        }
        if (dashboardViewModel.isComposeMessageAllowed || dashboardViewModel.isBookAppointmentAllowed) {
            setFloatingButtonContent()
            binding.dashboardFabAdd.showView()
            binding.dashboardFabMainRl.showView()
        } else {
            binding.dashboardFabAdd.hideView()
            binding.dashboardFabMainRl.hideView()
        }
    }

    private fun getPatientSection() {

        if (dashboardViewModel.patientDetailLive.hasObservers()) {
            dashboardViewModel.patientDetailLive.removeObservers(this)
        }


        dashboardViewModel.getPatientDetails().observe(this) { resp ->

            if (resp != null) {

                if (resp is SectionConfigList) {
                    Log.d("MDProgress", "execution Called")

                    updateAnnouncement(dashboardViewModel.unreadCountLive.value)
                    dashboardViewModel.fragmentCallbackLive.postValue(resp)
                    val practiceList =
                        ModelUtil.getEnabledPractices(authManager, ModelUtil.messageModuleId)
                    if (practiceList.isNotEmpty()) {
                        fetchComposeRestriction()
                    } else {
                        binding.patientImageView.visibility = View.VISIBLE
                        updateFab()
                    }
                } else if (resp is APIResponseError) {

                    binding.dashboardToolbarAnnoucementImg.visibility = View.GONE
                    binding.dashboardToolbarAnnouncementUnreadCount.visibility = View.GONE

                    dashboardViewModel.fragmentCallbackLive.postValue(DashboardViewPagerAdapter.DashboardCallback.ERROR)
                }

                dashboardViewModel.patientDetailLive.postValue(null)

            }
        }
    }

    private fun updateAnnouncement(unreadCount: UnreadCount?) {

        if (unreadCount != null) {

            val isEnabled = ModelUtil.isModuleEnabled(authManager.selectedPatientSection, ModelUtil.announcementModelId)

            if (isEnabled) {
                binding.dashboardToolbarAnnoucementImg.visibility = View.VISIBLE
                if (unreadCount.practiceAnnouncementCount > 0) {
                    binding.dashboardToolbarAnnouncementUnreadCount.visibility = View.VISIBLE
                    if (unreadCount.practiceAnnouncementCount > 99) {
                        binding.dashboardToolbarAnnouncementUnreadCount.setText(R.string.dashboard_max_notification)
                    } else {
                        binding.dashboardToolbarAnnouncementUnreadCount.text =
                            "${unreadCount.practiceAnnouncementCount}"
                    }
                } else {
                    binding.dashboardToolbarAnnouncementUnreadCount.visibility = View.GONE
                }
            } else {
                binding.dashboardToolbarAnnouncementUnreadCount.visibility = View.GONE
                binding.dashboardToolbarAnnoucementImg.visibility = View.GONE

            }
        }
    }

    interface DashboardFragmentsCallback {
        fun showProgress()
        fun startPopulate(sectionConfig: SectionConfigList?, isFromWeb: Boolean)
        fun showErrorPage()
    }
}
