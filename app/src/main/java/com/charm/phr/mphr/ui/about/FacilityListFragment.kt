package com.charm.phr.mphr.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentFacilityListBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Facility
import com.charm.phr.mphr.model.Member
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.UIUtil
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * [FacilityListFragment] fetches and shows the facility list of the practices of the selected patient.
 */
class FacilityListFragment : DaggerFragment(), FacilityListAdapter.MapLinkSelectListener {

    @Inject
    lateinit var facilityListAdapter: FacilityListAdapter

    lateinit var aboutViewModel: AboutViewModel

    private lateinit var binding: FragmentFacilityListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFacilityListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aboutViewModel = ViewModelProvider(requireActivity()).get(AboutViewModel::class.java)


        binding.aboutFacilityListRV.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.aboutFacilityListRV.adapter = facilityListAdapter

        binding.aboutFacilityListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)


        aboutViewModel.aboutListLive.observe(viewLifecycleOwner, Observer { resp ->

            if (resp != null) {

                binding.aboutFacilityListProgress.root.visibility = View.GONE

                if (binding.aboutFacilityListSwipeRefreshLayout.isRefreshing) {
                    binding.aboutFacilityListSwipeRefreshLayout.isRefreshing = false
                }

                if (resp is Pair<*, *>) {

                    val pairResp = resp as Pair<ArrayList<Facility>, ArrayList<Member>>

                    val size = facilityListAdapter.setFacilityList(pairResp.first)

                    if (size == 0) {
                        binding.aboutFacilityListNoDataTV.root.visibility = View.VISIBLE
                        binding.aboutFacilityListRV.visibility = View.GONE
                    } else {
                        binding.aboutFacilityListNoDataTV.root.visibility = View.GONE
                        binding.aboutFacilityListRV.visibility = View.VISIBLE
                        checkToShowBgImage()
                    }
                } else if (resp is APIResponseError) {
                    binding.aboutFacilityListRV.visibility = View.GONE
                    binding.aboutFacilityListNoDataTV.root.visibility = View.GONE
                    binding.aboutFacilityListErrorPageLayout.root.visibility = View.VISIBLE
                }
            }
        })

        binding.aboutFacilityListSwipeRefreshLayout.setOnRefreshListener {
            binding.aboutFacilityListSwipeRefreshLayout.isRefreshing = true
            aboutViewModel.fetchAboutList()
        }

        binding.aboutFacilityListErrorPageLayout.errorPageTryAgainBtn.setOnClickListener {
            binding.aboutFacilityListProgress.root.visibility = View.VISIBLE
            binding.aboutFacilityListErrorPageLayout.root.visibility = View.GONE
            aboutViewModel.fetchAboutList()
        }

        facilityListAdapter.setMapLinkSelectListener(this)

    }

    /**
     * Method use to open map apps with the given location.
     **/
    override fun onMapLinkSelect(locationLink: String) {
        try {
            MDBaseActivity.isPassCodeAllowed = false
            val intent = Intent(
                android.content.Intent.ACTION_VIEW, Uri.parse(locationLink)
            )
            startActivity(intent)
        } catch (e: Exception) {
            UIUtil.showToast(this@FacilityListFragment.context, getString(R.string.error_map_app_not_found), Toast.LENGTH_LONG)
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

            val footerTreeObserver = binding.aboutFacilityListRV.viewTreeObserver

            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    try {

                        binding.aboutFacilityListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)

                        val supplementHeight = binding.aboutFacilityListRV.height ?: 0

                        Log.d("MDHPBgHeights", "viewHeight -> $viewHeight ; rvHeight -> $supplementHeight")

                        if (supplementHeight < viewHeight) {
                            binding.aboutFacilityListBgLayout.visibility = View.VISIBLE
                        } else {
                            binding.aboutFacilityListBgLayout.visibility = View.GONE
                        }

                    } catch (e: Exception) {

                    }
                }
            })
        } else {
            binding.aboutFacilityListBgLayout.visibility = View.GONE
        }

    }

}
