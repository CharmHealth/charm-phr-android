package com.charm.phr.mphr.ui.components

import android.util.Log
import com.github.mikephil.charting.formatter.ValueFormatter

class MDAxisStringFormatter: ValueFormatter() {


    var labelList  = arrayListOf<String>()

    override fun getFormattedValue(value: Float): String {


        Log.d("mdFormatValue" , "$value float")

        val currentValue = (value - 2).toInt()
        return if (currentValue % 4 == 0){

            val index =  (currentValue/4)

            if (index >= 0 && index < labelList.size) {
                labelList[index]
            }
            else{
                ""
            }
        }
        else{
            ""
        }

    }

    fun setLabelStrings(labelList: ArrayList<String>){
        this.labelList = labelList
    }
}