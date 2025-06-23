package bibles

import ComboOption
import androidx.compose.runtime.Composable
import locale.L

val bibleList = listOf(
    ComboOption("English Amplified Bible", 0),
    ComboOption("English Chain Reference", 1),
    ComboOption("English KJV", 2),
    ComboOption("English Tyndale 1537", 3),
    ComboOption("English YLT", 4),
    ComboOption("Greek Textus Receptus", 5),
    ComboOption("Spanish RV 2020", 6),
    ComboOption("Torreys Topical Textbook", 7),
    ComboOption("Amharic Bible", 8),
    ComboOption("Cebuano Bible", 9),
    ComboOption("Croatian Bible", 10),
    ComboOption("Czech Bible", 11),
    ComboOption("Danish Bible", 12),
    ComboOption("Dutch Bible", 13),
    ComboOption("Greek Bible", 14),
    ComboOption("Hausa 2013 Bible", 15),
    ComboOption("Hebrew Bible", 16),
    ComboOption("Hungarian Bible", 17),
    ComboOption("Indonesian TL Bible", 18),
    ComboOption("Norwegian Bible", 19),
    ComboOption("Polish Bible", 20),
    ComboOption("Romanian Bible", 21),
    ComboOption("Serbian Bible", 22),
    ComboOption("Swahili Bible", 23),
    ComboOption("Swedish Bible", 24),
    ComboOption("Tagalog Bible", 25),
    ComboOption("Thai Bible", 26),
    ComboOption("Yoruba Bible", 27)
)

/**
 * Returns a localized version of the Bible list.
 * Each Bible name is passed through the localization function.
 */
@Composable
fun getLocalizedBibleList(): List<ComboOption> {
    return bibleList.map { ComboOption(L.current.l(it.text), it.id) }
}

/**
 * Returns a localized version of the book list.
 * Each book name is passed through the localization function.
 */
@Composable
fun getLocalizedBookList(): List<ComboOption> {
    return bookList.map { ComboOption(L.current.l(it.text), it.id) }
}

val bookList = listOf(
    // Old Testament
    ComboOption("Genesis", 1),
    ComboOption("Exodus", 2),
    ComboOption("Leviticus", 3),
    ComboOption("Numbers", 4),
    ComboOption("Deuteronomy", 5),
    ComboOption("Joshua", 6),
    ComboOption("Judges", 7),
    ComboOption("Ruth", 8),
    ComboOption("1 Samuel", 9),
    ComboOption("2 Samuel", 10),
    ComboOption("1 Kings", 11),
    ComboOption("2 Kings", 12),
    ComboOption("1 Chronicles", 13),
    ComboOption("2 Chronicles", 14),
    ComboOption("Ezra", 15),
    ComboOption("Nehemiah", 16),
    ComboOption("Esther", 17),
    ComboOption("Job", 18),
    ComboOption("Psalms", 19),
    ComboOption("Proverbs", 20),
    ComboOption("Ecclesiastes", 21),
    ComboOption("Song of Solomon", 22),
    ComboOption("Isaiah", 23),
    ComboOption("Jeremiah", 24),
    ComboOption("Lamentations", 25),
    ComboOption("Ezekiel", 26),
    ComboOption("Daniel", 27),
    ComboOption("Hosea", 28),
    ComboOption("Joel", 29),
    ComboOption("Amos", 30),
    ComboOption("Obadiah", 31),
    ComboOption("Jonah", 32),
    ComboOption("Micah", 33),
    ComboOption("Nahum", 34),
    ComboOption("Habakkuk", 35),
    ComboOption("Zephaniah", 36),
    ComboOption("Haggai", 37),
    ComboOption("Zechariah", 38),
    ComboOption("Malachi", 39),

    // New Testament
    ComboOption("Matthew", 40),
    ComboOption("Mark", 41),
    ComboOption("Luke", 42),
    ComboOption("John", 43),
    ComboOption("Acts", 44),
    ComboOption("Romans", 45),
    ComboOption("1 Corinthians", 46),
    ComboOption("2 Corinthians", 47),
    ComboOption("Galatians", 48),
    ComboOption("Ephesians", 49),
    ComboOption("Philippians", 50),
    ComboOption("Colossians", 51),
    ComboOption("1 Thessalonians", 52),
    ComboOption("2 Thessalonians", 53),
    ComboOption("1 Timothy", 54),
    ComboOption("2 Timothy", 55),
    ComboOption("Titus", 56),
    ComboOption("Philemon", 57),
    ComboOption("Hebrews", 58),
    ComboOption("James", 59),
    ComboOption("1 Peter", 60),
    ComboOption("2 Peter", 61),
    ComboOption("1 John", 62),
    ComboOption("2 John", 63),
    ComboOption("3 John", 64),
    ComboOption("Jude", 65),
    ComboOption("Revelation", 66)
)
