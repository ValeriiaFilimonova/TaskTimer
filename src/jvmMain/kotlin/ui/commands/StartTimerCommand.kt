package ui.commands

import Timer
import picocli.CommandLine.*
import ui.screen.TimerScreen
import kotlin.time.ExperimentalTime

@Command(
    name = "start",
    description = ["Start last created timer."],
    headerHeading = "%n########################################################################%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription: ",
    optionListHeading = "%nOptions:%n"
)
@ExperimentalTime
class StartTimerCommand : TimerSubCommand() {
    @ParentCommand
    override lateinit var applicationCommand: TimerApplicationCommand

    @Option(
        names = ["-o", "--open"],
        description = ["Timer screen is displayed on start if this option specified."]
    )
    private var displayTimer: Boolean = false

    override fun run() {
        val newTimer = Timer(properties!!.build())

        if (displayTimer) {
            TimerScreen.show()

            newTimer.onStart = {
                TimerScreen.display(newTimer.duration.toLong())
            }
            newTimer.onTick = {
                TimerScreen.display((newTimer.duration.toLong() - newTimer.elapsedTime.toLong()))
            }
        }

        newTimer.start()
        timer = newTimer
    }
}
