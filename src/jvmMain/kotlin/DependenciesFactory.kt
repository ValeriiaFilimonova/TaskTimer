import alerts.sound.JvmSoundPlayer
import alerts.sound.Player
import alerts.voice.JvmSpeechSynthesizer
import alerts.voice.Speaker
import ui.screen.JvmTerminalScreen
import ui.screen.TerminalScreen
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import kotlin.time.ExperimentalTime

actual object DependenciesFactory {
    actual fun getPlayer(): Player {
        return JvmSoundPlayer
    }

    actual fun getSpeaker(): Speaker {
        return JvmSpeechSynthesizer
    }

    @ExperimentalTime
    fun getTerminalScreen() : TerminalScreen {
        return JvmTerminalScreen
    }

    fun createExecutor(): ScheduledExecutorService {
        return Executors.newScheduledThreadPool(3)
    }
}
