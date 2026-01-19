package bibles

import storage.createPlatformStorage

/**
 * Manages Bible selection preferences for the application.
 * Handles saving and loading the user's selected Bible translations.
 */
object BiblePreferences {
    private const val PREFERENCES_FILE = "bible_preferences.properties"
    private const val SELECTED_BIBLES_KEY = "selected_bibles"

    private val storage = createPlatformStorage()

    /**
     * Gets the list of selected Bible IDs.
     * @return List of Bible IDs that were previously selected, or empty list if none
     */
    fun getSelectedBibleIds(): List<Int> {
        val properties = storage.loadProperties(PREFERENCES_FILE)
        val selectedStr = properties[SELECTED_BIBLES_KEY] ?: return emptyList()
        return selectedStr.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    /**
     * Sets the selected Bible IDs.
     * @param bibleIds List of Bible IDs to save
     */
    fun setSelectedBibleIds(bibleIds: List<Int>) {
        val properties = mapOf(SELECTED_BIBLES_KEY to bibleIds.joinToString(","))
        storage.saveProperties(PREFERENCES_FILE, properties)
    }

    /**
     * Gets the list of selected Bible names.
     * @return List of Bible names that were previously selected
     */
    fun getSelectedBibleNames(): List<String> {
        val ids = getSelectedBibleIds()
        return bibleList.filter { it.id in ids }.map { it.text }
    }

    /**
     * Checks if any Bibles have been previously selected.
     * @return True if there are saved Bible selections
     */
    fun hasSelectedBibles(): Boolean {
        return getSelectedBibleIds().isNotEmpty()
    }
}