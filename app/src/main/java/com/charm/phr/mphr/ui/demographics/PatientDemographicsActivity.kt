package com.charm.phr.mphr.ui.demographics

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivityPatientDemographicsBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.ui.demographics.demographics_pdf.PatDemoPDFActivity
import com.charm.phr.mphr.util.APIList
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import javax.inject.Inject

class PatientDemographicsActivity : MDBaseActivity() {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var patDemoViewModel: PatientDemographicsViewModel


    @Inject
    lateinit var picasso: Picasso

    private lateinit var animOut: AnimatorSet
    private lateinit var animIn: AnimatorSet

    private var isBackVisible = false

    lateinit var binding: ActivityPatientDemographicsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientDemographicsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        patDemoViewModel = ViewModelProvider(this, viewModelFactory).get(PatientDemographicsViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.patDemographicsToolbar)
        UIUtil.applyViewInsertForBottomView(binding.patientDemographicsRootLayout)

        animOut = AnimatorInflater.loadAnimator(this, R.animator.flip_out) as AnimatorSet
        animIn = AnimatorInflater.loadAnimator(this, R.animator.flip_in) as AnimatorSet


        patDemoViewModel.demographicsLive.observe(this, Observer { patient ->

            if (patient != null) {


                binding.patDemographicsProgress.root.visibility = View.GONE
                if (patient is Patient) {

                    patDemoViewModel.patient = patient

                    binding.patDemographicsMainLayout.visibility = View.VISIBLE

                    binding.patDemographicsNameTV.text = patient.fullName

                    binding.patDemographicsImgDummyTV.text = UIUtil.formPatientInitials(patient)


                    showPatientImg()

                    val isGenderPresent = !patient.gender.isNullOrBlank()
                    if (isGenderPresent) {
                        binding.patDemographicsGender.text = patient.gender?.replaceFirstChar{ it.uppercase() }
                    }

                    val isDOBPresent = patient.dateOfBirth != null

                    if (isDOBPresent) {
                        binding.patDemographicsDOB.text = patient.dateOfBirth?.toString(ModelUtil.charmDisplayDateFormat())
                        if (isGenderPresent) {
                            binding.patDemographicsDetailSeparator1.visibility = View.VISIBLE
                        } else {
                            binding.patDemographicsDetailSeparator1.visibility = View.GONE
                        }
                    } else {
                        binding.patDemographicsDetailSeparator1.visibility = View.GONE
                    }

                    if (!patient.bloodGroup.isNullOrBlank()) {
                        binding.patDemographicsBloodGrpTV.visibility = View.VISIBLE
                        binding.patDemographicsBloodGrpTV.text = patient.bloodGroup

                        if (isDOBPresent || isGenderPresent) {
                            binding.patDemographicsDetailSeparator2.visibility = View.VISIBLE
                        } else {
                            binding.patDemographicsDetailSeparator2.visibility = View.GONE
                        }
                    } else {
                        binding.patDemographicsBloodGrpTV.visibility = View.GONE
                        binding.patDemographicsDetailSeparator2.visibility = View.GONE
                    }

                    if (!patient.contactDetails?.mobile.isNullOrBlank()) {
                        /*   patDemographicsContactLabel.visibility = View.VISIBLE
                           patDemographicsContactTV.visibility = View.VISIBLE*/
                        binding.patDemographicsContactTV.text = patient.contactDetails?.mobile
                        binding.patDemographicsContactTV.setTextColor(ContextCompat.getColor(this, R.color.patDemoLabelTV))

                    } else {
                        /*  patDemographicsContactLabel.visibility = View.GONE
                          patDemographicsContactTV.visibility = View.GONE*/
                        binding.patDemographicsContactTV.setText(R.string.pat_demographics_not_available_str)
                        binding.patDemographicsContactTV.setTextColor(ContextCompat.getColor(this, R.color.patDemoLabelNotAvailableTV))
                    }

                    val addressArr = arrayListOf<String?>(patient.postalAddress?.line1, patient.postalAddress?.line2, patient.postalAddress?.city, patient.postalAddress?.state, patient.postalAddress?.postalCode)

                    val address = addressArr.filter { !it.isNullOrBlank() }.joinToString(", ")


                    if (address.isNotBlank()) {
                        binding.patDemographicsAddressLine1TV.text = address
                        binding.patDemographicsAddressLine1TV.setTextColor(ContextCompat.getColor(this, R.color.patDemoLabelTV))
                    } else {
                        binding.patDemographicsAddressLine1TV.setText(R.string.pat_demographics_not_available_str)
                        binding.patDemographicsAddressLine1TV.setTextColor(ContextCompat.getColor(this, R.color.patDemoLabelNotAvailableTV))
                    }


                    if (!patient.caregiverFullName.isNullOrBlank()) {
                        /*patDemographicsCaregiverNameLabel.visibility = View.VISIBLE
                        patDemographicsCaregiverNameTV.visibility = View.VISIBLE*/
                        binding.patDemographicsCaregiverNameTV.text = patient.caregiverFullName
                        binding.patDemographicsCaregiverNameTV.setTextColor(ContextCompat.getColor(this, R.color.patDemoLabelTV))
                    } else {
                        /*patDemographicsCaregiverNameLabel.visibility = View.GONE
                        patDemographicsCaregiverNameTV.visibility = View.GONE*/
                        binding.patDemographicsCaregiverNameTV.setText(R.string.pat_demographics_not_available_str)
                        binding.patDemographicsCaregiverNameTV.setTextColor(ContextCompat.getColor(this, R.color.patDemoLabelNotAvailableTV))


                    }

                    if (!patient.caregiverMobile.isNullOrBlank()) {
                        /*           patDemographicsCaregiverNumberLabel.visibility = View.VISIBLE
                                   patDemographicsCaregiverNumberTV.visibility = View.VISIBLE*/
                        binding.patDemographicsCaregiverNumberTV.text = patient.caregiverMobile
                        binding.patDemographicsCaregiverNumberTV.setTextColor(ContextCompat.getColor(this, R.color.patDemoLabelTV))

                    } else {
                        /* patDemographicsCaregiverNumberLabel.visibility = View.GONE
                         patDemographicsCaregiverNumberTV.visibility = View.GONE*/
                        binding.patDemographicsCaregiverNumberTV.setText(R.string.pat_demographics_not_available_str)
                        binding.patDemographicsCaregiverNumberTV.setTextColor(ContextCompat.getColor(this, R.color.patDemoLabelNotAvailableTV))

                    }


                    val url = "<a href='${patient.hpModelURL}'>${patient.hpModelURL}</a>"


                    binding.patDemographicsURLTV.text = HtmlCompat.fromHtml(url, 0)
                    binding.patDemographicsURLTV.movementMethod = LinkMovementMethod.getInstance()


                } else if (patient is APIResponseError) {
                    binding.patDemographicsErrorPage.root.visibility = View.VISIBLE
                }
            }
        })


