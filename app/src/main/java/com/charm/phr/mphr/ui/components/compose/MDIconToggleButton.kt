package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R

@Composable
fun MDIconToggleButton(
    modifier: Modifier,
    icRadioSelectedIcon: Int,
    icRadioUnSelectedIcon: Int,
    contentDescription: String?,
    tint: Color,
    isSelected: Boolean,
    onValueChange: (Boolean) -> Unit,enabled: Boolean = true,
) {

    IconToggleButton(checked = isSelected, onCheckedChange = { onValueChange(!isSelected) }, modifier = modifier
        .height(24.dp),enabled
    ) {
        val icon = if (isSelected) icRadioSelectedIcon else icRadioUnSelectedIcon
        Icon(
            painter = painterResource(id = icon), contentDescription = contentDescription, modifier = modifier,tint=tint
        )
        //tint -  Color.Unspecified so that it takes icon original color
    }
}


//region Preview
@Preview(showBackground = true)
@Composable
private fun CustomIconToggleButtonPreview() {
    MDIconToggleButton(Modifier.width(24.dp),
        R.drawable.ic_checkbox_selected, R.drawable.ic_checkbox_unselected_with_plus, null, Color.Unspecified, false, {})
}

@Preview(showBackground = true)
@Composable
private fun CustomIconToggleButtonPreview1() {
    MDIconToggleButton(Modifier.width(34.dp),
        R.drawable.ic_radio_selected_with_arrow, R.drawable.ic_radio_unselected_with_arrow, null, Color.Unspecified, true, {})
}

@Preview(showBackground = true)
@Composable
private fun CustomIconToggleButtonPreview2() {
    MDIconToggleButton(Modifier.width(24.dp),
        R.drawable.ic_radio_selected, R.drawable.ic_radio_unselect, null, Color.Unspecified, false, {})
}
//endregion



