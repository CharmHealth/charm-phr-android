package com.charm.phr.mphr.ui.document.share_document

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemMemberListForMessageBinding
import com.charm.phr.mphr.databinding.ItemShareDocumentAddNewContactTemplateBinding
import com.charm.phr.mphr.databinding.ItemShareDocumentEmailNotAvailableBinding
import com.charm.phr.mphr.model.Pharmacy
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import java.util.regex.Pattern

class ShareDocumentPharmacyMemberListAdapter : ArrayAdapter<Pharmacy> {
    constructor(context: Context, memberList: ArrayList<Pharmacy>) : super(context, 0, memberList) {
        this.pharmacymemberList.clear()
        this.pharmacymemberList.addAll(memberList)
    }

    val pharmacymemberList: ArrayList<Pharmacy> = arrayListOf()

    val selectedPharmacyMemberList: ArrayList<Pharmacy> = arrayListOf()

    var isNoContactAvailable = false


    var pharmacymemberFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {

            val filterResults = FilterResults()
            val suggestions = arrayListOf<Pharmacy>()
            isNoContactAvailable = false

            Log.d("MDChipsSelectAE", "constraint -> ${constraint ?: ""}")
            if (constraint.isNullOrBlank()) {
                if (pharmacymemberList.size > 10) {
                    suggestions.addAll(pharmacymemberList.subList(0, 9))
                } else {
                    suggestions.addAll(pharmacymemberList)
                }
                suggestions.add(
                    Pharmacy(
                        pharmacyName = context.getString(R.string.full_name_str),
                        patientPharmaMapId = context.getString(R.string.email_address_template_str)
                    )
                )
            } else {
                if (!constraint.contains(":")) {
                    suggestions.addAll(pharmacymemberList.filter {
                        val pharmacymemberName = it.pharmacyName
                        (pharmacymemberName?.contains(constraint, true) == true || it.contactDetails?.emailId?.contains(
                            constraint,
                            true
                        ) == true) && !this@ShareDocumentPharmacyMemberListAdapter.selectedPharmacyMemberList.contains(it)
                    })

                    if (suggestions.isEmpty()) {
                        val pattern = Pattern.compile(ModelUtil.emailRegex)
                        val matcher = pattern.matcher(constraint)
                        if (matcher.matches()) {
                            isNoContactAvailable = true
                            suggestions.add(
                                Pharmacy(
                                    pharmacyName = constraint.substring(0, constraint.lastIndexOf("@")),
                                    patientPharmaMapId = constraint.toString()
                                )
                            )
                        } else {
                            suggestions.add(
                                Pharmacy(
                                    pharmacyName = context.getString(R.string.full_name_str),
                                    patientPharmaMapId = context.getString(R.string.email_address_template_str)
                                )
                            )
                        }
                    } else {
                        suggestions.add(
                            Pharmacy(
                                pharmacyName = context.getString(R.string.full_name_str),
                                patientPharmaMapId = context.getString(R.string.email_address_template_str)
                            )
                        )
                    }
                }
            }
            Log.d("MDChipsSelectAE", "suggestions size-> ${suggestions.size}")


            filterResults.count = suggestions.size
            filterResults.values = suggestions

            return filterResults

        }

        override fun publishResults(p0: CharSequence?, results: FilterResults) {
            clear()
            if (results.values is MutableCollection<*>) {
                addAll(results.values as MutableCollection<out Pharmacy>)
            }

            notifyDataSetChanged()
            Log.d("MDChipsSelectAE", "result size-> $count")
        }

        override fun convertResultToString(resultValue: Any): CharSequence {
            val pharmacymember = resultValue as Pharmacy


            return pharmacymember.pharmacyName + if (pharmacymember.patientPharmaMapId.isNullOrBlank()) {
                ""
            } else {
                ":${pharmacymember.patientPharmaMapId}"
            }

        }

    }

    override fun getFilter(): Filter {
        return pharmacymemberFilter
    }


    fun setPharmacyMemberList(memberList: ArrayList<Pharmacy>) {
        this.pharmacymemberList.clear()
        this.pharmacymemberList.addAll(memberList)
    }


/*

    override fun getCount(): Int {
        return memberList.size
    }

    override fun getItem(position: Int): Member? {
        return memberList[position]
    }
*/


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val member = getItem(position)
        if (member?.patientPharmaMapId.equals(context.getString(R.string.email_address_template_str))) {
            val binding = ItemShareDocumentAddNewContactTemplateBinding.inflate(LayoutInflater.from(context), parent, false)
            binding.root.setTag(ModelUtil.documentShareAddContact)
            binding.addNewContactTV.setText(R.string.add_new_contacts)
            binding.addNewContactTV.setTextColor(context.getColor(R.color.colorPrimary))
            return binding.root
        } else if (isNoContactAvailable) {
            val binding = ItemShareDocumentEmailNotAvailableBinding.inflate(LayoutInflater.from(context), parent, false)
            binding.root.setTag(ModelUtil.documentShareAddContact)
            return binding.root
        } else {
            val itemBinding = ItemMemberListForMessageBinding.inflate(LayoutInflater.from(context), parent, false)
            val itemView = if (convertView?.tag == ModelUtil.documentShareAddContact) {
                itemBinding.root
            } else {
                convertView ?: itemBinding.root
            }
            itemBinding.memberListName?.text = member?.pharmacyName
            itemBinding.memberListDummyTV?.text = UIUtil.formMemberInitials(member?.pharmacyName ?: "")
            itemBinding.memberListDetail?.text = member?.contactDetails?.emailId ?: ""

            return itemView
        }
    }
}