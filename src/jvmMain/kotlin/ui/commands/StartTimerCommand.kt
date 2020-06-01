package ui.commands

import picocli.CommandLine.Command
import picocli.CommandLine.ParentCommand
import kotlin.time.ExperimentalTime

@Command(
    name = "start",
    description = ["Start last created timer."],
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription: ",
    optionListHeading = "%nOptions:%n"
)
@ExperimentalTime
class StartTimerCommand : TimerStartSubCommand() {
    @ParentCommand
    override lateinit var applicationCommand: TimerApplicationCommand

    override fun run() {
        timer = createAndStartNewTimer()
    }
}
