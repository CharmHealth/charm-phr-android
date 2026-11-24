package com.charm.phr.mphr.ui.components.HorizontalCalendar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.HorizontalCalendarViewBinding
import com.charm.phr.mphr.ui.components.HorizontalCalendar.CalendarRecyclerView.Companion.getMonthText
import org.joda.time.DateTime
import java.util.Calendar

class HorizontalCalendarLayout : LinearLayout {

    var calendarRecyclerView: CalendarRecyclerView? = null
        private set
    var onDateSelectedListener: OnDateSelectedListener? = null
    private var timelineScrollListener: CalendarScrollListener? = null
    private var onScrollRecyclerViewListener: OnScrollRecyclerViewListener? = null

    var dynamicViewId: Int? = null
    var staticViewRightPosition: Int? = null
    var staticViewId: Int? = null

    lateinit var binding: HorizontalCalendarViewBinding

    /*val selectedYear: Int
        get() = calendarRecyclerView!!.selectedYear

    val selectedMonth: Int
        get() = calendarRecyclerView!!.selectedMonth

    val selectedDay: Int
        get() = calendarRecyclerView!!.selectedDay*/

    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {

        val calendar = Calendar.getInstance()
        var startYear = calendar.get(Calendar.YEAR)

        if (calendar.get(Calendar.MONTH) == Calendar.JANUARY) {
            // If we are in january, we'll probably want to have previous year :)
            startYear--
        }

        val startMonth = Calendar.JANUARY
        val startDay = 1

        // Load default values

        var bgTimelineColor = ContextCompat.getColor(context,android.R.color.white)
        var lblDayColor = ContextCompat.getColor(context, R.color.mti_lbl_day)
        var lblDateColor = ContextCompat.getColor(context, R.color.mti_lbl_date)
        var lblDateSelectedColor = ContextCompat.getColor(context,android.R.color.white)
        var bgLblDateSelectedColor = ContextCompat.getColor(context, R.color.calSelectedDateColor)
        var ringLblDateSelectedColor = ContextCompat.getColor(context, R.color.calSelectedDateColor)
        var bgLblTodayColor = ContextCompat.getColor(context,android.R.color.white)
        var lblLabelColor = ContextCompat.getColor(context, R.color.mti_lbl_label)

        // Load xml attrs
        val a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalCalendarLayout, defStyleAttr, 0)

        bgTimelineColor = a.getColor(R.styleable.HorizontalCalendarLayout_mti_bgTimelineColor, bgTimelineColor)
        //
        lblDayColor = a.getColor(R.styleable.HorizontalCalendarLayout_mti_lblDayColor, lblDayColor)
        lblDateColor = a.getColor(R.styleable.HorizontalCalendarLayout_mti_lblDateColor, lblDateColor)
        lblDateSelectedColor = a.getColor(R.styleable.HorizontalCalendarLayout_mti_lblDateSelectedColor, lblDateSelectedColor)
        bgLblDateSelectedColor = a.getColor(R.styleable.HorizontalCalendarLayout_mti_bgLblDateSelectedColor, bgLblDateSelectedColor)
        ringLblDateSelectedColor = a.getColor(R.styleable.HorizontalCalendarLayout_mti_ringLblDateSelectedColor, ringLblDateSelectedColor)
        bgLblTodayColor = a.getColor(R.styleable.HorizontalCalendarLayout_mti_bgLblTodayColor, bgLblTodayColor)
        lblLabelColor = a.getColor(R.styleable.HorizontalCalendarLayout_mti_lblLabelColor, lblLabelColor)

        //

        val followScroll = a.getBoolean(R.styleable.HorizontalCalendarLayout_mti_followScroll, true)
        a.recycle()

/*
        val selectedDrawable = ContextCompat
                .getDrawable(context, R.drawable.mti_bg_lbl_date_selected) as LayerDrawable?
        (selectedDrawable!!.getDrawable(0) as GradientDrawable).setColor(ringLblDateSelectedColor)
        (selectedDrawable.getDrawable(1) as GradientDrawable).setColor(bgLblDateSelectedColor)
*/

        /*val todayDrawable = ContextCompat
                .getDrawable(context, R.drawable.mti_bg_lbl_date_today) as LayerDrawable?
        (todayDrawable!!.getDrawable(1) as GradientDrawable).setColor(bgLblTodayColor)*/

        orientation = LinearLayout.VERTICAL

