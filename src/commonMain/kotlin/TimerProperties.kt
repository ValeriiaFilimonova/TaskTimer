import tasks.Task

data class TimerProperties constructor(
    val durationInMillis: MillisecondsTimeUnit,
    val tickIntervalInMillis: MillisecondsTimeUnit,
    val tasks: Iterable<Task>
)
