package com.charm.phr.mphr.ui.support

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.databinding.ItemSupportAttachmentBinding
import com.charm.phr.mphr.model.SupportDocument
import com.charm.phr.mphr.util.FileUtil

class SupportAttachmentAdapter : RecyclerView.Adapter<SupportAttachmentAdapter.ViewHolder>() {

    interface SupportAttachmentListener {
        fun onAttachmentSelect(supportAttachment: SupportDocument)
        fun onAttachmentDelete(position: Int)
    }

    private var supportAttachmentList = arrayListOf<SupportDocument>()
    private var listener: SupportAttachmentListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {
        val supportAttachmentBinding = ItemSupportAttachmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(supportAttachmentBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val supportAttachment = supportAttachmentList[position]
        holder.onBind(supportAttachment)
        holder.binder.root.setOnClickListener {
            listener?.onAttachmentSelect(supportAttachment)
        }
        holder.binder.supportCloseBtn.setOnClickListener {
            listener?.onAttachmentDelete(position)
        }
    }

    override fun getItemCount(): Int {
        return supportAttachmentList.size
    }

    fun setSupportAttachmentListener(supportAttachmentListener: SupportAttachmentListener) {
        listener = supportAttachmentListener
    }

    fun setSupportAttachmentList(supportAttachmentList: ArrayList<SupportDocument>) {
        this.supportAttachmentList.clear()
        this.supportAttachmentList.addAll(supportAttachmentList)
        notifyDataSetChanged()
    }

    fun addItemToSupportAttachmentList(supportAttachment: SupportDocument) {
        this.supportAttachmentList.add(supportAttachment)
        notifyDataSetChanged()
    }

    fun deleteItemFromSupportAttachmentList(position: Int) {
        supportAttachmentList.removeAt(position)
        notifyDataSetChanged()
    }

    class ViewHolder(val binder: ItemSupportAttachmentBinding) : RecyclerView.ViewHolder(binder.root) {
        fun onBind(supportAttachment: SupportDocument) {
            binder.supportAttachmentNameTV.text = supportAttachment.fileName
            binder.supportAttachmentIcon.setImageResource(FileUtil.getIconForFileType(supportAttachment.fileType))
        }
    }

}