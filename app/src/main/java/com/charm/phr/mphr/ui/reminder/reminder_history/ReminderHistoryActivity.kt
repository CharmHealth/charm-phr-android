package com.charm.phr.mphr.ui.reminder.reminder_history

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityReminderHistoryBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.joda.time.DateTime
import javax.inject.Inject

class ReminderHistoryActivity : MDBaseActivity() {

    companion object {
        const val fetchReminderCount = 50
    }

    private lateinit var activityReminderHistoryBinding: ActivityReminderHistoryBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var reminderHistoryViewModel: ReminderHistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityReminderHistoryBinding = ActivityReminderHistoryBinding.inflate(layoutInflater)
        setContentView(activityReminderHistoryBinding.root)
        reminderHistoryViewModel = ViewModelProvider(this, viewModelFactory)[ReminderHistoryViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(activityReminderHistoryBinding.reminderHistoryToolbar)
        UIUtil.applyViewInsertForBottomView(activityReminderHistoryBinding.reminderHistoryMainLayout)

        val reminderPOJOStr = intent.getStringExtra(ModelUtil.ReminderPOJO)
        val reminderId = intent.getStringExtra(ModelUtil.ReminderModuleId)
        val reminderPOJOJson = JsonParser.parseString(reminderPOJOStr)
        reminderHistoryViewModel.reminderPOJO =
            MDParsingHelper.formReminderObjectWithLocalId(reminderPOJOJson as JsonObject)
        reminderHistoryViewModel.reminderPOJO.reminder?.reminderId = reminderId
        renderDrugDetails()
        getReminderHistory()
        setOnClickListener()
    }

    private fun onBackClick() {
        super.onBackPressed()
    }

    fun setOnClickListener() {
        activityReminderHistoryBinding.reminderHistoryBackBtn.setOnClickListener {
            onBackClick()
        }
    }

    private fun renderDrugDetails() {
        activityReminderHistoryBinding.reminderHistoryModuleItemNameTV.text =
            intent.getStringExtra(ModelUtil.drugName)
        if (intent.getStringExtra(ModelUtil.drugDirection).isNullOrBlank()) {
            activityReminderHistoryBinding.reminderHistoryDrugDirectionTV.visibility = View.GONE
        } else {
            activityReminderHistoryBinding.reminderHistoryDrugDirectionTV.text =
                intent.getStringExtra(ModelUtil.drugDirection)
        }
        activityReminderHistoryBinding.reminderHistoryStartDateTV.text =
            reminderHistoryViewModel.reminderPOJO.reminder?.startDate?.toString(ModelUtil.charmDisplayDateFormat())
        activityReminderHistoryBinding.reminderEndDateTV.text =
            reminderHistoryViewModel.reminderPOJO.reminder?.endDate?.toString(ModelUtil.charmDisplayDateFormat())
    }

    private fun createDayLayout(layoutDate: DateTime?, reminderDayViewLL: LinearLayout) {
        val reminderHistoryList = reminderHistoryViewModel.reminderHistoryDateGrouping[layoutDate]
        reminderDayViewLL.removeAllViews()
        if (!reminderHistoryList.isNullOrEmpty()) {
            reminderHistoryViewModel.reminderTimingSet.forEach {
                val time = it.toString(ModelUtil.charmDisplay24HrTimeFormat)
                val reminderHistory = reminderHistoryList.filter { reminderHistoryPOJO ->
                    reminderHistoryPOJO.reminderFrequencyMap.firstOrNull()?.time == time
                }
                var status: Boolean? = true
                if (reminderHistory.firstOrNull() == null) {
                    status = null
                } else if (reminderHistory.firstOrNull()?.reminderHistory?.status == 1) {
                    status = false
                }
                val imageView = createImageView(status)
                reminderDayViewLL.addView(imageView)
            }
        } else {
            reminderHistoryViewModel.reminderTimingSet.forEach { _ ->
                val imageView = createImageView(null)
                reminderDayViewLL.addView(imageView)
            }
        }
    }

    private fun renderReminderHistory() {
        groupHistoryByDate()
        setReminderTimingMap()
        setTimeLayout()
        setDayLayout()
    }

    private fun groupHistoryByDate() {
        reminderHistoryViewModel.reminderHistoryPOJOArrList.forEach { reminderHistory ->
            val statusUpdatedDate =
                reminderHistory.reminderHistory?.statusUpdatedTime?.toString(ModelUtil.charmDisplayDateFormat())
            val reminderHistoryTime = UIUtil.formatDate(
                statusUpdatedDate,
                ModelUtil.charmDisplayDateFormat()
            ) ?: DateTime.now()
            if (reminderHistoryViewModel.reminderHistoryDateGrouping.containsKey(reminderHistoryTime)) {
                reminderHistoryViewModel.reminderHistoryDateGrouping[reminderHistoryTime]?.add(
                    reminderHistory
                )
            } else {
                reminderHistoryViewModel.reminderHistoryDateGrouping[reminderHistoryTime] =
                    arrayListOf(reminderHistory)
            }
        }
    }

