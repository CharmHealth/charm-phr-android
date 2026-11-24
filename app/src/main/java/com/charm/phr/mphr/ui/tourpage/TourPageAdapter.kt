package com.charm.phr.mphr.ui.tourpage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.LayoutTourPageItemBinding
import com.charm.phr.mphr.model.TourPageModel
import com.charm.phr.mphr.util.BuildUtil

class TourPageAdapter(private val context: Context) : androidx.viewpager.widget.PagerAdapter() {


    private val normalImgWidth =
        context.resources.getDimensionPixelSize(R.dimen.tp_common_image_width)
    private val normalImgHeight =
        context.resources.getDimensionPixelSize(R.dimen.tp_common_image_height)
    private val appIconImgSize = context.resources.getDimensionPixelSize(R.dimen.tp_app_image_size)

    public val tourList = if (BuildUtil.isCustomApp) {
        arrayListOf(
            TourPageModel(
                R.string.tp_app_custom_icon_heading,
                R.string.tp_app_icon_content,
                R.drawable.ic_tourpage_app_icon
            ),
            TourPageModel(
                R.string.tp_appointment_heading,
                R.string.tp_appointment_content,
                R.drawable.ic_tourpage_appointment
            ),
            TourPageModel(
                R.string.tp_messages_heading,
                R.string.tp_messages_content,
                R.drawable.ic_tourpage_messages
            ),
            TourPageModel(
                R.string.tp_lab_heading,
                R.string.tp_lab_content,
                R.drawable.ic_tourpage_labs
            )
        )

    } else if (BuildUtil.isThalassemia) {
        arrayListOf(
            TourPageModel(
                R.string.tp_app_custom_icon_heading,
                R.string.tp_app_icon_content,
                R.drawable.ic_thalassure_logo
            )
        )
    } else {

        arrayListOf(
            TourPageModel(
                R.string.tp_app_icon_heading,
                R.string.tp_app_icon_content,
                R.drawable.ic_tourpage_app_icon
            ),
            TourPageModel(
                R.string.tp_health_passport_heading,
                R.string.tp_health_passport_content,
                R.drawable.ic_tourpage_health_passport
            ),
            TourPageModel(
                R.string.tp_appointment_heading,
                R.string.tp_appointment_content,
                R.drawable.ic_tourpage_appointment
            ),
            TourPageModel(
                R.string.tp_messages_heading,
                R.string.tp_messages_content,
                R.drawable.ic_tourpage_messages
            ),
            TourPageModel(
                R.string.tp_lab_heading,
                R.string.tp_lab_content,
                R.drawable.ic_tourpage_labs
            )
        )

    }

    override fun isViewFromObject(view: View, objectFromView: Any): Boolean {
        return view == objectFromView
    }

    override fun getCount(): Int = tourList.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val tourModel = tourList[position]

        val binding = LayoutTourPageItemBinding.inflate(
            LayoutInflater.from(container.context),
            container,
            false
        )

        binding.tpItemImg.setImageResource(tourModel.sourceId)
        binding.tpItemHeading.setText(tourModel.headingId)
        binding.tpItemContent.setText(tourModel.contentId)

        val layoutParam = binding.tpItemImg.layoutParams as ConstraintLayout.LayoutParams

        if (tourModel.sourceId == R.drawable.ic_tourpage_app_icon) {
            layoutParam.width = appIconImgSize
        } else {
            layoutParam.width = normalImgWidth
        }
        binding.tpItemImg.layoutParams = layoutParam

        container.addView(binding.root)
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, objectFromView: Any) {
        container.removeView(objectFromView as View)
    }
}