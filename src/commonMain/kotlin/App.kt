import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import locale.L
import phonetics.PhoneticLanguage
import phonetics.PhoneticSettings
import phonetics.rememberPhoneticSettings
import theme.BibleProTheme
import theme.ThemeMode
import theme.rememberThemeState

@Composable
fun App() {
    // Theme state to manage light/dark mode
    val themeState = rememberThemeState(initialThemeMode = ThemeMode.SYSTEM)

    // Phonetics settings to manage phonetics display
    val phoneticSettings = rememberPhoneticSettings(initialShowPhonetics = false, initialLanguage = PhoneticLanguage.NONE)

    BibleProTheme(themeMode = themeState.themeMode) {
        var m by rememberSaveable { mutableStateOf(1) }
        var search_count by rememberSaveable { mutableStateOf(0) }
        var notes_count by rememberSaveable { mutableStateOf(0) }

        Column(modifier = Modifier.fillMaxSize()) {
            // Main content
            Row(modifier = Modifier.fillMaxSize().weight(1f)) {
                (1..m).forEach {
                    var closed by remember { mutableStateOf(false) }
                    if(!closed)
                    {
                        Column(
                            modifier = Modifier
                                .border(
                                    1.dp, 
                                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                                )
                                .weight(1F)
                                .background(MaterialTheme.colors.background)
                        ) {
                            BiblePane(
                                { m += 1 }, 
                                { closed = true}, 
                                { search_count += 1 }, 
                                { notes_count += 1 },
                                it,  
                                m.toFloat(),
                                themeState = themeState,
                                phoneticSettings = phoneticSettings
                            )
                        }
                    }
                }
                (1 .. search_count).forEach {
                    var closed by remember { mutableStateOf(false) }
                    if(!closed)
                    {
                        Column(
                            modifier = Modifier
                                .border(
                                    1.dp, 
                                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                                )
                                .weight(1F)
                                .background(MaterialTheme.colors.background)
                        ) {
                            SearchPane({ search_count += 1 }, { closed = true}, it, search_count.toFloat())
                        }
                    }
                }
                (1 .. notes_count).forEach {
                    var closed by remember { mutableStateOf(false) }
                    if(!closed)
                    {
                        Column(
                            modifier = Modifier
                                .border(
                                    1.dp, 
                                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                                )
                                .weight(1F)
                                .background(MaterialTheme.colors.background)
                        ) {
                            GlobalNotesView({ closed = true}, it, notes_count.toFloat())
                        }
                    }
                }
            }
        }
    }
}