package com.charm.phr.mphr.ui.dashboard.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.Theme.MdTheme
import com.charm.phr.mphr.util.APIList
import com.charm.phr.mphr.util.UIUtil
import org.joda.time.LocalDate
import org.joda.time.Months

@Composable
fun PatientInfoItem(
    patient: Patient,
    imageLoader: ImageLoader,
    onItemClicked: (Patient) -> Unit,
) {

    val mutableInteractionSource = remember {
        MutableInteractionSource()
    }
    val imgSize = 48.dp

    val hasImage = patient.isPhotoAvailableOnline
    val imageUrl = APIList.getPatientImageURL(patient.practicePatientId)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = mutableInteractionSource,
                indication = ripple(bounded = true)
            ) {
                onItemClicked(patient)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(Modifier.padding(horizontal = 8.dp)) {
            PatientImageItem(
                hasImage = hasImage,
                imageUrl = imageUrl,
                imageLoader = imageLoader,
                imgSize = imgSize,
                selectedPatient = patient,
                textStyle = MdTheme.typography.medium18TextStyle.copy(letterSpacing = 1.sp),
                imageReloadKey = 0 // Replace with actual image reload key if needed
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                patient.fullName ?: "",
                style = MdTheme.typography.semiBold14TextStyle.copy(fontSize = 15.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(UIUtil.formPatientDetail(patient), style = MdTheme.typography.regular14TextStyle)
        }
        GreyRightArrow()
        Spacer(modifier = Modifier.size(16.dp))
    }
}

@Composable
@Preview(showBackground = true)
fun PatientInfoItemPreview() {
    MPHRTheme {
        PatientInfoItem(
            Patient(fullName = "Max Verstappen", gender = "Male", dateOfBirth = LocalDate.now().minus(Months.months(27))),
            ImageLoader.Builder(LocalContext.current)
                .crossfade(true)
                .build(),
            {}
        )
    }
}