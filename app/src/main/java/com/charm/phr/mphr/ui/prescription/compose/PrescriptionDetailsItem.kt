package com.charm.phr.mphr.ui.prescription.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MdTheme
import com.charm.phr.mphr.ui.components.compose.MDTextView


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrescriptionItemDetails(
    isSupplement: Boolean,
    direction: String?,
    prescribedBy: String?,
    prescribedOn: String?,
    stopDate: String?,
    dispense: String?,
    refill: String?,
    showRefill: Boolean = false,
    allowToEdit: Boolean,
    onRefillClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    itemOnClick: () -> Unit,
    fromReminder: Boolean, isMultipleLayout: Boolean
) {
    val modifier = if (allowToEdit && isMultipleLayout && !fromReminder) {
        Modifier.combinedClickable(
            onClick = {
                itemOnClick()
            },
            onLongClick = {
                onDeleteClick()
            },
            indication = ripple(),
            interactionSource = remember { MutableInteractionSource() }
        )
    } else if (fromReminder && isMultipleLayout) {
        Modifier.clickable {
            itemOnClick()
        }
    } else {
        Modifier
    }
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (!direction.isNullOrBlank()) {
            MDTextView(
                value = direction,
                style = MdTheme.typography.regular14TextStyle,
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.common_card_item_margin_vertical),
                    start = if (isSupplement) dimensionResource(R.dimen.selectable_min_height) else dimensionResource(R.dimen.margin_start_drug_detail),
                    end = dimensionResource(R.dimen.card_padding)
                )
            )
        }
        PrescriptionSubItemDetails(
            isSupplement = isSupplement,
            prescribedBy = prescribedBy,
            prescribedOn = prescribedOn,
            stopDate = stopDate,
            dispense = dispense,
            refill = refill,
            showRefill = showRefill,
            allowToEdit = allowToEdit,
            onRefillClick = onRefillClick,
            onEditClick = onEditClick,
        )
    }
}

@Composable
fun PrescriptionSubItemDetails(
    prescribedBy: String?,
    prescribedOn: String?,
    stopDate: String?,
    dispense: String?,
    refill: String?,
    showRefill: Boolean = false,
    allowToEdit: Boolean,
    onRefillClick: () -> Unit,
    onEditClick: () -> Unit,
    isSupplement: Boolean
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .padding(bottom = dimensionResource(R.dimen.card_padding))
        ) {
            if (!prescribedBy.isNullOrBlank()) {
                LabelValueText(
                    label = stringResource(R.string.prescription_prescribed_by_label),
                    value = prescribedBy,
                    modifier = Modifier.padding(
                        start = if (isSupplement) dimensionResource(R.dimen.selectable_min_height) else dimensionResource(R.dimen.margin_start_drug_detail),
                        top = dimensionResource(R.dimen.common_card_item_margin_vertical)
                    ),
                    valueModifier = Modifier.padding(start = dimensionResource(R.dimen.prescription_list_more_value_start_margin))
                )
            }
            if (!prescribedOn.isNullOrBlank()) {
                LabelValueText(
                    label = stringResource(R.string.prescribed_on_label),
                    value = prescribedOn,
                    modifier = Modifier.padding(
                        start = if (isSupplement) dimensionResource(R.dimen.selectable_min_height) else dimensionResource(R.dimen.margin_start_drug_detail),
                        top = dimensionResource(R.dimen.common_card_item_margin_vertical)
                    ),
                    valueModifier = Modifier.padding(start = dimensionResource(R.dimen.prescription_list_more_value_start_margin))
                )
            }
            if (!stopDate.isNullOrBlank()) {
                LabelValueText(
                    label = stringResource(R.string.prescription_stop_date_label),
                    value = stopDate,
                    modifier = Modifier.padding(
                        start = if (isSupplement) dimensionResource(R.dimen.selectable_min_height) else dimensionResource(R.dimen.margin_start_drug_detail),
                        top = dimensionResource(R.dimen.common_card_item_margin_vertical)
                    ),
                    valueModifier = Modifier.padding(start = 32.dp)
                )
            }
            if (!dispense.isNullOrBlank() || !refill.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .padding(
                            start = if (isSupplement) dimensionResource(R.dimen.selectable_min_height) else dimensionResource(R.dimen.margin_start_drug_detail),
                            top = dimensionResource(R.dimen.common_card_item_margin_vertical)
                        )
                        .fillMaxWidth()

                ) {
                    if (!dispense.isNullOrBlank()) {
                        LabelValueText(
                            label = stringResource(R.string.prescription_dispense_label),
                            value = dispense,
                            modifier = Modifier.wrapContentWidth(),
                            valueModifier = Modifier.padding(start = dimensionResource(R.dimen.prescription_list_more_value_start_margin))
                        )

                    }
                    if (dispense.isNullOrBlank() && !refill.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    if (!refill.isNullOrBlank()) {
                        LabelValueText(
                            label = stringResource(R.string.prescription_refill_label),
                            value = refill,
                            modifier = Modifier.padding(start = dimensionResource(R.dimen.card_padding)),
                            valueModifier = Modifier.padding(start = dimensionResource(R.dimen.prescription_list_more_value_start_margin))
                        )
                    }
                }
            }
        }

        if (showRefill) {
            OutlinedButton(
                onClick = onRefillClick,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, colorResource(R.color.prescriptionListRefillTxtColor)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorResource(R.color.prescriptionListRefillTxtColor),
                ),
                modifier = Modifier
                    .padding(end = dimensionResource(R.dimen.card_padding), bottom = dimensionResource(R.dimen.card_padding))
                    .height(dimensionResource(R.dimen.prescription_refill_height))
                    .align(Alignment.BottomEnd),
                contentPadding = PaddingValues(0.dp)
            ) {
                MDTextView(
                    modifier = Modifier,
                    value = stringResource(R.string.prescription_refill_label).uppercase(),
                    style = MdTheme.typography.semiBold14TextStyle.copy(color = colorResource(R.color.prescriptionListRefillTxtColor))
                )
            }
        }
        if (allowToEdit) {
            IconButton(modifier = Modifier.align(Alignment.BottomEnd), onClick = onEditClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_edit),
                    contentDescription = "Edit",
                    tint = colorResource(R.color.colorPrimary)
                )
            }
        }
    }
}


@Composable
fun LabelValueText(label: String, value: String, modifier: Modifier = Modifier, valueModifier: Modifier) {
    Row(
        modifier = modifier
    ) {
        MDTextView(
            value = label,
            style = MdTheme.typography.regular14TextStyle.copy(color = colorResource(R.color.medicationListMoreTemplateLabelTxtColor)),
        )
        MDTextView(
            value = value,
            modifier = valueModifier,
            style = MdTheme.typography.semiBold14TextStyle.copy(color = colorResource(R.color.medicationListMoreTemplateValueTxtColor))
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun MedicationItemDetailsPreview() {
    PrescriptionItemDetails(
        true,
        direction = "1 tablet(s) 3 times a day before a meal for Everyday, oral route",
        prescribedBy = "Dr.John Smith",
        prescribedOn = "10 Jan, 1998",
        stopDate = "10 Jan, 1998",
        dispense = "1",
        refill = "1",
        allowToEdit = true,
        onRefillClick = { /* Do something */ },
        onEditClick = { /* Do something */ }, onDeleteClick = {}, itemOnClick = {}, fromReminder = false, isMultipleLayout = false
    )
}
