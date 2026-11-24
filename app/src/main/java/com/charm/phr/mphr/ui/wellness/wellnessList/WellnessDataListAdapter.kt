package com.charm.phr.mphr.ui.wellness.wellnessList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.BuildConfig
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemWellnessDataListBinding
import com.charm.phr.mphr.databinding.ItemWellnessModuleListBinding
import com.charm.phr.mphr.model.Module
import com.charm.phr.mphr.model.TrackerMetricMap
import com.charm.phr.mphr.model.WellnessData
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil

class WellnessDataListAdapter : RecyclerView.Adapter<WellnessDataListAdapter.ViewHolder>() {


    private val viewPool = RecyclerView.RecycledViewPool()

    private var listener: ModuleSelectListener? = null


    private val vitalModules = arrayListOf(
        Module(
            ModelUtil.wellnessBPModelId,
            R.string.wellness_bp_txt,
            R.drawable.ic_bp_wellness,
            position = 0
        ),
        Module(
            ModelUtil.wellnessPulseModelId,
            R.string.wellness_pulse_txt,
            R.drawable.ic_pulse_rate,
            position = 1
        ),
        Module(
            ModelUtil.wellnessTemperatureModelId,
            R.string.wellness_temperature_txt,
            R.drawable.ic_temperature,
            position = 2
        ),
        Module(
            ModelUtil.wellnessGeneralModelId,
            R.string.wellness_weight_and_height_txt,
            R.drawable.ic_general_wellness,
            position = 3
        ),
        Module(
            ModelUtil.wellnessBloodSugarModelId,
            R.string.wellness_blood_sugar_txt,
            R.drawable.ic_blood_sugar,
            position = 4
        ),
        Module(
            ModelUtil.wellnessCholesterolModelId,
            R.string.wellness_cholesterol_txt,
            R.drawable.ic_cholesterol,
            position = 5
        )

    )

    private val fitnessModules = arrayListOf(
        Module(
            ModelUtil.metricWalking,
            R.string.wellness_walking_txt,
            R.drawable.ic_fitness_walking,
            position = 0
        ),
        Module(
            ModelUtil.metricCycling,
            R.string.wellness_cycling_txt,
            R.drawable.ic_fitness_cycling,
            position = 1
        ),
        Module(
            ModelUtil.metricSwimming,
            R.string.wellness_swimming_txt,
            R.drawable.ic_fitness_swimming,
            position = 2
        ),
        Module(
            ModelUtil.metricJogging,
            R.string.wellness_jogging_txt,
            R.drawable.ic_fitness_walking,
            position = 3
        ),
        Module(
            ModelUtil.metricTreadmill,
            R.string.wellness_treadmill_txt,
            R.drawable.ic_fitness_treadmill,
            position = 4
        ),
        Module(
            ModelUtil.metricAerobics,
            R.string.wellness_aerobics_txt,
            R.drawable.ic_fitness_aerobic,
            position = 5
        ),
        Module(
            ModelUtil.metricCalBurned,
            R.string.wellness_cal_burned_txt,
            R.drawable.ic_fitness_calories,
            position = 6
        ),
        Module(
            ModelUtil.metricCalConsumed,
            R.string.wellness_cal_consumed_txt,
            R.drawable.ic_fitness_calories_gained,
            position = 7
        ),
        Module(
            ModelUtil.metricWater,
            R.string.wellness_water_consumption_txt,
            R.drawable.ic_fitness_water_consumption,
            position = 8
        ),
        Module(
            ModelUtil.metricSleep,
            R.string.wellness_sleep_txt,
            R.drawable.ic_fitness_sleep,
            position = 9
        )

    )


    /* private val wellnessDataList = arrayListOf<WellnessData>(WellnessData(headingName = R.string.wellness_data_vital_txt , moduleList =

             //Module(ModelUtil.wellnessBloodSugarModelId , R.string.wellness_blood_sugar_txt , R.drawable.ic_blood_sugar)

     )))*/

    private val wellnessValueDataList = arrayListOf<WellnessData>()

