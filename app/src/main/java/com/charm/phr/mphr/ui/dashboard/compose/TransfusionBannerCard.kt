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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MPHRTheme
import com.charm.phr.mphr.ui.Theme.MdColors
import com.charm.phr.mphr.ui.Theme.MdTheme

sealed class TransfusionCountdown {
    data class DaysToGo(val days: Int) : TransfusionCountdown()
    object Tomorrow : TransfusionCountdown()
    object Today : TransfusionCountdown()
    data class DaysAgo(val days: Int) : TransfusionCountdown()
}

fun transfusionCountdown(daysRemaining: Int): TransfusionCountdown = when {
    daysRemaining > 1 -> TransfusionCountdown.DaysToGo(daysRemaining)
    daysRemaining == 1 -> TransfusionCountdown.Tomorrow
    daysRemaining == 0 -> TransfusionCountdown.Today
    else -> TransfusionCountdown.DaysAgo(-daysRemaining)
}

@Composable
fun TransfusionBannerCard(
    modifier: Modifier = Modifier,
    dueDateText: String,
    countdown: TransfusionCountdown
) {
    val isUrgent = countdown is TransfusionCountdown.DaysAgo || countdown is TransfusionCountdown.Today

    val chipBg = if (isUrgent) MdColors.Red50 else MdColors.Blue50
    val chipColor = if (isUrgent) MdColors.Red600 else MdColors.primaryColor

    val countdownText = when (countdown) {
        is TransfusionCountdown.DaysToGo -> pluralStringResource(R.plurals.number_days_to_go, countdown.days, countdown.days)
        is TransfusionCountdown.Tomorrow -> stringResource(R.string.due_tomorrow)
        is TransfusionCountdown.Today -> stringResource(R.string.due_today)
        is TransfusionCountdown.DaysAgo -> pluralStringResource(R.plurals.number_days_ago, countdown.days, countdown.days)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = stringResource(R.string.next_transfusion_due),
                style = MdTheme.typography.medium14TextStyle.copy(color = MdColors.Black87)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dueDateText,
                style = MdTheme.typography.semiBold16TextStyle.copy(color = MdColors.Black87)
            )
        }

        Text(
            text = countdownText,
            style = MdTheme.typography.medium14TextStyle.copy(color = chipColor),
            modifier = Modifier
                .background(color = chipBg, shape = RoundedCornerShape(20.dp))
                .border(width = 1.dp, color = chipColor, shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F6F8)
@Composable
private fun TransfusionBannerNormalPreview() {
    MPHRTheme {
        TransfusionBannerCard(
            modifier = Modifier.padding(12.dp),
            dueDateText = "Apr 10, 2026",
            countdown = transfusionCountdown(23)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F6F8)
@Composable
private fun TransfusionBannerTomorrowPreview() {
    MPHRTheme {
        TransfusionBannerCard(
            modifier = Modifier.padding(12.dp),
            dueDateText = "Mar 25, 2026",
            countdown = transfusionCountdown(1)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F6F8)
@Composable
private fun TransfusionBannerTodayPreview() {
    MPHRTheme {
        TransfusionBannerCard(
            modifier = Modifier.padding(12.dp),
            dueDateText = "Mar 24, 2026",
            countdown = transfusionCountdown(0)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F6F8)
@Composable
private fun TransfusionBannerOverduePreview() {
    MPHRTheme {
        TransfusionBannerCard(
            modifier = Modifier.padding(12.dp),
            dueDateText = "Mar 22, 2026",
            countdown = transfusionCountdown(-2)
        )
    }
}
