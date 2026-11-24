package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MdCardWithoutTonalElevation(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: Color = Color.White,
    border: BorderStroke? = null,
    elevation: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit = {}
)  {
    Surface(
        modifier = modifier,
        shape = shape,
        color = colors,
        tonalElevation = 0.dp,
        shadowElevation = elevation,
        border = border,
    ) {
        Column(content = content)
    }
}