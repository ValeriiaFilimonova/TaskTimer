import alerts.Alert
import alerts.AlertFactory
import alerts.sound.Sound
import tasks.AfterElapsedTimeTask
import tasks.RepeatableTask
import tasks.Task

class TimerPropertiesBuilder(private val durationInMillis: Long) {
    private var tickIntervalInMillis: Long = 10

    private var finalAlarmSound: Sound? = Sound.WISE_MASTER

    private val tasks: MutableList<Task> = ArrayList()

    fun tickInterval(timeInMillis: Long) = apply { this.tickIntervalInMillis = timeInMillis }

    fun finalAlarm(sound: Sound?) = apply { finalAlarmSound = sound }

    fun afterPassed(timeInMillis: Long, alert: Alert) = apply {
        val afterElapsedTimeTask = AfterElapsedTimeTask(timeInMillis, alert)
        tasks.add(afterElapsedTimeTask)
    }

    fun beforeLeft(timeInMillis: Long, alert: Alert) = apply {
        val beforeTimeLeftTask = AfterElapsedTimeTask(durationInMillis - timeInMillis, alert)
        tasks.add(beforeTimeLeftTask)
    }

    fun repeatEvery(timeInMillis: Long, alert: Alert, delayInMillis: Long = timeInMillis) = apply {
        val afterElapsedTimeTask = AfterElapsedTimeTask(timeInMillis, alert)
        val repeatableTask = RepeatableTask(afterElapsedTimeTask, delayInMillis)
        tasks.add(repeatableTask)
    }

    // todo fun repeatEvery from .. to

    // todo fun repeatEvery afterTimerElapsed

    fun build(): TimerProperties {
        //todo validate negative numbers
        if (durationInMillis <= 0) {
            throw TimerPropertiesInitError("Timer duration can't be negative")
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
