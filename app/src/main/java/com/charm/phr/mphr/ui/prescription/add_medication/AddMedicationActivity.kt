package com.charm.phr.mphr.ui.prescription.add_medication

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAddMedicationBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.DataWithId
import com.charm.phr.mphr.model.DrugDetails
import com.charm.phr.mphr.model.MedicationCommonData
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.PatientPrescriptionHistory
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

class AddMedicationActivity : MDAddBaseActivity<AddMedicationViewModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var medicationNameAdapter: MDAutoCompleteTextViewAdapter

    private lateinit var binding: ActivityAddMedicationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UIUtil.applyViewInsertForToolbar(binding.addMedicationToolbar)
        UIUtil.applyViewInsertForBottomView(binding.addMedicationMainLayout)

        addViewModel.isHealthPassport = intent.getBooleanExtra(ModelUtil.isHealthPassportIntent, false)
        addViewModel.toEdit = intent.getBooleanExtra(ModelUtil.medicationToEditIntent, false)

        UIUtil.makeTextViewMandatory(binding.addMedicationNameLayout)
        UIUtil.makeTextViewMandatory(binding.addMedicationMedConditionLayout)
        UIUtil.makeTextViewMandatory(binding.addMedicationStartDateLayout)
        UIUtil.makeTextViewMandatory(binding.addMedicationEndDateLayout)



        if (addViewModel.toEdit) {

            val medicationStr = intent.getStringExtra(ModelUtil.medicationEditDataIntent) ?: ""

            binding.addMedicationToolbarText.setText(R.string.medication_update_str)
            binding.addMedicationBtn.setText(R.string.update_str)

            addViewModel.medicationToBeEditted = MDParsingHelper.formMedicationObj(
                medicationStr.parseStringAsJsonObject() ?: JsonObject(), ""
            )


            binding.addMedicationNameET.inputType = InputType.TYPE_NULL
            binding.addMedicationNameET.isEnabled = false

            binding.addMedicationMedConditionET.inputType = InputType.TYPE_NULL
            binding.addMedicationMedConditionET.isEnabled = false

            binding.addMedicationMedConditionLayout.visibility = View.GONE

        } else {
            medicationNameAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())
            binding.addMedicationNameET.setAdapter(medicationNameAdapter)
        }


        if (addViewModel.isHealthPassport) {
            binding.addMedicationRefillsLayout.visibility = View.GONE
            binding.addMedicationIntakeWhenLayout.visibility = View.GONE
            binding.addMedicationCommentsLayout.visibility = View.GONE
        }

        if (!addViewModel.toEdit && !addViewModel.isHealthPassport) {


            val practiceList =
                ModelUtil.getPHREntryEnabledPractices(authManager, ModelUtil.medicationModelId)
            if (practiceList.size == 1) {
                addViewModel.practiceId = practiceList.firstOrNull()?.practiceId ?: ""
                getCommonData()
                binding.addMedicationPracticeLayout.visibility = View.GONE
            } else {
                binding.addMedicationProgress.root.visibility = View.GONE
                binding.addMedicationPracticeLayout.visibility = View.VISIBLE
                binding.addMedicationPracticeET.setItems(
                    practiceList.mapNotNull { it.practiceName },
                    this
                )
                binding.addMedicationPracticeET.setOnItemClickListener { position, str ->
                    addViewModel.practiceId =
                        practiceList[position].practiceId ?: ""
                    getCommonData(true)
                }
            }


        } else {
            addViewModel.practiceId = intent.getStringExtra(ModelUtil.practiceIdIntent) ?: ""
            getCommonData()
            binding.addMedicationPracticeLayout.visibility = View.GONE
        }


        binding.addMedicationIntakeET.setOnItemClickListener { position, str ->

            addViewModel.selectedIntake = addViewModel.medicationCommonData.intakeList[position]
        }

        binding.addMedicationMedConditionET.setOnItemClickListener { position, str ->

            addViewModel.selectedMedConditionRoute =
                addViewModel.medicationCommonData.medNameList[position]
        }

        binding.addMedicationIntakeWhenET.setOnItemClickListener { position, str ->

            addViewModel.selectedIntakeWhen =
                addViewModel.medicationCommonData.intakeWhenList[position]
        }

        binding.addMedicationDosageUnitET.setOnItemClickListener { position, str ->

            addViewModel.selectedDosageUnit =
                addViewModel.medicationCommonData.rxUnitAndName[position]
        }



        binding.addMedicationStartDateET.inputType = InputType.TYPE_NULL
        binding.addMedicationEndDateET.inputType = InputType.TYPE_NULL


        binding.addMedicationBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addMedicationStartDateET.setOnClickListener {
            showDatePicker(binding.addMedicationStartDateET)
        }

        binding.addMedicationStartDateET.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {
                showDatePicker(binding.addMedicationStartDateET)
            }
        }

        binding.addMedicationEndDateET.setOnClickListener {
            showDatePicker(binding.addMedicationEndDateET)
        }

        binding.addMedicationEndDateET.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {
                showDatePicker(binding.addMedicationEndDateET)
            }
        }

        getCommonData()


        binding.addMedicationErrorPage.errorPageTryAgainBtn.setOnClickListener {
            getCommonData()
        }

        /*binding.addMedicationMemberNameET.setOnFocusChangeListener { view, hasFocus ->

            if (hasFocus){
                addMedicationMainLayout.scrollTo(0 , 50)
            }
        }
*/
        binding.addMedicationBtn.setOnClickListener {
            val medication = validateData()

            if (medication != null) {

                progressDialog.show()
                val responseLive = if (!addViewModel.toEdit) {
                    addViewModel.addMedication(medication)
                } else {
                    addViewModel.editMedication(medication)
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

                            if (resp.charmApiCode == 5073 && resp.errorMsg != null) {
                                UIUtil.showToast(
                                    this,
                                    resp.errorMsg
                                        ?: getString(R.string.error_add_medication_req_failed),
                                    Toast.LENGTH_LONG
                                )
                            } else {
                                if (!addViewModel.toEdit) {
                                    UIUtil.showToast(
                                        this,
                                        R.string.error_add_medication_req_failed,
                                        Toast.LENGTH_LONG
                                    )
                                } else {
                                    UIUtil.showToast(
                                        this,
                                        R.string.error_edit_medication_req_failed,
                                        Toast.LENGTH_LONG
                                    )
                                }
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
            binding.addMedicationProgress.root.visibility = View.VISIBLE
            binding.addMedicationMainLayout.visibility = View.GONE
            binding.addMedicationErrorPage.root.visibility = View.GONE
        }

        addViewModel.getMedicationCommonData().observe(this, Observer { resp ->

            if (resp != null) {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
                binding.addMedicationProgress.root.visibility = View.GONE
                if (resp is MedicationCommonData) {

                    addViewModel.medicationCommonData = resp
                    binding.addMedicationMainLayout.visibility = View.VISIBLE
                    binding.addMedicationStartDateET.setText(
                        DateTime.now().toString(ModelUtil.charmDisplayDateFormat())
                    )
                    binding.addMedicationEndDateET.setText(
                        DateTime.now().toString(ModelUtil.charmDisplayDateFormat())
                    )


                    binding.addMedicationIntakeUnitET.setItems(resp.rxStrengthUnitList, this)
                    binding.addMedicationIntakeUnitET.setAnchorView(binding.addMedicationIntakeUnitLayout)


                    binding.addMedicationDosageUnitET.setItems(
                        resp.rxUnitAndName.map { it.name },
                        this
                    )
                    binding.addMedicationDosageUnitET.setAnchorView(binding.addMedicationDosageUnitLayout)


                    binding.addMedicationIntakeET.setItems(resp.intakeList.map { it.name }, this)
                    binding.addMedicationIntakeET.setAnchorView(binding.addMedicationIntakeLayout)

                    binding.addMedicationIntakeWhenET.setItems(
                        resp.intakeWhenList.map { it.name },
                        this
                    )
                    binding.addMedicationIntakeWhenET.setAnchorView(binding.addMedicationIntakeWhenLayout)


                    binding.addMedicationIntakeRouteET.setItems(resp.routeList, this)
                    binding.addMedicationIntakeRouteET.setAnchorView(binding.addMedicationIntakeRouteLayout)


                    binding.addMedicationMemberNameET.setItems(resp.providerList, this)
                    binding.addMedicationMemberNameET.setAnchorView(binding.addMedicationMemberNameLayout)



                    if (addViewModel.toEdit) {

                        val patHistory =
                            addViewModel.medicationToBeEditted.patMedPHRExt.firstOrNull()

                        if (patHistory != null) {

                            val commonData = addViewModel.medicationCommonData

                            val dosageUnit =
                                commonData.rxUnitAndName.filter { it.key == patHistory.dosageUnitId }
                                    .firstOrNull()

                            if (dosageUnit != null) {
                                binding.addMedicationDosageUnitET.setText(dosageUnit.name)
                                addViewModel.selectedDosageUnit = dosageUnit

                            } else {
                                binding.addMedicationDosageUnitET.setText(binding.addMedicationDosageUnitET.items[0])
                                addViewModel.selectedDosageUnit =
                                    addViewModel.medicationCommonData.rxUnitAndName[0]
                            }


                            val intake = commonData.intakeList.filter {
                                it.key.lowercase(
                                    Locale.ENGLISH
                                ) == patHistory.intake?.lowercase(
                                    Locale.ENGLISH
                                )
                            }.firstOrNull()

                            if (intake != null) {
                                binding.addMedicationIntakeET.setText(intake?.name)
                                addViewModel.selectedIntake = intake

                            } else {
                                binding.addMedicationIntakeET.setText(binding.addMedicationIntakeET.items[0])
                                addViewModel.selectedIntake =
                                    addViewModel.medicationCommonData.intakeList[0]
                            }


                            val intakeWhen = commonData.intakeWhenList.firstOrNull {
                                it.key.equals(
                                    patHistory.intakeWhen,
                                    true
                                )
                            }

                            if (intakeWhen != null) {
                                binding.addMedicationIntakeWhenET.setText(intakeWhen.name)
                                addViewModel.selectedIntakeWhen = intakeWhen
                            } else {
                                binding.addMedicationIntakeWhenET.setText(binding.addMedicationIntakeWhenET.items[0])
                                addViewModel.selectedIntakeWhen =
                                    addViewModel.medicationCommonData.intakeWhenList[0]
                            }


                            val medicalCondition =
                                commonData.medNameList.filter { it.key == patHistory.medicalConditionId }
                                    .firstOrNull()
                            binding.addMedicationMedConditionET.setText(medicalCondition?.name)
                            addViewModel.selectedMedConditionRoute =
                                medicalCondition ?: DataWithId()

                            binding.addMedicationStartDateET.setText(
                                patHistory.fromDate?.toString(
                                    ModelUtil.charmDisplayDateFormat()
                                )
                            )
                            binding.addMedicationEndDateET.setText(
                                patHistory.toDate?.toString(
                                    ModelUtil.charmDisplayDateFormat()
                                )
                            )

                            binding.addMedicationNoOfIntakeET.setText(patHistory.noOfIntakes?.toFloat()?.toInt()?.toString()?:"")

                            binding.addMedicationIntakeUnitET.setText(patHistory.intakeUnit)
                            binding.addMedicationDosageET.setText(patHistory.dosage)
                            binding.addMedicationIntakeRouteET.setText(patHistory.intakeRoute)
                            binding.addMedicationMemberNameET.setText(
                                UIUtil.formMemberName(
                                    addViewModel.medicationToBeEditted.member
                                )
                            )
                            binding.addMedicationRefillsET.setText(patHistory.refills)
                            binding.addMedicationCommentsET.setText(patHistory.comments)

                            binding.addMedicationNameET.setText(addViewModel.medicationToBeEditted.drugDetail.firstOrNull()?.genericDrugName)


                        }

                    } else {
                        medicationNameAdapter.setItemList(resp.searchString)

                        binding.addMedicationMedConditionET.setItems(
                            resp.medNameList.map { it.name },
                            this
                        )
                        binding.addMedicationMedConditionET.setAnchorView(binding.addMedicationMedConditionLayout)
                    }


                } else if (resp is APIResponseError) {
                    binding.addMedicationErrorPage.root.visibility = View.VISIBLE
                }
            }
        })
    }


    /**
     *
     * */

    private fun validateData(): PatMedPOJO? {

        val medicationName = binding.addMedicationNameET.text.toString()

        val comments = binding.addMedicationCommentsET.text.toString()


        /*if (medicationName.isBlank() ){
            UIUtil.showToast(this , (R.string.error_empty_drug_name) , Toast.LENGTH_LONG)

            return null
        }*/



        if (isTextInputLayoutBlank(
                binding.addMedicationNameLayout,
                binding.addMedicationScrollView,
                R.string.error_empty_drug_name
            )
        ) {
            return null
        }


        /* if (medicationName.isBlank()) {
             UIUtil.showToast(this , (R.string.error_empty_drug_name) , Toast.LENGTH_LONG)

             addMedicationNameLayout.error = getString(R.string.error_empty_drug_name)

             addMedicationScrollView.smoothScrollTo(0 , addMedicationNameLayout.top)
             handler.postDelayed({
                 binding.addMedicationNameET.requestFocus()
                 imm.showSoftInput(binding.addMedicationNameET , InputMethodManager.SHOW_IMPLICIT)

             } , ModelUtil.addErrorAnimationDelayDuration)
             return null
         } else {
             addMedicationNameLayout.error = null
         }*/


        if (!addViewModel.toEdit) {
            if (isTextInputLayoutBlank(
                    binding.addMedicationNameLayout,
                    binding.addMedicationScrollView,
                    R.string.error_invalid_drug_name,
                    true
                )
            ) {
                return null
            }
        }

        /* if (!ModelUtil.hasAlpha( medicationName) && !addViewModel.toEdit){
             UIUtil.showToast(this , (R.string.error_invalid_drug_name) , Toast.LENGTH_LONG)
             addMedicationNameLayout.error = getString(R.string.error_invalid_drug_name)

             addMedicationScrollView.smoothScrollTo(0 , addMedicationNameLayout.top)
             handler.postDelayed({
                 binding.addMedicationNameET.requestFocus()
                 imm.showSoftInput(binding.addMedicationNameET , InputMethodManager.SHOW_IMPLICIT)

             } , ModelUtil.addErrorAnimationDelayDuration)
             return null
         }
         else {
             addMedicationNameLayout.error = null
         }*/


        val fromDate = DateTime.parse(
            binding.addMedicationStartDateET.text.toString(),
            ModelUtil.charmDisplayDateFormat()
        )
        val toDate = DateTime.parse(
            binding.addMedicationEndDateET.text.toString(),
            ModelUtil.charmDisplayDateFormat()
        )

        if (toDate.isBefore(fromDate)) {
            UIUtil.showToast(this, R.string.error_invalid_period, Toast.LENGTH_LONG)
            return null
        }

        val patMedPOJO = if (addViewModel.toEdit) {
            addViewModel.medicationToBeEditted
        } else {
            PatMedPOJO()
        }


        if (!addViewModel.toEdit) {


            if (isTextInputLayoutBlank(
                    binding.addMedicationMedConditionLayout,
                    binding.addMedicationScrollView,
                    R.string.error_empty_med_condition
                )
            ) {
                return null
            }
            /* if (binding.addMedicationMedConditionET.position == -1) {
                 UIUtil.showToast(this , (R.string.error_empty_med_condition) , Toast.LENGTH_LONG)

                 addMedicationScrollView.smoothScrollTo(0 , addMedicationMedConditionLayout.top)
                 binding.addMedicationMedConditionET.requestFocus()
                 handler.postDelayed({
                     imm.showSoftInput(binding.addMedicationMedConditionET , InputMethodManager.SHOW_IMPLICIT)

                 } , ModelUtil.addErrorAnimationDelayDuration)
                 return null
             }*/

            val drugDetails = DrugDetails()
            drugDetails.genericDrugName = medicationName
            patMedPOJO.drugDetail = arrayListOf(drugDetails)
        }

        if (comments.isNotBlank() && !addViewModel.isHealthPassport) {
            if (isTextInputLayoutBlank(
                    binding.addMedicationCommentsLayout,
                    binding.addMedicationScrollView,
                    R.string.error_invalid_comments,
                    true
                )
            ) {
                return null
            }
        }

        if (patMedPOJO.patMedPHRExt.isEmpty()) {
            patMedPOJO.patMedPHRExt = arrayListOf(PatientPrescriptionHistory())
        }



        patMedPOJO.patMedPHRExt.firstOrNull()?.fromDate = fromDate
        patMedPOJO.patMedPHRExt.firstOrNull()?.toDate = toDate

        patMedPOJO.patMedPHRExt.firstOrNull()?.comments = comments
        patMedPOJO.patMedPHRExt.firstOrNull()?.intake = addViewModel.selectedIntake.key
        patMedPOJO.patMedPHRExt.firstOrNull()?.intakeRoute =
            binding.addMedicationIntakeRouteET.text.toString()
        patMedPOJO.patMedPHRExt.firstOrNull()?.medicalConditionId =
            addViewModel.selectedMedConditionRoute.key
        patMedPOJO.patMedPHRExt.firstOrNull()?.intakeWhen = addViewModel.selectedIntakeWhen.key
        patMedPOJO.patMedPHRExt.firstOrNull()?.dosageUnitId = addViewModel.selectedDosageUnit.key
        patMedPOJO.patMedPHRExt.firstOrNull()?.dosageUnit = addViewModel.selectedDosageUnit.name

        patMedPOJO.patMedPHRExt.firstOrNull()?.noOfIntakes =
            binding.addMedicationNoOfIntakeET.text.toString()
        patMedPOJO.patMedPHRExt.firstOrNull()?.intakeUnit =
            binding.addMedicationIntakeUnitET.text.toString()
        patMedPOJO.patMedPHRExt.firstOrNull()?.dosage =
            binding.addMedicationDosageET.text.toString()

        patMedPOJO.patMedPHRExt.firstOrNull()?.refills =
            binding.addMedicationRefillsET.text.toString()

        patMedPOJO.member = Member(fullName = binding.addMedicationMemberNameET.text.toString())
        patMedPOJO.practiceId = addViewModel.practiceId
        patMedPOJO.isHealthPassport = addViewModel.isHealthPassport

        return patMedPOJO
    }

    override fun checkDataIsAvailable(): Boolean {


        if (binding.addMedicationIntakeUnitET.items != null && binding.addMedicationIntakeUnitET.items.isNotEmpty()) {


            if (binding.addMedicationNameET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addMedicationNoOfIntakeET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addMedicationIntakeUnitET.text.toString() != binding.addMedicationIntakeUnitET.items[0]) {
                return true
            }

            if (binding.addMedicationDosageET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addMedicationDosageUnitET.text.toString() != binding.addMedicationDosageUnitET.items[0]) {
                return true
            }

            if (binding.addMedicationIntakeET.text.toString() != binding.addMedicationIntakeET.items[0]) {
                return true
            }



            if (binding.addMedicationIntakeWhenET.text.toString() != binding.addMedicationIntakeWhenET.items[0] && !addViewModel.isHealthPassport) {
                return true
            }

            if (binding.addMedicationIntakeRouteET.text.toString() != binding.addMedicationIntakeRouteET.items[0]) {
                return true
            }

            if (binding.addMedicationStartDateET.text.toString() != DateTime.now()
                    .toString(ModelUtil.charmDisplayDateFormat())
            ) {
                return true
            }

            if (binding.addMedicationEndDateET.text.toString() != DateTime.now()
                    .toString(ModelUtil.charmDisplayDateFormat())
            ) {
                return true
            }

            if (binding.addMedicationMedConditionET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addMedicationMemberNameET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addMedicationRefillsET.text.toString()
                    .isNotBlank() && !addViewModel.isHealthPassport
            ) {
                return true
            }

            if (binding.addMedicationCommentsET.text.toString()
                    .isNotBlank() && !addViewModel.isHealthPassport
            ) {
                return true
            }
        }

        return false
    }

    override fun checkDataIsEditted(): Boolean {

        if (binding.addMedicationNameET.text.toString().isNotBlank()) {

            val patHistory = addViewModel.medicationToBeEditted.patMedPHRExt.firstOrNull()

            if (patHistory != null) {

                if (binding.addMedicationNoOfIntakeET.text.toString()
                        .trim() != patHistory.noOfIntakes ?: ""
                ) {
                    Log.d(
                        "MDAddMedication",
                        "noOfIntake ;view -> ${
                            binding.addMedicationNoOfIntakeET.text.toString().trim()
                        } ; local -> ${patHistory.noOfIntakes}"
                    )
                    return true
                }

                if (binding.addMedicationIntakeUnitET.text.toString()
                        .trim() != patHistory.intakeUnit ?: ""
                ) {
                    Log.d(
                        "MDAddMedication",
                        "intakeUnit ;view -> ${
                            binding.addMedicationIntakeUnitET.text.toString().trim()
                        } ; local -> ${patHistory.intakeUnit}"
                    )
                    return true
                }

                if (binding.addMedicationDosageET.text.toString()
                        .trim() != patHistory.dosage ?: ""
                ) {
                    Log.d(
                        "MDAddMedication",
                        "dosage ;view -> ${
                            binding.addMedicationDosageET.text.toString().trim()
                        } ; local -> ${patHistory.dosage}"
                    )
                    return true
                }

                if (binding.addMedicationDosageUnitET.text.toString()
                        .trim() != patHistory.dosageUnit ?: ""
                ) {
                    Log.d(
                        "MDAddMedication",
                        "dosage unit ;view -> ${
                            binding.addMedicationDosageUnitET.text.toString().trim()
                        } ; local -> ${patHistory.dosageUnit}"
                    )
                    return true
                }

                if (binding.addMedicationIntakeET.text.toString().lowercase(Locale.ENGLISH)
                        .trim() != patHistory.intake?.lowercase(Locale.ENGLISH)?.trim() ?: ""
                ) {
                    Log.d(
                        "MDAddMedication",
                        "intake ;view -> ${
                            binding.addMedicationIntakeET.text.toString().trim()
                        } ; local -> ${patHistory.intake}"
                    )
                    return true
                }

                if (binding.addMedicationIntakeWhenET.text.toString().lowercase(Locale.ENGLISH)
                        .trim() != patHistory.intakeWhen?.lowercase(Locale.ENGLISH) ?: "" && !addViewModel.isHealthPassport
                ) {
                    Log.d(
                        "MDAddMedication",
                        "intakeWhen ;view -> ${
                            binding.addMedicationIntakeWhenET.text.toString().trim()
                        } ; local -> ${patHistory.intakeWhen}"
                    )
                    return true
                }

                if (binding.addMedicationIntakeRouteET.text.toString()
                        .trim() != patHistory.intakeRoute?.trim() ?: ""
                ) {
                    Log.d(
                        "MDAddMedication",
                        "intakeRoute ;view -> ${
                            binding.addMedicationIntakeRouteET.text.toString().trim()
                        } ; local -> ${patHistory.intakeRoute}"
                    )
                    return true
                }

                if (binding.addMedicationStartDateET.text.toString() != patHistory.fromDate?.toString(
                        ModelUtil.charmDisplayDateFormat()
                    )
                ) {
                    Log.d(
                        "MDAddMedication",
                        "start date ;view -> ${
                            binding.addMedicationStartDateET.text.toString().trim()
                        } ; local -> ${patHistory.fromDate?.toString(ModelUtil.charmDisplayDateFormat())}"
                    )
                    return true
                }

                if (binding.addMedicationEndDateET.text.toString() != patHistory.toDate?.toString(
                        ModelUtil.charmDisplayDateFormat()
                    )
                ) {
                    Log.d(
                        "MDAddMedication",
                        "end date ;view -> ${
                            binding.addMedicationStartDateET.text.toString().trim()
                        } ; local -> ${patHistory.toDate?.toString(ModelUtil.charmDisplayDateFormat())}"
                    )
                    return true
                }

                if (binding.addMedicationMedConditionET.text.toString()
                        .trim() != patHistory.medicalConditionName ?: ""
                ) {
                    Log.d(
                        "MDAddMedication",
                        "medical condition ;view -> ${
                            binding.addMedicationMedConditionET.text.toString().trim()
                        } ; local -> ${patHistory.medicalConditionName}"
                    )
                    return true
                }

                if (binding.addMedicationMemberNameET.text.toString() != UIUtil.formMemberName(
                        addViewModel.medicationToBeEditted.member
                    )
                ) {
                    Log.d(
                        "MDAddMedication",
                        "member name ;view -> ${
                            binding.addMedicationMemberNameET.text.toString().trim()
                        } ; local -> ${UIUtil.formMemberName(addViewModel.medicationToBeEditted.member)}"
                    )
                    return true
                }

                if (binding.addMedicationRefillsET.text.toString()
                        .trim() != patHistory.refills ?: "" && !addViewModel.isHealthPassport
                ) {
                    Log.d(
                        "MDAddMedication",
                        "refills ;view -> ${
                            binding.addMedicationRefillsET.text.toString().trim()
                        } ; local -> ${patHistory.refills}"
                    )
                    return true
                }

                if (binding.addMedicationCommentsET.text.toString()
                        .trim() != patHistory.comments?.trim() ?: "" && !addViewModel.isHealthPassport
                ) {
                    Log.d(
                        "MDAddMedication",
                        "comments ;view -> ${
                            binding.addMedicationCommentsET.text.toString().trim()
                        } ; local -> ${patHistory.comments}"
                    )
                    return true
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
            DatePickerDialog.OnDateSetListener { _, yr, mnth, dt ->

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

    override fun getViewModel(): AddMedicationViewModel {
        return ViewModelProvider(this, viewModelFactory).get(AddMedicationViewModel::class.java)
    }
}
