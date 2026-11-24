package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MdTheme

@Composable
fun CircleImageViewWithInitials(initial: String, size: Dp = 88.dp, style: TextStyle = MdTheme.typography.medium18TextStyle) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color = colorResource(id = R.color.cardSelectedBgColor)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = style,
            color = colorResource(R.color.colorAccent)
        )
    }
}

@Composable
@Preview(showBackground = true)
fun CircleImageViewWithInitialsPreview() {
    CircleImageViewWithInitials(initial = "LH")
}