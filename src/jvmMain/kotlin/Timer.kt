import alerts.Alert
import tasks.RepeatableTask
import tasks.Task
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

actual class Timer actual constructor(actual val properties: TimerProperties) {
    private var taskExecutionService = Executors.newScheduledThreadPool(3)

    private var scheduledTaskToTimerTaskMap: MutableMap<ScheduledFuture<*>, Task> = HashMap()

    private val onTickTask = RepeatableTask(100.MILLISECONDS, 0.MILLISECONDS, alert = object : Alert {
        override fun alert() {
            executeOnTick()
        }
    })

    val duration: MillisecondsTimeUnit
        get() = properties.durationInMillis

    actual var elapsedTime = 0.MILLISECONDS

    actual fun start() {
        properties.tasks
            .plus(onTickTask)
            .sortedBy { it.executionTimeInMillis }
            .forEach { task ->
                if (task is RepeatableTask) {
                    taskExecutionService.scheduleAtFixedRate(task)
                } else {

                    taskExecutionService.schedule(task)
                }
            }

        onStart?.invoke()
    }

    actual fun stop() {
        taskExecutionService.shutdown()

        if (!taskExecutionService.awaitTermination(1, SECONDS)) {
            taskExecutionService.shutdownNow()
        }

        onStop?.invoke()
    }

    actual fun pause() {
        val shutDownTasks = taskExecutionService.shutdownNow()

        taskExecutionService.awaitTermination(1, SECONDS)

        scheduledTaskToTimerTaskMap = shutDownTasks
            .map { future -> future as ScheduledFuture<*> }
            .map { future -> Pair(future, scheduledTaskToTimerTaskMap[future]!!) }
            .toMap()
            .toMutableMap()

        onPause?.invoke()
    }

    actual fun resume() {
        val tasks = ArrayList(scheduledTaskToTimerTaskMap.values)

        taskExecutionService = Executors.newScheduledThreadPool(3)
        scheduledTaskToTimerTaskMap.clear()

        for (task in tasks) {
            if (task is RepeatableTask) {
                if (task == onTickTask) {
                    taskExecutionService.scheduleAtFixedRate(onTickTask, 0.MILLISECONDS)
                } else {
                    val newDelay = task.executionTimeInMillis + task.repeatFrom - elapsedTime
                    taskExecutionService.scheduleAtFixedRate(task, newDelay)
                }
            } else {
                taskExecutionService.schedule(task, task.executionTimeInMillis - elapsedTime)
            }
        }

        onResume?.invoke()
    }

    actual var onStart: (() -> Unit)? = null

    actual var onStop: (() -> Unit)? = null

    actual var onPause: (() -> Unit)? = null

    actual var onResume: (() -> Unit)? = null

    actual var onTick: (() -> Unit)? = null

    private fun executeOnTick() {
        if (taskExecutionService.isShutdown) {
            return
        }

        elapsedTime += onTickTask.repeatEvery

        if (elapsedTime % properties.tickIntervalInMillis == 0L) {
            onTick?.invoke()
        }

        scheduledTaskToTimerTaskMap
            .filterValues { task -> task != onTickTask } // todo check why it's needed
            .filterValues { task -> task is RepeatableTask && task.repeatFrom < duration }
            .filterValues { task -> task is RepeatableTask && task.repeatTill != null && task.repeatTill < elapsedTime }
            .forEach { (future) -> future.cancel(true) }
    }

    private fun ScheduledExecutorService.scheduleAtFixedRate(
        task: RepeatableTask,
        delayInMillis: MillisecondsTimeUnit = task.repeatFrom
    ): ScheduledFuture<*> {

        return scheduleAtFixedRate(
            { task.execute() },
            delayInMillis.toLong(),
            task.repeatEvery.toLong(),
            MILLISECONDS
        )
            .also { scheduledTaskToTimerTaskMap[it] = task }
    }

    private fun ScheduledExecutorService.schedule(
        task: Task,
        executionTimeInMillis: MillisecondsTimeUnit = task.executionTimeInMillis
    ): ScheduledFuture<*> {

        return schedule({ task.execute() }, executionTimeInMillis.toLong(), MILLISECONDS)
            .also { scheduledTaskToTimerTaskMap[it] = task }
    }
}
