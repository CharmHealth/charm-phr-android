package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.Theme.MdTheme

@Composable
fun MDSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    templateType: String? = null,
    onTemplateTypeChanged: (String) -> Unit = {},
    options: ArrayList<String>? = null,
    showDropdown: Boolean = false,
    hint: String,
    focusOnLoad: Boolean = true,
    onClick: () -> Unit = {},
    backgroundColor: Color = colorResource(R.color.separatorColor)
) {

    var expanded by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    // Request focus when the composable is loaded
    if (focusOnLoad) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
    // Container with border and background
    Column(
        Modifier.padding(
            start = MdTheme.dimension.horizontalPadding,
            end = MdTheme.dimension.horizontalPadding,
            top = MdTheme.dimension.doubleCardMargin,
            bottom = MdTheme.dimension.horizontalPadding
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = colorResource(R.color.colorPrimary),
                    shape = RoundedCornerShape(24.dp)
                )
                //.background(color = backgroundColor, shape = RoundedCornerShape(24.dp))
                .padding(4.dp)
                .clickable {
                    onClick()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown Menu with dropdown icon

                if (showDropdown) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White, shape = RoundedCornerShape(24.dp))
                            .clickable { expanded = true }
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = templateType ?: ""
                            )

                            // Dropdown icon
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown Arrow"
                            )
                        }

                    }

                    Spacer(modifier = Modifier.width(16.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = colorResource(R.color.black40),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(24.dp)

                    )
                }


                // Text area with search icon
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    contentAlignment = Alignment.CenterEnd // Align icon to the end
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // BasicTextField with hint
                        Box(modifier = Modifier.weight(1f)) {
                            if (query.isEmpty()) {
                                MDTextView(
                                    modifier = Modifier,
                                    value = hint,
                                    style = MdTheme.typography.regular16TextStyle
                                )
                            }

                            MDBasicTextField(
                                value = query,
                                onValueChange = { onQueryChanged(it) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(0.dp)
                                    .focusRequester(focusRequester),
                                maxLines = 1,
                                style = MdTheme.typography.medium16TextStyle,
                                enabled = true
                            )
                        }

                        // Cross Icon to clear text
                        Icon(
                            painter = painterResource(R.drawable.ic_close_24),
                            tint = colorResource(R.color.black30),
                            contentDescription = "Clear Text",
                            modifier = Modifier
                                .alpha(if (query.isNotBlank()) 1f else 0f)
                                .padding(end = 4.dp, start = 4.dp)
                                .size(24.dp)
                                .clickable {
                                    onQueryChanged("")
                                }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (showDropdown) {
            DropdownMenu(
                modifier = Modifier.background(Color.White),
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options?.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            MDTextView(
                                value = option,
                                style = MdTheme.typography.regular14TextStyle
                            )
                        },
                        onClick = {
                            onTemplateTypeChanged(option)
                            expanded = false
                        }
                    )
                }
            }
        }

    }
}

@Preview(showSystemUi = true)
@Composable
fun MDSearchBarPreview() {
    val options = arrayListOf(
        "Option 1",
        "Option 2",
        "Option 3"
    )

    MPHRTheme {
        Column(Modifier.padding(top = 100.dp)) {
            MDSearchBar(
                query = "wedwedfw",
                onQueryChanged = { /* Handle query change */ },
                templateType = "Option 1",
                onTemplateTypeChanged = { /* Handle template type change */ },
                options = options, false, "testing",
                onClick = {

                }
            )
        }
    }


}