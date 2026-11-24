package com.charm.phr.mphr.util

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.InputFilter
import android.text.InputType
import android.text.Layout
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemAddFlowsheetLayoutBinding
import com.charm.phr.mphr.databinding.ItemAddFlowsheetRatingBinding
import com.charm.phr.mphr.databinding.ItemQuestionnaireOptionsRadioButton2Binding
import com.charm.phr.mphr.databinding.ItemQuestionnairePermissionBinding
import com.charm.phr.mphr.model.OptionData
import com.charm.phr.mphr.model.SOAPTemplateTableCell
import com.charm.phr.mphr.ui.components.MDTextInputEdittext
import com.google.android.material.slider.Slider
import com.google.gson.JsonArray
import com.google.gson.JsonObject

object TemplateUIUtil {
    /**
     * Method to render a Q/A based edittext
     */
    fun createQuestionAnswer(
        isSimpleQuestion: Boolean,
        parentView: LinearLayout,
        notes: String,
        entryId: String,
        isMandatory: Boolean,
        answer: String?,
        tagToIdMap: LinkedHashMap<String, String>,
        isParentSection: Boolean, isDataEditAllowed: Boolean, context: Context, tagNo: Int, isNotesType: Boolean = false
    ) {
        val cardView = ItemAddFlowsheetLayoutBinding.inflate(LayoutInflater.from(context), parentView, false)

        cardView.addFlowsheetDataHeadingTV.text = notes

        if (isNotesType) {
            cardView.addFlowsheetDataHeadingTV.visibility = View.GONE
            cardView.addFlowsheetDataView.visibility = View.GONE
        }

        if (isParentSection) {
            renderSectionCardView(cardView, context)
        }


        val questionAnsET =
            LayoutInflater.from(context).inflate(
                R.layout.item_add_flowsheet_question,
                cardView.addFlowsheetDataEntryLayout,
                false
            ) as MDTextInputEdittext

        if (isDataEditAllowed) {
            questionAnsET.tag = tagNo
            tagToIdMap[tagNo.toString()] = entryId
        }

        if (isMandatory) {
            UIUtil.makeTextViewMandatory(cardView.addFlowsheetDataHeadingTV)
        }

        if (isSimpleQuestion) {

            val questionAnsETHeight =
                context.resources.getDimension(R.dimen.questionnaire_detail_simple_ques_ans_value_height)
                    .toInt()
            questionAnsET.layoutParams.height = questionAnsETHeight
            questionAnsET.minHeight = questionAnsETHeight
            questionAnsET.requestLayout()

            questionAnsET.gravity = Gravity.CENTER_VERTICAL

            /*
                        questionAnsET.imeOptions = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            */
            questionAnsET.maxLines = 1
            questionAnsET.filters = arrayOf(InputFilter.LengthFilter(ValidationUtil.questSingleLineMaxLength))
            questionAnsET.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES

        } else {
            questionAnsET.filters = arrayOf(InputFilter.LengthFilter(ValidationUtil.questMultipleLineMaxLength))
        }


        questionAnsET.setText(answer)
        questionAnsET.setSelection(questionAnsET.text?.length ?: 0)

        if (!isDataEditAllowed) {
            questionAnsET.isEnabled = false
        }

        cardView.addFlowsheetDataEntryLayout.addView(questionAnsET)
        parentView.addView(cardView.root)
    }

    fun renderSectionCardView(cardView: ItemAddFlowsheetLayoutBinding, context: Context) {
        cardView.addFlowsheetDataEntryCV.elevation = 0F
        cardView.addFlowsheetDataEntryCV.radius = 0F
        val layoutParam = cardView.addFlowsheetDataEntryCV.layoutParams as LinearLayout.LayoutParams
        layoutParam.setMargins(0, 0, 0, 0)
        layoutParam.marginStart =
            context.resources.getDimensionPixelOffset(R.dimen.questionnaire_section_card_margin_horizontal)
        layoutParam.marginEnd =
            context.resources.getDimensionPixelOffset(R.dimen.questionnaire_section_card_margin_horizontal)
        cardView.addFlowsheetDataView.visibility = View.GONE
    }

