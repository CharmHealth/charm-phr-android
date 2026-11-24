package com.charm.phr.mphr.ui.insurance.add

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAddInsuranceBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ContactDetails
import com.charm.phr.mphr.model.InsuranceCommonData
import com.charm.phr.mphr.model.InsuredAndGuarantorDetails
import com.charm.phr.mphr.model.InsuredEmployerDetails
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientInsuranceDetails
import com.charm.phr.mphr.model.PostalAddress
import com.charm.phr.mphr.model.PracticePayer
import com.charm.phr.mphr.ui.components.Decimal2Filter
import com.charm.phr.mphr.ui.components.DecimalFilter
import com.charm.phr.mphr.ui.components.MDAutoCompleteTextViewAdapter
import com.charm.phr.mphr.ui.components.MDTextInputAutoCompleteTextView
import com.charm.phr.mphr.ui.components.MDTextInputEdittext
import com.charm.phr.mphr.ui.components.MDTextInputLayout
import com.charm.phr.mphr.util.MDAddBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.UIUtil
import com.google.gson.JsonObject
import org.joda.time.DateTime
import org.joda.time.LocalDate
import javax.inject.Inject

class AddInsuranceActivity : MDAddBaseActivity<AddInsuranceViewModel>() {

    @Inject
    lateinit var viewModelProvider: ViewModelProvider.Factory

    lateinit var insuranceNameAdapter: MDAutoCompleteTextViewAdapter


    private lateinit var countryAdapter: MDAutoCompleteTextViewAdapter
    private lateinit var stateAdapter: MDAutoCompleteTextViewAdapter

    private lateinit var payerCountryAdapter: MDAutoCompleteTextViewAdapter
    private lateinit var payerStateAdapter: MDAutoCompleteTextViewAdapter

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {

        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            insuranceNameAdapter.setItemList(arrayListOf())

            performSearch(inputFinishChecker, false)
        }
    }

    val inputFinishChecker = Runnable {
        // to send request only there is a definite time difference between typing
        val newText = binding.insuranceRoot.addInsurancePayerNameET?.text?.toString()
        if (newText.isNullOrBlank()) {
            insuranceNameAdapter.setItemList(arrayListOf())
        } else {
            addViewModel.getPayerList(newText)
        }
    }

    lateinit var binding: ActivityAddInsuranceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddInsuranceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UIUtil.applyViewInsertForToolbar(binding.addInsuranceToolbar)
        UIUtil.applyViewInsertForBottomView(binding.addInsuranceRootLayout)

        stateAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())
        countryAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())

        payerStateAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())
        payerCountryAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())

        binding.insuranceRoot.addInsuranceHasInsuranceLayout.visibility = View.GONE

        UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsurancePlanLayout)
        UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsuranceMemberIdLayout)
        UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsurancePayerNameLayout)
        UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsuranceRelationshipLayout)
        UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsuranceFirstNameLayout)
        UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsuranceLastNameLayout)
        UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsuranceDOBLayout)
        UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsuranceTypeLayout)
        UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsuranceTypeUSLayout)
        UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsuranceInsuranceTypeLabel)
        UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsuranceGenderLayout)
        UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsuranceStartDateLayout)
        UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsuranceEndDateLayout)
        // UIUtil.makeTextViewMandatory(addInsurancePracticeLayout)


        binding.insuranceRoot.addInsuranceStateET.setAdapter(stateAdapter)
        binding.insuranceRoot.addInsuranceCountryET.setAdapter(countryAdapter)

        binding.insuranceRoot.addInsurancePayerStateET.setAdapter(payerStateAdapter)
        binding.insuranceRoot.addInsurancePayerCountryET.setAdapter(payerCountryAdapter)

        addViewModel.toEdit = intent.getBooleanExtra(ModelUtil.insuranceToEditIntent, false)
