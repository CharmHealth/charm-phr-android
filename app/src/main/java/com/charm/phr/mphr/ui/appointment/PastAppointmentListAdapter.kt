package com.charm.phr.mphr.ui.appointment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemCardViewAppointmentListBinding
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Picasso
import org.joda.time.DateTime

class PastAppointmentListAdapter(val picasso: Picasso) : RecyclerView.Adapter<PastAppointmentListAdapter.ViewHolder>() {

    private val appointments = arrayListOf<Appointment>()

    private var currentDate = DateTime.now()

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

        holder.onBind(appointment, currentDate, (position == (appointments.size - 1)))

    }

    fun setAppointments(appointmentList: ArrayList<Appointment>) {
        this.appointments.clear()
        this.appointments.addAll(appointmentList)
        notifyDataSetChanged()
    }

    fun addAppointments(appointmentList: ArrayList<Appointment>) {
        this.appointments.addAll(appointmentList)
        currentDate = DateTime.now()
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binder: ItemCardViewAppointmentListBinding, val picasso: Picasso) : RecyclerView.ViewHolder(binder.root) {


        fun onBind(appointment: Appointment, currentDate: DateTime, isLastElement: Boolean) {
            binder.appointmentListDateTv.text = ModelUtil.getDisplayDateForAppointment(appointment.appointmentDate)
            binder.appointmentListDayTv.visibility = View.VISIBLE
            binder.appointmentListDayTv.text = ModelUtil.getDisplayDayForAppointment(appointment.appointmentDate)
            binder.appointmentListTimeTv.text = appointment.appointmentDate?.toString(ModelUtil.charmDisplayTimeFormat)?.uppercase()
            binder.appointmentListMemberTxtView.text = UIUtil.formMemberName(appointment.member)
            binder.appointmentListProviderTxtView.text = appointment.facilityName
            if (appointment.timezone.isNullOrBlank()) {
                binder.appointmentListTimeZoneTv.visibility = View.GONE
            } else {
                binder.appointmentListTimeZoneTv.requestLayout()
                binder.appointmentListTimeZoneTv.text = appointment.timezone
                binder.appointmentListTimeZoneTv.visibility = View.VISIBLE
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
            binder.appointmentListCalenderCL.background =
                ResourcesCompat.getDrawable(binder.root.resources, R.drawable.light_grey_round_corner_background, null)
            binder.appointmentListMeetingTypeTxtView.requestLayout()
            ModelUtil.renderMeetingTypeUI(binder.appointmentListMeetingTypeTxtView, binder.appointmentListMeetingTypeIV, appointment.appointmentType ?: "")
            val imgSize =
                itemView.resources.getDimensionPixelSize(R.dimen.message_detail_sent_member_image_size)
            ModelUtil.loadProfilePic(appointment, binder.appointmentProfilePic, binder.appointmentProfileDummyTV, imgSize, picasso)
            binder.appointmentMoreBtn.visibility = View.GONE
        }
    }
}