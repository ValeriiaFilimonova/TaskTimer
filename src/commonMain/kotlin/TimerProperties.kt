import tasks.Task

data class TimerProperties constructor(
    val durationInMillis: Long,
    val tickIntervalInMillis: Long,
    val tasks: Iterable<Task>
)
