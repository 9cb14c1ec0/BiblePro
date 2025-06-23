package bibles

import androidx.compose.runtime.mutableStateMapOf
import storage.DesktopPlatformStorage
import storage.PlatformStorage

/**
 * A class to track notes for verses.
 * It stores the notes in a map where the key is a string in the format "book:chapter:verse"
 * and the value is the note text.
 */
class NoteTracker {
    // Map to store the notes for verses
    private val verseNotes = mutableStateMapOf<String, String>()

    // Platform storage for notes
    private val storage: PlatformStorage = DesktopPlatformStorage()
    private val notesFilename = "notes.properties"

    init {
        // Load notes from file if it exists
        loadNotes()
    }

    /**
     * Adds or updates a note for a verse.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     * @param note The note text
     */
    fun setNote(book: Int, chapter: Int, verse: Int, note: String) {
        val key = "$book:$chapter:$verse"
        if (note.isBlank()) {
            // If note is blank, remove it
            verseNotes.remove(key)
        } else {
            verseNotes[key] = note
        }
        saveNotes()
    }

    /**
     * Gets the note for a verse.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     * @return The note text, or an empty string if no note exists
     */
    fun getNote(book: Int, chapter: Int, verse: Int): String {
        val key = "$book:$chapter:$verse"
        return verseNotes[key] ?: ""
    }

    /**
     * Checks if a verse has a note.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     * @return True if the verse has a note, false otherwise
     */
    fun hasNote(book: Int, chapter: Int, verse: Int): Boolean {
        val key = "$book:$chapter:$verse"
        return verseNotes.containsKey(key) && verseNotes[key]?.isNotBlank() == true
    }

    /**
     * Removes a note for a verse.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     */
    fun removeNote(book: Int, chapter: Int, verse: Int) {
        val key = "$book:$chapter:$verse"
        verseNotes.remove(key)
        saveNotes()
    }

    /**
     * Gets all notes.
     * @return A map of all notes where the key is in the format "book:chapter:verse" and the value is the note text
     */
    fun getAllNotes(): Map<String, String> {
        return verseNotes.toMap()
    }

    /**
     * Loads the notes from storage.
     */
    private fun loadNotes() {
        val properties = storage.loadProperties(notesFilename)
        properties.forEach { (key, value) ->
            verseNotes[key] = value
        }
    }

    /**
     * Saves the notes to storage.
     */
    private fun saveNotes() {
        storage.saveProperties(notesFilename, verseNotes.toMap())
    }

    companion object {
        // Singleton instance
        val instance = NoteTracker()
    }
}
