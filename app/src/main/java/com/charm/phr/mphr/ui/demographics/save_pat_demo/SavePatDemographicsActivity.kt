package com.charm.phr.mphr.ui.demographics.save_pat_demo

import android.Manifest
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionManager
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivitySavePatDemographicsBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.model.PatientCommonData
import com.charm.phr.mphr.ui.components.MDAutoCompleteTextViewAdapter
import com.charm.phr.mphr.ui.components.MDClickToSelectEdittext
import com.charm.phr.mphr.ui.components.mdCamera.ImageCropperFragment
import com.charm.phr.mphr.util.APIList
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.PermissionUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import org.joda.time.LocalDate
import java.util.concurrent.Executor
import javax.inject.Inject

class SavePatDemographicsActivity : MDBaseActivity(), ImageCropperFragment.ImageCropperListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var executor: Executor

    @Inject
    lateinit var picasso: Picasso

    lateinit var savePatDemoViewModel: SavePatDemographicsViewModel

    lateinit var countryAdapter: MDAutoCompleteTextViewAdapter
    lateinit var stateAdapter: MDAutoCompleteTextViewAdapter


    private lateinit var backPressAlertDialogBuilder: android.app.AlertDialog.Builder

    val imm: InputMethodManager by lazy {
        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    }

    lateinit var binding: ActivitySavePatDemographicsBinding

    private val TAG = "SavePatDemoActivity"

    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    private var capturedImageUri: Uri? = null

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                capturedImageUri?.let {
                    val (rotatedBitmap, rotatedFilePath) = FileUtil.getRotatedBitmapAndSave(
                        this,
                        it
                    )
                    if (rotatedFilePath != null) {
                        intentToImagerCropper(rotatedFilePath)
                    } else {
                        UIUtil.showToast(
                            this,
                            getString(R.string.error_occurred_while_handling_photo),
                            Toast.LENGTH_SHORT
                        )
                    }
                }
            }
        }


    private val pickMediaLauncher =
        registerForActivityResult(PickVisualMedia()) { contentUri ->
            onImageSelectHandler(contentUri)
        }

    private val legacyPicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { contentUri ->
        onImageSelectHandler(contentUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavePatDemographicsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        savePatDemoViewModel = ViewModelProvider(this, viewModelFactory)[SavePatDemographicsViewModel::class.java]

        UIUtil.applyViewInsertForToolbar(binding.savePatDemoToolbar)
        UIUtil.applyViewInsertForBottomView(binding.savePatDemoLayout)


        initializePermissionResultLauncher()

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        /* val patientStr = intent.getStringExtra(ModelUtil.patientEditIntent) ?: ""
         savePatDemoViewModel.patient = MDParsingHelper.formPatientObj(JsonParser().parse(patientStr).asJsonObject)
 */
        stateAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())
        countryAdapter = MDAutoCompleteTextViewAdapter(this, arrayListOf())

        binding.savePatDemoStateET.setAdapter(stateAdapter)
        binding.savePatDemoCountryET.setAdapter(countryAdapter)

        savePatDemoViewModel.fetchMiscLiveData.observe(this, Observer { resp ->
            if (resp != null) {
                binding.savePatDemoProgress.root.visibility = View.GONE
                if (resp is PatientCommonData) {
                    binding.savePatDemoMainLayout.visibility = View.VISIBLE
                    countryAdapter.setItemList(resp.country.mapNotNull { it.name })

                    binding.savePatDemoGenderET.setItems(
                        arrayListOf(
                            ModelUtil.genderMale,
                            ModelUtil.genderFemale,
                            ModelUtil.genderUnknown
                        ), this
                    )
                    binding.savePatDemoBloodGroupET.setItems(resp.bloodGrpList, this)
                    fillValues(savePatDemoViewModel.patient)

                    setEnabled(false)

                    binding.savePatDemoEditBtn.visibility = View.VISIBLE
                } else if (resp is APIResponseError) {
                    binding.savePatDemoErrorPage.root.visibility = View.VISIBLE
                }
            }
        })


        val pat = authManager.selectedPatientLive.value

        binding.savePatDemoDummyTV.text = UIUtil.formPatientInitials(pat)

        if (pat != null) {
            if (pat.isPhotoAvailableOnline && savePatDemoViewModel.filePath.isNullOrBlank()) {

                val patImgSize = resources.getDimensionPixelSize(R.dimen.save_pat_demo_img_size)
                val dashboardNavImgLoader =
                    picasso.load(APIList.getPatientImageURL(pat.practicePatientId))
                dashboardNavImgLoader.networkPolicy(NetworkPolicy.NO_CACHE)
                dashboardNavImgLoader.memoryPolicy(MemoryPolicy.NO_CACHE)
                dashboardNavImgLoader.resize(patImgSize, patImgSize).into(
                    binding.savePatDemoImg,
                    object : Callback {
                        override fun onSuccess() {

                            try {
                                binding.savePatDemoImg.visibility = View.VISIBLE
                                binding.savePatDemoDummyTV.visibility = View.GONE
                            } catch (e: Exception) {

                            }
                        }

                        override fun onError(e: Exception?) {
                        }
                    })
            }
        }

        binding.savePatDemoCountryET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                if (editable.toString().trim() != "") {
                    val textInputLayout =
                        (binding.savePatDemoCountryET.parent as ViewGroup).parent as TextInputLayout
                    if (binding.savePatDemoCountryET.isGravityCenter) {
                        TransitionManager.beginDelayedTransition(binding.savePatDemoCountryET.parent as ViewGroup)
                        binding.savePatDemoCountryET.gravity = Gravity.BOTTOM
                        textInputLayout.setPadding(
                            binding.savePatDemoCountryET.edittextPadding,
                            binding.savePatDemoCountryET.edittextPadding,
                            binding.savePatDemoCountryET.edittextPadding,
                            binding.savePatDemoCountryET.edittextPadding
                        )
                        binding.savePatDemoCountryET.isGravityCenter = false
                    }
                }

                if (editable.toString().equals("india", ignoreCase = true)) {

                    if (savePatDemoViewModel.inStates.isNotEmpty()) {
                        stateAdapter.setItemList(savePatDemoViewModel.inStates)
                    } else {
                        fetchStates(ModelUtil.patientInState)
                    }
                } else if (editable.toString().equals("united states", ignoreCase = true)) {

                    if (savePatDemoViewModel.usStates.isNotEmpty()) {
                        stateAdapter.setItemList(savePatDemoViewModel.usStates)
                    } else {
                        fetchStates(ModelUtil.patientUsState)
                    }
                } else {
                    stateAdapter.setItemList(arrayListOf())
                }
            }
        })

        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    super.onBackPressed()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }

                DialogInterface.BUTTON_NEGATIVE -> {

                }
            }
        }

        backPressAlertDialogBuilder = android.app.AlertDialog.Builder(this)
        backPressAlertDialogBuilder.setMessage(R.string.discard_changes_confirmation_string)
            .setPositiveButton(R.string.yes_str, dialogClickListener)
            .setNegativeButton(R.string.no_str, dialogClickListener)


        binding.savePatDemoDOBET.setOnClickListener {
            showDatePicker()
        }

        binding.savePatDemoDOBET.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {
                showDatePicker()
            }
        }

        binding.savePatDemoBackBtn.setOnClickListener {
            onBackPressed()
        }

        binding.savePatDemoSaveBtn.setOnClickListener {
            val patient = validateData()
            if (patient != null) {
                progressDialog.show()
                savePatDemoViewModel.savePatient(patient).observe(this, Observer { resp ->
                    if (resp != null) {
                        progressDialog.dismiss()
                        if (resp is Boolean) {
                            savePatDemoViewModel.isDataSaved = true
                            setEnabled(false)
                            binding.savePatDemoSaveBtn.visibility = View.GONE
                            binding.savePatDemoEditBtn.visibility = View.VISIBLE
                            UIUtil.showToast(
                                this,
                                R.string.save_pat_success_msg,
                                Toast.LENGTH_SHORT
                            )
                        } else if (resp is APIResponseError) {
                            UIUtil.showToast(
                                this,
                                R.string.error_save_pat_failed,
                                Toast.LENGTH_SHORT
                            )
                        }
                    }
                })
            }
        }

        if (savePatDemoViewModel.patientCommonData?.country.isNullOrEmpty()) {
            fetchCommonData()
        }

        binding.savePatDemoChoosePic.setOnClickListener {
            if (binding.savePatDemoSaveBtn.visibility == View.VISIBLE)
                chooseDialog()
        }

        binding.savePatDemoEditBtn.setOnClickListener {
            setEnabled(true)
            binding.savePatDemoEditBtn.visibility = View.GONE
            binding.savePatDemoSaveBtn.visibility = View.VISIBLE

            binding.savePatDemoFirstNameET.requestFocus()
            imm.showSoftInput(binding.savePatDemoFirstNameET, InputMethodManager.SHOW_IMPLICIT)

        }

        binding.savePatDemoErrorPage.errorPageTryAgainBtn.setOnClickListener {
            fetchCommonData()
        }

        if(BuildUtil.isThalassemia){
            binding.savePatDemoToolbarText.text=getString(R.string.my_profile)
        }
    }


    private fun chooseDialog() {


        val builder = AlertDialog.Builder(this)
        builder.setItems(arrayOf("Take photo", "Choose photo")) { _, posn ->

            if (posn == 0) {
                PermissionUtil.requestCameraPermission(
                    this,
                    ::permissionAcceptCallBack,
                    requestPermissionLauncher
                )
            } else {
                intentToGallery()
            }
        }
        builder.setTitle(R.string.chg_prof_pic_str)

        builder.create().show()
    }

    private fun intentToGallery() {
        PermissionUtil.launchGalleryPicker(pickMediaLauncher, legacyPicker)
    }

    private fun intentToImagerCropper(uriString: String) {

        val imageCropperFragment = ImageCropperFragment()

        imageCropperFragment.setImageCropperListener(this)
        imageCropperFragment.imageFilePath = uriString
        savePatDemoViewModel.isImageCropperFragmentPushed = true
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(
            R.anim.slide_in_right,
            R.anim.slide_out_left,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        ft.addToBackStack("imageCropperFragment")
        ft.replace(R.id.savePatDemoLayout, imageCropperFragment, "imageCropperFragment")
        ft.commit()

    }

    private fun setImageIntoView(filePath: String) {

        savePatDemoViewModel.filePath = filePath

        progressDialog.show()
        progressDialog.setMessage(R.string.loading_data)

        executor.execute {

            val bm = FileUtil.getBitmapFromFile(filePath)

            runOnUiThread {

                progressDialog.dismiss()

                if (bm != null) {
                    binding.savePatDemoImg.setImageBitmap(bm)

                    binding.savePatDemoImg.visibility = View.VISIBLE
                    binding.savePatDemoDummyTV.visibility = View.GONE
                } else UIUtil.showToast(
                    this,
                    getString(R.string.error_occurred_while_handling_photo),
                    Toast.LENGTH_SHORT
                )

            }

        }

    }


    private fun startMdCameraForResult() {

        val result = PermissionUtil.launchSystemCamera(
            this,
            cameraLauncher,
            resources.getString(R.string.content_provider_authority)
        )
        capturedImageUri = result.second
    }

    fun fetchStates(country: String) {
        progressDialog.show()
        savePatDemoViewModel.fetchStates(country).observe(this, Observer { resp ->

            if (resp != null) {
                progressDialog.dismiss()
                if (resp is ArrayList<*>) {

                    if (country == ModelUtil.patientInState) {
                        savePatDemoViewModel.inStates.addAll(resp as ArrayList<String>)
                        stateAdapter.setItemList(savePatDemoViewModel.inStates)
                    } else {
                        savePatDemoViewModel.usStates.addAll(resp as ArrayList<String>)
                        stateAdapter.setItemList(savePatDemoViewModel.usStates)
                    }
                } else {
                    stateAdapter.setItemList(arrayListOf())
                }


            }
        })
    }

    fun setEnabled(isEnabled: Boolean) {

        binding.savePatDemoFirstNameET.isEnabled = isEnabled
        binding.savePatDemoLastNameET.isEnabled = isEnabled
        binding.savePatDemoGenderET.isEnabled = isEnabled
        binding.savePatDemoBloodGroupET.isEnabled = isEnabled
        binding.savePatDemoDOBET.isEnabled = isEnabled

        binding.savePatDemoMobilePhoneET.isEnabled = isEnabled
        binding.savePatDemoHomePhoneET.isEnabled = isEnabled


        binding.savePatDemoLine1ET.isEnabled = isEnabled
        binding.savePatDemoLine2ET.isEnabled = isEnabled
        binding.savePatDemoCityET.isEnabled = isEnabled
        binding.savePatDemoStateET.isEnabled = isEnabled
        binding.savePatDemoCountryET.isEnabled = isEnabled

        binding.savePatDemoCaregiverNameET.isEnabled = isEnabled
        binding.savePatDemoCaregiverNumET.isEnabled = isEnabled
        binding.savePatDemoEmgContactNameET.isEnabled = isEnabled
        binding.savePatDemoEmgContactNumET.isEnabled = isEnabled

        binding.savePatDemoCountryLayout.isEnabled = isEnabled
        binding.savePatDemoGenderLayout.isEnabled = isEnabled
        binding.savePatDemoBloodGroupLayout.isEnabled = isEnabled
        binding.savePatDemoStateLayout.isEnabled = isEnabled

        if (isEnabled) {
            binding.savePatDemoChoosePic.visibility = View.VISIBLE
        } else {
            binding.savePatDemoChoosePic.visibility = View.GONE
        }
    }


    fun validateData(): Patient? {

        val firstName = binding.savePatDemoFirstNameET.text.toString().trim()
        val lastName = binding.savePatDemoLastNameET.text.toString().trim()
        val mobileNumber = binding.savePatDemoMobilePhoneET.text.toString().trim()
        val homeNumber = binding.savePatDemoHomePhoneET.text.toString().trim()
        val dob = binding.savePatDemoDOBET.text.toString().trim()
        val bloodGrp = binding.savePatDemoBloodGroupET.text.toString().trim()
        val gender = binding.savePatDemoGenderET.text.toString().trim()

        val addressLine1 = binding.savePatDemoLine1ET.text.toString().trim()
        val addressLine2 = binding.savePatDemoLine2ET.text.toString().trim()
        val city = binding.savePatDemoCityET.text.toString().trim()
        val state = binding.savePatDemoStateET.text.toString().trim()
        val countryName = binding.savePatDemoCountryET.text.toString().trim()

        val caregiverName = binding.savePatDemoCaregiverNameET.text.toString().trim()
        val caregiverNum = binding.savePatDemoCaregiverNumET.text.toString().trim()
        val emgContactName = binding.savePatDemoEmgContactNameET.text.toString().trim()
        val emgContactNum = binding.savePatDemoEmgContactNumET.text.toString().trim()

        var countryKey = ""


        if (isTextInputLayoutBlank(
                binding.savePatDemoFirstNameLayout,
                R.string.error_empty_first_name
            )
        ) {
            return null
        }

        if (isTextInputLayoutBlank(
                binding.savePatDemoFirstNameLayout,
                R.string.error_invalid_first_name,
                true
            )
        ) {
            return null
        }

        if (isTextInputLayoutBlank(
                binding.savePatDemoLastNameLayout,
                R.string.error_empty_last_name
            )
        ) {
            return null
        }

        if (isTextInputLayoutBlank(
                binding.savePatDemoLastNameLayout,
                R.string.error_invalid_last_name,
                true
            )
        ) {
            return null
        }

        if (isTextInputLayoutBlank(
                binding.savePatDemoLine1Layout,
                R.string.error_invalid_line_1,
                true
            )
        ) {
            return null
        }

        if (isTextInputLayoutBlank(
                binding.savePatDemoLine2Layout,
                R.string.error_invalid_line_2,
                true
            )
        ) {
            return null
        }

        if (isTextInputLayoutBlank(
                binding.savePatDemoCountryLayout,
                R.string.error_empty_country
            )
        ) {
            return null
        }

        countryKey = savePatDemoViewModel.patientCommonData?.country?.firstOrNull {
            it.name.equals(
                countryName,
                true
            )
        }?.key ?: ""

        if (countryKey.isBlank()) {
            UIUtil.showToast(this, R.string.error_wrong_country, Toast.LENGTH_SHORT)
            showFocus(binding.savePatDemoCountryLayout, binding.savePatDemoCountryET)
            return null
        }

        if (state.isNotBlank()) {

            if (countryKey == ModelUtil.patientInState) {
                if (!savePatDemoViewModel.inStates.any { state.equals(it, true) }) {
                    UIUtil.showToast(this, R.string.error_wrong_state, Toast.LENGTH_SHORT)
                    showFocus(binding.savePatDemoStateLayout, binding.savePatDemoStateET)
                    return null
                }
            } else if (countryKey == ModelUtil.patientUsState) {
                if (!savePatDemoViewModel.usStates.any { state.equals(it, true) }) {
                    UIUtil.showToast(this, R.string.error_wrong_state, Toast.LENGTH_SHORT)
                    showFocus(binding.savePatDemoStateLayout, binding.savePatDemoStateET)
                    return null
                }
            } else if (isTextInputLayoutBlank(
                    binding.savePatDemoStateLayout,
                    R.string.error_invalid_state,
                    true
                )
            ) {
                return null
            }
        }

        if (isTextInputLayoutBlank(
                binding.savePatDemoCityLayout,
                R.string.error_invalid_city,
                true
            )
        ) {
            return null
        }

        if (isTextInputLayoutBlank(
                binding.savePatDemoMobilePhoneLayout,
                R.string.error_empty_mobile_num
            )
        ) {
            return null
        }

        if (isTextInputLayoutBlank(
                binding.savePatDemoCaregiverNameLayout,
                R.string.error_invalid_caregiver_name,
                true
            )
        ) {
            return null
        }

        if (isTextInputLayoutBlank(
                binding.savePatDemoEmgContactNameLayout,
                R.string.error_invalid_emg_contact_name,
                true
            )
        ) {
            return null
        }

        val patient = savePatDemoViewModel.patient

        patient.firstName = firstName
        patient.lastName = lastName
        patient.dateOfBirth = LocalDate.parse(dob, ModelUtil.charmDisplayDateFormat())
        patient.gender = gender
        patient.bloodGroup = bloodGrp

        val caregiverArr = caregiverName.split(" ")
        patient.caregiverFirstName = caregiverArr[0].trim()
        patient.caregiverLastName = caregiverArr.drop(1).joinToString(" ")
        patient.caregiverMobile = caregiverNum
        patient.caregiverFullName = caregiverName

        patient.emergencyContactName = emgContactName
        patient.emergencyContactNumber = emgContactNum

        patient.contactDetails?.mobile = mobileNumber
        patient.contactDetails?.landline = homeNumber


        patient.postalAddress?.line1 = addressLine1
        patient.postalAddress?.line2 = addressLine2
        patient.postalAddress?.city = city
        patient.postalAddress?.state = state
        patient.postalAddress?.country = countryKey

        patient.filePath = savePatDemoViewModel.filePath

        return patient
    }

    private fun showDatePicker() {

        val value = binding.savePatDemoDOBET.text.toString().trim()

        val localDate = if (value.isNotBlank()) {
            LocalDate.parse(value, ModelUtil.charmDisplayDateFormat())
        } else {
            LocalDate.now()
        }

        val dialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, yr, mnth, dt ->

                val text = LocalDate(yr, mnth + 1, dt)
                binding.savePatDemoDOBET.setText(text.toString(ModelUtil.charmDisplayDateFormat()))

            },
            localDate.year, localDate.monthOfYear - 1, localDate.dayOfMonth
        )

        dialog.datePicker.minDate =
            LocalDate(LocalDate.now().year - 150, 1, 1).toDateTimeAtStartOfDay().millis
        dialog.datePicker.maxDate = System.currentTimeMillis()

        dialog.show()
    }

    fun fillValues(patient: Patient) {

        binding.savePatDemoFirstNameET.setText(patient.firstName)
        binding.savePatDemoLastNameET.setText(patient.lastName)
        binding.savePatDemoGenderET.setText(patient.gender)
        binding.savePatDemoBloodGroupET.setText(patient.bloodGroup)
        binding.savePatDemoDOBET.setText(patient.dateOfBirth?.toString(ModelUtil.charmDisplayDateFormat()))

        binding.savePatDemoMobilePhoneET.setText(patient.contactDetails?.mobile)
        binding.savePatDemoHomePhoneET.setText(patient.contactDetails?.landline)


        binding.savePatDemoLine1ET.setText(patient.postalAddress?.line1)
        binding.savePatDemoLine2ET.setText(patient.postalAddress?.line2)
        binding.savePatDemoCityET.setText(patient.postalAddress?.city)
        binding.savePatDemoStateET.setText(patient.postalAddress?.state)
        binding.savePatDemoCountryET.setText(savePatDemoViewModel.patientCommonData?.country?.firstOrNull {
            it.key.equals(
                patient.postalAddress?.country,
                true
            )
        }?.name)

        binding.savePatDemoCaregiverNameET.setText(patient.caregiverFullName)
        binding.savePatDemoCaregiverNumET.setText(patient.caregiverMobile)
        binding.savePatDemoEmgContactNameET.setText(patient.emergencyContactName)
        binding.savePatDemoEmgContactNumET.setText(patient.emergencyContactNumber)


    }

    private fun isDataChanged(): Boolean {

        val patient = savePatDemoViewModel.patient

        if (binding.savePatDemoFirstNameET.text.toString().trim() != patient.firstName) {
            return true
        }

        if (binding.savePatDemoLastNameET.text.toString().trim() != patient.lastName) {
            return true
        }

        if (binding.savePatDemoGenderET.text.toString().trim() != patient.gender) {
            return true
        }

        if (binding.savePatDemoBloodGroupET.text.toString().trim() != patient.bloodGroup) {
            return true
        }

        if (binding.savePatDemoDOBET.text.toString().trim() != patient.dateOfBirth?.toString(
                ModelUtil.charmDisplayDateFormat()
            )
        ) {
            return true
        }

        if (binding.savePatDemoMobilePhoneET.text.toString()
                .trim() != patient.contactDetails?.mobile
        ) {
            return true
        }

        if (binding.savePatDemoHomePhoneET.text.toString()
                .trim() != patient.contactDetails?.landline
        ) {
            return true
        }

        if (binding.savePatDemoLine1ET.text.toString().trim() != patient.postalAddress?.line1) {
            return true
        }

        if (binding.savePatDemoLine2ET.text.toString().trim() != patient.postalAddress?.line2) {
            return true
        }

        if (binding.savePatDemoCityET.text.toString().trim() != patient.postalAddress?.city) {
            return true
        }

        if (binding.savePatDemoStateET.text.toString().trim() != patient.postalAddress?.state) {
            return true
        }

        if (binding.savePatDemoCountryET.text.toString()
                .trim() != savePatDemoViewModel.patientCommonData?.country?.firstOrNull {
                it.key.equals(
                    patient.postalAddress?.country,
                    true
                )
            }?.name
        ) {
            return true
        }

        if (binding.savePatDemoCaregiverNameET.text.toString()
                .trim() != patient.caregiverFullName
        ) {
            return true
        }

        if (binding.savePatDemoCaregiverNumET.text.toString().trim() != patient.caregiverMobile) {
            return true
        }

        if (binding.savePatDemoEmgContactNameET.text.toString()
                .trim() != patient.emergencyContactName
        ) {
            return true
        }

        if (binding.savePatDemoEmgContactNumET.text.toString()
                .trim() != patient.emergencyContactNumber
        ) {
            return true
        }


        return false
    }


    override fun onBackPressed() {

        if (savePatDemoViewModel.isImageCropperFragmentPushed) {
            supportFragmentManager.popBackStack()
        } else if (binding.savePatDemoSaveBtn.visibility == View.GONE) {

            if (savePatDemoViewModel.isDataSaved) {

                if (!savePatDemoViewModel.filePath.isNullOrBlank()) {
                    val currentPat = authManager.selectedPatientLive.value
                    if (currentPat != null) {
                        currentPat.isPhotoAvailableOnline = true
                        authManager.selectedPatientLive.postValue(currentPat)
                    }
                    // Notify both activities to reload image
                    val resultIntent = Intent()
                    resultIntent.putExtra(ModelUtil.patientImageUpdated, true)
                    setResult(RESULT_OK, resultIntent)
                } else {
                    setResult(RESULT_OK)
                }
            }
            super.onBackPressed()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } else {

            if (isDataChanged()) {
                backPressAlertDialogBuilder.show()
            } else {

                super.onBackPressed()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    fun fetchCommonData() {
        binding.savePatDemoMainLayout.visibility = View.GONE
        binding.savePatDemoErrorPage.root.visibility = View.GONE
        binding.savePatDemoProgress.root.visibility = View.VISIBLE

        savePatDemoViewModel.fetchPatientCommonData()
    }

    private fun isTextInputLayoutBlank(
        textInputLayout: TextInputLayout,
        @StringRes errorMsg: Int,
        hasAlpha: Boolean = false
    ): Boolean {

        val editText = textInputLayout.editText

        val text = editText?.text?.toString()

        if ((hasAlpha && !ModelUtil.hasAlpha(text ?: "")) || (!hasAlpha && text.isNullOrBlank())) {
            UIUtil.showToast(this, errorMsg, Toast.LENGTH_LONG)
            textInputLayout.error = getString(errorMsg)
            showFocus(textInputLayout, editText)
            return true
        } else {
            textInputLayout.error = null
            return false
        }
    }

    private fun showFocus(textInputLayout: TextInputLayout, editText: EditText?) {
        binding.savePatDemoMainLayout.smoothScrollTo(0, textInputLayout.top)

        handler.postDelayed({
            editText?.requestFocus()

            if (editText?.inputType != InputType.TYPE_NULL) {
                if (editText is MDClickToSelectEdittext) {
                    editText.performClick()
                } else {
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                }
            }

        }, ModelUtil.addErrorAnimationDelayDuration)
    }


    override fun afterImageCroppedForGallery(filePath: String?, isCancelClicked: Boolean) {
        savePatDemoViewModel.isImageCropperFragmentPushed = false

        if (!isCancelClicked) {

            if (filePath != null) {

                setImageIntoView(filePath)

            } else UIUtil.showToast(
                this,
                getString(R.string.error_occurred_while_handling_photo),
                Toast.LENGTH_SHORT
            )

        }

        supportFragmentManager.popBackStack()
    }

    //region Handling permission for camera and read external storage
    private fun initializePermissionResultLauncher() {
        requestPermissionLauncher = PermissionUtil.getPermissionResultLauncher(
            this,
            ::permissionAcceptCallBack,
            R.string.allow_patient_profile,
            null, false, ::permissionDeniedCallBack
        )
    }

    private fun permissionAcceptCallBack(permissions: Array<String>) {
        permissions.forEach { permission ->
            when (permission) {
                Manifest.permission.CAMERA -> {
                    startMdCameraForResult()
                }
            }
        }
    }


    private fun permissionDeniedCallBack(permissions: Array<String>) {
        PermissionUtil.requestPermission(permissions, requestPermissionLauncher)
    }
    //endregion

    private fun onImageSelectHandler(uri: Uri?) {
        if (uri != null) {

            val list = uri.lastPathSegment?.split(".") ?: arrayListOf()

            val path = FileUtil.createImageFileFromUri(
                uri,
                this,
                ModelUtil.folderTemp,
                list[list.size - 1]
            )

            if (path != null) {

                intentToImagerCropper(path)

            } else UIUtil.showToast(
                this,
                getString(R.string.error_occurred_while_handling_photo),
                Toast.LENGTH_SHORT
            )

        } else UIUtil.showToast(
            this,
            getString(R.string.error_occurred_while_handling_photo),
            Toast.LENGTH_SHORT
        )
    }
}
