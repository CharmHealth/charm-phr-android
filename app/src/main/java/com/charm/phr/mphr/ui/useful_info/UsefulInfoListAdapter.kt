package com.charm.phr.mphr.ui.useful_info

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.databinding.ItemUsefulInfoListBinding
import com.charm.phr.mphr.model.UsefulInfo
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.ModelUtil

class UsefulInfoListAdapter(val authManager: AuthManager)  : RecyclerView.Adapter<UsefulInfoListAdapter.ViewHolder>() {

    private val usefulInfoList = arrayListOf<UsefulInfo>()

    private var listener: UsefulInfoListSelectListener ? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUsefulInfoListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int  = usefulInfoList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val announcements = usefulInfoList[position]

        holder.bindView(announcements)

        holder.itemView.setOnClickListener {
            listener?.onUsefulInfoListClick(announcements)
        }
    }

    fun setUsefulInfoList(usefulInfoList: ArrayList<UsefulInfo>): Int{
        this.usefulInfoList.clear()
        this.usefulInfoList.addAll(usefulInfoList)
        notifyDataSetChanged()

        return usefulInfoList.size
    }


    inner class ViewHolder(private val binding: ItemUsefulInfoListBinding): RecyclerView.ViewHolder(binding.root) {

        private val usefulInfoListDate: TextView = binding.usefulInfoListDateTV
        private val usefulInfoListCategory: TextView = binding.usefulInfoListCategoryTV

        fun bindView(usefulInfo: UsefulInfo){

            usefulInfoListDate.text = usefulInfo.time?.toString(ModelUtil.charmDisplayDateFormat())
            usefulInfoListCategory.text = usefulInfo.category
        }

    }

    fun setUsefulInfoListSelectListener(usefulInfoListSelectListener: UsefulInfoListSelectListener){
        listener = usefulInfoListSelectListener
    }

    interface UsefulInfoListSelectListener {
        fun onUsefulInfoListClick(usefulInfo: UsefulInfo)
    }

}