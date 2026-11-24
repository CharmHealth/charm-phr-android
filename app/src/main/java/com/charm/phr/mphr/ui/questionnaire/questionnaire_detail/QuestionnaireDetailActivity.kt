package com.charm.phr.mphr.ui.questionnaire.questionnaire_detail

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityQuestionniareDetailBinding
import com.charm.phr.mphr.databinding.ItemAddFlowsheetDateBinding
import com.charm.phr.mphr.databinding.ItemAddFlowsheetLayoutBinding
import com.charm.phr.mphr.databinding.ItemQuestionnaireAllergyBinding
import com.charm.phr.mphr.databinding.ItemQuestionnaireAllergyEntryBinding
import com.charm.phr.mphr.databinding.ItemQuestionnaireOptionRadioButtonBinding
import com.charm.phr.mphr.databinding.ItemQuestionnaireOptionsRadioButton2Binding
import com.charm.phr.mphr.databinding.ItemQuestionnairePrescriptionBinding
import com.charm.phr.mphr.databinding.ItemQuestionnairePrescriptionEntryBinding
import com.charm.phr.mphr.databinding.ItemQuestionnaireSectionHeaderTemplateBinding
import com.charm.phr.mphr.databinding.ItemQuestionnaireSectionTemplateBinding
import com.charm.phr.mphr.databinding.ItemQuestionnaireSignatureBinding
import com.charm.phr.mphr.databinding.LayoutPatientContactBinding
import com.charm.phr.mphr.databinding.LayoutPatientDetailsBinding
import com.charm.phr.mphr.databinding.LayoutPatientInsuranceBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ContactDetails
import com.charm.phr.mphr.model.InsuredAndGuarantorDetails
import com.charm.phr.mphr.model.InsuredEmployerDetails
import com.charm.phr.mphr.model.IntakeQuestAllergyEntry
import com.charm.phr.mphr.model.IntakeQuestMedicationEntry
import com.charm.phr.mphr.model.IntakeQuestSupplementEntry
import com.charm.phr.mphr.model.OptionData
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientInsuranceDetails
import com.charm.phr.mphr.model.PatientQuesMap
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.model.PostalAddress
import com.charm.phr.mphr.model.PracticePayer
import com.charm.phr.mphr.model.QuestionnaireDetailPOJO
import com.charm.phr.mphr.model.QuestionnaireEntryPOJO
import com.charm.phr.mphr.model.SOAPTemplateTableCell
import com.charm.phr.mphr.model.SOAPWidgetEntryPOJO
import com.charm.phr.mphr.model.StateList
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.components.ExpandableSectionLayout
import com.charm.phr.mphr.ui.components.MDAutoCompleteTextViewAdapter
import com.charm.phr.mphr.ui.components.MDSignatureDialog
import com.charm.phr.mphr.ui.components.MDTextInputAutoCompleteTextView
import com.charm.phr.mphr.ui.components.MDTextInputEdittext
import com.charm.phr.mphr.ui.components.MDTextInputLayout
import com.charm.phr.mphr.ui.components.compose.MDNestedCheckBox
import com.charm.phr.mphr.ui.components.compose.MDNestedRadioButton
import com.charm.phr.mphr.ui.components.compose.MDTable
import com.charm.phr.mphr.ui.components.compose.MdRatingScale
import com.charm.phr.mphr.ui.demographics.save_pat_demo.RaceSelectDialog
import com.charm.phr.mphr.util.APIList
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.TemplateUIUtil
import com.charm.phr.mphr.util.TemplateUIUtil.getValueFromTable
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.ValidationUtil
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.JsonObject
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.io.File
import java.util.regex.Pattern
import javax.inject.Inject


class QuestionnaireDetailActivity : MDBaseActivity() {

    @Inject
    lateinit var picasso: Picasso

    lateinit var questionnaireDetailViewModel: QuestionnaireDetailViewModel

    @Inject
    lateinit var viewModelProviderFactory: ViewModelProvider.Factory

    private lateinit var activityQuestionnaireDetailBinding: ActivityQuestionniareDetailBinding

    lateinit var imm: InputMethodManager

    private val signatureAddedMap = hashMapOf<String, Boolean>()

    private var tagNo = 0
    private var insuranceTag = -1

    private lateinit var backPressAlertDialogBuilder: AlertDialog.Builder
    private lateinit var consentFormAlertDialogBuilder: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityQuestionnaireDetailBinding = ActivityQuestionniareDetailBinding.inflate(layoutInflater)
        setContentView(activityQuestionnaireDetailBinding.root)

        UIUtil.applyViewInsertForToolbar(activityQuestionnaireDetailBinding.questionnaireDetailToolbar)
        UIUtil.applyViewInsertForBottomView(activityQuestionnaireDetailBinding.questionnaireListRootLayout)

        imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager

        questionnaireDetailViewModel = ViewModelProvider(
            this,
            viewModelProviderFactory
        )[QuestionnaireDetailViewModel::class.java]

        val isHealthPassport = intent.getBooleanExtra(ModelUtil.isHealthPassportIntent, false)

        questionnaireDetailViewModel.isPast =
            intent.getBooleanExtra(ModelUtil.questionnaireIsPastIntent, false)
        if (isHealthPassport) {

            val patQuestionMapId = intent.getStringExtra(ModelUtil.questionnairePatQuesMapIdIntent)
                ?: ""

            val questionnaireName = intent.getStringExtra(ModelUtil.questionnaireNameIntent) ?: ""

            activityQuestionnaireDetailBinding.questionnaireDetailToolbarText.text =
                questionnaireName
            questionnaireDetailViewModel.physicianTemplates = PhysicianTemplates(
                patientQuesMap = PatientQuesMap(
                    patientQuesMapId = patQuestionMapId,
                    isSubmitted = false
                ),
                isHealthPassport = true,
                name = questionnaireName
            )
        } else {
            val physicianTemplatesStr =
                intent.getStringExtra(ModelUtil.questionnairePhysicianTemplatesIntent)
                    ?: ""

            if (physicianTemplatesStr.isNotBlank()) {
                questionnaireDetailViewModel.physicianTemplates =
                    MDParsingHelper.formQuestionnaireObj(
                        physicianTemplatesStr.parseStringAsJsonObject() ?: JsonObject()
                    )
                activityQuestionnaireDetailBinding.questionnaireDetailToolbarText.text =
                    questionnaireDetailViewModel.physicianTemplates.name
            }
        }

        Log.d(
            "MdCallback",
            "isFilled -> ${questionnaireDetailViewModel.physicianTemplates.patientQuesMap?.isFilled}"
        )

        questionnaireDetailViewModel.isHealthPassport = isHealthPassport

