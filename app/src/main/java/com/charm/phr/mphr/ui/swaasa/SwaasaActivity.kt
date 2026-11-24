package com.charm.phr.mphr.ui.swaasa

import android.Manifest
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ActivitySwaasaBinding
import com.charm.phr.mphr.model.APIResponseError
import com.charm.phr.mphr.model.SwaasaModel
import com.charm.phr.mphr.util.ExtAudioRecorder
import com.charm.phr.mphr.util.FileUtil
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.PermissionUtil
import com.charm.phr.mphr.util.UIUtil
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import javax.inject.Inject

class SwaasaActivity : MDBaseActivity() {


    private var extAudioRecorder: ExtAudioRecorder? = null
    private var player: MediaPlayer? = null

    private var isAudioRecording = false
    private val modelList = arrayListOf<SwaasaModel>(
        SwaasaModel("frequent_cough", R.string.swaasa_questionnaire_label_1),
        SwaasaModel("sputum", R.string.swaasa_questionnaire_label_2),
        SwaasaModel("cough_at_night", R.string.swaasa_questionnaire_label_3),
        SwaasaModel("wheezing", R.string.swaasa_questionnaire_label_4),
        SwaasaModel("pain_in_chest", R.string.swaasa_questionnaire_label_5),
        SwaasaModel("shortness_of_breath", R.string.swaasa_questionnaire_label_6)
    )

    private val runnable = Runnable {
        stopAudio()
    }

    private var isOTPError = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var swaasaViewModel: SwaasaViewModel

    lateinit var binding: ActivitySwaasaBinding

    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySwaasaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        swaasaViewModel = ViewModelProvider(this, viewModelFactory).get(SwaasaViewModel::class.java)

        UIUtil.applyViewInsertForToolbar(binding.swaasaToolbar)
        UIUtil.applyViewInsertForBottomView(binding.swaasaRootLayout)

        swaasaViewModel.isFlowsheet = intent.getBooleanExtra(ModelUtil.swaasaIsFlowsheetIntent, false)

        initializePermissionResultLauncher()

        if (swaasaViewModel.isFlowsheet) {

        } else {
            swaasaViewModel.swaasaId = intent.getStringExtra(ModelUtil.swaasaIdIntent) ?: ""
            swaasaViewModel.askOTP = intent.getBooleanExtra(ModelUtil.swaasaShowOtpIntent, false)
        }


        if (swaasaViewModel.swaasaId.isBlank() && !swaasaViewModel.isFlowsheet) {
            binding.swaasaEmptyTV.root.visibility = View.VISIBLE
            binding.swaasaBtn.visibility = View.GONE
            binding.swaasaToolbarScroll.visibility = View.GONE
        } else {
            binding.swaasaEmptyTV.root.visibility = View.GONE
            binding.swaasaBtn.visibility = View.VISIBLE
            binding.swaasaToolbarScroll.visibility = View.VISIBLE
        }

        modelList.forEach {
            addChipMetric(it)
        }

        binding.swaasaOtpPinET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val text = editable?.toString() ?: ""

