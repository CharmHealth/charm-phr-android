package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Preview(showBackground = true)
@Composable
fun OpenTokMeetingDialogPreview() {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        OpenTokMeetingDialog(passcode = "351578", onCopyAndJoinClicked = {
            showDialog = false // Dismiss the dialog on proceed
        }, onDismiss = {
            showDialog = false
        })
    }

}

@Composable
fun OpenTokMeetingDialog(
    passcode: String, onCopyAndJoinClicked: () -> Unit, onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier,
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .background(color = Color.White, shape = MaterialTheme.shapes.medium)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Video Consultation", style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "To join the call, please enter the passcode on the next screen.", style = TextStyle(fontSize = 14.sp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Color(0xFFF5F5F5), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionContainer(Modifier.weight(1f)) {
                        Row(modifier = Modifier.height(42.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = passcode,
                                style = TextStyle(fontSize = 18.sp, letterSpacing = 2.sp),
                                modifier = Modifier
                                    .weight(1f),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp), horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onCopyAndJoinClicked,
                        modifier = Modifier.padding(start = 4.dp),
                    ) {
                        Text(text = "Copy & Join", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }

        }

    }
}

