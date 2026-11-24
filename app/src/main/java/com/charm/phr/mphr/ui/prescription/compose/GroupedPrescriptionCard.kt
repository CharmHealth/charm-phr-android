package com.charm.phr.mphr.ui.prescription.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.model.PatMedPOJO
import com.charm.phr.mphr.model.PatientSupplements
import com.charm.phr.mphr.ui.Theme.MdTheme
import com.charm.phr.mphr.ui.components.compose.MDHorizontalLine
import com.charm.phr.mphr.ui.components.compose.MDTextView
import com.charm.phr.mphr.ui.components.compose.MdCardWithoutTonalElevation
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.UIUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun GroupedSupplementCardLayout(
    patientSupplementList: ArrayList<PatientSupplements>,
    fromReminder: Boolean,
    onRefillClick: (PatientSupplements) -> Unit,
    onEditClick: (PatientSupplements) -> Unit,
    onDeleteClick: (PatientSupplements) -> Unit,
    onReminderClick: (PatientSupplements) -> Unit,
    allowToEdit: Boolean,
    addedByPatient: Boolean
) {
    var isCollapsed by remember { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()
    val cardModifier = if (allowToEdit && addedByPatient && patientSupplementList.size == 1 && !fromReminder) {
        Modifier
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        coroutineScope.launch {
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press) // Start ripple
                            onDeleteClick(patientSupplementList.first())
                            delay(200) // Add delay before releasing ripple

                            interactionSource.emit(PressInteraction.Release(press)) // Stop ripple
                        }
                    }
                )
            }
    } else if (patientSupplementList.size > 1 && !fromReminder) {
        Modifier.clickable {
            isCollapsed = !isCollapsed
        }
    } else if (fromReminder && patientSupplementList.size == 1) {
        Modifier.clickable {
            onReminderClick(patientSupplementList.first())
        }
    } else {
        Modifier
    }
    if (fromReminder) {
        isCollapsed = false
    }
    MdCardWithoutTonalElevation(
        elevation = 1.dp,
        shape = RoundedCornerShape(dimensionResource(R.dimen.common_card_radius)),
        modifier = cardModifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.card_margin_horizontal), vertical = dimensionResource(id = R.dimen.card_margin_vertical))
    ) {
        var supplementsName = patientSupplementList.firstOrNull()?.supplement?.supplementName

        val noOfIntake =
            patientSupplementList.firstOrNull()?.patientSupplementMap?.patientSupplementHistory?.numberOfIntake
        var intakeUnit =
            patientSupplementList.firstOrNull()?.patientSupplementMap?.patientSupplementHistory?.intakeUnit ?: ""
        if (intakeUnit == "-") {
            intakeUnit = ""
        }
        if (noOfIntake != null && (noOfIntake.trim().toFloatOrNull() ?: -1f) > 0f) {
            supplementsName += " $noOfIntake $intakeUnit"
        }

        PrescriptionHeaderView(supplementsName ?: "", true, patientSupplementList.size > 1 && !fromReminder, isCollapsed)
        RenderSupplementItem(
            patientSupplementList.first(),
            allowToEdit,
            addedByPatient,
            onRefillClick,
            onEditClick,
            onDeleteClick,
            fromReminder,
            onItemClick = { medication ->
                if (fromReminder) {
                    onReminderClick(medication)
                } else {
                    isCollapsed = !isCollapsed
                }
            }, patientSupplementList.size > 1
        )
        AnimatedVisibility(visible = !isCollapsed) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                RenderSupplementList(
                    patientSupplementList,
                    allowToEdit,
                    addedByPatient,
                    onRefillClick,
                    onEditClick,
                    onDeleteClick,
                    fromReminder
                ) { supplements ->
                    if (fromReminder) {
                        onReminderClick(supplements)
                    } else {
                        isCollapsed = !isCollapsed
                    }
                }
            }
        }
    }
}


