import alerts.Alert
import alerts.AlertFactory
import alerts.sound.Sound
import tasks.AfterElapsedTimeTask
import tasks.RepeatableTask
import tasks.Task

class TimerPropertiesBuilder(private val durationInMillis: MillisecondsTimeUnit) {
    private var tickIntervalInMillis: MillisecondsTimeUnit = 10.MILLISECONDS

    private var finalAlarmSound: Sound? = Sound.WISE_MASTER

    private val tasks: MutableList<Task> = ArrayList()

    fun tickInterval(timeInMillis: MillisecondsTimeUnit) = apply {
        if (timeInMillis > durationInMillis || timeInMillis.equals(0)) {
            throw TimerPropertiesInitError("Tick interval can't be zero or exceed timer duration")
        }

        this.tickIntervalInMillis = timeInMillis
    }

    fun finalAlarm(sound: Sound?) = apply { finalAlarmSound = sound }

    fun afterPassed(timeInMillis: MillisecondsTimeUnit, alert: Alert) = apply {
        if (timeInMillis > durationInMillis) {
            throw TimerPropertiesInitError("Task execution time can't exceed timer duration")
        }

        val afterElapsedTimeTask = AfterElapsedTimeTask(timeInMillis, alert)
        tasks.add(afterElapsedTimeTask)
    }

    fun beforeLeft(timeInMillis: MillisecondsTimeUnit, alert: Alert) = apply {
        if (timeInMillis > durationInMillis) {
            throw TimerPropertiesInitError("Task execution time can't exceed timer duration")
        }

        val beforeTimeLeftTask = AfterElapsedTimeTask(durationInMillis - timeInMillis, alert)
        tasks.add(beforeTimeLeftTask)
    }

    fun repeatEvery(
        timeInMillis: MillisecondsTimeUnit,
        alert: Alert,
        delayInMillis: MillisecondsTimeUnit = timeInMillis,
        finishTimeInMillis: MillisecondsTimeUnit = durationInMillis
    ) = apply {
        if (timeInMillis > durationInMillis) {
            throw TimerPropertiesInitError("Task execution time can't exceed timer duration")
        }

        if (delayInMillis > durationInMillis) {
            throw TimerPropertiesInitError("Task delay time can't exceed timer duration")
        }

        if (finishTimeInMillis > durationInMillis) {
            throw TimerPropertiesInitError("Task finish time can't exceed timer duration")
        }

        val afterElapsedTimeTask = AfterElapsedTimeTask(timeInMillis, alert)
        val repeatableTask = RepeatableTask(afterElapsedTimeTask, delayInMillis, finishTimeInMillis)
        tasks.add(repeatableTask)
    }

    fun remindAfterFinishEvery(timeInMillis: MillisecondsTimeUnit, alert: Alert) = apply {
        if (timeInMillis > durationInMillis || timeInMillis.equals(0)) {
            throw TimerPropertiesInitError("Task execution time can't be zero or exceed timer duration")
        }

        val afterElapsedTimeTask = AfterElapsedTimeTask(timeInMillis, alert)
        val repeatableTask = RepeatableTask(afterElapsedTimeTask, durationInMillis + timeInMillis)
        tasks.add(repeatableTask)
    }

    fun build(): TimerProperties {
        if (durationInMillis <= 0) {
            throw TimerPropertiesInitError("Timer duration can't be zero or negative")
        }

        if (durationInMillis > 20.DAYS) {
            throw TimerPropertiesInitError("Timer duration can't be exceed 20 days")
        }

        if (finalAlarmSound != null) {
            val timerExpiredTask = AfterElapsedTimeTask(durationInMillis, AlertFactory.getSoundAlert(finalAlarmSound!!))
            tasks.add(timerExpiredTask)
        }

        return TimerProperties(durationInMillis, tickIntervalInMillis, tasks)
    }
}
