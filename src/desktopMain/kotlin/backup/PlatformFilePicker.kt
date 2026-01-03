package backup

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * Desktop implementation of PlatformFilePicker using AWT FileDialog.
 */
actual class PlatformFilePicker {

    /**
     * Opens a save file dialog for exporting data.
     * Uses AWT FileDialog in SAVE mode.
     */
    actual fun pickExportLocation(defaultFilename: String, onResult: (String?) -> Unit) {
        val dialog = FileDialog(null as Frame?, "Export Data", FileDialog.SAVE)
        dialog.file = defaultFilename
        dialog.setFilenameFilter { _, name -> name.endsWith(".json") }
        dialog.isVisible = true

        val directory = dialog.directory
        val file = dialog.file

        if (directory != null && file != null) {
            var path = "$directory$file"
            // Ensure .json extension
            if (!path.endsWith(".json")) {
                path += ".json"
            }
            onResult(path)
        } else {
            onResult(null)
        }
    }

    /**
     * Opens an open file dialog for importing data.
     * Uses AWT FileDialog in LOAD mode.
     */
    actual fun pickImportFile(onResult: (String?) -> Unit) {
        val dialog = FileDialog(null as Frame?, "Import Data", FileDialog.LOAD)
        dialog.setFilenameFilter { _, name -> name.endsWith(".json") }
        dialog.isVisible = true

        val directory = dialog.directory
        val file = dialog.file

        if (directory != null && file != null) {
            onResult("$directory$file")
        } else {
            onResult(null)
        }
    }

    /**
     * Writes content to a file.
     */
    actual fun writeFile(path: String, content: String): Boolean {
        return try {
            File(path).writeText(content)
            true
        } catch (e: Exception) {
            println("Error writing file: ${e.message}")
            false
        }
    }

    /**
     * Reads content from a file.
     */
    actual fun readFile(path: String): String? {
        return try {
            File(path).readText()
        } catch (e: Exception) {
            println("Error reading file: ${e.message}")
            null
        }
    }
}