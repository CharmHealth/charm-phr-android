package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MdTheme

@Composable
fun MDCustomIconRadio(
    optionString: String,
    isSelected: Boolean,
    onValueChange: (Boolean) -> Unit,
    isParent: Boolean,
    enable: Boolean = true
) {
    var rowModifier = Modifier
        .heightIn(min = dimensionResource(id = R.dimen.selectable_compose_min_height))
        .height(IntrinsicSize.Min)
        .fillMaxWidth()
    if (enable) {
        rowModifier = rowModifier.selectable(
            selected = isSelected,
            onClick = { onValueChange(!isSelected) },
            role = Role.RadioButton
        )
    }
    Row(
        rowModifier
    ) {
        Column(Modifier.padding(start = 4.dp, top = 2.dp)) {
            val modifier = if (isParent) Modifier.width(34.dp) else Modifier.width(24.dp)
            val selectedIcon = if (isParent) R.drawable.ic_radio_selected_with_arrow else R.drawable.ic_radio_selected
            val unSelectedIcon = if (isParent) R.drawable.ic_radio_unselected_with_arrow else R.drawable.ic_radio_unselect
            val tint = if (enable) Color.Unspecified else colorResource(id = R.color.light_grey)
            MDIconToggleButton(
                modifier,
                selectedIcon,
                unSelectedIcon,
                null,
                tint,
                isSelected,
                onValueChange,enable
            )
            if (isParent && isSelected) {
                MDVerticalLine(
                    Modifier
                        .weight(1f)
                        .padding(start = 11.9.dp), 0.5.dp, colorResource(R.color.grey)
                )
            }
        }
        val textModifier =
            if (isParent) Modifier.padding(bottom = 6.dp, top = 3.dp, start = 6.dp) else Modifier.padding(bottom = 6.dp, top = 3.dp, start = 10.dp)
        MDTextView(
            value = optionString, modifier = textModifier, style = MdTheme.typography.medium14TextStyle
        )
    }
}

//region Preview
@Preview(showBackground = true)
@Composable
fun ViewRadioBox() {
    var test by remember { mutableStateOf(false) }
    MDCustomIconRadio(
        "test",
        test,
        onValueChange = { isChecked ->
            test = isChecked
        },
        true,
        true
    )
}

@Preview(showBackground = true)
@Composable
fun ViewRadioBox1() {
    var test by remember { mutableStateOf(false) }
    MDCustomIconRadio(
        "test",
        test,
        onValueChange = { isChecked ->
            test = isChecked
        },
        false,
        true
    )
}
//endregion
