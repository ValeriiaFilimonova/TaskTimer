package ui.converters

import alerts.sound.Sound
import picocli.CommandLine
import ui.commands.ApplicationUsageError

class SoundConverter : CommandLine.ITypeConverter<Sound?> {
    override fun convert(soundString: String?): Sound? {
        if (soundString == null) {
            return null
        }

        try {
            if (soundString == "NONE") {
                return null
            }

            return Sound.valueOf(soundString)
        } catch (error: IllegalArgumentException) {
            throw ApplicationUsageError("Unknown sound $soundString")
        }
    }
}
