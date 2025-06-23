package bibles

import androidx.compose.runtime.mutableStateMapOf
import storage.DesktopPlatformStorage
import storage.PlatformStorage
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * A class to track which verses have been read.
 * It stores the reading status in a map where the key is a string in the format "book:chapter:verse"
 * and the value is a boolean indicating whether the verse has been read.
 * It also provides functionality for reading plans and statistics.
 */
class ReadingTracker {
    // Map to store the reading status of verses
    private val readVerses = mutableStateMapOf<String, Boolean>()

    // Map to store reading timestamps
    private val readTimestamps = mutableStateMapOf<String, String>()

    // Platform storage for reading data
    private val storage: PlatformStorage = DesktopPlatformStorage()
    private val readingFilename = "reading.properties"
    private val statsFilename = "reading_stats.properties"

    init {
        // Load reading status from file if it exists
        loadReadingStatus()
    }

    /**
     * Marks a verse as read.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     */
    fun markAsRead(book: Int, chapter: Int, verse: Int) {
        val key = "$book:$chapter:$verse"
        readVerses[key] = true
        readTimestamps[key] = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        // Check if this reading completes a reading plan entry
        checkReadingPlanProgress(book, chapter)

        saveReadingStatus()
    }

    /**
     * Checks if a verse has been read.
     * @param book The book number
     * @param chapter The chapter number
     * @param verse The verse number
     * @return True if the verse has been read, false otherwise
     */
    fun isRead(book: Int, chapter: Int, verse: Int): Boolean {
        val key = "$book:$chapter:$verse"
        return readVerses[key] ?: false
    }

    /**
     * Resets all reading status.
     */
    fun resetReadingStatus() {
        readVerses.clear()
        readTimestamps.clear()
        saveReadingStatus()
    }

    /**
     * Gets a map of read books and chapters.
     * @return A map where the key is the book number and the value is a list of read chapter numbers
     */
    fun getReadSections(): Map<Int, List<Int>> {
        val result = mutableMapOf<Int, MutableList<Int>>()

        readVerses.keys.forEach { key ->
            val parts = key.split(":")
            val book = parts[0].toInt()
            val chapter = parts[1].toInt()

            if (!result.containsKey(book)) {
                result[book] = mutableListOf()
            }

            if (!result[book]!!.contains(chapter)) {
                result[book]!!.add(chapter)
            }
        }

        return result
    }

    /**
     * Checks if a chapter has been completely read.
     * @param book The book number
     * @param chapter The chapter number
     * @return True if all verses in the chapter have been read, false otherwise
     */
    fun isChapterRead(book: Int, chapter: Int): Boolean {
        // This is a simplified implementation - in a real implementation,
        // we would check if all verses in the chapter have been read
        // For now, we'll consider a chapter read if at least one verse has been read
        return getReadSections()[book]?.contains(chapter) ?: false
    }

    /**
     * Gets reading statistics.
     * @return A ReadingStatistics object containing reading statistics
     */
    fun getStatistics(): ReadingStatistics {
        val readSections = getReadSections()
        val totalChaptersRead = readSections.values.sumOf { it.size }
        val totalBooksStarted = readSections.size
        val totalBooksCompleted = readSections.count { (bookId, chapters) ->
            chapters.size == getChapterCount(bookId)
        }

        // Calculate reading streak
        val streak = calculateReadingStreak()

        // Calculate average chapters per day
        val chaptersPerDay = calculateAverageChaptersPerDay()

        return ReadingStatistics(
            totalChaptersRead = totalChaptersRead,
            totalBooksStarted = totalBooksStarted,
            totalBooksCompleted = totalBooksCompleted,
            currentStreak = streak,
            averageChaptersPerDay = chaptersPerDay
        )
    }

