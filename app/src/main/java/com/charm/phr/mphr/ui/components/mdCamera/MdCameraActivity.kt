package com.charm.phr.mphr.ui.components.mdCamera

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.charm.phr.mphr.R
import com.charm.phr.mphr.util.MDBaseActivity
import com.charm.phr.mphr.util.ModelUtil
import javax.inject.Inject

class MdCameraActivity : MDBaseActivity(), CameraFragmentTransListener,
        ImageCropperFragment.ImageCropperListenerForMdCamera {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var mdCameraViewModel: MdCameraViewModel

    /**
     * Note:Start this activity using start activityForResult
     * The Result will be returned in 'resultIntent'
     *
     * > MANDATORY PARAMS:allowCompressionForMdCamera, mdCameraCaptureType]
     */

    val TAG = "MdCameraActivity"


    //region lifecycle methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_md_camera)

        mdCameraViewModel = ViewModelProvider(this, viewModelFactory).get(MdCameraViewModel::class.java)

        mdCameraViewModel.cameraCaptureType = when (intent.getStringExtra(ModelUtil.mdCameraCaptureType)) {

            ModelUtil.captureTypeSingleShot -> {
                CameraCaptureType.SINGLE_SHOT
            }

            ModelUtil.captureTypeMultipleShot -> {
                CameraCaptureType.MULTI_SHOT
            }

            else -> CameraCaptureType.SINGLE_SHOT
        }

        mdCameraViewModel.allowCompression = intent.getBooleanExtra(ModelUtil.allowCompressionForMdCamera, false)

        intentCameraFragment()

    }

    //endregion

    //region other methods

    private fun intentCameraFragment(reverseAnimation :Boolean = false) {

        val mdCameraFragment = MdCameraFragment()
        mdCameraFragment.intentCallBack = this

        supportFragmentManager.beginTransaction().run {

            if (reverseAnimation) setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
            else setCustomAnimations(R.anim.slide_in_up, android.R.anim.fade_in, R.anim.slide_out_down, android.R.anim.fade_out)
            replace(R.id.mdCameraActivityMainLayout, mdCameraFragment, "mdCameraFragment")
            commit()
        }

    }

    private fun intentImageCropperFragment(filePath: String, position: Int) {

        val imageCropperFragment = ImageCropperFragment()
        imageCropperFragment.setImageCropperListenerFromMdCamera(this)

        imageCropperFragment.positionOfTheImageInArr = position
        imageCropperFragment.imageFilePath = filePath

        supportFragmentManager.beginTransaction().run {

            setCustomAnimations(R.anim.slide_in_up, android.R.anim.fade_in, R.anim.slide_out_down, android.R.anim.fade_out)
            replace(R.id.mdCameraActivityMainLayout, imageCropperFragment, "imageCropperFragment")
            addToBackStack("imageCropperFragment")
            commit()

        }

    }

    private fun intentImageCropperFragment(filePath: String) {

        val imageCropperFragment = ImageCropperFragment()
        imageCropperFragment.setImageCropperListenerFromMdCamera(this)

        imageCropperFragment.imageFilePath = filePath

        supportFragmentManager.beginTransaction().run {

            setCustomAnimations(R.anim.slide_in_up, android.R.anim.fade_in, R.anim.slide_out_down, android.R.anim.fade_out)
            replace(R.id.mdCameraActivityMainLayout, imageCropperFragment, "imageCropperFragment")
            addToBackStack("imageCropperFragment")
            commit()

        }

    }

    private fun intentPreviewFragment() {

        val mdCameraFragment = MdPhotoPreviewFragment()
        mdCameraFragment.intentCallBack = this

        supportFragmentManager.beginTransaction().run {

            setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
            replace(R.id.mdCameraActivityMainLayout, mdCameraFragment, "mdCameraFragment")
            addToBackStack("mdCameraFragment")
            commit()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

    }

    override fun intentToCameraFragment() {
        //all images are deleted
        intentCameraFragment(reverseAnimation = true)
    }

    override fun intentToPreviewFragment() {
        intentPreviewFragment()
    }

    override fun intentToImageCropperFragment(filePath: String, position: Int) {  //CameraCaptureType.MULTI_SHOT
        intentImageCropperFragment(filePath, position)
    }

    override fun intentToImageCropperFragment(filePath: String) { // CameraCaptureType.SINGLE_SHOT
        intentImageCropperFragment(filePath)
    }

    override fun afterImageCroppedForMdCamera(filePath: String?, position: Int?, isCancelClicked: Boolean) {

        if (!isCancelClicked) {

            if (filePath != null) {

                if (mdCameraViewModel.cameraCaptureType == CameraCaptureType.MULTI_SHOT && position != null) {

                    //Position wont be null for MULTI_SHOT
                    mdCameraViewModel.capturedImageFileArr[position] = filePath
                    mdCameraViewModel.selectedPositionForPreview = position

                } else setFinalResult(filePath)
            }
        }

        supportFragmentManager.popBackStack()

    }

    override fun setFinalResult(filesArr: ArrayList<String>) {

        val returnIntent = Intent()
        returnIntent.putStringArrayListExtra(ModelUtil.mdCameraFiles, filesArr)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down)

    }

    override fun setFinalResult(filePath: String) {

        val returnIntent = Intent()
        returnIntent.putExtra(ModelUtil.mdCameraFiles, filePath)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down)
    }

    override fun setFinalResult() {

        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_down)
    }

    //endregion

}

//region interface

interface CameraFragmentTransListener {

    fun intentToCameraFragment()
    fun intentToPreviewFragment()

    fun intentToImageCropperFragment(filePath: String, position: Int) //CameraCaptureType.MULTI_SHOT
    fun intentToImageCropperFragment(filePath: String) // CameraCaptureType.SINGLE_SHOT

    fun setFinalResult(filesArr: ArrayList<String>)
    fun setFinalResult(filePath: String)
    fun setFinalResult()//Error

}

//endregion

//region
enum class CameraCaptureType {
    SINGLE_SHOT, MULTI_SHOT
}
//endregion