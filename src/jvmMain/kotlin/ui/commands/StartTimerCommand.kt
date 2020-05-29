package ui.commands

import Timer
import picocli.CommandLine.*
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
        val screen = DependenciesFactory.getTerminalScreen()
        val newTimer = Timer(properties!!.build()).apply {
            onTick = { screen.setTime(duration.toLong() - elapsedTime.toLong()) }
            onStart = { screen.setTime(duration.toLong()) }
        }
        newTimer.start()
        timer = newTimer
    }
}
