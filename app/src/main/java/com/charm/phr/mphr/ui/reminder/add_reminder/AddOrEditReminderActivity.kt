package com.charm.phr.mphr.ui.reminder.add_reminder

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAddOrEditReminderBinding
import com.charm.phr.mphr.databinding.ItemReminderTimingBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.database.entity.Reminder
import com.charm.phr.mphr.model.database.entity.ReminderFrequencyMap
import com.charm.phr.mphr.model.database.entity.ReminderMap
import com.charm.phr.mphr.ui.components.MDTimePickerDialog
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.PermissionUtil
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.isAfterOrEqual
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.LocalDate
import javax.inject.Inject

class AddOrEditReminderActivity : MDBaseActivity() {

    lateinit var backPressAlertDialogBuilder: AlertDialog.Builder
    private lateinit var deleteAlertDialogBuilder: AlertDialog.Builder

    companion object {
        // const val reminderOnce = "Once"
        const val reminderDaily = "day"
        const val reminderWeekly = "week"
    }

    private lateinit var activityAddOrEditReminderBinding: ActivityAddOrEditReminderBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var addReminderViewModel: AddReminderViewModel

    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null


    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityAddOrEditReminderBinding = ActivityAddOrEditReminderBinding.inflate(layoutInflater)
        setContentView(activityAddOrEditReminderBinding.root)
        addReminderViewModel = ViewModelProvider(this, viewModelFactory)[AddReminderViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(activityAddOrEditReminderBinding.adReminderToolbar)
        UIUtil.applyViewInsertForBottomView(activityAddOrEditReminderBinding.addOrEditReminderMainLayout)

        initializePermissionResultLauncher()
        if (intent.getBooleanExtra(ModelUtil.isAddReminder, true)) {
            getReminderDetailForADD()
        } else {
            val reminderPOJOStr = intent.getStringExtra(ModelUtil.ReminderPOJO)
            val reminderId = intent.getStringExtra(ModelUtil.ReminderModuleId)
            val reminderPOJOJson = JsonParser.parseString(reminderPOJOStr)
            addReminderViewModel.dosageName = intent.getStringExtra(ModelUtil.drugName) ?: ""
            addReminderViewModel.drugDirection =
                intent.getStringExtra(ModelUtil.drugDirection) ?: ""
            addReminderViewModel.startDate =
                intent.getStringExtra(ModelUtil.ReminderModuleStartDate)
            addReminderViewModel.endDate = intent.getStringExtra(ModelUtil.ReminderModuleEndDate)
            addReminderViewModel.reminderPOJO =
                MDParsingHelper.formReminderObjectWithLocalId(reminderPOJOJson as JsonObject)
            addReminderViewModel.reminderPOJO.reminder?.reminderId = reminderId
        }
        setOnclickListener()
        renderDefaultValueAndView()
        setMandatoryView()
        onBackPressAlert()
        onDeleteAlert()
    }

    private fun onBackClick() {
        super.onBackPressed()
    }
    //endregion

