package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MdTheme

@Composable
fun CustomDropdownMenuItem(
    icon: Painter,
    title: String,
    onClick: () -> Unit
) {
    val mutableInteractionSource = remember {
        MutableInteractionSource()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = mutableInteractionSource,
                indication = ripple(
                    bounded = true,
                    color = colorResource(R.color.itemRippleForWhiteBg)
                )
            ) {
                onClick()
            }
            .padding(dimensionResource(R.dimen.card_padding))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier
                    .height(18.dp)
                    .width(18.dp),
                tint = colorResource(R.color.grey)
            )
            MDTextView(modifier = Modifier.padding(start = 8.dp), value = title, style = MdTheme.typography.regular14TextStyle.copy(color = colorResource(R.color.black87)))
        }
    }
}