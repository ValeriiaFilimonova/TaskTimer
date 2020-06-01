import alerts.sound.Sound
import picocli.CommandLine
import ui.commands.TimerApplicationCommand
import ui.converters.SoundConverter
import ui.converters.TimeDurationConverter
import ui.handlers.ExecutionExceptionHandler
import ui.handlers.ParameterExceptionHandler
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    val appCommand = TimerApplicationCommand()
    val commandLine = CommandLine(appCommand).apply {
        val terminalScreen = appCommand.terminalScreen.also {
            it.setResizeListener { size ->
                this.usageHelpWidth = size.columns
            }
        }

        this.isCaseInsensitiveEnumValuesAllowed = true
        this.isUsageHelpAutoWidth = true
        this.executionStrategy = CommandLine.RunLast()
        this.out = terminalScreen.getOutput()
        this.err = terminalScreen.getOutput()

        this.executionExceptionHandler = ExecutionExceptionHandler()
        this.parameterExceptionHandler = ParameterExceptionHandler()

        registerConverter(MillisecondsTimeUnit::class.java, TimeDurationConverter())
        registerConverter(Sound::class.java, SoundConverter())
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