//Note:Insurance module in phr is not associated with practice as of now so always get the first practice from the list for add and edit
        //practice id is mandatory for fetching payer list and adding payer
        addViewModel.practiceId =
            ModelUtil.getPHREntryEnabledPractices(authManager, ModelUtil.insuranceModelId)
                .firstOrNull()?.practiceId
                ?: ""

        getCommonData()


        if (!addViewModel.toEdit) {

//            val currentDate = DateTime.now()
//            addInsuranceStartDateET.setText(currentDate.toString(ModelUtil.charmDisplayDateFormat()))
//            addInsuranceEndDateET.setText(currentDate.toString(ModelUtil.charmDisplayDateFormat()))

            binding.insuranceRoot.addInsurancePayerNameET.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ -> onPayerChange(position) }

            addViewModel.payerListLive.observe(this) { resp ->
                if (resp != null) {
                    if (resp is ArrayList<*>) {

                        addViewModel.payerList.clear()
                        addViewModel.payerList.addAll(resp as ArrayList<PracticePayer>)

                        insuranceNameAdapter = MDAutoCompleteTextViewAdapter(
                            this,
                            ArrayList(addViewModel.payerList.mapNotNull { it.payerName })
                        )
                        insuranceNameAdapter.filter.filter(binding.insuranceRoot.addInsurancePayerNameET.text.toString())
                        binding.insuranceRoot.addInsurancePayerNameET.setAdapter(insuranceNameAdapter)
                    } else if (resp is APIResponseError) {

                    }
                }
            }

        } else {

            binding.addInsuranceBtn.setText(R.string.update_str)
            binding.addInsuranceToolbarText.setText(R.string.insurance_edit_str)

            addViewModel.insuranceToBeEditted =
                MDParsingHelper.formInsuranceObj(
                    (intent.getStringExtra(ModelUtil.insuranceEditDataIntent)
                        .parseStringAsJsonObject() ?: JsonObject())
                )
        }


        val copayFilter = binding.insuranceRoot.addInsuranceCopayET.filters.toCollection(ArrayList())
        copayFilter.add(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Decimal2Filter()
            } else {
                DecimalFilter()
            }
        )
        binding.insuranceRoot.addInsuranceCopayET.filters = copayFilter.toTypedArray()

        val deductableFilter = binding.insuranceRoot.addInsuranceDeductableET.filters.toCollection(ArrayList())
        deductableFilter.add(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Decimal2Filter()
            } else {
                DecimalFilter()
            }
        )

        binding.insuranceRoot.addInsuranceDeductableET.filters = deductableFilter.toTypedArray()


        setPreCommonDataFetchListener()


    }

    override fun checkDataIsAvailable(): Boolean {

        if (addViewModel.insuranceCommonData.country.isNotEmpty()) {

            if (binding.insuranceRoot.addInsurancePayerNameET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsurancePayerIdET.text.toString().isNotBlank()) {
                return true
            }
//            if (addInsurancePracticeET.text.toString().isNotBlank()) {
//                return true
//            }

            if (addViewModel.insuranceCommonData.isUSUser) {
                val typeChange = UIUtil.onInsuranceTypeChange(binding.insuranceRoot.addInsuranceTypeUSET.text.toString())


                if (binding.insuranceRoot.addInsuranceTypeUSET.text.toString() != binding.insuranceRoot.addInsuranceTypeUSET.items[0]) {
                    return true
                }

                if (binding.insuranceRoot.addInsurancePlanET.text.toString() != typeChange.planName) {
                    return true
                }
            } else {
                if (binding.insuranceRoot.addInsuranceTypeET.text.toString().isNotBlank()) {
                    return true
                }
            }

            if (binding.insuranceRoot.addInsuranceTypeSecondary.isChecked) {
                return true
            }

            if (binding.insuranceRoot.addInsuranceMemberIdET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsurancePolicyGrpET.text.toString().isNotBlank()) {
                return true
            }


            if (binding.insuranceRoot.addInsuranceEmpNameET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceRelationshipET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceFirstNameET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceLastNameET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceDOBET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceGenderET.text.toString() != binding.insuranceRoot.addInsuranceGenderET.items[0]) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceAddressCheckbox.isChecked) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceLine1ET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceLine2ET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceCityET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceStateET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceCountryET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsurancePinCodeET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceHomePhoneET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceMobilePhoneET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsurancePayerLine1ET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsurancePayerCityET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsurancePayerStateET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsurancePayerCountryET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsurancePayerPinCodeET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceCopayET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceDeductableET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceCommentET.text.toString().isNotBlank()) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceMobilePhoneET.text.toString().isNotBlank()) {
                return true
            }

        }
        return false
    }

    override fun checkDataIsEditted(): Boolean {

        if (addViewModel.insuranceCommonData.country.isNotEmpty()) {

            val insuranceDetails = addViewModel.insuranceToBeEditted.insuredAndGuarantorDetails
            if (insuranceDetails != null) {
                if (binding.insuranceRoot.addInsuranceFirstNameET.text.toString()
                        .trim() != insuranceDetails.firstName?.trim()
                ) {
                    return true
                }
                if (binding.insuranceRoot.addInsuranceLastNameET.text.toString()
                        .trim() != insuranceDetails.lastName?.trim()
                ) {
                    return true
                }
                if (binding.insuranceRoot.addInsuranceDOBET.text.toString()
                        .trim() != insuranceDetails.dateOfBirth?.toString(ModelUtil.charmDisplayDateFormat())
                ) {
                    return true
                }
                if (binding.insuranceRoot.addInsuranceGenderET.text.toString()
                        .trim() != insuranceDetails.gender?.trim()
                ) {
                    return true
                }

                val address = insuranceDetails.postalAddress

                if (address != null) {
                    if (binding.insuranceRoot.addInsuranceLine1ET.text.toString().trim() != address.line1?.trim()) {
                        return true
                    }
                    if (binding.insuranceRoot.addInsuranceLine2ET.text.toString().trim() != address.line2?.trim()) {
                        return true
                    }
                    if (binding.insuranceRoot.addInsuranceCityET.text.toString().trim() != address.city?.trim()) {
                        return true
                    }
                    if (binding.insuranceRoot.addInsuranceStateET.text.toString().trim() != address.state?.trim()) {
                        return true
                    }
                    if (binding.insuranceRoot.addInsuranceCountryET.text.toString()
                            .trim() != (addViewModel.insuranceCommonData.country.firstOrNull { it.key == address.country?.trim() }?.name
                            ?: "")
                    ) {
                        return true
                    }
                    if (binding.insuranceRoot.addInsurancePinCodeET.text.toString()
                            .trim() != address.postalCode?.trim()
                    ) {
                        return true
                    }
                }

                val contactDetails = insuranceDetails.contactDetails
                if (contactDetails != null) {
                    if (binding.insuranceRoot.addInsuranceHomePhoneET.text.toString()
                            .trim() != contactDetails.homePhone?.trim()
                    ) {
                        return true
                    }
                    if (binding.insuranceRoot.addInsuranceMobilePhoneET.text.toString()
                            .trim() != contactDetails.mobile?.trim()
                    ) {
                        return true
                    }
                }

            }

            val payerAddress = addViewModel.insuranceToBeEditted.practicePayer?.postalAddress
            if (payerAddress != null) {
                if (binding.insuranceRoot.addInsurancePayerLine1ET.text.toString().trim() != payerAddress.line1?.trim()) {
                    return true
                }

                if (binding.insuranceRoot.addInsurancePayerCityET.text.toString().trim() != payerAddress.city?.trim()) {
                    return true
                }
                if (binding.insuranceRoot.addInsurancePayerStateET.text.toString().trim() != payerAddress.state?.trim()) {
                    return true
                }
                if (binding.insuranceRoot.addInsurancePayerCountryET.text.toString()
                        .trim() != (addViewModel.insuranceCommonData.country.firstOrNull { it.key == payerAddress.country?.trim() }?.name
                        ?: "")
                ) {
                    return true
                }
                if (binding.insuranceRoot.addInsurancePayerPinCodeET.text.toString()
                        .trim() != payerAddress.postalCode?.trim()
                ) {
                    return true
                }
            }

            if (binding.insuranceRoot.addInsuranceStartDateET.text.toString()
                    .trim() != addViewModel.insuranceToBeEditted.policyStartDate?.toString(ModelUtil.charmDisplayDateFormat())
            ) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceEndDateET.text.toString()
                    .trim() != addViewModel.insuranceToBeEditted.policyEndDate?.toString(ModelUtil.charmDisplayDateFormat())
            ) {
                return true
            }
            if (binding.insuranceRoot.addInsuranceCommentET.text.toString()
                    .trim() != addViewModel.insuranceToBeEditted.comment?.trim()
            ) {
                return true
            }
        }

        return false
    }

    private fun onInsuredAddressCheckChange(isChecked: Boolean, updatePat: Boolean = true) {


        val toDisable = !isChecked
        binding.insuranceRoot.addInsuranceLine1ET.isEnabled = toDisable
        binding.insuranceRoot.addInsuranceLine2ET.isEnabled = toDisable
        binding.insuranceRoot.addInsuranceCityET.isEnabled = toDisable
        binding.insuranceRoot.addInsuranceStateET.isEnabled = toDisable
        binding.insuranceRoot.addInsuranceCountryET.isEnabled = toDisable
        binding.insuranceRoot.addInsurancePinCodeET.isEnabled = toDisable
        binding.insuranceRoot.addInsuranceHomePhoneET.isEnabled = toDisable
        binding.insuranceRoot.addInsuranceMobilePhoneET.isEnabled = toDisable


        if (updatePat) {
            if (isChecked) {
                getPatientDetails {


                    handler.postDelayed({
                        val postalAddress = addViewModel.patientDetail.postalAddress
                            ?: PostalAddress()

                        binding.insuranceRoot.addInsuranceLine1ET.setText(postalAddress.line1)
                        binding.insuranceRoot.addInsuranceLine2ET.setText(postalAddress.line2)
                        binding.insuranceRoot.addInsuranceCityET.setText(postalAddress.city)
                        binding.insuranceRoot.addInsuranceCountryET.setText(addViewModel.insuranceCommonData.country.firstOrNull { it.key == postalAddress.country }?.name)
                        binding.insuranceRoot.addInsuranceStateET.setText(postalAddress.state)
                        binding.insuranceRoot.addInsurancePinCodeET.setText(postalAddress.postalCode)

                        val contactDetails = addViewModel.patientDetail.contactDetails
                            ?: ContactDetails()

                        binding.insuranceRoot.addInsuranceHomePhoneET.setText(contactDetails.landline)
                        binding.insuranceRoot.addInsuranceMobilePhoneET.setText(contactDetails.mobile)
                    }, 200)
                }
            } else {
                binding.insuranceRoot.addInsuranceLine1ET.setText("")
                binding.insuranceRoot.addInsuranceLine2ET.setText("")
                binding.insuranceRoot.addInsuranceCityET.setText("")
                binding.insuranceRoot.addInsuranceCountryET.setText("")
                binding.insuranceRoot.addInsuranceStateET.setText("")
                binding.insuranceRoot.addInsurancePinCodeET.setText("")
                binding.insuranceRoot.addInsuranceHomePhoneET.setText("")
                binding.insuranceRoot.addInsuranceMobilePhoneET.setText("")
            }
        }
    }

    private fun onRelationshipChange(isSelf: Boolean, updatePat: Boolean = true) {


        val toDisable = !isSelf
        binding.insuranceRoot.addInsuranceFirstNameET.isEnabled = toDisable
        binding.insuranceRoot.addInsuranceLastNameET.isEnabled = toDisable
        binding.insuranceRoot.addInsuranceDOBET.isEnabled = toDisable
        binding.insuranceRoot.addInsuranceGenderET.isEnabled = toDisable

        if (updatePat) {
            if (isSelf) {
                getPatientDetails {
                    binding.insuranceRoot.addInsuranceAddressCheckbox.isChecked = isSelf
                    binding.insuranceRoot.addInsuranceAddressCheckbox.isEnabled = toDisable
                    binding.insuranceRoot.addInsuranceFirstNameET.setText(addViewModel.patientDetail.firstName)
                    binding.insuranceRoot.addInsuranceLastNameET.setText(addViewModel.patientDetail.lastName)
                    binding.insuranceRoot.addInsuranceDOBET.setText(
                        addViewModel.patientDetail.dateOfBirth?.toString(
                            ModelUtil.charmDisplayDateFormat()
                        )
                    )
                    binding.insuranceRoot.addInsuranceGenderET.setText(addViewModel.patientDetail.gender)
                }
            } else {
                binding.insuranceRoot.addInsuranceAddressCheckbox.isChecked = isSelf
                binding.insuranceRoot.addInsuranceAddressCheckbox.isEnabled = toDisable
                binding.insuranceRoot.addInsuranceFirstNameET.setText("")
                binding.insuranceRoot.addInsuranceLastNameET.setText("")
                binding.insuranceRoot.addInsuranceDOBET.setText("")
                binding.insuranceRoot.addInsuranceGenderET.setText("")
            }
        } else {
            binding.insuranceRoot.addInsuranceAddressCheckbox.isChecked = isSelf
            binding.insuranceRoot.addInsuranceAddressCheckbox.isEnabled = toDisable

            onInsuredAddressCheckChange(isSelf, updatePat)
        }

    }

    private fun getPatientDetails(callback: (() -> Unit)) {

        if (addViewModel.patientDetail.practicePatientId.isNullOrBlank()) {

            progressDialog.show()
            addViewModel.getPatientDetail().observe(this) { resp ->

                if (resp != null) {
                    progressDialog.dismiss()

                    if (resp is Patient) {
                        addViewModel.patientDetail = resp
                        callback.invoke()
                    } else if (resp is APIResponseError) {

                        val alertDialogBuilder = AlertDialog.Builder(this)

                        alertDialogBuilder.setMessage(R.string.error_add_insurance_get_pat_detail_failed)
                        alertDialogBuilder.setPositiveButton(R.string.retry_str) { _, _ ->
                            getPatientDetails(callback)
                        }
                        val alertDialog = alertDialogBuilder.create()
                        alertDialog.setCanceledOnTouchOutside(false)
                        alertDialog.setCancelable(false)
                        alertDialog.show()
                    }
                }

            }
        } else {
            callback.invoke()
        }
    }

    private fun validateData(): PatientInsuranceDetails? {

        val insurance = PatientInsuranceDetails()
        if (!addViewModel.toEdit) {

//            if (addInsurancePracticeLayout.visibility == View.VISIBLE) {
//                if (isTextInputLayoutBlank(
//                        addInsurancePracticeLayout,
//                        addInsuranceScrollView,
//                        R.string.error_empty_practice_name
//                    )
//                ) {
//                    return null
//                }
//            }

            if (addViewModel.insuranceCommonData.isUSUser) {
                if (isTextInputLayoutBlank(
                        binding.insuranceRoot.addInsuranceTypeUSLayout,
                        binding.addInsuranceScrollView,
                        R.string.error_empty_insurance_type
                    )
                ) {
                    return null
                }
            } else {
                if (isTextInputLayoutBlank(
                        binding.insuranceRoot.addInsuranceTypeLayout,
                        binding.addInsuranceScrollView,
                        R.string.error_empty_insurance_type
                    )
                ) {
                    return null
                }
            }

            val insuranceType = if (addViewModel.insuranceCommonData.isUSUser) {
                binding.insuranceRoot.addInsuranceTypeUSET.text.toString()
            } else {
                binding.insuranceRoot.addInsuranceTypeET.text.toString()
            }

            if (insuranceType.isBlank()) {
                UIUtil.showToast(this, R.string.error_empty_insurance_type, Toast.LENGTH_LONG)
                return null
            }

            val planName = binding.insuranceRoot.addInsurancePlanET.text.toString()
            /* if (planName.isBlank()) {
                 UIUtil.showToast(this, R.string.error_empty_plan_name, Toast.LENGTH_LONG)
                 return null
             }*/
            if (isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsurancePlanLayout,
                    binding.addInsuranceScrollView,
                    R.string.error_empty_plan_name
                )
            ) {
                return null
            }

            val memberId = binding.insuranceRoot.addInsuranceMemberIdET.text.toString()
            if (memberId.isBlank()) {
                var hint = binding.insuranceRoot.addInsuranceMemberIdLayout.hint
                if (hint.isNullOrBlank()) {
                    hint = binding.insuranceRoot.addInsuranceMemberIdET.hint ?: ""
                }
                UIUtil.showToast(
                    this,
                    getString(R.string.error_empty_member_id, hint),
                    Toast.LENGTH_LONG
                )
                showFocus(
                    binding.insuranceRoot.addInsuranceMemberIdLayout,
                    binding.insuranceRoot.addInsuranceMemberIdET,
                    binding.addInsuranceScrollView
                )
                return null
            }

            val payerName = binding.insuranceRoot.addInsurancePayerNameET.text.toString()


            if (isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsurancePayerNameLayout,
                    binding.addInsuranceScrollView,
                    R.string.error_empty_payer_name
                )
            ) {
                return null
            }

            if (isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsurancePayerNameLayout,
                    binding.addInsuranceScrollView,
                    R.string.error_invalid_payer_name,
                    true
                )
            ) {
                return null
            }

            /*   if (payerName.isBlank()) {
                   UIUtil.showToast(this, R.string.error_empty_payer_name, Toast.LENGTH_LONG)
                   return null
               }
               if (!ModelUtil.hasAlpha(payerName)) {
                   UIUtil.showToast(this, R.string.error_invalid_payer_name, Toast.LENGTH_LONG)
                   return null
               }*/

            val payerID = binding.insuranceRoot.addInsurancePayerIdET.text.toString()
            /*if (payerID.isBlank()){
                UIUtil.showToast(this , R.string.error_empty_payer_id , Toast.LENGTH_LONG)
                return null
            }*/

            val payer = PracticePayer()
            payer.payerId = payerID
            payer.payerName = payerName

            val policyGrp = binding.insuranceRoot.addInsurancePolicyGrpET.text.toString()
            /* if (policyGrp.isBlank()){
                 UIUtil.showToast(this ,R.string.error_empty_policy_grp_num , Toast.LENGTH_LONG )
                 return null
             }*/

            val empName = binding.insuranceRoot.addInsuranceEmpNameET.text.toString()
            /* if (empName.isBlank()){
                 UIUtil.showToast(this ,R.string.error_empty_emp_num , Toast.LENGTH_LONG )
                 return null
             }*/


            val relationship = binding.insuranceRoot.addInsuranceRelationshipET.text.toString()

            /* if (relationship.isBlank()) {
                 UIUtil.showToast(this, R.string.error_empty_relationship, Toast.LENGTH_LONG)
                 return null
             }*/

