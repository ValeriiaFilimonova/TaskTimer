package tasks

import MillisecondsTimeUnit
import alerts.Alert

class AfterElapsedTimeTask(
    executionTimeInMillis: MillisecondsTimeUnit,
    override val alert: Alert
) : TaskWithAlert {

    override var executionTimeInMillis: MillisecondsTimeUnit = executionTimeInMillis
        private set

    override fun execute() {
        alert.alert()
    }
}
