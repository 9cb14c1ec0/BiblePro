package verse

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset

/**
 * No-op on Android - long-press is handled via combinedClickable.
 */
actual fun Modifier.onRightClick(onClick: (DpOffset) -> Unit): Modifier = this