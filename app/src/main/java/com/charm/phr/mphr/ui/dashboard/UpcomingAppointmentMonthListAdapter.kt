package com.charm.phr.mphr.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.databinding.ItemUpcomingAppointmentMonthListBinding
import com.charm.phr.mphr.model.Appointment
import com.charm.phr.mphr.util.ModelUtil
import com.squareup.picasso.Picasso

class UpcomingAppointmentMonthListAdapter(val picasso: Picasso) : RecyclerView.Adapter<UpcomingAppointmentMonthListAdapter.ViewHolder>() {

    val appointmentList: ArrayList<ArrayList<Appointment>> = arrayListOf()
    private val viewPool = RecyclerView.RecycledViewPool()

    private var listener: com.charm.phr.mphr.ui.appointment.UpcomingAppointmentListAdapter.OnClickListener? = null


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemUpcomingAppointmentMonthListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return appointmentList.size
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val parent = appointmentList[position]
        holder.onBind(parent)
        holder.getAdapter().setOnClickListener(object : UpcomingAppointmentListAdapter.OnQuestionnaireSelectListener {
            override fun onQuestinnaireSelect(appointment: Appointment) {
                listener?.onQuestionnaireClickListener(appointment)
            }

            override fun onItemCardClick() {
                listener?.onCardClick()
            }

            override fun onDelete(appointment: Appointment, childPosition: Int) {
                listener?.onDelete(appointment, childPosition, holder.bindingAdapterPosition, false)
            }
        })

    }

    fun removeAppointment(parentPosition: Int, position: Int): Int {
        val appointmentSubList = appointmentList[parentPosition]
        appointmentSubList.removeAt(position)
        if (appointmentSubList.size == 0) {
            appointmentList.removeAt(parentPosition)
            notifyItemRemoved(parentPosition)
            notifyItemRangeChanged(parentPosition, appointmentList.size)
        } else {
            appointmentList[parentPosition] = appointmentSubList
            notifyItemChanged(parentPosition)
        }
        return appointmentList.size
    }

    fun setAppointmentList(appointmentList: ArrayList<Appointment>): Int {
        this.appointmentList.clear()

        if (appointmentList.isNotEmpty()) {

            var tempAppointmentList = arrayListOf<Appointment>()
            tempAppointmentList.add(appointmentList.first())

            var tempAppointment = appointmentList.first()

            for (index in 1 until appointmentList.size) {
                val currentAppointment = appointmentList[index]
                if (currentAppointment.appointmentDate?.monthOfYear == tempAppointment.appointmentDate?.monthOfYear) {
                    tempAppointmentList.add(currentAppointment)
                } else {
                    this.appointmentList.add(tempAppointmentList)
                    tempAppointmentList = arrayListOf()
                    tempAppointmentList.add(currentAppointment)
                    tempAppointment = currentAppointment
                }
            }

            if (tempAppointmentList.isNotEmpty()) {
                this.appointmentList.add(tempAppointmentList)
            }
        }

        notifyDataSetChanged()

        return appointmentList.size

    }


    fun setListener(onClickListener: com.charm.phr.mphr.ui.appointment.UpcomingAppointmentListAdapter.OnClickListener) {
        this.listener = onClickListener
    }


    inner class ViewHolder(val binding: ItemUpcomingAppointmentMonthListBinding) : RecyclerView.ViewHolder(binding.root) {
        private val upcomingAppointmentItemHeading: TextView = binding.upcomingAppointmentItemHeading
        private val upcomingAppointmentRV: RecyclerView = binding.upcomingAppointmentRV

        fun onBind(appointmentList: ArrayList<Appointment>) {
            upcomingAppointmentItemHeading.text = appointmentList.firstOrNull()?.appointmentDate?.toString(ModelUtil.monthAndYearDateFormat)
            val childLayoutManager = LinearLayoutManager(upcomingAppointmentRV.context, RecyclerView.VERTICAL, false)
            childLayoutManager.initialPrefetchItemCount = 4

            val upcomingAppointmentListAdapter = UpcomingAppointmentListAdapter(appointmentList, picasso)

            upcomingAppointmentListAdapter.setOnClickListener(object : UpcomingAppointmentListAdapter.OnQuestionnaireSelectListener {
                override fun onQuestinnaireSelect(appointment: Appointment) {

                }

                override fun onItemCardClick() {

                }

                override fun onDelete(appointment: Appointment, position: Int) {

                }
            })
            upcomingAppointmentRV.apply {
                layoutManager = childLayoutManager
                adapter = upcomingAppointmentListAdapter
                setRecycledViewPool(viewPool)
            }
        }

        fun getAdapter(): UpcomingAppointmentListAdapter {
            return upcomingAppointmentRV.adapter as UpcomingAppointmentListAdapter
        }
    }
}