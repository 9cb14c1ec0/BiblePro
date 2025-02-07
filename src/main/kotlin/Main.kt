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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.getSelectedText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import bibles.*
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

data class ComboOption(
    override val text: String,
    val id: Int,
) : SelectableOption

interface SelectableOption {
    val text: String
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MyComboBox(
    labelText: String,
    options: List<ComboOption>,
    onOptionsChosen: (List<ComboOption>) -> Unit,
    modifier: Modifier = Modifier,
    selectedIds: List<Int> = emptyList(),
    singleSelect: Boolean = false,
    triggerVar: Int = 0
) {
    var expanded by remember { mutableStateOf(false) }
    // when no options available, I want ComboBox to be disabled
    val isEnabled by rememberUpdatedState { options.isNotEmpty() }
    var selectedOptionsList  = remember { mutableStateListOf<Int>()}

    //Initial setup of selected ids
    selectedIds.forEach{
        selectedOptionsList.add(it)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (isEnabled()) {
                expanded = !expanded
                if (!expanded) {
                    onOptionsChosen(options.filter { it.id in selectedOptionsList }.toList())
                }
            }
        },
        modifier = modifier,
    ) {
        val selectedSummary = when (selectedOptionsList.size) {
            0 -> ""
            1 -> options.first { it.id == selectedOptionsList.first() }.text
            else -> selectedOptionsList.joinToString(", ") { options[it].text }
        }
        TextField(
            enabled = isEnabled(),
            readOnly = true,
            value = selectedSummary,
            onValueChange = {},
            label = { Text(text = labelText) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                onOptionsChosen(options.filter { it.id in selectedOptionsList }.toList())
            },
            modifier = Modifier
        ) {
            for (option in options) {

                //use derivedStateOf to evaluate if it is checked
                var checked = remember {
                    derivedStateOf{option.id in selectedOptionsList}
                }.value

                DropdownMenuItem(
                    onClick = {
                        if (!checked) {
                            if(singleSelect)
                            {
                                selectedOptionsList.clear()
                            }
                            selectedOptionsList.add(option.id)
                        } else {
                            selectedOptionsList.remove(option.id)
                        }
                    }
                ){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { newCheckedState ->
                                if (newCheckedState) {
                                    if(singleSelect)
                                    {
                                        selectedOptionsList.clear()
                                    }
                                    selectedOptionsList.add(option.id)
                                } else {
                                    selectedOptionsList.remove(option.id)
                                }
                            },
                        )
                        Text(text = option.text)
                    }
                }
            }
        }
    }
}

@Composable
fun VerseCard(bibles: MutableMap<String, Bible>,
              book: Int,
              chapter: Int,
              verse: Int,
              OnSelectionChange: (data: String) -> Unit){

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
               bible.value.testaments.first { it.name == testament }.books.first {
               it.number == book }.chapters.first {  it.number == chapter}.verses.forEach {
                   if(it.number == verse)
                   {
                       has_verse = true
                   }
               }
               if(has_verse)
               {
                   val verse_text = bible.value.testaments.first { it.name == testament }.books.first {
                       it.number == book }.chapters.first {  it.number == chapter}.verses.first { it.number == verse }.text
                   val annotated_text = buildAnnotatedString { verse_text.split(" ").forEach { s ->
                       pushStringAnnotation(s, s)
                       append(s)
                       append(" ")
                       pop()
                   } }
                   SelectionContainer {
                       ClickableText(
                           text = annotated_text,
                           onClick = { offset ->
                               // offset is the position of the click
                               annotated_text.getStringAnnotations(start = offset, end = offset)
                                   .firstOrNull()?.let { annotation ->
                                       println(annotation.tag)
                                       OnSelectionChange(annotation.tag)
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
    var concordance_visible by remember { mutableStateOf(false) }
    var lexicon by remember { mutableStateOf(LexiconLoad().loadLexicon())}
    var lexicon_key by remember { mutableStateOf("") }

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
                    var new_chapters = mutableListOf<ComboOption>()
                    var book = loaded_bibles["English KJV"]!!.testaments.first{t -> t.name == testament}.books.first {
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
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(count = chapter.verses.size , itemContent = { item ->
                VerseCard(bibles,  book.number, chapter.number, item + 1) {
                    val lkey = createLexiconKey(it)
                    println(lkey)
                    if (lexicon.entries.containsKey(lkey)) {
                        concordance_visible = true
                        lexicon_key = lkey
                    }
                }
            })
        }
    }

    if(concordance_visible) {
        println(lexicon_key)
        val entry = lexicon.entries[lexicon_key]
        if (entry != null) {
            Row {
                Text(entry.definition, modifier = Modifier.defaultMinSize(100.dp, 40.dp))
            }
        }
    }
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
