package com.charm.phr.mphr.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemHealthListBinding
import com.charm.phr.mphr.databinding.ItemWellnessDataListBinding
import com.charm.phr.mphr.model.Module
import com.charm.phr.mphr.model.WellnessData
import com.charm.phr.mphr.util.ModelUtil

interface ModuleSelectListener {
    fun onModuleSelect(moduleId: String)
}

class WellnessDataListAdapter : RecyclerView.Adapter<WellnessDataListAdapter.ViewHolder>() {


    private val viewPool = RecyclerView.RecycledViewPool()

    private var listener :ModuleSelectListener? = null

    private val wellnessDataList = arrayListOf(WellnessData(headingName = R.string.wellness_data_vital_txt , moduleList = arrayListOf(
            Module(ModelUtil.wellnessBPModelId , R.string.wellness_bp_txt , R.drawable.ic_bp_wellness),
                    Module(ModelUtil.wellnessPulseModelId , R.string.wellness_pulse_txt , R.drawable.ic_pulse_rate),
            Module(ModelUtil.wellnessTemperatureModelId , R.string.wellness_temperature_txt , R.drawable.ic_temperature)
            //Module(ModelUtil.wellnessBloodSugarModelId , R.string.wellness_blood_sugar_txt , R.drawable.ic_blood_sugar)

    )))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWellnessDataListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = wellnessDataList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(wellnessDataList[position] , listener)
    }


    fun setModuleSelectListener(moduleSelectListener: ModuleSelectListener){
        listener = moduleSelectListener
    }

    inner class ViewHolder(private val binding: ItemWellnessDataListBinding): RecyclerView.ViewHolder(binding.root) {

        val wellnessHeadingTV = binding.wellnessDataSubHeading
        val wellnessInnerRV = binding.wellnessDataInnerRV

        fun onBind(wellnessData: WellnessData , moduleSelectListener: ModuleSelectListener?){

            val childLayoutManager = LinearLayoutManager(  wellnessInnerRV.context, RecyclerView.VERTICAL, false)

            wellnessHeadingTV.setText(wellnessData.headingName)

            wellnessInnerRV.apply {
                layoutManager = childLayoutManager
                adapter = WellnessDataInnerListAdapter(wellnessData.moduleList , moduleSelectListener)
                setRecycledViewPool(viewPool)
            }
        }
    }


}

class WellnessDataInnerListAdapter(val moduleList: ArrayList<Module> ,val moduleSelectListener: ModuleSelectListener?)  : RecyclerView.Adapter<WellnessDataInnerListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHealthListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int  = moduleList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val module = moduleList[position]
        holder.onBind(module)

        holder.itemView.setOnClickListener {
            moduleSelectListener?.onModuleSelect(module.moduleId)
        }
    }

    inner class ViewHolder(private val binding: ItemHealthListBinding): RecyclerView.ViewHolder(binding.root) {

        val healthListName: TextView = binding.healthListName
        val healthListIcon: ImageView = binding.healthListIcon
        val healthListNotificationTV: TextView = binding.healthListNotificationTV
        val healthListConstraintLayout = binding.healthListConstraintLayout

        fun onBind(module: Module) {


            healthListConstraintLayout.setBackgroundResource(R.drawable.item_ripple_white_bg)
            if (module.moduleNameFromSection.isNullOrBlank()) {
                healthListName.setText(module.moduleName)
            }
            else{
                healthListName.text = module.moduleNameFromSection
            }

            healthListIcon.setImageResource(module.moduleIconId)

            if (module.unreadCount == 0){
                healthListNotificationTV.visibility = View.GONE
            }
            else{
                healthListNotificationTV.visibility = View.VISIBLE
                healthListNotificationTV.text = "${module.unreadCount}"
            }
        }
    }


}