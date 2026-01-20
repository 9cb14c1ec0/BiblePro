package verse

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.onRightClick(onClick: (DpOffset) -> Unit): Modifier = this.pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            if (event.type == PointerEventType.Press && event.button == PointerButton.Secondary) {
                val position = event.changes.firstOrNull()?.position
                val dpOffset = if (position != null) {
                    DpOffset(position.x.dp / density, position.y.dp / density)
                } else {
                    DpOffset.Zero
                }
                onClick(dpOffset)
            }
        }
    }
}