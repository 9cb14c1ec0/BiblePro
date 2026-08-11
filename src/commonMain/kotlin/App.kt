import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bibles.BiblePreferences
import bibles.LexiconCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import locale.L
import locale.LanguagePreferences
import platform.isMobilePlatform
import phonetics.PhoneticLanguage
import phonetics.PhoneticSettings
import phonetics.rememberPhoneticSettings
import session.AppSession
import session.PaneKind
import session.PaneSession
import session.SessionPreferences
import theme.BibleProTheme
import theme.ThemeMode
import theme.rememberThemeState

@Composable
fun App() {
    // Check if language preference is set
    var showLanguageSelection by remember { mutableStateOf(!LanguagePreferences.hasPreferredLanguage()) }

    // Restore the session that was saved when the app was last used
    val savedSession = remember { SessionPreferences.load() }

    // Initialize language from preferences and preload lexicon data for fast lookups
    LaunchedEffect(Unit) {
        val preferredLanguage = LanguagePreferences.getPreferredLanguage()
        if (preferredLanguage != null) {
            L.current.language = preferredLanguage
        }

        // Preload lexicon data in background for fast Strong's lookups
        launch(Dispatchers.IO) {
            LexiconCache.preloadData()
        }
    }

    // Theme state to manage light/dark mode, restored from the saved session
    val themeState = rememberThemeState(
        initialThemeMode = savedSession.themeMode
            ?.let { name -> ThemeMode.values().firstOrNull { it.name == name } }
            ?: ThemeMode.SYSTEM
    )

    // Phonetics settings to manage phonetics display, restored from the saved session
    val phoneticSettings = rememberPhoneticSettings(
        initialShowPhonetics = savedSession.showPhonetics,
        initialLanguage = savedSession.phoneticLanguage
            ?.let { name -> PhoneticLanguage.values().firstOrNull { it.name == name } }
            ?: PhoneticLanguage.NONE
    )

    BibleProTheme(themeMode = themeState.themeMode) {
        if (showLanguageSelection) {
            LanguageSelectionScreen(
                onLanguageSelected = {
                    showLanguageSelection = false
                }
            )
        } else {
            MainAppContent(savedSession, themeState, phoneticSettings)
        }
    }
}

/**
 * A single open pane. Holds the pane's restorable state as observable state so
 * that changes can be persisted as they happen.
 */
private class PaneEntry(val id: Int, val kind: PaneKind, initial: PaneSession) {
    val initialBibleIds: List<Int> = initial.bibleIds
    val initialBookId: Int = initial.bookId
    val initialChapterNum: Int = initial.chapterNum
    val initialSearchText: String = initial.searchText

    var bibleIds by mutableStateOf(initial.bibleIds)
    var bookId by mutableStateOf(initial.bookId)
    var chapterNum by mutableStateOf(initial.chapterNum)
    var searchText by mutableStateOf(initial.searchText)

    fun toSession() = PaneSession(kind, bibleIds, bookId, chapterNum, searchText)
}

/**
 * Builds the initial pane list, either from the saved session or as a single
 * default Bible pane.
 */
private fun initialPanes(savedSession: AppSession): List<PaneEntry> {
    if (savedSession.panes.isNotEmpty()) {
        return savedSession.panes.mapIndexed { index, pane -> PaneEntry(index, pane.kind, pane) }
    }
    // First run: fall back to the last used translations, if any
    return listOf(
        PaneEntry(0, PaneKind.BIBLE, PaneSession(PaneKind.BIBLE, bibleIds = BiblePreferences.getSelectedBibleIds()))
    )
}

