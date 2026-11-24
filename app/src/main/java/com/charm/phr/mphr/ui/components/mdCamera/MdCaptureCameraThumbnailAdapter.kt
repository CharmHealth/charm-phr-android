package com.charm.phr.mphr.ui.components.mdCamera

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.charm.phr.mphr.databinding.ItemCameraCaptureThumbnailBinding
import com.charm.phr.mphr.util.FileUtil
import java.io.File
import java.util.concurrent.Executor

class MdCaptureCameraThumbnailAdapter(val context: Context, val executor: Executor) : androidx.recyclerview.widget.RecyclerView.Adapter<MdCaptureCameraThumbnailAdapter.CustomViewHolder>() {


    private var allFilesArr: ArrayList<String> = ArrayList()
    private var selectedFilesHashMap: HashMap<Int, String> = HashMap()

    var callback: CaptureThumbnailListener? = null

    //region base class methods

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding = ItemCameraCaptureThumbnailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding, context, executor)
    }

    override fun getItemCount(): Int {
        return allFilesArr.count()
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        val filePath = allFilesArr[position]

        holder.bindItems(filePath, selectedFilesHashMap[position] != null)

        holder.itemView.setOnClickListener {

            val selectedFileStr: String? = selectedFilesHashMap[position]
            if (selectedFileStr == null) {

                selectedFilesHashMap[position] = filePath
                holder.binding.cameraCaptureThumbnailSelector.visibility = View.VISIBLE

                callback?.showDoneButton()

            } else {

                selectedFilesHashMap.remove(position)
                if (selectedFilesHashMap.size == 0) callback?.noImageIsSelected()
                holder.binding.cameraCaptureThumbnailSelector.visibility = View.GONE

            }

        }

    }

    //endregion

    //region Other methods

    fun setItemsAndRefreshList(filesStrArr: ArrayList<String>) {

        allFilesArr.clear()
        allFilesArr.addAll(filesStrArr)
        selectedFilesHashMap.clear()

        allFilesArr.forEachIndexed { index, filePath ->
            selectedFilesHashMap[index] = filePath
        }

        notifyDataSetChanged()

    }

    fun addItemAndScrollToLastlyAdded(filePath: String) {

        Log.d("MdCaptureCameraAdapter", "addItemAndScrollToLastlyAdded")

        allFilesArr.add(filePath)
        val lastPos = allFilesArr.count() - 1
        selectedFilesHashMap[lastPos] = filePath
        notifyItemInserted(lastPos)

    }

    fun getSelectedImagesArr(): ArrayList<String> {

        val selectedFilesArr: ArrayList<String> = ArrayList()

        val selectedList = selectedFilesHashMap.values.filter { it.trim().isNotBlank() }
        if (selectedList.isNotEmpty()) selectedFilesArr.addAll(selectedList)


        return selectedFilesArr

    }

    //endregion

    //region viewHolder

    class CustomViewHolder(val binding: ItemCameraCaptureThumbnailBinding, val context: Context, val executor: Executor) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bindItems(filePath: String, selected: Boolean) {

            //greater the number -> Greater reduction size
            val file = File(filePath)

            if (file.exists()) {

                executor.execute {

                    val bm = FileUtil.getBitmapFromFile(filePath,16)

                    if(bm != null) {
                        binding.mdCameraCaptureThumbnailView.post {
                            binding.mdCameraCaptureThumbnailView.setImageBitmap(bm)
                        }
                    }
                }
            }

            if (selected)
                binding.cameraCaptureThumbnailSelector.visibility = View.VISIBLE
            else
                binding.cameraCaptureThumbnailSelector.visibility = View.GONE

        }

    }

    //endregion

    //region interface

    interface CaptureThumbnailListener {

        fun noImageIsSelected()

        fun showDoneButton()
    }

    //endregion

}