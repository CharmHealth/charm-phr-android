package com.charm.phr.mphr.ui.Theme

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object MdDimen {

    val cardPadding = 8.dp
    val cardModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp)
    val carHeadingModifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)

    val cardRoundCornerShape = RoundedCornerShape(4.dp)

    val spaceAboveHeader = 8.dp

}