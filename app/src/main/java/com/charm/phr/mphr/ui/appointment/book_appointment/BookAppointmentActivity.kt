package com.charm.phr.mphr.ui.appointment.book_appointment

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityBookAppointmentBinding
import com.charm.phr.mphr.databinding.ItemTimeSlotBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.AppointmentConfig
import com.charm.phr.mphr.model.AppointmentRequest
import com.charm.phr.mphr.model.CardOnFilePOJO
import com.charm.phr.mphr.model.Facility
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.MemberAvailability
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PaymentGatewayToken
import com.charm.phr.mphr.model.PaymentInfo
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.model.SlotMetaData
import com.charm.phr.mphr.model.TransactionInfo
import com.charm.phr.mphr.ui.appointment.book_appointment.easytelecare.EasyTeleBookAppointmentActivity
import com.charm.phr.mphr.ui.components.MDTextInputEdittext
import com.charm.phr.mphr.ui.components.MDTimePickerDialog
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.PaymentUtil
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.UIUtil.Companion.hideView
import com.charm.phr.mphr.util.UIUtil.Companion.removeTime
import com.charm.phr.mphr.util.UIUtil.Companion.showView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.Period
import java.time.DayOfWeek
import java.time.YearMonth
import javax.inject.Inject

/**
 * [BookAppointmentActivity] is used to book an appointment to the selected patient.
 **/

//TODO: The Third Party Calendar uses localTime And Other date time objects, so we need to change from third party to our own calendar lib
class BookAppointmentActivity : MDBaseActivity(), PaymentResultListener {

    val TAG = "bookAppActivity"

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var bookAppointmentViewModel: BookAppointmentViewModel
    private lateinit var backPressAlertDialogBuilder: AlertDialog.Builder
    private lateinit var binding: ActivityBookAppointmentBinding

    companion object {
        const val bookAppointmentFacilityFetchCode = 1
        const val bookAppointmentFacilityFetchFromPracticeCode = 5
        const val bookAppointmentMemberFetchCode = 2
        const val bookAppointmentAppointmentConfigFetchCode = 3
        const val bookAppointmentSlotsFetchCode = 4

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBookAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookAppointmentViewModel =
            ViewModelProvider(this, viewModelFactory).get(BookAppointmentViewModel::class.java)
        bookAppointmentViewModel.isFromDashboard =
            intent.getBooleanExtra(ModelUtil.appointmentIsFromDashboardIntent, false)

        UIUtil.applyViewInsertForToolbar(binding.bookAppointmentToolbar)
        UIUtil.applyViewInsertForBottomView(binding.bookAppointmentRootLayout)

        UIUtil.makeTextViewMandatory(binding.bookAppointmentVisitTypeTV)
        UIUtil.makeTextViewMandatory(binding.bookAppointmentReasonTV)
        UIUtil.makeTextViewMandatory(binding.bookAppointmentMemberNameTV)

        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    super.onBackPressed()
                    if (bookAppointmentViewModel.isFromDashboard) {
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    }
                }

