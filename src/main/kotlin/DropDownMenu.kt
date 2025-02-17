import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
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
            Icon(Icons.Filled.Language, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                content = { Text("English") },
                onClick = { L.current.language = "english" }
            )
            DropdownMenuItem(
                content = { Text("Español") },
                onClick = { L.current.language = "spanish" }
            )
            DropdownMenuItem(
                content = { Text("Français") },
                onClick = { L.current.language = "french" }
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
            Icon(icon, contentDescription = icon.name)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
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