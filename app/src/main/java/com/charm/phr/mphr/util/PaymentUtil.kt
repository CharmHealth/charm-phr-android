package com.charm.phr.mphr.util

import android.app.AlertDialog
import androidx.annotation.StringRes
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.TransactionInfo
import com.razorpay.Checkout
import org.json.JSONObject

class PaymentUtil {

    companion object {

        fun checkoutRazorpay(
            activity: MDBaseActivity,
            transactionInfo: TransactionInfo,
            patient: Patient
        ): Boolean {
            // val patientEmail = authManager.loggedInPatient?.contactDetails?.emailId
            val patientName = patient.fullName
            //val patientMobile = authManager.loggedInPatient?.contactDetails?.mobile

            //Todo :Check ask mandatory check fields

            val publicKey = transactionInfo.publicToken
            val orderId = transactionInfo.razorPayOrderId


            Checkout.clearUserData(activity)

            if (!publicKey.isNullOrBlank() && !orderId.isNullOrBlank()) {


                val checkout = Checkout()

                checkout.setKeyID(publicKey)
                checkout.setImage(R.mipmap.ic_launcher_round) //Todo: change
                checkout.setFullScreenDisable(true)


                try {

                    val options = JSONObject()

                    if (transactionInfo.service == ModelUtil.paymentServiceBilling) {
                        options.put("name", "Billing Payment")
                    } else {
                        options.put("name", "Appointment Payment")
                    }
                    options.put("prefill.name", patientName)
                    options.put("prefill.email", transactionInfo.emailId)
                    options.put("prefill.contact", transactionInfo.mobile)
//                    options.put("prefill.method", "upi")
                    options.put("theme.color", "#4074D9")
                    /*options.put("image", MDAccountsRelatedUtil.facilityLogoUrl + authManager.kioskSettings.facility?.facilityId)*/

                    options.put("description", "Click Show QR to scan")
                    options.put("order_id", orderId)
                    options.put("currency", "INR")

                    //Amount is always passed in currency paise subunits * Eg: "500" = INR 5.00
                    options.put("amount", transactionInfo.paymentAntFromAPI) // 5 rs should be 500

                    MDBaseActivity.isPassCodeAllowed = false
                    checkout.open(activity, options)

                } catch (e: Exception) {

                    return false
                }

            } else return false


            return true
        }

        fun validatePaymentTransactionData(
            paymentTransaction: TransactionInfo,
            cardProcessingType: String?
        ): Boolean {

            val facilityId = paymentTransaction.facilityId
            val merchantAccountId = paymentTransaction.merchantAccountId
            val paymentAmount = paymentTransaction.paymentAmount?.toFloatOrNull()
            val memberId = paymentTransaction.memberId


            return !facilityId.isNullOrBlank() && (!merchantAccountId.isNullOrBlank()) && ((paymentAmount != null &&
                    paymentAmount > 0F) || cardProcessingType == ModelUtil.appointmentCardProcessingTypeStoreOnly) && (!memberId.isNullOrBlank())

        }

        fun retryCreateTransaction(
            activity: MDBaseActivity,
            @StringRes stringRes: Int,
            callback: (() -> Unit)
        ) {

            val alertDialog = AlertDialog.Builder(activity)


            alertDialog.setMessage(activity.getString(stringRes))

            alertDialog.setPositiveButton(R.string.retry_str) { _, _ ->
                callback.invoke()
            }

            alertDialog.show()

        }

        fun retryCreateTransaction(
            activity: MDBaseActivity,
            errorMessage: String,
            callback: (() -> Unit)
        ) {

            val alertDialog = AlertDialog.Builder(activity)


            alertDialog.setMessage(errorMessage)

            alertDialog.setPositiveButton(R.string.retry_str) { _, _ ->
                callback.invoke()
            }

            alertDialog.show()

        }

        fun retryUpdateTransaction(
            activity: MDBaseActivity,
            @StringRes stringRes: Int,
            callback: () -> Unit
        ) {
            val alertDialog = AlertDialog.Builder(activity)


            alertDialog.setMessage(stringRes)

            alertDialog.setPositiveButton(R.string.retry_str) { _, _ ->
                callback.invoke()
            }

            val alert = alertDialog.create()
            alert.setCancelable(false)
            alert.setCanceledOnTouchOutside(false)
            alert.show()
        }

    }

}