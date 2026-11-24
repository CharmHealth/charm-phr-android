package com.charm.phr.mphr.ui.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.charm.phr.mphr.R

class MDAutoCompleteTextViewAdapter : ArrayAdapter<String> {


    constructor(context: Context, itemList: ArrayList<String>) : super(context, 0, itemList) {
        this.itemList.clear()
        this.itemList.addAll(itemList)
    }

    val itemList: ArrayList<String> = arrayListOf()


    var itemListFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {

            val filterResults = FilterResults()
            val suggestions = arrayListOf<String>()


            /*if (constraint.isNullOrBlank()){
                if (itemList.size > 5){
                    suggestions.addAll(itemList.subList(0 , 4))
                }
                else{
                    suggestions.addAll(itemList)
                }
            }
            else{*/


            if (!constraint.isNullOrBlank()) {
                suggestions.addAll(itemList.filter {
                    it.contains(constraint, true) /*&& !this@BookAppointmentMemberListAdapter.selectedMemberList.contains(it)*/
                })
            }


            filterResults.count = suggestions.size
            filterResults.values = suggestions

            return filterResults

        }

        override fun publishResults(p0: CharSequence?, results: FilterResults) {
            clear()
            if (results.values is MutableCollection<*>) {
                addAll(results.values as MutableCollection<out String>)
            }
            notifyDataSetChanged()
        }

        override fun convertResultToString(resultValue: Any): CharSequence {
            return resultValue as String
        }

    }


    override fun getFilter(): Filter {
        return itemListFilter
    }

    fun setItemList(itemList: List<String>) {
        this.itemList.clear()
        this.itemList.addAll(itemList)
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


        val itemView = (convertView ?: LayoutInflater.from(context).inflate(
                R.layout.item_custom_list_pop_menu,
                null,
                false
        )) as TextView
        itemView.text = getItem(position)
        return itemView

    }
}