@OptIn(FlowPreview::class)
@Composable
private fun MainAppContent(
    savedSession: AppSession,
    themeState: theme.ThemeState,
    phoneticSettings: PhoneticSettings
) {
    val panes: SnapshotStateList<PaneEntry> = remember {
        mutableStateListOf<PaneEntry>().apply { addAll(initialPanes(savedSession)) }
    }
    var nextPaneId by remember { mutableStateOf(panes.size) }

    // Movable content per pane so that each pane keeps its internal state
    // (scroll position, selections) when rotation switches the pane container
    // between a Row and a Column.
    val movablePaneContents = remember { mutableMapOf<Int, @Composable (Modifier) -> Unit>() }

    fun addPane(kind: PaneKind) {
        // New Bible panes start from the translations currently in use
        val bibleIds = if (kind == PaneKind.BIBLE) {
            panes.firstOrNull { it.kind == PaneKind.BIBLE }?.bibleIds ?: BiblePreferences.getSelectedBibleIds()
        } else {
            emptyList()
        }
        panes.add(PaneEntry(nextPaneId, kind, PaneSession(kind, bibleIds = bibleIds)))
        nextPaneId += 1
    }

    fun closePane(id: Int) {
        panes.removeAll { it.id == id }
        movablePaneContents.remove(id)
        // Never leave the user with an empty window
        if (panes.isEmpty()) {
            addPane(PaneKind.BIBLE)
        }
    }

    // Persist the session whenever it changes, debounced so typing in a search
    // box does not hit the disk on every keystroke.
    LaunchedEffect(Unit) {
        snapshotFlow {
            AppSession(
                panes = panes.map { it.toSession() },
                themeMode = themeState.themeMode.name,
                showPhonetics = phoneticSettings.showPhonetics,
                phoneticLanguage = phoneticSettings.language.name
            )
        }
            .distinctUntilChanged()
            .debounce(500)
            .collect { SessionPreferences.save(it) }
    }

    fun paneContentFor(pane: PaneEntry): @Composable (Modifier) -> Unit =
        movablePaneContents.getOrPut(pane.id) {
            movableContentOf { modifier: Modifier ->
                Column(
                    modifier = modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    when (pane.kind) {
                        PaneKind.BIBLE -> BiblePane(
                            OnAddClicked = { addPane(PaneKind.BIBLE) },
                            OnCloseClicked = { closePane(pane.id) },
                            OnNewSearch = { addPane(PaneKind.SEARCH) },
                            OnGlobalNotesClicked = { addPane(PaneKind.NOTES) },
                            totalUnits = panes.size.toFloat(),
                            initialBibleIds = pane.initialBibleIds,
                            initialBookId = pane.initialBookId,
                            initialChapterNum = pane.initialChapterNum,
                            onSessionChanged = { bibleIds, bookId, chapterNum ->
                                pane.bibleIds = bibleIds
                                pane.bookId = bookId
                                pane.chapterNum = chapterNum
                            },
                            themeState = themeState,
                            phoneticSettings = phoneticSettings
                        )

                        PaneKind.SEARCH -> SearchPane(
                            OnAddClicked = { addPane(PaneKind.SEARCH) },
                            OnCloseClicked = { closePane(pane.id) },
                            totalUnits = panes.size.toFloat(),
                            initialSearchText = pane.initialSearchText,
                            onSearchTextChanged = { pane.searchText = it }
                        )

                        PaneKind.NOTES -> GlobalNotesView(
                            OnCloseClicked = { closePane(pane.id) },
                            totalUnits = panes.size.toFloat(),
                            initialSearchText = pane.initialSearchText,
                            onSearchTextChanged = { pane.searchText = it }
                        )
                    }
                }
            }
        }

    Column(modifier = Modifier.fillMaxSize()) {
        // Main content: panes sit side by side, except on portrait mobile
        // screens where they stack vertically.
        BoxWithConstraints(modifier = Modifier.fillMaxSize().weight(1f)) {
            val stackVertically = isMobilePlatform && maxHeight > maxWidth
            if (stackVertically) {
                Column(modifier = Modifier.fillMaxSize()) {
                    panes.forEach { pane ->
                        key(pane.id) {
                            paneContentFor(pane)(Modifier.weight(1F))
                        }
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    panes.forEach { pane ->
                        key(pane.id) {
                            paneContentFor(pane)(Modifier.weight(1F))
                        }
                    }
                }
            }
        }
    }
}
