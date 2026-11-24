package com.charm.phr.mphr.ui.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.components.compose.CustomDropdownMenuItem

@Composable
fun AppointmentMenuDetails(onCancelAppointment: () -> Unit?,cancelMenuString: String) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .wrapContentWidth()
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(painter = painterResource(R.drawable.ic_more_vert_black), contentDescription = "More options")
        }
        DropdownMenu(
            modifier = Modifier.background(color = colorResource(R.color.white)),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CustomDropdownMenuItem(painterResource(R.drawable.ic_calendar_cancel), cancelMenuString, {
                expanded = false
                onCancelAppointment()
            })
        }
    }
}



@Composable
@Preview(showSystemUi = true)
fun PreviewUI() {
    MPHRTheme {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            AppointmentMenuDetails(onCancelAppointment = {}, stringResource(R.string.cancel_appointment_str))
            CustomDropdownMenuItem(painterResource(R.drawable.ic_calendar_cancel), stringResource(R.string.cancel_appointment_str), {})
        }
    }

}