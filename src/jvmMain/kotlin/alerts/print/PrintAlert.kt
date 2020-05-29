package alerts.print

import alerts.Alert
import ui.screen.JvmTerminalScreen
import kotlin.time.ExperimentalTime

@ExperimentalTime
class PrintAlert(private val text: String): Alert {
    override fun alert() {
        JvmTerminalScreen.printMessage(text)
    }
}
