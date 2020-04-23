package tasks

import alerts.Alert

abstract class AlertTask: Task {
    abstract val getAlert: () -> Alert

    override fun execute() {
        getAlert().alert()
    }
}
