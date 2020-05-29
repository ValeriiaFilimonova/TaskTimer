package ui.commands

import Timer
import picocli.CommandLine.*
import ui.screen.JvmTerminalScreen
import kotlin.time.ExperimentalTime

@Command(
    name = "start",
    description = ["Start last created timer."],
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription: ",
    optionListHeading = "%nOptions:%n"
)
@ExperimentalTime
class StartTimerCommand : TimerSubCommand() {
    @ParentCommand
    override lateinit var applicationCommand: TimerApplicationCommand

    override fun run() {
        Timer(properties!!.build()).apply {
            onTick = { JvmTerminalScreen.setTime(duration.toLong() - elapsedTime.toLong()) }
            onStart = { JvmTerminalScreen.setTime(duration.toLong()) }

            start()

            timer = this
        }
    }
}
