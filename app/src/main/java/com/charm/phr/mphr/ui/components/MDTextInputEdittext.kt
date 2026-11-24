package com.charm.phr.mphr.ui.components

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.transition.TransitionManager
import com.charm.phr.mphr.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

class MDTextInputEdittext constructor(context: Context, attributeSet: AttributeSet?) : TextInputEditText(context, attributeSet) {


    constructor(context: Context):this(context , null)






    private var filterRegex = 3

    private var isGravityCenter = true

    private val edittextPadding = resources.getDimensionPixelSize(R.dimen.edittext_padding)
    private val edittextTopPadding = resources.getDimensionPixelSize(R.dimen.edittext_top_padding)


    private var setBgTransparent = true

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.MDTextInputEdittext)

        setBgTransparent = typedArray.getBoolean(R.styleable.MDTextInputEdittext_setBgTransparent , true)



        filterRegex = typedArray.getInt(R.styleable.MDTextInputEdittext_filterRegex , 3)
        typedArray.recycle()

        updateFilterInternal(filterRegex)

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


                        val textInputLayout = (parent as? ViewGroup)?.parent ?: return

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

//        setCustomSelectionActionModeCallback(object : ActionMode.Callback {
//            override fun onPrepareActionMode(
//                    mode: ActionMode,
//                    menu: Menu
//            ): Boolean {
//                return false
//            }
//
//            override fun onDestroyActionMode(mode: ActionMode) {}
//            override fun onCreateActionMode(
//                    mode: ActionMode,
//                    menu: Menu
//            ): Boolean {
//                return false
//            }
//
//            override fun onActionItemClicked(
//                    mode: ActionMode,
//                    item: MenuItem
//            ): Boolean {
//                return false
//            }
//        })

        if(setBgTransparent){
            setBackgroundResource(android.R.color.transparent)
        }



/* val arrayList  = filters.toCollection(ArrayList())
 arrayList.add(object : InputFilter {
     override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
         source.forEach {
             if(!filterRegex.contains(it)) {
                 return ""
             }
         }
         return null
     }

 })*/

// arrayList.add(object : InputFilter {
//     override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
//
////         if (dest?.endsWith(" ") == true) {
////             if (source.isBlank()) {
////                 return ""
////             } else {
////                 val matcher: Matcher = mPattern.matcher(source)
////
////                 if (matcher.matches()) {
////                     return null
////                 }
////                 return ""
////             }
////         }
//
//         val  matcher : Matcher =mPattern.matcher(source)
//
//         if(matcher.matches()){
//             Log.d("mdKioskNull" , "null")
//             return null
//         }
//         Log.d("mdKioskSpace" , "space")
//         return ""
//     }
//
//     private var mPattern: Pattern = Pattern.compile(filterRegex)
//
// })
//
//        filters = arrayList.toTypedArray()




    }


    /*  if (str == "") {
          return ""
      }

      if (str == " ") {
          setText("")
          return ""
      }

      if (str.startsWith(" ")) {
          str = str.substring(1, str.length)
          return str
      }
      val splitVal = str.split(" ")

      Log.d("MdKioskTemp", "-----------------------------------------")
      Log.d("MdKioskSplitValLen", splitVal.size.toString())
      Log.d("MdKioskSplitVal", splitVal.joinToString(prefix = "[", postfix = "]", separator = "/"))

      val splitValBlank = splitVal.filterIndexed { index, s ->
          if (index == splitVal.size - 1 && s.isBlank()) {
              return@filterIndexed false
          }

          s.isBlank()
      }
      val splitValBlankLen = splitValBlank.size

      Log.d("MdKioskSplitValBlankLen", splitValBlankLen.toString())

      val splitValLengthBool = splitValBlankLen >= 1


      if (splitValLengthBool) {
          var splitValLen = splitVal.size
          val filteredVal = splitVal.filterIndexed { index, str ->
              if (str == "" && index == splitValLen - 1) {
                  return@filterIndexed true
              }

              str != ""
          }


          Log.d("MdKioskSplitStr", splitVal.joinToString())
          Log.d("MdKioskFilterStr", filteredVal.joinToString())

          splitValLen = filteredVal.size
          val strBuffer = StringBuffer()
          filteredVal.forEachIndexed { index, splitStr ->
              if (splitStr.isNotBlank()) {
                  strBuffer.append(splitStr)
                  if (index < (splitValLen - 1)) {
                      strBuffer.append(" ")
                  } else if (index == (splitValLen - 1) && splitStr == "") {
                      strBuffer.append(" ")
                  }
              }
          }


         return strBuffer.toString()

      } else {
          Log.d("MdKioskSplitStrReg", splitVal.joinToString())
      }

      return str
  }
})
     filters = arrayList.toTypedArray()

 }*/

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        if (isEnabled){
            updateFilter(filterRegex)
        }
        else{
            updateFilter(8)
        }
    }

