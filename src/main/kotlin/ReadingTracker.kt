package bibles

import androidx.compose.runtime.mutableStateMapOf
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

/**
 * A class to track which verses have been read.
 * It stores the reading status in a map where the key is a string in the format "book:chapter:verse"
 * and the value is a boolean indicating whether the verse has been read.
 */
class ReadingTracker {
    // Map to store the reading status of verses
    private val readVerses = mutableStateMapOf<String, Boolean>()
    
    // File to store the reading status
    private val readingFile = File(System.getProperty("user.home"), ".biblepro_reading.properties")
    
    init {
        // Load reading status from file if it exists
        loadReadingStatus()
    }
    
    /**
     * Marks a verse as read.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     */
    fun markAsRead(book: Int, chapter: Int, verse: Int) {
        val key = "$book:$chapter:$verse"
        readVerses[key] = true
        saveReadingStatus()
    }
    
    /**
     * Checks if a verse has been read.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     * @return True if the verse has been read, false otherwise
     */
    fun isRead(book: Int, chapter: Int, verse: Int): Boolean {
        val key = "$book:$chapter:$verse"
        return readVerses[key] ?: false
    }
    
    /**
     * Resets all reading status.
     */
    fun resetReadingStatus() {
        readVerses.clear()
        saveReadingStatus()
    }
    
    /**
     * Gets a map of read books and chapters.
     * @return A map where the key is the book number and the value is a list of read chapter numbers
     */
    fun getReadSections(): Map<Int, List<Int>> {
        val result = mutableMapOf<Int, MutableList<Int>>()
        
        readVerses.keys.forEach { key ->
            val parts = key.split(":")
            val book = parts[0].toInt()
            val chapter = parts[1].toInt()
            
            if (!result.containsKey(book)) {
                result[book] = mutableListOf()
            }
            
            if (!result[book]!!.contains(chapter)) {
                result[book]!!.add(chapter)
            }
        }
        
        return result
    }
    
    /**
     * Loads the reading status from a file.
     */
    private fun loadReadingStatus() {
        if (readingFile.exists()) {
            val properties = Properties()
            FileInputStream(readingFile).use { properties.load(it) }
            
            properties.forEach { (key, value) ->
                if (value == "true") {
                    readVerses[key.toString()] = true
                }
            }
        }
    }
    
    /**
     * Saves the reading status to a file.
     */
    private fun saveReadingStatus() {
        val properties = Properties()
        
        readVerses.forEach { (key, value) ->
            properties[key] = value.toString()
        }
        
        FileOutputStream(readingFile).use { properties.store(it, "Bible Reading Tracker") }
    }
    
    companion object {
        // Singleton instance
        val instance = ReadingTracker()
    }
}