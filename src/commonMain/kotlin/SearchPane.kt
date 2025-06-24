import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import bibles.loaded_bibles
import viewmodels.SearchViewModel
// Import from correct locations
import ComboOption
import MyDropdownMenu

@Composable
fun SearchPane(
    OnAddClicked: () -> Unit,
    OnCloseClicked: (unit: Int) -> Unit,
    thisUnit: Int,
    totalUnits: Float,
    viewModel: SearchViewModel = remember { SearchViewModel() }
) {
    // Collect state from ViewModel
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        Row(modifier = Modifier.padding(5.dp)) {
            TextField(
                value = state.searchText,
                placeholder = { Text("Search") },
                onValueChange = { newText ->
                    // Update search text using ViewModel
                    viewModel.updateSearchText(newText, loaded_bibles)
                },
                modifier = Modifier.weight(1f)
            )
            if(totalUnits > 1) {
                MyDropdownMenu(
                    listOf(
                        ComboOption("New Search", -1),
                    ),
                    Icons.Default.MoreVert
                ) { item: ComboOption ->
                    when(item.id) {
                        -1 -> OnAddClicked()
                    }
                }
            }
            IconButton(onClick = { OnCloseClicked(thisUnit) }) {
                Icon(
                    Icons.Default.Close, 
                    contentDescription = "Close",
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }
        
        Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f), thickness = 1.dp)

        LazyColumn { 
            items(state.searchResults.size) { index ->
                SelectionContainer(
                    Modifier
                        .padding(10.dp)
                        .border(1.dp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f), shape = RoundedCornerShape(5.dp))
                ) {
                    ClickableText(
                        text = buildAnnotatedString { 
                            append(
                                state.searchResults[index].bible + ": " +
                                state.searchResults[index].reference + "\n" + 
                                state.searchResults[index].text
                            ) 
                        },
                        onClick = { offset ->
                            // Open the clicked verse in a new pane
                            // This could be enhanced in future iterations
                        },
                        style = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onSurface),
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }
    }
}
