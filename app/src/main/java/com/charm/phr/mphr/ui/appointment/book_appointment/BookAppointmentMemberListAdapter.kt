package com.charm.phr.mphr.ui.appointment.book_appointment

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.LinearLayout
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemMemberListForMessageBinding
import com.charm.phr.mphr.databinding.ItemShareDocumentAddNewContactTemplateBinding
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil

class BookAppointmentMemberListAdapter : ArrayAdapter<Member> {


    constructor(context: Context, memberList: ArrayList<Member>) : super(context, 0, memberList) {
        this.memberList.clear()
        this.memberList.addAll(memberList)
    }

    val memberList: ArrayList<Member> = arrayListOf()

    val selectedMemberList: ArrayList<Member> = arrayListOf()

    private var memberSelectListener: MemberSelectListener? = null

    var memberFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {

            val filterResults = FilterResults()
            val suggestions = arrayListOf<Member>()

            Log.d("MDChipsSelectAE", "constraint -> ${constraint ?: ""}")


            if (constraint.isNullOrBlank()) {
//                if (memberList.size > 10) {
//                    suggestions.addAll(memberList.subList(0, 9))
//                } else {
                    suggestions.addAll(memberList)
//                }
            } else {
                if (!constraint.contains(":")) {
                    suggestions.addAll(memberList.filter {
                        val memberName = UIUtil.formMemberName(it)
                        memberName.contains(constraint, true) && !this@BookAppointmentMemberListAdapter.selectedMemberList.contains(it)
                    })

                    if (suggestions.isEmpty()) {
                        suggestions.add(Member(fullName = context.getString(R.string.full_name_str), memberId = context.getString(R.string.dummy_id_str)))
                    }
                }
            }

            Log.d("MDChipsSelectAE", "suggestions size-> ${suggestions.size}")


            filterResults.count = suggestions.size
            filterResults.values = suggestions

            return filterResults

        }

        override fun publishResults(p0: CharSequence?, results: FilterResults) {
            clear()
            if (results.values is MutableCollection<*>) {
                addAll(results.values as MutableCollection<out Member>)
            }

            notifyDataSetChanged()
            Log.d("MDChipsSelectAE", "result size-> $count")
        }

        override fun convertResultToString(resultValue: Any): CharSequence {
            val member = resultValue as Member


            return UIUtil.formMemberName(member) + if (member.memberId.isNullOrBlank()) {
                ""
            } else {
                ":${member.memberId}"
            }

        }

    }

    override fun getFilter(): Filter {
        return memberFilter
    }


    fun setMemberList(memberList: ArrayList<Member>) {
        this.memberList.clear()
        this.memberList.addAll(memberList)
    }

    fun setSelectedMemberList(selectedMemberList: ArrayList<Member>) {
        this.selectedMemberList.clear()
        this.selectedMemberList.addAll(selectedMemberList)
    }

    fun setMemberSelectListener(listener: MemberSelectListener) {
        memberSelectListener = listener
    }

    /*

        override fun getCount(): Int {
            return memberList.size
        }

        override fun getItem(position: Int): Member? {
            return memberList[position]
        }
    */


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val member = getItem(position)
        if (member?.memberId.equals(context.getString(R.string.dummy_id_str))) {
            val binding = ItemShareDocumentAddNewContactTemplateBinding.inflate(LayoutInflater.from(context), parent, false)
            binding.root.setTag(ModelUtil.documentShareNoMember)
            binding.addNewContactTV.setText(R.string.no_member_available)
            binding.addNewContactTV.setTextColor(context.getColor(R.color.black))
            return binding.root
        } else {

            val binding = if (convertView?.tag?.equals(ModelUtil.documentShareNoMember) == true) {
                ItemMemberListForMessageBinding.inflate(LayoutInflater.from(context), parent, false)
            } else {
                convertView?.let { ItemMemberListForMessageBinding.bind(it) } ?: ItemMemberListForMessageBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            }

            binding.memberListName.text = UIUtil.formMemberName(member)
            binding.memberListDummyTV.text = UIUtil.formMemberInitials(member?.fullName ?: "")
            if (member?.specialization.isNullOrBlank() || member?.specialization.equals("null", true)) {
                val layout = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 2f)
                binding.memberListName.layoutParams = layout
            } else {
                val layout = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1f)
                binding.memberListName.layoutParams = layout
                binding.memberListDetail.text = member?.specialization ?: ""
            }
            return binding.root
        }
    }


    interface MemberSelectListener {
        fun onMemberSelect(member: Member?)
    }

}