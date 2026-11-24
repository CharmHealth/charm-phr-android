package com.charm.phr.mphr.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemRecentMessageBinding
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.ui.message.MessageActivity
import com.charm.phr.mphr.util.APIList
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class RecentMessageListAdapter(val picasso: Picasso, val authManager: AuthManager) :
    RecyclerView.Adapter<RecentMessageListAdapter.ViewHolder>() {

    private val messageList: ArrayList<Messages> = arrayListOf()

    private var messageListener: MessageActivity.MessageListener? = null

    private var patient: Patient? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, picasso)
    }

    override fun getItemCount(): Int {
        val messageSize = messageList.size
        return if (messageSize > ModelUtil.dashboardMaxMessageCount) {
            ModelUtil.dashboardMaxMessageCount
        } else {
            messageSize
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messageList[position]
        holder.onBind(message)

        holder.itemView.setOnClickListener {
            messageListener?.onMessageSelect(message , holder.itemView , position)
        }
    }


    fun setMessageList(messageList: List<Messages>): Int {
        this.messageList.clear()
        this.messageList.addAll(messageList)
        notifyDataSetChanged()
        patient = authManager.selectedPatientLive.value
        return this.messageList.size
    }

    fun setMessageSelectListener(listener: MessageActivity.MessageListener){
        messageListener = listener
    }

    inner class ViewHolder(val binding: ItemRecentMessageBinding, val picasso: Picasso) : RecyclerView.ViewHolder(binding.root) {

    val memberNameTV = binding.recentMessageMemberNameTV
    val subjectTV = binding.recentMessageSubjectTV
    val dateTV = binding.recentMessageDateTV
    val memberImg = binding.recentMessageProfilePic
    val memberDummyTV = binding.recentMessageProfileDummyTV

        fun onBind(messages: Messages) {
            memberNameTV.text = messages.messageRecipient?.fromName

            dateTV.text = ModelUtil.getDisplayDateForMessage(messages.messageDate)

            if (messages.subject.isNullOrBlank()) {
                subjectTV.text = itemView.context.getString(R.string.message_list_no_subject_str)
            } else {
                subjectTV.text = messages.subject
            }

            if (messages.messageRecipient?.fromId == patient?.practicePatientId) {

                val initials = UIUtil.formPatientInitials(patient ?: Patient())

                memberImg.visibility = View.INVISIBLE
                memberDummyTV.visibility = View.VISIBLE
                memberDummyTV.text = initials

                if (patient!!.isPhotoAvailableOnline && authManager.isUserOnline()) {
                    val picLoader =
                        picasso.load(APIList.getPatientImageURL(patient?.practicePatientId))
                    val imgSize =
                        itemView.resources.getDimensionPixelSize(R.dimen.message_detail_sent_member_image_size)
                    picLoader.resize(imgSize, imgSize).into(memberImg, object :
                        Callback {
                        override fun onSuccess() {
                            memberDummyTV.visibility = View.GONE
                            memberImg.visibility = View.VISIBLE
                        }

                        override fun onError(e: Exception?) {
                        }
                    })
                }

            } else {
                val memberName = messages.messageRecipient?.fromName ?: ""
                val memInitials = UIUtil.formMemberInitials(memberName)
                memberDummyTV.visibility = View.VISIBLE
                memberDummyTV.text = memInitials
                memberImg.visibility = View.INVISIBLE

                val picLoader = picasso.load(
                    APIList.getMemberImageURL(
                        messages.messageRecipient?.fromId,
                        messages.practiceId
                    )
                )
                val imgSize =
                    itemView.resources.getDimensionPixelSize(R.dimen.message_detail_sent_member_image_size)

                picLoader.resize(imgSize, imgSize).into(memberImg, object :
                    Callback {
                    override fun onSuccess() {
                        memberImg.visibility = View.VISIBLE
                        memberDummyTV.visibility = View.GONE
                    }

                    override fun onError(e: Exception?) {
                    }
                })
            }
        }

    }


}