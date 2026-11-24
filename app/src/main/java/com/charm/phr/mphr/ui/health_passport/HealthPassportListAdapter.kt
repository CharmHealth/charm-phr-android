package com.charm.phr.mphr.ui.health_passport

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemHealthListBinding
import com.charm.phr.mphr.model.Module
import com.charm.phr.mphr.ui.dashboard.ModuleSelectListener
import com.charm.phr.mphr.util.ModelUtil

class HealthPassportListAdapter : RecyclerView.Adapter<HealthPassportListAdapter.ViewHolder>() {

    private var moduleList:ArrayList<Module> = arrayListOf(
                Module(ModelUtil.allergiesModelId, R.string.allergy_str, R.drawable.ic_hp_allergies),
                Module(ModelUtil.procedureModelId, R.string.procedure_health_passport_str, R.drawable.ic_hp_procedure),
                Module(ModelUtil.questionnaireModelId , R.string.questionnaire_str , R.drawable.ic_hp_questionnaires),
                Module(ModelUtil.permissionsModelId , R.string.permissions_str , R.drawable.ic_hp_permissions)
        )


    private var moduleSelectListener: ModuleSelectListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthPassportListAdapter.ViewHolder {
        val binding = ItemHealthListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemHealthListBinding): RecyclerView.ViewHolder(binding.root) {

    val healthListName: TextView = binding.healthListName
    val healthListIcon: ImageView = binding.healthListIcon
    val healthListNotificationTV: TextView = binding.healthListNotificationTV
    val healthListNxtBtn: ImageView = binding.healthListNxtBtn

        fun onBind(module: Module) {

            healthListName.setText(module.moduleName)
            healthListIcon.setImageResource(module.moduleIconId)
      //      healthListName.setTextColor(ContextCompat.getColor(itemView.context , R.color.colorAccent))
            healthListNxtBtn.setImageResource(R.drawable.ic_hp_nav_next)
            healthListNotificationTV.visibility = View.GONE

        }
    }
}
class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int , private val topSpace:Int = 0, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {



    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        val top = if (topSpace == 0){
            spacing
        }
        else{
            topSpace
        }
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)

            if (position < spanCount) { // top edge
                outRect.top = top
            }
            outRect.bottom = top // item bottom
        } else {
            outRect.left = column * spacing / spanCount // column * ((1f / spanCount) * spacing)
            outRect.right = spacing - (column + 1) * spacing / spanCount // spacing - (column + 1) * ((1f /    spanCount) * spacing)
            if (position >= spanCount) {
                outRect.top = top // item top
            }
        }
    }
}