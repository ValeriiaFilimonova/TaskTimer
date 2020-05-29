import alerts.sound.Sound
import picocli.CommandLine
import ui.commands.TimerApplicationCommand
import ui.converters.SoundConverter
import ui.converters.TimeDurationConverter
import ui.screen.JvmTerminalScreen
import kotlin.time.ExperimentalTime

// todo fix timer screen
@ExperimentalTime
fun main() {
    val appCommand = TimerApplicationCommand()
    val commandLine = CommandLine(appCommand).also {
        it.executionStrategy = CommandLine.RunLast()
        it.out = JvmTerminalScreen.printStream
        it.err = JvmTerminalScreen.printStream

        it.registerConverter(MillisecondsTimeUnit::class.java, TimeDurationConverter())
        it.registerConverter(Sound::class.java, SoundConverter())
    }

    JvmTerminalScreen.open()
    commandLine.execute("help")

    while (true) {
        val commandInput = JvmTerminalScreen.readCommand()

        if (commandInput == null) {
            Thread.sleep(10)
            continue
        }

        val args = commandInput.split(Regex(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")).toTypedArray()
        commandLine.execute(*args)
    }
}
