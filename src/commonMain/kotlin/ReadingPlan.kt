package bibles

import androidx.compose.runtime.mutableStateMapOf
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Properties

/**
 * A class to represent a reading plan.
 * It defines a sequence of Bible readings to be completed over a period of time.
 */
data class ReadingPlanEntry(
    val day: Int,
    val bookId: Int,
    val chapterStart: Int,
    val chapterEnd: Int
)

/**
 * Enum representing different types of reading plans.
 */
enum class ReadingPlanType {
    BIBLE_IN_A_YEAR,
    NEW_TESTAMENT_IN_90_DAYS,
    CUSTOM
}

/**
 * A class to manage reading plans.
 */
class ReadingPlanManager {
    // Map to store active reading plans
    private val activePlans = mutableStateMapOf<ReadingPlanType, ReadingPlan>()

    // File to store reading plan data
    private val plansFile = File(System.getProperty("user.home"), ".biblepro_reading_plans.properties")

    init {
        // Load plans from file if it exists
        loadPlans()
    }

    /**
     * Gets a reading plan by type.
     * @param type The type of reading plan
     * @return The reading plan, or null if no plan of that type exists
     */
    fun getPlan(type: ReadingPlanType): ReadingPlan? {
        return activePlans[type]
    }

    /**
     * Starts a new reading plan.
     * @param type The type of reading plan to start
     * @return The newly created reading plan
     */
    fun startPlan(type: ReadingPlanType): ReadingPlan {
        val plan = when (type) {
            ReadingPlanType.BIBLE_IN_A_YEAR -> createBibleInAYearPlan()
            ReadingPlanType.NEW_TESTAMENT_IN_90_DAYS -> createNewTestamentIn90DaysPlan()
            ReadingPlanType.CUSTOM -> createCustomPlan()
        }

        activePlans[type] = plan
        savePlans()
        return plan
    }

    /**
     * Removes a reading plan.
     * @param type The type of reading plan to remove
     */
    fun removePlan(type: ReadingPlanType) {
        activePlans.remove(type)
        savePlans()
    }

    /**
     * Updates an existing reading plan.
     * @param type The type of reading plan to update
     * @param plan The updated reading plan
     */
    fun updatePlan(type: ReadingPlanType, plan: ReadingPlan) {
        activePlans[type] = plan
        savePlans()
    }

    /**
     * Gets all active reading plans.
     * @return A map of all active reading plans
     */
    fun getAllPlans(): Map<ReadingPlanType, ReadingPlan> {
        return activePlans.toMap()
    }

    /**
     * Clears all reading plans.
     */
    fun clearAllPlans() {
        activePlans.clear()
        savePlans()
    }

    /**
     * Sets all reading plans from a list (used for import).
     * @param plans List of reading plans to set
     * @param clearExisting If true, clears existing plans before importing
     */
    fun setAllPlans(plans: List<ReadingPlan>, clearExisting: Boolean = false) {
        if (clearExisting) {
            activePlans.clear()
        }
        plans.forEach { plan ->
            activePlans[plan.type] = plan
        }
        savePlans()
    }

    /**
     * Creates a Bible in a year reading plan.
     * @return A new Bible in a year reading plan
     */
    private fun createBibleInAYearPlan(): ReadingPlan {
        val entries = mutableListOf<ReadingPlanEntry>()

        // Old Testament (Genesis to Malachi) - Books 1-39
        var day = 1
        for (bookId in 1..39) {
            val chapterCount = getChapterCount(bookId)
            var chapter = 1

            while (chapter <= chapterCount) {
                // Read about 3 chapters per day for OT
                val endChapter = minOf(chapter + 2, chapterCount)
                entries.add(ReadingPlanEntry(day, bookId, chapter, endChapter))
                chapter = endChapter + 1
                day++
            }
        }

        // New Testament (Matthew to Revelation) - Books 40-66
        for (bookId in 40..66) {
            val chapterCount = getChapterCount(bookId)
            var chapter = 1

            while (chapter <= chapterCount) {
                // Read about 3 chapters per day for NT
                val endChapter = minOf(chapter + 2, chapterCount)
                entries.add(ReadingPlanEntry(day, bookId, chapter, endChapter))
                chapter = endChapter + 1
                day++
            }
        }

        return ReadingPlan(
            type = ReadingPlanType.BIBLE_IN_A_YEAR,
            name = "Bible in a Year",
            description = "Read through the entire Bible in 365 days",
            entries = entries,
            startDate = LocalDate.now()
        )
    }

