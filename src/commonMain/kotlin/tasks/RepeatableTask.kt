package tasks

import MILLISECONDS
import MillisecondsTimeUnit
import alerts.Alert
import alerts.AlertGenerator
import plus
import times

class RepeatableTask(
    val repeatEvery: MillisecondsTimeUnit,
    val repeatFrom: MillisecondsTimeUnit,
    val repeatTill: MillisecondsTimeUnit? = null,
    private val generator: AlertGenerator
) : AlertTask() {

    constructor(
        repeatEvery: MillisecondsTimeUnit,
        repeatFrom: MillisecondsTimeUnit,
        repeatTill: MillisecondsTimeUnit? = null,
        alert: Alert
    ) : this(repeatEvery, repeatFrom, repeatTill, object : AlertGenerator {
        override fun generate(task: AlertTask, vararg params: Any): Alert = alert
    })

    private var executionCounter: Int = 0

    override var executionTimeInMillis: MillisecondsTimeUnit = 0.MILLISECONDS
        private set

    override val getAlert: () -> Alert
        get() = { generator.generate(this) }

    override fun execute() {
        executionTimeInMillis = repeatEvery * executionCounter + repeatFrom
        executionCounter += 1

        super.execute()
    }
}
