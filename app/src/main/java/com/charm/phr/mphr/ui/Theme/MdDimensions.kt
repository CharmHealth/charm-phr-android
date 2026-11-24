package com.charm.phr.mphr.ui.Theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class MdDimensions(
    val small: TextUnit,
    val smMedium: TextUnit,
    val medium: TextUnit,
    val medLarge: TextUnit,
    val large: TextUnit,
    val xLarge: TextUnit,
    val xxLarge: TextUnit,
    val doubleCardMargin: Dp,
    val cardMargin : Dp,
    val halfCardMargin : Dp,
    val patientProfileImageSize: Dp,
    val cardCornerRadius: Dp,
    val contentPadding: Dp,
    val horizontalPadding: Dp,
)

val LocalDimensions = staticCompositionLocalOf {
    MdDimensions(
        small = 12.sp,
        smMedium = 13.sp,
        medium = 14.sp,
        medLarge = 15.sp,
        large = 16.sp,
        xLarge = 18.sp,
        xxLarge = 20.sp,
        cardMargin=8.dp,
        halfCardMargin=4.dp,
        patientProfileImageSize=50.dp,
        doubleCardMargin=16.dp,
        cardCornerRadius=12.dp,
        contentPadding=6.dp,
        horizontalPadding=8.dp,
    )
}