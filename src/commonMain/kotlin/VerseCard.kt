import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import icons.CommonIcons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import bibles.*
import phonetics.PhoneticLanguage
import phonetics.PhoneticGenerator
import phonetics.PhoneticGeneratorFactory
import viewmodels.VerseViewModel

@Composable
fun VerseCard(
    bibles: Map<String, Bible>,
    book: Int,
    chapter: Int,
    verse: Int,
    OnSelectionChange: (index: Int) -> Unit,
    viewModel: VerseViewModel = remember { VerseViewModel() },
    showPhonetics: Boolean = false,
    phoneticLanguage: PhoneticLanguage = PhoneticLanguage.NONE
) {
    // Initialize ViewModel with verse information
    LaunchedEffect(book, chapter, verse, bibles, showPhonetics, phoneticLanguage) {
        viewModel.initialize(book, chapter, verse, bibles)

        // Update phonetics settings in ViewModel
        if (showPhonetics != viewModel.state.value.showPhonetics) {
            viewModel.togglePhonetics()
        }

        if (phoneticLanguage != viewModel.state.value.phoneticLanguage) {
            viewModel.setPhoneticLanguage(phoneticLanguage)
        }
    }

    // Collect state from ViewModel
    val state by viewModel.state.collectAsState()

    // Dialog for editing notes
    if (state.showNoteDialog) {
        NoteDialog(
            bookName = bookList.find { it.id == state.book }?.text ?: "Book",
            chapter = state.chapter,
            verse = state.verse,
            initialNoteText = state.noteText,
            onDismiss = { viewModel.hideNoteDialog() },
            onSave = { noteText -> viewModel.saveNote(noteText) }
        )
    }

    // Use remember with derivedStateOf to minimize recompositions
    val isCompactMode = remember(state.isCompactMode) { state.isCompactMode }

    // Check if only one Bible is selected for compact mode
    if (isCompactMode) {
        // Use key to help with recomposition optimization
        key(state.book, state.chapter, state.verse) {
            CompactVerseCard(
                state = state,
                onMarkAsRead = { viewModel.markAsRead() },
                onShowNoteDialog = { viewModel.showNoteDialog() },
                onToggleHighlight = { viewModel.toggleHighlighting() },
                onToggleCrossReference = { viewModel.toggleCrossReferenceInput() },
                onWordSelected = OnSelectionChange
            )
        }
    } else {
        // Use key to help with recomposition optimization
        key(state.book, state.chapter, state.verse) {
            ExpandedVerseCard(
                state = state,
                onMarkAsRead = { viewModel.markAsRead() },
                onShowNoteDialog = { viewModel.showNoteDialog() },
                onToggleHighlight = { viewModel.toggleHighlighting() },
                onToggleCrossReference = { viewModel.toggleCrossReferenceInput() },
                onWordSelected = OnSelectionChange
            )
        }
    }

    // Cross-reference section
    if (state.showCrossReferenceInput || state.crossReferences.isNotEmpty()) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Display existing cross-references if available
            if (state.crossReferences.isNotEmpty()) {
                CrossReferenceSection(
                    references = state.crossReferences,
                    onRemoveReference = { reference -> viewModel.removeCrossReference(reference) }
                )
            }

            // Add cross-reference UI only when showCrossReferenceInput is true
            if (state.showCrossReferenceInput) {
                var newReference by remember { mutableStateOf("") }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newReference,
                        onValueChange = { newReference = it },
                        label = { Text("Add Cross Reference") },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (newReference.isNotBlank()) {
                                viewModel.addCrossReference(newReference)
                                newReference = ""
                            }
                        },
                        enabled = newReference.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteDialog(
    bookName: String,
    chapter: Int,
    verse: Int,
    initialNoteText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colors.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Note for $bookName $chapter:$verse",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                var textFieldValue by remember { mutableStateOf(TextFieldValue(initialNoteText)) }

                TextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    label = { Text("Enter your note") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onSave(textFieldValue.text)
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactVerseCard(
    state: viewmodels.VerseState,
    onMarkAsRead: () -> Unit,
    onShowNoteDialog: () -> Unit,
    onToggleHighlight: () -> Unit = {},
    onToggleCrossReference: () -> Unit = {},
    onWordSelected: (Int) -> Unit
) {
    // Remember the regex to avoid recreating it on each recomposition
    val greekRegex = remember { Regex("""[\u0370-\u03FF\u1F00-\u1FFF]""") }
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(6.dp))
            .border(
                width = if (state.isRead) 2.dp else 0.5.dp,
                color = if (state.isRead) MaterialTheme.colors.primary.copy(alpha = 0.5f) else MaterialTheme.colors.primary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(6.dp)
            )
            .background(
                MaterialTheme.colors.surface, 
                RoundedCornerShape(6.dp)
            )
            .clickable { onMarkAsRead() }
    ) {
        // Smaller verse number with read indicator
        Text(
            state.verse.toString(),
            style = MaterialTheme.typography.caption.copy(
                fontWeight = if (state.isRead) FontWeight.ExtraBold else FontWeight.Bold
            ),
            color = if (state.isRead) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = 0.7f),
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .align(Alignment.CenterVertically)
        )

        // Note icon
        Icon(
            imageVector = CommonIcons.Note,
            contentDescription = if (state.hasNote) "Edit note" else "Add note",
            tint = if (state.hasNote) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically)
                .clickable(onClick = onShowNoteDialog)
                .padding(end = 4.dp)
        )

        // Highlight icon
        Icon(
            imageVector = CommonIcons.Highlight,
            contentDescription = if (state.isHighlighted) "Disable highlighting" else "Enable highlighting",
            tint = if (state.isHighlighted) MaterialTheme.colors.secondary else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically)
                .clickable(onClick = onToggleHighlight)
                .padding(end = 4.dp)
        )

        // Cross reference icon
        Icon(
            imageVector = CommonIcons.Link,
            contentDescription = "Cross References",
            tint = if (state.crossReferences.isNotEmpty()) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.CenterVertically)
                .clickable(onClick = onToggleCrossReference)
                .padding(end = 4.dp)
        )

        // Verse content
        val bibleName = state.bibles.keys.firstOrNull() ?: ""
        val annotatedText = state.processedVerseTexts[bibleName]

        if (annotatedText != null) {

            // Compact verse text display
            Column {
                SelectionContainer {
                    ClickableText(
                        text = annotatedText,
                        onClick = { offset ->
                            annotatedText.getStringAnnotations(start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    if (greekRegex.containsMatchIn(annotation.item)) {
                                        onWordSelected(annotation.tag.toInt())
                                    }
                                }
                        },
                        modifier = Modifier
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .background(
                                when {
                                    state.isHighlighted -> MaterialTheme.colors.secondary.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colors.background
                                }
                            ),
                        style = MaterialTheme.typography.body1.copy(
                            fontFamily = FontFamily.Serif,
                            lineHeight = 22.sp,
                            letterSpacing = 0.sp,
                            color = MaterialTheme.colors.onSurface
                        )
                    )
                }

                // Display phonetics if enabled and available for this Bible
                if (state.showPhonetics && state.phoneticTexts.containsKey(bibleName)) {
                    Text(
                        text = "Pronunciation: ${state.phoneticTexts[bibleName]}",
                        style = MaterialTheme.typography.caption.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colors.primary.copy(alpha = 0.7f),
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .background(MaterialTheme.colors.primary.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                            .padding(4.dp)
                            .fillMaxWidth()
                    )
                }

                // Display note if it exists
                if (state.hasNote) {
                    Text(
                        text = state.noteText,
                        style = MaterialTheme.typography.caption.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        color = MaterialTheme.colors.primary.copy(alpha = 0.7f),
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .background(MaterialTheme.colors.primary.copy(alpha = 0.05f))
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Displays a section for cross-references.
 */
@Composable
private fun CrossReferenceSection(
    references: List<String>,
    onRemoveReference: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(
            "Cross References",
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        references.forEach { reference ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    reference,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { onRemoveReference(reference) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(
                        "×",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedVerseCard(
    state: viewmodels.VerseState,
    onMarkAsRead: () -> Unit,
    onShowNoteDialog: () -> Unit,
    onToggleHighlight: () -> Unit = {},
    onToggleCrossReference: () -> Unit = {},
    onWordSelected: (Int) -> Unit
) {
    // Remember the regex to avoid recreating it on each recomposition
    val greekRegex = remember { Regex("""[\u0370-\u03FF\u1F00-\u1FFF]""") }
    Row(
        modifier = Modifier
            .padding(8.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
            .border(
                width = if (state.isRead) 2.dp else 1.dp,
                color = if (state.isRead) MaterialTheme.colors.primary.copy(alpha = 0.6f) else MaterialTheme.colors.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                MaterialTheme.colors.surface, 
                RoundedCornerShape(8.dp)
            )
            .clickable { onMarkAsRead() }
    ) {
        // Verse number and note icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp)
        ) {
            // Verse number in a circle with read indicator
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (state.isRead) MaterialTheme.colors.primary.copy(alpha = 0.15f) 
                        else MaterialTheme.colors.primary.copy(alpha = 0.1f), 
                        CircleShape
                    )
                    .border(
                        width = if (state.isRead) 1.25.dp else 1.dp,
                        color = if (state.isRead) MaterialTheme.colors.primary.copy(alpha = 0.5f) 
                               else MaterialTheme.colors.primary.copy(alpha = 0.3f), 
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    state.verse.toString(),
                    style = MaterialTheme.typography.caption.copy(
                        fontWeight = if (state.isRead) FontWeight.ExtraBold else FontWeight.Bold
                    ),
                    color = if (state.isRead) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = 0.7f)
                )
            }

            // Note icon
            Icon(
                imageVector = CommonIcons.Note,
                contentDescription = if (state.hasNote) "Edit note" else "Add note",
                tint = if (state.hasNote) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 4.dp)
                    .clickable(onClick = onShowNoteDialog)
            )

            // Highlight icon
            Icon(
                imageVector = CommonIcons.Highlight,
                contentDescription = if (state.isHighlighted) "Disable highlighting" else "Enable highlighting",
                tint = if (state.isHighlighted) MaterialTheme.colors.secondary else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 4.dp)
                    .clickable(onClick = onToggleHighlight)
            )

            // Cross reference icon
            Icon(
                imageVector = CommonIcons.Link,
                contentDescription = "Cross References",
                tint = if (state.crossReferences.isNotEmpty()) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(28.dp)
                    .padding(start = 4.dp)
                    .clickable(onClick = onToggleCrossReference)
            )
        }

        // Verse content column
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
            var counter = 0
            for (bibleName in state.bibles.keys) {
                val annotatedText = state.processedVerseTexts[bibleName]

                if (annotatedText != null) {
                    // Add divider between translations if not the first one
                    if (counter > 0) {
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
                        )
                    }

                    // Bible version label
                    Text(
                        bibleName,
                        style = MaterialTheme.typography.overline,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 2.dp, top = if (counter > 0) 4.dp else 0.dp)
                    )

                    // Improved verse text display
                    SelectionContainer {
                        ClickableText(
                            text = annotatedText,
                            onClick = { offset ->
                                annotatedText.getStringAnnotations(start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        if (greekRegex.containsMatchIn(annotation.item)) {
                                            onWordSelected(annotation.tag.toInt())
                                        }
                                    }
                            },
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .background(
                                    // Apply highlighting if enabled, otherwise use subtle pastel colors
                                    when {
                                        state.isHighlighted -> MaterialTheme.colors.secondary.copy(alpha = 0.15f)
                                        else -> when (counter % 3) {
                                            0 -> MaterialTheme.colors.primary.copy(alpha = 0.05f)
                                            1 -> MaterialTheme.colors.secondary.copy(alpha = 0.05f)
                                            else -> MaterialTheme.colors.background
                                        }
                                    }
                                ),
                            style = MaterialTheme.typography.body1.copy(
                                fontFamily = FontFamily.Serif,
                                lineHeight = 24.sp,
                                letterSpacing = 0.sp,
                                color = MaterialTheme.colors.onSurface
                            )
                        )
                    }

                    // Display phonetics if enabled and available for this Bible
                    if (state.showPhonetics && state.phoneticTexts.containsKey(bibleName)) {
                        Text(
                            text = "Pronunciation: ${state.phoneticTexts[bibleName]}",
                            style = MaterialTheme.typography.caption.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontWeight = FontWeight.Normal
                            ),
                            color = MaterialTheme.colors.primary.copy(alpha = 0.7f),
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .background(MaterialTheme.colors.primary.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                                .fillMaxWidth()
                        )
                    }

                    counter += 1
                }
            }

            // Display note if it exists (after all translations)
            if (state.hasNote) {
                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colors.primary.copy(alpha = 0.2f)
                )
                Text(
                    text = "Note: ${state.noteText}",
                    style = MaterialTheme.typography.caption.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colors.primary.copy(alpha = 0.8f),
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                )
            }
        }
    }
}