    var trackerMetricMapList = arrayListOf<TrackerMetricMap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemWellnessDataListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = wellnessValueDataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(wellnessValueDataList[position], position, listener)
    }


    fun setModuleSelectListener(moduleSelectListener: ModuleSelectListener) {
        listener = moduleSelectListener
    }


    fun setWellnessData(patientVitalTrackerList: List<TrackerMetricMap>): Int {

        wellnessValueDataList.clear()
        val tempVitalList = arrayListOf<Module>()
        val tempFitnessList = arrayListOf<Module>()
        //google fit
//        val googleSyncList= arrayListOf<Module>()
        // val tempCustomList = arrayListOf<Module>()


        var fitnessPosn = fitnessModules.size

        patientVitalTrackerList.forEach { patientVitalTracker ->
            if (patientVitalTracker.tracker?.isDefaultTracker == true) {

                if (patientVitalTracker.tracker?.trackerName?.equals(
                        ModelUtil.wellnessOthersModelId,
                        true
                    ) == true
                ) {

                    patientVitalTracker.metricList.forEach { metric ->
                        if (metric.isDefaultMetric) {
                            val fitnessData = fitnessModules.firstOrNull {
                                it.moduleId.equals(
                                    metric.metricName,
                                    true
                                )
                            }
                            if (fitnessData != null) {
                                fitnessData.moduleServerId =
                                    "${patientVitalTracker.tracker?.trackerId},${metric.metricId}"
                                tempFitnessList.add(fitnessData)
                            }
                        } else {

                            val fitnessData = Module(
                                moduleNameFromSection = metric.metricName,
                                position = fitnessPosn++
                            )
                            fitnessData.moduleServerId =
                                "${patientVitalTracker.tracker?.trackerId},${metric.metricId}"
                            tempFitnessList.add(fitnessData)
                        }
                    }


                } else {
                    val wellnessData = vitalModules.firstOrNull {
                        it.moduleId.equals(
                            patientVitalTracker.tracker?.trackerName,
                            true
                        )
                    }

                    if (wellnessData != null) {
                        wellnessData.moduleServerId = patientVitalTracker.tracker?.trackerId
                        tempVitalList.add(wellnessData)
                    }
                }
            } else {
                //add Custom modules here
            }
        }


        if (tempVitalList.isNotEmpty()) {
            tempVitalList.sortBy { it.position }
            wellnessValueDataList.add(WellnessData(R.string.wellness_data_vital_txt, tempVitalList))
        }

        if (!BuildUtil.isThalassemia && tempFitnessList.isNotEmpty()) {
            tempFitnessList.sortBy { it.position }
            wellnessValueDataList.add(
                WellnessData(
                    R.string.wellness_data_fitness_txt,
                    tempFitnessList
                )
            )
        }

//        googleSyncList.add(Module(ModelUtil.syncWithGoogleFit , R.string.sync_with_google_fit , R.drawable.ic_fitness_sleep , position = 0))
//        wellnessValueDataList.add(WellnessData(R.string.google_sync , googleSyncList))
        notifyDataSetChanged()

        trackerMetricMapList.clear()
        trackerMetricMapList.addAll(patientVitalTrackerList)

        return tempVitalList.size + (if (BuildUtil.isThalassemia) 0 else tempFitnessList.size)
    }

    inner class ViewHolder(private val binding: ItemWellnessDataListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val wellnessHeadingTV = binding.wellnessDataSubHeading
        val wellnessInnerRV = binding.wellnessDataInnerRV

        fun onBind(
            wellnessData: WellnessData,
            position: Int,
            moduleSelectListener: ModuleSelectListener?
        ) {

            val childLayoutManager =
                LinearLayoutManager(wellnessInnerRV.context, RecyclerView.VERTICAL, false)

            wellnessHeadingTV.setText(wellnessData.headingName)

            wellnessInnerRV.apply {
                layoutManager = childLayoutManager
                adapter = WellnessDataInnerListAdapter(
                    wellnessData.moduleList,
                    position,
                    moduleSelectListener
                )
                setRecycledViewPool(viewPool)
            }
        }
    }


    interface ModuleSelectListener {
        fun onModuleSelect(module: Module, position: Int)
    }
}

class WellnessDataInnerListAdapter(
    val moduleList: ArrayList<Module>,
    val position: Int,
    val moduleSelectListener: WellnessDataListAdapter.ModuleSelectListener?
) : RecyclerView.Adapter<WellnessDataInnerListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWellnessModuleListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun getItemCount(): Int = moduleList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val module = moduleList[position]
        holder.onBind(module)

        holder.itemView.setOnClickListener {
            moduleSelectListener?.onModuleSelect(module, this.position)
        }
    }

    inner class ViewHolder(private val binding: ItemWellnessModuleListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val healthListName: TextView = binding.wellnessModuleListName
        val healthListIcon: ImageView = binding.wellnessModuleListIcon
        val healthListConstraintLayout = binding.wellnessModuleListConstraintLayout
        val healthListDummyTV: TextView = binding.wellnessModuleListDummyTV


        fun onBind(module: Module) {


            healthListConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)

            if (module.moduleNameFromSection.isNullOrBlank()) {
                healthListName.setText(module.moduleName)
                healthListIcon.setImageResource(module.moduleIconId)
                healthListIcon.visibility = View.VISIBLE
                healthListDummyTV.visibility = View.INVISIBLE
            } else {
                healthListName.text = module.moduleNameFromSection
                healthListIcon.visibility = View.INVISIBLE
                healthListDummyTV.visibility = View.VISIBLE
                healthListDummyTV.text = UIUtil.formInitials(module.moduleNameFromSection)
            }


        }
    }


}
