import alerts.Alert
import alerts.AlertGenerator
import tasks.AlertTask
import tasks.RepeatableTask
import tasks.Task
import tasks.TaskPrototype
import java.util.concurrent.RunnableScheduledFuture
import java.util.concurrent.ScheduledFuture

actual class Timer actual constructor(actual val properties: TimerProperties) {
    private val tickTime = 10.milliseconds

    private var state = TimerState.STOPPED

    private val taskExecutor = TaskExecutor()

    private var timerTaskToFutureMap: Map<Task, ScheduledFuture<*>> = HashMap()

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
        elapsedTime = 0.milliseconds
        timerTaskToFutureMap = properties.taskPrototypes
            .plus(onTickPrototype)
            .map(TaskPrototype::getTask)
            .map { it to schedule(it) }
            .toMap()

        onStart?.invoke()
    }

    actual fun stop() = moveToState(TimerState.STOPPED) {
        taskExecutor.shutdownGracefully()

        onStop?.invoke()
    }

    actual fun resume() = moveToState(TimerState.RESUMED) {
        timerTaskToFutureMap = timerTaskToFutureMap.keys
            .map { it to schedule(it) }
            .toMap()

        onResume?.invoke()
    }

    actual fun pause() = moveToState(TimerState.PAUSED) {
        taskExecutor.shutdownAbruptly()

        timerTaskToFutureMap = timerTaskToFutureMap
            .mapValues { (_, future) -> future as RunnableScheduledFuture<*> }
            .filterValues { future -> future.isPeriodic || !future.isDone }
            .toMutableMap()

        onPause?.invoke()
    }

    actual var onStart: (() -> Unit)? = null

    actual var onStop: (() -> Unit)? = null

    actual var onPause: (() -> Unit)? = null

    actual var onResume: (() -> Unit)? = null

    actual var onTick: (() -> Unit)? = null

    private val onTickPrototype = TaskPrototype(RepeatableTask(tickTime, 0.milliseconds, null, object : AlertGenerator {
        private val alert = object : Alert {
            override fun alert() {
                if (taskExecutor.isShutdown) {
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
        }

        override fun generate(task: AlertTask, vararg params: Any): Alert {
            elapsedTime = task.executionTimeInMillis

            return alert
        }
    }))

    private fun schedule(task: Task): ScheduledFuture<*> {
        return if (task is RepeatableTask) {
            val newDelay = if (task.executionTimeInMillis.equals(0L)) {
                task.repeatFrom - elapsedTime
            } else {
                task.executionTimeInMillis + task.repeatEvery - elapsedTime
            }

            taskExecutor.scheduleAtFixedRate(task, newDelay)
        } else {
            taskExecutor.schedule(task, task.executionTimeInMillis - elapsedTime)
        }
    }

    private fun moveToState(newState: TimerState, block: () -> Unit) {
        if (!state.allowedStates.contains(newState.name)) {
            throw JvmTimerError("Can't move from $state to $newState timer state")
        }

        state = newState
        block.invoke()
    }
}

enum class TimerState(vararg val allowedStates: String) {
    STARTED("PAUSED", "STOPPED"),
    STOPPED("STARTED"),
    PAUSED("RESUMED", "STOPPED"),
    RESUMED("PAUSED", "STOPPED")
}
