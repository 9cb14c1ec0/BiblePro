package locale

import storage.createPlatformStorage

/**
 * Manages language preferences for the application.
 * Handles saving and loading the user's preferred language setting.
 */
object LanguagePreferences {
    private const val PREFERENCES_FILE = "language_preferences.properties"
    private const val LANGUAGE_KEY = "preferred_language"
    
    private val storage = createPlatformStorage()
    
    /**
     * Gets the user's preferred language.
     * @return The preferred language code, or null if not set
     */
    fun getPreferredLanguage(): String? {
        val properties = storage.loadProperties(PREFERENCES_FILE)
        return properties[LANGUAGE_KEY]
    }
    
    /**
     * Sets the user's preferred language.
     * @param language The language code to set as preferred
     */
    fun setPreferredLanguage(language: String) {
        val properties = mapOf(LANGUAGE_KEY to language)
        storage.saveProperties(PREFERENCES_FILE, properties)
    }
    
    /**
     * Checks if a preferred language has been set.
     * @return True if a preferred language exists, false otherwise
     */
    fun hasPreferredLanguage(): Boolean {
        return getPreferredLanguage() != null
    }
    
    /**
     * Gets all available languages with their display names.
     * @return Map of language codes to display names
     */
    fun getAvailableLanguages(): Map<String, String> = mapOf(
        "english" to "English",
        "spanish" to "Español",
        "french" to "Français",
        "german" to "Deutsch",
        "hindi" to "हिन्दी",
        "italian" to "Italiano",
        "chinese" to "中文",
        "arabic" to "العربية",
        "portuguese" to "Português",
        "bengali" to "বাংলা",
        "russian" to "Русский",
        "japanese" to "日本語",
        "javanese" to "Basa Jawa",
        "telugu" to "తెలుగు",
        "marathi" to "मराठी",
        "turkish" to "Türkçe",
        "tamil" to "தமிழ்",
        "urdu" to "اردو",
        "vietnamese" to "Tiếng Việt",
        "korean" to "한국어",
        "indonesian" to "Bahasa Indonesia",
        "swahili" to "Kiswahili",
        "dutch" to "Nederlands",
        "polish" to "Polski",
        "romanian" to "Română",
        "greek" to "Ελληνικά",
        "hebrew" to "עברית",
        "tagalog" to "Tagalog",
        "amharic" to "አማርኛ",
        "thai" to "ไทย",
        "cebuano" to "Cebuano",
        "hausa" to "Hausa",
        "yoruba" to "Yorùbá",
        "norwegian" to "Norsk",
        "swedish" to "Svenska",
        "danish" to "Dansk",
        "czech" to "Čeština",
        "hungarian" to "Magyar",
        "croatian" to "Hrvatski",
        "serbian" to "Српски"
    )
}