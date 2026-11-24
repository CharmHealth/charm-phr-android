package com.charm.phr.mphr.ui.components.scaleView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.charm.phr.mphr.R
import com.charm.phr.mphr.util.UIUtil

class ScaleValuePicker : CardView, ObservableHorizontalScrollView.ScrollChangedListener {
    /**
     * Left side empty view to add padding to the ruler.
     */
    private lateinit var mLeftSpacer: View

    /**
     * Right side empty view to add padding to the ruler.
     */
    private lateinit var mRightSpacer: View

    /**
     * Ruler view with values.
     */
    private lateinit var mScaleView: ScaleView

    /**
     * Left side empty view to add padding to the ruler.
     */
    private lateinit var mSelectedValueTV: TextView


    private lateinit var labelView: TextView

    /**
     * Right side empty view to add padding to the ruler.
     */
    private lateinit var mSelectedValueHintTV: TextView


    private var mUnitTxt: String = ""
        set(value) {
            mSelectedValueHintTV.text = value
            field = value
        }
    /**
     * [ObservableHorizontalScrollView], that will host all three components.
     *
     * @see .mLeftSpacer
     *
     * @see .mRightSpacer
     *
     * @see .mScaleView
     */
    private lateinit var mHorizontalScrollView: ObservableHorizontalScrollView

    @Nullable
    private var mListener: ScaleValuePickerListener? = null
    private var mNotchPaint: Paint? = null
    private var mNotchPath: Path? = null
    private var mNotchColor: Int = Color.WHITE

    var label: String? = null
    set(value) {
        labelView.text = value
        field = value
    }

    /**
     * Public constructor.
     */
    constructor(context: Context) : super(context) {
        init(null)
    }

