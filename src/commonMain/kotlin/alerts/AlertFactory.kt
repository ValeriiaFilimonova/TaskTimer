package alerts

import alerts.sound.Sound
import alerts.sound.SoundAlert
import alerts.voice.VoiceAlert

object AlertFactory {
    fun getSoundAlert(sound: Sound = Sound.TING_A_LING): SoundAlert {
        return SoundAlert(sound)
    }

    fun getVoiceAlert(text: String): VoiceAlert {
        return VoiceAlert(text)
    }
}
