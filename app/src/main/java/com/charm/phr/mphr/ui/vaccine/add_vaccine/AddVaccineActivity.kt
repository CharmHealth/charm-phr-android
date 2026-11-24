package com.charm.phr.mphr.ui.vaccine.add_vaccine

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAddVaccineBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ImmunizationList
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.PatientImmunizationCommonData
import com.charm.phr.mphr.model.PatientImmunizationHistory
import com.charm.phr.mphr.model.PatientImmunizationMap
import com.charm.phr.mphr.model.VaccineImmunizationPOJO
import com.charm.phr.mphr.ui.appointment.book_appointment.BookAppointmentMemberListAdapter
import com.charm.phr.mphr.ui.components.MDAutoCompleteTextViewAdapter
import com.charm.phr.mphr.util.MDAddBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.UIUtil
import com.google.gson.JsonObject
import org.joda.time.DateTime
import org.joda.time.LocalDate
import javax.inject.Inject

class AddVaccineActivity : MDAddBaseActivity<AddVaccineViewModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var vaccineNameAdapter: MDAutoCompleteTextViewAdapter

    lateinit var memberNameAdapter: BookAppointmentMemberListAdapter

    lateinit var binding: ActivityAddVaccineBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddVaccineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addViewModel.toEdit = intent.getBooleanExtra(ModelUtil.vaccineToEditIntent , false)

        UIUtil.applyViewInsertForToolbar(binding.addVaccineToolbar)
        UIUtil.applyViewInsertForBottomView(binding.addVaccineRootLayout)

        UIUtil.makeTextViewMandatory(binding.addVaccineNameLayout)

        if (addViewModel.toEdit){

            val procedureStr = intent.getStringExtra(ModelUtil.vaccineEditDataIntent) ?: ""

            binding.addVaccineToolbarText.setText(R.string.vaccine_edit_str)
            binding.addVaccineBtn.setText(R.string.update_str)

            addViewModel.vaccineToBeEditted = MDParsingHelper.formVaccineObj(
                procedureStr.parseStringAsJsonObject()?: JsonObject()
            )


            binding.addVaccineNameET.inputType = InputType.TYPE_NULL

            binding.addVaccineNameET.isEnabled = false
            addViewModel.practiceId = intent.getStringExtra(ModelUtil.practiceIdIntent) ?: ""
        }
        else {
            vaccineNameAdapter = MDAutoCompleteTextViewAdapter(this , arrayListOf())

            binding.addVaccineNameET.setAdapter(vaccineNameAdapter)
        }

        memberNameAdapter = BookAppointmentMemberListAdapter(this , arrayListOf())

        binding.addVaccineMemberNameET.setAdapter(memberNameAdapter)

        if (!addViewModel.toEdit) {

            val practiceList =  ModelUtil.getPHREntryEnabledPractices(authManager , ModelUtil.vaccineModelId)
            if (practiceList.size == 1) {
                addViewModel.practiceId = practiceList.firstOrNull()?.practiceId ?: ""
                getCommonData()
                binding.addVaccinePracticeLayout.visibility = View.GONE
            }
            else {
                binding.addVaccineProgress.root.visibility = View.GONE
                binding.addVaccinePracticeLayout.visibility = View.VISIBLE
                binding.addVaccinePracticeET.setItems(practiceList.mapNotNull { it.practiceName }, this)
                binding.addVaccinePracticeET.setOnItemClickListener { position, str ->
                    addViewModel.practiceId = practiceList[position].practiceId ?: ""
                    getCommonData(true)
                }
            }
        }
        else{
            binding.addVaccinePracticeLayout.visibility = View.GONE
            getCommonData()
        }


        memberNameAdapter.setMemberSelectListener(object : BookAppointmentMemberListAdapter.MemberSelectListener{
            override fun onMemberSelect(member: Member?) {

                if (member !=  null) {
                    imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                    binding.addVaccineToolbarText.requestFocus()
                }
            }
        })



        binding.addVaccineDateET.inputType = InputType.TYPE_NULL

        binding.addVaccineBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addVaccineDateET.setOnClickListener{
            showDatePicker()
        }

        binding.addVaccineDateET.setOnFocusChangeListener { view, isFocus  ->
            if (isFocus){
                showDatePicker()
            }
        }



        binding.addVaccineErrorPage.errorPageTryAgainBtn.setOnClickListener {
            getCommonData()
        }

        /*addVaccineMemberNameET.setOnFocusChangeListener { view, hasFocus ->

            if (hasFocus){
                addVaccineMainLayout.scrollTo(0 , 50)
            }
        }
*/
        binding.addVaccineBtn.setOnClickListener  {
            val vaccine = validateData()

            if (vaccine != null){

                progressDialog.show()
                val responseLive = if (!addViewModel.toEdit){
                    addViewModel.addVaccine(vaccine)
                }
                else{
                    addViewModel.editVaccine(vaccine)
                }

                responseLive.observe(this , Observer { resp ->

                    if (resp != null){
                        progressDialog.dismiss()

                        if (resp is Boolean){
                            addViewModel.isDataAdded = true
                            setResult(Activity.RESULT_OK)
                            super.onBackPressed()
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

                        }
                        else if (resp is APIResponseError){

                            if (!addViewModel.toEdit){
                                UIUtil.showToast(this , R.string.error_add_vaccine_req_failed , Toast.LENGTH_LONG)
                            }
                            else{
                                UIUtil.showToast(this , R.string.error_edit_vaccine_req_failed , Toast.LENGTH_LONG)
                            }
                        }
                    }
                })
            }
        }
    }

    private fun getCommonData(showDialog: Boolean = false){


        if (showDialog){
            progressDialog.show()
        }
        else{
            binding.addVaccineProgress.root.visibility = View.VISIBLE
            binding.addVaccineMainLayout.visibility = View.GONE
            binding.addVaccineErrorPage.root.visibility = View.GONE
        }

        addViewModel.getVaccineCommonData().observe(this, Observer { resp ->

            if (resp != null) {
                if (progressDialog.isShowing){
                    progressDialog.dismiss()
                }
                binding.addVaccineProgress.root.visibility = View.GONE
                if (resp is PatientImmunizationCommonData) {
                    binding.addVaccineMainLayout.visibility = View.VISIBLE
                    binding.addVaccineDateET.setText(DateTime.now().toString(ModelUtil.charmDisplayDateFormat()))

                    memberNameAdapter.setMemberList(resp.providerList)

                    binding.addVaccineAdministeredListET.setItems(resp.administeredList , this)
                    binding.addVaccineAdministeredListET.setAnchorView(binding.addVaccineAdministeredListLayout)

                    if (addViewModel.toEdit){

                        val immuneMap = addViewModel.vaccineToBeEditted.patientImmunizationMap
                        binding.addVaccineNameET.setText(immuneMap?.immunizationList?.immunizationName)

                        if (immuneMap?.immunizationList?.immunizationType == ModelUtil.vaccineTypeAdolescent){
                            binding.addVaccineTypeRadioGrp.check(R.id.addVaccineTypeAdolescent)
                        }
                        else{
                            binding.addVaccineTypeRadioGrp.check(R.id.addVaccineTypeChildhood)
                        }

                        binding.addVaccineTypeAdolescent.isEnabled = false
                        binding.addVaccineTypeChildhood.isEnabled = false

                        binding.addVaccineAdministeredListET.setText(immuneMap?.patientImmunizationHistory?.howAdministered)
                        binding.addVaccineMemberNameET.setText(immuneMap?.patientImmunizationHistory?.givenBy)
                        binding.addVaccineManufacturerET.setText(immuneMap?.patientImmunizationHistory?.manufacturer)
                        binding.addVaccineCommentsET.setText(immuneMap?.patientImmunizationHistory?.comments)
                        binding.addVaccineDateET.setText(immuneMap?.patientImmunizationHistory?.vaccinesOn?.toString(ModelUtil.charmDisplayDateFormat()))


                    }
                    else{
                        vaccineNameAdapter.setItemList(resp.searchString)
                    }



                } else if (resp is APIResponseError) {
                    binding.addVaccineErrorPage.root.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun validateData():VaccineImmunizationPOJO?{

        val vaccineName = binding.addVaccineNameET.text.toString()

        if (isTextInputLayoutBlank(binding.addVaccineNameLayout , binding.addVaccineScrollView ,R.string.error_empty_vaccine_name )){
            return null
        }



        if (!addViewModel.toEdit){
            if (isTextInputLayoutBlank(binding.addVaccineNameLayout , binding.addVaccineScrollView ,R.string.error_invalid_vaccine_name , true )){
                return null
            }
        }

       /* if (vaccineName.isBlank()){
            UIUtil.showToast(this , (R.string.error_empty_vaccine_name) , Toast.LENGTH_LONG)
            return null
        }

        if (!ModelUtil.hasAlpha(vaccineName) && !addViewModel.toEdit){
            UIUtil.showToast(this , R.string.error_invalid_vaccine_name , Toast.LENGTH_LONG)
            return null
        }
*/

        val vaccineComments = binding.addVaccineCommentsET.text.toString()

        if (isTextInputLayoutBlank(binding.addVaccineCommentsLayout , binding.addVaccineScrollView ,R.string.error_invalid_vaccine_comments , true )){
            return null
        }

       /* if (!ModelUtil.hasAlpha(vaccineComments)){
            UIUtil.showToast(this , R.string.error_invalid_vaccine_comments , Toast.LENGTH_LONG)
            return null
        }
*/
        val memberName = binding.addVaccineMemberNameET.text.toString()

        if (isTextInputLayoutBlank(binding.addVaccineMemberNameLayout , binding.addVaccineScrollView ,R.string.error_invalid_vaccine_member_name , true )){
            return null
        }
       /* if (!ModelUtil.hasAlpha(memberName)){
            UIUtil.showToast(this , R.string.error_invalid_vaccine_member_name , Toast.LENGTH_LONG)
            return null
        }*/


        val vaccineImmunizationPOJO = if(addViewModel.toEdit){
            addViewModel.vaccineToBeEditted
        }
        else{
            VaccineImmunizationPOJO()
        }


        if (!addViewModel.toEdit) {


            val immune = ImmunizationList()
            immune.immunizationName = vaccineName
            immune.immunizationType = if (binding.addVaccineTypeRadioGrp.checkedRadioButtonId == R.id.addVaccineTypeChildhood){
                ModelUtil.vaccineTypeChildhood
            }
            else{
                ModelUtil.vaccineTypeAdolescent
            }

            val patientImmunizationMap = PatientImmunizationMap()
            patientImmunizationMap.immunizationList = immune
            patientImmunizationMap.patientImmunizationHistory = PatientImmunizationHistory()
            vaccineImmunizationPOJO.patientImmunizationMap = patientImmunizationMap
        }

        vaccineImmunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.vaccinesOn = DateTime.parse(binding.addVaccineDateET.text.toString() , ModelUtil.charmDisplayDateFormat())
        vaccineImmunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.comments = vaccineComments
        vaccineImmunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.howAdministered = binding.addVaccineAdministeredListET.text.toString()
        vaccineImmunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.manufacturer = binding.addVaccineManufacturerET.text.toString()
        vaccineImmunizationPOJO.patientImmunizationMap?.patientImmunizationHistory?.givenBy = binding.addVaccineMemberNameET.text.toString()


        return vaccineImmunizationPOJO
    }

    override fun checkDataIsAvailable(): Boolean {


        if (binding.addVaccineAdministeredListET.items != null && binding.addVaccineAdministeredListET.items.isNotEmpty()) {

            if (binding.addVaccineTypeRadioGrp.checkedRadioButtonId == R.id.addVaccineTypeAdolescent) {
                return true
            }

            if (binding.addVaccineNameET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addVaccineManufacturerET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addVaccineDateET.text.toString() != DateTime.now().toString(ModelUtil.charmDisplayDateFormat())) {
                return true
            }

            if (binding.addVaccineAdministeredListET.text.toString() != binding.addVaccineAdministeredListET.items[0]) {
                return true
            }

            if (binding.addVaccineMemberNameET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addVaccineCommentsET.text.toString().isNotBlank()) {
                return true
            }
        }
       return false
    }

    override fun checkDataIsEditted(): Boolean {

        if (binding.addVaccineNameET.text.toString().isNotBlank()) {

            val immuneMap = addViewModel.vaccineToBeEditted.patientImmunizationMap

            if (immuneMap != null) {

                if (binding.addVaccineManufacturerET.text.toString().trim() != immuneMap.patientImmunizationHistory?.manufacturer?.trim() ?: "") {
                    return true
                }

                if (binding.addVaccineDateET.text.toString() != immuneMap.patientImmunizationHistory?.vaccinesOn?.toString(ModelUtil.charmDisplayDateFormat())) {
                    return true
                }

                if (binding.addVaccineAdministeredListET.text.toString() != immuneMap.patientImmunizationHistory?.howAdministered?.trim() ?: "") {
                    return true
                }

                if (binding.addVaccineMemberNameET.text.toString().trim() != immuneMap.patientImmunizationHistory?.givenBy?.trim() ?: "") {
                    return true
                }

                if (binding.addVaccineCommentsET.text.toString().trim() != immuneMap.patientImmunizationHistory?.comments?.trim() ?: "") {
                    return true
                }
            }
        }
        return false
    }

    private fun showDatePicker() {

        val value = binding.addVaccineDateET.text.toString().trim()

        val localDate = if (value.isNotBlank()) {
            LocalDate.parse(value, ModelUtil.charmDisplayDateFormat())
        } else {
            LocalDate.now()
        }

        val dialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, yr, mnth, dt ->

                val text = LocalDate(yr, mnth + 1, dt)
                binding.addVaccineDateET.setText(text.toString(ModelUtil.charmDisplayDateFormat()))

            },
            localDate.year, localDate.monthOfYear - 1, localDate.dayOfMonth)

        dialog.datePicker.minDate = LocalDate(LocalDate.now().year - 150, 1, 1).toDateTimeAtStartOfDay().millis
        dialog.datePicker.maxDate = LocalDate(LocalDate.now().year + 150, 1, 1).toDateTimeAtStartOfDay().millis

        dialog.show()
    }


    override fun getViewModel(): AddVaccineViewModel {
        return ViewModelProvider(this , viewModelFactory).get(AddVaccineViewModel::class.java)
    }
}