        getQuestionnaireDetail()
        setOnClickListener()
        onBackPressAlert()
        questionnaireDetailViewModel.questionnaireDetailLive.observe(this) { resp ->
            if (resp != null) {
                activityQuestionnaireDetailBinding.questionnaireDetailProgress.root.visibility =
                    View.GONE
                if (resp is Boolean) {


                    var isSupported = true


                    if (questionnaireDetailViewModel.questionnaireDetailPOJO.questionnairEntryPOJOList.isEmpty()) {
                        Log.d("MdQuestionnaire", "Empty")
                        isSupported = false
                    } else {

                        questionnaireDetailViewModel.questionnaireDetailPOJO.questionnairEntryPOJOList.mapNotNull { it.questionnaireEntry.notesType }
                            .forEach { notesType ->
                                if (!ModelUtil.questionnaireSupportedWidgets.contains(notesType)) {
                                    Log.d("MdQuestionnaire", "Not Supported -> $notesType")
                                    isSupported = false
                                }
                                if (!isSupported) {
                                    return@forEach
                                }
                            }
                    }

                    if (isSupported) {

                        questionnaireDetailViewModel.physicianTemplates.templateId =
                            questionnaireDetailViewModel.questionnaireDetailPOJO.templateId
                        activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.visibility =
                            View.VISIBLE

                        if (questionnaireDetailViewModel.physicianTemplates.isHealthPassport) {
                            activityQuestionnaireDetailBinding.questionnaireDetailSubmitBtn.visibility =
                                View.GONE
                            activityQuestionnaireDetailBinding.questionnaireDetailSaveBtn.visibility =
                                View.VISIBLE
                            activityQuestionnaireDetailBinding.questionnaireDetailBtnLayout.visibility =
                                View.VISIBLE
                            activityQuestionnaireDetailBinding.questionnaireDetailSaveBtn.layoutParams.width =
                                LayoutParams.MATCH_PARENT

                        }
                        renderFeedbackView(questionnaireDetailViewModel.questionnaireDetailPOJO)
                    } else {
                        activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.visibility =
                            View.GONE
                        activityQuestionnaireDetailBinding.questionnaireDetailNoDataTV.root.visibility =
                            View.VISIBLE
                        activityQuestionnaireDetailBinding.questionnaireDetailSubmitBtn.visibility =
                            View.GONE
                        activityQuestionnaireDetailBinding.questionnaireDetailSaveBtn.visibility =
                            View.GONE
                        activityQuestionnaireDetailBinding.questionnaireDetailBtnLayout.visibility =
                            View.GONE
                        activityQuestionnaireDetailBinding.questionnaireDetailNoDataTV.nodataImageView.setImageResource(
                            R.drawable.ic_swaasa_to_many_attempts
                        )
                        if (questionnaireDetailViewModel.questionnaireDetailPOJO.questionnairEntryPOJOList.isEmpty()) {
                            activityQuestionnaireDetailBinding.questionnaireDetailNoDataTV.nodataTextView.setText(
                                R.string.error_empty_questionniare
                            )
                        } else {
                            activityQuestionnaireDetailBinding.questionnaireDetailNoDataTV.nodataTextView.setText(
                                R.string.error_questionniare_feature_inavailability
                            )
                        }
                    }
                } else if (resp is APIResponseError) {
                    activityQuestionnaireDetailBinding.questionnaireDetailErrorLayout.root.visibility =
                        View.VISIBLE
                }
            }
        }

    }

    fun setOnClickListener() {
        activityQuestionnaireDetailBinding.questionnaireDetailErrorLayout.errorPageTryAgainBtn.setOnClickListener {
            getQuestionnaireDetail()
        }

        activityQuestionnaireDetailBinding.questionnaireDetailSaveBtn.setOnClickListener {
            updateQuestionnaire(false)
        }

        activityQuestionnaireDetailBinding.questionnaireDetailSubmitBtn.setOnClickListener {
            updateQuestionnaire(true)
        }

        activityQuestionnaireDetailBinding.questionnaireDetailBackBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun onConsentFormSubmit(
        isSubmitted: Boolean,
        questionnairePOJO: QuestionnaireDetailPOJO
    ) {
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    updateQuestionnaireCall(isSubmitted, questionnairePOJO)
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                    questionnaireDetailViewModel.isSaveCallInProgress = false
                }
            }
        }
        consentFormAlertDialogBuilder = AlertDialog.Builder(this)
        consentFormAlertDialogBuilder.setMessage(R.string.consent_form_submit_msg)
            .setPositiveButton(R.string.yes_str, dialogClickListener)
            .setNegativeButton(R.string.no_str, dialogClickListener)
        consentFormAlertDialogBuilder.setCancelable(false)
    }

    private fun onBackPressAlert() {
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    goBack()
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

    private fun updateQuestionnaire(isSubmitted: Boolean) {

        if (questionnaireDetailViewModel.isSaveCallInProgress) {
            return
        }
        questionnaireDetailViewModel.isSaveCallInProgress = true

        val questionnairePOJO = onSubmitFeedback(isSubmitted)
        if (questionnairePOJO != null) {
            if (isConsentForm() && isSubmitted) {
                onConsentFormSubmit(isSubmitted, questionnairePOJO)
                consentFormAlertDialogBuilder.show()
            } else {
                updateQuestionnaireCall(isSubmitted, questionnairePOJO)
            }
        } else {
            questionnaireDetailViewModel.isSaveCallInProgress = false
        }


    }

    private fun updateQuestionnaireCall(
        isSubmitted: Boolean,
        questionnairePOJO: QuestionnaireDetailPOJO
    ) {

        progressDialog.show()

        questionnaireDetailViewModel.saveQuestionnaireDetailPOJO(isSubmitted, questionnairePOJO)
            .observe(this) { resp ->

                if (resp != null) {
                    progressDialog.dismiss()
                    questionnaireDetailViewModel.isSaveCallInProgress = false
                    if (resp is Boolean) {

                        if (isSubmitted) {
                            UIUtil.showToast(
                                this,
                                R.string.questionnaire_submit_success,
                                Toast.LENGTH_LONG
                            )

                        } else {
                            questionnaireDetailViewModel.isSaved = true
                            UIUtil.showToast(
                                this,
                                R.string.questionnaire_save_success,
                                Toast.LENGTH_LONG
                            )
                        }
                        setResult(Activity.RESULT_OK)
                        finish()
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)

                    } else if (resp is APIResponseError) {

                        if (isSubmitted) {
                            UIUtil.showToast(
                                this@QuestionnaireDetailActivity,
                                R.string.error_questionnaire_submit_failed,
                                Toast.LENGTH_LONG
                            )
                        } else {
                            UIUtil.showToast(
                                this@QuestionnaireDetailActivity,
                                R.string.error_questionnaire_save_failed,
                                Toast.LENGTH_LONG
                            )
                        }
                    }
                }
            }
    }

    private fun getQuestionnaireDetail() {

        questionnaireDetailViewModel.questionnaireDetailLive.postValue(null)

        activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.visibility = View.GONE
        activityQuestionnaireDetailBinding.questionnaireDetailProgress.root.visibility =
            View.VISIBLE
        activityQuestionnaireDetailBinding.questionnaireDetailErrorLayout.root.visibility =
            View.GONE
        activityQuestionnaireDetailBinding.questionnaireDetailSaveBtn.visibility = View.GONE
        activityQuestionnaireDetailBinding.questionnaireDetailSubmitBtn.visibility = View.GONE
        activityQuestionnaireDetailBinding.questionnaireDetailBtnLayout.visibility = View.GONE



        questionnaireDetailViewModel.fetchQuestionnaireDetails()
    }

    /**///region view rendering methods


    private fun renderFeedbackView(questionnaireDetailPOJO: QuestionnaireDetailPOJO) {

        activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.removeAllViews()

        val patQuesMap = questionnaireDetailViewModel.physicianTemplates.patientQuesMap

        if (patQuesMap != null) {
            questionnaireDetailViewModel.isDataEditAllowed =
                patQuesMap.allowPhrEditing && !questionnaireDetailViewModel.isPast

            if (!questionnaireDetailViewModel.physicianTemplates.isHealthPassport) {
                if (questionnaireDetailViewModel.isDataEditAllowed) {

                    if (patQuesMap.isSubmitted) {
                        if (isConsentForm()) {
                            UIUtil.showToast(
                                this,
                                R.string.consent_form_edit_msg,
                                Toast.LENGTH_LONG
                            )
                            activityQuestionnaireDetailBinding.questionnaireDetailSubmitBtn.visibility =
                                View.GONE
                            questionnaireDetailViewModel.isDataEditAllowed = false
                        } else {
                            activityQuestionnaireDetailBinding.questionnaireDetailSubmitBtn.visibility =
                                View.VISIBLE

                            activityQuestionnaireDetailBinding.questionnaireDetailSubmitBtn.text =
                                getString(R.string.submit_again_txt)

                        }

                        activityQuestionnaireDetailBinding.questionnaireDetailBtnLayout.visibility =
                            View.VISIBLE
                        activityQuestionnaireDetailBinding.questionnaireDetailSaveBtn.visibility =
                            View.GONE
                        activityQuestionnaireDetailBinding.questionnaireDetailSubmitBtn.layoutParams.width =
                            LayoutParams.MATCH_PARENT
                    } else {
                        if (isConsentForm()) {
                            activityQuestionnaireDetailBinding.questionnaireDetailSaveBtn.visibility =
                                View.GONE
                            activityQuestionnaireDetailBinding.questionnaireDetailSubmitBtn.layoutParams.width =
                                LayoutParams.MATCH_PARENT
                        } else {
                            activityQuestionnaireDetailBinding.questionnaireDetailSaveBtn.visibility =
                                View.VISIBLE
                        }
                        activityQuestionnaireDetailBinding.questionnaireDetailSubmitBtn.visibility =
                            View.VISIBLE
                        activityQuestionnaireDetailBinding.questionnaireDetailBtnLayout.visibility =
                            View.VISIBLE
                    }
                } else {
                    activityQuestionnaireDetailBinding.questionnaireDetailSubmitBtn.visibility =
                        View.GONE
                    activityQuestionnaireDetailBinding.questionnaireDetailSaveBtn.visibility =
                        View.GONE
                    activityQuestionnaireDetailBinding.questionnaireDetailBtnLayout.visibility =
                        View.GONE
                    if (!questionnaireDetailViewModel.isPast) {
                        UIUtil.showToast(
                            this,
                            R.string.questionnaire_form_edit_msg,
                            Toast.LENGTH_LONG
                        )
                    }
                }
            }
        }
        var parentView = activityQuestionnaireDetailBinding.questionnaireDetailMainLayout
        questionnaireDetailPOJO.questionnairEntryPOJOList.forEachIndexed { index, feedBackPOJO ->

            when (feedBackPOJO.questionnaireEntry.notesType) {
                ModelUtil.questionnaireNotesTypeSection -> {
                    if (index != questionnaireDetailPOJO.questionnairEntryPOJOList.size - 1) {
                        val nextElement =
                            questionnaireDetailPOJO.questionnairEntryPOJOList[index + 1]
                        if (nextElement.questionnaireEntry.notesType != ModelUtil.questionnaireNotesTypeSection) {
                            parentView =
                                activityQuestionnaireDetailBinding.questionnaireDetailMainLayout
                            questionnaireDetailViewModel.isParentSection = true
                            parentView = createSection(
                                feedBackPOJO,
                                parentView
                            )
                        }
                    }
                }

                ModelUtil.questionnaireNotesTypeOptionWithOthers -> {
                    createOptions(feedBackPOJO, true, parentView)
                }

                ModelUtil.questionnaireNotesTypeOption -> {
                    createOptions(feedBackPOJO, false, parentView)
                }

                ModelUtil.questionnaireNotesTypeYesOrNoOption -> {
                    createOptions(feedBackPOJO, false, parentView)
                }

                ModelUtil.questionnaireNotesTypeSimpleQuestion -> {
                    TemplateUIUtil.createQuestionAnswer(
                        true,
                        parentView,
                        feedBackPOJO.questionnaireEntry.notes ?: "",
                        feedBackPOJO.questionnaireEntry.entryId,
                        feedBackPOJO.questionnaireEntry.isMandatory,
                        feedBackPOJO.questionnaireEntry.answersList?.firstOrNull(),
                        questionnaireDetailViewModel.tagToIdMap,
                        questionnaireDetailViewModel.isParentSection,
                        questionnaireDetailViewModel.isDataEditAllowed,
                        this, tagNo
                    )
                    if (questionnaireDetailViewModel.isDataEditAllowed) {
                        tagNo++
                    }

                }

                ModelUtil.questionnaireNotesTypeQuestion -> {
                    TemplateUIUtil.createQuestionAnswer(
                        false,
                        parentView,
                        feedBackPOJO.questionnaireEntry.notes ?: "",
                        feedBackPOJO.questionnaireEntry.entryId,
                        feedBackPOJO.questionnaireEntry.isMandatory,
                        feedBackPOJO.questionnaireEntry.answersList?.firstOrNull(),
                        questionnaireDetailViewModel.tagToIdMap,
                        questionnaireDetailViewModel.isParentSection,
                        questionnaireDetailViewModel.isDataEditAllowed,
                        this,
                        tagNo
                    )
                    if (questionnaireDetailViewModel.isDataEditAllowed) {
                        tagNo++
                    }

                }

                ModelUtil.questionnaireNotesTypeRatingScale -> {
                    TemplateUIUtil.createRatingScale(
                        feedBackPOJO.questionnaireEntry.notes,
                        feedBackPOJO.questionnaireEntry.isMandatory,
                        parentView,
                        (feedBackPOJO.questionnaireEntry.fromScale)?.toFloat() ?: 0F,
                        (feedBackPOJO.questionnaireEntry.toScale)?.toFloat() ?: 10f,
                        feedBackPOJO.questionnaireEntry.answersList?.firstOrNull(),
                        feedBackPOJO.questionnaireEntry.entryId,
                        questionnaireDetailViewModel.tagToIdMap,
                        questionnaireDetailViewModel.isParentSection,
                        this,
                        questionnaireDetailViewModel.isDataEditAllowed,
                        tagNo, questionnaireDetailViewModel.sliderIsChangedList
                    )
                    if (questionnaireDetailViewModel.isDataEditAllowed) {
                        tagNo++
                    }

                }

                ModelUtil.questionnaireNotesTypeLabel -> {
                    TemplateUIUtil.createLabel(
                        parentView,
                        feedBackPOJO.questionnaireEntry.notes ?: "",
                        feedBackPOJO.questionnaireLabel.fontSize,
                        feedBackPOJO.questionnaireLabel.textAlign,
                        feedBackPOJO.questionnaireLabel.fontStyle,
                        feedBackPOJO.questionnaireLabel.fontWeight,
                        feedBackPOJO.questionnaireLabel.textDecoration,
                        this,
                        questionnaireDetailViewModel.isHealthPassport,
                        questionnaireDetailViewModel.physicianTemplates.name,
                        entryId = feedBackPOJO.questionnaireEntry.entryId,
                        tagNo = 0
                    )
                }

                ModelUtil.questionnaireNotesTypeDate -> {
                    createDate(feedBackPOJO, parentView)
                }

                ModelUtil.questionnaireNotesTypeSignature -> {
                    createSignature(feedBackPOJO, parentView)
                }

                ModelUtil.questionnaireNotesTypeMedication -> {
                    createMedicationWidget(feedBackPOJO, parentView)
                }

                ModelUtil.questionnaireNotesTypeSupplement -> {
                    createSupplementWidget(feedBackPOJO, parentView)
                }

                ModelUtil.questionnaireNotesTypeAllergy -> {
                    createAllergyWidget(feedBackPOJO, parentView)
                }

                ModelUtil.questionnaireNotesTypePersonalDetails -> {
                    createPersonalDetailsWidget(feedBackPOJO, parentView)
                }

                ModelUtil.questionnaireNotesTypePrimaryContactDetail -> {
                    createPrimaryContactWidget(feedBackPOJO, parentView)
                }

                ModelUtil.questionnaireNotesTypeInsurance -> {
                    createPatientInsurance(feedBackPOJO, parentView)
                }

                ModelUtil.historyOfIllnessSOAPType, ModelUtil.pastMedicalHistorySOAPType, ModelUtil.familyHistorySOAPType,
                ModelUtil.socialHistorySOAPType, ModelUtil.reviewOfSystemsSOAPType, ModelUtil.chiefComplaintsSOAPType,
                ModelUtil.symptomsSOAPType -> {
                    createSoapWidget(feedBackPOJO, parentView)
                }

                ModelUtil.questionnaireNotesTypeRating -> {
                    createFeedBackRating(feedBackPOJO, parentView)
                }

                else -> {

                }
            }
        }

//        if (activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.childCount > 0) {
//            val lp = activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.getChildAt(0)?.layoutParams as ViewGroup.MarginLayoutParams
//            lp.topMargin = resources.getDimensionPixelOffset(R.dimen.card_padding)
//        }
    }

    //region SOAP
    private fun createSoapWidget(
        questionnaireEntryPOJO: QuestionnaireEntryPOJO,
        parentView: LinearLayout
    ) {
        val soapWidgetEntryList = questionnaireEntryPOJO.soapWidget.soapWidgetEntryPOJOList
        questionnaireDetailViewModel.tagToIdMap[tagNo.toString()] =
            questionnaireEntryPOJO.questionnaireEntry.entryId
        tagNo++
        val tempTagToIdMap = linkedMapOf<String, String>()
        soapWidgetEntryList.forEach { soapWidgetEntryObj ->
            createViewForWidgetEntry(soapWidgetEntryObj, parentView, tempTagToIdMap)
        }
        questionnaireDetailViewModel.soapTagToIdMap[questionnaireEntryPOJO.questionnaireEntry.entryId] =
            tempTagToIdMap
    }

    private fun createViewForWidgetEntry(
        soapWidgetEntryObj: SOAPWidgetEntryPOJO,
        parentView: LinearLayout,
        tempTagToIdMap: LinkedHashMap<String, String>
    ) {
        val soapWidgetEntry = soapWidgetEntryObj.soapWidgetEntry
        when (soapWidgetEntry?.entryType) {
            ModelUtil.soapWidgetHeaderType -> {
                var fontSize = 14
                var fontStyle = ""
                var fontWeight = ""
                var textAlign = ""
                var textDecoration = ""

                soapWidgetEntryObj.soapWidgetProperty.forEach { property ->
                    when (property.propertyName) {
                        ModelUtil.swfontSize() -> {
                            if (property.propertyValue != null) {
                                val len = property.propertyValue?.length ?: 0
                                val fontSizeWithpx = property.propertyValue?.substring(0, len - 2)
                                fontSize = fontSizeWithpx?.toInt() ?: 14
                            }
                        }

                        ModelUtil.swtextAlign() -> {
                            textAlign = property.propertyValue ?: ""
                        }

                        ModelUtil.swfontWeight() -> {
                            fontWeight = property.propertyValue ?: ""
                        }

                        ModelUtil.swfontStyle() -> {
                            fontStyle = property.propertyValue ?: ""
                        }

                        ModelUtil.swtextDecoration() -> {
                            textDecoration = property.propertyValue ?: ""
                        }

                    }
                }
                TemplateUIUtil.createLabel(
                    parentView,
                    soapWidgetEntry.entryName ?: "",
                    fontSize,
                    textAlign,
                    fontStyle,
                    fontWeight,
                    textDecoration,
                    this,
                    questionnaireDetailViewModel.isHealthPassport,
                    questionnaireDetailViewModel.physicianTemplates.name,
                    true,
                    soapWidgetEntry.isMandatory ?: false,
                    tempTagToIdMap,
                    questionnaireDetailViewModel.isDataEditAllowed,
                    soapWidgetEntry.entryId ?: "",
                    tagNo
                )
                if (questionnaireDetailViewModel.isDataEditAllowed && soapWidgetEntry.isMandatory == true) {
                    tagNo++
                }
            }

            ModelUtil.soapWidgetSimpleQuestionType -> {

                TemplateUIUtil.createQuestionAnswer(
                    !soapWidgetEntry.options.equals("MULTILINE:true"),
                    parentView,
                    soapWidgetEntry.entryName ?: "",
                    soapWidgetEntry.entryId ?: "",
                    soapWidgetEntry.isMandatory ?: false,
                    soapWidgetEntry.entryAnswer,
                    tempTagToIdMap, questionnaireDetailViewModel.isParentSection,
                    questionnaireDetailViewModel.isDataEditAllowed,
                    this, tagNo
                )
                if (questionnaireDetailViewModel.isDataEditAllowed) {
                    tagNo++
                }

            }

            ModelUtil.soapWidgetRatingScaleType -> {
                val minMaxValue = soapWidgetEntry.options?.split(ModelUtil.swOptionsSeparatorSymbol)

                val min = minMaxValue?.first()?.toFloat() ?: 0f
                val max = minMaxValue?.last()?.toFloat() ?: 10f
                TemplateUIUtil.createRatingScale(
                    soapWidgetEntry.entryName ?: "",
                    soapWidgetEntry.isMandatory ?: false,
                    parentView,
                    min,
                    max,
                    soapWidgetEntry.entryAnswer,
                    soapWidgetEntry.entryId ?: "",
                    tempTagToIdMap,
                    questionnaireDetailViewModel.isParentSection,
                    this,
                    questionnaireDetailViewModel.isDataEditAllowed,
                    tagNo,
                    questionnaireDetailViewModel.sliderIsChangedList
                )
                if (questionnaireDetailViewModel.isDataEditAllowed) {
                    tagNo++
                }
            }

            ModelUtil.soapWidgetTextBoxType -> {

                TemplateUIUtil.createQuestionAnswer(
                    false,
                    parentView,
                    soapWidgetEntry.entryName ?: "",
                    soapWidgetEntry.entryId ?: "",
                    soapWidgetEntry.isMandatory ?: false,
                    soapWidgetEntry.entryAnswer ?: soapWidgetEntry.entryName,
                    tempTagToIdMap, questionnaireDetailViewModel.isParentSection,
                    questionnaireDetailViewModel.isDataEditAllowed,
                    this, tagNo, true
                )
                if (questionnaireDetailViewModel.isDataEditAllowed) {
                    tagNo++
                }
            }

            ModelUtil.soapWidgetYesOrNoQuestionType -> {
                var answer = soapWidgetEntry.entryAnswer ?: ""
                var comments = ""
                if (soapWidgetEntry.soapWidgetSelectableEntry?.isAddCommentsForNo == true || soapWidgetEntry.soapWidgetSelectableEntry?.isAddCommentsForYes == true) {
                    val ansComment =
                        soapWidgetEntry.entryAnswer?.split(ModelUtil.swOptionsSeparatorSymbol)
                    answer = ansComment?.first()?.toString() ?: ""
                    comments = if (ansComment?.size == 2) ansComment[1] else ""
                }
                createYesOrNoComponent(
                    parentView,
                    soapWidgetEntry.entryName,
                    soapWidgetEntry.isMandatory ?: false,
                    soapWidgetEntry.entryId ?: "",
                    soapWidgetEntry.soapWidgetSelectableEntry?.isAddCommentsForNo ?: false,
                    soapWidgetEntry.soapWidgetSelectableEntry?.isAddCommentsForYes ?: false,
                    answer,
                    comments,
                    tempTagToIdMap
                )
            }

            ModelUtil.soapWidgetCheckListType -> {
                createMultiSelectComponent(
                    parentView,
                    soapWidgetEntry.entryId ?: "",
                    soapWidgetEntry.entryName ?: "",
                    soapWidgetEntry.isMandatory ?: false,
                    tempTagToIdMap,
                    soapWidgetEntry.soapWidgetSelectableEntry?.optionList ?: arrayListOf(),
                    true
                )
            }

            ModelUtil.soapWidgetCheckBoxType -> {

                createMultiSelectComponent(
                    parentView,
                    soapWidgetEntry.entryId ?: "",
                    soapWidgetEntry.entryName ?: "",
                    soapWidgetEntry.isMandatory ?: false,
                    tempTagToIdMap,
                    soapWidgetEntry.soapWidgetSelectableEntry?.optionList ?: arrayListOf(),
                    isAdditionalComments = soapWidgetEntry.soapWidgetSelectableEntry?.isAdditionalComments
                        ?: false,
                    additionalComments = soapWidgetEntry.soapWidgetSelectableEntry?.additionalComments
                        ?: ""
                )
            }

            ModelUtil.soapWidgetRadioType -> {
                createSingleSelectComponent(
                    parentView,
                    soapWidgetEntry.entryId ?: "",
                    soapWidgetEntry.entryName ?: "",
                    soapWidgetEntry.isMandatory ?: false,
                    tempTagToIdMap,
                    soapWidgetEntry.soapWidgetSelectableEntry?.optionList ?: arrayListOf(),
                    soapWidgetEntry.soapWidgetSelectableEntry?.isAdditionalComments ?: false,
                    soapWidgetEntry.soapWidgetSelectableEntry?.additionalComments
                )
            }

            ModelUtil.soapWidgetTableType -> {
                createTableComponent(
                    parentView,
                    soapWidgetEntry.entryId ?: "",
                    tempTagToIdMap,
                    soapWidgetEntry.soapWidgetTableData
                )
            }
        }
    }

    private fun createTableComponent(
        parentView: LinearLayout,
        entryId: String,
        tagToIdMap: LinkedHashMap<String, String>,
        tableData: ArrayList<ArrayList<SOAPTemplateTableCell>>
    ) {
        val cardView =
            ItemAddFlowsheetLayoutBinding.inflate(LayoutInflater.from(this), parentView, false)
        cardView.addFlowsheetDataHeadingTV.visibility = View.GONE
        cardView.addFlowsheetDataView.visibility = View.GONE
        if (questionnaireDetailViewModel.isParentSection) {
            TemplateUIUtil.renderSectionCardView(cardView, this)
        }
        if (questionnaireDetailViewModel.isDataEditAllowed) {
            cardView.addFlowsheetDataEntryLayout.tag = tagNo
            tagToIdMap[tagNo.toString()] = entryId
            tagNo++
        }
        val totalColSpan = calculateTotalColSpan(tableData)
        cardView.addFlowsheetDataEntryLayout.addView(ComposeView(this).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MPHRTheme {
                    MDTable(
                        Modifier.padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 10.dp),
                        tableData,
                        totalColSpan,
                        questionnaireDetailViewModel.isDataEditAllowed
                    )
                }
            }
        })
        parentView.addView(cardView.root)
    }

    private fun calculateTotalColSpan(tableData: ArrayList<ArrayList<SOAPTemplateTableCell>>): Int {
        var totalCalSpan = 0
        tableData.firstOrNull()?.forEach {
            if (it.colspan.isNotBlank())
                totalCalSpan += it.colspan.toInt()
        }
        return totalCalSpan
    }

    private fun createSingleSelectComponent(
        parentView: LinearLayout,
        entryId: String,
        entryName: String,
        isMandatory: Boolean,
        tagToIdMap: LinkedHashMap<String, String>,
        optionList: ArrayList<OptionData>,
        isAdditionalComments: Boolean,
        additionalComments: String?
    ) {
        val cardView =
            ItemAddFlowsheetLayoutBinding.inflate(LayoutInflater.from(this), parentView, false)
        cardView.addFlowsheetDataHeadingTV.text = entryName
        if (isMandatory) {
            UIUtil.makeTextViewMandatory(cardView.addFlowsheetDataHeadingTV)
        }
        if (questionnaireDetailViewModel.isParentSection) {
            TemplateUIUtil.renderSectionCardView(cardView, this)
        }
        if (questionnaireDetailViewModel.isDataEditAllowed) {
            cardView.addFlowsheetDataEntryLayout.tag = tagNo
            tagToIdMap[tagNo.toString()] = entryId
            tagNo++
        }


        cardView.addFlowsheetDataEntryLayout.addView(ComposeView(this).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MPHRTheme {
                    val list by rememberSaveable { mutableStateOf(optionList.toList()) }
                    MDNestedRadioButton(
                        Modifier
                            .wrapContentSize()
                            .padding(start = 8.dp, top = 4.dp, bottom = 8.dp, end = 8.dp),
                        list,
                        questionnaireDetailViewModel.isDataEditAllowed
                    )
                }
            }
        })

        if (isAdditionalComments) {
            TemplateUIUtil.renderCommentsView(
                additionalComments,
                cardView.addFlowsheetDataEntryLayout,
                this,
                questionnaireDetailViewModel.isDataEditAllowed
            )
        }

        parentView.addView(cardView.root)
    }


    private fun createMultiSelectComponent(
        parentView: LinearLayout,
        entryId: String,
        entryName: String,
        isMandatory: Boolean,
        tagToIdMap: LinkedHashMap<String, String>,
        optionList: ArrayList<OptionData>,
        isCheckList: Boolean = false,
        isAdditionalComments: Boolean = false,
        additionalComments: String = ""
    ) {
        val cardView =
            ItemAddFlowsheetLayoutBinding.inflate(LayoutInflater.from(this), parentView, false)
        if (isCheckList) {
            cardView.addFlowsheetDataHeadingTV.text = getString(R.string.choose_one_option)
        } else {
            cardView.addFlowsheetDataHeadingTV.text = entryName
        }
        if (isMandatory) {
            UIUtil.makeTextViewMandatory(cardView.addFlowsheetDataHeadingTV)
        }
        if (questionnaireDetailViewModel.isParentSection) {
            TemplateUIUtil.renderSectionCardView(cardView, this)
        }
        if (questionnaireDetailViewModel.isDataEditAllowed) {
            cardView.addFlowsheetDataEntryLayout.tag = tagNo
            tagToIdMap[tagNo.toString()] = entryId
            tagNo++
        }

        cardView.addFlowsheetDataEntryLayout.addView(ComposeView(this).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MPHRTheme {
                    val list by rememberSaveable { mutableStateOf(optionList.toList()) }
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 8.dp)
                    ) {
                        MDNestedCheckBox(
                            list,
                            arrayListOf(),
                            enable = questionnaireDetailViewModel.isDataEditAllowed
                        )
                    }
                }
            }
        })

        if (isAdditionalComments) {
            TemplateUIUtil.renderCommentsView(
                additionalComments,
                cardView.addFlowsheetDataEntryLayout,
                this,
                questionnaireDetailViewModel.isDataEditAllowed
            )
        }
        parentView.addView(cardView.root)
    }
    //endregion


    /**
     * Method to render a Section
     */

    private fun createSection(
        feedbackPOJO: QuestionnaireEntryPOJO,
        parentView: LinearLayout
    ): LinearLayout {
        val itemView = ItemQuestionnaireSectionTemplateBinding.inflate(
            LayoutInflater.from(this),
            parentView,
            false
        )
        if (questionnaireDetailViewModel.isDataEditAllowed) {
            itemView.root.tag = tagNo
            questionnaireDetailViewModel.tagToIdMap[tagNo.toString()] =
                feedbackPOJO.questionnaireEntry.entryId
            tagNo++
        }
        val expandableSectionLayout = ExpandableSectionLayout(this)
        expandableSectionLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        expandableSectionLayout.setBackgroundColor(Color.TRANSPARENT)
        val headerView = ItemQuestionnaireSectionHeaderTemplateBinding.inflate(
            LayoutInflater.from(this),
            expandableSectionLayout,
            false
        )
        if (!feedbackPOJO.questionnaireEntry.notes.isNullOrBlank()) {
            headerView.quesSectionHeadingTV.text = feedbackPOJO.questionnaireEntry.notes
        }
        expandableSectionLayout.setHeaderView(
            headerView,
            feedbackPOJO.questionnaireEntry.isCollapsible
        )
        val contentLayout = LinearLayout(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.bottomMargin =
            resources.getDimensionPixelOffset(R.dimen.questionnaire_card_margin_vertical)
        contentLayout.layoutParams = layoutParams
        contentLayout.orientation = LinearLayout.VERTICAL
        contentLayout.setBackgroundColor(Color.TRANSPARENT)
        contentLayout.layoutTransition = LayoutTransition()
        contentLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        expandableSectionLayout.setContentView(
            contentLayout,
            feedbackPOJO.questionnaireEntry.expandByDefault
        )
        itemView.root.addView(expandableSectionLayout)
        parentView.addView(itemView.root)
        return contentLayout
    }


    /**
     * Method to render a FeedBack  rating scale
     */
    private fun createFeedBackRating(
        feedbackPOJO: QuestionnaireEntryPOJO,
        parentView: LinearLayout
    ) {

        val cardView =
            ItemAddFlowsheetLayoutBinding.inflate(LayoutInflater.from(this), parentView, false)
        cardView.addFlowsheetDataHeadingTV.text = feedbackPOJO.questionnaireEntry.notes

        if (feedbackPOJO.questionnaireEntry.isMandatory) {
            UIUtil.makeTextViewMandatory(cardView.addFlowsheetDataHeadingTV)
        }
        if (questionnaireDetailViewModel.isParentSection) {
            TemplateUIUtil.renderSectionCardView(cardView, this)
        }
        val ratingValue =
            feedbackPOJO.questionnaireEntry.answersList?.firstOrNull()?.toIntOrNull() ?: 0
        val desc = feedbackPOJO.questionnaireRating.filter { it.option?.toInt() == ratingValue }
        val textView = TemplateUIUtil.getTextView(
            this,
            desc.firstOrNull()?.optionDescription ?: "",
            marginBottom = resources.getDimensionPixelOffset(R.dimen.card_margin_patient_list_horizontal)
        )
        textView.gravity = Gravity.CENTER
        textView.typeface = UIUtil.getFont(this, R.font.open_sans_medium)
        if (desc.firstOrNull()?.optionDescription.isNullOrBlank()) {
            textView.visibility = View.GONE
        }

        if (questionnaireDetailViewModel.isDataEditAllowed) {
            cardView.addFlowsheetDataEntryLayout.tag = tagNo
            questionnaireDetailViewModel.tagToIdMap[tagNo.toString()] =
                feedbackPOJO.questionnaireEntry.entryId
            tagNo++
        }

        cardView.addFlowsheetDataEntryLayout.addView(ComposeView(this).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MPHRTheme {
                    var rating by rememberSaveable { mutableIntStateOf(ratingValue) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MdRatingScale(
                            rating = rating,
                            spaceBetween = 8.dp,
                            starSize = 48.dp,
                            onRatingChange = {
                                rating = it
                                feedbackPOJO.questionnaireEntry.answersList =
                                    arrayListOf(it.toString())
                                val descVal =
                                    feedbackPOJO.questionnaireRating.filter { intakeQuestTemplateRating -> intakeQuestTemplateRating.option?.toInt() == it }
                                textView.text = descVal.firstOrNull()?.optionDescription ?: ""
                                textView.visibility = View.VISIBLE
                            },
                            enable = questionnaireDetailViewModel.isDataEditAllowed
                        )
                    }
                }
            }
        })

        cardView.addFlowsheetDataEntryLayout.addView(textView)
        parentView.addView(cardView.root)

    }

    /**
     * Method to render a date based edittext
     */
    private fun createDate(feedbackPOJO: QuestionnaireEntryPOJO, parentView: LinearLayout) {
        val cardView =
            ItemAddFlowsheetLayoutBinding.inflate(LayoutInflater.from(this), parentView, false)
        cardView.addFlowsheetDataHeadingTV.text = feedbackPOJO.questionnaireEntry.notes

        if (feedbackPOJO.questionnaireEntry.isMandatory) {
            UIUtil.makeTextViewMandatory(cardView.addFlowsheetDataHeadingTV)
        }
        if (questionnaireDetailViewModel.isParentSection) {
            TemplateUIUtil.renderSectionCardView(cardView, this)
        }
        val dateView =
            ItemAddFlowsheetDateBinding.inflate(LayoutInflater.from(this), parentView, false)

        val questAnsNotes = feedbackPOJO.questionnaireEntry.notes ?: ""
        cardView.addFlowsheetDataHeadingTV.text = questAnsNotes

        if (feedbackPOJO.questionnaireEntry.isMandatory) {
            UIUtil.makeTextViewMandatory(cardView.addFlowsheetDataHeadingTV)
        }

        dateView.addFlowsheetDataDateTV.setText(feedbackPOJO.questionnaireEntry.answersList?.firstOrNull())
        dateView.addFlowsheetDataDateTV.inputType = InputType.TYPE_NULL


        if (questionnaireDetailViewModel.isDataEditAllowed) {
            dateView.addFlowsheetDataDateTV.tag = tagNo
            questionnaireDetailViewModel.tagToIdMap[tagNo.toString()] =
                feedbackPOJO.questionnaireEntry.entryId
            tagNo++

            dateView.addFlowsheetDataDateTV.setOnClickListener {
                showDatePicker(dateView.addFlowsheetDataDateTV, isDOB = false, isInsurance = false)
            }

            dateView.addFlowsheetDataDateTV.setOnFocusChangeListener { _, isFocus ->
                if (isFocus) {
                    showDatePicker(
                        dateView.addFlowsheetDataDateTV,
                        isDOB = false,
                        isInsurance = false
                    )
                }
            }
        } else {
            dateView.addFlowsheetDataDateTV.setCompoundDrawables(null, null, null, null)
            dateView.addFlowsheetDataDateTV.setHint("")
        }
        cardView.addFlowsheetDataEntryLayout.addView(dateView.root)
        parentView.addView(cardView.root)

    }


    private fun createSignature(feedbackPOJO: QuestionnaireEntryPOJO, parentView: LinearLayout) {

        val constraintLayoutMain =
            ItemQuestionnaireSignatureBinding.inflate(LayoutInflater.from(this), parentView, false)
        val screenWidth = UIUtil.getScreenWidth(this)
        val width =
            screenWidth - (2 * this.resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))

        val height = UIUtil.getScreenHeight(this)

        val signatureDialog = MDSignatureDialog(this, width, height / 2)

        signatureDialog.setSignatureClickListener(object :
            MDSignatureDialog.SignatureClickListener {
            override fun saveSignature(transparentSignatureBitmap: Bitmap) {
                var file: File?
                lifecycleScope.launch(Dispatchers.IO) {
                    file = FileUtil.saveImageFromBitmap(
                        transparentSignatureBitmap,
                        this@QuestionnaireDetailActivity,
                        ModelUtil.questionnaireModelId
                    )
                    runOnUiThread {
                        saveSignature(
                            feedbackPOJO,
                            constraintLayoutMain,
                            file,
                            transparentSignatureBitmap
                        )
                    }
                }
            }

        })
        if (questionnaireDetailViewModel.isDataEditAllowed) {
            constraintLayoutMain.root.tag = tagNo
            questionnaireDetailViewModel.tagToIdMap[tagNo.toString()] =
                feedbackPOJO.questionnaireEntry.entryId
            tagNo++

            constraintLayoutMain.questionnaireDetailItemSignatureInfo.setOnClickListener {
                signatureDialog.show()
                signatureDialog.clearSignature()
            }
            constraintLayoutMain.questionnaireDetailItemSignatureSaveBtn.setOnClickListener {
                signatureDialog.show()
                signatureDialog.clearSignature()
            }
            constraintLayoutMain.questionnaireDetailItemSignatureInfo.onFocusChangeListener =
                OnFocusChangeListener { view, hasFocus ->
                    if (hasFocus) {
                        view.performClick()
                    }
                }
        }

        if (feedbackPOJO.questionnaireEntry.answersList?.isNotEmpty() == true) {


            signatureAddedMap[feedbackPOJO.questionnaireEntry.entryId] = true

            val imageLoader = picasso.load(
                APIList.getQuestionnaireSignature(
                    authManager.selectedPatientLive.value?.practicePatientId
                        ?: "",
                    questionnaireDetailViewModel.physicianTemplates.patientQuesMap?.patientQuesMapId,
                    feedbackPOJO.questionnaireEntry.entryId,
                    questionnaireDetailViewModel.physicianTemplates.practiceId
                )
            )

            imageLoader.networkPolicy(NetworkPolicy.NO_CACHE)
            imageLoader.memoryPolicy(MemoryPolicy.NO_CACHE)
            imageLoader.into(object : Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {

                    if (!questionnaireDetailViewModel.isDataEditAllowed) {
                        constraintLayoutMain.questionnaireDetailItemSignatureSaveBtn.visibility =
                            View.GONE
                    }
                }


                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    constraintLayoutMain.questionnaireDetailItemSignaturePad.signatureBitmap =
                        bitmap
                    constraintLayoutMain.questionnaireDetailItemSignatureInfo.visibility = View.GONE
                    constraintLayoutMain.questionnaireDetailItemSignaturePad.isEnabled = false

                    if (!questionnaireDetailViewModel.isDataEditAllowed) {
                        constraintLayoutMain.questionnaireDetailItemSignatureSaveBtn.visibility =
                            View.GONE
                    } else {
                        constraintLayoutMain.questionnaireDetailItemSignatureSaveBtn.visibility =
                            View.VISIBLE
                    }
                }

            })
        } else {
            signatureAddedMap[feedbackPOJO.questionnaireEntry.entryId] = false
            if (!questionnaireDetailViewModel.isDataEditAllowed) {
                constraintLayoutMain.questionnaireDetailItemSignaturePad.isEnabled = false
                constraintLayoutMain.questionnaireDetailItemSignatureInfo.setText(R.string.questionnaire_signature_empty_msg)
            }
        }

        constraintLayoutMain.questionnaireDetailItemSignatureHint.text =
            feedbackPOJO.questionnaireEntry.notes

        if (feedbackPOJO.questionnaireEntry.isMandatory) {
            UIUtil.makeTextViewMandatory(constraintLayoutMain.questionnaireDetailItemSignatureHint)
        }
        parentView.addView(constraintLayoutMain.root)

    }

    private fun saveSignature(
        feedbackPOJO: QuestionnaireEntryPOJO,
        constraintLayoutMain: ItemQuestionnaireSignatureBinding,
        file: File?, transparentSignatureBitmap: Bitmap
    ) {

        if (file?.exists() == true) {

            feedbackPOJO.questionnaireEntry.answersList = arrayListOf(file.path)

            progressDialog.show()
            questionnaireDetailViewModel.addSignature(feedbackPOJO).observe(this) { resp ->

                if (resp != null) {
                    progressDialog.dismiss()
                    if (resp is Boolean) {

                        signatureAddedMap[feedbackPOJO.questionnaireEntry.entryId] = true
                        UIUtil.showToast(
                            this,
                            R.string.questionnaire_signature_success,
                            Toast.LENGTH_LONG
                        )
                        constraintLayoutMain.questionnaireDetailItemSignatureInfo.visibility =
                            View.GONE
                        constraintLayoutMain.questionnaireDetailItemSignaturePad.signatureBitmap =
                            transparentSignatureBitmap
                        constraintLayoutMain.questionnaireDetailItemSignaturePad.isEnabled = false
                        constraintLayoutMain.questionnaireDetailItemSignatureSaveBtn.visibility =
                            View.VISIBLE
                    } else if (resp is APIResponseError) {
                        UIUtil.showToast(this, R.string.error_signature_failed, Toast.LENGTH_LONG)
                    }
                }
            }
        } else {
            UIUtil.showToast(this, R.string.error_occurred_while_handling_file, Toast.LENGTH_LONG)
        }
    }

    private fun createMedicationWidget(
        feedbackPOJO: QuestionnaireEntryPOJO,
        parentView: LinearLayout
    ) {
        val constraintLayoutMain = ItemQuestionnairePrescriptionBinding.inflate(
            LayoutInflater.from(this),
            parentView,
            false
        )

        if (questionnaireDetailViewModel.isDataEditAllowed) {
            constraintLayoutMain.root.tag = tagNo
            questionnaireDetailViewModel.tagToIdMap[tagNo.toString()] =
                feedbackPOJO.questionnaireEntry.entryId
            tagNo++
        }


        if (feedbackPOJO.questionnaireMedicationList.isEmpty()) {

            constraintLayoutMain.questionnaireDetailPrescriptionAddMore.visibility = View.GONE
            val firstChild = ItemQuestionnairePrescriptionEntryBinding.inflate(
                LayoutInflater.from(this),
                constraintLayoutMain.questionnaireDetailPrescriptionMainLayout,
                false
            )

            setPrescriptionView(firstChild, constraintLayoutMain, feedbackPOJO)
            constraintLayoutMain.questionnaireDetailPrescriptionMainLayout.addView(firstChild.root)
        } else {
            constraintLayoutMain.questionnaireDetailPrescriptionAddMore.visibility = View.VISIBLE

            feedbackPOJO.questionnaireMedicationList.forEach { intakeMedEntry ->
                val medicationChildView = ItemQuestionnairePrescriptionEntryBinding.inflate(
                    LayoutInflater.from(this),
                    constraintLayoutMain.questionnaireDetailPrescriptionMainLayout,
                    false
                )

                medicationChildView.questionnaireDetailItemPescriptionHintTV.text =
                    feedbackPOJO.questionnaireEntry.notes
                medicationChildView.questionnaireDetailItemPrescriptionNameLayout.setHint(R.string.medication_drug_name_str)
                medicationChildView.questionnaireDetailItemPrescriptionNameET.setText(intakeMedEntry.drugName)
                medicationChildView.questionnaireDetailItemPrescriptionIntakeDetailsET.setText(
                    intakeMedEntry.intakeDetails
                )
                renderPrescriptionEntrySectionView(medicationChildView)

                if (questionnaireDetailViewModel.isDataEditAllowed) {
                    medicationChildView.questionnaireDetailItemPrescriptionDeleteTV.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.questionnaireDeleteTxtColor
                        )
                    )
                    medicationChildView.questionnaireDetailItemPrescriptionDeleteTV.setOnClickListener {
                        removePrescriptionView(
                            medicationChildView,
                            constraintLayoutMain,
                            feedbackPOJO
                        )
                    }
                } else {
                    medicationChildView.questionnaireDetailItemPrescriptionNameET.isEnabled = false
                    medicationChildView.questionnaireDetailItemPrescriptionIntakeDetailsET.isEnabled =
                        false
                    medicationChildView.questionnaireDetailItemPrescriptionDeleteTV.visibility =
                        View.GONE
                }



                constraintLayoutMain.questionnaireDetailPrescriptionMainLayout.addView(
                    medicationChildView.root
                )
            }
        }

        if (questionnaireDetailViewModel.isDataEditAllowed) {

            constraintLayoutMain.questionnaireDetailPrescriptionAddMore.setOnClickListener {

                val medicationChildView = ItemQuestionnairePrescriptionEntryBinding.inflate(
                    LayoutInflater.from(this),
                    constraintLayoutMain.questionnaireDetailPrescriptionMainLayout,
                    false
                )

                medicationChildView.questionnaireDetailItemPescriptionHintTV.text =
                    feedbackPOJO.questionnaireEntry.notes

                medicationChildView.questionnaireDetailItemPrescriptionNameLayout.setHint(R.string.medication_drug_name_str)

                medicationChildView.questionnaireDetailItemPrescriptionDeleteTV.setOnClickListener {
                    removePrescriptionView(medicationChildView, constraintLayoutMain, feedbackPOJO)
                }
                constraintLayoutMain.questionnaireDetailPrescriptionMainLayout.addView(
                    medicationChildView.root
                )
                renderPrescriptionEntrySectionView(medicationChildView)
            }
        } else {
            constraintLayoutMain.questionnaireDetailPrescriptionAddMore.visibility = View.GONE
        }

        parentView.addView(constraintLayoutMain.root)
    }

    private fun renderPrescriptionEntrySectionView(childView: ItemQuestionnairePrescriptionEntryBinding) {
        if (questionnaireDetailViewModel.isParentSection) {
            childView.root.setBackgroundResource(R.drawable.item_questionnaire_section_bg)
            childView.root.elevation = 0f
        }
    }


    private fun setPrescriptionView(
        firstChild: ItemQuestionnairePrescriptionEntryBinding,
        constraintLayoutMain: ItemQuestionnairePrescriptionBinding,
        feedbackPOJO: QuestionnaireEntryPOJO,
        isMedication: Boolean = true
    ) {

        firstChild.questionnaireDetailItemPrescriptionMainLayout.visibility = View.GONE

        if (questionnaireDetailViewModel.isDataEditAllowed) {
            firstChild.questionnaireDetailItemPrescriptionDeleteTV.setText(R.string.add_str)
            firstChild.questionnaireDetailItemPrescriptionDeleteTV.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimary
                )
            )
            firstChild.questionnaireDetailItemPrescriptionDeleteTV.visibility = View.VISIBLE
        } else {
            firstChild.questionnaireDetailItemPrescriptionDeleteTV.visibility = View.GONE
        }

        firstChild.questionnaireDetailItemPrescriptionNameET.setText("")
        firstChild.questionnaireDetailItemPrescriptionIntakeDetailsET.setText("")

        if (isMedication) {
            firstChild.questionnaireDetailItemPrescriptionNameLayout.setHint(R.string.medication_drug_name_str)
        } else {
            firstChild.questionnaireDetailItemPrescriptionNameLayout.setHint(R.string.supplement_name_str)
        }

        firstChild.questionnaireDetailItemPescriptionHintTV.text =
            feedbackPOJO.questionnaireEntry.notes

        firstChild.questionnaireDetailItemPrescriptionDeleteTV.setOnClickListener {
            firstChild.questionnaireDetailItemPrescriptionMainLayout.visibility = View.VISIBLE
            firstChild.questionnaireDetailItemPrescriptionDeleteTV.setText(R.string.delete_str)
            firstChild.questionnaireDetailItemPrescriptionDeleteTV.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.questionnaireDeleteTxtColor
                )
            )

            constraintLayoutMain.questionnaireDetailPrescriptionAddMore.visibility = View.VISIBLE

            firstChild.questionnaireDetailItemPrescriptionDeleteTV.setOnClickListener {
                removePrescriptionView(firstChild, constraintLayoutMain, feedbackPOJO, isMedication)
            }
        }

        renderPrescriptionEntrySectionView(firstChild)
    }

    private fun removePrescriptionView(
        firstChild: ItemQuestionnairePrescriptionEntryBinding,
        constraintLayoutMain: ItemQuestionnairePrescriptionBinding,
        feedbackPOJO: QuestionnaireEntryPOJO,
        isMedication: Boolean = true
    ) {
        if (constraintLayoutMain.questionnaireDetailPrescriptionMainLayout.childCount == 1) {
            setPrescriptionView(firstChild, constraintLayoutMain, feedbackPOJO, isMedication)
            constraintLayoutMain.questionnaireDetailPrescriptionAddMore.visibility = View.GONE
        } else {
            constraintLayoutMain.questionnaireDetailPrescriptionMainLayout.removeView(firstChild.root)
        }
    }

    private fun createSupplementWidget(
        feedbackPOJO: QuestionnaireEntryPOJO,
        parentView: LinearLayout
    ) {
        val constraintLayoutMain = ItemQuestionnairePrescriptionBinding.inflate(
            LayoutInflater.from(this),
            parentView,
            false
        )

        if (questionnaireDetailViewModel.isDataEditAllowed) {
            constraintLayoutMain.root.tag = tagNo
            questionnaireDetailViewModel.tagToIdMap[tagNo.toString()] =
                feedbackPOJO.questionnaireEntry.entryId
            tagNo++
        }

        constraintLayoutMain.questionnaireDetailPrescriptionAddMore.setText(R.string.questionnaire_detail_add_more_supplement)
        if (feedbackPOJO.questionnaireSupplementList.isEmpty()) {
            constraintLayoutMain.questionnaireDetailPrescriptionAddMore.visibility = View.GONE
            val firstChild = ItemQuestionnairePrescriptionEntryBinding.inflate(
                LayoutInflater.from(this),
                constraintLayoutMain.questionnaireDetailPrescriptionMainLayout,
                false
            )

            setPrescriptionView(firstChild, constraintLayoutMain, feedbackPOJO, false)
            constraintLayoutMain.questionnaireDetailPrescriptionMainLayout.addView(firstChild.root)

        } else {
            constraintLayoutMain.questionnaireDetailPrescriptionAddMore.visibility = View.VISIBLE

            feedbackPOJO.questionnaireSupplementList.forEach { intakeMedEntry ->
                val medicationChildView = ItemQuestionnairePrescriptionEntryBinding.inflate(
                    LayoutInflater.from(this),
                    constraintLayoutMain.questionnaireDetailPrescriptionMainLayout,
                    false
                )

                medicationChildView.questionnaireDetailItemPescriptionHintTV.text =
                    feedbackPOJO.questionnaireEntry.notes
                medicationChildView.questionnaireDetailItemPrescriptionNameLayout.setHint(R.string.supplement_name_str)
                medicationChildView.questionnaireDetailItemPrescriptionNameET.setText(intakeMedEntry.supplementName)
                medicationChildView.questionnaireDetailItemPrescriptionIntakeDetailsET.setText(
                    intakeMedEntry.intakeDetails
                )
                renderPrescriptionEntrySectionView(medicationChildView)

                if (questionnaireDetailViewModel.isDataEditAllowed) {
                    medicationChildView.questionnaireDetailItemPrescriptionDeleteTV.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.questionnaireDeleteTxtColor
                        )
                    )
                    medicationChildView.questionnaireDetailItemPrescriptionDeleteTV.setOnClickListener {
                        removePrescriptionView(
                            medicationChildView,
                            constraintLayoutMain,
                            feedbackPOJO
                        )
                    }
                } else {
                    medicationChildView.questionnaireDetailItemPrescriptionNameET.isEnabled = false
                    medicationChildView.questionnaireDetailItemPrescriptionIntakeDetailsET.isEnabled =
                        false
                    medicationChildView.questionnaireDetailItemPrescriptionDeleteTV.visibility =
                        View.GONE
                }

                constraintLayoutMain.questionnaireDetailPrescriptionMainLayout.addView(
                    medicationChildView.root
                )
            }
        }

        if (questionnaireDetailViewModel.isDataEditAllowed) {

            constraintLayoutMain.questionnaireDetailPrescriptionAddMore.setOnClickListener {
                val medicationChildView = ItemQuestionnairePrescriptionEntryBinding.inflate(
                    LayoutInflater.from(this),
                    constraintLayoutMain.questionnaireDetailPrescriptionMainLayout,
                    false
                )

                medicationChildView.questionnaireDetailItemPescriptionHintTV.text =
                    feedbackPOJO.questionnaireEntry.notes

                medicationChildView.questionnaireDetailItemPrescriptionNameLayout.setHint(R.string.supplement_name_str)

                medicationChildView.questionnaireDetailItemPrescriptionDeleteTV.setOnClickListener {
                    removePrescriptionView(
                        medicationChildView,
                        constraintLayoutMain,
                        feedbackPOJO,
                        false
                    )
                }
                renderPrescriptionEntrySectionView(medicationChildView)
                constraintLayoutMain.questionnaireDetailPrescriptionMainLayout.addView(
                    medicationChildView.root
                )
            }
        } else {
            constraintLayoutMain.questionnaireDetailPrescriptionAddMore.visibility = View.GONE
        }

        parentView.addView(constraintLayoutMain.root)
    }

    private fun createAllergyWidget(
        feedbackPOJO: QuestionnaireEntryPOJO,
        parentView: LinearLayout
    ) {
        val constraintLayoutMain =
            ItemQuestionnaireAllergyBinding.inflate(LayoutInflater.from(this), parentView, false)

        if (questionnaireDetailViewModel.isDataEditAllowed) {
            constraintLayoutMain.root.tag = tagNo
            questionnaireDetailViewModel.tagToIdMap[tagNo.toString()] =
                feedbackPOJO.questionnaireEntry.entryId
            tagNo++
        }


        if (feedbackPOJO.questionnaireAllergyList.isEmpty()) {

            createAllergyEntryObj(
                null,
                false,
                ModelUtil.questionnaireNotesTypeAllergyDrug,
                constraintLayoutMain
            )
            createAllergyEntryObj(
                null,
                false,
                ModelUtil.questionnaireNotesTypeAllergyFood,
                constraintLayoutMain
            )
            createAllergyEntryObj(
                null,
                false,
                ModelUtil.questionnaireNotesTypeAllergyEnvironment,
                constraintLayoutMain
            )

        } else {
            feedbackPOJO.questionnaireAllergyList.forEach { intakeAllergyEntry ->

                createAllergyEntryObj(
                    intakeAllergyEntry, true, intakeAllergyEntry.type
                        ?: "", constraintLayoutMain
                )
            }


            if (constraintLayoutMain.questionnaireDetailAllergyDrugMainLayout.childCount == 0) {
                createAllergyEntryObj(
                    null,
                    false,
                    ModelUtil.questionnaireNotesTypeAllergyDrug,
                    constraintLayoutMain
                )
            }
            if (constraintLayoutMain.questionnaireDetailAllergyFoodMainLayout.childCount == 0) {
                createAllergyEntryObj(
                    null,
                    false,
                    ModelUtil.questionnaireNotesTypeAllergyFood,
                    constraintLayoutMain
                )
            }
            if (constraintLayoutMain.questionnaireDetailAllergyEnvironmentMainLayout.childCount == 0) {
                createAllergyEntryObj(
                    null,
                    false,
                    ModelUtil.questionnaireNotesTypeAllergyEnvironment,
                    constraintLayoutMain
                )
            }
        }

        if (feedbackPOJO.questionnaireOptions.any { it.option == ModelUtil.questionnaireNotesTypeAllergyDrugFN } && questionnaireDetailViewModel.isDataEditAllowed) {

            constraintLayoutMain.questionnaireDetailAllergyDrugAddMore.setOnClickListener {
                createAllergyEntryObj(
                    null,
                    true,
                    ModelUtil.questionnaireNotesTypeAllergyDrug,
                    constraintLayoutMain
                )
            }

        } else {

            if (!feedbackPOJO.questionnaireOptions.any { it.option == ModelUtil.questionnaireNotesTypeAllergyDrugFN }) {
                constraintLayoutMain.questionnaireDetailAllergyDrugMainLayout.visibility = View.GONE
            } else {
                constraintLayoutMain.questionnaireDetailAllergyDrugMainLayout.visibility =
                    View.VISIBLE
            }
            constraintLayoutMain.questionnaireDetailAllergyDrugAddMore.visibility = View.GONE
        }
        if (feedbackPOJO.questionnaireOptions.any { it.option == ModelUtil.questionnaireNotesTypeAllergyFoodFN } && questionnaireDetailViewModel.isDataEditAllowed) {

            constraintLayoutMain.questionnaireDetailAllergyFoodAddMore.setOnClickListener {
                createAllergyEntryObj(
                    null,
                    true,
                    ModelUtil.questionnaireNotesTypeAllergyFood,
                    constraintLayoutMain
                )
            }
        } else {
            if (!feedbackPOJO.questionnaireOptions.any { it.option == ModelUtil.questionnaireNotesTypeAllergyFoodFN }) {
                constraintLayoutMain.questionnaireDetailAllergyFoodMainLayout.visibility = View.GONE
            } else {
                constraintLayoutMain.questionnaireDetailAllergyFoodMainLayout.visibility =
                    View.VISIBLE
            }
            constraintLayoutMain.questionnaireDetailAllergyFoodAddMore.visibility = View.GONE
        }

        if (feedbackPOJO.questionnaireOptions.any { it.option == ModelUtil.questionnaireNotesTypeAllergyEnvironmentFN } && questionnaireDetailViewModel.isDataEditAllowed) {

            constraintLayoutMain.questionnaireDetailAllergyEnvironmentAddMore.setOnClickListener {
                createAllergyEntryObj(
                    null,
                    true,
                    ModelUtil.questionnaireNotesTypeAllergyEnvironment,
                    constraintLayoutMain
                )
            }
        } else {
            if (!feedbackPOJO.questionnaireOptions.any { it.option == ModelUtil.questionnaireNotesTypeAllergyEnvironmentFN }) {
                constraintLayoutMain.questionnaireDetailAllergyEnvironmentMainLayout.visibility =
                    View.GONE
            } else {
                constraintLayoutMain.questionnaireDetailAllergyEnvironmentMainLayout.visibility =
                    View.VISIBLE
            }
            constraintLayoutMain.questionnaireDetailAllergyEnvironmentAddMore.visibility = View.GONE

        }

        parentView.addView(constraintLayoutMain.root)
    }


    private fun createAllergyEntryObj(
        intakeQuestAllergyEntry: IntakeQuestAllergyEntry?,
        toOpen: Boolean,
        allergyType: String,
        layoutMain: ItemQuestionnaireAllergyBinding
    ) {
        val allergyChildView = ItemQuestionnaireAllergyEntryBinding.inflate(
            LayoutInflater.from(this), when (allergyType) {
                ModelUtil.questionnaireNotesTypeAllergyDrug -> {
                    layoutMain.questionnaireDetailAllergyDrugMainLayout
                }

                ModelUtil.questionnaireNotesTypeAllergyFood -> {
                    layoutMain.questionnaireDetailAllergyFoodMainLayout
                }

                else -> {
                    layoutMain.questionnaireDetailAllergyEnvironmentMainLayout
                }
            }, false
        )


        allergyChildView.questionnaireDetailItemAllergyHintTV.text = when (allergyType) {
            ModelUtil.questionnaireNotesTypeAllergyDrug -> getString(R.string.questionnaire_detail_allergy_drug)
            ModelUtil.questionnaireNotesTypeAllergyFood -> getString(R.string.questionnaire_detail_allergy_food)
            else -> getString(R.string.questionnaire_detail_allergy_environment)
        }

        allergyChildView.questionnaireDetailItemAllergySeverityET.setItems(
            arrayListOf(
                ModelUtil.allergySeveritySevere,
                ModelUtil.allergySeverityModerate,
                ModelUtil.allergySeverityMild
            ), this
        )

        if (questionnaireDetailViewModel.isParentSection) {
            allergyChildView.root.setBackgroundResource(R.drawable.item_questionnaire_section_bg)
            allergyChildView.root.elevation = 0f
        }

        if (toOpen) {
            allergyChildView.questionnaireDetailItemAllergyMainLayout.visibility = View.VISIBLE


            if (questionnaireDetailViewModel.isDataEditAllowed) {
                allergyChildView.questionnaireDetailItemAllergyDeleteTV.visibility = View.VISIBLE
                allergyChildView.questionnaireDetailItemAllergyDeleteTV.setText(R.string.delete_str)
                allergyChildView.questionnaireDetailItemAllergyDeleteTV.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.questionnaireDeleteTxtColor
                    )
                )

                allergyChildView.questionnaireDetailItemAllergyDeleteTV.setOnClickListener {
                    removeAllergyView(allergyType, allergyChildView, layoutMain)
                }
            } else {
                allergyChildView.questionnaireDetailItemAllergyDeleteTV.visibility = View.GONE

                allergyChildView.questionnaireDetailItemAllergyNameET.isEnabled = false
                allergyChildView.questionnaireDetailItemAllergySeverityET.isEnabled = false
                allergyChildView.questionnaireDetailItemAllergyReactionsET.isEnabled = false

            }

            if (intakeQuestAllergyEntry != null) {
                allergyChildView.questionnaireDetailItemAllergyNameET.setText(
                    intakeQuestAllergyEntry.allergen
                )
                allergyChildView.questionnaireDetailItemAllergySeverityET.setText(
                    intakeQuestAllergyEntry.severity
                )
                allergyChildView.questionnaireDetailItemAllergyReactionsET.setText(
                    intakeQuestAllergyEntry.reactions
                )

            } else {
                allergyChildView.questionnaireDetailItemAllergyNameET.setText("")
                allergyChildView.questionnaireDetailItemAllergySeverityET.setText("")
                allergyChildView.questionnaireDetailItemAllergyReactionsET.setText("")
            }
        } else {
            setBaseAllergyView(allergyChildView, layoutMain, allergyType)
        }


        when (allergyType) {
            ModelUtil.questionnaireNotesTypeAllergyDrug -> {
                layoutMain.questionnaireDetailAllergyDrugMainLayout.addView(allergyChildView.root)
            }

            ModelUtil.questionnaireNotesTypeAllergyFood -> {
                layoutMain.questionnaireDetailAllergyFoodMainLayout.addView(allergyChildView.root)
            }

            else -> {
                layoutMain.questionnaireDetailAllergyEnvironmentMainLayout.addView(allergyChildView.root)
            }
        }
    }

    private fun setBaseAllergyView(
        allergyChildView: ItemQuestionnaireAllergyEntryBinding,
        layoutMain: ItemQuestionnaireAllergyBinding,
        allergyType: String
    ) {
        allergyChildView.questionnaireDetailItemAllergyMainLayout.visibility = View.GONE

        allergyChildView.questionnaireDetailItemAllergyNameET.setText("")
        allergyChildView.questionnaireDetailItemAllergySeverityET.setText("")
        allergyChildView.questionnaireDetailItemAllergyReactionsET.setText("")


        when (allergyType) {
            ModelUtil.questionnaireNotesTypeAllergyDrug -> {
                layoutMain.questionnaireDetailAllergyDrugAddMore.visibility = View.GONE
            }

            ModelUtil.questionnaireNotesTypeAllergyFood -> {
                layoutMain.questionnaireDetailAllergyFoodAddMore.visibility = View.GONE
            }

            else -> {
                layoutMain.questionnaireDetailAllergyEnvironmentAddMore.visibility = View.GONE
            }
        }

        if (questionnaireDetailViewModel.isDataEditAllowed) {
            allergyChildView.questionnaireDetailItemAllergyDeleteTV.visibility = View.VISIBLE
            allergyChildView.questionnaireDetailItemAllergyDeleteTV.setText(R.string.add_str)
            allergyChildView.questionnaireDetailItemAllergyDeleteTV.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimary
                )
            )

            allergyChildView.questionnaireDetailItemAllergyDeleteTV.setOnClickListener {
                allergyChildView.questionnaireDetailItemAllergyMainLayout.visibility = View.VISIBLE
                allergyChildView.questionnaireDetailItemAllergyDeleteTV.setText(R.string.delete_str)
                allergyChildView.questionnaireDetailItemAllergyDeleteTV.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.questionnaireDeleteTxtColor
                    )
                )
                when (allergyType) {
                    ModelUtil.questionnaireNotesTypeAllergyDrug -> {
                        layoutMain.questionnaireDetailAllergyDrugAddMore.visibility = View.VISIBLE
                    }

                    ModelUtil.questionnaireNotesTypeAllergyFood -> {
                        layoutMain.questionnaireDetailAllergyFoodAddMore.visibility = View.VISIBLE
                    }

                    else -> {
                        layoutMain.questionnaireDetailAllergyEnvironmentAddMore.visibility =
                            View.VISIBLE
                    }
                }

                allergyChildView.questionnaireDetailItemAllergyDeleteTV.setOnClickListener {
                    removeAllergyView(allergyType, allergyChildView, layoutMain)
                }
            }
        } else {
            allergyChildView.questionnaireDetailItemAllergyDeleteTV.visibility = View.GONE
        }
    }


    private fun removeAllergyView(
        allergyType: String,
        allergyChildView: ItemQuestionnaireAllergyEntryBinding,
        layoutMain: ItemQuestionnaireAllergyBinding
    ) {

        val mainLayout = when (allergyType) {
            ModelUtil.questionnaireNotesTypeAllergyDrug -> {
                layoutMain.questionnaireDetailAllergyDrugMainLayout
            }

            ModelUtil.questionnaireNotesTypeAllergyFood -> {
                layoutMain.questionnaireDetailAllergyFoodMainLayout
            }

            else -> {
                layoutMain.questionnaireDetailAllergyEnvironmentMainLayout
            }
        }

        if (mainLayout.childCount == 1) {

            setBaseAllergyView(allergyChildView, layoutMain, allergyType)

            when (allergyType) {
                ModelUtil.questionnaireNotesTypeAllergyDrug -> {
                    layoutMain.questionnaireDetailAllergyDrugAddMore.visibility = View.GONE
                }

                ModelUtil.questionnaireNotesTypeAllergyFood -> {
                    layoutMain.questionnaireDetailAllergyFoodAddMore.visibility = View.GONE
                }

                else -> {
                    layoutMain.questionnaireDetailAllergyEnvironmentAddMore.visibility = View.GONE
                }
            }
        } else {
            mainLayout.removeView(allergyChildView.root)
        }

    }

    private fun createYesOrNoComponent(
        parentView: LinearLayout,
        notes: String?,
        isMandatory: Boolean,
        entryId: String,
        isAddCommentsForNo: Boolean,
        isAddCommentsForYes: Boolean,
        answer: String,
        comments: String,
        tempTagToIdMap: LinkedHashMap<String, String>
    ) {
        val optionsParent =
            ItemAddFlowsheetLayoutBinding.inflate(LayoutInflater.from(this), parentView, false)
        val optionsQuestionsTxt = optionsParent.addFlowsheetDataHeadingTV
        if (questionnaireDetailViewModel.isParentSection) {
            TemplateUIUtil.renderSectionCardView(optionsParent, this)
        }
        optionsQuestionsTxt.text = notes
        if (isMandatory) {
            UIUtil.makeTextViewMandatory(optionsQuestionsTxt)
        }
        val radioGroup = RadioGroup(this)
        radioGroup.layoutParams =
            LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        radioGroup.orientation = RadioGroup.HORIZONTAL
        val yesBtn =
            TemplateUIUtil.createRadioButton(optionsParent, getString(R.string.yes_str), this)
        radioGroup.addView(yesBtn)
        val noBtn =
            TemplateUIUtil.createRadioButton(optionsParent, getString(R.string.no_str), this)
        radioGroup.addView(noBtn)
        if (!questionnaireDetailViewModel.isDataEditAllowed) {
            yesBtn.isEnabled = false
            noBtn.isEnabled = false
        }
        optionsParent.addFlowsheetDataEntryLayout.addView(radioGroup)

        if (answer.equals("yes", true)) {
            yesBtn.isChecked = true
        } else if (answer.equals("no", true)) {
            noBtn.isChecked = true
        }

        if (isAddCommentsForNo || isAddCommentsForYes) {
            val mdTextInputEditText = TemplateUIUtil.getEditTextView(
                this,
                isEnable = questionnaireDetailViewModel.isDataEditAllowed
            )
            mdTextInputEditText.hint = getString(R.string.enter_comments)
            if (isAddCommentsForNo && isAddCommentsForYes) {
                mdTextInputEditText.visibility = View.GONE
                yesBtn.setOnCheckedChangeListener { _, isChecked ->
                    mdTextInputEditText.setText("")
                    if (isChecked && mdTextInputEditText.visibility == View.GONE) {
                        mdTextInputEditText.visibility = View.VISIBLE
                        animateOthersOption(mdTextInputEditText, true)
                    }
                }
                noBtn.setOnCheckedChangeListener { _, isChecked ->
                    mdTextInputEditText.setText("")
                    if (isChecked && mdTextInputEditText.visibility == View.GONE) {
                        mdTextInputEditText.visibility = View.VISIBLE
                        animateOthersOption(mdTextInputEditText, true)
                    }
                }
            } else if (isAddCommentsForNo) {
                noBtn.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        animateOthersOption(mdTextInputEditText, true)
                    } else {
                        animateOthersOption(mdTextInputEditText, false)
                    }
                }
            } else if (isAddCommentsForYes) {
                yesBtn.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        animateOthersOption(mdTextInputEditText, true)
                    } else {
                        animateOthersOption(mdTextInputEditText, false)
                    }
                }
            }
            if (yesBtn.isChecked && isAddCommentsForYes) {
                mdTextInputEditText.visibility = View.VISIBLE
                animateOthersOption(mdTextInputEditText, true)
                if (comments.isNotBlank()) {
                    mdTextInputEditText.setText(comments.substring(16))
                }
            } else if (noBtn.isChecked && isAddCommentsForNo) {
                mdTextInputEditText.visibility = View.VISIBLE
                animateOthersOption(mdTextInputEditText, true)
                if (comments.isNotBlank()) {
                    mdTextInputEditText.setText(comments.substring(15))
                }
            }
            optionsParent.addFlowsheetDataEntryLayout.addView(mdTextInputEditText)
        }

        if (questionnaireDetailViewModel.isDataEditAllowed) {
            radioGroup.tag = tagNo
            tempTagToIdMap[tagNo.toString()] = entryId
            tagNo++
        }
        parentView.addView(optionsParent.root)
    }


    /**
     * Method to render a radio button type widget
     */
    private fun createOptions(
        feedbackPOJO: QuestionnaireEntryPOJO,
        allowOthers: Boolean,
        parentView: LinearLayout
    ) {
        val optionsParent =
            ItemAddFlowsheetLayoutBinding.inflate(LayoutInflater.from(this), parentView, false)
        val optionsQuestionsTxt = optionsParent.addFlowsheetDataHeadingTV
        if (questionnaireDetailViewModel.isParentSection) {
            TemplateUIUtil.renderSectionCardView(optionsParent, this)
        }
        val optionsQuestions = feedbackPOJO.questionnaireEntry.notes ?: ""
        optionsQuestionsTxt.text = optionsQuestions

        if (feedbackPOJO.questionnaireEntry.isMandatory) {
            UIUtil.makeTextViewMandatory(optionsQuestionsTxt)
        }

        if (feedbackPOJO.questionnaireEntry.isMultiChoice) {

            if (questionnaireDetailViewModel.isDataEditAllowed) {
                optionsParent.addFlowsheetDataEntryLayout.tag = tagNo
                questionnaireDetailViewModel.tagToIdMap[tagNo.toString()] =
                    feedbackPOJO.questionnaireEntry.entryId
                tagNo++
            }

            feedbackPOJO.questionnaireOptions.forEachIndexed { index, intakeQuesOptions ->
                val optionsButton = ItemQuestionnaireOptionRadioButtonBinding.inflate(
                    LayoutInflater.from(this),
                    optionsParent.root,
                    false
                )
                optionsButton.root.isSingleLine = false
                optionsButton.root.text = intakeQuesOptions.option
                optionsParent.addFlowsheetDataEntryLayout.addView(optionsButton.root)

                if (index == 0) {
                    val lp = optionsButton.root.layoutParams as ViewGroup.MarginLayoutParams
                    lp.topMargin = lp.bottomMargin
                    optionsButton.root.layoutParams = lp
                }

                if (feedbackPOJO.questionnaireEntry.answersList?.contains(intakeQuesOptions.option) == true) {
                    optionsButton.root.isChecked = true
                }


                if (!questionnaireDetailViewModel.isDataEditAllowed) {
                    optionsButton.root.isEnabled = false
                }

            }

            if (allowOthers) {
                val optionsButton = ItemQuestionnaireOptionRadioButtonBinding.inflate(
                    LayoutInflater.from(this),
                    optionsParent.root,
                    false
                )
                optionsButton.root.isSingleLine = false
                optionsButton.root.text = getString(R.string.Others_no_i18n)

                val mdTextInputEditText =
                    TemplateUIUtil.getEditTextView(
                        this,
                        resources.getDimensionPixelOffset(R.dimen.margin_start_others),
                        ValidationUtil.questOthersMaxLength
                    )

                val otherValue =
                    feedbackPOJO.questionnaireEntry.answersList?.firstOrNull { answer ->
                        !feedbackPOJO.questionnaireOptions.mapNotNull { it.option }
                            .contains(answer) && answer.isNotBlank()
                    }
                mdTextInputEditText.setText(otherValue)

                optionsButton.root.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        animateOthersOption(mdTextInputEditText, true)
                    } else {
                        animateOthersOption(mdTextInputEditText, false)
                    }
                }

                optionsParent.addFlowsheetDataEntryLayout.addView(optionsButton.root)
                optionsParent.addFlowsheetDataEntryLayout.addView(mdTextInputEditText)

                if (!otherValue.isNullOrBlank()) {
                    optionsButton.root.isChecked = true
                }

                if (!questionnaireDetailViewModel.isDataEditAllowed) {
                    optionsButton.root.isEnabled = false
                    mdTextInputEditText.isEnabled = false
                }

            }

        } else {


            val radioGroup = RadioGroup(this)
            radioGroup.layoutParams =
                LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

            if (feedbackPOJO.questionnaireEntry.notesType == ModelUtil.questionnaireNotesTypeYesOrNoOption) {
                radioGroup.orientation = RadioGroup.HORIZONTAL
            }

            feedbackPOJO.questionnaireOptions.forEachIndexed { index, intakeQuesOptions ->
                val optionsButton = ItemQuestionnaireOptionsRadioButton2Binding.inflate(
                    LayoutInflater.from(this),
                    optionsParent.root,
                    false
                )

                if (radioGroup.orientation == RadioGroup.HORIZONTAL) {
                    optionsButton.root.layoutParams?.width = LayoutParams.WRAP_CONTENT
                }
                optionsButton.root.requestLayout()

                optionsButton.root.isSingleLine = false
                optionsButton.root.text = intakeQuesOptions.option
                radioGroup.addView(optionsButton.root)

                if (index == 0 || radioGroup.orientation == RadioGroup.HORIZONTAL) {
                    val lp = optionsButton.root.layoutParams as ViewGroup.MarginLayoutParams
                    lp.topMargin = lp.bottomMargin
                    optionsButton.root.layoutParams = lp
                }

                if (feedbackPOJO.questionnaireEntry.answersList?.contains(intakeQuesOptions.option) == true) {
                    radioGroup.clearCheck()
                    optionsButton.root.isChecked = true
                }

                if (!questionnaireDetailViewModel.isDataEditAllowed) {
                    optionsButton.root.isEnabled = false
                }
            }

            if (questionnaireDetailViewModel.isDataEditAllowed) {
                radioGroup.tag = tagNo
                questionnaireDetailViewModel.tagToIdMap[tagNo.toString()] =
                    feedbackPOJO.questionnaireEntry.entryId
                tagNo++
            }

            optionsParent.addFlowsheetDataEntryLayout.addView(radioGroup)


            if (allowOthers) {
                val optionsButton = ItemQuestionnaireOptionsRadioButton2Binding.inflate(
                    LayoutInflater.from(this),
                    optionsParent.root,
                    false
                )
                optionsButton.root.isSingleLine = false
                optionsButton.root.text = getString(R.string.Others_no_i18n)

                val mdTextInputEditText =
                    TemplateUIUtil.getEditTextView(
                        this,
                        resources.getDimensionPixelOffset(R.dimen.margin_start_others),
                        ValidationUtil.questOthersMaxLength
                    )
                optionsButton.root.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        animateOthersOption(mdTextInputEditText, true)
                    } else {
                        animateOthersOption(mdTextInputEditText, false)
                    }
                }
                radioGroup.addView(optionsButton.root)
                radioGroup.addView(mdTextInputEditText)
                val otherValue =
                    feedbackPOJO.questionnaireEntry.answersList?.firstOrNull { answer ->
                        !feedbackPOJO.questionnaireOptions.mapNotNull { it.option }
                            .contains(answer) && answer.isNotBlank()
                    }


                if (!otherValue.isNullOrBlank()) {
                    radioGroup.clearCheck()
                    optionsButton.root.isChecked = true
                    mdTextInputEditText.setText(otherValue)
                }

                if (!questionnaireDetailViewModel.isDataEditAllowed) {
                    optionsButton.root.isEnabled = false
                    mdTextInputEditText.isEnabled = false
                }

            }


        }

        parentView.addView(optionsParent.root)

    }

    private fun createPersonalDetailsWidget(
        feedbackPOJO: QuestionnaireEntryPOJO,
        parentView: LinearLayout
    ) {

        val parentLayout =
            LayoutPatientDetailsBinding.inflate(LayoutInflater.from(this), parentView, false)

        val patient = questionnaireDetailViewModel.personalDetail


        val patCommonData = questionnaireDetailViewModel.patientCommonData


        parentLayout.patDetailDOBET.inputType = InputType.TYPE_NULL

        parentLayout.patDetailDOBET.setOnClickListener {
            showDatePicker(parentLayout.patDetailDOBET, true)
        }

        parentLayout.patDetailDOBET.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                showDatePicker(parentLayout.patDetailDOBET, true)
            }
        }

        if (questionnaireDetailViewModel.personalDetRequiredTabs.isNotEmpty()) {
            if (questionnaireDetailViewModel.personalDetRequiredTabs.contains("Race")) {
                parentLayout.patDetailRaceLayout.visibility = View.VISIBLE
            } else {
                parentLayout.patDetailRaceLayout.visibility = View.GONE
            }
            if (questionnaireDetailViewModel.personalDetRequiredTabs.contains("Ethnicity")) {
                parentLayout.patDetailEthnicityLayout.visibility = View.VISIBLE
            } else {
                parentLayout.patDetailEthnicityLayout.visibility = View.GONE
            }
            if (questionnaireDetailViewModel.personalDetRequiredTabs.contains("Gender Identity")) {
                parentLayout.patDetailGenderIdLayout.visibility = View.VISIBLE
            } else {
                parentLayout.patDetailGenderIdLayout.visibility = View.GONE
            }
        }

        if (questionnaireDetailViewModel.isParentSection) {
            parentLayout.patDetailDOBLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patDetailGenderLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patDetailGenderIdLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patDetailGenderOtherLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patDetailBloodGrpLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patDetailLanguageLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patDetailRaceLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patDetailEthnicityLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patDetailMStatusLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patDetailEStatusLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patDetailSStatusLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
        }

        UIUtil.makeTextViewMandatory(parentLayout.patDetailFirstNameLayout)
        UIUtil.makeTextViewMandatory(parentLayout.patDetailLastNameLayout)
        UIUtil.makeTextViewMandatory(parentLayout.patDetailDOBLayout)
        UIUtil.makeTextViewMandatory(parentLayout.patDetailGenderLayout)
        UIUtil.makeTextViewMandatory(parentLayout.patDetailGenderOtherLayout)


        if (patCommonData != null && questionnaireDetailViewModel.isDataEditAllowed) {

            parentLayout.patDetailGenderET.setItems(
                arrayListOf(
                    ModelUtil.genderMale,
                    ModelUtil.genderFemale,
                    ModelUtil.genderUnknown
                ), this
            )
            parentLayout.patDetailGenderET.setAnchorView(parentLayout.patDetailGenderLayout)

            val genderIdentityList = ArrayList(patCommonData.genderIdentity)
            genderIdentityList.add(ModelUtil.questionnairePersonalDetailDeclineToSpecify)

            parentLayout.patDetailGenderIdET.setItems(genderIdentityList, this)
            parentLayout.patDetailGenderIdET.setAnchorView(parentLayout.patDetailGenderIdLayout)


            parentLayout.patDetailGenderIdET.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable?) {
                    val text = editable?.toString() ?: ""

                    if (text.equals("other", true)) {
                        parentLayout.patDetailGenderOtherLayout.visibility = View.VISIBLE
                    } else {
                        parentLayout.patDetailGenderOtherLayout.visibility = View.GONE
                    }

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })

            parentLayout.patDetailBloodGrpET.setItems(patCommonData.bloodGrpList, this)
            parentLayout.patDetailBloodGrpET.setAnchorView(parentLayout.patDetailBloodGrpLayout)

