import tasks.RepeatableTask
import tasks.Task
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.MILLISECONDS

actual class Timer actual constructor(actual val properties: TimerProperties) {
    private var taskExecutionService = Executors.newScheduledThreadPool(3)

    private var scheduledTaskToTimerTaskMap: MutableMap<ScheduledFuture<*>, Task> = HashMap()

    private val onTickTask: RepeatableTask by lazy {
        RepeatableTask(object : Task {
            override var executionTimeInMillis: Long = 1.MILLISECONDS

            override fun execute() {
                executeOnTick()
            }
        }, 0)
    }

    val duration: Long
        get() = properties.durationInMillis

    actual var elapsedTime: Long = 0L

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

        onStop?.invoke()
    }

    actual fun pause() {
        // todo make it more safe
        scheduledTaskToTimerTaskMap = taskExecutionService
            .shutdownNow()
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
                val nextExecutionTime = task.executionCounter * task.executionTimeInMillis + task.repeatFromTimeInMillis
                taskExecutionService.scheduleAtFixedRate(task, nextExecutionTime - elapsedTime)
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
        elapsedTime += onTickTask.executionTimeInMillis

        if (elapsedTime % properties.tickIntervalInMillis == 0L) {
            onTick?.invoke()
        }

        scheduledTaskToTimerTaskMap
            .filterValues { task -> task != onTickTask } // todo check why it's needed
            .filterValues { task -> task is RepeatableTask && task.repeatFromTimeInMillis < duration }
            .filterValues { task -> task is RepeatableTask && task.repeatTillTimeInMillis != null && task.repeatTillTimeInMillis < elapsedTime }
            .forEach { (future) -> future.cancel(true) }
    }

    private fun ScheduledExecutorService.scheduleAtFixedRate(
        task: RepeatableTask,
        delayInMillis: Long = task.repeatFromTimeInMillis
    ): ScheduledFuture<*> {

        return scheduleAtFixedRate({ task.execute() }, delayInMillis, task.executionTimeInMillis, MILLISECONDS)
            .also { scheduledTaskToTimerTaskMap[it] = task }
    }

    private fun ScheduledExecutorService.schedule(
        task: Task,
        executionTimeInMillis: Long = task.executionTimeInMillis
    ): ScheduledFuture<*> {

        return schedule({ task.execute() }, executionTimeInMillis, MILLISECONDS)
            .also { scheduledTaskToTimerTaskMap[it] = task }
    }
}
