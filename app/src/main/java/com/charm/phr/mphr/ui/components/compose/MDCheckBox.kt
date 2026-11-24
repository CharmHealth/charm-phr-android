package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
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
fun MDCustomIconCheckBox(optionString: String, isChecked: Boolean, onValueChange: (Boolean) -> Unit, hasChildren: Boolean, enable: Boolean = true) {
    var rowModifier = Modifier
        .heightIn(min = dimensionResource(id = R.dimen.selectable_compose_min_height))
        .height(IntrinsicSize.Min)
        .fillMaxWidth()
    if (enable) {
        rowModifier = rowModifier.toggleable(
            value = isChecked,
            onValueChange = { onValueChange(!isChecked) },
            role = Role.Checkbox
        )
    }
    Row(
        modifier = rowModifier
    ) {
        Column {
            val unselectedIcon = if (hasChildren) R.drawable.ic_checkbox_unselected_with_plus else R.drawable.ic_checkbox_unselected
            val tint = if (enable) Color.Unspecified else colorResource(id = R.color.light_grey)
            MDIconToggleButton(
                Modifier
                    .padding(top = 2.dp, bottom = 2.dp)
                    .width(24.dp), R.drawable.ic_checkbox_selected, unselectedIcon, null,
                tint, isChecked, onValueChange,enable
            )
            if (hasChildren && isChecked) {
                MDVerticalLine(
                    Modifier
                        .weight(1f)
                        .padding(start = 11.9.dp), 0.5.dp, colorResource(R.color.grey)
                )
            }
        }
        MDTextView(
            value = optionString, modifier = Modifier
                .padding(start = 6.dp, bottom = 6.dp, top = 3.dp), style = MdTheme.typography.regular14TextStyle
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ViewCheckBox() {
    var test by remember { mutableStateOf(false) }
    MDCustomIconCheckBox(
        "opt ewrjwerhu werhuewhurw rewrhwehrwe rwehirwerwe rweihrhwierw erihwerhiwer weriwerwiejr ewrjiwejrwe rwejrweijrjwer weijrweijr ijerweihr  ",
        test,
        onValueChange = { isChecked ->
            test = isChecked
        },
        true,false
    )
}