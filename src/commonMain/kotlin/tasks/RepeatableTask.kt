package tasks

class RepeatableTask(
    private val task: Task,
    val delayInMillis: Long
) : Task by task {

    var executionCounter = 0
        private set

    override fun execute() {
        executionCounter += 1

        task.execute()
    }
}