    /**
     * Creates a New Testament in 90 days reading plan.
     * @return A new New Testament in 90 days reading plan
     */
    private fun createNewTestamentIn90DaysPlan(): ReadingPlan {
        val entries = mutableListOf<ReadingPlanEntry>()

        // New Testament only (Matthew to Revelation) - Books 40-66
        var day = 1
        for (bookId in 40..66) {
            val chapterCount = getChapterCount(bookId)
            var chapter = 1

            while (chapter <= chapterCount) {
                // Read about 1-2 chapters per day
                val endChapter = minOf(chapter + 1, chapterCount)
                entries.add(ReadingPlanEntry(day, bookId, chapter, endChapter))
                chapter = endChapter + 1
                day++
            }
        }

        return ReadingPlan(
            type = ReadingPlanType.NEW_TESTAMENT_IN_90_DAYS,
            name = "New Testament in 90 Days",
            description = "Read through the New Testament in 90 days",
            entries = entries,
            startDate = LocalDate.now()
        )
    }

    /**
     * Creates a custom reading plan.
     * @return A new custom reading plan
     */
    private fun createCustomPlan(): ReadingPlan {
        // This would be implemented based on user input
        return ReadingPlan(
            type = ReadingPlanType.CUSTOM,
            name = "Custom Plan",
            description = "Custom reading plan",
            entries = emptyList(),
            startDate = LocalDate.now()
        )
    }

    /**
     * Gets the number of chapters in a book.
     * @param bookId The book ID
     * @return The number of chapters in the book
     */
    private fun getChapterCount(bookId: Int): Int {
        // This is a simplified version - in a real implementation, 
        // we would get this data from the Bible data
        return when (bookId) {
            1 -> 50    // Genesis
            2 -> 40    // Exodus
            3 -> 27    // Leviticus
            4 -> 36    // Numbers
            5 -> 34    // Deuteronomy
            6 -> 24    // Joshua
            7 -> 21    // Judges
            8 -> 4     // Ruth
            9 -> 31    // 1 Samuel
            10 -> 24   // 2 Samuel
            11 -> 22   // 1 Kings
            12 -> 25   // 2 Kings
            13 -> 29   // 1 Chronicles
            14 -> 36   // 2 Chronicles
            15 -> 10   // Ezra
            16 -> 13   // Nehemiah
            17 -> 10   // Esther
            18 -> 42   // Job
            19 -> 150  // Psalms
            20 -> 31   // Proverbs
            21 -> 12   // Ecclesiastes
            22 -> 8    // Song of Solomon
            23 -> 66   // Isaiah
            24 -> 52   // Jeremiah
            25 -> 5    // Lamentations
            26 -> 48   // Ezekiel
            27 -> 12   // Daniel
            28 -> 14   // Hosea
            29 -> 3    // Joel
            30 -> 9    // Amos
            31 -> 1    // Obadiah
            32 -> 4    // Jonah
            33 -> 7    // Micah
            34 -> 3    // Nahum
            35 -> 3    // Habakkuk
            36 -> 3    // Zephaniah
            37 -> 2    // Haggai
            38 -> 14   // Zechariah
            39 -> 4    // Malachi
            40 -> 28   // Matthew
            41 -> 16   // Mark
            42 -> 24   // Luke
            43 -> 21   // John
            44 -> 28   // Acts
            45 -> 16   // Romans
            46 -> 16   // 1 Corinthians
            47 -> 13   // 2 Corinthians
            48 -> 6    // Galatians
            49 -> 6    // Ephesians
            50 -> 4    // Philippians
            51 -> 4    // Colossians
            52 -> 5    // 1 Thessalonians
            53 -> 3    // 2 Thessalonians
            54 -> 6    // 1 Timothy
            55 -> 4    // 2 Timothy
            56 -> 3    // Titus
            57 -> 1    // Philemon
            58 -> 13   // Hebrews
            59 -> 5    // James
            60 -> 5    // 1 Peter
            61 -> 3    // 2 Peter
            62 -> 5    // 1 John
            63 -> 1    // 2 John
            64 -> 1    // 3 John
            65 -> 1    // Jude
            66 -> 22   // Revelation
            else -> 1
        }
    }

