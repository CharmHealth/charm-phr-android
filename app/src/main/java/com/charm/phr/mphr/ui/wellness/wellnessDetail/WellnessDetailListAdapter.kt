package com.charm.phr.mphr.ui.wellness.wellnessDetail

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.databinding.ItemWellnessListBinding
import com.charm.phr.mphr.model.PatientVitalTracker
import com.charm.phr.mphr.util.ModelUtil

class WellnessDetailListAdapter : RecyclerView.Adapter<WellnessDetailListAdapter.ViewHolder>() {

    private val patientVitalTrackerList = arrayListOf<PatientVitalTracker>()
    private val tempPatientVitalTrackerList = arrayListOf<PatientVitalTracker>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWellnessListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = patientVitalTrackerList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val patVitalTracker = patientVitalTrackerList[position]

        holder.onBind(patVitalTracker)
    }

    fun setPatVitalTrackerList(patientVitalTrackerList: ArrayList<PatientVitalTracker>): Int{

        tempPatientVitalTrackerList.clear()
        patientVitalTrackerList.forEach {patientVitalTracker ->

            if (patientVitalTracker.patientVitalsList.any { !it.trackedValue.isNullOrBlank() }){
                tempPatientVitalTrackerList.add(patientVitalTracker)
            }
        }

       // this.patientVitalTrackerList.addAll(patientVitalTrackerList)
        Log.d("MDWellnessListSize" , "${tempPatientVitalTrackerList.size}")


        this.patientVitalTrackerList.clear()

        if (tempPatientVitalTrackerList.size >= 5){
            this.patientVitalTrackerList.addAll(tempPatientVitalTrackerList.subList(0,5))
        }
        else{
            this.patientVitalTrackerList.addAll(tempPatientVitalTrackerList)
        }
        notifyDataSetChanged()

        return tempPatientVitalTrackerList.size
    }

    fun showMorePatVitalList(){

        val morePatVitalList  = tempPatientVitalTrackerList.subList(5 ,tempPatientVitalTrackerList.size)
        patientVitalTrackerList.addAll(morePatVitalList)
        notifyDataSetChanged()
    }



    inner class ViewHolder(private val binding: ItemWellnessListBinding): RecyclerView.ViewHolder(binding.root) {

    val wellnessDateTV = binding.wellnessListDateTV
    val wellnessTimeTV = binding.wellnessListTimeTV
    val wellnessValueTV = binding.wellnessListValueTV

        fun onBind(patientVitalTracker: PatientVitalTracker){

            wellnessDateTV.text = patientVitalTracker.dateOfEntry?.toString(ModelUtil.charmDisplayDateFormat())
            wellnessTimeTV.text = patientVitalTracker.dateOfEntry?.toString(ModelUtil.charmDisplayTimeFormat())

            if (patientVitalTracker.tracker?.trackerName == ModelUtil.wellnessBPModelId){

                val systolObj = patientVitalTracker.patientVitalsList.firstOrNull { it.metricName?.contains(ModelUtil.metricSystolic , true) ?: false  }
                val diastolicObj = patientVitalTracker.patientVitalsList.firstOrNull { it.metricName?.contains(ModelUtil.metricDiastolic , true) ?: false  }

                if (systolObj != null || diastolicObj != null) {

                    val systolFormatValue = ModelUtil.getTwoDecimalStr(systolObj?.trackedValue?.toFloatOrNull())
                    val diastolFormatValue =  ModelUtil.getTwoDecimalStr(diastolicObj?.trackedValue?.toFloatOrNull())

                    val value =  "${systolFormatValue}/${diastolFormatValue} ${systolObj?.trackedValueUnit ?: diastolicObj?.trackedValueUnit ?: ""}"

                    wellnessValueTV.text = value.trim()
                }
            }

            else if(patientVitalTracker.tracker?.trackerName == ModelUtil.wellnessTemperatureModelId)  {


                val temperatureObj = patientVitalTracker.patientVitalsList.firstOrNull()

                if (temperatureObj != null){
                    val temperatureFormatValue = "${ModelUtil.getTwoDecimalStr(temperatureObj.trackedValue?.toFloatOrNull())} ${ModelUtil.wellnessDegree}${temperatureObj.trackedValueUnit}"

                    wellnessValueTV.text =  temperatureFormatValue

                }
            }

            else {

                val pulseObj = patientVitalTracker.patientVitalsList.firstOrNull()

                if (pulseObj != null){
                    val pulseObjFormatValue ="${ModelUtil.getTwoDecimalStr(pulseObj.trackedValue?.toFloatOrNull())} ${pulseObj.trackedValueUnit}"

                    wellnessValueTV.text =  pulseObjFormatValue

                }
            }
            /*else if (patientVitalTracker.tracker?.trackerName == ModelUtil.wellnessGeneralModelId) {

                val generalObj = patientVitalTracker.patientVitalsList.firstOrNull()

                if (generalObj != null){
                    val generalObjFormatValue ="${ModelUtil.getTwoDecimalStr(generalObj.trackedValue?.toFloatOrNull())} ${generalObj.trackedValueUnit}"
                    wellnessValueTV.text = generalObjFormatValue
                }
            }
            else if (patientVitalTracker.tracker?.trackerName == ModelUtil.wellnessOthersModelId) {

                val generalObj = patientVitalTracker.patientVitalsList.firstOrNull()

                if (generalObj != null){
                    val generalObjFormatValue ="${ModelUtil.getTwoDecimalStr(generalObj.trackedValue?.toFloatOrNull())} ${generalObj.trackedValueUnit}"
                    wellnessValueTV.text = generalObjFormatValue
                }
            }*/
        }
    }
}