package bibles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import icons.CommonIcons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.format.DateTimeFormatter
import locale.L

/**
 * A dropdown menu that displays what sections of the Bible have been read.
 * It also includes reading plans, statistics, and reminders.
 */
@Composable
fun ReadingMenu() {
    var expanded by remember { mutableStateOf(false) }
    var showReadingPlanDialog by remember { mutableStateOf(false) }
    var showRemindersDialog by remember { mutableStateOf(false) }

    val readSections = ReadingTracker.instance.getReadSections()
    val statistics = ReadingTracker.instance.getStatistics()
    val activePlans = ReadingPlanManager.instance.getAllPlans()

    Box(
        modifier = Modifier
            .padding(16.dp)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(CommonIcons.Book, contentDescription = L.current.l("Reading Tracker"), tint = MaterialTheme.colors.onSurface)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
                .width(IntrinsicSize.Min)
                .widthIn(min = 300.dp)
        ) {
            // Title
            Text(
                L.current.l("Reading Tracker"),
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(8.dp)
            )

            Divider()

            // Reading Statistics
            Text(
                L.current.l("Statistics"),
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                StatisticItem(
                    value = statistics.totalChaptersRead.toString(),
                    label = L.current.l("Chapters Read"),
                    modifier = Modifier.weight(1f)
                )
                StatisticItem(
                    value = statistics.totalBooksCompleted.toString(),
                    label = L.current.l("Books Completed"),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                StatisticItem(
                    value = statistics.currentStreak.toString(),
                    label = L.current.l("Day Streak"),
                    modifier = Modifier.weight(1f)
                )
                StatisticItem(
                    value = String.format("%.1f", statistics.averageChaptersPerDay),
                    label = L.current.l("Chapters/Day"),
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Reading Plans
            Text(
                L.current.l("Reading Plans"),
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
            )

            if (activePlans.isEmpty()) {
                Text(
                    L.current.l("No active reading plans"),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(8.dp)
                )

                Button(
                    onClick = { showReadingPlanDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = L.current.l("Add Reading Plan"),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(L.current.l("Start Reading Plan"))
                }
            } else {
                // Display active reading plans
                Column(modifier = Modifier.padding(8.dp)) {
                    activePlans.forEach { (_, plan) ->
                        ReadingPlanItem(plan)
                    }

                    Button(
                        onClick = { showReadingPlanDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = L.current.l("Add Reading Plan"),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(L.current.l("Add Another Plan"))
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Read Sections
            Text(
                L.current.l("Read Sections"),
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
            )

            // If no sections have been read, show a message
            if (readSections.isEmpty()) {
                Text(
                    L.current.l("No sections read yet"),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                // Display read sections
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    readSections.forEach { (bookId, chapters) ->
                        val bookName = bookList.find { it.id == bookId }?.text ?: "Book $bookId"
                        Text(
                            "$bookName: ${chapters.sorted().joinToString(", ")}",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Reminders
            Text(
                L.current.l("Daily Reminders"),
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
            )

            Button(
                onClick = { showRemindersDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = L.current.l("Set Reminders"),
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(L.current.l("Set Reading Reminders"))
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Reset button
            Button(
                onClick = {
                    ReadingTracker.instance.resetReadingStatus()
                    ReadingPlanManager.instance.clearAllPlans()
                    expanded = false
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = L.current.l("Reset"),
                    modifier = Modifier.padding(end = 8.dp),
                    tint = MaterialTheme.colors.onSurface
                )
                Text(L.current.l("Reset Reading Data"))
            }
        }
    }

    // Reading Plan Dialog
    if (showReadingPlanDialog) {
        ReadingPlanDialog(
            onDismiss = { showReadingPlanDialog = false },
            onSelectPlan = { planType ->
                ReadingPlanManager.instance.startPlan(planType)
                showReadingPlanDialog = false
            }
        )
    }

    // Reminders Dialog
    if (showRemindersDialog) {
        RemindersDialog(
            onDismiss = { showRemindersDialog = false }
        )
    }
}

/**
 * Displays a statistic item with a value and label.
 */
@Composable
private fun StatisticItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(4.dp)
            .border(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Displays a reading plan item with progress.
 */
@Composable
private fun ReadingPlanItem(plan: ReadingPlan) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(8.dp)
    ) {
        Text(
            text = plan.name,
            style = MaterialTheme.typography.subtitle2,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = plan.description,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Progress bar
        LinearProgressIndicator(
            progress = plan.getProgress() / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .height(8.dp)
        )

        // Progress details
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${plan.getProgress()}${L.current.l("% Complete")}",
                style = MaterialTheme.typography.caption
            )

            Text(
                text = "${plan.getDaysRemaining()} ${L.current.l("days left")}",
                style = MaterialTheme.typography.caption
            )
        }

        // Current reading
        val currentReading = plan.getCurrentReading()
        if (currentReading != null) {
            val bookName = bookList.find { it.id == currentReading.bookId }?.text ?: "${L.current.l("Book")} ${currentReading.bookId}"
            val chapterRange = if (currentReading.chapterStart == currentReading.chapterEnd) {
                "${L.current.l("Chapter")} ${currentReading.chapterStart}"
            } else {
                "${L.current.l("Chapters")} ${currentReading.chapterStart}-${currentReading.chapterEnd}"
            }

            Text(
                text = "${L.current.l("Today's Reading:")} $bookName $chapterRange",
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Dialog for selecting a reading plan.
 */
@Composable
private fun ReadingPlanDialog(
    onDismiss: () -> Unit,
    onSelectPlan: (ReadingPlanType) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colors.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    L.current.l("Select Reading Plan"),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ReadingPlanOption(
                    title = L.current.l("Bible in a Year"),
                    description = L.current.l("Read through the entire Bible in 365 days"),
                    onClick = { onSelectPlan(ReadingPlanType.BIBLE_IN_A_YEAR) }
                )

                ReadingPlanOption(
                    title = L.current.l("New Testament in 90 Days"),
                    description = L.current.l("Read through the New Testament in 90 days"),
                    onClick = { onSelectPlan(ReadingPlanType.NEW_TESTAMENT_IN_90_DAYS) }
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 16.dp)
                ) {
                    Text(L.current.l("Cancel"))
                }
            }
        }
    }
}

/**
 * A selectable reading plan option.
 */
@Composable
private fun ReadingPlanOption(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        elevation = 2.dp,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = description,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Dialog for setting reading reminders.
 */
@Composable
private fun RemindersDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colors.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    L.current.l("Reading Reminders"),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    L.current.l("Reminder functionality will be implemented in a future update. This would allow you to set daily reminders to keep up with your reading plans."),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 16.dp)
                ) {
                    Text(L.current.l("Close"))
                }
            }
        }
    }
}
