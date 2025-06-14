import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bibles.NoteTracker
import bibles.bookList
import viewmodels.GlobalNotesViewModel

/**
 * A view that displays all notes in the application with a search box to filter them.
 */
@Composable
fun GlobalNotesView(
    OnCloseClicked: (unit: Int) -> Unit,
    thisUnit: Int,
    totalUnits: Float,
    viewModel: GlobalNotesViewModel = remember { GlobalNotesViewModel() }
) {
    // Collect state from ViewModel
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with search box and menu
        Row(
            modifier = Modifier.padding(5.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Global Notes",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(end = 8.dp)
            )
            
            TextField(
                value = state.searchText,
                placeholder = { Text("Search notes...") },
                onValueChange = { newText ->
                    // Update search text using ViewModel
                    viewModel.updateSearchText(newText)
                },
                modifier = Modifier.weight(1f)
            )
            
            if(totalUnits > 1) {
                IconButton(onClick = { OnCloseClicked(thisUnit) }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Close")
                }
            }
        }

        Divider(color = Color.Black, thickness = 1.dp)

        // Notes list
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) { 
            items(state.filteredNotes.size) { index ->
                val note = state.filteredNotes[index]
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Note reference
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val bookName = bookList.find { it.id == note.book }?.text ?: "Unknown Book"
                            Text(
                                "$bookName ${note.chapter}:${note.verse}",
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colors.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Note content
                        SelectionContainer {
                            Text(
                                note.text,
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            
            // Show message if no notes found
            if (state.filteredNotes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.allNotes.isEmpty()) {
                            Text("No notes have been created yet.")
                        } else {
                            Text("No notes match your search.")
                        }
                    }
                }
            }
        }
    }
}