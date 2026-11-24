package com.charm.phr.mphr.ui.wellness.wellnessDetail

import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.WellnessGridEntry
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil

class WellnessDetailGridAdapter : RecyclerView.Adapter<WellnessDetailGridAdapter.ViewHolder>() {

    private val wellnessGridEntry = arrayListOf<WellnessGridEntry>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_wellness_detail_grid, parent, false)
        )
    }

    override fun getItemCount(): Int = wellnessGridEntry.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val patVitalTracker = wellnessGridEntry[position]

        holder.onBind(patVitalTracker, holder.itemView)
    }

    fun setWellnessGridEntryList(wellnessGridEntry: ArrayList<WellnessGridEntry>) {

        this.wellnessGridEntry.clear()
        this.wellnessGridEntry.addAll(wellnessGridEntry)
        Log.d("MDWellnessListSize", "${this.wellnessGridEntry.size}")
        notifyDataSetChanged()
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun onBind(wellnessGridEntry: WellnessGridEntry, view: View) {
            val textView = (view as TextView)
            if (wellnessGridEntry.entry.isNullOrBlank()) {
                textView.setText(R.string.vital_empty_txt)
            } else {
                if (wellnessGridEntry.entry?.toFloatOrNull() != null) {
                    textView.text = ModelUtil.getTwoDecimalStr(wellnessGridEntry.entry?.toFloatOrNull())
                } else {
                    textView.text = wellnessGridEntry.entry
                }
            }
            if (wellnessGridEntry.isHeading) {
                textView.typeface = UIUtil.getFont(textView.context, R.font.open_sans_medium)
                textView.setTextColor(ContextCompat.getColor(textView.context, R.color.colorPrimary))
                textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, textView.resources.getDimension(R.dimen.x_small)
                )
            } else {
                textView.typeface = UIUtil.getFont(textView.context, R.font.open_sans_medium)
                textView.setTextColor(ContextCompat.getColor(textView.context, R.color.black87))
                textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, textView.resources.getDimension(R.dimen.medium)
                )
            }
        }
    }
}