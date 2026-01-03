import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibles.*
import locale.L
import phonetics.PhoneticSettings
import phonetics.rememberPhoneticSettings
import theme.ThemeState
import viewmodels.BibleViewModel


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

    // Local state for lexicon text (could be moved to ViewModel in future iterations)
    var lexiconText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
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

        if (state.bibleCount > 0) {
        val testament = if (state.bookId > 39) "New" else "Old"

        // Get reference to KJV Bible for structure
        val kjvBible = state.bibles["English KJV"] ?: state.bibles.values.firstOrNull()

        if (kjvBible != null) {
            // Extract Bible structure safely before composable functions
            val bookAndChapter = try {
                val book = kjvBible.testaments
                    .first { it.name == testament }
                    .books
                    .first { it.number == state.bookId }

                val chapter = book.chapters.first { it.number == state.chapterNum }
                Pair(book, chapter)
            } catch (e: NoSuchElementException) {
                null
            }

            if (bookAndChapter != null) {
                val (book, chapter) = bookAndChapter

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colors.background)
                        .border(2.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
                ) {
                        items(count = chapter.verses.size, itemContent = { item ->
                            VerseCard(
                                bibles = state.bibles, 
                                book = state.bookId, 
                                chapter = state.chapterNum, 
                                verse = item + 1,
                                OnSelectionChange = { wordIndex ->
                                    // Get lexicon entry using ViewModel
                                    lexiconText = viewModel.getLexiconEntry(
                                        state.bookId, 
                                        state.chapterNum, 
                                        item + 1, 
                                        wordIndex
                                    )
                                },
                                showPhonetics = phoneticSettings.showPhonetics,
                                phoneticLanguage = phoneticSettings.language
                            )
                        })
                }
            } else {
                Text(L.current.l("Error loading Bible content"), Modifier.padding(5.dp))
            }
        }
    } else {
        // Empty state with proper theming when no Bible is selected
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colors.background)
                .border(2.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = L.current.l("Select a Bible to begin reading"),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = L.current.l("Choose from available translations above"),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }

        // Lexicon text with fixed height at bottom
        Text(
            text = lexiconText, 
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(5.dp)
        )
    }
}
