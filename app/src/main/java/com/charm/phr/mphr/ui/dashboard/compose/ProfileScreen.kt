package com.charm.phr.mphr.ui.dashboard.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.Module
import com.charm.phr.mphr.model.Patient
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.Theme.MdDimen
import com.charm.phr.mphr.ui.Theme.MdTheme
import com.charm.phr.mphr.ui.components.compose.MDTextView
import com.charm.phr.mphr.ui.components.compose.MdCardWithoutTonalElevation
import com.charm.phr.mphr.util.APIList
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.UIUtil
import org.joda.time.LocalDate
import org.joda.time.Months
import org.joda.time.Years

val contactPracticeModule = Module(
    moduleName = R.string.contact_practice,
    moduleIconId = R.drawable.ic_contact_practice,
)

val appFeedbackModule = Module(
    moduleName = R.string.app_feedback,
    moduleIconId = R.drawable.ic_app_feeback,
)

val appLockModule = Module(
    moduleName = R.string.app_lock,
    moduleIconId = R.drawable.ic_app_lock,
)

val myInfoModule = Module(
    moduleName = if(BuildUtil.isThalassemia) R.string.my_profile else R.string.demographics,
    moduleIconId = R.drawable.ic_demograpics,
)

val logOutModule = Module(
    moduleName = R.string.log_out,
    moduleIconId = R.drawable.ic_log_out,
)

@Composable
fun ProfileScreen(
    imageLoader: ImageLoader,
    selectedPatient: Patient,
    linkedPatientList: List<Patient>,
    onLinkedMembersClick: (Patient) -> Unit,
    onMyInfoClick: (String) -> Unit,
    onPatientImageClick: (Patient) -> Unit,
    onAppFeedbackBackClick: (String) -> Unit,
    onContactPracticeClick: (String) -> Unit,
    onOtherSettingsClick: (String) -> Unit,
    onLogOutClick: (String) -> Unit,
    onBackPressed: () -> Unit,
    isMessageModuleEnabled: Boolean,
    imageReloadKey: Int
) {

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            MdTopAppBar(onBackPressed = {
                onBackPressed()
            })
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(color = colorResource(id = R.color.commonBgColor))
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                )
        ) {

            PatientImageSection(
                imageLoader = imageLoader,
                selectedPatient = selectedPatient,
                onClick = onPatientImageClick,
                imageReloadKey = imageReloadKey
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 8.dp)
            ) {
                if (linkedPatientList.isNotEmpty()) {
                    LinkedMembersSection(
                        imageLoader = imageLoader,
                        linkedPatientList = linkedPatientList,
                        onClick = onLinkedMembersClick,
                        headingTextStyle = MdTheme.typography.semiBold14TextStyle
                    )
                }
                MyInfoSection(
                    onClick = onMyInfoClick,
                    headingTextStyle = MdTheme.typography.semiBold14TextStyle
                )
                if (!BuildUtil.isThalassemia) {
                    FeedBackSection(
                        onAppFeedbackClick = onAppFeedbackBackClick,
                        onContactPracticeClick = onContactPracticeClick,
                        headingTextStyle = MdTheme.typography.semiBold14TextStyle,
                        isMessageModuleEnabled
                    )
                }
                OtherSettings(
                    onClick = onOtherSettingsClick,
                    headingTextStyle = MdTheme.typography.semiBold14TextStyle
                )
                LogOutSection(onClick = onLogOutClick)
            }
        }
    }
}

@Composable
private fun LogOutSection(onClick: (String) -> Unit) {
    if(BuildUtil.isThalassemia){
        Spacer(modifier = Modifier.height(8.dp))
    }else{
        Spacer(modifier = Modifier.height(16.dp))
    }
    MdCardWithoutTonalElevation(
        MdDimen.cardModifier, shape = MdDimen.cardRoundCornerShape
    ) {
        ItemHealthSection(
            module = logOutModule,
            onItemClicked = onClick
        )
    }
    Spacer(modifier = Modifier.height(16.dp))

}

