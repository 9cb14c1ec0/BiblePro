# Adding New Bible Translations to BiblePro

This document provides a step-by-step guide on how to add new Bible translations to the BiblePro application.

## Overview

BiblePro supports multiple Bible translations that can be loaded and displayed side by side. Adding a new translation involves:

1. Creating an XML file with the Bible content in the required format
2. Adding the translation to the list of available Bibles
3. Placing the XML file in the correct location

## XML File Format

Bible translations are stored as XML files with a specific structure. Here's the required format:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bible translation="Translation Name" status="Copyright information" link="Source URL">
    <testament name="Old">
        <book number="1">
            <chapter number="1">
                <verse number="1">Verse text goes here.</verse>
                <verse number="2">Second verse text goes here.</verse>
                <!-- More verses -->
            </chapter>
            <!-- More chapters -->
        </book>
        <!-- More books -->
    </testament>
    <testament name="New">
        <!-- New Testament books -->
    </testament>
</bible>
```

### Required Elements and Attributes

- `<bible>` (root element)
  - `translation` attribute: The display name of the translation (e.g., "English KJV")
  - `status` attribute: Copyright information or translation status
  - `link` attribute: URL to the source or more information about the translation
  
- `<testament>` (child of `<bible>`)
  - `name` attribute: Either "Old" or "New"
  
- `<book>` (child of `<testament>`)
  - `number` attribute: Book number (1-66, see book list below)
  
- `<chapter>` (child of `<book>`)
  - `number` attribute: Chapter number
  
- `<verse>` (child of `<chapter>`)
  - `number` attribute: Verse number
  - Content: The actual verse text

## File Naming and Location

1. Name your XML file according to the translation name with spaces removed, followed by the `.xml` extension
   - Example: For "English KJV", the file should be named `EnglishKJV.xml`
   - Example: For "Spanish RV 2020", the file should be named `SpanishRV2020.xml`

2. Place the XML file in the `/src/main/resources/` directory of the project

## Adding the Translation to the Available List

To make your translation available in the application's UI, you need to add it to the `bibleList` in the `BibleList.kt` file:

1. Open `/src/main/kotlin/bibles/BibleList.kt`
2. Add a new `ComboOption` to the `bibleList` with:
   - The display name of your translation
   - A unique ID (increment from the last one)

Example:
```kotlin
val bibleList = listOf(
    // Existing translations...
    ComboOption("English KJV", 2),
    // Add your new translation here
    ComboOption("Your New Translation", 7)
)
```

## Book Numbers Reference

The Bible books are numbered 1-66 in the following order:

### Old Testament (1-39)
1. Genesis
2. Exodus
3. Leviticus
...
39. Malachi

### New Testament (40-66)
40. Matthew
41. Mark
42. Luke
...
66. Revelation

For the complete list, refer to the `bookList` in `/src/main/kotlin/bibles/BibleList.kt`.

## Example: Adding a New Translation

Let's walk through an example of adding a fictional "French LSG" translation:

1. Create an XML file named `FrenchLSG.xml` with the proper structure:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bible translation="French LSG" status="© Public Domain" link="https://example.com/french-lsg">
    <testament name="Old">
        <book number="1">
            <chapter number="1">
                <verse number="1">Au commencement, Dieu créa les cieux et la terre.</verse>
                <!-- More verses -->
            </chapter>
            <!-- More chapters -->
        </book>
        <!-- More books -->
    </testament>
    <!-- New Testament -->
</bible>
```

2. Place `FrenchLSG.xml` in the `/src/main/resources/` directory

3. Add the translation to `BibleList.kt`:

```kotlin
val bibleList = listOf(
    // Existing translations...
    ComboOption("Spanish RV 2020", 6),
    ComboOption("French LSG", 7)
)
```

4. Rebuild and run the application. The new translation should now be available in the Bible selection dropdown.

## Troubleshooting

If your translation doesn't appear or doesn't load correctly:

1. Verify the XML file is properly formatted and valid
2. Check that the file name matches the translation name (with spaces removed)
3. Ensure the file is in the correct location (`/src/main/resources/`)
4. Confirm you've added the translation to the `bibleList` in `BibleList.kt`
5. Check the application logs for any parsing errors

## Additional Notes

- The application caches loaded Bibles to improve performance
- All Bible translations should include all 66 books, even if some are empty
- Ensure proper UTF-8 encoding for non-English translations