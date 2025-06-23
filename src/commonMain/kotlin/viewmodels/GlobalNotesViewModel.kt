package viewmodels

import androidx.compose.runtime.mutableStateOf
import bibles.NoteTracker
import bibles.bookList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Data class representing a note with its reference and content
 */
data class NoteItem(
    val book: Int,
    val chapter: Int,
    val verse: Int,
    val text: String
)

/**
 * State class for the GlobalNotesView
 */
data class GlobalNotesState(
    val searchText: String = "",
    val allNotes: List<NoteItem> = emptyList(),
    val filteredNotes: List<NoteItem> = emptyList()
)

/**
 * ViewModel for the GlobalNotesView
 * Handles loading notes from NoteTracker and filtering them based on search text
 */
class GlobalNotesViewModel {
    private val _state = MutableStateFlow(GlobalNotesState())
    val state: StateFlow<GlobalNotesState> = _state.asStateFlow()

    init {
        loadNotes()
    }

    /**
     * Loads all notes from NoteTracker
     */
    private fun loadNotes() {
        val noteTracker = NoteTracker.instance
        val notes = mutableListOf<NoteItem>()

        // Extract all notes from NoteTracker
        // The keys in NoteTracker are in the format "book:chapter:verse"
        noteTracker.getAllNotes().forEach { (key, text) ->
            val parts = key.split(":")
            if (parts.size == 3) {
                try {
                    val book = parts[0].toInt()
                    val chapter = parts[1].toInt()
                    val verse = parts[2].toInt()
                    notes.add(NoteItem(book, chapter, verse, text))
                } catch (e: NumberFormatException) {
                    // Skip invalid entries
                }
            }
        }

        // Sort notes by reference (book, chapter, verse)
        val sortedNotes = notes.sortedWith(compareBy({ it.book }, { it.chapter }, { it.verse }))

        _state.update { currentState ->
            currentState.copy(
                allNotes = sortedNotes,
                filteredNotes = sortedNotes
            )
        }
    }

    /**
     * Updates the search text and filters notes accordingly
     */
    fun updateSearchText(newText: String) {
        val filteredNotes = if (newText.isBlank()) {
            _state.value.allNotes
        } else {
            _state.value.allNotes.filter { note ->
                // Search in note text
                if (note.text.contains(newText, ignoreCase = true)) {
                    return@filter true
                }

                // Search in book name
                val bookName = bookList.find { it.id == note.book }?.text ?: ""
                if (bookName.contains(newText, ignoreCase = true)) {
                    return@filter true
                }

                // Search in reference (e.g., "John 3:16")
                val reference = "$bookName ${note.chapter}:${note.verse}"
                reference.contains(newText, ignoreCase = true)
            }
        }

        _state.update { currentState ->
            currentState.copy(
                searchText = newText,
                filteredNotes = filteredNotes
            )
        }
    }
}
