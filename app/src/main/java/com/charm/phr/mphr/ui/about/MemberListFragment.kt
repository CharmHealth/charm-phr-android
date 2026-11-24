package com.charm.phr.mphr.ui.about

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentMemberListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.UIUtil
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * [MemberListFragment] fetches and shows the member lists of the practices of the selected patient.
 */
class MemberListFragment : DaggerFragment() {

    @Inject
    lateinit var memberListAdapter: MemberListAdapter

    lateinit var aboutViewModel: AboutViewModel

    lateinit var binding: FragmentMemberListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMemberListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aboutViewModel = ViewModelProvider(requireActivity())[AboutViewModel::class.java]

        binding.aboutMemberListRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.aboutMemberListRV.adapter = memberListAdapter
        binding.aboutMemberListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        aboutViewModel.aboutListLive.observe(viewLifecycleOwner, Observer { resp ->

            if (resp != null) {

                binding.aboutMemberListProgress.root.visibility = View.GONE

                if (binding.aboutMemberListSwipeRefreshLayout.isRefreshing) {
                    binding.aboutMemberListSwipeRefreshLayout.isRefreshing = false
                }

                if (resp is Pair<*, *>) {

                    val pairResp = resp as Pair<ArrayList<Member>, ArrayList<Member>>

                    val size = memberListAdapter.setMemberList(pairResp.second)

                    if (size == 0) {
                        binding.aboutMemberListNoDataTV.root.visibility = View.VISIBLE
                        binding.aboutMemberListRV.visibility = View.GONE
                    } else {
                        binding.aboutMemberListNoDataTV.root.visibility = View.GONE
                        binding.aboutMemberListRV.visibility = View.VISIBLE
                        checkToShowBgImage()
                    }
                } else if (resp is APIResponseError) {
                    binding.aboutMemberListRV.visibility = View.GONE
                    binding.aboutMemberListNoDataTV.root.visibility = View.GONE
                    binding.aboutMemberListErrorPageLayout.root.visibility = View.VISIBLE
                }
            }
        })

        binding.aboutMemberListSwipeRefreshLayout.setOnRefreshListener {
            binding.aboutMemberListSwipeRefreshLayout.isRefreshing = true
            aboutViewModel.fetchAboutList()
        }

        binding.aboutMemberListErrorPageLayout.errorPageTryAgainBtn.setOnClickListener {
            binding.aboutMemberListProgress.root.visibility = View.VISIBLE
            binding.aboutMemberListErrorPageLayout.root.visibility = View.GONE
            aboutViewModel.fetchAboutList()
        }

    }

    private fun checkToShowBgImage() {

        if (!BuildUtil.isCustomApp) {
            val height = UIUtil.getScreenHeight(this.activity)

            val tv = TypedValue()
            var actionBarHeight = 0
            if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
            }

            val viewHeight = ((height - actionBarHeight) * 2) / 3

            val footerTreeObserver = binding.aboutMemberListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        binding.aboutMemberListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val supplementHeight = binding.aboutMemberListRV.height ?: 0

                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $supplementHeight")

                        if (supplementHeight < viewHeight) {
                            binding.aboutMemberListBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.aboutMemberListBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            binding.aboutMemberListBgLayout.visibility = View.GONE
        }
    }
}
