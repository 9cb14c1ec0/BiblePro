import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import icons.CommonIcons
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuBox(
    label: String,
    initial_text: String,
    suggestions: List<String>,
    onSelection: (String) -> Unit,
    filterOptions: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(initial_text) }

    var textfieldSize by remember { mutableStateOf(Size.Zero)}

    val icon = if (expanded)
        CommonIcons.ArrowDropUp //it requires androidx.compose.material:material-icons-extended
    else
        Icons.Filled.ArrowDropDown


    Box() {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {
                selectedText = it
                if(suggestions.contains(it))
                {
                    onSelection(it)
                }
                else
                {
                    expanded = true
                }
                            },
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    //This value is used to assign to the DropDown the same width
                    textfieldSize = coordinates.size.toSize()
                },
            label = {Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))},
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            trailingIcon = {
                Icon(icon,"contentDescription",
                    Modifier.clickable { expanded = !expanded },
                    tint = MaterialTheme.colorScheme.onSurface)
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current){textfieldSize.width.toDp()})
                .background(MaterialTheme.colorScheme.surface)
        ) {
            suggestions.filter { !filterOptions or it.startsWith(selectedText) }.forEachIndexed { index, s ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        thickness = 0.5.dp
                    )
                }

                DropdownMenuItem(
                    text = {
                        Text(
                            text = s,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    },
                    onClick = {
                        selectedText = s
                        expanded = false
                        onSelection(s)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}