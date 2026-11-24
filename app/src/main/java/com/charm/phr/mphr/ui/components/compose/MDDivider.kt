package com.charm.phr.mphr.ui.components.compose

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Composable
fun MDVerticalLine(modifier: Modifier, thickness: Dp, color: Color) {
    VerticalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color,
    )
}

@Composable
fun MDHorizontalLine(modifier: Modifier, thickness: Dp, color: Color) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = color,
    )
}
