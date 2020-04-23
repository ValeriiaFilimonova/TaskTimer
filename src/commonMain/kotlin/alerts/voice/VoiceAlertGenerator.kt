package alerts.voice

import alerts.AlertGenerator
import tasks.AlertTask

class VoiceAlertGenerator(private val text: String) : AlertGenerator {
    override fun generate(task: AlertTask, vararg params: Any): VoiceAlert {
        return VoiceAlert(text)
    }
}
