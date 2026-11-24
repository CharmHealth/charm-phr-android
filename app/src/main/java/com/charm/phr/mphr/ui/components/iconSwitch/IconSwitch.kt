package com.charm.phr.mphr.ui.components.iconSwitch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.charm.phr.mphr.R
import com.charm.phr.mphr.util.UIUtil

class IconSwitch : ViewGroup {
    private var TOUCH_SLOP_SQUARE = 0.0
    private var FLING_MIN_VELOCITY = 0
    var leftIcon: ImageView? = null
        private set
    var rightIcon: ImageView? = null
        private set
    private var thumb: ThumbView? = null
    private var background: IconSwitchBg? = null
    private var thumbDragHelper: ViewDragHelper? = null
    private var velocityTracker: VelocityTracker? = null
    private var thumbPosition = 0f
    private var thumbDragDistance = 0
    private var switchWidth = 0
    private var switchHeight = 0
    private var iconOffset = 0
    private var iconSize = 0
    private var iconTop = 0
    private var iconBottom = 0
    private var thumbStartLeft = 0
    private var thumbEndLeft = 0
    private var thumbDiameter = 0
    private var inactiveTintIconLeft = 0
    private var activeTintIconLeft = 0
    private var inactiveTintIconRight = 0
    private var activeTintIconRight = 0
    private var thumbColorLeft = 0
    private var thumbColorRight = 0
    private var downPoint: PointF? = null
    private var isClick = false
    private var dragState = 0
    private var translationX = 0
    private var translationY = 0
    var checked: CheckedEnum? = null
        private set
    private var listener: CheckedChangeListener? = null

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private fun init(attr: AttributeSet?) {
        addView(ThumbView(context).also { thumb = it })
        addView(ImageView(context).also { leftIcon = it })
        addView(ImageView(context).also { rightIcon = it })
        setBackground(IconSwitchBg().also { background = it })
        iconSize = dpToPx(IconSwitch.Companion.DEFAULT_IMAGE_SIZE_DP)
        val colorDefInactive = accentColor
        val colorDefActive = Color.WHITE
        val colorDefBackground: Int = ContextCompat.getColor(context, R.color.iconViewDefaultBg)
        if (attr != null) {
            val ta = context.obtainStyledAttributes(attr, R.styleable.IconSwitch)
            iconSize = ta.getDimensionPixelSize(R.styleable.IconSwitch_md_icon_size, iconSize)
            leftIcon!!.setImageDrawable(ta.getDrawable(R.styleable.IconSwitch_md_icon_left))
            rightIcon!!.setImageDrawable(ta.getDrawable(R.styleable.IconSwitch_md_icon_right))
            inactiveTintIconLeft = ta.getColor(R.styleable.IconSwitch_md_inactive_tint_icon_left, colorDefInactive)
            activeTintIconLeft = ta.getColor(R.styleable.IconSwitch_md_active_tint_icon_left, colorDefActive)
            inactiveTintIconRight = ta.getColor(R.styleable.IconSwitch_md_inactive_tint_icon_right, colorDefInactive)
            activeTintIconRight = ta.getColor(R.styleable.IconSwitch_md_active_tint_icon_right, colorDefActive)
            background!!.setColor(ta.getColor(R.styleable.IconSwitch_md_background_color, colorDefBackground))
            thumbColorLeft = ta.getColor(R.styleable.IconSwitch_md_thumb_color_left, colorDefInactive)
            thumbColorRight = ta.getColor(R.styleable.IconSwitch_md_thumb_color_right, colorDefInactive)
            checked = CheckedEnum.values()[ta.getInt(R.styleable.IconSwitch_md_default_selection, 0)]
            ta.recycle()
        } else {
            checked = CheckedEnum.LEFT
            inactiveTintIconLeft = colorDefInactive
            activeTintIconLeft = colorDefActive
            inactiveTintIconRight = colorDefInactive
            activeTintIconRight = colorDefActive
            background!!.setColor(colorDefBackground)
            thumbColorLeft = colorDefInactive
            thumbColorRight = colorDefInactive
        }
        thumbPosition = if (checked === CheckedEnum.LEFT) 0f else 1f
        calculateSwitchDimensions()
        ensureCorrectColors()
    }

