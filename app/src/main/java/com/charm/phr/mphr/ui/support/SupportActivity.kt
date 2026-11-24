package com.charm.phr.mphr.ui.support

import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivitySupportBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.SupportDocument
import com.charm.phr.mphr.ui.health_passport.GridSpacingItemDecoration
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.getPackageInfoCompat
import com.charm.phr.mphr.util.ModelUtil.Companion.hasAlphaRegex
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.UIUtil.Companion.selectChip
import com.charm.phr.mphr.util.UIUtil.Companion.setMandatorySignToString
import com.charm.phr.mphr.util.UIUtil.Companion.unSelectChip
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

class SupportActivity : MDBaseActivity() {

    @Inject
    lateinit var supportViewModel: SupportViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var binding: ActivitySupportBinding

    val TAG = "SupportActivity"

    @Inject
    lateinit var supportAttachmentAdapter: SupportAttachmentAdapter

    private lateinit var backPressAlertDialogBuilder: AlertDialog.Builder

    private val docPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            supportViewModel.isValueChanged = true
            if (supportViewModel.supportData.fileList.size < 5) {
                if (FileUtil.getFileSizeFromURI(uri, this) < 20) {
                    val extension = FileUtil.getMimeType(this, uri)

                    val fileName = FileUtil.getFileName(uri, this)
                    if (extension != null) {
                        var filePath: String? = ""
                        try {
                            lifecycleScope.launch(Dispatchers.IO) {
                                filePath = FileUtil.createFileFromUri(
                                    uri,
                                    applicationContext,
                                    ModelUtil.folderTemp,
                                    null,
                                    customFileName = fileName?.split(ModelUtil.fileNameSplitRegex)
                                        ?.firstOrNull() ?: ""
                                )
                                runOnUiThread {
                                    if (!filePath.isNullOrBlank()) {
                                        val supportDocument = SupportDocument()
                                        supportDocument.fileName = fileName ?: ""
                                        supportDocument.fileType = extension
                                        supportDocument.filePath = filePath ?: ""
                                        supportViewModel.supportData.fileList.add(
                                            supportDocument
                                        )
                                        supportAttachmentAdapter.addItemToSupportAttachmentList(
                                            supportDocument
                                        )
                                        if (supportViewModel.supportData.fileList.isNotEmpty()) {
                                            binding.supportAttachmentLL.visibility =
                                                View.VISIBLE
                                        } else {
                                            binding.supportAttachmentLL.visibility =
                                                View.GONE
                                        }
                                        if (supportViewModel.supportData.fileList.size == 4) {
                                            val lp =
                                                binding.supportAttachmentLL.layoutParams as ConstraintLayout.LayoutParams
                                            lp.height = binding.supportAttachmentRV.height

                                        }
                                    } else {
                                        UIUtil.showToast(
                                            applicationContext,
                                            R.string.error_occurred_while_handling_file,
                                            Toast.LENGTH_LONG
                                        )

                                    }
                                }
                            }
                        } catch (ex: TimeoutCancellationException) {
                            Log.e(TAG, ex.printStackTrace().toString())
                            UIUtil.showToast(
                                applicationContext,
                                R.string.error_occurred_while_handling_file,
                                Toast.LENGTH_LONG
                            )
                        } catch (ex: Exception) {
                            Log.e(TAG, ex.printStackTrace().toString())
                            UIUtil.showToast(
                                applicationContext,
                                R.string.error_occurred_while_handling_file,
                                Toast.LENGTH_LONG
                            )
                        }
                    } else {
                        UIUtil.showToast(
                            this,
                            R.string.error_file_not_supported,
                            Toast.LENGTH_LONG
                        )
                    }
                } else {
                    UIUtil.showToast(
                        this,
                        R.string.please_select_document_size_not_more_than_20MB,
                        Toast.LENGTH_LONG
                    )
                }
            } else {
                UIUtil.showToast(
                    this,
                    R.string.only_five_documents_are_allowed_to_attach,
                    Toast.LENGTH_LONG
                )
            }
        }
    }

    //region Lifecycle and override methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.supportAppreciationTV.text = getString(R.string.what_do_you_like_about_charm_phr, getString(R.string.app_name))

        supportViewModel = ViewModelProvider(this, viewModelFactory)[SupportViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.supportToolbar)
        UIUtil.applyViewInsertForBottomView(binding.supportMainView)

        binding.supportAttachmentRV.layoutManager =
            GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.supportAttachmentRV.addItemDecoration(
            GridSpacingItemDecoration(
                2,
                resources.getDimensionPixelSize(R.dimen.card_padding),
                resources.getDimensionPixelSize(R.dimen.card_padding),
                includeEdge = true
            )
        )
        supportViewModel.currentTabSelected =
            intent.getIntExtra(ModelUtil.supportType, ModelUtil.supportAppreciate)
        binding.supportAttachmentRV.adapter = supportAttachmentAdapter
        if (supportViewModel.currentTabSelected == ModelUtil.supportSuggest) {
            showSuggestionView()
        } else {
            showAppreciateView()
        }
        setOnClickListener()
        setOnClickListenerOfAppreciation()
        onBackPressAlert()
        setSupportAttachmentListener()
        setTextChangeListener()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    //endregion

    private fun setTextChangeListener() {
        binding.supportSeverityET.setOnItemClickListener { position, str ->
            supportViewModel.isValueChanged = true
        }
        binding.supportSubjectET.addTextChangedListener {
            supportViewModel.isValueChanged = true
        }
        binding.supportdescriptionET.addTextChangedListener {
            supportViewModel.isValueChanged = true
        }
    }

    private fun setSupportAttachmentListener() {
        supportAttachmentAdapter.setSupportAttachmentListener(object :
            SupportAttachmentAdapter.SupportAttachmentListener {
            override fun onAttachmentSelect(supportAttachment: SupportDocument) {

            }

            override fun onAttachmentDelete(position: Int) {
                val builder = AlertDialog.Builder(this@SupportActivity)

                builder.setMessage(R.string.compose_message_attachment_remove_str)
                builder.setPositiveButton(R.string.yes_str) { _, _ ->

                    supportAttachmentAdapter.deleteItemFromSupportAttachmentList(position)
                    supportViewModel.supportData.fileList.removeAt(position)

                    if (supportViewModel.supportData.fileList.isEmpty()) {
                        binding.supportAttachmentLL.visibility = View.GONE
                    } else {
                        binding.supportAttachmentLL.visibility = View.VISIBLE
                    }
                }
                builder.setNegativeButton(R.string.no_str) { _, _ ->

                }

                builder.show()
            }

        })
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

    private fun setOnClickListenerOfAppreciation() {
        binding.supportAppreciationProductiveChip.setOnClickListener {
            handleAppreciationClick(
                getString(R.string.productive_str),
                binding.supportAppreciationProductiveChip
            )
        }
        binding.supportAppreciationGoodChip.setOnClickListener {
            handleAppreciationClick(
                getString(R.string.good_in_support),
                binding.supportAppreciationGoodChip
            )
        }
        binding.supportAppreciationCustomiseChip.setOnClickListener {
            handleAppreciationClick(
                getString(R.string.easy_to_customise),
                binding.supportAppreciationCustomiseChip
            )
        }
        binding.supportAppreciationIntegratedChip.setOnClickListener {
            handleAppreciationClick(
                getString(R.string.well_integrated),
                binding.supportAppreciationIntegratedChip
            )
        }
        binding.supportAppreciationUseChip.setOnClickListener {
            handleAppreciationClick(
                getString(R.string.easy_to_use),
                binding.supportAppreciationUseChip
            )
        }
        binding.supportAppreciationAffordableChip.setOnClickListener {
            handleAppreciationClick(
                getString(R.string.affordable),
                binding.supportAppreciationAffordableChip
            )
        }
        binding.supportAppreciationRobustChip.setOnClickListener {
            handleAppreciationClick(
                getString(R.string.robust),
                binding.supportAppreciationRobustChip
            )
        }
    }

    private fun handleAppreciationClick(appreciationString: String, chipElement: Chip) {
        supportViewModel.isValueChanged = true
        if (supportViewModel.appreciationList.contains(appreciationString)) {
            supportViewModel.appreciationList.remove(appreciationString)
            chipElement.unSelectChip(this)
        } else {
            supportViewModel.appreciationList.add(appreciationString)
            chipElement.selectChip(this)
        }
    }

    fun setOnClickListener() {
        binding.supportReportTabBtnChip.setOnClickListener {
            showReportView()
        }

        binding.supportSuggestTabBtnChip.setOnClickListener {
            showSuggestionView()
        }

        binding.supportAskQuestionTabBtnChip.setOnClickListener {
            showAskQuestionView()
        }

        binding.suportAppreciateTabBtnChip.setOnClickListener {
            showAppreciateView()
        }

        binding.supportBackBtn.setOnClickListener {
            if (supportViewModel.isValueChanged) {
                backPressAlertDialogBuilder.show()
            } else {
                onBackPressed()
            }
        }

        binding.supportSubmitBtn.setOnClickListener {
            validateAndSubmitValues()
        }
        binding.supportAttachmentBtn.setOnClickListener {
            isPassCodeAllowed = false   // preventing from asking passcode
            docPickerLauncher.launch(arrayOf("image/*", "application/pdf"))
        }
    }

    private fun validateAndSubmitValues() {
        var subject = getString(R.string.app_name) + ModelUtil.supportSubjectPrefix
        var description: String
        val pInfo = packageManager.getPackageInfoCompat(packageName, 0)
        val appDescription =
            ModelUtil.appSdkVersion + Build.VERSION.SDK_INT + ModelUtil.appVersion + pInfo.versionName
        if (supportViewModel.currentTabSelected != ModelUtil.supportAppreciate) {
            if (binding.supportSubjectET.text.isNullOrBlank()) {
                UIUtil.showToast(this, R.string.please_enter_subject, Toast.LENGTH_LONG)
                binding.supportSubjectET.requestFocus()
                return
            } else if (!binding.supportSubjectET.text.toString().hasAlphaRegex()) {
                UIUtil.showToast(this, R.string.error_invalid_subject, Toast.LENGTH_LONG)
                binding.supportSubjectET.requestFocus()
                return
            } else {

                when (supportViewModel.currentTabSelected) {
                    ModelUtil.supportReport -> {
                        subject += binding.supportSubjectET.text.toString()
                        if (!binding.supportSeverityET.text.isNullOrBlank() && binding.supportSeverityET.text.toString() != getString(
                                R.string.select_str
                            )
                        ) {
                            subject =
                                subject + "[" + binding.supportSeverityET.text + " " + getString(R.string.problem_str) + "]"
                        }
                        supportViewModel.supportData.category = ModelUtil.supportCategoryReport
                    }

                    ModelUtil.supportSuggest -> {
                        subject =
                            subject + getString(R.string.suggestion_str) + binding.supportSubjectET.text.toString()
                        supportViewModel.supportData.category = ModelUtil.supportCategorySuggest
                    }

                    ModelUtil.supportAskQuestion -> {
                        subject =
                            subject + getString(R.string.question_str) + binding.supportSubjectET.text.toString()
                        supportViewModel.supportData.category = ModelUtil.supportCategoryAskQuestion
                    }
                }
                if (subject.length > 255) {
                    UIUtil.showToast(
                        this,
                        getString(R.string.please_enter_subject_only_255_char) + " " + (subject.length - 255),
                        Toast.LENGTH_LONG
                    )
                    binding.supportSubjectET.requestFocus()
                    return
                }
            }
            if (binding.supportdescriptionET.text.isNullOrBlank()) {
                when (supportViewModel.currentTabSelected) {
                    ModelUtil.supportReport -> {
                        UIUtil.showToast(
                            this,
                            R.string.please_describe_your_problem,
                            Toast.LENGTH_LONG
                        )
                    }

                    ModelUtil.supportSuggest -> {
                        UIUtil.showToast(
                            this,
                            R.string.please_describe_your_suggestion,
                            Toast.LENGTH_LONG
                        )
                    }

                    ModelUtil.supportAskQuestion -> {
                        UIUtil.showToast(
                            this,
                            R.string.please_describe_your_question,
                            Toast.LENGTH_LONG
                        )
                    }
                }
                binding.supportdescriptionET.requestFocus()
                return
            } else if (!binding.supportdescriptionET.text.toString().hasAlphaRegex()) {
                UIUtil.showToast(this, R.string.error_invalid_subject, Toast.LENGTH_LONG)
                binding.supportdescriptionET.requestFocus()
                return
            } else {
                description = binding.supportdescriptionET.text.toString() + "." + appDescription
                if (description.length > 1000) {
                    UIUtil.showToast(
                        this,
                        getString(R.string.please_enter_description_only_1000_char) + " " + (description.length - 1000),
                        Toast.LENGTH_LONG
                    )
                    binding.supportdescriptionET.requestFocus()
                    return
                }
            }
            supportViewModel.supportData.subject = subject
            supportViewModel.supportData.description = description
            if (supportViewModel.supportData.fileList.isNotEmpty()) {
                uploadSupportDocuments()
            } else {
                submitValues("")
            }
        } else {
            supportViewModel.supportData.category = ModelUtil.supportCategoryAppreciate
            subject += ModelUtil.supportCategoryAppreciation
            if (supportViewModel.appreciationList.isEmpty()) {
                if (binding.supportdescriptionET.text.isNullOrBlank()) {
                    UIUtil.showToast(
                        this,
                        R.string.please_describe_your_appreciation,
                        Toast.LENGTH_LONG
                    )
                    binding.supportdescriptionET.requestFocus()
                    return
                } else {
                    description =
                        binding.supportdescriptionET.text.toString() + "." + appDescription
                    if (description.length > 1000) {
                        UIUtil.showToast(
                            this,
                            getString(R.string.please_enter_appreciation_only_1000_char) + " " + (description.length - 1000),
                            Toast.LENGTH_LONG
                        )
                        binding.supportdescriptionET.requestFocus()
                        return
                    }
                }
            } else {
                description = getString(R.string.app_name) + " is "
                if (supportViewModel.appreciationList.size == 1) {
                    description += supportViewModel.appreciationList[0]
                    if (!binding.supportdescriptionET.text.isNullOrBlank()) {
                        description =
                            description + " and " + binding.supportdescriptionET.text + "." + appDescription
                        if (description.length > 1000) {
                            UIUtil.showToast(
                                this,
                                getString(R.string.please_enter_appreciation_only_1000_char) + " " + (description.length - 1000),
                                Toast.LENGTH_LONG
                            )
                            binding.supportdescriptionET.requestFocus()
                            return
                        }

                    } else {
                        description = "$description.$appDescription"
                    }
                } else if (supportViewModel.appreciationList.size == 2) {
                    if (!binding.supportdescriptionET.text.isNullOrBlank()) {
                        description =
                            description + supportViewModel.appreciationList[0] + "," + supportViewModel.appreciationList[1] + ","
                        description =
                            description + binding.supportdescriptionET.text + "." + appDescription
                        if (description.length > 1000) {
                            UIUtil.showToast(
                                this,
                                getString(R.string.please_enter_appreciation_only_1000_char) + " " + (description.length - 1000),
                                Toast.LENGTH_LONG
                            )
                            binding.supportdescriptionET.requestFocus()
                            return
                        }
                    } else {
                        description =
                            description + supportViewModel.appreciationList[0] + " and " + supportViewModel.appreciationList[1] + "." + appDescription
                    }
                } else {
                    supportViewModel.appreciationList.forEachIndexed { pos, appreciation ->
                        description += appreciation
                        description = if (pos == supportViewModel.appreciationList.size - 1) {
                            "$description."
                        } else {
                            "$description,"
                        }
                    }
                    if (!binding.supportdescriptionET.text.isNullOrBlank()) {
                        description =
                            description + binding.supportdescriptionET.text + "." + appDescription
                        if (description.length > 1000) {
                            UIUtil.showToast(
                                this,
                                getString(R.string.please_enter_appreciation_only_1000_char) + " " + (description.length - 1000),
                                Toast.LENGTH_LONG
                            )
                            binding.supportdescriptionET.requestFocus()
                            return
                        }

                    } else {
                        description += appDescription
                    }
                }
            }
            supportViewModel.supportData.subject = subject
            supportViewModel.supportData.description = description
            submitValues("")
        }

    }

    private fun uploadSupportDocuments() {
        progressDialog.show()
        progressDialog.setMessage(R.string.uploading_documents)
        supportViewModel.uploadSupportDocuments().observe(this) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is String) {
                    submitValues(resp)
                } else if (resp is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(
                        this,
                        R.string.requestFailedTryAgain,
                        R.string.retry_str,
                        false
                    ) { isRetry ->
                        if (isRetry) {
                            uploadSupportDocuments()
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }
        }
    }

    private fun submitValues(fileIds: String) {
        progressDialog.show()
        progressDialog.setMessage(R.string.submitting_str)
        supportViewModel.submitSupportData(fileIds).observe(this) { resp ->
            if (resp != null) {
                progressDialog.dismiss()
                if (resp is Boolean) {
                    UIUtil.showToast(
                        applicationContext,
                        getString(R.string.feedback_sent_successfully),
                        Toast.LENGTH_LONG
                    )
                    onBackPressed()
                } else if (resp is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(
                        this,
                        R.string.requestFailedTryAgain,
                        R.string.retry_str,
                        false
                    ) { isRetry ->
                        if (isRetry) {
                            submitValues(fileIds)
                        } else {
                            onBackPressed()
                        }
                    }
                }
            }
        }
    }

    private fun showReportView() {
        binding.supportSeverityLayout.visibility = View.VISIBLE
        binding.supportSubjectLayout.visibility = View.VISIBLE
        binding.supportAppreciationLayout.visibility = View.GONE
        val severityList =
            arrayListOf(
                getString(R.string.select_str),
                getString(R.string.critical_str),
                getString(R.string.major_str),
                getString(R.string.minor_str)
            )
        binding.supportSeverityET.setItems(severityList, this)
        binding.supportDescriptionTV.setText(getString(R.string.describe_your_problem).setMandatorySignToString())
        binding.supportSubjectTV.setText(getString(R.string.subject_str).setMandatorySignToString())
        binding.supportAttachmentBtn.visibility = View.VISIBLE
        if (supportViewModel.supportData.fileList.isNotEmpty()) {
            binding.supportAttachmentLL.visibility = View.VISIBLE
        }
        focusOnView(binding.supportTabLayout, binding.supportReportTabBtnChip)
        binding.supportReportTabBtnChip.selectChip(this)
        changeTabSelected(ModelUtil.supportReport)
    }

    fun showSuggestionView() {
        binding.supportSeverityLayout.visibility = View.GONE
        binding.supportSubjectLayout.visibility = View.VISIBLE
        binding.supportAppreciationLayout.visibility = View.GONE
        binding.supportDescriptionTV.setText(getString(R.string.describe_your_suggestion).setMandatorySignToString())
        binding.supportSubjectTV.setText(getString(R.string.subject_str).setMandatorySignToString())
        binding.supportAttachmentBtn.visibility = View.VISIBLE
        if (supportViewModel.supportData.fileList.isNotEmpty()) {
            binding.supportAttachmentLL.visibility = View.VISIBLE
        }
        focusOnView(binding.supportTabLayout, binding.supportSuggestTabBtnChip)
        binding.supportSuggestTabBtnChip.selectChip(this)
        changeTabSelected(ModelUtil.supportSuggest)
    }

    fun showAskQuestionView() {
        binding.supportSeverityLayout.visibility = View.GONE
        binding.supportSubjectLayout.visibility = View.VISIBLE
        binding.supportAppreciationLayout.visibility = View.GONE
        binding.supportDescriptionTV.setText(getString(R.string.describe_your_question).setMandatorySignToString())
        binding.supportSubjectTV.setText(getString(R.string.subject_str).setMandatorySignToString())
        binding.supportAttachmentBtn.visibility = View.VISIBLE
        if (supportViewModel.supportData.fileList.isNotEmpty()) {
            binding.supportAttachmentLL.visibility = View.VISIBLE
        }
        focusOnView(binding.supportTabLayout, binding.supportAskQuestionTabBtnChip)
        binding.supportAskQuestionTabBtnChip.selectChip(this)
        changeTabSelected(ModelUtil.supportAskQuestion)
    }

    private fun showAppreciateView() {
        binding.supportSeverityLayout.visibility = View.GONE
        binding.supportSubjectLayout.visibility = View.GONE
        binding.supportAppreciationLayout.visibility = View.VISIBLE
        binding.supportDescriptionTV.setText(R.string.appreciate_in_your_own_words)
        binding.supportAttachmentBtn.visibility = View.GONE
        binding.supportAttachmentLL.visibility = View.GONE

        focusOnView(binding.supportTabLayout, binding.suportAppreciateTabBtnChip)
        binding.suportAppreciateTabBtnChip.selectChip(this)
        changeTabSelected(ModelUtil.supportAppreciate)
    }

    private fun changeTabSelected(selectedTab: Int): Boolean {

        val isCurrentTabSelected = supportViewModel.currentTabSelected == selectedTab
        if (supportViewModel.currentTabSelected != selectedTab) {


            when (supportViewModel.currentTabSelected) {
                ModelUtil.supportReport -> {
                    binding.supportReportTabBtnChip.unSelectChip(this)
                }

                ModelUtil.supportSuggest -> {
                    binding.supportSuggestTabBtnChip.unSelectChip(this)
                }

                ModelUtil.supportAskQuestion -> {
                    binding.supportAskQuestionTabBtnChip.unSelectChip(this)
                }

                else -> {
                    binding.suportAppreciateTabBtnChip.unSelectChip(this)
                }
            }

            supportViewModel.currentTabSelected = selectedTab
        }

        return isCurrentTabSelected
    }

    /**
     * Method to give focus to the selected view if the view is hidden inside scroll
     **/
    private fun focusOnView(scroll: HorizontalScrollView, view: View) {
        handler.post {
            val vLeft = view.left
            val vRight = view.right
            val sWidth = scroll.width
            scroll.smoothScrollTo((vLeft + vRight - sWidth) / 2, 0)
        }
    }

}