package alerts.print

import alerts.Alert
import alerts.AlertGenerator
import tasks.AlertTask

class PrintAlertGenerator(private val text: String) : AlertGenerator {
    override fun generate(task: AlertTask, vararg params: Any): Alert {
        return PrintAlert(text)
    }
}
