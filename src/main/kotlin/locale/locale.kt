package locale

import androidx.compose.runtime.Immutable

val spanish_strings: Map<String, String> = mapOf(
    "Bible Expert" to "Experto en Biblia",
    "Bibles" to "Biblias",
    "Book" to "Libro",
    "English Amplified Bible" to "Biblia Amplificada en Inglés",
    "English Chain Reference" to "Referencia en Cadena en Inglés",
    "English KJV" to "KJV en Inglés",
    "English Tyndale 1537" to "Tyndale 1537 en Inglés",
    "English YLT" to "YLT en Inglés",
    "Greek Textus Receptus" to "Textus Receptus en Griego",
    "Spanish RV 2020" to "RV 2020 en Español",
);

val french_strings: Map<String, String> = mapOf(
    "Bibles" to "Bibles",
    "Bible Expert" to "Expert en Bible",
    "Book" to "Livre",
    "English Amplified Bible" to "Bible Amplifiée en Anglais",
    "English Chain Reference" to "Référence en Chaîne en Anglais",
    "English KJV" to "KJV en Anglais",
    "English Tyndale 1537" to "Tyndale 1537 en Anglais",
    "English YLT" to "YLT en Anglais",
    "Greek Textus Receptus" to "Textus Receptus en Grec",
    "Spanish RV 2020" to "RV 2020 en Espagnol",
)

val german_strings: Map<String, String> = mapOf(
    "Bibles" to "Bibeln",
    "Bible Expert" to "Bibel-Experte",
    "Book" to "Buch",
    "English Amplified Bible" to "Englische Amplified Bible",
    "English Chain Reference" to "Englische Chain Reference",
    "English KJV" to "Englische KJV",
    "English Tyndale 1537" to "Englische Tyndale 1537",
    "English YLT" to "Englische YLT",
    "Greek Textus Receptus" to "Griechische Textus Receptus",
    "Spanish RV 2020" to "Spanische RV 2020",
)

val hindi_strings: Map<String, String> = mapOf(
    "Bibles" to "बाइबल",
    "Bible Expert" to "बाइबल विशेषज्ञ",
    "Book" to "पुस्तक",
    "English Amplified Bible" to "अंग्रेजी एम्प्लीफाइड बाइबिल",
    "English Chain Reference" to "अंग्रेजी चेन रेफरेंस",
    "English KJV" to "अंग्रेजी केजेवी",
    "English Tyndale 1537" to "अंग्रेजी टिंडेल 1537",
    "English YLT" to "अंग्रेजी वाईएलटी",
    "Greek Textus Receptus" to "ग्रीक टेक्स्टस रेसेप्टस",
    "Spanish RV 2020" to "स्पेनिश आरवी 2020",
)

val local_map: Map<String, Map<String, String>> = mapOf(
    "spanish" to spanish_strings,
    "french" to french_strings,
    "german" to german_strings
);

@Immutable
class L {
    var language: String = "english"
    companion object {
        val current = L()
    }

    fun l(key: String): String {
        if(language == "english")
        {
            return key
        }
        return local_map[language]?.get(key) ?: key
    }

    fun reverse(key: String): String {
        if(language == "english")
        {
            return key
        }
        return local_map[language]?.filter { it.value == key }?.keys?.first() ?: key
    }
}

fun l(key: String): String {
    return spanish_strings[key] ?: key
}