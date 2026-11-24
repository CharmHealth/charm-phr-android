package com.charm.phr.mphr.ui.components.iconSwitch

import android.content.Context
import com.charm.phr.mphr.model.MDEntry
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF


class GraphMarkerView(val appContext: Context, layoutRes: Int) : MarkerView(appContext, layoutRes) {


    private val twoDecimalFormat = "%.2f"

    var labelList = arrayListOf<String>()

    var unitTxt = arrayListOf<String>()

    override fun refreshContent(e: Entry, highlight: Highlight?) {


        val nameIndex = if (e is MDEntry) {
            e.unitPosn
        } else {
            0
        }

        val xAxis = (e.x - 2).toInt()
        val contentDate = if (xAxis % 4 == 0) {
            val index = (xAxis / 4)
            if (index >= 0 && index < labelList.size) {
                labelList[index]
            } else {
                ""
            }
        } else {
            ""
        }

        val chartSelectContent1TV =
            findViewById<android.widget.TextView>(com.charm.phr.mphr.R.id.chartSelectContent1TV)
        chartSelectContent1TV.setText(contentDate)

        val yValue = e.y

        var formatValue = twoDecimalFormat.format(yValue)

        if (formatValue.endsWith(".00")) {
            formatValue = formatValue.split(".")[0]
        }

        if (unitTxt.isNotEmpty() && nameIndex>=0 && nameIndex < unitTxt.size) {
            formatValue = "$formatValue ${unitTxt[nameIndex]}"
        }

        val chartSelectContent2TV =
            findViewById<android.widget.TextView>(com.charm.phr.mphr.R.id.chartSelectContent2TV)
        chartSelectContent2TV.text = formatValue.trim()


        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF? {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
}