    private fun calculateSwitchDimensions() {
        iconSize = Math.max(iconSize, dpToPx(IconSwitch.Companion.MIN_ICON_SIZE_DP))
        switchWidth = iconSize * 4
        switchHeight = Math.round(iconSize * 2f)
        iconOffset = Math.round(iconSize * 0.6f)
        iconTop = (switchHeight - iconSize) / 2
        iconBottom = iconTop + iconSize
        thumbDiameter = switchHeight
        val thumbRadius = thumbDiameter / 2
        val iconHalfSize = iconSize / 2
        thumbStartLeft = iconOffset + iconHalfSize - thumbRadius
        thumbEndLeft = switchWidth - iconOffset - iconHalfSize - thumbRadius
        thumbDragDistance = thumbEndLeft - thumbStartLeft
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val overshootPadding = Math.round(thumbDiameter * 0.1f)
        val width = getSize(widthMeasureSpec, switchWidth + overshootPadding * 2)
        val height = getSize(heightMeasureSpec, switchHeight)
        val thumbSpec = MeasureSpec.makeMeasureSpec(switchHeight, MeasureSpec.EXACTLY)
        thumb!!.measure(thumbSpec, thumbSpec)
        val iconSpec = MeasureSpec.makeMeasureSpec(iconSize, MeasureSpec.EXACTLY)
        leftIcon!!.measure(iconSpec, iconSpec)
        rightIcon!!.measure(iconSpec, iconSpec)
        background!!.init(iconSize, width, height)
        translationX = width / 2 - switchWidth / 2
        translationY = height / 2 - switchHeight / 2
        setMeasuredDimension(width, height + 5)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        leftIcon!!.layout(iconOffset, iconTop, iconOffset + iconSize, iconBottom)
        val rightIconLeft = switchWidth - iconOffset - iconSize
        rightIcon!!.layout(rightIconLeft, iconTop, rightIconLeft + iconSize, iconBottom)
        val thumbLeft = (thumbStartLeft + thumbDragDistance * thumbPosition).toInt()
        thumb!!.layout(thumbLeft, 0, thumbLeft + thumbDiameter, switchHeight)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val event = MotionEvent.obtain(e)
        event.setLocation(e.x - translationX, e.y - translationY)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> onDown(event)
            MotionEvent.ACTION_MOVE -> onMove(event)
            MotionEvent.ACTION_UP -> {
                onUp(event)
                clearTouchInfo()
            }
            MotionEvent.ACTION_CANCEL -> clearTouchInfo()
        }
        thumbDragHelper!!.processTouchEvent(event)
        event.recycle()
        return true
    }

    private fun onDown(e: MotionEvent) {
        velocityTracker = VelocityTracker.obtain()
        velocityTracker?.addMovement(e)
        downPoint!![e.x] = e.y
        isClick = true
        thumbDragHelper!!.captureChildView(thumb!!, e.getPointerId(0))
    }

    private fun onMove(e: MotionEvent) {
        velocityTracker!!.addMovement(e)
        val distance = Math.hypot(e.x - downPoint!!.x.toDouble(), e.y - downPoint!!.y.toDouble())
        if (isClick) {
            isClick = distance < TOUCH_SLOP_SQUARE
        }
    }

    private fun onUp(e: MotionEvent) {
        velocityTracker!!.addMovement(e)
        velocityTracker!!.computeCurrentVelocity(IconSwitch.Companion.UNITS_VELOCITY)
        if (isClick) {
            isClick = Math.abs(velocityTracker!!.xVelocity) < FLING_MIN_VELOCITY
        }
        if (isClick) {
            toggleSwitch()
            notifyCheckedChanged()
        }
    }

    private fun clearTouchInfo() {
        if (velocityTracker != null) {
            velocityTracker!!.recycle()
            velocityTracker = null
        }
    }

    private fun toggleSwitch() {
        checked = toggle(checked)
        val newLeft = if (checked === CheckedEnum.LEFT) thumbStartLeft else thumbEndLeft
        if (thumbDragHelper!!.smoothSlideViewTo(thumb, newLeft, thumb!!.getTop())) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun computeScroll() {
        if (thumbDragHelper!!.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        canvas.save()
        canvas.translate(translationX.toFloat(), translationY.toFloat())
        val result = super.drawChild(canvas, child, drawingTime)
        canvas.restore()
        return result
    }

    private fun getSize(measureSpec: Int, fallbackSize: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        return when (mode) {
            MeasureSpec.AT_MOST -> Math.min(size, fallbackSize)
            MeasureSpec.EXACTLY -> size
            MeasureSpec.UNSPECIFIED -> fallbackSize
            else -> size
        }
    }

    private fun getLeftAfterFling(direction: Float): Int {
        return if (direction > 0) thumbEndLeft else thumbStartLeft
    }

    fun setCheckedChangeListener(listener: CheckedChangeListener?) {
        this.listener = listener
    }

    private fun ensureCorrectColors() {
        leftIcon!!.setColorFilter(if (isLeftChecked) activeTintIconLeft else inactiveTintIconLeft)
        rightIcon!!.setColorFilter(if (isLeftChecked) inactiveTintIconRight else activeTintIconRight)
        thumb!!.color = (if (isLeftChecked) thumbColorLeft else thumbColorRight)
    }

    private val isLeftChecked: Boolean
        private get() = checked === CheckedEnum.LEFT

    private fun notifyCheckedChanged() {
        if (listener != null) {
            listener!!.onCheckChanged(checked)
        }
    }

    fun setChecked(newChecked: CheckedEnum) {
        if (checked !== newChecked) {
            toggleSwitch()
            notifyCheckedChanged()
        }
    }

    private fun toggle(checkedEnum: CheckedEnum?): CheckedEnum? {
        if (checkedEnum == null) {
            return null
        }
        return if (checkedEnum == CheckedEnum.LEFT) {
            CheckedEnum.RIGHT
        } else {
            CheckedEnum.LEFT
        }

    }

    fun setThumbColorLeft(@ColorInt thumbColorLeft: Int) {
        this.thumbColorLeft = thumbColorLeft
        ensureCorrectColors()
    }

    fun setThumbColorRight(@ColorInt thumbColorRight: Int) {
        this.thumbColorRight = thumbColorRight
        ensureCorrectColors()
    }

    fun setInactiveTintIconLeft(@ColorInt inactiveTintIconLeft: Int) {
        this.inactiveTintIconLeft = inactiveTintIconLeft
        ensureCorrectColors()
    }

    fun setInactiveTintIconRight(@ColorInt inactiveTintIconRight: Int) {
        this.inactiveTintIconRight = inactiveTintIconRight
        ensureCorrectColors()
    }

    fun setActiveTintIconLeft(@ColorInt activeTintIconLeft: Int) {
        this.activeTintIconLeft = activeTintIconLeft
        ensureCorrectColors()
    }

    fun setActiveTintIconRight(@ColorInt activeTintIconRight: Int) {
        this.activeTintIconRight = activeTintIconRight
        ensureCorrectColors()
    }

    override fun setBackgroundColor(@ColorInt color: Int) {
        background!!.setColor(color)
    }

    fun setIconSize(dp: Int) {
        iconSize = dpToPx(dp)
        calculateSwitchDimensions()
        requestLayout()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val state = Bundle()
        state.putParcelable(EXTRA_SUPER, super.onSaveInstanceState())
        state.putInt(EXTRA_CHECKED, checked!!.ordinal)
        return state
    }

    override fun onRestoreInstanceState(parcel: Parcelable) {
        val state = parcel as Bundle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            super.onRestoreInstanceState(state.getParcelable(EXTRA_SUPER,Parcelable::class.java))
        }
        else
        {
            super.onRestoreInstanceState(state.getParcelable(EXTRA_SUPER))
        }
        checked = IconSwitch.CheckedEnum.values()[state.getInt(EXTRA_CHECKED, 0)]
        thumbPosition = if (checked === CheckedEnum.LEFT) 0f else 1f
        ensureCorrectColors()
    }

    private fun applyPositionalTransform() {
        val clampedPosition = Math.max(0f, Math.min(thumbPosition, 1f)) //Ignore overshooting
        val leftColor = UIUtil.ofArgb(clampedPosition, activeTintIconLeft, inactiveTintIconLeft)
        leftIcon!!.setColorFilter(leftColor)
        val rightColor = UIUtil.ofArgb(clampedPosition, inactiveTintIconRight, activeTintIconRight)
        rightIcon!!.setColorFilter(rightColor)
        val thumbColor = UIUtil.ofArgb(clampedPosition, thumbColorLeft, thumbColorRight)
        thumb!!.color = (thumbColor)
        val closenessToCenter = 1f - Math.abs(clampedPosition - 0.5f) / 0.5f
        val iconScale = 1f - closenessToCenter * 0.3f
        leftIcon!!.scaleX = iconScale
        leftIcon!!.scaleY = iconScale
        rightIcon!!.scaleX = iconScale
        rightIcon!!.scaleY = iconScale
    }

    inner class ThumbDragCallback : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View?, pointerId: Int): Boolean {
            if (child !== thumb) {
                thumbDragHelper!!.captureChildView(thumb!!, pointerId)
                return false
            }
            return true
        }

        override fun onViewReleased(releasedChild: View?, xvel: Float, yvel: Float) {
            if (isClick) {
                return
            }
            val isFling = Math.abs(xvel) >= FLING_MIN_VELOCITY
            val newLeft = if (isFling) getLeftAfterFling(xvel) else leftToSettle
            val newChecked = if (newLeft == thumbStartLeft) CheckedEnum.LEFT else CheckedEnum.RIGHT
            if (newChecked !== checked) {
                checked = newChecked
                notifyCheckedChanged()
            }
            thumbDragHelper!!.settleCapturedViewAt(newLeft, thumb!!.getTop())
            invalidate()
        }

        private val leftToSettle: Int
            private get() = if (thumbPosition > 0.5f) thumbEndLeft else thumbStartLeft

        override fun onViewPositionChanged(changedView: View?, left: Int, top: Int, dx: Int, dy: Int) {
            thumbPosition = (left - thumbStartLeft).toFloat() / thumbDragDistance
            applyPositionalTransform()
        }

        override fun clampViewPositionHorizontal(child: View?, left: Int, dx: Int): Int {
            return if (dragState == ViewDragHelper.STATE_DRAGGING) {
                Math.max(thumbStartLeft, Math.min(left, thumbEndLeft))
            } else left
        }

        override fun onViewDragStateChanged(state: Int) {
            dragState = state
        }


        override fun getViewHorizontalDragRange(child: View?): Int {
            return if (child === thumb) thumbDragDistance else 0
        }
    }

    fun getThumbCenter(point: Point) {
        val thumbRadius = thumbDiameter / 2
        val thumbLeft = (thumbStartLeft + thumbDragDistance * thumbPosition).toInt()
        val thumbCenterX = thumbLeft + translationX
        val thumbCenterY = thumbRadius + translationY
        point[thumbCenterX] = thumbCenterY
    }

    private fun dpToPx(dp: Int): Int {
        return Math.round(resources.displayMetrics.density * dp)
    }

    private val accentColor: Int
        private get() {
            val typedValue = TypedValue()
            val a = context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorAccent))
            val color = a.getColor(0, 0)
            a.recycle()
            return color
        }

    interface CheckedChangeListener {
        fun onCheckChanged(current: CheckedEnum?)
    }

    enum class CheckedEnum {
        LEFT, RIGHT
    }

    companion object {
        private const val EXTRA_SUPER = "extra_super"
        private const val EXTRA_CHECKED = "extra_is_checked"
        private const val DEFAULT_IMAGE_SIZE_DP = 16
        private const val MIN_ICON_SIZE_DP = 12
        private const val UNITS_VELOCITY = 1000
    }

    init {
        val viewConf = ViewConfiguration.get(context)
        FLING_MIN_VELOCITY = viewConf.scaledMinimumFlingVelocity
        TOUCH_SLOP_SQUARE = Math.pow(viewConf.scaledTouchSlop.toDouble(), 2.0)
        thumbDragHelper = ViewDragHelper.create(this, ThumbDragCallback())
        downPoint = PointF()
    }
}