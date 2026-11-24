package com.charm.phr.mphr.ui.components.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R
import com.charm.phr.mphr.ui.Theme.MdTheme

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MDChip(
    modifier: Modifier = Modifier,
    chipText: String,
    onRemoveHandler: () -> Unit,
    showRemoveIcon: Boolean = true
) {
    Surface(
        modifier = modifier
            .clickable { onRemoveHandler() },
        color = colorResource(R.color.card_bg),
        shape = RoundedCornerShape(8.dp),
        contentColor = colorResource(R.color.Black)
    ) {
        BoxWithConstraints(modifier = Modifier.padding(end = MdTheme.dimension.horizontalPadding)) {
            val maxTextWidth = maxWidth - 24.dp // Reserve space for icon + spacing (16 + 8)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .wrapContentWidth()
                    .height(32.dp)
            ) {
                Text(
                    text = chipText,
                    style = MdTheme.typography.medium14TextStyle.copy(colorResource(R.color.messageComposeItemValueTxtColor)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .widthIn(max = maxTextWidth)
                        .padding(start = MdTheme.dimension.horizontalPadding)
                )

                Spacer(modifier = Modifier.width(8.dp))
                if (showRemoveIcon) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close_24),
                        contentDescription = "Close",
                        modifier = Modifier.size(16.dp),
                        tint =colorResource(R.color.colorPrimary)
                    )
                }

            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewMDChip() {
    MDChip(
        chipText = "Preview Chip wefhewbhf ewfwehf ewhfwhbefhb hbhhhbhb hbbhb",
        onRemoveHandler = {},
        showRemoveIcon = true
    )
}