//            val raceList = ArrayList(patCommonData.race)
            //raceList.add(ModelUtil.questionnairePersonalDetailDeclineToSpecify)


            parentLayout.patDetailRaceET.inputType = InputType.TYPE_NULL


            val ethnicityList = arrayListOf<String>()

            ethnicityList.addAll(patCommonData.ethnicity)
            ethnicityList.add(ModelUtil.questionnairePersonalDetailDeclineToSpecify)

            parentLayout.patDetailEthnicityET.setItems(ethnicityList, this)
            parentLayout.patDetailEthnicityET.setAnchorView(parentLayout.patDetailEthnicityLayout)

            val maritalStatusList = arrayListOf<String>()
            maritalStatusList.addAll(patCommonData.maritalStatus)
            parentLayout.patDetailMStatusET.setItems(maritalStatusList, this)
            parentLayout.patDetailMStatusET.setAnchorView(parentLayout.patDetailMStatusLayout)

            val employmentStatusList = arrayListOf<String>()
            employmentStatusList.addAll(patCommonData.employmentStatus)
            parentLayout.patDetailEStatusET.setItems(employmentStatusList, this)
            parentLayout.patDetailEStatusET.setAnchorView(parentLayout.patDetailEStatusLayout)

            val smokingStatusList = arrayListOf<String>()
            smokingStatusList.addAll(patCommonData.smokingStatus)
            parentLayout.patDetailSStatusET.setItems(smokingStatusList, this)
            parentLayout.patDetailSStatusET.setAnchorView(parentLayout.patDetailSStatusLayout)

        }

        if (questionnaireDetailViewModel.isDataEditAllowed) {

            val languageAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())
            parentLayout.patDetailLanguageET.setAdapter(languageAdapter)

            val languageList = ArrayList(questionnaireDetailViewModel.languageList)
            languageList.add(ModelUtil.questionnairePersonalDetailDeclineToSpecify)
            languageAdapter.setItemList(languageList)


        }

        if (patient != null) {

            parentLayout.patDetailFirstNameET.setText(patient.firstName)
            parentLayout.patDetailLastNameET.setText(patient.lastName)
            parentLayout.patDetailDOBET.setText(patient.dateOfBirth?.toString(ModelUtil.charmDisplayDateFormat()))
            parentLayout.patDetailGenderET.setText(patient.gender?.replaceFirstChar { it.uppercase() })

            val genderId = patient.genderIdentity ?: ""

            if (genderId.startsWith("other", true)) {
                val genderIdArr = genderId.split(":")
                parentLayout.patDetailGenderIdET.setText(genderIdArr.firstOrNull())
                parentLayout.patDetailGenderOtherET.setText(genderIdArr.filterIndexed { index, _ -> index != 0 }
                    .joinToString(":"))

            } else {
                parentLayout.patDetailGenderIdET.setText(genderId)
            }

            parentLayout.patDetailBloodGrpET.setText(patient.bloodGroup)
            parentLayout.patDetailLanguageET.setText(patient.language)
            parentLayout.patDetailRaceET.setText(patient.race ?: "")
            parentLayout.patDetailEthnicityET.setText(patient.ethnicity)
            parentLayout.patDetailMStatusET.setText(patient.maritalStatus)
            parentLayout.patDetailEStatusET.setText(patient.employmentStatus)
            parentLayout.patDetailSStatusET.setText(patient.smokingStatus)
        }




        if (questionnaireDetailViewModel.isDataEditAllowed) {

            parentLayout.root.tag = tagNo
            questionnaireDetailViewModel.tagToIdMap[tagNo.toString()] =
                feedbackPOJO.questionnaireEntry.entryId
            tagNo++


            parentLayout.patDetailRaceET.setOnClickListener {
                showRaceDialog(parentLayout.patDetailRaceET)
            }

            parentLayout.patDetailRaceET.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    showRaceDialog(parentLayout.patDetailRaceET)
                }
            }

            if (!patient?.firstName.isNullOrBlank()) {
                parentLayout.patDetailFirstNameET.isEnabled = false
                if (questionnaireDetailViewModel.isParentSection) {
                    parentLayout.patDetailFirstNameLayout.elevation = 0f
                }
            }

            if (!patient?.lastName.isNullOrBlank()) {
                parentLayout.patDetailLastNameET.isEnabled = false
                if (questionnaireDetailViewModel.isParentSection) {
                    parentLayout.patDetailLastNameLayout.elevation = 0f
                }
            }


        } else {
            if (questionnaireDetailViewModel.isParentSection) {
                parentLayout.patDetailFirstNameLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
                parentLayout.patDetailLastNameLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            }
            parentLayout.patDetailFirstNameET.isEnabled = false
            parentLayout.patDetailLastNameET.isEnabled = false
            parentLayout.patDetailDOBET.isEnabled = false
            parentLayout.patDetailGenderET.isEnabled = false
            parentLayout.patDetailGenderIdET.isEnabled = false
            parentLayout.patDetailBloodGrpET.isEnabled = false
            parentLayout.patDetailLanguageET.isEnabled = false
            parentLayout.patDetailRaceET.isEnabled = false
            parentLayout.patDetailEthnicityET.isEnabled = false
            parentLayout.patDetailMStatusET.isEnabled = false
            parentLayout.patDetailEStatusET.isEnabled = false
            parentLayout.patDetailSStatusET.isEnabled = false
        }

        parentView.addView(parentLayout.root)

    }

    private fun createPrimaryContactWidget(
        feedbackPOJO: QuestionnaireEntryPOJO,
        parentView: LinearLayout
    ) {
        val parentLayout =
            LayoutPatientContactBinding.inflate(LayoutInflater.from(this), parentView, false)

        val patient = questionnaireDetailViewModel.patContactDetails
        val contactDetails = questionnaireDetailViewModel.patContactDetails?.contactDetails
        val postalAddress = questionnaireDetailViewModel.patContactDetails?.postalAddress

        val patCommonData = questionnaireDetailViewModel.patientCommonData

        val countryAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())
        val stateAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())

        parentLayout.patContactCountryET.setAdapter(countryAdapter)
        parentLayout.patContactStateET.setAdapter(stateAdapter)

        parentLayout.patContactPrimaryPhET.setItems(
            arrayListOf(
                getString(R.string.save_pat_demo_home_no_str),
                getString(R.string.save_pat_demo_mobile_no_str),
                getString(R.string.add_insurance_work_phone_label)
            ), this
        )
        parentLayout.patContactPrimaryPhET.setAnchorView(parentLayout.patContactPrimaryPhLayout)


        UIUtil.makeTextViewMandatory(parentLayout.patContactEmailLayout)
        UIUtil.makeTextViewMandatory(parentLayout.patContactLine1Layout)
        UIUtil.makeTextViewMandatory(parentLayout.patContactCityLayout)
        UIUtil.makeTextViewMandatory(parentLayout.patContactCountryLayout)
        UIUtil.makeTextViewMandatory(parentLayout.patContactStateLayout)
        UIUtil.makeTextViewMandatory(parentLayout.patContactPinCodeLayout)

        if (questionnaireDetailViewModel.isParentSection) {
            parentLayout.patContactHomePhoneLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactMobilePhoneLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactWorkPhoneLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactWorkPhoneExtLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactFaxLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactEmailLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactPrimaryPhLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactLine1Layout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactLine2Layout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactCityLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactCountryLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactStateLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactPinCodeLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactPostBoxLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactEmgContactNameLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactEmgContactNumLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.patContactEmgContactExtLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
        }

        if (patCommonData != null) {

            countryAdapter.setItemList(patCommonData.country.map { it.name })

        }


        if (patient != null) {
            parentLayout.patContactEmgContactNameET.setText(patient.emergencyContactName)
            parentLayout.patContactEmgContactNumET.setText(patient.emergencyContactNumber)
            parentLayout.patContactEmgContactExtET.setText(patient.emergencyContactExtn)
        }

        if (contactDetails != null) {

            parentLayout.patContactHomePhoneET.setText(contactDetails.homePhone)
            parentLayout.patContactWorkPhoneET.setText(contactDetails.workPhone)
            parentLayout.patContactMobilePhoneET.setText(contactDetails.mobile)
            parentLayout.patContactWorkPhoneExtET.setText(contactDetails.workPhoneExtn)
            parentLayout.patContactFaxET.setText(contactDetails.fax)
            parentLayout.patContactEmailET.setText(contactDetails.emailId)


            Log.d("Primary phone", "Phone ${contactDetails.primaryPhone}")

            if (!contactDetails.primaryPhone.isNullOrBlank()) {
                if (contactDetails.primaryPhone.equals("C", true)) {
                    parentLayout.patContactPrimaryPhET.position = 1
                } else if (contactDetails.primaryPhone.equals("W", true)) {
                    parentLayout.patContactPrimaryPhET.position = 2
                } else if (contactDetails.primaryPhone.equals("H", true)) {
                    parentLayout.patContactPrimaryPhET.position = 0
                }

            }
        }

        if (postalAddress != null) {
            parentLayout.patContactCountryET.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable?) {
                    if (editable.toString().trim() != "") {
                        val textInputLayout =
                            (parentLayout.patContactCountryET.parent as ViewGroup).parent as TextInputLayout
                        if (parentLayout.patContactCountryET.isGravityCenter) {
                            TransitionManager.beginDelayedTransition(parentLayout.patContactCountryET.parent as ViewGroup)
                            parentLayout.patContactCountryET.gravity = Gravity.BOTTOM
                            textInputLayout.setPadding(
                                parentLayout.patContactCountryET.edittextPadding,
                                parentLayout.patContactCountryET.edittextPadding,
                                parentLayout.patContactCountryET.edittextPadding,
                                parentLayout.patContactCountryET.edittextPadding
                            )
                            parentLayout.patContactCountryET.isGravityCenter = false
                        }
                    }

                    val countryKey = patCommonData?.country?.firstOrNull {
                        it.name.equals(
                            editable.toString(),
                            true
                        )
                    }?.key
                        ?: ""

                    if (editable.toString().equals("united states", ignoreCase = true)) {
                        parentLayout.patContactPinCodeLayout.hint =
                            getString(R.string.save_pat_demo_zip_code_str)
                    } else {
                        parentLayout.patContactPinCodeLayout.hint =
                            getString(R.string.save_pat_demo_postal_code_str)
                    }
                    if (ModelUtil.patientStateList.contains(countryKey)) {
                        val stateList = questionnaireDetailViewModel.stateList.firstOrNull {
                            it.countryCode == countryKey
                        }
                        if (stateList != null && stateList.stateList.isNotEmpty()) {
                            UIUtil.setStateAdapter(
                                editable.toString(),
                                parentLayout.patContactStateLayout,
                                parentLayout.patContactStateET,
                                parentLayout.patContactPinCodeET,
                                stateAdapter,
                                stateList.stateList,
                                parentLayout.patContactPostBoxLayout,
                                parentLayout.patContactPostBoxET
                            )
                        } else {
                            UIUtil.applyCountryFilter(
                                editable.toString(),
                                parentLayout.patContactPinCodeET,
                                parentLayout.patContactPostBoxLayout,
                                parentLayout.patContactPostBoxET
                            )
                            parentLayout.patContactStateET.setText("")
                            fetchStates(countryKey, stateAdapter)
                        }
                    } else {
                        UIUtil.setStateAdapter(
                            editable.toString(),
                            parentLayout.patContactStateLayout,
                            parentLayout.patContactStateET,
                            parentLayout.patContactPinCodeET,
                            stateAdapter,
                            arrayListOf()
                        )
                    }
                }


                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })

            parentLayout.patContactLine1ET.setText(postalAddress.line1)
            parentLayout.patContactLine2ET.setText(postalAddress.line2)
            parentLayout.patContactCityET.setText(postalAddress.city)
            parentLayout.patContactCountryET.setText(patCommonData?.country?.firstOrNull {
                it.key.equals(
                    postalAddress.country,
                    true
                )
            }?.name)
            parentLayout.patContactStateET.setText(postalAddress.state)
            parentLayout.patContactPinCodeET.setText(postalAddress.postalCode)
            parentLayout.patContactPostBoxET.setText(postalAddress.postbox)

            if (ModelUtil.patientStateList.contains(postalAddress.country)) {
                val stateList =
                    questionnaireDetailViewModel.stateList.firstOrNull { it.countryCode == postalAddress.country }
                stateAdapter.setItemList(stateList?.stateList ?: arrayListOf())
            } else {
                stateAdapter.setItemList(arrayListOf())
            }

            if (questionnaireDetailViewModel.isDataEditAllowed) {

                parentLayout.root.tag = tagNo
                questionnaireDetailViewModel.tagToIdMap[tagNo.toString()] =
                    feedbackPOJO.questionnaireEntry.entryId
                tagNo++

            } else {
                parentLayout.patContactHomePhoneET.isEnabled = false
                parentLayout.patContactWorkPhoneET.isEnabled = false
                parentLayout.patContactMobilePhoneET.isEnabled = false
                parentLayout.patContactWorkPhoneExtET.isEnabled = false
                parentLayout.patContactFaxET.isEnabled = false
                parentLayout.patContactEmailET.isEnabled = false
                parentLayout.patContactPrimaryPhET.isEnabled = false

                parentLayout.patContactLine1ET.isEnabled = false
                parentLayout.patContactLine2ET.isEnabled = false
                parentLayout.patContactCityET.isEnabled = false
                parentLayout.patContactCountryET.isEnabled = false
                parentLayout.patContactStateET.isEnabled = false
                parentLayout.patContactPinCodeET.isEnabled = false
                parentLayout.patContactPostBoxET.isEnabled = false

                parentLayout.patContactEmgContactNumET.isEnabled = false
                parentLayout.patContactEmgContactNameET.isEnabled = false
                parentLayout.patContactEmgContactExtET.isEnabled = false
            }
        }

        parentView.addView(parentLayout.root)

    }

    private fun createPatientInsurance(
        feedbackPOJO: QuestionnaireEntryPOJO,
        parentView: LinearLayout
    ) {
        val parentLayout =
            LayoutPatientInsuranceBinding.inflate(LayoutInflater.from(this), parentView, false)
        parentLayout.root.setPadding(
            parentLayout.root.paddingLeft,
            0,
            parentLayout.root.paddingRight,
            0
        )
//
//        val lp = parentLayout.root.layoutParams as ViewGroup.MarginLayoutParams
//        lp.topMargin = resources.getDimensionPixelSize(R.dimen.card_padding)
//        parentLayout.root.layoutParams = lp

        parentLayout.addInsuranceMainLabel.visibility = View.VISIBLE


        parentLayout.addInsuranceHasInsuranceLayout.visibility = View.VISIBLE
        // parentLayout.addInsuranceMainLabel.visibility = View.GONE
        parentLayout.addInsurancePracticeLayout.visibility = View.GONE
        parentLayout.addInsurancePayerAddressLabel.visibility = View.GONE
        parentLayout.addInsurancePayerLine1Layout.visibility = View.GONE
        parentLayout.addInsurancePayerCityLayout.visibility = View.GONE
        parentLayout.addInsurancePayerCountryLayout.visibility = View.GONE
        parentLayout.addInsurancePayerStateLayout.visibility = View.GONE
        parentLayout.addInsurancePayerPinCodeLayout.visibility = View.GONE
        parentLayout.addInsuranceTypeRadioGrp.visibility = View.GONE
        parentLayout.addInsuranceInsuranceTypeLabel.visibility = View.GONE
        parentLayout.addInsuranceTypeLayout.visibility = View.GONE
        parentLayout.addInsuranceTypeUSLayout.visibility = View.VISIBLE

        parentView.addView(parentLayout.root)

        if (questionnaireDetailViewModel.isParentSection) {
            parentLayout.addInsuranceHasInsuranceLayout.elevation = 0f
            parentLayout.addInsuranceHasInsuranceLayout.radius = 0F
            val layoutParam =
                parentLayout.addInsuranceHasInsuranceLayout.layoutParams as LinearLayout.LayoutParams
            layoutParam.setMargins(0, 0, 0, 0)
            layoutParam.marginStart =
                resources.getDimensionPixelOffset(R.dimen.questionnaire_section_card_margin_horizontal)
            layoutParam.marginEnd =
                resources.getDimensionPixelOffset(R.dimen.questionnaire_section_card_margin_horizontal)
            parentLayout.addInsuranceHasInsuranceSepView.visibility = View.GONE

            parentLayout.addInsuranceTypePrimary.elevation = 0f
            parentLayout.addInsuranceTypeSecondary.elevation = 0f

//            if(questionnaireDetailViewModel.isDataEditAllowed)
//            {
            parentLayout.addInsurancePracticeLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsurancePayerNameLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsurancePayerIdLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceTypeUSLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceMemberIdLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsurancePlanLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsurancePolicyGrpLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceEmpNameLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsurancePayerLine1Layout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsurancePayerCityLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsurancePayerCountryLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsurancePayerStateLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsurancePayerPinCodeLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceRelationshipLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceFirstNameLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceLastNameLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceDOBLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceGenderLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceLine1Layout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceLine2Layout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceCityLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceCountryLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceStateLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsurancePinCodeLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceHomePhoneLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceMobilePhoneLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceStartDateLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceEndDateLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceCopayLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceDeductableLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
            parentLayout.addInsuranceCommentLayout.setBackgroundResource(R.drawable.item_add_flowsheet_et_bg)
//            }
//            else
//            {
//                parentLayout.addInsuranceTypeLayout.elevation=0f
//                parentLayout.addInsurancePayerIdLayout.elevation=0f
//                parentLayout.addInsurancePayerNameLayout.elevation=0f
//                parentLayout.addInsurancePlanLayout.elevation=0f
//                parentLayout.addInsuranceMemberIdLayout.elevation=0f
//                parentLayout.addInsurancePolicyGrpLayout.elevation=0f
//                parentLayout.addInsuranceStartDateLayout.elevation=0f
//                parentLayout.addInsuranceEndDateLayout.elevation=0f
//                parentLayout.addInsuranceCopayLayout.elevation=0f
//                parentLayout.addInsuranceDeductableLayout.elevation=0f
//                parentLayout.addInsuranceCommentLayout.elevation=0f
//
//                parentLayout.addInsuranceEmpNameLayout.elevation=0f
//
//                parentLayout.addInsuranceFirstNameLayout.elevation=0f
//                parentLayout.addInsuranceLastNameLayout.elevation=0f
//                parentLayout.addInsuranceRelationshipLayout.elevation=0f
//                parentLayout.addInsuranceDOBLayout.elevation=0f
//                parentLayout.addInsuranceGenderLayout.elevation=0f
//
//                parentLayout.addInsuranceLine1Layout.elevation=0f
//                parentLayout.addInsuranceLine2Layout.elevation=0f
//                parentLayout.addInsuranceCityLayout.elevation=0f
//                parentLayout.addInsuranceCountryLayout.elevation=0f
//                parentLayout.addInsuranceStateLayout.elevation=0f
//                parentLayout.addInsurancePinCodeLayout.elevation=0f
//
//
//                parentLayout.addInsuranceHomePhoneLayout.elevation=0f
//                parentLayout.addInsuranceMobilePhoneLayout.elevation=0f
//            }
        }


        UIUtil.makeTextViewMandatory(parentLayout.addInsuranceTypeUSLayout)
        UIUtil.makeTextViewMandatory(parentLayout.addInsuranceMemberIdLayout)
        UIUtil.makeTextViewMandatory(parentLayout.addInsurancePayerNameLayout)
        UIUtil.makeTextViewMandatory(parentLayout.addInsurancePolicyGrpLayout)
        UIUtil.makeTextViewMandatory(parentLayout.addInsuranceRelationshipLayout)
        UIUtil.makeTextViewMandatory(parentLayout.addInsuranceFirstNameLayout)
        UIUtil.makeTextViewMandatory(parentLayout.addInsuranceLastNameLayout)
        UIUtil.makeTextViewMandatory(parentLayout.addInsuranceDOBLayout)
        UIUtil.makeTextViewMandatory(parentLayout.addInsuranceGenderLayout)


        parentLayout.addInsuranceHasInsuranceRadioGrp.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.addInsuranceHasInsuranceYesRadioBtn) {
                parentLayout.addInsuranceDataMainLayout.visibility = View.VISIBLE
            } else {
                parentLayout.addInsuranceDataMainLayout.visibility = View.GONE
            }
        }

        val answer = feedbackPOJO.questionnaireEntry.answersList?.firstOrNull()

        if (!answer.isNullOrBlank()) {
            if (answer == ModelUtil.questionnaireNoInsuranceResp) {
                parentLayout.addInsuranceHasInsuranceRadioGrp.check(R.id.addInsuranceHasInsuranceNoRadioBtn)
            } else {
                parentLayout.addInsuranceHasInsuranceRadioGrp.check(R.id.addInsuranceHasInsuranceYesRadioBtn)
            }
        } else {
            parentLayout.addInsuranceHasInsuranceRadioGrp.check(R.id.addInsuranceHasInsuranceYesRadioBtn)
        }




        parentLayout.root.tag = tagNo
        questionnaireDetailViewModel.tagToIdMap[tagNo.toString()] =
            feedbackPOJO.questionnaireEntry.entryId
        insuranceTag = tagNo
        tagNo++

        val patientInsuranceDetails = questionnaireDetailViewModel.patientInsuranceDetails

        if (questionnaireDetailViewModel.isDataEditAllowed) {

            val countryAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())
            val stateAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())

            if (patientInsuranceDetails?.isNewPayerAllowed == true) {
                val insuranceNameAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())

                val inputFinishChecker = Runnable {
                    // to send request only there is a definite time difference between typing
                    val newText = parentLayout.addInsurancePayerNameET.text?.toString()
                    if (newText.isNullOrBlank()) {
                        insuranceNameAdapter.setItemList(arrayListOf())
                    } else {
                        questionnaireDetailViewModel.getPayerList(newText)
                    }
                }
                val textWatcher = object : TextWatcher {
                    override fun afterTextChanged(p0: Editable?) {

                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                        insuranceNameAdapter.setItemList(arrayListOf())
                        performPayerSearch(inputFinishChecker, false)
                    }
                }

                parentLayout.addInsurancePayerNameET.setAdapter(insuranceNameAdapter)

                parentLayout.addInsurancePayerNameET.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ -> onPayerChange(position) }

                insuranceNameAdapter.setItemList(arrayListOf())
                parentLayout.addInsurancePayerNameET.addTextChangedListener(textWatcher)

                questionnaireDetailViewModel.payerListLive.observe(this) { resp ->
                    if (resp != null) {
                        if (resp is ArrayList<*>) {

                            questionnaireDetailViewModel.payerList.clear()
                            questionnaireDetailViewModel.payerList.addAll(resp as ArrayList<PracticePayer>)

                            insuranceNameAdapter.setItemList((questionnaireDetailViewModel.payerList.mapNotNull { it.payerName }))
                            insuranceNameAdapter.filter.filter(parentLayout.addInsurancePayerNameET.text.toString())
                            parentLayout.addInsurancePayerNameET.setAdapter(insuranceNameAdapter)


                        } else if (resp is APIResponseError) {

                        }
                    }
                }

            } else {
                parentLayout.addInsurancePayerNameLayout.visibility = View.GONE
                parentLayout.addInsurancePayerNameSelectableLayout.visibility = View.VISIBLE
                parentLayout.addInsurancePayerIdLayout.visibility = View.GONE
                parentLayout.addInsurancePayerNameSelector.setItems(
                    (patientInsuranceDetails?.practicePayerList?.map { it.payerName }),
                    this
                )
                parentLayout.addInsurancePayerNameSelector.setAnchorView(parentLayout.addInsurancePayerNameSelectableLayout)
            }

            parentLayout.addInsuranceCountryET.setAdapter(countryAdapter)
            parentLayout.addInsuranceStateET.setAdapter(stateAdapter)


            parentLayout.addInsuranceDOBET.inputType = InputType.TYPE_NULL

            parentLayout.addInsuranceDOBET.setOnClickListener {
                showDatePicker(parentLayout.addInsuranceDOBET, true)
            }

            parentLayout.addInsuranceDOBET.setOnFocusChangeListener { _, isFocus ->
                if (isFocus) {
                    showDatePicker(parentLayout.addInsuranceDOBET, true)
                }
            }

            parentLayout.addInsuranceStartDateET.inputType = InputType.TYPE_NULL
            parentLayout.addInsuranceStartDateET.setOnClickListener {
                showDatePicker(parentLayout.addInsuranceStartDateET, isInsurance = true)
            }
            parentLayout.addInsuranceStartDateET.setOnFocusChangeListener { _, isFocus ->
                if (isFocus) {
                    showDatePicker(parentLayout.addInsuranceStartDateET, isInsurance = true)
                }
            }

            parentLayout.addInsuranceEndDateET.inputType = InputType.TYPE_NULL
            parentLayout.addInsuranceEndDateET.setOnClickListener {
                showDatePicker(parentLayout.addInsuranceEndDateET, isInsurance = true)
            }
            parentLayout.addInsuranceEndDateET.setOnFocusChangeListener { _, isFocus ->
                if (isFocus) {
                    showDatePicker(parentLayout.addInsuranceEndDateET, isInsurance = true)
                }
            }


