import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import icons.CommonIcons
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import locale.L
import locale.LanguagePreferences

private fun selectLanguage(languageCode: String) {
    L.current.language = languageCode
    LanguagePreferences.setPreferredLanguage(languageCode)
}

@Composable
fun MinimalDropdownMenu() {
    var expanded by remember { mutableStateOf(false) }
    val availableLanguages = LanguagePreferences.getAvailableLanguages()
    
    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(CommonIcons.Language, contentDescription = "More options", tint = MaterialTheme.colors.onSurface)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colors.surface)
        ) {
            availableLanguages.forEach { (languageCode, displayName) ->
                DropdownMenuItem(
                    content = { Text(displayName) },
                    onClick = { 
                        selectLanguage(languageCode)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MyDropdownMenu(options: List<ComboOption>, icon: ImageVector, OnSelectionChange: (item: ComboOption) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(icon, contentDescription = icon.name, tint = MaterialTheme.colors.onSurface)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colors.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    content = { Text(option.text) },
                    onClick = {
                        OnSelectionChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
