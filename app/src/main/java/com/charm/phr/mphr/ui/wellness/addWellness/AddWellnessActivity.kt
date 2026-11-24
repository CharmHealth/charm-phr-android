package com.charm.phr.mphr.ui.wellness.addWellness

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAddWellnessBinding
import com.charm.phr.mphr.databinding.ItemAddWellnessInputBinding
import com.charm.phr.mphr.databinding.ItemWellnessGeneralViewBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Metric
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientVitalTracker
import com.charm.phr.mphr.model.PatientVitals
import com.charm.phr.mphr.model.ScalePickerModel
import com.charm.phr.mphr.ui.components.Decimal2Filter
import com.charm.phr.mphr.ui.components.DecimalFilter
import com.charm.phr.mphr.ui.components.MDTextInputEdittext
import com.charm.phr.mphr.ui.components.MDTimePickerDialog
import com.charm.phr.mphr.ui.components.scaleView.ScaleValuePicker
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDAddBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.joda.time.DateTime
import org.joda.time.LocalDate
import javax.inject.Inject

class AddWellnessActivity : MDAddBaseActivity<AddWellnessViewModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private val wellnessGeneralTag = 101

    private var metricTag = 102

    private val tagToMetricIdMap = hashMapOf<Int, String>()

    lateinit var binding: ActivityAddWellnessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddWellnessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UIUtil.applyViewInsertForToolbar(binding.addWellnessToolbar)
        UIUtil.applyViewInsertForBottomView(binding.addWellnessRootLayout)

        val trackerMetricMapStr = intent.getStringExtra(ModelUtil.wellnessTrackerDetailIntent) ?: ""
        val metricId = intent.getStringExtra(ModelUtil.wellnessMetricIdIntent) ?: ""
        if (trackerMetricMapStr.isNotBlank()) {
            val trackerobj = trackerMetricMapStr.parseStringAsJsonObject()
            if (trackerobj != null) {
                addViewModel.trackerMetricMap =
                    MDParsingHelper.getVitalTrackerObj(trackerobj, metricId)
            } else {
                addViewModel.trackerMetricMap = null
            }
        }

        binding.addWellnessToolbarText.text = getString(
            R.string.add_wellness_toolbar_text,
            addViewModel.trackerMetricMap?.tracker?.trackerName
        )

        binding.addWellnessTimeET.inputType = InputType.TYPE_NULL

        addViewModel.minDate = LocalDate(LocalDate.now().year - 150, 1, 1).toDateTimeAtStartOfDay()
        addViewModel.maxDate = DateTime.now().withTimeAtStartOfDay().plusDays(1).minusSeconds(1)

        val currentDateTime = DateTime.now()
        binding.addWellnessDateET.text =
            currentDateTime.toString(ModelUtil.charmWellnessAddDateYearFormatter())

        binding.addWellnessTimeET.setText(
            currentDateTime.minusMinutes(currentDateTime.minuteOfHour % 5)
                .toString(ModelUtil.charmDisplayTimeFormat())
        )



        binding.addWellnessDateET.setOnClickListener {
            showDatePicker()
        }

        binding.addWellnessNextDateBtn.setOnClickListener {
            updateDate(true)
        }

        binding.addWellnessPrevDateBtn.setOnClickListener {
            updateDate(false)
        }

        dateChangeBtnUpdate()

        binding.addWellnessTimeET.setOnClickListener {
            showTimePicker(binding.addWellnessTimeET)
        }

        binding.addWellnessTimeET.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                showTimePicker(binding.addWellnessTimeET)
            }
        }

        binding.addWellnessBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addWellnessBtn.setOnClickListener {
            addWellness()
        }

        if (addViewModel.trackerMetricMap?.tracker?.isDefaultTracker == true && (addViewModel.trackerMetricMap?.tracker?.trackerName?.contains(
                ModelUtil.wellnessGeneralModelId
            ) == true || addViewModel.trackerMetricMap?.tracker?.trackerName?.contains(ModelUtil.wellnessTemperatureModelId) == true)
        ) {
            val patient = authManager.selectedPatientLive.value ?: Patient()

            if (patient.unitSystem == null) {
                getWellnessUnitSystem()
            } else if (patient.unitSystem.isNullOrBlank()) {
                if (BuildUtil.isThalassemia) {
                    updateWellness(ModelUtil.wellnessMetric)
                } else {
                    showWellnessUnitDialog()
                }
            } else {
                updateLocalUnits(patient.unitSystem ?: "")
            }
        } else {
            addViews()
        }
    }

    override fun checkDataIsAvailable(): Boolean {

        if (binding.addWellnessScroll.visibility == View.VISIBLE) {

            if (addViewModel.trackerMetricMap?.tracker?.isDefaultTracker == true && addViewModel.trackerMetricMap?.tracker?.trackerName?.contains(
                    ModelUtil.wellnessGeneralModelId,
                    true
                ) == true
            ) {
                val generalLayout = binding.addWellnessMainLayout.findViewWithTag<ConstraintLayout>(
                    wellnessGeneralTag
                )
                if (generalLayout == null) {
                    return false
                }
                val itemBinding = ItemWellnessGeneralViewBinding.bind(generalLayout)
                val heightMetric = addViewModel.trackerMetricMap?.metricList?.firstOrNull {
                    it.isDefaultMetric && it.metricName?.equals(
                        ModelUtil.wellnessHeightModelId,
                        true
                    ) == true
                }
                val weightValue = itemBinding?.addWellnessGeneralWeightET?.text?.toString() ?: ""

                if (weightValue.isNotBlank()) {
                    return true
                }





                if (heightMetric?.metricUnit?.contains("cm", true) == true) {
                    val cmsValue = itemBinding?.addWellnessGeneralHeightPrimaryET?.text?.toString()
                        ?.toIntOrNull() ?: 0

                    if (cmsValue != 0) {
                        return true
                    }


                } else {

                    val ftsValue = itemBinding?.addWellnessGeneralHeightPrimaryET?.text?.toString()
                        ?.toIntOrNull()
                        ?: 0
                    val inchesValue =
                        itemBinding?.addWellnessGeneralHeightSecondaryET?.text?.toString()
                            ?.toIntOrNull()
                            ?: 0

                    if (ftsValue != 0 || inchesValue != 0) {
                        return true
                    }

                }

            } else {

                if (tagToMetricIdMap.isNotEmpty()) {

                    tagToMetricIdMap.entries.forEach { mapOfTagAndId ->

                        val selectedView =
                            binding.addWellnessMainLayout.findViewWithTag<ViewGroup>(mapOfTagAndId.key)
                        val metric =
                            addViewModel.trackerMetricMap?.metricList?.firstOrNull { it.metricId == mapOfTagAndId.value }
                        selectedView?.let {
                            if (!(metric?.isDefaultMetric == true && addViewModel.trackerMetricMap?.tracker?.trackerName?.contains(
                                    ModelUtil.wellnessBPModelId,
                                    true
                                ) == true)
                            ) {
                                val itemBinding = ItemAddWellnessInputBinding.bind(selectedView)
                                if (metric != null) {
                                    val text =
                                        itemBinding.addWellnessInputET.text?.toString() ?: ""

                                    val value = text.toDoubleOrNull() ?: 0.0
                                    if (value != 0.0) {
                                        return true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }


    private fun addWellness() {

        val data = validateData()
        if (data != null) {
            progressDialog.show()
            addViewModel.addTracker(data).observe(this, Observer { resp ->
                if (resp != null) {
                    progressDialog.dismiss()
                    if (resp is Boolean) {
                        addViewModel.isDataAdded = true
                        setResult(Activity.RESULT_OK)
                        super.onBackPressed()
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                    } else if (resp is APIResponseError) {
                        UIUtil.showToast(
                            this,
                            R.string.error_add_wellness_failed,
                            Toast.LENGTH_LONG
                        )
                    }
                }
            })
        }
    }


    private fun validateData(): PatientVitalTracker? {


        val patientVitalTracker = PatientVitalTracker()

        patientVitalTracker.tracker = addViewModel.trackerMetricMap?.tracker

        val currentDateTimeStr = DateTime.parse(
            binding.addWellnessDateET.text.toString(),
            ModelUtil.charmWellnessAddDateYearFormatter()
        ).toString(ModelUtil.charmDisplayDateFormat())
        patientVitalTracker.dateOfEntry = DateTime.parse(
            "$currentDateTimeStr ${binding.addWellnessTimeET.text?.toString()}",
            ModelUtil.charmDateFormatForMessageList()
        )
        patientVitalTracker.notes = binding.addWellnessNotesET.text.toString()


        val alertValuesList = arrayListOf<String>()


        if (addViewModel.trackerMetricMap?.tracker?.isDefaultTracker == true && addViewModel.trackerMetricMap?.tracker?.trackerName?.contains(
                ModelUtil.wellnessGeneralModelId,
                true
            ) == true
        ) {
            val generalLayout =
                binding.addWellnessMainLayout.findViewWithTag<ConstraintLayout>(wellnessGeneralTag)
            val itemBinding = ItemWellnessGeneralViewBinding.bind(generalLayout)

            val weightMetric = addViewModel.trackerMetricMap?.metricList?.firstOrNull {
                it.isDefaultMetric && it.metricName?.equals(
                    ModelUtil.wellnessWeightModelId,
                    true
                ) == true
            }
            val heightMetric = addViewModel.trackerMetricMap?.metricList?.firstOrNull {
                it.isDefaultMetric && it.metricName?.equals(
                    ModelUtil.wellnessHeightModelId,
                    true
                ) == true
            }
            val bmiMetric = addViewModel.trackerMetricMap?.metricList?.firstOrNull {
                it.isDefaultMetric && it.metricName?.equals(
                    "bmi",
                    true
                ) == true
            }


            val weightValue = itemBinding.addWellnessGeneralWeightET.text.toString()

            if (isEditTextBlank(
                    itemBinding.addWellnessGeneralWeightET,
                    binding.addWellnessScroll,
                    R.string.error_empty_weight
                )
            ) {
                return null
            }

            val weight = weightValue.toDoubleOrNull() ?: 0.0

            if (weightMetric?.metricUnit?.contains("kg", true) == true) {

                if (weight < weightMinVal || weight > weightMaxVal) {
                    alertValuesList.add(weightMetric.metricName ?: "")
                }
            } else {
                if (weight < (weightMinVal * 2.2) || weight > (weightMaxVal * 2.2)) {
                    alertValuesList.add(weightMetric?.metricName ?: "")
                }
            }


            val patientWeightVitals = PatientVitals()
            patientWeightVitals.dateOfEntry = patientVitalTracker.dateOfEntry
            patientWeightVitals.metricId = weightMetric?.metricId
            patientWeightVitals.trackerId = patientVitalTracker.tracker?.trackerId
            patientWeightVitals.metricName = weightMetric?.metricName
            patientWeightVitals.trackedValueUnit = weightMetric?.metricUnit
            patientWeightVitals.trackedValue = weightValue


            val patientHeightVitals = PatientVitals()
            patientHeightVitals.dateOfEntry = patientVitalTracker.dateOfEntry
            patientHeightVitals.metricId = heightMetric?.metricId
            patientHeightVitals.trackerId = patientVitalTracker.tracker?.trackerId
            patientHeightVitals.metricName = heightMetric?.metricName
            patientHeightVitals.trackedValueUnit = heightMetric?.metricUnit





            if (heightMetric?.metricUnit?.contains("cm", true) == true) {
                val cmsValue =
                    itemBinding.addWellnessGeneralHeightPrimaryET.text.toString().toIntOrNull()
                        ?: 0

                if (cmsValue == 0) {
                    UIUtil.showToast(this, R.string.error_empty_height, Toast.LENGTH_LONG)
                    showFocus(
                        itemBinding.addWellnessGeneralHeightPrimaryET,
                        binding.addWellnessScroll
                    )
                    return null
                }

                if (cmsValue < heightMinVal || cmsValue > heightMaxVal) {
                    alertValuesList.add(heightMetric.metricName ?: "")
                }

                patientHeightVitals.trackedValue = "$cmsValue"

            } else {

                val ftsValue =
                    itemBinding.addWellnessGeneralHeightPrimaryET.text.toString().toIntOrNull()
                        ?: 0
                var inchesValue =
                    itemBinding.addWellnessGeneralHeightSecondaryET.text.toString().toIntOrNull()
                        ?: 0

                if (ftsValue == 0 && inchesValue == 0) {
                    UIUtil.showToast(this, R.string.error_empty_height, Toast.LENGTH_LONG)
                    showFocus(
                        itemBinding.addWellnessGeneralHeightPrimaryET,
                        binding.addWellnessScroll
                    )
                    return null
                }


                inchesValue += (ftsValue * 12)
                if (inchesValue < (heightMinVal.toDouble() / 2.54) || inchesValue > (heightMaxVal.toDouble() / 2.54)) { //formula to convert cms to inches cms = inches/ 2.54
                    alertValuesList.add(heightMetric?.metricName ?: "")
                }

                patientHeightVitals.trackedValue = "$inchesValue"
            }


            val patientBMIVitals = PatientVitals()
            patientBMIVitals.dateOfEntry = patientVitalTracker.dateOfEntry
            patientBMIVitals.metricId = bmiMetric?.metricId
            patientBMIVitals.trackerId = patientVitalTracker.tracker?.trackerId
            patientBMIVitals.metricName = bmiMetric?.metricName
            patientBMIVitals.trackedValueUnit = bmiMetric?.metricUnit
            patientBMIVitals.trackedValue = itemBinding.addWellnessGeneralBMIET.text.toString()


            patientVitalTracker.patientVitalsList.add(patientWeightVitals)
            patientVitalTracker.patientVitalsList.add(patientHeightVitals)
            patientVitalTracker.patientVitalsList.add(patientBMIVitals)

        } else {

            if (tagToMetricIdMap.isNotEmpty()) {

                tagToMetricIdMap.entries.forEach { mapOfTagAndId ->
                    val selectedView =
                        binding.addWellnessMainLayout.findViewWithTag<ViewGroup>(mapOfTagAndId.key)


                    val metric =
                        addViewModel.trackerMetricMap?.metricList?.firstOrNull { it.metricId == mapOfTagAndId.value }

                    if (metric != null) {

                        val patientVitals = PatientVitals()
                        patientVitals.dateOfEntry = patientVitalTracker.dateOfEntry
                        patientVitals.metricId = metric.metricId
                        patientVitals.trackerId = patientVitalTracker.tracker?.trackerId
                        patientVitals.metricName = metric.metricName
                        patientVitals.trackedValueUnit = metric.metricUnit

                        if (selectedView is ScaleValuePicker) {

                            val currentValue = selectedView.currentValue

                            if (metric.isDefaultMetric) {
                                if (metric.metricName?.contains(
                                        ModelUtil.metricSystolic,
                                        true
                                    ) == true
                                ) {
                                    if (currentValue < bpSystolicMinVal || currentValue > bpSystolicMaxVal) {
                                        alertValuesList.add(metric.metricName ?: "")
                                    }
                                } else if (metric.metricName?.contains(
                                        ModelUtil.metricDiastolic,
                                        true
                                    ) == true
                                ) {
                                    if (currentValue < bpDiastolicMinVal || currentValue > bpDiastolicMaxVal) {
                                        alertValuesList.add(metric.metricName ?: "")
                                    }
                                }
                            }

                            patientVitals.trackedValue = "${selectedView.currentValue}"
                        } else if (selectedView is ConstraintLayout) {
                            val itemBinding = ItemAddWellnessInputBinding.bind(selectedView)
                            val text = itemBinding.addWellnessInputET.text.toString()

                            if (text.toFloatOrNull() == null) {
                                UIUtil.showToast(
                                    this,
                                    getString(R.string.error_empty_value, metric.metricName),
                                    Toast.LENGTH_LONG
                                )
                                showFocus(itemBinding.addWellnessInputET, binding.addWellnessScroll)

                                return null
                            } else {

                                val value = text.toDoubleOrNull() ?: 0.0

                                if (metric.isDefaultMetric) {

                                    if (metric.metricName?.contains(
                                            ModelUtil.wellnessTemperatureModelId,
                                            true
                                        ) == true
                                    ) {
                                        if (metric.metricUnit?.contains("f", true) == true) {
                                            if (value < temperatureFahrenheitMinVal || value > temperatureFahrenheitMaxVal) {
                                                alertValuesList.add(metric.metricName ?: "")
                                            }
                                        } else {
                                            if (value < temperatureCelsiusMinVal || value > temperatureCelsiusMaxVal) {
                                                alertValuesList.add(metric.metricName ?: "")
                                            }
                                        }
                                    } else if (metric.metricName?.contains(
                                            ModelUtil.wellnessPulseModelId,
                                            true
                                        ) == true
                                    ) {
                                        if (value < pulseRateMinValue || value > pulseRateMaxValue) {
                                            alertValuesList.add(metric.metricName ?: "")
                                        }
                                    }

                                }
                                patientVitals.trackedValue = text.trim()
                            }
                        }
                        patientVitalTracker.patientVitalsList.add(patientVitals)
                    }

                }
            } else {

                UIUtil.showToast(this, (R.string.error_empty_metric_list), Toast.LENGTH_LONG)
                return null
            }

            /*
                        addViewModel.trackerMetricMap?.metricList?.forEachIndexed { index, metric ->
                            val patientVitals = PatientVitals()
                            patientVitals.dateOfEntry = patientVitalTracker.dateOfEntry
                            patientVitals.metricId = metric.metricId
                            patientVitals.trackerId = patientVitalTracker.tracker?.trackerId
                            patientVitals.metricName = metric.metricName
                            patientVitals.trackedValueUnit = metric.metricUnit

                            val childView = addWellnessMainLayout.getChildAt(index)


                            if (childView is ScaleValuePicker) {
                                patientVitals.trackedValue = "${childView.currentValue}"
                            } else if (childView is ConstraintLayout) {

                            }
                            patientVitalTracker.patientVitalsList.add(patientVitals)

                        }
            */
        }

        if (alertValuesList.isNotEmpty() && addViewModel.toCheckAlert) {
            showAddValidationAlert(alertValuesList)
            return null
        }

        return patientVitalTracker
    }

    private fun showAddValidationAlert(alertValues: ArrayList<String>) {

        val alertDialogBuilder = AlertDialog.Builder(this)
        if (alertValues.size == 1) {
            alertDialogBuilder.setMessage(
                getString(
                    R.string.wellness_add_alert_single_item_str,
                    alertValues[0]
                )
            )
        } else {
            alertDialogBuilder.setMessage(R.string.wellness_add_alert_multiple_item_str)
        }

        alertDialogBuilder.setPositiveButton(R.string.yes_str) { _, _ ->
            addViewModel.toCheckAlert = false
            addWellness()
        }

        alertDialogBuilder.setNegativeButton(R.string.no_str) { _, _ ->

        }

        alertDialogBuilder.show()
    }


    private fun addViews() {

        if (addViewModel.trackerMetricMap?.tracker?.trackerName?.contains(
                ModelUtil.wellnessGeneralModelId,
                true
            ) == true
        ) {
            binding.addWellnessMainLayout.addView(createGeneralView())
            binding.addWellnessSelectParamLayout.visibility = View.GONE
        } else if (addViewModel.trackerMetricMap?.tracker?.isDefaultTracker == false || (addViewModel.trackerMetricMap?.tracker?.isDefaultTracker == true && addViewModel.trackerMetricMap?.tracker?.trackerName?.contains(
                ModelUtil.wellnessBloodSugarModelId,
                true
            ) == true)
        ) {
            addViewModel.trackerMetricMap?.metricList?.forEach { metric ->
                addChipMetric(metric)
            }
            binding.addWellnessSelectParamLayout.visibility = View.VISIBLE
        } else {
            binding.addWellnessSelectParamLayout.visibility = View.GONE


            addViewModel.trackerMetricMap?.metricList?.forEach { metric ->

                if (metric.isDefaultMetric) {

                    if (addViewModel.trackerMetricMap?.tracker?.trackerName?.contains(
                            ModelUtil.wellnessBPModelId,
                            true
                        ) == true
                    ) {
                        val scalePickerModel = ScalePickerModel()
                        scalePickerModel.label = metric.metricName
                        scalePickerModel.unit = metric.metricUnit

                        /*if (metric.metricName?.contains(ModelUtil.wellnessPulseModelId, true) == true) {
                            scalePickerModel.minValue = 50
                            scalePickerModel.maxValue = 120
                            scalePickerModel.selectedValue = 60
                        } else*/
                        if (metric.metricName?.contains(ModelUtil.metricSystolic, true) == true) {
                            scalePickerModel.minValue = 0
                            scalePickerModel.maxValue = 200
                            scalePickerModel.selectedValue = 120
                        } else if (metric.metricName?.contains(
                                ModelUtil.metricDiastolic,
                                true
                            ) == true
                        ) {
                            scalePickerModel.minValue = 0
                            scalePickerModel.maxValue = 150
                            scalePickerModel.selectedValue = 80
                        }

                        val scaleValuePicker = createScalePickerView(scalePickerModel, metric)
                        binding.addWellnessMainLayout.addView(scaleValuePicker)
                    } else {
                        createInputView(metric)
                    }
                } else {
                    /*val scalePickerModel = ScalePickerModel()
                    scalePickerModel.label = metric.metricName
                    scalePickerModel.unit = metric.metricUnit
                    scalePickerModel.selectedValue = 60
                    val scaleValuePicker = createScalePickerView(scalePickerModel ,  metric)
                    addWellnessMainLayout.addView(scaleValuePicker)*/
                    createInputView(metric)
                }
            }
        }
    }


    private fun createGeneralView(): ConstraintLayout {
        val cardView = ItemWellnessGeneralViewBinding.inflate(
            layoutInflater,
            binding.addWellnessMainLayout,
            false
        )


        cardView.root.tag = wellnessGeneralTag

        val heightMetricUnit = addViewModel.trackerMetricMap?.metricList?.firstOrNull {
            it.isDefaultMetric && it.metricName?.contains(
                ModelUtil.wellnessHeightModelId,
                true
            ) == true
        }?.metricUnit
            ?: ""
        val weightMetricUnit = addViewModel.trackerMetricMap?.metricList?.firstOrNull {
            it.isDefaultMetric && it.metricName?.contains(
                ModelUtil.wellnessWeightModelId,
                true
            ) == true
        }?.metricUnit
            ?: ""

        UIUtil.makeTextViewMandatory(cardView.addWellnessGeneralHeightLabel)
        UIUtil.makeTextViewMandatory(cardView.addWellnessGeneralWeightLabel)


        Log.d("MDGeneralHint", "weight :$weightMetricUnit ; height  :$heightMetricUnit ")


        var weightMaxValue = 200
        var heightMaxValue = 200
        if (heightMetricUnit.equals("inches", true)) {

            weightMaxValue = 440
            heightMaxValue = 10
            val constraintSet = ConstraintSet()
            constraintSet.clone(cardView.root)
            constraintSet.clear(cardView.addWellnessGeneralHeightPrimaryET.id, ConstraintSet.END)
            constraintSet.connect(
                cardView.addWellnessGeneralHeightPrimaryET.id,
                ConstraintSet.END,
                cardView.addWellnessGeneralHeightCentreGuideLine.id,
                ConstraintSet.START
            )
            constraintSet.applyTo(cardView.root)

            val lp =
                cardView.addWellnessGeneralHeightPrimaryET.layoutParams as ConstraintLayout.LayoutParams
            lp.marginEnd = resources.getDimensionPixelSize(R.dimen.card_padding_half)
            cardView.addWellnessGeneralHeightPrimaryET.layoutParams = lp

            cardView.addWellnessGeneralHeightPrimaryET.hint = "ft"
            cardView.addWellnessGeneralHeightSecondaryET.hint = "in"

            cardView.addWellnessGeneralHeightSecondaryET.visibility = View.VISIBLE

            cardView.addWellnessGeneralHeightSecondaryET.addTextChangedListener(object :
                TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    val value = text.toIntOrNull()

                    if (value != null) {
                        if (value > 12) {
                            cardView.addWellnessGeneralHeightSecondaryET.setText(
                                text.substring(
                                    0,
                                    text.length - 1
                                )
                            )
                        } else if (value == 12) {

                            var primaryValue =
                                cardView.addWellnessGeneralHeightPrimaryET?.text?.toString()
                                    ?.toIntOrNull()
                                    ?: 0

                            if (primaryValue == heightMaxValue) {
                                cardView.addWellnessGeneralHeightSecondaryET.setText(
                                    text.substring(
                                        0,
                                        text.length - 1
                                    )
                                )
                            } else {
                                cardView.addWellnessGeneralHeightSecondaryET.setText("")
                                primaryValue += 1
                                cardView.addWellnessGeneralHeightPrimaryET.setText("$primaryValue")
                            }
                        }
                    }

                    updateBMI()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }
            })

        } else {
            cardView.addWellnessGeneralHeightPrimaryET.hint = heightMetricUnit
        }

        cardView.addWellnessGeneralHeightPrimaryET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                val text = s?.toString() ?: ""
                val value = text.toIntOrNull()

                if (value != null && value > heightMaxValue) {
                    cardView.addWellnessGeneralHeightPrimaryET.setText(
                        text.substring(
                            0,
                            text.length - 1
                        )
                    )
                }

                updateBMI()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })


        val primaryFilter = cardView.addWellnessGeneralWeightET.filters.toCollection(ArrayList())
        primaryFilter.add(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Decimal2Filter()
            } else {
                DecimalFilter()
            }
        )
        cardView.addWellnessGeneralWeightET.filters = primaryFilter.toTypedArray()

        cardView.addWellnessGeneralWeightET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                val text = s?.toString() ?: ""
                val value = text.toIntOrNull()

                if (value != null && value > weightMaxValue) {
                    cardView.addWellnessGeneralWeightET.setText(text.substring(0, text.length - 1))
                }

                updateBMI()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        cardView.addWellnessGeneralWeightET.hint = weightMetricUnit

        return cardView.root
    }


    private fun createInputView(metric: Metric, showRemove: Boolean = false) {

        val cardView = ItemAddWellnessInputBinding.inflate(
            layoutInflater,
            binding.addWellnessMainLayout,
            false
        )

        tagToMetricIdMap[metricTag] = metric.metricId ?: ""
        cardView.root.tag = metricTag

        metricTag++

        cardView.addWellnessInputLabel.setText(metric.metricName)

        if (addViewModel.trackerMetricMap?.tracker?.isDefaultTracker == true) {
            if (addViewModel.trackerMetricMap?.tracker?.trackerName?.equals(
                    ModelUtil.wellnessBloodSugarModelId,
                    true
                ) != true
            ) {
                UIUtil.makeTextViewMandatory(cardView.addWellnessInputLabel)
            }
        }

        val primaryFilter = cardView.addWellnessInputET.filters.toCollection(ArrayList())
        primaryFilter.add(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Decimal2Filter()
            } else {
                DecimalFilter()
            }
        )

        cardView.addWellnessInputET.filters = primaryFilter.toTypedArray()

        var maxValue = 200


        var isDecimal = true

        if (metric.metricUnit?.equals("minutes", true) == true || metric.metricUnit?.equals(
                "min",
                true
            ) == true
        ) {
            isDecimal = false
            maxValue = 300
        }
        if (metric.metricUnit?.equals(
                "percent",
                true
            ) == true || metric.metricUnit?.equals(
                "percentage",
                true
            ) == true || metric.metricUnit?.equals("%", true) == true
        ) {
            isDecimal = false
            maxValue = 100
        } else if (metric.metricUnit?.equals("ml", true) == true) {
            isDecimal = false
            maxValue = 6000
        } else if (metric.metricUnit?.equals(
                "hours",
                true
            ) == true || metric.metricUnit?.equals("hrs", true) == true
        ) {
            isDecimal = false
            maxValue = 24
        } else if (metric.metricUnit?.equals("calories", true) == true) {
            maxValue = 10000
        } else if (metric.isDefaultMetric && metric.metricName?.contains(
                ModelUtil.wellnessTemperatureModelId,
                true
            ) == true
        ) {
            if (metric.metricUnit?.equals("f", true) == true) {
                maxValue = 110
            } else {
                maxValue = 43
            }
        }

        if (!isDecimal) {
            cardView.addWellnessInputET.inputType = InputType.TYPE_CLASS_NUMBER
        }


        cardView.addWellnessInputET.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {

                    val text = s?.toString() ?: ""
                    val value = text.toIntOrNull()

                    if (value != null && value > maxValue) {
                        cardView.addWellnessInputET.setText(text.substring(0, text.length - 1))
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }
            })

        if (metric.isDefaultMetric && addViewModel.trackerMetricMap?.tracker?.isDefaultTracker == true) {
            val metricUnit = metric.metricUnit ?: ""
            if (metric.metricName?.contains(ModelUtil.wellnessTemperatureModelId, true) == true) {
                cardView.addWellnessInputET.hint = "\u00B0" + metricUnit
            } else {
                cardView.addWellnessInputET.hint = metricUnit
            }
        } else {
            cardView.addWellnessInputET.hint = metric.metricUnit
        }

        if (showRemove) {
            cardView.addWellnessInputDelete.visibility = View.VISIBLE

            cardView.addWellnessInputDelete.setOnClickListener {
                tagToMetricIdMap.remove(cardView.root.tag as Int)
                binding.addWellnessMainLayout.removeView(cardView.root)
                addChipMetric(metric)
            }
        } else {
            cardView.addWellnessInputDelete.visibility = View.GONE
        }
        binding.addWellnessMainLayout.addView(cardView.root)
    }

    private fun createScalePickerView(
        scalePickerModel: ScalePickerModel,
        metric: Metric
    ): ScaleValuePicker {
        val scaleValuePicker = ScaleValuePicker(this)
        val param = LinearLayout.LayoutParams(
            MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.wellness_scale_height)
        )

        tagToMetricIdMap[metricTag] = metric.metricId ?: ""
        scaleValuePicker.tag = metricTag
        metricTag++
        val margin = try {
            resources.getDimensionPixelSize(R.dimen.card_padding).toInt()
        } catch (e: Exception) {
            0
        }

        param.topMargin = margin
        param.marginStart = margin
        param.marginEnd = margin

        if (metric.isDefaultMetric && metric.metricName?.contains(
                ModelUtil.metricDiastolic,
                true
            ) == true
        ) {
            param.bottomMargin = margin
        }




        scaleValuePicker.cardElevation =
            resources.getDimensionPixelSize(R.dimen.common_card_elevation).toFloat()
        scaleValuePicker.radius =
            resources.getDimensionPixelSize(R.dimen.common_card_radius).toFloat()

        scaleValuePicker.indicatorColor =
            ContextCompat.getColor(this, R.color.scaleViewIndicatorColor)
        scaleValuePicker.selectedIndicatorColor =
            ContextCompat.getColor(this, R.color.scaleViewNotchColor)
        scaleValuePicker.textColor = ContextCompat.getColor(this, android.R.color.black)
        scaleValuePicker.setIndicatorIntervalDistanceRes(R.dimen.scaleview_indicator_interval)
        scaleValuePicker.setIndicatorWidthRes(R.dimen.scaleview_indicator_width)
        scaleValuePicker.setTextSizeRes(R.dimen.xx_medium)

        scaleValuePicker.label = scalePickerModel.label
        scaleValuePicker.setUnitText(scalePickerModel.unit ?: "")
        scaleValuePicker.setMinMaxValue(scalePickerModel.minValue, scalePickerModel.maxValue)
        scaleValuePicker.selectValue(scalePickerModel.selectedValue)

        scaleValuePicker.layoutParams = param
        return scaleValuePicker
    }


    private fun addChipMetric(metric: Metric) {

        val chip = Chip(this)
        chip.layoutParams = ChipGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

        chip.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.xx_small)
        )
        chip.setTextColor(ContextCompat.getColor(this, R.color.black87))
        chip.setChipBackgroundColorResource(R.color.addWellnessChipBgColor)
        chip.setChipIconResource(R.drawable.ic_add_wellness_chip)
        chip.setChipIconTintResource(R.color.colorPrimary)
        chip.text = metric.metricName


        chip.setOnClickListener {


            createInputView(metric, true)
            binding.addWellnessChipGrp.removeView(chip)
        }

        binding.addWellnessChipGrp.addView(chip)
    }

    private fun updateBMI() {

        val generalLayout =
            binding.addWellnessMainLayout.findViewWithTag<ConstraintLayout>(wellnessGeneralTag)
        val itemBinding = ItemWellnessGeneralViewBinding.bind(generalLayout)

        val weightET = itemBinding.addWellnessGeneralWeightET
        val heightPrimaryET = itemBinding.addWellnessGeneralHeightPrimaryET
        val heightSecondaryET = itemBinding.addWellnessGeneralHeightSecondaryET
        val bmiET = itemBinding.addWellnessGeneralBMIET

        if (weightET != null && heightPrimaryET != null && heightSecondaryET != null && bmiET != null) {


            val weightStr = weightET.text?.toString()
            val weightValue = weightStr?.toDoubleOrNull() ?: 0.0

            val heightStr = heightPrimaryET.text?.toString()

            val heightPrimaryValue = heightStr?.toDoubleOrNull() ?: 0.0

            if (addViewModel.trackerMetricMap?.metricList?.firstOrNull {
                    it.metricName?.contains(
                        ModelUtil.wellnessWeightModelId,
                        true
                    ) == true
                }?.metricUnit?.contains("kg") == true) {


                if (weightStr.isNullOrBlank() || heightStr.isNullOrBlank()) {
                    bmiET.setText("")
                } else {

                    val bmi =
                        100 * 100 * weightValue / (heightPrimaryValue * heightPrimaryValue) // multiplication by 100*100 for cm to m conversion
                    if (bmi == 0.0) {
                        bmiET.setText("")
                    } else {
                        bmiET.setText(ModelUtil.getTwoDecimalStr(bmi))
                    }
                }
            } else {

                val secondaryHeightStr = heightSecondaryET.text?.toString()

                val heightSecondaryValue = secondaryHeightStr?.toDoubleOrNull() ?: 0.0

                val inchesValue = (heightPrimaryValue * 12) + heightSecondaryValue

                if (weightStr.isNullOrBlank() || (heightStr.isNullOrBlank() && secondaryHeightStr.isNullOrBlank())) {
                    bmiET.setText("")
                } else {

                    val bmi = 703 * weightValue / (inchesValue * inchesValue)
                    if (bmi == 0.0) {
                        bmiET.setText("")
                    } else {
                        bmiET.setText(ModelUtil.getTwoDecimalStr(bmi))
                    }
                }
            }

        }
    }

    private fun showTimePicker(edittext: MDTextInputEdittext) {

        val value = edittext.text.toString().trim()

        val dateTime = if (value.isNotBlank()) {
            DateTime.parse(value, ModelUtil.charmDisplayTimeFormat())
        } else {
            DateTime.now()
        }


        val dialog = MDTimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hrs, min ->

                val selectedTime =
                    DateTime.parse("$hrs:$min", ModelUtil.charmDisplay24HrTimeFormat())
                edittext.setText(selectedTime.toString(ModelUtil.charmDisplayTimeFormat))

            },
            dateTime.hourOfDay, dateTime.minuteOfHour, false
        )





        dialog.show()
    }


    private fun updateDate(toAdd: Boolean) {

        val currentSelectedDate = DateTime.parse(
            binding.addWellnessDateET.text.toString().trim(),
            ModelUtil.charmWellnessAddDateYearFormatter()
        )
        if (toAdd) {
            val nextDay = currentSelectedDate.plusDays(1)
            binding.addWellnessDateET.text =
                nextDay.toString(ModelUtil.charmWellnessAddDateYearFormatter())
        } else {
            val prevDate = currentSelectedDate.minusDays(1)
            binding.addWellnessDateET.text =
                prevDate.toString(ModelUtil.charmWellnessAddDateYearFormatter())
        }

        dateChangeBtnUpdate()
    }

    private fun dateChangeBtnUpdate() {
        val currentSelectedDate = DateTime.parse(
            binding.addWellnessDateET.text.toString().trim(),
            ModelUtil.charmWellnessAddDateYearFormatter()
        )

        binding.addWellnessNextDateBtn.isEnabled =
            currentSelectedDate.plusDays(1).millis <= addViewModel.maxDate.millis
        binding.addWellnessPrevDateBtn.isEnabled =
            currentSelectedDate.minusDays(1).millis >= addViewModel.minDate.millis
    }


    private fun showDatePicker() {

        val value = binding.addWellnessDateET.text.toString().trim()

        val localDate = if (value.isNotBlank()) {
            LocalDate.parse(value, ModelUtil.charmWellnessAddDateYearFormatter())
        } else {
            LocalDate.now()
        }

        val dialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, yr, mnth, dt ->

                val text = LocalDate(yr, mnth + 1, dt)

                binding.addWellnessDateET.text =
                    text.toString(ModelUtil.charmWellnessAddDateYearFormatter())
                dateChangeBtnUpdate()

            },
            localDate.year, localDate.monthOfYear - 1, localDate.dayOfMonth
        )

        dialog.datePicker.minDate = addViewModel.minDate.millis
        dialog.datePicker.maxDate = addViewModel.maxDate.millis

        dialog.show()
    }

    private fun getWellnessUnitSystem() {
        progressDialog.show()
        progressDialog.setMessage(R.string.loading_data)
        addViewModel.getWellnessUnit().observe(this, Observer { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is String) {

                    if (resp.isBlank()) {
                        showWellnessUnitDialog()
                    } else {
                        updateLocalUnits(resp)
                    }
                } else if (resp is APIResponseError) {

                    val alertDialogBuilder = AlertDialog.Builder(this)
                    alertDialogBuilder.setMessage(R.string.error_wellness_unit_type_get_failed)
                    alertDialogBuilder.setPositiveButton(R.string.retry_str) { _, _ ->
                        getWellnessUnitSystem()
                    }

                    alertDialogBuilder.setNegativeButton(R.string.cancel_str) { _, _ ->
                        super.onBackPressed()
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

                    }

                    alertDialogBuilder.setOnCancelListener {
                        super.onBackPressed()
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

                    }
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.setCanceledOnTouchOutside(false)

                    alertDialog.show()
                }
            }
        })
    }

    private fun showWellnessUnitDialog(unitSystem: String? = null) {

        val screenWidth = UIUtil.getScreenWidth(this)
        val width =
            screenWidth - (2 * resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))

        val unitDialog = WellnessUnitSystemDialog(this, width)

        if (unitSystem != null) {
            unitDialog.isMetric = ModelUtil.wellnessMetric == unitSystem
        }

        unitDialog.setSelectUnitSystemListener(object :
            WellnessUnitSystemDialog.SelectUnitSystemListener {
            override fun onSelectUnitSystem(unitSystem: String) {
                updateWellness(unitSystem)
            }
        })

        unitDialog.setOnCancelListener {
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        unitDialog.show()
    }

    private fun updateWellness(unitSystem: String) {

        progressDialog.show()
        progressDialog.setMessage(R.string.update_data_str)

        addViewModel.updateWellnessUnit(unitSystem).observe(this, Observer { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is Boolean) {
                    updateLocalUnits(unitSystem)
                } else if (resp is APIResponseError) {
                    UIUtil.showToast(
                        this,
                        R.string.error_unit_system_update_failed,
                        Toast.LENGTH_LONG
                    )
                    showWellnessUnitDialog(unitSystem)
                }
            }
        })
    }

    private fun updateLocalUnits(unitSystem: String) {

        if (addViewModel.trackerMetricMap?.tracker?.trackerName?.contains(
                ModelUtil.wellnessGeneralModelId,
                true
            ) == true
        ) {


            val heightMetric = addViewModel.trackerMetricMap?.metricList?.firstOrNull {
                it.isDefaultMetric && it.metricName?.contains(
                    ModelUtil.wellnessHeightModelId,
                    true
                ) == true
            }
            val weightMetric = addViewModel.trackerMetricMap?.metricList?.firstOrNull {
                it.isDefaultMetric && it.metricName?.contains(
                    ModelUtil.wellnessWeightModelId,
                    true
                ) == true
            }

            if (unitSystem == ModelUtil.wellnessMetric) {
                heightMetric?.metricUnit = "cms"
                weightMetric?.metricUnit = "kgs"
            } else {
                heightMetric?.metricUnit = "inches"
                weightMetric?.metricUnit = "lbs"
            }
        } else if (addViewModel.trackerMetricMap?.tracker?.trackerName?.contains(
                ModelUtil.wellnessTemperatureModelId,
                true
            ) == true
        ) {

            val metric = addViewModel.trackerMetricMap?.metricList?.firstOrNull {
                it.isDefaultMetric && it.metricName?.contains(
                    ModelUtil.wellnessTemperatureModelId,
                    true
                ) == true
            }

            if (unitSystem == ModelUtil.wellnessMetric) {
                metric?.metricUnit = "F"
            } else {
                metric?.metricUnit = "C"
            }
        }

        addViews()
    }


    companion object {

        const val weightMaxVal = 150
        const val weightMinVal = 5

        const val heightMaxVal = 210
        const val heightMinVal = 50

        const val bpSystolicMaxVal = 200
        const val bpSystolicMinVal = 90

        const val bpDiastolicMaxVal = 100
        const val bpDiastolicMinVal = 60

        const val temperatureCelsiusMaxVal = 39
        const val temperatureCelsiusMinVal = 36

        const val temperatureFahrenheitMaxVal = 103
        const val temperatureFahrenheitMinVal = 97


        const val pulseRateMaxValue = 100
        const val pulseRateMinValue = 60

        const val spo2MaxValue = 101
        const val spo2MinValue = 95

        const val respiratoryRateMaxValue = 25
        const val respiratoryRateMinValue = 12


    }

    override fun checkDataIsEditted(): Boolean {
        return false
    }

    override fun getViewModel(): AddWellnessViewModel {
        return ViewModelProvider(this, viewModelFactory).get(AddWellnessViewModel::class.java)
    }
}