//    override fun isSuggestionsEnabled(): Boolean {
//        return false
//    }

//    override fun getSelectionStart(): Int {
//        for (element in Thread.currentThread().stackTrace) {
//            if (element.methodName == "canPaste") {
//                return -1
//            }
//        }
//        return super.getSelectionStart()
//    }

    private fun updateFilterInternal(filterCode: Int , filtersTemp: ArrayList<InputFilter>? = null ){
        val arrayList  = filtersTemp ?: filters.toCollection(ArrayList())
        if (filterCode ==  ALPHABETS){
            arrayList.add(AlphabetInputFilter())
        }
        else if (filterCode == ALPHABETS_NUMBERS){
            arrayList.add(AlphabetNumberInputFilter())
        }

        else if (filterCode == ALPHABETS_NUMBERS_SPLCHARS){
            arrayList.add(AlphabetNumberAndSplCharInputFilter())

        }
        else if (filterCode == NUMBERS){
            arrayList.add(NumberInputFilter())
        }
        else if(filterCode == PASSPORT){
            arrayList.add(AlphabetNumberInputFilter())

            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(beforeTxtEditable: Editable) {
                    val beforeText = beforeTxtEditable.toString()

                    var toUppercase = false

                    for ( char in beforeText){
                        if (!char.isDigit() && char.isLowerCase()){
                            toUppercase  = true
                            break
                        }
                    }
                    if (toUppercase){
                        setText(beforeText.uppercase())
                        setSelection(beforeText.length)

                    }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })
        }
        else if (filterCode == ADDRESS){
            //filterRegex == 6
            arrayList.add(AddressInputFilter())

        } else if (filterCode == NUMBER_DECIMAL){
            arrayList.add(NumberDecimalInputFilter())
        }
        filters = arrayList.toTypedArray()

        filterRegex = filterCode
    }



    fun updateFilter(filterCode: Int){

        val arrayList  = filters.toCollection(ArrayList())

        val filteredList =  arrayList.filter {
            it is AlphabetNumberInputFilter || it is AlphabetNumberAndSplCharInputFilter || it is NumberInputFilter || it is AlphabetInputFilter || it is AddressInputFilter
        }

        if (filteredList.isNotEmpty()){
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(beforeTxtEditable: Editable) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })
        }

        arrayList.removeAll(filteredList)
        updateFilterInternal(filterCode , arrayList)
    }

    override fun setText(text: CharSequence?, type: BufferType?) {

        if (text?.trim() != getText()?.trim()){
            super.setText(text, type)
            setSelection(getText()?.toString()?.length ?: 0)
        }

    }

    companion object {
        const val ALPHABETS = 1
        const val ALPHABETS_NUMBERS = 2
        const val ALPHABETS_NUMBERS_SPLCHARS = 3
        const val NUMBERS = 4
        const val PASSPORT = 5
        const val ADDRESS = 6
        const val NUMBER_DECIMAL = 7

    }

}



class AlphabetNumberInputFilter : InputFilter {
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        val  matcher : Matcher =mPattern.matcher(source)

        if(matcher.matches()){
            return null
        }

        return ""
    }

    private var mPattern: Pattern = Pattern.compile("[a-zA-Z0-9 \n]*")

}

class AlphabetNumberAndSplCharInputFilter : InputFilter {
    // Sanitize-don't-reject: pastes from the web carry curly quotes / em-dashes / nbsp etc.
    // The old "reject the whole paste on any invalid char" behavior made paste appear broken.
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        var changed = false
        val out = StringBuilder(end - start)
        for (i in start until end) {
            val c = source[i]
            val replacement: String? = when (c) {
                '‘', '’', '‚', '′' -> "'"   // ‘ ’ ‚ ′
                '“', '”', '„', '″' -> "\""  // “ ” „ ″
                '–', '—', '−' -> "-"             // – — −
                '…' -> "..."                               // …
                '•', '·' -> "*"                       // • ·
                ' ', ' ', ' ' -> " "             // nbsp / figure space / narrow nbsp
                '\t' -> "    "
                else -> null
            }
            if (replacement != null) {
                out.append(replacement)
                changed = true
            } else if (isAllowed(c)) {
                out.append(c)
            } else {
                changed = true
            }
        }
        return if (changed) out.toString() else null
    }

    private fun isAllowed(c: Char): Boolean {
        if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9') return true
        return c in ALLOWED_SPECIALS
    }

    companion object {
        private const val ALLOWED_SPECIALS = " \\`~!@#\$%^/&*()_+-=[]{};:'\",.?><\n"
    }
}

