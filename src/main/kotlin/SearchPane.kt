import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import bibles.bookList
import bibles.loaded_bibles

class BibleSearchResult(val reference: String, val text: String, val Bible: String, val sortByBook:Int)
{
    override fun toString(): String {
        return "$reference: $text"
    }
}

fun searchAllBibles(search_text: String): List<BibleSearchResult>
{
    val results: MutableList<BibleSearchResult> = mutableListOf()
    loaded_bibles.forEach()
    {
        val bible = it.value
        bible.testaments.forEach { testament ->
            testament.books.forEach { book ->
                book.chapters.forEach { chapter ->
                    chapter.verses.forEach { verse ->
                        if(verse.text.lowercase().contains(search_text.lowercase()))
                        {
                            results.add(BibleSearchResult("${bookList.first { bl -> bl.id == book.number }.text} ${chapter.number}:${verse.number}",
                                verse.text, bible.translation, book.number))
                        }
                    }
                }
            }
        }
    }
    // dedupe
    return results.distinctBy { it.reference }.sortedBy { it.sortByBook }
}

@Composable
fun SearchPane(
    OnAddClicked: () -> Unit,
    OnCloseClicked: (unit: Int) -> Unit,
    thisUnit: Int,
    totalUnits: Float,
) {

    var search_text by remember { mutableStateOf("") }
    var search_results by remember { mutableStateOf(emptyList<BibleSearchResult>()) }

    Row(modifier = Modifier.padding(5.dp)) {
        TextField(
            value = search_text,
            placeholder = { Text("Search") },
            onValueChange = { newText ->
                search_text = newText
                search_results = emptyList()
                if(search_text.length > 2)
                {
                    search_results = searchAllBibles(search_text)
                }
            },
            modifier = Modifier.weight(1f)
        )
        if(totalUnits > 1)
        {
            MyDropdownMenu(
                listOf(
                    ComboOption("New Search", -1),
                    ComboOption("Close Search", 1),
                ),
                Icons.Default.MoreVert
            ) { item: ComboOption ->
                when(item.id)
                {
                    -1 -> OnAddClicked()
                    1 -> OnCloseClicked(thisUnit)
                }
            }
        }
    }
    Divider(color = Color.Black, thickness = 1.dp)
    LazyColumn { items(search_results.size) { index ->
        SelectionContainer(Modifier.padding(10.dp).border(1.dp, Color.Gray, RoundedCornerShape(5.dp))) {
            ClickableText(
                text = buildAnnotatedString { append(search_results[index].Bible + ": " +
                        search_results[index].reference + "\n" + search_results[index].text) },
                onClick = { offset ->
                    // Open the clicked verse in a new pane
                },
                modifier = Modifier.padding(5.dp)
            )
        }
    }}

}