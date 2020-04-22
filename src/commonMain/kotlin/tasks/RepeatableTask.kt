package tasks

import MillisecondsTimeUnit

class RepeatableTask(
    private val task: Task,
    val repeatFromTimeInMillis: MillisecondsTimeUnit,
    val repeatTillTimeInMillis: MillisecondsTimeUnit? = null
) : Task by task {

    var executionCounter: Int = 0
        private set

    override fun execute() {
        executionCounter += 1

        task.execute()
    }
}
