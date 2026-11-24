package com.charm.phr.mphr.ui.components

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.transition.TransitionManager
import com.charm.phr.mphr.R
import com.google.android.material.textfield.TextInputLayout

class MDTextInputAutoCompleteTextView : AppCompatAutoCompleteTextView {


     var isGravityCenter = true
     val edittextPadding = resources.getDimensionPixelSize(R.dimen.edittext_padding)

    constructor(context: Context) : super(context){
        init()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        init()
    }

    private fun init(){
        this.ellipsize = TextUtils.TruncateAt.END
        this.isSingleLine = true

        onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
            val textInputLayout = (parent as ViewGroup).parent as TextInputLayout
            if (hasFocus) {
                if (isGravityCenter) {
                    TransitionManager.beginDelayedTransition(parent as ViewGroup)
                    gravity = Gravity.BOTTOM
                    textInputLayout.setPadding(
                        edittextPadding,
                        edittextPadding,
                        edittextPadding,
                        edittextPadding
                    )
                    isGravityCenter = false
                }
            } else {
                if (!isGravityCenter && (text?.toString() ?: "" ).trim()  == "") {
                    TransitionManager.beginDelayedTransition(parent as ViewGroup)
                    gravity = Gravity.CENTER_VERTICAL
                    textInputLayout.setPadding(edittextPadding, 0, edittextPadding, edittextPadding)
                    isGravityCenter = true
                }
            }
        }

        setCustomSelectionActionModeCallback(object : ActionMode.Callback {
            override fun onPrepareActionMode(
                    mode: ActionMode,
                    menu: Menu
            ): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode) {}
            override fun onCreateActionMode(
                    mode: ActionMode,
                    menu: Menu
            ): Boolean {
                return false
            }

            override fun onActionItemClicked(
                    mode: ActionMode,
                    item: MenuItem
            ): Boolean {
                return false
            }
        })


        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                val textInputLayout = (parent as ViewGroup).parent as TextInputLayout


                if (!textInputLayout.error.isNullOrBlank()){
                    textInputLayout.error = null
                }

                if (editable.toString().trim().isNotBlank()) {
                    if (isGravityCenter) {
                        TransitionManager.beginDelayedTransition(parent as ViewGroup)
                        gravity = Gravity.BOTTOM
                        textInputLayout.setPadding(
                            edittextPadding,
                            edittextPadding,
                            edittextPadding,
                            edittextPadding
                        )
                        isGravityCenter = false
                    }
                }
                else{
                    if (!isFocused){
                        if (gravity != Gravity.CENTER_VERTICAL && text?.toString().isNullOrBlank()) {
                            TransitionManager.beginDelayedTransition(parent as ViewGroup)
                            gravity = Gravity.CENTER_VERTICAL
                            textInputLayout.setPadding(
                                    edittextPadding,
                                    0,
                                    edittextPadding,
                                    edittextPadding
                            )
                            isGravityCenter = true
                        }
                    }
                }
            }
        })
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val ic = super.onCreateInputConnection(outAttrs)
        if (ic != null && outAttrs.hintText == null) {
            // If we don't have a hint and our parent is a TextInputLayout, use it's hint for the
            // EditorInfo. This allows us to display a hint in 'extract mode'.
            val parent = parent
            if (parent is TextInputLayout) {
                outAttrs.hintText = parent.hint
            }
        }
        return ic
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

    }
    override fun isSuggestionsEnabled(): Boolean {
        return false
    }

    override fun getSelectionStart(): Int {
        for (element in Thread.currentThread().stackTrace) {
            if (element.methodName == "canPaste") {
                return -1
            }
        }
        return super.getSelectionStart()
    }
}