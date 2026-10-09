package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BalajiColorScheme = darkColorScheme(
    primary = BalajiTealPrimary,
    onPrimary = BalajiNavyDark,
    primaryContainer = BalajiNavyElevated,
    onPrimaryContainer = BalajiTealSecondary,
    secondary = BalajiOrangeWarm,
    onSecondary = BalajiNavyDark,
    secondaryContainer = BalajiNavyElevated,
    onSecondaryContainer = BalajiOrangeFlame,
    tertiary = BalajiGuaranteeGreen,
    onTertiary = BalajiNavyDark,
    background = BalajiNavyDark,
    onBackground = BalajiTextLight,
    surface = BalajiNavySurface,
    onSurface = BalajiTextLight,
    surfaceVariant = BalajiNavyElevated,
    onSurfaceVariant = BalajiTextSubtle,
    error = BalajiEmergencyRed,
    onError = BalajiCardWhite,
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BalajiColorScheme,
        typography = Typography,
        content = content
    )
}
