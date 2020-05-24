import alerts.sound.JvmSoundPlayer
import alerts.sound.Player
import alerts.voice.JvmSpeechSynthesizer
import alerts.voice.Speaker
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

actual object DependenciesFactory {
    actual fun getPlayer(): Player {
        return JvmSoundPlayer
    }

    actual fun getSpeaker(): Speaker {
        return JvmSpeechSynthesizer
    }

    fun createExecutor(): ScheduledExecutorService {
        return Executors.newScheduledThreadPool(3)
    }
}
