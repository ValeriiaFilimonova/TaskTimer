package ui.commands

import picocli.CommandLine.Command
import picocli.CommandLine.ParentCommand
import kotlin.time.ExperimentalTime

@Command(
    name = "stop",
    description = ["Stop currently running timer. This command also closes timer display screen if opened."]
)
@ExperimentalTime
class StopTimerCommand : TimerSubCommand(){
    @ParentCommand
    override lateinit var applicationCommand: TimerApplicationCommand

    override fun run() {
        timer?.stop()
        timer = null
    }
}