@Composable
fun DrugGroupedMedicationCardLayout(
    patMedList: List<PatMedPOJO>, allowToEdit: Boolean = true, onRefillClick: (medications: PatMedPOJO) -> Unit,
    onEditClick: (medications: PatMedPOJO) -> Unit, onDeleteClick: (medications: PatMedPOJO) -> Unit, fromReminder: Boolean, addedByPatient: Boolean = false,
    onReminderClick: (medications: PatMedPOJO) -> Unit
) {
    var isCollapsed by remember { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()
    val cardModifier = if (allowToEdit && addedByPatient && patMedList.size == 1 && !fromReminder) {
        Modifier
            .indication(interactionSource, LocalIndication.current)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        coroutineScope.launch {
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press) // Start ripple
                            onDeleteClick(patMedList.first())
                            delay(200) // Add delay before releasing ripple

                            interactionSource.emit(PressInteraction.Release(press)) // Stop ripple
                        }
                    }
                )
            }
    } else if (patMedList.size > 1 && !fromReminder) {
        Modifier.clickable {
            isCollapsed = !isCollapsed
        }
    } else if (fromReminder && patMedList.size == 1) {
        Modifier.clickable {
            onReminderClick(patMedList.first())
        }
    } else {
        Modifier
    }
    if (fromReminder) {
        isCollapsed = false
    }
    MdCardWithoutTonalElevation(
        elevation = 1.dp,
        shape = RoundedCornerShape(dimensionResource(R.dimen.common_card_radius)),
        modifier = cardModifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(id = R.dimen.card_margin_horizontal), vertical = dimensionResource(id = R.dimen.card_margin_vertical))
    ) {
        var medicationName: String?
        if (!addedByPatient) {
            medicationName = UIUtil.getRxDisplayName(patMedList.first(), LocalContext.current)
        } else {
            medicationName = patMedList.first().drugDetail.firstOrNull()?.genericDrugName
            val noOfIntake = patMedList.first().patMedPHRExt.firstOrNull()?.noOfIntakes
            var intakeUnit = patMedList.first().patMedPHRExt.firstOrNull()?.intakeUnit ?: ""
            if (intakeUnit == "-") {
                intakeUnit = ""
            }
            if ((noOfIntake != null) && ((noOfIntake.trim().toFloatOrNull() ?: -1f) > 0f)) {
                medicationName += " $noOfIntake $intakeUnit"
            }
        }
        PrescriptionHeaderView(medicationName ?: "", false, patMedList.size > 1 && !fromReminder, isCollapsed)
        RenderMedicationItem(
            patMedList.first(),
            allowToEdit,
            addedByPatient,
            onRefillClick,
            onEditClick,
            onDeleteClick,
            fromReminder,
            onItemClick = { medication ->
                if (fromReminder) {
                    onReminderClick(medication)
                } else {
                    isCollapsed = !isCollapsed
                }
            }, patMedList.size > 1
        )

        AnimatedVisibility(visible = !isCollapsed) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                RenderMedicationList(
                    patMedList,
                    allowToEdit,
                    addedByPatient,
                    onRefillClick,
                    onEditClick,
                    onDeleteClick,
                    fromReminder,
                    { medication ->
                        if (fromReminder) {
                            onReminderClick(medication)
                        } else {
                            isCollapsed = !isCollapsed
                        }
                    })
            }
        }
    }
}

@Composable
fun PrescriptionHeaderView(headerName: String, isSupplement: Boolean, addExpandView: Boolean, isCollapsed: Boolean) {
    val rotationAngle by animateFloatAsState(if (!isCollapsed) 180f else 0f, label = "")
    val modifier = if (isSupplement) Modifier
        .padding(
            start = dimensionResource(R.dimen.card_padding_half),
            top = 12.dp,
            end = dimensionResource(R.dimen.card_padding_half),
        )
        .width(24.dp)
        .height(20.dp) else Modifier
        .padding(
            start = dimensionResource(R.dimen.card_padding),
            top = dimensionResource(R.dimen.card_padding),
            end = dimensionResource(R.dimen.card_padding)
        )
        .size(24.dp)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Icon(
                painter = if (isSupplement) painterResource(id = R.drawable.ic_supplements_24) else painterResource(id = R.drawable.ic_tablet_24),
                contentDescription = null,
                modifier = modifier,
                tint = colorResource(R.color.colorPrimary)
            )

            MDTextView(
                modifier = Modifier
                    .padding(top = dimensionResource(R.dimen.card_padding), end = dimensionResource(R.dimen.card_padding)),
                value = headerName, style = MdTheme.typography.semiBold16TextStyle
            )
        }
        if (addExpandView) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = dimensionResource(R.dimen.card_padding), top = dimensionResource(R.dimen.card_padding))
                    .size(24.dp)
                    .rotate(rotationAngle),
                tint = colorResource(R.color.colorPrimary)
            )
        }
    }
}

@Composable
fun RenderMedicationList(
    patMedList: List<PatMedPOJO>,
    allowToEdit: Boolean = true,
    isAddedByPatient: Boolean,
    onRefillClick: (medications: PatMedPOJO) -> Unit,
    onEditClick: (medications: PatMedPOJO) -> Unit,
    onDeleteClick: (medications: PatMedPOJO) -> Unit,
    fromReminder: Boolean,
    onItemClick: (medications: PatMedPOJO) -> Unit
) {
    patMedList.drop(1).forEach { medication ->
        MDHorizontalLine(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.card_padding_half)),
            1.dp,
            colorResource(R.color.practiceSeparator)
        )
        RenderMedicationItem(
            medication,
            allowToEdit,
            isAddedByPatient,
            onRefillClick,
            onEditClick,
            onDeleteClick,
            fromReminder,
            onItemClick = { selectedMedication -> onItemClick(selectedMedication) }, true
        )
    }
}

