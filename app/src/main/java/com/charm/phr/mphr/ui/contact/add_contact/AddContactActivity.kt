package com.charm.phr.mphr.ui.contact.add_contact

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.forEach
import androidx.core.view.size
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityAddContactBinding
import com.charm.phr.mphr.databinding.ItemAddContactTemplateBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ContactDetails
import com.charm.phr.mphr.model.Pharmacy
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.gson.JsonArray
import java.util.regex.Pattern
import javax.inject.Inject

class AddContactActivity : MDBaseActivity() {

    private lateinit var addContactViewModel: AddContactViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityAddContactBinding

    private lateinit var backPressAlertDialogBuilder: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addContactViewModel = ViewModelProvider(this, viewModelFactory)[AddContactViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.addContactToolbar)
        UIUtil.applyViewInsertForBottomView(binding.addContactMainLayout)

        createAddContactItem(intent.getStringExtra(ModelUtil.documentShareAddContactEmail), intent.getStringExtra(ModelUtil.documentShareAddContactFullName))
        setOnClickListener()
        setBackPressDialogue()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun onBackClick() {
        if (addContactViewModel.isValueChanged) {
            backPressAlertDialogBuilder.show()
        } else {
            onBackPressed()
        }
    }

    fun setOnClickListener() {
        binding.addMoreContactTV.setOnClickListener {
            createAddContactItem("", "")
        }
        binding.addContactBtn.setOnClickListener {
            if (binding.addContactLinearLayout.size > 0) {
                validateData()
            }
        }
        binding.addContactBackBtn.setOnClickListener {
            onBackClick()
        }
    }

    private fun setBackPressDialogue() {
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    onBackPressed()
                }

                DialogInterface.BUTTON_NEGATIVE -> {

                }
            }
        }
        backPressAlertDialogBuilder = AlertDialog.Builder(this)
        backPressAlertDialogBuilder.setMessage(R.string.discard_changes_confirmation_string).setPositiveButton(R.string.yes_str, dialogClickListener)
            .setNegativeButton(R.string.no_str, dialogClickListener)
    }

    private fun saveContacts() {
        renderData()
        progressDialog.show()
        progressDialog.setMessage(R.string.adding_data_str)
        addContactViewModel.saveContacts().observe(this) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is JsonArray) {
                    setContactId(resp)
                    val contactJson = MDJsonBuilder.formPharmacyObjList(addContactViewModel.contactList)
                    val data = Intent()
                    data.setData(Uri.parse(contactJson.toString()))
                    setResult(0, data)
                    UIUtil.showToast(applicationContext, getString(R.string.added_successfully_str), Toast.LENGTH_LONG)
                    onBackPressed()

                } else if (resp is APIResponseError) {
                    saveContactsOnFail()
                }
            }
        }
    }

    private fun saveContactsOnFail() {
        UIUtil.showRetryAndCancelDialog(this, R.string.requestFailedTryAgain, R.string.retry_str, false) { isRetry ->
            if (isRetry) {
                saveContacts()
            } else {
                onBackPressed()
            }
        }
    }

    fun validateData() {
        binding.addContactLinearLayout.forEach {
            val view = ItemAddContactTemplateBinding.bind(it)
            val emailId = view.addContactEmailET.text.toString()
            val fullName = view.addContactFullNameET.text.toString()
            if (fullName.isBlank()) {
                view.addContactFullNameET.requestFocus()
                UIUtil.showToast(this, (R.string.error_empty_full_name), Toast.LENGTH_LONG)
                return
            }

            if (!ModelUtil.hasAlpha(fullName)) {
                view.addContactFullNameET.requestFocus()
                UIUtil.showToast(this, (R.string.error_invalid_full_name), Toast.LENGTH_LONG)
                return
            }

            if (emailId.isBlank()) {
                view.addContactEmailET.requestFocus()
                UIUtil.showToast(this, (R.string.error_empty_physician_email), Toast.LENGTH_LONG)
                return
            }
            val pattern = Pattern.compile(ModelUtil.emailRegex)
            val matcher = pattern.matcher(emailId)
            if (!matcher.matches()) {
                view.addContactEmailET.requestFocus()
                UIUtil.showToast(this, (R.string.error_invalid_email), Toast.LENGTH_LONG)
                return
            }

        }
        saveContacts()
    }

    private fun renderData() {
        addContactViewModel.contactList.clear()
        binding.addContactLinearLayout.forEach {
            val view = ItemAddContactTemplateBinding.bind(it)
            val newContact = Pharmacy(
                pharmacyName = view.addContactFullNameET.text.toString(),
                contactDetails = ContactDetails(emailId = view.addContactEmailET.text.toString())
            )
            addContactViewModel.contactList.add(newContact)
        }
    }

    private fun setContactId(contactIdArr: JsonArray) {
        try {
            contactIdArr.forEachIndexed { index, element ->
                if (index < addContactViewModel.contactList.size) {
                    val contactId = element?.asString
                    if (!contactId.isNullOrBlank()) {
                        addContactViewModel.contactList[index].patientPharmaMapId = contactId
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("AddContactActivity", "Error setting contact IDs:", e)
        }
    }

    private fun createAddContactItem(email: String?, name: String?) {
        val itemAddContactTemplateBinding = ItemAddContactTemplateBinding.inflate(layoutInflater, binding.addContactLinearLayout, false)
        itemAddContactTemplateBinding.addContactEmailET.setText(email)
        itemAddContactTemplateBinding.addContactFullNameET.setText(name)
        itemAddContactTemplateBinding.addContactRemove.setOnClickListener {
            if (binding.addContactLinearLayout.size > 1) {
                binding.addContactLinearLayout.removeView(itemAddContactTemplateBinding.root)
            } else {
                UIUtil.showToast(applicationContext, getString(R.string.add_contact_contrain), Toast.LENGTH_LONG)
            }
        }
        itemAddContactTemplateBinding.addContactEmailET.addTextChangedListener {
            addContactViewModel.isValueChanged = true
        }
        itemAddContactTemplateBinding.addContactFullNameET.addTextChangedListener {
            addContactViewModel.isValueChanged = true
        }
        binding.addContactLinearLayout.addView(itemAddContactTemplateBinding.root)
    }
}