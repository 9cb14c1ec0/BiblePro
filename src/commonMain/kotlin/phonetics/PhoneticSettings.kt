package phonetics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver

/**
 * Enum class representing supported languages for phonetics
 */
enum class PhoneticLanguage {
    SPANISH,
    // Add more languages here in the future
    NONE
}

/**
 * Class to manage phonetic display settings
 */
class PhoneticSettings(initialShowPhonetics: Boolean = false, initialLanguage: PhoneticLanguage = PhoneticLanguage.NONE) {
    // Whether to show phonetics
    var showPhonetics by mutableStateOf(initialShowPhonetics)
        private set

    // Current language for phonetics
    var language by mutableStateOf(initialLanguage)
        private set

    /**
     * Toggle phonetics display on/off
     */
    fun togglePhonetics() {
        showPhonetics = !showPhonetics
    }

    /**
     * Change the language for phonetics
     * @param language The language to set
     */
    fun changeLanguage(language: PhoneticLanguage) {
        this.language = language
    }
}

// Saver for PhoneticSettings to handle configuration changes
private val PhoneticSettingsSaver = listSaver<PhoneticSettings, Any>(
    save = { listOf(it.showPhonetics, it.language.name) },
    restore = { 
        PhoneticSettings(it[0] as Boolean, PhoneticLanguage.valueOf(it[1] as String))
    }
)

/**
 * Composable function to create and remember a PhoneticSettings instance
 */
@Composable
fun rememberPhoneticSettings(
    initialShowPhonetics: Boolean = false,
    initialLanguage: PhoneticLanguage = PhoneticLanguage.NONE
): PhoneticSettings {
    return rememberSaveable(saver = PhoneticSettingsSaver) { 
        PhoneticSettings(initialShowPhonetics, initialLanguage) 
    }
}
