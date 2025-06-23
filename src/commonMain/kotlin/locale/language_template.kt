package locale

// Template for adding a new language to the application
// 1. Copy this file and rename it to match your language (e.g., italian.kt)
// 2. Replace "language_name" with your language name (e.g., "italian")
// 3. Translate all the strings in the map
// 4. Add your language map to the local_map in locale.kt
// 5. Add your language to the dropdown menu in DropDownMenu.kt

val language_name_strings: Map<String, String> = mapOf(
    "Bible Expert" to "", // Translate to your language
    "BiblePro" to "", // Translate to your language
    "Bibles" to "", // Translate to your language
    "Book" to "", // Translate to your language
    "Chapter" to "", // Translate to your language
    "Close Bible" to "", // Translate to your language
    "English Amplified Bible" to "", // Translate to your language
    "English Chain Reference" to "", // Translate to your language
    "English KJV" to "", // Translate to your language
    "English Tyndale 1537" to "", // Translate to your language
    "English YLT" to "", // Translate to your language
    "Error loading Bible content" to "", // Translate to your language
    "Global Notes" to "", // Translate to your language
    "Greek Textus Receptus" to "", // Translate to your language
    "New Bible" to "", // Translate to your language
    "New Search" to "", // Translate to your language
    "Spanish RV 2020" to "", // Translate to your language
)

// Example of how to add your language to the local_map in locale.kt:
/*
val local_map: Map<String, Map<String, String>> = mapOf(
    "spanish" to spanish_strings,
    "french" to french_strings,
    "german" to german_strings,
    "hindi" to hindi_strings,
    "language_name" to language_name_strings // Add your language here
);
*/

// Example of how to add your language to the dropdown menu in DropDownMenu.kt:
/*
DropdownMenuItem(
    content = { Text("Your Language Name") },
    onClick = { L.current.language = "language_name" }
)
*/