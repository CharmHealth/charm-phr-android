package com.charm.phr.mphr.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import com.charm.phr.mphr.R
import com.google.android.material.textfield.TextInputEditText


class MDPinEntryEditText constructor(context: Context, attributeSet: AttributeSet?) :
        TextInputEditText(context, attributeSet) {


    constructor(context: Context) : this(context, null)

    private var mSpace = 24f //24 dp by default, space between the lines
    private var mCharSize = 0f
    private var mNumChars = 4f
    private var mLineSpacing = 8f //8dp by default, height of the text from our lines
    private var mMaxLength = 4
    private var mClickListener: OnClickListener? = null
    private var mLineStroke = 1f //1dp by default
    private var mLineStrokeSelected = 2f //2dp by default
    private var mLinesPaint: Paint? = null

    private var hideInput = false

    private val astrix = "*"

    private var mIsError = false

    val mStates = arrayOf(
            intArrayOf(android.R.attr.state_selected),
            intArrayOf(android.R.attr.state_focused),
            intArrayOf(-android.R.attr.state_focused)
    )
    val mColors = intArrayOf(
            Color.GREEN,
            ContextCompat.getColor(context, R.color.colorPrimary),
            ContextCompat.getColor(context, R.color.pinColor)
    )
    var mColorStates = ColorStateList(mStates, mColors)


    init {
        init(context, attributeSet)
    }

    private fun init(
            context: Context,
            attrs: AttributeSet?
    ) {
        val multi = context.resources.displayMetrics.density
        mLineStroke = multi * mLineStroke
        mLineStrokeSelected = multi * mLineStrokeSelected
        mLinesPaint = Paint(paint)
        mLinesPaint!!.strokeWidth = mLineStroke
        if (!isInEditMode) {
            val outValue = TypedValue()
            context.theme.resolveAttribute(
                    R.attr.colorControlActivated,
                    outValue, true
            )
            val colorActivated = outValue.data
            mColors[0] = colorActivated
            context.theme.resolveAttribute(
                    R.attr.colorPrimaryDark,
                    outValue, true
            )
            val colorDark = outValue.data
            mColors[1] = colorDark
            context.theme.resolveAttribute(
                    R.attr.colorControlHighlight,
                    outValue, true
            )
            val colorHighlight = outValue.data
            mColors[2] = colorHighlight
        }
        setBackgroundResource(0)
        mSpace = multi * mSpace //convert to pixels for our density
        mLineSpacing = multi * mLineSpacing //convert to pixels for our density
        mMaxLength = attrs?.getAttributeIntValue(
                XML_NAMESPACE_ANDROID,
                "maxLength",
                4
        ) ?: 4
        mNumChars = mMaxLength.toFloat()

        //Disable copy paste
        super.setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onPrepareActionMode(
                    mode: ActionMode,
                    menu: Menu
            ): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}
            override fun onCreateActionMode(
                    mode: ActionMode,
                    menu: Menu
            ): Boolean {
                return false
            }

            override fun onActionItemClicked(
                    mode: ActionMode,
                    item: MenuItem
            ): Boolean {
                return false
            }
        })
        // When tapped, move cursor to end of text.
        super.setOnClickListener { v ->
            setSelection(text?.length ?: 0)
            if (mClickListener != null) {
                mClickListener!!.onClick(v)
            }
        }

        inputType = InputType.TYPE_CLASS_NUMBER
        updateFilter()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        mClickListener = l
    }

    override fun setCustomSelectionActionModeCallback(actionModeCallback: ActionMode.Callback?) {
        throw RuntimeException("setCustomSelectionActionModeCallback() not supported.")
    }

    override fun onDraw(canvas: Canvas) {
        //super.onDraw(canvas);
        try {


            val availableWidth = width - paddingRight - paddingLeft
            mCharSize = if (mSpace < 0) {
                availableWidth / (mNumChars * 2 - 1)
            } else {
                (availableWidth - mSpace * (mNumChars - 1)) / mNumChars
            }
            var startX = paddingLeft
            val bottom = height - paddingBottom

            //Text Width
            val text = text
            val textLength = text?.length ?: 0
            val textWidths = FloatArray(textLength)
            paint.getTextWidths(getText(), 0, textLength, textWidths)

            if (!getText()?.toString().isNullOrBlank()){
                mIsError = false
            }
            var i = 0
            while (i < mNumChars) {
                Log.d("MDPinEntry", "loop enters ; $mNumChars - $i ")
                updateColorForLines(i == textLength)
                canvas.drawLine(
                        startX.toFloat(),
                        bottom.toFloat(),
                        startX + mCharSize,
                        bottom.toFloat(),
                        mLinesPaint!!
                )
                if (getText()?.length ?: 0 > i) {
                    val middle = startX + mCharSize / 2

                    Log.d("MDPinEntry", "i -> $i ; ${i + 1} ; length -> ${getText()?.length ?: 0} ")
                    canvas.drawText(
                            if (hideInput) {
                                astrix
                            } else {
                                text.toString()
                            },

                            if (hideInput) {
                                0
                            } else {
                                i
                            },

                            if (hideInput) {
                                1
                            } else {
                                i + 1
                            },
                            middle - textWidths[0] / 2,
                            bottom - mLineSpacing,
                            paint
                    )
                }
                if (mSpace < 0) {
                    startX = (startX + (mCharSize * 2)).toInt()
                } else {
                    startX = (startX + (mCharSize + mSpace.toInt())).toInt()
                }
                i++
            }
            Log.d("MDPinEntry", "loop breaks")
        } catch (e: Exception) {
            Log.d("MDPinEntry", "error -> ${e.cause}")
        }
    }

    private fun getColorForState(vararg states: Int): Int {
        return mColorStates.getColorForState(
                states,
                ContextCompat.getColor(context, R.color.colorPrimary)
        )
    }

    /**
     * @param next Is the current char the next character to be input?
     */
    private fun updateColorForLines(next: Boolean) {
        if (isFocused) {
            mLinesPaint!!.strokeWidth = mLineStrokeSelected
            if (mIsError){
                mLinesPaint!!.color = ContextCompat.getColor(context, R.color.pinColorError)
            }
            else {
                mLinesPaint!!.color = ContextCompat.getColor(context, R.color.pinColor)
                if (next) {
                    mLinesPaint!!.color = ContextCompat.getColor(context, R.color.pinColorSelected)
                }
            }
        } else {
            mLinesPaint!!.strokeWidth = mLineStroke
            if (mIsError){
                mLinesPaint!!.color = ContextCompat.getColor(context, R.color.pinColorError)
            }
            else {
                mLinesPaint!!.color = ContextCompat.getColor(context, R.color.pinColor)
            }
        }
    }

    private fun updateFilter() {

        val arrayList = filters.toCollection(ArrayList())

        val filteredList = arrayList.filter {
            it is AlphabetNumberInputFilter || it is AlphabetNumberAndSplCharInputFilter || it is NumberInputFilter || it is AlphabetInputFilter || it is AddressInputFilter
        }

        if (filteredList.isNotEmpty()) {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(beforeTxtEditable: Editable) {
                }

                override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })
        }

        arrayList.removeAll(filteredList)
        arrayList.add(NumberInputFilter())

        filters = arrayList.toTypedArray()
    }

    fun setError(isError: Boolean){
        mIsError = isError
        invalidate()
    }

    fun getIsError(): Boolean = mIsError

    companion object {
        const val XML_NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android"
    }
}