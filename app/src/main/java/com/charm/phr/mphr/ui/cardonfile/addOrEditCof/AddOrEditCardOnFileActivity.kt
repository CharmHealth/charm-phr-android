package com.charm.phr.mphr.ui.cardonfile.addOrEditCof

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentAddOrEditCofBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.MerchantAccountInfo
import com.charm.phr.mphr.model.PaymentGatewayToken
import com.charm.phr.mphr.model.PaymentInfo
import com.charm.phr.mphr.model.PostalAddress
import com.charm.phr.mphr.model.TransactionInfo
import com.charm.phr.mphr.ui.components.MDAutoCompleteTextViewAdapter
import com.charm.phr.mphr.util.MDAddBaseActivity
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.getString
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import javax.inject.Inject

class AddOrEditCardOnFileActivity : MDAddBaseActivity<AddOrEditCofModel>() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: FragmentAddOrEditCofBinding
    private lateinit var countryAdapter: MDAutoCompleteTextViewAdapter

    val TAG = "AddOrEditCardOnFileActivity"

    //region Life cycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAddOrEditCofBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UIUtil.applyViewInsertForToolbar(binding.addCofToolbar)
        UIUtil.applyViewInsertForBottomView(binding.addCofMainLayout)

        addViewModel.toEdit = intent.getBooleanExtra(ModelUtil.cardEditIntent, false)
        loadDefaultData()
        if (addViewModel.toEdit) {
            val editDataStr = intent.getStringExtra(ModelUtil.cardEditDataIntent)
            addViewModel.selectedPracticePosition = intent.getIntExtra(ModelUtil.cardEditPositionIntent, 0)
            val editDataJson = editDataStr?.parseStringAsJsonObject()
            renderData(editDataJson)
        }
        setTextChangeListener()
        setOnClickListener()
        onBackPressAlert()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    //endregion

    //region UI Logic
    private fun loadDefaultData() {

        // load and set country and PostalAddress
        val countryStr = intent.getStringExtra(ModelUtil.cardEditCountryIntent)
        val countryJson = countryStr?.parseStringAsJsonObject()
        countryJson?.let { addViewModel.countryList = MDParsingHelper.getCountryList(it) }
        val addressStr = intent.getStringExtra(ModelUtil.cardEditAddressIntent)
        val addressJson = addressStr?.parseStringAsJsonObject()
        addViewModel.selectedPaymentGatewayToken = PaymentGatewayToken()
        addressJson?.let { addViewModel.selectedPaymentGatewayToken?.postalAddress = MDParsingHelper.getPostalAddressObject(addressJson) }
        val country = addViewModel.countryList.firstOrNull {
            it.key.equals(addViewModel.selectedPaymentGatewayToken?.postalAddress?.country, true)
        }?.name
        binding.addCofCountryET.setText(country)
        binding.addCofLine1ET.setText(addViewModel.selectedPaymentGatewayToken?.postalAddress?.line1)
        binding.addCofLine2ET.setText(addViewModel.selectedPaymentGatewayToken?.postalAddress?.line2)
        binding.addCofCityET.setText(addViewModel.selectedPaymentGatewayToken?.postalAddress?.city)
        binding.addCofStateET.setText(addViewModel.selectedPaymentGatewayToken?.postalAddress?.state)
        binding.addCofPinCodeET.setText(addViewModel.selectedPaymentGatewayToken?.postalAddress?.postalCode)

        countryAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())

        binding.addCofCountryET.setAdapter(countryAdapter)

        countryAdapter.setItemList(addViewModel.countryList.map { it.name })

        //Load Card Expiry Month and Year
        binding.addCofCardExpMonthET.setItems(ModelUtil.createNumberList(endNumber = 12), this)

        val currentDate = DateTime.now()
        val currentYear = currentDate.year

        binding.addCofCardExpYearET.setItems(ModelUtil.createNumberList(currentYear, currentYear + 20), this)

        //load and set practice value
        val selectedPatient = authManager.selectedPatientLive.value
        if (selectedPatient != null) {
            val practiceList = ModelUtil.getBluefinEnabledPractice(selectedPatient)
            if (!practiceList.isNullOrEmpty()) {
                addViewModel.bluefinEnabledPractices = practiceList
                val practiceNameList = arrayListOf<String>()
                addViewModel.bluefinEnabledPractices.forEach {
                    practiceNameList.add(it.practiceName ?: "")
                }
                binding.addCofPracticeET.setItems(practiceNameList, this)
                if (addViewModel.bluefinEnabledPractices.size == 1) {
                    binding.addCofPracticeET.setText(practiceNameList[0])
                    binding.addCofPracticeET.isEnabled = false
                    binding.addCofPracticeLayout.setShowLine(false)
                    binding.addCofPracticeLayout.setShowDropDown(false)
                    addViewModel.selectedPaymentGatewayToken?.practiceId = addViewModel.bluefinEnabledPractices[0].practiceId
                }
            }
        }

    }

    private fun renderData(editDataJson: JsonObject?) {
        val practiceId = editDataJson?.getString("practice_id")
        addViewModel.selectedPaymentGatewayToken = editDataJson?.let { MDParsingHelper.getPaymentGatewayToken(it, practiceId ?: "") }
        renderEditView()
    }

    private fun renderEditView() {
        binding.addCofPracticeTV.visibility = View.GONE
        binding.addCofPracticeLayout.visibility = View.GONE
        binding.addCofCardNameET.visibility = View.GONE
        binding.addCofCardNumberET.visibility = View.GONE
        binding.addCofCardExpiryLayout.visibility = View.GONE
        binding.addCofCardCvvET.visibility = View.GONE
        binding.addCofBtn.visibility = View.GONE
        binding.updateCofBtn.visibility = View.VISIBLE
        binding.addCofToolbarText.setText(R.string.edit_card_str)

        binding.addCofCardNickNameET.setText(addViewModel.selectedPaymentGatewayToken?.cardNickName)
        if (addViewModel.selectedPaymentGatewayToken?.isPrimaryCard == true) {
            binding.addCofPrimaryCardCheckbox.isChecked = true
        }

        val country = addViewModel.countryList.firstOrNull {
            it.key.equals(addViewModel.selectedPaymentGatewayToken?.postalAddress?.country, true)
        }?.name
        binding.addCofCountryET.setText(country)
        binding.addCofLine1ET.setText(addViewModel.selectedPaymentGatewayToken?.postalAddress?.line1)
        binding.addCofLine2ET.setText(addViewModel.selectedPaymentGatewayToken?.postalAddress?.line2)
        binding.addCofCityET.setText(addViewModel.selectedPaymentGatewayToken?.postalAddress?.city)
        binding.addCofStateET.setText(addViewModel.selectedPaymentGatewayToken?.postalAddress?.state)
        binding.addCofPinCodeET.setText(addViewModel.selectedPaymentGatewayToken?.postalAddress?.postalCode)
    }

    private fun onBackPressAlert() {
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
        backPressAlertDialogBuilder.setMessage(R.string.discard_changes_confirmation_string)
            .setPositiveButton(R.string.yes_str, dialogClickListener)
            .setNegativeButton(R.string.no_str, dialogClickListener)
    }
    //endregion

    //region Listener block
    private fun setTextChangeListener() {
        binding.addCofCardCvvET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                addViewModel.isDataEdited = true
            }

        })
        binding.addCofCardNameET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                addViewModel.isDataEdited = true
            }

        })
        binding.addCofCardNickNameET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                addViewModel.isDataEdited = true
            }

        })
        binding.addCofCardNumberET.addTextChangedListener(object : TextWatcher {
            private val totalTxtCount = 19 // size of pattern 0000 0000 0000 0000

            /*
                        private val totalDigits = 16 // max numbers of digits in pattern: 0000 x 4
            */
            private val dividerModulo = 5 //  means divider position is every 5th symbol beginning with 1
            private val dividerPosition = dividerModulo - 1 // means divider position is every 4th symbol beginning with 0
            private val divider = " "

            override fun afterTextChanged(editable: Editable) {
                if (!isInputCorrect(editable, totalTxtCount, dividerModulo, divider)) {
                    binding.addCofCardNumberET.setText(buildCorrectString(getDigitArray(editable), dividerPosition, divider))
                    binding.addCofCardNumberET.setSelection(binding.addCofCardNumberET.text?.length ?: 0)
                    addViewModel.isDataEdited = true
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            private fun isInputCorrect(editable: Editable, totalSymbols: Int, dividerModulo: Int, divider: String): Boolean {
                var isCorrect = editable.length <= totalSymbols
                for (index in editable.indices) {
                    isCorrect = if (index > 0 && (((index + 1) % dividerModulo) == 0)) {
                        isCorrect && divider == editable.toString()[index].toString()
                    } else {
                        isCorrect && Character.isDigit(editable.toString()[index])
                    }
                }

                if (editable.isNotEmpty()) {
                    isCorrect = isCorrect && divider != editable.toString()[editable.length - 1].toString()
                }




                return isCorrect
            }

            private fun buildCorrectString(digits: ArrayList<Char>, dividerPosition: Int, divider: String): String {
                val formatted: StringBuilder = StringBuilder()

                for (index in 0 until digits.size) {
                    formatted.append(digits[index])
                    if ((index > 0) && (index < (digits.size - 1)) && (((index + 1) % dividerPosition) == 0)) {
                        formatted.append(divider)
                    }

                }
                return formatted.toString().trim()
            }


            private fun getDigitArray(s: Editable): ArrayList<Char> {
                val digits = arrayListOf<Char>()
                for (element in s) {
                    if (Character.isDigit(element)) {
                        digits.add(element)
                    }
                }

                return digits
            }
        })

        binding.addCofCardNumberET.setOnEditorActionListener { textView, imeOption, _ ->

            if (imeOption == EditorInfo.IME_ACTION_NEXT) {
                imm.hideSoftInputFromWindow(textView.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
                handler.postDelayed({
                    binding.addCofCardExpMonthET.performClick()
                }, 200)
            }
            return@setOnEditorActionListener false
        }

        binding.addCofCountryET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                if (editable.toString().trim() != "") {
                    val textInputLayout = (binding.addCofCountryET.parent as ViewGroup).parent as TextInputLayout
                    if (binding.addCofCountryET.isGravityCenter) {
                        TransitionManager.beginDelayedTransition(binding.addCofCountryET.parent as ViewGroup)
                        binding.addCofCountryET.gravity = Gravity.BOTTOM
                        textInputLayout.setPadding(
                            binding.addCofCountryET.edittextPadding,
                            binding.addCofCountryET.edittextPadding,
                            binding.addCofCountryET.edittextPadding,
                            binding.addCofCountryET.edittextPadding
                        )
                        binding.addCofCountryET.isGravityCenter = false
                    }
                }
                addViewModel.isDataEdited = true
                binding.addCofStateET.setText("")
            }

        })

        binding.addCofStateET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                addViewModel.isDataEdited = true
            }

        })

        binding.addCofPinCodeET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                addViewModel.isDataEdited = true
            }

        })

        binding.addCofCityET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                addViewModel.isDataEdited = true
            }

        })

        binding.addCofLine2ET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                addViewModel.isDataEdited = true
            }

        })

        binding.addCofLine1ET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                addViewModel.isDataEdited = true
            }

        })

    }

    fun setOnClickListener() {
        binding.addCofPracticeET.setOnItemClickListener { position, _ ->
            addViewModel.isDataEdited = true
            addViewModel.selectedPaymentGatewayToken?.practiceId = addViewModel.bluefinEnabledPractices[position].practiceId
        }
        binding.addCofCardExpMonthET.setOnItemClickListener { _, str ->

            val month = str.toInt()

            val currentDate = DateTime.now()

            val currentMonth = currentDate.monthOfYear

            if (month < currentMonth) {
                val currentYear = currentDate.year
                binding.addCofCardExpYearET.setItems(ModelUtil.createNumberList(currentYear + 1, currentYear + 20), this)
            } else {
                val currentYear = currentDate.year
                binding.addCofCardExpYearET.setItems(ModelUtil.createNumberList(currentYear, currentYear + 20), this)
            }
            addViewModel.isDataEdited = true
        }

        binding.addCofCardExpYearET.setOnItemClickListener { _, str ->

            val selectedYear = str.toInt()
            val currentDate = DateTime.now()

            val currentYear = currentDate.year

            if (selectedYear == currentYear) {
                val currentMonth = currentDate.monthOfYear
                binding.addCofCardExpMonthET.setItems(ModelUtil.createNumberList(currentMonth, 12), this)
            } else {
                if (binding.addCofCardExpMonthET.items.size != 12) {
                    binding.addCofCardExpMonthET.setItems(ModelUtil.createNumberList(1, 12), this)
                }
            }
            addViewModel.isDataEdited = true
        }

        binding.addCofBackBtn.setOnClickListener {
            if (addViewModel.isDataEdited) {
                backPressAlertDialogBuilder.show()
            } else {
                onBackPressed()
            }
        }

        binding.updateCofBtn.setOnClickListener {
            updateData()
        }

        binding.addCofBtn.setOnClickListener {
            validateData()
        }
        binding.addCofPrimaryCardCheckbox.setOnClickListener {
            addViewModel.isDataEdited = true
        }
    }
    //endregion


    private fun updateData() {
        addViewModel.selectedPaymentGatewayToken?.isPrimaryCard = binding.addCofPrimaryCardCheckbox.isChecked
        addViewModel.selectedPaymentGatewayToken?.cardNickName = binding.addCofCardNickNameET.text.toString()
        val postalAddress = validateAddress()
        if (postalAddress is Boolean) {
            return
        } else {
            addViewModel.selectedPaymentGatewayToken?.postalAddress = postalAddress as PostalAddress
        }
        updateCardOnFile(addViewModel.selectedPaymentGatewayToken ?: PaymentGatewayToken())
    }

    fun validateData() {
        if (binding.addCofCardNameET.text.toString().isBlank()) {
            binding.addCofCardNameET.requestFocus()
            UIUtil.showToast(
                this,
                R.string.error_book_appointment_empty_card_holder_name,
                Toast.LENGTH_LONG
            )
            return
        }

        if (!ModelUtil.hasAlpha(binding.addCofCardNameET.text.toString())) {
            UIUtil.showToast(this, R.string.error_invalid_card_holder_name, Toast.LENGTH_LONG)
            binding.addCofCardNameET.requestFocus()
            return
        }

        if (binding.addCofCardNumberET.text.toString().isBlank()) {
            binding.addCofCardNumberET.requestFocus()
            UIUtil.showToast(
                this,
                R.string.error_book_appointment_empty_card_number,
                Toast.LENGTH_LONG
            )
            return
        }

        /* if (binding.addCofCardNumberET.text.toString().length != 19) {
             binding.addCofCardNumberET.requestFocus()
             UIUtil.showToast(
                 this,
                 R.string.error_book_appointment_invalid_card_number,
                 Toast.LENGTH_LONG
             )
             return
         }*/

        if (binding.addCofCardExpMonthET.text.toString().isBlank()) {
            binding.addCofCardExpMonthET.requestFocus()
            UIUtil.showToast(
                this,
                R.string.error_book_appointment_empty_card_expiry_month,
                Toast.LENGTH_LONG
            )
            return
        }

        if (binding.addCofCardExpYearET.text.toString().isBlank()) {
            binding.addCofCardExpYearET.requestFocus()
            UIUtil.showToast(
                this,
                R.string.error_book_appointment_empty_card_expiry_year,
                Toast.LENGTH_LONG
            )
            return
        }

        if (binding.addCofCardCvvET.text.toString().isBlank()) {
            binding.addCofCardCvvET.requestFocus()
            UIUtil.showToast(
                this,
                R.string.error_book_appointment_empty_card_cvv,
                Toast.LENGTH_LONG
            )
            return
        }

        if (binding.addCofCardCvvET.text.toString().length < 3) {
            binding.addCofCardCvvET.requestFocus()
            UIUtil.showToast(
                this,
                R.string.error_book_appointment_invalid_card_cvv,
                Toast.LENGTH_LONG
            )
            return
        }


        val paymentInfo = PaymentInfo()
        paymentInfo.cardHolderName = binding.addCofCardNameET.text.toString().trim()

        val cardNumber = binding.addCofCardNumberET.text.toString().split(" ")
            .joinToString(separator = "")

        paymentInfo.cardNumber = cardNumber
        paymentInfo.cardCvv = binding.addCofCardCvvET.text.toString()
        paymentInfo.cardExpMonth = binding.addCofCardExpMonthET.text.toString()
        paymentInfo.cardExpYear = binding.addCofCardExpYearET.text.toString()
        paymentInfo.cardNickName = binding.addCofCardNickNameET.text.toString()

        paymentInfo.transactionType = ModelUtil.appointmentTransactionTypeAuthorization
        paymentInfo.isPrimaryCard = binding.addCofPrimaryCardCheckbox.isChecked
        val postalAddress = validateAddress()
        if (postalAddress is Boolean) {
            return
        } else {
            paymentInfo.postalAddress = postalAddress as PostalAddress
        }
        getMerchantAccountsForPractice(addViewModel.selectedPaymentGatewayToken?.practiceId ?: "", paymentInfo)
    }

    private fun validateAddress(): Any {
        if (!ModelUtil.hasAlpha(binding.addCofLine1ET.text.toString())) {
            UIUtil.showToast(this, R.string.error_invalid_line_1, Toast.LENGTH_LONG)
            binding.addCofLine1ET.requestFocus()
            return false
        }

        if (!ModelUtil.hasAlpha(binding.addCofLine2ET.text.toString())) {
            UIUtil.showToast(this, R.string.error_invalid_line_2, Toast.LENGTH_LONG)
            binding.addCofLine2ET.requestFocus()
            return false
        }

        val countryName = binding.addCofCountryET.text.toString()
        val state = binding.addCofStateET.text.toString()

        val countryKey: String = addViewModel.countryList.firstOrNull { it.name.equals(countryName, true) }?.key ?: ""

        if (countryKey.isBlank()) {
            UIUtil.showToast(this, R.string.error_wrong_country, Toast.LENGTH_SHORT)
            binding.addCofCountryET.requestFocus()
            return false
        }

        if (state.isNotBlank()) {

            if (!ModelUtil.hasAlpha(binding.addCofStateET.text.toString())) {
                UIUtil.showToast(this, R.string.error_invalid_state, Toast.LENGTH_LONG)
                binding.addCofStateET.requestFocus()
                return false
            }
        }

        if (!ModelUtil.hasAlpha(binding.addCofCityET.text.toString())) {
            UIUtil.showToast(this, R.string.error_invalid_city, Toast.LENGTH_LONG)
            binding.addCofCityET.requestFocus()
            return false
        }

        val postalAddress = PostalAddress()
        postalAddress.line1 = binding.addCofLine1ET.text.toString()
        postalAddress.line2 = binding.addCofLine2ET.text.toString()
        postalAddress.city = binding.addCofCityET.text.toString()
        postalAddress.country = countryKey
        postalAddress.state = binding.addCofStateET.text.toString()
        postalAddress.postalCode = binding.addCofPinCodeET.text.toString()

        return postalAddress

    }

    @Throws(UnsupportedOperationException::class)
    private fun addCardToMerchantAccounts(paymentInfo: PaymentInfo, groupId: String) {
        progressDialog.setMessage(R.string.adding_data_str)
        progressDialog.show()
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                addViewModel.merchantAccountInfo.forEach { merchantAccountInfo ->
                    if (isActive) {
                        addCardToMerchantAccount(paymentInfo, groupId, merchantAccountInfo)
                    } else {
                        throw UnsupportedOperationException()
                    }
                }
                runOnUiThread { showResult() }
            }
        } catch (ex: TimeoutCancellationException) {
            Log.e(TAG, ex.printStackTrace().toString())
            showResult()
        } catch (ex: Exception) {
            Log.e(TAG, ex.printStackTrace().toString())
            showResult()
        }
    }

    private suspend fun addCardToMerchantAccount(paymentInfo: PaymentInfo, groupId: String, merchantAccountInfo: MerchantAccountInfo) {
        val transactionInfo = TransactionInfo()
        transactionInfo.facilityId = merchantAccountInfo.facilityId
        transactionInfo.practiceId = addViewModel.selectedPaymentGatewayToken?.practiceId
        transactionInfo.merchantAccountId = merchantAccountInfo.merchantAccountId
        transactionInfo.service = ModelUtil.paymentServiceBilling
        transactionInfo.gateway = ModelUtil.appointmentGatewayTypeBluefin
        paymentInfo.transactionInfo = transactionInfo
        saveCardDetails(paymentInfo, groupId)
    }

    private fun showResult() {
        progressDialog.dismiss()
        intent.putExtra(ModelUtil.cardEditIntent, addViewModel.toEdit)
        setResult(Activity.RESULT_OK, intent)
        if (addViewModel.merchantAccountInfo.size != addViewModel.addSuccessCount) {
            UIUtil.showToast(this, addViewModel.errorMessages ?: getString(R.string.card_add_error_msg), Toast.LENGTH_LONG)
            addViewModel.errorMessages = null
        } else {
            UIUtil.showToast(this, R.string.card_added_success_msg, Toast.LENGTH_LONG)
        }
        onBackPressed()
    }

    //region API Call
    private suspend fun saveCardDetails(paymentInfo: PaymentInfo, groupId: String) {
        val response = addViewModel.saveCardDetails(paymentInfo, groupId)
        if (response != null && response is Boolean && response) {
            addViewModel.addSuccessCount++
        } else if (response is APIResponseError) {
            if (addViewModel.errorMessages.isNullOrBlank()) {
                addViewModel.errorMessages = response.errorMsg
            }
        }
    }

    private fun updateCardOnFile(paymentGatewayToken: PaymentGatewayToken) {
        progressDialog.setMessage(R.string.update_data_str)
        progressDialog.show()
        addViewModel.updateCardOnFile(paymentGatewayToken).observe(this) {
            if (it != null) {
                progressDialog.dismiss()
                if (it is Boolean && it) {
                    val responseStr = MDJsonBuilder.formPaymentGatewayTokenObj(paymentGatewayToken)
                    intent.putExtra(ModelUtil.cardEditDataIntent, responseStr.toString())
                    intent.putExtra(ModelUtil.cardEditIntent, addViewModel.toEdit)
                    intent.putExtra(ModelUtil.cardEditPositionIntent, addViewModel.selectedPracticePosition)
                    setResult(Activity.RESULT_OK, intent)
                    UIUtil.showToast(this, R.string.card_update_success_msg, Toast.LENGTH_LONG)
                    onBackPressed()
                } else if (it is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(this, it.errorMsg ?: getString(R.string.requestFailedTryAgain), R.string.retry_str, false) { isRetry ->
                        if (isRetry) {
                            updateCardOnFile(paymentGatewayToken)
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }
        }
    }


    private fun getMerchantAccountsForPractice(practiceId: String, paymentInfo: PaymentInfo) {
        progressDialog.setMessage(R.string.loading_data)
        progressDialog.show()
        addViewModel.getMerchantAccountsForPractice(practiceId).observe(this) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is ArrayList<*>) {
                    addViewModel.merchantAccountInfo.clear()
                    addViewModel.merchantAccountInfo.addAll(resp as ArrayList<MerchantAccountInfo>)
                    groupMerchantAndPatients(paymentInfo)
                } else if (resp is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(this, resp.errorMsg ?: getString(R.string.requestFailedTryAgain), R.string.retry_str, false) { isRetry ->
                        if (isRetry) {
                            getMerchantAccountsForPractice(practiceId, paymentInfo)
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }
        }
    }

    private fun groupMerchantAndPatients(paymentInfo: PaymentInfo) {
        progressDialog.setMessage(R.string.loading_data)
        progressDialog.show()
        addViewModel.groupMerchantAndPatients().observe(this) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is String) {
                    addCardToMerchantAccounts(paymentInfo, resp)
                } else if (resp is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(this, resp.errorMsg ?: getString(R.string.requestFailedTryAgain), R.string.retry_str, false) { isRetry ->
                        if (isRetry) {
                            groupMerchantAndPatients(paymentInfo)
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }
        }
    }
    //endregion

    override fun checkDataIsAvailable(): Boolean {
        return false
    }

    override fun checkDataIsEditted(): Boolean {
        return false
    }

    override fun getViewModel(): AddOrEditCofModel {
        return ViewModelProvider(this, viewModelFactory).get(AddOrEditCofModel::class.java)
    }

}