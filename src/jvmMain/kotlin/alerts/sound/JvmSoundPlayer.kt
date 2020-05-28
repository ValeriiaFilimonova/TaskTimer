package alerts.sound

import JvmTimerError
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

// TODO add loop and potentially refactor this logic
object JvmSoundPlayer : Player {
    override fun playOnce(sound: Sound) {
        GlobalScope.launch {
            AudioSystem.getClip().use {
                it.open(sound)
                it.start()
                delay(sound.duration)
                it.stop()
                it.close()
            }
        }
    }
}

fun Clip.open(sound: Sound) {
    val resourceURL = sound.javaClass.classLoader.getResource(sound.resourceName)

    if (resourceURL == null) {
        throw JvmTimerError("Sound is not found: ${sound.prettyName}")
    }

    val audioInputStream = AudioSystem.getAudioInputStream(resourceURL)
    open(audioInputStream)
}
