import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import bibles.*
import locale.L
import phonetics.PhoneticSettings
import phonetics.rememberPhoneticSettings
import theme.ThemeState
import verse.ChapterView
import viewmodels.BibleViewModel
import viewmodels.ChapterViewModel


@Composable
fun BiblePane(
    OnAddClicked: () -> Unit,
    OnCloseClicked: (unit: Int) -> Unit,
    OnNewSearch: () -> Unit,
    OnGlobalNotesClicked: () -> Unit = {},
    thisUnit: Int,
    totalUnits: Float,
    viewModel: BibleViewModel = remember { BibleViewModel() },
    themeState: ThemeState? = null,
    phoneticSettings: PhoneticSettings = rememberPhoneticSettings()
) {
    // Collect state from ViewModel
    val state by viewModel.state.collectAsState()

    // Shared ChapterViewModel for the entire chapter (performance improvement)
    val chapterViewModel = remember { ChapterViewModel() }

    // Local state for lexicon text
    var lexiconText by remember { mutableStateOf("") }

    // Load saved Bible preferences on first composition (only for the first pane)
    val savedBibleIds = remember { if (thisUnit == 1) BiblePreferences.getSelectedBibleIds() else emptyList() }

    // Auto-load saved Bibles on first composition and initialize book/chapter
    LaunchedEffect(Unit) {
        if (thisUnit == 1 && savedBibleIds.isNotEmpty()) {
            val savedBibleNames = bibleList.filter { it.id in savedBibleIds }.map { it.text }
            if (savedBibleNames.isNotEmpty()) {
                viewModel.loadBibles(savedBibleNames)
                // Initialize the chapters list for the default book
                viewModel.selectBook(viewModel.state.value.bookId)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top toolbar row
        Row(modifier = Modifier.padding(5.dp)) {
            MyComboBox(
                L.current.l("Bibles"), bibleList,
                onOptionsChosen = { selectedOptions ->
                    // Load selected Bibles using ViewModel
                    viewModel.loadBibles(selectedOptions.map { it.text })
                },
                modifier = Modifier.weight(1f),
                singleSelect = false
            )
            MinimalDropdownMenu()
            ReadingMenu()

            // Add settings menu with theme and phonetics options
            SettingsMenu(
                themeState = themeState,
                phoneticSettings = phoneticSettings
            )

            if(totalUnits > 1) {
                MyDropdownMenu(
                    listOf(
                        ComboOption(L.current.l("New Bible"), -1),
                        ComboOption(L.current.l("Close Bible"), 1),
                        ComboOption(L.current.l("New Search"), 2),
                        ComboOption(L.current.l("Global Notes"), 3)
                    ),
                    Icons.Filled.MoreVert,
                    OnSelectionChange = { i ->
                        if(i.id == 1) {
                            OnCloseClicked(thisUnit)
                        } else if(i.id == -1) {
                            OnAddClicked()
                        } else if(i.id == 2) {
                            OnNewSearch()
                        } else if(i.id == 3) {
                            OnGlobalNotesClicked()
                        }
                    }
                )
            } else if(totalUnits.toInt() == 1) {
                MyDropdownMenu(
                    listOf(
                        ComboOption(L.current.l("New Bible"), -1),
                        ComboOption(L.current.l("New Search"), 2),
                        ComboOption(L.current.l("Global Notes"), 3)
                    ),
                    Icons.Filled.MoreVert,
                    OnSelectionChange = { i ->
                        if(i.id == -1) {
                            OnAddClicked()
                        } else if(i.id == 2) {
                            OnNewSearch()
                        } else if(i.id == 3) {
                            OnGlobalNotesClicked()
                        }
                    }
                )
            }
        }

        // Book and chapter selection row
        Row(modifier = Modifier.padding(5.dp)) {
            val localizedBookList = getLocalizedBookList()
            DropdownMenuBox(
                L.current.l("Book"),
                localizedBookList.firstOrNull { it.id == state.bookId }?.text ?: "",
                localizedBookList.map { it.text },
                { selected ->
                    // Select book using ViewModel
                    val bookId = localizedBookList.first { it.text == selected }.id
                    viewModel.selectBook(bookId)
                },
                modifier = Modifier.weight(1f).padding(5.dp)
            )

            DropdownMenuBox(
                L.current.l("Chapter"),
                state.chapterNum.toString(),
                state.chapters,
                { selected ->
                    if (state.chapters.contains(selected)) {
                        // Select chapter using ViewModel
                        viewModel.selectChapter(selected.toInt())
                    }
                },
                filterOptions = false,
                modifier = Modifier.weight(1f).padding(5.dp)
            )
        }

        // Main content area
        if (state.bibleCount > 0) {
            // Use the new ChapterView component
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                ChapterView(
                    bibles = state.bibles,
                    book = state.bookId,
                    chapter = state.chapterNum,
                    showPhonetics = phoneticSettings.showPhonetics,
                    phoneticLanguage = phoneticSettings.language,
                    onWordSelected = { wordIndex ->
                        // Get lexicon entry using ViewModel
                        lexiconText = viewModel.getLexiconEntry(
                            state.bookId,
                            state.chapterNum,
                            1, // The ChapterView handles verse internally
                            wordIndex
                        )
                    },
                    viewModel = chapterViewModel
                )
            }
        } else {
            // Empty state with proper theming when no Bible is selected
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = L.current.l("Select a Bible to begin reading"),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                        Text(
                            text = L.current.l("Choose from available translations above"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Lexicon text at bottom
        if (lexiconText.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Text(
                    text = lexiconText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}