package com.charm.phr.mphr.ui.components.HorizontalCalendar

import android.content.Context
import android.graphics.PointF
import android.text.format.DateUtils
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemHorizontalCalendarBinding
import com.charm.phr.mphr.ui.components.StartSnapHelper
import org.joda.time.DateTime
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.concurrent.TimeUnit

class CalendarRecyclerView : RecyclerView {

    private val calendar = Calendar.getInstance()//Locale(LocaleHelper.getLanguage(context))

    private var adapter: TimelineAdapter? = null
    private var layoutManager: CustomLinearLayoutManager? = null
    var onDateSelectedListener: HorizontalCalendarLayout.OnDateSelectedListener? = null
    var onScrollRecyclerViewListener: HorizontalCalendarLayout.OnScrollRecyclerViewListener? = null


    var startYear = 1970

    var startMonth = 0

    var startDay = 1

    var selectedYear: Int = 0

    var selectedMonth: Int = 0

    var selectedDay: Int = 0


    private var selectedPosition = 1
    private var dayCount = Integer.MAX_VALUE

    // Day letter
    private var lblDayColor: Int = 0
    // Day number label
    private var lblDateColor: Int = 0

    private var lblDateSelectedColor: Int = 0

    var currentVisiblePositionForScroll: Int? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }


    private fun init() {

        calendar.timeInMillis = System.currentTimeMillis()
        /*setSelectedDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))*/
        resetCalendar()

        setHasFixedSize(true)
        layoutManager = CustomLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        adapter = TimelineAdapter()
        setLayoutManager(layoutManager)
        setAdapter(adapter)

        addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                Log.d("CalendarRecyclerView", "onScrolled dx${dx}, dy${dy}")
                onRecyclerViewScrolled(dx)

            }

        })

        clipChildren = false

        val snapHelper = StartSnapHelper()
        snapHelper.attachToRecyclerView(this)
