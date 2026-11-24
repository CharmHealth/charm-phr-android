package com.charm.phr.mphr.ui.announcement

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemAnnoucementListBinding
import com.charm.phr.mphr.model.Announcements
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.ModelUtil

class AnnouncementListAdapter(val authManager: AuthManager) : RecyclerView.Adapter<AnnouncementListAdapter.ViewHolder>() {

    private val announcementList = arrayListOf<Announcements>()

    private var listener: AnnouncementListSelectListener? = null

    private lateinit var binding: ItemAnnoucementListBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemAnnoucementListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = announcementList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val announcements = announcementList[position]
        holder.bindView(announcements)
        holder.itemView.setOnClickListener {
            listener?.onAnnouncementListClick(announcements)
            if (!announcements.isRead) {
                announcements.isRead = true
                holder.changeItemToRead()
            }
        }
    }

    fun setAnnouncementList(announcementList: ArrayList<Announcements>): Int {
        this.announcementList.clear()
        this.announcementList.addAll(announcementList)
        notifyDataSetChanged()

        return announcementList.size
    }

    inner class ViewHolder(binding: ItemAnnoucementListBinding) : RecyclerView.ViewHolder(binding.root) {

        private val announcementListDate: TextView = binding.announcementListDateTV
        private val announcementListSubject: TextView = binding.announcementListSubjectTV

        fun bindView(announcements: Announcements) {

            announcementListDate.text = announcements.time?.toString(ModelUtil.charmDisplayDateFormat())
            announcementListSubject.text = announcements.subject
            if (announcements.isRead) {
                changeItemToRead()
            } else {
                changeItemToNotRead()
            }
        }

        private fun changeItemToNotRead() {
            TextViewCompat.setTextAppearance(announcementListSubject, R.style.itemIsNotRead)

        }

        fun changeItemToRead() {
            TextViewCompat.setTextAppearance(announcementListSubject, R.style.itemIsRead)
        }

    }

    fun setAnnouncementListSelectListener(announcementListSelectListener: AnnouncementListSelectListener) {
        listener = announcementListSelectListener
    }

    interface AnnouncementListSelectListener {
        fun onAnnouncementListClick(announcements: Announcements)
    }

}