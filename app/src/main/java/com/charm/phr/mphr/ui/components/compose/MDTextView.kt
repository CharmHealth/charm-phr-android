package com.charm.phr.mphr.ui.components.compose


import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun MDTextView(modifier: Modifier = Modifier, value: String,style: TextStyle = LocalTextStyle.current) {
    Text(
        value, modifier, style = style
    )
}