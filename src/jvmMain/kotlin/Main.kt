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
    val application = TimerApplicationCommand()
    val commandLine = CommandLine(application).apply {
        val terminalScreen = application.terminalScreen.also {
            it.setResizeListener { size ->
                this.usageHelpWidth = size.columns
            }
        }

        this.isCaseInsensitiveEnumValuesAllowed = true
        this.isUsageHelpAutoWidth = true
        this.usageHelpWidth = terminalScreen.getSize().columns
        this.executionStrategy = CommandLine.RunLast()
        this.out = terminalScreen.getOutput()
        this.err = terminalScreen.getOutput()

        this.executionExceptionHandler = ExecutionExceptionHandler()
        this.parameterExceptionHandler = ParameterExceptionHandler()

        registerConverter(MillisecondsTimeUnit::class.java, TimeDurationConverter())
        registerConverter(Sound::class.java, SoundConverter())
    }

    application.run()
    commandLine.execute("help")

    while (true) {
        var args: Array<String>? = null

        try {
            val commandInput = application.readInput()

            if (commandInput == null) {
                Thread.sleep(10)
                continue
            }

            args = commandInput.split(Regex(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")).toTypedArray()
        } catch (e: Exception) {
            commandLine.executionExceptionHandler.handleExecutionException(e, commandLine, null)
        }

        if (args != null) {
            commandLine.execute(*args)
        }
    }
}
