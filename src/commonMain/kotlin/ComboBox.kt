import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

data class ComboOption(
    override val text: String,
    val id: Int,
) : SelectableOption

interface SelectableOption {
    val text: String
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MyComboBox(
    labelText: String,
    options: List<ComboOption>,
    onOptionsChosen: (List<ComboOption>) -> Unit,
    modifier: Modifier = Modifier,
    selectedIds: List<Int> = emptyList(),
    singleSelect: Boolean = false,
    triggerVar: Int = 0
) {
    var expanded by remember { mutableStateOf(false) }
    // when no options available, I want ComboBox to be disabled
    val isEnabled by rememberUpdatedState { options.isNotEmpty() }

    // Initialize with selectedIds only once
    val selectedOptionsList = remember(selectedIds) {
        mutableStateListOf<Int>().apply {
            addAll(selectedIds)
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (isEnabled()) {
                expanded = !expanded
                if (!expanded) {
                    onOptionsChosen(options.filter { it.id in selectedOptionsList }.toList())
                }
            }
        },
        modifier = modifier,
    ) {
        val selectedSummary = when (selectedOptionsList.size) {
            0 -> ""
            1 -> options.firstOrNull { it.id == selectedOptionsList.first() }?.text ?: ""
            else -> selectedOptionsList.mapNotNull { id -> options.find { it.id == id }?.text }.joinToString(", ")
        }
        TextField(
            enabled = isEnabled(),
            readOnly = true,
            value = selectedSummary,
            onValueChange = {},
            label = { Text(text = labelText) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                onOptionsChosen(options.filter { it.id in selectedOptionsList }.toList())
            },
            modifier = Modifier
        ) {
            for (option in options) {

                //use derivedStateOf to evaluate if it is checked
                var checked = remember {
                    derivedStateOf{option.id in selectedOptionsList}
                }.value

                DropdownMenuItem(
                    onClick = {
                        if (!checked) {
                            if(singleSelect)
                            {
                                selectedOptionsList.clear()
                            }
                            selectedOptionsList.add(option.id)
                        } else {
                            selectedOptionsList.remove(option.id)
                        }
                    }
                ){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { newCheckedState ->
                                if (newCheckedState) {
                                    if(singleSelect)
                                    {
                                        selectedOptionsList.clear()
                                    }
                                    selectedOptionsList.add(option.id)
                                } else {
                                    selectedOptionsList.remove(option.id)
                                }
                            },
                        )
                        Text(text = option.text)
                    }
                }
            }
        }
    }
}