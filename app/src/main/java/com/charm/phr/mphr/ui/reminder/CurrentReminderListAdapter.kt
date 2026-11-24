package com.charm.phr.mphr.ui.reminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemReminderDrugDetailsBinding
import com.charm.phr.mphr.databinding.ItemTimeGroupedReminderBinding
import com.charm.phr.mphr.model.ReminderCustomPOJO
import com.charm.phr.mphr.model.ReminderHistoryPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.database.entity.Medications
import com.charm.phr.mphr.model.database.entity.Supplements
import com.charm.phr.mphr.ui.components.MDReminderStatusUpdateDialog
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import org.joda.time.DateTime


class CurrentReminderListAdapter(val activity: ReminderActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var currentReminder =
        arrayListOf<MutableMap.MutableEntry<DateTime, ArrayList<ReminderCustomPOJO>>>()

    private lateinit var itemTimeGroupedReminderBinding: ItemTimeGroupedReminderBinding

    private var currentReminderListAdapterListener: CurrentReminderListAdapterListener? = null


    //region override method
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        itemTimeGroupedReminderBinding = ItemTimeGroupedReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(itemTimeGroupedReminderBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val reminderPOJOList = currentReminder[position].value
        val reminderTime =
            (currentReminder[position].key).toString(ModelUtil.charmDisplayTimeFormat)
        if (holder is ViewHolder) {
            holder.bindViews(
                reminderPOJOList,
                reminderTime,
                activity,
                currentReminderListAdapterListener,
                position
            )

        }
    }

    override fun getItemCount(): Int {
        return currentReminder.size
    }
    //endregion

    class ViewHolder(val binder: ItemTimeGroupedReminderBinding) :
        RecyclerView.ViewHolder(binder.root) {
        fun bindViews(
            reminderList: ArrayList<ReminderCustomPOJO>,
            time: String,
            activity: ReminderActivity,
            currentReminderListAdapterListener: CurrentReminderListAdapterListener?,
            position: Int
        ) {
            binder.currentReminderTimeTV.text = time
            val screenWidth = UIUtil.getScreenWidth(activity)
            val width =
                screenWidth - (2 * binder.root.context.resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))
            binder.currentReminderSelectAllTV.setOnClickListener {
                binder.currentReminderSelectAllTV.isEnabled = false
                val mDReminderStatusUpdateDialog =
                    MDReminderStatusUpdateDialog(binder.root.context, width)
                mDReminderStatusUpdateDialog.setReminderStatusUpdateDialogListener(object :
                    MDReminderStatusUpdateDialog.ReminderStatusUpdateDialogListener {
                    override fun onEditClick(
                        reminderPOJO: ReminderPOJO,
                        drugName: String,
                        drugDirection: String,
                        startDate: String,
                        endDate: String
                    ) {
                        currentReminderListAdapterListener?.onEditClick(
                            reminderPOJO,
                            drugName,
                            drugDirection,
                            startDate,
                            endDate
                        )
                        mDReminderStatusUpdateDialog.dismiss()
                        binder.currentReminderSelectAllTV.isEnabled = true
                    }

                    override fun onSkipClick(reminderPOJO: ArrayList<ReminderCustomPOJO>) {
                        currentReminderListAdapterListener?.onSkipClick(
                            reminderPOJO,
                            time,
                            position
                        )
                        mDReminderStatusUpdateDialog.dismiss()
                        binder.currentReminderSelectAllTV.isEnabled = true
                    }

                    override fun onTakenClick(
                        takenReminderPOJO: ArrayList<ReminderCustomPOJO>,
                        skipReminderPOJO: ArrayList<ReminderCustomPOJO>
                    ) {
                        currentReminderListAdapterListener?.onTakenClick(
                            takenReminderPOJO,
                            skipReminderPOJO,
                            time,
                            position
                        )
                        mDReminderStatusUpdateDialog.dismiss()
                        binder.currentReminderSelectAllTV.isEnabled = true
                    }

                    override fun onHistoryClick(
                        reminderPOJO: ReminderPOJO,
                        drugName: String,
                        drugDirection: String
                    ) {
                        currentReminderListAdapterListener?.onHistoryClick(
                            reminderPOJO,
                            drugName,
                            drugDirection
                        )
                        mDReminderStatusUpdateDialog.dismiss()
                        binder.currentReminderSelectAllTV.isEnabled = true
                    }

                    override fun onBackPress() {
                        //not required
                    }

                })
                mDReminderStatusUpdateDialog.setOnCancelListener {
                    //When you touch outside of dialog bounds,
                    //the dialog gets canceled and this method executes.
                    binder.currentReminderSelectAllTV.isEnabled = true
                }
                mDReminderStatusUpdateDialog.setSource(reminderList)
                mDReminderStatusUpdateDialog.scheduledTime = time
                mDReminderStatusUpdateDialog.isFromSelectALl = true
                mDReminderStatusUpdateDialog.isFromStatusUpdate = true
                mDReminderStatusUpdateDialog.show()
            }
            binder.currentReminderLL.removeAllViews()
            if (reminderList.size > 1) {
                binder.currentReminderSelectAllTV.visibility = View.VISIBLE
            } else {
                binder.currentReminderSelectAllTV.visibility = View.GONE
            }
            reminderList.forEach { reminderPOJO ->
                val itemReminderDrugDetailsBinding =
                    ItemReminderDrugDetailsBinding.inflate(
                        LayoutInflater.from(binder.root.context),
                        binder.currentReminderLL,
                        false
                    )
                when (reminderPOJO.reminderMap.first().type) {
                    ModelUtil.medicationAddedAsMed -> {
                        itemReminderDrugDetailsBinding.reminderDrugIcon.setImageResource(R.drawable.ic_tablet)
                        var medicationObject = Medications()
                        if (reminderPOJO.selectedModuleObject != null) {
                            medicationObject = reminderPOJO.selectedModuleObject as Medications
                        }
                        itemReminderDrugDetailsBinding.reminderModuleItemNameTV.text =
                            medicationObject.genericDrugName
                        if (medicationObject.directions.isNullOrBlank()) {
                            itemReminderDrugDetailsBinding.reminderDrugDirectionTV.visibility =
                                View.GONE
                        } else {
                            itemReminderDrugDetailsBinding.reminderDrugDirectionTV.text =
                                medicationObject.directions
                            itemReminderDrugDetailsBinding.reminderDrugDirectionTV.visibility =
                                View.VISIBLE
                        }
                    }

                    ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() } -> {
                        itemReminderDrugDetailsBinding.reminderDrugIcon.setImageResource(R.drawable.ic_supplements)
                        var supplementObject = Supplements()
                        if (reminderPOJO.selectedModuleObject != null) {
                            supplementObject = reminderPOJO.selectedModuleObject as Supplements
                        }
                        itemReminderDrugDetailsBinding.reminderModuleItemNameTV.text =
                            supplementObject.genericDrugName
                        if (supplementObject.directions.isNullOrBlank()) {
                            itemReminderDrugDetailsBinding.reminderDrugDirectionTV.visibility =
                                View.GONE
                        } else {
                            itemReminderDrugDetailsBinding.reminderDrugDirectionTV.text =
                                supplementObject.directions
                            itemReminderDrugDetailsBinding.reminderDrugDirectionTV.visibility =
                                View.VISIBLE
                        }

                    }
                }
                itemReminderDrugDetailsBinding.root.setOnClickListener {
                    itemReminderDrugDetailsBinding.root.isEnabled = false
                    val mDReminderStatusUpdateDialog =
                        MDReminderStatusUpdateDialog(binder.root.context, width)
                    mDReminderStatusUpdateDialog.setReminderStatusUpdateDialogListener(object :
                        MDReminderStatusUpdateDialog.ReminderStatusUpdateDialogListener {
                        override fun onEditClick(
                            reminderPOJO: ReminderPOJO,
                            drugName: String,
                            drugDirection: String,
                            startDate: String,
                            endDate: String
                        ) {
                            currentReminderListAdapterListener?.onEditClick(
                                reminderPOJO,
                                drugName,
                                drugDirection,
                                startDate,
                                endDate
                            )
                            mDReminderStatusUpdateDialog.dismiss()
                            itemReminderDrugDetailsBinding.root.isEnabled = true
                        }

                        override fun onSkipClick(reminderPOJO: ArrayList<ReminderCustomPOJO>) {
                            currentReminderListAdapterListener?.onSkipClick(
                                reminderPOJO,
                                time,
                                position
                            )
                            mDReminderStatusUpdateDialog.dismiss()
                            itemReminderDrugDetailsBinding.root.isEnabled = true
                        }

                        override fun onTakenClick(
                            takenReminderPOJO: ArrayList<ReminderCustomPOJO>,
                            skipReminderPOJO: ArrayList<ReminderCustomPOJO>
                        ) {
                            currentReminderListAdapterListener?.onTakenClick(
                                takenReminderPOJO,
                                skipReminderPOJO,
                                time,
                                position
                            )
                            mDReminderStatusUpdateDialog.dismiss()
                            itemReminderDrugDetailsBinding.root.isEnabled = true
                        }

                        override fun onHistoryClick(
                            reminderPOJO: ReminderPOJO,
                            drugName: String,
                            drugDirection: String
                        ) {
                            currentReminderListAdapterListener?.onHistoryClick(
                                reminderPOJO,
                                drugName,
                                drugDirection
                            )
                            mDReminderStatusUpdateDialog.dismiss()
                            itemReminderDrugDetailsBinding.root.isEnabled = true
                        }

                        override fun onBackPress() {
                            //not required
                        }

                    })
                    mDReminderStatusUpdateDialog.setOnCancelListener {
                        //When you touch outside of dialog bounds,
                        //the dialog gets canceled and this method executes.
                        itemReminderDrugDetailsBinding.root.isEnabled = true
                    }
                    mDReminderStatusUpdateDialog.setSource(arrayListOf(reminderPOJO))
                    mDReminderStatusUpdateDialog.isFromSelectALl = false
                    mDReminderStatusUpdateDialog.isFromStatusUpdate = true
                    mDReminderStatusUpdateDialog.scheduledTime = time
                    mDReminderStatusUpdateDialog.show()
                }
                val reminderHistory = reminderPOJO.reminderHistory.firstOrNull()
                if (reminderHistory != null) {
                    itemReminderDrugDetailsBinding.reminderStatusIcon.visibility = View.VISIBLE
                    if (reminderHistory.reminderHistory?.status == 1) {
                        itemReminderDrugDetailsBinding.reminderStatusIcon.setImageResource(R.drawable.ic_taken)
                    } else {
                        itemReminderDrugDetailsBinding.reminderStatusIcon.setImageResource(R.drawable.ic_skipped)
                    }
                } else {
                    val currentTimeStr = DateTime.now().toString(ModelUtil.charmDisplayTimeFormat)
                    val currentTime = UIUtil.formatDate(
                        currentTimeStr,
                        ModelUtil.charmDisplayTimeFormat()
                    )?.millis ?: 0
                    val scheduledTime =
                        UIUtil.formatDate(time, ModelUtil.charmDisplayTimeFormat())?.millis ?: 0
                    if (scheduledTime <= currentTime) {
                        itemReminderDrugDetailsBinding.reminderStatusIcon.visibility = View.VISIBLE
                        itemReminderDrugDetailsBinding.reminderStatusIcon.setImageResource(R.drawable.ic_missing)
                    } else {
                        itemReminderDrugDetailsBinding.reminderStatusIcon.visibility = View.GONE
                    }
                }
                binder.currentReminderLL.addView(itemReminderDrugDetailsBinding.root)
            }

        }
    }

    fun setCurrentReminderListAdapterListener(listener: CurrentReminderListAdapterListener) {
        currentReminderListAdapterListener = listener
    }

    fun setCurrentReminders(currentReminder: List<MutableMap.MutableEntry<DateTime, ArrayList<ReminderCustomPOJO>>>) {
        this.currentReminder.clear()
        this.currentReminder.addAll(currentReminder)
        this.notifyDataSetChanged()
    }

    fun updateReminderHistory(position: Int, reminderHistoryList: ArrayList<ReminderHistoryPOJO>) {
        val reminderFrequencyList = currentReminder[position].value
        reminderHistoryList.forEach { reminderHistoryPOJO ->
            val reminderPOJO = reminderFrequencyList.firstOrNull { reminderPOJO ->
                reminderPOJO.reminder?.reminderId == reminderHistoryPOJO.reminderHistory?.reminderMapId
            }
            reminderPOJO?.reminderHistory = arrayListOf(reminderHistoryPOJO)
        }
        this.notifyDataSetChanged()
    }

    interface CurrentReminderListAdapterListener {
        fun onEditClick(
            reminderPOJO: ReminderPOJO,
            drugName: String,
            drugDirection: String,
            startDate: String,
            endDate: String
        )

        fun onSkipClick(
            reminderPOJO: ArrayList<ReminderCustomPOJO>,
            scheduledTime: String,
            position: Int
        )

        fun onTakenClick(
            reminderPOJO: ArrayList<ReminderCustomPOJO>,
            skipReminderPOJO: ArrayList<ReminderCustomPOJO>,
            scheduledTime: String,
            position: Int
        )

        fun onHistoryClick(reminderPOJO: ReminderPOJO, drugName: String, drugDirection: String)
    }
}