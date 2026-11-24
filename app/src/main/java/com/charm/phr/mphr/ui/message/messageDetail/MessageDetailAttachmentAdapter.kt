package com.charm.phr.mphr.ui.message.messageDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemMessageDetailAttachmentBinding
import com.charm.phr.mphr.model.MessagesAttachmentsMap
import com.charm.phr.mphr.util.ModelUtil

class MessageDetailAttachmentAdapter : RecyclerView.Adapter<MessageDetailAttachmentAdapter.ViewHolder>() {


    private val messageAttachmentMapList = arrayListOf<MessagesAttachmentsMap>()

    private var listener: MessageDetailAttachmentSelectListener? = null

    lateinit var binding: ItemMessageDetailAttachmentBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMessageDetailAttachmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = messageAttachmentMapList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val messagesAttachmentsMap = messageAttachmentMapList[position]
        holder.onBind(messagesAttachmentsMap)

        holder.itemView.setOnClickListener {
            listener?.onAttachmentSelect(messagesAttachmentsMap)
        }
    }


    fun setMessageAttachmentMapList(messagesAttachmentsMapList: ArrayList<MessagesAttachmentsMap>): Int{
        this.messageAttachmentMapList.clear()
        this.messageAttachmentMapList.addAll(messagesAttachmentsMapList)
        notifyDataSetChanged()

        return this.messageAttachmentMapList.size
    }


    fun setMessageDetailAttachmentSelectListener(attachmentSelectListener: MessageDetailAttachmentSelectListener){
        listener = attachmentSelectListener
    }




    inner class ViewHolder(private val binding: ItemMessageDetailAttachmentBinding) : RecyclerView.ViewHolder(binding.root) {

        private val messageDetailAttachmentIcon = binding.messageDetailAttachmentIcon
        private val messageDetailAttachmentNameTV = binding.messageDetailAttachmentNameTV


        fun onBind(messagesAttachmentsMap: MessagesAttachmentsMap){

            messageDetailAttachmentNameTV.text = messagesAttachmentsMap.attachmentName

            if (messagesAttachmentsMap.attachmentName?.endsWith("pdf" , true) == true){
                messageDetailAttachmentIcon.setImageResource(R.drawable.ic_pdf)
            }
            else if(messagesAttachmentsMap.attachmentName?.endsWith(ModelUtil.imageFileTypePNG , true) == true || messagesAttachmentsMap.attachmentName?.endsWith(
                    ModelUtil.imageFileTypeJPEG , true) == true || messagesAttachmentsMap.attachmentName?.endsWith(ModelUtil.imageFileTypeJPG , true) == true){
                messageDetailAttachmentIcon.setImageResource(R.drawable.ic_image)
            }
            else
            {
                messageDetailAttachmentIcon.setImageResource(R.drawable.ic_image)
            }
        }
    }

    interface MessageDetailAttachmentSelectListener {
        fun onAttachmentSelect(messagesAttachmentsMap: MessagesAttachmentsMap)
    }

}

