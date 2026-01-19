package backup

import bibles.NoteTracker
import bibles.ReadingPlan
import bibles.ReadingPlanEntry
import bibles.ReadingPlanManager
import bibles.ReadingPlanType
import bibles.ReadingTracker
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Result of an import operation.
 */
sealed class ImportResult {
    data class Success(
        val notesCount: Int,
        val readingStatsCount: Int,
        val readingPlansCount: Int
    ) : ImportResult()

    data class Error(val message: String, val details: String? = null) : ImportResult()
}

/**
 * Manager for exporting and importing backup data.
 * Handles serialization of notes, reading stats, and reading plans.
 */
object BackupManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Exports all data to a JSON string.
     * @return JSON string containing all backup data
     */
    fun exportToJson(): String {
        val backupData = collectBackupData()
        return json.encodeToString(backupData)
    }

    /**
     * Imports data from a JSON string.
     * @param jsonString The JSON string to import
     * @param clearExisting If true, clears existing data before importing
     * @return ImportResult with success details or error message
     */
    fun importFromJson(jsonString: String, clearExisting: Boolean = false): ImportResult {
        // Check if the string is empty
        if (jsonString.isBlank()) {
            return ImportResult.Error("File is empty", "The selected file contains no data.")
        }

        // Check if it looks like JSON
        val trimmed = jsonString.trim()
        if (!trimmed.startsWith("{")) {
            val preview = if (trimmed.length > 100) trimmed.substring(0, 100) + "..." else trimmed
            return ImportResult.Error(
                "Invalid file format",
                "Expected JSON but got: $preview"
            )
        }

        // Try to parse the JSON
        val backupData: BackupData
        try {
            backupData = json.decodeFromString<BackupData>(jsonString)
        } catch (e: kotlinx.serialization.SerializationException) {
            return ImportResult.Error(
                "Invalid backup format",
                "JSON parsing error: ${e.message}"
            )
        } catch (e: Exception) {
            return ImportResult.Error(
                "Failed to read backup",
                "${e::class.simpleName}: ${e.message}"
            )
        }

        // Validate the backup data
        if (backupData.version > 1) {
            return ImportResult.Error(
                "Incompatible backup version",
                "Backup version ${backupData.version} is not supported. Please update the app."
            )
        }

        // Restore the data
        try {
            restoreBackupData(backupData, clearExisting)
        } catch (e: Exception) {
            return ImportResult.Error(
                "Failed to restore data",
                "${e::class.simpleName}: ${e.message}"
            )
        }

        return ImportResult.Success(
            notesCount = backupData.notes.size,
            readingStatsCount = backupData.readingStats.size,
            readingPlansCount = backupData.readingPlans.size
        )
    }

    /**
     * Legacy method for compatibility - returns boolean.
     */
    fun importFromJsonLegacy(jsonString: String, clearExisting: Boolean = false): Boolean {
        return importFromJson(jsonString, clearExisting) is ImportResult.Success
    }

    /**
     * Collects all data from trackers into a BackupData object.
     */
    private fun collectBackupData(): BackupData {
        val notes = NoteTracker.instance.getAllNotes()
        val readingStats = ReadingTracker.instance.getAllReadTimestamps()
        val readingPlans = ReadingPlanManager.instance.getAllPlans().values.map { plan ->
            BackupReadingPlan(
                type = plan.type.name,
                name = plan.name,
                description = plan.description,
                startDate = plan.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                currentDay = plan.currentDay,
                entries = plan.entries.map { entry ->
                    BackupReadingPlanEntry(
                        day = entry.day,
                        bookId = entry.bookId,
                        chapterStart = entry.chapterStart,
                        chapterEnd = entry.chapterEnd
                    )
                }
            )
        }

        return BackupData(
            version = 1,
            exportDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            notes = notes,
            readingStats = readingStats,
            readingPlans = readingPlans
        )
    }

    /**
     * Restores data from a BackupData object to the trackers.
     */
    private fun restoreBackupData(data: BackupData, clearExisting: Boolean) {
        // Import notes
        NoteTracker.instance.setAllNotes(data.notes, clearExisting)

        // Import reading stats
        ReadingTracker.instance.setAllReadTimestamps(data.readingStats, clearExisting)

        // Import reading plans
        val plans = data.readingPlans.mapNotNull { backupPlan ->
            try {
                val type = ReadingPlanType.valueOf(backupPlan.type)
                val startDate = LocalDate.parse(backupPlan.startDate, DateTimeFormatter.ISO_LOCAL_DATE)
                val entries = backupPlan.entries.map { entry ->
                    ReadingPlanEntry(
                        day = entry.day,
                        bookId = entry.bookId,
                        chapterStart = entry.chapterStart,
                        chapterEnd = entry.chapterEnd
                    )
                }

                ReadingPlan(
                    type = type,
                    name = backupPlan.name,
                    description = backupPlan.description,
                    entries = entries,
                    startDate = startDate,
                    currentDay = backupPlan.currentDay
                )
            } catch (e: Exception) {
                println("Error parsing reading plan: ${e.message}")
                null
            }
        }

        ReadingPlanManager.instance.setAllPlans(plans, clearExisting)
    }

    /**
     * Gets the default filename for backup files.
     */
    fun getDefaultFilename(): String {
        val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return "biblepro_backup_$date.json"
    }
}