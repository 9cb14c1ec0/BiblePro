import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import bibles.*



@Composable
fun VerseCard(bibles: MutableMap<String, Bible>,
              book: Int,
              chapter: Int,
              verse: Int,
              OnSelectionChange: (index: Int) -> Unit){

    var testament = "Old"
    if(book > 39)
    {
        testament = "New"
    }
   Row(modifier = Modifier.padding(5.dp)) {
       Text(verse.toString(), modifier = Modifier.padding(10.dp).align(Alignment.CenterVertically))
       Column {
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
                   SelectionContainer {
                       ClickableText(
                           text = annotated_text,
                           onClick = { offset ->
                               // offset is the position of the click
                               annotated_text.getStringAnnotations(start = offset, end = offset)
                                   .firstOrNull()?.let { annotation ->
                                       if(greek_regex.containsMatchIn(annotation.item))
                                       {
                                           OnSelectionChange(annotation.tag.toInt())
                                       }
                                   }
                           },
                           modifier = Modifier.background( Color(255-(counter*15), 255-(counter*15), 255-(counter*15)))
                       )
                   }

                   counter += 1
               }

           }
       }
   }
    Divider(color = Color.Black)


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
