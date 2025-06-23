package theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light theme colors
private val LightColorPalette = lightColors(
    primary = Color(0xFF3F51B5),         // Indigo
    primaryVariant = Color(0xFF303F9F),  // Dark Indigo
    secondary = Color(0xFFFF9800),       // Orange
    secondaryVariant = Color(0xFFF57C00),// Dark Orange
    background = Color(0xFFF5F5F5),      // Light Gray
    surface = Color(0xFFFFFFFF),         // White
    error = Color(0xFFB00020),           // Red
    onPrimary = Color(0xFFFFFFFF),       // White
    onSecondary = Color(0xFF000000),     // Black
    onBackground = Color(0xFF000000),    // Black
    onSurface = Color(0xFF000000),       // Black
    onError = Color(0xFFFFFFFF)          // White
)

// Dark theme colors
private val DarkColorPalette = darkColors(
    primary = Color(0xFF5C6BC0),         // Light Indigo
    primaryVariant = Color(0xFF3F51B5),  // Indigo
    secondary = Color(0xFFFFB74D),       // Light Orange
    secondaryVariant = Color(0xFFFF9800),// Orange
    background = Color(0xFF121212),      // Dark Gray
    surface = Color(0xFF1E1E1E),         // Slightly lighter Dark Gray
    error = Color(0xFFCF6679),           // Light Red
    onPrimary = Color(0xFFFFFFFF),       // White
    onSecondary = Color(0xFF000000),     // Black
    onBackground = Color(0xFFFFFFFF),    // White
    onSurface = Color(0xFFFFFFFF),       // White
    onError = Color(0xFF000000)          // Black
)

// Theme preference enum
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

// Composition local to hold the current theme mode
val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }

// Composable function to provide the BiblePro theme
@Composable
fun BibleProTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    // Determine if dark mode should be used
    val isDarkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Select the appropriate color palette
    val colors = if (isDarkTheme) DarkColorPalette else LightColorPalette

    // Provide the theme
    CompositionLocalProvider(LocalThemeMode provides themeMode) {
        MaterialTheme(
            colors = colors,
            content = content
        )
    }
}

// Saver for ThemeState to handle configuration changes
private val ThemeStateSaver = listSaver<ThemeState, Any>(
    save = { listOf(it.themeMode.name) },
    restore = { 
        ThemeState(ThemeMode.valueOf(it[0] as String))
    }
)

// Composable function to toggle between light and dark themes
@Composable
fun rememberThemeState(initialThemeMode: ThemeMode = ThemeMode.SYSTEM): ThemeState {
    return rememberSaveable(saver = ThemeStateSaver) { ThemeState(initialThemeMode) }
}

// State holder for theme mode
class ThemeState(initialThemeMode: ThemeMode) {
    var themeMode by mutableStateOf(initialThemeMode)
        internal set

    fun toggleTheme() {
        themeMode = when (themeMode) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            // For system theme, just switch to explicit light/dark mode
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
    }
}
