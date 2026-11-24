package com.charm.phr.mphr.ui.dashboard.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.Module
import com.charm.phr.mphr.model.UnreadCount
import com.charm.phr.mphr.ui.Theme.MdDimen
import com.charm.phr.mphr.ui.Theme.MdTheme
import com.charm.phr.mphr.ui.components.compose.MDTextView
import com.charm.phr.mphr.ui.components.compose.MdCardWithoutTonalElevation
import com.charm.phr.mphr.ui.dashboard.DashboardViewModel
import com.charm.phr.mphr.util.BuildUtil
import com.charm.phr.mphr.util.ModelUtil

@Composable
fun MyHealthScreen(
    viewModel: DashboardViewModel,
    recordsList: List<Module>,
    engagementList: List<Module>,
    billingList: List<Module>,
    resourcesList: List<Module>,
    onScrollStateChange: (scrollY: Int) -> Unit,
    onClickItem: (String) -> Unit,
) {

    val unreadCountState = viewModel.unreadCountLive.observeAsState()
    val updatedUnreadCountState = rememberUpdatedState(unreadCountState.value)

    val scrollState = rememberScrollState()
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .collect { scrollValue ->
                onScrollStateChange(scrollValue)
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(colorResource(R.color.commonBgColor))
            .padding(horizontal = 8.dp)
    ) {

        if (engagementList.isNotEmpty()) {
            EngagementSection(engagementList, MdTheme.typography.semiBold14TextStyle, onClickItem = { onClickItem(it) }, unreadCountState)
        }
        if (billingList.isNotEmpty()) {
            BillingSection(billingList, MdTheme.typography.semiBold14TextStyle, onClickItem = { onClickItem(it) }, unreadCountState)
        }
        if (recordsList.isNotEmpty()) {
            RecordsSection(recordsList, MdTheme.typography.semiBold14TextStyle, onClickItem = { onClickItem(it) }, unreadCountState)
        }
        if (resourcesList.isNotEmpty() && !BuildUtil.isThalassemia) {
            ResourcesSection(resourcesList, MdTheme.typography.semiBold14TextStyle, onClickItem = { onClickItem(it) })
        }

        Spacer(modifier = Modifier.size(16.dp))

    }

}

@Composable
private fun ResourcesSection(
    resourcesList: List<Module>,
    headingTextStyle: TextStyle,
    onClickItem: (String) -> Unit,
) {
    Spacer(
        modifier = Modifier.size(MdDimen.spaceAboveHeader)
    )
    MDTextView(
        value = stringResource(R.string.resources),
        modifier = MdDimen.carHeadingModifier,
        style = headingTextStyle
    )

    MdCardWithoutTonalElevation(MdDimen.cardModifier, shape = MdDimen.cardRoundCornerShape) {
        resourcesList.forEach { module ->

            ItemHealthSection(
                module = module,
                onItemClicked = {
                    onClickItem(module.moduleId)
                }
            )
        }
    }
}

@Composable
private fun BillingSection(
    billingList: List<Module>,
    headingTextStyle: TextStyle,
    onClickItem: (String) -> Unit,
    unreadCountState: State<UnreadCount?>
) {

    val billingUnreadCount = remember(unreadCountState.value) { unreadCountState.value?.billingCount }

    Spacer(modifier = Modifier.size(MdDimen.spaceAboveHeader))
    MDTextView(
        value = stringResource(R.string.billing_str),
        modifier = MdDimen.carHeadingModifier,
        style = headingTextStyle
    )


    MdCardWithoutTonalElevation(MdDimen.cardModifier, shape = MdDimen.cardRoundCornerShape) {
        billingList.forEach { item ->
            val unreadCount = when (item.moduleId) {
                ModelUtil.billingModelId -> {
                    billingUnreadCount
                }

                else -> {
                    0
                }
            }
            ItemHealthSection(
                module = item,
                onItemClicked = {
                    onClickItem(item.moduleId)
                },
                badgeText = unreadCount
            )
        }
    }
}

