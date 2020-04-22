import alerts.sound.JvmSoundPlayer
import alerts.sound.Player
import alerts.voice.JvmSpeechSynthesizer
import alerts.voice.Speaker

actual object DependenciesFactory {
    actual fun getPlayer(): Player {
        return JvmSoundPlayer
    }

    actual fun getSpeaker(): Speaker {
        return JvmSpeechSynthesizer
    }
}
