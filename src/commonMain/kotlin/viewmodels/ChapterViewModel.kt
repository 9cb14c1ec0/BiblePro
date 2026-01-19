package viewmodels

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import bibles.Bible
import bibles.CrossReferenceTracker
import bibles.NoteTracker
import bibles.ReadingTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import phonetics.PhoneticLanguage
import phonetics.PhoneticGeneratorFactory

/**
 * ViewModel for an entire chapter - manages state for all verses efficiently.
 * This replaces per-verse ViewModels for better performance.
 */
class ChapterViewModel {
    private val _state = MutableStateFlow(ChapterState())
    val state: StateFlow<ChapterState> = _state.asStateFlow()

    // Cached regex for Greek text detection
    private val greekRegex = Regex("""[\u0370-\u03FF\u1F00-\u1FFF]""")

    /**
     * Loads a chapter and pre-processes all verse data.
     */
    fun loadChapter(
        book: Int,
        chapter: Int,
        bibles: Map<String, Bible>,
        showPhonetics: Boolean = false,
        phoneticLanguage: PhoneticLanguage = PhoneticLanguage.NONE
    ) {
        // Don't reload if same chapter
        if (_state.value.book == book &&
            _state.value.chapter == chapter &&
            _state.value.bibleNames == bibles.keys) {
            return
        }

        val testament = if (book > 39) "New" else "Old"
        val verseCount = getVerseCount(bibles, testament, book, chapter)

        // Pre-process all verses
        val verseDataMap = mutableMapOf<Int, VerseData>()

        for (verseNum in 1..verseCount) {
            val texts = mutableMapOf<String, String>()
            val processedTexts = mutableMapOf<String, AnnotatedString>()

            for ((bibleName, bible) in bibles) {
                val verseText = getVerseText(bible, testament, book, chapter, verseNum)
                if (verseText != null) {
                    texts[bibleName] = verseText
                    processedTexts[bibleName] = processVerseText(verseText)
                }
            }

            // Generate phonetics if enabled
            val phoneticTexts = if (showPhonetics) {
                generatePhoneticTexts(texts, phoneticLanguage)
            } else {
                emptyMap()
            }

            verseDataMap[verseNum] = VerseData(
                verse = verseNum,
                texts = texts,
                processedTexts = processedTexts,
                phoneticTexts = phoneticTexts,
                isRead = ReadingTracker.instance.isRead(book, chapter, verseNum),
                hasNote = NoteTracker.instance.hasNote(book, chapter, verseNum),
                noteText = NoteTracker.instance.getNote(book, chapter, verseNum),
                crossReferences = CrossReferenceTracker.instance.getCrossReferences(book, chapter, verseNum),
                isHighlighted = false
            )
        }

        _state.update {
            ChapterState(
                book = book,
                chapter = chapter,
                bibleNames = bibles.keys.toSet(),
                bibles = bibles,
                verseCount = verseCount,
                verses = verseDataMap,
                showPhonetics = showPhonetics,
                phoneticLanguage = phoneticLanguage,
                isCompactMode = bibles.size == 1
            )
        }
    }

    /**
     * Marks a verse as read.
     */
    fun markVerseAsRead(verse: Int) {
        val book = _state.value.book
        val chapter = _state.value.chapter

        ReadingTracker.instance.markAsRead(book, chapter, verse)

        _state.update { state ->
            val updatedVerses = state.verses.toMutableMap()
            updatedVerses[verse] = updatedVerses[verse]?.copy(isRead = true) ?: return@update state
            state.copy(verses = updatedVerses)
        }
    }

    /**
     * Toggles highlight for a verse.
     */
    fun toggleHighlight(verse: Int) {
        _state.update { state ->
            val updatedVerses = state.verses.toMutableMap()
            val current = updatedVerses[verse] ?: return@update state
            updatedVerses[verse] = current.copy(isHighlighted = !current.isHighlighted)
            state.copy(verses = updatedVerses)
        }
    }

    /**
     * Saves a note for a verse.
     */
    fun saveNote(verse: Int, noteText: String) {
        val book = _state.value.book
        val chapter = _state.value.chapter

        NoteTracker.instance.setNote(book, chapter, verse, noteText)

        _state.update { state ->
            val updatedVerses = state.verses.toMutableMap()
            updatedVerses[verse] = updatedVerses[verse]?.copy(
                noteText = noteText,
                hasNote = noteText.isNotBlank()
            ) ?: return@update state
            state.copy(verses = updatedVerses)
        }
    }

    /**
     * Adds a cross-reference to a verse.
     */
    fun addCrossReference(verse: Int, reference: String) {
        val book = _state.value.book
        val chapter = _state.value.chapter

        CrossReferenceTracker.instance.addCrossReference(book, chapter, verse, reference)

        _state.update { state ->
            val updatedVerses = state.verses.toMutableMap()
            val current = updatedVerses[verse] ?: return@update state
            val updatedRefs = current.crossReferences.toMutableList()
            if (!updatedRefs.contains(reference)) {
                updatedRefs.add(reference)
            }
            updatedVerses[verse] = current.copy(crossReferences = updatedRefs)
            state.copy(verses = updatedVerses)
        }
    }

