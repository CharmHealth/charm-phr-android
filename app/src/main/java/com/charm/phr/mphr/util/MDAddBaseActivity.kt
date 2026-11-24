package com.charm.phr.mphr.util

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.components.MDClickToSelectEdittext
import com.google.android.material.textfield.TextInputLayout

abstract class MDAddBaseActivity <VIEW_MODEL: MDAddBaseViewModel>: MDBaseActivity() {

    lateinit var backPressAlertDialogBuilder : AlertDialog.Builder

    lateinit var addViewModel: VIEW_MODEL

    var isEndAnimationSlide = true

    lateinit var imm: InputMethodManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addViewModel = getViewModel()

        imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager

        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    goBack()
                }

                DialogInterface.BUTTON_NEGATIVE -> {

                }
            }
        }
        backPressAlertDialogBuilder = AlertDialog.Builder(this)
        backPressAlertDialogBuilder.setMessage(R.string.discard_changes_confirmation_string).setPositiveButton(R.string.yes_str, dialogClickListener).setNegativeButton(R.string.no_str, dialogClickListener)

    }

    override fun onBackPressed() {

        if (addViewModel.isDataAdded) {
            goBack()
        }
        else {
            if (addViewModel.toEdit) {
                if (checkDataIsEditted()) {
                    backPressAlertDialogBuilder.show()
                } else {
                    goBack()
                }
            } else {

                if (checkDataIsAvailable()) {
                    backPressAlertDialogBuilder.show()
                } else {
                    goBack()
                }
            }
        }
    }

    abstract fun checkDataIsAvailable(): Boolean

    abstract fun checkDataIsEditted(): Boolean

    abstract fun getViewModel(): VIEW_MODEL

    fun isTextInputLayoutBlank(textInputLayout: TextInputLayout , scrollView: ScrollView , @StringRes errorMsg: Int ,hasAlpha: Boolean =false): Boolean{

        val editText = textInputLayout.editText

        val text  = editText?.text?.toString()

        return if ((hasAlpha && !ModelUtil.hasAlpha(text ?: "")) || (!hasAlpha && text.isNullOrBlank())) {
            UIUtil.showToast(this , errorMsg , Toast.LENGTH_SHORT)
            textInputLayout.error = getString(errorMsg)
            showFocus(textInputLayout, editText, scrollView)
            true
        } else {
            textInputLayout.error = null
            false
        }
    }

    fun isEditTextBlank(editText: EditText , scrollView: ScrollView , @StringRes errorMsg: Int ,hasAlpha: Boolean =false): Boolean{

        val text  = editText.text?.toString()

        return if ((hasAlpha && !ModelUtil.hasAlpha(text ?: "")) || (!hasAlpha && text.isNullOrBlank())) {
            UIUtil.showToast(this , errorMsg , Toast.LENGTH_SHORT)
            showFocus(editText, scrollView)
            true
        } else {
            false
        }
    }

//    fun showFocus(textInputLayout: TextInputLayout , editText: EditText? , scrollView: ScrollView){
//        scrollView.smoothScrollTo(0 , textInputLayout.top)
//        focus(editText, scrollView)
//    }

    fun showFocus(editText: EditText , scrollView: ScrollView){
        scrollView.smoothScrollTo(0 , editText.top)
        focus(editText, scrollView)
    }

    private fun focus( editText: EditText? , scrollView: ScrollView){

        handler.postDelayed({
            editText?.requestFocus()

            if (editText?.inputType != InputType.TYPE_NULL) {
                if (editText is MDClickToSelectEdittext) {
                    editText.performClick()
                } else {
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                }
            }

        } , ModelUtil.addErrorAnimationDelayDuration)
    }

    private fun goBack(){
        super.onBackPressed()
        if (isEndAnimationSlide) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
        else{
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}