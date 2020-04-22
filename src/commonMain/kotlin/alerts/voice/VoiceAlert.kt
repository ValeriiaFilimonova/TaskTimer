package alerts.voice

import DependenciesFactory
import alerts.Alert

class VoiceAlert(val text: String) : Alert {
    override fun alert() {
        DependenciesFactory.getSpeaker().say(text)
    }
}