    /**
     * Removes a cross-reference from a verse.
     */
    fun removeCrossReference(verse: Int, reference: String) {
        val book = _state.value.book
        val chapter = _state.value.chapter

        CrossReferenceTracker.instance.removeCrossReference(book, chapter, verse, reference)

        _state.update { state ->
            val updatedVerses = state.verses.toMutableMap()
            val current = updatedVerses[verse] ?: return@update state
            val updatedRefs = current.crossReferences.toMutableList()
            updatedRefs.remove(reference)
            updatedVerses[verse] = current.copy(crossReferences = updatedRefs)
            state.copy(verses = updatedVerses)
        }
    }

    /**
     * Sets the selected verse for context menu.
     */
    fun selectVerse(verse: Int?) {
        _state.update { it.copy(selectedVerse = verse) }
    }

    /**
     * Shows/hides the note dialog.
     */
    fun showNoteDialog(verse: Int?) {
        _state.update { it.copy(editingNoteForVerse = verse) }
    }

    /**
     * Updates phonetics settings.
     */
    fun updatePhonetics(showPhonetics: Boolean, language: PhoneticLanguage) {
        if (_state.value.showPhonetics == showPhonetics &&
            _state.value.phoneticLanguage == language) {
            return
        }

        _state.update { state ->
            val updatedVerses = if (showPhonetics) {
                state.verses.mapValues { (_, data) ->
                    data.copy(phoneticTexts = generatePhoneticTexts(data.texts, language))
                }
            } else {
                state.verses.mapValues { (_, data) ->
                    data.copy(phoneticTexts = emptyMap())
                }
            }
            state.copy(
                showPhonetics = showPhonetics,
                phoneticLanguage = language,
                verses = updatedVerses
            )
        }
    }

    // Helper functions

    private fun getVerseCount(bibles: Map<String, Bible>, testament: String, book: Int, chapter: Int): Int {
        val bible = bibles.values.firstOrNull() ?: return 0
        return try {
            bible.testaments
                .first { it.name == testament }
                .books
                .first { it.number == book }
                .chapters
                .first { it.number == chapter }
                .verses
                .size
        } catch (e: Exception) {
            0
        }
    }

    private fun getVerseText(bible: Bible, testament: String, book: Int, chapter: Int, verse: Int): String? {
        return try {
            bible.testaments
                .firstOrNull { it.name == testament }
                ?.books
                ?.firstOrNull { it.number == book }
                ?.chapters
                ?.firstOrNull { it.number == chapter }
                ?.verses
                ?.firstOrNull { it.number == verse }
                ?.text
        } catch (e: Exception) {
            null
        }
    }

    private fun processVerseText(text: String): AnnotatedString {
        var wordIndex = 0
        return buildAnnotatedString {
            text.split(" ").forEach { word ->
                pushStringAnnotation(tag = wordIndex.toString(), annotation = word)
                append(word)
                pop()
                append(" ")
                wordIndex++
            }
        }
    }

    private fun generatePhoneticTexts(
        verseTexts: Map<String, String>,
        language: PhoneticLanguage
    ): Map<String, String> {
        val generator = PhoneticGeneratorFactory.getGenerator(language) ?: return emptyMap()
        val phoneticTexts = mutableMapOf<String, String>()

        for ((bibleName, verseText) in verseTexts) {
            if (language == PhoneticLanguage.SPANISH && bibleName == "Spanish RV 2020") {
                phoneticTexts[bibleName] = generator.generatePhonetics(verseText)
            }
        }

        return phoneticTexts
    }
}

/**
 * Data for a single verse - immutable for performance.
 */
data class VerseData(
    val verse: Int,
    val texts: Map<String, String>,
    val processedTexts: Map<String, AnnotatedString>,
    val phoneticTexts: Map<String, String>,
    val isRead: Boolean,
    val hasNote: Boolean,
    val noteText: String,
    val crossReferences: List<String>,
    val isHighlighted: Boolean
)

/**
 * State for an entire chapter.
 */
data class ChapterState(
    val book: Int = 1,
    val chapter: Int = 1,
    val bibleNames: Set<String> = emptySet(),
    val bibles: Map<String, Bible> = emptyMap(),
    val verseCount: Int = 0,
    val verses: Map<Int, VerseData> = emptyMap(),
    val selectedVerse: Int? = null,
    val editingNoteForVerse: Int? = null,
    val showPhonetics: Boolean = false,
    val phoneticLanguage: PhoneticLanguage = PhoneticLanguage.NONE,
    val isCompactMode: Boolean = true
)