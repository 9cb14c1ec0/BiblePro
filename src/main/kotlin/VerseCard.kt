import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.material.icons.filled.Note
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
import nl.marc_apps.tts.TextToSpeechEngine
import nl.marc_apps.tts.experimental.ExperimentalDesktopTarget
import nl.marc_apps.tts.rememberTextToSpeechOrNull
import viewmodels.VerseViewModel

@OptIn(ExperimentalDesktopTarget::class)
@Composable
fun VerseCard(
    bibles: Map<String, Bible>,
    book: Int,
    chapter: Int,
    verse: Int,
    OnSelectionChange: (index: Int) -> Unit,
    viewModel: VerseViewModel = remember { VerseViewModel() }
) {
    // Initialize ViewModel with verse information
    LaunchedEffect(book, chapter, verse, bibles) {
        viewModel.initialize(book, chapter, verse, bibles)
    }
    
    // Collect state from ViewModel
    val state by viewModel.state.collectAsState()
    
    // Text-to-speech functionality
    val textToSpeech = rememberTextToSpeechOrNull(TextToSpeechEngine.SystemDefault)
    
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

    // Check if only one Bible is selected for compact mode
    if (state.isCompactMode) {
        CompactVerseCard(
            state = state,
            onMarkAsRead = { viewModel.markAsRead() },
            onShowNoteDialog = { viewModel.showNoteDialog() },
            onWordSelected = OnSelectionChange
        )
    } else {
        ExpandedVerseCard(
            state = state,
            onMarkAsRead = { viewModel.markAsRead() },
            onShowNoteDialog = { viewModel.showNoteDialog() },
            onWordSelected = OnSelectionChange
        )
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
    onWordSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(6.dp))
            .border(0.5.dp, MaterialTheme.colors.primary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            .background(
                if (state.isRead) MaterialTheme.colors.primary.copy(alpha = 0.1f) 
                else MaterialTheme.colors.surface, 
                RoundedCornerShape(6.dp)
            )
            .clickable { onMarkAsRead() }
    ) {
        // Smaller verse number
        Text(
            state.verse.toString(),
            style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colors.primary,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .align(Alignment.CenterVertically)
        )

        // Note icon
        Icon(
            imageVector = Icons.Default.Note,
            contentDescription = if (state.hasNote) "Edit note" else "Add note",
            tint = if (state.hasNote) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.CenterVertically)
                .clickable(onClick = onShowNoteDialog)
                .padding(end = 4.dp)
        )

        // Verse content
        val bibleName = state.bibles.keys.firstOrNull() ?: ""
        val verseText = state.bibles[bibleName]?.let { bible ->
            val testament = if (state.book > 39) "New" else "Old"
            try {
                bible.testaments
                    .first { it.name == testament }
                    .books
                    .first { it.number == state.book }
                    .chapters
                    .first { it.number == state.chapter }
                    .verses
                    .first { it.number == state.verse }
                    .text
            } catch (e: NoSuchElementException) {
                null
            }
        }

        if (verseText != null) {
            var wordCounter = 0
            val greekRegex = Regex("""[\u0370-\u03FF\u1F00-\u1FFF]""")
            val annotatedText = buildAnnotatedString { 
                verseText.split(" ").forEach { word ->
                    pushStringAnnotation(wordCounter.toString(), word)
                    append(word)
                    append(" ")
                    pop()
                    wordCounter += 1
                } 
            }

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
                            .background(MaterialTheme.colors.background),
                        style = MaterialTheme.typography.body1.copy(
                            fontFamily = FontFamily.Serif,
                            lineHeight = 22.sp
                        )
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

@Composable
private fun ExpandedVerseCard(
    state: viewmodels.VerseState,
    onMarkAsRead: () -> Unit,
    onShowNoteDialog: () -> Unit,
    onWordSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .background(
                if (state.isRead) MaterialTheme.colors.primary.copy(alpha = 0.1f) 
                else MaterialTheme.colors.surface, 
                RoundedCornerShape(8.dp)
            )
            .clickable { onMarkAsRead() }
    ) {
        // Verse number and note icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp)
        ) {
            // Verse number in a circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    state.verse.toString(),
                    style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colors.primary
                )
            }

            // Note icon
            Icon(
                imageVector = Icons.Default.Note,
                contentDescription = if (state.hasNote) "Edit note" else "Add note",
                tint = if (state.hasNote) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(20.dp)
                    .padding(start = 4.dp)
                    .clickable(onClick = onShowNoteDialog)
            )
        }

        // Verse content column
        Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
            var counter = 0
            for (bibleName in state.bibles.keys) {
                val verseText = state.bibles[bibleName]?.let { bible ->
                    val testament = if (state.book > 39) "New" else "Old"
                    try {
                        bible.testaments
                            .first { it.name == testament }
                            .books
                            .first { it.number == state.book }
                            .chapters
                            .first { it.number == state.chapter }
                            .verses
                            .first { it.number == state.verse }
                            .text
                    } catch (e: NoSuchElementException) {
                        null
                    }
                }

                if (verseText != null) {
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

                    var wordCounter = 0
                    val greekRegex = Regex("""[\u0370-\u03FF\u1F00-\u1FFF]""")
                    val annotatedText = buildAnnotatedString { 
                        verseText.split(" ").forEach { word ->
                            pushStringAnnotation(wordCounter.toString(), word)
                            append(word)
                            append(" ")
                            pop()
                            wordCounter += 1
                        } 
                    }

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
                                    // Subtle pastel colors instead of grayscale
                                    when (counter % 3) {
                                        0 -> MaterialTheme.colors.primary.copy(alpha = 0.05f)
                                        1 -> MaterialTheme.colors.secondary.copy(alpha = 0.05f)
                                        else -> MaterialTheme.colors.background
                                    }
                                ),
                            style = MaterialTheme.typography.body1.copy(
                                fontFamily = FontFamily.Serif,
                                lineHeight = 24.sp
                            )
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