package alerts.sound

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

object JvmSoundPlayer : Player {
    // TODO still not sure it's ok
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

// TODO wrap null pointer
fun Clip.open(sound: Sound) {
    val resourceURL = sound.javaClass.classLoader.getResource(sound.resourceName)
    val audioInputStream = AudioSystem.getAudioInputStream(resourceURL)
    open(audioInputStream)
}
