import alerts.sound.JvmSoundPlayer
import alerts.sound.Player

actual object DependenciesFactory {
    actual fun getPlayer(): Player {
        return JvmSoundPlayer
    }
}
