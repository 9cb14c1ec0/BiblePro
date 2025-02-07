import xml.etree.ElementTree as ET
from typing import List, Dict

def indent(text: str, level: int = 1) -> str:
    """Add proper indentation to text."""
    return "    " * level + text

def generate_verse_objects(verses: List[ET.Element]) -> str:
    """Generate Kotlin code for verse objects."""
    verse_objects = []
    for verse in verses:
        number = verse.get('number')
        text = verse.text.replace('"', '\\"')  # Escape quotes in text
        verse_objects.append(indent(f'Verse(\n' +
                                  indent(f'number = {number},\n', 2) +
                                  indent(f'text = "{text}"\n', 2) +
                                  indent('),\n')))
    return ''.join(verse_objects)

def generate_chapter_objects(chapters: List[ET.Element]) -> str:
    """Generate Kotlin code for chapter objects."""
    chapter_objects = []
    for chapter in chapters:
        number = chapter.get('number')
        verses = chapter.findall('verse')
        chapter_objects.append(indent(f'Chapter(\n' +
                                    indent(f'number = {number},\n', 2) +
                                    indent('verses = listOf(\n', 2) +
                                    generate_verse_objects(verses) +
                                    indent(')\n', 2) +
                                    indent('),\n')))
    return ''.join(chapter_objects)

def generate_book_objects(books: List[ET.Element]) -> str:
    """Generate Kotlin code for book objects."""
    book_objects = []
    for book in books:
        number = book.get('number')
        chapters = book.findall('chapter')
        book_objects.append(indent(f'Book(\n' +
                                 indent(f'number = {number},\n', 2) +
                                 indent('chapters = listOf(\n', 2) +
                                 generate_chapter_objects(chapters) +
                                 indent(')\n', 2) +
                                 indent('),\n')))
    return ''.join(book_objects)

def generate_testament_objects(testaments: List[ET.Element]) -> str:
    """Generate Kotlin code for testament objects."""
    testament_objects = []
    for testament in testaments:
        name = testament.get('name')
        books = testament.findall('book')
        testament_objects.append(indent(f'Testament(\n' +
                                     indent(f'name = "{name}",\n', 2) +
                                     indent('books = listOf(\n', 2) +
                                     generate_book_objects(books) +
                                     indent(')\n', 2) +
                                     indent('),\n')))
    return ''.join(testament_objects)

def generate_kotlin_code(xml_file: str) -> str:
    """Generate complete Kotlin code from XML file."""
    # Parse XML
    tree = ET.parse(xml_file)
    root = tree.getroot()

    # Generate data classes
    data_classes = """

"""

    # Generate Bible instance
    translation = root.get('translation')
    testaments = root.findall('testament')

    bible_instance = f"""
val bible = Bible(
    translation = "{translation}",
    testaments = listOf(
{generate_testament_objects(testaments)}    )
)
"""

    # Combine everything
    return data_classes + bible_instance

def main():
    # Generate the Kotlin code
    kotlin_code = generate_kotlin_code('data/EnglishKJBible.xml')

    # Write to file
    with open("/home/edwin/Documents/Projects/BiblePro/src/main/kotlin/bibles/KJV.kt", 'w') as b:
        b.write(kotlin_code)

if __name__ == "__main__":
    main()
