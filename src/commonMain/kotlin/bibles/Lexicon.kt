package bibles

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import resources.ResourceLoader

@Serializable
data class LexiconEntry(
    val strong: String,
    val part_of_speech: String,
    val transliteration: String,
    val phonetic: String,
    val definition: String,
    val usage: String,
    val origin: String
)

@Serializable
data class LexiconDictionary(
    val entries: Map<String, LexiconEntry>
)


data class StrongsMappingVerse(
    val book: Int,
    val chapter: Int,
    val verse: Int,
    val words: List<String>
)

class LexiconLoad {
    fun loadLexicon(): LexiconDictionary {
        val inputStream = ResourceLoader.loadResourceWithExtension("lexicon", "json")
        val dict = Json.decodeFromString<Map<String, LexiconEntry>>(inputStream.readAllBytes().decodeToString())
        return LexiconDictionary(entries = dict)
    }
}

class StrongsLoad {
    fun loadStrongsMapping(): List<StrongsMappingVerse> {
        val l = mutableListOf<StrongsMappingVerse>()
        val inputStream = ResourceLoader.loadResourceWithExtension("strongs_mapping", "txt")
        inputStream.readAllBytes().decodeToString().split("\n").filter { it.length > 5 }.forEach {
            val words = it.split(" ")
            val ref = words[0].split('.')
            val numbers = words.drop(1)
                .chunked(3).filter { it.size == 3 }
                .map {l -> l[1] }
            l.add(StrongsMappingVerse(ref[0].toInt(), ref[1].toInt(), ref[2].toInt(), numbers))
        }
        return l
    }
}

/**
 * Singleton cache for lexicon data to avoid reloading on every word click.
 * Provides efficient O(1) lookups for Strong's numbers and verse mappings.
 */
object LexiconCache {
    private var lexiconData: LexiconDictionary? = null
    private var strongsMappingData: List<StrongsMappingVerse>? = null
    
    // Efficient lookup maps - built once, used many times
    private var strongsToLexiconMap: Map<String, LexiconEntry>? = null
    private var verseToStrongsMap: Map<String, StrongsMappingVerse>? = null
    
    /**
     * Gets the lexicon dictionary, loading it once and caching for subsequent calls
     */
    fun getLexicon(): LexiconDictionary {
        if (lexiconData == null) {
            lexiconData = LexiconLoad().loadLexicon()
            buildLexiconIndex()
        }
        return lexiconData!!
    }
    
    /**
     * Gets the Strong's mapping data, loading it once and caching for subsequent calls
     */
    fun getStrongsMapping(): List<StrongsMappingVerse> {
        if (strongsMappingData == null) {
            strongsMappingData = StrongsLoad().loadStrongsMapping()
            buildStrongsIndex()
        }
        return strongsMappingData!!
    }
    
    /**
     * Fast O(1) lookup for lexicon entry by Strong's number
     */
    fun getLexiconEntryByStrongs(strongsNumber: String): LexiconEntry? {
        if (strongsToLexiconMap == null) {
            getLexicon() // This will build the index
        }
        return strongsToLexiconMap?.get(strongsNumber)
    }
    
    /**
     * Fast O(1) lookup for verse Strong's mapping
     */
    fun getStrongsMappingForVerse(book: Int, chapter: Int, verse: Int): StrongsMappingVerse? {
        if (verseToStrongsMap == null) {
            getStrongsMapping() // This will build the index
        }
        val key = "$book.$chapter.$verse"
        return verseToStrongsMap?.get(key)
    }
    
    /**
     * Build efficient lookup index for lexicon entries by Strong's number
     */
    private fun buildLexiconIndex() {
        val lexicon = lexiconData ?: return
        strongsToLexiconMap = lexicon.entries.values.associateBy { it.strong }
    }
    
    /**
     * Build efficient lookup index for Strong's mappings by verse reference
     */
    private fun buildStrongsIndex() {
        val mappings = strongsMappingData ?: return
        verseToStrongsMap = mappings.associateBy { "${it.book}.${it.chapter}.${it.verse}" }
    }
    
    /**
     * Preload all lexicon data in background (call during app startup)
     */
    fun preloadData() {
        getLexicon()
        getStrongsMapping()
    }
}
