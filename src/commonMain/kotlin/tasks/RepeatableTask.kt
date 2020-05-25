package tasks

import milliseconds
import MillisecondsTimeUnit
import alerts.Alert
import alerts.AlertGenerator
import plus
import times
import kotlin.properties.Delegates

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

    private var executionCounter: Int by Delegates.observable(0) { _, _, count ->
        executionTimeInMillis = repeatEvery * (count - 1) + repeatFrom
    }

    override var executionTimeInMillis: MillisecondsTimeUnit = 0.milliseconds
        private set

    override val getAlert: () -> Alert
        get() = { generator.generate(this) }

    override fun execute() {
        executionCounter++

        super.execute()
    }

    override fun clone(): Task {
        return RepeatableTask(repeatEvery, repeatFrom, repeatTill, generator)
    }
}
