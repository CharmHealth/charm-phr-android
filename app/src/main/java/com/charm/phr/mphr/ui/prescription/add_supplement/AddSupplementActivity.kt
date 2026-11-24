package com.charm.phr.mphr.ui.prescription.add_supplement

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAddSupplementBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.DosageUnit
import com.charm.phr.mphr.model.Manufacturer
import com.charm.phr.mphr.model.PatientSupplementHistory
import com.charm.phr.mphr.model.PatientSupplementMap
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.model.SupplementCommonData
import com.charm.phr.mphr.model.SupplementList
import com.charm.phr.mphr.ui.components.MDAutoCompleteTextViewAdapter
import com.charm.phr.mphr.ui.components.MDTextInputEdittext
import com.charm.phr.mphr.util.MDAddBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.UIUtil
import com.google.gson.JsonObject
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.util.Locale
import javax.inject.Inject

class AddSupplementActivity : MDAddBaseActivity<AddSupplementViewModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var supplementNameAdapter: MDAutoCompleteTextViewAdapter

    lateinit var manufacturerAdapter: MDAutoCompleteTextViewAdapter

    lateinit var binding: ActivityAddSupplementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSupplementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UIUtil.applyViewInsertForToolbar(binding.addSupplementToolbar)
        UIUtil.applyViewInsertForBottomView(binding.addSupplementRootLayout)

        addViewModel.toEdit = intent.getBooleanExtra(ModelUtil.supplementToEditIntent, false)


        UIUtil.makeTextViewMandatory(binding.addSupplementNameLayout)
        UIUtil.makeTextViewMandatory(binding.addSupplementManufacturerLayout)
        UIUtil.makeTextViewMandatory(binding.addSupplementStartDateLayout)
        UIUtil.makeTextViewMandatory(binding.addSupplementEndDateLayout)


        if (addViewModel.toEdit) {

            val supplementStr = intent.getStringExtra(ModelUtil.supplementEditDataIntent) ?: ""

            binding.addSupplementToolbarText.setText(R.string.supplement_update_str)
            binding.addSupplementBtn.setText(R.string.update_str)

            addViewModel.supplementToBeEditted = MDParsingHelper.formSupplementObj(
                supplementStr.parseStringAsJsonObject() ?: JsonObject(), ""
            )


            binding.addSupplementNameET.inputType = InputType.TYPE_NULL
            binding.addSupplementNameET.isEnabled = false


            binding.addSupplementManufacturerET.inputType = InputType.TYPE_NULL
            binding.addSupplementManufacturerET.isEnabled = false


        } else {
            supplementNameAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())
            binding.addSupplementNameET.setAdapter(supplementNameAdapter)
        }

        manufacturerAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())
        binding.addSupplementManufacturerET.setAdapter(manufacturerAdapter)
        binding.addSupplementManufacturerET.dropDownAnchor = R.id.addSupplementManufacturerLayout

        if (!addViewModel.toEdit) {
            val practiceList =
                ModelUtil.getPHREntryEnabledPractices(authManager, ModelUtil.supplementModelId)

            if (practiceList.size == 1) {
                addViewModel.practiceId = practiceList.firstOrNull()?.practiceId ?: ""
                getCommonData()
                binding.addSupplementPracticeLayout.visibility = View.GONE
            } else {
                binding.addSupplementProgress.root.visibility = View.GONE
                binding.addSupplementPracticeLayout.visibility = View.VISIBLE
                binding.addSupplementPracticeET.setItems(
                    practiceList.mapNotNull { it.practiceName },
                    this
                )
                binding.addSupplementPracticeET.setOnItemClickListener { position, str ->
                    addViewModel.practiceId = practiceList[position].practiceId ?: ""
                    getCommonData(true)
                }
            }
        } else {


            addViewModel.practiceId = intent.getStringExtra(ModelUtil.practiceIdIntent) ?: ""
            getCommonData()
            binding.addSupplementPracticeLayout.visibility = View.GONE
        }


        binding.addSupplementIntakeET.setOnItemClickListener { position, str ->

            addViewModel.selectedIntake = addViewModel.supplementCommonData.intakeList[position]
        }



        binding.addSupplementIntakeWhenET.setOnItemClickListener { position, str ->

            addViewModel.selectedIntakeWhen =
                addViewModel.supplementCommonData.intakeWhenList[position]
        }

        binding.addSupplementDosageUnitET.setOnItemClickListener { position, str ->

            addViewModel.selectedDosageUnit =
                addViewModel.supplementCommonData.sxUnitAndName[position]
        }



        binding.addSupplementStartDateET.inputType = InputType.TYPE_NULL
        binding.addSupplementEndDateET.inputType = InputType.TYPE_NULL


        binding.addSupplementBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addSupplementStartDateET.setOnClickListener {
            showDatePicker(binding.addSupplementStartDateET)
        }

        binding.addSupplementStartDateET.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {
                showDatePicker(binding.addSupplementStartDateET)
            }
        }

        binding.addSupplementEndDateET.setOnClickListener {
            showDatePicker(binding.addSupplementEndDateET)
        }

        binding.addSupplementEndDateET.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {
                showDatePicker(binding.addSupplementEndDateET)
            }
        }

        getCommonData()


        binding.addSupplementErrorPage.errorPageTryAgainBtn.setOnClickListener {
            getCommonData()
        }

        /*addSupplementMemberNameET.setOnFocusChangeListener { view, hasFocus ->

            if (hasFocus){
                addSupplementMainLayout.scrollTo(0 , 50)
            }
        }
*/
        binding.addSupplementBtn.setOnClickListener {
            val supplement = validateData()

            if (supplement != null) {

                progressDialog.show()
                val responseLive = if (!addViewModel.toEdit) {
                    addViewModel.addSupplement(supplement)
                } else {
                    addViewModel.editSupplement(supplement)
                }

                responseLive.observe(this) { resp ->

                    if (resp != null) {
                        progressDialog.dismiss()

                        if (resp is Boolean) {
                            addViewModel.isDataAdded = true
                            setResult(Activity.RESULT_OK)
                            super.onBackPressed()
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

                        } else if (resp is APIResponseError) {

                            if (!addViewModel.toEdit) {
                                UIUtil.showToast(
                                    this,
                                    R.string.error_add_supplement_req_failed,
                                    Toast.LENGTH_LONG
                                )
                            } else {
                                UIUtil.showToast(
                                    this,
                                    R.string.error_edit_supplement_req_failed,
                                    Toast.LENGTH_LONG
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getCommonData(showDialog: Boolean = false) {


        if (showDialog) {
            progressDialog.show()
        } else {
            binding.addSupplementProgress.root.visibility = View.VISIBLE
            binding.addSupplementMainLayout.visibility = View.GONE
            binding.addSupplementErrorPage.root.visibility = View.GONE
        }

        addViewModel.getSupplementCommonData().observe(this) { resp ->

            if (resp != null) {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
                binding.addSupplementProgress.root.visibility = View.GONE
                if (resp is SupplementCommonData) {

                    addViewModel.supplementCommonData = resp
                    binding.addSupplementMainLayout.visibility = View.VISIBLE
                    binding.addSupplementStartDateET.setText(
                        DateTime.now().toString(ModelUtil.charmDisplayDateFormat())
                    )
                    binding.addSupplementEndDateET.setText(
                        DateTime.now().toString(ModelUtil.charmDisplayDateFormat())
                    )


                    binding.addSupplementIntakeUnitET.setItems(resp.sxStrengthUnitList, this)
                    binding.addSupplementIntakeUnitET.setAnchorView(binding.addSupplementIntakeUnitLayout)


                    binding.addSupplementDosageUnitET.setItems(
                        resp.sxUnitAndName.map { it.name },
                        this
                    )
                    binding.addSupplementDosageUnitET.setAnchorView(binding.addSupplementDosageUnitLayout)


                    binding.addSupplementIntakeET.setItems(resp.intakeList.map { it.name }, this)
                    binding.addSupplementIntakeET.setAnchorView(binding.addSupplementIntakeLayout)

                    binding.addSupplementIntakeWhenET.setItems(
                        resp.intakeWhenList.map { it.name },
                        this
                    )
                    binding.addSupplementIntakeWhenET.setAnchorView(binding.addSupplementIntakeWhenLayout)


                    manufacturerAdapter.setItemList(resp.manufacturerList)




                    if (addViewModel.toEdit) {

                        val supplementHistory =
                            addViewModel.supplementToBeEditted.patientSupplementMap?.patientSupplementHistory

                        if (supplementHistory != null) {

                            val commonData = addViewModel.supplementCommonData

                            val dosageUnit =
                                commonData.sxUnitAndName.filter { it.key == supplementHistory.dosageUnit?.dosageUnitId }
                                    .firstOrNull()

                            if (dosageUnit != null) {
                                binding.addSupplementDosageUnitET.setText(dosageUnit.name)
                                addViewModel.selectedDosageUnit = dosageUnit

                            } else {
                                binding.addSupplementDosageUnitET.setText(binding.addSupplementDosageUnitET.items[0])
                                addViewModel.selectedDosageUnit =
                                    addViewModel.supplementCommonData.sxUnitAndName[0]
                            }


                            val intake = commonData.intakeList.filter {
                                it.key.lowercase(
                                    Locale.ENGLISH
                                ) == supplementHistory.intake?.lowercase(
                                    Locale.ENGLISH
                                )
                            }.firstOrNull()

                            if (intake != null) {
                                binding.addSupplementIntakeET.setText(intake.name)
                                addViewModel.selectedIntake = intake

                            } else {
                                binding.addSupplementIntakeET.setText(binding.addSupplementIntakeET.items[0])
                                addViewModel.selectedIntake =
                                    addViewModel.supplementCommonData.intakeList[0]
                            }


                            val intakeWhen = commonData.intakeWhenList.firstOrNull {
                                it.key.equals(
                                    supplementHistory.intakeWhen,
                                    true
                                )
                            }

                            if (intakeWhen != null) {
                                binding.addSupplementIntakeWhenET.setText(intakeWhen.name)
                                addViewModel.selectedIntakeWhen = intakeWhen
                            } else {
                                binding.addSupplementIntakeWhenET.setText(binding.addSupplementIntakeWhenET.items[0])
                                addViewModel.selectedIntakeWhen =
                                    addViewModel.supplementCommonData.intakeWhenList[0]
                            }


                            binding.addSupplementManufacturerET.setText(addViewModel.supplementToBeEditted.manufacturer?.manufactureName)

                            binding.addSupplementStartDateET.setText(
                                supplementHistory.fromDate?.toString(
                                    ModelUtil.charmDisplayDateFormat()
                                )
                            )
                            binding.addSupplementEndDateET.setText(
                                supplementHistory.toDate?.toString(
                                    ModelUtil.charmDisplayDateFormat()
                                )
                            )

                            binding.addSupplementNoOfIntakeET.setText(
                                supplementHistory.numberOfIntake?.toFloat()?.toInt()?.toString()
                                    ?: ""
                            )
                            binding.addSupplementIntakeUnitET.setText(supplementHistory.intakeUnit)
                            binding.addSupplementDosageET.setText(supplementHistory.dosage)
                            binding.addSupplementCommentsET.setText(supplementHistory.comments)

                            binding.addSupplementNameET.setText(addViewModel.supplementToBeEditted.supplement?.supplementName)


                        }

                    } else {
                        supplementNameAdapter.setItemList(resp.searchString)

                    }


                } else if (resp is APIResponseError) {
                    binding.addSupplementErrorPage.root.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun validateData(): PatientSupplements? {

        val supplementName = binding.addSupplementNameET.text.toString()

        val comments = binding.addSupplementCommentsET.text.toString()


        if (isTextInputLayoutBlank(
                binding.addSupplementNameLayout,
                binding.addSupplementScrollView,
                R.string.error_empty_supplement_name
            )
        ) {
            return null
        }

        /*if (supplementName.isBlank()){
            UIUtil.showToast(this , (R.string.error_empty_supplement_name) , Toast.LENGTH_LONG)
            return null
        }*/

        if (!addViewModel.toEdit) {
            if (isTextInputLayoutBlank(
                    binding.addSupplementNameLayout,
                    binding.addSupplementScrollView,
                    R.string.error_invalid_supplement_name,
                    true
                )
            ) {
                return null
            }
        }

        /* if (!ModelUtil.hasAlpha(supplementName) && !addViewModel.toEdit){
             UIUtil.showToast(this , (R.string.error_invalid_supplement_name) , Toast.LENGTH_LONG)
             return null
         }*/

        val manufacturerName = binding.addSupplementManufacturerET.text.toString()

        if (isTextInputLayoutBlank(
                binding.addSupplementManufacturerLayout,
                binding.addSupplementScrollView,
                R.string.error_empty_manufacturer_name
            )
        ) {
            return null
        }

        if (isTextInputLayoutBlank(
                binding.addSupplementManufacturerLayout,
                binding.addSupplementScrollView,
                R.string.error_invalid_manufacturer_name,
                true
            )
        ) {
            return null
        }



        if (comments.isNotBlank()) {
            if (isTextInputLayoutBlank(
                    binding.addSupplementCommentsLayout,
                    binding.addSupplementScrollView,
                    R.string.error_invalid_comments,
                    true
                )
            ) {
                return null
            }
        }
        /*    if (manufacturerName.isBlank()){
                UIUtil.showToast(this , (R.string.error_empty_manufacturer_name) , Toast.LENGTH_LONG)
                return null
            }


            if (!ModelUtil.hasAlpha(comments)){
                UIUtil.showToast(this , (R.string.error_invalid_comments) , Toast.LENGTH_LONG)
                return null
            }*/

        val patSupplement = if (addViewModel.toEdit) {
            addViewModel.supplementToBeEditted
        } else {
            PatientSupplements()
        }


        if (!addViewModel.toEdit) {


            val supplement = SupplementList()
            supplement.supplementName = supplementName
            patSupplement.supplement = supplement
        }

        if (patSupplement.patientSupplementMap == null) {

            val patientSupplementsMap = PatientSupplementMap()
            val patientSupplementHistory = PatientSupplementHistory()


            val dosageUnit = DosageUnit()

            patientSupplementHistory.dosageUnit = dosageUnit



            patientSupplementsMap.patientSupplementHistory = patientSupplementHistory
            patSupplement.patientSupplementMap = patientSupplementsMap
        }

        if (patSupplement.manufacturer == null) {
            patSupplement.manufacturer = Manufacturer()
        }

        val fromDate = DateTime.parse(
            binding.addSupplementStartDateET.text.toString(),
            ModelUtil.charmDisplayDateFormat()
        )
        val toDate = DateTime.parse(
            binding.addSupplementEndDateET.text.toString(),
            ModelUtil.charmDisplayDateFormat()
        )

        if (toDate.isBefore(fromDate)) {
            UIUtil.showToast(this, R.string.error_invalid_period, Toast.LENGTH_LONG)
            return null
        }



        patSupplement.patientSupplementMap?.patientSupplementHistory?.fromDate = fromDate
        patSupplement.patientSupplementMap?.patientSupplementHistory?.toDate = toDate

        patSupplement.patientSupplementMap?.patientSupplementHistory?.comments = comments
        patSupplement.patientSupplementMap?.patientSupplementHistory?.intake =
            addViewModel.selectedIntake.key
        patSupplement.manufacturer?.manufactureName = manufacturerName
        patSupplement.patientSupplementMap?.patientSupplementHistory?.intakeWhen =
            addViewModel.selectedIntakeWhen.key
        patSupplement.patientSupplementMap?.patientSupplementHistory?.dosageUnit?.dosageUnitId =
            addViewModel.selectedDosageUnit.key
        patSupplement.patientSupplementMap?.patientSupplementHistory?.dosageUnit?.dosageUnit =
            addViewModel.selectedDosageUnit.name

        patSupplement.patientSupplementMap?.patientSupplementHistory?.numberOfIntake =
            binding.addSupplementNoOfIntakeET.text.toString()
        patSupplement.patientSupplementMap?.patientSupplementHistory?.intakeUnit =
            binding.addSupplementIntakeUnitET.text.toString()
        patSupplement.patientSupplementMap?.patientSupplementHistory?.dosage =
            binding.addSupplementDosageET.text.toString()



        patSupplement.practiceId = addViewModel.practiceId



        return patSupplement
    }

    override fun checkDataIsAvailable(): Boolean {


        if (addViewModel.supplementCommonData.searchString.isNotEmpty()) {


            if (binding.addSupplementNameET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addSupplementNoOfIntakeET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addSupplementIntakeUnitET.text.toString() != binding.addSupplementIntakeUnitET.items[0]) {
                return true
            }

            if (binding.addSupplementDosageET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addSupplementDosageUnitET.text.toString() != binding.addSupplementDosageUnitET.items[0]) {
                return true
            }

            if (binding.addSupplementManufacturerET.text.toString().isNotBlank()) {

            }

            if (binding.addSupplementIntakeET.text.toString() != binding.addSupplementIntakeET.items[0]) {
                return true
            }

            if (binding.addSupplementIntakeWhenET.text.toString() != binding.addSupplementIntakeWhenET.items[0]) {
                return true
            }


            if (binding.addSupplementStartDateET.text.toString() != DateTime.now()
                    .toString(ModelUtil.charmDisplayDateFormat())
            ) {
                return true
            }

            if (binding.addSupplementEndDateET.text.toString() != DateTime.now()
                    .toString(ModelUtil.charmDisplayDateFormat())
            ) {
                return true
            }

            if (binding.addSupplementCommentsET.text.toString().isNotBlank()) {
                return true
            }
        }

        return false
    }

    override fun checkDataIsEditted(): Boolean {

        if (binding.addSupplementNameET.text.toString().isNotBlank()) {

            if (addViewModel.supplementCommonData.searchString.isNotEmpty()) {
                val patHistory =
                    addViewModel.supplementToBeEditted.patientSupplementMap?.patientSupplementHistory

                if (patHistory != null) {

                    if (binding.addSupplementNoOfIntakeET.text.toString()
                            .trim() != (patHistory.numberOfIntake?.trim() ?: "")
                    ) {
                        return true
                    }

                    if (binding.addSupplementIntakeUnitET.text.toString()
                            .trim() != (patHistory.intakeUnit?.trim() ?: "")
                    ) {
                        return true
                    }

                    if (binding.addSupplementDosageET.text.toString()
                            .trim() != (patHistory.dosage?.trim() ?: "")
                    ) {
                        return true
                    }

                    if (binding.addSupplementDosageUnitET.text.toString()
                            .trim() != (patHistory.dosageUnit?.dosageUnit ?: "")
                    ) {
                        return true
                    }

                    if (binding.addSupplementIntakeET.text.toString().lowercase(Locale.ENGLISH)
                            .trim() != (patHistory.intake?.lowercase(
                            Locale.ENGLISH
                        )?.trim() ?: "")
                    ) {
                        return true
                    }

                    if (binding.addSupplementIntakeWhenET.text.toString().lowercase(Locale.ENGLISH)
                            .trim() != (patHistory.intakeWhen?.lowercase(
                            Locale.ENGLISH
                        ) ?: "")
                    ) {
                        return true
                    }

                    if (binding.addSupplementManufacturerET.text.toString()
                            .trim() != (addViewModel.supplementToBeEditted.manufacturer?.manufactureName?.trim()
                            ?: "")
                    ) {
                        return true
                    }

                    if (binding.addSupplementStartDateET.text.toString() != patHistory.fromDate?.toString(
                            ModelUtil.charmDisplayDateFormat()
                        )
                    ) {
                        return true
                    }

                    if (binding.addSupplementEndDateET.text.toString() != patHistory.toDate?.toString(
                            ModelUtil.charmDisplayDateFormat()
                        )
                    ) {
                        return true
                    }

                    if (binding.addSupplementCommentsET.text.toString()
                            .trim() != (patHistory.comments?.trim() ?: "")
                    ) {
                        return true
                    }

                }
            }
        }
        return false
    }

    private fun showDatePicker(mdTextInputEdittext: MDTextInputEdittext) {


        val value = mdTextInputEdittext.text.toString().trim()

        val localDate = if (value.isNotBlank()) {
            LocalDate.parse(value, ModelUtil.charmDisplayDateFormat())
        } else {
            LocalDate.now()
        }

        val dialog = DatePickerDialog(
            this,
            { _, yr, mnth, dt ->

                val text = LocalDate(yr, mnth + 1, dt)
                mdTextInputEdittext.setText(text.toString(ModelUtil.charmDisplayDateFormat()))

            },
            localDate.year, localDate.monthOfYear - 1, localDate.dayOfMonth
        )

        dialog.datePicker.minDate =
            LocalDate(LocalDate.now().year - 150, 1, 1).toDateTimeAtStartOfDay().millis
        dialog.datePicker.maxDate =
            LocalDate(LocalDate.now().year + 150, 1, 1).toDateTimeAtStartOfDay().millis

        dialog.show()
    }

    override fun getViewModel(): AddSupplementViewModel {
        return ViewModelProvider(this, viewModelFactory).get(AddSupplementViewModel::class.java)
    }
}
