package com.charm.phr.mphr.ui.flowsheet.add

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAddFlowsheetDataBinding
import com.charm.phr.mphr.databinding.ItemAddFlowsheetDateBinding
import com.charm.phr.mphr.databinding.ItemAddFlowsheetInstructionBinding
import com.charm.phr.mphr.databinding.ItemAddFlowsheetLayoutBinding
import com.charm.phr.mphr.databinding.ItemAddFlowsheetVitalLabBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.FlowsheetEntries
import com.charm.phr.mphr.model.FlowsheetVital
import com.charm.phr.mphr.model.PHRFlowsheets
import com.charm.phr.mphr.ui.components.MDTextInputEdittext
import com.charm.phr.mphr.ui.wellness.addWellness.AddWellnessActivity
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.slider.Slider
import com.google.gson.JsonObject
import com.nex3z.flowlayout.FlowLayout
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import javax.inject.Inject

class AddFlowsheetDataActivity : MDBaseActivity() {

    lateinit var addFlowsheetDataViewModel: AddFlowsheetDataViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreference: SharedPreferences


    val tagToIdMap = hashMapOf<String, String>()
    val radioToNotesMap = hashMapOf<Int, Int>()
    val sliderIsChangedList = arrayListOf<Int>()

    var tagNo = 0


    var prevET: MDTextInputEdittext? = null

    var bmiCalListTag = arrayListOf<ArrayList<String>>()

    lateinit var imm: InputMethodManager

    private val degree = "\u00B0"

    private lateinit var binding: ActivityAddFlowsheetDataBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFlowsheetDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addFlowsheetDataViewModel =
            ViewModelProvider(this, viewModelFactory).get(AddFlowsheetDataViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.addFlowsheetDataToolbar)
        UIUtil.applyViewInsertForBottomView(binding.addFlowsheetDataRootLayout)

        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        val flowsheetStr = intent.getStringExtra(ModelUtil.flowsheetListSelectIntent)
        if (!flowsheetStr.isNullOrBlank()) {
            addFlowsheetDataViewModel.flowsheet = MDParsingHelper.formFlowsheetObj(
                flowsheetStr.parseStringAsJsonObject() ?: JsonObject()
            )
            addFlowsheetDataViewModel.flowsheet.practiceId =
                intent.getStringExtra(ModelUtil.practiceIdIntent)
        }

        val phrFlowsheetStr = intent.getStringExtra(ModelUtil.flowsheetDataSelectIntent)

        if (!phrFlowsheetStr.isNullOrBlank()) {
            binding.addFlowsheetDataToolbarText.setText(R.string.edit_flowsheet_entries_txt)
            binding.addFlowsheetDataBtn.setText(R.string.update_str)
            addFlowsheetDataViewModel.phrFlowsheets = MDParsingHelper.formFlowsheetEntryObj(
                phrFlowsheetStr.parseStringAsJsonObject() ?: JsonObject()
            )

            binding.addFlowsheetDataObservedOnTV.text =
                addFlowsheetDataViewModel.phrFlowsheets?.date?.toString(ModelUtil.charmFlowsheetDataDateFormatter())
        } else {
            binding.addFlowsheetDataObservedOnTV.text =
                DateTime.now().toString(ModelUtil.charmFlowsheetDataDateFormatter())
        }


        binding.addFlowsheetDataToolbarText.text = addFlowsheetDataViewModel.flowsheet.name


        if (addFlowsheetDataViewModel.flowsheet.allowEdit) {
            binding.addFlowsheetDataBtn.visibility = View.VISIBLE
            binding.addFlowsheetDataBtn.setOnClickListener {
                onSubmitFlowsheet()
            }
            binding.addFlowsheetDataObservedOnCardview.setOnClickListener {
                showDatePicker(binding.addFlowsheetDataObservedOnTV, true)
            }
        } else {
            binding.addFlowsheetDataBtn.visibility = View.GONE
            binding.addFlowsheetDataObservedOnCardview.isClickable = false

        }

        binding.addFlowsheetDataBackBtn.setOnClickListener {
            onBackPressed()
        }


