package backup

import kotlinx.serialization.Serializable

/**
 * Data class representing a reading plan entry for backup.
 */
@Serializable
data class BackupReadingPlanEntry(
    val day: Int,
    val bookId: Int,
    val chapterStart: Int,
    val chapterEnd: Int
)

/**
 * Data class representing a reading plan for backup.
 */
@Serializable
data class BackupReadingPlan(
    val type: String,
    val name: String,
    val description: String,
    val startDate: String,
    val currentDay: Int,
    val entries: List<BackupReadingPlanEntry>
)

/**
 * Main backup data class containing all exportable data.
 * This is the root object that gets serialized to JSON.
 */
@Serializable
data class BackupData(
    val version: Int = 1,
    val exportDate: String,
    val notes: Map<String, String> = emptyMap(),
    val readingStats: Map<String, String> = emptyMap(),
    val readingPlans: List<BackupReadingPlan> = emptyList()
)