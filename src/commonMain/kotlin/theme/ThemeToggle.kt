package theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import icons.CommonIcons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable that provides a UI for toggling between light and dark themes.
 *
 * @param themeState The state holder for the current theme mode
 * @param modifier Modifier to be applied to the composable
 */
@Composable
fun ThemeToggle(
    themeState: ThemeState,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Theme toggle icon button
        IconButton(
            onClick = { expanded = true }
        ) {
            // Show different icon based on current theme
            when (themeState.themeMode) {
                ThemeMode.LIGHT -> Icon(
                    imageVector = CommonIcons.LightMode,
                    contentDescription = "Light Mode",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                ThemeMode.DARK -> Icon(
                    imageVector = CommonIcons.DarkMode,
                    contentDescription = "Dark Mode",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                ThemeMode.SYSTEM -> Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "System Theme",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Dropdown menu for theme selection
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = CommonIcons.LightMode,
                            contentDescription = "Light Mode"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Light Mode")
                    }
                },
                onClick = {
                    themeState.themeMode = ThemeMode.LIGHT
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = CommonIcons.DarkMode,
                            contentDescription = "Dark Mode"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dark Mode")
                    }
                },
                onClick = {
                    themeState.themeMode = ThemeMode.DARK
                    expanded = false
                }
            )

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "System Theme"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("System Default")
                    }
                },
                onClick = {
                    themeState.themeMode = ThemeMode.SYSTEM
                    expanded = false
                }
            )
        }
    }
}