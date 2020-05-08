import alerts.Alert
import tasks.RepeatableTask
import tasks.Task
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

actual class Timer actual constructor(actual val properties: TimerProperties) {
    private var state = TimerState.STOPPED

    private var taskExecutionService = Executors.newScheduledThreadPool(3)

    private var scheduledTaskToTimerTaskMap: MutableMap<ScheduledFuture<*>, Task> = HashMap()

    private val onTickTask = RepeatableTask(100.milliseconds, 0.milliseconds, alert = object : Alert {
        override fun alert() {
            executeOnTick()
        }
    })

    init {
        if (properties.tickIntervalInMillis <  200.milliseconds) {
            throw JvmTimerError("Tick interval can't be less than 200ms")
        }
    }

    val duration: MillisecondsTimeUnit
        get() = properties.durationInMillis

    actual var elapsedTime = 0.milliseconds

    actual fun start() {
        updateState(TimerState.STARTED)

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
        updateState(TimerState.STOPPED)

        taskExecutionService.shutdown()

        if (!taskExecutionService.awaitTermination(1, SECONDS)) {
            taskExecutionService.shutdownNow()
        }

        onStop?.invoke()
    }

    actual fun pause() {
        updateState(TimerState.PAUSED)

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
        updateState(TimerState.RESUMED)

        val tasks = ArrayList(scheduledTaskToTimerTaskMap.values)

        taskExecutionService = Executors.newScheduledThreadPool(3)
        scheduledTaskToTimerTaskMap.clear()

        for (task in tasks) {
            if (task is RepeatableTask) {
                if (task == onTickTask) {
                    taskExecutionService.scheduleAtFixedRate(onTickTask, 0.milliseconds)
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
            .filterValues { task -> task != onTickTask } // TODO check why it's needed
            .filterValues { task -> task is RepeatableTask && task.repeatFrom < duration }
            .filterValues { task -> task is RepeatableTask && task.repeatTill != null && task.repeatTill < elapsedTime }
            .forEach { (future) -> future.cancel(true) }
    }

    private fun updateState(newState: TimerState) {
        if (!state.allowedStates.contains(newState.name)) {
            throw JvmTimerError("Can't move from $state to $newState timer state")
        }

        state = newState
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

enum class TimerState(vararg val allowedStates: String) {
    STARTED("PAUSED", "STOPPED"),
    STOPPED("STARTED"),
    PAUSED("RESUMED", "STOPPED"),
    RESUMED("PAUSED", "STOPPED")
}
