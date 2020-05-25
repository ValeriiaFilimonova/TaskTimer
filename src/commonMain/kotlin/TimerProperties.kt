import tasks.TaskPrototype

data class TimerProperties constructor(
    val durationInMillis: MillisecondsTimeUnit,
    val tickIntervalInMillis: MillisecondsTimeUnit,
    val taskPrototypes: Iterable<TaskPrototype>
)
