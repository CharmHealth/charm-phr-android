package com.charm.phr.mphr.ui.components

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.DialogFragment
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.DialogBillingPaymentBinding
import com.charm.phr.mphr.model.PaymentGatewayToken
import com.charm.phr.mphr.model.PaymentInfo
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.joda.time.DateTime

class MDBillingPaymentDialog : BottomSheetDialogFragment() {


    var billingAmount = ""
    var billingCurrency = ""

    var isPartialPaymentAllowed = false

    var partialPaymentMinAmount = 0.00

    private var listener: OnPayClickListner? = null

    lateinit var imm: InputMethodManager

    val handler = Handler(Looper.getMainLooper())

    var cardOnFileList: ArrayList<PaymentGatewayToken> = arrayListOf()

    private var selectedCardDetail = PaymentGatewayToken()

    lateinit var binding: DialogBillingPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.MDBottomSheetDialog)
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        binding = DialogBillingPaymentBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)


        binding.billingPaymentAmountValueTV.text = "${billingCurrency} ${billingAmount}"

        renderView()

        if (isPartialPaymentAllowed) {
            renderPartialPaymentView()
        }

        binding.billingPaymentCardNumberET.addTextChangedListener(object : TextWatcher {
            private val totalTxtCount = 19 // size of pattern 0000 0000 0000 0000

            /*
                        private val totalDigits = 16 // max numbers of digits in pattern: 0000 x 4
            */
            private val dividerModulo = 5 //  means divider position is every 5th symbol beginning with 1
            private val dividerPosition = dividerModulo - 1 // means divider position is every 4th symbol beginning with 0
            private val divider = " "

            override fun afterTextChanged(editable: Editable) {
                if (!isInputCorrect(editable, totalTxtCount, dividerModulo, divider)) {
                    binding.billingPaymentCardNumberET.setText(buildCorrectString(getDigitArray(editable), dividerPosition, divider))
                    binding.billingPaymentCardNumberET.setSelection(binding.billingPaymentCardNumberET.text?.length ?: 0)
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

        binding.billingPaymentEditAmountValueTV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(amt: Editable?) {
               if(amt?.isBlank() == true)
               {
                   binding.billingPaymentEditAmountValueTV.setText("0.00")
               }
            }
        })

        binding.billingPaymentCardNumberET.setOnEditorActionListener { textView, imeOption, _ ->

            if (imeOption == EditorInfo.IME_ACTION_NEXT) {
                imm.hideSoftInputFromWindow(textView.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
                handler.postDelayed({
                    binding.billingPaymentCardExpMonthET.performClick()
                }, 200)
            }
            return@setOnEditorActionListener false
        }

        binding.billingPaymentCardExpMonthET.setOnItemClickListener { _, str ->

            val month = str.toInt()

            val currentDate = DateTime.now()

            val currentMonth = currentDate.monthOfYear

            if (month < currentMonth) {
                val currentYear = currentDate.year
                binding.billingPaymentCardExpYearET.setItems(ModelUtil.createNumberList(currentYear + 1, currentYear + 20), this.activity)
            } else {
                val currentYear = currentDate.year
                binding.billingPaymentCardExpYearET.setItems(ModelUtil.createNumberList(currentYear, currentYear + 20), this.activity)
            }
        }

        binding.billingPaymentCardExpYearET.setOnItemClickListener { _, str ->

            val selectedYear = str.toInt()
            val currentDate = DateTime.now()

            val currentYear = currentDate.year

            if (selectedYear == currentYear) {
                val currentMonth = currentDate.monthOfYear
                binding.billingPaymentCardExpMonthET.setItems(ModelUtil.createNumberList(currentMonth, 12), this.activity)
            } else {
                if (binding.billingPaymentCardExpMonthET.items.size != 12) {
                    binding.billingPaymentCardExpMonthET.setItems(ModelUtil.createNumberList(1, 12), this.activity)
                }
            }
        }


        binding.billingPaymentCardExpMonthET.setItems(ModelUtil.createNumberList(endNumber = 12), this.activity)

        val currentDate = DateTime.now()
        val currentYear = currentDate.year

        binding.billingPaymentCardExpYearET.setItems(ModelUtil.createNumberList(currentYear, currentYear + 20), this.activity)


        binding.billingPaymentBtn.setOnClickListener {

            val paymentInfo = validateData()
            var amountToPay = ""
            if (isPartialPaymentAllowed) {
                amountToPay = binding.billingPaymentEditAmountValueTV.text.toString()
            }
            if (paymentInfo != null) {
                listener?.onPayClick(paymentInfo, amountToPay)
                dismiss()
            }
        }

    }

    private fun renderPartialPaymentView() {
        binding.billingPaymentAmountValueTV.visibility = View.GONE
        binding.billingPaymentEditAmountValueTV.visibility = View.VISIBLE
        binding.billingPaymentCurrencyTV.visibility = View.VISIBLE
        binding.billingPaymentCurrencyTV.text = billingCurrency
        binding.billingPaymentEditAmountValueTV.setText(billingAmount)
    }

    private fun renderView() {
        if (cardOnFileList.isEmpty()) {
            binding.billingPaymentCardLayout.visibility = View.VISIBLE
            binding.billingPaymentChooseCardLayout.visibility = View.GONE
        } else {
            binding.billingPaymentCardLayout.visibility = View.GONE
            binding.billingPaymentChooseCardLayout.visibility = View.VISIBLE
            setChooseCardItems()
        }
    }

    private fun setChooseCardItems() {
        val listOfCard = cardOnFileList.map {
            getCardConCatCardDetail(it)
        }
        binding.billingPaymentChooseCardET.setItems(listOfCard, activity)
        binding.billingPaymentChooseCardET.setOnItemClickListener { position, _ ->
            selectedCardDetail = cardOnFileList[position]
        }
    }

    private fun getCardConCatCardDetail(paymentGatewayToken: PaymentGatewayToken): String {
        return getString(R.string.choose_card_detail, paymentGatewayToken.cardDigit, paymentGatewayToken.expiry, paymentGatewayToken.cardBrand)
    }

    override fun onStart() {
        super.onStart()

        if (dialog != null) {
            val bottomSheet = binding.billingPaymentDialog
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        val view = view
        view?.post {
            val parent = view.parent as View
            val params = (parent).layoutParams as (CoordinatorLayout.LayoutParams)
            val behavior = params.behavior
            val bottomSheetBehavior = behavior as (BottomSheetBehavior)
            bottomSheetBehavior.peekHeight = view.measuredHeight

        }
    }

    private fun validateData(): PaymentInfo? {

        if (isPartialPaymentAllowed && binding.billingPaymentEditAmountValueTV.visibility == View.VISIBLE) {

            if (binding.billingPaymentEditAmountValueTV.text.toString().isBlank() || binding.billingPaymentEditAmountValueTV.text.toString()
                    .toDoubleOrNull() == 0.00
            ) {
                UIUtil.showToast(this.activity, R.string.error_book_appointment_invalid_amount, Toast.LENGTH_LONG)
                return null
            } else if ((binding.billingPaymentEditAmountValueTV.text.toString().toDoubleOrNull() ?: 0.00) <= partialPaymentMinAmount) {
                binding.billingPaymentEditAmountValueTV.setText(partialPaymentMinAmount.toString())
                UIUtil.showToast(this.activity, getString(R.string.error_min_invalid_amount, partialPaymentMinAmount.toString()), Toast.LENGTH_LONG)
                return null
            } else if ((binding.billingPaymentEditAmountValueTV.text.toString().toDoubleOrNull() ?: 0.00) > billingAmount.toDoubleOrNull() ?: 0.00) {
                binding.billingPaymentEditAmountValueTV.setText(billingAmount)
                UIUtil.showToast(this.activity, getString(R.string.error_max_invalid_amount, billingAmount), Toast.LENGTH_LONG)
                return null
            }
        }

        val paymentInfo = PaymentInfo()
        if (binding.billingPaymentCardLayout.visibility == View.VISIBLE) {
            if (binding.billingPaymentCardNameET.text.toString().isBlank()) {
                UIUtil.showToast(this.activity, R.string.error_book_appointment_empty_card_holder_name, Toast.LENGTH_LONG)
                return null
            }

            if (binding.billingPaymentCardNumberET.text.toString().isBlank()) {
                UIUtil.showToast(this.activity, R.string.error_book_appointment_empty_card_number, Toast.LENGTH_LONG)
                return null
            }

//            if (binding.billingPaymentCardNumberET.text.toString().length != 19) {
//                UIUtil.showToast(this.activity, R.string.error_book_appointment_invalid_card_number, Toast.LENGTH_LONG)
//                return null
//            }

            if (binding.billingPaymentCardExpMonthET.text.toString().isBlank()) {
                UIUtil.showToast(this.activity, R.string.error_book_appointment_empty_card_expiry_month, Toast.LENGTH_LONG)
                return null
            }

            if (binding.billingPaymentCardExpYearET.text.toString().isBlank()) {
                UIUtil.showToast(this.activity, R.string.error_book_appointment_empty_card_expiry_year, Toast.LENGTH_LONG)
                return null
            }

            if (binding.billingPaymentCardCvvET.text.toString().isBlank()) {
                UIUtil.showToast(this.activity, R.string.error_book_appointment_empty_card_cvv, Toast.LENGTH_LONG)
                return null
            }

            if (binding.billingPaymentCardCvvET.text.toString().length < 3) {
                UIUtil.showToast(this.activity, R.string.error_book_appointment_invalid_card_cvv, Toast.LENGTH_LONG)
                return null
            }
            paymentInfo.cardHolderName = binding.billingPaymentCardNameET.text.toString()
            paymentInfo.cardNumber = binding.billingPaymentCardNumberET.text.toString().split(" ").joinToString(separator = "")
            paymentInfo.cardExpMonth = binding.billingPaymentCardExpMonthET.text.toString()
            paymentInfo.cardExpYear = binding.billingPaymentCardExpYearET.text.toString()
            paymentInfo.cardNickName = binding.billingPaymentCardNickNameET.text.toString()
            paymentInfo.cardCvv = binding.billingPaymentCardCvvET.text.toString()
        } else {
            if (binding.billingPaymentChooseCardET.text.toString().isBlank()) {
                UIUtil.showToast(
                    this.activity,
                    R.string.error_book_appointment_empty_card,
                    Toast.LENGTH_LONG
                )
                return null
            }
            paymentInfo.token = selectedCardDetail.token
        }
        paymentInfo.chargeProcessingType = ModelUtil.appointmentCardProcessingTypeChargeOnly
        return paymentInfo
    }

    fun setOnPayClickListener(listener: OnPayClickListner) {
        this.listener = listener
    }

    interface OnPayClickListner {
        fun onPayClick(paymentInfo: PaymentInfo, payableAmount: String)
    }
}