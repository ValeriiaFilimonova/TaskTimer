package tasks

import MillisecondsTimeUnit
import alerts.Alert
import alerts.AlertGenerator

class AfterElapsedTimeTask(
    override val executionTimeInMillis: MillisecondsTimeUnit,
    private val generator: AlertGenerator
) : AlertTask() {

    override val getAlert: () -> Alert
        get() = { generator.generate(this) }

    override fun clone(): Task {
        return AfterElapsedTimeTask(executionTimeInMillis, generator)
    }
}
