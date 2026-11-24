package com.charm.phr.mphr.ui.components

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import androidx.transition.TransitionManager
import com.charm.phr.mphr.R
import com.charm.phr.mphr.util.ModelUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MDMultiClickToSelectEdittext constructor(context: Context, attributeSet: AttributeSet?) : TextInputEditText(context, attributeSet) {

    constructor(context: Context):this(context , null)

    private var showIcon = false
    private var declineToSpecify = false

    lateinit var listener: DialogInterface.OnMultiChoiceClickListener

    var headingStr = R.string.multi_clicktoselect_empty_field

    val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)

    var selectedList = BooleanArray(1){
        false
    }
    var sourceList = arrayOf<String>()

    private val edittextPadding = resources.getDimensionPixelSize(R.dimen.edittext_padding)
    private val edittextTopPadding = resources.getDimensionPixelSize(R.dimen.edittext_top_padding)

    private val edittextDrawablePadding = resources.getDimensionPixelSize(R.dimen.ctc_edittext_drawable_padding)

    private var isGravityCenter = true


    private var canvasWidth = 0f
    private var canvasHeight = 0f

    private val paint = Paint()


    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.MDMultiClickToSelectEdittext)
        showIcon = typedArray.getBoolean(R.styleable.MDMultiClickToSelectEdittext_showIcon, true)
        typedArray.recycle()

        paint.color = Color.parseColor("#33000000")
        paint.strokeWidth = 2f


        alertDialog.setPositiveButton(R.string.ok_str){dialogInterface,_ ->

            val selectedIndex = selectedList.mapIndexed { index, isSelected ->
                if (isSelected){
                    index
                }
                else{
                    -1
                }
            }.filter { it != -1 }

            val selectedValue =  sourceList.filterIndexed { index, _ -> selectedIndex.contains(index)  }.joinToString(", ")
            setText(selectedValue)
            dialogInterface.dismiss()
        }

        alertDialog.setNegativeButton(R.string.cancel_str) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }


        alertDialog.setNeutralButton(R.string.clear_all_str) { dialogInterface, _ ->
            setText("")
            dialogInterface.dismiss()
        }

        listener = OnMultiChoiceClickListener { dialogInterface, index, isSelected ->


            val selectedValue = sourceList[index]


            if (declineToSpecify){
                if (selectedValue == ModelUtil.questionnairePersonalDetailDeclineToSpecify) {

                    if (isSelected) {
                        setText(ModelUtil.questionnairePersonalDetailDeclineToSpecify)
                        dialogInterface.dismiss()
                    } else {
                        selectedList[index] = isSelected
                    }
                }
                else{

                    selectedList[index] = isSelected
                    if (isSelected) {
                        selectedList[selectedList.lastIndex] = false

                        Log.d("MDMultiSelect" ,"count ${alertDialog.create()}")

                        //alertDialog.create().setMultiChoiceItems(sourceList , selectedList , listener)

                    }


                }
            }
            else{
                selectedList[index] = isSelected
            }
            // check condition
            /*if (isSelected) {
                // when checkbox selected
                // Add position  in lang list

                val selectedItem = sourceList.getOrNull(index) ?: ""

                if (selectedItem.isNotBlank() && !selectedList.contains(selectedItem)){
                    selectedList[index] = isSelected
                }

                // Sort array list
            } else {
                // when checkbox unselected
                // Remove position from langList
                val selectedItem = sourceList.getOrNull(index) ?: ""

                if (selectedItem.isNotBlank() && selectedList.contains(selectedItem)){
                    selectedList.remove(selectedItem)
                }
            }*/

        }
        onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->


            try {


                val textInputLayout = (parent as ViewGroup).parent

                if (textInputLayout is TextInputLayout) {
                    if (hasFocus) {
                        if (gravity != Gravity.BOTTOM) {
                            TransitionManager.beginDelayedTransition(parent as ViewGroup)
                            gravity = Gravity.BOTTOM
                            textInputLayout.setPadding(
                                    edittextPadding,
                                    edittextTopPadding,
                                    edittextPadding,
                                    edittextPadding
                            )
                            isGravityCenter = false
                        }
                    } else {
                        if (gravity != Gravity.CENTER_VERTICAL && (text?.toString()
                                        ?: "").trim() == "") {
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
            catch (e:TypeCastException){

            }
        }

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {

                try {


                    val textInputLayout = (parent as ViewGroup).parent

                    if (textInputLayout is TextInputLayout) {

                        if (!textInputLayout.error.isNullOrBlank()){
                            textInputLayout.error = null
                        }

                        if (editable.toString().isNotBlank()) {

                            if (gravity != Gravity.BOTTOM && isGravityCenter) {
                                TransitionManager.beginDelayedTransition(parent as ViewGroup)
                                gravity = Gravity.BOTTOM
                                textInputLayout.setPadding(
                                        edittextPadding,
                                        edittextTopPadding,
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
                }
                catch (e:TypeCastException){

                }

            }
        })

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

        inputType = InputType.TYPE_NULL

        updateIcon()

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        isFocusable = false
        isClickable = true
        if (showIcon && !this.sourceList.isEmpty()) {
            canvas.drawLine(canvasWidth - 120f, 30f, canvasWidth - 120f, canvasHeight - 30f, paint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasWidth = w.toFloat()
        canvasHeight = h.toFloat()
    }


    fun setItems(items: ArrayList<String> ,declineToSpecify: Boolean){

        this.declineToSpecify = declineToSpecify

        if (declineToSpecify){
            items.add(ModelUtil.questionnairePersonalDetailDeclineToSpecify)
        }

        sourceList = items.toTypedArray()



        configureOnClickListener()
        updateIcon()

    }


    fun setValue( selectedItemStr: String){

        val selectedItems = selectedItemStr.split(", ").filter { it.isNotBlank() }.map { it.trim() }
        setText(selectedItemStr)

        this.selectedList = BooleanArray(sourceList.size) { posn ->
            selectedItems.contains(sourceList[posn])
        }
    }
    private fun configureOnClickListener() {
        setOnClickListener {

            val selectedText = text?.toString() ?: ""
            val selectedArr = selectedText.split(",").filter { it.isNotBlank() }.map { it.trim() }


            sourceList.forEachIndexed { index, source ->
                selectedList[index] = selectedArr.contains(source)
            }

            alertDialog.setTitle(headingStr)
            alertDialog.setMultiChoiceItems(sourceList , selectedList , listener)
        }
    }

    private fun updateIcon() {
        if (showIcon && !sourceList.isEmpty()) {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_drop_down_black, 0)
            compoundDrawablePadding = edittextDrawablePadding
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
        invalidate()
    }


}