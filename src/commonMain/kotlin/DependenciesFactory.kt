import alerts.sound.Player
import alerts.voice.Speaker

expect object DependenciesFactory {
    fun getPlayer(): Player

    fun getSpeaker(): Speaker
}
