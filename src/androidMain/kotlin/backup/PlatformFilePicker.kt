package backup

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * Android implementation of PlatformFilePicker.
 * Uses the app's external files directory for backup storage.
 */
actual class PlatformFilePicker {

    /**
     * Opens a save file dialog for exporting data.
     * On Android, saves to the app's external files directory.
     */
    actual fun pickExportLocation(defaultFilename: String, onResult: (String?) -> Unit) {
        val context = AndroidFilePickerContext.context
        if (context == null) {
            onResult(null)
            return
        }

        // Use external files directory (accessible via file manager)
        val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        if (externalDir != null) {
            if (!externalDir.exists()) {
                externalDir.mkdirs()
            }
            val file = File(externalDir, defaultFilename)
            onResult(file.absolutePath)
        } else {
            // Fallback to internal files directory
            val internalDir = context.filesDir
            val file = File(internalDir, defaultFilename)
            onResult(file.absolutePath)
        }
    }

    /**
     * Opens an open file dialog for importing data.
     * On Android, looks for backup files in the app's external files directory.
     */
    actual fun pickImportFile(onResult: (String?) -> Unit) {
        val context = AndroidFilePickerContext.context
        if (context == null) {
            onResult(null)
            return
        }

        // Look for the most recent backup file
        val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val backupFiles = externalDir?.listFiles { file ->
            file.isFile && file.name.startsWith("biblepro_backup_") && file.name.endsWith(".json")
        }?.sortedByDescending { it.lastModified() }

        if (!backupFiles.isNullOrEmpty()) {
            onResult(backupFiles.first().absolutePath)
        } else {
            // Check internal files directory
            val internalDir = context.filesDir
            val internalBackupFiles = internalDir.listFiles { file ->
                file.isFile && file.name.startsWith("biblepro_backup_") && file.name.endsWith(".json")
            }?.sortedByDescending { it.lastModified() }

            if (!internalBackupFiles.isNullOrEmpty()) {
                onResult(internalBackupFiles.first().absolutePath)
            } else {
                onResult(null)
            }
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

/**
 * Object to hold Android context for file picker operations.
 * Must be initialized from MainActivity.
 */
object AndroidFilePickerContext {
    var context: Context? = null
        private set

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
}

/**
 * Initialize the Android file picker context.
 * Call this from MainActivity.onCreate().
 */
fun initializeFilePicker(context: Context) {
    AndroidFilePickerContext.initialize(context)
}