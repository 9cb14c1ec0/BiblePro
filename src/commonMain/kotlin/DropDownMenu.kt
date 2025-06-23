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
            DropdownMenuItem(
                content = { Text("Bahasa Indonesia") },
                onClick = { L.current.language = "indonesian" }
            )
            DropdownMenuItem(
                content = { Text("Kiswahili") },
                onClick = { L.current.language = "swahili" }
            )
            DropdownMenuItem(
                content = { Text("Nederlands") },
                onClick = { L.current.language = "dutch" }
            )
            DropdownMenuItem(
                content = { Text("Polski") },
                onClick = { L.current.language = "polish" }
            )
            DropdownMenuItem(
                content = { Text("Română") },
                onClick = { L.current.language = "romanian" }
            )
            DropdownMenuItem(
                content = { Text("Ελληνικά") },
                onClick = { L.current.language = "greek" }
            )
            DropdownMenuItem(
                content = { Text("עברית") },
                onClick = { L.current.language = "hebrew" }
            )
            DropdownMenuItem(
                content = { Text("Tagalog") },
                onClick = { L.current.language = "tagalog" }
            )
            DropdownMenuItem(
                content = { Text("አማርኛ") },
                onClick = { L.current.language = "amharic" }
            )
            DropdownMenuItem(
                content = { Text("ไทย") },
                onClick = { L.current.language = "thai" }
            )
            DropdownMenuItem(
                content = { Text("Cebuano") },
                onClick = { L.current.language = "cebuano" }
            )
            DropdownMenuItem(
                content = { Text("Hausa") },
                onClick = { L.current.language = "hausa" }
            )
            DropdownMenuItem(
                content = { Text("Yorùbá") },
                onClick = { L.current.language = "yoruba" }
            )
            DropdownMenuItem(
                content = { Text("Norsk") },
                onClick = { L.current.language = "norwegian" }
            )
            DropdownMenuItem(
                content = { Text("Svenska") },
                onClick = { L.current.language = "swedish" }
            )
            DropdownMenuItem(
                content = { Text("Dansk") },
                onClick = { L.current.language = "danish" }
            )
            DropdownMenuItem(
                content = { Text("Čeština") },
                onClick = { L.current.language = "czech" }
            )
            DropdownMenuItem(
                content = { Text("Magyar") },
                onClick = { L.current.language = "hungarian" }
            )
            DropdownMenuItem(
                content = { Text("Hrvatski") },
                onClick = { L.current.language = "croatian" }
            )
            DropdownMenuItem(
                content = { Text("Српски") },
                onClick = { L.current.language = "serbian" }
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