//            if (isTextInputLayoutBlank(
//                    addInsuranceRelationshipLayout,
//                    addInsuranceScrollView,
//                    R.string.error_empty_relationship
//                )
//            ) {
//                return null
//            }


            val copay = binding.insuranceRoot.addInsuranceCopayET.text.toString()
            val deductable = binding.insuranceRoot.addInsuranceDeductableET.text.toString()


            insurance.insurancePlanName = planName
            insurance.insurancePlanType = insuranceType

            insurance.policyNumber = memberId
            insurance.groupNumber = policyGrp
            insurance.copay = copay
            insurance.deductable = deductable

            insurance.practicePayer = payer
            val insuredAndGuarantorDetails = InsuredAndGuarantorDetails()


            val employerDetails = InsuredEmployerDetails()
            employerDetails.name = empName
            insuredAndGuarantorDetails.insuredEmployerDetails = employerDetails

            insurance.insuredAndGuarantorDetails = insuredAndGuarantorDetails
        } else {

            val tempInsurance = addViewModel.insuranceToBeEditted
            insurance.insurancePlanName = tempInsurance.insurancePlanName
            insurance.insurancePlanType = tempInsurance.insurancePlanType
            insurance.patientInsuranceDetailsId = tempInsurance.patientInsuranceDetailsId

            insurance.type = tempInsurance.type
            insurance.isPrimary = tempInsurance.isPrimary
            insurance.policyNumber = tempInsurance.policyNumber
            insurance.groupNumber = tempInsurance.groupNumber
            insurance.copay = tempInsurance.copay
            insurance.deductable = tempInsurance.deductable

            val insuredAndGuarantorDetails = InsuredAndGuarantorDetails()
            insuredAndGuarantorDetails.relationship =
                tempInsurance.insuredAndGuarantorDetails?.relationship


            val practicePayer = PracticePayer()
            practicePayer.payerName = tempInsurance.practicePayer?.payerName
            practicePayer.payerId = tempInsurance.practicePayer?.payerId
            insurance.practicePayer = practicePayer

            val employerDetails = InsuredEmployerDetails()
            employerDetails.name =
                tempInsurance.insuredAndGuarantorDetails?.insuredEmployerDetails?.name
            insuredAndGuarantorDetails.insuredEmployerDetails = employerDetails

            insurance.insuredAndGuarantorDetails = insuredAndGuarantorDetails
        }

        if (binding.insuranceRoot.addInsuranceTypeRadioGrp.checkedRadioButtonId == -1) {
            UIUtil.showToast(this, R.string.error_empty_insurance_category, Toast.LENGTH_LONG)
            showFocus(binding.insuranceRoot.addInsuranceInsuranceTypeLabel, binding.addInsuranceScrollView)
            return null
        }

        insurance.type = if (binding.insuranceRoot.addInsuranceTypePrimary.isChecked) {
            ModelUtil.insuranceTypePrimary
        } else {
            ModelUtil.insuranceTypeSecondary
        }

        insurance.isPrimary = binding.insuranceRoot.addInsuranceTypePrimary.isChecked

        val startDateTxt = binding.insuranceRoot.addInsuranceStartDateET.text.toString()
        val endDateTxt = binding.insuranceRoot.addInsuranceEndDateET.text.toString()
        if (startDateTxt.isBlank()) {
            UIUtil.showToast(this, R.string.error_empty_valid_from, Toast.LENGTH_SHORT)
            // showFocus(addInsuranceStartDateLayout, addInsuranceScrollView)
            binding.addInsuranceScrollView.scrollToView(binding.insuranceRoot.addInsuranceStartDateLayout)
            binding.insuranceRoot.addInsuranceStartDateET.performClick()
            return null
        }
        if (endDateTxt.isBlank()) {
            UIUtil.showToast(this, R.string.error_empty_valid_to, Toast.LENGTH_SHORT)
            // showFocus(addInsuranceEndDateLayout, addInsuranceScrollView)
            binding.addInsuranceScrollView.scrollToView(binding.insuranceRoot.addInsuranceEndDateET)
            binding.insuranceRoot.addInsuranceEndDateET.performClick()
            return null
        }
        val startDate = DateTime.parse(
            binding.insuranceRoot.addInsuranceStartDateET.text.toString(),
            ModelUtil.charmDisplayDateFormat()
        )
        val endDate = DateTime.parse(
            binding.insuranceRoot.addInsuranceEndDateET.text.toString(),
            ModelUtil.charmDisplayDateFormat()
        )


        if (endDate.isBefore(startDate)) {
            UIUtil.showToast(this, R.string.error_add_insurance_invalid_period, Toast.LENGTH_LONG)
            binding.addInsuranceScrollView.smoothScrollTo(0, binding.insuranceRoot.addInsuranceStartDateLayout.top)
            return null
        }


        val insuredAndGuarantorDetails = insurance.insuredAndGuarantorDetails

        if (insuredAndGuarantorDetails != null) {

            if (!addViewModel.toEdit) {
                val relationship = binding.insuranceRoot.addInsuranceRelationshipET.text.toString()

                /* if (relationship.isBlank()) {
                     UIUtil.showToast(this, R.string.error_empty_relationship, Toast.LENGTH_LONG)
                     return null
                 }*/

                if (isTextInputLayoutBlank(
                        binding.insuranceRoot.addInsuranceRelationshipLayout,
                        binding.addInsuranceScrollView,
                        R.string.error_empty_relationship
                    )
                ) {
                    return null
                }

                insuredAndGuarantorDetails.relationship = relationship

            }

            val firstName = binding.insuranceRoot.addInsuranceFirstNameET.text.toString()


            if (isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsuranceFirstNameLayout,
                    binding.addInsuranceScrollView,
                    R.string.error_empty_first_name
                )
            ) {
                return null
            }
            if (isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsuranceFirstNameLayout,
                    binding.addInsuranceScrollView,
                    R.string.error_invalid_first_name,
                    true
                )
            ) {
                return null
            }

            /* if (firstName.isBlank()) {
                 UIUtil.showToast(this, R.string.error_empty_first_name, Toast.LENGTH_SHORT)
                 return null
             }

             if (!ModelUtil.hasAlpha(firstName)) {
                 UIUtil.showToast(this, R.string.error_invalid_first_name, Toast.LENGTH_SHORT)
                 return null
             }*/

            val lastName = binding.insuranceRoot.addInsuranceLastNameET.text.toString()

            if (isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsuranceLastNameLayout,
                    binding.addInsuranceScrollView,
                    R.string.error_empty_last_name
                )
            ) {
                return null
            }
            if (isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsuranceLastNameLayout,
                    binding.addInsuranceScrollView,
                    R.string.error_invalid_last_name,
                    true
                )
            ) {
                return null
            }

            /*if (lastName.isBlank()) {
                UIUtil.showToast(this, R.string.error_empty_last_name, Toast.LENGTH_SHORT)
                return null
            }

            if (!ModelUtil.hasAlpha(lastName)) {
                UIUtil.showToast(this, R.string.error_invalid_last_name, Toast.LENGTH_SHORT)
                return null
            }*/

            val dob = binding.insuranceRoot.addInsuranceDOBET.text.toString()

