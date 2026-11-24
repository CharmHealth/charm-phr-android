package com.charm.phr.mphr.ui.problem_list.add_problem_list

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAddProblemListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.MedicalCondition
import com.charm.phr.mphr.model.PatientMedicalCondition
import com.charm.phr.mphr.ui.components.MDAutoCompleteTextViewAdapter
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.UIUtil
import com.google.gson.JsonObject
import org.joda.time.DateTime
import org.joda.time.LocalDate
import javax.inject.Inject

class AddProblemListActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var addProblemListViewModel: AddProblemListViewModel

    private lateinit var backPressAlertDialogBuilder: AlertDialog.Builder

    lateinit var problemNameAdapter: MDAutoCompleteTextViewAdapter

    lateinit var binding: ActivityAddProblemListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProblemListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addProblemListViewModel = ViewModelProvider(this, viewModelFactory).get(AddProblemListViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.addProblemListToolbar)
        UIUtil.applyViewInsertForBottomView(binding.addProblemListRootLayout)

        addProblemListViewModel.toEdit = intent.getBooleanExtra(ModelUtil.problemListToEditIntent, false)

        addProblemListViewModel.isHealthPassport = intent.getBooleanExtra(ModelUtil.isHealthPassportIntent, false)



        if (addProblemListViewModel.toEdit) {

            val problemListStr = intent.getStringExtra(ModelUtil.problemListEditDataIntent) ?: ""

            if (addProblemListViewModel.isHealthPassport) {
                binding.addProblemListToolbarText.setText(R.string.problem_list_health_passport_edit_str)
            } else {
                binding.addProblemListToolbarText.setText(R.string.problem_list_edit_str)
            }
            binding.addProblemListBtn.setText(R.string.update_str)

            addProblemListViewModel.problemListToBeEditted = MDParsingHelper.formMedicalConditionObj(
                problemListStr.parseStringAsJsonObject() ?: JsonObject()
            )

            binding.addProblemListNameET.inputType = InputType.TYPE_NULL
            binding.addProblemListNameET.isEnabled = false

        } else {
            problemNameAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())
            binding.addProblemListNameET.setAdapter(problemNameAdapter)

            if (addProblemListViewModel.isHealthPassport) {
                binding.addProblemListToolbarText.setText(R.string.problem_list_health_passport_add_str)
            } else {
                binding.addProblemListToolbarText.setText(R.string.problem_list_add_str)
            }
        }

        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    super.onBackPressed()
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                }

                DialogInterface.BUTTON_NEGATIVE -> {

                }
            }
        }

        backPressAlertDialogBuilder = AlertDialog.Builder(this)
        backPressAlertDialogBuilder.setMessage(R.string.discard_changes_confirmation_string).setPositiveButton(R.string.yes_str, dialogClickListener)
            .setNegativeButton(R.string.no_str, dialogClickListener)


        binding.addProblemListDateET.inputType = InputType.TYPE_NULL

        binding.addProblemListBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addProblemListDateET.setOnClickListener {
            showDatePicker()
        }

        binding.addProblemListDateET.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {
                showDatePicker()
            }
        }

        if (addProblemListViewModel.isHealthPassport) {
            binding.addProblemListTypeET.setItems(arrayListOf(ModelUtil.problemListStatusActive, ModelUtil.problemListStatusInActive), this)
        } else {
            binding.addProblemListTypeET.setItems(
                arrayListOf(
                    ModelUtil.problemListStatusActive,
                    ModelUtil.problemListStatusInActive,
                    ModelUtil.problemListStatusResolved
                ), this
            )
        }
        binding.addProblemListTypeET.setAnchorView(binding.addProblemListTypeLayout)


        if (addProblemListViewModel.isHealthPassport) {
            binding.addProblemListNotesLayout.visibility = View.GONE
        } else {
            binding.addProblemListNotesLayout.visibility = View.VISIBLE
        }


        if (addProblemListViewModel.toEdit || addProblemListViewModel.isHealthPassport) {
            binding.addProblemListPracticeLayout.visibility = View.GONE
            getCommonData()
        } else {
            val selectedPatient = authManager.selectedPatientLive.value

            if (selectedPatient != null) {

                val practiceList = selectedPatient.practiceList.filter { it.isActive }

                if (practiceList.size == 1) {
                    addProblemListViewModel.practiceId = practiceList.firstOrNull()?.practiceId ?: ""
                    getCommonData()
                    binding.addProblemListPracticeLayout.visibility = View.GONE
                } else {

                    binding.addProblemListProgress.root.visibility = View.GONE
                    binding.addProblemListPracticeLayout.visibility = View.VISIBLE
                    binding.addProblemListPracticeET.setItems(practiceList.mapNotNull { it.practiceName }, this)
                    binding.addProblemListPracticeET.setAnchorView(binding.addProblemListPracticeLayout)
                    binding.addProblemListPracticeET.setOnItemClickListener { position, str ->
                        addProblemListViewModel.practiceId =
                            selectedPatient.practiceList[position].practiceId ?: ""
                        getCommonData(true)
                    }
                }

            }
        }



        binding.addProblemListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            getCommonData()
        }


        binding.addProblemListBtn.setOnClickListener {
            val procedure = validateData()

            if (procedure != null) {

                progressDialog.show()

                addProblemListViewModel.saveMedCondition(procedure).observe(this, Observer { resp ->

                    if (resp != null) {
                        progressDialog.dismiss()

                        if (resp is Boolean) {
                            setResult(Activity.RESULT_OK)
                            super.onBackPressed()
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

                        } else if (resp is APIResponseError) {

                            if (!addProblemListViewModel.toEdit) {
                                if (addProblemListViewModel.isHealthPassport) {
                                    UIUtil.showToast(this, R.string.error_add_problem_list_health_passport_req_failed, Toast.LENGTH_LONG)
                                } else {
                                    UIUtil.showToast(this, R.string.error_add_problem_list_req_failed, Toast.LENGTH_LONG)

                                }
                            } else {
                                if (addProblemListViewModel.isHealthPassport) {
                                    UIUtil.showToast(this, R.string.error_edit_problem_list_health_passport_req_failed, Toast.LENGTH_LONG)
                                } else {
                                    UIUtil.showToast(this, R.string.error_edit_problem_list_req_failed, Toast.LENGTH_LONG)
                                }
                            }
                        }
                    }
                })
            }
        }
    }

    private fun getCommonData(showDialog: Boolean = false) {

        if (!addProblemListViewModel.toEdit) {
            if (showDialog) {
                progressDialog.show()
            } else {
                binding.addProblemListProgress.root.visibility = View.VISIBLE
                binding.addProblemListMainLayout.visibility = View.GONE
                binding.addProblemListErrorPage.root.visibility = View.GONE
            }

            addProblemListViewModel.getMedConditionCommonData().observe(this, Observer { resp ->

                if (resp != null) {
                    if (progressDialog.isShowing) {
                        progressDialog.dismiss()
                    }
                    binding.addProblemListProgress.root.visibility = View.GONE
                    if (resp is ArrayList<*>) {
                        binding.addProblemListMainLayout.visibility = View.VISIBLE
                        binding.addProblemListDateET.setText(DateTime.now().toString(ModelUtil.charmDisplayDateFormat()))
                        addProblemListViewModel.medicalConditionList = resp as ArrayList<MedicalCondition>
                        problemNameAdapter.setItemList(ArrayList(addProblemListViewModel.medicalConditionList.mapNotNull { it.medicalConditionName }))
                    } else if (resp is APIResponseError) {
                        binding.addProblemListErrorPage.root.visibility = View.VISIBLE
                    }
                }
            })
        } else {

            binding.addProblemListPracticeLayout.visibility = View.GONE
            binding.addProblemListProgress.root.visibility = View.GONE
            binding.addProblemListMainLayout.visibility = View.VISIBLE
            binding.addProblemListErrorPage.root.visibility = View.GONE

            val problemList = addProblemListViewModel.problemListToBeEditted

            binding.addProblemListNameET.setText(problemList.medicalCondition?.medicalConditionName)

            binding.addProblemListTypeET.setText(problemList.status)
            binding.addProblemListDateET.setText(
                problemList.dateOfOnset?.toString(
                    ModelUtil.charmDisplayDateFormat()
                )
            )
            binding.addProblemListNotesET.setText(problemList.notes)
        }


    }

    override fun onBackPressed() {

        if (addProblemListViewModel.toEdit) {
            if (checkDataIsEditted()) {
                backPressAlertDialogBuilder.show()
            } else {
                super.onBackPressed()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        } else {

            if (checkDataIsAvailable()) {
                backPressAlertDialogBuilder.show()
            } else {
                super.onBackPressed()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }


    private fun validateData(): PatientMedicalCondition? {

        val problemName = binding.addProblemListNameET.text.toString()

        val procedureNote = binding.addProblemListNotesET.text.toString()

        if (problemName.isBlank()) {
            UIUtil.showToast(this, (R.string.error_empty_problem_List_name), Toast.LENGTH_LONG)
            return null
        }

        if (!ModelUtil.hasAlpha(problemName) && !addProblemListViewModel.toEdit) {
            UIUtil.showToast(this, (R.string.error_invalid_problem_list_name), Toast.LENGTH_LONG)
            return null
        }

        if (!ModelUtil.hasAlpha(procedureNote) && !addProblemListViewModel.isHealthPassport) {
            UIUtil.showToast(this, (R.string.error_invalid_notes), Toast.LENGTH_LONG)
            return null
        }

        val patMedicalCondition = if (addProblemListViewModel.toEdit) {
            addProblemListViewModel.problemListToBeEditted
        } else {
            PatientMedicalCondition()
        }

        if (!addProblemListViewModel.toEdit) {

            for (medCondition in addProblemListViewModel.medicalConditionList) {

                if (medCondition.medicalConditionName?.trim().equals(problemName.trim(), true)) {
                    patMedicalCondition.medicalCondition = medCondition
                    break
                }
            }
            if (patMedicalCondition.medicalCondition == null) {
                patMedicalCondition.medicalCondition = MedicalCondition(medicalConditionName = problemName)
            }
        }
        patMedicalCondition.status = binding.addProblemListTypeET.text.toString().trim()


        patMedicalCondition.dateOfOnset = DateTime.parse(binding.addProblemListDateET.text.toString(), ModelUtil.charmDisplayDateFormat())
        patMedicalCondition.notes = procedureNote
        patMedicalCondition.isHealthPassport = addProblemListViewModel.isHealthPassport

        return patMedicalCondition
    }

    private fun checkDataIsAvailable(): Boolean {


        if (!binding.addProblemListTypeET.items.isNullOrEmpty()) {

            if (binding.addProblemListNameET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addProblemListTypeET.text.toString() != binding.addProblemListTypeET.items[0]) {
                return true
            }

            if (binding.addProblemListDateET.text.toString() != DateTime.now().toString(ModelUtil.charmDisplayDateFormat())) {
                return true
            }

            if (binding.addProblemListNotesET.text.toString().isNotBlank() && !addProblemListViewModel.isHealthPassport) {
                return true
            }
        }
        return false
    }

    private fun checkDataIsEditted(): Boolean {


        if (binding.addProblemListNameET.text.toString().isNotBlank()) {

            val procedure = addProblemListViewModel.problemListToBeEditted

            if (binding.addProblemListTypeET.text.toString().trim() != procedure.status?.trim() ?: "") {
                return true
            }

            if (binding.addProblemListDateET.text.toString() != procedure.dateOfOnset?.toString(ModelUtil.charmDisplayDateFormat())) {
                return true
            }

            if (binding.addProblemListNotesET.text.toString().trim() != procedure.notes?.trim() ?: "" && !addProblemListViewModel.isHealthPassport) {
                return true
            }
        }

        return false
    }


    private fun showDatePicker() {

        val value = binding.addProblemListDateET.text.toString().trim()

        val localDate = if (value.isNotBlank()) {
            LocalDate.parse(value, ModelUtil.charmDisplayDateFormat())
        } else {
            LocalDate.now()
        }

        val dialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, yr, mnth, dt ->

                val text = LocalDate(yr, mnth + 1, dt)
                binding.addProblemListDateET.setText(text.toString(ModelUtil.charmDisplayDateFormat()))

            },
            localDate.year, localDate.monthOfYear - 1, localDate.dayOfMonth
        )

        dialog.datePicker.minDate = LocalDate(LocalDate.now().year - 150, 1, 1).toDateTimeAtStartOfDay().millis
        dialog.datePicker.maxDate = System.currentTimeMillis()

        dialog.show()
    }
}