/*
        Log.d("selectedLanguage" , LocaleHelper.getLanguage(context))
*/

    }

    private fun resetCalendar() {
        calendar.set(startYear, startMonth, startDay, 1, 0, 0)
    }

    private fun setDayCount(dayCount: Int) {

        if (this.dayCount == dayCount) {
            return
        }

        this.dayCount = dayCount
        if (adapter != null) {
            adapter!!.notifyDataSetChanged()
        }

    }

    fun onRecyclerViewScrolled(dx: Int) {

        val currentVisiblePosition = currentVisiblePositionForScroll
        if (childCount > 0 && dx != 0) {

            //region left scrolling

            if (dx > 0) {

                //scroll left

                Log.d("CalendarRecyclerView", " scroll left dx = " + dx + "currentVisiblePosition: ${currentVisiblePosition}")

                if (currentVisiblePosition == null) {

                    val lastVisibleItemPosition = layoutManager!!.findLastVisibleItemPosition()
                    val targetView = layoutManager!!.findViewByPosition(lastVisibleItemPosition)
                    if (targetView != null) {

                        val itemBinding = ItemHorizontalCalendarBinding.bind(targetView)
                        val dateStr = itemBinding.horizontalCalendarDateTv.text?.toString()?.trim()
                        if (dateStr != null && dateStr.isNotBlank()) {
                            if (dateStr.split(" ")[0].toIntOrNull() == 1) {
                                //enable
                                currentVisiblePositionForScroll = lastVisibleItemPosition
                                onScrollRecyclerViewListener!!.onScrolled(targetView.left.toFloat(), Pair(first = true, second = false), itemBinding.horizontalCalendarMonthAndYearTv.text.toString())
                                Log.d("CalendarRecyclerView", "absolute position left: " + Math.abs(targetView.x))
                                Log.d("CalendarRecyclerView", "currentVisiblePosition ${currentVisiblePosition}")
                            }
                        }

                    }

                } else {


                    if (layoutManager!!.findFirstVisibleItemPosition() == currentVisiblePosition) {
                        //stop
                        Log.d("CalendarRecyclerView", "stop: findFirstVisibleItemPosition():${layoutManager!!.findFirstVisibleItemPosition()} currentVisiblePosition ${currentVisiblePosition}")
                        currentVisiblePositionForScroll = null
                        onScrollRecyclerViewListener!!.onScrolled(0F, Pair(true, true), "")
                        return
                    }

                    val targetView = layoutManager!!.findViewByPosition(currentVisiblePosition)
                    if (targetView != null) {
                        val itemBinding = ItemHorizontalCalendarBinding.bind(targetView)

                        val dateStr = itemBinding.horizontalCalendarDateTv.text?.toString()?.trim()
                        if (dateStr != null && dateStr.isNotBlank()) {
                            if (dateStr.split(" ")[0].toIntOrNull() == 1) {

                                onScrollRecyclerViewListener!!.onScrolled(targetView.left.toFloat(), Pair(first = true, second = false), "")
                                Log.d("CalendarRecyclerView", "absolute position: " + Math.abs(targetView.x))
                                Log.d("CalendarRecyclerView", "currentVisiblePosition ${currentVisiblePosition}")
                            }
                        }

                    }
                }

            }

            //endregion

            //region right scrolling

            else {

                //scroll right
                Log.d("CalendarRecyclerView", " scroll right dx = " + dx)

                if (currentVisiblePosition == null) {

                    val firstVisibleItemPosition = layoutManager!!.findFirstCompletelyVisibleItemPosition()
                    val targetView = layoutManager!!.findViewByPosition(firstVisibleItemPosition)

                    if (targetView != null) {
                        val itemBinding = ItemHorizontalCalendarBinding.bind(targetView)

                        val dateStr = itemBinding.horizontalCalendarDateTv.text?.toString()?.trim()
                        if (dateStr != null && dateStr.isNotBlank()) {
                            if (dateStr.split(" ")[0].toIntOrNull() == 1) {
                                //enable
                                currentVisiblePositionForScroll = firstVisibleItemPosition
                                onScrollRecyclerViewListener!!.onScrolled(targetView.left.toFloat(), Pair(first = false, second = false), itemBinding.horizontalCalendarMonthAndYearTv.text.toString())
                                Log.d("CalendarRecyclerView", "absolute position right scroll: " + Math.abs(targetView.x))
                                Log.d("CalendarRecyclerView", "currentVisiblePosition ${currentVisiblePosition}")
                            }
                        }

                    }

                } else {

                    if (layoutManager!!.findLastVisibleItemPosition() == currentVisiblePosition - 1) {
                        //stop
                        Log.d("CalendarRecyclerView", "stop: findLastVisibleItemPosition():${layoutManager!!.findLastVisibleItemPosition()} currentVisiblePosition -1 ${currentVisiblePosition - 1}")
                        currentVisiblePositionForScroll = null
                        onScrollRecyclerViewListener!!.onScrolled(0F, Pair(first = false, second = true), "")
                        return
                    }

                    val targetView = layoutManager!!.findViewByPosition(currentVisiblePosition)
                    if (targetView != null) {
                        val itemBinding = ItemHorizontalCalendarBinding.bind(targetView)
                        val dateStr = itemBinding.horizontalCalendarDateTv.text?.toString()?.trim()
                        if (dateStr != null && dateStr.isNotBlank()) {
                            if (dateStr.split(" ")[0].toIntOrNull() == 1) {

                                onScrollRecyclerViewListener!!.onScrolled(targetView.left.toFloat(), Pair(first = false, second = false), "")
                                Log.d("CalendarRecyclerView", "absolute position left scroll: " + Math.abs(targetView.x))
                                Log.d("CalendarRecyclerView", "currentVisiblePosition ${currentVisiblePosition}")
                            }
                        }

                    }
                }
            }

            //endregion

        }
    }

    fun setInitialPositionOfDynamicView() {

        post {

            Log.d("CalendarRecyclerView", "setInitialPositionOfDynamicView()")
            //views are not rendered by this time so posting to background thread

            val firstVisibleItemPosition = layoutManager!!.findFirstVisibleItemPosition()
            val lastVisibleItemPosition = layoutManager!!.findLastVisibleItemPosition()
            val firstCompletelyVisibleItemPosition = layoutManager!!.findFirstCompletelyVisibleItemPosition()

            var dynamicViewLeft: Int? = null
            var dateString: String? = null
            var positionOfDynamicViewInRecyclerView: Int? = null

            for (i in firstVisibleItemPosition..lastVisibleItemPosition) {
                val itemView = layoutManager!!.findViewByPosition(i)
                if (itemView !== null){
                    val itemBinding = ItemHorizontalCalendarBinding.bind(itemView)
                    val dateViewStr = itemBinding?.horizontalCalendarDateTv?.text?.toString()
                    if (dateViewStr != null && dateViewStr.trim().isNotBlank()) {
                        if (dateViewStr.toInt() == 1) {
                            positionOfDynamicViewInRecyclerView = i
                            dynamicViewLeft = itemView.left
                            dateString = itemBinding.horizontalCalendarMonthAndYearTv.text.toString()
                            break
                        }
                    }
                }
            }

            if (dynamicViewLeft != null && positionOfDynamicViewInRecyclerView != null) { //No date 1 on screen

                if (positionOfDynamicViewInRecyclerView != 0) {
                    currentVisiblePositionForScroll = positionOfDynamicViewInRecyclerView
                } else {
                    currentVisiblePositionForScroll = null
                }

                onScrollRecyclerViewListener?.onInitialPositionSetup(dynamicViewLeft.toFloat(), dateString)

            } else {

                val firstVisibleView = layoutManager!!.findViewByPosition(firstCompletelyVisibleItemPosition)
                if (firstVisibleView != null){
                    val itemBinding = ItemHorizontalCalendarBinding.bind(firstVisibleView)
                    val str = itemBinding?.horizontalCalendarMonthAndYearTv?.text?.toString()
                    if (str != null){
                        onScrollRecyclerViewListener?.onInitialPositionSetup(null, str)
                    }
                }
            }

            //only the trace of 1 position here!

        }

    }

    private fun onDateSelected(position: Int, year: Int, month: Int, day: Int, changeDataSet: Boolean = true) {

        val oldPosition = selectedPosition
        selectedPosition = position

        this.selectedYear = year
        this.selectedMonth = month
        this.selectedDay = day

        if (adapter != null && layoutManager != null) {

            adapter!!.notifyItemChanged(oldPosition)
            adapter!!.notifyItemChanged(position)

            if (onDateSelectedListener != null) {

                val selectedDateTime = DateTime(selectedYear , selectedMonth +1 , selectedDay , 0 ,0)
                onDateSelectedListener!!.onDateSelected(selectedDateTime, selectedPosition, changeDataSet)
            }
        }

    }

    fun setSelectedDate(year: Int, month: Int, d: Int, dataSetChange: Boolean = true) {

        var day = d

        if (year == startYear && month == startMonth && day < startDay) {
            day = startDay
        }

        // Get new selected dayOfYear
        calendar.set(year, month, day, 1, 0, 0)
        val newDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val newTimestamp = calendar.timeInMillis

        // Get current selected dayOfYear
        calendar.set(selectedYear, selectedMonth, selectedDay, 1, 0, 0)
        val oldDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val oldTimestamp = calendar.timeInMillis

        val dayDifference: Int
        dayDifference = if (year == selectedYear) {
            newDayOfYear - oldDayOfYear
        } else {
            // Lazy...
            val dayDifferenceApprox = ((newTimestamp - oldTimestamp) / TimeUnit.DAYS.toMillis(1)).toInt()
            calendar.add(Calendar.DAY_OF_YEAR, dayDifferenceApprox)
            dayDifferenceApprox + (newDayOfYear - calendar.get(Calendar.DAY_OF_YEAR))
        }

        Log.d("CalendarRecyclerView", "dayDifference: ${dayDifference}")
        val selectedPos = if(selectedPosition < 0) 0 else selectedPosition
        onDateSelected(selectedPos + dayDifference, year, month, day, dataSetChange)

    }

    fun setFirstDate(startYear: Int, startMonth: Int, startDay: Int) {

        this.startYear = startYear
        this.startMonth = startMonth
        this.startDay = startDay

        selectedYear = startYear
        selectedMonth = startMonth
        selectedDay = startDay
        selectedPosition = -1
        if (adapter != null) {
            adapter!!.notifyDataSetChanged()
        }

    }

    fun setLastDate(endYear: Int, endMonth: Int, endDay: Int) {

        val firstDate = Calendar.getInstance()
        firstDate.set(startYear, startMonth, startDay)
        val lastDate = Calendar.getInstance()
        lastDate.set(endYear, endMonth, endDay)

        val dayDiff = TimeUnit.DAYS.convert(lastDate.timeInMillis - firstDate.timeInMillis,
                TimeUnit.MILLISECONDS).toInt()

        setDayCount(dayDiff + 1)
    }

    //region scroll

    fun scrollRight() {

        if (childCount == 0) return

        val totalListItemCount = adapter?.itemCount
        if (totalListItemCount != null) {
            val firstVisibleItemPosition = layoutManager!!.findFirstVisibleItemPosition()
            val lastVisibleItemPosition = layoutManager!!.findLastCompletelyVisibleItemPosition()
            val completelyVisibleItemsCount = lastVisibleItemPosition - firstVisibleItemPosition
            if (completelyVisibleItemsCount > 0) {
                if (completelyVisibleItemsCount + lastVisibleItemPosition <= totalListItemCount - 1) //-1 to get position
                    smoothScrollToPosition(completelyVisibleItemsCount + lastVisibleItemPosition)
                else
                    smoothScrollToPosition(totalListItemCount - 1)
            }
        }
    }

    fun scrollLeft() {

        if (childCount == 0) return

        val firstVisibleItemPosition = layoutManager!!.findFirstCompletelyVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager!!.findLastVisibleItemPosition()
        val completelyVisibleItemsCount = lastVisibleItemPosition - firstVisibleItemPosition
        if (completelyVisibleItemsCount > 0) {
            if (firstVisibleItemPosition - completelyVisibleItemsCount > 0)
                smoothScrollToPosition(firstVisibleItemPosition - completelyVisibleItemsCount)
            else
                smoothScrollToPosition(0)
        }

    }

    /* fun setToPosition(position: Int) {

         if (childCount == 0) {
             return
         }
         *//*else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!isLaidOut()) {
                return;
            }
        }*//*

        // Animate scroll
        *//*   val offset = measuredWidth / 2 - getChildAt(0).measuredWidth / 2
           layoutManager!!.scrollToPositionWithOffset(position, offset)*//*

    }*/

    //endregion

    fun customNotifyDataSetChanged() {
        adapter!!.notifyDataSetChanged()
    }

    /*fun setSelectedPosition(position: Int) {
        resetCalendar()
        calendar.add(Calendar.DAY_OF_YEAR, position)
        onDateSelected(position, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), false)
    }*/

