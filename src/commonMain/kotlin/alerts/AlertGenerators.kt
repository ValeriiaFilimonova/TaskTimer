package alerts

import alerts.sound.Sound
import alerts.sound.SoundAlertGenerator
import alerts.voice.SayTimeAlertGenerator
import alerts.voice.VoiceAlertGenerator
import kotlin.time.ExperimentalTime

object AlertGenerators {
    fun getSoundAlertGenerator(sound: Sound) = SoundAlertGenerator(sound)

    fun getVoiceAlertGenerator(text: String) = VoiceAlertGenerator(text)

    @ExperimentalTime
    fun getSayTimeAlertGenerator() = SayTimeAlertGenerator()
}