    private fun setTimeLayout() {
        reminderHistoryViewModel.reminderTimingSet.forEach {
            val textView = TextView(this)
            textView.text = it.toString(ModelUtil.charmDisplayTimeFormat)
            textView.setTextColor(getColor(R.color.black87))
            textView.textSize = 12F
            val typeface = UIUtil.getFont(this, R.font.open_sans)
            textView.typeface = typeface
            val params = LinearLayout.LayoutParams(
                ModelUtil.dpToPxConversion(65, this),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView.layoutParams = params
            textView.setPadding(5, 0, 5, 0)
            activityReminderHistoryBinding.reminderHistoryTimeLL.addView(textView)
        }
    }

    private fun setDayLayout() {
        val startDate = reminderHistoryViewModel.reminderPOJO.reminder?.startDate?.minusDays(1)
        var endDate =
            if (reminderHistoryViewModel.reminderPOJO.reminder?.endDate?.isAfterNow == true) DateTime.now()
                .withTimeAtStartOfDay() else reminderHistoryViewModel.reminderPOJO.reminder?.endDate

        val layoutManager = LinearLayoutManager(this)
        activityReminderHistoryBinding.reminderHistoryDayRV.layoutManager = layoutManager

        val adapter = ReminderHistoryAdapter(
            startDate ?: DateTime.now(), endDate ?: DateTime.now(), reminderHistoryViewModel.reminderPOJO.reminder?.frequencyUnit,
            reminderHistoryViewModel.selectedWeekDay
        ) { date, holder ->
            holder.binder.reminderDayViewTV.text = date.toString(ModelUtil.charmDisplayDateMonthDateFormat)
            createDayLayout(date, holder.binder.reminderDayViewLL)
        }

        activityReminderHistoryBinding.reminderHistoryDayRV.adapter = adapter

    }

    private fun setReminderTimingMap() {

        reminderHistoryViewModel.reminderPOJO.reminderFrequencyMap.forEach {
            val day = it.day
            if (day != -1) {
                reminderHistoryViewModel.selectedWeekDay[day] = true
            }
            reminderHistoryViewModel.reminderTimingSet.add(
                UIUtil.formatDate(
                    it.time,
                    ModelUtil.charmDisplay24HrTimeFormat()
                ) ?: DateTime.now()
            )
        }
        reminderHistoryViewModel.reminderTimingSet =
            reminderHistoryViewModel.reminderTimingSet.sorted().toMutableSet()
    }


    private fun createImageView(isSkipped: Boolean?): ImageView {
        val imageView = ImageView(this)
        imageView.id = View.generateViewId()
        if (isSkipped == null) {
            imageView.setImageResource(R.drawable.ic_missing)
        } else if (isSkipped) {
            imageView.setImageResource(R.drawable.ic_skipped)
        } else {
            imageView.setImageResource(R.drawable.ic_taken)
        }
        val params = LinearLayout.LayoutParams(
            ModelUtil.dpToPxConversion(65, this),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        imageView.layoutParams = params
        imageView.setPadding(0, 0, 15, 0)
        return imageView
    }

    private fun getReminderHistory() {
        progressDialog.setMessage(R.string.loading_reminder_history)
        progressDialog.show()
        reminderHistoryViewModel.getReminderHistory().observe(this) { historyResp ->
            if (historyResp != null) {
                progressDialog.dismiss()
                if (historyResp is ArrayList<*>) {
                    val reminderHistoryPOJOList = historyResp as ArrayList<ReminderHistoryPOJO>
                    reminderHistoryViewModel.reminderHistoryPOJOArrList.addAll(
                        reminderHistoryViewModel.addOrUpdateReminderHistoryInLocalDb(
                            reminderHistoryPOJOList
                        )
                    )
                    if (reminderHistoryPOJOList.size >= fetchReminderCount) {
                        reminderHistoryViewModel.fromRange += fetchReminderCount
                        reminderHistoryViewModel.toRange += fetchReminderCount
                        getReminderHistory()
                    } else {
                        renderReminderHistory()
                    }

                } else if (historyResp is APIResponseError) {
                    UIUtil.showRetryAndCancelDialog(
                        this,
                        historyResp.errorMsg ?: getString(R.string.requestFailedTryAgain),
                        R.string.retry_str,
                        false
                    ) { isRetry ->
                        if (isRetry) {
                            getReminderHistory()
                        } else {
                            onBackClick()
                        }
                    }
                }
            }
        }
    }
}