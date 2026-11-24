package com.charm.phr.mphr.ui.patientList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemPatientListBinding
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.util.APIList
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import de.hdodenhof.circleimageview.CircleImageView

class PatientListAdapter(val picasso: Picasso, val appContext: Context, val authManager: AuthManager) : RecyclerView.Adapter<PatientListAdapter.ViewHolder>() {

    private val patientList = ArrayList<Patient>()
    private val targetInstance = ArrayList<Target?>()

    var listener: PatientSelectListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootView = ItemPatientListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val lp = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rootView.root.layoutParams = lp

        return ViewHolder(rootView, picasso, appContext, authManager)
    }

    override fun getItemCount(): Int = patientList.size

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val patient = patientList[position]
        viewHolder.onBindView(patient, position, targetInstance)

        viewHolder.itemView.setOnClickListener {
            listener?.onPatientSelect(patient, viewHolder.itemView)
        }
    }

    fun setItems(items: ArrayList<Patient>) {
        //
        patientList.clear()
        patientList.addAll(items)
        targetInstance.clear()
        for (item in items) {
            targetInstance.add(null)
        }
        notifyDataSetChanged()
    }


    fun setPatientSelectListener(patientSelectListener: PatientSelectListener) {
        this.listener = patientSelectListener
    }

    class ViewHolder(val binding: ItemPatientListBinding, val picasso: Picasso, val appContext: Context, val authManager: AuthManager) : RecyclerView.ViewHolder(binding.root) {

        val patientNameTV: TextView = binding.patientListName
        val dateOfBirthTV: TextView = binding.patientListDetail
        val patientImgView: CircleImageView = binding.patientListImg
        val patientDummyTV: TextView = binding.patientListDummyTV

        fun onBindView(patient: Patient, position: Int, targetInstance: ArrayList<Target?>) {


            patientNameTV.text = patient.fullName

            dateOfBirthTV.text = UIUtil.formPatientDetail(patient)



            picasso.cancelRequest(patientImgView)

            patientDummyTV.visibility = View.VISIBLE
            patientImgView.visibility = View.INVISIBLE
            patientDummyTV.text = UIUtil.formMemberInitials(patient.fullName ?: "")
//            try {
//                if (targetInstance[position] != null){
//                    picasso.cancelRequest(targetInstance[position]!!)
//                    targetInstance[position] = null
//                }
//
//            }
//            catch (e: ArrayIndexOutOfBoundsException){
//            }
//            catch (e:IndexOutOfBoundsException){
//            }

            if (patient.isPhotoAvailableOnline && authManager.isUserOnline()) {
//                val target  = object :Target{
//                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
//                    }
//
//                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
//
//                    }
//
//
//
//                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
//
//                        patientImgView.setImageBitmap(bitmap)
//                    }
//                }
//
//                targetInstance[position] = target


                val picLoader = picasso.load(APIList.getPatientImageURL(patient.practicePatientId))

                val imgSize = patientImgView.context.resources.getDimensionPixelSize(R.dimen.patient_list_img_size)
                picLoader.resize(imgSize, imgSize).into(patientImgView, object : Callback {
                    override fun onSuccess() {
                        try {
                            patientDummyTV.visibility = View.GONE
                            patientImgView.visibility = View.VISIBLE
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