class AddressInputFilter : InputFilter {
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        val  matcher : Matcher =mPattern.matcher(source)

        if(matcher.matches()){
            return null
        }
        return ""
    }

    private var mPattern: Pattern = Pattern.compile("[a-zA-Z0-9 \\\\#/&amp*\\-;:,.\n]*")

}
class AlphabetInputFilter : InputFilter {
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        val  matcher : Matcher =mPattern.matcher(source)

        if(matcher.matches()){
            return null
        }

        val builder = StringBuilder()
        for (i in start until end) {
            val c: Char = source[i]
            if (Character.isLetter(c)) {
                builder.append(c)
            }
        }

        // If all characters are valid, return null, otherwise only return the filtered characters
        // If all characters are valid, return null, otherwise only return the filtered characters
        val allCharactersValid = builder.length == end - start
        return if (allCharactersValid) null else builder.toString()
    }

    private var mPattern: Pattern = Pattern.compile("[a-zA-Z ]*")

}


class NumberInputFilter : InputFilter {
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        val  matcher : Matcher =mPattern.matcher(source)

        if(matcher.matches()){
            return null
        }
        return ""
    }

    private var mPattern: Pattern = Pattern.compile("[0-9]*")

}

class NumberDecimalInputFilter : InputFilter {
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        // Combine the existing text with the new input
        val newText = dest?.substring(0, dstart) + source + dest?.substring(dend)
        val matcher: Matcher = mPattern.matcher(newText)

        return if (matcher.matches()) null else ""
    }

    // Pattern allows numbers with an optional decimal point and any number of digits after the decimal. {nnn..}.{nnn..}
    private val mPattern: Pattern = Pattern.compile("^\\d*\\.?\\d*$")
}




class DecimalFilter : DigitsKeyListener( false, true) {

    private var digits = 2
    fun setDigits(d: Int) {
        digits = d
    }

    override fun filter(source: CharSequence, start: Int, end: Int,
                        dest: Spanned, dstart: Int, dend: Int): CharSequence {
        var source = source
        var start = start
        var end = end
        val out = super.filter(source, start, end, dest, dstart, dend)

        // if changed, replace the source
        if (out != null) {
            source = out
            start = 0
            end = out.length
        }
        val len = end - start

        // if deleting, source is empty
        // and deleting can't break anything
        if (len == 0) {
            return source
        }
        val dlen = dest.length

        // Find the position of the decimal .
        for (i in 0 until dstart) {
            if (dest[i] == '.') {
                // being here means, that a number has
                // been inserted after the dot
                // check if the amount of digits is right
                return if (dlen - (i + 1) + len > digits) "" else SpannableStringBuilder(source, start, end)
            }
        }
        for (i in start until end) {
            if (source[i] == '.') {
                // being here means, dot has been inserted
                // check if the amount of digits is right
                return if (dlen - dend + (end - (i + 1)) > digits) "" else break // return new SpannableStringBuilder(source, start, end);
            }
        }

        // if the dot is after the inserted part,
        // nothing can break
        return SpannableStringBuilder(source, start, end)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class Decimal2Filter : DigitsKeyListener(Locale.ENGLISH, false, true) {

    private var digits = 2
    fun setDigits(d: Int) {
        digits = d
    }

    override fun filter(source: CharSequence, start: Int, end: Int,
                        dest: Spanned, dstart: Int, dend: Int): CharSequence {
        var source = source
        var start = start
        var end = end
        val out = super.filter(source, start, end, dest, dstart, dend)

        // if changed, replace the source
        if (out != null) {
            source = out
            start = 0
            end = out.length
        }
        val len = end - start

        // if deleting, source is empty
        // and deleting can't break anything
        if (len == 0) {
            return source
        }
        val dlen = dest.length

        // Find the position of the decimal .
        for (i in 0 until dstart) {
            if (dest[i] == '.') {
                // being here means, that a number has
                // been inserted after the dot
                // check if the amount of digits is right
                return if (dlen - (i + 1) + len > digits) "" else SpannableStringBuilder(source, start, end)
            }
        }
        for (i in start until end) {
            if (source[i] == '.') {
                // being here means, dot has been inserted
                // check if the amount of digits is right
                return if (dlen - dend + (end - (i + 1)) > digits) "" else break // return new SpannableStringBuilder(source, start, end);
            }
        }

        // if the dot is after the inserted part,
        // nothing can break
        return SpannableStringBuilder(source, start, end)
    }
}