package com.charm.phr.mphr.ui.message.compose

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemMessageDetailAttachmentBinding
import com.charm.phr.mphr.model.MessagesAttachmentsMap
import com.charm.phr.mphr.util.ModelUtil

class ComposeMessageAttachmentAdapter : RecyclerView.Adapter<ComposeMessageAttachmentAdapter.ViewHolder>() {


    private val messageAttachmentMapList = arrayListOf<MessagesAttachmentsMap>()

    private var listener: ComposeMessageAttachmentListener? = null

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

        holder.messageDetailCloseBtn.setOnClickListener {
            listener?.onAttachmentDelete(position)
        }
    }


    fun setMessageAttachmentMapList(messagesAttachmentsMapList: ArrayList<MessagesAttachmentsMap>): Int{
        this.messageAttachmentMapList.clear()
        this.messageAttachmentMapList.addAll(messagesAttachmentsMapList)
        notifyDataSetChanged()

        return this.messageAttachmentMapList.size
    }



    fun addMessageAttachmentMap(messagesAttachmentsMap: MessagesAttachmentsMap): Int{
        this.messageAttachmentMapList.add(messagesAttachmentsMap)
        notifyDataSetChanged()

        return this.messageAttachmentMapList.size
    }

    fun deleteMessageAttachmentPosition(position: Int):Int{

        messageAttachmentMapList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, messageAttachmentMapList.size);

        return  messageAttachmentMapList.size
    }

    fun getMessageAttachmentList():ArrayList<MessagesAttachmentsMap>{
        return  messageAttachmentMapList
    }


    fun setComposeMessageAttachmentSelectListener(attachmentSelectListener: ComposeMessageAttachmentListener){
        listener = attachmentSelectListener
    }




    inner class ViewHolder(private val binding: ItemMessageDetailAttachmentBinding) : RecyclerView.ViewHolder(binding.root) {

        private val messageDetailAttachmentIcon = binding.messageDetailAttachmentIcon
        private val messageDetailAttachmentNameTV = binding.messageDetailAttachmentNameTV
        val messageDetailCloseBtn = binding.messageDetailCloseBtn


        fun onBind(messagesAttachmentsMap: MessagesAttachmentsMap){

            messageDetailAttachmentNameTV.text = messagesAttachmentsMap.attachmentName

            if (messagesAttachmentsMap.attachmentName?.endsWith("pdf" , true) == true){
                messageDetailAttachmentIcon.setImageResource(R.drawable.ic_pdf)
            }
            else if(messagesAttachmentsMap.attachmentName?.endsWith(ModelUtil.imageFileTypePNG , true) == true || messagesAttachmentsMap.attachmentName?.endsWith(ModelUtil.imageFileTypeJPEG , true) == true || messagesAttachmentsMap.attachmentName?.endsWith(ModelUtil.imageFileTypeJPG , true) == true){
                messageDetailAttachmentIcon.setImageResource(R.drawable.ic_image)
            }
            else
            {
                messageDetailAttachmentIcon.setImageResource(R.drawable.ic_image)
            }

            messageDetailCloseBtn.visibility = View.VISIBLE



        }
    }

    interface ComposeMessageAttachmentListener {
        fun onAttachmentSelect(messagesAttachmentsMap: MessagesAttachmentsMap)

        fun onAttachmentDelete(position: Int)
    }

}
