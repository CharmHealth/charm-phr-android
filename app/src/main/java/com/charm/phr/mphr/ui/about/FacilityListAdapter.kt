package com.charm.phr.mphr.ui.about

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemFacilityListBinding
import com.charm.phr.mphr.model.Facility

class FacilityListAdapter : RecyclerView.Adapter<FacilityListAdapter.ViewHolder>() {

    private val facilityList = arrayListOf<Facility>()

    private var mapLinkSelectListener: MapLinkSelectListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemFacilityListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = facilityList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val facility = facilityList[position]

        holder.onBind(facility)

        holder.facilityListConstLayout.setOnClickListener {
            mapLinkSelectListener?.onMapLinkSelect(facility.directions ?: "")
        }
    }

    fun setFacilityList(facilityList: ArrayList<Facility>): Int {
        this.facilityList.clear()
        this.facilityList.addAll(facilityList)
        notifyDataSetChanged()

        return facilityList.size
    }

    inner class ViewHolder(binding: ItemFacilityListBinding) : RecyclerView.ViewHolder(binding.root) {

        val facilityListNameTV: TextView = binding.aboutFacilityListNameTV
        val facilityListMapTmg: ImageView = binding.aboutFacilityListLocationImg
        val facilityListConstLayout = binding.aboutFacilityListConstraintLayout
        val facilityListAddressTV: TextView = binding.aboutFacilityListDirectionTV
        val facilityListPhoneTV: TextView = binding.aboutFacilityListPhoneTV

        fun onBind(facility: Facility) {

            facilityListNameTV.text = facility.facilityName

            if (facility.postalAddress?.line1.isNullOrBlank() && facility.postalAddress?.line2.isNullOrBlank()) {
                facilityListAddressTV.visibility = View.GONE
            } else {
                facilityListAddressTV.visibility = View.VISIBLE
                facilityListAddressTV.text = if (!facility.postalAddress?.line1.isNullOrBlank() && !facility.postalAddress?.line2.isNullOrBlank()) {
                    itemView.context.getString(R.string.about_facility_address_str, facility.postalAddress?.line1, facility.postalAddress?.line2)
                } else if (!facility.postalAddress?.line1.isNullOrBlank()) {
                    facility.postalAddress?.line1
                } else {
                    facility.postalAddress?.line2
                }

            }

            if (facility.contactDetails?.landline.isNullOrBlank()) {
                facilityListPhoneTV.visibility = View.GONE
            } else {
                facilityListPhoneTV.visibility = View.VISIBLE
                facilityListPhoneTV.text = itemView.context.getString(R.string.about_facility_phone_str, facility.contactDetails?.landline)
            }


            if (facility.directions.isNullOrBlank()) {
                facilityListConstLayout.visibility = View.GONE
            } else {
                facilityListMapTmg.visibility = View.VISIBLE
            }

        }
    }

    fun setMapLinkSelectListener(listener: MapLinkSelectListener) {
        mapLinkSelectListener = listener
    }

    interface MapLinkSelectListener {
        fun onMapLinkSelect(locationLink: String)
    }
}