/*

    fun getSelectedPosition(): Int {
        return selectedPosition
    }
*/

    fun setDayLabelColor(lblDayColor: Int) {
        this.lblDayColor = lblDayColor
    }

    fun setDateLabelColor(lblDateColor: Int) {
        this.lblDateColor = lblDateColor
    }

    fun setDateLabelSelectedColor(lblDateSelectedColor: Int) {
        this.lblDateSelectedColor = lblDateSelectedColor
    }


    private inner class TimelineAdapter internal constructor() : RecyclerView.Adapter<TimelineViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
            val binding = ItemHorizontalCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TimelineViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
            // Set calendar
            resetCalendar()
            calendar.add(Calendar.DAY_OF_YEAR, position)

            // Get values
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val isToday = DateUtils.isToday(calendar.timeInMillis)

            holder.bind(position, year, month, day, dayOfWeek, position == selectedPosition, isToday)
        }

        override fun getItemCount(): Int {
            return dayCount
        }
    }

    private inner class TimelineViewHolder internal constructor(val binding: ItemHorizontalCalendarBinding) : RecyclerView.ViewHolder(binding.root) {

    private val dayTv: TextView = binding.horizontalCalendarDayTv
    private val dateTv: TextView = binding.horizontalCalendarDateTv
    private val mainLayout: ConstraintLayout = binding.horizontalCalendarLayout

        var holderPosition: Int = 0
        private var year: Int = 0
        private var month: Int = 0
        private var day: Int = 0

        init {

            dayTv.setTextColor(lblDayColor)
            dateTv.setTextColor(lblDateColor)

            binding.root.setOnClickListener {
                onDateSelected(holderPosition, year, month, day)
                Log.d("CalendarRecyclerView", "onClick from time line")
            }
        }

        internal fun bind(bindPosition: Int, year: Int, month: Int, day: Int, dayOfWeek: Int, selected: Boolean, isToday: Boolean) {

            this.holderPosition = bindPosition
            this.year = year
            this.month = month
            this.day = day


            dayTv.text = getDayOfWeekInShort(context)[dayOfWeek]
            val monthStr = day.toString() + " "+ getMonthText(context)[month]
            dateTv.text = monthStr


            if (selected && isToday) {

                dayTv.setText(R.string.today_str)
                dateTv.setTextColor(ContextCompat.getColor(context,R.color.textColorForColorPrimaryBg))
                dayTv.setTextColor(ContextCompat.getColor(context,R.color.textColorForColorPrimaryBg))
                mainLayout.setBackgroundResource(R.drawable.horizontal_calendar_selected_item)

            } else if (selected) {

                dateTv.setTextColor(ContextCompat.getColor(context,R.color.textColorForColorPrimaryBg))
                dayTv.setTextColor(ContextCompat.getColor(context,R.color.textColorForColorPrimaryBg))
                mainLayout.setBackgroundResource(R.drawable.horizontal_calendar_selected_item)

            } else if (isToday) {

                dayTv.setText(R.string.today_str)
                dateTv.setTextColor(ContextCompat.getColor(context,android.R.color.black))
                dayTv.setTextColor(ContextCompat.getColor(context,android.R.color.black))
                mainLayout.setBackgroundResource(R.drawable.horizontal_calendar_unselected_item)

            } else {

                dateTv.setTextColor(ContextCompat.getColor(context,android.R.color.black))
                dayTv.setTextColor(ContextCompat.getColor(context,android.R.color.black))
                mainLayout.setBackgroundResource(R.drawable.horizontal_calendar_unselected_item)
            }

/*
            dateTv.setTextColor(if (selected) lblDateSelectedColor else lblDateColor)
*/

        }
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {

        //setting max fling velocity (1000 pixels per second)
        val customVelocityX: Int = if (velocityX in -1000..1000) {
            velocityX
        } else {
            if (velocityX >= 1000) {
                1000
            } else {
                if (velocityX <= -1000) -1000 else velocityX
            }
        }



        Log.d("CalendarRecyclerView", "CalendarRecyclerView fling customVelocityX $customVelocityX")

        return super.fling(customVelocityX, velocityY)
    }

    companion object {

        fun getDayOfWeekInShort(context: Context) = DateFormatSymbols.getInstance(/*LocaleHelper.getLocale(context)*/).shortWeekdays
        fun getMonthText(context: Context) = DateFormatSymbols.getInstance(/*LocaleHelper.getLocale(context)*/).shortMonths //month should be given -1

    }

}

