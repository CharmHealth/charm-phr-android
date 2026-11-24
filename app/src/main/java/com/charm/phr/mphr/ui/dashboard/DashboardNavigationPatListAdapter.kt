package com.charm.phr.mphr.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemDashboardNavigationPatientListBinding
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.util.APIList
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class DashboardNavigationPatListAdapter(val picasso: Picasso, val appContext: Context, val authManager: AuthManager) :
    RecyclerView.Adapter<DashboardNavigationPatListAdapter.ViewHolder>() {

    private val patientList = ArrayList<Patient>()

    var listener: PatientSelectListener? = null

    private var isImgChanged = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDashboardNavigationPatientListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, picasso, appContext, authManager)
    }

    override fun getItemCount(): Int = patientList.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val patient = patientList[position]
        viewHolder.onBindView(patient, isImgChanged)

        viewHolder.itemView.setOnClickListener {
            listener?.onPatientSelect(patient, viewHolder.itemView)
        }
    }

    fun setItems(items: ArrayList<Patient>) {
        //
        patientList.clear()
        patientList.addAll(items)
        notifyDataSetChanged()
    }

    fun setImgChange(imgChange: Boolean) {
        isImgChanged = imgChange
        notifyDataSetChanged()
    }

    fun setPatientSelectListener(patientSelectListener: PatientSelectListener) {
        this.listener = patientSelectListener
    }

    class ViewHolder(val binding: ItemDashboardNavigationPatientListBinding, val picasso: Picasso, val appContext: Context, val authManager: AuthManager) :
    RecyclerView.ViewHolder(binding.root) {
    val patientNameTV: TextView = binding.dashboardNavigationPatListPatientName
    val patientDetail: TextView = binding.dashboardNavigationPatListDetailTV
    val patientImgView: CircleImageView = binding.dashboardNavigationPatListPatientImg
    val patientDummyTV: TextView = binding.dashboardNavigationPatListPatientDummyPicTV

        fun onBindView(patient: Patient, isImgChange: Boolean) {


            patientNameTV.text = patient.fullName
            patientDetail.text = UIUtil.formPatientDetail(patient)

            val initials = UIUtil.formPatientInitials(patient)

            patientDummyTV.visibility = View.VISIBLE
            patientImgView.visibility = View.INVISIBLE
            patientDummyTV.setText(initials)


            picasso.cancelRequest(patientImgView)

            if (patient.isPhotoAvailableOnline && authManager.isUserOnline()) {

                val picLoader = picasso.load(APIList.getPatientImageURL(patient.practicePatientId))

                if (isImgChange) {
                    picLoader.memoryPolicy(MemoryPolicy.NO_CACHE)
                    picLoader.networkPolicy(NetworkPolicy.NO_CACHE)
                }

                val imgSize = patientImgView.context.resources.getDimensionPixelSize(R.dimen.dashboard_navigation_patient_list_img_size)
                picLoader.resize(imgSize, imgSize).into(patientImgView, object : Callback {
                    override fun onSuccess() {
                        try {
                            patientImgView.visibility = View.VISIBLE
                            patientDummyTV.visibility = View.GONE
                        } catch (e: Exception) {

                        }
                    }
                    override fun onError(e: Exception?) {
                    }
                })
            }


        }
    }

    interface PatientSelectListener {
        fun onPatientSelect(patient: Patient, view: View)
    }
}