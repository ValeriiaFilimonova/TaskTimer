package alerts.sound

import alerts.Alert
import DependenciesFactory

class SoundAlert(val sound: Sound) : Alert {
    override fun alert() {
        DependenciesFactory.getPlayer().playOnce(sound)
    }
}