    /**
     * Method to render a label
     */
    fun createLabel(
        parentView: LinearLayout,
        notes: String,
        fontSize: Int,
        textAlignment: String,
        fontStyle: String,
        fontWeight: String,
        fontDecoration: String,
        context: Context,
        isHealthPassport: Boolean,
        physicianTemplatesName: String?,
        isSOAP: Boolean = false,
        isMandatory: Boolean = false,
        tagToIdMap: LinkedHashMap<String, String>? = null,
        isDataEditAllowed: Boolean = false,
        entryId: String,
        tagNo: Int
    ) {


        if (isHealthPassport && physicianTemplatesName == ModelUtil.questionnaireNameValuePermission) {

            val constraintLayoutMain = ItemQuestionnairePermissionBinding.inflate(
                LayoutInflater.from(context),
                parentView,
                false
            )
            constraintLayoutMain.questionnairePermissionTV.text =
                notes
            parentView.addView(constraintLayoutMain.root)

        } else {
            val label = TextView(context)
            val labelLayoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            label.text = notes

            label.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.questionnaireOtherOptionLabelTxtColor
                )
            )
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())


            when (textAlignment) {

                ModelUtil.questionnaireLabelAlignmentRight -> {
                    label.gravity = Gravity.END
                }

                ModelUtil.questionnaireLabelAlignmentCenter -> {
                    label.gravity = Gravity.CENTER
                }

                ModelUtil.questionnaireLabelAlignmentJustify -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        label.justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD
                    }
                }

                else -> { // ModelUtil.feedbackLabelAlignmentLeft
                    label.gravity = Gravity.START
                }
            }


//        label.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);


            val typefaceRegular = UIUtil.getFont(context, R.font.open_sans)


            label.typeface = when {
                ((fontWeight.equals(
                    ModelUtil.questionnaireLabelAlignmentBold,
                    true
                ) || fontWeight == "700") && fontStyle.equals(
                    ModelUtil.questionnaireLabelAlignmentItalics,
                    true
                )) -> {
                    Typeface.create(typefaceRegular, Typeface.BOLD_ITALIC)
                }

                (fontWeight.equals(ModelUtil.questionnaireLabelAlignmentBold, true) || fontWeight == "700") -> {
                    Typeface.create(typefaceRegular, Typeface.BOLD)
                }

                (fontStyle.equals(ModelUtil.questionnaireLabelAlignmentItalics, true)) -> {
                    Typeface.create(typefaceRegular, Typeface.ITALIC)
                }

                else -> {
                    typefaceRegular
                }
            }

            if (fontDecoration.equals(ModelUtil.questionnaireLabelAlignmentUnderline, true) || fontDecoration.contains("underline")) {
                label.paintFlags = Paint.UNDERLINE_TEXT_FLAG or label.paintFlags
            }

            labelLayoutParams.marginEnd =
                context.resources.getDimensionPixelOffset(R.dimen.card_margin_horizontal)
            labelLayoutParams.marginStart =
                context.resources.getDimensionPixelOffset(R.dimen.card_margin_horizontal)
            labelLayoutParams.topMargin =
                context.resources.getDimensionPixelOffset(R.dimen.questionnaire_card_margin_vertical)
            labelLayoutParams.bottomMargin =
                context.resources.getDimensionPixelOffset(R.dimen.questionnaire_card_margin_vertical)

