package com.charm.phr.mphr.ui.document.share_document

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.databinding.ItemSharedDocumentsBinding
import com.charm.phr.mphr.model.Document
import com.charm.phr.mphr.util.FileUtil

class ShareDocumentListAdapter  : RecyclerView.Adapter<ShareDocumentListAdapter.ViewHolder>() {
    interface ShareDocumentListener {
        fun onDocumentRemove(position: Int)
    }

    private var sharedDocumentList = arrayListOf<Document>()
    private var listener: ShareDocumentListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {
        val sharedDocumentsBinding = ItemSharedDocumentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(sharedDocumentsBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sharedDocument = sharedDocumentList[position]
        holder.onBind(sharedDocument)

        holder.binder.sharedDocumentRemoveBtn.setOnClickListener {
            listener?.onDocumentRemove(position)
        }
    }

    override fun getItemCount(): Int {
        return sharedDocumentList.size
    }

    fun setShareDocumentListener(shareDocumentListener: ShareDocumentListener) {
        listener = shareDocumentListener
    }

    fun setSharedDocumentList(sharedDocumentList: ArrayList<Document>) {
        this.sharedDocumentList.clear()
        this.sharedDocumentList.addAll(sharedDocumentList)
        notifyDataSetChanged()
    }

    fun addItemToSharedDocumentList(sharedDocument: Document) {
        this.sharedDocumentList.add(sharedDocument)
        notifyDataSetChanged()
    }

    fun deleteItemFromSharedDocumentList(position: Int) {
        sharedDocumentList.removeAt(position)
        notifyDataSetChanged()
    }

    class ViewHolder(val binder: ItemSharedDocumentsBinding) : RecyclerView.ViewHolder(binder.root) {
        fun onBind(sharedDocument: Document) {
            binder.sharedDocumentNameTV.text = sharedDocument.fileName
            binder.sharedDocumentIcon.setImageResource(FileUtil.getIconForFileType(sharedDocument.fileName?.split(".")?.lastOrNull()?:""))
            binder.sharedDocumentSizeTV.setText(sharedDocument.fileSize)
        }
    }
}