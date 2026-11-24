package com.charm.phr.mphr.ui.health_passport

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemHealthGridListBinding
import com.charm.phr.mphr.model.Module
import com.charm.phr.mphr.ui.dashboard.ModuleSelectListener
import com.charm.phr.mphr.util.ModelUtil

class HealthPassportGridModuleAdapter : RecyclerView.Adapter<HealthPassportGridModuleAdapter.ViewHolder>() {

    private var moduleList: ArrayList<Module> = arrayListOf(
            Module(ModelUtil.prescriptionModelId, R.string.medications_health_passport_str, R.drawable.ic_hp_medication),
            Module(ModelUtil.documentModelId, R.string.document_str, R.drawable.ic_hp_documents),
            Module(ModelUtil.physicianModelId, R.string.physician_str, R.drawable.ic_hp_physicians),
            Module(ModelUtil.problemListModelId, R.string.problem_list_health_passport_str, R.drawable.ic_hp_medical_conditions)
        )

    private var moduleSelectListener: ModuleSelectListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHealthGridListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val module = moduleList[position]
        holder.onBind(module)

        holder.itemView.setOnClickListener {
            moduleSelectListener?.onModuleSelect(module.moduleId)
        }
    }

    override fun getItemCount(): Int = moduleList.size

    fun setModuleSelectListener(listener: ModuleSelectListener){
        moduleSelectListener = listener
    }

    inner class ViewHolder(binding: ItemHealthGridListBinding): RecyclerView.ViewHolder(binding.root) {

        val healthGridListName: TextView = binding.healthGridListName
        val healthGridListIcon: ImageView = binding.healthGridListIcon

        fun onBind(module: Module) {

            healthGridListName.setText(module.moduleName)
            healthGridListIcon.setImageResource(module.moduleIconId)


            val lp = healthGridListName.layoutParams as ConstraintLayout.LayoutParams

            healthGridListName.viewTreeObserver.addOnPreDrawListener( object : ViewTreeObserver.OnPreDrawListener {

                override fun onPreDraw(): Boolean {
                    // Remove listener because we don't want this called before _every_ frame
                    healthGridListName.viewTreeObserver.removeOnPreDrawListener(this)


                    // Drawing happens after layout so we can assume getLineCount() returns the correct value
                    if(healthGridListName.lineCount > 1) {
                        lp.bottomMargin = healthGridListName.resources.getDimensionPixelSize(R.dimen.hp_grid_txt_bottom_margin)
                    }
                    else{
                        lp.bottomMargin = 0
                    }

                    healthGridListName.layoutParams = lp

                    return true; // true because we don't want to skip this frame
                }
            });


        }
    }
}