                DialogInterface.BUTTON_NEGATIVE -> {

                }
            }
        }

        backPressAlertDialogBuilder = AlertDialog.Builder(this)
        backPressAlertDialogBuilder.setMessage(R.string.discard_changes_confirmation_string)
            .setPositiveButton(R.string.yes_str, dialogClickListener)
            .setNegativeButton(R.string.no_str, dialogClickListener)

        val datePreference = arrayListOf<String>()
        datePreference.add(getString(R.string.book_appointment_date_pref_any_str))
        datePreference.add(getString(R.string.book_appointment_date_pref_specific_str))
        binding.bookAppointmentDatePreferenceET.setItems(datePreference, this)

        val timePreference = arrayListOf<String>()
        timePreference.add(getString(R.string.book_appointment_time_pref_any_str))
        timePreference.add(getString(R.string.book_appointment_time_pref_range_str))
        binding.bookAppointmentTimePreferenceET.setItems(timePreference, this)

        binding.bookAppointmentDateET.inputType = InputType.TYPE_NULL
        binding.bookAppointmentStartTimeET.inputType = InputType.TYPE_NULL
        binding.bookAppointmentEndTimeET.inputType = InputType.TYPE_NULL

        binding.bookAppointmentDateET.setOnClickListener {
            showDatePicker(binding.bookAppointmentDateET)
        }

        binding.bookAppointmentStartTimeET.setOnClickListener {
            showTimePicker(binding.bookAppointmentStartTimeET)
        }

        binding.bookAppointmentEndTimeET.setOnClickListener {
            showTimePicker(binding.bookAppointmentEndTimeET)
        }

        binding.bookAppointmentDatePreferenceET.setOnItemClickListener { position, str ->

            if (position == 0) {
                binding.bookAppointmentDateLayout.visibility = View.GONE
            } else {
                binding.bookAppointmentDateLayout.visibility = View.VISIBLE
            }
        }

        binding.bookAppointmentCardNumberET.addTextChangedListener(object : TextWatcher {
            private val totalTxtCount = 19 // size of pattern 0000 0000 0000 0000

            /*
                        private val totalDigits = 16 // max numbers of digits in pattern: 0000 x 4
            */
            private val dividerModulo =
                5 //  means divider position is every 5th symbol beginning with 1
            private val dividerPosition =
                dividerModulo - 1 // means divider position is every 4th symbol beginning with 0
            private val divider = " "

            override fun afterTextChanged(editable: Editable) {
                if (!isInputCorrect(editable, totalTxtCount, dividerModulo, divider)) {
                    binding.bookAppointmentCardNumberET.setText(
                        buildCorrectString(
                            getDigitArray(editable), dividerPosition, divider
                        )
                    )
                    binding.bookAppointmentCardNumberET.setSelection(
                        binding.bookAppointmentCardNumberET.text?.length ?: 0
                    )
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            private fun isInputCorrect(
                editable: Editable, totalSymbols: Int, dividerModulo: Int, divider: String
            ): Boolean {
                var isCorrect = editable.length <= totalSymbols
                for (index in 0 until editable.length) {
                    isCorrect = if (index > 0 && (((index + 1) % dividerModulo) == 0)) {
                        isCorrect && divider == editable.toString()[index].toString()
                    } else {
                        isCorrect && Character.isDigit(editable.toString()[index])
                    }
                }

                if (editable.isNotEmpty()) {
                    isCorrect =
                        isCorrect && divider != editable.toString()[editable.length - 1].toString()
                }




                return isCorrect
            }

            private fun buildCorrectString(
                digits: ArrayList<Char>, dividerPosition: Int, divider: String
            ): String {
                val formatted: StringBuilder = StringBuilder()

                for (index in 0 until digits.size) {
                    formatted.append(digits[index])
                    if ((index > 0) && (index < (digits.size - 1)) && (((index + 1) % dividerPosition) == 0)) {
                        formatted.append(divider)
                    }

                }
                return formatted.toString().trim()
            }

            private fun getDigitArray(s: Editable): ArrayList<Char> {
                val digits = arrayListOf<Char>()
                for (i in 0 until s.length) {
                    val current = s[i]
                    if (Character.isDigit(current)) {
                        digits.add(current)
                    }
                }

                return digits
            }
        })


        setUpClickListeners()
        updatePracticeList()
        setCalendarView()
        setUpObservers()

    }

    override fun onResume() {
        super.onResume()
        if (bookAppointmentViewModel.isFromEasyTeleCareWebView) {
            onBackPressed()
        }
    }

    private fun setUpClickListeners() {

        binding.bookAppointmentCardNumberET.setOnEditorActionListener { textView, imeOption, keyEvent ->

            if (imeOption == EditorInfo.IME_ACTION_NEXT) {
                inputManager.hideSoftInputFromWindow(
                    textView.windowToken,
                    InputMethodManager.HIDE_IMPLICIT_ONLY
                )
                handler.postDelayed({
                    binding.bookAppointmentCardExpMonthET.performClick()
                }, 200)
            }
            return@setOnEditorActionListener false
        }


        binding.bookAppointmentCardExpMonthET.setOnItemClickListener { position, str ->

            val month = str.toInt()

            val currentDate = bookAppointmentViewModel.appointmentConfig?.currentFacilityTimezone
                ?: DateTime.now()

            val currentMonth = currentDate.monthOfYear

            if (month < currentMonth) {
                val currentYear = currentDate.year
                binding.bookAppointmentCardExpYearET.setItems(
                    ModelUtil.createNumberList(
                        currentYear + 1, currentYear + 20
                    ), this
                )
            } else {
                val currentYear = currentDate.year
                binding.bookAppointmentCardExpYearET.setItems(
                    ModelUtil.createNumberList(
                        currentYear, currentYear + 20
                    ), this
                )
            }
        }

        binding.bookAppointmentCardExpYearET.setOnItemClickListener { position, str ->

            val selectedYear = str.toInt()
            val currentDate = bookAppointmentViewModel.appointmentConfig?.currentFacilityTimezone
                ?: DateTime.now()

            val currentYear = currentDate.year

            if (selectedYear == currentYear) {
                val currentMonth = currentDate.monthOfYear
                binding.bookAppointmentCardExpMonthET.setItems(
                    ModelUtil.createNumberList(currentMonth, 12), this
                )
            } else {
                if (binding.bookAppointmentCardExpMonthET.items.size != 12) {
                    binding.bookAppointmentCardExpMonthET.setItems(
                        ModelUtil.createNumberList(
                            1,
                            12
                        ), this
                    )
                }
            }
        }



        binding.bookAppointmentTimePreferenceET.setOnItemClickListener { position, str ->

            if (position == 0) {
                binding.bookAppointmentStartTimeLayout.visibility = View.GONE
                binding.bookAppointmentEndTimeLayout.visibility = View.GONE
            } else {
                binding.bookAppointmentStartTimeLayout.visibility = View.VISIBLE
                binding.bookAppointmentEndTimeLayout.visibility = View.VISIBLE
            }
        }

        binding.bookAppointmentVisitTypeET.setOnItemClickListener { position, str ->
            clearSlotMetaData()
            bookAppointmentViewModel.selectedVisitType =
                bookAppointmentViewModel.appointmentConfig?.visitTypeList?.get(position)
            binding.bookAppointmentTypeET.setItems(arrayListOf(), this)
            binding.bookAppointmentTypeET.setText(bookAppointmentViewModel.selectedVisitType?.appointmentMode)
            setUpSlotsLayout()

            if (bookAppointmentViewModel.appointmentConfig?.isCardProcessingEnabled == true) {
                if (bookAppointmentViewModel.appointmentConfig?.chargeType == ModelUtil.appointmentPaymentChargeTypeVisitBased) {

                    val paymentAmt =
                        bookAppointmentViewModel.selectedVisitType?.charge?.toFloatOrNull() ?: 0f

                    if (paymentAmt > 0f) {
                        binding.bookAppointmentPaymentAmtLayout.visibility = View.VISIBLE
                        val paymentStr =
                            "${bookAppointmentViewModel.appointmentConfig?.paymentCurrencySymbol ?: ""} $paymentAmt"
                        binding.bookAppointmentPaymentAmtTV.text = paymentStr.trim()
                    } else {
                        binding.bookAppointmentPaymentAmtLayout.visibility = View.GONE
                    }

                    if (paymentAmt > 0f && bookAppointmentViewModel.appointmentConfig?.paymentGateway == ModelUtil.appointmentGatewayTypeBluefin) {
                        showCardSelectionMode()
                    } else {
                        binding.bookAppointmentCardLayout.visibility = View.GONE
                        binding.bookAppointmentChooseCardLayout.visibility = View.GONE
                        binding.bookAppointmentCardSelectionToggle.visibility = View.GONE
                    }
                }
            }
        }

        binding.bookAppointmentFacilityNameET.setOnItemClickListener { position, str ->
            clearSlotMetaData()
            bookAppointmentViewModel.selectedFacility =
                bookAppointmentViewModel.facilityList[position]
            binding.bookAppointmentPaymentAmtLayout.visibility = View.GONE
            binding.bookAppointmentChooseCardLayout.visibility = View.GONE
            binding.bookAppointmentCardSelectionToggle.visibility = View.GONE
            binding.bookAppointmentCardLayout.visibility = View.GONE
            getMembersList()
        }


        binding.bookAppointmentMemberNameET.setOnItemClickListener { position, str ->
            clearSlotMetaData()
            bookAppointmentViewModel.selectedMember = bookAppointmentViewModel.memberList[position]
            getAppointmentConfig()
        }


        binding.bookAppointmentSlotMorningButton.setOnClickListener {

            binding.bookAppointmentSlotMorningButton.setTextColor(
                ContextCompat.getColor(
                    this, R.color.textColorForColorPrimaryBg
                )
            )
            binding.bookAppointmentSlotMorningButton.setBackgroundResource(R.drawable.item_book_appointment_slot_selected_button)
            binding.bookAppointmentSlotNoonButton.setTextColor(
                ContextCompat.getColor(
                    this, R.color.colorAccent
                )
            )
            binding.bookAppointmentSlotNoonButton.setBackgroundResource(R.drawable.item_book_appointment_slot_unselected_button)
            binding.bookAppointmentSlotEveningButton.setTextColor(
                ContextCompat.getColor(
                    this, R.color.colorAccent
                )
            )
            binding.bookAppointmentSlotEveningButton.setBackgroundResource(R.drawable.item_book_appointment_slot_unselected_button)

            binding.bookAppointmentScrollView.setScrollingEnable(false)

            binding.bookAppointmentSlotNoonLayout.visibility = View.GONE
            binding.bookAppointmentSlotEveningLayout.visibility = View.GONE

            binding.bookAppointmentSlotNoDataTV.visibility = View.GONE

            if (binding.bookAppointmentSlotMorningLayout.childCount == 0) {
                binding.bookAppointmentSlotMorningLayout.visibility = View.GONE
                binding.bookAppointmentSlotNoDataTV.visibility = View.VISIBLE
            } else {
                binding.bookAppointmentSlotMorningLayout.visibility = View.VISIBLE
                binding.bookAppointmentSlotNoDataTV.visibility = View.GONE
            }


            binding.bookAppointmentScrollView.setScrollingEnable(true)

        }

        binding.bookAppointmentSlotNoonButton.setOnClickListener {

            binding.bookAppointmentSlotMorningButton.setTextColor(
                ContextCompat.getColor(
                    this, R.color.colorAccent
                )
            )
            binding.bookAppointmentSlotMorningButton.setBackgroundResource(R.drawable.item_book_appointment_slot_unselected_button)
            binding.bookAppointmentSlotNoonButton.setTextColor(
                ContextCompat.getColor(
                    this, R.color.textColorForColorPrimaryBg
                )
            )
            binding.bookAppointmentSlotNoonButton.setBackgroundResource(R.drawable.item_book_appointment_slot_selected_button)
            binding.bookAppointmentSlotEveningButton.setTextColor(
                ContextCompat.getColor(
                    this, R.color.colorAccent
                )
            )
            binding.bookAppointmentSlotEveningButton.setBackgroundResource(R.drawable.item_book_appointment_slot_unselected_button)

            binding.bookAppointmentScrollView.setScrollingEnable(false)

            binding.bookAppointmentSlotMorningLayout.visibility = View.GONE
            binding.bookAppointmentSlotEveningLayout.visibility = View.GONE

            binding.bookAppointmentSlotNoDataTV.visibility = View.GONE

            if (binding.bookAppointmentSlotNoonLayout.childCount == 0) {
                binding.bookAppointmentSlotNoonLayout.visibility = View.GONE
                binding.bookAppointmentSlotNoDataTV.visibility = View.VISIBLE
            } else {
                binding.bookAppointmentSlotNoonLayout.visibility = View.VISIBLE
                binding.bookAppointmentSlotNoDataTV.visibility = View.GONE
            }

            binding.bookAppointmentScrollView.setScrollingEnable(true)

        }


        binding.bookAppointmentSlotEveningButton.setOnClickListener {

            binding.bookAppointmentSlotMorningButton.setTextColor(
                ContextCompat.getColor(
                    this, R.color.colorAccent
                )
            )
            binding.bookAppointmentSlotMorningButton.setBackgroundResource(R.drawable.item_book_appointment_slot_unselected_button)
            binding.bookAppointmentSlotNoonButton.setTextColor(
                ContextCompat.getColor(
                    this, R.color.colorAccent
                )
            )
            binding.bookAppointmentSlotNoonButton.setBackgroundResource(R.drawable.item_book_appointment_slot_unselected_button)
            binding.bookAppointmentSlotEveningButton.setTextColor(
                ContextCompat.getColor(
                    this, R.color.textColorForColorPrimaryBg
                )
            )
            binding.bookAppointmentSlotEveningButton.setBackgroundResource(R.drawable.item_book_appointment_slot_selected_button)
            binding.bookAppointmentScrollView.setScrollingEnable(false)

            binding.bookAppointmentSlotNoonLayout.visibility = View.GONE
            binding.bookAppointmentSlotMorningLayout.visibility = View.GONE

            binding.bookAppointmentSlotNoDataTV.visibility = View.GONE

            if (binding.bookAppointmentSlotEveningLayout.childCount == 0) {
                binding.bookAppointmentSlotEveningLayout.visibility = View.GONE
                binding.bookAppointmentSlotNoDataTV.visibility = View.VISIBLE
            } else {
                binding.bookAppointmentSlotEveningLayout.visibility = View.VISIBLE
                binding.bookAppointmentSlotNoDataTV.visibility = View.GONE
            }

            binding.bookAppointmentScrollView.setScrollingEnable(true)

        }

        /*  bookAppointmentCalendarView.onDateSelectedListener = object : HorizontalCalendarLayout.OnDateSelectedListener {
              override fun onDateSelected(
                      selectedDateTime: DateTime,
                      index: Int,
                      changeDataSet: Boolean
              ) {

              }
          }*/

        binding.bookAppointmentBtn.setOnClickListener {
            bookAppointmentAction()
        }

        binding.bookAppointmentBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.bookAppointmentErrorPage.errorPageTryAgainBtn.setOnClickListener {

            when (bookAppointmentViewModel.errorInAPI) {
                bookAppointmentFacilityFetchCode -> {
                    getFacilityList(true)
                }

                bookAppointmentFacilityFetchFromPracticeCode -> {
                    getFacilityList()
                }

                bookAppointmentMemberFetchCode -> {
                    getMembersList()
                }

                bookAppointmentAppointmentConfigFetchCode -> {
                    getAppointmentConfig()
                }

                bookAppointmentSlotsFetchCode -> {
                    getSlots(bookAppointmentViewModel.selectedDate)
                }

                else -> {
                    hideErrorPage()
                }
            }
        }

        setCalendarClickListeners()

    }

    private fun bookAppointmentAction() {
        val appointmentRequest = validateAppointment()

        if (appointmentRequest != null) {

            bookAppointmentViewModel.appointmentRequest = appointmentRequest

            if (bookAppointmentViewModel.appointmentConfig?.isCardProcessingEnabled == true && appointmentRequest.paymentInfo != null) {

                bookAppointmentViewModel.paymentTransaction =
                    appointmentRequest.paymentInfo?.transactionInfo
                createPaymentTransaction()
            } else {
                addAppointment()
            }
        }
    }

//    private fun daysOfWeekFromLocale(): Array<DayOfWeek> {
//        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
//        var daysOfWeek = DayOfWeek.values()
//        // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
//        // Only necessary if firstDayOfWeek != DayOfWeek.MONDAY which has ordinal 0.
//        if (firstDayOfWeek != DayOfWeek.MONDAY) {
//            val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
//            val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
//            daysOfWeek = rhs + lhs
//        }
//        return daysOfWeek
//    }

    private fun updatePracticeList() {
        bookAppointmentViewModel.practiceList = ArrayList(
            ModelUtil.getPHREntryEnabledPractices(
                authManager, ModelUtil.appointmentModelId
            )
        )

        if (bookAppointmentViewModel.practiceList.size == 1) {
            binding.bookAppointmentPracticeNameLayout.visibility = View.GONE
            bookAppointmentViewModel.selectedPractice =
                bookAppointmentViewModel.practiceList.first()
            if (!bookAppointmentViewModel.selectedPractice.easyCareURL.isNullOrBlank()) {
                intentToEasyTeleCareWebView()
            } else {
                getFacilityList(true)
            }
        } else {
            binding.bookAppointmentProgress.root.visibility = View.GONE
            binding.bookAppointmentPracticeNameLayout.visibility = View.VISIBLE
            val practiceListStr =
                bookAppointmentViewModel.practiceList.mapNotNull { practice -> practice.practiceName }
            binding.bookAppointmentPracticeNameET.setItems(practiceListStr, this)
            resetCompleteData(R.string.book_appointment_practice_select_info)
            binding.bookAppointmentPracticeNameET.setOnItemClickListener { position, str ->
                bookAppointmentViewModel.selectedPractice =
                    bookAppointmentViewModel.practiceList[position]
                if (!bookAppointmentViewModel.selectedPractice.easyCareURL.isNullOrBlank()) {
                    intentToEasyTeleCareWebView()
                } else {
                    bookAppointmentViewModel.selectedFacility = Facility()
                    bookAppointmentViewModel.facilityList.clear()
                    binding.bookAppointmentFacilityNameET.setText("")
                    getFacilityList()
                }
            }
        }
    }

    private fun intentToEasyTeleCareWebView() {
        val intent = Intent(this, EasyTeleBookAppointmentActivity::class.java)
        bookAppointmentViewModel.isFromEasyTeleCareWebView = true
        intent.putExtra(
            ModelUtil.appointmentEasyTeleCare,
            bookAppointmentViewModel.selectedPractice.easyCareURL
        )
        intent.putExtra(ModelUtil.webviewHeader, getString(R.string.book_appointment_str))
        intent.putExtra(
            ModelUtil.appointmentIsFromDashboardIntent,
            bookAppointmentViewModel.isFromDashboard
        )
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun setChooseCardItems() {
        val listOfCard = bookAppointmentViewModel.cardOnFileList.map {
            getCardConCatCardDetail(it)
        }
        binding.bookAppointmentChooseCardET.setItems(listOfCard, this)
        binding.bookAppointmentChooseCardET.setOnItemClickListener { position, str ->
            bookAppointmentViewModel.selectedCardDetail =
                bookAppointmentViewModel.cardOnFileList[position]
        }
        binding.bookAppointmentCardLayout.visibility = View.GONE
        binding.bookAppointmentChooseCardLayout.visibility = View.VISIBLE
        binding.bookAppointmentCardSelectionToggle.visibility = View.VISIBLE
        binding.bookAppointmentCardSelectionToggle.check(R.id.bookAppointmentCardOnFile)
        binding.bookAppointmentCardSelectionToggle.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.bookAppointmentCardOnFile -> {
                        binding.bookAppointmentChooseCardLayout.visibility = View.VISIBLE
                        binding.bookAppointmentCardLayout.visibility = View.GONE
                    }

                    R.id.bookAppointmentNewCard -> {
                        resetCardData()
                        binding.bookAppointmentChooseCardLayout.visibility = View.GONE
                        binding.bookAppointmentCardLayout.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun getCardConCatCardDetail(paymentGatewayToken: PaymentGatewayToken): String {
        return getString(
            R.string.choose_card_detail,
            paymentGatewayToken.cardDigit,
            paymentGatewayToken.expiry,
            paymentGatewayToken.cardBrand
        )
    }

    private fun setUpObservers() {
        bookAppointmentViewModel.memberListLiveResponse.observe(this) { resp ->
            if (resp != null) {
                if (resp is ArrayList<*>) {

                    if (resp.isNotEmpty()) {

                        hideErrorPage()
                        val memberListStr = bookAppointmentViewModel.memberList.map { member ->
                            UIUtil.formMemberName(member)
                        }
                        binding.bookAppointmentMemberNameET.setItems(memberListStr, this)
                        binding.bookAppointmentMemberNameET.setText("")

                        bookAppointmentViewModel.selectedMember = Member()
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                        if (binding.bookAppointmentProgress.root.visibility == View.VISIBLE) {
                            binding.bookAppointmentProgress.root.visibility = View.GONE
                        }

                        binding.bookAppointmentMainLayout.visibility = View.VISIBLE
                        binding.bookAppointmentMemberNameLayout.visibility = View.VISIBLE
                        resetAppointmentData(R.string.book_appointment_provider_select_info)

                        /*else{
                        bookAppointmentViewModel.selectedMember = bookAppointmentViewModel.memberList.first()
                        bookAppointmentMemberNameET.setText(UIUtil.formMemberName(bookAppointmentViewModel.selectedMember))
                        getAppointmentConfig()
                    }*/
                    } else {
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                        binding.bookAppointmentMainLayout.visibility = View.VISIBLE
                        resetCompleteData(R.string.error_book_appointment_member_empty, true)
                    }

                } else if (resp is APIResponseError) {
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }

                    bookAppointmentViewModel.errorInAPI = bookAppointmentMemberFetchCode

                    binding.bookAppointmentProgress.root.visibility = View.GONE
                    binding.bookAppointmentErrorPage.root.visibility = View.VISIBLE
                }
            }

        }
    }

    /**
     * Method to fetch facility list of the selected patient.
     **/
    private fun getFacilityList(selectFacility: Boolean = false) {

        if (selectFacility) {
            binding.bookAppointmentMainLayout.visibility = View.GONE
            binding.bookAppointmentProgress.root.visibility = View.VISIBLE
        } else {
            progressDialog.show()
        }
        bookAppointmentViewModel.getFacilityList().observe(this) { resp ->
            if (resp != null) {
                if (resp is ArrayList<*>) {

                    if (resp.isNotEmpty()) {

                        hideErrorPage()

                        binding.bookAppointmentBtn.visibility = View.VISIBLE
                        bookAppointmentViewModel.facilityList = resp as ArrayList<Facility>
                        val facilityListStr =
                            bookAppointmentViewModel.facilityList.mapNotNull { facility -> facility.facilityName }
                        binding.bookAppointmentFacilityNameET.setItems(facilityListStr, this)

                        if (selectFacility) {
                            bookAppointmentViewModel.selectedFacility =
                                bookAppointmentViewModel.facilityList.first()
                            binding.bookAppointmentFacilityNameET.setText(bookAppointmentViewModel.selectedFacility.facilityName)
                            getMembersList(true)
                        } else {
                            progressDialog.dismiss()
                            binding.bookAppointmentProgress.root.visibility = View.GONE
                            binding.bookAppointmentMainLayout.visibility = View.VISIBLE
                            binding.bookAppointmentFacilityNameLayout.visibility = View.VISIBLE
                            resetAppointmentData(R.string.book_appointment_facility_select_info)
                            binding.bookAppointmentMemberNameLayout.visibility = View.GONE
                        }
                    } else {

                        binding.bookAppointmentBtn.visibility = View.GONE
                        if (progressDialog.isShowing) {
                            progressDialog.dismiss()
                        }
                        binding.bookAppointmentMainLayout.visibility = View.VISIBLE
                        resetCompleteData(R.string.error_book_appointment_no_facility)
                    }

                } else if (resp is APIResponseError) {
                    if (!selectFacility) {
                        progressDialog.dismiss()
                        bookAppointmentViewModel.errorInAPI =
                            bookAppointmentFacilityFetchFromPracticeCode
                    } else {
                        bookAppointmentViewModel.errorInAPI = bookAppointmentFacilityFetchCode
                    }
                    binding.bookAppointmentProgress.root.visibility = View.GONE
                    binding.bookAppointmentErrorPage.root.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * Method to fetch member list of the selected facility.
     * member list is fetched while selecting a facility.
     * Appointment data resets after calling getMemberList
     **/
    private fun getMembersList(isFirst: Boolean = false) {
        if (isFirst) {
            binding.bookAppointmentProgress.root.visibility = View.VISIBLE
            binding.bookAppointmentMainLayout.visibility = View.GONE
        } else {
            if (!progressDialog.isShowing) {
                progressDialog.show()
            }
        }

        bookAppointmentViewModel.memberList = ArrayList()
        bookAppointmentViewModel.memberListStartIndex = 0
        bookAppointmentViewModel.getMemberList()
    }

    private fun getCardOnFileForPractice(practiceList: PracticeList, merchantAccountId: String?) {
        progressDialog.setMessage(R.string.loading_data)
        progressDialog.show()
        bookAppointmentViewModel.getCardOnFileForPractice(practiceList, merchantAccountId)
            .observe(this) {
                if (it != null) {
                    progressDialog.dismiss()
                    if (it is CardOnFilePOJO) {
                        bookAppointmentViewModel.cardOnFileList.addAll(it.patientSavedCardArrayList)
                        bookAppointmentViewModel.cardOnFileList.addAll(it.practiceSavedCardArrayList)
                        bookAppointmentViewModel.cardOnFileList.removeAll { card ->
                            card.hasCardExpired
                        }
                        if (bookAppointmentViewModel.cardOnFileList.isEmpty()) {
                            binding.bookAppointmentCardSelectionToggle.visibility = View.GONE
                            resetCardData()
                        } else {
                            if (bookAppointmentViewModel.appointmentConfig?.cardProcessingType != ModelUtil.appointmentCardProcessingTypeStoreOnly) {
                                setChooseCardItems()
                            } else {
                                binding.bookAppointmentCardLayout.visibility = View.GONE
                                binding.bookAppointmentChooseCardLayout.visibility = View.GONE
                                binding.bookAppointmentCardSelectionToggle.visibility = View.GONE
                            }
                        }
                    } else if (it is APIResponseError) {
                        UIUtil.showRetryAndCancelDialog(
                            this,
                            it.errorMsg ?: getString(R.string.requestFailedTryAgain),
                            R.string.retry_str,
                            false
                        ) { isRetry ->
                            if (isRetry) {
                                getCardOnFileForPractice(practiceList, merchantAccountId)
                            } else {
                                onBackPressed()
                            }
                        }
                    }
                }
            }
    }

    /**
     * Method to reset appointment data.
     **/

    private fun resetCompleteData(@StringRes resId: Int, showFacility: Boolean = false) {

        binding.bookAppointmentProgress.root.visibility = View.GONE

        if (showFacility) {
            binding.bookAppointmentFacilityNameLayout.visibility = View.VISIBLE
        } else {
            binding.bookAppointmentFacilityNameLayout.visibility = View.GONE
        }
        binding.bookAppointmentMemberNameLayout.visibility = View.GONE
        resetAppointmentData(resId)
    }

    private fun resetAppointmentData(@StringRes resId: Int) {
        binding.bookAppointmentTypeLayout.visibility = View.GONE
        binding.bookAppointmentDateTimeLayout.visibility = View.GONE
        binding.bookAppointmentSlotLayout.visibility = View.GONE
        binding.bookAppointmentReasonLayout.visibility = View.GONE
        binding.bookAppointmentVisitTypeLayout.visibility = View.GONE

        bookAppointmentViewModel.selectedMember = Member()
        bookAppointmentViewModel.selectedVisitType = null

        bookAppointmentViewModel.selectedTimeSlotAndDate = Triple(null, null, null)

        bookAppointmentViewModel.lastSelectedSlotStr = null

        binding.bookAppointmentErrorMsgLayout.visibility = View.VISIBLE

        binding.bookAppointmentErrorMsgTV.setText(resId)

        binding.bookAppointmentTimeZoneTV.visibility = View.GONE
    }

    /**
     * Method to fetch appointment configuration of the selected facility and member.
     **/
    private fun getAppointmentConfig() {
        if (!progressDialog.isShowing && binding.bookAppointmentProgress.root.visibility != View.VISIBLE) {
            progressDialog.show()
        }
        bookAppointmentViewModel.getAppointmentConfig().observe(this) { resp ->
            if (resp != null) {

                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
                if (binding.bookAppointmentProgress.root.visibility == View.VISIBLE) {
                    binding.bookAppointmentProgress.root.visibility = View.GONE
                }

                if (resp is AppointmentConfig) {
                    hideErrorPage()
                    bookAppointmentViewModel.appointmentConfig = resp
                } else if (resp is APIResponseError) {
                    bookAppointmentViewModel.appointmentConfig = null
                    bookAppointmentViewModel.errorInAPI = bookAppointmentAppointmentConfigFetchCode
                    binding.bookAppointmentErrorPage.root.visibility = View.VISIBLE

                }
                afterAppointmentConfigFetch()
            }
        }
    }

    /**
     * Method to set appointment configuration after fetch.
     * Configurations:
     *      ~ facility timezone is shown
     *      ~ Default appointment type-> In person and Phone call is added
     *      ~ If telehealth is enabled then it is also added in appointment type
     *      ~ If online appointment is disabled , then date and time preference is shown
     *      ~ In date preference the user can select a specific date or can select any date option
     *      ~ In time preference the user can select a specific time range or can select any time.
     *      ~ If online appointment is enabled then slots is shown.
     *      ~ If visit-types are present for online appointment then they are shown
     *      ~ In visitType, If duration is asked to show then  visit type is shown along with its duration.
     *      ~ appointment dates are shown in slider with current time of the selected facility plus slotsOpeningPriorHours as starting date and current time of the selected facility plus maximum number of days online appointment can be booked.
     *      ~ Slots are fetched when date is selected and if visit-types are present then a visit-type should also be selected
     *      ~ If the slots are empty and if adding waiting list is allowed and if the member is working on that specific date then add as waiting list checkbox to be shown
     *      ~ If slots are present then the user can select any one slot.
     *      ~ If payment is enabled and gateway is bluefin card details view is shown
     *      ~ In payment if charge type is visit based then the charge from the selected visittype to be shown
     *
     **/
    private fun afterAppointmentConfigFetch() {

        bookAppointmentViewModel.selectedDate = null
        bookAppointmentViewModel.lastSelectedSlotStr = null
        val appointmentConfig = bookAppointmentViewModel.appointmentConfig
        if (appointmentConfig != null) {

            binding.bookAppointmentErrorMsgLayout.visibility = View.GONE

            binding.bookAppointmentMainLayout.visibility = View.VISIBLE
            binding.bookAppointmentErrorPage.root.visibility = View.GONE

            val appointmentTypeList = arrayListOf<String>()
            appointmentTypeList.add(ModelUtil.appointmentTypeInPerson)
            appointmentTypeList.add(ModelUtil.appointmentTypePhoneCall)


            if (appointmentConfig.facilityTimezone.isNullOrBlank()) {
                binding.bookAppointmentTimeZoneTV.visibility = View.GONE
            } else {
                binding.bookAppointmentTimeZoneTV.visibility = View.VISIBLE
                val timeZoneStr = if (appointmentConfig.showAppOnPatTimeZone) {
                    getString(
                        R.string.book_appointment_patient_time_zone_str,
                        appointmentConfig.facilityTimezone
                    )
                } else {
                    getString(
                        R.string.book_appointment_time_zone_str, appointmentConfig.facilityTimezone
                    )
                }
                binding.bookAppointmentTimeZoneTV.text =
                    UIUtil.makeTimeZoneAsMedium(this, timeZoneStr)
            }

            if (appointmentConfig.isTelehealthEnabled && BuildUtil.isVideoCallEnabled) {
                appointmentTypeList.add(ModelUtil.appointmentTypeVideoCall)
            }
            binding.bookAppointmentTypeET.setItems(appointmentTypeList, this)
            binding.bookAppointmentTypeET.setText(appointmentTypeList.first())
            binding.bookAppointmentTypeLayout.visibility = View.VISIBLE
            binding.bookAppointmentReasonLayout.visibility = View.VISIBLE
            if (appointmentConfig.isEnabled) {

                binding.bookAppointmentBtn.setText(R.string.book_appointment_book_str)

                binding.bookAppointmentDateTimeLayout.visibility = View.GONE
                binding.bookAppointmentSlotLayout.visibility = View.VISIBLE


                if (appointmentConfig.visitTypeList.isEmpty()) {
                    binding.bookAppointmentVisitTypeLayout.visibility = View.GONE
                    //get Slots API
                    setUpSlotsLayout()
                } else {
                    binding.bookAppointmentVisitTypeLayout.visibility = View.VISIBLE
                    binding.bookAppointmentVisitTypeET.setText("")
                    if (appointmentConfig.showVisitDuration) {
                        val visitTypesList =
                            appointmentConfig.visitTypeList.map { visitTypes -> "${visitTypes.visitType ?: ""} [ ${visitTypes.duration} mins ]" }
                        binding.bookAppointmentVisitTypeET.setItems(visitTypesList, this)
                    } else {
                        binding.bookAppointmentVisitTypeET.setItems(
                            appointmentConfig.visitTypeList.mapNotNull { visitTypes -> visitTypes.visitType },
                            this
                        )
                    }

                    binding.bookAppointmentCalendarView.hideView()
                    binding.bookAppointmentNavLayout.hideView()
                    binding.bookAppointmentSlotViewLayout.visibility = View.GONE
                    binding.bookAppointmentVisitTypeSelectInfoTV.visibility = View.VISIBLE
                    binding.bookAppointmentVisitTypeSelectInfoTV.setText(R.string.book_appointment_visittype_select_info)
                }
            } else {
                binding.bookAppointmentBtn.setText(R.string.book_appointment_request_str)
                binding.bookAppointmentVisitTypeLayout.visibility = View.GONE
                binding.bookAppointmentDateTimeLayout.visibility = View.VISIBLE
                binding.bookAppointmentSlotLayout.visibility = View.GONE
                val currentDate = appointmentConfig.currentFacilityTimezone

                if (currentDate != null) {
                    binding.bookAppointmentDateET.setText(currentDate.toString(ModelUtil.charmDisplayDateFormat()))

                    val minutesToAdd = currentDate.minuteOfHour % 5

                    val appointmentStartTime = if (minutesToAdd != 0) {
                        currentDate.plusMinutes(5 - minutesToAdd)
                    } else {
                        currentDate
                    }


                    binding.bookAppointmentStartTimeET.setText(
                        appointmentStartTime.toString(
                            ModelUtil.charmDisplayTimeFormat
                        )
                    )
                    binding.bookAppointmentEndTimeET.setText(
                        appointmentStartTime.plusHours(1).toString(ModelUtil.charmDisplayTimeFormat)
                    )
                }
            }

            if (appointmentConfig.isCardProcessingEnabled) {

                /*if (appointmentConfig.paymentGateway == ModelUtil.appointmentGatewayTypeBluefin) {
                    resetAppointmentData(R.string.error_book_appointment_feature_inavailability)
                }
                else {*/

                if (appointmentConfig.chargeType != ModelUtil.appointmentPaymentChargeTypeVisitBased) {
                    val paymentAmt = appointmentConfig.chargeAmount?.toFloatOrNull() ?: 0f

                    val paymentStr =
                        "${bookAppointmentViewModel.appointmentConfig?.paymentCurrencySymbol ?: ""} $paymentAmt"

                    binding.bookAppointmentPaymentAmtTV.text = paymentStr.trim()
                    if (bookAppointmentViewModel.appointmentConfig?.cardProcessingType == ModelUtil.appointmentCardProcessingTypeStoreOnly) {
                        binding.bookAppointmentPaymentAmtLayout.visibility = View.GONE
                    } else {
                        binding.bookAppointmentPaymentAmtLayout.visibility = View.VISIBLE
                    }
                } else {
                    binding.bookAppointmentPaymentAmtLayout.visibility = View.GONE
                }

                if (appointmentConfig.paymentGateway == ModelUtil.appointmentGatewayTypeRazorpay) {
                    binding.bookAppointmentCardLayout.visibility = View.GONE
                    binding.bookAppointmentChooseCardLayout.visibility = View.GONE
                    binding.bookAppointmentCardSelectionToggle.visibility = View.GONE
                } else {
                    if (appointmentConfig.chargeType != ModelUtil.appointmentPaymentChargeTypeVisitBased) {
                        showCardSelectionMode()
                    } else {
                        binding.bookAppointmentCardLayout.visibility = View.GONE
                        binding.bookAppointmentChooseCardLayout.visibility = View.GONE
                        binding.bookAppointmentCardSelectionToggle.visibility = View.GONE
                    }
                }
                //}
            } else {
                binding.bookAppointmentCardLayout.visibility = View.GONE
                binding.bookAppointmentChooseCardLayout.visibility = View.GONE
                binding.bookAppointmentCardSelectionToggle.visibility = View.GONE
                binding.bookAppointmentPaymentAmtLayout.visibility = View.GONE
            }
        } else {
            binding.bookAppointmentErrorPage.root.visibility = View.VISIBLE
            binding.bookAppointmentMainLayout.visibility = View.GONE
        }
    }

    /**
     * Method to set datepicker with min and max date and rest slot layout.
     **/
    private fun setUpSlotsLayout() {
        binding.bookAppointmentCalendarView.showView()
        binding.bookAppointmentNavLayout.showView()
        binding.bookAppointmentSlotViewLayout.visibility = View.GONE
        binding.bookAppointmentVisitTypeSelectInfoTV.visibility = View.GONE

        binding.bookAppointmentSlotMorningLayout.removeAllViews()
        binding.bookAppointmentSlotNoonLayout.removeAllViews()
        binding.bookAppointmentSlotEveningLayout.removeAllViews()
        val appointmentConfig = bookAppointmentViewModel.appointmentConfig

        if (appointmentConfig != null) {
            val facilityDateTime = appointmentConfig.currentFacilityTimezone ?: DateTime.now()

            val appointmentBookingStartDateTime =
                facilityDateTime.plusHours(appointmentConfig.slotsOpeningPriorHours ?: 0)
            val appointmentBookingEndDateTime = appointmentBookingStartDateTime.plusDays(
                appointmentConfig.onlineBookingDaysLimit ?: 0
            )

            val monthsBetween =
                Period(appointmentBookingStartDateTime, appointmentBookingEndDateTime).months + 1
//            val daysOfWeek = daysOfWeekFromLocale()
            val currentMonth = YearMonth.now()
            binding.bookAppointmentCalendarView.apply {
                setup(
                    currentMonth,
                    currentMonth.plusMonths(monthsBetween.toLong()),
                    DayOfWeek.SUNDAY
                )
                scrollToMonth(currentMonth)
            }
            bookAppointmentViewModel.selectedDate = DateTime.now().removeTime()
            getSlots(bookAppointmentViewModel.selectedDate)
        }
    }

    /**
     * Method to fetch slots.
     **/
    private fun getSlots(selectedDateForSlot: DateTime?) {
        selectedDateForSlot ?: return
        Log.d(TAG, "getSlots")
        val slotMetaData =
            bookAppointmentViewModel.slotsHashMap[selectedDateForSlot.getMonthPlusYear()]
        if (slotMetaData != null) {
            splitSlotsAndAddToItemView(slotMetaData, selectedDateForSlot)
            Log.d(TAG, "getSlots return")
            return
        }

        if (bookAppointmentViewModel.isGettingSlots) {
            return
        }
        Log.d(TAG, "getting slots...")
        progressDialog.show()
        bookAppointmentViewModel.isGettingSlots = true
        bookAppointmentViewModel.getSlots(selectedDateForSlot).observe(this) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is SlotMetaData) {
                    hideErrorPage()

                    bookAppointmentViewModel.slotsHashMap[selectedDateForSlot.getMonthPlusYear()] =
                        resp
                    splitSlotsAndAddToItemView(resp, selectedDateForSlot)

                    val yearMonth =
                        YearMonth.of(selectedDateForSlot.year, selectedDateForSlot.monthOfYear)
                    Log.d(TAG, "slots Received... yearMonth $yearMonth")
                    binding.bookAppointmentCalendarView.notifyMonthChanged(yearMonth)

                } else if (resp is APIResponseError) {
                    binding.bookAppointmentErrorPage.root.visibility = View.VISIBLE
                    bookAppointmentViewModel.errorInAPI = bookAppointmentSlotsFetchCode
                }

                bookAppointmentViewModel.isGettingSlots = false
            }
        }
    }

    /**
     * Method to split slot list based on morning , noon and evening and views.
     **/
    private fun splitSlotsAndAddToItemView(
        slotMetaData: SlotMetaData, selectedDateForSlot: DateTime
    ) {

        val slotArr: ArrayList<MemberAvailability> =
            slotMetaData.memberAvailability[selectedDateForSlot] ?: return

        val morningArr = ArrayList<MemberAvailability>()
        val noonArr = ArrayList<MemberAvailability>()
        val eveningArr = ArrayList<MemberAvailability>()

        binding.bookAppointmentSlotMorningLayout.removeAllViews()
        binding.bookAppointmentSlotNoonLayout.removeAllViews()
        binding.bookAppointmentSlotEveningLayout.removeAllViews()

        val currentDate = (bookAppointmentViewModel.selectedDate ?: DateTime.now().removeTime())
        val currentDateStr = currentDate.toString(ModelUtil.charmApiDateFormat())

        val MD_MORNING_START_TIME = DateTime.parse(
            "$currentDateStr ${ModelUtil.morningStartTimeString}",
            ModelUtil.sqlDateTimeFormatForNormalTiming()
        )
        val MD_NOON_START_TIME = DateTime.parse(
            "$currentDateStr ${ModelUtil.noonStartTimeString}",
            ModelUtil.sqlDateTimeFormatForNormalTiming()
        )
        val MD_EVENING_START_TIME = DateTime.parse(
            "$currentDateStr ${ModelUtil.eveningStartTimeString}",
            ModelUtil.sqlDateTimeFormatForNormalTiming()
        )

        val onlineAppointmentStartingTime =
            (bookAppointmentViewModel.appointmentConfig?.currentFacilityTimezone
                ?: DateTime.now()).plusHours(
                bookAppointmentViewModel.appointmentConfig?.slotsOpeningPriorHours ?: 0
            )

        slotArr.forEachIndexed { index, memberAvailability ->

            val startTime = memberAvailability.startTime

            if (startTime != null && startTime.isAfter(onlineAppointmentStartingTime)) {

                when {

                    startTime.isEqual(MD_MORNING_START_TIME) || startTime.isAfter(
                        MD_MORNING_START_TIME
                    ) && startTime.isBefore(MD_NOON_START_TIME) -> {
                        morningArr.add(memberAvailability)
                    }

                    startTime.isEqual(MD_NOON_START_TIME) || startTime.isAfter(MD_NOON_START_TIME) && startTime.isBefore(
                        MD_EVENING_START_TIME
                    ) -> {
                        noonArr.add(memberAvailability)
                    }

                    else -> {
                        eveningArr.add(memberAvailability)
                    }
                }
            }
        }

        binding.bookAppointmentSlotViewLayout.visibility = View.VISIBLE

        if (morningArr.isEmpty() && noonArr.isEmpty() && eveningArr.isEmpty()) {
            binding.bookAppointmentSlotButtonLayout.visibility = View.GONE
            binding.bookAppointmentSlotMorningLayout.visibility = View.GONE
            binding.bookAppointmentSlotNoonLayout.visibility = View.GONE
            binding.bookAppointmentSlotEveningLayout.visibility = View.GONE

            if (slotMetaData.showWaitingList && slotMetaData.memberWorkTiming.firstOrNull { currentDate.dayOfWeek == it.dayOfWeek }?.isMemberWorking == true) {
                binding.bookAppointmentSlotWaitingListLayout.visibility = View.VISIBLE
                binding.bookAppointmentSlotNoDataTV.visibility = View.GONE
                binding.bookAppointmentSlotWaitingLisCheckbox.isChecked = false
            } else {
                binding.bookAppointmentSlotWaitingListLayout.visibility = View.GONE
                binding.bookAppointmentSlotNoDataTV.visibility = View.VISIBLE
            }
            return
        } else {

            binding.bookAppointmentSlotWaitingListLayout.visibility = View.GONE
            binding.bookAppointmentSlotButtonLayout.visibility = View.VISIBLE

            val screenWidth = UIUtil.getScreenWidth(this)
            val eachSpace = resources.getDimensionPixelSize(R.dimen.book_appointment_slot_spacing)

            val flowLayoutWidth =
                screenWidth - (2 * resources.getDimensionPixelSize(R.dimen.edittext_margin_horizontal)) - (2 * eachSpace)
            val totalNumOfColumns = getNumOfColumns(screenWidth)
            val totalWidthForButton = flowLayoutWidth - ((totalNumOfColumns - 1) * eachSpace)
            val eachButtonWidth = totalWidthForButton / totalNumOfColumns

            Log.d(
                "TimeSlotFragment",
                "screenWidth -> $screenWidth \nflowLayoutWidth -> $flowLayoutWidth \nsingleBtnWidth -> $eachButtonWidth \ntotalNumOfColumns -> $totalNumOfColumns \ntotalWidthForButton -> $totalWidthForButton \neachSpace -> $eachSpace"
            )

            var isSlotHeadingSelected = false
            if (morningArr.isNotEmpty()) {
                isSlotHeadingSelected = true

                morningArr.forEachIndexed { index, memberAvailability ->
                    renderAndAddSlotViewWithClickListener(
                        memberAvailability, index, ModelUtil.morning
                    )
                }

                binding.bookAppointmentSlotMorningButton.performClick()
                binding.bookAppointmentSlotMorningLayout.childSpacing = eachSpace

                val childCount = binding.bookAppointmentSlotMorningLayout.childCount

                for (index in 0 until childCount) {
                    val child = binding.bookAppointmentSlotMorningLayout.getChildAt(index)

                    if (child != null) {
                        val params = child.layoutParams
                        params.width = eachButtonWidth
                        child.layoutParams = params
                    }
                }

            }

            if (noonArr.isNotEmpty()) {

                noonArr.forEachIndexed { index, memberAvailability ->
                    renderAndAddSlotViewWithClickListener(memberAvailability, index, ModelUtil.noon)
                }

                if (!isSlotHeadingSelected) {
                    isSlotHeadingSelected = true
                    binding.bookAppointmentSlotNoonButton.performClick()
                }

                binding.bookAppointmentSlotNoonLayout.childSpacing = eachSpace
                val childCount = binding.bookAppointmentSlotNoonLayout.childCount

                for (index in 0 until childCount) {
                    val child = binding.bookAppointmentSlotNoonLayout.getChildAt(index)

                    if (child != null) {
                        val params = child.layoutParams
                        params.width = eachButtonWidth
                        child.layoutParams = params
                    }
                }
            }

            if (eveningArr.isNotEmpty()) {

                eveningArr.forEachIndexed { index, memberAvailability ->
                    renderAndAddSlotViewWithClickListener(
                        memberAvailability, index, ModelUtil.evening
                    )
                }

                if (!isSlotHeadingSelected) {
                    binding.bookAppointmentSlotEveningButton.performClick()
                }

                binding.bookAppointmentSlotEveningLayout.childSpacing = eachSpace
                val childCount = binding.bookAppointmentSlotEveningLayout.childCount

                for (index in 0 until childCount) {
                    val child = binding.bookAppointmentSlotEveningLayout.getChildAt(index)

                    if (child != null) {
                        val params = child.layoutParams
                        params.width = eachButtonWidth
                        child.layoutParams = params
                    }
                }
            }

        }

    }

    private fun getNumOfColumns(screenWidth: Int): Int {

        return when {
            screenWidth <= 720 -> {
                2
            }

            screenWidth <= 1200 -> {
                3
            }

            screenWidth <= 1600 -> {
                4
            }

            screenWidth <= 2000 -> {
                5
            }

            else -> {
                6
            }
        }
    }

    /**
     * Method to add slots in its respective layout.
     **/
    private fun renderAndAddSlotViewWithClickListener(
        memberAvailability: MemberAvailability, index: Int, session: Int
    ) {

        val startTime = memberAvailability.startTime

        if (startTime != null) {

            val startTimeStr = startTime.toString(ModelUtil.charmDisplayTimeFormat).trim()
            if (startTimeStr.isNotBlank()) {

                val slotViewBinding = ItemTimeSlotBinding.inflate(layoutInflater)

                slotViewBinding.appointmentSlotTv.text = startTimeStr
                slotViewBinding.appointmentSlotTv.tag = index + 1 //inorder to avoid 0 as tag

                slotViewBinding.appointmentSlotTv.setOnClickListener {

                    val tempTriple = bookAppointmentViewModel.selectedTimeSlotAndDate
                    val slotTag = slotViewBinding.appointmentSlotTv.tag
                    if (slotTag != null) {

                        var isSlotAlreadySelected = false
                        val alreadySelectedPosTag = tempTriple.first
                        if (alreadySelectedPosTag != null) {

                            //removing selection
                            val alreadySelectedView = when (tempTriple.second) { //selected session
                                ModelUtil.morning -> {
                                    binding.bookAppointmentSlotMorningLayout.findViewWithTag<View>(
                                        alreadySelectedPosTag
                                    )
                                }

                                ModelUtil.noon -> {
                                    binding.bookAppointmentSlotNoonLayout.findViewWithTag<View>(
                                        alreadySelectedPosTag
                                    )
                                }

                                else -> {
                                    binding.bookAppointmentSlotEveningLayout.findViewWithTag<View>(
                                        alreadySelectedPosTag
                                    )
                                }
                            }
                            if (alreadySelectedView != null) {

                                if (slotViewBinding.appointmentSlotTv == alreadySelectedView) { //TODO:Check
                                    isSlotAlreadySelected = true
                                }

                                val appointmentSlotTv =
                                    alreadySelectedView.findViewById<Button>(R.id.appointmentSlotTv)
                                appointmentSlotTv?.setTextColor(
                                    ContextCompat.getColor(
                                        this, android.R.color.black
                                    )
                                )
                                appointmentSlotTv?.setBackgroundResource(R.drawable.item_book_appointment_unselected_slot)
                            }

                        }

                        if (!isSlotAlreadySelected) { //when no slot is selected

                            if (binding.bookAppointmentBtn.visibility != View.VISIBLE) {
                                binding.bookAppointmentBtn.visibility = View.VISIBLE/*
                                                                setMaxHeightForScrollView(true)
                                */
                            }

                            slotViewBinding.appointmentSlotTv.setBackgroundResource(R.drawable.item_book_appointment_slot_selected_button)
                            slotViewBinding.appointmentSlotTv.setTextColor(
                                ContextCompat.getColor(
                                    this, R.color.textColorForColorPrimaryBg
                                )
                            )
                            bookAppointmentViewModel.selectedTimeSlotAndDate =
                                Triple(slotTag as Int, session, tempTriple.third)
                            bookAppointmentViewModel.lastSelectedSlotStr =
                                slotViewBinding.appointmentSlotTv.text.toString()
                            binding.bookAppointmentSelectedSlotDateCV.visibility = View.VISIBLE
                            var dayOfSelectedDate =
                                DayOfWeek.of(bookAppointmentViewModel.selectedDate?.dayOfWeek ?: 1)
                                    .toString()
                            dayOfSelectedDate = dayOfSelectedDate[0] + dayOfSelectedDate.lowercase()
                                .substring(1, dayOfSelectedDate.length)
                            val dateTimeStr =
                                dayOfSelectedDate + ", " + bookAppointmentViewModel.selectedDate?.toString(
                                    ModelUtil.charmDisplayDateFormat()
                                ) + " " + slotViewBinding.appointmentSlotTv.text.toString()
                            binding.bookAppointmentSelectedSlotDate.setText(dateTimeStr)
                        } else {

                            bookAppointmentViewModel.selectedTimeSlotAndDate =
                                Triple(null, null, tempTriple.third)
                        }

                    }

                }

                when (session) {

                    ModelUtil.morning -> {
                        binding.bookAppointmentSlotMorningLayout.addView(slotViewBinding.root)
                    }

                    ModelUtil.noon -> {
                        binding.bookAppointmentSlotNoonLayout.addView(slotViewBinding.root)
                    }

                    ModelUtil.evening -> {
                        binding.bookAppointmentSlotEveningLayout.addView(slotViewBinding.root)
                    }
                }

            }
        }

    }

    /**
     * Method to book/request an appointment.
     **/
    private fun addAppointment() {

        val appointmentRequest = bookAppointmentViewModel.appointmentRequest

        if (!progressDialog.isShowing) {
            progressDialog.show()
        }
        bookAppointmentViewModel.bookAppointment(appointmentRequest).observe(this) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is String) {

                    val respIntent = intent

                    var stringRes: Int

                    if (appointmentRequest.isEnabled) {
                        if (appointmentRequest.addAsWaitingList) {
                            respIntent.putExtra(
                                ModelUtil.appointmentCode, ModelUtil.appointmentWaitingList
                            )
                            stringRes = R.string.book_appointment_waitlist_success_txt
                        } else {
                            if (resp.contains("requested", true)) {
                                respIntent.putExtra(
                                    ModelUtil.appointmentCode, ModelUtil.appointmentRequested
                                )
                                stringRes = R.string.book_appointment_request_success_txt
                            } else {
                                respIntent.putExtra(
                                    ModelUtil.appointmentCode, ModelUtil.appointmentUpcoming
                                )
                                stringRes = R.string.book_appointment_book_success_txt
                            }
                        }
                    } else {
                        if (resp.contains("requested", true)) {
                            respIntent.putExtra(
                                ModelUtil.appointmentCode, ModelUtil.appointmentRequested
                            )
                            stringRes = R.string.book_appointment_request_success_txt
                        } else {
                            respIntent.putExtra(
                                ModelUtil.appointmentCode, ModelUtil.appointmentUpcoming
                            )
                            stringRes = R.string.book_appointment_waitlist_success_txt
                        }
                    }

                    UIUtil.showToast(
                        this,
                        getString(R.string.book_appointment_success_msg, getString(stringRes)),
                        Toast.LENGTH_LONG
                    )

                    setResult(Activity.RESULT_OK, respIntent)
                    finish()

                } else if (resp is APIResponseError) {
                    if (appointmentRequest.isEnabled) {
                        if (appointmentRequest.addAsWaitingList) {
                            UIUtil.showToast(
                                this,
                                R.string.error_book_appointment_waitlist_failure,
                                Toast.LENGTH_LONG
                            )
                        } else {
                            UIUtil.showToast(
                                this, R.string.error_book_appointment_failure, Toast.LENGTH_LONG
                            )
                        }
                    } else {
                        UIUtil.showToast(
                            this, R.string.error_book_appointment_request_failure, Toast.LENGTH_LONG
                        )
                    }
                }
            }
        }
    }

    private fun showCardSelectionMode() {
        bookAppointmentViewModel.cardOnFileList.clear()
        binding.bookAppointmentChooseCardET.setText("")
        getCardOnFileForPractice(
            bookAppointmentViewModel.selectedPractice,
            bookAppointmentViewModel.appointmentConfig?.merchantAccountId
        )
    }

    /**
     * Method to reset card data.
     **/
    private fun resetCardData() {
        binding.bookAppointmentCardLayout.visibility = View.VISIBLE
        binding.bookAppointmentChooseCardLayout.visibility = View.GONE
        binding.bookAppointmentCardNumberET.setText("")
        binding.bookAppointmentCardNameET.setText("")
        binding.bookAppointmentCardExpMonthET.setText("")
        binding.bookAppointmentCardExpYearET.setText("")
        binding.bookAppointmentCardCvvET.setText("")

        binding.bookAppointmentCardExpMonthET.setItems(
            ModelUtil.createNumberList(endNumber = 12),
            this
        )

        val currentDate =
            bookAppointmentViewModel.appointmentConfig?.currentFacilityTimezone ?: DateTime.now()
        val currentYear = currentDate.year

        binding.bookAppointmentCardExpYearET.setItems(
            ModelUtil.createNumberList(
                currentYear, currentYear + 20
            ), this
        )
    }

    /**
     * Method to validate appointment data whether it is ready to get book/request.
     **/
    private fun validateAppointment(): AppointmentRequest? {

        if (binding.bookAppointmentPracticeNameLayout.visibility == View.VISIBLE) {
            if (binding.bookAppointmentPracticeNameET.text.toString().isBlank()) {
                UIUtil.showToast(
                    this, (R.string.error_book_appointment_empty_facility), Toast.LENGTH_LONG
                )
                return null
            }
        }

        if (bookAppointmentViewModel.facilityList.isEmpty()) {
            UIUtil.showToast(this, (R.string.error_book_appointment_no_facility), Toast.LENGTH_LONG)
            return null
        }

        if (binding.bookAppointmentFacilityNameET.text.toString().isBlank()) {
            UIUtil.showToast(
                this, (R.string.error_book_appointment_empty_facility), Toast.LENGTH_LONG
            )
            return null
        }

        if (bookAppointmentViewModel.memberList.isEmpty()) {
            UIUtil.showToast(
                this, (R.string.error_book_appointment_member_empty), Toast.LENGTH_LONG
            )
            return null
        }

        if (binding.bookAppointmentMemberNameET.text.toString().isBlank()) {
            UIUtil.showToast(
                this, getString(R.string.error_book_appointment_empty_provider), Toast.LENGTH_LONG
            )
            return null
        }

        val appointmentConfig = bookAppointmentViewModel.appointmentConfig

        if (appointmentConfig != null) {

            /*if (appointmentConfig.isCardProcessingEnabled && appointmentConfig.paymentGateway == ModelUtil.appointmentGatewayTypeBluefin) {
                UIUtil.showToast(this, getString(R.string.error_book_appointment_feature_inavailability), Toast.LENGTH_LONG)
                return null
            }*/
            if (appointmentConfig.isEnabled) {

                if (appointmentConfig.visitTypeList.isNotEmpty()) {
                    if (binding.bookAppointmentVisitTypeET.text.toString().isBlank()) {
                        UIUtil.showToast(
                            this,
                            getString(R.string.error_book_appointment_empty_visit_type),
                            Toast.LENGTH_LONG
                        )
                        return null
                    }

                    if (bookAppointmentViewModel.selectedVisitType?.appointmentMode == ModelUtil.appointmentTypeVideoCall && bookAppointmentViewModel.appointmentConfig?.isTelehealthEnabled != true) {
                        UIUtil.showToast(
                            this,
                            getString(R.string.error_book_appointment_telehealth_not_enabled),
                            Toast.LENGTH_LONG
                        )
                        return null
                    }
                }
                if (bookAppointmentViewModel.selectedDate == null) {
                    UIUtil.showToast(
                        this,
                        getString(R.string.error_book_appointment_empty_date),
                        Toast.LENGTH_LONG
                    )
                    return null
                }

                if (binding.bookAppointmentSlotWaitingListLayout.visibility == View.VISIBLE) {

                    if (!binding.bookAppointmentSlotWaitingLisCheckbox.isChecked) {
                        UIUtil.showToast(
                            this,
                            getString(R.string.error_book_appointment_uncheck_waiting_list),
                            Toast.LENGTH_LONG
                        )
                        return null
                    }
                } else {

                    if (bookAppointmentViewModel.lastSelectedSlotStr == null) {
                        UIUtil.showToast(
                            this,
                            getString(R.string.error_book_appointment_empty_slot),
                            Toast.LENGTH_LONG
                        )
                        return null
                    }
                }
            } else {

                if (binding.bookAppointmentTimePreferenceET.text.toString() == getString(R.string.book_appointment_time_pref_range_str)) {
                    val startTime = DateTime.parse(
                        binding.bookAppointmentStartTimeET.text.toString(),
                        ModelUtil.charmDisplayTimeFormat()
                    )
                    val endTime = DateTime.parse(
                        binding.bookAppointmentEndTimeET.text.toString(),
                        ModelUtil.charmDisplayTimeFormat()
                    )

                    if (startTime.isAfter(endTime)) {
                        UIUtil.showToast(
                            this, R.string.error_book_appointment_incorrect_time, Toast.LENGTH_LONG
                        )
                        return null
                    }
                }
            }

            val reason = binding.bookAppointmentReasonET.text.toString()

            if (reason.isBlank()) {
                UIUtil.showToast(
                    this, R.string.error_book_appointment_empty_reason, Toast.LENGTH_LONG
                )
                return null
            }

            if (!ModelUtil.hasAlpha(reason)) {
                UIUtil.showToast(
                    this, R.string.error_book_appointment_invalid_reason, Toast.LENGTH_LONG
                )
                return null
            }

            if (appointmentConfig.isCardProcessingEnabled && binding.bookAppointmentChooseCardLayout.visibility == View.VISIBLE) {
                if (binding.bookAppointmentChooseCardET.text.toString().isBlank()) {
                    UIUtil.showToast(
                        this, R.string.error_book_appointment_empty_card, Toast.LENGTH_LONG
                    )
                    return null
                }
            }

            if (appointmentConfig.isCardProcessingEnabled && binding.bookAppointmentCardLayout.visibility == View.VISIBLE) {

                if (binding.bookAppointmentCardNameET.text.toString().isBlank()) {
                    UIUtil.showToast(
                        this,
                        R.string.error_book_appointment_empty_card_holder_name,
                        Toast.LENGTH_LONG
                    )
                    return null
                }

                if (binding.bookAppointmentCardNumberET.text.toString().isBlank()) {
                    UIUtil.showToast(
                        this, R.string.error_book_appointment_empty_card_number, Toast.LENGTH_LONG
                    )
                    return null
                }

//                if (bookAppointmentCardNumberET.text.toString().length != 19) {
//                    UIUtil.showToast(
//                        this,
//                        R.string.error_book_appointment_invalid_card_number,
//                        Toast.LENGTH_LONG
//                    )
//                    return null
//                }

                if (binding.bookAppointmentCardExpMonthET.text.toString().isBlank()) {
                    UIUtil.showToast(
                        this,
                        R.string.error_book_appointment_empty_card_expiry_month,
                        Toast.LENGTH_LONG
                    )
                    return null
                }

                if (binding.bookAppointmentCardExpYearET.text.toString().isBlank()) {
                    UIUtil.showToast(
                        this,
                        R.string.error_book_appointment_empty_card_expiry_year,
                        Toast.LENGTH_LONG
                    )
                    return null
                }

                if (binding.bookAppointmentCardCvvET.text.toString().isBlank()) {
                    UIUtil.showToast(
                        this, R.string.error_book_appointment_empty_card_cvv, Toast.LENGTH_LONG
                    )
                    return null
                }

                if (binding.bookAppointmentCardCvvET.text.toString().length != 3) {
                    UIUtil.showToast(
                        this, R.string.error_book_appointment_invalid_card_cvv, Toast.LENGTH_LONG
                    )
                    return null
                }
            }

            val appointmentRequest = AppointmentRequest()
            appointmentRequest.member = bookAppointmentViewModel.selectedMember
            appointmentRequest.facility = bookAppointmentViewModel.selectedFacility
            appointmentRequest.reason = binding.bookAppointmentReasonET.text.toString()
            appointmentRequest.appointmentType = binding.bookAppointmentTypeET.text.toString()
            appointmentRequest.isEnabled = appointmentConfig.isEnabled

            val practiceId = appointmentRequest.facility?.practiceId ?: ""
            appointmentRequest.practiceId = practiceId

            if (appointmentConfig.isEnabled) {
                appointmentRequest.datePreference = ModelUtil.appointmentDatePreferenceSpecificDate
                appointmentRequest.dateChoice1 = bookAppointmentViewModel.selectedDate

                if (binding.bookAppointmentSlotWaitingListLayout.visibility == View.VISIBLE) {
                    appointmentRequest.addAsWaitingList = true
                } else {
                    appointmentRequest.addAsWaitingList = false
                    val lastSelectedSlot = bookAppointmentViewModel.lastSelectedSlotStr ?: ""

                    val lastSelectedSlotArr = lastSelectedSlot.split(" ")


                    appointmentRequest.timePreference = ModelUtil.appointmentTimePreferenceTimeRange
                    appointmentRequest.startSession = lastSelectedSlotArr[1].uppercase()

                    val lastSelectedSlotDayMinArr = lastSelectedSlotArr[0].split(":")
                    appointmentRequest.startHour = lastSelectedSlotDayMinArr[0]
                    appointmentRequest.startMinutes = lastSelectedSlotDayMinArr[1]
                }

                if (appointmentConfig.visitTypeList.isNotEmpty()) {
                    appointmentRequest.visitType = bookAppointmentViewModel.selectedVisitType
                }
            } else {
                appointmentRequest.reminderType = "Mail"

                appointmentRequest.addAsWaitingList = false

                val datePreference = binding.bookAppointmentDatePreferenceET.text.toString()

                if (datePreference == getString(R.string.book_appointment_date_pref_any_str)) {
                    appointmentRequest.datePreference = ModelUtil.appointmentDatePreferenceAnyDate
                } else {
                    appointmentRequest.datePreference =
                        ModelUtil.appointmentDatePreferenceSpecificDate
                    appointmentRequest.dateChoice1 = DateTime.parse(
                        binding.bookAppointmentDateET.text.toString(),
                        ModelUtil.charmDisplayDateFormat()
                    )
                }

                val timePreference = binding.bookAppointmentTimePreferenceET.text.toString()

                if (timePreference == getString(R.string.book_appointment_time_pref_any_str)) {
                    appointmentRequest.timePreference = ModelUtil.appointmentTimePreferenceAnyTime
                } else {
                    appointmentRequest.timePreference = ModelUtil.appointmentTimePreferenceTimeRange

                    val startTimeStr = binding.bookAppointmentStartTimeET.text.toString()
                    val startTimeArr = startTimeStr.split(" ")

                    appointmentRequest.startSession = startTimeArr[1].uppercase()

                    val startTimeDayMinArr = startTimeArr[0].split(":")
                    appointmentRequest.startHour = startTimeDayMinArr[0]
                    appointmentRequest.startMinutes = startTimeDayMinArr[1]

                    val endTimeStr = binding.bookAppointmentEndTimeET.text.toString()
                    val endTimeArr = endTimeStr.split(" ")

                    appointmentRequest.endSession = endTimeArr[1].uppercase()

                    val endTimeDayMinArr = endTimeArr[0].split(":")
                    appointmentRequest.endHour = endTimeDayMinArr[0]
                    appointmentRequest.endMinutes = endTimeDayMinArr[1]

                }

            }

            val paymentInfo = PaymentInfo()

            if (binding.bookAppointmentCardLayout.visibility == View.VISIBLE) {

                paymentInfo.cardHolderName =
                    binding.bookAppointmentCardNameET.text.toString().trim()

                val cardNumber = binding.bookAppointmentCardNumberET.text.toString().split(" ")
                    .joinToString(separator = "")

                paymentInfo.cardNumber = cardNumber
                paymentInfo.cardCvv = binding.bookAppointmentCardCvvET.text.toString()
                paymentInfo.cardExpMonth = binding.bookAppointmentCardExpMonthET.text.toString()
                paymentInfo.cardExpYear = binding.bookAppointmentCardExpYearET.text.toString()
                paymentInfo.cardNickName = binding.bookAppointmentCardNickNameET.text.toString()
                paymentInfo.chargeProcessingType = appointmentConfig.cardProcessingType

            }

            if ((binding.bookAppointmentCardLayout.visibility == View.VISIBLE) || (binding.bookAppointmentChooseCardLayout.visibility == View.VISIBLE) || (appointmentConfig.isCardProcessingEnabled && (((appointmentConfig.chargeAmount != null) && ((appointmentConfig.chargeAmount!!.toFloatOrNull()
                    ?: 0f) > 0f) && (appointmentConfig.chargeType != ModelUtil.appointmentPaymentChargeTypeVisitBased)) || ((appointmentConfig.chargeType == ModelUtil.appointmentPaymentChargeTypeVisitBased) && ((appointmentRequest.visitType?.charge?.toFloatOrNull()
                    ?: 0f) > 0f))) && (appointmentConfig.paymentGateway == ModelUtil.appointmentGatewayTypeRazorpay))
            ) {

                if (appointmentConfig.chargeType == ModelUtil.appointmentPaymentChargeTypeVisitBased) {
                    paymentInfo.chargeAmount = appointmentRequest.visitType?.charge
                } else {
                    paymentInfo.chargeAmount = appointmentConfig.chargeAmount
                }

                val transactionInfo = TransactionInfo()
                transactionInfo.facilityId = appointmentRequest.facility?.facilityId
                transactionInfo.practiceId = appointmentRequest.practiceId
                transactionInfo.memberId = appointmentRequest.member?.memberId
                transactionInfo.merchantAccountId = appointmentConfig.merchantAccountId
                transactionInfo.service = ModelUtil.paymentServiceAppointment
                transactionInfo.paymentAmount = paymentInfo.chargeAmount

                //Code added for card on file
                if (binding.bookAppointmentChooseCardLayout.visibility == View.VISIBLE) {
                    paymentInfo.token = bookAppointmentViewModel.selectedCardDetail?.token
                }

                paymentInfo.transactionInfo = transactionInfo

                appointmentRequest.paymentInfo = paymentInfo
            }

            return appointmentRequest
        }


        return null
    }

    /**
     * Method to show datepicker for the given edittext.
     **/
    private fun showDatePicker(edittext: MDTextInputEdittext) {

        val value = edittext.text.toString().trim()

        val localDate = if (value.isNotBlank()) {
            LocalDate.parse(value, ModelUtil.charmDisplayDateFormat())
        } else {
            LocalDate.now()
        }

        val dialog = DatePickerDialog(
            this, { _, yr, mnth, dt ->

                val text = LocalDate(yr, mnth + 1, dt)
                edittext.setText(text.toString(ModelUtil.charmDisplayDateFormat()))

            }, localDate.year, localDate.monthOfYear - 1, localDate.dayOfMonth
        )

        dialog.datePicker.minDate =
            bookAppointmentViewModel.appointmentConfig?.currentFacilityTimezone?.millis
                ?: System.currentTimeMillis()
        dialog.datePicker.maxDate = LocalDate.now().plusYears(1).toDateTimeAtStartOfDay().millis

        dialog.show()
    }

    /**
     * Method to show timepicker for the given edittext.
     **/
    private fun showTimePicker(edittext: MDTextInputEdittext) {

        val value = edittext.text.toString().trim()

        val dateTime = if (value.isNotBlank()) {
            DateTime.parse(value, ModelUtil.charmDisplayTimeFormat())
        } else {
            DateTime.now()
        }

        val dialog = MDTimePickerDialog(
            this, TimePickerDialog.OnTimeSetListener { _, hrs, min ->

                val selectedTime = DateTime.parse(
                    "$hrs:$min", ModelUtil.charmDisplay24HrTimeFormat()
                )
                edittext.setText(selectedTime.toString(ModelUtil.charmDisplayTimeFormat))

            }, dateTime.hourOfDay, dateTime.minuteOfHour, false
        )





        dialog.show()
    }

    private fun hideErrorPage() {

        bookAppointmentViewModel.errorInAPI = -1
        if (binding.bookAppointmentErrorPage.root.visibility == View.VISIBLE) {
            binding.bookAppointmentErrorPage.root.visibility = View.GONE
        }
    }

    //region Razorpay

    override fun onPaymentError(code: Int, response: String?) {
        Log.d("MdError", "onPaymentError code $code response")

        val paymentAmount =
            bookAppointmentViewModel.appointmentConfig?.chargeAmount?.toIntOrNull() ?: 0

        when (code) {

            Checkout.NETWORK_ERROR -> { //There was a network error, for example loss of internet connectivity.
                retryCreateTransaction(R.string.internet_connection_error)
            }

            Checkout.INVALID_OPTIONS -> {  //An issue with options passed in checkout.open.
                //{"code":"BAD_REQUEST_ERROR","description":"The OAuth token used in the request was invalid or had expired"}
                retryCreateTransaction(R.string.error_occurred_try_again)
            }

            Checkout.PAYMENT_CANCELED -> { //User cancelled the payment.
                //Do nothing stay on this page
            }

            Checkout.TLS_ERROR -> { //Device does not support TLS v1.1 or TLS v1.2.
                retryCreateTransaction(R.string.error_occurred_try_again)
            }

        }
    }

    override fun onPaymentSuccess(transactionId: String?) {
        bookAppointmentViewModel.paymentTransaction?.razorPayPaymentId = transactionId
        updatePaymentTransaction(bookAppointmentViewModel.paymentTransaction?.transactionId ?: "")
    }

    private fun retryUpdateTransaction(transactionId: String) {

        PaymentUtil.retryUpdateTransaction(this, R.string.payment_under_process) {
            updatePaymentTransaction(transactionId)
        }
    }

    //region Bluefin Payment methods
    private fun checkStatusResponseHandler(resp: Any?) {
        if (resp != null) {
            if (resp is String) {
                val appointmentRequest = bookAppointmentViewModel.appointmentRequest
                appointmentRequest.receiptId = resp
                addAppointment()
            } else {
                progressDialog.dismiss()
                PaymentUtil.retryUpdateTransaction(this, R.string.payment_under_process) {
                    checkStatus()
                }
            }
        }
    }

    private fun checkStatus() {

        progressDialog.show()
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                var resp: Any? = ""
                resp = bookAppointmentViewModel.checkTransactionStatus()
                runOnUiThread {
                    checkStatusResponseHandler(resp)
                }
            }
        } catch (ex: TimeoutCancellationException) {
            Log.e(TAG, ex.printStackTrace().toString())
            progressDialog.dismiss()
        } catch (ex: Exception) {
            Log.e(TAG, ex.printStackTrace().toString())
            progressDialog.dismiss()
        }
    }

    private fun createSaveOnlyForBluefin() {
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                var resp: Any? = ""

                resp = bookAppointmentViewModel.saveBluefinCardDetails()

                runOnUiThread {
                    createSaveOnlyForBluefinResponseHandler(resp)
                }
            }
        } catch (ex: TimeoutCancellationException) {
            Log.e(TAG, ex.printStackTrace().toString())
            progressDialog.dismiss()
        } catch (ex: Exception) {
            Log.e(TAG, ex.printStackTrace().toString())
            progressDialog.dismiss()
        }
    }

    private fun createSaveOnlyForBluefinResponseHandler(resp: Any?) {
        if (resp != null) {
            if (resp is Boolean) {
                addAppointment()
            } else if (resp is APIResponseError) {
                progressDialog.dismiss()
                val apiError = resp
                if (apiError.charmApiCode == ModelUtil.errorPaymentSuccessButReceiptFailedCode) {
                    PaymentUtil.retryUpdateTransaction(
                        this, R.string.payment_under_process
                    ) {
                        checkStatus()
                    }
                } else {

                    retryCreateTransaction(
                        resp.errorMsg ?: getString(R.string.error_occurred_try_again)
                    )
                }
            }
        }
    }

    private fun saveAndPayForBluefinResponseHandler(resp: Any?) {
        if (resp != null) {
            if (resp is Boolean) {
                createPaymentOnlyForBluefin()
            } else if (resp is APIResponseError) {
                progressDialog.dismiss()

                if (resp.charmApiCode == ModelUtil.errorPaymentSuccessButReceiptFailedCode) {
                    PaymentUtil.retryUpdateTransaction(
                        this, R.string.payment_under_process
                    ) {
                        checkStatus()
                    }
                } else {
                    retryCreateTransaction(
                        resp.errorMsg ?: getString(R.string.error_occurred_try_again)
                    )
                }
            }
        }
    }

    private fun saveAndPayForBluefin() {
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                var resp: Any? = ""

                resp = bookAppointmentViewModel.saveBluefinCardDetails()

                runOnUiThread {
                    saveAndPayForBluefinResponseHandler(resp)
                }
            }
        } catch (ex: TimeoutCancellationException) {
            Log.e(TAG, ex.printStackTrace().toString())
            progressDialog.dismiss()
        } catch (ex: Exception) {
            Log.e(TAG, ex.printStackTrace().toString())
            progressDialog.dismiss()
        }
    }

    private fun createPaymentOnlyForBluefinResponseHandler(resp: Any?) {
        if (resp != null) {
            if (resp is String) {
                val appointmentRequest = bookAppointmentViewModel.appointmentRequest
                appointmentRequest.receiptId = resp

                addAppointment()
            } else if (resp is APIResponseError) {
                progressDialog.dismiss()

                if (resp.charmApiCode == ModelUtil.errorPaymentSuccessButReceiptFailedCode) {
                    PaymentUtil.retryUpdateTransaction(
                        this, R.string.payment_under_process
                    ) {
                        checkStatus()
                    }
                } else {

                    retryCreateTransaction(
                        resp.errorMsg ?: getString(R.string.error_occurred_try_again)
                    )
                }
            }
        }
    }

    private fun createPaymentOnlyForBluefin() {
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                var resp: Any? = ""

                resp = bookAppointmentViewModel.createPaymentForBluefin()

                runOnUiThread { createPaymentOnlyForBluefinResponseHandler(resp) }
            }
        } catch (ex: TimeoutCancellationException) {
            Log.e(TAG, ex.printStackTrace().toString())
            progressDialog.dismiss()
        } catch (ex: Exception) {
            Log.e(TAG, ex.printStackTrace().toString())
            progressDialog.dismiss()
        }
    }
    //endregion

    private fun createPaymentTransaction() {

        val paymentTransaction = bookAppointmentViewModel.paymentTransaction!!

        val validated = PaymentUtil.validatePaymentTransactionData(
            paymentTransaction,
            bookAppointmentViewModel.appointmentConfig?.cardProcessingType
        )

        if (validated) {

            paymentTransaction.gateway = bookAppointmentViewModel.appointmentConfig?.paymentGateway
            progressDialog.show()

            if (bookAppointmentViewModel.appointmentConfig?.paymentGateway == ModelUtil.appointmentGatewayTypeBluefin) {
                if (bookAppointmentViewModel.appointmentConfig?.cardProcessingType == ModelUtil.appointmentCardProcessingTypeChargeOnly) {
                    createPaymentOnlyForBluefin()
                } else if (bookAppointmentViewModel.appointmentConfig?.cardProcessingType == ModelUtil.appointmentCardProcessingTypeStoreCharge) {
                    if (binding.bookAppointmentChooseCardLayout.visibility == View.VISIBLE) {
                        createPaymentOnlyForBluefin()
                    } else {
                        saveAndPayForBluefin()
                    }
                } else if (bookAppointmentViewModel.appointmentConfig?.cardProcessingType == ModelUtil.appointmentCardProcessingTypeStoreOnly) {
                    if (binding.bookAppointmentCardLayout.visibility == View.VISIBLE) {
                        createSaveOnlyForBluefin()
                    } else {
                        addAppointment()
                    }
                }
            } else {

                bookAppointmentViewModel.createTransactionForRazorpay(paymentTransaction)
                    .observe(this) { resp ->

                        if (resp != null) {

                            progressDialog.dismiss()

                            if (resp is TransactionInfo) {

                                bookAppointmentViewModel.paymentTransaction = resp
                                bookAppointmentViewModel.razorPayPublicToken = resp.publicToken

                                val isCheckoutSuccess = PaymentUtil.checkoutRazorpay(
                                    this, resp, authManager.selectedPatientLive.value ?: Patient()
                                )

                                if (!isCheckoutSuccess) {
                                    retryCreateTransaction(R.string.error_occurred_try_again)
                                }

                            } else retryCreateTransaction(R.string.error_occurred_try_again)

                        }

                    }
            }

        } else retryCreateTransaction(R.string.error_occurred_try_again)

    }

    private fun retryCreateTransaction(@StringRes stringRes: Int) {
        PaymentUtil.retryCreateTransaction(this, stringRes) {
            createPaymentTransaction()
        }
    }

    private fun retryCreateTransaction(errorMessage: String) {
        PaymentUtil.retryCreateTransaction(this, errorMessage) {
            createPaymentTransaction()
        }
    }

    private fun updatePaymentTransaction(transactionId: String) {

        progressDialog.show()
        bookAppointmentViewModel.updateTransactionForRazorpay(transactionId)
            .observe(this, Observer {

                if (it != null) {

                    if (it is String) {

                        bookAppointmentViewModel.appointmentRequest.receiptId = it
                        addAppointment()

                    } else {
                        progressDialog.dismiss()
                        retryUpdateTransaction(transactionId)
                        //Error handeling
                    }

                }

            })

    }

    private fun checkDataIsAvailable(): Boolean {

        if (bookAppointmentViewModel.facilityList.isNotEmpty()) {
            if (bookAppointmentViewModel.facilityList.firstOrNull()?.facilityId != bookAppointmentViewModel.selectedFacility.facilityId) {
                return true
            }
            if (!bookAppointmentViewModel.selectedMember.memberId.isNullOrBlank()) {
                return true
            }
            if (binding.bookAppointmentReasonET.text.toString().isNotBlank()) {
                return true
            }
        }
        return false
    }

    //endregion

    override fun onBackPressed() {

        if (checkDataIsAvailable()) {
            backPressAlertDialogBuilder.show()
        } else {
            super.onBackPressed()
            if (bookAppointmentViewModel.isFromDashboard) {
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            } else if (bookAppointmentViewModel.isFromEasyTeleCareWebView) {
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }

    private fun setCalendarView() {

        binding.bookAppointmentCalendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
//                Log.d(TAG, " day $day")
                container.day = day
                val textView = container.exThreeDayText
                val dotView = container.exThreeDotView

                val selectedDateTime = day.date.toDateTime()
                textView.text = selectedDateTime.dayOfMonth.toString()

                val slotMetaData =
                    bookAppointmentViewModel.slotsHashMap[selectedDateTime.getMonthPlusYear()]
                val slotsArrList = slotMetaData?.memberAvailability?.get(selectedDateTime)
                val showDot = slotsArrList?.isNotEmpty() ?: false

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.showView()
                    when (selectedDateTime.toString(ModelUtil.charmApiDateFormat)) {

                        bookAppointmentViewModel.selectedDate?.toString(ModelUtil.charmApiDateFormat) -> {
                            textView.setTextColorRes(R.color.white)
                            textView.setBackgroundResource(R.drawable.calendar_today_drawable)
                            dotView.setBackgroundResource(R.drawable.calendar_slot_dot_white)
                        }

                        DateTime.now().toString(ModelUtil.charmApiDateFormat) -> {

                            textView.setTextColorRes(R.color.colorPrimary)
                            textView.setBackgroundResource(R.drawable.calendar_selected_drawable)
                            dotView.setBackgroundResource(R.drawable.calendar_today_drawable)

                        }

                        else -> {
                            textView.setTextColorRes(R.color.black87)
                            textView.background = null
                            dotView.setBackgroundResource(R.drawable.calendar_today_drawable)
                        }
                    }
                    if (showDot) {
                        dotView.showView()
                    } else dotView.hideView()
                    if (isDayBeforeToday(selectedDateTime)) {
                        textView.setTextColorRes(R.color.black30)
                    }
                } else {
                    textView.hideView()
                    dotView.hideView()
                }
            }
        }

        binding.bookAppointmentCalendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, month: CalendarMonth) {

                    Log.d(TAG, "MonthHeaderFooterBinder  ${month.month}  ${month.year}")
                    // Setup each header day text if we have not done that already.
                    if (container.monthHeaderLayoutContainer?.tag == null) {
                        container.monthHeaderLayoutContainer?.tag = month.yearMonth/* container.monthHeaderLayoutContainer?.children?.map { it as TextView }?.forEachIndexed { index, tv ->
                         tv.text = daysOfWeek[index].name.first().toString()
                         tv.setTextColorRes(R.color.black87)
                     }*/

                    }

                }
            }

        binding.bookAppointmentCalendarView.monthScrollListener = {
            bookAppointmentViewModel.selectedCalendarMonth = it
            val dateTimeNow = DateTime.now()
            var dateTime = DateTime(it.year, it.month, 1, 0, 0, 0)
            val monthOfTheYear = dateTimeNow.monthOfYear
            if (it.month == monthOfTheYear) {
                val day = dateTimeNow.dayOfMonth
                dateTime = DateTime(it.year, it.month, day, 0, 0, 0)
            }

            val monthStr = dateTime.toString(ModelUtil.monthShortFormat)
            binding.bookAppointmentCalendarMonthTV.text = monthStr.plus(" ").plus(it.year)
            val oldDateTime = bookAppointmentViewModel.selectedDate
            oldDateTime?.let {
                binding.bookAppointmentCalendarView.notifyDateChanged(it.toMdLocaleDate())
            }
            bookAppointmentViewModel.selectedDate = dateTime
            bookAppointmentViewModel.lastSelectedSlotStr = null
            binding.bookAppointmentCalendarView.notifyDateChanged(dateTime.toMdLocaleDate())
            binding.bookAppointmentSelectedSlotDateCV.visibility = View.GONE
            getSlots(dateTime)

        }
    }

    private fun clearSlotMetaData() {
        bookAppointmentViewModel.slotsHashMap.clear()
    }

    private fun selectDate(date: java.time.LocalDate) {/* if (selectedDate != date) {
             val oldDate = selectedDate
             selectedDate = date
             oldDate?.let { bookAppointmentCalendarView.notifyDateChanged(it) }
             bookAppointmentCalendarView.notifyDateChanged(date)
         }*/
    }

    class MonthViewContainer(view: View) : ViewContainer(view) {
        val monthHeaderLayoutContainer =
            view.findViewById(R.id.monthHeaderLayoutContainer) as? LinearLayout
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        lateinit var day: CalendarDay // Will be set when this container is bound.
        val exThreeDayText: TextView = view.findViewById(R.id.exThreeDayText)
        val exThreeDotView: View = view.findViewById(R.id.exThreeDotView)

        init {
            view.setOnClickListener {
                if (day.owner == DayOwner.THIS_MONTH) {
                    selectDate(day.date)
                    val date = day.date
                    val selectedDateTime = date.toDateTime()

                    if (isDayBeforeToday(selectedDateTime)) {
                        UIUtil.showToast(
                            this@BookAppointmentActivity,
                            getString(R.string.appointment_select_valid_date),
                            Toast.LENGTH_LONG
                        )
                        return@setOnClickListener
                    }

                    val oldDateTime = bookAppointmentViewModel.selectedDate
                    oldDateTime?.let {
                        binding.bookAppointmentCalendarView.notifyDateChanged(it.toMdLocaleDate())
                    }
                    bookAppointmentViewModel.selectedDate = selectedDateTime
                    bookAppointmentViewModel.lastSelectedSlotStr = null
                    binding.bookAppointmentCalendarView.notifyDateChanged(date)
                    binding.bookAppointmentSelectedSlotDateCV.visibility = View.GONE
                    getSlots(selectedDateTime)
                }
            }
        }
    }

    private fun isDayBeforeToday(selectedDateTime: DateTime): Boolean {
        val today = DateTime.now().removeTime()
        if (today.toString(ModelUtil.charmApiDateFormat) != selectedDateTime.toString(ModelUtil.charmApiDateFormat)) {
            if (selectedDateTime.isBefore(today.millis)) {
                return true
            }
        }
        return false
    }

    private fun setCalendarClickListeners() {
        binding.bkAppNextMonth.setOnClickListener {
            val cm = bookAppointmentViewModel.selectedCalendarMonth ?: return@setOnClickListener

            val selectedDateTime = DateTime(cm.year, cm.month, 1, 0, 0, 0)
            val newDateTime = selectedDateTime.plusMonths(1)

            Log.d(TAG, newDateTime.toString())

            binding.bookAppointmentCalendarView.scrollToMonth(
                YearMonth.of(
                    newDateTime.year, newDateTime.monthOfYear
                )
            )
        }

        binding.bkAppPreviousMonth.setOnClickListener {
            val cm = bookAppointmentViewModel.selectedCalendarMonth ?: return@setOnClickListener
            val selectedDateTime = DateTime(cm.year, cm.month, 1, 0, 0, 0)
            val newDateTime = selectedDateTime.minusMonths(1)

            Log.d(TAG, newDateTime.toString())
            binding.bookAppointmentCalendarView.scrollToMonth(
                YearMonth.of(
                    newDateTime.year, newDateTime.monthOfYear
                )
            )
        }
    }

    fun showConfirmAppointmentDialog() {

        val messageString = ""
        val builder = AlertDialog.Builder(this)
        builder.setMessage(messageString)

        builder.setPositiveButton(R.string.book_appointment_book_str) { dialog, which ->

        }
        builder.setNegativeButton(R.string.no_str) { dialog, which ->
            dialog.dismiss()
        }

        builder.show()
    }

}

internal fun java.time.LocalDate.toDateTime(): DateTime {
    return DateTime(this.year, this.month.value, this.dayOfMonth, 0, 0, 0, 0)
}

internal fun DateTime.toMdLocaleDate(): java.time.LocalDate {
    return java.time.LocalDate.of(this.year, this.monthOfYear, this.dayOfMonth)
}

/*internal fun java.time.LocalDate.toDateTime(): DateTime {
    return DateTime(this.year, this.month.value, this.dayOfMonth,0 ,0 ,0, 0)
}*/

internal fun checkForSameMonth(dateTime: DateTime) {

}

internal fun DateTime.getMonthPlusYear(): String {
    val month = this.monthOfYear.toString()
    val year = this.year.toString()
    return month.plus("+").plus(year)

}

internal fun Context.getDrawableCompat(@DrawableRes drawable: Int) =
    ContextCompat.getDrawable(this, drawable)

internal fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

internal fun TextView.setTextColorRes(@ColorRes color: Int) =
    setTextColor(context.getColorCompat(color))