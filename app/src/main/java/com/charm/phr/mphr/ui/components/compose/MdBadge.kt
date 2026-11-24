package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MdTheme

@Composable
fun MdBadge(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
    ) {
        Text(
            modifier = Modifier
                .background(color = colorResource(R.color.orangeishRed), shape = CircleShape)
                .badgeLayout()
                .padding(4.dp),
            text = text,
            color = Color.White,
            style = MdTheme.typography.semiBold12TextStyle
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMdBadge() {
    Row(modifier = Modifier.padding(12.dp)) {
        MdBadge(text = "1")
        Spacer(modifier = Modifier.size(8.dp))
        MdBadge(text = "89")
        Spacer(modifier = Modifier.size(8.dp))
        MdBadge(text = "99+")
    }
}

fun Modifier.badgeLayout() =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        // based on the expectation of only one line of text
        val minPadding = placeable.height / 4

        val width = maxOf(placeable.width + minPadding, placeable.height)
        layout(width, placeable.height) {
            placeable.place((width - placeable.width) / 2, 0)
        }
    }