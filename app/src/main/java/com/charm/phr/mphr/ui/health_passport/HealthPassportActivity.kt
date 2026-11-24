package com.charm.phr.mphr.ui.health_passport

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityHealthPassportBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.PhysicianTemplates
import com.charm.phr.mphr.ui.allergy.AllergyActivity
import com.charm.phr.mphr.ui.dashboard.ModuleSelectListener
import com.charm.phr.mphr.ui.document.DocumentActivity
import com.charm.phr.mphr.ui.physician.PhysicianActivity
import com.charm.phr.mphr.ui.prescription.PrescriptionActivity
import com.charm.phr.mphr.ui.problem_list.ProblemListActivity
import com.charm.phr.mphr.ui.procedure.ProcedureActivity
import com.charm.phr.mphr.ui.questionnaire.health_passport.QuestionnaireListActivity
import com.charm.phr.mphr.ui.questionnaire.questionnaire_detail.QuestionnaireDetailActivity
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Picasso
import javax.inject.Inject

class HealthPassportActivity : MDBaseActivity() , ModuleSelectListener {


    @Inject
    lateinit var healthPassportGridModuleAdapter: HealthPassportGridModuleAdapter

    @Inject
    lateinit var healthPassportListAdapter: HealthPassportListAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var healthPassportViewModel: HealthPassportViewModel

    @Inject
    lateinit var picasso: Picasso

    lateinit var binding: ActivityHealthPassportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthPassportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        healthPassportViewModel = ViewModelProvider(this , viewModelFactory).get(HealthPassportViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.healthPassportToolbar)
        UIUtil.applyViewInsertForBottomView(binding.healthPassportDrawerLayout)

        authManager.selectedPatientLive.observe(this , Observer { patient ->

            if (patient != null) {
                binding.healthPassportToolbarText.text = getString(R.string.dashboard_toolbar_heading, patient.fullName)

/*  val initials = UIUtil.formPatientInitials(patient)

                dashboardNavigationPatientName.text = patient.fullName

                dashboardNavigationPatientDummyPicTxt.visibility = View.VISIBLE
                dashboardNavigationPatientDummyPicTxt.text = initials


                if (patient.isPhotoAvailableOnline && authManager.isUserOnline()){

                    val dashboardNavigationImgSize = resources.getDimensionPixelSize(R.dimen.patient_list_img_size)

                    val patientImageURl = APIList.getPatientImageURL(patient.practicePatientId)
                    Log.d("MDPatLoad" , "URL -> ${patientImageURl}")

                    val dashboardNavImgLoader =  picasso.load(patientImageURl)
                    dashboardNavigationPatientDummyPicTxt.visibility = View.VISIBLE
                    dashboardNavImgLoader.resize(dashboardNavigationImgSize , dashboardNavigationImgSize).into(dashboardNavigationPatientImg ,  object : Callback {

                        override fun onSuccess() {
                            dashboardNavigationPatientDummyPicTxt.visibility = View.GONE

                        }

                        override fun onError(e: Exception?) {
                        }
                    })
                }


                dashboardNavigationPatientDetail.text = UIUtil.formPatientDetail(patient)
*/


            }
        })

        binding.healthPassportGridRV.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        binding.healthPassportGridRV.adapter = healthPassportGridModuleAdapter
        binding.healthPassportGridRV.addItemDecoration(GridSpacingItemDecoration(2 ,resources.getDimensionPixelSize(R.dimen.dashboard_card_margin)  , includeEdge = true))


        binding.healthPassportListRV.layoutManager = LinearLayoutManager(this , RecyclerView.VERTICAL  , false )
        binding.healthPassportListRV.adapter = healthPassportListAdapter

        binding.healthPassportMenuBtn.setOnClickListener {
            onBackPressed()
        }



        healthPassportGridModuleAdapter.setModuleSelectListener(this)
        healthPassportListAdapter.setModuleSelectListener(this)

    }

    override fun onModuleSelect(moduleId: String){

            val classInstance = when (moduleId) {
                ModelUtil.prescriptionModelId -> PrescriptionActivity::class.java
                ModelUtil.allergiesModelId -> AllergyActivity::class.java
                ModelUtil.procedureModelId -> ProcedureActivity::class.java
                ModelUtil.questionnaireModelId -> QuestionnaireListActivity::class.java
                ModelUtil.physicianModelId -> PhysicianActivity::class.java
                ModelUtil.documentModelId -> DocumentActivity::class.java
                ModelUtil.permissionsModelId -> QuestionnaireDetailActivity::class.java
                else -> ProblemListActivity::class.java
            }


            val intent = Intent(this, classInstance)
            intent.putExtra(ModelUtil.isHealthPassportIntent, true)
            if (moduleId == ModelUtil.permissionsModelId){

                if (authManager.questionnaireHPList != null){

                    val questionnaire = authManager.questionnaireHPList?.firstOrNull { it.name == ModelUtil.questionnaireNameValuePermission }

                    if (questionnaire != null){
                        intent.putExtra(ModelUtil.questionnairePatQuesMapIdIntent , questionnaire.patientQuesMap?.patientQuesMapId)
                        intent.putExtra(ModelUtil.questionnaireNameIntent , questionnaire.name)
                        startActivityForResult(intent, 0)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                    else{
                        UIUtil.showToast(this , R.string.error_occurred_try_again , Toast.LENGTH_SHORT)
                    }
                }
                else{

                    progressDialog.show()
                    healthPassportViewModel.getQuestionnaireList().observe(this , Observer {resp ->

                        progressDialog.dismiss()
                        if(resp != null) {
                            if (resp is ArrayList<*>) {
                                val response = resp as ArrayList<PhysicianTemplates>

                                val questionnaireResp = response.firstOrNull { it.name == ModelUtil.questionnaireNameValuePermission }

                                if (questionnaireResp != null) {
                                    intent.putExtra(ModelUtil.questionnairePatQuesMapIdIntent, questionnaireResp.patientQuesMap?.patientQuesMapId)
                                    intent.putExtra(ModelUtil.questionnaireNameIntent, questionnaireResp.name)
                                    startActivityForResult(intent, 0)
                                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                }
                                else{
                                    UIUtil.showToast(this , R.string.error_occurred_try_again , Toast.LENGTH_SHORT)
                                }
                            }
                            else if (resp is APIResponseError){
                                UIUtil.showToast(this , R.string.error_occurred_try_again , Toast.LENGTH_SHORT)
                            }
                        }
                    })
                }
            }
            else {

                if (moduleId == ModelUtil.prescriptionModelId){
                    startActivityForResult(intent, 2)
                }
                else {
                    startActivityForResult(intent, 0)
                }
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2){
            if (resultCode == Activity.RESULT_OK){
                healthPassportViewModel.isMedicationUpdated = true
            }
        }


    }

    override fun onBackPressed() {

        if (healthPassportViewModel.isMedicationUpdated){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}