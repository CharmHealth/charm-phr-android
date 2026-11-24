package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MdTheme

@Composable
fun MdOutlineButton(
    modifier: Modifier,
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colorResource(R.color.colorPrimary)
        ),
        border = BorderStroke(1.dp,  colorResource(R.color.colorPrimary)),
        modifier = modifier.padding(8.dp)
    ) {
        MDTextView(value = text, style = MdTheme.typography.semiBold16TextStyle.copy(color = colorResource(R.color.colorPrimary)))
    }
}