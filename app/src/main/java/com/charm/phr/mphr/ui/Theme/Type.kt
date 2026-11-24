package com.charm.phr.mphr.ui.Theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

@Immutable
data class CustomTypography(
    val regular12TextStyle: TextStyle,
    val regular14TextStyle: TextStyle,
    val regular16TextStyle: TextStyle,
    val regular18TextStyle: TextStyle,
    val regular20TextStyle: TextStyle,
    val semiBold12TextStyle: TextStyle,
    val semiBold14TextStyle: TextStyle,
    val semiBold16TextStyle: TextStyle,
    val semiBold18TextStyle: TextStyle,
    val semiBold20TextStyle: TextStyle,
    val semiBold22TextStyle: TextStyle,
    val medium12TextStyle: TextStyle,
    val medium14TextStyle: TextStyle,
    val medium16TextStyle: TextStyle,
    val medium18TextStyle: TextStyle,
    val medium20TextStyle: TextStyle,

)

val LocalCustomTypography = staticCompositionLocalOf {
    CustomTypography(
        regular12TextStyle = TextStyle.Default,
        regular14TextStyle = TextStyle.Default,
        regular16TextStyle = TextStyle.Default,
        regular18TextStyle = TextStyle.Default,
        regular20TextStyle = TextStyle.Default,
        semiBold12TextStyle = TextStyle.Default,
        semiBold14TextStyle = TextStyle.Default,
        semiBold16TextStyle = TextStyle.Default,
        semiBold18TextStyle = TextStyle.Default,
        semiBold20TextStyle = TextStyle.Default,
        semiBold22TextStyle = TextStyle.Default,
        medium12TextStyle = TextStyle.Default,
        medium14TextStyle = TextStyle.Default,
        medium16TextStyle = TextStyle.Default,
        medium18TextStyle = TextStyle.Default,
        medium20TextStyle = TextStyle.Default,
    )
}
