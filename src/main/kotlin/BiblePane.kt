import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bibles.*
import kotlinx.coroutines.flow.StateFlow
import nl.marc_apps.tts.TextToSpeechEngine
import nl.marc_apps.tts.experimental.ExperimentalDesktopTarget
import nl.marc_apps.tts.rememberTextToSpeechOrNull
import theme.ThemeMode
import theme.ThemeState
import theme.ThemeToggle
import viewmodels.BibleState
import viewmodels.BibleViewModel

@OptIn(ExperimentalDesktopTarget::class)
@Composable
fun BiblePane(
    OnAddClicked: () -> Unit,
    OnCloseClicked: (unit: Int) -> Unit,
    OnNewSearch: () -> Unit,
    OnGlobalNotesClicked: () -> Unit = {},
    thisUnit: Int,
    totalUnits: Float,
    viewModel: BibleViewModel = remember { BibleViewModel() },
    themeState: ThemeState? = null
) {
    // Collect state from ViewModel
    val state by viewModel.state.collectAsState()

    // Local state for lexicon text (could be moved to ViewModel in future iterations)
    var lexiconText by remember { mutableStateOf("") }

    Row(modifier = Modifier.padding(5.dp)) {
        MyComboBox(
            "Bibles", bibleList,
            onOptionsChosen = { selectedOptions ->
                // Load selected Bibles using ViewModel
                viewModel.loadBibles(selectedOptions.map { it.text })
            },
            modifier = Modifier.weight(1f),
            singleSelect = false
        )
        MinimalDropdownMenu()
        ReadingMenu()
        // Add theme toggle if themeState is provided
        themeState?.let {
            ThemeToggle(themeState = it)
        }
        if(totalUnits > 1) {
            MyDropdownMenu(
                listOf(
                    ComboOption("New Bible", -1),
                    ComboOption("Close Bible", 1),
                    ComboOption("New Search", 2),
                    ComboOption("Global Notes", 3)
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
                    ComboOption("New Bible", -1),
                    ComboOption("New Search", 2),
                    ComboOption("Global Notes", 3)
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
        DropdownMenuBox(
            "Book", 
            bookList.firstOrNull { it.id == state.bookId }?.text ?: "", 
            bookList.map { it.text }, 
            { selected ->
                // Select book using ViewModel
                val bookId = bookList.first { it.text == selected }.id
                viewModel.selectBook(bookId)
            }, 
            modifier = Modifier.weight(1f).padding(5.dp)
        )

        DropdownMenuBox(
            "Chapter", 
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

                Row {
                    LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92F).border(2.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.5f))) {
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
                                }
                            )
                        })
                    }
                }
            } else {
                Text("Error loading Bible content", Modifier.padding(5.dp))
            }
        }
    }

    Text(lexiconText, Modifier.padding(5.dp))
}
