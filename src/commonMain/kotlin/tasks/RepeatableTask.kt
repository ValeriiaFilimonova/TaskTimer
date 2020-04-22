package tasks

import MillisecondsTimeUnit
import alerts.Alert
import times

class RepeatableTask(
    val repeatEvery: MillisecondsTimeUnit,
    val repeatFrom: MillisecondsTimeUnit,
    val repeatTill: MillisecondsTimeUnit? = null,
    override val alert: Alert
) : TaskWithAlert {

    private var executionCounter: Int = 0

    override val executionTimeInMillis: MillisecondsTimeUnit
        get() = repeatEvery * executionCounter

    override fun execute() {
        executionCounter += 1

        alert.alert()
    }
}
