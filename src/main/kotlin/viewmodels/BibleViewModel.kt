package viewmodels

import androidx.compose.runtime.mutableStateOf
import bibles.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the Bible pane component.
 * Handles loading Bibles, managing book and chapter selection, and providing verse data.
 */
class BibleViewModel {
    // State for the Bible pane
    private val _state = MutableStateFlow(BibleState())
    val state: StateFlow<BibleState> = _state.asStateFlow()

    // Cache for loaded Bibles
    private val loadedBibles = mutableMapOf<String, Bible>()

    init {
        // Load KJV Bible by default for book and chapter information
        if (!loadedBibles.containsKey("English KJV")) {
            loadedBibles["English KJV"] = BibleXmlParser().parseFromResource("English KJV")
        }
    }

    /**
     * Loads the selected Bibles.
     * @param selectedBibles List of Bible names to load
     */
    fun loadBibles(selectedBibles: List<String>) {
        val bibles = mutableMapOf<String, Bible>()
        
        selectedBibles.forEach { bibleName ->
            if (!loadedBibles.containsKey(bibleName)) {
                loadedBibles[bibleName] = BibleXmlParser().parseFromResource(bibleName)
            }
            bibles[bibleName] = loadedBibles[bibleName]!!
        }
        
        // Ensure KJV is loaded for reference
        if (!loadedBibles.containsKey("English KJV")) {
            loadedBibles["English KJV"] = BibleXmlParser().parseFromResource("English KJV")
        }
        
        _state.update { currentState ->
            currentState.copy(
                bibles = bibles,
                bibleCount = bibles.size
            )
        }
    }

    /**
     * Sets the selected book.
     * @param bookId The book ID to select
     */
    fun selectBook(bookId: Int) {
        val testament = if (bookId > 39) "New" else "Old"
        val chapters = mutableListOf<String>()
        
        if (loadedBibles.containsKey("English KJV")) {
            val book = loadedBibles["English KJV"]!!.testaments
                .first { it.name == testament }
                .books
                .first { it.number == bookId }
            
            book.chapters.forEach { chapter ->
                chapters.add(chapter.number.toString())
            }
        }
        
        _state.update { currentState ->
            currentState.copy(
                bookId = bookId,
                chapters = chapters,
                chapterNum = 1 // Reset to first chapter when book changes
            )
        }
    }

    /**
     * Sets the selected chapter.
     * @param chapterNum The chapter number to select
     */
    fun selectChapter(chapterNum: Int) {
        _state.update { currentState ->
            currentState.copy(
                chapterNum = chapterNum
            )
        }
    }

    /**
     * Gets the lexicon entry for a Strong's number.
     * @param bookId The book ID
     * @param chapterNum The chapter number
     * @param verseNum The verse number
     * @param wordIndex The index of the word in the verse
     * @return The lexicon entry text
     */
    fun getLexiconEntry(bookId: Int, chapterNum: Int, verseNum: Int, wordIndex: Int): String {
        val lexicon = LexiconLoad().loadLexicon()
        val strongsMapping = StrongsLoad().loadStrongsMapping()
        
        try {
            val strongsVerse = strongsMapping.first { 
                it.book == (bookId - 39) && 
                it.chapter == chapterNum && 
                it.verse == verseNum 
            }
            
            val strongs = strongsVerse.words[wordIndex]
            val formattedStrongs = "%04d".format(strongs.toInt())
            val lexiconKey = lexicon.entries.filter { it.value.strong == "g$formattedStrongs" }.keys.firstOrNull()
            
            return if (lexiconKey != null) {
                "Strongs: ${lexicon.entries[lexiconKey]!!.definition}"
            } else {
                ""
            }
        } catch (e: Exception) {
            return ""
        }
    }
}

/**
 * Data class representing the state of the Bible pane.
 */
data class BibleState(
    val bookId: Int = 40, // Default to Matthew
    val chapterNum: Int = 1,
    val bibles: Map<String, Bible> = emptyMap(),
    val bibleCount: Int = 0,
    val chapters: List<String> = emptyList(),
    val lexiconText: String = ""
)