@Composable
private fun EngagementSection(
    engagementList: List<Module>,
    headingTextStyle: TextStyle,
    onClickItem: (String) -> Unit,
    unreadCountState: State<UnreadCount?>
) {

    val messageUnreadCount = remember(unreadCountState.value) { unreadCountState.value?.messageUnreadCount }
    val upcomingAppointmentCount = remember(unreadCountState.value) { unreadCountState.value?.upcomingAppointmentCount }
    val questionnaireCount = remember(unreadCountState.value) { unreadCountState.value?.questionnaireUnfilledCount }

    Spacer(modifier = Modifier.size(MdDimen.spaceAboveHeader))
    MDTextView(
        value = stringResource(R.string.engagement),
        modifier = MdDimen.carHeadingModifier,
        style = headingTextStyle
    )

    MdCardWithoutTonalElevation(modifier = MdDimen.cardModifier, shape = MdDimen.cardRoundCornerShape) {
        engagementList.forEach { item ->
            val unreadCount = when (item.moduleId) {
                ModelUtil.messageModuleId -> {
                    messageUnreadCount
                }

                ModelUtil.appointmentModelId -> {
                    upcomingAppointmentCount
                }

                ModelUtil.questionnaireModelId -> {
                    questionnaireCount
                }

                else -> 0
            }
            ItemHealthSection(
                module = item,
                onItemClicked = {
                    onClickItem(item.moduleId)
                },
                badgeText = unreadCount
            )
        }
    }
}

@Composable
private fun RecordsSection(
    recordsModuleList: List<Module>,
    headingTextStyle: TextStyle,
    onClickItem: (String) -> Unit,
    unreadCountState: State<UnreadCount?>
) {
    Spacer(modifier = Modifier.size(MdDimen.spaceAboveHeader))
    MDTextView(
        value = stringResource(R.string.records),
        modifier = MdDimen.carHeadingModifier,
        style = headingTextStyle
    )

    val labUnreadCount = remember(unreadCountState.value) { unreadCountState.value?.labUnreadCount }
    val documentUnreadCount = remember(unreadCountState.value) { unreadCountState.value?.documentUnreadCount }
    val visitSummaryUnreadCount = remember(unreadCountState.value) { unreadCountState.value?.visitSummaryUnreadCount }

    MdCardWithoutTonalElevation(MdDimen.cardModifier, shape = MdDimen.cardRoundCornerShape) {
        recordsModuleList.forEach { module ->
            val unreadCount = when (module.moduleId) {
                ModelUtil.labModelId -> {
                    labUnreadCount
                }

                ModelUtil.documentModelId -> {
                    documentUnreadCount
                }

                ModelUtil.visitSummaryModelId -> {
                    visitSummaryUnreadCount
                }

                else -> 0
            }

            ItemHealthSection(
                module = module,
                onItemClicked = {
                    onClickItem(module.moduleId)
                }, badgeText = unreadCount
            )
        }
    }
}

@Composable
fun GreyRightArrow() {
    Image(
        painter = painterResource(id = R.drawable.ic_arrow_back_accent),
        contentDescription = null,
        modifier = Modifier
            .size(16.dp)
            .rotate(180f),
        colorFilter = ColorFilter.tint(colorResource(R.color.black30))
    )
}

//@Composable
//@Preview(showBackground = true)
//fun MyHealthScreenPreview() {
//    MPHRTheme {
//        MyHealthScreen(
//            state = viewModel(),
//
//            recordsList = listOf(
//                Module("Health Passport", moduleIconId = R.drawable.ic_health_passport),
//                Module("Documents", moduleIconId = R.drawable.ic_documents),
//                Module("Insurance", moduleIconId = R.drawable.ic_insurance),
//                Module("Prescriptions", moduleIconId = R.drawable.ic_prescriptions),
//                Module("Labs", moduleIconId = R.drawable.ic_labs),
//                Module("Allergies", moduleIconId = R.drawable.ic_allergies),
//                Module("Visit Summary", moduleIconId = R.drawable.ic_visit_summary),
//                Module("Procedures", moduleIconId = R.drawable.ic_procedures),
//                Module("Immunisations", moduleIconId = R.drawable.ic_immunisations),
//            ),
//            engagementList = listOf(
//                Module("Appointments", moduleIconId = R.drawable.ic_appointment),
//                Module("Messages", moduleIconId = R.drawable.ic_messages),
//                Module("Flowsheets", moduleIconId = R.drawable.ic_flowsheets),
//                Module("Reminders", moduleIconId = R.drawable.ic_reminders),
//                Module("Questionnaires", moduleIconId = R.drawable.ic_questionnaires),
//                Module("Wellness", moduleIconId = R.drawable.ic_wellness)
//            ),
//            billingList = listOf(
//                Module("Invoice", moduleIconId = R.drawable.ic_invoice),
//                Module("Receipt", moduleIconId = R.drawable.ic_receipt),
//                Module("Card On File", moduleIconId = R.drawable.ic_card_on_file)
//            ),
//            resourcesList = listOf(
//                Module("Useful Info", moduleIconId = R.drawable.ic_useful_info),
//                Module("About", moduleIconId = R.drawable.ic_about)
//            ),
//            onScrollStateChange = { scrollY ->
//
//            },
//            onClickItem = {
//
//            }
//        )
//    }
//}

