package com.charm.phr.mphr.ui.appointment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.res.stringResource
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemCardViewAppointmentListBinding
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Picasso

class WaitListAppointmentListAdapter(val picasso: Picasso) : RecyclerView.Adapter<WaitListAppointmentListAdapter.ViewHolder>() {

    private val appointments = arrayListOf<Appointment>()

    private var appointmentDeleteListener: AppointmentActivity.AppointmentDeleteListener? = null


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = ItemCardViewAppointmentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, picasso)
    }

    override fun getItemCount(): Int {
        return appointments.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val appointment = appointments[position]

        holder.onBind(appointment)

    }

    fun setAppointments(appointmentList: ArrayList<Appointment>) {
        this.appointments.clear()
        this.appointments.addAll(appointmentList)
        notifyDataSetChanged()
    }

    fun addAppointments(appointmentList: ArrayList<Appointment>) {
        this.appointments.addAll(appointmentList)
        notifyDataSetChanged()
    }

    fun removeAppointment(position: Int): Int {
        appointments.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, appointments.size)
        return appointments.size
    }

    fun setAppointmentDeleteListener(listener: AppointmentActivity.AppointmentDeleteListener) {
        appointmentDeleteListener = listener
    }

    inner class ViewHolder(private val binder: ItemCardViewAppointmentListBinding, val picasso: Picasso) : RecyclerView.ViewHolder(binder.root) {

        fun onBind(appointment: Appointment) {
            binder.appointmentListTimeTv.visibility = View.VISIBLE

            if (appointment.datePreference == ModelUtil.appointmentDatePreferenceSpecificDate) {
                binder.appointmentListDateTv.text = ModelUtil.getDisplayDateForAppointment(appointment.appointmentDate)
                binder.appointmentListDayTv.visibility = View.VISIBLE
                binder.appointmentListDayTv.text = ModelUtil.getDisplayDayForAppointment(appointment.appointmentDate)
            }
            if (appointment.timePreference == ModelUtil.appointmentTimePreferenceTimeRange) {
                binder.appointmentListTimeTv.text = itemView.context.getString(
                    R.string.appointment_list_str_template,
                    appointment.appointmentStartTime?.toString(ModelUtil.charmDisplayTimeFormat)?.uppercase(),
                    appointment.appointmentEndTime?.toString(ModelUtil.charmDisplayTimeFormat)?.uppercase()
                )
            } else {
                binder.appointmentListTimeTv.setText(R.string.book_appointment_time_pref_any_str)
            }
            //No time zone in case of waiting list
//            if (appointment.timezone.isNullOrBlank()) {
//                binder.appointmentListTimeZoneTv.visibility = View.GONE
//            } else {
//                binder.appointmentListTimeZoneTv.requestLayout()
//                binder.appointmentListTimeZoneTv.text = appointment.timezone
//                binder.appointmentListTimeZoneTv.visibility = View.VISIBLE
//            }
            binder.appointmentListMemberTxtView.text = UIUtil.formMemberName(appointment.member)
            binder.appointmentListProviderTxtView.text = appointment.facilityName

            if (appointment.reason.isNullOrBlank()) {
                binder.appointmentReasonValTxtView.visibility = View.GONE
            } else {
                binder.appointmentReasonValTxtView.visibility = View.VISIBLE

                val reasonStr = itemView.context.getString(
                    R.string.two_string_template,
                    itemView.context.getString(R.string.requested_for_str),
                    appointment.reason?.trim()
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
            binder.appointmentListCalenderCL.background =
                ResourcesCompat.getDrawable(binder.root.resources, R.drawable.light_grey_round_corner_background, null)
            binder.appointmentMoreBtn.setContent {
                MPHRTheme {
                AppointmentMenuDetails(
                    cancelMenuString = stringResource(R.string.cancel_appointment_str),
                    onCancelAppointment = {
                        appointmentDeleteListener?.onDelete(appointment, position)
                    }
                )}
            }
        }
    }

}