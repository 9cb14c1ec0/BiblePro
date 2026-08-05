package session

import storage.createPlatformStorage

/**
 * The kinds of panes the main layout can hold.
 */
enum class PaneKind {
    BIBLE, SEARCH, NOTES
}

/**
 * A snapshot of a single open pane, small enough to be written to a properties file.
 */
data class PaneSession(
    val kind: PaneKind,
    val bibleIds: List<Int> = emptyList(),
    val bookId: Int = 40,
    val chapterNum: Int = 1,
    val searchText: String = ""
)

/**
 * A snapshot of the whole application session: which panes are open and the
 * app-wide display settings.
 */
data class AppSession(
    val panes: List<PaneSession> = emptyList(),
    val themeMode: String? = null,
    val showPhonetics: Boolean = false,
    val phoneticLanguage: String? = null
)

/**
 * Persists the set of open panes (and display settings) so the application comes
 * back up exactly as the user left it.
 */
object SessionPreferences {
    private const val SESSION_FILE = "session.properties"

    private const val PANE_COUNT_KEY = "pane_count"
    private const val THEME_MODE_KEY = "theme_mode"
    private const val SHOW_PHONETICS_KEY = "show_phonetics"
    private const val PHONETIC_LANGUAGE_KEY = "phonetic_language"

    private val storage = createPlatformStorage()

    /**
     * Loads the previously saved session.
     * @return The saved session, or an empty session if nothing was saved
     */
    fun load(): AppSession {
        val properties = storage.loadProperties(SESSION_FILE)
        if (properties.isEmpty()) return AppSession()

        val count = properties[PANE_COUNT_KEY]?.toIntOrNull() ?: 0
        val panes = (0 until count).mapNotNull { index ->
            val kind = properties["pane_${index}_kind"]
                ?.let { name -> PaneKind.values().firstOrNull { it.name == name } }
                ?: return@mapNotNull null

            PaneSession(
                kind = kind,
                bibleIds = properties["pane_${index}_bibles"]
                    ?.split(",")
                    ?.mapNotNull { it.trim().toIntOrNull() }
                    ?: emptyList(),
                bookId = properties["pane_${index}_book"]?.toIntOrNull() ?: 40,
                chapterNum = properties["pane_${index}_chapter"]?.toIntOrNull() ?: 1,
                searchText = properties["pane_${index}_search"] ?: ""
            )
        }

        return AppSession(
            panes = panes,
            themeMode = properties[THEME_MODE_KEY],
            showPhonetics = properties[SHOW_PHONETICS_KEY]?.toBoolean() ?: false,
            phoneticLanguage = properties[PHONETIC_LANGUAGE_KEY]
        )
    }

    /**
     * Saves the current session.
     * @param session The session to persist
     */
    fun save(session: AppSession) {
        val properties = mutableMapOf<String, String>()
        properties[PANE_COUNT_KEY] = session.panes.size.toString()

        session.panes.forEachIndexed { index, pane ->
            properties["pane_${index}_kind"] = pane.kind.name
            properties["pane_${index}_bibles"] = pane.bibleIds.joinToString(",")
            properties["pane_${index}_book"] = pane.bookId.toString()
            properties["pane_${index}_chapter"] = pane.chapterNum.toString()
            properties["pane_${index}_search"] = pane.searchText
        }

        session.themeMode?.let { properties[THEME_MODE_KEY] = it }
        properties[SHOW_PHONETICS_KEY] = session.showPhonetics.toString()
        session.phoneticLanguage?.let { properties[PHONETIC_LANGUAGE_KEY] = it }

        storage.saveProperties(SESSION_FILE, properties)
    }

    /**
     * Removes the saved session.
     */
    fun clear() {
        storage.deleteFile(SESSION_FILE)
    }
}