        fetchPatDemographics()

        binding.patDemographicsErrorPage.errorPageTryAgainBtn.setOnClickListener {
            fetchPatDemographics()
        }



        binding.patientDemographicsPDFConstraintLayout.setOnClickListener {

            val intent = Intent(this, PatDemoPDFActivity::class.java)
            startActivityForResult(intent, 0)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            /*  val intent = Intent(this , SavePatDemographicsActivity::class.java)
              intent.putExtra(ModelUtil.patientEditIntent , patDemoViewModel.patient.toString)
              startActivityForResult(intent , 0)
              overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)*/

        }

        /*patientDemographicsPDFConstraintLayout.setOnClickListener {
            val intent = Intent(this , PatDemoPDFActivity::class.java)
            startActivityForResult(intent , 0)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        }*/


        binding.patDemographicsPasscodeBtn.setOnClickListener {

            val screenWidth = UIUtil.getScreenWidth(this)

            val width = screenWidth - (2 * resources.getDimensionPixelSize(R.dimen.common_dialog_horizontal_margin))

            val patDemographicsPasscodeDialog = PatientDemographicsPasscodeDialog(this, width, patDemoViewModel.patient.passcode
                    ?: "")
            patDemographicsPasscodeDialog.show()
        }

        binding.patDemographicsHealthURLShareTV.setOnClickListener {
            shareURL()
        }

