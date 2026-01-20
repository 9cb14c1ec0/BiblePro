package verse

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset

/**
 * Modifier that triggers the callback on right-click (desktop) or is a no-op (Android).
 * Long-press is handled separately via combinedClickable.
 * The callback receives the click position as a DpOffset.
 */
expect fun Modifier.onRightClick(onClick: (DpOffset) -> Unit): Modifier