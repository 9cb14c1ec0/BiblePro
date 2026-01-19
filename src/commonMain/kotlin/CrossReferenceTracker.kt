package bibles

import androidx.compose.runtime.mutableStateMapOf
import storage.PlatformStorage
import storage.createPlatformStorage

/**
 * A class to track cross-references for verses.
 * It stores the cross-references in a map where the key is a string in the format "book:chapter:verse"
 * and the value is a list of cross-reference strings.
 */
class CrossReferenceTracker {
    // Map to store the cross-references for verses
    private val verseCrossReferences = mutableStateMapOf<String, List<String>>()

    // Storage for cross-references (lazy to avoid Android context issues during class loading)
    private val storage: PlatformStorage by lazy { createPlatformStorage() }

    // Track whether we've loaded cross-references
    private var isLoaded = false

    private fun ensureLoaded() {
        if (!isLoaded) {
            isLoaded = true
            loadCrossReferences()
        }
    }

    /**
     * Sets cross-references for a verse.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     * @param references The list of cross-reference strings
     */
    fun setCrossReferences(book: Int, chapter: Int, verse: Int, references: List<String>) {
        ensureLoaded()
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
        ensureLoaded()
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
        ensureLoaded()
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
        ensureLoaded()
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
     * Loads the cross-references from storage.
     */
    private fun loadCrossReferences() {
        try {
            val properties = storage.loadProperties(STORAGE_FILENAME)

            properties.forEach { (key, value) ->
                val references = value.split("|").filter { it.isNotBlank() }
                if (references.isNotEmpty()) {
                    verseCrossReferences[key] = references
                }
            }
        } catch (e: Exception) {
            println("Error loading cross-references: ${e.message}")
        }
    }

    /**
     * Saves the cross-references to storage.
     */
    private fun saveCrossReferences() {
        try {
            val properties = mutableMapOf<String, String>()

            verseCrossReferences.forEach { (key, value) ->
                properties[key] = value.joinToString("|")
            }

            storage.saveProperties(STORAGE_FILENAME, properties)
        } catch (e: Exception) {
            println("Error saving cross-references: ${e.message}")
        }
    }

    companion object {
        private const val STORAGE_FILENAME = "crossreferences.properties"

        // Singleton instance (lazy to avoid Android context issues during class loading)
        val instance: CrossReferenceTracker by lazy { CrossReferenceTracker() }
    }
}