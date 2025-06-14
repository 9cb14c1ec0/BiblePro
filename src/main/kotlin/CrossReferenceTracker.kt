package bibles

import androidx.compose.runtime.mutableStateMapOf
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

/**
 * A class to track cross-references for verses.
 * It stores the cross-references in a map where the key is a string in the format "book:chapter:verse"
 * and the value is a list of cross-reference strings.
 */
class CrossReferenceTracker {
    // Map to store the cross-references for verses
    private val verseCrossReferences = mutableStateMapOf<String, List<String>>()
    
    // File to store the cross-references
    private val crossReferencesFile = File(System.getProperty("user.home"), ".biblepro_crossreferences.properties")
    
    init {
        // Load cross-references from file if it exists
        loadCrossReferences()
    }
    
    /**
     * Sets cross-references for a verse.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     * @param references The list of cross-reference strings
     */
    fun setCrossReferences(book: Int, chapter: Int, verse: Int, references: List<String>) {
        val key = "$book:$chapter:$verse"
        if (references.isEmpty()) {
            // If references list is empty, remove it
            verseCrossReferences.remove(key)
        } else {
            verseCrossReferences[key] = references
        }
        saveCrossReferences()
    }
    
    /**
     * Gets the cross-references for a verse.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     * @return The list of cross-reference strings, or an empty list if no cross-references exist
     */
    fun getCrossReferences(book: Int, chapter: Int, verse: Int): List<String> {
        val key = "$book:$chapter:$verse"
        return verseCrossReferences[key] ?: emptyList()
    }
    
    /**
     * Adds a cross-reference to a verse.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     * @param reference The cross-reference string
     */
    fun addCrossReference(book: Int, chapter: Int, verse: Int, reference: String) {
        val key = "$book:$chapter:$verse"
        val currentReferences = verseCrossReferences[key]?.toMutableList() ?: mutableListOf()
        if (!currentReferences.contains(reference)) {
            currentReferences.add(reference)
            verseCrossReferences[key] = currentReferences
            saveCrossReferences()
        }
    }
    
    /**
     * Removes a cross-reference from a verse.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     * @param reference The cross-reference string
     */
    fun removeCrossReference(book: Int, chapter: Int, verse: Int, reference: String) {
        val key = "$book:$chapter:$verse"
        val currentReferences = verseCrossReferences[key]?.toMutableList() ?: return
        if (currentReferences.remove(reference)) {
            if (currentReferences.isEmpty()) {
                verseCrossReferences.remove(key)
            } else {
                verseCrossReferences[key] = currentReferences
            }
            saveCrossReferences()
        }
    }
    
    /**
     * Loads the cross-references from a file.
     */
    private fun loadCrossReferences() {
        if (crossReferencesFile.exists()) {
            val properties = Properties()
            FileInputStream(crossReferencesFile).use { properties.load(it) }
            
            properties.forEach { (key, value) ->
                val references = value.toString().split("|").filter { it.isNotBlank() }
                if (references.isNotEmpty()) {
                    verseCrossReferences[key.toString()] = references
                }
            }
        }
    }
    
    /**
     * Saves the cross-references to a file.
     */
    private fun saveCrossReferences() {
        val properties = Properties()
        
        verseCrossReferences.forEach { (key, value) ->
            properties[key] = value.joinToString("|")
        }
        
        FileOutputStream(crossReferencesFile).use { properties.store(it, "Bible Verse Cross References") }
    }
    
    companion object {
        // Singleton instance
        val instance = CrossReferenceTracker()
    }
}