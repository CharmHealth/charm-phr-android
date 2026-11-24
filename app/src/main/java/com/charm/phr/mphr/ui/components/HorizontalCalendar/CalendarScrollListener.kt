package com.charm.phr.mphr.ui.components.HorizontalCalendar

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import java.util.Calendar

internal class CalendarScrollListener(private val timelineView: CalendarRecyclerView) : RecyclerView.OnScrollListener() {

    private val calendar = Calendar.getInstance()
    private var year = -1
    private var yearStartOffset = 0
    private var yearEndOffset = 0
    private var monthCount = 12

    private val timelineItemWidth: Int = timelineView.resources.getDimensionPixelSize(R.dimen.hcItemWidth)

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val scrollOffset = recyclerView.computeHorizontalScrollOffset()
        val scrollOffsetCenter = scrollOffset + recyclerView.measuredWidth / 2
        val centerPosition = scrollOffsetCenter / timelineItemWidth

        if (scrollOffsetCenter !in yearStartOffset..yearEndOffset) {

            calendar.set(timelineView.startYear, timelineView.startMonth, timelineView.startDay)
            val startDay = calendar.get(Calendar.DAY_OF_YEAR)
            calendar.add(Calendar.DAY_OF_YEAR, centerPosition)

            year = calendar.get(Calendar.YEAR)
            var yearDayCount = calendar.getActualMaximum(Calendar.DAY_OF_YEAR)

            if (year != timelineView.startYear) {
                val yearDay = calendar.get(Calendar.DAY_OF_YEAR)
                yearStartOffset = scrollOffsetCenter - yearDay * timelineItemWidth - scrollOffsetCenter % timelineItemWidth
                monthCount = 12
            } else {
                yearStartOffset = 0
                monthCount = 12 - timelineView.startMonth
                yearDayCount -= startDay
            }

            yearEndOffset = yearStartOffset + yearDayCount * timelineItemWidth
        }

        Log.v("CalendarScrollListener", "yearStartOffset: " + yearStartOffset + ", " + "yearEndOffset: " + yearEndOffset + ", "
                + "scrollOffsetCenter: " + scrollOffsetCenter)
    }

}
