package phonetics

/**
 * Interface for generating phonetics for different languages
 */
interface PhoneticGenerator {
    /**
     * Generate phonetic representation of text
     * @param text The text to generate phonetics for
     * @return The phonetic representation of the text
     */
    fun generatePhonetics(text: String): String
}

/**
 * Factory for creating PhoneticGenerator instances
 */
object PhoneticGeneratorFactory {
    /**
     * Get a PhoneticGenerator for the specified language
     * @param language The language to get a generator for
     * @return A PhoneticGenerator for the specified language, or null if not supported
     */
    fun getGenerator(language: PhoneticLanguage): PhoneticGenerator? {
        return when (language) {
            PhoneticLanguage.SPANISH -> SpanishPhoneticGenerator()
            PhoneticLanguage.NONE -> null
            // Add more languages here in the future
        }
    }
}

/**
 * PhoneticGenerator implementation for Spanish
 */
class SpanishPhoneticGenerator : PhoneticGenerator {
    override fun generatePhonetics(text: String): String {
        // Basic Spanish pronunciation rules
        var result = text.lowercase()
        
        // Replace Spanish characters with their phonetic equivalents
        val replacements = mapOf(
            "á" to "AH",
            "é" to "EH",
            "í" to "EE",
            "ó" to "OH",
            "ú" to "OO",
            "ü" to "OO",
            "ñ" to "NY",
            "ll" to "Y",
            "j" to "H",
            "h" to "", // silent in Spanish
            "qu" to "k",
            "z" to "s", // Latin American pronunciation
            "ce" to "se",
            "ci" to "si",
            "ge" to "he",
            "gi" to "hi",
            "gue" to "geh",
            "gui" to "gee",
            "güe" to "gooeh",
            "güi" to "gooee",
            "ch" to "ch",
            "rr" to "rr", // rolled r
            "v" to "b"  // often pronounced similarly in Spanish
        )
        
        // Apply replacements
        for ((spanish, phonetic) in replacements) {
            result = result.replace(spanish, phonetic)
        }
        
        return result
    }
}