    /**
     * Public constructor.
     */
    constructor(context: Context,
                @Nullable attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    /**
     * Public constructor.
     */
    constructor(context: Context,
                @Nullable attrs: AttributeSet?,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

 /*   *//**
     * Public constructor.
     *//*
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context,
                @Nullable attrs: AttributeSet?,
                defStyleAttr: Int,
                defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }*/

    /**
     * Initialize the view and parse the [AttributeSet].
     *
     * @param attributeSet [AttributeSet] to parse or null if no attribute parameters set.
     */
    private fun init(@Nullable attributeSet: AttributeSet?) {
        //Add all the children
        addChildViews()
        if (attributeSet != null) {
            val a = context.theme.obtainStyledAttributes(attributeSet,
                    R.styleable.ScaleValuePicker,
                    0,
                    0)
            try { //Parse params
                if (a.hasValue(R.styleable.ScaleValuePicker_notch_color)) {
                    mNotchColor = a.getColor(R.styleable.ScaleValuePicker_notch_color, Color.WHITE)
                }
                if (a.hasValue(R.styleable.ScaleValuePicker_ruler_text_color)) {
                    textColor = a.getColor(R.styleable.ScaleValuePicker_ruler_text_color, Color.WHITE)
                }
                if (a.hasValue(R.styleable.ScaleValuePicker_ruler_text_size)) {
                    setTextSize(a.getDimension(R.styleable.ScaleValuePicker_ruler_text_size, 14f).toInt())
                }
                if (a.hasValue(R.styleable.ScaleValuePicker_indicator_color)) {
                    indicatorColor = a.getColor(R.styleable.ScaleValuePicker_indicator_color, Color.WHITE)
                }


                if (a.hasValue(R.styleable.ScaleValuePicker_indicator_select_color)){
                    selectedIndicatorColor = a.getColor(R.styleable.ScaleValuePicker_indicator_select_color , Color.WHITE)
                }

                if (a.hasValue(R.styleable.ScaleValuePicker_unit_text)){
                    mUnitTxt = a.getString(R.styleable.ScaleValuePicker_unit_text) ?: ""
                }

                if (a.hasValue(R.styleable.ScaleValuePicker_label)){
                    label = a.getString(R.styleable.ScaleValuePicker_label) ?: ""
                }

                if (a.hasValue(R.styleable.ScaleValuePicker_indicator_width)) {
                    setIndicatorWidth(a.getDimensionPixelSize(R.styleable.ScaleValuePicker_indicator_width,
                            4))
                }
                if (a.hasValue(R.styleable.ScaleValuePicker_indicator_interval)) {
                    setIndicatorIntervalDistance(a.getDimensionPixelSize(R.styleable.ScaleValuePicker_indicator_interval,
                            4))
                }
                if (a.hasValue(R.styleable.ScaleValuePicker_long_height_height_ratio)
                        || a.hasValue(R.styleable.ScaleValuePicker_short_height_height_ratio)) {
                    setIndicatorHeight(a.getFraction(R.styleable.ScaleValuePicker_long_height_height_ratio,
                            1, 1, 0.6f),
                            a.getFraction(R.styleable.ScaleValuePicker_short_height_height_ratio,
                                    1, 1, 0.4f))
                }
                if (a.hasValue(R.styleable.ScaleValuePicker_min_value) ||
                        a.hasValue(R.styleable.ScaleValuePicker_max_value)) {
                    setMinMaxValue(a.getInteger(R.styleable.ScaleValuePicker_min_value, 0),
                            a.getInteger(R.styleable.ScaleValuePicker_max_value, 100))
                }
            } finally {
                a.recycle()
            }
        }
        //Prepare the notch color.
        mNotchPaint = Paint()
        prepareNotchPaint()
        mNotchPath = Path()
    }



    /**
     * Create the paint for notch. This will
     */
    private fun prepareNotchPaint() {
        mNotchPaint?.setColor(mNotchColor)
        mNotchPaint?.setStrokeWidth(5f)
        mNotchPaint?.setStyle(Paint.Style.FILL_AND_STROKE)
    }

    /**
     * Programmatically add the children to the view.
     *
     *
     *  * The main view contains the [android.widget.HorizontalScrollView]. That allows
     * [ScaleView] to scroll horizontally.
     *  * [.mHorizontalScrollView] contains [LinearLayout] that will act as the container
     * to hold the children inside the horizontal view.
     *  * [LinearLayout] container will contain three children.
     * **Left spacer:** Width of this view will be the half width of the view. This will add staring at the start of the ruler.
     * **Right spacer:** Width of this view will be the half width of the view. This will add ending at the end of the ruler.
     * **[ScaleView]:** Ruler view will contain the ruler with indicator.
     *
     */
    private fun addChildViews() {


        /*val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val margin = 2
        params.setMargins(margin, margin, margin, margin)
        setLayoutParams(params)
        setCardBackgroundColor(Color.WHITE)
        radius = resources.getDimension(R.dimen.common_card_radius)
        elevation = resources.getDimension(R.dimen.common_card_elevation)*/

        val mainParentLayout = LinearLayout(context)
        mainParentLayout.orientation = LinearLayout.VERTICAL

        mainParentLayout.gravity = Gravity.CENTER_HORIZONTAL


        val labelLayout = LinearLayout(context)
        labelLayout.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        labelLayout.orientation = LinearLayout.HORIZONTAL



        val cardPadding =  resources.getDimensionPixelSize(R.dimen.card_padding_half)
        val labelPadding =  resources.getDimensionPixelSize(R.dimen.edittext_padding)


         labelView = TextView(context)

        val labelViewParams =  LinearLayout.LayoutParams(0 , WRAP_CONTENT)
        labelViewParams.weight = 1f
        labelViewParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.edittext_padding)
        labelView.layoutParams = labelViewParams
        labelView.setTextColor(ContextCompat.getColor(context , R.color.black87))
        labelView.typeface = UIUtil.getFont(context , R.font.open_sans_medium)
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.medium))
        labelView.text = label
        labelView.setPaddingRelative(labelPadding,0,labelPadding,0)

        val  deleteView = TextView(context)

