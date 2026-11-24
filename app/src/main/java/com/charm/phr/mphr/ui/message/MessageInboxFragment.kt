package com.charm.phr.mphr.ui.message


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentMessageInboxBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.ComposeMsgRestriction
import com.charm.phr.mphr.model.Messages
import com.charm.phr.mphr.ui.message.messageDetail.MessageDetailActivity
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.DisableMultiSelectListener
import com.charm.phr.mphr.util.MDJsonBuilder
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.OnMultipleSelectChangeListener
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.UIUtil
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 */
class MessageInboxFragment : DaggerFragment()  , DisableMultiSelectListener {

    val TAG = "MessageInboxFragment"

    lateinit var messageViewModel: MessageViewModel

    lateinit var binding : FragmentMessageInboxBinding

    @Inject
    lateinit var messageInboxListAdapter: MessageInboxListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMessageInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageViewModel = ViewModelProvider(requireActivity()).get(MessageViewModel::class.java)

        binding.messageInboxListRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.messageInboxListRV.adapter = messageInboxListAdapter

        binding.messageInboxListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)


        binding.messageInboxListNoDataTV.nodataTextViewHint.visibility = View.VISIBLE
        binding.messageInboxListNoDataTV.nodataTextViewHint.setText(R.string.message_add_hint)
        UIUtil.makePlusColorPrimary(binding.messageInboxListNoDataTV.nodataTextViewHint)

        messageInboxListAdapter.setOnMultipleSelectChangeListener(object : OnMultipleSelectChangeListener {
            override fun onMultiSelect(isMultiSelectEnabled: Boolean) {
                val activity = activity
                if (activity is MessageActivity){
                    activity.onMultiSelectChange(isMultiSelectEnabled)

                    binding.messageInboxListSwipeRefreshLayout.isEnabled = !isMultiSelectEnabled
                }
            }
        })

        messageInboxListAdapter.setMessageSelectListener(object : MessageActivity.MessageListener{
            override fun onMessageSelect(messages: Messages , itemView:View , position: Int) {


                if (messageViewModel.isComposeMsgRestrictionFetched) {

                    if (messages.messageRecipient?.status == ModelUtil.messageStatusUnSeen) {
                        val messagesActivity = activity
                        if (messagesActivity is MessageActivity) {
                            messagesActivity.updateBadge(false)
                        }
                    }

                    val intent =
                        Intent(this@MessageInboxFragment.context, MessageDetailActivity::class.java)
                    intent.putExtra(ModelUtil.messageIdIntent, messages.messageId)
                    intent.putExtra(ModelUtil.practiceIdIntent, messages.practiceId)
                    intent.putExtra(
                        ModelUtil.messageDateIntent,
                        messages.messageDate?.toString(ModelUtil.sqlTimeStampFormattor())
                    )

                    val filteredArray = ArrayList(messageViewModel.composeMsgRestrictionList.filter { it.practiceId == messages.practiceId })

                    if (filteredArray.isEmpty()){
                        filteredArray.add(ComposeMsgRestriction(practiceId = messages.practiceId ?: ""))
                    }

                    messageViewModel.selectedMessagePosition =  position
                    intent.putExtra(ModelUtil.messageReceipientIdIntent , messages.messageRecipient?.messageRecipientId)
                    intent.putExtra(ModelUtil.messageComposeMsgRestrictionListIntent , MDJsonBuilder.formComposeMsgRestrictionList(filteredArray))
                    activity?.startActivityForResult(intent, 2)
                    activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                else{
                    messageViewModel.message = messages
                    messageViewModel.isComposeDetailClicked = true
                    val messagesActivity =   activity
                    if (messagesActivity is MessageActivity){
                        messagesActivity.progressDialog.show()
                    }
                }
            }

            override fun onMessageDelete(messages: Messages  ,position: Int) {

                val messageActivity = activity
                if (messageActivity is MessageActivity){
                    messageActivity.deleteMessage(messages , position)
                }
            }
        })

        messageViewModel.messageInboxListLive.observe(viewLifecycleOwner , Observer {  resp ->

            if (resp != null) {


                if (resp is Pair<*, *>) {
                    val messagePairList = resp as Pair<ArrayList<Messages>, Boolean>

                    if (messagePairList.second) {

                        binding.messageInboxListProgress.root.visibility = View.GONE

                        if (binding.messageInboxListSwipeRefreshLayout.isRefreshing){
                            binding.messageInboxListSwipeRefreshLayout.isRefreshing = false
                        }

                        val size = messageInboxListAdapter.setMessages(messagePairList.first)
                        if (size == 0) {
                            binding.messageInboxListNoDataTV.root.visibility = View.VISIBLE
                            binding.messageInboxListRV.visibility = View.GONE
                        } else {
                            binding.messageInboxListNoDataTV.root.visibility = View.GONE
                            binding.messageInboxListRV.visibility = View.VISIBLE
                            checkToShowBgImage()
                        }
                    } else {

                        messageInboxListAdapter.removeInfiniteScroll()
                        messageInboxListAdapter.addMessages(messagePairList.first)
                    }
                }
                else if (resp is APIResponseError) {
                    if (!messageViewModel.isMessageInboxListFetched) {
                        binding.messageInboxListNoDataTV.root.visibility = View.GONE
                        binding.messageInboxListRV.visibility = View.GONE
                        binding.messageInboxListErrorPage.root.visibility = View.VISIBLE
                    }
                }

                messageViewModel.isMessageInboxFetchComplete = true
            }
        })


        if (!messageViewModel.isMessageInboxListFetched){
            messageViewModel.fetchMessageList(ModelUtil.messageTypeInbox , true)
        }

        binding.messageInboxListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, scrollX: Int, scrollY: Int) {
                val lastIndexInScreen = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()


                Log.d("messageIbListItemCount" , messageInboxListAdapter.itemCount.toString())
                Log.d("messageIbListComplete" , messageViewModel.isMessageInboxFetchComplete .toString())

                if ((lastIndexInScreen + 1) >=  messageInboxListAdapter.itemCount - 5 &&  messageInboxListAdapter.itemCount != 0 && messageViewModel.isMessageInboxFetchComplete && messageViewModel.messageInboxHasMore) {
                    Log.d("scroll", "now")

                    messageViewModel.isMessageInboxFetchComplete = false
                    messageInboxListAdapter.addInfiniteScroll()
                    messageViewModel.fetchMessageList(ModelUtil.messageTypeInbox, false)

                }


                if (messageViewModel.isComposeViewShown) {

                    try {

                        val dashboardActivity = activity as MessageActivity
                        if (scrollY > 0 && dashboardActivity.floatingActionButtonVisiblity() == View.VISIBLE) {
                            dashboardActivity.hideFab()
                        } else if (scrollY < 0 && dashboardActivity.floatingActionButtonVisiblity() != View.VISIBLE && RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_MESSAGES,requireContext())) {
                            dashboardActivity.showFab()
                        }

                        /*    handler.removeCallbacks(runnable)
                        handler.postDelayed(runnable, 100)*/
                    } catch (e: Exception) {

                    }
                }
            }
        })

        binding.messageInboxListSwipeRefreshLayout.setOnRefreshListener {
            binding.messageInboxListSwipeRefreshLayout.isRefreshing = true
            messageViewModel.fetchMessageList(ModelUtil.messageTypeInbox , true)
        }

        binding.messageInboxListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.messageInboxListErrorPage.root.visibility = View.GONE
            messageViewModel.isMessageInboxListFetched = false
            messageViewModel.isMessageInboxFetchComplete = false
            binding.messageInboxListProgress.root.visibility = View.VISIBLE
            messageViewModel.fetchMessageList(ModelUtil.messageTypeInbox , true)

            (activity as MessageActivity).fetchComposeRestriction()

        }



    }

    fun deleteMessage(position: Int){
        try {
            val size = messageInboxListAdapter.removeMessage(position)
            if (size == 0) {
                binding.messageInboxListNoDataTV.root.visibility = View.VISIBLE
                binding.messageInboxListRV.visibility = View.GONE
            } else {
                binding.messageInboxListNoDataTV.root.visibility = View.GONE
                binding.messageInboxListRV.visibility = View.VISIBLE
                checkToShowBgImage()
            }
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }


    override fun disableMultiSelect() {
        messageInboxListAdapter.disableMultiSelect()

        binding.messageInboxListSwipeRefreshLayout.isEnabled = true
    }


    private fun checkToShowBgImage() {


        if (!BuildUtil.isCustomApp) {

            val height = UIUtil.getScreenHeight(this.activity)

            val tv = TypedValue()
            var actionBarHeight = 0
            if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            }

            val viewHeight = ((height - (2 * actionBarHeight)) * 3) / 5

            val footerTreeObserver = binding.messageInboxListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        binding.messageInboxListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val medicationHeight = binding.messageInboxListRV.height ?: 0

                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $medicationHeight")

                        if (medicationHeight < viewHeight) {
                            binding.messageInboxBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.messageInboxBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        }
        else{
            binding.messageInboxBgLayout.visibility = View.GONE
        }
    }

}
