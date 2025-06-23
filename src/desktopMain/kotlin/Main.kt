import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import locale.L

@Composable
@Preview
fun DesktopPreview() {
    App()
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = L.current.l("BiblePro")) {
        App()
    }
}