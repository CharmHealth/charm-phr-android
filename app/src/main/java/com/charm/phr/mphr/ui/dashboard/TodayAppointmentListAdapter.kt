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
import com.charm.phr.mphr.ui.appointment.UpcomingAppointmentListAdapter
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Picasso
import java.util.Locale

class TodayAppointmentListAdapter(val picasso: Picasso) : RecyclerView.Adapter<TodayAppointmentListAdapter.ViewHolder>() {

    val appointments: ArrayList<Appointment> = arrayListOf()

    private var listener: UpcomingAppointmentListAdapter.OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNewAppointmentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, picasso)
    }

    override fun getItemCount(): Int = appointments.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.onBind(appointment, position, listener)
        holder.binder.appointmentListVideoCamLayout.setOnClickListener {
            listener?.onVideoCallBtnClickListener(appointment)
        }
        holder.binder.appointmentListFillQuestLL.setOnClickListener {
            listener?.onQuestionnaireClickListener(appointment)
        }
        holder.binder.itemAppointmentListMainLayout.setOnClickListener {
            listener?.onCardClick()
        }
    }


    fun setAppointmentList(appointments: ArrayList<Appointment>): Int {
        this.appointments.clear()
        this.appointments.addAll(appointments)
        notifyDataSetChanged()
        return this.appointments.size
    }

    fun removeAppointment(position: Int): Int {
        appointments.removeAt(position)
        if (appointments.size == position) {
            notifyItemChanged(position - 1)
        }
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, appointments.size)

        return appointments.size
    }

    fun setOnClickListener(onClickListener: UpcomingAppointmentListAdapter.OnClickListener) {
        listener = onClickListener
    }

    inner class ViewHolder(val binder: ItemNewAppointmentListBinding, val picasso: Picasso) : RecyclerView.ViewHolder(binder.root) {


        fun onBind(appointment: Appointment, position: Int, listener: UpcomingAppointmentListAdapter.OnClickListener?) {


            binder.appointmentListMemberTxtView.text = UIUtil.formMemberName(appointment.member)
            binder.appointmentListProviderTxtView.text = appointment.facilityName


            if (!appointment.timezone.isNullOrBlank()) {
                binder.appointmentListTimeZoneTv.requestLayout()
                binder.appointmentListTimeZoneTv.text = appointment.timezone
                binder.appointmentListTimeZoneTv.visibility = View.VISIBLE
            } else {
                binder.appointmentListTimeZoneTv.visibility = View.GONE
            }

            binder.appointmentListTimeTv.text = appointment.appointmentDate?.toString(ModelUtil.charmDisplayTimeFormat)?.uppercase(Locale.getDefault())
                ?: ""

            binder.appointmentListDateTv.text = ModelUtil.getDisplayDateForAppointment(appointment.appointmentDate)

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
            ModelUtil.setAppointmentView(
                appointment.appointmentType,
                appointment.teleMode,
                binder.appointmentListVideoCamLayout,
                appointment.appointmentDate,
                binder.appointmentListMeetingTypeTxtView,
                binder.appointmentListMeetingTypeIV, appointment.timezone ?: ""
            )
            val imgSize =
                itemView.resources.getDimensionPixelSize(R.dimen.message_detail_sent_member_image_size)
            ModelUtil.loadProfilePic(appointment, binder.appointmentProfilePic, binder.appointmentProfileDummyTV, imgSize, picasso)
            binder.appointmentMoreBtn.setContent {
                MPHRTheme {
                    AppointmentMenuDetails({
                        listener?.onDelete(appointment, position, 0,true)
                    }, stringResource(R.string.cancel_appointment_str))
                }
            }
            if (position == appointments.size - 1) {
                binder.appointmentListSeparator.visibility = View.GONE
            }
        }

    }
}