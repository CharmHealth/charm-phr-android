package com.charm.phr.mphr.ui.components.mdCamera

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.charm.phr.mphr.databinding.ItemCameraThumbnailBinding
import com.charm.phr.mphr.util.FileUtil
import com.squareup.picasso.Picasso
import java.io.File
import java.util.concurrent.Executor


class MdPhotoPreviewAdapter(val context: Context, val picasso: Picasso, val executor: Executor) : androidx.recyclerview.widget.RecyclerView.Adapter<MdPhotoPreviewAdapter.CustomViewHolder>() {


    var callBackListener: ImagePickerListener? = null

    private var filePathArrayList: ArrayList<String> = ArrayList()

    private var selectedFilePosition: Int = 0


    //region base class methods

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemCameraThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filePathArrayList.count()
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        Log.d("MdPhotoPreviewAdapter", "position $position")

        holder.bindItems(selectedFilePosition == position)

        val filePath = filePathArrayList[position]

        //greater the number -> Greater reduction size 2 power n

        val file = File(filePath)
        if (file.exists()) {
            executor.execute {
                val bitmap = FileUtil.getBitmapFromFile(filePath, inSampleSize = 16)
                bitmap?.let {
                    holder.itemView.post {
                        holder.binding.mdCameraPreviewThumbnailView.setImageBitmap(bitmap)
                    }
                }
            }


        }

        holder.itemView.setOnClickListener {

            callBackListener?.selectedImageForPreview(filePathArrayList[holder.adapterPosition])
            holder.binding.cameraThumbnailSelector.visibility = View.VISIBLE

            if (selectedFilePosition != holder.adapterPosition) {
                notifyItemChanged(selectedFilePosition)
                selectedFilePosition = holder.adapterPosition
            }

        }

        if (selectedFilePosition == position) {
            callBackListener?.selectedImageForPreview(filePathArrayList[position])
        }

    }


    //endregion

    //region Other methods

    fun setItemsAndRefreshList(filesStrArr: ArrayList<String>, selectedPos: Int? = null) {

        Log.d("MdPhotoPreviewAdapter", "setItemsAndRefreshList ")

        filePathArrayList = filesStrArr

        selectedFilePosition = selectedPos ?: filePathArrayList.lastIndex

        notifyDataSetChanged()

    }

    fun deleteSelectedItem() {

        Log.d("MdPhotoPreviewAdapter", "deleteSelectedItem")

        val pos = selectedFilePosition
        filePathArrayList.removeAt(pos)
        notifyItemRemoved(pos)

        callBackListener?.updateSelectedFiles(filePathArrayList)

        if (filePathArrayList.count() > 0) { //Count

            selectedFilePosition = if (pos - 1 < 0) 0
            else pos - 1 //Position

            notifyDataSetChanged()

        } else {

            //intent to capture camera fragment
            callBackListener?.onAllImagesDeleted()
        }

    }

    fun getFinalFilesArrList(): ArrayList<String> = filePathArrayList

    fun getSelectedImageForCrop(): Pair<String, Int> = Pair(filePathArrayList[selectedFilePosition], selectedFilePosition)

    //endregion

    //region viewHolder

    class CustomViewHolder(val binding: ItemCameraThumbnailBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bindItems(isItemSelected: Boolean) {
            if (isItemSelected) binding.cameraThumbnailSelector.visibility = View.VISIBLE
            else binding.cameraThumbnailSelector.visibility = View.GONE
        }

    }

    //endregion

    //region interface

    interface ImagePickerListener {

        fun selectedImageForPreview(selectedImagePath: String)
        fun onAllImagesDeleted()
        fun updateSelectedFiles(arrayList: ArrayList<String>)

    }

    //endregion
}