        binding = HorizontalCalendarViewBinding.inflate(LayoutInflater.from(context), this, true)
        val layoutView = HorizontalCalendarViewBinding.inflate(LayoutInflater.from(context), this, true)
        calendarRecyclerView = layoutView.calendarViewComponent as CalendarRecyclerView
        calendarRecyclerView!!.setBackgroundColor(bgTimelineColor)
        calendarRecyclerView!!.setFirstDate(startYear, startMonth, startDay)
        calendarRecyclerView!!.setDayLabelColor(lblDayColor)
        calendarRecyclerView!!.setDateLabelColor(lblDateColor)
        calendarRecyclerView!!.setDateLabelSelectedColor(lblDateSelectedColor)

        calendarRecyclerView!!.onDateSelectedListener = object : OnDateSelectedListener {

            override fun onDateSelected(selectedDateTime: DateTime, index: Int, changeDataSet: Boolean) {
                if (onDateSelectedListener != null) {
                    onDateSelectedListener!!.onDateSelected(selectedDateTime, index, changeDataSet)
                }
            }

        }

        calendarRecyclerView!!.onScrollRecyclerViewListener = object : OnScrollRecyclerViewListener {

            override fun onInitialPositionSetup(dynamicViewInitPos: Float?, dateString: String?) {

                if (dateString != null && dateString.isNotBlank() && staticViewId == null) {

                    val screenW = context.resources.displayMetrics.widthPixels
                    val screenH = context.resources.displayMetrics.heightPixels

                    if (dynamicViewInitPos != null) { // when date 1 is visible in screen

                        val staticTv = LayoutInflater.from(context).inflate(R.layout.text_view_month_and_year, binding.monthAndYearContainerLayout, false) as TextView


                        staticTv.id = View.generateViewId()
                        staticViewId = staticTv.id

                        //
                        val strArr = dateString.split("_")

                        var month = if (dynamicViewInitPos != 0F) {
                            strArr[1].toInt() - 1 //for prev month
                        } else {
                            strArr[1].toInt()  //only static view will be shown returning current month
                        }

                        if (month < 0) month = 11
                        val year = if (month == 11) {
                            strArr.first().toInt()
                        } else {
                            strArr.first().toInt()
                        }

                        //month ie (Jan - 0),(feb - 1)...
                        staticTv.text = getMonthText(context)[month].toString().plus(" ").plus(year)

                        layoutView.monthAndYearContainerLayout.addView(staticTv) //adding to layout

                        staticTv.measure(screenW, screenH)

                        val viewWidth = staticTv.measuredWidth

                        staticViewRightPosition = viewWidth

                        if (dynamicViewInitPos != 0F) { //when date 1 is not at view at 0 position, only static view enough

                            val dynamicTv = LayoutInflater.from(context).inflate(R.layout.text_view_month_and_year, binding.monthAndYearContainerLayout, false) as TextView
                            dynamicTv.id = View.generateViewId()
                            dynamicViewId = dynamicTv.id

                            if (dynamicViewInitPos < viewWidth) { //when Dynamic view intersecting with static view

                                //
                                val dateStrArr = dateString.split("_")
                                var dynMonth = dateStrArr[1].toInt()
                                if (dynMonth < 0) dynMonth = 11
                                val dynYear = if (dynMonth == 11) {
                                    strArr.first().toInt()
                                } else {
                                    strArr.first().toInt()
                                }

                                //month ie (Jan - 0),(feb - 1)...
                                dynamicTv.text = getMonthText(context)[dynMonth].toString().plus(" ").plus(dynYear)
                                //

                                val pos = dynamicViewInitPos - viewWidth // negative value outside the screen
                                ObjectAnimator.ofFloat(dynamicTv, "x", pos).apply {
                                    duration = 0
                                    start()
                                }
                                ObjectAnimator.ofFloat(dynamicTv, "x", dynamicViewInitPos).apply {
                                    duration = 0
                                    start()
                                }


                                layoutView.monthAndYearContainerLayout.addView(dynamicTv) //adding to layout

                            } else { //when dynamic view not intersecting with static view
                                //no changes needed in static view animation

                                val dateStrArr = dateString.split("_")
                                var dynMonth = dateStrArr[1].toInt()
                                if (dynMonth < 0) dynMonth = 11
                                val dynYear = if (dynMonth == 11) {
                                    strArr.first().toInt()
                                } else {
                                    strArr.first().toInt()
                                }

                                //month ie (Jan - 0),(feb - 1)...
                                dynamicTv.text = getMonthText(context)[dynMonth].toString().plus(" ").plus(dynYear)
                                //

                                ObjectAnimator.ofFloat(dynamicTv, "x", dynamicViewInitPos).apply {
                                    duration = 0
                                    start()
                                }

                                layoutView.monthAndYearContainerLayout.addView(dynamicTv) //adding to layout

                            }

                        }

                    } else { //date 1 is not visible in recycler view (only static view)

                        val staticTv = LayoutInflater.from(context).inflate(R.layout.text_view_month_and_year, binding.monthAndYearContainerLayout, false) as TextView

                        staticTv.id = View.generateViewId()
                        staticViewId = staticTv.id

                        //
                        val strArr = dateString.split("_")

                        var month = strArr[1].toInt()
                        if (month < 0) month = 11
                        val year = if (month == 11) {
                            strArr.first().toInt()
                        } else {
                            strArr.first().toInt()
                        }

                        //month ie (Jan - 0),(feb - 1)...
                        staticTv.text = getMonthText(context)[month].toString().plus(" ").plus(year)
                        //

                        layoutView.monthAndYearContainerLayout.addView(staticTv) //adding to layout

                        staticTv.measure(screenW, screenH)

                        val viewWidth = staticTv.measuredWidth

                        staticViewRightPosition = viewWidth

                        ObjectAnimator.ofFloat(staticTv, "x", 0F).apply {
                            duration = 0
                            start()
                        }

                    }

                }
            }

            override fun onScrolled(position: Float, scrollingDirection: Pair<Boolean, Boolean>, dateString: String) {
                //date string will be returned only for the first time of view creation

                if (scrollingDirection.first) { //scrollingTowardsLeft

                    Log.d("HorizonCalLayout", "scrollingTowardsLeft")
                    if (dynamicViewId == null) {

                        val dynamicTv = LayoutInflater.from(context).inflate(R.layout.text_view_month_and_year, binding.monthAndYearContainerLayout, false) as TextView
                        dynamicTv.id = View.generateViewId()

                        if (dateString.trim().isNotBlank()) {
                            val strArr = dateString.split("_")
                            dynamicTv.text = getMonthText(context)[strArr[1].toInt()].plus(" ").plus(strArr[0])
                            dynamicViewId = dynamicTv.id
                            layoutView.monthAndYearContainerLayout.addView(dynamicTv)
                            ObjectAnimator.ofFloat(dynamicTv, "x", position).apply {
                                duration = 0
                                start()
                            }
                        }

                    }
                    else {

                        val dId = dynamicViewId
                        if (dId != null) {

                            val dynamicTv = layoutView.monthAndYearContainerLayout.findViewById<TextView>(dId)
                            if (dynamicTv != null) {

                                val staticViewRightPos = staticViewRightPosition
                                val sId = staticViewId

                                if (staticViewRightPos != null && sId != null && position <= staticViewRightPos.toFloat()) {

                                    val staticTv = layoutView.monthAndYearContainerLayout.findViewById<TextView>(sId)
                                    if (staticTv != null)
                                        ObjectAnimator.ofFloat(staticTv, "x", position - staticViewRightPos).apply {

                                            duration = 0
                                            if (position == 0F) {
                                                addListener(object : AnimatorListenerAdapter() {
                                                    override fun onAnimationEnd(animation: Animator) {
                                                        super.onAnimationEnd(animation)
                                                        layoutView.monthAndYearContainerLayout.removeViewInLayout(staticTv)
                                                    }
                                                })
                                            }
                                            start()
                                        }

                                }

                                ObjectAnimator.ofFloat(dynamicTv, "x", position).apply {
                                    duration = 0
                                    if (position == 0F) {
                                        addListener(object : AnimatorListenerAdapter() {
                                            override fun onAnimationEnd(animation: Animator) {
                                                super.onAnimationEnd(animation)
                                                    staticViewId = dynamicViewId
                                                    staticViewRightPosition = dynamicTv.right
                                                    dynamicViewId = null
                                            }
                                        })
                                    }
                                    start()

                                }
                            }


                        }

                    }

                }
                else if (!scrollingDirection.first) { //scrollingTowardsRight

                    Log.d("HorizonCalLayout", "scrollingDirection.first == scrollingRight")

                    val dynamicId = dynamicViewId
                    if (dynamicId != null) {

                        val dynamicTv = layoutView.monthAndYearContainerLayout.findViewById<TextView>(dynamicId)

                        Log.d("HorizonCalLayout", "Scrolling right")
                        val staticId = staticViewId
                        val staticViewRightPos = staticViewRightPosition

                        if (staticId != null) { //static id wont be null at this place

                            val staticTv = layoutView.monthAndYearContainerLayout.findViewById<TextView>(staticId)

                            //static view animation


                            Log.d("HorizonCalLayout", "staticViewRightPosition ${staticViewRightPosition}")

                            if (staticViewRightPos != null)
                                if (position < staticViewRightPos && position != 0F) {

                                    Log.d("HorizonCalLayout", "position ${position}")
                                    Log.d("HorizonCalLayout", "staticViewRightPos ${staticViewRightPos}")

                                    val pos = position - staticViewRightPos
                                    ObjectAnimator.ofFloat(staticTv, "x", pos).apply {
                                        duration = 0
                                        start()
                                    }

                                } else {

                                    if (position < (staticViewRightPos * 2F)) {
                                        Log.d("HorizonCalLayout", "inner else")
                                        ObjectAnimator.ofFloat(staticTv, "x", 0F).apply {
                                            addListener(object : AnimatorListenerAdapter() {
                                                override fun onAnimationEnd(animation: Animator) {
                                                    super.onAnimationEnd(animation)
                                                    staticViewRightPosition = staticTv.right
                                                }
                                            })
                                            duration = 0
                                            start()
                                        }

                                    }

                                }

                            //dynamic animation
                            Log.d("HorizonCalLayout", "is scroll ended ${scrollingDirection.second}")
                            if (!scrollingDirection.second) {

                                ObjectAnimator.ofFloat(dynamicTv, "x", position).apply {
                                    duration = 0
                                    start()
                                }

                            } else { // view gone out of the parent view
                                layoutView.monthAndYearContainerLayout.removeView(dynamicTv)
                                Log.d("HorizonCalLayout", "dynamicTv removed")
                                dynamicViewId = null
                            }
                        }

                    }
                    else { //dynamicId == null

                        Log.d("HorizonCalLayout", "dynamicTv == null")

                        val staticViewRightPos = staticViewRightPosition

                        if (dateString.trim().isNotBlank() && staticViewRightPos != null) {

                            Log.d("HorizonCalLayout", "dateString NotBlank")

                            dynamicViewId = staticViewId
                            val staticTv = LayoutInflater.from(context).inflate(R.layout.text_view_month_and_year, binding.monthAndYearContainerLayout, false) as TextView
                            staticTv.id = View.generateViewId()
                            staticViewId = staticTv.id

                            val strArr = dateString.split("_")

                            var month = strArr[1].toInt() - 1 //for prev month
                            if (month < 0) month = 11
                            val year = if (month == 11) {
                                strArr.first().toInt() - 1
                            } else {
                                strArr.first().toInt()
                            }

                            //month ie (Jan - 0),(feb - 1)...
                            staticTv.text = getMonthText(context)[month].toString().plus(" ").plus(year)

                            layoutView.monthAndYearContainerLayout.addView(staticTv)
                            val pos = position - staticViewRightPos
                            ObjectAnimator.ofFloat(staticTv, "x", pos).apply {
                                duration = 0
                                start()
                            }

                        }
                    }

                }

            }

        }

