package verse

import androidx.compose.ui.Modifier

/**
 * Modifier that triggers the callback on right-click (desktop) or is a no-op (Android).
 * Long-press is handled separately via combinedClickable.
 */
expect fun Modifier.onRightClick(onClick: () -> Unit): Modifier