    private fun getReminderDetailForADD() {
        val selectedReminderObj = intent?.getStringExtra(ModelUtil.resModuleObject)
        val practiceId = intent?.getStringExtra(ModelUtil.practiceIdIntent)
        val selectedReminderModule = intent?.getStringExtra(ModelUtil.reminderModuleId)
        val selectedReminderJsonObj = JsonParser.parseString(selectedReminderObj) as JsonObject
        val patientId = authManager.selectedPatientLive.value?.practicePatientId ?: ""
        val reminderMap = ReminderMap()
        addReminderViewModel.reminderPOJO.reminder = Reminder()
        when (selectedReminderModule) {
            getString(R.string.medications_str) -> {
                addReminderViewModel.medicationObj =
                    MDParsingHelper.formMedicationObj(selectedReminderJsonObj, patientId)
                reminderMap.type = ModelUtil.medicationAddedAsMed
                if (addReminderViewModel.medicationObj.patMed?.isAddedByPatient == false) {
                    reminderMap.moduleId =
                        addReminderViewModel.medicationObj.patMed?.patientMedicationId
                    reminderMap.practiceId = practiceId
                    addReminderViewModel.drugDirection =
                        addReminderViewModel.medicationObj.patMedExt.firstOrNull()?.directions ?: ""
                    addReminderViewModel.dosageName = UIUtil.getRxDisplayName(
                        addReminderViewModel.medicationObj,
                        applicationContext
                    ) ?: ""
                } else {
                    reminderMap.moduleId =
                        addReminderViewModel.medicationObj.patMedPHRExt.firstOrNull()?.prescriptionHistoryId
                    addReminderViewModel.drugDirection =
                        UIUtil.formDirectionsForMedication(addReminderViewModel.medicationObj)
                    addReminderViewModel.dosageName =
                        addReminderViewModel.medicationObj.drugDetail.firstOrNull()?.genericDrugName.toString()
                }
                addReminderViewModel.reminderPOJO.reminder?.startDate =
                    addReminderViewModel.medicationObj.patMed?.startDate ?: DateTime.now()
                addReminderViewModel.startDate =
                    addReminderViewModel.medicationObj.patMed?.startDate?.toString(ModelUtil.charmDisplayDateFormat())
                addReminderViewModel.reminderPOJO.reminder?.endDate =
                    addReminderViewModel.medicationObj.patMed?.stopDate
                addReminderViewModel.endDate =
                    addReminderViewModel.medicationObj.patMed?.stopDate?.toString(ModelUtil.charmDisplayDateFormat())
            }

            getString(R.string.supplements_str) -> {
                addReminderViewModel.supplementObj =
                    MDParsingHelper.formSupplementObj(selectedReminderJsonObj, patientId)
                reminderMap.type =
                    ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() }
                if (!addReminderViewModel.supplementObj.isAddedByPatient) {
                    reminderMap.moduleId = addReminderViewModel.supplementObj.patientSupplementId
                    reminderMap.practiceId = practiceId
                    addReminderViewModel.drugDirection =
                        addReminderViewModel.supplementObj.patientSupplementMap?.patientSupplementHistory?.instructions
                            ?: ""
                } else {
                    reminderMap.moduleId =
                        addReminderViewModel.supplementObj.patientSupplementMap?.patientSupplementHistory?.supplementHistoryId
                    addReminderViewModel.drugDirection =
                        UIUtil.formDirectionsForSupplement(addReminderViewModel.supplementObj)
                }
                var supplementsName = addReminderViewModel.supplementObj.supplement?.supplementName
                val noOfIntake =
                    addReminderViewModel.supplementObj.patientSupplementMap?.patientSupplementHistory?.numberOfIntake
                if (noOfIntake != null && (noOfIntake.trim().toFloatOrNull() ?: -1f) > 0f) {
                    supplementsName += " $noOfIntake ${addReminderViewModel.supplementObj.patientSupplementMap?.patientSupplementHistory?.intakeUnit ?: ""}"
                }
                addReminderViewModel.dosageName = supplementsName ?: ""
                addReminderViewModel.reminderPOJO.reminder?.startDate =
                    addReminderViewModel.supplementObj.patientSupplementMap?.patientSupplementHistory?.fromDate
                        ?: DateTime.now()
                addReminderViewModel.startDate =
                    addReminderViewModel.supplementObj.patientSupplementMap?.patientSupplementHistory?.fromDate?.toString(
                        ModelUtil.charmDisplayDateFormat()
                    )
                addReminderViewModel.reminderPOJO.reminder?.endDate =
                    addReminderViewModel.supplementObj.patientSupplementMap?.patientSupplementHistory?.toDate
                addReminderViewModel.endDate =
                    addReminderViewModel.supplementObj.patientSupplementMap?.patientSupplementHistory?.toDate?.toString(
                        ModelUtil.charmDisplayDateFormat()
                    )
            }
        }
        addReminderViewModel.reminderPOJO.reminderMap = arrayListOf(reminderMap)
    }

    //region render View and Value
    private fun renderDefaultValueAndView() {
        activityAddOrEditReminderBinding.addReminderStartDateET.inputType = InputType.TYPE_NULL
        activityAddOrEditReminderBinding.addReminderEndDateET.inputType = InputType.TYPE_NULL
        activityAddOrEditReminderBinding.addReminderModuleItemNameTV.text =
            addReminderViewModel.dosageName
        if (addReminderViewModel.drugDirection.isNotBlank()) {
            activityAddOrEditReminderBinding.addReminderModuleItemDirectionsTV.text =
                addReminderViewModel.drugDirection
        } else {
            activityAddOrEditReminderBinding.addReminderModuleItemDirectionsTV.visibility =
                View.GONE
        }
        activityAddOrEditReminderBinding.addReminderStartDateET.setText(
            addReminderViewModel.reminderPOJO.reminder?.startDate?.toString(
                ModelUtil.charmDisplayDateFormat()
            )
        )
        if (intent.getBooleanExtra(ModelUtil.IsHistoryPresent, false)) {
            activityAddOrEditReminderBinding.addReminderStartDateET.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                0,
                0
            )
            activityAddOrEditReminderBinding.addReminderStartDateLayout.hintTextColor =
                AppCompatResources.getColorStateList(this, R.color.edittext_txt_color)
            activityAddOrEditReminderBinding.addReminderStartDateET.isClickable = false
            activityAddOrEditReminderBinding.addReminderStartDateLayout.setShowLine(false)
        }
        if (intent.getBooleanExtra(ModelUtil.isAddReminder, true)) {
            if (addReminderViewModel.reminderPOJO.reminder?.endDate != null && addReminderViewModel.reminderPOJO.reminder?.endDate?.withTimeAtStartOfDay()
                    ?.isAfterOrEqual(DateTime.now().withTimeAtStartOfDay()) == true
            ) {
                activityAddOrEditReminderBinding.addReminderEndDateET.setText(
                    addReminderViewModel.reminderPOJO.reminder?.endDate?.toString(
                        ModelUtil.charmDisplayDateFormat()
                    )
                )
            }
        } else {
            activityAddOrEditReminderBinding.addReminderEndDateET.setText(
                addReminderViewModel.reminderPOJO.reminder?.endDate?.toString(
                    ModelUtil.charmDisplayDateFormat()
                )
            )
        }
        setFrequencyValue(
            addReminderViewModel.reminderPOJO.reminder?.frequencyUnit ?: reminderDaily,
            addReminderViewModel.reminderPOJO.reminder?.frequency ?: "", true
        )
        if (!intent.getBooleanExtra(ModelUtil.isAddReminder, true)) {
            renderValue()
            activityAddOrEditReminderBinding.reminderAddButton.visibility = View.GONE
            addReminderViewModel.reminderPOJO.reminder?.reminderStatus
            if (addReminderViewModel.reminderPOJO.reminder?.reminderStatus == ModelUtil.CompletedStatus) {
                activityAddOrEditReminderBinding.reminderEditButton.visibility = View.GONE
            } else {
                activityAddOrEditReminderBinding.reminderEditButton.visibility = View.VISIBLE
            }
            activityAddOrEditReminderBinding.addReminderDeleteBtn.visibility = View.VISIBLE
            activityAddOrEditReminderBinding.addReminderToolbarText.text =
                getString(R.string.edit_reminder)
        }
    }

    private fun setMandatoryView() {
        if (!intent.getBooleanExtra(ModelUtil.IsHistoryPresent, false)) {
            UIUtil.makeTextViewMandatory(activityAddOrEditReminderBinding.addReminderStartDateLayout)
        }
        UIUtil.makeTextViewMandatory(activityAddOrEditReminderBinding.addReminderEndDateLayout)
        UIUtil.makeTextViewMandatory(activityAddOrEditReminderBinding.addReminderTimingTV)
    }

    private fun renderValue() {
        setReminderTimingMap()
        setReminderTiming()
        setWeekDayView()
    }

    private fun setReminderTiming() {
        addReminderViewModel.reminderTimingMap.forEach { it ->
            val key = it.key
            val timeArr = it.value
            if (timeArr.size > 0) {
                activityAddOrEditReminderBinding.addReminderAddTimingLL.visibility = View.VISIBLE
                activityAddOrEditReminderBinding.addReminderAddTimingLL.removeAllViews()
                activityAddOrEditReminderBinding.addReminderAddTimingSeparator.visibility =
                    View.VISIBLE
            }
            timeArr.sortBy { it }
            timeArr.forEach {
                createReminderTimeItem(it?.toString(ModelUtil.charmDisplayTimeFormat) ?: "", key)
            }
        }
    }

    private fun createReminderTimeItem(time: String, key: Int) {
        val itemReminderTimingBinding = ItemReminderTimingBinding.inflate(
            layoutInflater,
            activityAddOrEditReminderBinding.addReminderAddTimingLL,
            false
        )
        itemReminderTimingBinding.addedReminderTimeTV.text = time
        itemReminderTimingBinding.removeReminderTimeIV.setOnClickListener {
            activityAddOrEditReminderBinding.addReminderAddTimingLL.removeView(
                itemReminderTimingBinding.root
            )
            addReminderViewModel.reminderTimingMap[key]?.remove(
                UIUtil.formatDate(time, ModelUtil.charmDisplayTimeFormat())
            )
            if (addReminderViewModel.reminderTimingMap[key]?.size == 0) {
                activityAddOrEditReminderBinding.addReminderAddTimingLL.visibility = View.GONE
                activityAddOrEditReminderBinding.addReminderAddTimingSeparator.visibility =
                    View.GONE
            }
        }
        itemReminderTimingBinding.addedReminderTimeTV.setOnClickListener {
            showTimePicker(time, itemReminderTimingBinding, key)
        }
        if ((addReminderViewModel.reminderTimingMap[key]?.size ?: 0) > 0) {
            activityAddOrEditReminderBinding.addReminderAddTimingLL.visibility = View.VISIBLE
            activityAddOrEditReminderBinding.addReminderAddTimingSeparator.visibility = View.VISIBLE
        }
        activityAddOrEditReminderBinding.addReminderAddTimingLL.addView(itemReminderTimingBinding.root)
    }

    private fun setReminderTimingMap() {
        addReminderViewModel.reminderTimingMap = hashMapOf()
        val reminderTimingSet = mutableSetOf<DateTime?>()
        addReminderViewModel.reminderPOJO.reminderFrequencyMap.forEach {
            val day = it.day
            if (day != -1) {
                addReminderViewModel.selectedWeekDay[day] = true
            }
            val time = UIUtil.formatDate(it.time, ModelUtil.charmDisplay24HrTimeFormat())
            reminderTimingSet.add(time)
        }
        addReminderViewModel.reminderTimingMap[-1] = ArrayList(reminderTimingSet)
    }

    private fun setWeekDayView() {
        addReminderViewModel.selectedWeekDay.forEachIndexed { index, it ->
            if (it) {
                selectWeekday(getViewOfWeekDay(index), index)
            }
        }
    }

    private fun setFrequencyValue(
        frequencyUnit: String,
        frequency: String,
        isFromRenderValue: Boolean = false
    ) {
        when (frequencyUnit) {
            reminderDaily -> {
                if (frequency == "1") {
                    activityAddOrEditReminderBinding.addReminderWeeklyHSV.visibility = View.GONE
                    activityAddOrEditReminderBinding.addReminderOneTimeFrequency.isChecked = true
                    activityAddOrEditReminderBinding.addReminderEndDateLayout.visibility = View.GONE
                    activityAddOrEditReminderBinding.addReminderStartDateET.setHint(R.string.reminder_date_str)
                    activityAddOrEditReminderBinding.addReminderStartDateLayout.setHint(R.string.reminder_date_str)
                    activityAddOrEditReminderBinding.addReminderEndDateET.text =
                        activityAddOrEditReminderBinding.addReminderStartDateET.text
                    activityAddOrEditReminderBinding.addReminderIntakeET.setText(getString(R.string.one_time))
                    activityAddOrEditReminderBinding.addReminderFrequencyUnitLL.visibility =
                        View.GONE

                } else {
                    activityAddOrEditReminderBinding.addReminderDailyFrequency.isChecked = true
                    activityAddOrEditReminderBinding.addReminderWeeklyHSV.visibility = View.GONE
                    activityAddOrEditReminderBinding.addReminderEndDateLayout.visibility =
                        View.VISIBLE
                    activityAddOrEditReminderBinding.addReminderStartDateET.setHint(R.string.medication_start_date_string)
                    activityAddOrEditReminderBinding.addReminderStartDateLayout.setHint(R.string.medication_start_date_string)
                    activityAddOrEditReminderBinding.addReminderIntakeET.setText(getString(R.string.daily_str))
                    activityAddOrEditReminderBinding.addReminderFrequencyUnitLL.visibility =
                        View.GONE
                }
            }

            reminderWeekly -> {
                activityAddOrEditReminderBinding.addReminderWeeklyFrequency.isChecked = true
                activityAddOrEditReminderBinding.addReminderIntakeET.setText(getString(R.string.weekly_str))
                activityAddOrEditReminderBinding.addReminderWeeklyHSV.visibility = View.VISIBLE
                activityAddOrEditReminderBinding.addReminderEndDateLayout.visibility = View.VISIBLE
                activityAddOrEditReminderBinding.addReminderFrequencyUnitLL.visibility =
                    View.VISIBLE
                activityAddOrEditReminderBinding.addReminderStartDateET.setHint(R.string.medication_start_date_string)
                activityAddOrEditReminderBinding.addReminderStartDateLayout.setHint(R.string.medication_start_date_string)
                showWeekDays()
                if (isFromRenderValue && intent.getBooleanExtra(
                        ModelUtil.IsHistoryPresent,
                        false
                    )
                ) {
                    activityAddOrEditReminderBinding.addReminderFrequencyGroup.visibility =
                        View.GONE
                }
            }
        }
    }
    //endregion

    //region Logic to show weekdays when weekly selected based on start and end date
    private fun showWeekDays() {
        val endDateStr =
            activityAddOrEditReminderBinding.addReminderEndDateET.text?.toString() ?: ""
        val startDateStr =
            activityAddOrEditReminderBinding.addReminderStartDateET.text.toString()
        if (endDateStr.isNotBlank() && startDateStr.isNotBlank()) {
            val fromDate =
                UIUtil.formatDate(startDateStr, ModelUtil.charmDisplayDateFormat())
            val toDate = UIUtil.formatDate(endDateStr, ModelUtil.charmDisplayDateFormat())
            val numOfDays = Days.daysBetween(fromDate, toDate).days + 1
            if (numOfDays < 7) {
                val startDay = fromDate?.dayOfWeek ?: 0
                val endDay = toDate?.dayOfWeek ?: 0
                var weekday = 1
                if (startDay <= endDay) {
                    while (weekday <= 7) {
                        if (weekday in startDay..endDay) {
                            enableDayView(weekday)
                        } else {
                            disableDayView(weekday)
                        }
                        weekday++
                    }
                } else {
                    while (weekday <= 7) {
                        if (weekday <= endDay || weekday >= startDay) {
                            enableDayView(weekday)
                        } else {
                            disableDayView(weekday)
                        }
                        weekday++
                    }
                }
            } else {
                var weekday = 1
                while (weekday <= 7) {
                    enableDayView(weekday)
                    weekday++
                }
            }
        }
    }

    private fun disableDayView(weekDay: Int) {
        val formatWeekDay = if (weekDay == 7) 0 else weekDay
        val view = getViewOfWeekDay(formatWeekDay)
        unSelectWeekDay(view, formatWeekDay)
        view?.isEnabled = false
        view?.setTextColor(getColor(R.color.addFlowsheetVitalLabETStroke))
    }

    private fun enableDayView(weekDay: Int) {
        val formatWeekDay = if (weekDay == 7) 0 else weekDay
        val view = getViewOfWeekDay(formatWeekDay)
        view?.isEnabled = true
        if (addReminderViewModel.selectedWeekDay[formatWeekDay]) {
            view?.setTextColor(getColor(R.color.colorPrimary))
        } else {
            view?.setTextColor(getColor(R.color.black87))
        }
    }
    //endregion

    //region Click Listener
    private fun setOnclickListener() {
        activityAddOrEditReminderBinding.addReminderAddTimeLL.setOnClickListener {
            if (addReminderViewModel.reminderPOJO.reminderFrequencyMap.isEmpty()) {
                setNotConfiguredFrequencyValue()
            }
            showTimePicker()
        }
        if (!intent.getBooleanExtra(ModelUtil.IsHistoryPresent, false)) {
            activityAddOrEditReminderBinding.addReminderStartDateET.setOnClickListener {
                showDatePicker(activityAddOrEditReminderBinding.addReminderStartDateET, true)
            }
            activityAddOrEditReminderBinding.addReminderStartDateET.setOnFocusChangeListener { _, isFocus ->
                if (isFocus) {
                    showDatePicker(activityAddOrEditReminderBinding.addReminderStartDateET, true)
                }
            }
            activityAddOrEditReminderBinding.addReminderIntakeET.setOnFocusChangeListener { _, isFocus ->
                if (isFocus) {
                    activityAddOrEditReminderBinding.addReminderFrequencyUnitLL.visibility =
                        View.VISIBLE
                }
            }
            activityAddOrEditReminderBinding.addReminderIntakeET.setOnClickListener {
                if (activityAddOrEditReminderBinding.addReminderFrequencyUnitLL.visibility == View.VISIBLE) {
                    activityAddOrEditReminderBinding.addReminderFrequencyUnitLL.visibility =
                        View.GONE
                } else {
                    activityAddOrEditReminderBinding.addReminderFrequencyUnitLL.visibility =
                        View.VISIBLE
                }
            }
            weekDayOnClickListener()
        }
        activityAddOrEditReminderBinding.addReminderEndDateET.setOnClickListener {
            showDatePicker(activityAddOrEditReminderBinding.addReminderEndDateET)
        }
        activityAddOrEditReminderBinding.addReminderEndDateET.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                showDatePicker(activityAddOrEditReminderBinding.addReminderEndDateET)
            }
        }
        activityAddOrEditReminderBinding.addReminderBackBtn.setOnClickListener {
            if (addReminderViewModel.isDataEdited) {
                backPressAlertDialogBuilder.show()
            } else {
                onBackPressed()
            }
        }
        activityAddOrEditReminderBinding.reminderAddButton.setOnClickListener {
            checkPermissionAndValidateData(true)
        }
        activityAddOrEditReminderBinding.reminderEditButton.setOnClickListener {
            checkPermissionAndValidateData(false)
        }

        activityAddOrEditReminderBinding.addReminderOneTimeFrequency.setOnClickListener {
            setFrequencyValue(reminderDaily, "1")
            addReminderViewModel.isDataEdited = true
        }
        activityAddOrEditReminderBinding.addReminderDailyFrequency.setOnClickListener {
            setFrequencyValue(reminderDaily, "")
            addReminderViewModel.isDataEdited = true
        }
        activityAddOrEditReminderBinding.addReminderWeeklyFrequency.setOnClickListener {
            setFrequencyValue(reminderWeekly, "")
            addReminderViewModel.isDataEdited = true
        }
        activityAddOrEditReminderBinding.addReminderDeleteBtn.setOnClickListener {
            deleteAlertDialogBuilder.show()
        }
        activityAddOrEditReminderBinding.addReminderStartDateET.addTextChangedListener {
            if (activityAddOrEditReminderBinding.addReminderIntakeET.text.toString() == getString(R.string.weekly_str)) {
                showWeekDays()
            }
        }
        activityAddOrEditReminderBinding.addReminderEndDateET.addTextChangedListener {
            if (activityAddOrEditReminderBinding.addReminderIntakeET.text.toString() == getString(R.string.weekly_str)) {
                showWeekDays()
            }
        }
    }

    private fun getViewOfWeekDay(weekDay: Int): TextView? {
        return when (weekDay) {
            0 -> activityAddOrEditReminderBinding.addReminderSunBtn
            1 -> activityAddOrEditReminderBinding.addReminderMonBtn
            2 -> activityAddOrEditReminderBinding.addReminderTueBtn
            3 -> activityAddOrEditReminderBinding.addReminderWedBtn
            4 -> activityAddOrEditReminderBinding.addReminderThuBtn
            5 -> activityAddOrEditReminderBinding.addReminderFriBtn
            6 -> activityAddOrEditReminderBinding.addReminderSatBtn
            else -> null
        }
    }

    private fun unSelectWeekDay(weekDayView: TextView?, weekDay: Int) {

        addReminderViewModel.selectedWeekDay[weekDay] = false
        weekDayView?.background =
            ResourcesCompat.getDrawable(
                resources,
                R.color.transparent,
                resources.newTheme()
            )
        weekDayView?.setTextColor(
            resources.getColor(
                R.color.black87,
                resources.newTheme()
            )
        )

    }

    private fun selectWeekday(weekDayView: TextView?, weekDay: Int) {
        addReminderViewModel.selectedWeekDay[weekDay] = true
        weekDayView?.background =
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.item_button_secondary_bg,
                resources.newTheme()
            )
        weekDayView?.setTextColor(
            resources.getColor(
                R.color.colorPrimary,
                resources.newTheme()
            )
        )
    }

    private fun weekDayOnClickListener() {
        for (weekDay in 0..6) {
            val view = getViewOfWeekDay(weekDay)
            view?.setOnClickListener {
                if (addReminderViewModel.selectedWeekDay[weekDay]) {
                    unSelectWeekDay(view, weekDay)
                } else {
                    selectWeekday(view, weekDay)
                }
            }
        }
    }

    private fun setNotConfiguredFrequencyValue() {
        val frequencyMap = ReminderFrequencyMap()
        frequencyMap.day = -1
        addReminderViewModel.reminderPOJO.reminderFrequencyMap = arrayListOf(frequencyMap)
    }
    //endregion

    fun validateData(isAdd: Boolean) {
        if (addReminderViewModel.reminderPOJO.reminder == null) {
            addReminderViewModel.reminderPOJO.reminder = Reminder()
        }
        val startDateStr = activityAddOrEditReminderBinding.addReminderStartDateET.text
        if (activityAddOrEditReminderBinding.addReminderStartDateET.text.isNullOrBlank()) {
            UIUtil.showToast(this, getString(R.string.enter_start_date), Toast.LENGTH_LONG)
            activityAddOrEditReminderBinding.addReminderStartDateET.requestFocus()
            return
        } else {

            addReminderViewModel.reminderPOJO.reminder?.startDate = UIUtil.formatDate(
                startDateStr.toString(),
                ModelUtil.charmDisplayDateFormat()
            )

            if (activityAddOrEditReminderBinding.addReminderIntakeET.text.toString() == getString(
                    R.string.one_time
                ) && addReminderViewModel.reminderPOJO.reminder?.startDate?.isBefore(
                    DateTime.now().minusDays(1)
                ) == true
            ) {
                UIUtil.showToast(
                    this,
                    R.string.add_reminder_date_less_than_currentDate_error,
                    Toast.LENGTH_SHORT
                )
                return
            }

            val currentDate = DateTime.now().withTimeAtStartOfDay()
            if ((addReminderViewModel.reminderPOJO.reminder?.startDate)?.isAfter(currentDate) == true) {
                addReminderViewModel.reminderPOJO.reminder?.reminderStatus =
                    ModelUtil.UpcomingStatus
            } else {
                addReminderViewModel.reminderPOJO.reminder?.reminderStatus = ModelUtil.ActiveStatus
            }

            if (activityAddOrEditReminderBinding.addReminderIntakeET.text.toString() == getString(
                    R.string.one_time
                )
            ) {
                addReminderViewModel.reminderPOJO.reminder?.endDate =
                    addReminderViewModel.reminderPOJO.reminder?.startDate
            }
        }
        if (activityAddOrEditReminderBinding.addReminderIntakeET.text.toString() != getString(R.string.one_time)) {
            val endDateStr =
                activityAddOrEditReminderBinding.addReminderEndDateET.text?.toString() ?: ""
            if (endDateStr.isBlank()) {
                UIUtil.showToast(this, getString(R.string.enter_end_date), Toast.LENGTH_LONG)
                activityAddOrEditReminderBinding.addReminderEndDateET.performClick()
                return
            } else {
                val fromDate =
                    UIUtil.formatDate(startDateStr.toString(), ModelUtil.charmDisplayDateFormat())
                val toDate = UIUtil.formatDate(endDateStr, ModelUtil.charmDisplayDateFormat())

                if (toDate?.isBefore(fromDate) == true) {
                    UIUtil.showToast(this, R.string.error_invalid_period, Toast.LENGTH_LONG)
                    return
                } else if (toDate?.isBefore(DateTime.now().minusDays(1)) == true) {
                    UIUtil.showToast(
                        this,
                        R.string.add_reminder_endDate_less_than_currentDate_error,
                        Toast.LENGTH_LONG
                    )
                    return
                }
            }
            addReminderViewModel.reminderPOJO.reminder?.endDate = UIUtil.formatDate(
                endDateStr,
                ModelUtil.charmDisplayDateFormat()
            )
        }
        if (activityAddOrEditReminderBinding.addReminderIntakeET.text.toString() == getString(R.string.weekly_str)) {
            val tempSelectedWeekDay = addReminderViewModel.selectedWeekDay.filter {
                it
            }
            if (tempSelectedWeekDay.isEmpty()) {
                UIUtil.showToast(this, R.string.select_week_day, Toast.LENGTH_LONG)
                activityAddOrEditReminderBinding.addReminderFrequencyUnitLL.visibility =
                    View.VISIBLE
                return
            }
        }

        if (addReminderViewModel.reminderTimingMap.isEmpty() || addReminderViewModel.reminderTimingMap[-1]?.isEmpty() == true) {
            UIUtil.showToast(this, R.string.enter_reminder_timing, Toast.LENGTH_LONG)
            return
        } else {
            val reminderFrequencyMapList = arrayListOf<ReminderFrequencyMap>()
            if (activityAddOrEditReminderBinding.addReminderIntakeET.text.toString() == getString(R.string.weekly_str)) {
                addReminderViewModel.selectedWeekDay.forEachIndexed { index, it ->
                    if (it) {
                        addReminderViewModel.reminderTimingMap[-1]?.forEach {
                            val reminderFrequencyMap =
                                ReminderFrequencyMap(
                                    reminderId = addReminderViewModel.reminderPOJO.reminder?.reminderId,
                                    day = index,
                                    time = it?.toString(ModelUtil.charmDisplay24HrTimeFormat)
                                )
                            reminderFrequencyMapList.add(reminderFrequencyMap)
                        }
                    }
                }
            } else {
                addReminderViewModel.reminderTimingMap[-1]?.forEach {
                    val reminderFrequencyMap = ReminderFrequencyMap(
                        reminderId = addReminderViewModel.reminderPOJO.reminder?.reminderId,
                        day = -1,
                        time = it?.toString(ModelUtil.charmDisplay24HrTimeFormat)
                    )
                    reminderFrequencyMapList.add(reminderFrequencyMap)
                }
            }
            addReminderViewModel.reminderPOJO.reminderFrequencyMap = reminderFrequencyMapList
        }
        when (activityAddOrEditReminderBinding.addReminderIntakeET.text.toString()) {
            getString(R.string.weekly_str) -> {
                addReminderViewModel.reminderPOJO.reminder?.frequencyUnit = reminderWeekly
                addReminderViewModel.reminderPOJO.reminder?.frequency = "0"
            }

            getString(R.string.daily_str) -> {
                addReminderViewModel.reminderPOJO.reminder?.frequencyUnit = reminderDaily
                addReminderViewModel.reminderPOJO.reminder?.frequency = "0"
            }

            getString(R.string.one_time) -> {
                addReminderViewModel.reminderPOJO.reminder?.frequencyUnit = reminderDaily
                addReminderViewModel.reminderPOJO.reminder?.frequency = "1"
            }
        }
        if (isAdd) {
            addReminder()
        } else {
            updateReminder()
        }
    }

    //region API calls
    private fun addReminder() {
        progressDialog.show()
        progressDialog.setMessage(R.string.adding_reminder)
        addReminderViewModel.addReminder().observe(this) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is Boolean) {
                    val resultIntent = Intent()
                    val type = addReminderViewModel.reminderPOJO.reminderMap.firstOrNull()?.type
                    if (type == ModelUtil.medicationAddedAsMed) {
                        val medicationObj = addReminderViewModel.addOrUpdateMedicationsInLocalDb(
                            MDParsingHelper.formMedicationListForDB(
                                arrayListOf(addReminderViewModel.medicationObj),
                                this
                            )
                        ).firstOrNull()
                        resultIntent.putExtra(ModelUtil.resModuleType, type)
                        resultIntent.putExtra(ModelUtil.resModuleObject, medicationObj)
                    } else if (type == ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() }) {
                        val supplementObj =
                            addReminderViewModel.addOrUpdateSupplementsInLocalDb(
                                MDParsingHelper.formSupplementListForDB(
                                    arrayListOf(addReminderViewModel.supplementObj)
                                )
                            )
                                .firstOrNull()
                        resultIntent.putExtra(ModelUtil.resModuleType, type)
                        resultIntent.putExtra(ModelUtil.resModuleObject, supplementObj)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    UIUtil.showToast(
                        this,
                        getString(R.string.reminder_add_success_msg),
                        Toast.LENGTH_SHORT
                    )
                    onBackClick()
                } else if (resp is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(
                        this,
                        resp.errorMsg ?: getString(R.string.requestFailedTryAgain),
                        R.string.retry_str,
                        false
                    ) { isRetry ->
                        if (isRetry) {
                            addReminder()
                        } else {
                            onBackClick()
                        }
                    }
                }
            }
        }
    }

    private fun updateReminder() {
        progressDialog.show()
        progressDialog.setMessage(R.string.updating_reminder)
        addReminderViewModel.updateReminder().observe(this) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is Boolean) {
                    setResult(Activity.RESULT_OK)
                    UIUtil.showToast(
                        this,
                        getString(R.string.reminder_update_success_msg),
                        Toast.LENGTH_SHORT
                    )
                    onBackClick()
                } else if (resp is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(
                        this,
                        resp.errorMsg ?: getString(R.string.requestFailedTryAgain),
                        R.string.retry_str,
                        false
                    ) { isRetry ->
                        if (isRetry) {
                            updateReminder()
                        } else {
                            onBackClick()
                        }
                    }
                }
            }
        }
    }

    private fun deleteReminder() {
        progressDialog.show()
        progressDialog.setMessage(R.string.deleting_reminder)
        addReminderViewModel.deleteReminder().observe(this) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is Boolean) {
                    setResult(Activity.RESULT_OK)
                    UIUtil.showToast(
                        this,
                        getString(R.string.reminder_delete_success_msg),
                        Toast.LENGTH_SHORT
                    )
                    onBackClick()
                } else if (resp is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(
                        this,
                        resp.errorMsg ?: getString(R.string.requestFailedTryAgain),
                        R.string.retry_str,
                        false
                    ) { isRetry ->
                        if (isRetry) {
                            deleteReminder()
                        } else {
                            onBackClick()
                        }
                    }
                }
            }
        }
    }
    //endregion

    //region DateTimePicker
    private fun showDatePicker(textView: TextView, isStartDate: Boolean = false) {

        val value = textView.text.toString().trim()

        val localDate = if (value.isNotBlank()) {
            UIUtil.formatDate(value, ModelUtil.charmDisplayDateFormat()) ?: DateTime.now()
        } else {
            DateTime.now()
        }


        val dialog = DatePickerDialog(
            this,
            { _, yr, month, dt ->

                val text = LocalDate(yr, month + 1, dt)
                textView.text = text.toString(ModelUtil.charmDisplayDateFormat())
                addReminderViewModel.isDataEdited = true


            },
            localDate.year, localDate.monthOfYear - 1, localDate.dayOfMonth
        )

        if (isStartDate) {
            if (addReminderViewModel.startDate?.isNotBlank() == true) {
                dialog.datePicker.minDate =
                    UIUtil.formatDate(
                        addReminderViewModel.startDate,
                        ModelUtil.charmDisplayDateFormat()
                    )?.millis ?: 0
            } else {
                dialog.datePicker.minDate = DateTime.now().millis
            }
        } else {
            if (addReminderViewModel.startDate?.isNotBlank() == true && UIUtil.formatDate(
                    addReminderViewModel.startDate,
                    ModelUtil.charmDisplayDateFormat()
                )?.isAfterNow == true
            ) {
                dialog.datePicker.minDate =
                    UIUtil.formatDate(
                        addReminderViewModel.startDate,
                        ModelUtil.charmDisplayDateFormat()
                    )?.millis ?: 0
            } else {
                dialog.datePicker.minDate = DateTime.now().millis
            }
        }
        if (addReminderViewModel.endDate?.isNotBlank() == true && UIUtil.formatDate(
                addReminderViewModel.endDate,
                ModelUtil.charmDisplayDateFormat()
            )?.plusDays(1)?.withTimeAtStartOfDay()?.minusSeconds(1)?.isAfterNow == true
        ) {
            UIUtil.formatDate(addReminderViewModel.endDate, ModelUtil.charmDisplayDateFormat())
            dialog.datePicker.maxDate =
                UIUtil.formatDate(
                    addReminderViewModel.endDate,
                    ModelUtil.charmDisplayDateFormat()
                )?.millis ?: 0
        } else {
            dialog.datePicker.maxDate =
                LocalDate.now().plusYears(150).toDateTimeAtStartOfDay().millis
        }
        dialog.show()
    }


    private fun showTimePicker(
        time: String = "",
        itemReminderTimingBinding: ItemReminderTimingBinding? = null,
        key: Int = -1
    ) {

        val dateTime = if (time.isNotBlank()) {
            DateTime.parse(time, ModelUtil.charmDisplayTimeFormat())
        } else {
            DateTime.now()
        }

        val dialog = MDTimePickerDialog(
            this,
            { _, hrs, min ->

                val selectedTime = UIUtil.formatDate(
                    "$hrs:$min",
                    ModelUtil.charmDisplay24HrTimeFormat()
                )

                if (itemReminderTimingBinding != null) {
                    activityAddOrEditReminderBinding.addReminderAddTimingLL.removeView(
                        itemReminderTimingBinding.root
                    )
                    addReminderViewModel.reminderTimingMap[key]?.remove(
                        UIUtil.formatDate(time, ModelUtil.charmDisplayTimeFormat())
                    )
                }

                val dayValue = -1
                if (addReminderViewModel.reminderTimingMap.containsKey(dayValue)) {
                    if (addReminderViewModel.reminderTimingMap[dayValue]?.contains(selectedTime) == false) {
                        addReminderViewModel.reminderTimingMap[dayValue]?.add(selectedTime)
                    } else {
                        UIUtil.showToast(
                            this,
                            getString(R.string.same_time_selected_error),
                            Toast.LENGTH_SHORT
                        )
                        return@MDTimePickerDialog
                    }
                } else {
                    addReminderViewModel.reminderTimingMap[dayValue] = arrayListOf(selectedTime)
                }
                setReminderTiming()
                addReminderViewModel.isDataEdited = true

            },
            dateTime.hourOfDay, dateTime.minuteOfHour, false, false
        )
        dialog.show()
    }

    //endregion

    private fun onBackPressAlert() {
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    onBackPressed()
                }

                DialogInterface.BUTTON_NEGATIVE -> {

                }
            }
        }

        backPressAlertDialogBuilder = AlertDialog.Builder(this)
        backPressAlertDialogBuilder.setMessage(R.string.discard_changes_confirmation_string)
            .setPositiveButton(R.string.yes_str, dialogClickListener)
            .setNegativeButton(R.string.no_str, dialogClickListener)
    }

    private fun onDeleteAlert() {
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    deleteReminder()
                }

                DialogInterface.BUTTON_NEGATIVE -> {

                }
            }
        }

        deleteAlertDialogBuilder = AlertDialog.Builder(this)
        deleteAlertDialogBuilder.setMessage(R.string.reminder_delete_confirmation_msg)
            .setPositiveButton(R.string.yes_str, dialogClickListener)
            .setNegativeButton(R.string.no_str, dialogClickListener)
    }

    //region Handling Permission for Notification
    private fun initializePermissionResultLauncher() {
        requestPermissionLauncher = PermissionUtil.getPermissionResultLauncher(
            this,
            ::permissionAcceptCallBack,
            R.string.allow_notification,
            null, false, ::permissionDeniedCallBack
        )
    }

    private fun permissionAcceptCallBack(permissions: Array<String>) {
        permissions.forEach { permission ->
            when (permission) {
                Manifest.permission.POST_NOTIFICATIONS -> {
                    if (!PermissionUtil.canScheduleExactAlarms(applicationContext)) {
                        UIUtil.showAlarmAndReminderDialog(this)
                    }
                }
            }
        }
    }

    private fun permissionDeniedCallBack(permissions: Array<String>) {
        PermissionUtil.requestPermission(permissions, requestPermissionLauncher)
    }

    private fun checkPermissionAndValidateData(isAdd: Boolean) {
        if (PermissionUtil.isNotificationPermissionAllowed(this)) {
            if (PermissionUtil.canScheduleExactAlarms(applicationContext)) {
                validateData(isAdd)
            } else {
                UIUtil.showAlarmAndReminderDialog(this)
            }
        } else {
            PermissionUtil.requestNotificationPermission(
                this,
                ::permissionAcceptCallBack,
                requestPermissionLauncher
            )
        }
    }
    //endregion

}