@Composable
fun RenderMedicationItem(
    medication: PatMedPOJO,
    allowToEdit: Boolean,
    isAddedByPatient: Boolean,
    onRefillClick: (medications: PatMedPOJO) -> Unit,
    onEditClick: (medications: PatMedPOJO) -> Unit,
    onDeleteClick: (medications: PatMedPOJO) -> Unit,
    fromReminder: Boolean, onItemClick: (medications: PatMedPOJO) -> Unit, isMultipleLayout: Boolean
) {
    val direction: String?
    var showRefill = false
    var memberName: String? = null
    var refill: String? = null
    var dispense: String? = null
    if (!isAddedByPatient) {
        direction = medication.patMedExt.firstOrNull()?.directions
        showRefill =
            medication.patMed?.addedAs == ModelUtil.medicationAddedAsPrescription && medication.allowRefill && !medication.member?.memberId.isNullOrBlank() && !fromReminder && medication.patMed?.isActive == true
        memberName = UIUtil.formMemberName(medication.member)
        refill = medication.patMedExt.firstOrNull()?.refill
        dispense = medication.patMedExt.firstOrNull()?.dispense
    } else {
        direction = UIUtil.formDirectionsForMedication(medication)
    }
    PrescriptionItemDetails(
        false,
        direction = direction,
        prescribedBy = memberName,
        prescribedOn = medication.patMed?.startDate?.toString(ModelUtil.charmDisplayDateFormat()),
        stopDate = medication.patMed?.stopDate?.toString(ModelUtil.charmDisplayDateFormat()),
        dispense = dispense,
        refill = refill,
        showRefill = showRefill,
        isAddedByPatient && allowToEdit && !fromReminder,
        onRefillClick = { onRefillClick(medication) },
        { onEditClick(medication) },
        onDeleteClick = { onDeleteClick(medication) },
        itemOnClick = { onItemClick(medication) },
        fromReminder, isMultipleLayout
    )
}

@Composable
fun RenderSupplementList(
    supplementList: List<PatientSupplements>,
    allowToEdit: Boolean = true,
    isAddedByPatient: Boolean,
    onRefillClick: (medications: PatientSupplements) -> Unit,
    onEditClick: (medications: PatientSupplements) -> Unit,
    onDeleteClick: (medications: PatientSupplements) -> Unit,
    fromReminder: Boolean,
    onItemClick: (medications: PatientSupplements) -> Unit,
) {
    supplementList.drop(1).forEach { supplements ->
        MDHorizontalLine(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.card_padding_half)),
            1.dp,
            colorResource(R.color.practiceSeparator)
        )
        RenderSupplementItem(
            supplements,
            allowToEdit,
            isAddedByPatient,
            onRefillClick,
            onEditClick,
            onDeleteClick,
            fromReminder,
            onItemClick = { selectedSupplements -> onItemClick(selectedSupplements) }, true
        )
    }

}

@Composable
fun RenderSupplementItem(
    supplements: PatientSupplements,
    allowToEdit: Boolean,
    isAddedByPatient: Boolean,
    onRefillClick: (supplements: PatientSupplements) -> Unit,
    onEditClick: (supplements: PatientSupplements) -> Unit,
    onDeleteClick: (supplements: PatientSupplements) -> Unit,
    fromReminder: Boolean, onItemClick: (supplements: PatientSupplements) -> Unit, isMultipleLayout: Boolean
) {
    val direction: String?
    var showRefill = false
    var memberName: String? = null
    if (!isAddedByPatient) {
        direction = supplements.patientSupplementMap?.patientSupplementHistory?.instructions
        showRefill = supplements.allowRefill && supplements.isActive == true && !supplements.member?.memberId.isNullOrBlank() && !fromReminder
        memberName = UIUtil.formMemberName(supplements.member)
    } else {
        direction = UIUtil.formDirectionsForSupplement(supplements)
    }
    PrescriptionItemDetails(
        true,
        direction = direction,
        prescribedBy = memberName,
        prescribedOn = supplements.startDate?.toString(ModelUtil.charmDisplayDateFormat()),
        stopDate = supplements.stopDate?.toString(ModelUtil.charmDisplayDateFormat()),
        dispense = null,
        refill = null,
        showRefill = showRefill,
        isAddedByPatient && allowToEdit && !fromReminder,
        onRefillClick = { onRefillClick(supplements) },
        { onEditClick(supplements) },
        onDeleteClick = { onDeleteClick(supplements) },
        itemOnClick = { onItemClick(supplements) },
        fromReminder, isMultipleLayout
    )
}


@Preview(showBackground = true)
@Composable
fun DrugGroupedMedicationItemPreview() {
    DrugGroupedMedicationCardLayout(
        patMedList = arrayListOf(), true, {}, {}, {}, true, false, {}
    )
}