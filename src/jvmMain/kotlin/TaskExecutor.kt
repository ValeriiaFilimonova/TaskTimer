import tasks.RepeatableTask
import tasks.Task
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class TaskExecutor {
    private var taskExecutionService: ScheduledExecutorService = DependenciesFactory.createExecutor()

    var isShutdown = false
        private set

    fun scheduleAtFixedRate(
        task: RepeatableTask,
        delayInMillis: MillisecondsTimeUnit = task.repeatFrom
    ): ScheduledFuture<*> {

        return taskExecutionService.scheduleAtFixedRate(
            { task.execute() }, delayInMillis.toLong(), task.repeatEvery.toLong(), TimeUnit.MILLISECONDS
        )
    }

    fun schedule(
        task: Task,
        executionTimeInMillis: MillisecondsTimeUnit = task.executionTimeInMillis
    ): ScheduledFuture<*> {

        return taskExecutionService.schedule(
            { task.execute() }, executionTimeInMillis.toLong(), TimeUnit.MILLISECONDS
        )
    }

    fun shutdownAbruptly() = shutdownlock {
        taskExecutionService.shutdownNow()
        taskExecutionService.awaitTermination(1, TimeUnit.SECONDS)
    }

    fun shutdownGracefully() = shutdownlock {
        taskExecutionService.shutdown()

        if (!taskExecutionService.awaitTermination(1, TimeUnit.SECONDS)) {
            taskExecutionService.shutdownNow()
        }
    }

    private fun shutdownlock(block: () -> Unit) {
        isShutdown = true
        block.invoke()
        taskExecutionService = DependenciesFactory.createExecutor()
        isShutdown = false
    }
}
