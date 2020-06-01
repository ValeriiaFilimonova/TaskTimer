import alerts.sound.Sound
import picocli.CommandLine
import ui.commands.TimerApplicationCommand
import ui.converters.SoundConverter
import ui.converters.TimeDurationConverter
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    val appCommand = TimerApplicationCommand()
    val commandLine = CommandLine(appCommand).also {
        val terminalScreen = appCommand.terminalScreen.apply {
            setResizeListener {
//                it.usageHelpWidth = columns
            }
        }

        it.isCaseInsensitiveEnumValuesAllowed = true
        it.isUsageHelpAutoWidth = true
        it.usageHelpWidth = terminalScreen.getSize().columns
        it.executionStrategy = CommandLine.RunLast()
        it.out = terminalScreen.getOutput()
        it.err = terminalScreen.getOutput()

        it.registerConverter(MillisecondsTimeUnit::class.java, TimeDurationConverter())
        it.registerConverter(Sound::class.java, SoundConverter())
    }

    appCommand.run()
    commandLine.execute("help")

    while (true) {
        val commandInput = appCommand.readInput()

        if (commandInput == null) {
            Thread.sleep(10)
            continue
        }

        val args = commandInput.split(Regex(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")).toTypedArray()
        commandLine.execute(*args)
    }
}
