package com.charm.phr.mphr.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.DialogMdReminderUpdateStatusBinding
import com.charm.phr.mphr.databinding.ItemDialogDrugDetailsBinding
import com.charm.phr.mphr.model.ReminderCustomPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.database.entity.Medications
import com.charm.phr.mphr.model.database.entity.Supplements
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import org.joda.time.DateTime


class MDReminderStatusUpdateDialog(context: Context, val width: Int) : Dialog(context) {

    private var selectedReminderList = arrayListOf<ReminderCustomPOJO>()
    var scheduledTime: String = ""
    var isFromSelectALl = false
    var isFromStatusUpdate = false
    private lateinit var dialogMdReminderUpdateStatusBinding: DialogMdReminderUpdateStatusBinding
    private var reminderStatusUpdateDialogListener: ReminderStatusUpdateDialogListener? = null
    var drugName = ""
    private var drugDirection = ""
    private var dialogSelectedReminderList = arrayListOf<ReminderCustomPOJO>()
    var startDate = ""
    var endDate = ""
    var isFromNotificationUpdate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogMdReminderUpdateStatusBinding =
            DialogMdReminderUpdateStatusBinding.inflate(layoutInflater)
        setContentView(dialogMdReminderUpdateStatusBinding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialogMdReminderUpdateStatusBinding.reminderStatusUpdateDialogLayout.layoutParams?.width =
            width
        dialogMdReminderUpdateStatusBinding.reminderStatusUpdateDialogLayout.requestLayout()
        renderUI()
        setOnClickListener()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isFromNotificationUpdate) {
            reminderStatusUpdateDialogListener?.onBackPress()
        }
    }

    fun setOnClickListener() {
        dialogMdReminderUpdateStatusBinding.dialogReminderEdit.setOnClickListener {
            val reminderPOJO = ReminderPOJO()
            reminderPOJO.reminder = selectedReminderList.first().reminder
            reminderPOJO.reminderMap = selectedReminderList.first().reminderMap
            reminderPOJO.reminderFrequencyMap = selectedReminderList.first().reminderFrequencyMap
            reminderStatusUpdateDialogListener?.onEditClick(
                reminderPOJO,
                drugName,
                drugDirection,
                startDate,
                endDate
            )
        }
        dialogMdReminderUpdateStatusBinding.reminderHistoryIV.setOnClickListener {
            val reminderPOJO = ReminderPOJO()
            reminderPOJO.reminder = selectedReminderList.first().reminder
            reminderPOJO.reminderMap = selectedReminderList.first().reminderMap
            reminderPOJO.reminderFrequencyMap = selectedReminderList.first().reminderFrequencyMap
            reminderStatusUpdateDialogListener?.onHistoryClick(
                reminderPOJO,
                drugName,
                drugDirection
            )
        }
        dialogMdReminderUpdateStatusBinding.dialogSkipRL.setOnClickListener {
            val currentTime =
                DateTime.now().withTimeAtStartOfDay().toString(ModelUtil.charmDisplayDateFormat())
            val scheduledTimeTo = UIUtil.formatDate(
                "$currentTime $scheduledTime",
                ModelUtil.genericDateDisplayFormattor()
            )
            if (scheduledTimeTo?.isAfterNow == true) {
                UIUtil.showToast(this.context, R.string.reminder_history_error, Toast.LENGTH_SHORT)
            } else {
                if (isFromSelectALl && dialogSelectedReminderList.isEmpty()) {
                    UIUtil.showToast(
                        this.context,
                        R.string.reminder_selection_error,
                        Toast.LENGTH_SHORT
                    )
                } else {
                    if (selectedReminderList.size == 1) {
                        reminderStatusUpdateDialogListener?.onSkipClick(selectedReminderList)
                    } else {
                        reminderStatusUpdateDialogListener?.onSkipClick(dialogSelectedReminderList)
                    }
                }
            }
        }
        dialogMdReminderUpdateStatusBinding.dialogReminderTakenRL.setOnClickListener {
            val currentTime =
                DateTime.now().withTimeAtStartOfDay().toString(ModelUtil.charmDisplayDateFormat())
            val scheduledTimeTo = UIUtil.formatDate(
                "$currentTime $scheduledTime",
                ModelUtil.genericDateDisplayFormattor()
            )
            if (scheduledTimeTo?.isAfterNow == true) {
                UIUtil.showToast(this.context, R.string.reminder_history_error, Toast.LENGTH_SHORT)
            } else {
                if (isFromSelectALl && dialogSelectedReminderList.isEmpty()) {
                    UIUtil.showToast(
                        this.context,
                        R.string.reminder_selection_error,
                        Toast.LENGTH_SHORT
                    )
                } else {
                    if (selectedReminderList.size == 1) {
                        reminderStatusUpdateDialogListener?.onTakenClick(
                            selectedReminderList,
                            arrayListOf()
                        )
                    } else {
                        if (dialogSelectedReminderList.size == selectedReminderList.size) {
                            reminderStatusUpdateDialogListener?.onTakenClick(
                                dialogSelectedReminderList,
                                arrayListOf()
                            )
                        } else {
                            val dialogSelectedId =
                                dialogSelectedReminderList.map { it.reminder?.reminderId }
                            val unSelected =
                                selectedReminderList.filter { it.reminder?.reminderId !in dialogSelectedId && it.reminderHistory.isEmpty() }
                            reminderStatusUpdateDialogListener?.onTakenClick(
                                dialogSelectedReminderList,
                                ArrayList(unSelected)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun renderUI() {
        if (!isFromStatusUpdate) {
            dialogMdReminderUpdateStatusBinding.dialogReminderBottomSeparator.visibility = View.GONE
            dialogMdReminderUpdateStatusBinding.dialogReminderStatusLL.visibility = View.GONE
            dialogMdReminderUpdateStatusBinding.dialogScheduledTimeTV.visibility = View.GONE
            dialogMdReminderUpdateStatusBinding.dialogScheduledForTV.text =
                context.getString(R.string.reminder_fill, scheduledTime)
        } else {
            dialogMdReminderUpdateStatusBinding.dialogScheduledTimeTV.text = scheduledTime
            if (isFromSelectALl) {
                dialogMdReminderUpdateStatusBinding.reminderHistoryIV.visibility = View.GONE
                dialogMdReminderUpdateStatusBinding.dialogReminderEdit.visibility = View.GONE
                if (selectedReminderList.size > 1) {
                    dialogSelectedReminderList.addAll(selectedReminderList)
                }
            }
        }
        selectedReminderList.forEach { reminderPOJO ->
            val itemDialogDrugDetailsBinding =
                ItemDialogDrugDetailsBinding.inflate(
                    LayoutInflater.from(context),
                    dialogMdReminderUpdateStatusBinding.dialogReminderLL,
                    false
                )
            when (reminderPOJO.reminderMap.first().type) {
                ModelUtil.medicationAddedAsMed -> {
                    itemDialogDrugDetailsBinding.dialogDrugIcon.setImageResource(R.drawable.ic_tablet)
                    reminderPOJO.selectedModuleObject?.let {
                        val medicationObject = reminderPOJO.selectedModuleObject as Medications
                        drugName = medicationObject.genericDrugName ?: ""
                        drugDirection = medicationObject.directions ?: ""
                        startDate =
                            medicationObject.startDate?.toString(ModelUtil.charmDisplayDateFormat())
                                ?: ""
                        endDate =
                            medicationObject.endDate?.toString(ModelUtil.charmDisplayDateFormat())
                                ?: ""
                    }
                }

                ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() } -> {
                    itemDialogDrugDetailsBinding.dialogDrugIcon.setImageResource(R.drawable.ic_supplements)
                    reminderPOJO.selectedModuleObject?.let {
                        val supplementObject = reminderPOJO.selectedModuleObject as Supplements
                        drugName = supplementObject.genericDrugName ?: ""
                        drugDirection = supplementObject.directions ?: ""
                        startDate =
                            supplementObject.fromDate?.toString(ModelUtil.charmDisplayDateFormat())
                                ?: ""
                        endDate =
                            supplementObject.toDate?.toString(ModelUtil.charmDisplayDateFormat())
                                ?: ""
                    }
                }
            }
            itemDialogDrugDetailsBinding.dialogModuleItemNameTV.text = drugName
            if (drugDirection.isBlank()) {
                itemDialogDrugDetailsBinding.dialogDrugDirectionTV.visibility = View.GONE
            } else {
                itemDialogDrugDetailsBinding.dialogDrugDirectionTV.text = drugDirection
                itemDialogDrugDetailsBinding.dialogDrugDirectionTV.visibility = View.VISIBLE
            }
            if (isFromSelectALl && selectedReminderList.size > 1) {
                itemDialogDrugDetailsBinding.dialogReminderSelectCB.visibility = View.VISIBLE
                itemDialogDrugDetailsBinding.dialogReminderSelectCB.setOnClickListener {
                    if (itemDialogDrugDetailsBinding.dialogReminderSelectCB.isChecked) {
                        dialogSelectedReminderList.add(reminderPOJO)
                    } else {
                        dialogSelectedReminderList.remove(reminderPOJO)
                    }
                }
            }
            dialogMdReminderUpdateStatusBinding.dialogReminderLL.addView(
                itemDialogDrugDetailsBinding.root
            )
            if (reminderPOJO.reminder?.reminderStatus == ModelUtil.UpcomingStatus) {
                dialogMdReminderUpdateStatusBinding.reminderHistoryIV.visibility = View.GONE
            }
        }
        if (isFromNotificationUpdate) {
            dialogMdReminderUpdateStatusBinding.reminderHistoryIV.visibility = View.GONE
            dialogMdReminderUpdateStatusBinding.dialogReminderEdit.visibility = View.GONE
        }
    }

    fun setSource(selectedReminderList: ArrayList<ReminderCustomPOJO>) {
        this.selectedReminderList.clear()
        this.selectedReminderList.addAll(selectedReminderList)
    }

    fun setReminderStatusUpdateDialogListener(listener: ReminderStatusUpdateDialogListener) {
        reminderStatusUpdateDialogListener = listener
    }

    interface ReminderStatusUpdateDialogListener {
        fun onEditClick(
            reminderPOJO: ReminderPOJO,
            drugName: String,
            drugDirection: String,
            startDate: String,
            endDate: String
        )

        fun onSkipClick(reminderPOJO: ArrayList<ReminderCustomPOJO>)
        fun onTakenClick(
            takenReminderPOJO: ArrayList<ReminderCustomPOJO>,
            skipReminderPOJO: ArrayList<ReminderCustomPOJO>
        )

        fun onHistoryClick(reminderPOJO: ReminderPOJO, drugName: String, drugDirection: String)
        fun onBackPress()
    }
}