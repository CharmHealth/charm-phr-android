package com.charm.phr.mphr.ui.physician

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityPhysicianBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Pharmacy
import com.charm.phr.mphr.ui.physician.add_physician.AddPhysicianActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

class PhysicianActivity : MDBaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var physicianViewModel: PhysicianViewModel

    @Inject
    lateinit var physicianListAdapter: PhysicianListAdapter

    private val respFromAddOrEdit = 0

    lateinit var binding: ActivityPhysicianBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhysicianBinding.inflate(layoutInflater)
        setContentView(binding.root)

        physicianViewModel = ViewModelProvider(this, viewModelFactory).get(PhysicianViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.physicianToolbar)
        UIUtil.applyViewInsertForBottomView(binding.physicianMainLayout)

        binding.physicianListRV.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.physicianListRV.adapter = physicianListAdapter

        binding.physicianListNoDataTV.nodataTextViewHint.visibility = View.VISIBLE
        binding.physicianListNoDataTV.nodataTextViewHint.setText(R.string.physician_add_hint)
        UIUtil.makePlusColorPrimary(binding.physicianListNoDataTV.nodataTextViewHint)




        physicianListAdapter.setPhysicianSelectListener (object :
                PhysicianListAdapter.PhysicianListSelectListener {
            override fun onEdit(pharmacy: Pharmacy) {

                val intent = Intent(this@PhysicianActivity , AddPhysicianActivity::class.java)
                intent.putExtra(ModelUtil.physicianToEditIntent , true)
                intent.putExtra(ModelUtil.physicianEditDataIntent , pharmacy.toString)
                intent.putExtra(ModelUtil.isHealthPassportIntent , true)
                startActivityForResult(intent , respFromAddOrEdit)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            }

            override fun onDelete(pharmacy: Pharmacy, position: Int) {
                deletePhysician(pharmacy, position)
            }

            override fun onCall(pharmacy: Pharmacy) {

                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${pharmacy.contactDetails?.mobile}")

                val packageManager = this@PhysicianActivity.packageManager
                if (intent.resolveActivity(packageManager) != null) {

                    try {
                        MDBaseActivity.isPassCodeAllowed = false
                        startActivity(intent)
                    }
                    catch(e: ActivityNotFoundException){
                        MDBaseActivity.isPassCodeAllowed = true
                        UIUtil.showToast(this@PhysicianActivity, R.string.no_app_to_perform_call,Toast.LENGTH_SHORT)
                    }

                } else UIUtil.showToast(this@PhysicianActivity, R.string.no_app_to_perform_call,Toast.LENGTH_SHORT)
            }
        })

        binding.physicianListSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)

        binding.physicianListSwipeRefreshLayout.setOnRefreshListener {
            binding.physicianListSwipeRefreshLayout.isRefreshing = true
            fetchPhysician(false)
        }

        binding.physicianBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.physicianListAddFab.setOnClickListener {

            val intent = Intent(this , AddPhysicianActivity::class.java)
            intent.putExtra(ModelUtil.isHealthPassportIntent , true)
            startActivityForResult(intent , respFromAddOrEdit)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        }

        binding.physicianListErrorPage.errorPageTryAgainBtn.setOnClickListener {
            binding.physicianListErrorPage.root.visibility = View.GONE
            fetchPhysician()
        }

        fetchPhysician()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == respFromAddOrEdit){
            if (resultCode == Activity.RESULT_OK){
                fetchPhysician()
            }
        }
    }

    fun onMultiSelectChange(isEnabled:Boolean){

        if (isEnabled){
            binding.physicianDeleteBtn.visibility = View.VISIBLE
        }
        else{
            binding.physicianDeleteBtn.visibility = View.GONE
        }
    }

    private fun fetchPhysician(showProgress: Boolean = true) {

        if (showProgress) {
            binding.physicianListProgress.root.visibility = View.VISIBLE
            binding.physicianListNoDataTV.root.visibility = View.GONE
            binding.physicianListRV.visibility = View.GONE
        }
        physicianViewModel.getPhysicianList().observe(this , Observer {resp ->
            if (resp != null){
                binding.physicianListProgress.root.visibility = View.GONE

                if (resp is ArrayList<*>){

                    binding.physicianListErrorPage.root.visibility = View.GONE

                    if (binding.physicianListSwipeRefreshLayout.isRefreshing){
                        binding.physicianListSwipeRefreshLayout.isRefreshing = false
                    }

                    val size = physicianListAdapter.setPhysician(resp as ArrayList<Pharmacy>)
                    if (size == 0) {
                        binding.physicianListNoDataTV.root.visibility = View.VISIBLE
                        binding.physicianListRV.visibility = View.GONE
                    } else {
                        binding.physicianListNoDataTV.root.visibility = View.GONE
                        binding.physicianListRV.visibility = View.VISIBLE

                        if (!BuildUtil.isCustomApp) {
                            val height = UIUtil.getScreenHeight(this)

                            val tv = TypedValue()
                            var actionBarHeight = 0
                            if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
                            }

                            val viewHeight = ((height - actionBarHeight)*2) /3

                            val footerTreeObserver = binding.physicianListRV.viewTreeObserver

                            footerTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                                override fun onGlobalLayout() {

                                    try {
                                        binding.physicianListRV?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                                        val physicianHeight = binding.physicianListRV.height ?: 0
                                        Log.d("MDHPBgHeights" , "viewHeight -> $viewHeight ; rvHeight -> $physicianHeight")

                                        if (physicianHeight < viewHeight){
                                            binding.physicianListHPBgLayout.visibility = View.VISIBLE
                                        }
                                        else{
                                            binding.physicianListHPBgLayout.visibility = View.GONE
                                        }

                                    }
                                    catch (e: Exception){

                                    }
                                }
                            })
                        } else {
                            binding.physicianListHPBgLayout.visibility = View.GONE
                        }
                    }
                }
                else if (resp is APIResponseError){
                    binding.physicianListNoDataTV.root.visibility = View.GONE
                    binding.physicianListRV.visibility = View.GONE
                    binding.physicianListErrorPage.root.visibility = View.VISIBLE
                }
            }
        })

    }


    fun deletePhysician(pharmacy: Pharmacy, position: Int){
        val builder = AlertDialog.Builder(this)

        builder.setMessage(R.string.physician_delete_confirmation_msg)
        builder.setPositiveButton(R.string.yes_str ){_, _ ->
            progressDialog.show()
            physicianViewModel.deletePhysician(pharmacy).observe(this , Observer {resp ->
                if (resp != null){
                    progressDialog.dismiss()
                    if (resp is Boolean){
                        physicianListAdapter.removePhysician(position )
                        UIUtil.showToast(this , R.string.physician_delete_success_msg , Toast.LENGTH_LONG)
                    }
                    else if (resp is APIResponseError){
                        UIUtil.showToast(this , R.string.error_physician_delete__msg , Toast.LENGTH_LONG)
                    }
                }
            })
        }
        builder.setNegativeButton(R.string.no_str ){_ , _ ->

        }

        builder.show()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
