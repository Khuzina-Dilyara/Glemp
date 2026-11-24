package com.example.glamping.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Тёмно-зелёная шапка + белый «лист», зелёный акцент, цветные круглые иконки
val Navy = Color(0xFF1B5E3A)
val NavyDark = Color(0xFF0F3D26)
val Cyan = Color(0xFF26A05A)
val CyanDark = Color(0xFF1A7F45)
val Ink = Color(0xFF14332A)
val Muted = Color(0xFF74857C)
val Line = Color(0xFFD3E3D9)
val Soft = Color(0xFFF1F7F3)
val Orange = Color(0xFFFFA726)
val Amber = Color(0xFFF5A524)
val Coral = Color(0xFFFF5A4D)
val Pink = Color(0xFFF06292)
val Green = Color(0xFF2E9E6F)
val Purple = Color(0xFF7C5CFF)
val Teal = Color(0xFF18A999)

val NavyGradient = Brush.verticalGradient(listOf(Color(0xFF237A4B), Color(0xFF0F3D26)))
val CyanGradient = Brush.horizontalGradient(listOf(Cyan, CyanDark))

private val Scheme = lightColorScheme(
    primary = Navy, onPrimary = Color.White, secondary = Cyan, onSecondary = Color.White,
    background = Color.White, onBackground = Ink, surface = Color.White, onSurface = Ink,
    surfaceVariant = Soft, onSurfaceVariant = Muted, outline = Line, error = Coral,
    primaryContainer = Soft, onPrimaryContainer = Navy,
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color.White, surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White, surfaceContainerHighest = Soft
)

@Composable
fun GlampingTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, shapes = Shapes(medium = RoundedCornerShape(12.dp), large = RoundedCornerShape(16.dp))) {
        CompositionLocalProvider(LocalContentColor provides Ink, content = content)
    }
}