    /**
     * Calculates the current reading streak (consecutive days with reading).
     * @return The number of consecutive days with reading
     */
    private fun calculateReadingStreak(): Int {
        if (readTimestamps.isEmpty()) return 0

        val dates = readTimestamps.values.mapNotNull { dateStr ->
            try {
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                null
            }
        }.distinct().sorted()

        if (dates.isEmpty()) return 0

        // Check if read today
        val today = LocalDate.now()
        val lastReadDate = dates.last()
        if (lastReadDate.isBefore(today.minusDays(1))) {
            // Streak broken - last read was before yesterday
            return 0
        }

        // Count consecutive days
        var streak = 1
        var currentDate = if (lastReadDate == today) today.minusDays(1) else lastReadDate.minusDays(1)

        while (dates.contains(currentDate)) {
            streak++
            currentDate = currentDate.minusDays(1)
        }

        return streak
    }

    /**
     * Calculates the average number of chapters read per day.
     * @return The average number of chapters read per day
     */
    private fun calculateAverageChaptersPerDay(): Double {
        if (readTimestamps.isEmpty()) return 0.0

        val chaptersByDate = mutableMapOf<LocalDate, MutableSet<String>>()

        readTimestamps.forEach { (verseKey, dateStr) ->
            try {
                val date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                val parts = verseKey.split(":")
                val chapterKey = "${parts[0]}:${parts[1]}" // book:chapter

                if (!chaptersByDate.containsKey(date)) {
                    chaptersByDate[date] = mutableSetOf()
                }

                chaptersByDate[date]!!.add(chapterKey)
            } catch (e: Exception) {
                // Skip invalid date
            }
        }

        val totalDays = chaptersByDate.size
        val totalChapters = chaptersByDate.values.sumOf { it.size }

        return if (totalDays > 0) totalChapters.toDouble() / totalDays else 0.0
    }

    /**
     * Checks if the given reading completes a reading plan entry and advances the plan if needed.
     * @param book The book number
     * @param chapter The chapter number
     */
    private fun checkReadingPlanProgress(book: Int, chapter: Int) {
        val planManager = ReadingPlanManager.instance

        planManager.getAllPlans().forEach { (type, plan) ->
            val currentReading = plan.getCurrentReading() ?: return@forEach

            if (currentReading.bookId == book && 
                chapter >= currentReading.chapterStart && 
                chapter <= currentReading.chapterEnd) {

                // Check if all chapters in the current reading have been read
                var allRead = true
                for (c in currentReading.chapterStart..currentReading.chapterEnd) {
                    if (!isChapterRead(currentReading.bookId, c)) {
                        allRead = false
                        break
                    }
                }

                if (allRead) {
                    // Advance to the next day in the reading plan
                    val updatedPlan = plan.advanceToNextDay()
                    planManager.updatePlan(type, updatedPlan)
                }
            }
        }
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
     * Loads the reading status from storage.
     */
    private fun loadReadingStatus() {
        val properties = storage.loadProperties(readingFilename)
        
        properties.forEach { (key, value) ->
            if (key.contains(":")) {
                // Verse reading status
                if (value == "true") {
                    readVerses[key] = true
                } else if (value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    // It's a timestamp
                    readTimestamps[key] = value
                    readVerses[key] = true
                }
            }
        }
    }

    /**
     * Saves the reading status to storage.
     */
    private fun saveReadingStatus() {
        val properties = mutableMapOf<String, String>()

        // Save verse reading status with timestamps
        readVerses.forEach { (key, value) ->
            val timestamp = readTimestamps[key] ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            properties[key] = timestamp
        }

        storage.saveProperties(readingFilename, properties)
    }

    companion object {
        // Singleton instance
        val instance = ReadingTracker()
    }
}

/**
 * Data class representing reading statistics.
 */
data class ReadingStatistics(
    val totalChaptersRead: Int = 0,
    val totalBooksStarted: Int = 0,
    val totalBooksCompleted: Int = 0,
    val currentStreak: Int = 0,
    val averageChaptersPerDay: Double = 0.0
)
