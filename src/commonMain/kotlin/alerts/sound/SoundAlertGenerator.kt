package alerts.sound

import alerts.Alert
import alerts.AlertGenerator
import tasks.AlertTask

class SoundAlertGenerator(private val sound: Sound) : AlertGenerator {
    override fun generate(task: AlertTask, vararg params: Any): Alert {
        return SoundAlert(sound)
    }
}
