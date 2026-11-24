package com.charm.phr.mphr.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Insets
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.text.InputFilter
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.transition.ChangeBounds
import android.transition.Transition
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.charm.phr.mphr.BuildConfig
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.GraphData
import com.charm.phr.mphr.model.InsuranceTypeChange
import com.charm.phr.mphr.model.MDEntry
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.model.ParamHistoryEntryPOJO
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.ui.components.CustomTypefaceSpan
import com.charm.phr.mphr.ui.components.MDAutoCompleteTextViewAdapter
import com.charm.phr.mphr.ui.components.MDAxisStringFormatter
import com.charm.phr.mphr.ui.components.MDInAppRatingDialog
import com.charm.phr.mphr.ui.components.MDTextInputAutoCompleteTextView
import com.charm.phr.mphr.ui.components.MDTextInputEdittext
import com.charm.phr.mphr.ui.components.MDTextInputLayout
import com.charm.phr.mphr.ui.components.MdRequestPermissionDialog
import com.charm.phr.mphr.ui.components.iconSwitch.GraphMarkerView
import com.charm.phr.mphr.ui.support.SupportActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputLayout
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.zoho.apptics.rateus.AppticsInAppRatings
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.Months
import org.joda.time.Years
import org.joda.time.format.DateTimeFormatter
import java.util.TimeZone

class UIUtil {

    companion object {

        private val TAG = "UIUtil"
        fun showToast(context: Context?, source: String, duration: Int) {

            try {
                if (context == null) return

                val toast = Toast.makeText(context, source, duration)
                toast.setGravity(Gravity.BOTTOM, 0, 50)
                toast.show()
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
            }
        }

        fun showToast(context: Context?, @StringRes sourceId: Int, duration: Int) {

            try {
                if (context == null) return

                val toast = Toast.makeText(context, sourceId, duration)
                toast.setGravity(Gravity.BOTTOM, 0, 50)
                toast.show()
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
            }
        }

        fun enterTransition(): Transition {
            val bounds = ChangeBounds()
            bounds.duration = 300

            return bounds
        }

        fun returnTransition(): Transition {
            val bounds = ChangeBounds()
            bounds.interpolator = DecelerateInterpolator()
            bounds.duration = 300

            return bounds
        }

        fun TextView.setInterpretationColor(interpretation: String) {
            if (interpretation == ModelUtil.labResultsIntepretationAbnormal) {
                setTextColor(
                    ContextCompat.getColor(
                        context, R.color.labResultInterpretationAbnormalTxtColor
                    )
                )
            } else if (interpretation == ModelUtil.labResultsIntepretationNormal) {
                setTextColor(
                    ContextCompat.getColor(
                        context, R.color.labResultInterpretationNormalTxtColor
                    )
                )
            } else {
                setTextColor(
                    ContextCompat.getColor(
                        context, R.color.labResultInterpretationUnknownTxtColor
                    )
                )
            }
        }

        fun convertToDateTimeWithoutFormator(dateString: String): DateTime? {
            return try {
                val dateTime = DateTime.parse(dateString)
                dateTime.withZone(DateTimeZone.getDefault())
            } catch (e: Exception) {
                Log.e(TAG, e.message ?: "Error parsing date")
                null
            }
        }

        fun onInsuranceTypeChange(insuranceType: String): InsuranceTypeChange {

            val insuranceTypeChange = InsuranceTypeChange()

            if (insuranceType == "MEDICARE") {
                insuranceTypeChange.planName = insuranceType
                insuranceTypeChange.memberIdLabel = R.string.add_insurance_id_label_medicare_hash
            } else if (insuranceType == "MEDICAID") {
                insuranceTypeChange.planName = insuranceType
                insuranceTypeChange.memberIdLabel = R.string.add_insurance_id_label_medicaid_hash
            } else if (insuranceType == "TRICARE CHAMPUS") {
                insuranceTypeChange.memberIdLabel = R.string.add_insurance_id_label_sponser
            } else if (insuranceType == "CHAMPVA") {
                insuranceTypeChange.memberIdLabel = R.string.add_insurance_id_label_member
            } else if (insuranceType == "FECA BLK LUNG") {
                insuranceTypeChange.memberIdLabel = R.string.add_insurance_id_label_ssn
            }

            return insuranceTypeChange
        }

        fun setStateAdapter(
            countryName: String,
            stateLayout: MDTextInputLayout,
            stateET: MDTextInputAutoCompleteTextView,
            pinCodeET: MDTextInputEdittext,
            stateAdapter: MDAutoCompleteTextViewAdapter,
            stateList: ArrayList<String>,
            postBoxLayout: TextInputLayout? = null,
            postBoxET: MDTextInputEdittext? = null
        ) {

            if (stateList.isNotEmpty()) {
                stateAdapter.setItemList(stateList)
                val stateStr = stateET.text.toString()
                if (stateStr.isNotBlank()) {
                    if (!stateList.any {
                            it.equals(
                                stateStr,
                                true
                            )
                        }) {
                        stateET.setText("")
                    }
                }
                stateLayout.setShowDropDown(true)
                stateLayout.setShowLine(true)
            } else {
                stateAdapter.setItemList(arrayListOf())
                stateLayout.setShowLine(false)
                stateLayout.setShowDropDown(false)
            }
            applyCountryFilter(countryName, pinCodeET, postBoxLayout, postBoxET)
        }

        fun applyCountryFilter(
            countryName: String,
            pinCodeET: MDTextInputEdittext,
            postBoxLayout: TextInputLayout? = null,
            postBoxET: MDTextInputEdittext? = null
        ) {
            if (countryName.equals("india", ignoreCase = true)) {
                pinCodeET.setText("")
                pinCodeET.filters = arrayOf(InputFilter.LengthFilter(6))
                postBoxLayout?.visibility = View.GONE
                postBoxET?.setText("")

            } else if (countryName.equals("united states", ignoreCase = true)) {

                pinCodeET.setText("")
                pinCodeET.filters = arrayOf(InputFilter.LengthFilter(5))

                postBoxLayout?.visibility = View.VISIBLE

            } else {
                pinCodeET.filters = arrayOf(InputFilter.LengthFilter(10))
                postBoxLayout?.visibility = View.GONE
                postBoxET?.setText("")
            }
        }

        fun makeTextViewMandatory(editText: TextView) {
            val firstWord = editText.text.toString()
            val secondWord = " *"
            val spannable = SpannableString(firstWord + secondWord)
            spannable.setSpan(
                ForegroundColorSpan(editText.textColors.defaultColor),
                0,
                firstWord.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        editText.context,
                        R.color.mandatoryColor
                    )
                ),
                firstWord.length,
                firstWord.length + secondWord.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            editText.setText(spannable)
        }

