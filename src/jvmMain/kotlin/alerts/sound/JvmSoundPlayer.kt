package alerts.sound

import JvmTimerError
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

object JvmSoundPlayer : Player {
    private val executor = Executors.newSingleThreadExecutor()
    private var future: Future<*>? = null
    private var clip: Clip? = null

    override fun playOnce(sound: Sound) {
        terminate()

        future = executor.submit {
            val newClip = AudioSystem.getClip().apply {
                open(sound)
                start()
            }
            clip = newClip
            Thread.sleep(sound.duration)
            newClip.stop()
            newClip.close()
        }
    }

    fun terminate() {
        future?.cancel(true)

        if (clip?.isRunning == true) {
            clip!!.stop()
            clip!!.close()
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