        binding.patDemographicsHealthURLShareImg.setOnClickListener {
            shareURL()
        }


        binding.patDemographicsBackBtn.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                showPatientImg(true)
                fetchPatDemographics()
            }
        }
    }

/*
    fun flipCard() {
        patDemographicsHealthCardFrontCard.elevation = 0f
        patDemographicsHealthCardBackCard.elevation = 0f
        if (!isBackVisible) {
            animOut.setTarget(patDemographicsHealthCardFront)
            animIn.setTarget(patDemographicsHealthCardBack)
            animOut.start()
            animIn.start()
            isBackVisible = true
            patDemographicsURLTV.movementMethod = null

        }
        else {
            animOut.setTarget(patDemographicsHealthCardBack)
            animIn.setTarget(patDemographicsHealthCardFront)
            animOut.start()
            animIn.start()
            isBackVisible = false
            patDemographicsURLTV.movementMethod = LinkMovementMethod.getInstance()

        }

        handler.postDelayed({
            if (isBackVisible){
                patDemographicsHealthCardBackCard.elevation = 2f
            }
            else {
                patDemographicsHealthCardFrontCard.elevation = 2f
            }
        },500)
    }
*/

    private fun fetchPatDemographics() {
        binding.patDemographicsErrorPage.root.visibility = View.GONE
        binding.patDemographicsMainLayout.visibility = View.GONE
        binding.patDemographicsProgress.root.visibility = View.VISIBLE
        patDemoViewModel.fetchDemographics()
    }

    override fun onBackPressed() {
        if (patDemoViewModel.setResultOK) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showPatientImg(setInViewModel: Boolean = false) {
        val isPhotoAvailable = authManager.selectedPatientLive.value?.isPhotoAvailableOnline
                ?: false

        if (isPhotoAvailable) {

            if (setInViewModel) {
                patDemoViewModel.setResultOK = true
            }
            val patImgSize = resources.getDimensionPixelSize(R.dimen.pat_demo_img_size)

            val patientImageURl = APIList.getPatientImageURL(patDemoViewModel.patient.practicePatientId)
            Log.d("MDPatLoad", "URL -> ${patientImageURl}")
            binding.patDemographicsImgDummyTV.visibility = View.VISIBLE
            binding.patDemographicsImgLayout.visibility = View.INVISIBLE
            val dashboardNavImgLoader = picasso.load(patientImageURl)
            dashboardNavImgLoader.networkPolicy(NetworkPolicy.NO_CACHE)
            dashboardNavImgLoader.memoryPolicy(MemoryPolicy.NO_CACHE)
            dashboardNavImgLoader.resize(patImgSize, patImgSize).into(binding.patDemographicsImg, object : Callback {
                override fun onSuccess() {

                    try {
                        binding.patDemographicsImgDummyTV.visibility = View.INVISIBLE
                        binding.patDemographicsImgLayout.visibility = View.VISIBLE
                    } catch (e: Exception) {

                    }

                }

                override fun onError(e: Exception?) {
                    try {
                        binding.patDemographicsImgLayout.visibility = View.INVISIBLE
                    } catch (e: Exception) {
                    }

                }
            })
        } else {
            binding.patDemographicsImgDummyTV.visibility = View.VISIBLE
            binding.patDemographicsImgLayout.visibility = View.INVISIBLE
        }
    }

    private fun shareURL() {

        try {

            val intent = Intent(Intent.ACTION_SEND)
            val shareBody = "${patDemoViewModel.patient.hpModelURL}\nThis URL contains current Health Data of ${patDemoViewModel.patient.fullName}."
            intent.type = "text/plain"

            intent.putExtra(Intent.EXTRA_SUBJECT, "Share Health Passport URL")
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)

            startActivity(Intent.createChooser(intent, "Share Health Passport URL"))
        } catch (e: ActivityNotFoundException) {
            UIUtil.showToast(this, R.string.error_no_apps_to_share, Toast.LENGTH_LONG)
        }
    }
}