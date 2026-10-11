package com.snown.watchinstaller.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// 강조색은 파랑 하나, 나머지는 회색 단계. 기기 배경화면 색(dynamic color)은 따르지 않는다.
private val Blue = Color(0xFF2563EB)
private val BlueDark = Color(0xFF8AB4FF)

private val LightColors = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EBFD),
    onPrimaryContainer = Color(0xFF0F2C6E),
    secondary = Color(0xFF5B6270),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3EBFD),
    onSecondaryContainer = Color(0xFF0F2C6E),
    tertiary = Blue,
    background = Color(0xFFF3F4F6),
    onBackground = Color(0xFF1B1E23),
    surface = Color.White,
    onSurface = Color(0xFF1B1E23),
    surfaceVariant = Color(0xFFE8EAED),
    onSurfaceVariant = Color(0xFF5B6270),
    surfaceTint = Blue,
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE2E4E8),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F9FA),
    surfaceContainer = Color(0xFFF3F4F6),
    surfaceContainerHigh = Color(0xFFEDEEF1),
    surfaceContainerHighest = Color(0xFFE8EAED),
    outline = Color(0xFFB8BEC7),
    outlineVariant = Color(0xFFE1E4E8),
    error = Color(0xFFC62828),
    onError = Color.White,
    errorContainer = Color(0xFFFDECEC),
    onErrorContainer = Color(0xFF5F1111),
)

private val DarkColors = darkColorScheme(
    primary = BlueDark,
    onPrimary = Color(0xFF0B1D40),
    primaryContainer = Color(0xFF1E2F52),
    onPrimaryContainer = Color(0xFFD8E4FF),
    secondary = Color(0xFFA3AAB5),
    onSecondary = Color(0xFF15181C),
    secondaryContainer = Color(0xFF1E2F52),
    onSecondaryContainer = Color(0xFFD8E4FF),
    tertiary = BlueDark,
    background = Color(0xFF111315),
    onBackground = Color(0xFFE4E6EA),
    surface = Color(0xFF1B1E22),
    onSurface = Color(0xFFE4E6EA),
    surfaceVariant = Color(0xFF2A2E34),
    onSurfaceVariant = Color(0xFFA3AAB5),
    surfaceTint = BlueDark,
    surfaceBright = Color(0xFF2A2E34),
    surfaceDim = Color(0xFF111315),
    surfaceContainerLowest = Color(0xFF0C0E10),
    surfaceContainerLow = Color(0xFF16191C),
    surfaceContainer = Color(0xFF1B1E22),
    surfaceContainerHigh = Color(0xFF23272C),
    surfaceContainerHighest = Color(0xFF2A2E34),
    outline = Color(0xFF59606B),
    outlineVariant = Color(0xFF33383F),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF3F0A0A),
    errorContainer = Color(0xFF3A1E1E),
    onErrorContainer = Color(0xFFFFD9D6),
)

/** 성공 배너용 초록 — Material 색 체계에 없는 칸이라 따로 둔다. */
@Immutable
data class SuccessColors(val content: Color, val container: Color, val onContainer: Color)

private val LightSuccess = SuccessColors(Color(0xFF2E7D32), Color(0xFFE8F5E9), Color(0xFF173B19))
private val DarkSuccess = SuccessColors(Color(0xFF81C784), Color(0xFF1C3320), Color(0xFFD5EED6))

val LocalSuccessColors = staticCompositionLocalOf { LightSuccess }

@Composable
fun InstallerTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSuccessColors provides if (dark) DarkSuccess else LightSuccess) {
        MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
    }
}
