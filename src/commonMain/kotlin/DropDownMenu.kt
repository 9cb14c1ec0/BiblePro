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

@Composable
fun MinimalDropdownMenu() {
    var expanded by remember { mutableStateOf(false) }
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
            DropdownMenuItem(
                content = { Text("العربية") },
                onClick = { L.current.language = "arabic" }
            )
            DropdownMenuItem(
                content = { Text("বাংলা") },
                onClick = { L.current.language = "bengali" }
            )
            DropdownMenuItem(
                content = { Text("中文") },
                onClick = { L.current.language = "chinese" }
            )
            DropdownMenuItem(
                content = { Text("English") },
                onClick = { L.current.language = "english" }
            )
            DropdownMenuItem(
                content = { Text("Français") },
                onClick = { L.current.language = "french" }
            )
            DropdownMenuItem(
                content = { Text("Deutsch") },
                onClick = { L.current.language = "german" }
            )
            DropdownMenuItem(
                content = { Text("हिन्दी") },
                onClick = { L.current.language = "hindi" }
            )
            DropdownMenuItem(
                content = { Text("Italiano") },
                onClick = { L.current.language = "italian" }
            )
            DropdownMenuItem(
                content = { Text("日本語") },
                onClick = { L.current.language = "japanese" }
            )
            DropdownMenuItem(
                content = { Text("Basa Jawa") },
                onClick = { L.current.language = "javanese" }
            )
            DropdownMenuItem(
                content = { Text("한국어") },
                onClick = { L.current.language = "korean" }
            )
            DropdownMenuItem(
                content = { Text("मराठी") },
                onClick = { L.current.language = "marathi" }
            )
            DropdownMenuItem(
                content = { Text("Português") },
                onClick = { L.current.language = "portuguese" }
            )
            DropdownMenuItem(
                content = { Text("Русский") },
                onClick = { L.current.language = "russian" }
            )
            DropdownMenuItem(
                content = { Text("Español") },
                onClick = { L.current.language = "spanish" }
            )
            DropdownMenuItem(
                content = { Text("தமிழ்") },
                onClick = { L.current.language = "tamil" }
            )
            DropdownMenuItem(
                content = { Text("తెలుగు") },
                onClick = { L.current.language = "telugu" }
            )
            DropdownMenuItem(
                content = { Text("Türkçe") },
                onClick = { L.current.language = "turkish" }
            )
            DropdownMenuItem(
                content = { Text("اردو") },
                onClick = { L.current.language = "urdu" }
            )
            DropdownMenuItem(
                content = { Text("Tiếng Việt") },
                onClick = { L.current.language = "vietnamese" }
            )
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