        /*calendarRecyclerView!!.setSelectedDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))*/

        if (followScroll) {
            timelineScrollListener = CalendarScrollListener(calendarRecyclerView!!)
            calendarRecyclerView!!.addOnScrollListener(timelineScrollListener!!)
        }

        layoutView.calendarLeftScrollBtn.setOnClickListener {
            scrollLeft()
        }

        layoutView.calendarRightScrollBtn.setOnClickListener {
            scrollRight()
        }


    }

    //region

    /**
     * always enter month -1 to this method
     */
    fun setSelectedDate(year: Int, month: Int, day: Int, isDataSetChange:Boolean) {

        calendarRecyclerView!!.setSelectedDate(year, month, day, isDataSetChange)
    }

    fun setInitialPositionOfDynamicView(){
        calendarRecyclerView!!.setInitialPositionOfDynamicView()
    }

    /**
     * always enter month -1 to this method
     */
    fun setFirstVisibleDate(startDate:DateTime) {
        calendarRecyclerView!!.setFirstDate(startDate.year, startDate.monthOfYear - 1, startDate.dayOfMonth)
    }

    /**
     * always enter month -1 to this method
     */
    fun setLastVisibleDate(endDate: DateTime) {
        calendarRecyclerView!!.setLastDate(endDate.year, endDate.monthOfYear - 1, endDate.dayOfMonth)
    }



    //endregion

    //region
    fun scrollLeft() {
        calendarRecyclerView!!.scrollLeft()
    }

    fun scrollRight() {
        calendarRecyclerView!!.scrollRight()
    }

    //endregion

    //region
    interface OnDateSelectedListener {
        /**
         * Note :month  will be less of one count i.e {2-2-18 will return as 2-1-18}
         */
        fun onDateSelected(selectedDateTime: DateTime, index: Int, changeDataSet: Boolean)


    }

    interface OnScrollRecyclerViewListener {

        //isScrollLeft,ScrollFinished
        /**
         * position: dynamic view position
         * scrollingDirection:Pair<isScrollingLeft,isScrollFinished>
         *
         */
        fun onScrolled(position: Float, scrollingDirection: Pair<Boolean, Boolean>, dateString: String)

        fun onInitialPositionSetup(dynamicViewInitPos: Float?, dateString: String?)

    }

    //endregion

}
