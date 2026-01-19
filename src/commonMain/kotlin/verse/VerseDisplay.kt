package verse

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import viewmodels.VerseData

/**
 * Clean, performant verse row component.
 * Stateless - receives all data as parameters.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerseRow(
    verseData: VerseData,
    bibleNames: List<String>,
    isCompactMode: Boolean,
    showPhonetics: Boolean,
    isSelected: Boolean,
    onVerseClick: () -> Unit,
    onVerseLongClick: () -> Unit,
    onWordClick: (verse: Int, wordIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animate background color for smooth transitions
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            verseData.isHighlighted -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
            verseData.isRead -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else -> Color.Transparent
        },
        animationSpec = tween(150),
        label = "verseBackground"
    )

    // Animate left border color for read state
    val borderColor by animateColorAsState(
        targetValue = when {
            verseData.isRead -> MaterialTheme.colorScheme.primary
            verseData.hasNote -> MaterialTheme.colorScheme.tertiary
            else -> Color.Transparent
        },
        animationSpec = tween(150),
        label = "verseBorder"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onVerseClick,
                onLongClick = onVerseLongClick
            )
            .onRightClick(onVerseLongClick)
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        // Left accent bar (shows read state, notes)
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(IntrinsicSize.Max)
                .fillMaxHeight()
                .background(borderColor, RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Verse number
        Text(
            text = verseData.verse.toString(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (verseData.isRead) FontWeight.Bold else FontWeight.Medium,
                fontFeatureSettings = "tnum" // Tabular numbers
            ),
            color = if (verseData.isRead)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .width(28.dp)
                .padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Verse content
        Column(modifier = Modifier.weight(1f)) {
            if (isCompactMode) {
                // Single Bible - just show text
                val bibleName = bibleNames.firstOrNull() ?: return@Column
                val annotatedText = verseData.processedTexts[bibleName] ?: return@Column

                VerseText(
                    text = annotatedText,
                    isHighlighted = verseData.isHighlighted,
                    onWordClick = { wordIndex -> onWordClick(verseData.verse, wordIndex) }
                )

                // Phonetics
                if (showPhonetics && verseData.phoneticTexts.containsKey(bibleName)) {
                    PhoneticText(text = verseData.phoneticTexts[bibleName]!!)
                }
            } else {
                // Multiple Bibles - show each with label
                bibleNames.forEachIndexed { index, bibleName ->
                    val annotatedText = verseData.processedTexts[bibleName] ?: return@forEachIndexed

                    if (index > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Bible label
                    Text(
                        text = bibleName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    VerseText(
                        text = annotatedText,
                        isHighlighted = verseData.isHighlighted,
                        onWordClick = { wordIndex -> onWordClick(verseData.verse, wordIndex) }
                    )

                    // Phonetics for this Bible
                    if (showPhonetics && verseData.phoneticTexts.containsKey(bibleName)) {
                        PhoneticText(text = verseData.phoneticTexts[bibleName]!!)
                    }
                }
            }

            // Note preview (if exists)
            if (verseData.hasNote) {
                NotePreview(noteText = verseData.noteText)
            }

            // Cross-references (if exist)
            if (verseData.crossReferences.isNotEmpty()) {
                CrossReferenceChips(references = verseData.crossReferences)
            }
        }

        // Status indicators (minimal - just dots)
        Column(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (verseData.isRead) {
                StatusDot(color = MaterialTheme.colorScheme.primary)
            }
            if (verseData.isHighlighted) {
                StatusDot(color = MaterialTheme.colorScheme.tertiary)
            }
            if (verseData.hasNote) {
                StatusDot(color = MaterialTheme.colorScheme.error)
            }
            if (verseData.crossReferences.isNotEmpty()) {
                StatusDot(color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

/**
 * The actual verse text with word click support.
 */
@Composable
private fun VerseText(
    text: AnnotatedString,
    isHighlighted: Boolean,
    onWordClick: (Int) -> Unit
) {
    val greekRegex = remember { Regex("""[\u0370-\u03FF\u1F00-\u1FFF]""") }

    SelectionContainer {
        ClickableText(
            text = text,
            onClick = { offset ->
                text.getStringAnnotations(start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        if (greekRegex.containsMatchIn(annotation.item)) {
                            val wordIndex = annotation.tag.toIntOrNull()
                            if (wordIndex != null) {
                                onWordClick(wordIndex)
                            }
                        }
                    }
            },
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Serif,
                lineHeight = 26.sp,
                letterSpacing = 0.3.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

/**
 * Phonetic pronunciation display.
 */
@Composable
private fun PhoneticText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            fontStyle = FontStyle.Italic,
            lineHeight = 18.sp
        ),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

/**
 * Note preview shown inline.
 */
@Composable
private fun NotePreview(noteText: String) {
    Text(
        text = noteText,
        style = MaterialTheme.typography.bodySmall.copy(
            fontStyle = FontStyle.Italic
        ),
        color = MaterialTheme.colorScheme.tertiary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    )
}

/**
 * Cross-reference chips displayed inline.
 */
@Composable
private fun CrossReferenceChips(references: List<String>) {
    Row(
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        references.take(3).forEach { ref ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    text = ref,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        if (references.size > 3) {
            Text(
                text = "+${references.size - 3}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Small status indicator dot.
 */
@Composable
private fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .size(8.dp)
            .background(color, RoundedCornerShape(50))
    )
}