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
import com.charm.phr.mphr.databinding.FragmentMessageSentBinding
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
class MessageSentFragment : DaggerFragment(), DisableMultiSelectListener {

    val TAG = "MessageSentFragment"

    lateinit var messageViewModel: MessageViewModel

    @Inject
    lateinit var messageSentListAdapter: MessageSentListAdapter

    private var _binding: FragmentMessageSentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment using ViewBinding
        _binding = FragmentMessageSentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageViewModel = ViewModelProvider(requireActivity()).get(MessageViewModel::class.java)

        binding.messageSentListRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.messageSentListRV.adapter = messageSentListAdapter

        binding.messageSentListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        binding.messageSentListNoDataTV.nodataTextViewHint.visibility = View.VISIBLE
        binding.messageSentListNoDataTV.nodataTextViewHint.setText(R.string.message_add_hint)
        UIUtil.makePlusColorPrimary(binding.messageSentListNoDataTV.nodataTextViewHint)


//        messageSentListAdapter.setMessageSelectListener(object :MessageActivity.MessageSelectListener{
//            override fun onInvoiceSelect(phrBill: PHRBills) {
//                if (!phrBill.isStatement) {
//                    val intent = Intent(context, BillingDetailActivity::class.java)
//                    intent.putExtra(ModelUtil.billingType, ModelUtil.billingTypeInvoice)
//                    intent.putExtra(ModelUtil.phrBillId, phrBill.phrBillId)
//                    startActivity(intent)
//                }
//            }
//        })

        messageSentListAdapter.setOnMultipleSelectChangeListener(object : OnMultipleSelectChangeListener {
            override fun onMultiSelect(isMultiSelectEnabled: Boolean) {
                val activity = activity
                if (activity is MessageActivity) {
                    activity.onMultiSelectChange(isMultiSelectEnabled)

                    binding.messageSentListSwipeRefreshLayout.isEnabled = !isMultiSelectEnabled
                }
            }
        })

        messageSentListAdapter.setMessageSelectListener(object : MessageActivity.MessageListener {
            override fun onMessageSelect(messages: Messages, itemView: View, position: Int) {

                if (messageViewModel.isComposeMsgRestrictionFetched) {

                    val intent =
                        Intent(this@MessageSentFragment.context, MessageDetailActivity::class.java)
                    intent.putExtra(ModelUtil.messageIdIntent, messages.messageId)
                    intent.putExtra(ModelUtil.practiceIdIntent, messages.practiceId)
                    intent.putExtra(
                        ModelUtil.messageDateIntent,
                        messages.messageDate?.toString(ModelUtil.sqlTimeStampFormattor())
                    )

                    val filteredArray = ArrayList(messageViewModel.composeMsgRestrictionList.filter { it.practiceId == messages.practiceId })

                    if (filteredArray.isEmpty()) {
                        filteredArray.add(ComposeMsgRestriction(practiceId = messages.practiceId ?: ""))
                    }
                    messageViewModel.selectedMessagePosition = position
                    intent.putExtra(ModelUtil.messageReceipientIdIntent, messages.messageRecipient?.messageRecipientId)

                    intent.putExtra(ModelUtil.messageComposeMsgRestrictionListIntent, MDJsonBuilder.formComposeMsgRestrictionList(filteredArray))
                    activity?.startActivityForResult(intent, 2)
                    activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    messageViewModel.message = messages
                    messageViewModel.isComposeDetailClicked = true
                    val messagesActivity = activity

                    if (messagesActivity is MessageActivity) {
                        messagesActivity.progressDialog.show()
                    }
                }
            }

            override fun onMessageDelete(messages: Messages, position: Int) {
                val messageActivity = activity
                if (messageActivity is MessageActivity) {
                    messageActivity.deleteMessage(messages, position)
                }
            }
        })

