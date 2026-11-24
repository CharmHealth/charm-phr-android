package com.charm.phr.mphr.ui.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.DialogCancelAppointmentBinding
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Picasso

class MDCancelAppointmentDialog(context: Context, val width: Int, val picasso: Picasso) : Dialog(context) {
    private lateinit var cancelAppointmentBinder: DialogCancelAppointmentBinding
    private var cancelAppointmentListener: CancelAppointmentListener? = null

    var appointment: Appointment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        MDBaseActivity.isPassCodeAllowed = false
        cancelAppointmentBinder = DialogCancelAppointmentBinding.inflate(layoutInflater)
        setContentView(cancelAppointmentBinder.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        cancelAppointmentBinder.cancelAppDialogLayout.layoutParams?.width = width
        cancelAppointmentBinder.cancelAppDialogLayout.requestLayout()
        renderUI()
        setOnClickListener()
    }

    private fun renderUI() {
        cancelAppointmentBinder.cancelAppDateTv.text = ModelUtil.getDisplayDateForAppointment(appointment?.appointmentDate)
        cancelAppointmentBinder.cancelAppDayTv.visibility = View.VISIBLE
        cancelAppointmentBinder.cancelAppDayTv.text = ModelUtil.getDisplayDayForAppointment(appointment?.appointmentDate)
        cancelAppointmentBinder.cancelAppMemberTxtView.text = UIUtil.formMemberName(appointment?.member)
        cancelAppointmentBinder.cancelAppProviderTxtView.text = appointment?.facilityName
        cancelAppointmentBinder.cancelAppTimeTv.text = appointment?.appointmentDate?.toString(ModelUtil.charmDisplayTimeFormat)?.uppercase()

        if (appointment?.timezone.isNullOrBlank()) {
            cancelAppointmentBinder.cancelAppTimeZoneTv.visibility = View.GONE
        } else {
            cancelAppointmentBinder.cancelAppTimeZoneTv.requestLayout()
            cancelAppointmentBinder.cancelAppTimeZoneTv.visibility = View.VISIBLE
            cancelAppointmentBinder.cancelAppTimeZoneTv.text = appointment?.timezone
        }

        cancelAppointmentBinder.cancelAppMeetingTypeTxtView.requestLayout()
        ModelUtil.renderMeetingTypeUI(
            cancelAppointmentBinder.cancelAppMeetingTypeTxtView,
            cancelAppointmentBinder.cancelAppMeetingTypeIV,
            appointment?.appointmentType ?: ""
        )
        val imgSize =
            context.resources.getDimensionPixelSize(R.dimen.message_detail_sent_member_image_size)
        appointment?.let {
            ModelUtil.loadProfilePic(
                it,
                cancelAppointmentBinder.cancelAppProfilePic,
                cancelAppointmentBinder.cancelAppProfileDummyTV,
                imgSize,
                picasso
            )
        }
    }

    fun setOnClickListener() {
        cancelAppointmentBinder.cancelAppButton.setOnClickListener {
            val reason = cancelAppointmentBinder.appointmentCancelReasonET.text.toString()
            if (reason.isNotBlank()) {
                if (reason.length < 6) {
                    UIUtil.showToast(
                        cancelAppointmentBinder.root.context,
                        context.getString(R.string.error_valid_character_reason_cancel_appointment),
                        Toast.LENGTH_SHORT
                    )
                } else {
                    cancelAppointmentListener?.onCancelAppointmentClick(reason)
                    dismiss()
                }
            } else {
                UIUtil.showToast(cancelAppointmentBinder.root.context, context.getString(R.string.error_empty_reason_cancel_appointment), Toast.LENGTH_SHORT)
            }
        }
    }

    fun setCancelAppointmentListener(listener: CancelAppointmentListener) {
        cancelAppointmentListener = listener
    }

    interface CancelAppointmentListener {
        fun onCancelAppointmentClick(reason: String)
    }
}