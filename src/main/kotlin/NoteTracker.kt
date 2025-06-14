package bibles

import androidx.compose.runtime.mutableStateMapOf
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

/**
 * A class to track notes for verses.
 * It stores the notes in a map where the key is a string in the format "book:chapter:verse"
 * and the value is the note text.
 */
class NoteTracker {
    // Map to store the notes for verses
    private val verseNotes = mutableStateMapOf<String, String>()
    
    // File to store the notes
    private val notesFile = File(System.getProperty("user.home"), ".biblepro_notes.properties")
    
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
        println(verseNotes.values.joinToString("\n"))
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
     * Loads the notes from a file.
     */
    private fun loadNotes() {
        if (notesFile.exists()) {
            val properties = Properties()
            FileInputStream(notesFile).use { properties.load(it) }
            
            properties.forEach { (key, value) ->
                verseNotes[key.toString()] = value.toString()
            }
        }
    }
    
    /**
     * Saves the notes to a file.
     */
    private fun saveNotes() {
        val properties = Properties()
        
        verseNotes.forEach { (key, value) ->
            properties[key] = value
        }
        
        FileOutputStream(notesFile).use { properties.store(it, "Bible Verse Notes") }
    }
    
    companion object {
        // Singleton instance
        val instance = NoteTracker()
    }
}