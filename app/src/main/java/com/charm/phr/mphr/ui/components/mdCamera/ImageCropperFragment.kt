package com.charm.phr.mphr.ui.components.mdCamera

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.FragmentImageCropperBinding
import com.charm.phr.mphr.ui.components.ProgressDialog
import com.charm.phr.mphr.util.FileUtil
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.android.support.DaggerFragment
import java.io.File
import java.util.concurrent.Executor
import javax.inject.Inject


/**
 * Note :Image Cropper associated with MdCamera. Needs changes when it is used for other than MdCamera activity
 * Encryption wont be done here!
 */
class ImageCropperFragment : DaggerFragment() {

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var executor: Executor

    /**
     * [rotatedAngel] Rotation angle for an image.
     */
    private var rotatedAngel = 90

    var positionOfTheImageInArr: Int? = null

    lateinit var imageFilePath: String

    lateinit var progressDialog: ProgressDialog

    val TAG = "ImageCropperFragment"

    /**
     * [ImageCropperListener] listener to return cropped image.
     */
    private var imageCropperCallBack: ImageCropperListener? = null
    private var imageCopperCallBackForMdCamera: ImageCropperListenerForMdCamera? = null

    private var _binding: FragmentImageCropperBinding? = null
    private val binding get() = _binding!!

    //region lifecycle methods

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment using View Binding
        _binding = FragmentImageCropperBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    //endregion

    //region otherMethods

    private fun initViews() {

        progressDialog = ProgressDialog(requireActivity())

        binding.imageCropperView.guidelines = CropImageView.Guidelines.ON
        binding.imageCropperView.setAspectRatio(1, 1)
        binding.imageCropperView.isAutoZoomEnabled = false

        //Todo:remove in all places :imageToCropIcFragment

        val fileToCrop = File(imageFilePath)
        if (fileToCrop.exists()) binding.imageCropperView.setImageUriAsync(FileUtil.createUriFromFilePath(fileToCrop.path))
        else {
            imageCropperCallBack?.afterImageCroppedForGallery(null, true)
            imageCopperCallBackForMdCamera?.afterImageCroppedForMdCamera(null, null, true)
        }

    }

    private fun initListeners() {

        binding.imageCropperDoneBtn.setOnClickListener {

            val croppedBitmap = binding.imageCropperView?.croppedImage

            if (croppedBitmap != null) {

                Log.d(TAG, "progressDialog.show()")
                progressDialog.show()
                progressDialog.setMessage(R.string.loading_data)

                val filePath = FileUtil.createImageFileFromBitmap(requireContext(), croppedBitmap, FileUtil.FileType.TEMP)

                progressDialog.dismiss()
                imageCropperCallBack?.afterImageCroppedForGallery(filePath)
                imageCopperCallBackForMdCamera?.afterImageCroppedForMdCamera(filePath, positionOfTheImageInArr)


            } else {
                imageCropperCallBack?.afterImageCroppedForGallery(null, true)
                imageCopperCallBackForMdCamera?.afterImageCroppedForMdCamera(null, null, true)
            }

        }

        binding.imageCropperCancelBtn.setOnClickListener {
            Log.d(TAG, "imageCropperCancelBtn OnClick")
            imageCropperCallBack?.afterImageCroppedForGallery(null, true)
            imageCopperCallBackForMdCamera?.afterImageCroppedForMdCamera(null, null, true)
        }

        binding.imageCropperRotateBtn.setOnClickListener {
            rotatedAngel += 90
            rotatedAngel %= 360
            binding.imageCropperView.rotatedDegrees = rotatedAngel  //rotating image by 90 degree
        }

    }

    fun setImageCropperListener(imageCropperListener: ImageCropperListener) {
        this.imageCropperCallBack = imageCropperListener
    }

    fun setImageCropperListenerFromMdCamera(imageCropperListenerForMdCamera: ImageCropperListenerForMdCamera) {
        this.imageCopperCallBackForMdCamera = imageCropperListenerForMdCamera
    }

    //endregion

    //region interface

    interface ImageCropperListener {

        fun afterImageCroppedForGallery(filePath: String?, isCancelClicked: Boolean = false)

    }

    interface ImageCropperListenerForMdCamera {

        fun afterImageCroppedForMdCamera(filePath: String?, position: Int?, isCancelClicked: Boolean = false)

    }

    //endregion

}