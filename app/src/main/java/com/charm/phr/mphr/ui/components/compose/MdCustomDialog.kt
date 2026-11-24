package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.Theme.MdTheme


@Preview(showBackground = true)
@Composable
fun MdCustomMessageDialogPreview() {
    var showDialog by remember { mutableStateOf(true) }
    MPHRTheme {
        if (showDialog) {
            MdCustomMessageDialog(
                modifier = Modifier,
                messageText = stringResource(R.string.appointment_cancel_error_msg),
                onDismiss = {
                    showDialog = false
                })
        }
    }

}

@Preview(showBackground = true)
@Composable
fun MdCustomMessageDialogPreview1() {
    var showDialog by remember { mutableStateOf(true) }
    MPHRTheme {
        if (showDialog) {
            MdMessageActionCustomDialog(
                messageText = stringResource(R.string.appointment_cancel_error_msg),
                primaryButtonText = stringResource(R.string.contact_practice),
                secondaryButtonText = stringResource(R.string.close_str),
                primaryAction = {},
                onDismiss = {
                    showDialog = false
                })
        }
    }

}

@Composable
fun MdMessageActionCustomDialog(
    messageText: String, primaryButtonText: String, secondaryButtonText: String,
    primaryAction: () -> Unit, onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier,
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(color = Color.White, shape = MaterialTheme.shapes.medium)
                    .padding(8.dp)
            ) {

                MDTextView(
                    value = messageText, style = MdTheme.typography.medium16TextStyle.copy(lineHeight = 24.sp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {

                    MdOutlineButton(modifier = Modifier.fillMaxWidth(), text = primaryButtonText) { primaryAction() }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonColors(
                            contentColor = Color.White,
                            containerColor = colorResource(R.color.colorPrimary),
                            disabledContainerColor = colorResource(R.color.greyTrans70),
                            disabledContentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    ) {
                        MDTextView(
                            value = secondaryButtonText,
                            style = MdTheme.typography.semiBold16TextStyle.copy(color = colorResource(R.color.textColorForColorPrimaryBg))
                        )
                    }

                }
            }

        }
    }
}

@Composable
fun MdCustomMessageDialog(modifier: Modifier = Modifier, messageText: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(color = Color.White, shape = MaterialTheme.shapes.medium)
                    .padding(8.dp)
            ) {

                MDTextView(
                    value = messageText, style = MdTheme.typography.medium16TextStyle.copy(lineHeight = 24.sp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(), horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonColors(
                            contentColor = colorResource(R.color.White),
                            containerColor = colorResource(R.color.colorPrimary),
                            disabledContainerColor = colorResource(R.color.greyTrans70),
                            disabledContentColor = colorResource(R.color.White)
                        )
                    ) {
                        MDTextView(
                            value = stringResource(R.string.close_str),
                            style = MdTheme.typography.semiBold16TextStyle.copy(color = colorResource(R.color.white))
                        )
                    }
                }

            }

        }

    }

}