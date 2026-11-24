package com.charm.phr.mphr.ui.document

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.charm.phr.mphr.R
import com.charm.phr.mphr.databinding.DialogAddFolderBinding
import com.charm.phr.mphr.model.Category
import com.charm.phr.mphr.util.UIUtil

class AddFolderDialog(context: Context, val width: Int) : Dialog(context)  {

    var category: Category? = null
    var addFolderClickListener: AddFolderClickListener? = null
    lateinit var binding: DialogAddFolderBinding


    lateinit var  imm: InputMethodManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAddFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imm = context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager

        setCancelable(false)
        setCanceledOnTouchOutside(false)


        window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.addDocumentFolderDialogLayout.layoutParams?.width = width
        binding.addDocumentFolderDialogLayout.requestLayout()

        updateForEdit()

        binding.addDocumentFolderCancelBtn.setOnClickListener {
            dismiss()
        }

        binding.addDocumentFolderCreateBtn.setOnClickListener {

            val name = binding.addDocumentFolderDialogNameET.text.toString()

            if (name.isNotBlank()){
                val tempCategory = category ?: Category()
                tempCategory.categoryName = name
                addFolderClickListener?.onClick(tempCategory)
                dismiss()
            }
            else{
                UIUtil.showToast(context , R.string.error_document_folder_name_empty , Toast.LENGTH_LONG)
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            binding.addDocumentFolderDialogNameET.requestFocus()
            imm.showSoftInput(binding.addDocumentFolderDialogNameET , InputMethodManager.SHOW_IMPLICIT)
        } , 300)
    }

    fun setFolderClickListener(listener: AddFolderClickListener){
        addFolderClickListener = listener
    }

    fun setForEditCategory(category: Category){
        this.category = category
        updateForEdit()
    }

    fun updateForEdit(){

        try {

            if (category != null) {
                binding.addDocumentFolderDialogHeading.setText(R.string.document_folder_edit_txt)
                binding.addDocumentFolderDialogNameET.setText(category?.categoryName)
                binding.addDocumentFolderCreateBtn.setText(R.string.update_str)
            }
        }
        catch (e: Exception){

        }
    }

    interface AddFolderClickListener{
        fun onClick(category: Category)
    }
}