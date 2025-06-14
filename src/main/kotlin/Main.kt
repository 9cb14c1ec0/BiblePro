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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import bibles.*
import nl.marc_apps.tts.TextToSpeechEngine
import nl.marc_apps.tts.experimental.ExperimentalDesktopTarget
import nl.marc_apps.tts.rememberTextToSpeechOrNull


@OptIn(ExperimentalDesktopTarget::class)
@Composable
fun VerseCard(bibles: MutableMap<String, Bible>,
              book: Int,
              chapter: Int,
              verse: Int,
              OnSelectionChange: (index: Int) -> Unit){

    val textToSpeech = rememberTextToSpeechOrNull(TextToSpeechEngine.SystemDefault)
    var testament = "Old"
    if(book > 39)
    {
        testament = "New"
    }

    // Check if verse has been read
    val isRead = ReadingTracker.instance.isRead(book, chapter, verse)

    // Function to mark verse as read
    val markAsRead = {
        ReadingTracker.instance.markAsRead(book, chapter, verse)
    }

    // Check if only one Bible is selected for compact mode
    val isCompactMode = bibles.size == 1

    if (isCompactMode) {
        // Compact layout for single Bible
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(6.dp))
                .border(0.5.dp, MaterialTheme.colors.primary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .background(
                    if (isRead) MaterialTheme.colors.primary.copy(alpha = 0.1f) 
                    else MaterialTheme.colors.surface, 
                    RoundedCornerShape(6.dp)
                )
                .clickable { markAsRead() }
        ) {
            // Smaller verse number
            Text(
                verse.toString(),
                style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colors.primary,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .align(Alignment.CenterVertically)
            )

            // Verse content
            val bible = bibles.entries.first()
            var has_verse = false
            try {
                bible.value.testaments.first { it.name == testament }.books.first {
                    it.number == book }.chapters.first { it.number == chapter }.verses.forEach {
                    if(it.number == verse)
                    {
                        has_verse = true
                    }
                }
            }
            catch (e: NoSuchElementException)
            {
                has_verse = false
            }

            if(has_verse)
            {
                val verse_text = bible.value.testaments.first { it.name == testament }.books.first {
                    it.number == book }.chapters.first { it.number == chapter }.verses.first { it.number == verse }.text
                var word_counter = 0
                val greek_regex = Regex("""[\u0370-\u03FF\u1F00-\u1FFF]""")
                val annotated_text = buildAnnotatedString { verse_text.split(" ").forEach { s ->
                    pushStringAnnotation(word_counter.toString(), s)
                    append(s)
                    append(" ")
                    pop()
                    word_counter += 1
                } }

                // Compact verse text display
                SelectionContainer {
                    ClickableText(
                        text = annotated_text,
                        onClick = { offset ->
                            annotated_text.getStringAnnotations(start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    if(greek_regex.containsMatchIn(annotation.item))
                                    {
                                        OnSelectionChange(annotation.tag.toInt())
                                    }
                                }
                        },
                        modifier = Modifier
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                            .background(MaterialTheme.colors.background),
                        style = MaterialTheme.typography.body1.copy(
                            fontFamily = FontFamily.Serif,
                            lineHeight = 22.sp
                        )
                    )
                }
            }
        }
    } else {
        // Original enhanced card with better styling for multiple Bibles
        Row(
            modifier = Modifier
                .padding(8.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .background(
                    if (isRead) MaterialTheme.colors.primary.copy(alpha = 0.1f) 
                    else MaterialTheme.colors.surface, 
                    RoundedCornerShape(8.dp)
                )
                .clickable { markAsRead() }
        ) {
            // Verse number in a circle
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .size(28.dp)
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    verse.toString(),
                    style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colors.primary
                )
            }

            // Verse content column
            Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
                var counter = 0
                for (bible in bibles)
                {
                    var has_verse = false
                    try {
                        bible.value.testaments.first { it.name == testament }.books.first {
                            it.number == book }.chapters.first {  it.number == chapter}.verses.forEach {
                            if(it.number == verse)
                            {
                                has_verse = true
                            }
                        }
                    }
                    catch (e: NoSuchElementException)
                    {
                        has_verse = false
                    }

                    if(has_verse)
                    {
                        // Add divider between translations if not the first one
                        if (counter > 0) {
                            Divider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)
                            )
                        }

                        // Bible version label
                        Text(
                            bible.key,
                            style = MaterialTheme.typography.overline,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 2.dp, top = if (counter > 0) 4.dp else 0.dp)
                        )

                        val verse_text = bible.value.testaments.first { it.name == testament }.books.first {
                            it.number == book }.chapters.first {  it.number == chapter}.verses.first { it.number == verse }.text
                        var word_counter = 0
                        val greek_regex = Regex("""[\u0370-\u03FF\u1F00-\u1FFF]""")
                        val annotated_text = buildAnnotatedString { verse_text.split(" ").forEach { s ->
                            pushStringAnnotation(word_counter.toString(), s)
                            append(s)
                            append(" ")
                            pop()
                            word_counter += 1
                        } }

                        // Improved verse text display
                        SelectionContainer {
                            ClickableText(
                                text = annotated_text,
                                onClick = { offset ->
                                    //textToSpeech?.say(verse_text) {}
                                    // offset is the position of the click
                                    annotated_text.getStringAnnotations(start = offset, end = offset)
                                        .firstOrNull()?.let { annotation ->
                                            if(greek_regex.containsMatchIn(annotation.item))
                                            {
                                                OnSelectionChange(annotation.tag.toInt())
                                            }
                                        }
                                },
                                modifier = Modifier
                                    .padding(bottom = 4.dp)
                                    .background(
                                        // Subtle pastel colors instead of grayscale
                                        when (counter % 3) {
                                            0 -> MaterialTheme.colors.primary.copy(alpha = 0.05f)
                                            1 -> MaterialTheme.colors.secondary.copy(alpha = 0.05f)
                                            else -> MaterialTheme.colors.background
                                        }
                                    ),
                                style = MaterialTheme.typography.body1.copy(
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 24.sp
                                )
                            )
                        }

                        counter += 1
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme() {
        var m by remember { mutableStateOf(1) }
        var search_count by remember { mutableStateOf(0) }

        Row(modifier = Modifier.fillMaxSize(1f)) {
            (1..m).forEach {
                var closed by remember { mutableStateOf(false) }
                if(!closed)
                {
                    Column(modifier = Modifier.border(1.dp, Color.Gray).weight(1F)) {
                        BiblePane({ m += 1 }, { closed = true}, {search_count += 1}, it,  m.toFloat())
                    }
                }
            }
            (1 .. search_count).forEach {
                var closed by remember { mutableStateOf(false) }
                if(!closed)
                {
                    Column(modifier = Modifier.border(1.dp, Color.Gray).weight(1F)) {
                        SearchPane({ search_count += 1 }, { closed = true}, it,  search_count.toFloat())
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
