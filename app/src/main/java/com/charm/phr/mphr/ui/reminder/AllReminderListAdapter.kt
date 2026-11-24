package com.charm.phr.mphr.ui.reminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemAllReminderDrugDetailsBinding
import com.charm.phr.mphr.model.ReminderCustomPOJO
import com.charm.phr.mphr.model.ReminderPOJO
import com.charm.phr.mphr.model.database.entity.Medications
import com.charm.phr.mphr.model.database.entity.Supplements
import com.charm.phr.mphr.ui.components.MDReminderStatusUpdateDialog
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil

class AllReminderListAdapter(val activity: ReminderActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var itemAllReminderDrugDetailsBinding: ItemAllReminderDrugDetailsBinding

    private var allRemindersList = arrayListOf<ReminderCustomPOJO>()

    private var allReminderListAdapterListener: AllReminderListAdapterListener? = null


    //region override methods
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        itemAllReminderDrugDetailsBinding = ItemAllReminderDrugDetailsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(itemAllReminderDrugDetailsBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val reminderPOJO = allRemindersList[position]
        if (holder is ViewHolder) {
            holder.bindViews(reminderPOJO, activity, allReminderListAdapterListener)

        }
    }

    override fun getItemCount(): Int {
        return allRemindersList.size
    }
    //endregion

    class ViewHolder(private val binder: ItemAllReminderDrugDetailsBinding) :
        RecyclerView.ViewHolder(binder.root) {
        fun bindViews(
            reminderPOJO: ReminderCustomPOJO,
            activity: ReminderActivity,
            allReminderListAdapterListener: AllReminderListAdapterListener?
        ) {
            when (reminderPOJO.reminderMap.first().type) {
                ModelUtil.medicationAddedAsMed -> {
                    binder.reminderDrugIcon.setImageResource(R.drawable.ic_tablet)
                    var medicationObject = Medications()
                    if (reminderPOJO.selectedModuleObject != null) {
                        medicationObject = reminderPOJO.selectedModuleObject as Medications
                    }
                    binder.reminderModuleItemNameTV.text = medicationObject.genericDrugName
                    if (medicationObject.directions.isNullOrBlank()) {
                        binder.reminderDrugDirectionTV.visibility = View.GONE
                    } else {
                        binder.reminderDrugDirectionTV.text = medicationObject.directions
                        binder.reminderDrugDirectionTV.visibility = View.VISIBLE
                    }
                }

                ModelUtil.moduleNameSupplement.replaceFirstChar { it.uppercase() } -> {
                    binder.reminderDrugIcon.setImageResource(R.drawable.ic_supplements)
                    var supplementObject = Supplements()
                    if (reminderPOJO.selectedModuleObject != null) {
                        supplementObject = reminderPOJO.selectedModuleObject as Supplements
                    }
                    binder.reminderModuleItemNameTV.text = supplementObject.genericDrugName
                    if (supplementObject.directions.isNullOrBlank()) {
                        binder.reminderDrugDirectionTV.visibility = View.GONE
                    } else {
                        binder.reminderDrugDirectionTV.text = supplementObject.directions
                        binder.reminderDrugDirectionTV.visibility = View.VISIBLE
                    }
                }
            }
            when (reminderPOJO.reminder?.reminderStatus) {
                ModelUtil.CompletedStatus -> {
                    binder.reminderStateTV.text =
                        binder.root.context.getString(R.string.completed_str)
                    binder.reminderStateTV.setTextColor(binder.root.context.getColor(R.color.passcodeBulletin))
                    binder.reminderStateTV.background = AppCompatResources.getDrawable(
                        binder.root.context,
                        R.drawable.round_corner_grey_drawable
                    )
                }

                ModelUtil.UpcomingStatus -> {
                    binder.reminderStateTV.text =
                        binder.root.context.getString(R.string.upcoming_str)
                    binder.reminderStateTV.setTextColor(binder.root.context.getColor(R.color.todayAppointmentSeparatorColor))
                    binder.reminderStateTV.background = AppCompatResources.getDrawable(
                        binder.root.context,
                        R.drawable.round_corner_blue_drawable
                    )
                }

                ModelUtil.ActiveStatus -> {
                    binder.reminderStateTV.text =
                        binder.root.context.getString(R.string.allergy_status_active_str)
                    binder.reminderStateTV.setTextColor(binder.root.context.getColor(R.color.activeMedicationShowMoreTxtColor))
                    binder.reminderStateTV.background = AppCompatResources.getDrawable(
                        binder.root.context,
                        R.drawable.round_corner_green_drawable
                    )
                }

                else -> {
                    binder.reminderStateTV.text =
                        binder.root.context.getString(R.string.prescription_status_inactive)
                    binder.reminderStateTV.setTextColor(binder.root.context.getColor(R.color.passcodeBulletin))
                    binder.reminderStateTV.background = AppCompatResources.getDrawable(
                        binder.root.context,
                        R.drawable.round_corner_grey_drawable
                    )
                }
            }
            binder.reminderStartDateTV.text =
                reminderPOJO.reminder?.startDate?.toString(ModelUtil.charmDisplayDateFormat())
            binder.reminderEndDateTV.text =
                reminderPOJO.reminder?.endDate?.toString(ModelUtil.charmDisplayDateFormat())
            val screenWidth = UIUtil.getScreenWidth(activity)
            val width =
                screenWidth - (2 * binder.root.context.resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))
            binder.root.setOnClickListener {
                binder.root.isEnabled = false
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
                        allReminderListAdapterListener?.onEditClick(
                            reminderPOJO,
                            drugName,
                            drugDirection,
                            startDate,
                            endDate
                        )
                        mDReminderStatusUpdateDialog.dismiss()
                        binder.root.isEnabled = true
                    }

                    override fun onSkipClick(reminderPOJO: ArrayList<ReminderCustomPOJO>) {
                        //UI not shown
                    }

                    override fun onTakenClick(
                        takenReminderPOJO: ArrayList<ReminderCustomPOJO>,
                        skipReminderPOJO: ArrayList<ReminderCustomPOJO>
                    ) {
                        //UI not shown
                    }

                    override fun onHistoryClick(
                        reminderPOJO: ReminderPOJO,
                        drugName: String,
                        drugDirection: String
                    ) {
                        allReminderListAdapterListener?.onHistoryClick(
                            reminderPOJO,
                            drugName,
                            drugDirection
                        )
                        mDReminderStatusUpdateDialog.dismiss()
                        binder.root.isEnabled = true
                    }

                    override fun onBackPress() {
                        //not required
                    }

                })
                mDReminderStatusUpdateDialog.setOnCancelListener {
                    //When you touch outside of dialog bounds,
                    //the dialog gets canceled and this method executes.
                    binder.root.isEnabled = true
                }
                mDReminderStatusUpdateDialog.setSource(arrayListOf(reminderPOJO))
                mDReminderStatusUpdateDialog.isFromSelectALl = false
                mDReminderStatusUpdateDialog.scheduledTime = binder.reminderStateTV.text.toString()
                mDReminderStatusUpdateDialog.show()
            }
        }
    }

    fun setAllRemindersList(allRemindersList: ArrayList<ReminderCustomPOJO>): Int {
        this.allRemindersList.clear()
        this.allRemindersList.addAll(allRemindersList)
        this.notifyDataSetChanged()
        return this.allRemindersList.size
    }

    fun setAllReminderListAdapterListener(listener: AllReminderListAdapterListener) {
        allReminderListAdapterListener = listener
    }

    interface AllReminderListAdapterListener {
        fun onEditClick(
            reminderPOJO: ReminderPOJO,
            drugName: String,
            drugDirection: String,
            startDate: String,
            endDate: String
        )

        fun onHistoryClick(reminderPOJO: ReminderPOJO, drugName: String, drugDirection: String)
    }
}