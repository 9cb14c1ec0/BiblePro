import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
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
public fun BiblePane(
    OnAddClicked: () -> Unit,
    OnCloseClicked: () -> Unit,
    thisUnit: Int,
    totalUnits: Float,
) {
    var book_id by remember { mutableStateOf(40) }
    var chapter_num by remember { mutableStateOf(1) }
    var bibles by remember { mutableStateOf(emptyMap<String, Bible>().toMutableMap() )}
    var loaded_bibles by remember { mutableStateOf(emptyMap<String, Bible>().toMutableMap()) }
    var bible_count by remember { mutableStateOf(0) }
    var chapters by remember { mutableStateOf(emptyList<ComboOption>()) }
    var lexicon by remember { mutableStateOf(LexiconLoad().loadLexicon())}
    var lexicon_key by remember { mutableStateOf("") }
    var lexicon_text by remember { mutableStateOf("") }
    var strongs_mapping by remember { mutableStateOf(StrongsLoad().loadStrongsMapping()) }

    Row(modifier = Modifier.padding(5.dp)) {
        MyComboBox(
            "Bibles", bibleList,
            onOptionsChosen = {
                bibles.clear()
                it.map { option ->
                    if(!loaded_bibles.containsKey(option.text))
                    {
                        loaded_bibles[option.text] = BibleXmlParser().parseFromResource(option.text)
                    }
                    if(!loaded_bibles.containsKey("English KJV"))
                    {
                        loaded_bibles["English KJV"] = BibleXmlParser().parseFromResource("English KJV")
                    }
                    bibles[option.text] = loaded_bibles[option.text]!!
                }
                bible_count = bibles.size
            },
            modifier = Modifier.weight(1f),
            singleSelect = false
        )
        if((totalUnits - thisUnit).toInt() == 0)
        {
            Button(onClick = {OnAddClicked()}) {
                Text("+")
            }
            Button(onClick = {OnCloseClicked()}) {
                Text("-")
            }
        }
    }
    Row(modifier = Modifier.padding(5.dp)) {
        MyComboBox(
            "Book", bookList,
            onOptionsChosen = {
                book_id = it[0].id
                var testament = "Old"
                if(book_id > 39)
                {
                    testament = "New"
                }

                if(loaded_bibles.containsKey("English KJV"))
                {
                    val new_chapters = mutableListOf<ComboOption>()
                    val book = loaded_bibles["English KJV"]!!.testaments.first{ t -> t.name == testament}.books.first {
                        b -> b.number == book_id}
                    book.chapters.forEach {c ->
                        new_chapters.apply { add(ComboOption(c.number.toString(), c.number)) }
                    }
                    chapters = new_chapters.toList()
                }
            },
            modifier = Modifier.weight(1f),
            singleSelect = true
        )

        MyComboBox(
            "Chapter", chapters,
            onOptionsChosen = {
                chapter_num = it[0].id
            },
            modifier = Modifier.weight(1f),
            singleSelect = true
        )

    }

    if(bible_count > 0)
    {
        var testament = "Old"
        if(book_id > 39)
        {
            testament = "New"
        }
        val ot = loaded_bibles["English KJV"]!!.testaments.first { it.name == testament }
        val book = ot.books.first { it.number == book_id }
        val chapter = book.chapters.first{it.number == chapter_num}
        Row {
            LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92F).border(2.dp, Color.Black)) {
                items(count = chapter.verses.size , itemContent = { item ->
                    VerseCard(bibles,  book.number, chapter.number, item + 1) { index ->
                        var strongs_verse = strongs_mapping.first { it.book == (book.number - 39) && it.chapter == chapter.number &&
                                it.verse == item + 1}
                        println(strongs_verse.words)
                        println(index)
                        var strongs = strongs_verse.words[index]
                        strongs = "%04d".format(strongs.toInt())
                        lexicon_key = lexicon.entries.filter { item -> item.value.strong == "g$strongs" }.keys.single()
                        lexicon_text = "Strongs: ${lexicon.entries[lexicon_key]!!.definition}"
                        println(lexicon_text)
                    }
                })
            }
        }

    }

    Text(lexicon_text, Modifier.padding(5.dp))

}

@Composable
@Preview
fun App() {
    MaterialTheme() {
        var m by remember { mutableStateOf(1) }
        Row(modifier = Modifier.fillMaxSize(1f)) {
            (1..m).forEach {
                Column(modifier = Modifier.weight(1f).border(1.dp, Color.Gray)) {
                    BiblePane({ m += 1 }, { m -= 1 }, it,  m.toFloat())
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
