package bibles

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LexiconEntry(
    val strong: String,
    val part_of_speech: String,
    val transliteration: String,
    val phonetic: String,
    val definition: String,
    val usage: String,
    val origin: String
)

@Serializable
data class LexiconDictionary(
    val entries: Map<String, LexiconEntry>
)

class LexiconLoad {
    fun loadLexicon(): LexiconDictionary {
        val inputStream = javaClass.getResourceAsStream("/" + "lexicon.json")
        val dict =  Json.decodeFromString<Map<String, LexiconEntry>>(inputStream!!.readAllBytes().decodeToString())
        return LexiconDictionary(entries = dict)
    }
}

data class GreekArticle(
    val nominative: String,
    val genitive: String
)

object GreekArticles {
    val MASCULINE = GreekArticle("ὁ", "τοῦ")
    val FEMININE = GreekArticle("ἡ", "τῆς")
    val NEUTER = GreekArticle("τό", "τοῦ")
}

fun createLexiconKey(wordForm: String): String {
    // Handle single-word entries (mostly verbs)
    if (isSingleWordEntry(wordForm)) {
        return wordForm
    }

    // Handle indeclinable words
    if (isIndeclinable(wordForm)) {
        return "$wordForm, ${determineArticle(wordForm).nominative}"
    }

    // Determine the gender and declension pattern
    val (nominativeForm, genitiveEnding, article) = analyzeWord(wordForm)

    // For words that only show two parts in lexicon
    if (shouldUseSimplifiedFormat(nominativeForm)) {
        return "$nominativeForm, ${article.nominative}"
    }

    // Standard three-part format
    return "$nominativeForm, $genitiveEnding, ${article.nominative}"
}

private fun isSingleWordEntry(word: String): Boolean {
    return when {
        // Common verb endings
        word.endsWith("ω") -> true      // Present active indicative
        word.endsWith("ομαι") -> true   // Present middle/passive
        word.endsWith("έω") -> true     // Contract verbs
        word.endsWith("άω") -> true     // Contract verbs
        word.endsWith("όω") -> true     // Contract verbs
        word.endsWith("εύω") -> true    // Like ἱερατεύω
        word.endsWith("άζω") -> true    // Common verb ending
        word.endsWith("ίζω") -> true    // Common verb ending
        word.endsWith("ύω") -> true     // Common verb ending
        word.endsWith("σσω") -> true    // Common verb ending
        word.endsWith("ττω") -> true    // Common verb ending

        // Particles and other indeclinable words
        word in setOf("καί", "δέ", "γάρ", "οὖν", "μέν", "τε") -> true

        // Prepositions
        word in setOf("ἐν", "εἰς", "ἐκ", "ἀπό", "πρός", "διά", "μετά") -> true

        else -> false
    }
}

private fun isIndeclinable(word: String): Boolean {
    return word.endsWith("ί") ||
            word.endsWith("ίμ") ||
            word.endsWith("άμ") ||
            word.endsWith("άλ") ||
            word.endsWith("ώθ") ||
            word.endsWith("ίδ") ||
            word.endsWith("ών") && !word.endsWith("ίων")
}

private fun analyzeWord(wordForm: String): Triple<String, String, GreekArticle> {
    // First Declension Patterns
    when {
        // Feminine first declension
        wordForm.endsWith("α") -> {
            val stem = wordForm.dropLast(1)
            return Triple(wordForm, "ας", GreekArticles.FEMININE)
        }
        wordForm.endsWith("η") -> {
            val stem = wordForm.dropLast(1)
            return Triple(wordForm, "ης", GreekArticles.FEMININE)
        }
        wordForm.endsWith("ας") -> {
            val stem = wordForm.dropLast(2)
            return Triple(wordForm, "ου", GreekArticles.MASCULINE)
        }

        // Second Declension Patterns
        wordForm.endsWith("ος") -> {
            val stem = wordForm.dropLast(2)
            return Triple(wordForm, "ου", GreekArticles.MASCULINE)
        }
        wordForm.endsWith("ον") -> {
            val stem = wordForm.dropLast(2)
            return Triple(wordForm, "ου", GreekArticles.NEUTER)
        }

        // Special -ευς nouns
        wordForm.endsWith("εύς") -> {
            val stem = wordForm.dropLast(3)
            return Triple(wordForm, "έως", GreekArticles.MASCULINE)
        }

        // Third Declension Patterns
        wordForm.endsWith("ξ") || wordForm.endsWith("ψ") -> {
            return Triple(wordForm, "ος", GreekArticles.FEMININE)
        }
        wordForm.endsWith("μα") -> {
            val stem = wordForm.dropLast(2)
            return Triple(wordForm, "ματος", GreekArticles.NEUTER)
        }

        // Special case for Ἰησοῦς type words
        wordForm.endsWith("οῦς") -> {
            val stem = wordForm.dropLast(3)
            return Triple(wordForm, "οῦ", GreekArticles.MASCULINE)
        }
        wordForm.endsWith("οῦ") -> {
            val stem = wordForm.dropLast(2)
            return Triple("${stem}οῦς", "οῦ", GreekArticles.MASCULINE)
        }
    }

    // Default case
    return Triple(wordForm, "", GreekArticles.MASCULINE)
}

private fun determineArticle(word: String): GreekArticle {
    // Default to masculine unless specific feminine or neuter patterns are found
    return when {
        word.endsWith("α") || word.endsWith("η") -> GreekArticles.FEMININE
        word.endsWith("ον") || word.endsWith("μα") -> GreekArticles.NEUTER
        else -> GreekArticles.MASCULINE
    }
}

private fun shouldUseSimplifiedFormat(word: String): Boolean {
    // Words that traditionally use two-part format in lexicons
    return isIndeclinable(word) ||
            word.endsWith("ίας") ||
            word.endsWith("είας")
}