        renderFlowsheetData()

    }

    private fun onSubmitFlowsheet() {
        val phrFlowsheets = validateData()
        if (phrFlowsheets != null) {

            progressDialog.show()
            addFlowsheetDataViewModel.updateFlowsheet(phrFlowsheets)
                .observe(this, Observer { resp ->
                    if (resp != null) {
                        progressDialog.dismiss()
                        if (resp is Boolean) {
                            val flowsheetName = addFlowsheetDataViewModel.flowsheet.name
                            val prefKey = when {
                                flowsheetName.equals(
                                    ModelUtil.FLOWSHEET_TRANSFUSION_LOG,
                                    ignoreCase = true
                                ) ->
                                    ModelUtil.TRANSFUSION_LOG_UPDATED

                                flowsheetName.equals(
                                    ModelUtil.FLOWSHEET_FERRITIN_MONITORING,
                                    ignoreCase = true
                                ) ->
                                    ModelUtil.FERRITIN_MONITORING_UPDATED

                                flowsheetName.equals(
                                    ModelUtil.FLOWSHEET_MRI_T2_MONITORING,
                                    ignoreCase = true
                                ) ->
                                    ModelUtil.MRI_T2_MONITORING_UPDATED

                                else -> null
                            }
                            val prefEdit = sharedPreference.edit()
                            prefEdit.putBoolean(prefKey, true)
                            prefEdit.apply()
                            setResult(Activity.RESULT_OK)
                            onBackPressed()
                        } else if (resp is APIResponseError) {

                            if (phrFlowsheets.phrFlowsheetId.isNullOrBlank()) {
                                UIUtil.showToast(
                                    this,
                                    R.string.error_add_flowsheet_req_failed,
                                    Toast.LENGTH_LONG
                                )
                            } else {
                                UIUtil.showToast(
                                    this,
                                    R.string.error_edit_flowsheet_req_failed,
                                    Toast.LENGTH_LONG
                                )
                            }
                        }
                    }
                })
        }
    }

    private fun validateData(): PHRFlowsheets? {

        val flowsheetEntryList = arrayListOf<FlowsheetEntries>()


        val alertList = arrayListOf<String>()

        tagToIdMap.iterator().forEach { tagToId ->

            val tag = tagToId.key
            val flowsheetEntryId = tagToId.value


            val flowsheetEntry =
                addFlowsheetDataViewModel.flowsheet.flowsheetEntries.firstOrNull { it.flowSheetEntryId == flowsheetEntryId }
                    ?: FlowsheetEntries()


            if (flowsheetEntry.type == ModelUtil.flowsheetEntryTypeVitals) {
                val valueList = arrayListOf<String>()
                val tagList = tag.split(",").mapNotNull { it.toIntOrNull() }
                tagList.forEachIndexed { index, vitalTag ->

                    val vitalView =
                        binding.addFlowsheetDataMainLayout.findViewWithTag<View>(vitalTag)

                    if (vitalView is MDTextInputEdittext) {
                        var vitalValueStr = vitalView.text.toString() ?: ""
                        if (vitalValueStr.isEmpty()) {
                            vitalValueStr = " "
                        } else if (vitalValueStr.toDoubleOrNull() != null) {
                            val vitalValue = vitalValueStr.toDouble()
                            val flowsheetFieldName = flowsheetEntry.name ?: ""
                            if (index == 0) {
                                if (flowsheetFieldName.equals(
                                        ModelUtil.wellnessWeightModelId,
                                        true
                                    )
                                ) {
                                    if (tagList.size == 1) {//kgs
                                        if (vitalValue < AddWellnessActivity.weightMinVal || vitalValue > AddWellnessActivity.weightMaxVal) {
                                            alertList.add(flowsheetFieldName)
                                        }
                                    } else {// lbs , ozs
                                        if (vitalValue < (AddWellnessActivity.weightMinVal * 2.2) || vitalValue > (AddWellnessActivity.weightMaxVal * 2.2)) {
                                            alertList.add(flowsheetFieldName)
                                        }
                                    }
                                } else if (flowsheetFieldName.equals(
                                        ModelUtil.wellnessHeightModelId,
                                        true
                                    )
                                ) {
                                    if (tagList.size == 1) {//cms
                                        if (vitalValue < AddWellnessActivity.heightMinVal || vitalValue > AddWellnessActivity.heightMaxVal) {
                                            alertList.add(flowsheetFieldName)
                                        }
                                    } else {// ft, inc

                                        val inchesStr =
                                            binding.addFlowsheetDataMainLayout.findViewWithTag<MDTextInputEdittext>(
                                                tagList[1]
                                            )?.text.toString()

                                        val inchesValue =
                                            (vitalValue * 12) + (inchesStr.toDoubleOrNull()
                                                ?: 0.0)

                                        if (inchesValue < (AddWellnessActivity.heightMinVal.toDouble() / 2.54) || inchesValue > (AddWellnessActivity.heightMaxVal.toDouble() / 2.54)) {
                                            alertList.add(flowsheetFieldName)
                                        }
                                    }
                                } else if (flowsheetFieldName.equals("BP", true)) {

                                    if (vitalValue < AddWellnessActivity.bpSystolicMinVal || vitalValue > AddWellnessActivity.bpSystolicMaxVal) {
                                        alertList.add(flowsheetFieldName)
                                    }
                                } else if (flowsheetFieldName.equals(
                                        ModelUtil.wellnessPulseModelId,
                                        true
                                    )
                                ) {

                                    if (vitalValue < AddWellnessActivity.pulseRateMinValue || vitalValue > AddWellnessActivity.pulseRateMaxValue) {
                                        alertList.add(flowsheetFieldName)
                                    }
                                } else if (flowsheetFieldName.equals(
                                        ModelUtil.wellnessTemperatureModelId,
                                        true
                                    )
                                ) {


                                    if (flowsheetEntry.unit?.contains("f", true) == true) {
                                        if (vitalValue < AddWellnessActivity.temperatureFahrenheitMinVal || vitalValue > AddWellnessActivity.temperatureFahrenheitMaxVal) {
                                            alertList.add(flowsheetFieldName)
                                        }
                                    } else {
                                        if (vitalValue < AddWellnessActivity.temperatureCelsiusMinVal || vitalValue > AddWellnessActivity.temperatureCelsiusMaxVal) {
                                            alertList.add(flowsheetFieldName)
                                        }
                                    }
                                } else if (flowsheetFieldName.equals("SPO2", true)) {

                                    if (vitalValue < AddWellnessActivity.spo2MinValue || vitalValue > AddWellnessActivity.spo2MaxValue) {
                                        alertList.add(flowsheetFieldName)
                                    }
                                } else if (flowsheetFieldName.equals("Respiratory Rate", true)) {

                                    if (vitalValue < AddWellnessActivity.respiratoryRateMinValue || vitalValue > AddWellnessActivity.respiratoryRateMaxValue) {
                                        alertList.add(flowsheetFieldName)
                                    }
                                }

                            } else if (index == 1) {
                                if (flowsheetFieldName.equals("BP", true)) {

                                    if (vitalValue < AddWellnessActivity.bpDiastolicMinVal || vitalValue > AddWellnessActivity.bpDiastolicMaxVal) {
                                        alertList.add(flowsheetFieldName)
                                    }
                                } else if (flowsheetFieldName.equals(
                                        ModelUtil.wellnessHeightModelId,
                                        true
                                    )
                                ) {
                                    // ft, inc

                                    val ftStr =
                                        binding.addFlowsheetDataMainLayout.findViewWithTag<MDTextInputEdittext>(
                                            tagList[0]
                                        )?.text.toString()

                                    if (ftStr.isBlank()) {
                                        if (vitalValue < (AddWellnessActivity.heightMinVal.toDouble() / 2.54) || vitalValue > (AddWellnessActivity.heightMinVal.toDouble() / 2.54)) {
                                            alertList.add(flowsheetFieldName)
                                        }
                                    }

                                }

                            }
                        }
                        valueList.add(vitalValueStr)
                    } else if (vitalView is Slider) {
                        val selectedValue = vitalView.value.toInt()

                        var selectedValueStr = ""
                        if (sliderIsChangedList.contains(vitalTag) || vitalView.valueFrom != vitalView.value) {
                            selectedValueStr = selectedValue.toString()
                        }
                        valueList.add(selectedValueStr)
                    } else if (vitalView is RadioGroup) {
                        var selectedValueStr = ""
                        val singleChoiceOptionsCount = vitalView.childCount
                        for (optionsIndex in 0 until singleChoiceOptionsCount) {
                            val singleChoiceRadioButton = vitalView.getChildAt(optionsIndex)
                            if (singleChoiceRadioButton is RadioButton) {

                                if (singleChoiceRadioButton.isChecked) {
                                    var value = singleChoiceRadioButton.text.toString()
                                    if (!flowsheetEntry.unit.isNullOrBlank()) {
                                        value = value.removeSuffix(flowsheetEntry.unit ?: "")
                                    }
                                    selectedValueStr = value.trim()
                                    break
                                }
                            }
                        }
                        valueList.add(selectedValueStr)
                    }
                }

                Log.d(
                    "MDVitals",
                    "name ->${flowsheetEntry.name} ; value -> ${valueList.joinToString(",")} ; tag -> $tag"
                )

                if (valueList.any { it.isNotBlank() }) {
                    if (flowsheetEntry.name == "BP") {

                        flowsheetEntry.value = valueList.joinToString("/")
                    } else {
                        flowsheetEntry.value = valueList.joinToString(",")
                    }
                    flowsheetEntryList.add(flowsheetEntry)
                }
            } else {
                val selectedView =
                    binding.addFlowsheetDataMainLayout.findViewWithTag<View>(tag.toInt())

                if (selectedView is MDTextInputEdittext) {

                    val selectedValue = selectedView.text.toString()

                    if (selectedValue.isNotBlank()) {
                        if (flowsheetEntry.fieldType?.equals(
                                ModelUtil.flowsheetFieldTypeDate,
                                true
                            ) == true
                        ) {
                            val date =
                                DateTime.parse(selectedValue, ModelUtil.charmDisplayDateFormat())
                            flowsheetEntry.value = date.toString(ModelUtil.charmApiDateFormat())
                        } else if (flowsheetEntry.fieldType?.equals(
                                ModelUtil.flowsheetFieldTypeDateTime,
                                true
                            ) == true
                        ) {
                            val date = DateTime.parse(
                                selectedValue,
                                ModelUtil.charmFlowsheetDataDateFormatter()
                            )
                            flowsheetEntry.value = date.toString(ModelUtil.sqlTimeStampFormattor())
                        } else {
                            flowsheetEntry.value = selectedValue
                        }
                        flowsheetEntryList.add(flowsheetEntry)
                    }
                }
                /* else if (selectedView is TextView) {
                     val selectedValue = selectedView.text.toString()

                     if (selectedValue.isNotBlank()) {

                         if (flowsheetEntry.fieldType?.equals(ModelUtil.flowsheetFieldTypeDate, true) == true) {
                             val date = DateTime.parse(selectedValue, ModelUtil.charmDisplayDateFormat())
                             flowsheetEntry.value = date.toString(ModelUtil.charmApiDateFormat())
                         } else {
                             val date = DateTime.parse(selectedValue, ModelUtil.charmFlowsheetDataDateFormatter())
                             flowsheetEntry.value = date.toString(ModelUtil.sqlTimeStampFormattor())
                         }
                         flowsheetEntryList.add(flowsheetEntry)
                     }
                 }*/ else if (selectedView is Slider) {

                    val selectedValue = selectedView.value.toInt()
                    if (sliderIsChangedList.contains(tag.toInt()) || selectedView.valueFrom != selectedView.value) {
                        flowsheetEntry.value = selectedValue.toString()
                        flowsheetEntryList.add(flowsheetEntry)
                    }
                } else if (selectedView is RadioGroup) {

                    val singleChoiceOptionsCount = selectedView.childCount

                    for (optionsIndex in 0 until singleChoiceOptionsCount) {
                        val singleChoiceRadioButton = selectedView.getChildAt(optionsIndex)
                        if (singleChoiceRadioButton is RadioButton) {

                            if (singleChoiceRadioButton.isChecked) {


                                var value = singleChoiceRadioButton.text.toString()

                                if (!flowsheetEntry.unit.isNullOrBlank()) {
                                    value = value.removeSuffix(" ${flowsheetEntry.unit ?: ""}")
                                }
                                flowsheetEntry.value = value

                                if (flowsheetEntry.addAdditionalNotes) {
                                    val singleChoiceRadioButtonTag =
                                        singleChoiceRadioButton.tag as Int
                                    flowsheetEntry.comments =
                                        selectedView.findViewWithTag<MDTextInputEdittext>(
                                            radioToNotesMap.get(singleChoiceRadioButtonTag)
                                        )?.text.toString()
                                }
                                flowsheetEntryList.add(flowsheetEntry)
                                break
                            }
                        }
                    }
                } else if (selectedView is LinearLayout) {

                    val multiChoiceOptionsCount = selectedView.childCount


                    val valueList = arrayListOf<String>()
                    val commentList = arrayListOf<String>()
                    for (optionsIndex in 0 until multiChoiceOptionsCount) {
                        val multiChoiceRadioButton = selectedView.getChildAt(optionsIndex)
                        if (multiChoiceRadioButton is CheckBox) {

                            if (multiChoiceRadioButton.isChecked) {

                                var value = multiChoiceRadioButton.text.toString()

                                if (!flowsheetEntry.unit.isNullOrBlank()) {
                                    value = value.removeSuffix(flowsheetEntry.unit ?: "")
                                }
                                valueList.add(value.trim())

                                if (flowsheetEntry.addAdditionalNotes) {
                                    val singleChoiceRadioButtonTag =
                                        multiChoiceRadioButton.tag as Int

                                    var comment =
                                        selectedView.findViewWithTag<MDTextInputEdittext>(
                                            radioToNotesMap.get(singleChoiceRadioButtonTag)
                                        )?.text.toString()
                                    if (comment.isEmpty()) {
                                        comment = " "
                                    }
                                    commentList.add(comment)
                                }

                            }
                        }
                    }

                    if (valueList.isNotEmpty()) {

                        flowsheetEntry.value = valueList.joinToString("\n")
                        flowsheetEntry.comments = commentList.joinToString("\n")
                        flowsheetEntryList.add(flowsheetEntry)
                    }
                }
            }


        }

        if (flowsheetEntryList.isNotEmpty()) {


            if (alertList.isNotEmpty() && addFlowsheetDataViewModel.toCheckVitalAlert) {
                showAddValidationAlert(alertList)
                return null
            } else {

                val phrFlowsheets = PHRFlowsheets()
                phrFlowsheets.flowsheetEntries = flowsheetEntryList
                phrFlowsheets.flowSheetId = addFlowsheetDataViewModel.flowsheet.flowSheetId
                phrFlowsheets.practiceId = addFlowsheetDataViewModel.flowsheet.practiceId
                phrFlowsheets.phrFlowsheetId =
                    addFlowsheetDataViewModel.phrFlowsheets?.phrFlowsheetId
                phrFlowsheets.date = DateTime.parse(
                    binding.addFlowsheetDataObservedOnTV.text.toString(),
                    ModelUtil.charmFlowsheetDataDateFormatter()
                )
                return phrFlowsheets
            }
        } else {
            UIUtil.showToast(this, R.string.error_add_flowsheet_add_one_entry, Toast.LENGTH_LONG)
            return null
        }
    }

    private fun showAddValidationAlert(alertValues: ArrayList<String>) {

        val alertDialogBuilder = AlertDialog.Builder(this)
        if (alertValues.size == 1) {
            alertDialogBuilder.setMessage(
                getString(
                    R.string.add_flowsheet_alert_single_item_str,
                    alertValues[0]
                )
            )
        } else {
            alertDialogBuilder.setMessage(R.string.add_flowsheet_alert_multiple_item_str)
        }

        alertDialogBuilder.setPositiveButton(R.string.yes_str) { _, _ ->
            addFlowsheetDataViewModel.toCheckVitalAlert = false
            onSubmitFlowsheet()
        }

        alertDialogBuilder.setNegativeButton(R.string.no_str) { _, _ ->

        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        alertDialog.show()
    }


    private fun renderFlowsheetData() {

        val groupedFlowsheetEntry =
            groupFlowsheets(addFlowsheetDataViewModel.flowsheet.flowsheetEntries)

        groupedFlowsheetEntry.forEach { flowsheetEntries ->

            val isLast =
                groupedFlowsheetEntry.lastIndex == groupedFlowsheetEntry.indexOf(flowsheetEntries)
            when (flowsheetEntries.first().type) {
                ModelUtil.flowsheetEntryTypeVitals -> {
                    createVitalsEntry(flowsheetEntries, isLast)
                }

                ModelUtil.flowsheetEntryTypelab -> {
                    createLabEntry(flowsheetEntries, isLast)
                }

                ModelUtil.flowsheetEntryTypeHeading -> {
                    createHeading(flowsheetEntries.first(), isLast)
                }

                ModelUtil.flowsheetEntryTypeInstructions -> {
                    createInstruction(flowsheetEntries.first(), isLast)
                }

                else -> {

                    val flowsheetEntry = flowsheetEntries.first()

                    if (flowsheetEntry.fieldType?.equals(
                            ModelUtil.flowsheetFieldTypeSingleLine,
                            true
                        ) == true
                    ) {
                        createQuestion(flowsheetEntry, true, isLast)
                    } else if (flowsheetEntry.fieldType?.equals(
                            ModelUtil.flowsheetFieldTypeMultiLine,
                            true
                        ) == true
                    ) {
                        createQuestion(flowsheetEntry, false, isLast)
                    } else if (flowsheetEntry.fieldType?.equals(
                            ModelUtil.flowsheetFieldTypeNumber,
                            true
                        ) == true
                    ) {
                        createQuestion(flowsheetEntry, true, isLast)
                    } else if (flowsheetEntry.fieldType?.equals(
                            ModelUtil.flowsheetFieldTypeSingleChoice,
                            true
                        ) == true
                    ) {
                        createOptions(flowsheetEntry, false, isLast)
                    } else if (flowsheetEntry.fieldType?.equals(
                            ModelUtil.flowsheetFieldTypeMultiChoice,
                            true
                        ) == true
                    ) {
                        createOptions(flowsheetEntry, true, isLast)
                    } else if (flowsheetEntry.fieldType?.equals(
                            ModelUtil.flowsheetFieldTypeDate,
                            true
                        ) == true
                    ) {
                        createDateLayout(flowsheetEntry, false, isLast)
                    } else if (flowsheetEntry.fieldType?.equals(
                            ModelUtil.flowsheetFieldTypeDateTime,
                            true
                        ) == true
                    ) {
                        createDateLayout(flowsheetEntry, true, isLast)
                    } else if (flowsheetEntry.fieldType?.equals(
                            ModelUtil.flowsheetFieldTypeRatingScale,
                            true
                        ) == true
                    ) {
                        createRatingScale(flowsheetEntry, isLast)
                    }

                }
            }
        }
    }


    private fun createRatingScale(flowsheetEntry: FlowsheetEntries, isLast: Boolean) {

        val cardView = ItemAddFlowsheetLayoutBinding.inflate(
            layoutInflater,
            binding.addFlowsheetDataMainLayout,
            false
        )
        cardView.addFlowsheetDataHeadingTV.text = flowsheetEntry.name

        updateBottomMargin(cardView.root, isLast)

        cardView.addFlowsheetDataEntryLayout.addView(
            addRatingScale(
                flowsheetEntry,
                cardView.addFlowsheetDataEntryLayout
            )
        )

        binding.addFlowsheetDataMainLayout.addView(cardView.root)


    }

    private fun addRatingScale(flowsheetEntry: FlowsheetEntries, parent: ViewGroup): LinearLayout {
        val sliderLayout = LayoutInflater.from(this)
            .inflate(R.layout.item_add_flowsheet_rating, parent, false) as LinearLayout

        val slider = sliderLayout.getChildAt(0) as Slider
        val sliderValueTV = sliderLayout.getChildAt(1) as TextView


        val option = flowsheetEntry.option?.split("-")

        Log.d("MDSlider", "from -${slider.valueFrom} ; to - ${slider.valueTo}")
        slider.valueTo = (option?.getOrNull(1) ?: "10").toFloatOrNull() ?: 10f
        slider.value = (option?.getOrNull(0) ?: "0").toFloatOrNull() ?: 0f
        slider.valueFrom = (option?.getOrNull(0) ?: "0").toFloatOrNull() ?: 0f

        slider.setLabelFormatter { value ->
            val intVal = value.toInt()

            Log.d("MDSlider", "$value - $intVal")
            "$intVal ${flowsheetEntry.unit}".trim()
        }

        if (prevET != null) {
            prevET?.imeOptions = EditorInfo.IME_ACTION_DONE
            prevET = null
        }

        val sliderValue =
            addFlowsheetDataViewModel.phrFlowsheets?.flowsheetEntries?.firstOrNull { it.flowSheetEntryId == flowsheetEntry.flowSheetEntryId }?.value?.toFloatOrNull()
        if (sliderValue != null) {
            slider.value = sliderValue
            sliderValueTV.text = "${sliderValue.toInt()}"
        }

        if (addFlowsheetDataViewModel.flowsheet.allowEdit) {
            slider.tag = tagNo
            tagToIdMap.put(tagNo.toString(), flowsheetEntry.flowSheetEntryId ?: "")

            slider.addOnChangeListener { _, value, _ ->

                sliderValueTV.text = "${value.toInt()}"

            }

            slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                @SuppressLint("RestrictedApi")
                override fun onStartTrackingTouch(slider: Slider) {

                }

                @SuppressLint("RestrictedApi")
                override fun onStopTrackingTouch(slider: Slider) {

                    val sliderTag = slider.tag as Int
                    if (!sliderIsChangedList.contains(sliderTag)) {
                        sliderIsChangedList.add(sliderTag)
                    }
                }
            })
            tagNo++


        } else {
            slider.isEnabled = false
        }

        return sliderLayout
    }

    private fun createDateLayout(
        flowsheetEntry: FlowsheetEntries,
        isDateTime: Boolean,
        isLast: Boolean
    ) {

        val parentCardView = ItemAddFlowsheetLayoutBinding.inflate(
            layoutInflater,
            binding.addFlowsheetDataMainLayout,
            false
        )
        parentCardView.addFlowsheetDataHeadingTV.text = flowsheetEntry.name

        updateBottomMargin(parentCardView.root, isLast)


        val cardView = ItemAddFlowsheetDateBinding.inflate(
            layoutInflater,
            binding.addFlowsheetDataMainLayout,
            false
        )


        if (isLast) {
            val lp = cardView.root.layoutParams as ViewGroup.MarginLayoutParams
            lp.bottomMargin = resources.getDimensionPixelSize(R.dimen.card_padding)
            cardView.root.layoutParams = lp
        }

        //cardView.root.hint = flowsheetEntry.name
        cardView.addFlowsheetDataDateTV.setText(addFlowsheetDataViewModel.phrFlowsheets?.flowsheetEntries?.firstOrNull { it.flowSheetEntryId == flowsheetEntry.flowSheetEntryId }?.value)
        cardView.addFlowsheetDataDateTV.inputType = InputType.TYPE_NULL

        if (prevET != null) {
            prevET?.imeOptions = EditorInfo.IME_ACTION_DONE
            prevET = null
        }


        if (addFlowsheetDataViewModel.flowsheet.allowEdit) {
            cardView.addFlowsheetDataDateTV.tag = tagNo
            tagToIdMap.put(tagNo.toString(), flowsheetEntry.flowSheetEntryId ?: "")
            tagNo++

            cardView.addFlowsheetDataDateTV.setOnClickListener {
                showDatePicker(cardView.addFlowsheetDataDateTV, isDateTime)
            }

            cardView.addFlowsheetDataDateTV.setOnFocusChangeListener { view, isFocus ->
                if (isFocus) {
                    showDatePicker(cardView.addFlowsheetDataDateTV, isDateTime)
                }
            }
        }

        parentCardView.addFlowsheetDataEntryLayout.addView(cardView.root)
        binding.addFlowsheetDataMainLayout.addView(parentCardView.root)
    }

    private fun createOptions(
        flowsheetEntry: FlowsheetEntries,
        isMultichoice: Boolean,
        isLast: Boolean
    ) {

        val cardView = ItemAddFlowsheetLayoutBinding.inflate(
            layoutInflater,
            binding.addFlowsheetDataMainLayout,
            false
        )
        cardView.addFlowsheetDataHeadingTV.text = flowsheetEntry.name

        updateBottomMargin(cardView.root, isLast)


        val unit = flowsheetEntry.unit ?: ""
        val choices = flowsheetEntry.option?.split("\n") ?: listOf()
        val value =
            addFlowsheetDataViewModel.phrFlowsheets?.flowsheetEntries?.firstOrNull { it.flowSheetEntryId == flowsheetEntry.flowSheetEntryId }?.value
        val additionalNotes =
            addFlowsheetDataViewModel.phrFlowsheets?.flowsheetEntries?.firstOrNull { it.flowSheetEntryId == flowsheetEntry.flowSheetEntryId }?.comments

        if (prevET != null) {
            prevET?.imeOptions = EditorInfo.IME_ACTION_DONE
            prevET = null
        }

        if (isMultichoice) {

            if (addFlowsheetDataViewModel.flowsheet.allowEdit) {
                cardView.addFlowsheetDataEntryLayout.tag = tagNo
                tagToIdMap.put(tagNo.toString(), flowsheetEntry.flowSheetEntryId ?: "")
                tagNo++
            }

            val valueList = if (value.isNullOrBlank()) {
                listOf()
            } else {
                value.split("\n")
            }
            val additionalNotesList = if (additionalNotes.isNullOrBlank()) {
                listOf()
            } else {
                additionalNotes.split("\n")
            }

            choices.forEachIndexed { index, choice ->

                val optionsButton =
                    LayoutInflater.from(this).inflate(
                        R.layout.item_questionnaire_option_radio_button,
                        cardView.addFlowsheetDataEntryLayout,
                        false
                    ) as CheckBox
                optionsButton.setSingleLine(false)

                if (unit.isNotBlank()) {
                    optionsButton.text = "$choice $unit"
                } else {
                    optionsButton.text = choice
                }
                optionsButton.tag = tagNo++



                cardView.addFlowsheetDataEntryLayout.addView(optionsButton)

                if (index == 0) {
                    val optionParam = optionsButton.layoutParams as ViewGroup.MarginLayoutParams
                    optionParam.topMargin =
                        resources.getDimensionPixelSize(R.dimen.item_questionnaire_option_bottom_margin)
                    optionsButton.layoutParams = optionParam
                }

                if (flowsheetEntry.addAdditionalNotes) {

                    val questionET = LayoutInflater.from(this)
                        .inflate(
                            R.layout.item_add_flowsheet_question,
                            cardView.addFlowsheetDataEntryLayout,
                            false
                        ) as MDTextInputEdittext
                    questionET.tag = tagNo++
                    radioToNotesMap.put(optionsButton.tag as Int, questionET.tag as Int)


                    questionET.layoutParams.height = 0
                    questionET.minHeight = 0
                    questionET.requestLayout()
                    questionET.maxLines = 1
                    questionET.gravity = Gravity.CENTER_VERTICAL
                    questionET.hint = flowsheetEntry.additionalNotesLabel
                    questionET.imeOptions = EditorInfo.IME_ACTION_DONE
                    questionET.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

                    val questionETParam = questionET.layoutParams as ViewGroup.MarginLayoutParams
                    questionETParam.marginStart =
                        resources.getDimensionPixelSize(R.dimen.add_flowsheet_data_option_notes_margin_start)
                    questionETParam.topMargin = 0
                    questionETParam.bottomMargin =
                        resources.getDimensionPixelSize(R.dimen.card_padding)

                    questionET.layoutParams = questionETParam


                    if (valueList.contains(choice)) {
                        val valueIndex = valueList.indexOf(choice)
                        questionET.setText(additionalNotesList.getOrNull(valueIndex)?.trim())
                    }

                    cardView.addFlowsheetDataEntryLayout.addView(questionET)


                    optionsButton.setOnCheckedChangeListener { compoundButton, isChecked ->
                        if (isChecked) {
                            animateOthersOption(questionET, show = true)
                        } else {
                            animateOthersOption(questionET, show = false)
                        }
                    }

                }

                if (valueList.contains(choice)) {
                    optionsButton.isChecked = true
                }

                if (!addFlowsheetDataViewModel.flowsheet.allowEdit) {
                    optionsButton.isEnabled = false
                }

            }
        } else {
            val radioGroup = RadioGroup(this)
            radioGroup.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )



            if (addFlowsheetDataViewModel.flowsheet.allowEdit) {
                radioGroup.tag = tagNo
                tagToIdMap.put(tagNo.toString(), flowsheetEntry.flowSheetEntryId ?: "")
                tagNo++
            }

            choices.forEachIndexed { index, choice ->
                val optionsButton = LayoutInflater.from(this)
                    .inflate(
                        R.layout.item_questionnaire_options_radio_button2,
                        cardView.addFlowsheetDataEntryLayout,
                        false
                    ) as RadioButton
                optionsButton.setSingleLine(false)

                if (unit.isNotBlank()) {
                    optionsButton.text = "$choice $unit"
                } else {
                    optionsButton.text = choice
                }
                optionsButton.tag = tagNo++

                radioGroup.addView(optionsButton)


                if (index == 0) {
                    val optionParam = optionsButton.layoutParams as ViewGroup.MarginLayoutParams
                    optionParam.topMargin =
                        resources.getDimensionPixelSize(R.dimen.item_questionnaire_option_bottom_margin)
                    optionsButton.layoutParams = optionParam
                }



                if (flowsheetEntry.addAdditionalNotes) {


                    val questionET = LayoutInflater.from(this).inflate(
                        R.layout.item_add_flowsheet_question,
                        radioGroup,
                        false
                    ) as MDTextInputEdittext
                    questionET.tag = tagNo++
                    radioToNotesMap.put(optionsButton.tag as Int, questionET.tag as Int)


                    questionET.layoutParams.height = 0
                    questionET.minHeight = 0
                    questionET.requestLayout()


                    val questionETParam = questionET.layoutParams as ViewGroup.MarginLayoutParams
                    questionETParam.marginStart =
                        resources.getDimensionPixelSize(R.dimen.add_flowsheet_data_option_notes_margin_start)
                    questionETParam.topMargin = 0
                    questionETParam.bottomMargin =
                        resources.getDimensionPixelSize(R.dimen.card_padding)
                    questionET.layoutParams = questionETParam

                    questionET.maxLines = 1
                    questionET.gravity = Gravity.CENTER_VERTICAL
                    questionET.imeOptions = EditorInfo.IME_ACTION_DONE
                    questionET.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

                    questionET.hint = flowsheetEntry.additionalNotesLabel



                    if (value == choice) {
                        questionET.setText(additionalNotes)
                    }


                    radioGroup.addView(questionET)


                    optionsButton.setOnCheckedChangeListener { compoundButton, isChecked ->
                        if (isChecked) {
                            animateOthersOption(questionET, true)
                        } else {
                            animateOthersOption(questionET, false)
                        }
                    }

                }
                if (value == choice) {
                    optionsButton.isChecked = true
                }

                if (!addFlowsheetDataViewModel.flowsheet.allowEdit) {
                    optionsButton.isEnabled = false
                }
            }

            if (!addFlowsheetDataViewModel.flowsheet.allowEdit) {
                radioGroup.isEnabled = false
            }

            cardView.addFlowsheetDataEntryLayout.addView(radioGroup)
        }

        binding.addFlowsheetDataMainLayout.addView(cardView.root)

    }

    private fun createQuestion(
        flowsheetEntry: FlowsheetEntries,
        isSingleLine: Boolean,
        isLast: Boolean
    ) {

        val cardView = ItemAddFlowsheetLayoutBinding.inflate(
            layoutInflater,
            binding.addFlowsheetDataMainLayout,
            false
        )
        cardView.addFlowsheetDataHeadingTV.text = flowsheetEntry.name


        updateBottomMargin(cardView.root, isLast)


        val questionAny =
            addQuestion(flowsheetEntry, cardView.addFlowsheetDataEntryLayout, isSingleLine)

        if (questionAny is MDTextInputEdittext) {
            cardView.addFlowsheetDataEntryLayout.addView(questionAny)
        } else if (questionAny is LinearLayout) {
            cardView.addFlowsheetDataEntryLayout.addView(questionAny)
        }

        binding.addFlowsheetDataMainLayout.addView(cardView.root)
    }

    private fun addQuestion(
        flowsheetEntry: FlowsheetEntries,
        parent: ViewGroup,
        isSingleLine: Boolean
    ): Any {
        var parentLayout = parent

        if (!flowsheetEntry.unit.isNullOrBlank()) {

            val linearLayout = LinearLayout(this)

            val lp = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            linearLayout.weightSum = 1f
            linearLayout.layoutParams = lp


            parentLayout = linearLayout
        }


        val questionET = LayoutInflater.from(this).inflate(
            R.layout.item_add_flowsheet_question,
            parentLayout,
            false
        ) as MDTextInputEdittext
        questionET.setText(addFlowsheetDataViewModel.phrFlowsheets?.flowsheetEntries?.firstOrNull { it.flowSheetEntryId == flowsheetEntry.flowSheetEntryId }?.value)

        if (prevET != null) {
            prevET?.imeOptions = EditorInfo.IME_ACTION_NEXT

            prevET?.setOnEditorActionListener { textView, imeOption, keyEvent ->

                if (imeOption == EditorInfo.IME_ACTION_NEXT) {

                    questionET.requestFocus()
                    imm.showSoftInput(questionET, InputMethodManager.SHOW_IMPLICIT)
                    return@setOnEditorActionListener true
                }

                return@setOnEditorActionListener false
            }
        }

        prevET = questionET
        if (isSingleLine) {

            val questionAnsETHeight =
                resources.getDimension(R.dimen.flowsheet_add_simple_ques_ans_value_height).toInt()
            questionET.layoutParams.height = questionAnsETHeight
            questionET.minHeight = questionAnsETHeight
            questionET.requestLayout()
            questionET.maxLines = 1
            questionET.gravity = Gravity.CENTER_VERTICAL

            if (flowsheetEntry.fieldType?.equals(
                    ModelUtil.flowsheetFieldTypeNumber,
                    true
                ) == true
            ) {
                questionET.inputType = InputType.TYPE_CLASS_NUMBER
            } else {
                questionET.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            }
        }

        if (!flowsheetEntry.unit.isNullOrBlank()) {

            val etParam = questionET.layoutParams as LinearLayout.LayoutParams
            etParam.width = 0
            etParam.weight = 1f
            questionET.layoutParams = etParam

            val unitTV = TextView(this)
            val headingBPParam = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            headingBPParam.marginStart = resources.getDimensionPixelSize(R.dimen.card_padding)
            headingBPParam.marginEnd = headingBPParam.marginStart
            headingBPParam.gravity = Gravity.CENTER_VERTICAL

            unitTV.layoutParams = headingBPParam
            unitTV.text = flowsheetEntry.unit
            unitTV.gravity = Gravity.CENTER_VERTICAL
            unitTV.setTextColor(ContextCompat.getColor(this, R.color.black87))
            unitTV.typeface = UIUtil.getFont(this, R.font.open_sans)
            unitTV.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.xx_small)
            )

            parentLayout.addView(questionET)
            parentLayout.addView(unitTV)
        }

        if (addFlowsheetDataViewModel.flowsheet.allowEdit) {

            questionET.tag = tagNo
            tagToIdMap.put(tagNo.toString(), flowsheetEntry.flowSheetEntryId ?: "")
            tagNo++
        } else {
            questionET.isEnabled = false
        }

        return if (!flowsheetEntry.unit.isNullOrBlank()) {
            parentLayout
        } else {
            questionET
        }
    }

    private fun createHeading(flowsheetEntry: FlowsheetEntries, isLast: Boolean) {

        val headingTV = TextView(this)

        val lp = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        lp.topMargin = resources.getDimensionPixelSize(R.dimen.card_padding)
        lp.marginStart =
            resources.getDimensionPixelSize(R.dimen.questionnaire_card_margin_horizontal)
        lp.marginEnd = resources.getDimensionPixelSize(R.dimen.questionnaire_card_margin_horizontal)

        if (isLast) {
            lp.bottomMargin = lp.topMargin
        } else {
            lp.bottomMargin =
                resources.getDimensionPixelSize(R.dimen.flowsheet_add_card_heading_bottom_margin)
        }
        headingTV.layoutParams = lp

        headingTV.setTextColor(ContextCompat.getColor(this, R.color.black87))
        headingTV.typeface = UIUtil.getFont(this, R.font.open_sans_medium)
        headingTV.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.x_medium)
        )

        headingTV.text = flowsheetEntry.name

        if (prevET != null) {
            prevET?.imeOptions = EditorInfo.IME_ACTION_DONE
            prevET = null
        }
        binding.addFlowsheetDataMainLayout.addView(headingTV)
    }

    private fun createInstruction(flowsheetEntry: FlowsheetEntries, isLast: Boolean) {

        val cardView = ItemAddFlowsheetInstructionBinding.inflate(
            layoutInflater,
            binding.addFlowsheetDataMainLayout,
            false
        )

        var instructions = HtmlCompat.fromHtml(
            flowsheetEntry.name
                ?: "", 0
        ).removeSuffix("\n\n")

        if (instructions.isNotBlank()) {
            if (instructions.endsWith("\n")) {
                instructions = instructions.removeSuffix("\n")
            }
        }

        cardView.addFlowsheetDataInstructionTV.text = instructions
        cardView.addFlowsheetDataInstructionTV.movementMethod = LinkMovementMethod.getInstance()

        updateBottomMargin(cardView.root, isLast)

        if (prevET != null) {
            prevET?.imeOptions = EditorInfo.IME_ACTION_DONE
            prevET = null
        }

        binding.addFlowsheetDataMainLayout.addView(cardView.root)
    }

    private fun groupFlowsheets(flowsheetEntries: ArrayList<FlowsheetEntries>): ArrayList<ArrayList<FlowsheetEntries>> {
        val groupedFlowsheets = arrayListOf<ArrayList<FlowsheetEntries>>()

        var localFlowsheetEntries = arrayListOf<FlowsheetEntries>()

        flowsheetEntries.forEach { flowsheetEntry ->
            if (localFlowsheetEntries.isEmpty()) {
                localFlowsheetEntries.add(flowsheetEntry)
            } else {
                if (localFlowsheetEntries.first().type == flowsheetEntry.type) {
                    localFlowsheetEntries.add(flowsheetEntry)
                } else {
                    groupedFlowsheets.add(localFlowsheetEntries)
                    localFlowsheetEntries = arrayListOf<FlowsheetEntries>()
                    localFlowsheetEntries.add(flowsheetEntry)
                }
            }

            if ((flowsheetEntry.type != ModelUtil.flowsheetEntryTypeVitals && flowsheetEntry.type != ModelUtil.flowsheetEntryTypelab) && localFlowsheetEntries.isNotEmpty()) {
                groupedFlowsheets.add(localFlowsheetEntries)
                localFlowsheetEntries = arrayListOf<FlowsheetEntries>()
            }
        }

        if (localFlowsheetEntries.isNotEmpty()) {
            groupedFlowsheets.add(localFlowsheetEntries)
        }

        return groupedFlowsheets
    }

    private fun createLabEntry(flowsheetEntries: ArrayList<FlowsheetEntries>, isLast: Boolean) {
        val cardView = ItemAddFlowsheetLayoutBinding.inflate(
            layoutInflater,
            binding.addFlowsheetDataMainLayout,
            false
        )
        cardView.addFlowsheetDataHeadingTV.setText(R.string.add_flowsheet_lab_txt)

        updateBottomMargin(cardView.root, isLast)

        val vitalScreenSize =
            UIUtil.getScreenWidth(this) - (3 * resources.getDimensionPixelSize(R.dimen.card_padding))

        val flowLayout = FlowLayout(this)
        flowLayout.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

        val lastIndex = flowsheetEntries.lastIndex
        flowsheetEntries.forEachIndexed { index, flowsheetEntry ->
            val value =
                addFlowsheetDataViewModel.phrFlowsheets?.flowsheetEntries?.firstOrNull { it.flowSheetEntryId == flowsheetEntry.flowSheetEntryId }?.value

            val labConstraintLayout =
                ItemAddFlowsheetVitalLabBinding.inflate(layoutInflater, flowLayout, false)
            labConstraintLayout.root.minimumWidth = (vitalScreenSize / 2)

            labConstraintLayout.addFlowsheetDataVitalLabLabel.text = flowsheetEntry.name
            labConstraintLayout.addFlowsheetDataVitalLabUnitTV.text = flowsheetEntry.unit
            labConstraintLayout.addFlowsheetDataVitalLabET.setText(value)

            labConstraintLayout.addFlowsheetDataVitalLabET.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

            if (prevET != null) {
                prevET?.imeOptions = EditorInfo.IME_ACTION_NEXT

                prevET?.setOnEditorActionListener { textView, imeOption, keyEvent ->

                    if (imeOption == EditorInfo.IME_ACTION_NEXT) {

                        labConstraintLayout.addFlowsheetDataVitalLabET.requestFocus()
                        imm.showSoftInput(
                            labConstraintLayout.addFlowsheetDataVitalLabET,
                            InputMethodManager.SHOW_IMPLICIT
                        )

                        return@setOnEditorActionListener true
                    }

                    return@setOnEditorActionListener false
                }
            }

            prevET = labConstraintLayout.addFlowsheetDataVitalLabET


            flowLayout.addView(labConstraintLayout.root)

            if (addFlowsheetDataViewModel.flowsheet.allowEdit) {
                labConstraintLayout.addFlowsheetDataVitalLabET.tag = tagNo
                tagToIdMap.put(tagNo.toString(), flowsheetEntry.flowSheetEntryId ?: "")
                tagNo++
            } else {
                labConstraintLayout.addFlowsheetDataVitalLabET.isEnabled = false
            }

            if (lastIndex == index) {
                val lp = labConstraintLayout.root.layoutParams as ViewGroup.MarginLayoutParams
                lp.bottomMargin = resources.getDimensionPixelSize(R.dimen.card_padding)
                labConstraintLayout.root.layoutParams = lp
            }
        }

        cardView.addFlowsheetDataEntryLayout.addView(flowLayout)
        binding.addFlowsheetDataMainLayout.addView(cardView.root)
    }

    private fun createVitalsEntry(flowsheetEntries: ArrayList<FlowsheetEntries>, isLast: Boolean) {

        val cardView = ItemAddFlowsheetLayoutBinding.inflate(
            layoutInflater,
            binding.addFlowsheetDataMainLayout,
            false
        )
        cardView.addFlowsheetDataHeadingTV.setText(R.string.add_flowsheet_vital_txt)

        updateBottomMargin(cardView.root, isLast)

        val vitalScreenSize =
            UIUtil.getScreenWidth(this) - (2 * resources.getDimensionPixelSize(R.dimen.questionnaire_card_margin_horizontal)) - (2 * resources.getDimensionPixelSize(
                R.dimen.card_padding_half
            ))

        val flowLayout = FlowLayout(this)
        flowLayout.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        flowLayout.setPadding(
            resources.getDimensionPixelSize(R.dimen.card_padding_half), // start padding
            0, // top padding
            resources.getDimensionPixelSize(R.dimen.card_padding_half), // end padding
            0  // bottom padding
        )


        var hasSpace = false

        val lastIndex = flowsheetEntries.lastIndex

        var hasBMICal = flowsheetEntries.any {
            it.name?.equals(
                ModelUtil.wellnessWeightModelId,
                true
            ) == true
        } && flowsheetEntries.any {
            it.name?.equals(
                ModelUtil.wellnessHeightModelId,
                true
            ) == true
        } && flowsheetEntries.any { it.name?.equals(ModelUtil.wellnessBMIModelId, true) == true }

        val bmiList = arrayListOf<String>()
        flowsheetEntries.forEachIndexed { index, flowsheetEntry ->
            val value =
                addFlowsheetDataViewModel.phrFlowsheets?.flowsheetEntries?.firstOrNull { it.flowSheetEntryId == flowsheetEntry.flowSheetEntryId }?.value

            val internalList: ArrayList<FlowsheetVital> =
                if (flowsheetEntry.name?.equals(
                        ModelUtil.wellnessWeightModelId,
                        true
                    ) == true || flowsheetEntry.name?.equals(
                        ModelUtil.wellnessHeightModelId,
                        true
                    ) == true
                ) {

                    if (hasSpace && flowsheetEntry.unit?.equals(
                            "kgs",
                            true
                        ) != true && flowsheetEntry.unit?.equals(
                            "kg",
                            true
                        ) != true && flowsheetEntry.unit?.equals(
                            "cms",
                            true
                        ) != true && flowsheetEntry.unit?.equals("cm", true) != true
                    ) {
                        val view = View(this)
                        view.layoutParams =
                            ViewGroup.LayoutParams(vitalScreenSize / 2, WRAP_CONTENT)
                        flowLayout.addView(view)
                        hasSpace = false
                    }

                    val list = arrayListOf<FlowsheetVital>()
                    val valueList = value?.split(",")

                    flowsheetEntry.unit?.split(",")?.forEachIndexed { entryIndex, s ->
                        val flowsheetVital = if (entryIndex == 0) {
                            FlowsheetVital(
                                name = flowsheetEntry.name,
                                unit = s,
                                value = valueList?.getOrNull(entryIndex)
                            )
                        } else {
                            FlowsheetVital(unit = s, value = valueList?.getOrNull(entryIndex))
                        }

                        if (flowsheetEntry.name?.equals(
                                ModelUtil.wellnessWeightModelId,
                                true
                            ) == true
                        ) {
                            if (flowsheetVital.unit?.equals(
                                    "kgs",
                                    true
                                ) == true || flowsheetVital.unit?.equals("kg", true) == true
                            ) {
                                flowsheetVital.maxValue = 200
                                flowsheetVital.isDecimal = true
                            } else if (flowsheetVital.unit?.equals(
                                    "lbs",
                                    true
                                ) == true || flowsheetVital.unit?.equals(
                                    "lb",
                                    true
                                ) == true || flowsheetVital.unit?.equals("pounds", true) == true
                            ) {
                                flowsheetVital.maxValue = 440
                                flowsheetVital.isDecimal = false
                            } else if (flowsheetVital.unit?.equals(
                                    "ozs",
                                    true
                                ) == true || flowsheetVital.unit?.equals("ounces", true) == true
                            ) {
                                flowsheetVital.maxValue = 16
                                flowsheetVital.isDecimal = false

                            }

                        } else {
                            if (flowsheetVital.unit?.equals(
                                    "cms",
                                    true
                                ) == true || flowsheetVital.unit?.equals("cm", true) == true
                            ) {
                                flowsheetVital.maxValue = 300
                                flowsheetVital.isDecimal = false

                            } else if (flowsheetVital.unit?.equals(
                                    "feet",
                                    true
                                ) == true || flowsheetVital.unit?.equals(
                                    "ft",
                                    true
                                ) == true || flowsheetVital.unit?.equals("feets", true) == true
                            ) {
                                flowsheetVital.maxValue = 10
                                flowsheetVital.isDecimal = false

                            } else if (flowsheetVital.unit?.equals(
                                    "inches",
                                    true
                                ) == true || flowsheetVital.unit?.equals("inc", true) == true
                            ) {
                                flowsheetVital.maxValue = 12
                                flowsheetVital.isDecimal = false

                            }
                        }

                        list.add(flowsheetVital)
                    }

                    list
                } else if (flowsheetEntry.name?.equals(
                        ModelUtil.wellnessBPModelId,
                        true
                    ) == true || flowsheetEntry.name?.equals("BP", true) == true
                ) {

                    val headingTv = TextView(this)
                    val headingBPParam = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    headingBPParam.marginStart =
                        resources.getDimensionPixelSize(R.dimen.card_padding)
                    headingBPParam.marginEnd = headingBPParam.marginStart
                    headingBPParam.topMargin = headingBPParam.marginStart

                    headingTv.layoutParams = headingBPParam
                    headingTv.setText(R.string.wellness_bp_txt)
                    headingTv.setTextColor(ContextCompat.getColor(this, R.color.black87))
                    headingTv.typeface = UIUtil.getFont(this, R.font.open_sans_medium)
                    headingTv.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        resources.getDimension(R.dimen.xx_small)
                    )

                    flowLayout.addView(headingTv)
                    hasSpace = false


                    val valueList = value?.split("/")
                    arrayListOf(
                        FlowsheetVital(
                            unit = flowsheetEntry.unit,
                            name = ModelUtil.metricSystolic,
                            value = valueList?.getOrNull(0),
                            maxValue = 200,
                            isDecimal = false
                        ),
                        FlowsheetVital(
                            unit = flowsheetEntry.unit,
                            name = ModelUtil.vitalBPDiastolic,
                            value = valueList?.getOrNull(1),
                            maxValue = 150,
                            isDecimal = false
                        )
                    )
                } else {

                    val flowsheetVital = FlowsheetVital(
                        unit = flowsheetEntry.unit,
                        name = flowsheetEntry.name,
                        value = value
                    )
                    var maxValue = 200


                    var isDecimal = true

                    if (flowsheetEntry.unit?.equals(
                            "minutes",
                            true
                        ) == true || flowsheetEntry.unit?.equals("min", true) == true
                    ) {
                        isDecimal = false
                        maxValue = 300
                    }
                    if (flowsheetEntry.unit?.equals(
                            "percent",
                            true
                        ) == true || flowsheetEntry.unit?.equals(
                            "percentage",
                            true
                        ) == true || flowsheetEntry.unit?.equals("%", true) == true
                    ) {
                        isDecimal = false
                        maxValue = 100
                    } else if (flowsheetEntry.unit?.equals("ml", true) == true) {
                        isDecimal = false
                        maxValue = 6000
                    } else if (flowsheetEntry.unit?.equals(
                            "hours",
                            true
                        ) == true || flowsheetEntry.unit?.equals("hrs", true) == true
                    ) {
                        isDecimal = false
                        maxValue = 24
                    } else if (flowsheetEntry.unit?.equals("calories", true) == true) {
                        maxValue = 10000
                    } else if (flowsheetEntry.name?.contains(
                            ModelUtil.wellnessTemperatureModelId,
                            true
                        ) == true
                    ) {
                        if (flowsheetEntry.unit?.equals("f", true) == true) {
                            maxValue = 110
                        } else {
                            maxValue = 43
                        }
                    }

                    flowsheetVital.maxValue = maxValue
                    flowsheetVital.isDecimal = isDecimal

                    arrayListOf(flowsheetVital)
                }

            val tagList = arrayListOf<Int>()


            internalList.forEach { flowsheetVital ->

                if (flowsheetEntry.fieldType?.equals("Plain Text", true) == true) {

                    hasSpace = false
                    addSeparatorAndHeadingInVitals(flowsheetEntry, flowLayout)

                    val questionAny = addQuestion(flowsheetEntry, flowLayout, true)

                    val questionET = if (questionAny is MDTextInputEdittext) {
                        flowLayout.addView(questionAny)
                        questionAny
                    } else if (questionAny is LinearLayout) {
                        flowLayout.addView(questionAny)
                        questionAny.getChildAt(0) as MDTextInputEdittext
                    } else {
                        null
                    }

                    if (lastIndex == index) {
                        if (questionET != null) {
                            val questionETParam =
                                questionET.layoutParams as ViewGroup.MarginLayoutParams
                            questionETParam.bottomMargin =
                                resources.getDimensionPixelSize(R.dimen.card_padding)
                            questionET.layoutParams = questionETParam
                        }
                    }


                } else if (flowsheetEntry.fieldType?.equals(
                        ModelUtil.flowsheetFieldTypeRatingScale,
                        true
                    ) == true
                ) {
                    hasSpace = false

                    addSeparatorAndHeadingInVitals(flowsheetEntry, flowLayout)

                    val ratingLayout = addRatingScale(flowsheetEntry, flowLayout)

                    if (lastIndex == index) {
                        val slider = ratingLayout.getChildAt(0) as Slider

                        val sliderLP = slider.layoutParams as ViewGroup.MarginLayoutParams
                        sliderLP.bottomMargin =
                            resources.getDimensionPixelSize(R.dimen.card_padding)
                        slider.layoutParams = sliderLP
                    }

                    flowLayout.addView(ratingLayout)

                } else if (!flowsheetEntry.option.isNullOrBlank()) {

                    hasSpace = false
                    addSeparatorAndHeadingInVitals(flowsheetEntry, flowLayout, true)

                    val radioGroup = RadioGroup(this)
                    radioGroup.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)


                    val unit = flowsheetEntry.unit ?: ""
                    val choices = flowsheetEntry.option?.split("\n") ?: listOf()


                    if (prevET != null) {
                        prevET?.imeOptions = EditorInfo.IME_ACTION_DONE
                        prevET = null
                    }

                    if (addFlowsheetDataViewModel.flowsheet.allowEdit) {
                        radioGroup.tag = tagNo
                        tagToIdMap.put(tagNo.toString(), flowsheetEntry.flowSheetEntryId ?: "")
                        tagNo++
                    }

                    choices.forEachIndexed { index, choice ->
                        val optionsButton = LayoutInflater.from(this)
                            .inflate(
                                R.layout.item_questionnaire_options_radio_button2,
                                cardView.addFlowsheetDataEntryLayout,
                                false
                            ) as RadioButton
                        optionsButton.setSingleLine(false)
                        optionsButton.text = "$choice $unit".trim()
                        optionsButton.tag = tagNo++

                        radioGroup.addView(optionsButton)

                        if (index == 0) {
                            val optionParam =
                                optionsButton.layoutParams as ViewGroup.MarginLayoutParams
                            optionParam.topMargin =
                                resources.getDimensionPixelSize(R.dimen.item_questionnaire_option_bottom_margin)
                            optionsButton.layoutParams = optionParam
                        }

                        if (value == choice) {
                            optionsButton.isChecked = true
                        }

                        if (!addFlowsheetDataViewModel.flowsheet.allowEdit) {
                            optionsButton.isEnabled = false
                        }
                    }

                    if (!addFlowsheetDataViewModel.flowsheet.allowEdit) {
                        radioGroup.isEnabled = false
                    }

                    flowLayout.addView(radioGroup)

                } else {

                    val vitalConstraintLayout =
                        ItemAddFlowsheetVitalLabBinding.inflate(layoutInflater, flowLayout, false)


                    vitalConstraintLayout.root.minimumWidth = (vitalScreenSize / 2)
                    vitalConstraintLayout.addFlowsheetDataVitalLabLabel.text = flowsheetVital.name

                    if (flowsheetEntry.name?.equals(
                            ModelUtil.wellnessTemperatureModelId,
                            true
                        ) == true
                    ) {
                        val unit =
                            addFlowsheetDataViewModel.phrFlowsheets?.flowsheetEntries?.firstOrNull { it.flowSheetEntryId == flowsheetEntry.flowSheetEntryId }?.unit
                        if (flowsheetEntry.unit.isNullOrBlank() && unit.isNullOrBlank()) {
                            val popupMenu = PopupMenu(
                                this,
                                vitalConstraintLayout.addFlowsheetDataVitalLabUnitTV,
                                Gravity.END
                            )
                            popupMenu.menu.add(getString(R.string.add_flowsheet_farenheit))
                            popupMenu.menu.add(getString(R.string.add_flowsheet_celsius))

                            vitalConstraintLayout.addFlowsheetDataVitalLabUnitTV.setText(R.string.add_flowsheet_farenheit)
                            flowsheetVital.maxValue = 110
                            flowsheetEntry.unit = "F"

                            val endDrawable = ContextCompat.getDrawable(
                                this,
                                R.drawable.ic_keyboard_arrow_down_black
                            )
                            vitalConstraintLayout.addFlowsheetDataVitalLabUnitTV.setCompoundDrawablesWithIntrinsicBounds(
                                null, null, endDrawable, null
                            )

                            popupMenu.setOnMenuItemClickListener { menuItem ->

                                if (menuItem.title?.contains(flowsheetEntry.unit ?: "") == false) {
                                    if (menuItem.title?.contains("C") == true) {
                                        flowsheetEntry.unit = "C"
                                        vitalConstraintLayout.addFlowsheetDataVitalLabUnitTV.setText(
                                            R.string.add_flowsheet_celsius
                                        )
                                        flowsheetVital.maxValue = 43

                                    } else {
                                        flowsheetEntry.unit = "F"
                                        vitalConstraintLayout.addFlowsheetDataVitalLabUnitTV.setText(
                                            R.string.add_flowsheet_farenheit
                                        )
                                        flowsheetVital.maxValue = 110
                                    }
                                }

                                val tempValue =
                                    vitalConstraintLayout.addFlowsheetDataVitalLabET.text.toString()
                                        .toDoubleOrNull()
                                if (tempValue != null) {
                                    if (tempValue > flowsheetVital.maxValue) {
                                        vitalConstraintLayout.addFlowsheetDataVitalLabET.setText("")
                                    }
                                }

                                return@setOnMenuItemClickListener true
                            }

                            vitalConstraintLayout.addFlowsheetDataVitalLabUnitTV.setOnClickListener {
                                popupMenu.show()
                            }
                        } else {
                            if (!unit.isNullOrBlank()) {
                                vitalConstraintLayout.addFlowsheetDataVitalLabUnitTV.text = unit
                            } else {
                                vitalConstraintLayout.addFlowsheetDataVitalLabUnitTV.text =
                                    flowsheetVital.unit
                            }
                        }
                    } else {
                        vitalConstraintLayout.addFlowsheetDataVitalLabUnitTV.text =
                            flowsheetVital.unit
                    }
                    vitalConstraintLayout.addFlowsheetDataVitalLabET.setText(flowsheetVital.value)
                    vitalConstraintLayout.addFlowsheetDataVitalLabET.tag = tagNo





                    if (!flowsheetVital.isDecimal) {
                        vitalConstraintLayout.addFlowsheetDataVitalLabET.inputType =
                            InputType.TYPE_CLASS_NUMBER
                    }


                    val currentBMICalIndex = bmiCalListTag.size

                    if (hasBMICal && flowsheetEntry.name?.equals(
                            ModelUtil.wellnessBMIModelId,
                            true
                        ) == true
                    ) {
                        vitalConstraintLayout.addFlowsheetDataVitalLabET.inputType =
                            InputType.TYPE_NULL
                        vitalConstraintLayout.addFlowsheetDataVitalLabET.isEnabled = false
                    } else {
                        vitalConstraintLayout.addFlowsheetDataVitalLabET.addTextChangedListener(
                            object : TextWatcher {
                                override fun afterTextChanged(s: Editable?) {

                                    val text = s?.toString() ?: ""
                                    val vitalValue = text.toIntOrNull()

                                    if (vitalValue != null) {

                                        if (vitalValue > flowsheetVital.maxValue) {
                                            vitalConstraintLayout.addFlowsheetDataVitalLabET.setText(
                                                text.substring(0, text.length - 1)
                                            )
                                        } else if (flowsheetVital.name.isNullOrBlank() && vitalValue == flowsheetVital.maxValue) {
                                            updatePrimaryVitalValue(
                                                vitalConstraintLayout.addFlowsheetDataVitalLabET.tag as Int,
                                                internalList[0]
                                            )
                                        }


                                    }
                                    if (hasBMICal) {
                                        if (flowsheetEntry.name?.equals(
                                                ModelUtil.wellnessWeightModelId,
                                                true
                                            ) == true || flowsheetEntry.name?.equals(
                                                ModelUtil.wellnessHeightModelId,
                                                true
                                            ) == true
                                        ) {
                                            updateBMICal(currentBMICalIndex)
                                        }
                                    }
                                }

                                override fun beforeTextChanged(
                                    s: CharSequence?,
                                    start: Int,
                                    count: Int,
                                    after: Int
                                ) {
                                }

                                override fun onTextChanged(
                                    s: CharSequence?,
                                    start: Int,
                                    before: Int,
                                    count: Int
                                ) {

                                }
                            })
                    }
                    tagList.add(tagNo++)

                    if (prevET != null) {
                        prevET?.imeOptions = EditorInfo.IME_ACTION_NEXT

                        prevET?.setOnEditorActionListener { textView, imeOption, keyEvent ->

                            if (imeOption == EditorInfo.IME_ACTION_NEXT) {

                                vitalConstraintLayout.addFlowsheetDataVitalLabET.requestFocus()
                                imm.showSoftInput(
                                    vitalConstraintLayout.addFlowsheetDataVitalLabET,
                                    InputMethodManager.SHOW_IMPLICIT
                                )

                                return@setOnEditorActionListener true
                            }

                            return@setOnEditorActionListener false
                        }
                    }

                    prevET = vitalConstraintLayout.addFlowsheetDataVitalLabET


                    flowLayout.addView(vitalConstraintLayout.root)
                    hasSpace = !hasSpace

                    if (lastIndex == index) {

                        val internalListIndex = internalList.indexOf(flowsheetVital)

                        if (internalList.size == 1 || internalListIndex != 0) {

                            val lp =
                                vitalConstraintLayout.root.layoutParams as ViewGroup.MarginLayoutParams
                            lp.bottomMargin = resources.getDimensionPixelSize(R.dimen.card_padding)
                            vitalConstraintLayout.root.layoutParams = lp
                        }
                    }


                    if (!addFlowsheetDataViewModel.flowsheet.allowEdit) {
                        vitalConstraintLayout.addFlowsheetDataVitalLabET.isEnabled = false
                    }
                }
            }


            val tag = tagList.joinToString(",")

            if (hasBMICal) {
                if (flowsheetEntry.name?.equals(
                        ModelUtil.wellnessWeightModelId,
                        true
                    ) == true || flowsheetEntry.name?.equals(
                        ModelUtil.wellnessHeightModelId,
                        true
                    ) == true || flowsheetEntry.name?.equals(
                        ModelUtil.wellnessBMIModelId,
                        true
                    ) == true
                ) {
                    bmiList.add(tag)
                }
            }
            tagToIdMap.put(tag, flowsheetEntry.flowSheetEntryId ?: "")
        }

        if (hasBMICal) {
            bmiCalListTag.add(bmiList)
        }

        cardView.addFlowsheetDataEntryLayout.addView(flowLayout)
        binding.addFlowsheetDataMainLayout.addView(cardView.root)
    }


    private fun animateOthersOption(mdTextInputEditText: MDTextInputEdittext, show: Boolean) {
        val heightValue = if (show) {
            100
        } else {
            0
        }
        val headerAnim =
            ValueAnimator.ofInt(mdTextInputEditText.height, heightValue).setDuration(300)
        headerAnim.addUpdateListener {
            try {
                mdTextInputEditText.layoutParams.height = it.animatedValue as Int
                mdTextInputEditText.requestLayout()
            } catch (e: IllegalStateException) {
            } catch (e: NullPointerException) {
            }
        }


        headerAnim.start()

    }


    private fun updatePrimaryVitalValue(secondaryTag: Int, primaryVital: FlowsheetVital) {

        val tag =
            tagToIdMap.keys.firstOrNull { it.endsWith(",$secondaryTag") }?.split(",")?.getOrNull(0)

        if (!tag.isNullOrBlank()) {

            val primaryET =
                binding.addFlowsheetDataMainLayout.findViewWithTag<MDTextInputEdittext>(tag.toInt())
            val primaryValue = (primaryET.text.toString().toIntOrNull() ?: 0) + 1

            val secondaryET =
                binding.addFlowsheetDataMainLayout.findViewWithTag<MDTextInputEdittext>(secondaryTag.toInt())

            if (primaryValue <= primaryVital.maxValue) {
                primaryET.setText("$primaryValue")
                secondaryET.setText("")
            } else {
                val text = secondaryET.text.toString()
                secondaryET.setText(text.substring(0, text.length - 1))
            }
        }
    }

    private fun updateBMICal(index: Int) {

        val bmiList = bmiCalListTag[index]

        var bmiET: MDTextInputEdittext? = null
        var weightPrimaryET: MDTextInputEdittext? = null
        var weightSecondaryET: MDTextInputEdittext? = null
        var heightPrimaryET: MDTextInputEdittext? = null
        var heightSecondaryET: MDTextInputEdittext? = null

        bmiList.forEach { tag ->
            val flowsheetEntryId = tagToIdMap.get(tag)

            val flowsheetEntry =
                addFlowsheetDataViewModel.flowsheet.flowsheetEntries.firstOrNull { it.flowSheetEntryId == flowsheetEntryId }

            if (flowsheetEntry?.name?.equals(ModelUtil.wellnessWeightModelId, true) == true) {

                val tagList = tag.split(",")

                weightPrimaryET =
                    binding.addFlowsheetDataMainLayout.findViewWithTag<MDTextInputEdittext>(
                        tagList.get(0).toInt()
                    )

                if (tagList.size > 1) {
                    weightSecondaryET =
                        binding.addFlowsheetDataMainLayout.findViewWithTag<MDTextInputEdittext>(
                            tagList.get(1).toInt()
                        )
                }
            } else if (flowsheetEntry?.name?.equals(
                    ModelUtil.wellnessHeightModelId,
                    true
                ) == true
            ) {

                val tagList = tag.split(",")

                heightPrimaryET =
                    binding.addFlowsheetDataMainLayout.findViewWithTag<MDTextInputEdittext>(
                        tagList.get(0).toInt()
                    )

                if (tagList.size > 1) {
                    heightSecondaryET =
                        binding.addFlowsheetDataMainLayout.findViewWithTag<MDTextInputEdittext>(
                            tagList.get(1).toInt()
                        )
                }
            } else {
                bmiET =
                    binding.addFlowsheetDataMainLayout.findViewWithTag<MDTextInputEdittext>(tag.toInt())
            }
        }


        val weightStr = weightPrimaryET?.text?.toString()
        val heightStr = heightPrimaryET?.text?.toString()

        val weightVal = weightStr?.toDoubleOrNull() ?: 0.0
        val heightVal = heightStr?.toDoubleOrNull() ?: 0.0

        if (heightSecondaryET == null) {

            if (!weightStr.isNullOrBlank() && !heightStr.isNullOrBlank()) {
                val bmi =
                    100 * 100 * weightVal / (heightVal * heightVal) // multiplication by 100*100 for cm to m conversion
                if (bmi == 0.0) {
                    bmiET?.setText("")
                } else {
                    bmiET?.setText(ModelUtil.getTwoDecimalStr(bmi))
                }
            } else {
                bmiET?.setText("")
            }
        } else {
            val weightSecStr = weightSecondaryET?.text?.toString()
            val heightSecStr = heightSecondaryET?.text?.toString()

            val weightSecVal = weightSecStr?.toDoubleOrNull() ?: 0.0
            val heightSecVal = heightSecStr?.toDoubleOrNull() ?: 0.0

            if ((weightStr.isNullOrBlank() && weightSecStr.isNullOrBlank()) || (heightStr.isNullOrBlank() && heightSecStr.isNullOrBlank())) {
                bmiET?.setText("")
            } else {

                val inchesValue = (heightVal * 12) + heightSecVal
                val poundsValue = weightVal + (weightSecVal / 16)

                val bmi = 703 * poundsValue / (inchesValue * inchesValue)
                if (bmi == 0.0) {
                    bmiET?.setText("")
                } else {
                    bmiET?.setText(ModelUtil.getTwoDecimalStr(bmi))
                }
            }
        }

    }

    private fun showDatePicker(textView: TextView, isDateTime: Boolean) {

        val value = textView.text.toString().trim()

        val localDate = if (value.isNotBlank()) {
            if (isDateTime) {
                DateTime.parse(value, ModelUtil.charmFlowsheetDataDateFormatter())
            } else {
                DateTime.parse(value, ModelUtil.charmDisplayDateFormat())
            }
        } else {
            DateTime.now()
        }


        val dialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, yr, mnth, dt ->

                val text = LocalDate(yr, mnth + 1, dt)

                if (isDateTime) {
                    showTimePicker(textView, text)
                } else {
                    textView.setText(text.toString(ModelUtil.charmDisplayDateFormat()))
                }

            },
            localDate.year, localDate.monthOfYear - 1, localDate.dayOfMonth
        )

        dialog.datePicker.minDate = LocalDate.now().minusYears(150).toDateTimeAtStartOfDay().millis
        if (BuildUtil.isThalassemia) {
            //Note: adding one year to cover next due date work flow
            dialog.datePicker.maxDate =
                LocalDate.now().withYear(DateTime.now().year + 1).dayOfYear().withMaximumValue()
                    .toDateTimeAtStartOfDay().millis
        } else {
            dialog.datePicker.maxDate = DateTime.now().millis
        }
        dialog.show()
    }

    private fun showTimePicker(textView: TextView, selectedDate: LocalDate) {

        val value = textView.text.toString().trim()

        val dateTime = if (value.isNotBlank()) {
            DateTime.parse(value, ModelUtil.charmFlowsheetDataDateFormatter())
        } else {
            DateTime.now()
        }


        val dialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { _, hrs, min ->

                val selectedTime = DateTime.parse(
                    "${selectedDate.toString(ModelUtil.charmDisplayDateFormat())} $hrs:$min",
                    DateTimeFormat.forPattern(ModelUtil.charmFlowsheetDateTimeFieldFormat)
                        .withLocale(Locale.ENGLISH)
                )
                textView.text = selectedTime.toString(ModelUtil.charmFlowsheetDataDateFormatter())

            },
            dateTime.hourOfDay, dateTime.minuteOfHour, false
        )





        dialog.show()
    }

    private fun updateBottomMargin(cardView: CardView, isLast: Boolean) {

        if (isLast) {
            val lp = cardView.layoutParams as ViewGroup.MarginLayoutParams
            lp.bottomMargin = resources.getDimensionPixelSize(R.dimen.card_padding)
            cardView.layoutParams = lp
        }
    }

    private fun addSeparatorAndHeadingInVitals(
        flowsheetEntry: FlowsheetEntries,
        flowLayout: FlowLayout,
        isFontSemi: Boolean = false
    ) {

        if (flowLayout.childCount != 0) {
            val separator = View(this)
            val separatorParam = LinearLayout.LayoutParams(
                MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.separator_height)
            )
            separatorParam.topMargin = resources.getDimensionPixelSize(R.dimen.card_padding)
            separator.layoutParams = separatorParam
            separator.setBackgroundResource(R.color.separatorColor)
            flowLayout.addView(separator)
        }

        val headingTv = TextView(this)
        val headingBPParam = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        headingBPParam.marginStart = resources.getDimensionPixelSize(R.dimen.card_padding)
        headingBPParam.marginEnd = headingBPParam.marginStart
        headingBPParam.topMargin = headingBPParam.marginStart

        headingTv.layoutParams = headingBPParam
        headingTv.setText(flowsheetEntry.name)
        headingTv.setTextColor(ContextCompat.getColor(this, R.color.black87))

        if (isFontSemi) {
            headingTv.typeface = UIUtil.getFont(this, R.font.open_sans_medium)
            headingTv.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.xx_small)
            )
        } else {
            headingTv.typeface = UIUtil.getFont(this, R.font.open_sans)
            headingTv.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.xx_small)
            )
        }

        flowLayout.addView(headingTv)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}