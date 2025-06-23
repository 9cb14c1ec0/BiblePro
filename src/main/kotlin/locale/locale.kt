package locale

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Import language maps from their respective files
// The language string variables are defined in their respective files
import locale.spanish_strings
import locale.french_strings
import locale.german_strings
import locale.hindi_strings
import locale.italian_strings
import locale.chinese_strings
import locale.arabic_strings
import locale.portuguese_strings
import locale.bengali_strings
import locale.russian_strings
import locale.japanese_strings
import locale.javanese_strings
import locale.telugu_strings
import locale.marathi_strings
import locale.turkish_strings
import locale.tamil_strings
import locale.urdu_strings
import locale.vietnamese_strings
import locale.korean_strings

val local_map: Map<String, Map<String, String>> = mapOf(
    "spanish" to spanish_strings,
    "french" to french_strings,
    "german" to german_strings,
    "hindi" to hindi_strings,
    "italian" to italian_strings,
    "chinese" to chinese_strings,
    "arabic" to arabic_strings,
    "portuguese" to portuguese_strings,
    "bengali" to bengali_strings,
    "russian" to russian_strings,
    "japanese" to japanese_strings,
    "javanese" to javanese_strings,
    "telugu" to telugu_strings,
    "marathi" to marathi_strings,
    "turkish" to turkish_strings,
    "tamil" to tamil_strings,
    "urdu" to urdu_strings,
    "vietnamese" to vietnamese_strings,
    "korean" to korean_strings
);

@Immutable
class L {
    private val _language = mutableStateOf("english")
    var language: String by _language
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

// Deprecated: Use L.current.l() instead
fun l(key: String): String {
    return L.current.l(key)
}
