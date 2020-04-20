package tasks

import alerts.Alert

class AfterElapsedTimeTask(
    executionTimeInMillis: Long,
    override val alert: Alert
) : TaskWithAlert {

    override var executionTimeInMillis: Long = executionTimeInMillis
        private set

    override fun execute() {
        alert.alert()
    }
}