                if (text.isNotBlank() && isOTPError) {
                    binding.swaasaOTPLabel.setTextColor(ContextCompat.getColor(this@SwaasaActivity, R.color.colorPrimary))
                    binding.swaasaOTPErrorTV.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        binding.swaasaRecordBtn.setOnClickListener {
            PermissionUtil.requestRecordAudioPermission(this, ::permissionAcceptCallBack, requestPermissionLauncher)
        }

        binding.swaasaDeleteBtn.setOnClickListener {
            binding.swaasaRecordBtn.visibility = View.VISIBLE
            binding.swaasaRecordLabel.visibility = View.VISIBLE
            binding.swaasaRecordAudioSeparator.visibility = View.GONE
            binding.swaasaRecordAudioPausePlayLayout.visibility = View.GONE
            FileUtil.deleteSwaasaFiles(this)
        }

        binding.swaasaPausePlayBtn.setOnClickListener {
            if (player != null) {

                if (player!!.isPlaying) {
                    player?.pause()
                    binding.swaasaPausePlayBtn.setImageResource(R.drawable.ic_play)
                } else {
                    player?.start()
                    binding.swaasaPausePlayBtn.setImageResource(R.drawable.ic_pause)
                }
            }
        }


        binding.swaasaBackBtn.setOnClickListener {
            onBackPressed()
        }
        binding.swaasaBtn.setOnClickListener {

            val file = FileUtil.getSwaasaFile(this)
            if (file != null) {

                progressDialog.show()
                swaasaViewModel.recordAudio(modelList).observe(this, Observer { resp ->

                    if (resp != null) {
                        progressDialog.dismiss()
                        if (resp is Boolean) {

                            binding.swaasaMainLayout.visibility = View.GONE
                            binding.swaasaSuccessLayout.visibility = View.VISIBLE

                            //UIUtil.showToast(this, R.string.swaasa_success_msg, Toast.LENGTH_LONG)
                        } else if (resp is APIResponseError) {
                            UIUtil.showToast(this, resp.errorMsg ?: getString(R.string.something_went_wrong), Toast.LENGTH_LONG)
                        }
                    }
                })
            } else {
                UIUtil.showToast(this, R.string.error_swaasa_empty_file, Toast.LENGTH_LONG)
            }
        }
        FileUtil.deleteSwaasaFiles(this)


        if (!swaasaViewModel.askOTP) {
            getSwaasaData()
        } else {
            binding.swaasaMainLayout.visibility = View.GONE
            binding.swaasaOtpScrollView.visibility = View.VISIBLE
            binding.swaasaProgress.root.visibility = View.GONE
            binding.swaasaEmptyTV.root.visibility = View.GONE
        }

        binding.swaasaOTPVerifyBtn.setOnClickListener {

            val swaasaOTP = binding.swaasaOtpPinET.text?.toString() ?: ""

            if (swaasaOTP.length == 4) {
                getSwaasaData()
            } else {
                if (swaasaOTP.isBlank()) {
                    UIUtil.showToast(this, R.string.error_empty_otp, Toast.LENGTH_LONG)
                } else if (swaasaOTP.count() < 4) {
                    UIUtil.showToast(this, R.string.error_invalid_otp, Toast.LENGTH_LONG)
                }
            }
        }

    }

    private fun getSwaasaData() {

        var otp: String? = null
        if (swaasaViewModel.askOTP) {

            otp = binding.swaasaOtpPinET.text?.toString()
            progressDialog.show()
        } else {
            binding.swaasaMainLayout.visibility = View.GONE
            binding.swaasaOtpScrollView.visibility = View.GONE
            binding.swaasaProgress.root.visibility = View.VISIBLE
            binding.swaasaEmptyTV.root.visibility = View.GONE
        }

        swaasaViewModel.getSwaasaData(otp).observe(this, Observer { resp ->

            if (resp != null) {
                if (progressDialog.isShowing) {
                    progressDialog.dismiss()
                }

                if (resp is Boolean) {

                    binding.swaasaMainLayout.visibility = View.VISIBLE
                    binding.swaasaOtpScrollView.visibility = View.GONE
                    binding.swaasaProgress.root.visibility = View.GONE
                    binding.swaasaEmptyTV.root.visibility = View.GONE

                } else if (resp is APIResponseError) {

                    if (resp.charmApiCode == 50091) {// Patient mismatch
                        binding.swaasaOtpScrollView.visibility = View.VISIBLE
                        binding.swaasaProgress.root.visibility = View.GONE
                    } else {
                        if (resp.charmApiCode == 50088) {//Link expired
                            binding.swaasaProgress.root.visibility = View.GONE
                            binding.swaasaEmptyTV.root.visibility = View.VISIBLE
                            binding.swaasaEmptyTV.nodataImageView.setImageResource(R.drawable.ic_swaasa_link_expired)
                            binding.swaasaEmptyTV.nodataTextView.setText(R.string.swaasa_link_expired_msg)
                            binding.swaasaEmptyTV.nodataTextViewHint.setText(R.string.swaasa_contact_practice_msg)
                            binding.swaasaEmptyTV.nodataTextViewHint.visibility = View.VISIBLE
                        } else if (resp.charmApiCode == 50090) { //EXCEEDED_NO_OF_ATTEMPTS
                            binding.swaasaProgress.root.visibility = View.GONE
                            binding.swaasaEmptyTV.root.visibility = View.VISIBLE
                            binding.swaasaEmptyTV.nodataImageView.setImageResource(R.drawable.ic_swaasa_to_many_attempts)
                            binding.swaasaEmptyTV.nodataTextView.setText(R.string.swaasa_too_many_attempt_msg)
                            binding.swaasaEmptyTV.nodataTextViewHint.setText(R.string.swaasa_contact_practice_msg)
                            binding.swaasaEmptyTV.nodataTextViewHint.visibility = View.VISIBLE
                        } else if (resp.charmApiCode == 50089) {// Invalid pin

                            binding.swaasaOtpPinET.setText("")
                            binding.swaasaOtpPinET.setError(true)
                            isOTPError = true
                            binding.swaasaOTPLabel.setTextColor(ContextCompat.getColor(this, R.color.pinColorError))
                            binding.swaasaOTPErrorTV.visibility = View.VISIBLE

                        } else {
                            UIUtil.showToast(this, R.string.error_swaasa_failed_msg, Toast.LENGTH_LONG)
                        }
                    }
                }
            }
        })
    }