        fun makeTextViewMandatory(editText: TextInputLayout) {
            val firstWord = editText.hint.toString()
            val secondWord = " *"
            val spannable = SpannableString(firstWord + secondWord)
            /*  spannable.setSpan(ForegroundColorSpan(editText.hintTextColor?.defaultColor!!), 0, firstWord.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
              spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(editText.context , R.color.mandatoryColor)), firstWord.length, firstWord.length + secondWord.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
             */
            spannable.setSpan(
                ForegroundColorSpan(
                    editText.defaultHintTextColor?.defaultColor ?: ContextCompat.getColor(
                        editText.context,
                        R.color.edittext_hint_color
                    )
                ),
                0,
                firstWord.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        editText.context,
                        R.color.mandatoryColor
                    )
                ),
                firstWord.length,
                firstWord.length + secondWord.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            editText.hint = spannable
        }

        fun makePlusColorPrimary(textView: TextView) {

            val word = textView.text.toString()

            val typefaceMedium = getFont(textView.context, R.font.open_sans_bold)

            val spannable = SpannableString(word)
            spannable.setSpan(
                ForegroundColorSpan(textView.textColors.defaultColor),
                0,
                3,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        textView.context,
                        R.color.colorAccent
                    )
                ), 3, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                CustomTypefaceSpan("sans-serif", typefaceMedium ?: Typeface.DEFAULT),
                3,
                5,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            /*
                        spannable.setSpan( AbsoluteSizeSpan(textView.context.resources.getDimensionPixelSize(R) ), 3,5, 0) // set size
            */

            spannable.setSpan(
                ForegroundColorSpan(textView.textColors.defaultColor),
                5,
                word.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            textView.setText(spannable)

        }

        fun formMemberName(member: Member?): String {
            var memberName = ""
            if (member != null) {
                val prefix = member.prefix?.trim()
                val fullName = member.fullName?.trim() ?: ""

                memberName = if (prefix != null && prefix.isNotBlank() && !prefix.equals(
                        "null",
                        true
                    ) && !fullName.contains("Dr.", false)
                )
                    "$prefix. $fullName"
                else fullName
            }
            return memberName
        }

        fun formMemberName(prefix: String?, fullName: String): String {

            return if (!prefix.isNullOrBlank() && !prefix.equals(
                    "null",
                    true
                ) && !fullName.contains("Dr.", false)
            ) {
                "$prefix. $fullName"
            } else {
                fullName
            }
        }

        fun formPatientDetail(patient: Patient): String {
            var patDetailTxt = patient.gender?.replaceFirstChar { it.uppercase() } ?: ""
            if (patient.dateOfBirth != null) {
                val years = Years.yearsBetween(patient.dateOfBirth, LocalDate.now()).years
                if (years > 0) {
                    patDetailTxt += if (years == 1) {
                        ", $years year"
                    } else {
                        ", $years years"
                    }
                } else {
                    val months = Months.monthsBetween(patient.dateOfBirth, LocalDate.now()).months
                    patDetailTxt += if (months == 1) {
                        ", $months month"
                    } else {
                        ", $months months"
                    }
                }
            }
            return patDetailTxt
        }

        fun getPatientAge(patient: Patient): Int {
            if (patient.dateOfBirth != null) {
                val tempYrs = Years.yearsBetween(patient.dateOfBirth, LocalDate.now()).years
                return 0
            } else if (patient.age != null) {
                patient.age?.toIntOrNull() ?: 0
            }

            return 0
        }

        fun formPatientInitials(patient: Patient?): String {
            return formMemberInitials(patient?.fullName ?: "")
        }

        fun formInitials(name: String?): String {
            var initials = ""
            name?.split(" ")?.filter { str -> str.isNotBlank() }?.forEach { str ->
                initials += str[0].uppercase()
            }

            return initials
        }

        fun formMemberInitials(memberName: String): String {
            var initials = ""
            var name = memberName
            if (name.contains(".")) {
                val nameList = ArrayList(name.split("."))
                if (nameList.firstOrNull()?.equals("Dr", ignoreCase = true) == true) {
                    nameList.removeAt(0)
                }
                try {
                    name = nameList.joinToString(" ").trim()
                } catch (e: Exception) {
                    Log.d(TAG, e.toString())
                }
            }

            try {
                val nameArr = name.split(" ")
                if (nameArr.isNotEmpty()) {
                    val firstLetter = nameArr.firstOrNull()?.trim()?.firstOrNull()?.toString() ?: ""
                    if (firstLetter.isNotBlank()) {
                        initials += firstLetter.uppercase()
                    }
                    if (nameArr.size > 1) {
                        val secondLetter = nameArr[1].trim().firstOrNull()?.toString() ?: ""
                        if (secondLetter.isNotBlank()) {
                            initials += secondLetter.uppercase()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
            }

            return initials
        }

        fun formDirectionsForMedication(medPOJO: PatMedPOJO): String {
            var directions = medPOJO.patMedPHRExt.firstOrNull()?.dosageWithUnit ?: ""

            if (directions.isNotBlank()) {
                directions += ", "
            }

            directions += medPOJO.patMedPHRExt.firstOrNull()?.intakeRoute ?: ""

            if (directions.isNotBlank() && !directions.endsWith(", ")) {
                directions += ", "
            }

            directions += medPOJO.patMedPHRExt.firstOrNull()?.intake ?: ""

            if (directions.isNotBlank() && !directions.endsWith(", ")) {
                directions += ", "
            }

            directions += medPOJO.patMedPHRExt.firstOrNull()?.intakeWhen ?: ""

            if (directions.endsWith(", ")) {
                directions = directions.substring(0, directions.length - 2)
            }

            return directions
        }

        fun formDirectionsForSupplement(patientSupplements: PatientSupplements): String {
            var directions =
                patientSupplements.patientSupplementMap?.patientSupplementHistory?.dosage ?: ""
            if (directions.isNotBlank() && directions.trim().toFloatOrNull() == 0f) {
                directions = ""
            }
            if (directions.isNotBlank()) {
                directions += " ${patientSupplements.patientSupplementMap?.patientSupplementHistory?.dosageUnit?.dosageUnit}, "
            }

            directions += patientSupplements.patientSupplementMap?.patientSupplementHistory?.intake
                ?: ""

            if (directions.isNotBlank() && !directions.endsWith(", ")) {
                directions += ", "
            }

            directions += patientSupplements.patientSupplementMap?.patientSupplementHistory?.intakeWhen
                ?: ""


            if (directions.endsWith(", ")) {
                directions = directions.substring(0, directions.length - 2)
            }

            return directions
        }

        fun makeFirstWordFontMedium(context: Context, text: String): SpannableString {

            val typefaceMedium = getFont(context, R.font.open_sans_medium)
            val typefaceRegular = getFont(context, R.font.open_sans)

            val endIndex = text.split("-")[0].length

            val spannableStr = SpannableString(text)
            //   spannableStr.setSpan(CustomTypefaceSpan("sans-serif", typefaceMedium!!), 0, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableStr.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        android.R.color.black
                    )
                ), 0, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            //   spannableStr.setSpan(CustomTypefaceSpan("sans-serif", typefaceRegular!!), endIndex, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableStr.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        R.color.black87
                    )
                ), endIndex, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            return spannableStr
        }

        fun makeTimeZoneAsMedium(context: Context, text: String): SpannableString {

            val typefaceMedium = getFont(context, R.font.open_sans_medium)
            val typefaceRegular = getFont(context, R.font.open_sans)

            val spannableStr = SpannableString(text)
            spannableStr.setSpan(
                CustomTypefaceSpan("sans-serif", typefaceRegular ?: Typeface.DEFAULT),
                0,
                64,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // spannableStr.setSpan(ForegroundColorSpan(ContextCompat.getColor(context ,android.R.color.black)) , 0 , endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableStr.setSpan(
                CustomTypefaceSpan("sans-serif", typefaceMedium ?: Typeface.DEFAULT),
                64,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // spannableStr.setSpan(ForegroundColorSpan(ContextCompat.getColor(context ,R.color.black87)) , endIndex , text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            return spannableStr
        }

        fun makeSpannableStringForFlowsheetFrequency(
            context: Context,
            text: String
        ): SpannableString {

            val typefaceMedium = getFont(context, R.font.open_sans_medium)
            val typefaceRegular = getFont(context, R.font.open_sans)

            val spannableStr = SpannableString(text)
            spannableStr.setSpan(
                CustomTypefaceSpan("sans-serif", typefaceRegular ?: Typeface.DEFAULT),
                0,
                30,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableStr.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        R.color.black87
                    )
                ), 0, 30, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableStr.setSpan(
                CustomTypefaceSpan("sans-serif", typefaceMedium ?: Typeface.DEFAULT),
                30,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableStr.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        R.color.flowsheetFrequencyColor
                    )
                ), 30, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            return spannableStr
        }

        fun makeCharCount(
            context: Context,
            charCount: String,
            maxCharCount: String
        ): SpannableString {

            val typefaceMedium = getFont(context, R.font.open_sans_medium)
            val typefaceRegular = getFont(context, R.font.open_sans)

            val text =
                context.getString(R.string.compose_message_char_count_str, charCount, maxCharCount)
            val spannableStr = SpannableString(text)
            spannableStr.setSpan(
                CustomTypefaceSpan("sans-serif", typefaceMedium ?: Typeface.DEFAULT),
                0,
                charCount.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // spannableStr.setSpan(ForegroundColorSpan(ContextCompat.getColor(context ,android.R.color.black)) , 0 , endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableStr.setSpan(
                CustomTypefaceSpan("sans-serif", typefaceRegular ?: Typeface.DEFAULT),
                charCount.length,
                charCount.length + 4,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableStr.setSpan(
                CustomTypefaceSpan("sans-serif", typefaceMedium ?: Typeface.DEFAULT),
                charCount.length + 4,
                charCount.length + 4 + maxCharCount.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannableStr.setSpan(
                CustomTypefaceSpan("sans-serif", typefaceRegular ?: Typeface.DEFAULT),
                charCount.length + 4 + maxCharCount.length,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // spannableStr.setSpan(ForegroundColorSpan(ContextCompat.getColor(context ,R.color.black87)) , endIndex , text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            return spannableStr
        }

        fun makeInsuranceValidDateTxt(
            context: Context,
            fromDate: DateTime?,
            toDate: DateTime?
        ): SpannableString {

            val typefaceMedium = getFont(context, R.font.open_sans_medium)
            val typefaceRegular = getFont(context, R.font.open_sans)

            val fromDateStr = fromDate?.toString(ModelUtil.charmDisplayDateFormat()) ?: ""
            val toDateStr = toDate?.toString(ModelUtil.charmDisplayDateFormat()) ?: ""

            val text = context.getString(R.string.insurance_date_value, fromDateStr, toDateStr)
            val spannableStr = SpannableString(text)

            spannableStr.setSpan(
                CustomTypefaceSpan("sans-serif", typefaceRegular ?: Typeface.DEFAULT),
                0,
                11,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // spannableStr.setSpan(ForegroundColorSpan(ContextCompat.getColor(context ,android.R.color.black)) , 0 , endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableStr.setSpan(
                CustomTypefaceSpan("sans-serif", typefaceMedium ?: Typeface.DEFAULT),
                11,
                fromDateStr.length + 11,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableStr.setSpan(
                CustomTypefaceSpan("sans-serif", typefaceRegular ?: Typeface.DEFAULT),
                fromDateStr.length + 11,
                fromDateStr.length + 23,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannableStr.setSpan(
                CustomTypefaceSpan("sans-serif", typefaceMedium ?: Typeface.DEFAULT),
                fromDateStr.length + 23,
                text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // spannableStr.setSpan(ForegroundColorSpan(ContextCompat.getColor(context ,R.color.black87)) , endIndex , text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            return spannableStr
        }

        fun Context.getHighlightedText(
            stringResId: Int,
            start: Int,
            end: Int,
            @ColorRes colorResId: Int
        ): SpannableString {
            val spannable = SpannableString(getString(stringResId))
            spannable.setSpan(
                /* what = */ ForegroundColorSpan(ContextCompat.getColor(this, colorResId)),
                /* start = */ start,
                /* end = */ end,
                /* flags = */ Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannable
        }

        fun ofArgb(fraction: Float, startColor: Int, endColor: Int): Int {
            val startA = startColor shr 24 and 0xff
            val startR = startColor shr 16 and 0xff
            val startG = startColor shr 8 and 0xff
            val startB = startColor and 0xff
            val endA = endColor shr 24 and 0xff
            val endR = endColor shr 16 and 0xff
            val endG = endColor shr 8 and 0xff
            val endB = endColor and 0xff
            return startA + (fraction * (endA - startA)).toInt() shl 24 or (
                    startR + (fraction * (endR - startR)).toInt() shl 16) or (
                    startG + (fraction * (endG - startG)).toInt() shl 8) or
                    startB + (fraction * (endB - startB)).toInt()
        }

        fun TextView.setRxDisplayName(patMedPOJO: PatMedPOJO) {
            val uiType = patMedPOJO.uiType
            val drugDetail = patMedPOJO.drugDetail.firstOrNull()
            val genericDrugName = drugDetail?.genericDrugName ?: ""
            val tradeName = drugDetail?.tradeName ?: ""
            val eRxDisplayName = drugDetail?.eRxDisplayName ?: ""
            val productName = drugDetail?.genericProductName ?: ""

            Log.d("setRxDisplayName", "genericDrugName == ${genericDrugName}")
            Log.d("setRxDisplayName", "tradeName       ==  ${tradeName}")
            Log.d("setRxDisplayName", "eRxDisplayName  == ${eRxDisplayName}")
            Log.d("setRxDisplayName", "productName     == ${productName}")
            Log.d("setRxDisplayName", "-------------------------------------")

            this.text = getRxDisplayName(uiType, productName, tradeName, context, eRxDisplayName)
        }

        fun getRxDisplayName(patMedPOJO: PatMedPOJO, context: Context): String? {
            val uiType = patMedPOJO.uiType
            val drugDetail = patMedPOJO.drugDetail.firstOrNull()
            val genericDrugName = drugDetail?.genericDrugName ?: ""
            val tradeName = drugDetail?.tradeName ?: ""
            val eRxDisplayName = drugDetail?.eRxDisplayName ?: ""
            val productName = drugDetail?.genericProductName ?: ""
            return getRxDisplayName(uiType, productName, tradeName, context, eRxDisplayName)
        }

        private fun getRxDisplayName(
            uiType: String?,
            productName: String,
            tradeName: String,
            context: Context,
            eRxDisplayName: String
        ): String? {
            when (uiType) {
                ModelUtil.medicationUiTypeOne -> {
                    if (productName.equals(tradeName, true)) {
                        productName
                    } else {
                        if (tradeName.isNotBlank() && productName.isNotBlank()) {
                            context.getString(
                                R.string.medication_name_in_list,
                                tradeName,
                                productName
                            )
                        } else {
                            productName.ifBlank { tradeName }
                        }
                    }
                }

                else -> {
                    eRxDisplayName.ifBlank {
                        productName
                    }
                }
            }
            when (uiType) {
                ModelUtil.medicationUiTypeOne -> {
                    return if (productName.equals(tradeName, true)) {
                        productName
                    } else {
                        if (tradeName.isNotBlank() && productName.isNotBlank()) {
                            context.getString(
                                R.string.medication_name_in_list,
                                tradeName,
                                productName
                            )
                        } else {
                            productName.ifBlank { tradeName }
                        }
                    }
                }

                else -> {
                    return eRxDisplayName.ifBlank {
                        productName
                    }
                }
            }
        }

        fun getScreenWidth(activity: Activity?): Int {
            activity?.let {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val windowMetrics =
                        activity.windowManager.currentWindowMetrics
                    val insets: Insets = windowMetrics.windowInsets
                        .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                    windowMetrics.bounds.width() - insets.left - insets.right
                } else {
                    val displayMetrics = DisplayMetrics()
                    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                    displayMetrics.widthPixels
                }
            }

            return 0
        }

        fun getScreenHeight(activity: Activity?): Int {
            activity?.let {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val windowMetrics =
                        activity.windowManager.currentWindowMetrics
                    val insets: Insets = windowMetrics.windowInsets
                        .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                    windowMetrics.bounds.height() - insets.top - insets.bottom
                } else {
                    val displayMetrics = DisplayMetrics()
                    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                    displayMetrics.heightPixels
                }
            }

            return 0
        }

        fun DateTime.removeTime(): DateTime {
            return DateTime.parse(
                this.toString(ModelUtil.charmApiDateFormat),
                ModelUtil.charmApiDateFormat()
            )
        }

        fun formatDate(dateString: String?, forPattern: DateTimeFormatter): DateTime? {

            dateString ?: return null

            return try {
                DateTime.parse(dateString, forPattern)
            } catch (e: Exception) {
                null
            }

        }

        fun formatDateWithTimeZone(
            dateString: String?,
            forPattern: DateTimeFormatter,
            timeZoneString: String
        ): DateTime? {
            dateString ?: return null
            return try {
                val timeZone = getTimeZone(timeZoneString)
                DateTime.parse(dateString, forPattern.withZone(timeZone))
            } catch (e: Exception) {
                null
            }

        }

        fun getTimeZone(timeZoneString: String): DateTimeZone? {
            return try {
                DateTimeZone.forID(timeZoneString)
            } catch (ex: Exception) {
                if (TimeZone.getAvailableIDs().contains(timeZoneString)) {
                    DateTimeZone.forTimeZone(TimeZone.getTimeZone(timeZoneString))
                } else {
                    getDateTimeZone(timeZoneString)
                }

            }
        }

        private fun getDateTimeZone(abbreviation: String): DateTimeZone? {
            return when (abbreviation) {
                "IST" -> DateTimeZone.forID("Asia/Kolkata")
                "MIT" -> DateTimeZone.forID("Pacific/Apia")
                "CNT" -> DateTimeZone.forID("Canada/Newfoundland")
                "AGT" -> DateTimeZone.forID("America/Argentina/Buenos_Aires")
                "BET" -> DateTimeZone.forID("Brazil/East")
                "WET", "ECT" -> DateTimeZone.forID("Europe/Lisbon")
                "MET" -> DateTimeZone.forID("Europe/Paris")
                "CAT" -> DateTimeZone.forID("Africa/Harare")
                "EET" -> DateTimeZone.forID("Europe/Athens")
                "EAT" -> DateTimeZone.forID("Africa/Nairobi")
                "NET" -> DateTimeZone.forID("Asia/Yerevan")
                "PLT" -> DateTimeZone.forID("Asia/Karachi")
                "BST" -> DateTimeZone.forID("Asia/Dhaka")
                "VST" -> DateTimeZone.forID("Asia/Vientiane")
                "CTT", "PRC" -> DateTimeZone.forID("Asia/Shanghai")
                "JST" -> DateTimeZone.forID("Asia/Tokyo")
                "ROK" -> DateTimeZone.forID("Asia/Seoul")
                "ACT" -> DateTimeZone.forID("Australia/Adelaide")
                "AET" -> DateTimeZone.forID("Australia/Sydney")
                "SST" -> DateTimeZone.forID("Pacific/Guadalcanal ")
                "NST", "NZ" -> DateTimeZone.forID("Pacific/Auckland")
                "NZ-CHAT" -> DateTimeZone.forID("Pacific/Chatham")
                else -> null
            }
        }

        fun removeViewPaddings(view: View) {
            view.setPadding(0, 0, 0, 0)
            view.invalidate()
        }

        fun View.showView() {
            visibility = View.VISIBLE
        }

        fun View.hideView() {
            visibility = View.GONE
        }

        fun Chip.selectChip(context: Context) {
            this.setChipBackgroundColorResource(R.color.appointmentSelectedTab)
            this.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.appointmentSelectedTabTxtColor
                )
            )
            this.typeface = getFont(context, R.font.open_sans_medium)
        }

        fun Chip.unSelectChip(context: Context) {
            this.setChipBackgroundColorResource(R.color.appointmentUnSelectedTab)
            this.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.appointmentUnSelectedTabTxtColor
                )
            )
            this.typeface = getFont(context, R.font.open_sans)
        }

        fun showRetryAndCancelDialog(
            context: Context,
            @StringRes message: Int,
            @StringRes retryStr: Int = R.string.retry_str,
            isCancelable: Boolean,
            callback: ((isRetry: Boolean) -> Unit)
        ) {
            showRetryAndCancelDialog(
                context,
                context.getString(message),
                retryStr,
                isCancelable,
                callback
            )
        }

        fun showMdAlertDialog(
            context: Context,
            @StringRes message: Int,
            isCancelable: Boolean,
            positiveAction: Int = R.string.yes_str,
            negativeAction: Int = R.string.no_str,
            callback: ((isYes: Boolean) -> Unit),
        ) {

            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(context)
            alertDialog.setMessage(message)

            alertDialog.setPositiveButton(positiveAction) { _, _ ->
                callback.invoke(true)
            }
            alertDialog.setNegativeButton(negativeAction) { _, _ ->
                callback.invoke(false)
            }

            val alert = alertDialog.create()
            alert.setCancelable(isCancelable)
            alert.setCanceledOnTouchOutside(isCancelable)

            alert.show()
        }

        fun showRetryAndCancelDialog(
            context: Context,
            message: String?,
            @StringRes retryStr: Int = R.string.retry_str,
            isCancelable: Boolean,
            callback: ((isRetry: Boolean) -> Unit)
        ) {
            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(context)
            alertDialog.setMessage(message)

            alertDialog.setPositiveButton(retryStr) { _, _ ->
                callback.invoke(true)
            }
            alertDialog.setNegativeButton(R.string.cancel_str) { _, _ ->
                callback.invoke(false)
            }

            val alert = alertDialog.create()

            alert.setCancelable(isCancelable)
            alert.setCanceledOnTouchOutside(isCancelable)


            alert.show()
        }

        fun showRetryDialog(
            context: Context,
            @StringRes message: Int,
            @StringRes retryStr: Int = R.string.retry_str,
            isCancelable: Boolean,
            callback: (() -> Unit)
        ) {
            showRetryDialog(context, context.getString(message), retryStr, isCancelable, callback)
        }

        fun showRetryDialog(
            context: Context,
            message: String?,
            @StringRes retryStr: Int = R.string.retry_str,
            isCancelable: Boolean,
            callback: (() -> Unit)
        ) {
            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(context)
            alertDialog.setMessage(message)

            alertDialog.setPositiveButton(retryStr) { _, _ ->
                callback.invoke()
            }

            val alert = alertDialog.create()

            alert.setCancelable(isCancelable)
            alert.setCanceledOnTouchOutside(isCancelable)


            alert.show()
        }

        //Method to set Mandatory sign * to provided String
        fun String.setMandatorySignToString(): SpannableString {
            val spannableString = SpannableString(this + " *")
            spannableString.setSpan(
                ForegroundColorSpan(Color.RED),
                spannableString.length - 1,
                spannableString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannableString
        }

        fun ConstraintLayout.connectConstraint(
            @LayoutRes fromLayout: Int,
            fromConstraint: Int,
            @LayoutRes toLayout: Int,
            toConstraint: Int
        ) {
            val set = ConstraintSet()
            set.clone(this)
            set.connect(fromLayout, fromConstraint, toLayout, toConstraint)
            set.applyTo(this)
        }

        fun ConstraintLayout.removeConstraint(@LayoutRes layout: Int, constraint: Int) {
            val set = ConstraintSet()
            set.clone(this)
            set.clear(layout, constraint)
            set.applyTo(this)
        }

        fun showInAppRatingDialog(criteriaId: Long? = null, activity: Activity) {

            if (!BuildUtil.isInAppRatingsEnabled) {
                return
            }

            Log.d(TAG, "AppticsInAppRatings trigger -----------")
            var reviewInfo: ReviewInfo? = null
            val reviewManager: ReviewManager = ReviewManagerFactory.create(activity)

            if (criteriaId != null) {
                AppticsUtil.sendStats(
                    criteriaId,
                    AppticsInAppRatings.PopupAction.RATE_IN_STORE_CLICKED,
                    AppticsInAppRatings.PopupSource.DYNAMIC
                )
            }

            val requestTask = reviewManager.requestReviewFlow()
            requestTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "task.isSuccessful ")
                    reviewInfo = task.result
                } else {
                    Log.d(TAG, "task.isSuccessful false ")
                    // There was some problem, continue regardless of the result.
                }

                val flowTask = reviewInfo?.let { reviewManager.launchReviewFlow(activity, it) }
                flowTask?.addOnCompleteListener { _ ->
                    Log.d(TAG, "flowTask.addOnCompleteListener ")
                    // The flow has finished. The API does not indicate whether the user // reviewed or not, or even whether the review dialog was shown. Thus, no // matter the result, we continue our app flow.
                }

            }

        }

        fun updateSelectionInCardViews(
            selectedCardView: CardView,
            selectedImageView: ImageView,
            @DrawableRes selectedDrawableId: Int,
            unselectedCardView: CardView,
            unselectedImageView: ImageView,
            @DrawableRes unselectedDrawableId: Int,
        ) {
            selectedCardView.isSelected = true
            selectedCardView.setBackgroundResource(R.drawable.ic_card_selected_bg)
            selectedImageView.setImageResource(selectedDrawableId)

            unselectedCardView.isSelected = false
            unselectedCardView.setBackgroundResource(R.drawable.ic_card_unselected_bg)
            unselectedImageView.setImageResource(unselectedDrawableId)
        }

        //region InApp Rating
        private fun showMdRatingDialog(activity: Activity, criteriaId: Long?) {
            val screenWidth = getScreenWidth(activity)
            val width = screenWidth - (activity.resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin) * 2)

            val mdRatingDialog = MDInAppRatingDialog(activity, width)
            mdRatingDialog.headingText =
                activity.resources.getString(R.string.do_you_like_harm_mPHR, activity.getString(R.string.app_name))
            mdRatingDialog.subText = activity.resources.getString(R.string.feel_about_str)
            mdRatingDialog.image = R.drawable.ic_in_app_rating_image
            mdRatingDialog.criteriaId = criteriaId
            mdRatingDialog.setRatingDialogListener(object :
                MDInAppRatingDialog.MDInAppRatingDialogListener {
                override fun onYesClick() {
                    mdRatingDialog.dismiss()
                    showRatingDialog(
                        activity,
                        width, criteriaId
                    )
                }

                override fun onNoClick() {
                    mdRatingDialog.dismiss()
                    showMdFeedbackDialog(
                        activity,
                        width,
                        criteriaId
                    )
                }

            })
            mdRatingDialog.show()
        }

        private fun showMdFeedbackDialog(activity: Activity, width: Int, criteriaId: Long?) {
            val mdFeedBackDialog = MDInAppRatingDialog(activity, width)
            mdFeedBackDialog.headingText =
                activity.resources.getString(R.string.can_provide_feedback)
            mdFeedBackDialog.subText = activity.resources.getString(R.string.your_opinion_matters)
            mdFeedBackDialog.image = R.drawable.ic_feedback
            mdFeedBackDialog.criteriaId = criteriaId
            mdFeedBackDialog.setRatingDialogListener(object :
                MDInAppRatingDialog.MDInAppRatingDialogListener {
                override fun onYesClick() {
                    mdFeedBackDialog.dismiss()
                    if (criteriaId != null) {
                        AppticsUtil.sendStats(
                            criteriaId,
                            AppticsInAppRatings.PopupAction.SEND_FEEDBACK_CLICKED,
                            AppticsInAppRatings.PopupSource.DYNAMIC
                        )
                    }
                    intentToContactUs(activity)
                }

                override fun onNoClick() {
                    mdFeedBackDialog.dismiss()
                    if (criteriaId != null) {
                        AppticsUtil.sendStats(
                            criteriaId,
                            AppticsInAppRatings.PopupAction.LATER_CLICKED,
                            AppticsInAppRatings.PopupSource.DYNAMIC
                        )
                    }
                }

            })
            mdFeedBackDialog.show()
        }

        private fun showRatingDialog(activity: Activity, width: Int, criteriaId: Long?) {
            val mdRatingDialog = MDInAppRatingDialog(activity, width)
            mdRatingDialog.headingText = activity.resources.getString(R.string.can_you_rate_our_app)
            mdRatingDialog.subText = activity.resources.getString(R.string.help_us_improve_msg)
            mdRatingDialog.image = R.drawable.ic_rating
            mdRatingDialog.criteriaId = criteriaId
            mdRatingDialog.setRatingDialogListener(object :
                MDInAppRatingDialog.MDInAppRatingDialogListener {
                override fun onYesClick() {
                    mdRatingDialog.dismiss()
                    showInAppRatingDialog(criteriaId, activity)
                }

                override fun onNoClick() {
                    mdRatingDialog.dismiss()
                    if (criteriaId != null) {
                        AppticsUtil.sendStats(
                            criteriaId,
                            AppticsInAppRatings.PopupAction.LATER_CLICKED,
                            AppticsInAppRatings.PopupSource.DYNAMIC
                        )
                    }
                }

            })
            mdRatingDialog.show()
        }

        private fun intentToContactUs(activity: Activity) {
            val intent = Intent(activity, SupportActivity::class.java)
            intent.putExtra(ModelUtil.supportType, ModelUtil.supportSuggest)
            activity.startActivity(intent)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        fun askUserForRating(activity: Activity, criteriaId: Long?) {
            try {
                if (!BuildUtil.isInAppRatingsEnabled) {
                    return
                }
                showMdRatingDialog(
                    activity,
                    criteriaId
                )
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
            }
        }
        //endregion

        fun getFont(context: Context, id: Int): Typeface? {
            try {
                return ResourcesCompat.getFont(context, id)
            } catch (e: Exception) {
                return Typeface.DEFAULT
            }

        }

        fun showSettingsDialog(activity: Activity, permForUI: String) {
            val screenWidth = getScreenWidth(activity)
            val width =
                screenWidth - (activity.resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))

            val mdRequestPermissionDialogDialog = MdRequestPermissionDialog(activity, width)
            mdRequestPermissionDialogDialog.headingText =
                activity.getString(R.string.turn_on_permission, permForUI)
            mdRequestPermissionDialogDialog.positiveBtnText =
                activity.getString(R.string.dashboard_settings_str)
            mdRequestPermissionDialogDialog.negativeBtnText =
                activity.getString(R.string.cancel_str)
            mdRequestPermissionDialogDialog.subHeadingText =
                activity.getString(R.string.guide_for_permission_str) + (permForUI)
            mdRequestPermissionDialogDialog.setRequestPermissionListener(object :
                MdRequestPermissionDialog.MDRequestPermissionListener {
                override fun onYesClick() {
                    mdRequestPermissionDialogDialog.dismiss()
                    MDBaseActivity.isPassCodeAllowed = false   // preventing from asking passcode
                    PermissionUtil.intentToSettings(activity)
                }

                override fun onNoClick() {
                    mdRequestPermissionDialogDialog.dismiss()
                }

            })
            mdRequestPermissionDialogDialog.show()
        }

        fun showEducationalDialog(
            activity: Activity,
            permission: Array<String>,
            @StringRes permissionMsg: Int,
            permForUI: String,
            requestPermissionLauncher: (Array<String>) -> Unit
        ) {
            val screenWidth = getScreenWidth(activity)
            val width =
                screenWidth - (activity.resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))

            val mdRequestPermissionDialogDialog = MdRequestPermissionDialog(activity, width)
            mdRequestPermissionDialogDialog.headingText =
                activity.getString(R.string.allow_access, permForUI)
            mdRequestPermissionDialogDialog.positiveBtnText = activity.getString(R.string.allow_str)
            mdRequestPermissionDialogDialog.negativeBtnText =
                activity.getString(R.string.cancel_str)
            mdRequestPermissionDialogDialog.subHeadingText = activity.getString(permissionMsg)
            mdRequestPermissionDialogDialog.setRequestPermissionListener(object :
                MdRequestPermissionDialog.MDRequestPermissionListener {
                override fun onYesClick() {
                    mdRequestPermissionDialogDialog.dismiss()
                    requestPermissionLauncher(permission)
                }

                override fun onNoClick() {
                    mdRequestPermissionDialogDialog.dismiss()
                }

            })
            mdRequestPermissionDialogDialog.show()
        }

        fun showAlarmAndReminderDialog(
            activity: Activity,
        ) {
            val screenWidth = getScreenWidth(activity)
            val width =
                screenWidth - (activity.resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))

            val mdRequestPermissionDialogDialog = MdRequestPermissionDialog(activity, width)
            mdRequestPermissionDialogDialog.headingText = activity.getString(
                R.string.allow_access,
                activity.getString(R.string.alarm_reminders_str)
            )
            mdRequestPermissionDialogDialog.positiveBtnText = activity.getString(R.string.allow_str)
            mdRequestPermissionDialogDialog.negativeBtnText =
                activity.getString(R.string.cancel_str)
            mdRequestPermissionDialogDialog.subHeadingText =
                activity.getString(R.string.allow_notification)
            mdRequestPermissionDialogDialog.setRequestPermissionListener(object :
                MdRequestPermissionDialog.MDRequestPermissionListener {
                override fun onYesClick() {
                    mdRequestPermissionDialogDialog.dismiss()
                    PermissionUtil.requestScheduleExactAlarms(activity)
                }

                override fun onNoClick() {
                    mdRequestPermissionDialogDialog.dismiss()
                }

            })
            mdRequestPermissionDialogDialog.show()
        }

        fun openUrlInChromeInAppStack(context: Context, url: String) {
            try {
                val chromePackageName = "com.android.chrome"
                if (isPackageInstalled(context, chromePackageName)) {
                    // Use Chrome Custom Tabs
                    val builder = CustomTabsIntent.Builder()
                    val customTabsIntent = builder.build()
                    customTabsIntent.intent.setPackage(chromePackageName)
                    customTabsIntent.launchUrl(context, Uri.parse(url))
                } else {
                    // Fallback to any browser if Chrome is not available
                    openUrlInInstalledBrowser(context, url)
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error opening Chrome Custom Tab", e)
            }
        }

        fun isPackageInstalled(context: Context, packageName: String): Boolean {
            val packageManager = context.packageManager
            return try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: Exception) {
                false
            }
        }

        fun openUrlInInstalledBrowser(context: Context, url: String) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Handle the case where no browser is installed
                //No browser is present in the device please istall and try
                e.printStackTrace()
            }
        }

        fun applyViewInsertForToolbar(view: View) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemBars()
                )

                val typedArray = view.context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
                val actionBarSize = typedArray.getDimensionPixelSize(0, 0)
                typedArray.recycle()

                view.layoutParams?.height = actionBarSize + bars.top
                view.setPadding(view.paddingLeft, bars.top, view.paddingRight, view.paddingBottom)
                insets
            }
            ViewCompat.requestApplyInsets(view)

        }

        fun applyViewInsertForBottomView(view: View) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemBars()
                )
                view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight,bars.bottom)
                insets
            }

            ViewCompat.requestApplyInsets(view)

        }

        fun applyViewInsertForFullView(view: View) {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.systemBars()
                )
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
                insets
            }
            ViewCompat.requestApplyInsets(view)
        }

        fun buildLabParamGraphData(
            context: Context,
            entries: ArrayList<ParamHistoryEntryPOJO>,
            paramName: String,
            unit: String? = null
        ): GraphData {
            entries.sortBy { it.date?.millis }
            val graphData = GraphData()
            val nameList = arrayListOf(paramName)
            val unitList = if (!unit.isNullOrBlank()) arrayListOf(unit) else arrayListOf()
            var xAxisNum = 2f
            val entryList = arrayListOf<Entry>()
            val lineDataList = arrayListOf<ILineDataSet>()
            entries.forEach {
                if (it.value != null) {
                    entryList.add(MDEntry(xAxisNum, it.value?.toFloatOrNull() ?: 0f, 0))
                }
                xAxisNum += 4f
            }
            val lineDataSet = LineDataSet(entryList, paramName)
            lineDataSet.lineWidth = 2.5f
            lineDataSet.circleRadius = 4f
            lineDataSet.color = ContextCompat.getColor(context, R.color.firstAxisColor)
            lineDataSet.setCircleColor(ContextCompat.getColor(context, R.color.firstAxisColor))
            lineDataSet.setDrawValues(false)
            lineDataList.add(lineDataSet)
            graphData.dataList = lineDataList
            graphData.dateList = ArrayList(entries.mapNotNull { it.date })
            graphData.unitTxt = unitList
            graphData.lineNameList = nameList
            return graphData
        }

        fun drawLabParamLineChart(
            context: Context,
            chart: LineChart,
            graphData: GraphData
        ) {
            chart.fitScreen()
            chart.clear()
            val typefaceMedium = getFont(context, R.font.open_sans_medium)
            val labelList = ArrayList(graphData.dateList.map {
                it.toString(ModelUtil.charmDisplayDateFormat())
            })
            val xAxisFormatter = MDAxisStringFormatter()
            xAxisFormatter.labelList = labelList

            val xAxis = chart.xAxis
            xAxis.valueFormatter = xAxisFormatter
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(false)
            val xSpan = ((graphData.dateList.size * 4) + 2).toFloat()
            xAxis.granularity = 2f
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = xSpan
            xAxis.textColor = ContextCompat.getColor(context, R.color.graphXAxisTxtColor)
            xAxis.textSize = 13f
            xAxis.typeface = typefaceMedium

            val yAxis = chart.axisLeft
            yAxis.setDrawAxisLine(false)
            yAxis.setDrawGridLines(true)
            val graphMinMaxValue = ModelUtil.getGraphMinMaxValue(graphData)
            yAxis.axisMaximum = graphMinMaxValue.second
            yAxis.axisMinimum = graphMinMaxValue.first
            yAxis.gridColor = ContextCompat.getColor(context, R.color.graphGridColor)
            yAxis.enableGridDashedLine(15f, 15f, 0f)
            yAxis.textColor = ContextCompat.getColor(context, R.color.black87)
            yAxis.textSize = 13f
            yAxis.typeface = typefaceMedium

            chart.axisRight.isEnabled = false

            val l = chart.legend
            l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            l.orientation = Legend.LegendOrientation.HORIZONTAL
            l.typeface = typefaceMedium
            l.textColor = ContextCompat.getColor(context, R.color.black87)
            l.textSize = 13f
            l.setDrawInside(false)
            l.isEnabled = graphData.dataList.size > 1

            val data = LineData(graphData.dataList)
            chart.data = data
            chart.description.isEnabled = false
            chart.setScaleEnabled(false)
            chart.isHighlightPerTapEnabled = true

            val graphMarker = GraphMarkerView(context, R.layout.item_chart_select_view)
            graphMarker.unitTxt = graphData.unitTxt
            graphMarker.labelList = labelList
            graphMarker.chartView = chart
            chart.marker = graphMarker

            chart.moveViewToX(data.xMax)
            chart.zoom((graphData.dateList.size.toFloat() / 3), 0f, 0f, 0f)
            chart.extraBottomOffset = 10f
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    }

}

class BuildUtil {

    companion object {

        const val RELEASE = "release"
        const val DEBUG = "debug"

        val isCustomApp: Boolean = BuildConfig.IS_CUSTOM_APP
        val isThalassemia: Boolean = BuildConfig.IS_THALASSEMIA
        val isVideoCallEnabled: Boolean = BuildConfig.IS_VIDEO_CALL_ENABLED
        val isPushNotificationEnabled: Boolean = BuildConfig.IS_PUSH_NOTIFICATION_ENABLED
        val isInAppRatingsEnabled: Boolean = BuildConfig.IS_IN_APP_RATINGS_ENABLED
        var practiceId: String = ""

        val isOAuthEnabled: Boolean = BuildConfig.IS_OAUTH_ENABLED

    }
}

