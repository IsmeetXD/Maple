package com.ismeetxd.maple.ui.theme

import android.os.Build
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDirection
import com.google.android.material.color.MaterialColors
import com.ismeetxd.maple.ui.common.LocalDynamicColorSwitch
import com.ismeetxd.maple.ui.common.LocalFixedColorRoles
import com.kyant.monet.LocalTonalPalettes
import com.kyant.monet.dynamicColorScheme

fun Color.applyOpacity(enabled: Boolean): Color {
    return if (enabled) this else this.copy(alpha = 0.62f)
}

@Composable
@ReadOnlyComposable
fun Color.harmonizeWith(other: Color) =
    Color(MaterialColors.harmonize(this.toArgb(), other.toArgb()))

@Composable
@ReadOnlyComposable
fun Color.harmonizeWithPrimary(): Color =
    this.harmonizeWith(other = MaterialTheme.colorScheme.primary)

@Composable
fun MapleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isHighContrastModeEnabled: Boolean = false,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current

    LaunchedEffect(darkTheme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (darkTheme) {
                view.windowInsetsController?.setSystemBarsAppearance(
                    0,
                    APPEARANCE_LIGHT_STATUS_BARS,
                )
            } else {
                view.windowInsetsController?.setSystemBarsAppearance(
                    APPEARANCE_LIGHT_STATUS_BARS,
                    APPEARANCE_LIGHT_STATUS_BARS,
                )
            }
        }
    }

    val isDynamicColor = LocalDynamicColorSwitch.current

    val colorScheme =
        dynamicColorScheme(!darkTheme).run {
            if (isHighContrastModeEnabled && darkTheme) {
                copy(
                    surface = Color.Black,
                    background = Color.Black,
                    surfaceContainerLowest = Color.Black,
                    surfaceContainerLow = surfaceContainerLowest,
                    surfaceContainer = surfaceContainerLow,
                    surfaceContainerHigh = surfaceContainerLow,
                    surfaceContainerHighest = surfaceContainer,
                )
            } else if (!isDynamicColor && darkTheme) {
                // Gruvbox Autumn Dark Theme
                copy(
                    background = Color(0xFF282828),
                    surface = Color(0xFF282828),
                    surfaceContainerLowest = Color(0xFF1D2021),
                    surfaceContainerLow = Color(0xFF282828),
                    surfaceContainer = Color(0xFF32302F),
                    surfaceContainerHigh = Color(0xFF3C3836),
                    surfaceContainerHighest = Color(0xFF504945),
                    onBackground = Color(0xFFEBDBB2),
                    onSurface = Color(0xFFEBDBB2),
                    onSurfaceVariant = Color(0xFFA89984),
                    primary = Color(0xFFD79921),
                    onPrimary = Color(0xFF282828),
                    primaryContainer = Color(0xFF504945),
                    onPrimaryContainer = Color(0xFFFABD2F),
                    secondary = Color(0xFF98971A),
                    onSecondary = Color(0xFF282828),
                    secondaryContainer = Color(0xFF3C3836),
                    onSecondaryContainer = Color(0xFFB8BB26),
                    tertiary = Color(0xFFFE8019),
                    onTertiary = Color(0xFF282828),
                    tertiaryContainer = Color(0xFF42281D),
                    onTertiaryContainer = Color(0xFFFFAA66),
                    outline = Color(0xFF7C6F64),
                    outlineVariant = Color(0xFF504945),
                )
            } else if (!isDynamicColor && !darkTheme) {
                // Gruvbox Autumn Light Theme
                copy(
                    background = Color(0xFFFBF1C7),
                    surface = Color(0xFFFBF1C7),
                    surfaceContainerLowest = Color(0xFFFFFFFF),
                    surfaceContainerLow = Color(0xFFF4E8BA),
                    surfaceContainer = Color(0xFFF2E5BC),
                    surfaceContainerHigh = Color(0xFFEBDBB2),
                    surfaceContainerHighest = Color(0xFFD5C4A1),
                    onBackground = Color(0xFF3C3836),
                    onSurface = Color(0xFF3C3836),
                    onSurfaceVariant = Color(0xFF665C54),
                    primary = Color(0xFFB57614),
                    onPrimary = Color(0xFFFFFFFF),
                    primaryContainer = Color(0xFFF9E4B7),
                    onPrimaryContainer = Color(0xFF3D2600),
                    secondary = Color(0xFF79740E),
                    onSecondary = Color(0xFFFFFFFF),
                    secondaryContainer = Color(0xFFE3E8A6),
                    onSecondaryContainer = Color(0xFF222400),
                    tertiary = Color(0xFFAF3A03),
                    onTertiary = Color(0xFFFFFFFF),
                    tertiaryContainer = Color(0xFFFFDBCF),
                    onTertiaryContainer = Color(0xFF3B0B00),
                    outline = Color(0xFF7C6F64),
                    outlineVariant = Color(0xFFBDAE93),
                )
            } else this
        }

    val textStyle =
        LocalTextStyle.current.copy(
            lineBreak = LineBreak.Paragraph,
            textDirection = TextDirection.Content,
        )

    val tonalPalettes = LocalTonalPalettes.current

    CompositionLocalProvider(
        LocalFixedColorRoles provides FixedColorRoles.fromTonalPalettes(tonalPalettes),
        LocalTextStyle provides textStyle,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}


@Composable
@Deprecated("Use MapleTheme instead", replaceWith = ReplaceWith("MapleTheme(content)"))
fun PreviewThemeLight(content: @Composable () -> Unit) {
    MapleTheme(darkTheme = false, content = content)
}
