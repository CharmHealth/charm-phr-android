package com.charm.phr.mphr.ui.billing.billingDetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityBillingDetailBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.CardOnFilePOJO
import com.charm.phr.mphr.model.MDFile
import com.charm.phr.mphr.model.PHRBills
import com.charm.phr.mphr.model.PHRStatementDetail
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PaymentInfo
import com.charm.phr.mphr.model.PracticeList
import com.charm.phr.mphr.model.TransactionInfo
import com.charm.phr.mphr.ui.components.MDBillingPaymentDialog
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.PaymentUtil
import com.charm.phr.mphr.util.PdfUtil
import com.charm.phr.mphr.util.UIUtil
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class BillingDetailActivity : MDBaseActivity(), PaymentResultListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var billingDetailViewModel: BillingDetailViewModel

    val TAG = "BillingDetailActivity"

    private lateinit var binding: ActivityBillingDetailBinding


    private var totalCheckStatusCount = 0

    //region LifeCycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBillingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        billingDetailViewModel =
            ViewModelProvider(this, viewModelFactory)[BillingDetailViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.billingDetailToolbar)
        UIUtil.applyViewInsertForBottomView(binding.billingDetailRootLayout)

        billingDetailViewModel.isStatement =
            intent.getBooleanExtra(ModelUtil.billingIsStatementIntent, false)

        billingDetailViewModel.billingType =
            intent.getLongExtra(ModelUtil.billingType, ModelUtil.billingTypeInvoice)
        observePDF()
        renderUI()
        setOnClickListener()

    }

    override fun onBackPressed() {
        if (billingDetailViewModel.billingType == ModelUtil.billingTypeReceipt) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
    //endregion

    //region Event Handler
    private fun setOnClickListener() {
        binding.billingDetailBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.billingDetailPayBtn.setOnClickListener {


            if (billingDetailViewModel.paymentGateway == ModelUtil.appointmentGatewayTypeRazorpay) {
                callCreateTransaction()
            } else {
                if (!billingDetailViewModel.isCardOnFileListFetched) {
                    getCardOnFileForPractice(
                        PracticeList(practiceId = billingDetailViewModel.practiceId),
                        billingDetailViewModel.phrBills.merchantAccountId
                    )
                } else {
                    showPaymentBillingDialogue()
                }
            }
        }

        binding.billingDetailPDFErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.billingDetailProgress.root.visibility = View.VISIBLE
            binding.billingDetailMainLayout.visibility = View.GONE

            if (billingDetailViewModel.billingType == ModelUtil.billingTypeInvoice) {
                billingDetailViewModel.getInvoicePDF()
            } else {
                billingDetailViewModel.getReceiptPDF()
            }
        }

        binding.billingDetailPDFShareBtn.setOnClickListener {
            sharePDF()
        }
    }
    //endregion

    private fun renderUI() {
        binding.billingDetailProgress.root.visibility = View.VISIBLE
        binding.billingDetailMainLayout.visibility = View.GONE

        binding.billingDetailPDFErrorPage.root.setBackgroundResource(android.R.color.white)

        if (billingDetailViewModel.billingType == ModelUtil.billingTypeInvoice) {

            billingDetailViewModel.phrBillId = intent.getStringExtra(ModelUtil.phrBillIdIntent)
                ?: ""

            val isPaymentRequested =
                intent.getBooleanExtra(ModelUtil.billingIsPaymentRequested, false)

            if (isPaymentRequested) {
                binding.billingDetailPaymentDetailCard.visibility = View.VISIBLE
                binding.billingDetailInvoiceDateTV.text =
                    intent.getStringExtra(ModelUtil.billingDate) ?: ""
                binding.billingDetailInvoiceFacilityNameTV.text =
                    intent.getStringExtra(ModelUtil.billingFacilityName)
                        ?: ""
                binding.billingDetailInvoiceBillNumTV.text =
                    intent.getStringExtra(ModelUtil.billingInvoiceNum)
                        ?: ""

                val paymentReqAmt = intent.getStringExtra(ModelUtil.billingPaymentReqAmt) ?: ""
                val paymentCurrency =
                    intent.getStringExtra(ModelUtil.billingPaymentReqCurrency) ?: ""

                binding.billingDetailPaymentReqTV.text = "$paymentCurrency $paymentReqAmt"
                billingDetailViewModel.facilityId =
                    intent.getStringExtra(ModelUtil.billingFacilityIdIntent)
                        ?: ""
                billingDetailViewModel.practiceId =
                    intent.getStringExtra(ModelUtil.billingPracticeIdIntent)
                        ?: ""

                billingDetailViewModel.memberId =
                    intent.getStringExtra(ModelUtil.billingMemberIdIntent)
                        ?: ""

                val paymentReqAmtArr = paymentReqAmt.split(" ")

                val paymentGateway = intent.getStringExtra(ModelUtil.billingPaymentGatewayIntent)
                    ?: ""

                billingDetailViewModel.paymentAmt = paymentReqAmtArr.last()
                billingDetailViewModel.paymentSymbol =
                    paymentReqAmtArr.dropLast(1).joinToString(" ")



                binding.billingDetailPayBtn.visibility = View.VISIBLE
                billingDetailViewModel.paymentGateway = paymentGateway
                if (billingDetailViewModel.isStatement) {
                    if (billingDetailViewModel.paymentGateway == ModelUtil.appointmentGatewayTypeBluefin && authManager.isAllowStatementPayment) {
                        getStatementDetail(billingDetailViewModel.phrBillId)
                    } else {
                        binding.billingDetailPayBtn.visibility = View.GONE
                    }
                }
            } else {
                binding.billingDetailPaymentDetailCard.visibility = View.GONE
                binding.billingDetailPayBtn.visibility = View.GONE
            }

            billingDetailViewModel.getInvoicePDF()

        } else {

            binding.billingDetailPaymentDetailCard.visibility = View.GONE

            billingDetailViewModel.phrReceiptId =
                intent.getStringExtra(ModelUtil.phrReceiptIdIntent)
                    ?: ""

            billingDetailViewModel.getReceiptPDF()
        }

        val typeStr = if (billingDetailViewModel.billingType == ModelUtil.billingTypeInvoice) {

            if (billingDetailViewModel.isStatement) {
                getString(R.string.billing_statement_str)
            } else {
                getString(R.string.billing_invoices_str)
            }
        } else {
            getString(R.string.billing_receipts_str)
        }

        binding.billingDetailToolbarText.text =
            getString(R.string.billing_detail_toolbar_txt, typeStr)
    }

    private fun showPaymentBillingDialogue() {
        val paymentDialog = MDBillingPaymentDialog()
        paymentDialog.imm = inputManager
        val paymentCurrency = intent.getStringExtra(ModelUtil.billingPaymentReqCurrency) ?: ""
        paymentDialog.billingAmount = billingDetailViewModel.paymentAmt
        paymentDialog.billingCurrency = paymentCurrency
        paymentDialog.cardOnFileList = billingDetailViewModel.cardOnFileList
        paymentDialog.isPartialPaymentAllowed = billingDetailViewModel.isPartialPaymentAllowed
        paymentDialog.partialPaymentMinAmount = billingDetailViewModel.partialPaymentMinAmount
        paymentDialog.setOnPayClickListener(object : MDBillingPaymentDialog.OnPayClickListner {
            override fun onPayClick(paymentInfo: PaymentInfo, payableAmount: String) {

                callCreateTransaction(paymentInfo, payableAmount)
            }
        })

        paymentDialog.show(supportFragmentManager, paymentDialog.tag)
    }

    private fun showErrorMsg() {
        /* val typeStr = if(billingDetailViewModel.billingType == ModelUtil.billingTypeInvoice){
             getString(R.string.billing_invoices_str)
         }
         else{
             getString(R.string.billing_receipts_str)
         }

         UIUtil.showToast(this , getString(R.string.error_pdf_fetch_failed , typeStr) , Toast.LENGTH_LONG)*/

        binding.billingDetailPDFErrorPage.root.visibility = View.VISIBLE
        binding.billingDetailMainPDF.visibility = View.VISIBLE
        binding.billingDetailPDFViewer.visibility = View.GONE

    }

    private fun sharePDF() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val fileToShare = File(billingDetailViewModel.filePath ?: "")
        val typeStr = if (billingDetailViewModel.billingType == ModelUtil.billingTypeInvoice) {

            if (billingDetailViewModel.isStatement) {
                getString(R.string.billing_statement_str)
            } else {
                getString(R.string.billing_invoices_str)
            }
        } else {
            getString(R.string.billing_receipts_str)
        }

        if (fileToShare.exists()) {

            isPassCodeAllowed = false   // preventing from asking passcode
            val fileUri = FileProvider.getUriForFile(
                applicationContext,
                resources.getString(R.string.content_provider_authority),
                fileToShare
            )
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            shareIntent.type = "application/pdf"
            startActivity(
                (Intent.createChooser(
                    shareIntent,
                    getString(R.string.billing_detail_toolbar_txt, typeStr)
                ))
            )
        } else UIUtil.showToast(
            this,
            getString(
                R.string.demo_pdf_preview_failed,
                getString(R.string.billing_detail_toolbar_txt, typeStr)
            ),
            Toast.LENGTH_SHORT
        )

    }


    //region Server API
    private fun observePDF() {
        billingDetailViewModel.billingPDFLive.observe(this) { resp ->
            if (resp != null) {
                binding.billingDetailProgress.root.visibility = View.GONE
                binding.billingDetailMainLayout.visibility = View.VISIBLE
                binding.billingDetailMainPDF.visibility = View.VISIBLE
                binding.billingDetailPDFViewer.visibility = View.VISIBLE
                binding.billingDetailPDFErrorPage.root.visibility = View.GONE
                if (resp is MDFile) {

                    val savedFile = FileUtil.savePdfFromResponseBody(
                        resp,
                        applicationContext,
                        ModelUtil.folderTemp
                    )

                    if (savedFile != null) {
                        binding.billingDetailPDFViewer.initWithFile(savedFile)
                        billingDetailViewModel.filePath = savedFile.path
                    } else {
                        showErrorMsg()
                    }

                } else if (resp is Pair<*, *>) {

                    val respBody = resp.first

                    if (resp.second is PHRBills) {
                        billingDetailViewModel.phrBills = resp.second as PHRBills
                    }

                    if (billingDetailViewModel.memberId.isBlank()) {
                        billingDetailViewModel.memberId =
                            billingDetailViewModel.phrBills.memberId ?: ""
                    }
                    billingDetailViewModel.isPartialPaymentAllowed =
                        billingDetailViewModel.phrBills.isPartialPaymentAllowed ?: false
                    billingDetailViewModel.partialPaymentMinAmount =
                        billingDetailViewModel.phrBills.partialPaymentMinAmount?.toDouble() ?: 0.00

                    if (respBody is MDFile) {

                        val savedFile = FileUtil.savePdfFromResponseBody(
                            respBody,
                            applicationContext,
                            ModelUtil.folderTemp
                        )
                        if (savedFile != null) {
                            val success = PdfUtil.initPdfViewer(
                                binding.billingDetailPDFViewer,
                                savedFile,
                                this
                            ) {
                                showErrorMsg()
                            }
                            if (success) {
                                billingDetailViewModel.filePath = savedFile.path
                            }
                        } else {
                            showErrorMsg()
                        }
                    } else {
                        showErrorMsg()
                    }


                } else if (resp is APIResponseError) {
                    showErrorMsg()

                }
            }
        }
    }


    private fun getCardOnFileForPractice(practiceList: PracticeList, merchantAccountId: String?) {
        progressDialog.setMessage(R.string.loading_data)
        progressDialog.show()
        billingDetailViewModel.getCardOnFileForPractice(practiceList, merchantAccountId)
            .observe(this) {
                if (it != null) {
                    progressDialog.dismiss()
                    if (it is CardOnFilePOJO) {
                        billingDetailViewModel.cardOnFileList.addAll(it.patientSavedCardArrayList)
                        billingDetailViewModel.cardOnFileList.addAll(it.practiceSavedCardArrayList)
                        showPaymentBillingDialogue()
                        billingDetailViewModel.isCardOnFileListFetched = true
                    } else if (it is APIResponseError) {
                        UIUtil.showRetryAndCancelDialog(
                            this,
                            it.errorMsg ?: getString(R.string.requestFailedTryAgain),
                            R.string.retry_str,
                            false
                        ) { isRetry ->
                            if (isRetry) {
                                getCardOnFileForPractice(practiceList, merchantAccountId)
                            } else {
                                onBackPressed()
                            }
                        }
                    }
                }
            }
    }


    private fun getStatementDetail(statementId: String) {
        progressDialog.setMessage(R.string.loading_data)
        progressDialog.show()
        billingDetailViewModel.getStatementDetail(statementId).observe(this) {
            if (it != null) {
                progressDialog.dismiss()
                if (it is PHRStatementDetail) {
                    billingDetailViewModel.statementDetail = it
                    billingDetailViewModel.memberId =
                        billingDetailViewModel.statementDetail?.memberId ?: ""
                    billingDetailViewModel.isPartialPaymentAllowed =
                        billingDetailViewModel.statementDetail?.isPartialPaymentAllowed ?: false
                    billingDetailViewModel.partialPaymentMinAmount =
                        billingDetailViewModel.statementDetail?.partialPaymentMinAmount?.toDouble()
                            ?: 0.00
                    billingDetailViewModel.statementDetail?.requestedAmount?.let { paymentAmt ->
                        billingDetailViewModel.paymentAmt = paymentAmt
                    }
                    val paymentCurrency = intent.getStringExtra(ModelUtil.billingPaymentReqCurrency) ?: ""
                    binding.billingDetailPaymentReqTV.text =
                        "$paymentCurrency ${billingDetailViewModel.paymentAmt}"
                } else {
                    UIUtil.showRetryAndCancelDialog(
                        this,
                        R.string.requestFailedTryAgain,
                        R.string.retry_str,
                        false
                    ) { isRetry ->
                        if (isRetry) {
                            getStatementDetail(statementId)
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }
        }
    }
    //endregion

    //region Razorpay

    override fun onPaymentError(code: Int, response: String?) {
        Log.d("MdError", "onPaymentError code $code response")



        when (code) {

            Checkout.NETWORK_ERROR -> { //There was a network error, for example loss of internet connectivity.
                retryCreateTransaction(R.string.internet_connection_error)
            }

            Checkout.INVALID_OPTIONS -> {  //An issue with options passed in checkout.open.
                //{"code":"BAD_REQUEST_ERROR","description":"The OAuth token used in the request was invalid or had expired"}
                retryCreateTransaction(R.string.error_occurred_try_again)
            }

            Checkout.PAYMENT_CANCELED -> { //User cancelled the payment.
                //Do nothing stay on this page
            }

            Checkout.TLS_ERROR -> { //Device does not support TLS v1.1 or TLS v1.2.
                retryCreateTransaction(R.string.error_occurred_try_again)
            }

        }
    }

    override fun onPaymentSuccess(transactionId: String?) {
        billingDetailViewModel.billingPaymentTransactionInfo.razorPayPaymentId = transactionId
        updatePaymentTransaction(
            billingDetailViewModel.billingPaymentTransactionInfo.transactionId
                ?: ""
        )
    }


    private fun retryUpdateTransaction(transactionId: String) {

        PaymentUtil.retryUpdateTransaction(this, R.string.payment_under_process) {
            updatePaymentTransaction(transactionId)
        }
    }

    private fun createPaymentTransaction() {

        val paymentTransaction = billingDetailViewModel.billingPaymentTransactionInfo

        val validated = PaymentUtil.validatePaymentTransactionData(
            paymentTransaction, null
        )

        if (validated) {

            progressDialog.show()

            if (billingDetailViewModel.paymentGateway == ModelUtil.appointmentGatewayTypeBluefin) {

                totalCheckStatusCount = 1
                try {
                    lifecycleScope.launch(Dispatchers.IO) {
                        var resp: Any? = ""
                        resp = billingDetailViewModel.createPaymentForBluefin()
                        runOnUiThread {
                            onBluefinResponse(resp)
                        }
                    }
                } catch (ex: TimeoutCancellationException) {
                    Log.e(TAG, ex.printStackTrace().toString())
                    progressDialog.dismiss()
                } catch (ex: Exception) {
                    Log.e(TAG, ex.printStackTrace().toString())
                    progressDialog.dismiss()
                }
            } else {
                billingDetailViewModel.createTransactionForRazorPay(paymentTransaction)
                    .observe(this) { resp ->

                        if (resp != null) {

                            progressDialog.dismiss()

                            if (resp is TransactionInfo) {

                                billingDetailViewModel.billingPaymentTransactionInfo = resp

                                val isCheckoutSuccess = PaymentUtil.checkoutRazorpay(
                                    this, resp, authManager.selectedPatientLive.value
                                        ?: Patient()
                                )

                                if (!isCheckoutSuccess) {
                                    retryCreateTransaction(R.string.error_occurred_try_again)
                                }

                            } else retryCreateTransaction(R.string.error_occurred_try_again)

                        }

                    }
            }
        } else {
            retryCreateTransaction(R.string.mandatory_info_try_again)
        }

    }

    private fun onBluefinResponse(resp: Any?) {
        if (resp != null) {
            if (resp is String) {
                progressDialog.dismiss()
                onPaySuccess(resp)
            } else if (resp is APIResponseError) {

                if (resp.charmApiCode == ModelUtil.errorPaymentSuccessButReceiptFailedCode) {

                    totalCheckStatusCount++
                    if (totalCheckStatusCount <= 5) {

                        handler.postDelayed({
                            checkStatus()
                        }, 1000)
                    } else {
                        progressDialog.dismiss()
                        retryCreateTransaction(
                            resp.errorMsg ?: getString(R.string.error_occurred_try_again)
                        )
                    }
                } else {
                    progressDialog.dismiss()
                    retryCreateTransaction(
                        resp.errorMsg ?: getString(R.string.error_occurred_try_again)
                    )
                }
            } else {
                progressDialog.dismiss()
                retryCreateTransaction(R.string.error_occurred_try_again)
            }
        }
    }

    private fun checkStatus() {

        if (!progressDialog.isShowing) {
            progressDialog.dismiss()
        }
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                var resp: Any? = ""
                resp = billingDetailViewModel.checkTransactionStatus()
                runOnUiThread { onBluefinResponse(resp) }
            }
        } catch (ex: TimeoutCancellationException) {
            Log.e(TAG, ex.printStackTrace().toString())
            progressDialog.dismiss()
        } catch (ex: Exception) {
            Log.e(TAG, ex.printStackTrace().toString())
            progressDialog.dismiss()
        }
    }

    private fun retryCreateTransaction(@StringRes stringRes: Int) {
        PaymentUtil.retryCreateTransaction(this, stringRes) {
            createPaymentTransaction()
        }
    }

    private fun retryCreateTransaction(errorMessage: String) {
        PaymentUtil.retryCreateTransaction(this, errorMessage) {
            createPaymentTransaction()
        }
    }

    private fun updatePaymentTransaction(transactionId: String) {

        progressDialog.show()

        billingDetailViewModel.updateTransactionForRazorPay(transactionId).observe(this) {

            if (it != null) {
                progressDialog.dismiss()

                if (it is String) {
                    onPaySuccess(it)
                } else {
                    retryUpdateTransaction(transactionId)
                    //Error handeling
                }

            }

        }

    }

    private fun callCreateTransaction(
        paymentInfo: PaymentInfo? = null,
        payableAmount: String = ""
    ) {

        val transactionInfo = billingDetailViewModel.billingPaymentTransactionInfo

        transactionInfo.facilityId = billingDetailViewModel.facilityId
        transactionInfo.practiceId = billingDetailViewModel.practiceId
        transactionInfo.billId = billingDetailViewModel.phrBillId
        transactionInfo.service = ModelUtil.paymentServiceBilling
        if (billingDetailViewModel.isPartialPaymentAllowed) {
            transactionInfo.paymentAmount = payableAmount
        } else {
            transactionInfo.paymentAmount = billingDetailViewModel.paymentAmt
        }
        transactionInfo.memberId = billingDetailViewModel.memberId
        transactionInfo.gateway = billingDetailViewModel.paymentGateway
        if (billingDetailViewModel.isStatement) {
            transactionInfo.merchantAccountId =
                billingDetailViewModel.statementDetail?.merchantAccountId
        } else {
            transactionInfo.merchantAccountId = billingDetailViewModel.phrBills.merchantAccountId
        }

        if (paymentInfo != null) {
            paymentInfo.transactionInfo = transactionInfo
            paymentInfo.chargeAmount = transactionInfo.paymentAmount
            billingDetailViewModel.paymentInfo = paymentInfo
        }
        createPaymentTransaction()
    }

    private fun onPaySuccess(receiptId: String) {
        billingDetailViewModel.phrReceiptId = receiptId
        billingDetailViewModel.billingType = ModelUtil.billingTypeReceipt

        binding.billingDetailPaymentDetailCard.visibility = View.GONE

        binding.billingDetailToolbarText.text =
            getString(R.string.billing_detail_toolbar_txt, getString(R.string.billing_receipts_str))


        binding.billingDetailProgress.root.visibility = View.VISIBLE
        binding.billingDetailMainLayout.visibility = View.GONE
        binding.billingDetailPayBtn.visibility = View.GONE

        billingDetailViewModel.getReceiptPDF()
    }

    //endregion
}
