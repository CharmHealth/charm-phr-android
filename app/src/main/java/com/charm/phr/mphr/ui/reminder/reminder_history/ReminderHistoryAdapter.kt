package com.charm.phr.mphr.ui.reminder.reminder_history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.databinding.ItemReminderHistoryDayViewBinding
import com.charm.phr.mphr.util.ModelUtil
import org.joda.time.DateTime

class ReminderHistoryAdapter(private val startDate: DateTime,
                             private val endDate: DateTime,
                             private val frequencyUnit: String?,
                             private val selectedWeekDays: ArrayList<Boolean>,
                             private val onBindDateView: (DateTime, ViewHolder) -> Unit
): RecyclerView.Adapter<ReminderHistoryAdapter.ViewHolder>() {

    private val validDates = calculateValidDates()

    private fun calculateValidDates(): List<DateTime> {
        val dates = mutableListOf<DateTime>()
        var currentDate = endDate

        while (startDate != currentDate) {
            if (frequencyUnit == "week") {
                val dayOfWeek = ModelUtil.getFormattedDayOfWeek(currentDate)
                if (selectedWeekDays[dayOfWeek]) {
                    dates.add(currentDate)
                }
            } else {
                dates.add(currentDate)
            }
            currentDate = currentDate.minusDays(1)
        }

        return dates
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReminderHistoryDayViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = validDates.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val currentDate = validDates[position]
        onBindDateView(currentDate, holder)
    }

    inner class ViewHolder(internal val binder: ItemReminderHistoryDayViewBinding) : RecyclerView.ViewHolder(binder.root)
}