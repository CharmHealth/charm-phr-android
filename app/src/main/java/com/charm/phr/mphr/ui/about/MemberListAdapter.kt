package com.charm.phr.mphr.ui.about

import  android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemAboutMemberListBinding
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.util.UIUtil

class MemberListAdapter : RecyclerView.Adapter<MemberListAdapter.ViewHolder>() {

    private val memberList = arrayListOf<Member>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAboutMemberListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = memberList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = memberList[position]
        holder.onBind(member)
    }

    fun setMemberList(memberList: ArrayList<Member>): Int {
        this.memberList.clear()
        this.memberList.addAll(memberList)
        notifyDataSetChanged()
        return memberList.size
    }

    inner class ViewHolder(binding: ItemAboutMemberListBinding) : RecyclerView.ViewHolder(binding.root) {

        val memberListNameTV: TextView = binding.aboutMemberListNameTV
        val memberListSpecializationTV: TextView = binding.aboutMemberListSpecializationTV

        fun onBind(member: Member) {

            memberListNameTV.text = if (member.degree.isNullOrBlank()) {
                UIUtil.formMemberName(member)
            } else {
                itemView.context.getString(R.string.two_string_template, UIUtil.formMemberName(member), member.degree)
            }

            if (member.specialization.isNullOrBlank()) {
                memberListSpecializationTV.visibility = View.GONE
            } else {
                memberListSpecializationTV.visibility = View.VISIBLE
                memberListSpecializationTV.text = member.specialization
            }
        }
    }

}