//            if (isLastIndex(feedbackPOJO)) {
//                labelLayoutParams.bottomMargin = resources.getDimensionPixelOffset(R.dimen.card_padding)
//            }
            label.layoutParams = labelLayoutParams
            if (isSOAP && isMandatory && isDataEditAllowed) {
                label.tag = tagNo
                tagToIdMap?.set(tagNo.toString(), entryId)
            }
            parentView.addView(label)
            label.requestLayout()
        }
    }

    /**
     * Method to render a slider based rating scale
     */

    fun createRatingScale(
        notes: String?,
        isMandatory: Boolean,
        parentView: LinearLayout,
        minValue: Float,
        maxValue: Float,
        answer: String?,
        entryId: String,
        tagToIdMap: LinkedHashMap<String, String>,
        isParentSection: Boolean,
        context: Context,
        isDataEditAllowed: Boolean,
        tagNo: Int,
        sliderIsChangedList: ArrayList<Int>
    ) {

        val cardView =
            ItemAddFlowsheetLayoutBinding.inflate(LayoutInflater.from(context), parentView, false)
        cardView.addFlowsheetDataHeadingTV.text = notes

        if (isMandatory) {
            UIUtil.makeTextViewMandatory(cardView.addFlowsheetDataHeadingTV)
        }
        if (isParentSection) {
            renderSectionCardView(cardView, context)
        }

        val sliderLayout =
            ItemAddFlowsheetRatingBinding.inflate(LayoutInflater.from(context), cardView.root, false)
        val slider = sliderLayout.root.getChildAt(0) as Slider
        val sliderValueTV = sliderLayout.root.getChildAt(1) as TextView

        slider.valueTo = maxValue
        slider.value = minValue
        slider.valueFrom = minValue

        if (!answer.isNullOrBlank()) {
            slider.value = answer.toFloatOrNull() ?: 0f
            sliderIsChangedList.add(tagNo)
        }
        slider.setLabelFormatter { value ->
            val intVal = value.toInt()
            "$intVal"
        }
        val sliderValue = answer?.toFloatOrNull()
        if (sliderValue != null) {
            slider.value = sliderValue
            sliderValueTV.text = "${sliderValue.toInt()}"
        }

        if (isDataEditAllowed) {
            slider.tag = tagNo
            tagToIdMap[tagNo.toString()] = entryId

            slider.addOnChangeListener { _, value, _ ->

                sliderValueTV.text = "${value.toInt()}"

            }

            slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {

                }

                override fun onStopTrackingTouch(slider: Slider) {

                    val sliderTag = slider.tag as Int
                    if (!sliderIsChangedList.contains(sliderTag)) {
                        sliderIsChangedList.add(sliderTag)
                    }
                }
            })


        } else {
            slider.isEnabled = false
        }

        cardView.addFlowsheetDataEntryLayout.addView(sliderLayout.root)
        parentView.addView(cardView.root)
    }

    fun createRadioButton(optionsParent: ItemAddFlowsheetLayoutBinding, optionTxt: String, context: Context): RadioButton {
        val optionsButton = ItemQuestionnaireOptionsRadioButton2Binding.inflate(
            LayoutInflater.from(context),
            optionsParent.root,
            false
        )
        optionsButton.root.layoutParams?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        optionsButton.root.requestLayout()

        optionsButton.root.isSingleLine = false
        optionsButton.root.text = optionTxt
        val lp = optionsButton.root.layoutParams as ViewGroup.MarginLayoutParams
        lp.topMargin = lp.bottomMargin
        optionsButton.root.layoutParams = lp
        return optionsButton.root
    }

    fun getTextView(
        context: Context,
        value: String,
        marginStart: Int = context.resources.getDimensionPixelOffset(R.dimen.card_padding),
        marginBottom: Int = context.resources.getDimensionPixelOffset(R.dimen.card_padding)
    ): TextView {
        val textView = TextView(context)
        val mdTextInputEditTextLP = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mdTextInputEditTextLP.marginStart = marginStart
        mdTextInputEditTextLP.marginEnd = marginStart
        mdTextInputEditTextLP.bottomMargin = marginBottom
        // mdTextInputEditTextLP.bottomMargin = resources.getDimensionPixelOffset(R.dimen.card_padding)
        textView.text = value
        textView.layoutParams = mdTextInputEditTextLP
        textView.setTextColor(
            ContextCompat.getColorStateList(
                context,
                R.color.edittext_txt_color
            )
        )
        textView.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            context.resources.getDimensionPixelSize(R.dimen.font_size_for_edittext).toFloat()
        )
        textView.typeface = UIUtil.getFont(context, R.font.open_sans)
        return textView
    }

    fun getEditTextView(
        context: Context,
        marginStart: Int = context.resources.getDimensionPixelOffset(R.dimen.card_padding),
        maxLength: Int = 0,isEnable:Boolean=true
    ): MDTextInputEdittext {
        val padding =
            context.resources.getDimensionPixelOffset(R.dimen.questionnaire_detail_ques_ans_value_padding)
        val mdTextInputEditText = MDTextInputEdittext(context)

        val mdTextInputEditTextLP = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
        mdTextInputEditTextLP.marginStart =
            marginStart
        mdTextInputEditTextLP.marginEnd =
            context.resources.getDimensionPixelOffset(R.dimen.card_padding)
        mdTextInputEditTextLP.bottomMargin = context.resources.getDimensionPixelOffset(R.dimen.card_padding)
        // mdTextInputEditTextLP.bottomMargin = resources.getDimensionPixelOffset(R.dimen.card_padding)
        mdTextInputEditText.layoutParams = mdTextInputEditTextLP
        mdTextInputEditText.setTextColor(
            ContextCompat.getColorStateList(
                context,
                R.color.edittext_txt_color
            )
        )
        if (maxLength != 0) {
            mdTextInputEditText.filters = arrayOf(InputFilter.LengthFilter(maxLength))
        }
        if(!isEnable)
        {
            mdTextInputEditText.isEnabled=false
        }
        mdTextInputEditText.setPadding(padding, padding, padding, padding)
        mdTextInputEditText.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            context.resources.getDimensionPixelSize(R.dimen.font_size_for_edittext).toFloat()
        )
        mdTextInputEditText.typeface = UIUtil.getFont(context, R.font.open_sans)
        mdTextInputEditText.gravity = Gravity.CENTER_VERTICAL
        mdTextInputEditText.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        mdTextInputEditText.setBackgroundResource(R.drawable.item_questionnaire_option_other)

        return mdTextInputEditText
    }

    /*
         NOTE: IMP: Use this method everywhere to get the options from resp string.
         Don't do it yourself. Because this method trims and gives the option string.
         Even if procedure to get options changes, it'll need to be changed in only one place. There'll be uniformity.
         */

    fun getOptionsArrayFromString(optionsString: String): ArrayList<String> {
        var optStr = optionsString
        val optionsArrList = ArrayList<String>()

//To remove the trailing separator symbol(^^)
        val len = optionsString.length

        if (len >= 2 && optStr.substring(len - 2) == ModelUtil.swOptionsSeparatorSymbol) {
            optStr = optStr.substring(0, len - 2)
        }

        val optionsTempArrList = optStr.split(ModelUtil.swOptionsSeparatorSymbol)
        for (str: String in optionsTempArrList) {
            optionsArrList.add(str.trim())
        }
        return optionsArrList

    }

    /*
NOTE: This method returns NULL if the string is not hierarchical (i.e, doesn't begin with ">>").
It returns the option string (by removing the leading ">>") otherwise.
For uniformity use this method alone to get hierarchicalOptionString
*/

    private fun getHierarchicalOptionString(option: String): String? {
        if (option.startsWith(ModelUtil.swHierarchySymbol))
            return option.substring(2).trim()
        return null
    }

    fun getAnswerMapFromString(answerStr: String): LinkedHashMap<String, Any> {
        var optStr = answerStr

        //To remove the trailing separator symbol(^^)
        val len = answerStr.length

        if (len >= 2 && optStr.substring(len - 2) == ModelUtil.swOptionsSeparatorSymbol) {
            optStr = optStr.substring(0, len - 2)
        }

        val optionsTempArrList = ArrayList(optStr.split(ModelUtil.swOptionsSeparatorSymbol))
        return getAnswerMapFromArray(optionsTempArrList)
    }

    private fun getAnswerMapFromArray(answerArr: ArrayList<String>): LinkedHashMap<String, Any> {
        val answerHashMap = LinkedHashMap<String, Any>()
        var nextLevelArr = arrayListOf<String>()
        var currentStr = ""
        answerArr.forEach {
            var answer = it.trim()
            if (getHierarchicalOptionString(answer) == null) {
                if (nextLevelArr.isNotEmpty()) {
                    answerHashMap[currentStr] = getAnswerMapFromArray(nextLevelArr)
                }
                nextLevelArr = arrayListOf()
                currentStr = answer
                answerHashMap[currentStr] = ""
            } else {
                answer = getHierarchicalOptionString(answer) ?: ""
                nextLevelArr.add(answer)
            }
        }
        if (nextLevelArr.isNotEmpty()) {
            answerHashMap[currentStr] = getAnswerMapFromArray(nextLevelArr)
        }
        return answerHashMap
    }

    fun getOptionList(optionsAL: ArrayList<String>, answerAL: LinkedHashMap<String, Any>, viewLevel: Int): ArrayList<OptionData> {
        val optionArr: ArrayList<OptionData> = arrayListOf()
        var i = 0
        while (i < optionsAL.size) {
            val option = optionsAL[i]
            if (getHierarchicalOptionString(option) == null) {
                val optionData = OptionData()
                optionData.optionString = option
                optionData.viewLevel = viewLevel
                i++
                val childOptionList = ArrayList<String>()
                while (i < optionsAL.size && getHierarchicalOptionString(optionsAL[i]) != null) {
                    childOptionList.add(getHierarchicalOptionString(optionsAL[i]) ?: "")
                    i++
                }
                if (option in answerAL) {
                    optionData.checked = true
                }
                if (childOptionList.isNotEmpty()) {
                    if (optionData.checked && answerAL[option] is LinkedHashMap<*, *>) {
                        val childrenAnswer = answerAL[option] as LinkedHashMap<String, Any>
                        optionData.childrenOptionList = getOptionList(childOptionList, childrenAnswer, viewLevel + 1)
                    } else {
                        optionData.childrenOptionList = getOptionList(childOptionList, LinkedHashMap(), viewLevel + 1)
                    }
                }
                if (option in answerAL) {
                    answerAL.remove(option)
                }
                optionArr.add(optionData)
            }
        }
        return optionArr
    }

    fun getValueFromNestedCheckList(optionList: ArrayList<OptionData>?): String {
        var answer = ""
        optionList?.forEach {
            if (it.checked) {
                if (it.isOther && !it.otherValue.isNullOrBlank()) {
                    answer += it.otherValue
                } else {
                    var childLevel = it.viewLevel
                    var ansSubStr = ""
                    while (childLevel > 0) {
                        ansSubStr += ModelUtil.swHierarchySymbol
                        childLevel--
                    }
                    answer += ansSubStr + it.optionString + ModelUtil.swOptionsSeparatorSymbol
                    if (it.childrenOptionList.isNotEmpty()) {
                        val childList = arrayListOf<OptionData>()
                        childList.addAll(it.childrenOptionList)
                        answer += getValueFromNestedCheckList(childList)
                    }
                }
            }
        }
        return answer
    }

    fun renderCommentsView(commentValue: String?, parentLayout: LinearLayout, context: Context, dataEditAllowed: Boolean) {
        val textView = getTextView(context, context.getString(R.string.vaccine_comments_str))
        parentLayout.addView(textView)
        val mdTextInputEditText = getEditTextView(context, isEnable = dataEditAllowed)
        mdTextInputEditText.hint = context.getString(R.string.enter_comments)
        mdTextInputEditText.visibility = View.VISIBLE
        mdTextInputEditText.layoutParams.height = context.resources.getDimension(R.dimen.questionnaire_detail_simple_ques_ans_value_height)
            .toInt()
        if (commentValue?.isNotBlank() == true) {
            mdTextInputEditText.setText(commentValue)
        }
        parentLayout.addView(mdTextInputEditText)
    }

    fun getValueFromTable(tableData: ArrayList<ArrayList<SOAPTemplateTableCell>>): String {
        val tableDataArr = JsonArray()
        tableData.forEach { rowData ->
            val rowArr = JsonArray()
            rowData.forEach { celldata ->
                val cellJson = JsonObject()
                cellJson.addProperty("value", celldata.value)
                cellJson.addProperty("colspan", celldata.colspan)
                cellJson.addProperty("type", celldata.type)
                rowArr.add(cellJson)
            }
            tableDataArr.add(rowArr)
        }
        return tableDataArr.toString()
    }


}