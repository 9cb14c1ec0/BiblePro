package verse

import androidx.compose.ui.Modifier

/**
 * No-op on Android - long-press is handled via combinedClickable.
 */
actual fun Modifier.onRightClick(onClick: () -> Unit): Modifier = this