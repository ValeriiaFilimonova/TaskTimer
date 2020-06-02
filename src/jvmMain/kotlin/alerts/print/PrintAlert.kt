package alerts.print

import alerts.Alert
import kotlin.time.ExperimentalTime

@ExperimentalTime
class PrintAlert(private val text: String): Alert {
    override fun alert() {
        DependenciesFactory.getTerminalScreen().print(text)
    }
}
