package com.charm.phr.mphr.ui.document

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.ItemDocumentFolderListBinding
import com.charm.phr.mphr.model.Category

class DocumentFolderAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var category:ArrayList<Category> = arrayListOf()

    private var documentSelectListener: DocumentFolderSelectListener? = null

    private val viewTypeDocument = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemDocumentFolderListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = category.size

    override fun getItemViewType(position: Int): Int {
        return viewTypeDocument
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val category = category[position]

        if (holder is ViewHolder) {

            holder.onBind(category)

            /*holder.itemView.setOnLongClickListener {
                if (!isMultiSelectEnabled) {
                    isMultiSelectEnabled = true
                    problemMultiSelectListener?.onMultiSelect(true)
                    problems.isSelected = true
                    holder.changeBgToSelected()
                }
                else {
                    if (!problems.isSelected) {
                        problems.isSelected = true
                        holder.changeBgToSelected()
                    } else {
                        problems.isSelected = true
                        holder.changeBgToSelected()
                    }
                }

                return@setOnLongClickListener true
            }*/


            holder.itemView.setOnClickListener {
                /* if (isMultiSelectEnabled) {
                     if (problems.isSelected) {
                         problems.isSelected = false
                         holder.changeBgToUnSelected()
                         checkAllIsUnchecked()
                     } else {
                         problems.isSelected = true
                         holder.changeBgToSelected()
                     }
                 } else {*/

                // }
                documentSelectListener?.onSelect(category)
            }

            holder.popupMenu.setOnMenuItemClickListener {menuItem ->

                when(menuItem.itemId){

                    R.id.documentFolderEdit -> {
                        documentSelectListener?.onEdit(category , position)
                    }

                    R.id.documentFolderDelete -> {
                        documentSelectListener?.onDelete(category , position)
                    }

                    else -> {
                        return@setOnMenuItemClickListener false
                    }
                }

                return@setOnMenuItemClickListener true
            }

        }
    }

    fun setCategories(document: ArrayList<Category>): Int{
        this.category.clear()
        this.category.addAll(document.filter {!it.categoryId.isNullOrBlank() })
        this.category.add(Category(isSharedWithMe = true))
        this.category.add(Category(isSharedByMe = true))
        this.notifyDataSetChanged()
        return this.category.size
    }


    fun removeCategory(position: Int): Int{
        this.category.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, category.size);

        return  category.size
    }
    fun setDocumentSelectListener(listener: DocumentFolderSelectListener){
        documentSelectListener = listener
    }

    inner class ViewHolder(private val binding: ItemDocumentFolderListBinding): RecyclerView.ViewHolder(binding.root) {

    val documentNameTV: TextView = binding.documentFolderNameTV
    val documentMoreBtn: ImageButton = binding.documentFolderMoreBtn
    val documentFolderIcon: ImageView = binding.documentFolderIcon

        //val problemConstraintLayout: ConstraintLayout = itemView.documentConstraintLayout

        val popupMenu = PopupMenu(itemView.context, binding.documentFolderMoreBtn, Gravity.END)

        init {
            popupMenu.inflate(R.menu.menu_document_folder)
        }

        fun onBind(document:Category){


                /*  documentLayout.visibility = View.GONE
                  documentHPLayout.visibility = View.VISIBLE
                  problemHPDateTV.text = problems.dateOfOnset?.toString(ModelUtil.charmDisplayDateFormat)
                */
            if (document.isSharedByMe){
                documentNameTV.setText(R.string.document_folder_shared_by_me_str)
                documentFolderIcon.setImageResource(R.drawable.ic_shared_by_me)
                documentMoreBtn.visibility = View.GONE
            }
            else if (document.isSharedWithMe){
                documentNameTV.setText(R.string.document_folder_shared_with_me_str)
                documentFolderIcon.setImageResource(R.drawable.ic_shared_to_me)
                documentMoreBtn.visibility = View.GONE
            }
            else {
                documentNameTV.text = document.categoryName
                documentFolderIcon.setImageResource(R.drawable.ic_document)
                documentMoreBtn.visibility = View.VISIBLE
            }
            documentMoreBtn.setOnClickListener {
                popupMenu.show()
            }

        }

    }

    interface DocumentFolderSelectListener {
        fun onDelete(document: Category , position: Int)
        fun onEdit(document: Category , position: Int)
        fun onSelect(document: Category)
    }

}