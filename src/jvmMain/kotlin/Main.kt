import alerts.sound.Sound
import picocli.CommandLine
import picocli.CommandLine.ExitCode
import ui.commands.*
import ui.converters.SoundConverter
import ui.converters.TimeDurationConverter
import java.util.*
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main() {
    val isDebug = System.getenv("DEBUG") == true.toString()
    val commandPrefix = "timer> "
    val input: Scanner = Scanner(System.`in`).useDelimiter("\n")
    val appCommand = TimerApplicationCommand()

    val commandLine = CommandLine(appCommand).also {
        it.executionStrategy = CommandLine.RunLast()
        it.executionExceptionHandler = ExecutionExceptionHandler()
        it.parameterExceptionHandler = ParameterExceptionHandler()

        it.registerConverter(MillisecondsTimeUnit::class.java, TimeDurationConverter())
        it.registerConverter(Sound::class.java, SoundConverter())
    }

    commandLine.execute("help")

    if (isDebug) {
        print(commandPrefix)
    }

    while (true) {
        if (input.hasNext()) {
            val cmd: String = input.next()
            val args = cmd.split(Regex(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")).toTypedArray()

            val exitCode = commandLine.execute(*args)

            if (isDebug && exitCode == ExitCode.OK) {
                Thread.sleep(100)
                print(commandPrefix)
            }
        }
    }
}