//            parentLayout.addInsuranceTypeUSET.setOnItemClickListener { _, str ->
//                val insuranceTypeChange = UIUtil.onInsuranceTypeChange(str)
//                parentLayout.addInsuranceMemberIdLayout.hint =
//                    getString(insuranceTypeChange.memberIdLabel)
//                UIUtil.makeTextViewMandatory(parentLayout.addInsuranceMemberIdLayout)
//                parentLayout.addInsurancePlanET.setText(insuranceTypeChange.planName)
//            }

            parentLayout.addInsuranceCountryET.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {

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

                    val countryKey =
                        questionnaireDetailViewModel.insuranceCommonData?.country?.firstOrNull {
                            it.name.equals(
                                editable.toString(),
                                true
                            )
                        }?.key
                            ?: ""
                    val stateList =
                        questionnaireDetailViewModel.insuranceCommonData?.stateList?.firstOrNull {
                            it.countryCode == countryKey
                        }

                    UIUtil.setStateAdapter(
                        editable.toString(),
                        parentLayout.addInsuranceStateLayout,
                        parentLayout.addInsuranceStateET,
                        parentLayout.addInsurancePinCodeET,
                        stateAdapter, stateList?.stateList ?: arrayListOf()
                    )
                }
            })

            parentLayout.addInsuranceRelationshipET.setOnItemClickListener { _, str ->
                onRelationshipChange(str.equals("SELF", true))
            }

            parentLayout.addInsuranceAddressCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onInsuredAddressCheckChange(isChecked)
            }

            val insuranceCommonData = questionnaireDetailViewModel.insuranceCommonData

            if (insuranceCommonData != null) {
                countryAdapter.setItemList(insuranceCommonData.country.map { it.name })

                parentLayout.addInsuranceGenderET.setItems(
                    arrayListOf(
                        ModelUtil.genderMale,
                        ModelUtil.genderFemale,
                        ModelUtil.genderUnknown
                    ), this
                )
                parentLayout.addInsuranceGenderET.setAnchorView(parentLayout.addInsuranceGenderLayout)
                parentLayout.addInsuranceTypeUSET.setItems(
                    insuranceCommonData.insuranceTypes,
                    this
                )
                parentLayout.addInsuranceTypeUSET.setAnchorView(parentLayout.addInsuranceTypeUSLayout)
                parentLayout.addInsuranceRelationshipET.setItems(
                    insuranceCommonData.relationships,
                    this
                )
                parentLayout.addInsuranceRelationshipET.setAnchorView(parentLayout.addInsuranceRelationshipLayout)
            }

        } else {

            parentLayout.addInsuranceHasInsuranceRadioGrp.isEnabled = false
            parentLayout.addInsuranceHasInsuranceYesRadioBtn.isEnabled = false
            parentLayout.addInsuranceHasInsuranceNoRadioBtn.isEnabled = false

            parentLayout.addInsuranceDataMainLayout.isEnabled = false
            parentLayout.addInsuranceTypeET.isEnabled = false
            parentLayout.addInsuranceTypeUSET.isEnabled = false
            parentLayout.addInsurancePayerIdET.isEnabled = false
            parentLayout.addInsurancePayerNameET.isEnabled = false
            parentLayout.addInsurancePlanET.isEnabled = false
            parentLayout.addInsuranceMemberIdET.isEnabled = false
            parentLayout.addInsurancePolicyGrpET.isEnabled = false
            parentLayout.addInsuranceStartDateET.isEnabled = false
            parentLayout.addInsuranceEndDateET.isEnabled = false
            parentLayout.addInsuranceCopayET.isEnabled = false
            parentLayout.addInsuranceDeductableET.isEnabled = false
            parentLayout.addInsuranceCommentET.isEnabled = false

            parentLayout.addInsuranceEmpNameET.isEnabled = false
        }


        patientInsuranceDetails?.let { insurance ->

            parentLayout.addInsuranceTypeUSET.setText(insurance.insurancePlanType)
            parentLayout.addInsurancePlanET.setText(insurance.insurancePlanName)
            parentLayout.addInsuranceMemberIdET.setText(insurance.policyNumber)
            parentLayout.addInsurancePolicyGrpET.setText(insurance.groupNumber)
            parentLayout.addInsuranceStartDateET.setText(
                insurance.policyStartDate?.toString(
                    ModelUtil.charmDisplayDateFormat()
                )
            )
            parentLayout.addInsuranceEndDateET.setText(insurance.policyEndDate?.toString(ModelUtil.charmDisplayDateFormat()))
            parentLayout.addInsuranceCopayET.setText(insurance.copay)
            parentLayout.addInsuranceDeductableET.setText(insurance.deductable)
            parentLayout.addInsuranceCommentET.setText(insurance.comment)


            val insuredAndGuarantorDetails = insurance.insuredAndGuarantorDetails

            if (insuredAndGuarantorDetails != null) {

                parentLayout.addInsuranceEmpNameET.setText(insuredAndGuarantorDetails.insuredEmployerDetails?.name)

                parentLayout.addInsuranceFirstNameET.setText(insuredAndGuarantorDetails.firstName)
                parentLayout.addInsuranceLastNameET.setText(insuredAndGuarantorDetails.lastName)
                parentLayout.addInsuranceRelationshipET.setText(insuredAndGuarantorDetails.relationship)
                parentLayout.addInsuranceDOBET.setText(
                    insuredAndGuarantorDetails.dateOfBirth?.toString(
                        ModelUtil.charmDisplayDateFormat()
                    )
                )
                parentLayout.addInsuranceGenderET.setText(insuredAndGuarantorDetails.gender?.replaceFirstChar { it.uppercase() })

                onRelationshipChange(
                    insuredAndGuarantorDetails.relationship?.equals(
                        "SELF",
                        true
                    ) == true, false
                )
                insuredAndGuarantorDetails.postalAddress?.let { address ->
                    parentLayout.addInsuranceLine1ET.setText(address.line1)
                    parentLayout.addInsuranceLine2ET.setText(address.line2)
                    parentLayout.addInsuranceCityET.setText(address.city)
                    parentLayout.addInsuranceCountryET.setText(questionnaireDetailViewModel.insuranceCommonData?.country?.firstOrNull { it.key == address.country }?.name)
                    parentLayout.addInsuranceStateET.setText(address.state)
                    parentLayout.addInsurancePinCodeET.setText(address.postalCode)
                }

                insuredAndGuarantorDetails.contactDetails?.let { contactDetails ->
                    parentLayout.addInsuranceHomePhoneET.setText(contactDetails.homePhone)
                    parentLayout.addInsuranceMobilePhoneET.setText(contactDetails.mobile)
                }

            }

            if (!questionnaireDetailViewModel.isDataEditAllowed) {

                parentLayout.addInsuranceFirstNameET.isEnabled = false
                parentLayout.addInsuranceLastNameET.isEnabled = false
                parentLayout.addInsuranceRelationshipET.isEnabled = false
                parentLayout.addInsuranceDOBET.isEnabled = false
                parentLayout.addInsuranceGenderET.isEnabled = false

                parentLayout.addInsuranceAddressCheckbox.isEnabled = false

                parentLayout.addInsuranceLine1ET.isEnabled = false
                parentLayout.addInsuranceLine2ET.isEnabled = false
                parentLayout.addInsuranceCityET.isEnabled = false
                parentLayout.addInsuranceCountryET.isEnabled = false
                parentLayout.addInsuranceStateET.isEnabled = false
                parentLayout.addInsurancePinCodeET.isEnabled = false


                parentLayout.addInsuranceHomePhoneET.isEnabled = false
                parentLayout.addInsuranceMobilePhoneET.isEnabled = false
            }


            insurance.practicePayer?.let { payer ->
                if (insurance.isNewPayerAllowed) {
                    parentLayout.addInsurancePayerNameET.setText(payer.payerName)
                    parentLayout.addInsurancePayerIdET.setText(payer.payerId)
                } else {
                    parentLayout.addInsurancePayerNameSelector.setText(payer.payerName)
                }
            }

        }


    }

    fun fetchStates(country: String, stateAdapter: MDAutoCompleteTextViewAdapter) {
        progressDialog.show()
        questionnaireDetailViewModel.fetchStates(country).observe(this) { resp ->

            if (resp != null) {
                progressDialog.dismiss()
                if (resp is ArrayList<*>) {
                    val stateData = StateList()
                    stateData.stateList = resp as ArrayList<String>
                    stateData.countryCode = country
                    questionnaireDetailViewModel.stateList.add(stateData)
                    stateAdapter.setItemList(stateData.stateList)
                } else {
                    stateAdapter.setItemList(arrayListOf())
                }

            }
        }
    }


    private fun onSubmitFeedback(isSubmitted: Boolean): QuestionnaireDetailPOJO? {

        var isFeedbackGiven = false

        val questionnaireEntryList = arrayListOf<QuestionnaireEntryPOJO>()

        var isTextFieldPresent = false

        var parentView: Any = activityQuestionnaireDetailBinding.questionnaireDetailMainLayout
        var isParentSection = false

        questionnaireDetailViewModel.tagToIdMap.iterator().forEach { tagToId ->

            val tag = tagToId.key.toInt()
            val entryId = tagToId.value

            val feedBackPOJO =
                questionnaireDetailViewModel.questionnaireDetailPOJO.questionnairEntryPOJOList.firstOrNull { it.questionnaireEntry.entryId == entryId }

            val notesType = feedBackPOJO?.questionnaireEntry?.notesType ?: ""

            if (!isTextFieldPresent && notesType != ModelUtil.questionnaireNotesTypeLabel && notesType != ModelUtil.questionnaireNotesTypeSignature) {
                isTextFieldPresent = true
            }

            if (feedBackPOJO != null) {

                when (feedBackPOJO.questionnaireEntry.notesType) {

                    ModelUtil.questionnaireNotesTypeSection -> {
                        val sectionView = activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag<View>(tag)
                            ?: return@forEach
                        val itemView =
                            ItemQuestionnaireSectionTemplateBinding.bind(sectionView)
                        val expandableView = itemView.root.getChildAt(0)
                        if (expandableView is ExpandableSectionLayout) {
                            parentView = expandableView
                            isParentSection = true
                        }
                    }

                    ModelUtil.questionnaireNotesTypeInsurance -> {
                        val insurance = PatientInsuranceDetails()

                        val insuranceView = activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag<View>(tag)
                            ?: return@forEach
                        val parentLayout =
                            LayoutPatientInsuranceBinding.bind(insuranceView)

                        val checkedId =
                            parentLayout.addInsuranceHasInsuranceRadioGrp.checkedRadioButtonId


                        if (checkedId == R.id.addInsuranceHasInsuranceYesRadioBtn) {

                            var payerName = ""
                            if (questionnaireDetailViewModel.patientInsuranceDetails?.isNewPayerAllowed == true) {
                                payerName = parentLayout.addInsurancePayerNameET.text.toString()
                                if (payerName.isBlank() && isSubmitted) {
                                    showMandatoryAlert(
                                        parentLayout.addInsurancePayerNameLayout,
                                        parentLayout.addInsurancePayerNameET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_empty_payer_name)
                                    )
                                    return null
                                }
                                if (!ModelUtil.hasAlpha(payerName) && isSubmitted) {
                                    showMandatoryAlert(
                                        parentLayout.addInsurancePayerNameLayout,
                                        parentLayout.addInsurancePayerNameET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_invalid_payer_name)
                                    )
                                    return null
                                }
                            } else {
                                payerName =
                                    parentLayout.addInsurancePayerNameSelector.text.toString()
                                if (payerName.isBlank() && isSubmitted) {
                                    showMandatoryAlert(
                                        parentLayout.addInsurancePayerNameSelectableLayout,
                                        parentLayout.addInsurancePayerNameSelector,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_empty_payer_name)
                                    )
                                    return null
                                }
                            }

                            val payerID = parentLayout.addInsurancePayerIdET.text.toString()
                            val payer = PracticePayer()
                            payer.payerId = payerID
                            payer.payerName = payerName


                            val insuranceType = parentLayout.addInsuranceTypeUSET.text.toString()
                            if (insuranceType.isBlank() && isSubmitted) {
                                showMandatoryAlert(
                                    parentLayout.addInsuranceTypeUSLayout,
                                    parentLayout.addInsuranceTypeUSET,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.error_empty_insurance_type)
                                )
                                return null
                            }

                            val memberId = parentLayout.addInsuranceMemberIdET.text.toString()
                            if (memberId.isBlank() && isSubmitted) {
                                var hint = parentLayout.addInsuranceMemberIdLayout.hint
                                if (hint.isNullOrBlank()) {
                                    hint = parentLayout.addInsuranceMemberIdET.hint ?: ""
                                }
                                showMandatoryAlert(
                                    parentLayout.addInsuranceMemberIdLayout,
                                    parentLayout.addInsuranceMemberIdET,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.error_empty_member_id, hint)
                                )
                                return null
                            }


                            val planName = parentLayout.addInsurancePlanET.text.toString()
                            /*  if (planName.isBlank()) {
                                UIUtil.showToast(this, R.string.error_empty_plan_name, Toast.LENGTH_LONG)
                                return null
                            }*/


                            val policyGrp = parentLayout.addInsurancePolicyGrpET.text.toString()
                            if (policyGrp.isBlank() && isSubmitted) {
                                showMandatoryAlert(
                                    parentLayout.addInsurancePolicyGrpLayout,
                                    parentLayout.addInsurancePolicyGrpET,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.error_empty_policy_grp_num)
                                )
                                return null
                            }

                            val empName = parentLayout.addInsuranceEmpNameET.text.toString()
                            /* if (empName.isBlank()){
                             UIUtil.showToast(this ,R.string.error_empty_emp_num , Toast.LENGTH_LONG )
                             return null
                         }*/


                            val relationship =
                                parentLayout.addInsuranceRelationshipET.text.toString()

                            if (relationship.isBlank() && isSubmitted) {
                                showMandatoryAlert(
                                    parentLayout.addInsuranceRelationshipLayout,
                                    parentLayout.addInsuranceRelationshipET,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.error_empty_relationship)
                                )
                                return null
                            }

                            val copay = parentLayout.addInsuranceCopayET.text.toString()
                            val detectable = parentLayout.addInsuranceDeductableET.text.toString()


                            insurance.insurancePlanName = planName
                            insurance.insurancePlanType = insuranceType
                            insurance.type = ModelUtil.insuranceTypePrimary
                            insurance.isPrimary = true
                            insurance.policyNumber = memberId
                            insurance.groupNumber = policyGrp
                            insurance.copay = copay
                            insurance.deductable = detectable

                            insurance.practicePayer = payer
                            val insuredAndGuarantorDetails = InsuredAndGuarantorDetails()
                            insuredAndGuarantorDetails.relationship = relationship


                            val employerDetails = InsuredEmployerDetails()
                            employerDetails.name = empName
                            insuredAndGuarantorDetails.insuredEmployerDetails = employerDetails


                            val firstName = parentLayout.addInsuranceFirstNameET.text.toString()
                            val lastName = parentLayout.addInsuranceLastNameET.text.toString()
                            val dob = parentLayout.addInsuranceDOBET.text.toString()
                            val gender = parentLayout.addInsuranceGenderET.text.toString()

                            val addressLine1 = parentLayout.addInsuranceLine1ET.text.toString()
                            val addressLine2 = parentLayout.addInsuranceLine2ET.text.toString()
                            val countryName = parentLayout.addInsuranceCountryET.text.toString()
                            val state = parentLayout.addInsuranceStateET.text.toString()
                            val countryKey =
                                questionnaireDetailViewModel.insuranceCommonData?.country?.firstOrNull {
                                    it.name.equals(
                                        countryName,
                                        true
                                    )
                                }?.key
                                    ?: ""
                            val city = parentLayout.addInsuranceCityET.text.toString()
                            val pinCode = parentLayout.addInsurancePinCodeET.text.toString()

                            val homePhone = parentLayout.addInsuranceHomePhoneET.text.toString()
                            val mobileNumber =
                                parentLayout.addInsuranceMobilePhoneET.text.toString()
                            if (parentLayout.addInsuranceRelationshipET.position != 0) {

                                if (firstName.isBlank() && isSubmitted) {
                                    showMandatoryAlert(
                                        parentLayout.addInsuranceFirstNameLayout,
                                        parentLayout.addInsuranceFirstNameET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_empty_first_name)
                                    )
                                    return null
                                }

                                if (!ModelUtil.hasAlpha(firstName) && isSubmitted) {
                                    showMandatoryAlert(
                                        parentLayout.addInsuranceFirstNameLayout,
                                        parentLayout.addInsuranceFirstNameET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_invalid_first_name)
                                    )

                                    return null
                                }


                                if (lastName.isBlank() && isSubmitted) {
                                    showMandatoryAlert(
                                        parentLayout.addInsuranceLastNameLayout,
                                        parentLayout.addInsuranceLastNameET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_empty_last_name)
                                    )
                                    return null
                                }

                                if (!ModelUtil.hasAlpha(lastName)) {
                                    showMandatoryAlert(
                                        parentLayout.addInsuranceLastNameLayout,
                                        parentLayout.addInsuranceLastNameET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_invalid_last_name)
                                    )
                                    return null
                                }


                                if (dob.isBlank() && isSubmitted) {
                                    showMandatoryAlert(
                                        parentLayout.addInsuranceDOBLayout,
                                        parentLayout.addInsuranceDOBET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_empty_dob)
                                    )
                                    return null
                                }

                                if (gender.isBlank() && isSubmitted) {
                                    showMandatoryAlert(
                                        parentLayout.addInsuranceGenderLayout,
                                        parentLayout.addInsuranceGenderET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_empty_birth_sex)
                                    )
                                    return null
                                }


                                if (!ModelUtil.hasAlpha(addressLine1) && isSubmitted) {
                                    showMandatoryAlert(
                                        parentLayout.addInsuranceLine1Layout,
                                        parentLayout.addInsuranceLine1ET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_invalid_line_1)
                                    )
                                    return null
                                }


                                if (!ModelUtil.hasAlpha(addressLine2) && isSubmitted) {
                                    showMandatoryAlert(
                                        parentLayout.addInsuranceLine2Layout,
                                        parentLayout.addInsuranceLine2ET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_invalid_line_2)
                                    )
                                    return null
                                }



                                if (countryName.isNotBlank() && isSubmitted) {

                                    if (countryKey.isBlank()) {
                                        showMandatoryAlert(
                                            parentLayout.addInsuranceCountryLayout,
                                            parentLayout.addInsuranceCountryET,
                                            isParentSection,
                                            parentView,
                                            getString(R.string.error_wrong_country)
                                        )
                                        return null
                                    }



                                    if (state.isNotBlank()) {

                                        if (!isValidState(
                                                countryKey,
                                                state,
                                                parentLayout.addInsuranceStateET,
                                                parentLayout.addInsuranceStateLayout,
                                                isParentSection,
                                                parentView
                                            )
                                        ) {
                                            return null
                                        }
                                    }
                                } else {

                                    if (state.isNotBlank()) {
                                        if (!ModelUtil.hasAlpha(state) && isSubmitted) {
                                            showMandatoryAlert(
                                                parentLayout.addInsuranceStateLayout,
                                                parentLayout.addInsuranceStateET,
                                                isParentSection,
                                                parentView,
                                                getString(R.string.error_invalid_state)
                                            )
                                            return null
                                        }
                                    }
                                }

                                if (!ModelUtil.hasAlpha(city) && isSubmitted) {
                                    showMandatoryAlert(
                                        parentLayout.addInsuranceCityLayout,
                                        parentLayout.addInsuranceCityET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_invalid_city)
                                    )
                                    return null
                                }



                                if (isSubmitted) {

                                    if (pinCode.isNotBlank()) {
                                        if (countryKey == ModelUtil.patientInState) {
                                            if (pinCode.length != 6) {
                                                showMandatoryAlert(
                                                    parentLayout.addInsurancePinCodeLayout,
                                                    parentLayout.addInsurancePinCodeET,
                                                    isParentSection,
                                                    parentView,
                                                    getString(R.string.error_invalid_pincode)
                                                )
                                                return null
                                            }
                                        } else if (countryKey == ModelUtil.patientUsState) {
                                            if (pinCode.length != 5) {
                                                showMandatoryAlert(
                                                    parentLayout.addInsurancePinCodeLayout,
                                                    parentLayout.addInsurancePinCodeET,
                                                    isParentSection,
                                                    parentView,
                                                    getString(R.string.error_invalid_pincode)
                                                )
                                                return null
                                            }
                                        }
                                    }
                                }


                                /* if (mobileNumber.isBlank()){
                             UIUtil.showToast(this , R.string.error_empty_mobile_num , Toast.LENGTH_SHORT)
                             return null
                         }*/
                            }

                            insuredAndGuarantorDetails.firstName = firstName
                            insuredAndGuarantorDetails.lastName = lastName
                            insuredAndGuarantorDetails.dateOfBirth =
                                UIUtil.formatDate(dob, ModelUtil.charmDisplayDateFormat())
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

                            insurance.insuredAndGuarantorDetails = insuredAndGuarantorDetails


                            val startDate = if (parentLayout.addInsuranceStartDateET.text.toString()
                                    .isNotBlank()
                            ) {
                                UIUtil.formatDate(
                                    parentLayout.addInsuranceStartDateET.text.toString(),
                                    ModelUtil.charmDisplayDateFormat()
                                )
                            } else {
                                null
                            }


                            val endDate = if (parentLayout.addInsuranceEndDateET.text.toString()
                                    .isNotBlank()
                            ) {
                                UIUtil.formatDate(
                                    parentLayout.addInsuranceEndDateET.text.toString(),
                                    ModelUtil.charmDisplayDateFormat()
                                )
                            } else {
                                null
                            }


                            if (startDate != null && endDate != null) {

                                if (endDate.isBefore(startDate)) {
                                    showMandatoryAlert(
                                        parentLayout.addInsuranceStartDateLayout,
                                        parentLayout.addInsuranceStartDateET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_add_insurance_invalid_period)
                                    )
                                    return null
                                }
                            }

                            if (insuredAndGuarantorDetails.dateOfBirth != null) {

                                if (startDate != null) {
                                    if (startDate.isBefore(insuredAndGuarantorDetails.dateOfBirth)) {
                                        showMandatoryAlert(
                                            parentLayout.addInsuranceStartDateLayout,
                                            parentLayout.addInsuranceStartDateET,
                                            isParentSection,
                                            parentView,
                                            getString(R.string.error_add_insurance_start_date_before_dob)
                                        )

                                        return null
                                    }
                                }


                                if (endDate != null) {
                                    if (endDate.isBefore(insuredAndGuarantorDetails.dateOfBirth)) {
                                        showMandatoryAlert(
                                            parentLayout.addInsuranceEndDateLayout,
                                            parentLayout.addInsuranceEndDateET,
                                            isParentSection,
                                            parentView,
                                            getString(R.string.error_add_insurance_end_date_before_dob)
                                        )

                                        return null
                                    }
                                }
                            }


                            insurance.policyStartDate = startDate
                            insurance.policyEndDate = endDate
                            insurance.practiceId =
                                questionnaireDetailViewModel.physicianTemplates.practiceId
                            insurance.patientInsuranceDetailsId =
                                questionnaireDetailViewModel.patientInsuranceDetails?.patientInsuranceDetailsId
                            val comment = parentLayout.addInsuranceCommentET.text.toString()

                            if (comment.isNotBlank() && isSubmitted) {
                                if (!ModelUtil.hasAlpha(comment)) {
                                    showMandatoryAlert(
                                        parentLayout.addInsuranceCommentLayout,
                                        parentLayout.addInsuranceCommentET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_invalid_comments)
                                    )
                                    return null
                                }
                            }

                            insurance.comment = comment
                            insurance.isAvailable = true
                        } else {
                            insurance.isAvailable = false
                        }

                        isFeedbackGiven = true

                        feedBackPOJO.questionnaireInsurance = insurance
                        questionnaireEntryList.add(feedBackPOJO)
                    }

                    ModelUtil.questionnaireNotesTypePersonalDetails -> {

                        val patient = Patient()

                        val personalDetailsView = activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag<View>(tag)
                            ?: return@forEach
                        val parentLayout =
                            LayoutPatientDetailsBinding.bind(personalDetailsView)


                        if (parentLayout.patDetailFirstNameET.isEnabled) {

                            val firstName = parentLayout.patDetailFirstNameET.text.toString()

                            if (firstName.isBlank()) {
                                showMandatoryAlert(
                                    parentLayout.patDetailFirstNameLayout,
                                    parentLayout.patDetailFirstNameET,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.error_empty_first_name)
                                )
                                return null
                            }

                            if (!ModelUtil.hasAlpha(firstName)) {
                                showMandatoryAlert(
                                    parentLayout.patDetailFirstNameLayout,
                                    parentLayout.patDetailFirstNameET,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.error_invalid_first_name)
                                )
                                return null
                            }
                        } else {
                            patient.firstName =
                                questionnaireDetailViewModel.personalDetail?.firstName
                        }

                        if (parentLayout.patDetailLastNameET.isEnabled) {

                            val lastName = parentLayout.patDetailLastNameET.text.toString()

                            if (lastName.isBlank()) {
                                showMandatoryAlert(
                                    parentLayout.patDetailLastNameLayout,
                                    parentLayout.patDetailLastNameET,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.error_empty_last_name)
                                )
                                return null
                            }

                            if (!ModelUtil.hasAlpha(lastName)) {
                                showMandatoryAlert(
                                    parentLayout.patDetailLastNameLayout,
                                    parentLayout.patDetailLastNameET,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.error_invalid_last_name)
                                )
                                return null
                            }
                        } else {
                            patient.lastName = questionnaireDetailViewModel.personalDetail?.lastName
                        }

                        val dob = parentLayout.patDetailDOBET.text.toString()
                        if (dob.isBlank()) {
                            showMandatoryAlert(
                                parentLayout.patDetailDOBLayout,
                                parentLayout.patDetailDOBET,
                                isParentSection,
                                parentView,
                                getString(R.string.error_empty_dob)
                            )
                            return null
                        }

                        var genderId = parentLayout.patDetailGenderIdET.text?.toString()

                        if (genderId.equals("other", true)) {

                            val otherVal =
                                parentLayout.patDetailGenderOtherET.text?.toString() ?: ""

                            if (otherVal.isBlank()) {
                                showMandatoryAlert(
                                    parentLayout.patDetailGenderOtherLayout,
                                    parentLayout.patDetailGenderOtherET,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.error_empty_gender_id_other)
                                )
                                return null
                            }

                            genderId = "$genderId:$otherVal"
                        }


                        val language = parentLayout.patDetailLanguageET.text?.toString()

                        val languageList = ArrayList(questionnaireDetailViewModel.languageList)
                        languageList.add(ModelUtil.questionnairePersonalDetailDeclineToSpecify)

                        if (!languageList.any { language.equals(it, true) }) {
                            showMandatoryAlert(
                                parentLayout.patDetailLanguageLayout,
                                parentLayout.patDetailLanguageET,
                                isParentSection,
                                parentView,
                                getString(R.string.error_wrong_language)
                            )
                            return null
                        }



                        patient.dateOfBirth =
                            LocalDate.parse(dob, ModelUtil.charmDisplayDateFormat())
                        patient.gender = parentLayout.patDetailGenderET.text?.toString()
                        patient.genderIdentity = genderId
                        patient.bloodGroup = parentLayout.patDetailBloodGrpET.text?.toString()
                        patient.language = parentLayout.patDetailLanguageET.text?.toString()
                        patient.race = parentLayout.patDetailRaceET.text?.toString()
                        patient.ethnicity = parentLayout.patDetailEthnicityET.text?.toString()
                        patient.maritalStatus = parentLayout.patDetailMStatusET.text?.toString()
                        patient.smokingStatus = parentLayout.patDetailSStatusET.text?.toString()
                        patient.employmentStatus = parentLayout.patDetailEStatusET.text?.toString()


                        isFeedbackGiven = true



                        feedBackPOJO.questionnairePersonalDetails = patient
                        questionnaireEntryList.add(feedBackPOJO)

                    }

                    ModelUtil.questionnaireNotesTypePrimaryContactDetail -> {

                        val patient = Patient()
                        val contactView = activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag<View>(tag)
                            ?: return@forEach
                        val parentLayout =
                            LayoutPatientContactBinding.bind(contactView)


                        val email = parentLayout.patContactEmailET.text.toString()

                        if (email.isBlank()) {

                            showMandatoryAlert(
                                parentLayout.patContactEmailLayout,
                                parentLayout.patContactEmailET,
                                isParentSection,
                                parentView,
                                getString(R.string.error_empty_email)
                            )
                            return null
                        }

                        val pattern = Pattern.compile(ModelUtil.emailRegex)
                        val matcher = pattern.matcher(email)

                        if (!matcher.matches()) {
                            showMandatoryAlert(
                                parentLayout.patContactEmailLayout,
                                parentLayout.patContactEmailET,
                                isParentSection,
                                parentView,
                                getString(R.string.error_invalid_email)
                            )
                            return null
                        }

                        val line1 = parentLayout.patContactLine1ET.text.toString()
                        if (line1.isBlank()) {
                            showMandatoryAlert(
                                parentLayout.patContactLine1Layout,
                                parentLayout.patContactLine1ET,
                                isParentSection,
                                parentView,
                                getString(R.string.error_empty_line1)
                            )
                            return null
                        }
                        if (!ModelUtil.hasAlpha(line1)) {
                            showMandatoryAlert(
                                parentLayout.patContactLine1Layout,
                                parentLayout.patContactLine1ET,
                                isParentSection,
                                parentView,
                                getString(R.string.error_invalid_line_1)
                            )
                            return null
                        }

                        val line2 = parentLayout.patContactLine2ET.text.toString()

                        if (line2.isNotBlank()) {
                            if (!ModelUtil.hasAlpha(line2)) {
                                showMandatoryAlert(
                                    parentLayout.patContactLine2Layout,
                                    parentLayout.patContactLine2ET,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.error_invalid_line_2)
                                )
                                return null
                            }
                        }

                        val city = parentLayout.patContactCityET.text.toString()
                        if (city.isBlank()) {
                            showMandatoryAlert(
                                parentLayout.patContactCityLayout,
                                parentLayout.patContactCityET,
                                isParentSection,
                                parentView,
                                getString(R.string.error_empty_city)
                            )
                            return null
                        }
                        if (!ModelUtil.hasAlpha(city)) {
                            showMandatoryAlert(
                                parentLayout.patContactCityLayout,
                                parentLayout.patContactCityET,
                                isParentSection,
                                parentView,
                                getString(R.string.error_invalid_city)
                            )
                            return null
                        }

                        val countryName = parentLayout.patContactCountryET.text.toString()
                        if (countryName.isBlank()) {
                            showMandatoryAlert(
                                parentLayout.patContactCountryLayout,
                                parentLayout.patContactCountryET,
                                isParentSection,
                                parentView,
                                getString(R.string.error_empty_country)
                            )
                            return null
                        }

                        val countryKey =
                            questionnaireDetailViewModel.patientCommonData?.country?.firstOrNull {
                                it.name.equals(
                                    countryName,
                                    true
                                )
                            }?.key
                                ?: ""

                        if (countryKey.isBlank()) {
                            showMandatoryAlert(
                                parentLayout.patContactCountryLayout,
                                parentLayout.patContactCountryET,
                                isParentSection,
                                parentView,
                                getString(R.string.error_wrong_country)
                            )
                            return null
                        }

                        val state = parentLayout.patContactStateET.text.toString()

                        if (state.isNotBlank()) {

                            if (ModelUtil.patientStateList.contains(countryKey)) {
                                val stateList =
                                    questionnaireDetailViewModel.stateList.firstOrNull { it.countryCode == countryKey }
                                if (stateList != null && !stateList.stateList.any {
                                        state.equals(
                                            it,
                                            true
                                        )
                                    }) {
                                    showMandatoryAlert(
                                        parentLayout.patContactStateLayout,
                                        parentLayout.patContactStateET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_wrong_state)
                                    )
                                    return null
                                }
                            } else if (!ModelUtil.hasAlpha(state)) {
                                showMandatoryAlert(
                                    parentLayout.patContactStateLayout,
                                    parentLayout.patContactStateET,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.error_invalid_state)
                                )
                                return null
                            }
                        } else {
                            showMandatoryAlert(
                                parentLayout.patContactStateLayout,
                                parentLayout.patContactStateET,
                                isParentSection,
                                parentView,
                                getString(R.string.error_empty_state)
                            )
                            return null
                        }

                        val pinCode = parentLayout.patContactPinCodeET.text.toString()

                        if (pinCode.isNotBlank()) {
                            if (countryKey == ModelUtil.patientInState) {
                                if (pinCode.length != 6) {
                                    showMandatoryAlert(
                                        parentLayout.patContactPinCodeLayout,
                                        parentLayout.patContactPinCodeET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_invalid_pincode)
                                    )
                                    return null
                                }
                            } else if (countryKey == ModelUtil.patientUsState) {
                                if (pinCode.length != 5) {
                                    showMandatoryAlert(
                                        parentLayout.patContactPinCodeLayout,
                                        parentLayout.patContactPinCodeET,
                                        isParentSection,
                                        parentView,
                                        getString(R.string.error_invalid_pincode)
                                    )
                                    return null
                                }
                            }
                        } else {
                            showMandatoryAlert(
                                parentLayout.patContactPinCodeLayout,
                                parentLayout.patContactPinCodeET,
                                isParentSection,
                                parentView,
                                getString(R.string.error_empty_pincode)
                            )
                            return null
                        }

                        var postBox = ""


                        if (countryKey == ModelUtil.patientUsState) {
                            postBox = parentLayout.patContactPostBoxET.text.toString()
                        }



                        patient.emergencyContactName =
                            parentLayout.patContactEmgContactNameET.text.toString()
                        patient.emergencyContactNumber =
                            parentLayout.patContactEmgContactNumET.text.toString()
                        patient.emergencyContactExtn =
                            parentLayout.patContactEmgContactExtET.text.toString()
                        patient.practicePatientId =
                            questionnaireDetailViewModel.patContactDetails?.practicePatientId

                        val contactDetails = ContactDetails()
                        contactDetails.homePhone =
                            parentLayout.patContactHomePhoneET.text.toString()
                        contactDetails.workPhone =
                            parentLayout.patContactWorkPhoneET.text.toString()
                        contactDetails.workPhoneExtn =
                            parentLayout.patContactWorkPhoneExtET.text.toString()
                        contactDetails.mobile = parentLayout.patContactMobilePhoneET.text.toString()



                        contactDetails.primaryPhone =
                            when (parentLayout.patContactPrimaryPhET.position) {
                                1 -> "C"
                                2 -> "W"
                                else -> "H"
                            }
                        contactDetails.emailId = email
                        contactDetails.fax = parentLayout.patContactFaxET.text.toString()
                        contactDetails.contactDetailsId =
                            questionnaireDetailViewModel.patContactDetails?.contactDetails?.contactDetailsId


                        val postalAddress = PostalAddress()
                        postalAddress.line1 = line1
                        postalAddress.line2 = line2
                        postalAddress.city = city
                        postalAddress.country = countryKey
                        postalAddress.state = state
                        postalAddress.postalCode = pinCode
                        postalAddress.postbox = postBox
                        postalAddress.postalAddressId =
                            questionnaireDetailViewModel.patContactDetails?.postalAddress?.postalAddressId

                        patient.contactDetails = contactDetails
                        patient.postalAddress = postalAddress


                        isFeedbackGiven = true


                        feedBackPOJO.questionnairePatContacts = patient
                        questionnaireEntryList.add(feedBackPOJO)

                    }

                    ModelUtil.questionnaireNotesTypeOption -> {

                        if (feedBackPOJO.questionnaireEntry.isMultiChoice) {

                            val multiChoiceOptionsLayout: LinearLayout =
                                activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag(tag)
                                    ?: return@forEach
                            val multiChoiceOptionsCardView =
                                multiChoiceOptionsLayout.parent as CardView
                            val multiChoiceList = arrayListOf<String>()
                            val multiChoiceOptionsCount = multiChoiceOptionsLayout.childCount

                            for (optionsIndex in 0 until multiChoiceOptionsCount) {
                                val multiChoiceCheckBox =
                                    multiChoiceOptionsLayout.getChildAt(optionsIndex)
                                if (multiChoiceCheckBox is CheckBox) {
                                    if (multiChoiceCheckBox.isChecked) {
                                        multiChoiceList.add(multiChoiceCheckBox.text.toString())
                                    }
                                }
                            }
                            if (feedBackPOJO.questionnaireEntry.isMandatory && multiChoiceList.isEmpty() && isSubmitted) {
                                showMandatoryAlert(
                                    multiChoiceOptionsCardView,
                                    null,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.questionnaire_detail_mandatory_check_msg)
                                )
                                return null
                            } else if (multiChoiceList.isNotEmpty()) {
                                isFeedbackGiven = true
                                feedBackPOJO.questionnaireEntry.answersList = multiChoiceList

                                questionnaireEntryList.add(feedBackPOJO)
                            } else if (multiChoiceList.isEmpty()) {
                                feedBackPOJO.questionnaireEntry.answersList = null
                                questionnaireEntryList.add(feedBackPOJO)
                            }
                        } else {
                            val singleChoiceRadioGroup: RadioGroup =
                                activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag(tag)
                                    ?: return@forEach
                            val singleChoiceRadioGroupParent =
                                (singleChoiceRadioGroup.parent as ViewGroup).parent as CardView
                            val singleChoiceList = arrayListOf<String>()

                            val singleChoiceOptionsCount = singleChoiceRadioGroup.childCount

                            for (optionsIndex in 0 until singleChoiceOptionsCount) {
                                val singleChoiceRadioButton =
                                    singleChoiceRadioGroup.getChildAt(optionsIndex) as RadioButton
                                if (singleChoiceRadioButton.isChecked) {
                                    singleChoiceList.add(singleChoiceRadioButton.text.toString())
                                }
                            }

                            if (feedBackPOJO.questionnaireEntry.isMandatory && singleChoiceList.isEmpty() && isSubmitted) {
                                showMandatoryAlert(
                                    singleChoiceRadioGroupParent,
                                    null,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.questionnaire_detail_mandatory_check_msg)
                                )
                                return null
                            } else if (singleChoiceList.isNotEmpty()) {
                                isFeedbackGiven = true
                                feedBackPOJO.questionnaireEntry.answersList = singleChoiceList
                                questionnaireEntryList.add(feedBackPOJO)
                            } else if (singleChoiceList.isEmpty()) {
                                feedBackPOJO.questionnaireEntry.answersList = null
                                questionnaireEntryList.add(feedBackPOJO)
                            }

                        }
                    }

                    ModelUtil.questionnaireNotesTypeYesOrNoOption -> {
                        if (feedBackPOJO.questionnaireEntry.isMultiChoice) {

                            val multiChoiceOptionsLayout: LinearLayout =
                                activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag(tag)
                                    ?: return@forEach
                            val multiChoiceOptionsLayoutParent =
                                multiChoiceOptionsLayout.parent as CardView
                            val multiChoiceList = arrayListOf<String>()
                            val multiChoiceOptionsCount = multiChoiceOptionsLayout.childCount

                            for (optionsIndex in 0 until multiChoiceOptionsCount) {
                                val multiChoiceCheckBox =
                                    multiChoiceOptionsLayout.getChildAt(optionsIndex)

                                if (multiChoiceCheckBox is CheckBox) {
                                    if (multiChoiceCheckBox.isChecked) {
                                        multiChoiceList.add(multiChoiceCheckBox.text.toString())
                                    }
                                }
                            }
                            if (feedBackPOJO.questionnaireEntry.isMandatory && multiChoiceList.isEmpty() && isSubmitted) {
                                showMandatoryAlert(
                                    multiChoiceOptionsLayoutParent,
                                    null,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.questionnaire_detail_mandatory_check_msg)
                                )
                                return null
                            } else if (multiChoiceList.isNotEmpty()) {
                                isFeedbackGiven = true
                                feedBackPOJO.questionnaireEntry.answersList = multiChoiceList
                                questionnaireEntryList.add(feedBackPOJO)
                            } else if (multiChoiceList.isEmpty()) {
                                feedBackPOJO.questionnaireEntry.answersList = null
                                questionnaireEntryList.add(feedBackPOJO)
                            }
                        } else {
                            val singleChoiceRadioGroup: RadioGroup =
                                activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag(tag)
                                    ?: return@forEach
                            val singleChoiceRadioGroupLayout =
                                (singleChoiceRadioGroup.parent as ViewGroup).parent as CardView
                            val singleChoiceList = arrayListOf<String>()

                            val singleChoiceOptionsCount = singleChoiceRadioGroup.childCount

                            for (optionsIndex in 0 until singleChoiceOptionsCount) {
                                val singleChoiceRadioButton =
                                    singleChoiceRadioGroup.getChildAt(optionsIndex) as RadioButton
                                if (singleChoiceRadioButton.isChecked) {
                                    singleChoiceList.add(singleChoiceRadioButton.text.toString())
                                }
                            }

                            if (feedBackPOJO.questionnaireEntry.isMandatory && singleChoiceList.isEmpty() && isSubmitted) {
                                showMandatoryAlert(
                                    singleChoiceRadioGroupLayout,
                                    null,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.questionnaire_detail_mandatory_check_msg)
                                )
                                return null
                            } else if (singleChoiceList.isNotEmpty()) {
                                isFeedbackGiven = true
                                feedBackPOJO.questionnaireEntry.answersList = singleChoiceList
                                questionnaireEntryList.add(feedBackPOJO)
                            } else if (singleChoiceList.isEmpty()) {
                                feedBackPOJO.questionnaireEntry.answersList = null
                                questionnaireEntryList.add(feedBackPOJO)
                            }

                        }
                    }

                    ModelUtil.questionnaireNotesTypeOptionWithOthers -> {

                        if (feedBackPOJO.questionnaireEntry.isMultiChoice) {

                            val multiChoiceOptionsLayout: LinearLayout =
                                activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag(tag)
                                    ?: return@forEach
                            val multiChoiceOptionsCardView =
                                multiChoiceOptionsLayout.parent as CardView
                            val multiChoiceList = arrayListOf<String>()
                            val multiChoiceOptionsCount = multiChoiceOptionsLayout.childCount
                            Log.d(
                                "MDQuestionnaire",
                                "Notes ->${feedBackPOJO.questionnaireEntry.notes}"
                            )
                            Log.d("MDQuestionnaire", "Count ->${multiChoiceOptionsCount}")


                            for (optionsIndex in 0 until multiChoiceOptionsCount - 1) {
                                Log.d(
                                    "MDQuestionnaire",
                                    "Type ->${multiChoiceOptionsLayout.getChildAt(optionsIndex)}"
                                )

                                val multiChoiceCheckBox =
                                    multiChoiceOptionsLayout.getChildAt(optionsIndex)

                                if (multiChoiceCheckBox is CheckBox) {

                                    if (multiChoiceCheckBox.isChecked && optionsIndex != multiChoiceOptionsCount - 2) {
                                        multiChoiceList.add(multiChoiceCheckBox.text.toString())
                                    } else if (optionsIndex == multiChoiceOptionsCount - 2 && multiChoiceCheckBox.isChecked) {
                                        val multiChoiceCheckBoxOthersEditText =
                                            multiChoiceOptionsLayout.getChildAt(
                                                multiChoiceOptionsCount - 1
                                            ) as EditText
                                        if (multiChoiceCheckBoxOthersEditText.text.toString()
                                                .isBlank()
                                        ) {
                                            UIUtil.showToast(
                                                this,
                                                R.string.questionnaire_detail_others_text_not_given,
                                                Toast.LENGTH_LONG
                                            )
                                            showMandatoryAlert(
                                                multiChoiceOptionsCardView,
                                                null,
                                                isParentSection,
                                                parentView,
                                                getString(R.string.questionnaire_detail_mandatory_check_msg)
                                            )
                                            multiChoiceCheckBoxOthersEditText.requestFocus()
                                            return null
                                        } else {
                                            multiChoiceList.add(multiChoiceCheckBoxOthersEditText.text.toString())
                                        }
                                    }
                                }
                            }
                            if (feedBackPOJO.questionnaireEntry.isMandatory && multiChoiceList.isEmpty() && isSubmitted) {
                                showMandatoryAlert(
                                    multiChoiceOptionsCardView,
                                    null,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.questionnaire_detail_mandatory_check_msg)
                                )
                                return null
                            } else if (multiChoiceList.isNotEmpty()) {
                                isFeedbackGiven = true
                                feedBackPOJO.questionnaireEntry.answersList = multiChoiceList
                                questionnaireEntryList.add(feedBackPOJO)
                            } else if (multiChoiceList.isEmpty()) {
                                feedBackPOJO.questionnaireEntry.answersList = null
                                questionnaireEntryList.add(feedBackPOJO)
                            }
                        } else {
                            val singleChoiceRadioGroup: RadioGroup =
                                activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag(tag)
                                    ?: return@forEach
                            val singleChoiceRadioGroupParent =
                                (singleChoiceRadioGroup.parent as ViewGroup).parent as CardView
                            val singleChoiceList = arrayListOf<String>()

                            val singleChoiceOptionsCount = singleChoiceRadioGroup.childCount

                            for (optionsIndex in 0 until singleChoiceOptionsCount - 1) {
                                val singleChoiceRadioButton =
                                    singleChoiceRadioGroup.getChildAt(optionsIndex) as RadioButton
                                if (singleChoiceRadioButton.isChecked && optionsIndex != singleChoiceOptionsCount - 2) {
                                    singleChoiceList.add(singleChoiceRadioButton.text.toString())
                                } else if (singleChoiceRadioButton.isChecked && optionsIndex == singleChoiceOptionsCount - 2) {
                                    val singleChoiceOthersEditText =
                                        singleChoiceRadioGroup.getChildAt(singleChoiceOptionsCount - 1) as EditText
                                    if (singleChoiceOthersEditText.text.toString().isBlank()) {
                                        UIUtil.showToast(
                                            this,
                                            R.string.questionnaire_detail_others_text_not_given,
                                            Toast.LENGTH_LONG
                                        )
                                        showMandatoryAlert(
                                            singleChoiceRadioGroupParent,
                                            null,
                                            isParentSection,
                                            parentView,
                                            getString(R.string.questionnaire_detail_mandatory_check_msg)
                                        )
                                        singleChoiceOthersEditText.requestFocus()
                                        return null
                                    } else {
                                        singleChoiceList.add(singleChoiceOthersEditText.text.toString())
                                    }
                                }
                            }

                            if (feedBackPOJO.questionnaireEntry.isMandatory && singleChoiceList.isEmpty() && isSubmitted) {
                                showMandatoryAlert(
                                    singleChoiceRadioGroupParent,
                                    null,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.questionnaire_detail_mandatory_check_msg)
                                )
                                return null
                            } else if (singleChoiceList.isNotEmpty()) {
                                isFeedbackGiven = true
                                feedBackPOJO.questionnaireEntry.answersList = singleChoiceList
                                questionnaireEntryList.add(feedBackPOJO)
                            } else if (singleChoiceList.isEmpty()) {
                                feedBackPOJO.questionnaireEntry.answersList = null
                                questionnaireEntryList.add(feedBackPOJO)
                            }

                        }
                    }

                    ModelUtil.questionnaireNotesTypeSimpleQuestion, ModelUtil.questionnaireNotesTypeQuestion -> {

                        val answer = getValueFromTextView(
                            feedBackPOJO.questionnaireEntry.isMandatory,
                            isSubmitted,
                            tag,
                            isParentSection,
                            parentView
                        )
                        if (answer != null) {
                            if (answer.isNotBlank()) {
                                isFeedbackGiven = true
                            }
                            feedBackPOJO.questionnaireEntry.answersList = arrayListOf(answer)
                            questionnaireEntryList.add(feedBackPOJO)
                        } else {
                            return null
                        }
                    }

                    ModelUtil.questionnaireNotesTypeDate -> {
                        val questionEditText: MDTextInputEdittext =
                            activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag(tag)
                                ?: return@forEach
                        val questionEditTextLayout =
                            (questionEditText.parent as ViewGroup).parent as CardView
                        val answer = questionEditText.text.toString()

                        if (answer.isBlank() && feedBackPOJO.questionnaireEntry.isMandatory && isSubmitted) {
                            showMandatoryAlert(
                                questionEditTextLayout,
                                questionEditText,
                                isParentSection,
                                parentView,
                                getString(R.string.questionnaire_detail_mandatory_check_msg),
                                true
                            )
                            questionEditText.performClick()
                            return null
                        } else if (answer.isNotBlank()) {
                            isFeedbackGiven = true
                            feedBackPOJO.questionnaireEntry.answersList = arrayListOf(answer)
                            questionnaireEntryList.add(feedBackPOJO)
                        } else if (answer.isBlank()) {
                            feedBackPOJO.questionnaireEntry.answersList = null
                            questionnaireEntryList.add(feedBackPOJO)
                        }
                    }

                    ModelUtil.questionnaireNotesTypeMedication -> {

                        val medicationView = activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag<View>(tag)
                            ?: return@forEach
                        val questionMainLayout =
                            ItemQuestionnairePrescriptionBinding.bind(medicationView)
                        val medicationCount =
                            questionMainLayout.questionnaireDetailPrescriptionMainLayout.childCount

                        val intakeQuestMedicationEntryList =
                            arrayListOf<IntakeQuestMedicationEntry>()
                        for (index in 0 until medicationCount) {
                            val medicationLayout =
                                ItemQuestionnairePrescriptionEntryBinding.bind(
                                    questionMainLayout.questionnaireDetailPrescriptionMainLayout.getChildAt(
                                        index
                                    )
                                )

                            val medicationName =
                                medicationLayout.questionnaireDetailItemPrescriptionNameET.text.toString()
                            val medicationDetail =
                                medicationLayout.questionnaireDetailItemPrescriptionIntakeDetailsET.text.toString()

                            if (medicationName.isNotBlank() || medicationDetail.isNotBlank()) {

                                if (isSubmitted) {

                                    if (medicationName.isBlank()) {
                                        showMandatoryAlert(
                                            medicationLayout.questionnaireDetailItemPrescriptionNameLayout,
                                            medicationLayout.questionnaireDetailItemPrescriptionNameET,
                                            isParentSection,
                                            parentView,
                                            getString(R.string.error_questionnaire_medication_empty_name)
                                        )
                                        return null
                                    }

                                    if (medicationDetail.isBlank()) {
                                        showMandatoryAlert(
                                            medicationLayout.questionnaireDetailItemPrescriptionIntakeDetailsLayout,
                                            medicationLayout.questionnaireDetailItemPrescriptionIntakeDetailsET,
                                            isParentSection,
                                            parentView,
                                            getString(R.string.error_questionnaire_medication_empty_detail)
                                        )
                                        return null
                                    }
                                }

                                val intakeQuestMedicationEntry = IntakeQuestMedicationEntry()
                                intakeQuestMedicationEntry.drugName = medicationName
                                intakeQuestMedicationEntry.intakeDetails = medicationDetail
                                intakeQuestMedicationEntryList.add(intakeQuestMedicationEntry)
                            }

                        }

                        if (intakeQuestMedicationEntryList.isNotEmpty()) {
                            isFeedbackGiven = true
                        }

                        feedBackPOJO.questionnaireMedicationList = intakeQuestMedicationEntryList
                        questionnaireEntryList.add(feedBackPOJO)
                    }

                    ModelUtil.questionnaireNotesTypeSupplement -> {

                        val supplementView = activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag<View>(tag)
                            ?: return@forEach
                        val questionMainLayout =
                            ItemQuestionnairePrescriptionBinding.bind(supplementView)
                        val medicationCount =
                            questionMainLayout.questionnaireDetailPrescriptionMainLayout.childCount

                        val intakeQuestSupplementEntryList =
                            arrayListOf<IntakeQuestSupplementEntry>()
                        for (index in 0 until medicationCount) {
                            val medicationLayout =
                                ItemQuestionnairePrescriptionEntryBinding.bind(
                                    questionMainLayout.questionnaireDetailPrescriptionMainLayout.getChildAt(
                                        index
                                    )
                                )

                            val medicationName =
                                medicationLayout.questionnaireDetailItemPrescriptionNameET.text.toString()
                            val medicationDetail =
                                medicationLayout.questionnaireDetailItemPrescriptionIntakeDetailsET.text.toString()

                            if (medicationName.isNotBlank() || medicationDetail.isNotBlank()) {

                                if (isSubmitted) {

                                    if (medicationName.isBlank()) {
                                        showMandatoryAlert(
                                            medicationLayout.questionnaireDetailItemPrescriptionNameLayout,
                                            medicationLayout.questionnaireDetailItemPrescriptionNameET,
                                            isParentSection,
                                            parentView,
                                            getString(R.string.error_questionnaire_supplement_empty_name)
                                        )
                                        return null
                                    }

                                    if (medicationDetail.isBlank()) {
                                        showMandatoryAlert(
                                            medicationLayout.questionnaireDetailItemPrescriptionIntakeDetailsLayout,
                                            medicationLayout.questionnaireDetailItemPrescriptionIntakeDetailsET,
                                            isParentSection,
                                            parentView,
                                            getString(R.string.error_questionnaire_medication_empty_detail)
                                        )
                                        return null
                                    }
                                }

                                val intakeQuestSupplementEntry = IntakeQuestSupplementEntry()
                                intakeQuestSupplementEntry.supplementName = medicationName
                                intakeQuestSupplementEntry.intakeDetails = medicationDetail
                                intakeQuestSupplementEntryList.add(intakeQuestSupplementEntry)
                            }

                        }

                        if (intakeQuestSupplementEntryList.isNotEmpty()) {
                            isFeedbackGiven = true
                        }

                        feedBackPOJO.questionnaireSupplementList = intakeQuestSupplementEntryList
                        questionnaireEntryList.add(feedBackPOJO)
                    }

                    ModelUtil.questionnaireNotesTypeAllergy -> {

                        val allergyView = activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag<View>(tag)
                            ?: return@forEach
                        val questionMainLayout =
                            ItemQuestionnaireAllergyBinding.bind(allergyView)

                        val intakeQuestAllergyEntryList = arrayListOf<IntakeQuestAllergyEntry>()
                        val drugAllergyList = getAllergyData(
                            questionMainLayout.questionnaireDetailAllergyDrugMainLayout,
                            ModelUtil.questionnaireNotesTypeAllergyDrug,
                            isSubmitted, isParentSection, parentView
                        )
                        if (drugAllergyList != null) {
                            intakeQuestAllergyEntryList.addAll(drugAllergyList)
                        } else {
                            return null
                        }
                        val foodAllergyList = getAllergyData(
                            questionMainLayout.questionnaireDetailAllergyFoodMainLayout,
                            ModelUtil.questionnaireNotesTypeAllergyFood,
                            isSubmitted,
                            isParentSection,
                            parentView
                        )
                        if (foodAllergyList != null) {
                            intakeQuestAllergyEntryList.addAll(foodAllergyList)
                        } else {
                            return null
                        }
                        val envAllergyList = getAllergyData(
                            questionMainLayout.questionnaireDetailAllergyEnvironmentMainLayout,
                            ModelUtil.questionnaireNotesTypeAllergyEnvironment,
                            isSubmitted,
                            isParentSection,
                            parentView
                        )
                        if (envAllergyList != null) {
                            intakeQuestAllergyEntryList.addAll(envAllergyList)
                        } else {
                            return null
                        }

                        if (intakeQuestAllergyEntryList.isNotEmpty()) {
                            isFeedbackGiven = true
                        }

                        feedBackPOJO.questionnaireAllergyList = intakeQuestAllergyEntryList
                        questionnaireEntryList.add(feedBackPOJO)

                    }

                    ModelUtil.questionnaireNotesTypeSignature -> {
                        val signatureMainLayout: ConstraintLayout =
                            activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag(tag)
                                ?: return@forEach
                        val binder = ItemQuestionnaireSignatureBinding.bind(signatureMainLayout)
                        if (feedBackPOJO.questionnaireEntry.isMandatory && isSubmitted) {
                            if (signatureAddedMap[entryId] == false) {
                                showMandatoryAlert(
                                    signatureMainLayout,
                                    null,
                                    isParentSection,
                                    parentView,
                                    getString(R.string.questionnaire_detail_mandatory_check_msg),
                                    true
                                )
                                binder.questionnaireDetailItemSignatureInfo.performClick()
                                return null
                            }
                        }

                        if (signatureAddedMap[entryId] == true) {
                            isFeedbackGiven = true
                        }
                    }

                    ModelUtil.questionnaireNotesTypeRatingScale -> {
                        val answer = getValueFromSlider(
                            feedBackPOJO.questionnaireEntry.isMandatory,
                            isSubmitted,
                            tag,
                            isParentSection,
                            parentView
                        )
                        if (answer != null) {
                            if (answer.isNotBlank()) {
                                isFeedbackGiven = true
                            }
                            feedBackPOJO.questionnaireEntry.answersList = arrayListOf(answer)
                            questionnaireEntryList.add(feedBackPOJO)
                        } else {
                            return null
                        }
                    }

                    ModelUtil.questionnaireNotesTypeRating -> {
                        val layout: LinearLayout =
                            activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag(tag)
                                ?: return@forEach
                        if (feedBackPOJO.questionnaireEntry.answersList.isNullOrEmpty() && feedBackPOJO.questionnaireEntry.isMandatory && isSubmitted) {
                            showMandatoryAlert(
                                layout,
                                null,
                                isParentSection,
                                parentView,
                                getString(R.string.questionnaire_detail_mandatory_check_msg)
                            )
                            return null
                        }
                        questionnaireEntryList.add(feedBackPOJO)
                    }

                    ModelUtil.historyOfIllnessSOAPType, ModelUtil.pastMedicalHistorySOAPType, ModelUtil.familyHistorySOAPType,
                    ModelUtil.socialHistorySOAPType, ModelUtil.reviewOfSystemsSOAPType, ModelUtil.chiefComplaintsSOAPType,
                    ModelUtil.symptomsSOAPType -> {
                        val soapWidgetEntryMap =
                            questionnaireDetailViewModel.soapTagToIdMap[entryId]
                        soapWidgetEntryMap?.forEach { tagId ->
                            val soapTag = tagId.key.toInt()
                            val soapWidgetEntryObj =
                                feedBackPOJO.soapWidget.soapWidgetEntryPOJOList.firstOrNull { it.soapWidgetEntry?.entryId == tagId.value }
                            soapWidgetEntryObj?.let {
                                val response = getValueFromSOAPView(
                                    soapTag,
                                    soapWidgetEntryObj,
                                    isSubmitted,
                                    isParentSection,
                                    parentView
                                )
                                if (response == null) {
                                    return null
                                } else if (response == true) {
                                    isFeedbackGiven = true
                                }
                            }
                        }
                        questionnaireEntryList.add(feedBackPOJO)
                    }

                }
            }
        }

        if (!isFeedbackGiven && questionnaireDetailViewModel.physicianTemplates.patientQuesMap?.isFilled == false && isTextFieldPresent) {
            UIUtil.showToast(this, (R.string.questionnaire_detail_empty_msg), Toast.LENGTH_LONG)
            return null
        }


        val questionnaireDetailPOJO = QuestionnaireDetailPOJO()
        questionnaireDetailPOJO.patientQuesMap =
            questionnaireDetailViewModel.questionnaireDetailPOJO.patientQuesMap
        questionnaireDetailPOJO.patientQuesMap?.isSubmitted = isSubmitted
        questionnaireDetailPOJO.questionnairEntryPOJOList = questionnaireEntryList

        if (questionnaireDetailViewModel.questionnaireDetailPOJO.questionnairEntryPOJOList.any {
                val notesType = it.questionnaireEntry.notesType
                notesType == ModelUtil.questionnaireNotesTypePrimaryContactDetail || notesType == ModelUtil.questionnaireNotesTypePersonalDetails || notesType == ModelUtil.questionnaireNotesTypeInsurance
            }) {
            questionnaireDetailPOJO.saveNormalQuestionnaire =
                questionnaireDetailViewModel.questionnaireDetailPOJO.questionnairEntryPOJOList.any {
                    val notesType = it.questionnaireEntry.notesType
                    notesType != ModelUtil.questionnaireNotesTypePrimaryContactDetail && notesType != ModelUtil.questionnaireNotesTypePersonalDetails && notesType != ModelUtil.questionnaireNotesTypeInsurance && notesType != ModelUtil.questionnaireNotesTypeLabel && notesType != ModelUtil.questionnaireNotesTypeSignature
                }
        }

        return questionnaireDetailPOJO
    }

    private fun getValueFromSOAPView(
        tag: Int,
        soapWidgetEntryObj: SOAPWidgetEntryPOJO,
        isSubmitted: Boolean,
        isParentSection: Boolean,
        parentView: Any
    ): Boolean? {
        when (soapWidgetEntryObj.soapWidgetEntry?.entryType) {
            ModelUtil.soapWidgetHeaderType -> {
                if (soapWidgetEntryObj.soapWidgetEntry?.isMandatory == true) {
                    soapWidgetEntryObj.soapWidgetEntry?.entryAnswer =
                        soapWidgetEntryObj.soapWidgetEntry?.entryName
                    return true
                }
            }

            ModelUtil.soapWidgetSimpleQuestionType, ModelUtil.soapWidgetTextBoxType -> {

                val answer = getValueFromTextView(
                    soapWidgetEntryObj.soapWidgetEntry?.isMandatory,
                    isSubmitted,
                    tag,
                    isParentSection,
                    parentView
                )
                if (answer != null) {
                    soapWidgetEntryObj.soapWidgetEntry?.entryAnswer = answer
                    return answer.isNotBlank()
                } else {
                    return null
                }
            }

            ModelUtil.soapWidgetRatingScaleType -> {

                val answer = getValueFromSlider(
                    soapWidgetEntryObj.soapWidgetEntry?.isMandatory,
                    isSubmitted,
                    tag,
                    isParentSection,
                    parentView
                )
                if (answer != null) {
                    soapWidgetEntryObj.soapWidgetEntry?.entryAnswer = answer
                    return answer.isNotBlank()

                } else {
                    return null
                }

            }

            ModelUtil.soapWidgetYesOrNoQuestionType -> {
                val singleChoiceRadioGroup: RadioGroup =
                    activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag(tag)
                        ?: return false
                val singleChoiceRadioGroupLayout =
                    (singleChoiceRadioGroup.parent as ViewGroup).parent as CardView

                val view = singleChoiceRadioGroup.parent as ViewGroup

                var answer = ""

                if ((singleChoiceRadioGroup.getChildAt(0) as RadioButton).isChecked) {
                    answer = getString(R.string.yes_str)
                    if (soapWidgetEntryObj.soapWidgetEntry?.soapWidgetSelectableEntry?.isAddCommentsForYes == true) {
                        val editText = view.getChildAt(view.childCount - 1) as MDTextInputEdittext
                        if (editText.text.toString().isNotBlank()) {
                            answer += ModelUtil.swOptionsSeparatorSymbol + ModelUtil.appendStringYesComments + editText.text.toString()
                        }
                    }
                } else if ((singleChoiceRadioGroup.getChildAt(1) as RadioButton).isChecked) {
                    answer = getString(R.string.no_str)
                    if (soapWidgetEntryObj.soapWidgetEntry?.soapWidgetSelectableEntry?.isAddCommentsForNo == true) {
                        val editText = view.getChildAt(view.childCount - 1) as MDTextInputEdittext
                        if (editText.text.toString().isNotBlank()) {
                            answer += ModelUtil.swOptionsSeparatorSymbol + ModelUtil.appendStringNoComments + editText.text.toString()
                        }
                    }
                }

                if (soapWidgetEntryObj.soapWidgetEntry?.isMandatory == true && answer.isEmpty() && isSubmitted) {
                    showMandatoryAlert(
                        singleChoiceRadioGroupLayout,
                        null,
                        isParentSection,
                        parentView,
                        getString(R.string.questionnaire_detail_mandatory_check_msg)
                    )
                    return null
                } else {
                    soapWidgetEntryObj.soapWidgetEntry?.entryAnswer = answer
                    return answer.isNotBlank()
                }
            }

            ModelUtil.soapWidgetCheckListType, ModelUtil.soapWidgetCheckBoxType, ModelUtil.soapWidgetRadioType -> {
                val layout: LinearLayout =
                    activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag(tag)
                        ?: return false
                if (soapWidgetEntryObj.soapWidgetEntry?.soapWidgetSelectableEntry?.isIncludeOthers == true) {
                    val otherOption =
                        soapWidgetEntryObj.soapWidgetEntry?.soapWidgetSelectableEntry?.optionList?.last()
                    if (otherOption?.isOther == true && otherOption.checked && otherOption.otherValue.isNullOrBlank()) {
                        showMandatoryAlert(
                            layout,
                            null,
                            isParentSection,
                            parentView,
                            getString(R.string.questionnaire_detail_others_text_not_given)
                        )
                        return null
                    }
                }
                var answer =
                    TemplateUIUtil.getValueFromNestedCheckList(soapWidgetEntryObj.soapWidgetEntry?.soapWidgetSelectableEntry?.optionList)
                if (soapWidgetEntryObj.soapWidgetEntry?.soapWidgetSelectableEntry?.isAdditionalComments == true) {
                    val editText = layout.getChildAt(layout.childCount - 1) as MDTextInputEdittext
                    if (editText.text.toString().isNotBlank()) {
                        if (answer.isNotBlank()) {
                            answer += ModelUtil.swOptionsSeparatorSymbol
                        }
                        answer += ModelUtil.appendStringCommentsValue + editText.text.toString()
                    }
                }
                soapWidgetEntryObj.soapWidgetEntry?.entryAnswer = answer
                if (soapWidgetEntryObj.soapWidgetEntry?.isMandatory == true && answer.isEmpty() && isSubmitted) {
                    showMandatoryAlert(
                        layout,
                        null,
                        isParentSection,
                        parentView,
                        getString(R.string.questionnaire_detail_mandatory_check_msg)
                    )
                    return null
                } else {
                    soapWidgetEntryObj.soapWidgetEntry?.entryAnswer = answer
                    return answer.isNotBlank()
                }
            }

            ModelUtil.soapWidgetTableType -> {
                soapWidgetEntryObj.soapWidgetEntry?.soapWidgetTableData?.let {
                    soapWidgetEntryObj.soapWidgetEntry?.entryAnswer = getValueFromTable(it)
                }
                return true
            }
        }
        return false
    }

    private fun isValidState(
        countryKey: String,
        state: String,
        stateET: MDTextInputAutoCompleteTextView,
        stateLayout: MDTextInputLayout,
        isParentSection: Boolean,
        parentView: Any
    ): Boolean {
        val stateList = questionnaireDetailViewModel.insuranceCommonData?.stateList?.filter {
            it.countryCode == countryKey
        }

        if (stateList?.isNotEmpty() == true) {
            if (!stateList.first().stateList.any {
                    state.equals(
                        it,
                        true
                    )
                }) {
                showMandatoryAlert(
                    stateLayout,
                    stateET,
                    isParentSection,
                    parentView,
                    getString(R.string.error_wrong_state)
                )
                return false
            }
        } else if (!ModelUtil.hasAlpha(state)) {
            showMandatoryAlert(
                stateLayout,
                stateET,
                isParentSection,
                parentView,
                getString(R.string.error_invalid_state)
            )
            return false
        }
        return true
    }

    private fun getAllergyData(
        allergyTypeMainLayout: LinearLayout,
        type: String,
        isSubmitted: Boolean,
        isParentSection: Boolean,
        parentView: Any
    ): ArrayList<IntakeQuestAllergyEntry>? {

        val intakeQuestAllergyEntryList = arrayListOf<IntakeQuestAllergyEntry>()


        val childCount = allergyTypeMainLayout.childCount

        for (index in 0 until childCount) {
            val allergyChildView =
                ItemQuestionnaireAllergyEntryBinding.bind(allergyTypeMainLayout.getChildAt(index))
            val allergyName = allergyChildView.questionnaireDetailItemAllergyNameET.text.toString()
            val allergySeverity =
                allergyChildView.questionnaireDetailItemAllergySeverityET.text.toString()
            val allergyReaction =
                allergyChildView.questionnaireDetailItemAllergyReactionsET.text.toString()

            if (allergyName.isNotBlank() || allergySeverity.isNotBlank() || allergyReaction.isNotBlank()) {

                if (isSubmitted) {
                    if (allergyName.isBlank()) {
                        showMandatoryAlert(
                            allergyChildView.questionnaireDetailItemAllergyNameLayout,
                            allergyChildView.questionnaireDetailItemAllergyNameET,
                            isParentSection,
                            parentView,
                            getString(R.string.error_empty_allergen_name)
                        )
                        return null
                    }

                    if (!ModelUtil.hasAlpha(allergyName)) {
                        showMandatoryAlert(
                            allergyChildView.questionnaireDetailItemAllergyNameLayout,
                            allergyChildView.questionnaireDetailItemAllergyNameET,
                            isParentSection,
                            parentView,
                            getString(R.string.error_invalid_allergen_name)
                        )

                        return null
                    }

                    if (allergySeverity.isBlank()) {
                        showMandatoryAlert(
                            allergyChildView.questionnaireDetailItemAllergySeverityLayout,
                            allergyChildView.questionnaireDetailItemAllergySeverityET,
                            isParentSection,
                            parentView,
                            getString(R.string.error_empty_severity)
                        )
                        return null
                    }
                }

                val intakeQuestAllergyEntry = IntakeQuestAllergyEntry()
                intakeQuestAllergyEntry.allergen = allergyName
                intakeQuestAllergyEntry.severity = allergySeverity
                intakeQuestAllergyEntry.reactions = allergyReaction
                intakeQuestAllergyEntry.type = type
                intakeQuestAllergyEntryList.add(intakeQuestAllergyEntry)
            }


        }

        return intakeQuestAllergyEntryList
    }


    private fun animateOthersOption(mdTextInputEditText: MDTextInputEdittext, show: Boolean) {
        val heightValue = if (show) {
            100
        } else {
            0
        }
        val headerAnim =
            ValueAnimator.ofInt(mdTextInputEditText.height, heightValue).setDuration(300)

        headerAnim.addUpdateListener {
            try {
                mdTextInputEditText.layoutParams.height = it.animatedValue as Int
                mdTextInputEditText.requestLayout()
            } catch (e: IllegalStateException) {
            } catch (e: NullPointerException) {
            }
        }

        headerAnim.start()
    }


    private fun showDatePicker(
        edittext: TextView,
        isDOB: Boolean = false,
        isInsurance: Boolean = false
    ) {

        val value = edittext.text.toString().trim()

        var localDate = if (value.isNotBlank()) {
            LocalDate.parse(value, ModelUtil.charmDisplayDateFormat())
        } else {
            LocalDate.now()
        }

        val minDate = if (isDOB) {
            LocalDate(LocalDate.now().year - 150, 1, 1).toDateTimeAtStartOfDay()
        } else {

            if (isInsurance) {
                val binding = LayoutPatientInsuranceBinding.bind(
                    activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag<LinearLayout>(
                        insuranceTag
                    )
                )
                if (!binding.addInsuranceDOBET.text.isNullOrBlank()) {
                    LocalDate.parse(
                        binding.addInsuranceDOBET.text.toString(),
                        ModelUtil.charmDisplayDateFormat()
                    ).toDateTimeAtStartOfDay()
                } else {
                    LocalDate(LocalDate.now().year - 150, 1, 1).toDateTimeAtStartOfDay()
                }
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
            { _, yr, month, dt ->

                val text = LocalDate(yr, month + 1, dt)
                edittext.text = text.toString(ModelUtil.charmDisplayDateFormat())


            },
            localDate.year, localDate.monthOfYear - 1, localDate.dayOfMonth
        )




        dialog.datePicker.minDate = minDate.millis
        dialog.datePicker.maxDate = maxDate.millis


        dialog.show()
    }


    private fun showRaceDialog(raceET: EditText) {
        val screenWidth = UIUtil.getScreenWidth(this)
        val width =
            screenWidth - (2 * resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))

        val raceDialog = RaceSelectDialog(this, width, raceET)
        raceDialog.raceList.clear()
        raceDialog.raceList.addAll(
            questionnaireDetailViewModel.patientCommonData?.race ?: arrayListOf()
        )
        raceDialog.show()
    }
    //endregion

    fun performPayerSearch(inputFinishChecker: Runnable, isImmediate: Boolean = false) {
        handler.removeCallbacks(inputFinishChecker)

        val delay = if (isImmediate) {
            100L
        } else 600L

        handler.postDelayed(inputFinishChecker, delay)
    }

    private fun onPayerChange(position: Int) {

        if (insuranceTag != -1) {

            val parentLayout =
                LayoutPatientInsuranceBinding.bind(
                    activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag<LinearLayout>(
                        insuranceTag
                    )
                )
            val payer = questionnaireDetailViewModel.payerList.getOrNull(position)
            if (payer != null) {
                parentLayout.addInsurancePayerIdET.setText(payer.payerId)
            }
        }
    }

    private fun onRelationshipChange(isSelf: Boolean, updatePat: Boolean = true) {

        if (insuranceTag != -1) {

            val parentLayout =
                LayoutPatientInsuranceBinding.bind(
                    activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag<LinearLayout>(
                        insuranceTag
                    )
                )


            val toDisable = !isSelf
            parentLayout.addInsuranceFirstNameET.isEnabled = toDisable
            parentLayout.addInsuranceLastNameET.isEnabled = toDisable
            parentLayout.addInsuranceDOBET.isEnabled = toDisable
            parentLayout.addInsuranceGenderET.isEnabled = toDisable

            if (updatePat) {
                if (isSelf) {
                    getPatientDetails {
                        parentLayout.addInsuranceAddressCheckbox.isChecked = isSelf
                        parentLayout.addInsuranceAddressCheckbox.isEnabled = toDisable
                        parentLayout.addInsuranceFirstNameET.setText(questionnaireDetailViewModel.patientDetailForInsurance?.firstName)
                        parentLayout.addInsuranceLastNameET.setText(questionnaireDetailViewModel.patientDetailForInsurance?.lastName)
                        parentLayout.addInsuranceDOBET.setText(
                            questionnaireDetailViewModel.patientDetailForInsurance?.dateOfBirth?.toString(
                                ModelUtil.charmDisplayDateFormat()
                            )
                        )
                        parentLayout.addInsuranceGenderET.setText(questionnaireDetailViewModel.patientDetailForInsurance?.gender)
                    }
                } else {
                    parentLayout.addInsuranceAddressCheckbox.isChecked = isSelf
                    parentLayout.addInsuranceAddressCheckbox.isEnabled = toDisable
                    parentLayout.addInsuranceFirstNameET.setText("")
                    parentLayout.addInsuranceLastNameET.setText("")
                    parentLayout.addInsuranceDOBET.setText("")
                    parentLayout.addInsuranceGenderET.setText("")
                }
            } else {
                parentLayout.addInsuranceAddressCheckbox.isChecked = isSelf
                parentLayout.addInsuranceAddressCheckbox.isEnabled = toDisable

                onInsuredAddressCheckChange(isSelf, updatePat)
            }

        }

    }

    private fun onInsuredAddressCheckChange(isChecked: Boolean, updatePat: Boolean = true) {

        if (insuranceTag != -1) {
            val parentLayout =
                LayoutPatientInsuranceBinding.bind(
                    activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag<LinearLayout>(
                        insuranceTag
                    )
                )

            val toDisable = !isChecked
            parentLayout.addInsuranceLine1ET.isEnabled = toDisable
            parentLayout.addInsuranceLine2ET.isEnabled = toDisable
            parentLayout.addInsuranceCityET.isEnabled = toDisable
            parentLayout.addInsuranceStateET.isEnabled = toDisable
            parentLayout.addInsuranceCountryET.isEnabled = toDisable
            parentLayout.addInsurancePinCodeET.isEnabled = toDisable
            parentLayout.addInsuranceHomePhoneET.isEnabled = toDisable
            parentLayout.addInsuranceMobilePhoneET.isEnabled = toDisable


            if (updatePat) {
                if (isChecked) {
                    getPatientDetails {


                        handler.postDelayed({
                            val postalAddress =
                                questionnaireDetailViewModel.patientDetailForInsurance?.postalAddress
                                    ?: PostalAddress()

                            parentLayout.addInsuranceLine1ET.setText(postalAddress.line1)
                            parentLayout.addInsuranceLine2ET.setText(postalAddress.line2)
                            parentLayout.addInsuranceCityET.setText(postalAddress.city)
                            parentLayout.addInsuranceCountryET.setText(questionnaireDetailViewModel.insuranceCommonData?.country?.firstOrNull { it.key == postalAddress.country }?.name)
                            parentLayout.addInsuranceStateET.setText(postalAddress.state)
                            parentLayout.addInsurancePinCodeET.setText(postalAddress.postalCode)

                            val contactDetails =
                                questionnaireDetailViewModel.patientDetailForInsurance?.contactDetails
                                    ?: ContactDetails()

                            parentLayout.addInsuranceHomePhoneET.setText(contactDetails.homePhone)
                            parentLayout.addInsuranceMobilePhoneET.setText(contactDetails.mobile)
                        }, 200)
                    }
                } else {
                    parentLayout.addInsuranceLine1ET.setText("")
                    parentLayout.addInsuranceLine2ET.setText("")
                    parentLayout.addInsuranceCityET.setText("")
                    parentLayout.addInsuranceCountryET.setText("")
                    parentLayout.addInsuranceStateET.setText("")
                    parentLayout.addInsurancePinCodeET.setText("")
                    parentLayout.addInsuranceHomePhoneET.setText("")
                    parentLayout.addInsuranceMobilePhoneET.setText("")
                }
            }
        }
    }


    private fun getPatientDetails(callback: (() -> Unit)) {

        if (questionnaireDetailViewModel.patientDetailForInsurance?.practicePatientId.isNullOrBlank()) {

            progressDialog.show()
            questionnaireDetailViewModel.getPatientDetail().observe(this) { resp ->

                if (resp != null) {
                    progressDialog.dismiss()

                    if (resp is Patient) {
                        questionnaireDetailViewModel.patientDetailForInsurance = resp
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

    override fun onBackPressed() {
        if (questionnaireDetailViewModel.isDataEditAllowed) {
            backPressAlertDialogBuilder.show()
        } else {
            goBack()
        }
    }

    private fun goBack() {
        if (questionnaireDetailViewModel.isHealthPassport && questionnaireDetailViewModel.physicianTemplates.name == ModelUtil.questionnaireNameValuePermission) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            super.onBackPressed()
        } else {

            if (questionnaireDetailViewModel.isSaved) {
                if (questionnaireDetailViewModel.physicianTemplates.patientQuesMap?.isFilled == false) {
                    setResult(Activity.RESULT_OK)
                }
            }
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    //region Mandatory alert dialog and focus

    private fun showMandatoryAlert(
        view: View,
        editText: EditText?,
        isParentSection: Boolean,
        parentView: Any,
        message: String,
        isClickableComponent: Boolean = false
    ) {
        if (isParentSection) {
            val expandableSectionLayout = parentView as ExpandableSectionLayout
            if (!expandableSectionLayout.getIsExpanded()) {
                expand(view, editText, expandableSectionLayout, message, isClickableComponent)
            } else {
                showAlert(view, editText, message, isClickableComponent)
            }
        } else {
            showAlert(view, editText, message, isClickableComponent)
        }
    }

    private fun showAlert(
        view: View,
        editText: EditText?,
        message: String,
        isClickableComponent: Boolean
    ) {
        UIUtil.showToast(this, message, Toast.LENGTH_SHORT)
        if (isClickableComponent) {
            activityQuestionnaireDetailBinding.questionnaireDetailScroll.scrollToView(view)
        } else if (editText != null) {
            showFocus(view, editText, activityQuestionnaireDetailBinding.questionnaireDetailScroll)
        } else {
            showFocus(view, activityQuestionnaireDetailBinding.questionnaireDetailScroll)
        }
    }
    //endregion

    //region expand section view
    private fun expand(
        view: View,
        editText: EditText?,
        parentView: ExpandableSectionLayout,
        message: String, isClickableComponent: Boolean
    ) {
        parentView.getContentView()?.let { contentView ->
            contentView.visibility = View.VISIBLE
            val screenHeight = resources.displayMetrics.heightPixels

            val screenLocation = intArrayOf(0, 0)
            contentView.getLocationInWindow(screenLocation)

            val maxHeight = screenHeight - screenLocation[1]

            val heightAnimator = ValueAnimator.ofInt(contentView.measuredHeight, maxHeight)
            heightAnimator.addUpdateListener {
                val layoutParams = contentView.layoutParams
                layoutParams?.height = LinearLayout.LayoutParams.WRAP_CONTENT
                contentView.layoutParams = layoutParams
                parentView.getHeaderView()?.quesSectionCollapseImg?.animate()?.rotation(180.0f)
                    ?.start()
            }
            heightAnimator.addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    parentView.getHeaderView()?.quesSectionSeparatorView?.visibility = View.VISIBLE
                    parentView.setExpanded(true)
                    showAlert(view, editText, message, isClickableComponent)
                }
            })

            heightAnimator.duration = ModelUtil.animationDuration
            heightAnimator.start()
        }
    }
    //endregion

    private fun getValueFromSlider(
        isMandatory: Boolean?,
        isSubmitted: Boolean,
        tag: Int,
        isParentSection: Boolean,
        parentView: Any
    ): String? {
        val slider: Slider? =
            activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag(
                tag
            )

        if (slider == null) {
            return ""
        }

        val sliderLayout = (slider.parent as ViewGroup).parent.parent as CardView
        var answer = ""
        if (questionnaireDetailViewModel.sliderIsChangedList.contains(tag)) {
            answer = slider.value.toInt().toString()
        }

        return if (answer.isBlank() && isMandatory == true && isSubmitted) {
            showMandatoryAlert(
                sliderLayout,
                null,
                isParentSection,
                parentView,
                getString(R.string.questionnaire_detail_mandatory_check_msg)
            )
            null
        } else {
            answer
        }
    }

    private fun getValueFromTextView(
        isMandatory: Boolean?,
        isSubmitted: Boolean,
        tag: Int,
        isParentSection: Boolean,
        parentView: Any
    ): String? {
        val questionEditText: MDTextInputEdittext =
            activityQuestionnaireDetailBinding.questionnaireDetailMainLayout.findViewWithTag(tag)
                ?: return ""
        val questionEditTextLayout =
            (questionEditText.parent as ViewGroup).parent as CardView
        val answer = questionEditText.text.toString()

        return if (answer.isBlank() && isMandatory == true && isSubmitted) {
            showMandatoryAlert(
                questionEditTextLayout,
                questionEditText,
                isParentSection,
                parentView,
                getString(R.string.questionnaire_detail_mandatory_check_msg)
            )
            null
        } else {
            answer
        }
    }

    private fun isConsentForm(): Boolean {
        return questionnaireDetailViewModel.physicianTemplates.type == "Consent Form"
    }


}
