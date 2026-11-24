package com.charm.phr.mphr.ui.dashboard.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.Module
import com.charm.phr.mphr.ui.Theme.MdTheme
import com.charm.phr.mphr.ui.components.compose.MDTextView
import com.charm.phr.mphr.ui.components.compose.MdBadge

@Composable
fun ItemHealthSection(
    module: Module,
    badgeText: Int? = null,
    onItemClicked: (String) -> Unit,

    ) {
    val mutableInteractionSource = remember {
        MutableInteractionSource()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(
                interactionSource = mutableInteractionSource,
                indication = ripple(bounded = true)
            ) {
                onItemClicked(module.moduleId)
            }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = module.moduleIconId), contentDescription = null, modifier = Modifier
                .size(36.dp)
        )
        MDTextView(
            value = stringResource(module.moduleName),
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            style = MdTheme.typography.semiBold14TextStyle.copy(fontSize = 15.sp)
        )

        val badgeNumber = badgeText ?: 0
        when {
            badgeNumber in 1..99 -> {
                MdBadge(text = badgeNumber.toString())
            }
            badgeNumber >= 100 -> {
                MdBadge(text = "99+")
            }
            else -> {}
        }

        Spacer(modifier = Modifier.size(16.dp))
        GreyRightArrow()
        Spacer(modifier = Modifier.size(8.dp))

    }
}


@Preview(showBackground = true)
@Composable
fun ItemHealthSectionPreview() {
    Module(
        moduleId = "1",
        moduleName = R.string.health_passport_str,
        moduleIconId = R.drawable.ic_health_passport,
        unreadCount = 1
    ).let {
        ItemHealthSection(module = it, onItemClicked = {}, badgeText = 1)
    }
}
