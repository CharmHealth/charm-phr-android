package com.charm.phr.mphr.ui.patientList

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityPatientListBinding
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.ui.dashboard.DashboardActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.UIUtil
import javax.inject.Inject

class PatientListActivity : MDBaseActivity() {

    @Inject
    lateinit var patientListAdapter: PatientListAdapter

    lateinit var binding: ActivityPatientListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        UIUtil.applyViewInsertForToolbar(binding.patientListToolbar)
        UIUtil.applyViewInsertForBottomView(binding.patientListMainLayout)

        binding.patientListRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.patientListRecyclerView.adapter = patientListAdapter

     //   ModelUtil.showMissingScopes()

    /*    val dateTime =  DateTime.parse("2021-04-19 18:00:00.0" , ModelUtil.charmDateFormatForVitals())

        Log.d("selectedDate" ," ${dateTime.toString(ModelUtil.charmDisplayDateFormat())}")*/
        patientListAdapter.setPatientSelectListener(object :PatientListAdapter.PatientSelectListener{
            override fun onPatientSelect(patient: Patient, view: View) {

                authManager.questionnaireHPList = null
                authManager.updateSelectedPatient(patient)
                val intent = Intent(this@PatientListActivity , DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            }
        })

        patientListAdapter.setItems(authManager.patientList)

    }
}
