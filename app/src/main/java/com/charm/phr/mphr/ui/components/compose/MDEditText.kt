package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MdTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MdOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    style: TextStyle,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    isError: Boolean = false,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    MDBasicTextField(
        modifier,
        value,
        onValueChange,
        style,
        enabled,
        singleLine,
        readOnly,
        visualTransformation,
        interactionSource,
        @Composable { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                colors = colors,
                enabled = enabled,
                singleLine = singleLine,
                interactionSource = interactionSource,
                visualTransformation = visualTransformation,
                contentPadding = PaddingValues(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                container = {
                    OutlinedTextFieldDefaults.ContainerBox(
                        enabled,
                        isError,
                        interactionSource,
                        colors,
                        shape
                    )
                }
            )
        })
}

@Composable
fun MDBasicTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    style: TextStyle,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit =
        @Composable { innerTextField -> innerTextField() }, maxLines: Int = Int.MAX_VALUE
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = style,
        cursorBrush = SolidColor(colorResource(id = R.color.primaryColor)),
        singleLine = singleLine,
        decorationBox = decorationBox,
        enabled = enabled,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        maxLines=maxLines
    )
}


@Preview(showBackground = true)
@Composable
private fun SamplePreview() {
    var text by rememberSaveable { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding(start = 10.dp), verticalArrangement = Arrangement.Center
    ) {
        MdOutlinedTextField(
            Modifier
                .height(dimensionResource(id = R.dimen.questionnaire_detail_simple_ques_ans_value_height))
                .fillMaxWidth(),
            text,
            { text = it },
            { MDTextView(value = "test") },
            MdTheme.typography.regular14TextStyle,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.separatorColor),
                unfocusedBorderColor = colorResource(id = R.color.separatorColor),
            )
        )
    }
}


@Composable
fun SearchBar(query: String, onQueryChanged: (String) -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Icon
            Icon(
                imageVector = Icons.Default.Search, // Replace with your icon
                contentDescription = "Search Icon",
                modifier = Modifier.padding(end = 8.dp)
            )

            // Text Input Area
            BasicTextField(
                value = query,
                onValueChange = { onQueryChanged(it) },
                modifier = Modifier
                    .weight(1f) // Take up remaining space
                    .padding(vertical = 8.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp)
            )

            // Trailing Icon - Visible only if text is not empty
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Clear, // Replace with your icon
                    contentDescription = "Clear Icon",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable { onQueryChanged("") } // Clear text on click
                )
            }
        }

        // Custom underline
        MDHorizontalLine(color = Color.Gray, thickness = 1.dp, modifier = Modifier)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSingleLineEditTextWithDynamicIcon() {
    SearchBar("", {})
}

