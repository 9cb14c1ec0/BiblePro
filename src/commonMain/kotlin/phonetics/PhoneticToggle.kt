package phonetics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import icons.CommonIcons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A composable that provides a UI for toggling phonetics display and selecting language.
 * 
 * @param phoneticSettings The state holder for phonetics settings
 * @param modifier Modifier to be applied to the composable
 */
@Composable
fun PhoneticToggle(
    phoneticSettings: PhoneticSettings,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Phonetics toggle icon button
        IconButton(
            onClick = { expanded = true }
        ) {
            Icon(
                imageVector = CommonIcons.Language,
                contentDescription = "Phonetics Settings",
                tint = if (phoneticSettings.showPhonetics) 
                    MaterialTheme.colors.primary 
                else 
                    MaterialTheme.colors.onSurface
            )
        }

        // Dropdown menu for phonetics settings
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colors.surface)
        ) {
            // Toggle phonetics display
            DropdownMenuItem(
                onClick = {
                    phoneticSettings.togglePhonetics()
                    if (phoneticSettings.showPhonetics && phoneticSettings.language == PhoneticLanguage.NONE) {
                        // If enabling phonetics but no language is selected, default to Spanish
                        phoneticSettings.changeLanguage(PhoneticLanguage.SPANISH)
                    }
                    expanded = false
                }
            ) {
                Text(if (phoneticSettings.showPhonetics) "Hide Phonetics" else "Show Phonetics")
            }

            // Language selection
            if (phoneticSettings.showPhonetics) {
                DropdownMenuItem(
                    onClick = {
                        phoneticSettings.changeLanguage(PhoneticLanguage.SPANISH)
                        expanded = false
                    }
                ) {
                    Icon(
                        imageVector = CommonIcons.Language,
                        contentDescription = "Spanish Phonetics"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Spanish Phonetics")
                }

                // Add more languages here in the future
            }
        }
    }
}
