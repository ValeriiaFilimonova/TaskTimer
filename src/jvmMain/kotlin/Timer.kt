import alerts.Alert
import tasks.RepeatableTask
import tasks.Task
import java.util.concurrent.RunnableScheduledFuture
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.properties.Delegates

actual class Timer actual constructor(actual val properties: TimerProperties) {
    private val tickTime = 10.milliseconds

    private var state = TimerState.STOPPED

    private var taskExecutionService = DependenciesFactory.createExecutor()

    private var timerTaskToFutureMap: MutableMap<Task, ScheduledFuture<*>> = HashMap()

    init {
        if (properties.tickIntervalInMillis % tickTime != 0L) {
            throw JvmTimerError("Tick interval advised to be divisible by $tickTime")
        }
    }

    val duration: MillisecondsTimeUnit
        get() = properties.durationInMillis

    actual var elapsedTime = 0.milliseconds
        private set

    actual fun start() = moveToState(TimerState.STARTED) {
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

    actual fun stop() = moveToState(TimerState.STOPPED) {
        timerTaskToFutureMap.clear()
        taskExecutionService.shutdown()

        if (!taskExecutionService.awaitTermination(1, SECONDS)) {
            taskExecutionService.shutdownNow()
        }

        onStop?.invoke()
    }

    actual fun pause() = moveToState(TimerState.PAUSED) {
        taskExecutionService.shutdownNow()
        taskExecutionService.awaitTermination(1, SECONDS)

        timerTaskToFutureMap = timerTaskToFutureMap
            .mapValues { (_, future) -> future as RunnableScheduledFuture<*> }
            .filterValues { future -> future.isPeriodic || !future.isDone }
            .toMutableMap()

        onPause?.invoke()
    }

    actual fun resume() = moveToState(TimerState.RESUMED) {
        taskExecutionService = DependenciesFactory.createExecutor()

        timerTaskToFutureMap.keys
            .sortedBy { it.executionTimeInMillis }
            .forEach { task ->
                if (task is RepeatableTask) {
                    taskExecutionService.scheduleAtFixedRate(task, task.newDelay(elapsedTime))
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

    private val onTickTask = RepeatableTask(tickTime, 0.milliseconds, alert = object : Alert {
        private var iterations: Int by Delegates.observable(0) { _, _, newValue ->
            elapsedTime = tickTime * (newValue - 1)
        }

        override fun alert() {
            iterations++
            executeOnTick()
        }
    })

    private fun executeOnTick() {
        if (taskExecutionService.isShutdown) {
            return
        }

        if (elapsedTime > 0L && elapsedTime % properties.tickIntervalInMillis == 0L) {
            onTick?.invoke()
        }

        timerTaskToFutureMap
            .filterKeys { task -> task is RepeatableTask && task.repeatFrom < duration }
            .filterKeys { task -> task is RepeatableTask && task.repeatTill != null && task.repeatTill <= elapsedTime }
            .forEach { (_, future) -> future.cancel(true) }
    }

    private fun moveToState(newState: TimerState, block: () -> Unit) {
        if (!state.allowedStates.contains(newState.name)) {
            throw JvmTimerError("Can't move from $state to $newState timer state")
        }

        state = newState
        block.invoke()
    }

    private fun ScheduledExecutorService.scheduleAtFixedRate(
        task: RepeatableTask,
        delayInMillis: MillisecondsTimeUnit = task.repeatFrom
    ): ScheduledFuture<*> {

        return scheduleAtFixedRate(
            { task.execute() }, delayInMillis.toLong(), task.repeatEvery.toLong(), MILLISECONDS
        ).also { timerTaskToFutureMap[task] = it }
    }

    private fun ScheduledExecutorService.schedule(
        task: Task,
        executionTimeInMillis: MillisecondsTimeUnit = task.executionTimeInMillis
    ): ScheduledFuture<*> {

        return schedule(
            { task.execute() }, executionTimeInMillis.toLong(), MILLISECONDS
        ).also { timerTaskToFutureMap[task] = it }
    }

    private fun RepeatableTask.newDelay(elapsedTime: MillisecondsTimeUnit): MillisecondsTimeUnit {
        return if (executionTimeInMillis.equals(0L)) {
            repeatFrom - elapsedTime
        } else {
            executionTimeInMillis + repeatEvery - elapsedTime
        }
    }
}

enum class TimerState(vararg val allowedStates: String) {
    STARTED("PAUSED", "STOPPED"),
    STOPPED("STARTED"),
    PAUSED("RESUMED", "STOPPED"),
    RESUMED("PAUSED", "STOPPED")
}
