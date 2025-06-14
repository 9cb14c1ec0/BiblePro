import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Note
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import bibles.*
import bibles.bookList
import nl.marc_apps.tts.TextToSpeechEngine
import nl.marc_apps.tts.experimental.ExperimentalDesktopTarget
import nl.marc_apps.tts.rememberTextToSpeechOrNull
import theme.BibleProTheme
import theme.ThemeMode
import theme.ThemeToggle
import theme.rememberThemeState
import viewmodels.GlobalNotesViewModel
import GlobalNotesView



@Composable
@Preview
fun App() {
    // Theme state to manage light/dark mode
    val themeState = rememberThemeState(initialThemeMode = ThemeMode.SYSTEM)

    BibleProTheme(themeMode = themeState.themeMode) {
        var m by remember { mutableStateOf(1) }
        var search_count by remember { mutableStateOf(0) }
        var notes_count by remember { mutableStateOf(0) }

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
                                themeState = themeState
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

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "BiblePro") {
        App()
    }
}