        messageViewModel.messageSentListLive.observe(viewLifecycleOwner, Observer { resp ->

            if (resp != null) {


                if (resp is Pair<*, *>) {
                    val messagePairList = resp as Pair<ArrayList<Messages>, Boolean>

                    if (messagePairList.second) {

                        binding.messageSentListProgress.root.visibility = View.GONE

                        if (binding.messageSentListSwipeRefreshLayout.isRefreshing) {
                            binding.messageSentListSwipeRefreshLayout.isRefreshing = false
                        }

                        val size = messageSentListAdapter.setMessages(messagePairList.first)
                        if (size == 0) {
                            binding.messageSentListNoDataTV.root.visibility = View.VISIBLE
                            binding.messageSentListRV.visibility = View.GONE
                        } else {
                            binding.messageSentListNoDataTV.root.visibility = View.GONE
                            binding.messageSentListRV.visibility = View.VISIBLE
                            checkToShowBgImage()
                        }
                    } else {
                        messageSentListAdapter.removeInfiniteScroll()
                        messageSentListAdapter.addMessages(messagePairList.first)
                    }
                } else if (resp is APIResponseError) {
                    if (!messageViewModel.isMessageSentListFetched) {
                        binding.messageSentListNoDataTV.root.visibility = View.GONE
                        binding.messageSentListRV.visibility = View.GONE
                        binding.messageSentListErrorPage.root.visibility = View.VISIBLE
                    }
                }

                messageViewModel.isMessageSentFetchComplete = true
            }
        })


        if (!messageViewModel.isMessageSentListFetched) {
            messageViewModel.fetchMessageList(ModelUtil.messageTypeSent, true)
        }

        binding.messageSentListRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, scrollX: Int, scrollY: Int) {
                val lastIndexInScreen = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                if (lastIndexInScreen >= messageSentListAdapter.itemCount - 5 && messageSentListAdapter.itemCount != 0 && messageViewModel.isMessageSentFetchComplete && messageViewModel.messageSentHasMore) {
                    Log.d("scroll", "now")
                    messageViewModel.isMessageSentFetchComplete = false
                    messageViewModel.fetchMessageList(ModelUtil.messageTypeSent, false)

                    messageSentListAdapter.addInfiniteScroll()

                }

                if (messageViewModel.isComposeViewShown) {
                    try {

                        val dashboardActivity = activity as MessageActivity
                        if (scrollY > 0 && dashboardActivity.floatingActionButtonVisiblity() == View.VISIBLE) {
                            dashboardActivity.hideFab()
                        } else if (RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_MESSAGES,
                                requireContext()) && scrollY < 0 && dashboardActivity.floatingActionButtonVisiblity() != View.VISIBLE
                        ) {
                            dashboardActivity.showFab()
                        }

                        /*    handler.removeCallbacks(runnable)
                        handler.postDelayed(runnable, 100)*/
                    } catch (e: Exception) {
                        Log.d(TAG, e.toString())
                    }
                }
            }
        })

        binding.messageSentListSwipeRefreshLayout.setOnRefreshListener {

            binding.messageSentListSwipeRefreshLayout.isRefreshing = true
            messageViewModel.fetchMessageList(ModelUtil.messageTypeSent, true)
        }

        binding.messageSentListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.messageSentListErrorPage.root.visibility = View.GONE
            fetchSentMessage()

            (activity as MessageActivity).fetchComposeRestriction()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun fetchSentMessage() {
        try {
            messageViewModel.isMessageSentListFetched = false
            messageViewModel.isMessageSentFetchComplete = false
            binding.messageSentListProgress.root.visibility = View.VISIBLE
            messageViewModel.fetchMessageList(ModelUtil.messageTypeSent, true)
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }


    fun deleteMessage(position: Int) {
        try {
            val size = messageSentListAdapter.removeMessage(position)
            if (size == 0) {
                binding.messageSentListNoDataTV.root.visibility = View.VISIBLE
                binding.messageSentListRV.visibility = View.GONE
            } else {
                binding.messageSentListNoDataTV.root.visibility = View.GONE
                binding.messageSentListRV.visibility = View.VISIBLE
                checkToShowBgImage()
            }
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    override fun disableMultiSelect() {
        messageSentListAdapter.disableMultiSelect()

        binding.messageSentListSwipeRefreshLayout.isEnabled = true
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

            val footerTreeObserver = binding.messageSentListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        binding.messageSentListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val medicationHeight = binding.messageSentListRV.height ?: 0

                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $medicationHeight")

                        if (medicationHeight < viewHeight) {
                            binding.messageSentBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.messageSentBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            binding.messageSentBgLayout.visibility = View.GONE
        }
    }

}
