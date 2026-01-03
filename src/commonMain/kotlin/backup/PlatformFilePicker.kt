package backup

/**
 * Platform-specific file picker interface.
 * Each platform provides its own implementation for file selection dialogs.
 */
expect class PlatformFilePicker() {
    /**
     * Opens a save file dialog for exporting data.
     * @param defaultFilename The default filename to suggest
     * @param onResult Callback with the selected file path, or null if cancelled
     */
    fun pickExportLocation(defaultFilename: String, onResult: (String?) -> Unit)

    /**
     * Opens an open file dialog for importing data.
     * @param onResult Callback with the selected file path, or null if cancelled
     */
    fun pickImportFile(onResult: (String?) -> Unit)

    /**
     * Writes content to a file.
     * @param path The file path to write to
     * @param content The content to write
     * @return True if successful, false otherwise
     */
    fun writeFile(path: String, content: String): Boolean

    /**
     * Reads content from a file.
     * @param path The file path to read from
     * @return The file content, or null if failed
     */
    fun readFile(path: String): String?
}