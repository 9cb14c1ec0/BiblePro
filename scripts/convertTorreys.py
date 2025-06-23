#!/usr/bin/env python3
"""
Script to convert Torreys Topical Text Book XML to Chain Reference format.

This script reads the TorreysTopicalTextBook.xml file and converts it to a format
compatible with the EnglishChainReference.xml file used in the BiblePro application.

The Torreys Topical Text Book is organized by topics, with each topic containing
descriptions and Bible verse references. This script reorganizes this data into
a Bible-centric format, where each verse contains references to the topics that
mention it.

Usage:
    python3 convertTorreys.py

Output:
    Creates a new file at src/main/resources/TorreysChainReference.xml
"""

import xml.etree.ElementTree as ET
import re

def parse_mscope(mscope):
    """Parse the mscope attribute to extract book, chapter, and verse(s)."""
    parts = mscope.split(';')
    if len(parts) >= 3:
        book = int(parts[0])
        chapter = int(parts[1])
        verse_part = parts[2]

        # Handle verse ranges (e.g., "12-15")
        if '-' in verse_part:
            verse_range = verse_part.split('-')
            try:
                start_verse = int(verse_range[0])
                end_verse = int(verse_range[1])
                return [(book, chapter, v) for v in range(start_verse, end_verse + 1)]
            except ValueError:
                # If we can't parse the range, skip this reference
                return []
        else:
            try:
                verse = int(verse_part)
                return [(book, chapter, verse)]
            except ValueError:
                # If we can't parse the verse, skip this reference
                return []
    return []

def convert_torreys_to_chain_reference():
    """
    Convert Torreys Topical Text Book to Chain Reference format.

    This function:
    1. Parses the TorreysTopicalTextBook.xml file
    2. Collects all references from the Torreys file
    3. Sorts the references by book, chapter, and verse
    4. Creates a new XML structure with the Bible format (testament/book/chapter/verse)
    5. Adds the sorted references to the XML structure
    6. Writes the result to TorreysChainReference.xml with proper indentation

    The output file follows the same structure as EnglishChainReference.xml:
    - Root element is <bible> with a "translation" attribute
    - Contains <testament> elements with "name" attributes (Old/New Testament)
    - Contains <book> elements with "number" attributes (sorted by number)
    - Contains <chapter> elements with "number" attributes (sorted by number)
    - Contains <verse> elements with "number" attributes (sorted by number) and text content
      that includes the topic references
    """
    # Parse the Torreys XML
    torreys_tree = ET.parse('/home/edwin/Documents/Projects/BiblePro/src/main/resources/TorreysTopicalTextBook.xml')
    torreys_root = torreys_tree.getroot()

    # Dictionary to collect all references
    # Structure: {testament_type: {book_num: {chapter_num: {verse_num: [topic_refs]}}}}
    references = {'OT': {}, 'NT': {}}

    # Process each item (topic) in Torreys
    for item in torreys_root.findall('.//item'):
        topic_id = item.get('id')

        # Process each description in the topic
        for desc in item.findall('.//description'):
            title_elem = desc.find('title')
            if title_elem is not None:
                title = title_elem.text

                # Process each reference link
                for reflink in desc.findall('.//reflink'):
                    mscope = reflink.get('mscope')
                    verse_refs = parse_mscope(mscope)

                    for book, chapter, verse in verse_refs:
                        # Determine if Old Testament or New Testament
                        testament_type = 'OT' if book <= 39 else 'NT'

                        # Initialize dictionaries if they don't exist
                        if book not in references[testament_type]:
                            references[testament_type][book] = {}
                        if chapter not in references[testament_type][book]:
                            references[testament_type][book][chapter] = {}
                        if verse not in references[testament_type][book][chapter]:
                            references[testament_type][book][chapter][verse] = []

                        # Add the topic reference
                        topic_ref = f"{topic_id}: {title}"
                        references[testament_type][book][chapter][verse].append(topic_ref)

    # Create a new XML structure for Chain Reference
    chain_ref_root = ET.Element('bible')
    chain_ref_root.set('translation', 'Torreys Topical Text Book')

    # Create Old Testament and New Testament elements
    ot = ET.SubElement(chain_ref_root, 'testament')
    ot.set('name', 'Old Testament')

    nt = ET.SubElement(chain_ref_root, 'testament')
    nt.set('name', 'New Testament')

    # Add sorted books, chapters, and verses to the XML structure
    for testament_type, books in [('OT', references['OT']), ('NT', references['NT'])]:
        testament_elem = ot if testament_type == 'OT' else nt

        # Sort books by number
        for book_num in sorted(books.keys()):
            book_elem = ET.SubElement(testament_elem, 'book')
            book_elem.set('number', str(book_num))

            # Sort chapters by number
            for chapter_num in sorted(books[book_num].keys()):
                chapter_elem = ET.SubElement(book_elem, 'chapter')
                chapter_elem.set('number', str(chapter_num))

                # Sort verses by number
                for verse_num in sorted(books[book_num][chapter_num].keys()):
                    verse_elem = ET.SubElement(chapter_elem, 'verse')
                    verse_elem.set('number', str(verse_num))

                    # Join all topic references with a separator
                    verse_elem.text = " | ".join(references[testament_type][book_num][chapter_num][verse_num])

    # Function to add indentation to the XML tree
    def indent(elem, level=0):
        i = "\n" + level*"  "
        if len(elem):
            if not elem.text or not elem.text.strip():
                elem.text = i + "  "
            if not elem.tail or not elem.tail.strip():
                elem.tail = i
            for elem in elem:
                indent(elem, level+1)
            if not elem.tail or not elem.tail.strip():
                elem.tail = i
        else:
            if level and (not elem.tail or not elem.tail.strip()):
                elem.tail = i

    # Add indentation to the XML tree
    indent(chain_ref_root)

    # Write the result to a new XML file
    chain_ref_tree = ET.ElementTree(chain_ref_root)
    chain_ref_tree.write('/home/edwin/Documents/Projects/BiblePro/src/main/resources/TorreysChainReference.xml', 
                         encoding='utf-8', xml_declaration=True)

if __name__ == "__main__":
    convert_torreys_to_chain_reference()
