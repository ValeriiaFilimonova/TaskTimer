package alerts.print

import alerts.AlertGenerationError
import alerts.AlertGenerator
import alerts.voice.SayTimeAlertGenerator.Companion.getStringRepresentation
import tasks.AfterElapsedTimeTask
import tasks.AlertTask
import tasks.BeforeTimeLeftTask
import tasks.RepeatableTask
import kotlin.time.ExperimentalTime

@ExperimentalTime
class PrintTimeAlertGenerator : AlertGenerator {
    override fun generate(task: AlertTask, vararg params: Any): PrintAlert {
        val text = when (task) {
            is AfterElapsedTimeTask -> "${getStringRepresentation(task.executionTimeInMillis)} elapsed"
            is BeforeTimeLeftTask -> "${getStringRepresentation(task.beforeTime)} left"
            is RepeatableTask -> "${getStringRepresentation(task.executionTimeInMillis)} passed"
            else -> throw AlertGenerationError("Unsupported task type ${task::class.simpleName}")
        }

        return PrintAlert(text)
    }
}
