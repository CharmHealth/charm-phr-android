package com.charm.phr.mphr.ui.components.mdCamera

import androidx.lifecycle.ViewModel

class MdCameraViewModel : ViewModel() {

    //region mandatory params
    /**
     * [allowCompression] wheather to do compression or not
     */
    var allowCompression: Boolean = false

    /**
     * [cameraCaptureType] Type of camera capture
     */
    var cameraCaptureType :CameraCaptureType = CameraCaptureType.SINGLE_SHOT

    //endregion

    /**
     * [multiShotFirstTime] First time in the multiShot mode
     */
    var multiShotFirstTime: Boolean = true

    /**
     * [capturedImageFileArr] ArrayList of image file path in string
     */
    var capturedImageFileArr: ArrayList<String> = ArrayList()

    /**
     * [selectedPositionForPreview] cropped image position will selected when preview activity recreates
     */
    var selectedPositionForPreview :Int? = null

}