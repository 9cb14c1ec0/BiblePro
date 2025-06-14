package viewmodels

import bibles.Bible
import bibles.bookList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the Search pane component.
 * Handles searching Bibles and managing search results.
 */
class SearchViewModel {
    // State for the Search pane
    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    /**
     * Updates the search text and performs a search if the text is long enough.
     * @param searchText The new search text
     * @param loadedBibles Map of loaded Bibles to search in
     */
    fun updateSearchText(searchText: String, loadedBibles: Map<String, Bible>) {
        _state.update { currentState ->
            val newResults = if (searchText.length > 2) {
                searchAllBibles(searchText, loadedBibles)
            } else {
                emptyList()
            }
            
            currentState.copy(
                searchText = searchText,
                searchResults = newResults
            )
        }
    }

    /**
     * Searches all loaded Bibles for the given text.
     * @param searchText The text to search for
     * @param loadedBibles Map of loaded Bibles to search in
     * @return List of search results
     */
    private fun searchAllBibles(searchText: String, loadedBibles: Map<String, Bible>): List<BibleSearchResult> {
        val results = mutableListOf<BibleSearchResult>()
        
        loadedBibles.forEach { (bibleName, bible) ->
            bible.testaments.forEach { testament ->
                testament.books.forEach { book ->
                    book.chapters.forEach { chapter ->
                        chapter.verses.forEach { verse ->
                            if (verse.text.lowercase().contains(searchText.lowercase())) {
                                results.add(
                                    BibleSearchResult(
                                        "${bookList.first { bl -> bl.id == book.number }.text} ${chapter.number}:${verse.number}",
                                        verse.text,
                                        bible.translation,
                                        book.number
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Deduplicate and sort results
        return results.distinctBy { it.reference }.sortedBy { it.sortByBook }
    }

    /**
     * Clears the search results.
     */
    fun clearResults() {
        _state.update { currentState ->
            currentState.copy(
                searchResults = emptyList()
            )
        }
    }
}

/**
 * Data class representing a Bible search result.
 */
data class BibleSearchResult(
    val reference: String,
    val text: String,
    val bible: String,
    val sortByBook: Int
) {
    override fun toString(): String {
        return "$reference: $text"
    }
}

/**
 * Data class representing the state of the Search pane.
 */
data class SearchState(
    val searchText: String = "",
    val searchResults: List<BibleSearchResult> = emptyList()
)