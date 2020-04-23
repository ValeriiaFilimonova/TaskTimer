package alerts.voice

import SECONDS
import alerts.AlertGenerationError
import alerts.AlertGenerator
import div
import tasks.AfterElapsedTimeTask
import tasks.AlertTask
import tasks.BeforeTimeLeftTask
import tasks.RepeatableTask

class SayTimeAlertGenerator : AlertGenerator {
    // todo improve message generation algorithm
    override fun generate(task: AlertTask, vararg params: Any): VoiceAlert {
        val text = when (task) {
            is AfterElapsedTimeTask -> "${(task.executionTimeInMillis / 1.SECONDS).toLong()} seconds elapsed"
            is BeforeTimeLeftTask -> "${(task.beforeTime / 1.SECONDS).toLong()} seconds left"
            is RepeatableTask -> "${(task.executionTimeInMillis / 1.SECONDS).toLong()} seconds passed"
            else -> throw AlertGenerationError("Unsupported task type ${task::class.simpleName}")
        }

        return VoiceAlert(text)
    }
}
