import alerts.AlertGenerator
import alerts.AlertGenerators
import alerts.sound.Sound
import tasks.AfterElapsedTimeTask
import tasks.BeforeTimeLeftTask
import tasks.RepeatableTask
import tasks.Task

class TimerPropertiesBuilder(private val durationInMillis: MillisecondsTimeUnit) {
    private var tickIntervalInMillis: MillisecondsTimeUnit = 10.MILLISECONDS

    private var finalAlarmSound: Sound? = Sound.WISE_MASTER

    private val tasks: MutableList<Task> = ArrayList()

    fun tickInterval(timeInMillis: MillisecondsTimeUnit) = apply {
        if (timeInMillis > durationInMillis) {
            throw TimerPropertiesInitError("Tick interval can't exceed timer duration")
        }

        this.tickIntervalInMillis = timeInMillis
    }

    fun finalAlarm(sound: Sound?) = apply { finalAlarmSound = sound }

    fun task(task: Task) = apply { tasks.add(task) }

    fun afterPassed(timeInMillis: MillisecondsTimeUnit, generator: AlertGenerator) = apply {
        if (timeInMillis > durationInMillis) {
            throw TimerPropertiesInitError("Task execution time can't exceed timer duration")
        }

        val afterElapsedTimeTask = AfterElapsedTimeTask(timeInMillis, generator)
        tasks.add(afterElapsedTimeTask)
    }

    fun beforeLeft(timeInMillis: MillisecondsTimeUnit, generator: AlertGenerator) = apply {
        if (timeInMillis > durationInMillis) {
            throw TimerPropertiesInitError("Task execution time can't exceed timer duration")
        }

        val beforeTimeLeftTask = BeforeTimeLeftTask(timeInMillis, durationInMillis, generator)
        tasks.add(beforeTimeLeftTask)
    }

    fun repeatEvery(
        timeInMillis: MillisecondsTimeUnit,
        generator: AlertGenerator,
        delayInMillis: MillisecondsTimeUnit?,
        finishTimeInMillis: MillisecondsTimeUnit?
    ) = apply {
        repeatEvery(timeInMillis, delayInMillis ?: timeInMillis, finishTimeInMillis ?: durationInMillis, generator)
    }

    fun repeatEvery(
        timeInMillis: MillisecondsTimeUnit,
        delayInMillis: MillisecondsTimeUnit = timeInMillis,
        finishTimeInMillis: MillisecondsTimeUnit = durationInMillis,
        generator: AlertGenerator
    ) = apply {
        if (timeInMillis >= durationInMillis || timeInMillis.equals(0)) {
            throw TimerPropertiesInitError("Task execution time can't be zero or exceed timer duration")
        }

        if (delayInMillis >= durationInMillis) {
            throw TimerPropertiesInitError("Task delay time can't exceed timer duration")
        }

        if (finishTimeInMillis > durationInMillis) {
            throw TimerPropertiesInitError("Task finish time can't exceed timer duration")
        }

        val repeatableTask = RepeatableTask(timeInMillis, delayInMillis, finishTimeInMillis, generator)
        tasks.add(repeatableTask)
    }

    fun remindAfterFinishEvery(timeInMillis: MillisecondsTimeUnit, generator: AlertGenerator) = apply {
        if (timeInMillis.equals(0)) {
            throw TimerPropertiesInitError("Task execution time can't be zero")
        }

        val repeatableTask = RepeatableTask(timeInMillis, durationInMillis + timeInMillis, generator = generator)
        tasks.add(repeatableTask)
    }

    fun build(): TimerProperties {
        if (durationInMillis <= 0 || durationInMillis > 20.DAYS) {
            throw TimerPropertiesInitError("Timer duration can't be zero or exceed 20 days")
        }

        if (finalAlarmSound != null) {
            val timerExpiredTask = AfterElapsedTimeTask(
                durationInMillis, AlertGenerators.getSoundAlertGenerator(finalAlarmSound!!)
            )
            tasks.add(timerExpiredTask)
        }

        return TimerProperties(durationInMillis, tickIntervalInMillis, tasks)
    }
}
