package com.charm.phr.mphr.util

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.charm.phr.mphr.databinding.LayoutRichTextEditorBinding
import jp.wasabeef.richeditor.RichEditor

class RichTextEditor(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    var richEditor: RichEditor

    var binding: LayoutRichTextEditorBinding = LayoutRichTextEditorBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        orientation = VERTICAL
        binding =  LayoutRichTextEditorBinding.inflate(LayoutInflater.from(context), this, true)
        richEditor = binding.richTextEditor
        setOnClickListenerForEditor()
        addView(binding.root)
    }

    private fun setOnClickListenerForEditor() {

        val richEditor = binding.richTextEditor
        binding.rteBoldButton.setOnClickListener {
            richEditor.setBold()
        }
        binding.rteItalicButton.setOnClickListener {
            richEditor.setItalic()
        }
        binding.rteUnderlineButton.setOnClickListener {
            richEditor.setUnderline()
        }

        //TODO:Need to add more functionalities

    }

}