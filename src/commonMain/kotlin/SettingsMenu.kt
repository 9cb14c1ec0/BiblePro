import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import icons.CommonIcons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import backup.BackupManager
import backup.PlatformFilePicker
import locale.L
import phonetics.PhoneticLanguage
import phonetics.PhoneticSettings
import theme.ThemeMode
import theme.ThemeState

/**
 * Settings menu with theme, phonetics, and import/export functionality.
 */
@Composable
fun SettingsMenu(
    themeState: ThemeState? = null,
    phoneticSettings: PhoneticSettings? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var isResultSuccess by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(16.dp)) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                CommonIcons.Settings,
                contentDescription = L.current.l("Settings"),
                tint = MaterialTheme.colors.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
                .width(IntrinsicSize.Min)
                .widthIn(min = 220.dp)
        ) {
            // Title
            Text(
                L.current.l("Settings"),
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(8.dp)
            )

            Divider()

            // Theme section
            if (themeState != null) {
                Text(
                    L.current.l("Appearance"),
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 4.dp)
                )

                // Light Mode
                DropdownMenuItem(
                    onClick = {
                        themeState.themeMode = ThemeMode.LIGHT
                    }
                ) {
                    Icon(
                        imageVector = CommonIcons.LightMode,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = if (themeState.themeMode == ThemeMode.LIGHT)
                            MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                    )
                    Text(
                        L.current.l("Light Mode"),
                        color = if (themeState.themeMode == ThemeMode.LIGHT)
                            MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                    )
                }

                // Dark Mode
                DropdownMenuItem(
                    onClick = {
                        themeState.themeMode = ThemeMode.DARK
                    }
                ) {
                    Icon(
                        imageVector = CommonIcons.DarkMode,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = if (themeState.themeMode == ThemeMode.DARK)
                            MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                    )
                    Text(
                        L.current.l("Dark Mode"),
                        color = if (themeState.themeMode == ThemeMode.DARK)
                            MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                    )
                }

                // System Default
                DropdownMenuItem(
                    onClick = {
                        themeState.themeMode = ThemeMode.SYSTEM
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = if (themeState.themeMode == ThemeMode.SYSTEM)
                            MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                    )
                    Text(
                        L.current.l("System Default"),
                        color = if (themeState.themeMode == ThemeMode.SYSTEM)
                            MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }

            // Phonetics section
            if (phoneticSettings != null) {
                Text(
                    L.current.l("Phonetics"),
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
                )

                // Toggle phonetics
                DropdownMenuItem(
                    onClick = {
                        phoneticSettings.togglePhonetics()
                        if (phoneticSettings.showPhonetics && phoneticSettings.language == PhoneticLanguage.NONE) {
                            phoneticSettings.changeLanguage(PhoneticLanguage.SPANISH)
                        }
                    }
                ) {
                    Icon(
                        imageVector = CommonIcons.Language,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = if (phoneticSettings.showPhonetics)
                            MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                    )
                    Text(
                        if (phoneticSettings.showPhonetics) L.current.l("Hide Phonetics") else L.current.l("Show Phonetics")
                    )
                }

                // Language selection (only show if phonetics are enabled)
                if (phoneticSettings.showPhonetics) {
                    DropdownMenuItem(
                        onClick = {
                            phoneticSettings.changeLanguage(PhoneticLanguage.SPANISH)
                        }
                    ) {
                        Spacer(modifier = Modifier.width(32.dp))
                        Text(
                            L.current.l("Spanish Phonetics"),
                            color = if (phoneticSettings.language == PhoneticLanguage.SPANISH)
                                MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }

            // Backup & Restore section
            Text(
                L.current.l("Backup & Restore"),
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
            )

            // Export Data
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    showExportDialog = true
                }
            ) {
                Icon(
                    Icons.Filled.Upload,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(L.current.l("Export Data"))
            }

            // Import Data
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    showImportDialog = true
                }
            ) {
                Icon(
                    Icons.Filled.Download,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(L.current.l("Import Data"))
            }
        }
    }

    // Export confirmation dialog
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { success, message ->
                showExportDialog = false
                isResultSuccess = success
                resultMessage = message
                showResultDialog = true
            }
        )
    }

    // Import confirmation dialog
    if (showImportDialog) {
        ImportDialog(
            onDismiss = { showImportDialog = false },
            onImport = { success, message ->
                showImportDialog = false
                isResultSuccess = success
                resultMessage = message
                showResultDialog = true
            }
        )
    }

    // Result dialog
    if (showResultDialog) {
        ResultDialog(
            isSuccess = isResultSuccess,
            message = resultMessage,
            onDismiss = { showResultDialog = false }
        )
    }
}

/**
 * Dialog for confirming and executing export.
 */
@Composable
private fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (success: Boolean, message: String) -> Unit
) {
    val filePicker = remember { PlatformFilePicker() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colors.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    L.current.l("Export Data"),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    L.current.l("This will export your notes, reading progress, and reading plans to a JSON file."),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(L.current.l("Cancel"))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val defaultFilename = BackupManager.getDefaultFilename()
                            filePicker.pickExportLocation(defaultFilename) { path ->
                                if (path != null) {
                                    val jsonContent = BackupManager.exportToJson()
                                    val success = filePicker.writeFile(path, jsonContent)
                                    if (success) {
                                        onExport(true, "${L.current.l("Data exported successfully to:")} $path")
                                    } else {
                                        onExport(false, L.current.l("Failed to write export file"))
                                    }
                                } else {
                                    onDismiss()
                                }
                            }
                        }
                    ) {
                        Text(L.current.l("Export"))
                    }
                }
            }
        }
    }
}

/**
 * Dialog for confirming and executing import.
 */
@Composable
private fun ImportDialog(
    onDismiss: () -> Unit,
    onImport: (success: Boolean, message: String) -> Unit
) {
    val filePicker = remember { PlatformFilePicker() }
    var replaceExisting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colors.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    L.current.l("Import Data"),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    L.current.l("This will import notes, reading progress, and reading plans from a backup file."),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = replaceExisting,
                        onCheckedChange = { replaceExisting = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        L.current.l("Replace existing data"),
                        style = MaterialTheme.typography.body2
                    )
                }

                if (replaceExisting) {
                    Text(
                        L.current.l("Warning: This will delete all existing notes, reading progress, and reading plans before importing."),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(L.current.l("Cancel"))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            filePicker.pickImportFile { path ->
                                if (path != null) {
                                    val jsonContent = filePicker.readFile(path)
                                    if (jsonContent != null) {
                                        val success = BackupManager.importFromJson(jsonContent, replaceExisting)
                                        if (success) {
                                            onImport(true, L.current.l("Data imported successfully"))
                                        } else {
                                            onImport(false, L.current.l("Failed to parse backup file"))
                                        }
                                    } else {
                                        onImport(false, L.current.l("Failed to read backup file"))
                                    }
                                } else {
                                    onImport(false, L.current.l("No backup file found"))
                                }
                            }
                        }
                    ) {
                        Text(L.current.l("Import"))
                    }
                }
            }
        }
    }
}

/**
 * Dialog for showing import/export result.
 */
@Composable
private fun ResultDialog(
    isSuccess: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colors.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    if (isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                    contentDescription = null,
                    tint = if (isSuccess) MaterialTheme.colors.primary else MaterialTheme.colors.error,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    if (isSuccess) L.current.l("Success") else L.current.l("Error"),
                    style = MaterialTheme.typography.h6
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    message,
                    style = MaterialTheme.typography.body1
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) {
                    Text(L.current.l("OK"))
                }
            }
        }
    }
}