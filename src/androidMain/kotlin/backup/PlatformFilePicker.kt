package backup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Android implementation of PlatformFilePicker.
 * Uses Storage Access Framework (SAF) for file picking.
 */
actual class PlatformFilePicker {

    /**
     * Opens a save file dialog for exporting data.
     * Uses SAF CREATE_DOCUMENT intent.
     */
    actual fun pickExportLocation(defaultFilename: String, onResult: (String?) -> Unit) {
        val activity = AndroidFilePickerContext.activity
        if (activity == null) {
            onResult(null)
            return
        }

        // Store callback for later
        AndroidFilePickerContext.pendingExportCallback = onResult
        AndroidFilePickerContext.pendingExportFilename = defaultFilename

        // Launch SAF create document picker
        try {
            AndroidFilePickerContext.createDocumentLauncher?.launch(defaultFilename)
        } catch (e: Exception) {
            println("Error launching file picker: ${e.message}")
            onResult(null)
        }
    }

    /**
     * Opens an open file dialog for importing data.
     * Uses SAF OPEN_DOCUMENT intent.
     */
    actual fun pickImportFile(onResult: (String?) -> Unit) {
        val activity = AndroidFilePickerContext.activity
        if (activity == null) {
            onResult(null)
            return
        }

        // Store callback for later
        AndroidFilePickerContext.pendingImportCallback = onResult

        // Launch SAF open document picker
        try {
            AndroidFilePickerContext.openDocumentLauncher?.launch(arrayOf("application/json", "*/*"))
        } catch (e: Exception) {
            println("Error launching file picker: ${e.message}")
            onResult(null)
        }
    }

    /**
     * Writes content to a file using content URI.
     */
    actual fun writeFile(path: String, content: String): Boolean {
        val context = AndroidFilePickerContext.context ?: return false

        return try {
            // Check if it's a content URI or a file path
            if (path.startsWith("content://")) {
                val uri = Uri.parse(path)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
                true
            } else {
                // Regular file path (fallback)
                java.io.File(path).writeText(content)
                true
            }
        } catch (e: Exception) {
            println("Error writing file: ${e.message}")
            false
        }
    }

    /**
     * Reads content from a file using content URI.
     */
    actual fun readFile(path: String): String? {
        val context = AndroidFilePickerContext.context ?: return null

        return try {
            // Check if it's a content URI or a file path
            if (path.startsWith("content://")) {
                val uri = Uri.parse(path)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.readText()
                    }
                }
            } else {
                // Regular file path (fallback)
                java.io.File(path).readText()
            }
        } catch (e: Exception) {
            println("Error reading file: ${e.message}")
            null
        }
    }
}

/**
 * Object to hold Android context and activity result launchers for file picker operations.
 */
object AndroidFilePickerContext {
    var context: Context? = null
        private set
    var activity: ComponentActivity? = null
        private set

    // Activity result launchers
    var createDocumentLauncher: ActivityResultLauncher<String>? = null
        private set
    var openDocumentLauncher: ActivityResultLauncher<Array<String>>? = null
        private set

    // Pending callbacks
    var pendingExportCallback: ((String?) -> Unit)? = null
    var pendingExportFilename: String? = null
    var pendingImportCallback: ((String?) -> Unit)? = null

    fun initialize(activity: ComponentActivity) {
        this.context = activity.applicationContext
        this.activity = activity

        // Register activity result launchers
        createDocumentLauncher = activity.registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            val callback = pendingExportCallback
            pendingExportCallback = null
            pendingExportFilename = null

            if (uri != null) {
                callback?.invoke(uri.toString())
            } else {
                callback?.invoke(null)
            }
        }

        openDocumentLauncher = activity.registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            val callback = pendingImportCallback
            pendingImportCallback = null

            if (uri != null) {
                callback?.invoke(uri.toString())
            } else {
                callback?.invoke(null)
            }
        }
    }
}

/**
 * Initialize the Android file picker context.
 * MUST be called from MainActivity.onCreate() BEFORE setContent().
 */
fun initializeFilePicker(activity: ComponentActivity) {
    AndroidFilePickerContext.initialize(activity)
}