package com.charm.phr.mphr.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.res.stringResource
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemNewAppointmentListBinding
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.appointment.AppointmentMenuDetails
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Picasso


class UpcomingAppointmentListAdapter(private val appointments: List<Appointment>, val picasso: Picasso) :
    RecyclerView.Adapter<UpcomingAppointmentListAdapter.ViewHolder>() {

    private var listener: OnQuestionnaireSelectListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = ItemNewAppointmentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, picasso)
    }


    fun setOnClickListener(onClickListener: OnQuestionnaireSelectListener) {
        listener = onClickListener
    }


    override fun getItemCount(): Int {
        return appointments.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val appointment = appointments[position]

        holder.onBind(appointment, position, listener)

        holder.binder.appointmentListFillQuestLL.setOnClickListener {
            listener?.onQuestinnaireSelect(appointment)
        }

        holder.binder.itemAppointmentListMainLayout.setOnClickListener {
            listener?.onItemCardClick()
        }

    }

    interface OnQuestionnaireSelectListener {
        fun onQuestinnaireSelect(appointment: Appointment)
        fun onItemCardClick()
        fun onDelete(appointment: Appointment, childPosition: Int)
    }


    inner class ViewHolder(val binder: ItemNewAppointmentListBinding, val picasso: Picasso) : RecyclerView.ViewHolder(binder.root) {


        fun onBind(appointment: Appointment, position: Int, listener: OnQuestionnaireSelectListener?) {

            binder.appointmentListDateTv.text = ModelUtil.getDisplayDateForAppointment(appointment.appointmentDate)
            binder.appointmentListDayTv.visibility = View.VISIBLE
            binder.appointmentListDayTv.text = ModelUtil.getDisplayDayForAppointment(appointment.appointmentDate)
            binder.appointmentListMemberTxtView.text = UIUtil.formMemberName(appointment.member)
            binder.appointmentListProviderTxtView.text = appointment.facilityName
            binder.appointmentListTimeTv.text = appointment.appointmentDate?.toString(ModelUtil.charmDisplayTimeFormat)?.uppercase()

            if (appointment.timezone.isNullOrBlank()) {
                binder.appointmentListTimeZoneTv.visibility = View.GONE
            } else {
                binder.appointmentListTimeZoneTv.requestLayout()
                binder.appointmentListTimeZoneTv.visibility = View.VISIBLE
                binder.appointmentListTimeZoneTv.text = appointment.timezone
            }

            if (appointment.isQuestionnairePresent) {
                binder.appointmentListFillQuestLL.visibility = View.VISIBLE
                binder.appointmentListFillQuestCountTxtView.text = appointment.questionnaireCount.toString()
            } else {
                binder.appointmentListFillQuestLL.visibility = View.GONE
            }
            if (appointment.physicianRemark.isNullOrBlank()) {
                binder.appointmentReasonValTxtView.visibility = View.GONE
            } else {
                binder.appointmentReasonValTxtView.visibility = View.VISIBLE

                val reasonStr = itemView.context.getString(
                    R.string.two_string_template,
                    itemView.context.getString(R.string.instruction_str),
                    appointment.physicianRemark?.trim()
                )
                binder.appointmentReasonValTxtView.text = UIUtil.makeFirstWordFontMedium(itemView.context, reasonStr)
                binder.appointmentReasonValTxtView.maxLines = 3
                binder.appointmentReasonValTxtView.setOnClickListener {
                    binder.appointmentReasonValTxtView.maxLines = Integer.MAX_VALUE
                }

            }
            binder.appointmentListMeetingTypeTxtView.requestLayout()
            ModelUtil.renderMeetingTypeUI(binder.appointmentListMeetingTypeTxtView, binder.appointmentListMeetingTypeIV, appointment.appointmentType ?: "")
            val imgSize =
                itemView.resources.getDimensionPixelSize(R.dimen.message_detail_sent_member_image_size)
            ModelUtil.loadProfilePic(appointment, binder.appointmentProfilePic, binder.appointmentProfileDummyTV, imgSize, picasso)
            binder.appointmentMoreBtn.setContent {
                MPHRTheme {
                    AppointmentMenuDetails({
                        listener?.onDelete(appointment, position)
                    }, stringResource(R.string.cancel_appointment_str))
                }
            }
            if (appointments.size - 1 == position) {
                binder.appointmentListSeparator.visibility = View.GONE
            }
        }
    }
}