//            if (isTextInputLayoutBlank(
//                    addInsuranceDOBLayout,
//                    addInsuranceScrollView,
//                    R.string.error_empty_dob
//                )
//            ) {
//                return null
//            }

            if (dob.isBlank()) {
                UIUtil.showToast(this, R.string.error_empty_dob, Toast.LENGTH_SHORT)
                // showFocus(addInsuranceStartDateLayout, addInsuranceScrollView)
                binding.addInsuranceScrollView.scrollToView(binding.insuranceRoot.addInsuranceDOBLayout)
                binding.insuranceRoot.addInsuranceDOBET.performClick()
                return null
            }

            /*  if (dob.isBlank()) {
                  UIUtil.showToast(this, R.string.error_empty_dob, Toast.LENGTH_SHORT)
                  return null
              }
  */
            val gender = binding.insuranceRoot.addInsuranceGenderET.text.toString()

            if (isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsuranceGenderLayout,
                    binding.addInsuranceScrollView,
                    R.string.error_empty_birth_sex
                )
            ) {
                return null
            }

            val isSameAddressChecked = !binding.insuranceRoot.addInsuranceAddressCheckbox.isChecked

            val addressLine1 = binding.insuranceRoot.addInsuranceLine1ET.text.toString()

            if (isSameAddressChecked && isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsuranceLine1Layout,
                    binding.addInsuranceScrollView,
                    R.string.error_invalid_line_1,
                    true
                )
            ) {
                return null
            }
            /* if (!ModelUtil.hasAlpha(addressLine1)) {
                 UIUtil.showToast(this, R.string.error_invalid_line_1, Toast.LENGTH_SHORT)
                 return null
             }*/

            val addressLine2 = binding.insuranceRoot.addInsuranceLine2ET.text.toString()

            if (isSameAddressChecked && isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsuranceLine2Layout,
                    binding.addInsuranceScrollView,
                    R.string.error_invalid_line_2,
                    true
                )
            ) {
                return null
            }

            /* if (!ModelUtil.hasAlpha(addressLine2)) {
                 UIUtil.showToast(this, R.string.error_invalid_line_2, Toast.LENGTH_SHORT)
                 return null
             }*/

            val countryName = binding.insuranceRoot.addInsuranceCountryET.text.toString()
            val state = binding.insuranceRoot.addInsuranceStateET.text.toString()

            val countryKey = addViewModel.insuranceCommonData.country.firstOrNull {
                it.name.equals(
                    countryName,
                    true
                )
            }?.key
                ?: ""
            if (isSameAddressChecked && countryName.isNotBlank()) {

                if (countryKey.isBlank()) {
                    UIUtil.showToast(this, R.string.error_wrong_country, Toast.LENGTH_SHORT)
                    showFocus(
                        binding.insuranceRoot.addInsuranceCountryLayout,
                        binding.insuranceRoot.addInsuranceCountryET,
                        binding.addInsuranceScrollView
                    )

                    return null
                }



                if (state.isNotBlank()) {

                    if (!isValidState(
                            countryKey,
                            state,
                            binding.insuranceRoot.addInsuranceStateET,
                            binding.insuranceRoot.addInsuranceStateLayout
                        )
                    ) {
                        return null
                    }
                }
            } else {

                if (isSameAddressChecked && state.isNotBlank()) {
                    if (isTextInputLayoutBlank(
                            binding.insuranceRoot.addInsuranceStateLayout,
                            binding.addInsuranceScrollView,
                            R.string.error_invalid_state,
                            true
                        )
                    ) {
                        return null
                    }
                }
            }

            val city = binding.insuranceRoot.addInsuranceCityET.text.toString()

            if (isSameAddressChecked && isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsuranceCityLayout,
                    binding.addInsuranceScrollView,
                    R.string.error_invalid_city,
                    true
                )
            ) {
                return null
            }

            val pinCode = binding.insuranceRoot.addInsurancePinCodeET.text.toString()

            if (isSameAddressChecked && pinCode.isNotBlank()) {
                if (countryKey == "in") {
                    if (pinCode.length != 6) {
                        UIUtil.showToast(this, R.string.error_invalid_pincode, Toast.LENGTH_SHORT)
                        showFocus(
                            binding.insuranceRoot.addInsurancePinCodeLayout,
                            binding.insuranceRoot.addInsurancePinCodeET,
                            binding.addInsuranceScrollView
                        )
                        return null
                    }
                } else if (countryKey == "us") {
                    if (pinCode.length != 5) {
                        UIUtil.showToast(this, R.string.error_invalid_pincode, Toast.LENGTH_SHORT)
                        showFocus(
                            binding.insuranceRoot.addInsurancePinCodeLayout,
                            binding.insuranceRoot.addInsurancePinCodeET,
                            binding.addInsuranceScrollView
                        )
                        return null
                    }
                }
            }


            val homePhone = binding.insuranceRoot.addInsuranceHomePhoneET.text.toString()
            val mobileNumber = binding.insuranceRoot.addInsuranceMobilePhoneET.text.toString()
            /* if (mobileNumber.isBlank()){
                 UIUtil.showToast(this , R.string.error_empty_mobile_num , Toast.LENGTH_SHORT)
                 return null
             }*/

            insuredAndGuarantorDetails.firstName = firstName
            insuredAndGuarantorDetails.lastName = lastName
            insuredAndGuarantorDetails.dateOfBirth =
                DateTime.parse(dob, ModelUtil.charmDisplayDateFormat())
            insuredAndGuarantorDetails.gender = gender

            val postalAddress = PostalAddress()
            postalAddress.line1 = addressLine1
            postalAddress.line2 = addressLine2
            postalAddress.city = city
            postalAddress.state = state
            postalAddress.country = countryKey
            postalAddress.postalCode = pinCode

            val contactDetails = ContactDetails()
            contactDetails.homePhone = homePhone
            contactDetails.mobile = mobileNumber

            insuredAndGuarantorDetails.contactDetails = contactDetails
            insuredAndGuarantorDetails.postalAddress = postalAddress
        }

        val practicePayer = insurance.practicePayer
        if (practicePayer != null) {

            val addressLine1 = binding.insuranceRoot.addInsurancePayerLine1ET.text.toString()
            if (isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsurancePayerLine1Layout,
                    binding.addInsuranceScrollView,
                    R.string.error_invalid_line_1,
                    true
                )
            ) {
                return null
            }
            /* if (!ModelUtil.hasAlpha(addressLine1)) {
                 UIUtil.showToast(this, R.string.error_invalid_line_1, Toast.LENGTH_SHORT)
                 return null
             }
 */

            val countryName = binding.insuranceRoot.addInsurancePayerCountryET.text.toString()
