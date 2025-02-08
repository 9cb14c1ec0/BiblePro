package bibles

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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
        val inputStream = javaClass.getResourceAsStream("/" + "lexicon.json")
        val dict =  Json.decodeFromString<Map<String, LexiconEntry>>(inputStream!!.readAllBytes().decodeToString())
        return LexiconDictionary(entries = dict)
    }
}

class StrongsLoad {
    fun loadStrongsMapping(): List<StrongsMappingVerse> {
        val l = mutableListOf<StrongsMappingVerse>()
        val inputStream = javaClass.getResourceAsStream("/strongs_mapping.txt")
        inputStream!!.readAllBytes().decodeToString().split("\n").filter { it.length > 5 }.forEach {
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
