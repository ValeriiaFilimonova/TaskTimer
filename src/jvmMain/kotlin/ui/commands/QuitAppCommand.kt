package ui.commands

import picocli.CommandLine.Command
import picocli.CommandLine.ParentCommand
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

@Command(
    name = "quit",
    description = ["Quit application"]
)
@ExperimentalTime
class QuitAppCommand : Runnable {
    @ParentCommand
    lateinit var applicationCommand: TimerApplicationCommand

    override fun run() {
        applicationCommand.terminalScreen.close()
        exitProcess(0)
    }
}
