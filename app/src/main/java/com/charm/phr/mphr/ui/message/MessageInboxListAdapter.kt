package com.charm.phr.mphr.ui.message

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemMessageListBinding
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.util.InfiniteScrollViewHolder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.OnMultipleSelectChangeListener

class MessageInboxListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messageList:ArrayList<Messages?> = arrayListOf()

    private var messageListener: MessageActivity.MessageListener? = null
    private var messageMultiSelectListener: OnMultipleSelectChangeListener? = null

    private var isMultiSelectEnabled = false

    private val viewTypeMessage = 1
    private val viewTypeScroll = 2


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder =if (viewType == viewTypeMessage) {
            val binding = ItemMessageListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ViewHolder(binding)
        } else {
            InfiniteScrollViewHolder.getNewInstance(parent)
        }
        return viewHolder
    }

    override fun getItemCount(): Int = messageList.size

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position] != null){
            viewTypeMessage
        }
        else {
            viewTypeScroll
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val messages = messageList[position]

        if (messages != null && holder is ViewHolder) {

            holder.onBind(messages)
            holder.itemView.setOnLongClickListener {

                /*if (!BuildUtil.isCustomApp) {
                    if (!isMultiSelectEnabled) {
                        isMultiSelectEnabled = true
                        messageMultiSelectListener?.onMultiSelect(true)
                        messages.isSelected = true
                        holder.changeBgToSelected()
                    } else {
                        if (!messages.isSelected) {
                            messages.isSelected = true
                            holder.changeBgToSelected()
                        } else {
                            messages.isSelected = true
                            holder.changeBgToSelected()
                        }
                    }
                }*/

                messageListener?.onMessageDelete(messages , position)

                return@setOnLongClickListener true
            }


            holder.itemView.setOnClickListener {
               /* if (isMultiSelectEnabled) {
                    if (messages.isSelected) {
                        messages.isSelected = false
                        holder.changeBgToUnSelected()
                        checkAllIsUnchecked()
                    } else {
                        messages.isSelected = true
                        holder.changeBgToSelected()
                    }
                } else {*/

                    messageListener?.onMessageSelect(messages , holder.itemView , position)

                    if (messages.messageRecipient?.status == ModelUtil.messageStatusUnSeen) {
                        messages.messageRecipient?.status = ModelUtil.messageStatusSeen
                        holder.changeItemToRead()
                    }
                //}
            }
        }
    }

    fun setMessages(messageList: ArrayList<Messages>): Int{
        this.messageList.clear()
        this.messageList.addAll(messageList)
        this.notifyDataSetChanged()
        return this.messageList.size
    }

    fun addMessages(messageList: ArrayList<Messages>){
        this.messageList.addAll(messageList)
        this.notifyDataSetChanged()
    }

    fun addInfiniteScroll(){
        messageList.add(null)
        notifyItemInserted(messageList.size - 1)
    }

    fun removeInfiniteScroll() {
        messageList.removeAt(messageList.size - 1)
        val scrollPosition =  messageList.size
        notifyItemRemoved(scrollPosition)
    }

    fun removeMessage(position: Int): Int{
        messageList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, messageList.size);

        return  messageList.size
    }

    private fun checkAllIsUnchecked(){

        var atleastOneIsSelected = false
        for(messages in messageList){
            if (messages?.isSelected == true){
                atleastOneIsSelected = true
                break
            }
        }

        if (!atleastOneIsSelected){
            isMultiSelectEnabled = false
            messageMultiSelectListener?.onMultiSelect(false)
        }
    }

    fun disableMultiSelect(){
        isMultiSelectEnabled = false
        messageList.forEach{message->
            message?.isSelected = false
        }
        notifyDataSetChanged()
    }

    fun setMessageSelectListener(listener: MessageActivity.MessageListener){
        messageListener = listener
    }

    fun setOnMultipleSelectChangeListener(listener: OnMultipleSelectChangeListener){
        messageMultiSelectListener = listener
    }

    inner class ViewHolder(private val binding: ItemMessageListBinding): RecyclerView.ViewHolder(binding.root) {

        val messageDateTV: TextView = binding.messageListDateTV
        val messageFromSpaceTV: TextView = binding.messageListFromOrToTV
        val messageSubjectTV: TextView = binding.messageListSubjectTV
        val messageAttachmentIcon: ImageView = binding.messageListAttachmentIcon

        val messageConstraintLayout: ConstraintLayout = binding.messageListConstrainLayout


        private val handler = Handler(Looper.getMainLooper())
        private val runnable = Runnable {
            messageConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)
        }

        fun onBind(messages: Messages){


            messageDateTV.text = ModelUtil.getDisplayDateForMessage(messages.messageDate)

            if (messages.subject.isNullOrBlank()){
                messageSubjectTV.text = itemView.context.getString(R.string.message_list_no_subject_str)
            }
            else{
                messageSubjectTV.text = messages.subject
            }

            messageFromSpaceTV.text = messages.messageRecipient?.fromName
            if (messages.messageRecipient?.status == ModelUtil.messageStatusUnSeen){
                changeItemToNotRead()
            }
            else{
                changeItemToRead()
            }

            if (messages.isSelected){
                changeBgToSelected()
            }
            else{
                changeBgToUnSelected()
            }

            if (messages.hasAttachments == true){
                messageAttachmentIcon.visibility = View.VISIBLE
            }
            else{
                messageAttachmentIcon.visibility = View.GONE
            }
        }

        fun changeBgToSelected(){

            handler.removeCallbacks(runnable)
            messageConstraintLayout.setBackgroundResource( R.color.cardSelectedBgColor)
        }

        fun changeBgToUnSelected(){
            messageConstraintLayout.setBackgroundResource(android.R.color.white)
            handler.postDelayed(runnable , ModelUtil.delayedTimingForRippleChange)
        }

        private fun changeItemToNotRead(){
            TextViewCompat.setTextAppearance(messageSubjectTV , R.style.itemIsNotRead)
            TextViewCompat.setTextAppearance( messageDateTV , R.style.itemIsNotRead)
            TextViewCompat.setTextAppearance( messageFromSpaceTV , R.style.itemIsNotRead)

        }

        fun changeItemToRead(){
            TextViewCompat.setTextAppearance(messageSubjectTV , R.style.itemIsRead)
            TextViewCompat.setTextAppearance( messageDateTV , R.style.itemIsRead)
            TextViewCompat.setTextAppearance( messageFromSpaceTV , R.style.itemIsRead)
        }

    }

}

