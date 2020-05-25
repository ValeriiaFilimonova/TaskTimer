package tasks

import MillisecondsTimeUnit
import alerts.Alert
import alerts.AlertGenerator
import minus

class BeforeTimeLeftTask(
    val beforeTime: MillisecondsTimeUnit,
    private val timerDuration: MillisecondsTimeUnit,
    private val generator: AlertGenerator
) : AlertTask() {

    override val executionTimeInMillis: MillisecondsTimeUnit = timerDuration - beforeTime

    override val getAlert: () -> Alert
        get() = { generator.generate(this) }

    override fun clone(): Task {
        return BeforeTimeLeftTask(beforeTime, timerDuration, generator )
    }
}
