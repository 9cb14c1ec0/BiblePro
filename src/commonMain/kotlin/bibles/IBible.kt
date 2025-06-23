package bibles

import org.w3c.dom.Element
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import phonetics.PhoneticLanguage
import resources.ResourceLoader

// Data classes to represent the Bible structure
data class Bible(
    var translation: String,
    val testaments: List<Testament>
)

data class Testament(
    val name: String,
    val books: List<Book>
)

data class Book(
    val number: Int,
    val chapters: List<Chapter>
)

data class Chapter(
    val number: Int,
    val verses: List<Verse>
)

data class Verse(
    val number: Int,
    val text: String,
    var phonetics: Map<PhoneticLanguage, String> = emptyMap()
)

var loaded_bibles: MutableMap<String, Bible> = mutableMapOf()

class BibleXmlParser {
    public fun parseFromResource(resourcePath: String): Bible {
        // Get resource as stream
        if(loaded_bibles.containsKey(resourcePath))
        {
            return loaded_bibles[resourcePath]!!
        }
        val inputStream = ResourceLoader.loadResource(resourcePath)
        loaded_bibles[resourcePath] = parseFromStream(inputStream)
        loaded_bibles[resourcePath]!!.translation = resourcePath
        return loaded_bibles[resourcePath]!!
    }

    fun parseFromStream(inputStream: InputStream): Bible {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(inputStream)

        val bibleElement = document.documentElement

        return Bible(
            translation = bibleElement.getAttribute("translation"),
            testaments = bibleElement.getElementsByTagName("testament").toElementList().map { parseTestament(it) }
        )
    }

    // Rest of parsing methods remain the same
    private fun parseTestament(element: Element): Testament {
        return Testament(
            name = element.getAttribute("name"),
            books = element.getElementsByTagName("book").toElementList().map { parseBook(it) }
        )
    }

    private fun parseBook(element: Element): Book {
        return Book(
            number = element.getAttribute("number").toInt(),
            chapters = element.getElementsByTagName("chapter").toElementList().map { parseChapter(it) }
        )
    }

    private fun parseChapter(element: Element): Chapter {
        return Chapter(
            number = element.getAttribute("number").toInt(),
            verses = element.getElementsByTagName("verse").toElementList().map { parseVerse(it) }
        )
    }

    private fun parseVerse(element: Element): Verse {
        return Verse(
            number = element.getAttribute("number").toInt(),
            text = element.textContent
        )
    }

    private fun org.w3c.dom.NodeList.toElementList(): List<Element> {
        return (0 until length).map { item(it) as Element }
    }

    companion object {
        // Singleton instance if needed
        private var instance: Bible? = null

        fun getDefaultBible(): Bible {
            return instance ?: BibleXmlParser().parseFromResource("/EnglishKJBible.xml").also {
                instance = it
            }
        }
    }
}
