package com.charm.phr.mphr.ui.dashboard.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MdColors
import com.charm.phr.mphr.ui.Theme.MdTheme
import com.charm.phr.mphr.ui.Theme.MPHRTheme

enum class MriCardiacAlert { NONE, WARNING, CRITICAL }

@Composable
fun LastMriResultCard(
    modifier: Modifier = Modifier,
    hasData: Boolean = true,
    dateText: String = "",
    licValue: String = "",
    licUnit: String = "",
    cardiacT2Value: String = "",
    cardiacT2Unit: String = "",
    cardiacAlert: MriCardiacAlert = MriCardiacAlert.NONE
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.last_mri_result),
                style = MdTheme.typography.semiBold16TextStyle.copy(color = MdColors.Black87)
            )
            if (!hasData) {
                Text(
                    text = stringResource(R.string.no_result),
                    style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Black87)
                )
            }
        }

        if (hasData) {
            Spacer(modifier = Modifier.height(10.dp))
            MriInfoRow(
                label = stringResource(R.string.date_str),
                value = dateText,
                unit = ""
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = MdColors.Black10
            )

            MriInfoRow(
                label = stringResource(R.string.mri_lic_label),
                value = licValue,
                unit = licUnit
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = MdColors.Black10
            )
            MriCardiacRow(
                label = stringResource(R.string.mri_cardiac_t2_label),
                value = cardiacT2Value,
                unit = cardiacT2Unit,
                alert = cardiacAlert
            )
        }
    }
}

@Composable
private fun MriInfoRow(
    label: String,
    value: String,
    unit: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Black60)
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MdTheme.typography.semiBold14TextStyle.copy(color = MdColors.Black87)
            )
            if (unit.isNotBlank()) {
                Text(
                    text = " $unit",
                    style = MdTheme.typography.regular12TextStyle.copy(color = MdColors.Black60)
                )
            }
        }
    }
}

@Composable
private fun MriCardiacRow(
    label: String,
    value: String,
    unit: String,
    alert: MriCardiacAlert
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MdTheme.typography.regular14TextStyle.copy(color = MdColors.Black60)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MdTheme.typography.semiBold14TextStyle.copy(color = MdColors.Black87)
                )
                if (unit.isNotBlank()) {
                    Text(
                        text = " $unit",
                        style = MdTheme.typography.regular12TextStyle.copy(color = MdColors.Black60)
                    )
                }
            }

            if (alert != MriCardiacAlert.NONE) {
                val chipBg = if (alert == MriCardiacAlert.CRITICAL) MdColors.Red50 else MdColors.Amber50
                val chipBorder = if (alert == MriCardiacAlert.CRITICAL) MdColors.Red600 else MdColors.Amber800
                val chipText = if (alert == MriCardiacAlert.CRITICAL)
                    stringResource(R.string.critical_str)
                else
                    stringResource(R.string.warning)

                Text(
                    text = chipText,
                    style = MdTheme.typography.medium12TextStyle.copy(color = chipBorder),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .background(color = chipBg, shape = RoundedCornerShape(20.dp))
                        .border(width = 1.dp, color = chipBorder, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LastMriResultCardPreview() {
    MPHRTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            LastMriResultCard(hasData = false)
            Spacer(modifier = Modifier.height(16.dp))
            LastMriResultCard(
                dateText = "Mar 01, 2026",
                licValue = "2.5",
                licUnit = "mg/g dw",
                cardiacT2Value = "15",
                cardiacT2Unit = "ms",
                cardiacAlert = MriCardiacAlert.WARNING
            )
            Spacer(modifier = Modifier.height(16.dp))
            LastMriResultCard(
                dateText = "Mar 01, 2026",
                licValue = "3.2",
                licUnit = "mg/g dw",
                cardiacT2Value = "8",
                cardiacT2Unit = "ms",
                cardiacAlert = MriCardiacAlert.CRITICAL
            )
            Spacer(modifier = Modifier.height(16.dp))
            LastMriResultCard(
                dateText = "Mar 01, 2026",
                licValue = "1.8",
                licUnit = "mg/g dw",
                cardiacT2Value = "25",
                cardiacT2Unit = "ms",
                cardiacAlert = MriCardiacAlert.NONE
            )
        }
    }
}

