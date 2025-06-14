import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bibles.*
import nl.marc_apps.tts.TextToSpeechEngine
import nl.marc_apps.tts.experimental.ExperimentalDesktopTarget
import nl.marc_apps.tts.rememberTextToSpeechOrNull

@OptIn(ExperimentalDesktopTarget::class)
@Composable
fun BiblePane(
    OnAddClicked: () -> Unit,
    OnCloseClicked: (unit: Int) -> Unit,
    OnNewSearch: () -> Unit,
    thisUnit: Int,
    totalUnits: Float,
) {
    var book_id by remember { mutableStateOf(40) }
    var chapter_num by remember { mutableStateOf(1) }
    val bibles by remember { mutableStateOf(emptyMap<String, Bible>().toMutableMap() ) }
    val loaded_bibles by remember { mutableStateOf(emptyMap<String, Bible>().toMutableMap()) }
    var bible_count by remember { mutableStateOf(0) }
    var chapters by remember { mutableStateOf(emptyList<String>()) }
    val lexicon by remember { mutableStateOf(LexiconLoad().loadLexicon()) }
    var lexicon_key by remember { mutableStateOf("") }
    var lexicon_text by remember { mutableStateOf("") }
    val strongs_mapping by remember { mutableStateOf(StrongsLoad().loadStrongsMapping()) }


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
        MinimalDropdownMenu()
        ReadingMenu()
        if(totalUnits > 1)
        {
            MyDropdownMenu(
                listOf(
                    ComboOption("New Bible", -1),
                    ComboOption("Close Bible", 1),
                    ComboOption("New Search", 2)
                ),
                Icons.Filled.MoreVert,
                OnSelectionChange = { i ->
                    if(i.id == 1 )
                    {
                        OnCloseClicked(thisUnit)
                    }
                    else if(i.id == -1)
                    {
                        OnAddClicked()
                    }
                    else if(i.id == 2)
                    {
                        OnNewSearch()
                    }
                }
            )
        }
        else if(totalUnits.toInt() == 1)
        {
            MyDropdownMenu(
                listOf(
                    ComboOption("New Bible", -1),
                    ComboOption("New Search", 2)
                ),
                Icons.Filled.MoreVert,
                OnSelectionChange = { i ->
                    if(i.id == -1)
                    {
                        OnAddClicked()
                    }
                    else if(i.id == 2)
                    {
                        OnNewSearch()
                    }
                }
            )
        }
    }
    Row(modifier = Modifier.padding(5.dp)) {
        DropdownMenuBox("Book", "", bookList.map { it.text }, { selected ->
            println(selected)
            chapter_num = 1
            book_id = bookList.first { it.text == selected }.id
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
                chapters = new_chapters.map { it.text }
            }

        }, modifier = Modifier.weight(1f).padding(5.dp))

        DropdownMenuBox("Chapter", "1", chapters, { selected ->
            if(chapters.contains(selected))
            {
                chapter_num = selected.toInt()
            }
        }, filterOptions =  false, modifier = Modifier.weight(1f).padding(5.dp))

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
                        val strongs_verse = strongs_mapping.first { it.book == (book.number - 39) && it.chapter == chapter.number &&
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