        val deleteViewParams =  LinearLayout.LayoutParams(WRAP_CONTENT , WRAP_CONTENT)
        deleteViewParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.edittext_padding)
        deleteView.layoutParams = deleteViewParams
        deleteView.setTextColor(ContextCompat.getColor(context , R.color.darkRed))
        deleteView.isAllCaps = true
        deleteView.typeface = UIUtil.getFont(context , R.font.open_sans_medium)
        deleteView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.medium))
        deleteView.setText(R.string.delete_str)
        deleteView.visibility = View.GONE
        deleteView.gravity = Gravity.END
        deleteView.setPaddingRelative(0,labelPadding,labelPadding,0)

        labelLayout.addView(labelView)
        labelLayout.addView(deleteView)
        mainParentLayout.addView(labelLayout)

        val chatBubbleLayout = LinearLayout(context)
        val chatBubbleParam = LinearLayout.LayoutParams(WRAP_CONTENT , WRAP_CONTENT)
        chatBubbleParam.marginStart = cardPadding
        chatBubbleLayout.layoutParams = chatBubbleParam
        chatBubbleLayout.gravity = Gravity.CENTER_HORIZONTAL
        chatBubbleLayout.orientation = LinearLayout.HORIZONTAL
        chatBubbleLayout.setPadding(cardPadding ,cardPadding ,cardPadding ,cardPadding)
        chatBubbleLayout.setBackgroundResource(R.drawable.ic_chat_bubble_primary)

        mSelectedValueTV = TextView(context)
        mSelectedValueTV.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT , WRAP_CONTENT)
        mSelectedValueTV.setTextColor(ContextCompat.getColor(context , android.R.color.white))
        mSelectedValueTV.typeface = UIUtil.getFont(context , R.font.open_sans_medium)
        mSelectedValueTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.scaleview_picker_value_txt_size))
        mSelectedValueTV.setPaddingRelative(0,0,cardPadding,0)

        chatBubbleLayout.addView(mSelectedValueTV)

        mSelectedValueHintTV = TextView(context)
        mSelectedValueHintTV.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT , WRAP_CONTENT)
        mSelectedValueHintTV.setTextColor(ContextCompat.getColor(context , android.R.color.white))
        mSelectedValueHintTV.typeface = UIUtil.getFont(context , R.font.open_sans)
        mSelectedValueHintTV.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.small))
        mSelectedValueHintTV.text = mUnitTxt

        chatBubbleLayout.addView(mSelectedValueHintTV)


        mainParentLayout.addView(chatBubbleLayout)

        mHorizontalScrollView = ObservableHorizontalScrollView(context, this)

        val hScrollViewParam  = LinearLayout.LayoutParams(MATCH_PARENT , WRAP_CONTENT)
        hScrollViewParam.marginStart = cardPadding
        hScrollViewParam.marginEnd = cardPadding
        mHorizontalScrollView.layoutParams = hScrollViewParam
        mHorizontalScrollView.isHorizontalScrollBarEnabled = false //Don't display the scrollbar
        val rulerContainer = LinearLayout(context)
        //Add left spacing to the container
        mLeftSpacer = View(context)
        rulerContainer.addView(mLeftSpacer)
        //Add ruler to the container
        mScaleView = ScaleView(context)
        rulerContainer.addView(mScaleView)
        //Add right spacing to the container
        mRightSpacer = View(context)
        rulerContainer.addView(mRightSpacer)
        //Add this container to the scroll view.
        mHorizontalScrollView.removeAllViews()
        mHorizontalScrollView.addView(rulerContainer)
        //Add scroll view to this view.
        removeAllViews()

        mainParentLayout.addView(mHorizontalScrollView)

        addView(mainParentLayout)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //Draw the top notch
        canvas.drawPath(mNotchPath!!, mNotchPaint!!)
    }

    override fun onLayout(isChanged: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(isChanged, left, top, right, bottom)
        if (isChanged) {
            val width = width
            //Set width of the left spacer to the half of this view.
            val leftParams: ViewGroup.LayoutParams? = mLeftSpacer?.getLayoutParams()
            leftParams?.width = width / 2
            mLeftSpacer?.layoutParams = leftParams
            //Set width of the right spacer to the half of this view.
            val rightParams: ViewGroup.LayoutParams? = mRightSpacer?.getLayoutParams()
            rightParams?.width = width / 2
            mRightSpacer?.layoutParams = rightParams
            calculateNotchPath()
            invalidate()
        }
    }

    /**
     * Calculate notch path. Notch will be in the triangle shape at the top-center of this view.
     *
     * @see .mNotchPath
     */
    private fun calculateNotchPath() {
        mNotchPath?.reset()
        mNotchPath?.moveTo((width / 2 - 30).toFloat(), 0f)
        mNotchPath?.lineTo((width / 2).toFloat(), 40f)
        mNotchPath?.lineTo((width / 2 + 30).toFloat(), 0f)
    }

    fun setUnitText(text: String){
        mUnitTxt = text
        mSelectedValueHintTV.text = text
    }

    /**
     * Scroll the ruler to the given value.
     *
     * @param value Value to select. Value must be between [.getMinValue] and [.getMaxValue].
     * If the value is less than [.getMinValue], [.getMinValue] will be
     * selected.If the value is greater than [.getMaxValue], [.getMaxValue]
     * will be selected.
     */
    fun selectValue(value: Int) {
        mHorizontalScrollView.postDelayed(Runnable {
            val valuesToScroll: Int
            if (value < mScaleView.minValue ) {
                valuesToScroll = 0
            } else if (value > mScaleView.maxValue ?: 0) {
                valuesToScroll = mScaleView.maxValue - mScaleView.minValue
            } else {
                valuesToScroll = value - mScaleView.minValue
            }
            mHorizontalScrollView.smoothScrollTo(
                    valuesToScroll * mScaleView.indicatorIntervalWidth, 0)
        }, 400)
    }

    /**
     * @return Get the current selected value.
     */
    val currentValue: Int
        get() {
            val absoluteValue: Int = mHorizontalScrollView.getScrollX() / mScaleView.indicatorIntervalWidth
            val value: Int = mScaleView.minValue + absoluteValue
            return if (value > mScaleView.maxValue) {
                mScaleView.maxValue
            } else if (value < mScaleView.minValue) {
                mScaleView.minValue
            } else {
                value
            }
        }

    override fun onScrollChanged() {
        mSelectedValueTV.text = "$currentValue"
        mListener?.onIntermediateValueChange(currentValue)
    }

    override fun onScrollStopped() {
        makeOffsetCorrection(mScaleView.indicatorIntervalWidth)

        mSelectedValueTV.text = "$currentValue"
        mListener?.onValueChange(currentValue)
    }

    private fun makeOffsetCorrection(indicatorInterval: Int) {
        val offsetValue: Int = mHorizontalScrollView.getScrollX() % indicatorInterval
        if (offsetValue < indicatorInterval / 2) {
            mHorizontalScrollView.scrollBy(-offsetValue, 0)
        } else {
            mHorizontalScrollView.scrollBy(indicatorInterval - offsetValue, 0)
        }
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.value = currentValue
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        selectValue(ss.value)
    }
    //**********************************************************************************//
    //******************************** GETTERS/SETTERS *********************************//
    //**********************************************************************************//
    /**
     * @param notchColorRes Color resource of the notch to display. Default color os [Color.WHITE].
     * @see .setNotchColor
     * @see .getNotchColor
     */
    fun setNotchColorRes(@ColorRes notchColorRes: Int) {
        notchColor = ContextCompat.getColor(context, notchColorRes)
    }

    /**
     * @return Integer color of the notch. Default color os [Color.WHITE].
     * @see .setNotchColor
     * @see .setNotchColorRes
     */
    /**
     * @param notchColor Integer color of the notch to display. Default color os [Color.WHITE].
     * @see .prepareNotchPaint
     * @see .getNotchColor
     */
    @get:ColorInt
    var notchColor: Int
        get() = mNotchColor
        set(notchColor) {
            mNotchColor = notchColor
            prepareNotchPaint()
            invalidate()
        }

    /**
     * @return Color integer value of the ruler text color.
     * @see .setTextColor
     * @see .setTextColorRes
     */
    /**
     * Set the color of the text to display on the ruler.
     *
     * @param color Color integer value.
     * @see .getTextColor
     * @see ScaleView.mTextColor
     */
    @get:ColorInt
    @get:CheckResult
    var textColor: Int
        get() = mScaleView.textColor
        set(color) {
            mScaleView.textColor = color
        }

    /**
     * Set the color of the text to display on the ruler.
     *
     * @param color Color resource id.
     * @see ScaleView.mTextColor
     */
    fun setTextColorRes(@ColorRes color: Int) {
        textColor = ContextCompat.getColor(context, color)
    }

    /**
     * @return Size of the text of ruler in dp.
     * @see .setTextSize
     * @see .setTextSizeRes
     * @see ScaleView.mTextColor
     */
    @get:CheckResult
    val textSize: Float
        get() = mScaleView.textSize

    /**
     * Set the size of the text to display on the ruler.
     *
     * @param dimensionDp Text size dimension in dp.
     * @see .getTextSize
     * @see ScaleView.mTextSize
     */
    fun setTextSize(dimensionDp: Int) {
        mScaleView.setTextSize(dimensionDp)
    }

    /**
     * Set the size of the text to display on the ruler.
     *
     * @param dimension Text size dimension resource.
     * @see .getTextSize
     * @see ScaleView.mTextSize
     */
    fun setTextSizeRes(@DimenRes dimension: Int) {
        setTextSize(context.resources.getDimension(dimension).toInt())
    }


    @get:ColorInt
    @get:CheckResult
    var selectedIndicatorColor: Int
        get() = mScaleView.selectedTextColor
        set(color) {
            mScaleView.selectedTextColor = color
        }
    /**
     * @return Color integer value of the indicator color.
     * @see .setIndicatorColor
     * @see .setIndicatorColorRes
     * @see ScaleView.mIndicatorColor
     */
    /**
     * Set the indicator color.
     *
     * @param color Color integer value.
     * @see .getIndicatorColor
     * @see ScaleView.mIndicatorColor
     */
    @get:ColorInt
    @get:CheckResult
    var indicatorColor: Int
        get() = mScaleView.indicatorColor
        set(color) {
            mScaleView.indicatorColor = color
        }

    /**
     * Set the indicator color.
     *
     * @param color Color resource id.
     * @see .getIndicatorColor
     * @see ScaleView.mIndicatorColor
     */
    fun setIndicatorColorRes(@ColorRes color: Int) {
        indicatorColor = ContextCompat.getColor(context, color)
    }

    /**
     * @return Width of the indicator in pixels.
     * @see .setIndicatorWidth
     * @see .setIndicatorWidthRes
     * @see ScaleView.mIndicatorWidthPx
     */
    @get:CheckResult
    val indicatorWidth: Float
        get() = mScaleView.indicatorWidth

    /**
     * Set the width of the indicator line in the ruler.
     *
     * @param widthPx Width in pixels.
     * @see .getIndicatorWidth
     * @see ScaleView.mIndicatorWidthPx
     */
    fun setIndicatorWidth(widthPx: Int) {
        mScaleView.setIndicatorWidth(widthPx)
    }

    /**
     * Set the width of the indicator line in the ruler.
     *
     * @param width Dimension resource for indicator width.
     * @see .getIndicatorWidth
     * @see ScaleView.mIndicatorWidthPx
     */
    fun setIndicatorWidthRes(@DimenRes width: Int) {
        setIndicatorWidth(context.resources.getDimensionPixelSize(width))
    }

    /**
     * @return Get the minimum value displayed on the ruler.
     * @see .setMinMaxValue
     * @see ScaleView.mMinValue
     */
    @get:CheckResult
    val minValue: Int
        get() = mScaleView.minValue

    /**
     * @return Get the maximum value displayed on the ruler.
     * @see .setMinMaxValue
     * @see ScaleView.mMaxValue
     */
    @get:CheckResult
    val maxValue: Int
        get() = mScaleView.maxValue

    /**
     * Set the maximum value to display on the ruler. This will decide the range of values and number
     * of indicators that ruler will draw.
     *
     * @param minValue Value to display at the left end of the ruler. This can be positive, negative
     * or zero. Default minimum value is 0.
     * @param maxValue Value to display at the right end of the ruler. This can be positive, negative
     * or zero.This value must be greater than min value. Default minimum value is 100.
     * @see .getMinValue
     * @see .getMaxValue
     */
    fun setMinMaxValue(minValue: Int, maxValue: Int) {
        mScaleView.setValueRange(minValue, maxValue)
        invalidate()
        selectValue(minValue)
    }

    /**
     * @return Get distance between two indicator in pixels.
     * @see .setIndicatorIntervalDistance
     * @see ScaleView.mIndicatorInterval
     */
    @get:CheckResult
    val indicatorIntervalWidth: Int
        get() = mScaleView.indicatorIntervalWidth

    /**
     * Set the spacing between two vertical lines/indicators. Default value is 14 pixels.
     *
     * @param indicatorIntervalPx Distance in pixels. This cannot be negative number or zero.
     * @see ScaleView.mIndicatorInterval
     */
    fun setIndicatorIntervalDistance(indicatorIntervalPx: Int) {
        mScaleView.setIndicatorIntervalDistance(indicatorIntervalPx)
    }

    fun setIndicatorIntervalDistanceRes(@DimenRes indicatorIntervalPx: Int) {
        setIndicatorIntervalDistance(context.resources.getDimensionPixelSize(indicatorIntervalPx))
    }

    /**
     * @return Ratio of long indicator height to the ruler height.
     * @see .setIndicatorHeight
     * @see ScaleView.mLongIndicatorHeightRatio
     */
    @get:CheckResult
    val longIndicatorHeightRatio: Float
        get() = mScaleView.longIndicatorHeightRatio

    /**
     * @return Ratio of short indicator height to the ruler height.
     * @see .setIndicatorHeight
     * @see ScaleView.mShortIndicatorHeight
     */
    @get:CheckResult
    val shortIndicatorHeightRatio: Float
        get() = mScaleView.shortIndicatorHeightRatio

    /**
     * Set the height of the long and short indicators.
     *
     * @param longHeightRatio  Ratio of long indicator height to the ruler height. This value must
     * be between 0 to 1. The value should greater than [.getShortIndicatorHeightRatio].
     * Default value is 0.6 (i.e. 60%). If the value is 0, indicator won't
     * be displayed. If the value is 1, indicator height will be same as the
     * ruler height.
     * @param shortHeightRatio Ratio of short indicator height to the ruler height. This value must
     * be between 0 to 1. The value should less than [.getLongIndicatorHeightRatio].
     * Default value is 0.4 (i.e. 40%). If the value is 0, indicator won't
     * be displayed. If the value is 1, indicator height will be same as
     * the ruler height.
     * @see .getLongIndicatorHeightRatio
     * @see .getShortIndicatorHeightRatio
     */
    fun setIndicatorHeight(longHeightRatio: Float,
                           shortHeightRatio: Float) {
        mScaleView.setIndicatorHeight(longHeightRatio, shortHeightRatio)
    }

    /**
     * Set the [ScaleValuePickerListener] to get callbacks when the value changes.
     *
     * @param listener [ScaleValuePickerListener]
     */
    fun setValuePickerListener(@Nullable listener: ScaleValuePickerListener?) {
        mListener = listener
    }

    interface  ScaleValuePickerListener {
        fun onValueChange(selectedValue: Int)
        fun onIntermediateValueChange(selectedValue: Int)
    }

    /**
     * User interface state that is stored by ScaleView for implementing
     * [View.onSaveInstanceState].
     */
    class SavedState : BaseSavedState {
        var value = 0

        internal constructor(superState: Parcelable?) : super(superState) {}
        private constructor(`in`: Parcel) : super(`in`) {
            value = `in`.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(value)
        }

        companion object {
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<ScaleValuePicker.SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState? {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}
