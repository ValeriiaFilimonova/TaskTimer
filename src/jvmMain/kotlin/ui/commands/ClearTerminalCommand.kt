package ui.commands

import picocli.CommandLine.Command
import picocli.CommandLine.ParentCommand
import kotlin.time.ExperimentalTime

// todo add ctrl+l
@Command(
    name = "clear",
    description = ["Clears command input/output area"]
)
@ExperimentalTime
class ClearTerminalCommand : Runnable {
    @ParentCommand
    lateinit var applicationCommand: TimerApplicationCommand

    override fun run() {
        applicationCommand.terminalScreen.clear()
    }
}
