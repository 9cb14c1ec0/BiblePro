import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize


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
        Icons.Filled.ArrowDropUp //it requires androidx.compose.material:material-icons-extended
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
            label = {Text(label)},
            trailingIcon = {
                Icon(icon,"contentDescription",
                    Modifier.clickable { expanded = !expanded })
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current){textfieldSize.width.toDp()})
        ) {
            suggestions.filter { !filterOptions or it.startsWith(selectedText) }.forEach { s ->
                DropdownMenuItem(onClick = {
                    selectedText = s
                    expanded = false
                    onSelection(s)
                }) {
                    Text(text = s)
                }
            }
        }
    }
}