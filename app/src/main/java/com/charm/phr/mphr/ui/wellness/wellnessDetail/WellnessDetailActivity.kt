package com.charm.phr.mphr.ui.wellness.wellnessDetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityWellnessBinding
import com.charm.phr.mphr.ui.wellness.addWellness.AddWellnessActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.MDParsingHelper
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.ModelUtil.Companion.parseStringAsJsonObject
import com.charm.phr.mphr.util.RbacUtil
import com.charm.phr.mphr.util.UIUtil
import com.charm.phr.mphr.util.UIUtil.Companion.hideView
import javax.inject.Inject

class WellnessDetailActivity : MDBaseActivity(), WellnessList2StartListener {

    lateinit var wellnessDetailViewModel: WellnessDetailViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var binding: ActivityWellnessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWellnessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wellnessDetailViewModel = ViewModelProvider(this, viewModelFactory).get(WellnessDetailViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.wellnessToolbar)
        UIUtil.applyViewInsertForBottomView(binding.wellnessRootLayout)

        wellnessDetailViewModel.trackerId = intent.getStringExtra(ModelUtil.wellnessTrackerIdIntent)
                ?: ""
        wellnessDetailViewModel.wellnessTracker = intent.getStringExtra(ModelUtil.wellnessTrackerIntent)
                ?: ""
        wellnessDetailViewModel.isDefault = intent.getBooleanExtra(ModelUtil.wellnessIsDefaultIntent, false)

        val trackerMetricMapStr = intent.getStringExtra(ModelUtil.wellnessTrackerDetailIntent) ?: ""
        val metricId = intent.getStringExtra(ModelUtil.wellnessMetricIdIntent) ?: ""
        if (trackerMetricMapStr.isNotBlank()) {
            val trackerobj = trackerMetricMapStr.parseStringAsJsonObject()
            if (trackerobj != null) {
                wellnessDetailViewModel.trackerMetricMap = MDParsingHelper.getVitalTrackerObj(trackerobj, metricId)
            } else {
                wellnessDetailViewModel.trackerMetricMap = null
            }
        }

        val toolbarTxt = if (wellnessDetailViewModel.isDefault) {

            if (wellnessDetailViewModel.wellnessTracker == ModelUtil.wellnessGeneralModelId) {
                wellnessDetailViewModel.wellnessMetric = intent.getStringExtra(ModelUtil.wellnessMetricIntent)
                        ?: ""

                wellnessDetailViewModel.metricId = intent.getStringExtra(ModelUtil.wellnessMetricIdIntent)
                        ?: ""

                getString(R.string.wellness_weight_and_height_txt)

            } else if (wellnessDetailViewModel.wellnessTracker == ModelUtil.wellnessOthersModelId) {
                wellnessDetailViewModel.wellnessMetric = intent.getStringExtra(ModelUtil.wellnessMetricIntent)
                        ?: ""

                wellnessDetailViewModel.metricId = intent.getStringExtra(ModelUtil.wellnessMetricIdIntent)
                        ?: ""

                wellnessDetailViewModel.wellnessMetric
            } else {


                when (wellnessDetailViewModel.wellnessTracker) {
                    ModelUtil.wellnessBPModelId -> getString(R.string.wellness_bp_toolbar_txt)
                    ModelUtil.wellnessPulseModelId -> getString(R.string.wellness_pulse_toolbar_txt)
                    ModelUtil.wellnessTemperatureModelId -> getString(R.string.wellness_temperature_toolbar_txt)
                    ModelUtil.wellnessCholesterolModelId -> getString(R.string.wellness_cholesterol_txt)

                    else -> getString(R.string.wellness_blood_sugar_toolbar_txt)
                }
            }
        } else {
            wellnessDetailViewModel.wellnessTracker
        }

        binding.wellnessToolbarText.setText(toolbarTxt)


        val wellnessListFragment = WellnessList1Fragment()

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
        fragmentTransaction.replace(R.id.wellnessMainLayout, wellnessListFragment, "wellnessListFragment")
        fragmentTransaction.commit()

        binding.wellnessBackBtn.setOnClickListener {
            onBackPressed()
        }
        if(!RbacUtil.checkPrivilege(RbacUtil.Companion.RBACPrivileges.ADD_WELLNESS,this.applicationContext))
        {
            binding.wellnessAddBtn.hideView()
        }
        binding.wellnessAddBtn.setOnClickListener {
            val addIntent = Intent(this, AddWellnessActivity::class.java)
            addIntent.putExtra(ModelUtil.wellnessTrackerDetailIntent, intent.getStringExtra(ModelUtil.wellnessTrackerDetailIntent)
                    ?: "")
            addIntent.putExtra(ModelUtil.wellnessMetricIdIntent, wellnessDetailViewModel.metricId)
            startActivityForResult(addIntent, 0)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {

                if (wellnessDetailViewModel.isDefault && (wellnessDetailViewModel.wellnessTracker == ModelUtil.wellnessGeneralModelId || wellnessDetailViewModel.wellnessTracker == ModelUtil.wellnessBPModelId)){
                    wellnessDetailViewModel.isDashboardVitalsUpdated = true
                }

                setResult(Activity.RESULT_OK)

                val currentFragment = supportFragmentManager.findFragmentById(R.id.wellnessMainLayout)
                if (currentFragment is WellnessList1Fragment) {
                    currentFragment.fetchWellnessDetail()
                }


            }
        }
    }


    override fun onBackPressed() {


/* val currentFragment = supportFragmentManager.findFragmentById(R.id.wellnessMainLayout)

if (currentFragment is WellnessList2Fragment){
supportFragmentManager.popBackStack()
}
else{*/

        if (wellnessDetailViewModel.isDashboardVitalsUpdated){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
// }
    }

    fun floatingActionButtonVisiblity(): Int = binding.wellnessAddBtn.visibility

    fun showFab(){
        binding.wellnessAddBtn.show()
    }

    fun hideFab(){
        binding.wellnessAddBtn.hide()
    }

    override fun createNextFragment() {

    }

}

interface WellnessList2StartListener {
    fun createNextFragment()
}