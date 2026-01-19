package verse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import bibles.Bible
import bibles.bookList
import locale.L
import phonetics.PhoneticLanguage
import viewmodels.ChapterViewModel
import viewmodels.VerseData

/**
 * Efficient chapter view using shared ViewModel and lightweight verse rows.
 */
@Composable
fun ChapterView(
    bibles: Map<String, Bible>,
    book: Int,
    chapter: Int,
    showPhonetics: Boolean = false,
    phoneticLanguage: PhoneticLanguage = PhoneticLanguage.NONE,
    onWordSelected: (verse: Int, wordIndex: Int) -> Unit = { _, _ -> },
    viewModel: ChapterViewModel = remember { ChapterViewModel() },
    modifier: Modifier = Modifier
) {
    // Load chapter when parameters change
    LaunchedEffect(book, chapter, bibles.keys.toSet(), showPhonetics, phoneticLanguage) {
        viewModel.loadChapter(book, chapter, bibles, showPhonetics, phoneticLanguage)
    }

    // Update phonetics when settings change
    LaunchedEffect(showPhonetics, phoneticLanguage) {
        viewModel.updatePhonetics(showPhonetics, phoneticLanguage)
    }

    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    // Context menu state
    var contextMenuVerse by remember { mutableStateOf<Int?>(null) }
    var contextMenuOffset by remember { mutableStateOf(DpOffset.Zero) }

    // Dialog states
    var showNoteDialog by remember { mutableStateOf(false) }
    var showCrossRefDialog by remember { mutableStateOf(false) }
    var dialogVerse by remember { mutableStateOf<VerseData?>(null) }

    val bookName = bookList.find { it.id == book }?.text ?: "Book"
    val bibleNamesList = state.bibleNames.toList()

    Box(modifier = modifier.fillMaxSize()) {
        if (state.verseCount == 0) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = L.current.l("Select a Bible to begin reading"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    count = state.verseCount,
                    key = { verseNum -> verseNum + 1 }
                ) { index ->
                    val verseNum = index + 1
                    val verseData = state.verses[verseNum]

                    if (verseData != null) {
                        VerseRow(
                            verseData = verseData,
                            bibleNames = bibleNamesList,
                            isCompactMode = state.isCompactMode,
                            showPhonetics = state.showPhonetics,
                            isSelected = state.selectedVerse == verseNum,
                            onVerseClick = {
                                // Toggle selection on click
                                if (state.selectedVerse == verseNum) {
                                    viewModel.selectVerse(null)
                                } else {
                                    viewModel.selectVerse(verseNum)
                                }
                            },
                            onVerseLongClick = {
                                // Show context menu on long click
                                contextMenuVerse = verseNum
                            },
                            onWordClick = onWordSelected,
                            modifier = Modifier
                        )

                        // Thin divider between verses
                        if (index < state.verseCount - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 44.dp, end = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Context menu
            contextMenuVerse?.let { verseNum ->
                val verseData = state.verses[verseNum]
                if (verseData != null) {
                    VerseContextMenu(
                        expanded = true,
                        verseData = verseData,
                        bookName = bookName,
                        chapter = chapter,
                        onDismiss = { contextMenuVerse = null },
                        onMarkAsRead = {
                            viewModel.markVerseAsRead(verseNum)
                        },
                        onToggleHighlight = {
                            viewModel.toggleHighlight(verseNum)
                        },
                        onEditNote = {
                            dialogVerse = verseData
                            showNoteDialog = true
                        },
                        onAddCrossReference = {
                            dialogVerse = verseData
                            showCrossRefDialog = true
                        },
                        offset = contextMenuOffset
                    )
                }
            }
        }

        // Note dialog
        if (showNoteDialog && dialogVerse != null) {
            NoteEditDialog(
                bookName = bookName,
                chapter = chapter,
                verse = dialogVerse!!.verse,
                initialText = dialogVerse!!.noteText,
                onDismiss = {
                    showNoteDialog = false
                    dialogVerse = null
                },
                onSave = { noteText ->
                    viewModel.saveNote(dialogVerse!!.verse, noteText)
                    showNoteDialog = false
                    dialogVerse = null
                }
            )
        }

        // Cross-reference dialog
        if (showCrossRefDialog && dialogVerse != null) {
            CrossReferenceDialog(
                bookName = bookName,
                chapter = chapter,
                verse = dialogVerse!!.verse,
                references = dialogVerse!!.crossReferences,
                onDismiss = {
                    showCrossRefDialog = false
                    dialogVerse = null
                },
                onAdd = { ref ->
                    viewModel.addCrossReference(dialogVerse!!.verse, ref)
                    // Update dialogVerse to reflect new state
                    dialogVerse = state.verses[dialogVerse!!.verse]
                },
                onRemove = { ref ->
                    viewModel.removeCrossReference(dialogVerse!!.verse, ref)
                    // Update dialogVerse to reflect new state
                    dialogVerse = state.verses[dialogVerse!!.verse]
                }
            )
        }
    }
}

/**
 * Floating action button for verse actions (alternative to context menu on desktop).
 */
@Composable
fun VerseActionsFab(
    selectedVerse: Int?,
    verseData: VerseData?,
    bookName: String,
    chapter: Int,
    onMarkAsRead: () -> Unit,
    onToggleHighlight: () -> Unit,
    onEditNote: () -> Unit,
    onAddCrossReference: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedVerse == null || verseData == null) return

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = { expanded = true },
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = selectedVerse.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        VerseContextMenu(
            expanded = expanded,
            verseData = verseData,
            bookName = bookName,
            chapter = chapter,
            onDismiss = { expanded = false },
            onMarkAsRead = onMarkAsRead,
            onToggleHighlight = onToggleHighlight,
            onEditNote = onEditNote,
            onAddCrossReference = onAddCrossReference,
            offset = DpOffset(0.dp, (-56).dp)
        )
    }
}