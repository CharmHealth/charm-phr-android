package com.charm.phr.mphr.ui.appointment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.res.stringResource
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemCardViewAppointmentListBinding
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Picasso

class UpcomingAppointmentListAdapter(val picasso: Picasso) : RecyclerView.Adapter<UpcomingAppointmentListAdapter.ViewHolder>() {

    private val appointments = arrayListOf<Appointment>()

    private var listener: OnClickListener? = null
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

        holder.onBind(appointment, listener, position)

        if (appointment.appointmentType == ModelUtil.appointmentTypeVideoCall) {
            holder.binder.appointmentListVideoCamLayout.setOnClickListener {
                listener?.onVideoCallBtnClickListener(appointment)
            }
        }

        holder.binder.appointmentListFillQuestLL.setOnClickListener {
            listener?.onQuestionnaireClickListener(appointment)
        }

        holder.binder.itemAppointmentListMainLayout.setOnClickListener {
            listener?.onCardClick()
        }

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

    inner class ViewHolder(val binder: ItemCardViewAppointmentListBinding, val picasso: Picasso) : RecyclerView.ViewHolder(binder.root) {

        fun onBind(appointment: Appointment, listener: OnClickListener?, position: Int) {
            binder.appointmentReasonValTxtView.visibility = View.GONE
            binder.appointmentListTimeZoneTv.requestLayout()
            binder.appointmentListTimeZoneTv.visibility = View.VISIBLE
            binder.appointmentListTimeZoneTv.text = appointment.timezone

            /*  val constraintSet = ConstraintSet()

              val mainConstraintLayout = itemView as ConstraintLayout
              constraintSet.clone(mainConstraintLayout)
              constraintSet.connect(appointmentListVideoCallLayout.id , ConstraintSet.BOTTOM , appointmentListSub3TxtView.id , ConstraintSet.BOTTOM)
              constraintSet.applyTo(mainConstraintLayout)*/

            binder.appointmentListDateTv.text = ModelUtil.getDisplayDateForAppointment(appointment.appointmentDate)
            binder.appointmentListDayTv.visibility = View.VISIBLE
            binder.appointmentListDayTv.text = ModelUtil.getDisplayDayForAppointment(appointment.appointmentDate)
            binder.appointmentListTimeTv.text = appointment.appointmentDate?.toString(ModelUtil.charmDisplayTimeFormat)?.uppercase()
            binder.appointmentListMemberTxtView.text = UIUtil.formMemberName(appointment.member)
            binder.appointmentListProviderTxtView.text = appointment.facilityName
            if (appointment.isQuestionnairePresent) {
                binder.appointmentListFillQuestLL.visibility = View.VISIBLE
                binder.appointmentListFillQuestCountTxtView.text = appointment.questionnaireCount.toString()
            } else {
                binder.appointmentListFillQuestLL.visibility = View.GONE
            }
            binder.appointmentListMeetingTypeTxtView.requestLayout()
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
                    AppointmentMenuDetails(
                        {
                            listener?.onDelete(appointment, position, 0, false)
                        }, stringResource(R.string.cancel_appointment_str)
                    )
                }
            }
        }
    }


    fun setVideoCallBtnClickListener(onClickListener: OnClickListener) {
        listener = onClickListener
    }

    interface OnClickListener {
        fun onVideoCallBtnClickListener(appointment: Appointment)
        fun onQuestionnaireClickListener(appointment: Appointment)
        fun onCardClick()
        fun onDelete(appointment: Appointment, position: Int, parentPosition: Int, isFromToday: Boolean)
    }
}