@Composable
private fun LinkedMembersSection(
    imageLoader: ImageLoader,
    linkedPatientList: List<Patient>,
    onClick: (Patient) -> Unit,
    headingTextStyle: TextStyle,
) {
    MDTextView(
        value = stringResource(R.string.linked_members),
        modifier = MdDimen.carHeadingModifier,
        style = headingTextStyle
    )
    MdCardWithoutTonalElevation(
        modifier = MdDimen.cardModifier,
        shape = MdDimen.cardRoundCornerShape
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        linkedPatientList.forEach { patient ->
            PatientInfoItem(
                patient = patient,
                imageLoader = imageLoader,
                onItemClicked = {
                    onClick(patient)
                }
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun MyInfoSection(onClick: (String) -> Unit, headingTextStyle: TextStyle) {
    if(BuildUtil.isThalassemia){
        Spacer(modifier = Modifier.height(8.dp))
    }else{
        MDTextView(
            value = stringResource(R.string.my_info),
            modifier = MdDimen.carHeadingModifier,
            style = headingTextStyle
        )
    }
    MdCardWithoutTonalElevation(
        MdDimen.cardModifier, shape = MdDimen.cardRoundCornerShape
    ) {
        ItemHealthSection(
            module = myInfoModule,
            onItemClicked = onClick
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun FeedBackSection(
    onAppFeedbackClick: (String) -> Unit,
    onContactPracticeClick: (String) -> Unit,
    headingTextStyle: TextStyle,
    isMessageModuleEnabled: Boolean
) {

    MDTextView(
        value = stringResource(R.string.send_feedback),
        modifier = MdDimen.carHeadingModifier,
        style = headingTextStyle
    )
    MdCardWithoutTonalElevation(
        MdDimen.cardModifier, shape = MdDimen.cardRoundCornerShape
    ) {
        if (isMessageModuleEnabled) {
            ItemHealthSection(
                module = contactPracticeModule,
                onItemClicked = onContactPracticeClick
            )
        }
        ItemHealthSection(
            module = appFeedbackModule,
            onItemClicked = onAppFeedbackClick
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun OtherSettings(onClick: (String) -> Unit, headingTextStyle: TextStyle) {
    if(BuildUtil.isThalassemia){
        Spacer(modifier = Modifier.height(8.dp))
    }else{
        MDTextView(
            value = stringResource(R.string.other_settings),
            modifier = MdDimen.carHeadingModifier,
            style = headingTextStyle
        )
    }
    MdCardWithoutTonalElevation(
        MdDimen.cardModifier, shape = MdDimen.cardRoundCornerShape
    ) {
        ItemHealthSection(
            module = appLockModule,
            onItemClicked = onClick
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun PatientImageSection(
    imageLoader: ImageLoader,
    selectedPatient: Patient,
    onClick: (Patient) -> Unit,
    imageReloadKey: Int,
) {

    val hasImage = selectedPatient.isPhotoAvailableOnline
    val imageUrl = APIList.getPatientImageURL(selectedPatient.practicePatientId)
    val imgSize = 88.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                onClick(selectedPatient)
            }),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        PatientImageItem(
            hasImage = hasImage,
            imageUrl = imageUrl,
            imageLoader = imageLoader,
            imgSize = imgSize,
            selectedPatient = selectedPatient,
            textStyle = MdTheme.typography.regular18TextStyle
                .copy(fontSize = 40.sp)
                .copy(letterSpacing = 2.sp),
            imageReloadKey = imageReloadKey
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = selectedPatient.fullName ?: "",
            style = MdTheme.typography.semiBold18TextStyle,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = UIUtil.formPatientDetail(selectedPatient),
            style = MdTheme.typography.regular14TextStyle
        )
        Spacer(
            modifier = Modifier
                .height(16.dp)
                .padding(0.dp)
        )
    }
}

@Composable
fun PatientImageItem(
    hasImage: Boolean,
    imageUrl: String,
    imageLoader: ImageLoader,
    imgSize: Dp,
    selectedPatient: Patient,
    textStyle: TextStyle,
    imageReloadKey: Int,
) {
    if (hasImage) {
        var modelUrl by remember { mutableStateOf<String?>(imageUrl) }
        LaunchedEffect(imageReloadKey, imageUrl) {
            modelUrl = null
            // Small delay to ensure recomposition
            kotlinx.coroutines.delay(200)
            modelUrl = imageUrl
        }
        SubcomposeAsyncImage(
            model = modelUrl,
            imageLoader = imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(imgSize)
                .clip(CircleShape)
        ) {
            val painterState = painter.state
            if (painterState is AsyncImagePainter.State.Success) {
                SubcomposeAsyncImageContent()
            } else {
                CircleImageViewWithInitials(
                    initial = UIUtil.formMemberInitials(selectedPatient.fullName ?: ""),
                    size = imgSize,
                    style = textStyle
                )
            }
        }
    } else {
        CircleImageViewWithInitials(
            initial = UIUtil.formMemberInitials(selectedPatient.fullName ?: ""),
            size = imgSize,
            style = textStyle
        )
    }
}

@Composable
fun CircleImageViewWithInitials(
    initial: String,
    size: Dp = 88.dp,
    style: TextStyle = MdTheme.typography.medium18TextStyle
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color = colorResource(id = R.color.cardSelectedBgColor)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = style,
            color = colorResource(R.color.colorAccent)
        )
    }
}

@Composable
@Preview()
fun ProfileScreenPreview() {
    MPHRTheme {
        ProfileScreen(

            imageLoader = ImageLoader(LocalContext.current),

            selectedPatient = Patient(
                fullName = "Lewis Hamilton adfasdf asdfasdf asdfasdf asdfasdf",
                gender = "Male",
                dateOfBirth = LocalDate.now().minus(Years.years(43))
            ),

            linkedPatientList = arrayListOf(
                Patient(
                    fullName = "Lewis Hamilton asdfasdf asdfasdf asdfasdf asdfas",
                    gender = "Male",
                    dateOfBirth = LocalDate.now().minus(Years.years(30))
                ),
                Patient(
                    fullName = "Max Verstappen",
                    gender = "Male",
                    dateOfBirth = LocalDate.now().minus(Months.months(3))
                )
            ),
            onLinkedMembersClick = {

            },
            onMyInfoClick = {

            },
            onPatientImageClick = {

            },
            onAppFeedbackBackClick = {

            },
            onContactPracticeClick = {

            },
            onOtherSettingsClick = {

            },
            onLogOutClick = {

            },
            onBackPressed = {

            },
            isMessageModuleEnabled = true,
            imageReloadKey = 0
        )
    }
}

//@Composable
//@Preview(showBackground = true)
//fun PatientImageItemPreview() {
//    MPHRTheme {
//        Box(modifier = Modifier.background(MdColors.Black)){
//            PatientImageItem(
//                hasImage = true,
//                imageUrl = "https://example.com/patient_image.jpg",
//                imageLoader = ImageLoader(LocalContext.current),
//                imgSize = 88.dp,
//                selectedPatient = Patient(fullName = "John Doe", gender = "Male", dateOfBirth = LocalDate.now().minus(Years.years(30))),
//                textStyle = MdTheme.typography.regular18TextStyle.copy(fontSize = 40.sp).copy(letterSpacing = 2.sp)
//            )
//        }
//    }
//}


