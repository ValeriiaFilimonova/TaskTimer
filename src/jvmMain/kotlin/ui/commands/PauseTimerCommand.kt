package ui.commands

import picocli.CommandLine.Command
import picocli.CommandLine.ParentCommand
import kotlin.time.ExperimentalTime

@Command(
    name = "pause",
    description = ["Pause currently running timer."]
)
@ExperimentalTime
class PauseTimerCommand : TimerSubCommand(){
    @ParentCommand
    override lateinit var applicationCommand: TimerApplicationCommand

    override fun run() {
        timer?.pause()
    }
}