    /**
     * Loads reading plans from a file.
     */
    private fun loadPlans() {
        if (plansFile.exists()) {
            val properties = Properties()
            FileInputStream(plansFile).use { properties.load(it) }

            val planTypes = properties.getProperty("plan_types", "")
            if (planTypes.isNotBlank()) {
                planTypes.split(",").forEach { typeStr ->
                    try {
                        val type = ReadingPlanType.valueOf(typeStr)
                        val name = properties.getProperty("${typeStr}.name", "")
                        val description = properties.getProperty("${typeStr}.description", "")
                        val startDateStr = properties.getProperty("${typeStr}.start_date", "")
                        val currentDayStr = properties.getProperty("${typeStr}.current_day", "1")
                        val entriesStr = properties.getProperty("${typeStr}.entries", "")

                        val startDate = if (startDateStr.isNotBlank()) {
                            LocalDate.parse(startDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                        } else {
                            LocalDate.now()
                        }

                        val currentDay = currentDayStr.toIntOrNull() ?: 1

                        val entries = if (entriesStr.isNotBlank()) {
                            entriesStr.split(";").mapNotNull { entryStr ->
                                val parts = entryStr.split(":")
                                if (parts.size == 4) {
                                    ReadingPlanEntry(
                                        day = parts[0].toInt(),
                                        bookId = parts[1].toInt(),
                                        chapterStart = parts[2].toInt(),
                                        chapterEnd = parts[3].toInt()
                                    )
                                } else {
                                    null
                                }
                            }
                        } else {
                            emptyList()
                        }

                        val plan = ReadingPlan(
                            type = type,
                            name = name,
                            description = description,
                            entries = entries,
                            startDate = startDate,
                            currentDay = currentDay
                        )

                        activePlans[type] = plan
                    } catch (e: Exception) {
                        // Skip invalid plan
                    }
                }
            }
        }
    }

    /**
     * Saves reading plans to a file.
     */
    private fun savePlans() {
        val properties = Properties()

        // Save plan types
        properties["plan_types"] = activePlans.keys.joinToString(",") { type -> type.name }

        // Save each plan
        activePlans.forEach { (type, plan) ->
            properties["${type.name}.name"] = plan.name
            properties["${type.name}.description"] = plan.description
            properties["${type.name}.start_date"] = plan.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            properties["${type.name}.current_day"] = plan.currentDay.toString()

            // Save entries
            properties["${type.name}.entries"] = plan.entries.joinToString(";") { entry: ReadingPlanEntry ->
                "${entry.day}:${entry.bookId}:${entry.chapterStart}:${entry.chapterEnd}"
            }
        }

        FileOutputStream(plansFile).use { properties.store(it, "Bible Reading Plans") }
    }

    companion object {
        // Singleton instance
        val instance = ReadingPlanManager()
    }
}

/**
 * A class to represent a reading plan.
 */
data class ReadingPlan(
    val type: ReadingPlanType,
    val name: String,
    val description: String,
    val entries: List<ReadingPlanEntry>,
    val startDate: LocalDate,
    val currentDay: Int = 1
) {
    /**
     * Gets the current reading for today.
     * @return The current reading entry, or null if the plan is completed
     */
    fun getCurrentReading(): ReadingPlanEntry? {
        return entries.firstOrNull { it.day == currentDay }
    }

    /**
     * Gets the next reading.
     * @return The next reading entry, or null if the plan is completed
     */
    fun getNextReading(): ReadingPlanEntry? {
        return entries.firstOrNull { it.day == currentDay + 1 }
    }

    /**
     * Advances to the next day in the reading plan.
     * @return A new ReadingPlan with the updated current day
     */
    fun advanceToNextDay(): ReadingPlan {
        val nextDay = minOf(currentDay + 1, entries.maxOfOrNull { it.day } ?: currentDay)
        return copy(currentDay = nextDay)
    }

    /**
     * Checks if the reading plan is completed.
     * @return True if the plan is completed, false otherwise
     */
    fun isCompleted(): Boolean {
        return currentDay > (entries.maxOfOrNull { it.day } ?: 0)
    }

    /**
     * Gets the progress of the reading plan as a percentage.
     * @return The progress as a percentage (0-100)
     */
    fun getProgress(): Int {
        val maxDay = entries.maxOfOrNull { it.day } ?: 1
        return ((currentDay - 1) * 100) / maxDay
    }

    /**
     * Gets the estimated completion date based on the start date and total days.
     * @return The estimated completion date
     */
    fun getEstimatedCompletionDate(): LocalDate {
        val maxDay = entries.maxOfOrNull { it.day } ?: 1
        return startDate.plusDays((maxDay - 1).toLong())
    }

    /**
     * Gets the days remaining in the reading plan.
     * @return The number of days remaining
     */
    fun getDaysRemaining(): Int {
        val maxDay = entries.maxOfOrNull { it.day } ?: 1
        return maxDay - currentDay + 1
    }
}