//            if (isTextInputLayoutBlank(addInsurancePayerCountryLayout, addInsuranceScrollView, R.string.error_empty_country)) {
//                return null
//            }

            val countryKey = addViewModel.insuranceCommonData.country.firstOrNull {
                it.name.equals(
                    countryName,
                    true
                )
            }?.key
                ?: ""
//
//            if (countryKey.isBlank()) {
//                UIUtil.showToast(this, R.string.error_wrong_country, Toast.LENGTH_SHORT)
//                showFocus(addInsurancePayerCountryLayout, addInsurancePayerCountryET, addInsuranceScrollView)
//                return null
//            }

            val state = binding.insuranceRoot.addInsurancePayerStateET.text.toString()

            if (state.isNotBlank()) {

                if (!isValidState(
                        countryKey,
                        state,
                        binding.insuranceRoot.addInsurancePayerStateET,
                        binding.insuranceRoot.addInsurancePayerStateLayout
                    )
                ) {
                    return null
                }
            }

            val city = binding.insuranceRoot.addInsurancePayerCityET.text.toString()
            if (isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsurancePayerCityLayout,
                    binding.addInsuranceScrollView,
                    R.string.error_invalid_state,
                    true
                )
            ) {
                return null
            }

            val pinCode = binding.insuranceRoot.addInsurancePayerPinCodeET.text.toString()
            if (pinCode.isNotBlank()) {
                if (countryKey == "in") {
                    if (pinCode.length != 6) {
                        UIUtil.showToast(this, R.string.error_invalid_pincode, Toast.LENGTH_SHORT)
                        showFocus(
                            binding.insuranceRoot.addInsurancePayerPinCodeLayout,
                            binding.insuranceRoot.addInsurancePayerPinCodeET,
                            binding.addInsuranceScrollView
                        )
                        return null
                    }
                } else if (countryKey == "us") {
                    if (pinCode.length != 5) {
                        UIUtil.showToast(this, R.string.error_invalid_pincode, Toast.LENGTH_SHORT)
                        showFocus(
                            binding.insuranceRoot.addInsurancePayerPinCodeLayout,
                            binding.insuranceRoot.addInsurancePayerPinCodeET,
                            binding.addInsuranceScrollView
                        )
                        return null
                    }
                }
            }

            val postalAddress = PostalAddress()
            postalAddress.line1 = addressLine1
            postalAddress.city = city
            postalAddress.state = state
            postalAddress.country = countryKey
            postalAddress.postalCode = pinCode

            practicePayer.postalAddress = postalAddress
        }



        if (insuredAndGuarantorDetails?.dateOfBirth != null) {
            if (startDate.isBefore(insuredAndGuarantorDetails.dateOfBirth)) {
                UIUtil.showToast(
                    this,
                    R.string.error_add_insurance_start_date_before_dob,
                    Toast.LENGTH_LONG
                )
                binding.addInsuranceScrollView.smoothScrollTo(0, binding.insuranceRoot.addInsuranceStartDateLayout.top)
                return null
            }

            if (endDate.isBefore(insuredAndGuarantorDetails.dateOfBirth)) {
                UIUtil.showToast(
                    this,
                    R.string.error_add_insurance_end_date_before_dob,
                    Toast.LENGTH_LONG
                )
                binding.addInsuranceScrollView.smoothScrollTo(0, binding.insuranceRoot.addInsuranceStartDateLayout.top)
                return null
            }
        }


        insurance.policyStartDate = startDate
        insurance.policyEndDate = endDate
        insurance.practiceId = addViewModel.practiceId
        val comment = binding.insuranceRoot.addInsuranceCommentET.text.toString()

        if (comment.isNotBlank()) {

            if (isTextInputLayoutBlank(
                    binding.insuranceRoot.addInsuranceCommentLayout,
                    binding.addInsuranceScrollView,
                    R.string.error_invalid_comments,
                    true
                )
            ) {
                return null
            }
            /* if (!ModelUtil.hasAlpha(comment)) {
                 UIUtil.showToast(this, R.string.error_invalid_comments, Toast.LENGTH_SHORT)
                 return null
             }*/
        }

        insurance.comment = comment


        return insurance
    }

    private fun isValidState(
        countryKey: String,
        state: String,
        stateET: MDTextInputAutoCompleteTextView,
        stateLayout: MDTextInputLayout
    ): Boolean {
        val stateList = addViewModel.insuranceCommonData.stateList.filter {
            it.countryCode == countryKey
        }

        if (stateList.isNotEmpty()) {
            if (!stateList.first().stateList.any {
                    state.equals(
                        it,
                        true
                    )
                }) {
                UIUtil.showToast(this, R.string.error_wrong_state, Toast.LENGTH_SHORT)
                showFocus(
                    stateLayout,
                    stateET,
                    binding.addInsuranceScrollView
                )
                return false
            }
        } else if (isTextInputLayoutBlank(
                stateLayout,
                binding.addInsuranceScrollView,
                R.string.error_invalid_state,
                true
            )
        ) {
            return false
        }
        return true
    }

    private fun setPreCommonDataFetchListener() {


        binding.addInsuranceErrorPage.errorPageTryAgainBtn.setOnClickListener {
            getCommonData()
        }

        binding.addInsuranceBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.insuranceRoot.addInsuranceDOBET.inputType = InputType.TYPE_NULL

        binding.insuranceRoot.addInsuranceDOBET.setOnClickListener {
            showDatePicker(binding.insuranceRoot.addInsuranceDOBET, true)
        }

        binding.insuranceRoot.addInsuranceDOBET.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                showDatePicker(binding.insuranceRoot.addInsuranceDOBET, true)
            }
        }

        binding.insuranceRoot.addInsuranceStartDateET.inputType = InputType.TYPE_NULL
        binding.insuranceRoot.addInsuranceStartDateET.setOnClickListener {
            showDatePicker(binding.insuranceRoot.addInsuranceStartDateET, false)
        }
        binding.insuranceRoot.addInsuranceStartDateET.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                showDatePicker(binding.insuranceRoot.addInsuranceStartDateET, false)
            }
        }

        binding.insuranceRoot.addInsuranceEndDateET.inputType = InputType.TYPE_NULL
        binding.insuranceRoot.addInsuranceEndDateET.setOnClickListener {
            showDatePicker(binding.insuranceRoot.addInsuranceEndDateET, false)
        }
        binding.insuranceRoot.addInsuranceEndDateET.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                showDatePicker(binding.insuranceRoot.addInsuranceEndDateET, false)
            }
        }


        binding.insuranceRoot.addInsuranceTypeUSET.setOnItemClickListener { _, str ->
            val insuranceTypeChange = UIUtil.onInsuranceTypeChange(str)
            binding.insuranceRoot.addInsuranceMemberIdLayout.hint = getString(insuranceTypeChange.memberIdLabel)
            UIUtil.makeTextViewMandatory(binding.insuranceRoot.addInsuranceMemberIdLayout)
            binding.insuranceRoot.addInsurancePlanET.setText(insuranceTypeChange.planName)
        }

        binding.insuranceRoot.addInsuranceCountryET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                /*if (editable.toString().isNotBlank()) {
                    val textInputLayout = (addInsuranceCountryET.parent as ViewGroup).parent as TextInputLayout
                    if (addInsuranceCountryET.isGravityCenter) {
                        TransitionManager.beginDelayedTransition(addInsuranceCountryET.parent as ViewGroup)
                        addInsuranceCountryET.gravity = Gravity.BOTTOM
                        textInputLayout.setPadding(
                                addInsuranceCountryET.edittextPadding,
                                addInsuranceCountryET.edittextPadding,
                                addInsuranceCountryET.edittextPadding,
                                addInsuranceCountryET.edittextPadding
                        )
                        addInsuranceCountryET.isGravityCenter = false
                    }
                }*/

                val countryKey = addViewModel.insuranceCommonData.country.firstOrNull {
                    it.name.equals(
                        editable.toString(),
                        true
                    )
                }?.key
                    ?: ""
                val stateList = addViewModel.insuranceCommonData.stateList.firstOrNull {
                    it.countryCode == countryKey
                }

                UIUtil.setStateAdapter(
                    editable.toString(),
                    binding.insuranceRoot.addInsuranceStateLayout,
                    binding.insuranceRoot.addInsuranceStateET,
                    binding.insuranceRoot.addInsurancePinCodeET,
                    stateAdapter,
                    stateList?.stateList ?: arrayListOf()
                )
            }
        })

        binding.insuranceRoot.addInsurancePayerCountryET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                /*val textInputLayout = (addInsurancePayerCountryET.parent as ViewGroup).parent as TextInputLayout

                if (editable.toString().isNotBlank()) {
                    if (addInsurancePayerCountryET.isGravityCenter) {
                        TransitionManager.beginDelayedTransition(addInsurancePayerCountryET.parent as ViewGroup)
                        addInsurancePayerCountryET.gravity = Gravity.BOTTOM
                        textInputLayout.setPadding(
                                addInsurancePayerCountryET.edittextPadding,
                                addInsurancePayerCountryET.edittextPadding,
                                addInsurancePayerCountryET.edittextPadding,
                                addInsurancePayerCountryET.edittextPadding
                        )
                        addInsurancePayerCountryET.isGravityCenter = false
                    }
                }
                else{
                    if (!addInsurancePayerCountryET.isFocused){
                        if (addInsurancePayerCountryET.gravity != Gravity.CENTER_VERTICAL && editable?.toString().isNullOrBlank()) {
                            TransitionManager.beginDelayedTransition(parent as ViewGroup)
                            addInsurancePayerCountryET.gravity = Gravity.CENTER_VERTICAL
                            textInputLayout.setPadding(
                                    addInsurancePayerCountryET.edittextPadding,
                                    0,
                                    addInsurancePayerCountryET.edittextPadding,
                                    addInsurancePayerCountryET.edittextPadding
                            )
                            addInsurancePayerCountryET.isGravityCenter = true
                        }
                    }
                }*/

                val countryKey = addViewModel.insuranceCommonData.country.firstOrNull {
                    it.name.equals(
                        editable.toString(),
                        true
                    )
                }?.key
                    ?: ""
                val stateList = addViewModel.insuranceCommonData.stateList.firstOrNull {
                    it.countryCode == countryKey
                }

                UIUtil.setStateAdapter(
                    editable.toString(),
                    binding.insuranceRoot.addInsurancePayerStateLayout,
                    binding.insuranceRoot.addInsurancePayerStateET,
                    binding.insuranceRoot.addInsurancePayerPinCodeET,
                    payerStateAdapter,
                    stateList?.stateList ?: arrayListOf()
                )

            }
        })


        binding.addInsuranceBtn.setOnClickListener {

            val insurance = validateData()

            if (insurance != null) {

                progressDialog.show()

                val listener = if (!addViewModel.toEdit) {
                    addViewModel.addInsurance(insurance)
                } else {
                    addViewModel.editInsurance(insurance)
                }

                listener.observe(this) { resp ->
                    if (resp != null) {
                        progressDialog.dismiss()
                        if (resp is Boolean) {
                            addViewModel.isDataAdded = true
                            setResult(Activity.RESULT_OK)
                            super.onBackPressed()
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                        } else if (resp is APIResponseError) {

                            if (resp.errorMsg.isNullOrBlank()) {
                                if (!addViewModel.toEdit) {
                                    UIUtil.showToast(
                                        this,
                                        R.string.error_add_insurance_msg,
                                        Toast.LENGTH_LONG
                                    )
                                } else {
                                    UIUtil.showToast(
                                        this,
                                        R.string.error_edit_insurance_msg,
                                        Toast.LENGTH_LONG
                                    )
                                }
                            } else {
                                UIUtil.showToast(this, resp.errorMsg?:getString(R.string.something_went_wrong), Toast.LENGTH_LONG)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setPostCommonDataFetchListener() {

        binding.insuranceRoot.addInsuranceRelationshipET.setOnItemClickListener { _, str ->
            onRelationshipChange(str.equals("SELF", true))
        }

        binding.insuranceRoot.addInsuranceAddressCheckbox.setOnCheckedChangeListener { _, isChecked ->
            onInsuredAddressCheckChange(isChecked)
        }
    }

    private fun onPayerChange(position: Int) {

        val payer = addViewModel.payerList.getOrNull(position)
        if (payer != null) {

            binding.insuranceRoot.addInsurancePayerIdET.setText(payer.payerId)
            binding.insuranceRoot.addInsurancePayerLine1ET.setText(payer.postalAddress?.line1)
            binding.insuranceRoot.addInsurancePayerCityET.setText(payer.postalAddress?.city)

            binding.insuranceRoot.addInsurancePayerCountryET.setText(addViewModel.insuranceCommonData.country.firstOrNull { it.key == payer.postalAddress?.country }?.name)
            binding.insuranceRoot.addInsurancePayerStateET.setText(payer.postalAddress?.state)
            binding.insuranceRoot.addInsurancePayerPinCodeET.setText(payer.postalAddress?.postalCode)

        }
    }

    private fun getCommonData(showDialog: Boolean = false) {


        if (showDialog) {
            progressDialog.show()
        } else {
            binding.addInsuranceProgress.root.visibility = View.VISIBLE
            binding.addInsuranceMainLayout.visibility = View.GONE
            binding.addInsuranceErrorPage.root.visibility = View.GONE
        }

        addViewModel.getInsuranceCommonData().observe(this) { resp ->

            if (resp != null) {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }
                binding.addInsuranceProgress.root.visibility = View.GONE
                if (resp is InsuranceCommonData) {

                    addViewModel.insuranceCommonData = resp
                    binding.addInsuranceMainLayout.visibility = View.VISIBLE

                    countryAdapter.setItemList(resp.country.map { it.name })
                    payerCountryAdapter.setItemList(countryAdapter.itemList)

                    binding.insuranceRoot.addInsuranceGenderET.setItems(
                        arrayListOf(
                            ModelUtil.genderMale,
                            ModelUtil.genderFemale,
                            ModelUtil.genderUnknown
                        ), this
                    )
                    binding.insuranceRoot.addInsuranceGenderET.setAnchorView(binding.insuranceRoot.addInsuranceGenderLayout)

                    if (resp.isUSUser) {
                        binding.insuranceRoot.addInsuranceTypeUSLayout.visibility = View.VISIBLE
                        binding.insuranceRoot.addInsuranceTypeLayout.visibility = View.GONE
                        binding.insuranceRoot.addInsuranceTypeUSET.setItems(resp.insuranceTypes, this)
                        binding.insuranceRoot.addInsuranceTypeUSET.setAnchorView(binding.insuranceRoot.addInsuranceTypeUSLayout)
                    } else {
                        binding.insuranceRoot.addInsuranceTypeUSLayout.visibility = View.GONE
                        binding.insuranceRoot.addInsuranceTypeLayout.visibility = View.VISIBLE
                    }

                    binding.insuranceRoot.addInsuranceRelationshipET.setItems(resp.relationships, this)
                    binding.insuranceRoot.addInsuranceRelationshipET.setAnchorView(binding.insuranceRoot.addInsuranceRelationshipLayout)


                    if (!addViewModel.isFilled) {

                        addViewModel.isFilled = true


                        if (addViewModel.toEdit) {


                            val insurance = addViewModel.insuranceToBeEditted

                            if (addViewModel.insuranceCommonData.isUSUser) {
                                binding.insuranceRoot.addInsuranceTypeUSET.setText(insurance.insurancePlanType)
                            } else {
                                binding.insuranceRoot.addInsuranceTypeET.setText(insurance.insurancePlanType)
                            }

                            val insuranceTypeChange = UIUtil.onInsuranceTypeChange(
                                insurance.insurancePlanType
                                    ?: ""
                            )
                            binding.insuranceRoot.addInsuranceMemberIdLayout.hint =
                                getString(insuranceTypeChange.memberIdLabel)

                            binding.insuranceRoot.addInsurancePlanET.setText(insurance.insurancePlanName)
                            binding.insuranceRoot.addInsuranceMemberIdET.setText(insurance.policyNumber)
                            binding.insuranceRoot.addInsurancePolicyGrpET.setText(insurance.groupNumber)
                            binding.insuranceRoot.addInsuranceStartDateET.setText(
                                insurance.policyStartDate?.toString(
                                    ModelUtil.charmDisplayDateFormat()
                                )
                            )
                            binding.insuranceRoot.addInsuranceEndDateET.setText(
                                insurance.policyEndDate?.toString(
                                    ModelUtil.charmDisplayDateFormat()
                                )
                            )
                            binding.insuranceRoot.addInsuranceCopayET.setText(insurance.copay)
                            binding.insuranceRoot.addInsuranceDeductableET.setText(insurance.deductable)
                            binding.insuranceRoot.addInsuranceCommentET.setText(insurance.comment)

                            val insuredAndGuarantorDetails = insurance.insuredAndGuarantorDetails

                            if (insuredAndGuarantorDetails != null) {


                                binding.insuranceRoot.addInsuranceEmpNameET.setText(insuredAndGuarantorDetails.insuredEmployerDetails?.name)

                                binding.insuranceRoot.addInsuranceFirstNameET.setText(insuredAndGuarantorDetails.firstName)
                                binding.insuranceRoot.addInsuranceLastNameET.setText(insuredAndGuarantorDetails.lastName)
                                binding.insuranceRoot.addInsuranceRelationshipET.setText(insuredAndGuarantorDetails.relationship)
                                binding.insuranceRoot.addInsuranceDOBET.setText(
                                    insuredAndGuarantorDetails.dateOfBirth?.toString(
                                        ModelUtil.charmDisplayDateFormat()
                                    )
                                )
                                binding.insuranceRoot.addInsuranceGenderET.setText(insuredAndGuarantorDetails.gender)

                                onRelationshipChange(
                                    insuredAndGuarantorDetails.relationship?.equals(
                                        "SELF",
                                        true
                                    ) == true, false
                                )

                                val address = insuredAndGuarantorDetails.postalAddress
                                if (address != null) {
                                    binding.insuranceRoot.addInsuranceLine1ET.setText(address.line1)
                                    binding.insuranceRoot.addInsuranceLine2ET.setText(address.line2)
                                    binding.insuranceRoot.addInsuranceCityET.setText(address.city)
                                    binding.insuranceRoot.addInsuranceCountryET.setText(addViewModel.insuranceCommonData.country.firstOrNull { it.key == address.country }?.name)
                                    binding.insuranceRoot.addInsuranceStateET.setText(address.state)
                                    binding.insuranceRoot.addInsurancePinCodeET.setText(address.postalCode)
                                }

                                val contactDetails = insuredAndGuarantorDetails.contactDetails

                                if (contactDetails != null) {
                                    binding.insuranceRoot.addInsuranceHomePhoneET.setText(contactDetails.homePhone)
                                    binding.insuranceRoot.addInsuranceMobilePhoneET.setText(contactDetails.mobile)
                                }

                            }


                            if (insurance.isPrimary) {
                                binding.insuranceRoot.addInsuranceTypePrimary.isChecked = true
                            } else {
                                binding.insuranceRoot.addInsuranceTypeSecondary.isChecked = true
                            }


                            val payer = insurance.practicePayer
                            if (payer != null) {
                                binding.insuranceRoot.addInsurancePayerNameET.setText(payer.payerName)
                                binding.insuranceRoot.addInsurancePayerIdET.setText(payer.payerId)

                                val payerAddress = payer.postalAddress

                                if (payerAddress != null) {
                                    binding.insuranceRoot.addInsurancePayerLine1ET.setText(payerAddress.line1)
                                    binding.insuranceRoot.addInsurancePayerCityET.setText(payerAddress.city)
                                    binding.insuranceRoot.addInsurancePayerCountryET.setText(addViewModel.insuranceCommonData.country.firstOrNull { it.key == payerAddress.country }?.name)
                                    binding.insuranceRoot.addInsurancePayerStateET.setText(payerAddress.state)
                                    binding.insuranceRoot.addInsurancePayerPinCodeET.setText(payerAddress.postalCode)
                                }
                            }

                            binding.insuranceRoot.addInsurancePayerNameET.isEnabled = false
                            binding.insuranceRoot.addInsurancePayerIdET.isEnabled = false
                            binding.insuranceRoot.addInsurancePlanET.isEnabled = false
                            binding.insuranceRoot.addInsuranceTypeET.isEnabled = false
                            binding.insuranceRoot.addInsuranceTypeUSET.isEnabled = false
                            binding.insuranceRoot.addInsuranceMemberIdET.isEnabled = false
                            binding.insuranceRoot.addInsurancePolicyGrpET.isEnabled = false
                            binding.insuranceRoot.addInsuranceCopayET.isEnabled = false
                            binding.insuranceRoot.addInsuranceDeductableET.isEnabled = false
                            binding.insuranceRoot.addInsuranceEmpNameET.isEnabled = false
                            binding.insuranceRoot.addInsuranceRelationshipET.isEnabled = false
//                            addInsurancePracticeET.isEnabled = false
//                            addInsuranceTypePrimary.isEnabled = false
//                            addInsuranceTypeSecondary.isEnabled = false

                        }

                        setPostCommonDataFetchListener()
                    }


                    if (!addViewModel.toEdit) {
                        if (resp.isUSUser) {

                            insuranceNameAdapter = MDAutoCompleteTextViewAdapter(
                                this@AddInsuranceActivity,
                                arrayListOf()
                            )
                            binding.insuranceRoot.addInsurancePayerNameET.setAdapter(insuranceNameAdapter)

                            binding.insuranceRoot.addInsurancePayerNameET.removeTextChangedListener(textWatcher)
                            binding.insuranceRoot.addInsurancePayerNameET.addTextChangedListener(textWatcher)
                        } else {
                            binding.insuranceRoot.addInsurancePayerNameET.removeTextChangedListener(textWatcher)
                        }
                    }


                } else if (resp is APIResponseError) {
                    binding.addInsuranceErrorPage.root.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun showDatePicker(edittext: MDTextInputEdittext, isDOB: Boolean) {

        val value = edittext.text.toString().trim()

        var localDate = if (value.isNotBlank()) {
            LocalDate.parse(value, ModelUtil.charmDisplayDateFormat())
        } else {
            LocalDate.now()
        }

        val minDate = if (isDOB) {
            LocalDate(LocalDate.now().year - 150, 1, 1).toDateTimeAtStartOfDay()
        } else {
            if (!binding.insuranceRoot.addInsuranceDOBET.text.isNullOrBlank()) {
                LocalDate.parse(
                    binding.insuranceRoot.addInsuranceDOBET.text.toString(),
                    ModelUtil.charmDisplayDateFormat()
                ).toDateTimeAtStartOfDay()
            } else {
                LocalDate(LocalDate.now().year - 150, 1, 1).toDateTimeAtStartOfDay()
            }
        }

        val maxDate = if (isDOB) {
            DateTime.now()
        } else {
            LocalDate(LocalDate.now().year + 150, 1, 1).toDateTimeAtStartOfDay()
        }

        if (!isDOB) {
            if (localDate.isBefore(minDate.toLocalDate()) || localDate.isAfter(maxDate.toLocalDate())) {
                localDate = LocalDate.now()
            }
        }

        val dialog = DatePickerDialog(
            this,
            { _, yr, mnth, dt ->

                val text = LocalDate(yr, mnth + 1, dt)
                edittext.setText(text.toString(ModelUtil.charmDisplayDateFormat()))


            },
            localDate.year, localDate.monthOfYear - 1, localDate.dayOfMonth
        )




        dialog.datePicker.minDate = minDate.millis
        dialog.datePicker.maxDate = maxDate.millis


        dialog.show()
    }

    fun performSearch(inputFinishChecker: Runnable, isImmediate: Boolean = false) {
        handler.removeCallbacks(inputFinishChecker)

        val delay = if (isImmediate) {
            100L
        } else 600L

        handler.postDelayed(inputFinishChecker, delay)
    }

    override fun getViewModel(): AddInsuranceViewModel {
        return ViewModelProvider(this, viewModelProvider)[AddInsuranceViewModel::class.java]
    }
}