    private fun addChipMetric(model: SwaasaModel) {

        val chip = Chip(this)
        chip.layoutParams = ChipGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        chip.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            resources.getDimension(R.dimen.xx_small)
        )
        chip.setTextColor(ContextCompat.getColor(this, R.color.black87))
        chip.setChipBackgroundColorResource(R.color.addWellnessChipBgColor)

        if (model.isSelected) {
            chip.setChipIconResource(R.drawable.ic_selected_chip)
        } else {
            chip.setChipIconResource(R.drawable.ic_add_primary)
        }

        //   chip.setChipIconTintResource(R.color.colorPrimary)
        chip.setText(model.string)



        chip.setOnClickListener {
            model.isSelected = !model.isSelected

            if (model.isSelected) {
                chip.setChipIconResource(R.drawable.ic_selected_chip)
            } else {
                chip.setChipIconResource(R.drawable.ic_add_primary)
            }
        }

        binding.swaasaChipSelectGrp.addView(chip)
    }

    fun recordAudio() {
        FileUtil.deleteSwaasaFiles(this)
        extAudioRecorder = ExtAudioRecorder.getInstance(false)
        extAudioRecorder?.setOutputFile(FileUtil.getFolderPathAsSting(this, ModelUtil.folderSwaasa) + "/file.wav")
        extAudioRecorder?.prepare()
        extAudioRecorder?.start()

        if (extAudioRecorder?.state == ExtAudioRecorder.State.ERROR) {
            UIUtil.showToast(this, "Mic is not available to use", Toast.LENGTH_LONG)
        } else {
            binding.swaasaRecordBtn.visibility = View.INVISIBLE
            binding.swaasaRecordLoading.visibility = View.VISIBLE
            isAudioRecording = true
            handler.postDelayed(runnable, 5000)
        }
    }

    fun stopAudio() {


        if (extAudioRecorder?.state != ExtAudioRecorder.State.ERROR) {

            extAudioRecorder?.stop()
            extAudioRecorder?.release()


            if (extAudioRecorder?.state != ExtAudioRecorder.State.ERROR) {

                binding.swaasaRecordBtn.visibility = View.VISIBLE
                binding.swaasaRecordLoading.visibility = View.GONE
                isAudioRecording = false

                val file = FileUtil.getSwaasaFile(this)

                if (file != null) {
                    binding.swaasaRecordBtn.visibility = View.INVISIBLE
                    binding.swaasaRecordLabel.visibility = View.GONE
                    binding.swaasaRecordAudioSeparator.visibility = View.VISIBLE
                    binding.swaasaRecordAudioPausePlayLayout.visibility = View.VISIBLE

                    player = MediaPlayer.create(this, Uri.fromFile(file))
                    player?.setOnCompletionListener {
                        binding.swaasaPausePlayBtn.setImageResource(R.drawable.ic_play)
                    }
                }
            } else {
                Log.d("MDSwaasa", extAudioRecorder?.errorMsg ?: "")
            }
        } else {
            Log.d("MDSwaasa", extAudioRecorder?.errorMsg ?: "")
        }


    }

    override fun onBackPressed() {
        if (!isAudioRecording) {
            super.onBackPressed()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    //region Handling permission for recording audio
    private fun initializePermissionResultLauncher() {
        requestPermissionLauncher = PermissionUtil.getPermissionResultLauncher(
            this,
            ::permissionAcceptCallBack,
            R.string.allow_record_audio,
            null, false, ::permissionDeniedCallBack
        )
    }

    private fun permissionAcceptCallBack(permissions: Array<String>) {
        permissions.forEach { permission ->
            when (permission) {
                Manifest.permission.RECORD_AUDIO -> {
                    recordAudio()
                }
            }
        }
    }


    private fun permissionDeniedCallBack(permissions: Array<String>) {
        PermissionUtil.requestPermission(permissions, requestPermissionLauncher)
    }
    //endregion
}