package com.charm.phr.mphr.ui.physician.add_physician

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAddPhysicianBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ContactDetails
import com.charm.phr.mphr.model.Pharmacy
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.UIUtil
import com.google.gson.JsonObject
import javax.inject.Inject

class AddPhysicianActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var addPhysicianViewModel: AddPhysicianViewModel

    private lateinit var backPressAlertDialogBuilder : AlertDialog.Builder

    lateinit var binding: ActivityAddPhysicianBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPhysicianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addPhysicianViewModel = ViewModelProvider(this , viewModelFactory).get(AddPhysicianViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.addPhysicianToolbar)
        UIUtil.applyViewInsertForBottomView(binding.addPhysicianRootLayout)

        addPhysicianViewModel.toEdit = intent.getBooleanExtra(ModelUtil.physicianToEditIntent , false)

        addPhysicianViewModel.isHealthPassport = intent.getBooleanExtra(ModelUtil.isHealthPassportIntent , false)

        if (addPhysicianViewModel.toEdit){

            val problemListStr = intent.getStringExtra(ModelUtil.physicianEditDataIntent) ?: ""

            binding.addPhysicianToolbarText.setText(R.string.physician_edit_str)
            binding.addPhysicianBtn.setText(R.string.update_str)

            addPhysicianViewModel.physicianToBeEditted = MDParsingHelper.formContactObj(
                    problemListStr.parseStringAsJsonObject() ?: JsonObject(), ModelUtil.contactTypeDoctor)


            binding.addPhysicianNameET.setText(addPhysicianViewModel.physicianToBeEditted.pharmacyName)
            binding.addPhysicianMobileET.setText(addPhysicianViewModel.physicianToBeEditted.contactDetails?.mobile)

        }


        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
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
        backPressAlertDialogBuilder.setMessage(R.string.discard_changes_confirmation_string).setPositiveButton(R.string.yes_str, dialogClickListener).setNegativeButton(R.string.no_str, dialogClickListener)



        binding.addPhysicianBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addPhysicianPracticeLayout.visibility = View.GONE


      /*  if (addPhysicianViewModel.toEdit || addPhysicianViewModel.isHealthPassport){
            addPhysicianPracticeLayout.visibility = View.GONE
            getCommonData()
        }
        else{
            val selectedPatient = authManager.selectedPatientLive.value

            if (selectedPatient != null) {

                val practiceList = selectedPatient.practiceList.filter { it.isActive }

                if (practiceList.size == 1) {
                    addPhysicianViewModel.practiceId = practiceList.firstOrNull()?.practiceId ?: ""
                    getCommonData()
                    addPhysicianPracticeLayout.visibility = View.GONE
                } else {
                    addPhysicianPracticeLayout.visibility = View.VISIBLE
                    addPhysicianPracticeET.setItems(practiceList.mapNotNull { it.practiceName },
                            this)
                    addPhysicianPracticeET.setAnchorView(addPhysicianPracticeLayout)
                    addPhysicianPracticeET.setOnItemClickListener { position, str ->
                        addPhysicianViewModel.practiceId =
                                selectedPatient.practiceList[position].practiceId ?: ""
                        getCommonData(true)
                    }
                }

            }
        }*/


        binding.addPhysicianBtn.setOnClickListener  {
            val procedure = validateData()

            if (procedure != null){

                progressDialog.show()

                addPhysicianViewModel.saveContacts(procedure).observe(this , Observer { resp ->

                    if (resp != null){
                        progressDialog.dismiss()

                        if (resp is Boolean){
                            setResult(Activity.RESULT_OK)
                            super.onBackPressed()
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

                        }
                        else if (resp is APIResponseError){

                            if (!addPhysicianViewModel.toEdit){

                                    UIUtil.showToast(this , R.string.error_add_physician_req_failed , Toast.LENGTH_LONG)


                            }
                            else{
                                UIUtil.showToast(this , R.string.error_edit_physician_req_failed , Toast.LENGTH_LONG)

                            }
                        }
                    }
                })
            }
        }
    }

    override fun onBackPressed() {

        if (addPhysicianViewModel.toEdit){
            if (checkDataIsEditted()){
                backPressAlertDialogBuilder.show()
            }
            else{
                super.onBackPressed()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
        else{

            if (checkDataIsAvailable()){
                backPressAlertDialogBuilder.show()
            }
            else{
                super.onBackPressed()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
    }


    private fun validateData(): Pharmacy?{

        val physicianName = binding.addPhysicianNameET.text.toString()
        val mobile = binding.addPhysicianMobileET.text.toString()


        if (physicianName.isBlank()){
            UIUtil.showToast(this , (R.string.error_empty_physician_name) , Toast.LENGTH_LONG)
            return null
        }

        if (!ModelUtil.hasAlpha(physicianName)){
            UIUtil.showToast(this , (R.string.error_invalid_physician_name) , Toast.LENGTH_LONG)
            return null
        }

        if (mobile.isBlank()){
            UIUtil.showToast(this , (R.string.error_empty_physician_mobile) , Toast.LENGTH_LONG)
            return null
        }



        val pharmacy = if(addPhysicianViewModel.toEdit){
            addPhysicianViewModel.physicianToBeEditted
        }
        else{
            Pharmacy( contactDetails =  ContactDetails() , contactType = ModelUtil.contactTypeDoctor)
        }

        pharmacy.pharmacyName = physicianName
        pharmacy.contactDetails?.mobile = mobile
        pharmacy.isHealthPassport = addPhysicianViewModel.isHealthPassport

        return pharmacy
    }

    private fun checkDataIsAvailable(): Boolean {



            if (binding.addPhysicianNameET.text.toString().isNotBlank()) {
                return true
            }

            if (binding.addPhysicianMobileET.text.toString().isNotBlank()) {
                return true
            }

        return false
    }

    private fun checkDataIsEditted(): Boolean {



        val physician = addPhysicianViewModel.physicianToBeEditted

        if (binding.addPhysicianNameET.text.toString().trim() != physician.pharmacyName?.trim() ?: "") {
            return true
        }

        if (binding.addPhysicianMobileET.text.toString().trim() != physician.contactDetails?.mobile?.trim() ?: "") {
            return true
        }


        return false
    }
}
