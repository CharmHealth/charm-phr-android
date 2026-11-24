package com.charm.phr.mphr.ui.components.mdCamera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.databinding.FragmentMdPhotoPreviewBinding
import com.charm.phr.mphr.util.FileUtil
import dagger.android.support.DaggerFragment
import java.util.concurrent.Executor
import javax.inject.Inject

class MdPhotoPreviewFragment : DaggerFragment(), MdPhotoPreviewAdapter.ImagePickerListener {


    //region variables declaration and definition

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mdPhotoPreviewAdapter: MdPhotoPreviewAdapter

    @Inject
    lateinit var executor: Executor

    private lateinit var mdCameraViewModel: MdCameraViewModel

    var intentCallBack: CameraFragmentTransListener? = null

    private var _binding: FragmentMdPhotoPreviewBinding? = null
    private val binding get() = _binding!!

    //endregion

    //region lifecycle methods

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment using ViewBinding
        _binding = FragmentMdPhotoPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mdCameraViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(MdCameraViewModel::class.java)

        val lm = androidx.recyclerview.widget.LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        lm.reverseLayout = true
        lm.stackFromEnd = true

        binding.MdCameraPreviewRecyclerView.layoutManager = lm

        mdPhotoPreviewAdapter.callBackListener = this

        val selectedPosition = mdCameraViewModel.selectedPositionForPreview
        mdPhotoPreviewAdapter.setItemsAndRefreshList(mdCameraViewModel.capturedImageFileArr, selectedPosition)

        binding.MdCameraPreviewRecyclerView.adapter = mdPhotoPreviewAdapter

        //region listeners

        binding.mdPreviewDeleteBtn.setOnClickListener {
            mdPhotoPreviewAdapter.deleteSelectedItem()
        }

        binding.mdPreviewCropBtn.setOnClickListener {

            val pair = mdPhotoPreviewAdapter.getSelectedImageForCrop()
            intentCallBack?.intentToImageCropperFragment(filePath = pair.first, position = pair.second)

        }

        binding.mdPreviewAddMoreImagesBtn.setOnClickListener {
            mdCameraViewModel.multiShotFirstTime = false
            intentCallBack?.intentToCameraFragment()
        }

        binding.photoPreviewDoneBtn.setOnClickListener {

            intentCallBack?.setFinalResult(mdPhotoPreviewAdapter.getFinalFilesArrList())

        }

        //endregion

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //endregion

    override fun onAllImagesDeleted() {

        mdCameraViewModel.multiShotFirstTime = true //it will be enabled after first image capture
        intentCallBack?.intentToCameraFragment()

    }

    override fun selectedImageForPreview(selectedImagePath: String) {

        binding.mdCameraMainPreview.setBackgroundResource(android.R.color.black)
        binding.photoPreviewSpinner.root.visibility = View.VISIBLE

        executor.execute {
            val bitmap = FileUtil.getBitmapFromFile(selectedImagePath, 2)
            if (bitmap != null)
                requireActivity().runOnUiThread {
                    binding.mdCameraMainPreview?.setImageBitmap(bitmap)
                    binding.photoPreviewSpinner.root?.visibility = View.GONE
                }

        }


    }

    override fun updateSelectedFiles(arrayList: ArrayList<String>) {
        mdCameraViewModel.capturedImageFileArr = arrayList
    }

}