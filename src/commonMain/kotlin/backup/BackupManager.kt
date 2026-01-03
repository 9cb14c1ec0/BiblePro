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
     * @return True if import was successful, false otherwise
     */
    fun importFromJson(jsonString: String, clearExisting: Boolean = false): Boolean {
        return try {
            val backupData = json.decodeFromString<BackupData>(jsonString)
            restoreBackupData(backupData, clearExisting)
            true
        } catch (e: Exception) {
            println("Error importing backup: ${e.message}")
            false
        }
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