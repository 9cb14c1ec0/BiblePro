package theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Modern Material 3 Light theme colors
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F5BD5),           // Modern indigo
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDFE0FF),
    onPrimaryContainer = Color(0xFF000B62),
    secondary = Color(0xFFE8590C),          // Warm orange
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF341100),
    tertiary = Color(0xFF7B5263),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E5),
    onTertiaryContainer = Color(0xFF2F1120),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE3E1EC),
    onSurfaceVariant = Color(0xFF46464F),
    outline = Color(0xFF777680),
    outlineVariant = Color(0xFFC7C5D0),
    inverseSurface = Color(0xFF303034),
    inverseOnSurface = Color(0xFFF3F0F4),
    inversePrimary = Color(0xFFBCC2FF),
    surfaceTint = Color(0xFF4F5BD5)
)

// Modern Material 3 Dark theme colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBCC2FF),            // Light indigo
    onPrimary = Color(0xFF1E2578),
    primaryContainer = Color(0xFF3642BC),
    onPrimaryContainer = Color(0xFFDFE0FF),
    secondary = Color(0xFFFFB599),           // Light orange
    onSecondary = Color(0xFF552000),
    secondaryContainer = Color(0xFFB04500),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = Color(0xFFEFB8CA),
    onTertiary = Color(0xFF482535),
    tertiaryContainer = Color(0xFF613A4B),
    onTertiaryContainer = Color(0xFFFFD8E5),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1B1B1F),
    onBackground = Color(0xFFE5E1E6),
    surface = Color(0xFF1B1B1F),
    onSurface = Color(0xFFE5E1E6),
    surfaceVariant = Color(0xFF46464F),
    onSurfaceVariant = Color(0xFFC7C5D0),
    outline = Color(0xFF91909A),
    outlineVariant = Color(0xFF46464F),
    inverseSurface = Color(0xFFE5E1E6),
    inverseOnSurface = Color(0xFF303034),
    inversePrimary = Color(0xFF4F5BD5),
    surfaceTint = Color(0xFFBCC2FF)
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

    // Select the appropriate color scheme
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    // Provide the theme
    CompositionLocalProvider(LocalThemeMode provides themeMode) {
        MaterialTheme(
            colorScheme = colorScheme,
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