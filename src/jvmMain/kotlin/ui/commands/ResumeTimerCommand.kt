package ui.commands

import picocli.CommandLine.Command
import picocli.CommandLine.ParentCommand
import kotlin.time.ExperimentalTime

@Command(
    name = "resume",
    description = ["Resume currently running timer."]
)
@ExperimentalTime
class ResumeTimerCommand : TimerSubCommand(){
    @ParentCommand
    override lateinit var applicationCommand: TimerApplicationCommand

    override fun run() {
        timer?.resume()
    }
}
