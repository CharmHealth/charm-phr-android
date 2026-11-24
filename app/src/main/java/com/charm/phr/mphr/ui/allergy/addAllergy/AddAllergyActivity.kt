package com.charm.phr.mphr.ui.allergy.addAllergy


import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAddAllergyBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Allergy
import com.charm.phr.mphr.model.PatientAllergy
import com.charm.phr.mphr.model.PatientAllergyCommonData
import com.charm.phr.mphr.ui.components.MDTextInputEdittext
import com.charm.phr.mphr.util.MDAddBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.UIUtil
import com.google.gson.JsonObject
import org.joda.time.DateTime
import org.joda.time.LocalDate
import javax.inject.Inject

class AddAllergyActivity : MDAddBaseActivity<AddAllergyViewModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityAddAllergyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddAllergyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UIUtil.applyViewInsertForToolbar(binding.addAllergyToolbar)
        UIUtil.applyViewInsertForBottomView(binding.addAllergyRootLayout)

        UIUtil.makeTextViewMandatory(binding.addAllergyNameLayout)
        UIUtil.makeTextViewMandatory(binding.addAllergyObservedOnLayout)

        addViewModel.toEdit = intent.getBooleanExtra(ModelUtil.allergyToEditIntent, false)
        addViewModel.isHealthPassport = intent.getBooleanExtra(ModelUtil.isHealthPassportIntent, false)

        if (addViewModel.toEdit) {

            val patientAllergyStr = intent.getStringExtra(ModelUtil.allergyEditDataIntent) ?: ""

            val allergyStatus = intent.getStringExtra(ModelUtil.allergyStatusIntent) ?: ""

            addViewModel.practiceId = intent.getStringExtra(ModelUtil.practiceIdIntent) ?: ""

            binding.addAllergyToolbarText.setText(R.string.allergy_edit_toolbar_str)
            binding.addAllergyBtn.setText(R.string.update_str)

            addViewModel.patientAllergyToBeEditted =
                MDParsingHelper.formPatientAllergyObj(patientAllergyStr.parseStringAsJsonObject() ?: JsonObject(), allergyStatus)

            binding.addAllergyNameET.inputType = InputType.TYPE_NULL
            binding.addAllergyTypeET.inputType = InputType.TYPE_NULL

            binding.addAllergyNameET.isEnabled = false
            binding.addAllergyTypeET.isEnabled = false

            if (addViewModel.patientAllergyToBeEditted.status == ModelUtil.allergyStatusResolved) {
                binding.addAllergyStatusET.isEnabled = false
            }

        }


        binding.addAllergyReactionsET.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.addAllergyScrollLayout.post {
                    binding.addAllergyScrollLayout.fullScroll(View.FOCUS_DOWN)
                }
            }
        }

        binding.addAllergyObservedOnET.inputType = InputType.TYPE_NULL
        binding.addAllergyResolvedOnET.inputType = InputType.TYPE_NULL

        binding.addAllergyBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addAllergyObservedOnET.setOnClickListener {
            showDatePicker(binding.addAllergyObservedOnET)
        }

        binding.addAllergyResolvedOnET.setOnClickListener {
            showDatePicker(binding.addAllergyResolvedOnET)
        }

        binding.addAllergyStatusET.setOnItemClickListener { position, str ->
            if (binding.addAllergyStatusET.text.toString() == ModelUtil.allergyStatusResolved) {
                binding.addAllergyResolvedOnLayout.visibility = View.VISIBLE
            } else {
                binding.addAllergyResolvedOnLayout.visibility = View.GONE
            }
        }

        if (!addViewModel.toEdit) {

            binding.addAllergyTypeET.setOnItemClickListener { position, str ->
                if (str.equals(ModelUtil.othersValue, true)) {
                    binding.addAllergyOtherTypeLayout.visibility = View.VISIBLE
                } else {
                    if (binding.addAllergyOtherTypeLayout.isVisible) {
                        binding.addAllergyOtherTypeLayout.visibility = View.GONE
                    }
                }
            }
        }

        binding.addAllergyObservedOnET.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {
                showDatePicker(binding.addAllergyObservedOnET)
            }
        }

        binding.addAllergyResolvedOnET.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {
                showDatePicker(binding.addAllergyResolvedOnET)
            }
        }



        if (addViewModel.toEdit || addViewModel.isHealthPassport) {
            getCommonData()
            binding.addAllergyPracticeLayout.visibility = View.GONE
        } else {
            val practiceList = ModelUtil.getPHREntryEnabledPractices(authManager, ModelUtil.allergiesModelId)

            if (practiceList.size == 1) {
                addViewModel.practiceId = practiceList.firstOrNull()?.practiceId ?: ""
                getCommonData()
                binding.addAllergyPracticeLayout.visibility = View.GONE
            } else {
                binding.addAllergyProgress.root.visibility = View.GONE
                binding.addAllergyPracticeLayout.visibility = View.VISIBLE
                binding.addAllergyPracticeET.setItems(
                    practiceList.mapNotNull { it.practiceName }, this
                )
                binding.addAllergyPracticeET.setOnItemClickListener { position, str ->
                    addViewModel.practiceId = practiceList[position].practiceId ?: ""
                    getCommonData(true)
                }
            }
        }

        binding.addAllergyErrorPage.errorPageTryAgainBtn.setOnClickListener {
            getCommonData()
        }

        binding.addAllergyBtn.setOnClickListener {
            val patientAllergy = validateData()

            if (patientAllergy != null) {

                progressDialog.show()

                val responseLive = if (!addViewModel.toEdit) {
                    addViewModel.addPatientAllergy(patientAllergy)
                } else {
                    addViewModel.editPatientAllergy(patientAllergy)
                }

                responseLive.observe(this, Observer { resp ->

                    if (resp != null) {
                        progressDialog.dismiss()

                        if (resp is Boolean) {
                            addViewModel.isDataAdded = true
                            setResult(RESULT_OK)
                            super.onBackPressed()
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

                        } else if (resp is APIResponseError) {

                            if (resp.charmApiCode == 5007) {
                                UIUtil.showToast(this, R.string.error_allergen_already_exist, Toast.LENGTH_LONG)
                                return@Observer
                            }

                            if (!addViewModel.toEdit) {
                                UIUtil.showToast(this, R.string.error_add_allergy_req_failed, Toast.LENGTH_LONG)
                            } else {
                                UIUtil.showToast(this, R.string.error_edit_allergy_req_failed, Toast.LENGTH_LONG)
                            }
                        }
                    }
                })

            }
        }

    }

    private fun getCommonData(showProgressDialog: Boolean = false) {

        if (showProgressDialog) {
            progressDialog.show()
        } else {

            binding.addAllergyProgress.root.visibility = View.VISIBLE
            binding.addAllergyMainLayout.visibility = View.GONE
            binding.addAllergyErrorPage.root.visibility = View.GONE
        }

        addViewModel.getPatAllergyCommonData().observe(this, Observer { resp ->

            if (resp != null) {
                if (showProgressDialog) {
                    progressDialog.dismiss()
                }
                binding.addAllergyProgress.root.visibility = View.GONE
                if (resp is PatientAllergyCommonData) {
                    binding.addAllergyMainLayout.visibility = View.VISIBLE

                    binding.addAllergySeverityET.setItems(resp.allergySeverityList, this)
                    binding.addAllergyStatusET.setItems(resp.allergyStatusList, this)

                    binding.addAllergySeverityET.setAnchorView(binding.addAllergySeverityLayout)
                    binding.addAllergyStatusET.setAnchorView(binding.addAllergyStatusLayout)

                    binding.addAllergyObservedOnET.setText(DateTime.now().toString(ModelUtil.charmDisplayDateFormat()))
                    binding.addAllergyResolvedOnET.setText(DateTime.now().toString(ModelUtil.charmDisplayDateFormat()))

                    if (addViewModel.toEdit) {

                        if (!addViewModel.isFilled) {
                            addViewModel.isFilled = true
                            val patientAllergy = addViewModel.patientAllergyToBeEditted

                            //  addAllergyNameET.updateFilter(MDTextInputEdittext.ALPHABETS_NUMBERS_SPLCHARS)
                            binding.addAllergyNameET.setText(patientAllergy.allergy?.allergy)
                            binding.addAllergySeverityET.setText(patientAllergy.severity)
                            binding.addAllergyStatusET.setText(patientAllergy.status)
                            if (patientAllergy.status == ModelUtil.allergyStatusResolved) {
                                binding.addAllergyResolvedOnLayout.visibility = View.VISIBLE
                                binding.addAllergyResolvedOnET.setText(patientAllergy.resolvedDate?.toString(ModelUtil.charmDisplayDateFormat()))
                            }
                            binding.addAllergyTypeET.setText(patientAllergy.allergy?.allergyType)
                            binding.addAllergyObservedOnET.setText(patientAllergy.observedDate?.toString(ModelUtil.charmDisplayDateFormat()))
                            binding.addAllergyReactionsET.setText(patientAllergy.reactions)
                        }

                    } else {
                        binding.addAllergyTypeET.setItems(resp.allergyTypeList, this)

                        binding.addAllergyTypeET.setAnchorView(binding.addAllergyTypeLayout)

                    }

                } else if (resp is APIResponseError) {
                    binding.addAllergyErrorPage.root.visibility = View.VISIBLE
                }
            }
        })

    }

    private fun validateData(): PatientAllergy? {

        val allergyName = binding.addAllergyNameET.text.toString()

        val reactions = binding.addAllergyReactionsET.text.toString()

        if (isTextInputLayoutBlank(binding.addAllergyNameLayout, binding.addAllergyScrollLayout, R.string.error_empty_allergen_name)) {
            return null
        }

        if (!addViewModel.toEdit) {
            if (isTextInputLayoutBlank(binding.addAllergyNameLayout, binding.addAllergyScrollLayout, R.string.error_invalid_allergen_name, true)) {
                return null
            }
        }

        if (isTextInputLayoutBlank(binding.addAllergyReactionsLayout, binding.addAllergyScrollLayout, R.string.error_invalid_reaction_name, true)) {
            return null
        }

        val patientAllergy = if (addViewModel.toEdit) {
            addViewModel.patientAllergyToBeEditted
        } else {
            PatientAllergy()
        }


        if (!addViewModel.toEdit) {

            val allergy = Allergy()
            allergy.allergy = allergyName
            allergy.allergyType = binding.addAllergyTypeET.text.toString()

            if (binding.addAllergyOtherTypeLayout.isVisible) {
                allergy.allergyOtherType = binding.addAllergyOtherTypeET.text.toString()
            }
            patientAllergy.allergy = allergy
        }

        patientAllergy.observedDate = DateTime.parse(binding.addAllergyObservedOnET.text.toString(), ModelUtil.charmDisplayDateFormat())
        patientAllergy.status = binding.addAllergyStatusET.text.toString()
        if (patientAllergy.status == ModelUtil.allergyStatusResolved) {
            patientAllergy.resolvedDate = DateTime.parse(binding.addAllergyResolvedOnET.text.toString(), ModelUtil.charmDisplayDateFormat())
            if (patientAllergy.resolvedDate?.isBefore(patientAllergy.observedDate) == true) {
                UIUtil.showToast(this, R.string.error_invalid_date_period, Toast.LENGTH_LONG)
                return null
            }
        }
        patientAllergy.severity = binding.addAllergySeverityET.text.toString()
        patientAllergy.reactions = reactions

        patientAllergy.isHealthPassport = addViewModel.isHealthPassport

        return patientAllergy
    }

    private fun showDatePicker(editText: MDTextInputEdittext) {

        val value = editText.text.toString().trim()

        val localDate = if (value.isNotBlank()) {
            LocalDate.parse(value, ModelUtil.charmDisplayDateFormat())
        } else {
            LocalDate.now()
        }

        val dialog = DatePickerDialog(
            this, DatePickerDialog.OnDateSetListener { _, yr, mnth, dt ->

                val text = LocalDate(yr, mnth + 1, dt)
                editText.setText(text.toString(ModelUtil.charmDisplayDateFormat()))

            }, localDate.year, localDate.monthOfYear - 1, localDate.dayOfMonth
        )

        dialog.datePicker.minDate = LocalDate(LocalDate.now().year - 150, 1, 1).toDateTimeAtStartOfDay().millis
        dialog.datePicker.maxDate = System.currentTimeMillis()

        dialog.show()
    }

    override fun getViewModel(): AddAllergyViewModel {
        return ViewModelProvider(this, viewModelFactory)[AddAllergyViewModel::class.java]
    }

    override fun checkDataIsAvailable(): Boolean {

        if (binding.addAllergyStatusET.items != null && binding.addAllergyStatusET.items.isNotEmpty()) {

            if (binding.addAllergyNameET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addAllergyTypeET.text.toString() != binding.addAllergyTypeET.items[0]) {
                return true
            }

            if (binding.addAllergyOtherTypeLayout.isVisible && binding.addAllergyOtherTypeET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addAllergyStatusET.text.toString() != binding.addAllergyStatusET.items[0]) {
                return true
            }

            if (binding.addAllergySeverityET.text.toString() != binding.addAllergySeverityET.items[0]) {
                return true
            }

            if (binding.addAllergyObservedOnET.text.toString() != DateTime.now().toString(ModelUtil.charmDisplayDateFormat())) {
                return true
            }

            if (binding.addAllergyReactionsET.text.toString().isNotBlank()) {
                return true
            }
        }
        return false
    }

    override fun checkDataIsEditted(): Boolean {

        if (binding.addAllergyNameET.text.toString().isNotBlank()) {

            val patAllergy = addViewModel.patientAllergyToBeEditted

            if (binding.addAllergyStatusET.text.toString() != (patAllergy.status?.trim() ?: "")) {
                return true
            }

            if (binding.addAllergySeverityET.text.toString() != (patAllergy.severity?.trim() ?: "")) {
                return true
            }

            if (binding.addAllergyObservedOnET.text.toString() != patAllergy.observedDate?.toString(ModelUtil.charmDisplayDateFormat())) {
                return true
            }

            if (binding.addAllergyReactionsET.text.toString().trim() != (patAllergy.reactions?.trim() ?: "")) {
                return true
            }
        }

        return false
    }

}
