package alerts

import alerts.sound.Sound
import alerts.sound.SoundAlert

object AlertFactory {
    fun getSoundAlert(sound: Sound = Sound.TING_A_LING): SoundAlert {
        return SoundAlert(sound)
    }
}
