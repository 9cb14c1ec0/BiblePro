package bibles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A dropdown menu that displays what sections of the Bible have been read.
 * It also includes a button to reset the reading data.
 */
@Composable
fun ReadingMenu() {
    var expanded by remember { mutableStateOf(false) }
    val readSections = ReadingTracker.instance.getReadSections()

    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Filled.Book, contentDescription = "Reading Tracker", tint = MaterialTheme.colors.onSurface)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colors.surface)
        ) {
            // Title
            Text(
                "Reading Progress",
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(8.dp)
            )

            Divider()

            // If no sections have been read, show a message
            if (readSections.isEmpty()) {
                Text(
                    "No sections read yet",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                // Display read sections
                Column(modifier = Modifier.padding(8.dp)) {
                    readSections.forEach { (bookId, chapters) ->
                        val bookName = bookList.find { it.id == bookId }?.text ?: "Book $bookId"
                        Text(
                            "$bookName: ${chapters.sorted().joinToString(", ")}",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Divider()

            // Reset button
            DropdownMenuItem(
                onClick = {
                    ReadingTracker.instance.resetReadingStatus()
                    expanded = false
                }
            ) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = "Reset",
                    modifier = Modifier.padding(end = 8.dp),
                    tint = MaterialTheme.colors.onSurface
                )
                Text("Reset Reading Data")
            }
        }
    }
}
