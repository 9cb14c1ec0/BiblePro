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
import locale.indonesian_strings
import locale.swahili_strings
import locale.dutch_strings
import locale.polish_strings
import locale.romanian_strings
import locale.greek_strings
import locale.hebrew_strings
import locale.tagalog_strings
import locale.amharic_strings
import locale.thai_strings
import locale.cebuano_strings
import locale.hausa_strings
import locale.yoruba_strings
import locale.norwegian_strings
import locale.swedish_strings
import locale.danish_strings
import locale.czech_strings
import locale.hungarian_strings
import locale.croatian_strings
import locale.serbian_strings

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
    "korean" to korean_strings,
    "indonesian" to indonesian_strings,
    "swahili" to swahili_strings,
    "dutch" to dutch_strings,
    "polish" to polish_strings,
    "romanian" to romanian_strings,
    "greek" to greek_strings,
    "hebrew" to hebrew_strings,
    "tagalog" to tagalog_strings,
    "amharic" to amharic_strings,
    "thai" to thai_strings,
    "cebuano" to cebuano_strings,
    "hausa" to hausa_strings,
    "yoruba" to yoruba_strings,
    "norwegian" to norwegian_strings,
    "swedish" to swedish_strings,
    "danish" to danish_strings,
    "czech" to czech_strings,
    "hungarian" to hungarian_strings,
    "croatian" to croatian_strings,
    "serbian" to serbian_strings
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