class CustomLinearLayoutManager : LinearLayoutManager {

    constructor(context: Context) : super(context)

    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {

        val linearSmoothScroller = object : LinearSmoothScroller(recyclerView.context) {

            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                return super.computeScrollVectorForPosition(targetPosition)
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }
        }

        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    /*override fun scrollToPosition(position: Int) {

        val linearSmoothScroller = object : LinearSmoothScroller() {

            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                return super.computeScrollVectorForPosition(targetPosition)
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }
        }

        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }*/

    companion object {

        private val MILLISECONDS_PER_INCH = 70f //default is 25f (bigger = slower)
    }
}


/*class StartSnapHelper : LinearSnapHelper() {

    private lateinit var mVerticalHelper: OrientationHelper
    private lateinit var mHorizontalHelper: OrientationHelper

    @Throws(IllegalStateException::class)
    override fun attachToRecyclerView( recyclerView: RecyclerView?) {
        super.attachToRecyclerView(recyclerView)
    }

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager,
                                              targetView: View): IntArray? {
        val out = IntArray(2)

        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager))
        } else {
            out[0] = 0
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager))
        } else {
            out[1] = 0
        }
        return out
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {

        return if (layoutManager is LinearLayoutManager) {

            if (layoutManager.canScrollHorizontally()) {
                getStartView(layoutManager, getHorizontalHelper(layoutManager))
            } else {
                getStartView(layoutManager, getVerticalHelper(layoutManager))
            }
        } else super.findSnapView(layoutManager)

    }

    private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
        return helper.getDecoratedStart(targetView) - helper.startAfterPadding
    }

    private fun getStartView(layoutManager: RecyclerView.LayoutManager,
                             helper: OrientationHelper): View? {

        if (layoutManager is LinearLayoutManager) {
            val firstChild = layoutManager.findFirstVisibleItemPosition()

            val isLastItem = layoutManager
                    .findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1

            if (firstChild == RecyclerView.NO_POSITION || isLastItem) {
                return null
            }

            val child = layoutManager.findViewByPosition(firstChild)

            return if (helper.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2 && helper.getDecoratedEnd(child) > 0) {
                child
            } else {
                if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1) {
                    null
                } else {
                    layoutManager.findViewByPosition(firstChild + 1)
                }
            }
        }

        return super.findSnapView(layoutManager)
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return mVerticalHelper
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return mHorizontalHelper
    }
}*/


