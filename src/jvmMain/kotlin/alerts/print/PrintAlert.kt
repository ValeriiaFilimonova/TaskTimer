package alerts.print

import alerts.Alert

class PrintAlert(private val text: String): Alert {
    override fun alert() {
        println(text)
    }
}
