package verse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import icons.CommonIcons
import locale.L
import viewmodels.VerseData

/**
 * Context menu for verse actions.
 * Works on both desktop and mobile.
 */
@Composable
fun VerseContextMenu(
    expanded: Boolean,
    verseData: VerseData,
    bookName: String,
    chapter: Int,
    onDismiss: () -> Unit,
    onMarkAsRead: () -> Unit,
    onToggleHighlight: () -> Unit,
    onEditNote: () -> Unit,
    onAddCrossReference: () -> Unit,
    offset: DpOffset = DpOffset(0.dp, 0.dp)
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = offset,
        modifier = Modifier.widthIn(min = 200.dp)
    ) {
        // Header with verse reference
        Text(
            text = "$bookName $chapter:${verseData.verse}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        HorizontalDivider()

        // Mark as Read
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (verseData.isRead) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (verseData.isRead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (verseData.isRead) L.current.l("Marked as read") else L.current.l("Mark as read"))
                }
            },
            onClick = {
                onMarkAsRead()
                onDismiss()
            }
        )

        // Highlight
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = CommonIcons.Highlight,
                        contentDescription = null,
                        tint = if (verseData.isHighlighted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (verseData.isHighlighted) L.current.l("Remove highlight") else L.current.l("Highlight"))
                }
            },
            onClick = {
                onToggleHighlight()
                onDismiss()
            }
        )

        // Add/Edit Note
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = CommonIcons.Note,
                        contentDescription = null,
                        tint = if (verseData.hasNote) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (verseData.hasNote) L.current.l("Edit note") else L.current.l("Add note"))
                }
            },
            onClick = {
                onEditNote()
                onDismiss()
            }
        )

        // Cross References
        DropdownMenuItem(
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = CommonIcons.Link,
                        contentDescription = null,
                        tint = if (verseData.crossReferences.isNotEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(L.current.l("Cross references"))
                    if (verseData.crossReferences.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${verseData.crossReferences.size}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            },
            onClick = {
                onAddCrossReference()
                onDismiss()
            }
        )
    }
}

/**
 * Dialog for editing verse notes.
 */
@Composable
fun NoteEditDialog(
    bookName: String,
    chapter: Int,
    verse: Int,
    initialText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var textValue by remember { mutableStateOf(TextFieldValue(initialText)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp, max = 400.dp)
            ) {
                // Title
                Text(
                    text = "${L.current.l("Note for")} $bookName $chapter:$verse",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Text field
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 200.dp),
                    placeholder = { Text(L.current.l("Enter your note...")) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(L.current.l("Cancel"))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onSave(textValue.text) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(L.current.l("Save"))
                    }
                }
            }
        }
    }
}

/**
 * Dialog for managing cross-references.
 */
@Composable
fun CrossReferenceDialog(
    bookName: String,
    chapter: Int,
    verse: Int,
    references: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var newReference by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp, max = 400.dp)
            ) {
                // Title
                Text(
                    text = "${L.current.l("Cross references for")} $bookName $chapter:$verse",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Existing references
                if (references.isNotEmpty()) {
                    references.forEach { ref ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = ref,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { onRemove(ref) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = L.current.l("Remove"),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // Add new reference
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newReference,
                        onValueChange = { newReference = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(L.current.l("e.g., John 3:16")) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (newReference.isNotBlank()) {
                                onAdd(newReference)
                                newReference = ""
                            }
                        },
                        enabled = newReference.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = L.current.l("Add"),
                            tint = if (newReference.isNotBlank())
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(L.current.l("Done"))
                    }
                }
            }
        }
    }
}