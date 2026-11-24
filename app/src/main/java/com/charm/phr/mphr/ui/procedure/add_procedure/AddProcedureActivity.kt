package com.charm.phr.mphr.ui.procedure.add_procedure

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAddProcedureBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ProceduresList
import com.charm.phr.mphr.model.ProceduresListCommonData
import com.charm.phr.mphr.model.ProceduresMap
import com.charm.phr.mphr.util.MDAddBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.UIUtil
import com.google.gson.JsonObject
import org.joda.time.DateTime
import org.joda.time.LocalDate
import javax.inject.Inject

class AddProcedureActivity : MDAddBaseActivity<AddProcedureViewModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityAddProcedureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProcedureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UIUtil.applyViewInsertForToolbar(binding.addProcedureToolbar)
        UIUtil.applyViewInsertForBottomView(binding.addProcedureRootLayout)

        UIUtil.makeTextViewMandatory(binding.addProcedureNameLayout)

        addViewModel.toEdit = intent.getBooleanExtra(ModelUtil.procedureToEditIntent, false)

        addViewModel.isHealthPassport = intent.getBooleanExtra(ModelUtil.isHealthPassportIntent, false)

        if (addViewModel.toEdit) {

            val procedureStr = intent.getStringExtra(ModelUtil.procedureEditDataIntent) ?: ""

            if (addViewModel.isHealthPassport) {
                binding.addProcedureToolbarText.setText(R.string.procedure_health_passport_edit_str)
            } else {
                binding.addProcedureToolbarText.setText(R.string.procedure_edit_str)
            }
            binding.addProcedureBtn.setText(R.string.update_str)

            addViewModel.procedureToBeEditted = MDParsingHelper.formProcedureObj(
                    procedureStr.parseStringAsJsonObject()?: JsonObject()
            )

            binding.addProcedureNameET.inputType = InputType.TYPE_NULL
            binding.addProcedureTypeET.inputType = InputType.TYPE_NULL

            binding.addProcedureNameET.isEnabled = false
            binding.addProcedureTypeET.isEnabled = false

        } else {
            if (addViewModel.isHealthPassport) {
                binding.addProcedureToolbarText.setText(R.string.procedure_health_passport_add_str)
                binding.addProcedureBtn.setText(R.string.procedure_health_passport_add_str)
            } else {
                binding.addProcedureToolbarText.setText(R.string.procedure_add_str)
                binding.addProcedureBtn.setText(R.string.procedure_add_str)
            }

            binding.addProcedureTypeET.setOnItemClickListener { position, str ->

                if (str.equals(ModelUtil.othersValue, true)) {
                    binding.addProcedureOtherTypeLayout.visibility = View.VISIBLE
                } else {
                    if (binding.addProcedureOtherTypeLayout.visibility == View.VISIBLE) {
                        binding.addProcedureOtherTypeLayout.visibility = View.GONE
                    }
                }
            }
        }

        if (addViewModel.isHealthPassport) {
            binding.addProcedureNotesLayout.hint = getString(R.string.procedure_health_passport_notes_hint)
            binding.addProcedureNameLayout.hint = getString(R.string.procedure_health_passport_name_hint)
            binding.addProcedureDateLayout.hint = getString(R.string.procedure_health_passport_date_hint)

        } else {
            binding.addProcedureNotesLayout.hint = getString(R.string.procedure_notes_hint)
            binding.addProcedureNameLayout.hint = getString(R.string.procedure_name_hint)
            binding.addProcedureDateLayout.hint = getString(R.string.procedure_date_hint)

        }

        binding.addProcedureDateET.inputType = InputType.TYPE_NULL

        binding.addProcedureBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addProcedureDateET.setOnClickListener {
            showDatePicker()
        }

        binding.addProcedureDateET.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {
                showDatePicker()
            }
        }

        if (addViewModel.toEdit || addViewModel.isHealthPassport) {
            binding.addProcedurePracticeLayout.visibility = View.GONE
            getCommonData()

        } else {
            val practiceList = ModelUtil.getPHREntryEnabledPractices(authManager, ModelUtil.procedureModelId)
            if (practiceList.size == 1) {
                addViewModel.practiceId = practiceList.firstOrNull()?.practiceId ?: ""
                getCommonData()
                binding.addProcedurePracticeLayout.visibility = View.GONE
            } else {
                binding.addProcedureProgress.root.visibility = View.GONE
                binding.addProcedurePracticeLayout.visibility = View.VISIBLE
                binding.addProcedurePracticeET.setItems(practiceList.mapNotNull { it.practiceName },
                        this)
                binding.addProcedurePracticeET.setOnItemClickListener { position, str ->
                    addViewModel.practiceId = practiceList[position].practiceId ?: ""
                    getCommonData(true)
                }
            }
        }

        binding.addProcedureErrorPage.errorPageTryAgainBtn.setOnClickListener {
            getCommonData()
        }

        binding.addProcedureBtn.setOnClickListener {
            val procedure = validateData()

            if (procedure != null) {

                progressDialog.show()
                val responseLive = if (!addViewModel.toEdit) {
                    addViewModel.addProcedure(procedure)
                } else {
                    addViewModel.editProcedure(procedure)
                }

                responseLive.observe(this, Observer { resp ->

                    if (resp != null) {
                        progressDialog.dismiss()

                        if (resp is Boolean) {
                            addViewModel.isDataAdded = true
                            setResult(Activity.RESULT_OK)
                            super.onBackPressed()
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

                        } else if (resp is APIResponseError) {

                            if (!addViewModel.toEdit) {

                                if (resp.charmApiCode == ModelUtil.errorProcedureDataExistCode) {
                                    if (addViewModel.isHealthPassport) {
                                        UIUtil.showToast(this, R.string.error_add_procedure_health_passport_data_exists, Toast.LENGTH_LONG)
                                    } else {
                                        UIUtil.showToast(this, R.string.error_add_procedure_data_exists, Toast.LENGTH_LONG)
                                    }
                                } else {
                                    if (addViewModel.isHealthPassport) {
                                        UIUtil.showToast(this, R.string.error_add_procedure_health_passport_req_failed, Toast.LENGTH_LONG)
                                    } else {
                                        UIUtil.showToast(this, R.string.error_add_procedure_req_failed, Toast.LENGTH_LONG)
                                    }
                                }
                            } else {
                                if (addViewModel.isHealthPassport) {
                                    UIUtil.showToast(this, R.string.error_edit_procedure_health_passport_req_failed, Toast.LENGTH_LONG)
                                } else {
                                    UIUtil.showToast(this, R.string.error_edit_procedure_req_failed, Toast.LENGTH_LONG)
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    private fun getCommonData(showDialog: Boolean = false) {

        if (!addViewModel.toEdit) {
            if (showDialog) {
                progressDialog.show()
            } else {
                binding.addProcedureProgress.root.visibility = View.VISIBLE
                binding.addProcedureMainLayout.visibility = View.GONE
                binding.addProcedureErrorPage.root.visibility = View.GONE
            }

            addViewModel.getProcedureCommonData().observe(this, Observer { resp ->

                if (resp != null) {
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    binding.addProcedureProgress.root.visibility = View.GONE
                    if (resp is ProceduresListCommonData) {
                        binding.addProcedureMainLayout.visibility = View.VISIBLE
                        binding.addProcedureDateET.setText(DateTime.now().toString(ModelUtil.charmDisplayDateFormat()))
                        binding.addProcedureTypeET.setItems(resp.procedureTypeList, this)
                        binding.addProcedureTypeET.setAnchorView(binding.addProcedureTypeLayout)

                    } else if (resp is APIResponseError) {
                        binding.addProcedureErrorPage.root.visibility = View.VISIBLE
                    }
                }
            })
        } else {

            binding.addProcedurePracticeLayout.visibility = View.GONE
            binding.addProcedureProgress.root.visibility = View.GONE
            binding.addProcedureMainLayout.visibility = View.VISIBLE
            binding.addProcedureErrorPage.root.visibility = View.GONE

            val procedure = addViewModel.procedureToBeEditted

            binding.addProcedureNameET.setText(procedure.procedure?.procedure)

            binding.addProcedureTypeET.setText(procedure.procedure?.procedureType)
            binding.addProcedureDateET.setText(
                    procedure.fromDate?.toString(
                            ModelUtil.charmDisplayDateFormat()
                    )
            )
            binding.addProcedureNotesET.setText(procedure.notes)
        }
    }

    private fun validateData(): ProceduresMap? {

        val procedureName = binding.addProcedureNameET.text.toString()

        val procedureNote = binding.addProcedureNotesET.text.toString()

        val procedureNameErrorCode =  if (addViewModel.isHealthPassport) {
           R.string.error_empty_procedure_health_passport_name
        } else {
            R.string.error_empty_procedure_name
        }

        if (isTextInputLayoutBlank(binding.addProcedureNameLayout, binding.addProcedureScrollView, procedureNameErrorCode)) {
            return null
        }

        if (!addViewModel.toEdit) {
            val procedureNameInvalidErrorCode = if (addViewModel.isHealthPassport) {
                R.string.error_invalid_procedure_health_passport_name
            } else {
                R.string.error_invalid_procedure_name
            }
            if (isTextInputLayoutBlank(binding.addProcedureNameLayout, binding.addProcedureScrollView, procedureNameInvalidErrorCode, true)) {
                return null
            }
        }

        val procedureCommentInvalidErrorCode = if (addViewModel.isHealthPassport) {
            R.string.error_invalid_outcome
        } else {
            R.string.error_invalid_notes
        }

        if (isTextInputLayoutBlank(binding.addProcedureNotesLayout, binding.addProcedureScrollView, procedureCommentInvalidErrorCode, true)) {
            return null
        }

        val procedureMap = if (addViewModel.toEdit) {
            addViewModel.procedureToBeEditted
        } else {
            ProceduresMap()
        }

        if (!addViewModel.toEdit) {

            val procedure = ProceduresList()
            procedure.procedure = procedureName
            procedure.procedureType = binding.addProcedureTypeET.text.toString()

            if (binding.addProcedureOtherTypeLayout.visibility == View.VISIBLE) {
                procedure.otherType = binding.addProcedureOtherTypeET.text.toString()
            }

            procedureMap.procedure = procedure
        }

        procedureMap.fromDate = DateTime.parse(binding.addProcedureDateET.text.toString(), ModelUtil.charmDisplayDateFormat())
        procedureMap.notes = procedureNote.trim()
        procedureMap.isHealthPassport = addViewModel.isHealthPassport

        return procedureMap
    }

    override fun checkDataIsAvailable(): Boolean {

        if (binding.addProcedureTypeET.items != null && !binding.addProcedureTypeET.items.isNullOrEmpty()) {

            if (binding.addProcedureNameET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addProcedureTypeET.text.toString() != binding.addProcedureTypeET.items[0]) {
                return true
            }

            if (binding.addProcedureDateET.text.toString() != DateTime.now().toString(ModelUtil.charmDisplayDateFormat())) {
                return true
            }

            if (binding.addProcedureNotesET.text.toString().isNotBlank()) {
                return true
            }
        }
        return false
    }

    override fun checkDataIsEditted(): Boolean {

        if (binding.addProcedureNameET.text.toString().isNotBlank()) {

            val procedure = addViewModel.procedureToBeEditted

            if (binding.addProcedureDateET.text.toString() != procedure.fromDate?.toString(ModelUtil.charmDisplayDateFormat())) {
                return true
            }

            if (binding.addProcedureNotesET.text.toString().trim() != procedure.notes?.trim() ?: "") {
                return true
            }
        }

        return false
    }

    private fun showDatePicker() {

        val value = binding.addProcedureDateET.text.toString().trim()

        val localDate = if (value.isNotBlank()) {
            LocalDate.parse(value, ModelUtil.charmDisplayDateFormat())
        } else {
            LocalDate.now()
        }

        val dialog = DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { _, yr, mnth, dt ->

                    val text = LocalDate(yr, mnth + 1, dt)
                binding.addProcedureDateET.setText(text.toString(ModelUtil.charmDisplayDateFormat()))

                },
                localDate.year, localDate.monthOfYear - 1, localDate.dayOfMonth)

        dialog.datePicker.minDate = LocalDate(LocalDate.now().year - 150, 1, 1).toDateTimeAtStartOfDay().millis
        dialog.datePicker.maxDate = System.currentTimeMillis()

        dialog.show()
    }

    override fun getViewModel(): AddProcedureViewModel {
        return ViewModelProvider(this, viewModelFactory).get(AddProcedureViewModel::class.java)
    }
}
