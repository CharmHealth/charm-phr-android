package com.charm.phr.mphr.ui.components

import android.app.TimePickerDialog
import android.content.Context
import android.widget.TimePicker

class MDTimePickerDialog(
    context: Context, callBack: OnTimeSetListener,
    hourOfDay: Int, minute: Int, is24HourView: Boolean, val isAbsTimeInterval: Boolean = true
) :
    TimePickerDialog(context, callBack, hourOfDay, minute, is24HourView) {

    private val TIME_PICKER_INTERVAL = 5

    /*
        private  var mTimePicker : TimePicker
    */


    private var mIgnoreEvent = false

    private var callBack: OnTimeSetListener? = null


    init {
        /* mTimePicker =  TimePicker(context)
         mTimePicker.descendantFocusability = TimePicker.FOCUS_BLOCK_DESCENDANTS
         mTimePicker.setIs24HourView(is24HourView)

         this.callBack = callBack

         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
             mTimePicker.currentHour = hourOfDay
             mTimePicker.currentMinute = minute
         }
         else {
             mTimePicker.hour = hourOfDay
             mTimePicker.minute = minute
         }


         mTimePicker.setOnTimeChangedListener(this)
         setView(mTimePicker)*/


    }


    override fun onTimeChanged(timePicker: TimePicker, hourOfDay: Int, minute: Int) {
        var tempMinute = minute
        super.onTimeChanged(timePicker, hourOfDay, tempMinute)
        this.setTitle("2. Select Time")
        if (!mIgnoreEvent && isAbsTimeInterval) {
            tempMinute = getRoundedMinute(tempMinute)
            mIgnoreEvent = true
            timePicker.minute = tempMinute
            mIgnoreEvent = false
        }
    }


    fun getRoundedMinute(minute: Int): Int {
        var tempMinute = minute
        if (tempMinute % TIME_PICKER_INTERVAL != 0) {
            val minuteFloor = tempMinute - tempMinute % TIME_PICKER_INTERVAL
            tempMinute = minuteFloor + if (tempMinute == minuteFloor + 1) TIME_PICKER_INTERVAL else 0
            if (tempMinute == 60) tempMinute = 0
        }

        return tempMinute
    }


}
