package com.charm.phr.mphr.ui.components.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charm.phr.mphr.R

@Composable
fun MdRatingScale(
    modifier: Modifier = Modifier,
    rating: Int,
    spaceBetween: Dp = 0.dp,
    starSize: Dp = 24.dp,
    onRatingChange: (Int) -> Unit,
    enable: Boolean = true
) {
    var currentRating by remember { mutableIntStateOf(rating) }
    var selectedRating by remember { mutableIntStateOf(-1) }

    val image = ImageBitmap.imageResource(id = R.drawable.ic_outline_star)
    val imageFull = ImageBitmap.imageResource(id = R.drawable.ic_filled_star)

    val totalCount = 5

    val starSizePx = with(LocalDensity.current) { starSize.toPx() }
    val spacePx = with(LocalDensity.current) { spaceBetween.toPx() }
    val totalWidth = starSizePx * totalCount + spacePx * (totalCount - 1)
    var boxModifier = modifier
        .width(with(LocalDensity.current) { totalWidth.toDp() })
        .height(starSize).drawBehind {
            drawRating(currentRating, selectedRating, image, imageFull, spacePx, starSizePx)
        }
    if (enable) {
        boxModifier = boxModifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = { offset ->
                    val newRating = ((offset.x / (starSizePx + spacePx)).toInt() + 1).coerceIn(1, totalCount)
                    currentRating = newRating
                    onRatingChange(currentRating)
                })
            }
            .pointerInput(Unit) {
                detectDragGestures(onDragStart = { offset ->
                    val newRating = ((offset.x / (starSizePx + spacePx)).toInt() + 1).coerceIn(1, totalCount)
                    selectedRating = newRating
                    currentRating = newRating
                    onRatingChange(currentRating)
                }, onDrag = { change, _ ->
                    val newRating = ((change.position.x / (starSizePx + spacePx)).toInt() + 1).coerceIn(1, totalCount)
                    selectedRating = newRating
                    currentRating = newRating
                    onRatingChange(currentRating)
                }, onDragEnd = {
                    selectedRating = -1
                }, onDragCancel = {
                    selectedRating = -1
                })
            }
    }

    Box(
        boxModifier
    )
}

private fun DrawScope.drawRating(
    rating: Int,
    selectedRating: Int,
    image: ImageBitmap,
    imageFull: ImageBitmap,
    space: Float,
    starSize: Float,
) {
    val totalCount = 5
    val selectedScale = 1.2f // Scale factor for the selected icon

    for (i in 0 until totalCount) {
        val start = starSize * i + space * i
        val isSelected = i + 1 == selectedRating

        drawIntoCanvas { canvas ->
            val paint = Paint()
            val scale = if (isSelected) selectedScale else 1f
            canvas.save()
            canvas.translate(start, 0f)
            canvas.scale(scale, scale, starSize / 2, starSize / 2)
            val srcRect = android.graphics.Rect(0, 0, image.width, image.height)
            val dstRect = android.graphics.Rect(0, 0, starSize.toInt(), starSize.toInt())
            canvas.nativeCanvas.drawBitmap(if (i < rating) imageFull.asAndroidBitmap() else image.asAndroidBitmap(), srcRect, dstRect, paint.asFrameworkPaint())
            canvas.restore()
        }
    }
}

@Composable
@Preview(device = "spec:width=700dp,height=1200dp,dpi=420,isRound=false,chinSize=0dp,orientation=portrait")
fun MdRatingBarPreview() {
    var rating by remember { mutableIntStateOf(3) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
        MdRatingScale(rating = rating, spaceBetween = 8.dp, starSize = 48.dp, onRatingChange = {rating=it}, enable = false)
    }
}