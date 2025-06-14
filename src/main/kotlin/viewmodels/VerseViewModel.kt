package viewmodels

import androidx.compose.runtime.mutableStateOf
import bibles.Bible
import bibles.NoteTracker
import bibles.ReadingTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the Verse card component.
 * Handles verse display, note management, and reading tracking.
 */
class VerseViewModel {
    // State for the Verse card
    private val _state = MutableStateFlow(VerseState())
    val state: StateFlow<VerseState> = _state.asStateFlow()

    /**
     * Initializes the ViewModel with verse information.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     * @param bibles Map of loaded Bibles
     */
    fun initialize(book: Int, chapter: Int, verse: Int, bibles: Map<String, Bible>) {
        val isRead = ReadingTracker.instance.isRead(book, chapter, verse)
        val hasNote = NoteTracker.instance.hasNote(book, chapter, verse)
        val noteText = NoteTracker.instance.getNote(book, chapter, verse)
        
        _state.update { currentState ->
            currentState.copy(
                book = book,
                chapter = chapter,
                verse = verse,
                bibles = bibles,
                isRead = isRead,
                hasNote = hasNote,
                noteText = noteText,
                isCompactMode = bibles.size == 1
            )
        }
    }

    /**
     * Marks the verse as read.
     */
    fun markAsRead() {
        val book = _state.value.book
        val chapter = _state.value.chapter
        val verse = _state.value.verse
        
        ReadingTracker.instance.markAsRead(book, chapter, verse)
        
        _state.update { currentState ->
            currentState.copy(
                isRead = true
            )
        }
    }

    /**
     * Shows the note dialog.
     */
    fun showNoteDialog() {
        _state.update { currentState ->
            currentState.copy(
                showNoteDialog = true
            )
        }
    }

    /**
     * Hides the note dialog.
     */
    fun hideNoteDialog() {
        _state.update { currentState ->
            currentState.copy(
                showNoteDialog = false
            )
        }
    }

    /**
     * Saves the note text.
     * @param noteText The note text to save
     */
    fun saveNote(noteText: String) {
        val book = _state.value.book
        val chapter = _state.value.chapter
        val verse = _state.value.verse
        
        NoteTracker.instance.setNote(book, chapter, verse, noteText)
        
        _state.update { currentState ->
            currentState.copy(
                noteText = noteText,
                hasNote = noteText.isNotBlank(),
                showNoteDialog = false
            )
        }
    }

    /**
     * Gets the verse text for a specific Bible.
     * @param bibleName The name of the Bible
     * @return The verse text, or null if the verse doesn't exist in the Bible
     */
    fun getVerseText(bibleName: String): String? {
        val book = _state.value.book
        val chapter = _state.value.chapter
        val verse = _state.value.verse
        val bibles = _state.value.bibles
        
        if (!bibles.containsKey(bibleName)) {
            return null
        }
        
        val bible = bibles[bibleName]!!
        val testament = if (book > 39) "New" else "Old"
        
        try {
            return bible.testaments
                .first { it.name == testament }
                .books
                .first { it.number == book }
                .chapters
                .first { it.number == chapter }
                .verses
                .first { it.number == verse }
                .text
        } catch (e: NoSuchElementException) {
            return null
        }
    }

    /**
     * Checks if a verse exists in a specific Bible.
     * @param bibleName The name of the Bible
     * @return True if the verse exists, false otherwise
     */
    fun hasVerse(bibleName: String): Boolean {
        return getVerseText(bibleName) != null
    }
}

/**
 * Data class representing the state of a verse card.
 */
data class VerseState(
    val book: Int = 1,
    val chapter: Int = 1,
    val verse: Int = 1,
    val bibles: Map<String, Bible> = emptyMap(),
    val isRead: Boolean = false,
    val hasNote: Boolean = false,
    val noteText: String = "",
    val showNoteDialog: Boolean = false,
    val isCompactMode: Boolean = true
)